package doom.app;

import engine.core.Pixel;
import engine.shapes.Rectangle;
import engine.util.ColorUtils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Game loop executed on the application's single-thread executor.
 *
 * <p>Each frame: poll input, clear the framebuffer with {@link Pixel#put}, update the temporary
 * test animation, render it with engine primitives, publish the frame through
 * {@link FramePresenter#present()}, and pace the loop to 60 frames per second. Statistics are
 * logged approximately every {@value #STATS_INTERVAL_FRAMES} frames.
 *
 * <p>This class runs exclusively on the game-loop thread. It never touches Swing components
 * directly: presentation goes through {@link FramePresenter}, which only calls the thread-safe
 * {@code repaint()}. Interruption during shutdown is expected and restores the interrupt flag;
 * unexpected runtime failures are reported and forwarded to the fatal-error handler for a
 * controlled application shutdown.
 *
 * <p><strong>Temporary infrastructure test only.</strong> The bouncing rectangle exists to
 * validate loop timing, clearing cost, input, and double-buffered presentation. It is not
 * gameplay code and will be replaced in later steps.
 */
public final class GameLoop implements Runnable {

    /** Target frame budget: 1,000,000,000 ns / 60 frames. */
    private static final long TARGET_FRAME_NANOS = 1_000_000_000L / 60L;

    /** Clamp for long frames so a stall cannot cause a huge position jump. */
    private static final double MAX_DT_SECONDS = 0.1;

    /** Temporary statistics cadence, in frames (~5 s at 60 fps). */
    private static final int STATS_INTERVAL_FRAMES = 300;

    /** Temporary rectangle size in pixels. */
    private static final int RECT_SIZE = 48;

    /** Temporary idle drift speed in pixels per second (keeps frames differing without input). */
    private static final double DRIFT_SPEED_X = 180.0;
    private static final double DRIFT_SPEED_Y = 130.0;

    /** Temporary held-key movement speed in pixels per second. */
    private static final double MOVE_SPEED = 320.0;

    /** Approved clear baseline: one {@link Pixel#put} call per pixel. */
    private static final Color BACKGROUND = Color.BLACK;

    /** Static outline so torn or stale frames are easy to spot. */
    private static final Color FRAME_OUTLINE = new Color(64, 64, 64);

    private final FramePresenter presenter;
    private final InputState input;
    private final Runnable shutdownRequest;
    private final Consumer<Throwable> fatalErrorHandler;

    /** Set by any thread to end the loop; checked once per frame. */
    private volatile boolean stopRequested;

    // Animation state; owned exclusively by the game-loop thread.
    private double x;
    private double y;
    private double vx = DRIFT_SPEED_X;
    private double vy = DRIFT_SPEED_Y;
    private double colorPhase;
    private int confirmFlashFrames;

    /**
     * @param presenter double-buffered frame publisher
     * @param input shared keyboard state
     * @param shutdownRequest called (on the loop thread) when ESC requests a clean shutdown
     * @param fatalErrorHandler called (on the loop thread) after an unexpected failure is logged
     */
    public GameLoop(FramePresenter presenter,
                    InputState input,
                    Runnable shutdownRequest,
                    Consumer<Throwable> fatalErrorHandler) {
        this.presenter = presenter;
        this.input = input;
        this.shutdownRequest = shutdownRequest;
        this.fatalErrorHandler = fatalErrorHandler;
    }

    /** Requests a clean stop; safe to call from any thread, idempotent by nature. */
    public void requestStop() {
        stopRequested = true;
    }

    @Override
    public void run() {
        if (stopRequested) {
            // The application was closed before this task started; do not resurrect the loop.
            System.out.println("[GameLoop] stop requested before start — exiting without running");
            return;
        }

        System.out.println("[GameLoop] started on thread '" + Thread.currentThread().getName() + "'");

        long lastFrameNanos = System.nanoTime();
        long statsStartNanos = lastFrameNanos;
        long statsFrames = 0;
        long statsFrameNanos = 0;
        long statsClearNanos = 0;

        try {
            while (!stopRequested) {
                long frameStart = System.nanoTime();

                double dt = (frameStart - lastFrameNanos) / 1_000_000_000.0;
                if (dt < 0.0) {
                    dt = 0.0;
                } else if (dt > MAX_DT_SECONDS) {
                    dt = MAX_DT_SECONDS;
                }
                lastFrameNanos = frameStart;

                InputState.Snapshot keys = input.poll();
                BufferedImage back = presenter.backBuffer();

                long clearStart = System.nanoTime();
                clearFrame(back);
                long clearNanos = System.nanoTime() - clearStart;

                update(dt, keys, back.getWidth(), back.getHeight());
                render(back);
                presenter.present();

                long frameNanos = System.nanoTime() - frameStart;
                statsFrames++;
                statsFrameNanos += frameNanos;
                statsClearNanos += clearNanos;

                if (statsFrames >= STATS_INTERVAL_FRAMES) {
                    logStats(frameStart - statsStartNanos, statsFrames,
                            statsFrameNanos, statsClearNanos);
                    statsStartNanos = frameStart;
                    statsFrames = 0;
                    statsFrameNanos = 0;
                    statsClearNanos = 0;
                }

                pace(frameStart);
            }
        } catch (InterruptedException interrupted) {
            // Expected during shutdown (shutdownNow interrupts the pacing sleep).
            Thread.currentThread().interrupt();
            System.out.println("[GameLoop] interrupted — treating as stop request");
        } catch (RuntimeException failure) {
            // Unexpected game-loop failure: report it, stop, and request controlled shutdown.
            // Errors (e.g. OutOfMemoryError) are not caught here; they surface through the
            // submitted Future, which GameApplication checks at shutdown.
            stopRequested = true;
            System.err.println("[GameLoop] unexpected failure on thread '"
                    + Thread.currentThread().getName() + "':");
            failure.printStackTrace(System.err);
            fatalErrorHandler.accept(failure);
        } finally {
            stopRequested = true;
            System.out.println("[GameLoop] stopped");
        }
    }

    /** Updates the temporary animation; game-loop thread only. */
    private void update(double dt, InputState.Snapshot keys, int width, int height) {
        double moveX = 0.0;
        double moveY = 0.0;
        if (keys.isDown(InputState.Key.LEFT)) {
            moveX -= 1.0;
        }
        if (keys.isDown(InputState.Key.RIGHT)) {
            moveX += 1.0;
        }
        if (keys.isDown(InputState.Key.UP)) {
            moveY -= 1.0;
        }
        if (keys.isDown(InputState.Key.DOWN)) {
            moveY += 1.0;
        }

        if (moveX != 0.0 || moveY != 0.0) {
            // Continuous held-key movement; diagonal input is normalized.
            double length = Math.hypot(moveX, moveY);
            x += (moveX / length) * MOVE_SPEED * dt;
            y += (moveY / length) * MOVE_SPEED * dt;
        } else {
            // Idle drift so consecutive frames differ even without input (stress test).
            x += vx * dt;
            y += vy * dt;
        }

        double maxX = width - RECT_SIZE;
        double maxY = height - RECT_SIZE;
        if (x <= 0.0) {
            x = 0.0;
            vx = Math.abs(vx);
        } else if (x >= maxX) {
            x = maxX;
            vx = -Math.abs(vx);
        }
        if (y <= 0.0) {
            y = 0.0;
            vy = Math.abs(vy);
        } else if (y >= maxY) {
            y = maxY;
            vy = -Math.abs(vy);
        }

        // Continuous colour cycling through the existing engine helper (4 s per cycle).
        colorPhase += dt * 0.25;
        if (colorPhase >= 1.0) {
            colorPhase -= 1.0;
        }

        applyOneTimeActions(keys, width, height);

        if (confirmFlashFrames > 0) {
            confirmFlashFrames--;
        }
    }

    /** One-shot input reactions: ENTER re-centres with a flash, ESC requests shutdown. */
    private void applyOneTimeActions(InputState.Snapshot keys, int width, int height) {
        if (keys.wasPressed(InputState.Key.CONFIRM)) {
            x = (width - RECT_SIZE) / 2.0;
            y = (height - RECT_SIZE) / 2.0;
            confirmFlashFrames = 15;
            System.out.println("[GameLoop] ENTER test action — rectangle recentered at ("
                    + (int) x + ", " + (int) y + ")");
        }
        if (keys.wasPressed(InputState.Key.BACK)) {
            System.out.println("[GameLoop] ESC pressed — requesting clean shutdown");
            stopRequested = true;
            shutdownRequest.run();
        }
    }

    /** Draws the temporary animation into the back buffer; game-loop thread only. */
    private void render(BufferedImage back) {
        int ix = (int) Math.round(x);
        int iy = (int) Math.round(y);

        Color color = confirmFlashFrames > 0
                ? Color.WHITE
                : ColorUtils.rainbow(colorPhase);

        Rectangle.draw(back, ix, iy, ix + RECT_SIZE - 1, iy + RECT_SIZE - 1, color);

        // Static outline: any torn, stale, or uncleared frame shows up immediately.
        Rectangle.draw(back, 2, 2, back.getWidth() - 3, back.getHeight() - 3, FRAME_OUTLINE);
    }

    /**
     * Clears the framebuffer with the approved baseline of one {@link Pixel#put} per pixel.
     *
     * <p>No bulk {@code setRGB}, {@code DataBufferInt}, {@code Arrays.fill}, or Java2D drawing
     * may be introduced here without separate explicit approval.
     */
    private void clearFrame(BufferedImage back) {
        int width = back.getWidth();
        int height = back.getHeight();
        for (int py = 0; py < height; py++) {
            for (int px = 0; px < width; px++) {
                Pixel.put(back, px, py, BACKGROUND);
            }
        }
    }

    /** Logs temporary performance statistics for the previous interval of frames. */
    private void logStats(long elapsedNanos, long frames, long frameNanos, long clearNanos) {
        double seconds = elapsedNanos / 1_000_000_000.0;
        double fps = frames / seconds;
        double avgFrameMs = frameNanos / (double) frames / 1_000_000.0;
        double avgClearMs = clearNanos / (double) frames / 1_000_000.0;
        Runtime runtime = Runtime.getRuntime();
        long heapUsedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        System.out.printf(
                "[stats] frames=%d fps=%.1f avgFrameMs=%.2f avgClearMs=%.2f pos=(%.0f,%.0f) heapMB=%d%n",
                frames, fps, avgFrameMs, avgClearMs, x, y, heapUsedMb);
    }

    /** Sleeps out the remainder of the frame budget; preserves interruption semantics. */
    private void pace(long frameStartNanos) throws InterruptedException {
        long elapsed = System.nanoTime() - frameStartNanos;
        long remaining = TARGET_FRAME_NANOS - elapsed;
        if (remaining > 0L) {
            TimeUnit.NANOSECONDS.sleep(remaining);
        }
    }
}
