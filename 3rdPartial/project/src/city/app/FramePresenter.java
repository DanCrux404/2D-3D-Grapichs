package city.app;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * Presents completed frames with double buffering.
 *
 * <p>The render thread exclusively owns the back buffer. When a frame is complete, {@link
 * #present()} swaps the back and front buffers under a lock and requests a repaint. The Swing EDT
 * only reads the front buffer inside {@link #paint(Graphics)} while holding the same lock, so it
 * never observes a partially rendered frame and the render thread never writes to a buffer being
 * presented.
 */
public class FramePresenter {

    private final int width;
    private final int height;
    private final Object lock = new Object();
    private final Runnable repaintAction;
    private final int[] clearPixels;

    private BufferedImage back;
    private BufferedImage front;

    /**
     * Creates a presenter with two image buffers.
     *
     * @param width frame width in pixels
     * @param height frame height in pixels
     * @param repaintAction thread-safe action that schedules an EDT repaint
     */
    public FramePresenter(int width, int height, Runnable repaintAction) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Frame dimensions must be positive.");
        }

        if (repaintAction == null) {
            throw new IllegalArgumentException("Repaint action cannot be null.");
        }

        this.width = width;
        this.height = height;
        this.repaintAction = repaintAction;
        this.clearPixels = new int[width * height];

        back = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        front = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    /**
     * Returns the back buffer. Only the render thread may call this and write to the returned
     * image, and only before {@link #present()} is called for the frame.
     *
     * @return the back buffer owned by the render thread
     */
    public BufferedImage backBuffer() {
        return back;
    }

    /**
     * Clears the back buffer using direct pixel writes (no Java2D drawing calls).
     *
     * @param color clear color applied to every pixel
     */
    public void clearBackBuffer(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("Clear color cannot be null.");
        }

        Arrays.fill(clearPixels, color.getRGB());
        back.setRGB(0, 0, width, height, clearPixels, 0, width);
    }

    /** Swaps the completed back buffer to the front and requests a repaint. */
    public void present() {
        synchronized (lock) {
            BufferedImage completed = back;
            back = front;
            front = completed;
        }

        repaintAction.run();
    }

    /**
     * Draws the current front buffer. Called by the EDT inside {@code paintComponent}.
     *
     * @param graphics destination graphics context
     */
    public void paint(Graphics graphics) {
        synchronized (lock) {
            graphics.drawImage(front, 0, 0, null);
        }
    }
}
