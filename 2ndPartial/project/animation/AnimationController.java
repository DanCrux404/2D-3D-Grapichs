package project.animation;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongConsumer;

import javax.swing.SwingUtilities;

/**
 * Schedules animation updates and keeps rendering on Swing's EDT.
 */
public class AnimationController {

    private static final long FRAME_INTERVAL_MS = 16L;

    private final ScheduledExecutorService executor;
    private final AtomicBoolean framePending;
    private final LongConsumer updateAction;


    private volatile boolean running;
    private long lastUpdateNanos;

    /**
     * @param updateAction action that updates and renders one frame
     */
    public AnimationController(LongConsumer updateAction) {
        if (updateAction == null) {
            throw new IllegalArgumentException(
                "Update action cannot be null."
            );
        }

        this.updateAction = updateAction;
        this.framePending = new AtomicBoolean(false);

        this.executor = Executors.newSingleThreadScheduledExecutor(
            runnable -> {
                Thread thread = new Thread(
                    runnable,
                    "City-Animation-Scheduler"
                );

                thread.setDaemon(true);
                return thread;
            }
        );
    }

    /**
     * Starts the animation scheduler.
     */
    public void start() {
        if (running) {
            return;
        }

        running = true;
        lastUpdateNanos = System.nanoTime();

        executor.scheduleAtFixedRate(
            this::scheduleFrame,
            0,
            FRAME_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        );
    }

    /**
     * Schedules a frame on Swing's Event Dispatch Thread.
     */
    private void scheduleFrame() {
        if (!running || !framePending.compareAndSet(false, true)) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                if (!running) {
                    return;
                }

                long now = System.nanoTime();

                long deltaMillis = TimeUnit.NANOSECONDS.toMillis(
                    now - lastUpdateNanos
                );

                lastUpdateNanos = now;

                // The supplied action updates and renders the scene.
                updateAction.accept(deltaMillis);

            } finally {
                framePending.set(false);
            }
        });
    }

    /**
     * Stops the animation scheduler.
     */
    public void stop() {
        running = false;
        executor.shutdownNow();
    }
}

