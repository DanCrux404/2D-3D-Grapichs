package doom.app;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import javax.swing.JPanel;

/**
 * Application-level double buffering for the game window.
 *
 * <p>Owns two framebuffers: the back buffer that the game loop renders into and the published
 * buffer that the Event Dispatch Thread presents with {@code g.drawImage}. The buffer references
 * are exchanged under a private lock inside {@link #present()}, which is called from the
 * game-loop thread only. {@link #paintPublished(Graphics, ImageObserver)} acquires the same lock, so the
 * EDT can never read a buffer while the loop is writing it, and the loop can never render into
 * the buffer that is currently being presented.
 *
 * <p>This class only moves completed images between threads. Pixel content is produced
 * exclusively by the game layer through {@code engine.core.Pixel} and the engine's shape
 * routines.
 */
public final class FramePresenter {

    private final BufferedImage bufferA;
    private final BufferedImage bufferB;
    private final JPanel panel;

    /** Guards every read and write of the {@link #rendering} / {@link #published} references. */
    private final Object lock = new Object();

    /** Buffer the game loop currently renders into; only ever touched by the loop thread. */
    private BufferedImage rendering;

    /** Buffer currently presented by the EDT; guarded by {@link #lock}. */
    private BufferedImage published;

    /** Creates two empty {@code TYPE_INT_RGB} buffers of the given size. */
    public FramePresenter(int width, int height, JPanel panel) {
        bufferA = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        bufferB = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        this.rendering = bufferA;
        this.published = bufferB;
        this.panel = panel;
    }

    /**
     * Returns the back buffer for rendering.
     *
     * <p>Game-loop thread only. The reference is exchanged exclusively inside {@link #present()}
     * on this same thread, so no synchronization is required for this call.
     */
    public BufferedImage backBuffer() {
        return rendering;
    }

    /**
     * Publishes the completed back buffer and requests presentation.
     *
     * <p>Game-loop thread only. Exchanges the two buffers under the lock, then calls
     * {@code repaint()} outside the lock so the EDT is never held up by frame swapping.
     */
    public void present() {
        synchronized (lock) {
            if (published == rendering) {
                // Unreachable by construction: the references always differ after a swap.
                System.err.println("[FramePresenter] invariant violation: published == back buffer");
            }
            BufferedImage completed = rendering;
            rendering = published;
            published = completed;
        }
        // Swing documents repaint() as safe to call from any thread.
        panel.repaint();
    }

    /**
     * Draws the published buffer; called by the panel's {@code paintComponent} on the EDT.
     *
     * <p>Holds the lock for the draw so a concurrent {@link #present()} cannot swap the
     * reference mid-blit.
     */
    public void paintPublished(Graphics g, ImageObserver observer) {
        synchronized (lock) {
            if (published == rendering) {
                System.err.println("[FramePresenter] invariant violation: published == back buffer");
            }
            g.drawImage(published, 0, 0, observer);
        }
    }
}
