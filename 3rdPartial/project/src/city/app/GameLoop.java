package city.app;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongConsumer;

/**
 * Fixed-rate game loop managed by a single-thread {@link ScheduledExecutorService}.
 *
 * <p>Each tick computes the elapsed time since the previous tick and invokes the supplied frame
 * action, which updates state, renders the complete frame, and presents it. The EDT is never used
 * for update or render work.
 */
public class GameLoop {

    private static final long FRAME_INTERVAL_MS = 16L;
    private static final long MAX_DELTA_MS = 100L;

    private final ScheduledExecutorService executor;
    private final LongConsumer frameAction;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private long lastTickNanos;

    /**
     * Creates a game loop.
     *
     * @param frameAction action that updates, renders, and presents one frame given the elapsed
     *     milliseconds since the previous tick
     */
    public GameLoop(LongConsumer frameAction) {
        if (frameAction == null) {
            throw new IllegalArgumentException("Frame action cannot be null.");
        }

        this.frameAction = frameAction;
        this.executor =
                Executors.newSingleThreadScheduledExecutor(
                        runnable -> {
                            Thread thread = new Thread(runnable, "City-Game-Loop");
                            thread.setDaemon(true);
                            return thread;
                        });
    }

    /** Starts the loop. Repeated calls while running are ignored. */
    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        lastTickNanos = System.nanoTime();
        executor.scheduleAtFixedRate(this::tick, 0, FRAME_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    private void tick() {
        if (!running.get()) {
            return;
        }

        long now = System.nanoTime();
        long deltaMillis = TimeUnit.NANOSECONDS.toMillis(now - lastTickNanos);
        lastTickNanos = now;

        if (deltaMillis < 0) {
            deltaMillis = 0;
        }

        if (deltaMillis > MAX_DELTA_MS) {
            deltaMillis = MAX_DELTA_MS;
        }

        try {
            frameAction.accept(deltaMillis);
        } catch (RuntimeException exception) {
            exception.printStackTrace();
        }
    }

    /** Stops the loop and shuts down the executor, waiting briefly for termination. */
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        executor.shutdown();

        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException exception) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
