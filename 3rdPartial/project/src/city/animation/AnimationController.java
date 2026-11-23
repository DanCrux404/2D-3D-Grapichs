package city.animation;

import city.scene.CloudField;
import city.scene.StarField;
import city.scene.SunMoonOrbit;

import engine.core.Point3D;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class AnimationController {

    private final DayNightCycle cycle = new DayNightCycle();
    private final SunMoonOrbit orbit = new SunMoonOrbit();
    private final AtomicReference<CycleSnapshot> cycleSnapshot;
    private final AtomicReference<AnimationSnapshot> animationSnapshot;
    private final AtomicBoolean tickPending = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicReference<Throwable> lastError = new AtomicReference<>();
    private final ThreadPoolExecutor executor;

    public AnimationController() {
        cycleSnapshot = new AtomicReference<>(cycle.snapshot());
        animationSnapshot = new AtomicReference<>(initialSnapshot());

        ThreadFactory factory = runnable -> {
            Thread thread = new Thread(runnable, "city-animation");
            thread.setDaemon(true);
            return thread;
        };

        executor = new ThreadPoolExecutor(
                3,
                3,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(3),
                factory,
                new ThreadPoolExecutor.AbortPolicy());

        executor.prestartAllCoreThreads();
    }

    public void update(long deltaMillis) {
        if (!running.get()) {
            return;
        }

        cycle.update(deltaMillis);
        cycleSnapshot.set(cycle.snapshot());
        requestTick();
    }

    public CycleSnapshot cycleSnapshot() {
        return cycleSnapshot.get();
    }

    public AnimationSnapshot snapshot() {
        return animationSnapshot.get();
    }

    public Throwable lastError() {
        return lastError.get();
    }

    public ThreadPoolExecutor executor() {
        return executor;
    }

    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        executor.shutdown();

        try {
            if (!executor.awaitTermination(2L, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

    private AnimationSnapshot initialSnapshot() {
        long elapsedMillis = cycle.snapshot().elapsedMillis();

        return new AnimationSnapshot(
                elapsedMillis,
                orbitSnapshot(elapsedMillis),
                cloudSnapshot(elapsedMillis),
                starSnapshot(elapsedMillis));
    }

    private void requestTick() {
        if (!running.get() || !tickPending.compareAndSet(false, true)) {
            return;
        }

        long elapsedMillis = cycleSnapshot.get().elapsedMillis();

        try {
            CompletableFuture<AnimationSnapshot.OrbitSnapshot> orbitFuture =
                    CompletableFuture.supplyAsync(new OrbitTask(elapsedMillis), executor);
            CompletableFuture<AnimationSnapshot.CloudSnapshot> cloudFuture =
                    CompletableFuture.supplyAsync(new CloudTask(elapsedMillis), executor);
            CompletableFuture<AnimationSnapshot.StarSnapshot> starFuture =
                    CompletableFuture.supplyAsync(new StarTask(elapsedMillis), executor);

            CompletableFuture.allOf(orbitFuture, cloudFuture, starFuture).thenAccept(ignored -> {
                try {
                    AnimationSnapshot next =
                            new AnimationSnapshot(
                                    elapsedMillis,
                                    orbitFuture.join(),
                                    cloudFuture.join(),
                                    starFuture.join());
                    tickPending.set(false);
                    animationSnapshot.set(next);
                } catch (CompletionException exception) {
                    lastError.compareAndSet(null, exception.getCause());
                    tickPending.set(false);
                }
            });
        } catch (RejectedExecutionException exception) {
            tickPending.set(false);
        }
    }

    private AnimationSnapshot.OrbitSnapshot orbitSnapshot(long elapsedMillis) {
        return new AnimationSnapshot.OrbitSnapshot(
                orbit.sunAt(elapsedMillis), orbit.moonAt(elapsedMillis));
    }

    private AnimationSnapshot.CloudSnapshot cloudSnapshot(long elapsedMillis) {
        List<Point3D> positions = new ArrayList<>();

        for (CloudField.Base base : CloudField.bases()) {
            double x = CloudField.wrapX(base.x() + base.speed() * elapsedMillis / 1000.0);
            positions.add(new Point3D(x, base.y(), base.z()));
        }

        return new AnimationSnapshot.CloudSnapshot(positions);
    }

    private AnimationSnapshot.StarSnapshot starSnapshot(long elapsedMillis) {
        List<Double> brightness = new ArrayList<>();

        for (StarField.Star star : StarField.stars()) {
            double wave = Math.sin(elapsedMillis / 700.0 + star.phase());
            brightness.add(0.86 + 0.14 * wave);
        }

        return new AnimationSnapshot.StarSnapshot(brightness);
    }

    private final class OrbitTask implements Supplier<AnimationSnapshot.OrbitSnapshot> {

        private final long elapsedMillis;

        private OrbitTask(long elapsedMillis) {
            this.elapsedMillis = elapsedMillis;
        }

        @Override
        public AnimationSnapshot.OrbitSnapshot get() {
            return orbitSnapshot(elapsedMillis);
        }
    }

    private final class CloudTask implements Supplier<AnimationSnapshot.CloudSnapshot> {

        private final long elapsedMillis;

        private CloudTask(long elapsedMillis) {
            this.elapsedMillis = elapsedMillis;
        }

        @Override
        public AnimationSnapshot.CloudSnapshot get() {
            return cloudSnapshot(elapsedMillis);
        }
    }

    private final class StarTask implements Supplier<AnimationSnapshot.StarSnapshot> {

        private final long elapsedMillis;

        private StarTask(long elapsedMillis) {
            this.elapsedMillis = elapsedMillis;
        }

        @Override
        public AnimationSnapshot.StarSnapshot get() {
            return starSnapshot(elapsedMillis);
        }
    }
}
