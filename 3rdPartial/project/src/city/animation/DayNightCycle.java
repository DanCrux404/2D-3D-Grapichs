package city.animation;

import java.awt.Color;

public class DayNightCycle {

    public enum Phase {
        MORNING,
        AFTERNOON,
        SUNSET,
        NIGHT
    }

    private static final long CYCLE_DURATION_MS = 360_000L;

    private static final Color MORNING_SKY = new Color(145, 198, 225);
    private static final Color GOLDEN_SKY = new Color(244, 211, 162);
    private static final Color ORANGE_SKY = new Color(232, 177, 155);
    private static final Color PURPLE_SKY = new Color(185, 154, 181);
    private static final Color NIGHT_SKY = new Color(37, 45, 80);

    private long elapsedMillis;

    public DayNightCycle() {
        elapsedMillis = 0L;
    }

    public void update(long deltaMillis) {
        if (deltaMillis < 0) {
            throw new IllegalArgumentException("Elapsed time cannot be negative.");
        }

        elapsedMillis = (elapsedMillis + deltaMillis % CYCLE_DURATION_MS) % CYCLE_DURATION_MS;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }

    public long getCycleDurationMillis() {
        return CYCLE_DURATION_MS;
    }

    public Phase getPhase() {
        if (elapsedMillis < 90_000L) {
            return Phase.MORNING;
        }

        if (elapsedMillis < 165_000L) {
            return Phase.AFTERNOON;
        }

        if (elapsedMillis < 240_000L) {
            return Phase.SUNSET;
        }

        return Phase.NIGHT;
    }

    public Color getSkyColor() {
        long time = elapsedMillis;

        if (time < 80_000L) {
            return MORNING_SKY;
        }

        if (time < 120_000L) {
            return interpolate(MORNING_SKY, GOLDEN_SKY, progress(time, 80_000L, 120_000L));
        }

        if (time < 165_000L) {
            return interpolate(GOLDEN_SKY, ORANGE_SKY, progress(time, 120_000L, 165_000L));
        }

        if (time < 205_000L) {
            return interpolate(ORANGE_SKY, PURPLE_SKY, progress(time, 165_000L, 205_000L));
        }

        if (time < 240_000L) {
            return interpolate(PURPLE_SKY, NIGHT_SKY, progress(time, 205_000L, 240_000L));
        }

        if (time < 315_000L) {
            return NIGHT_SKY;
        }

        return interpolate(NIGHT_SKY, MORNING_SKY, progress(time, 315_000L, CYCLE_DURATION_MS));
    }

    public double getWindowIntensity() {
        long time = elapsedMillis;

        if (time < 130_000L) {
            return 0.0;
        }

        if (time < 220_000L) {
            return smoothstep(progress(time, 130_000L, 220_000L));
        }

        if (time < 315_000L) {
            return 1.0;
        }

        return 1.0 - smoothstep(progress(time, 315_000L, CYCLE_DURATION_MS));
    }

    public double getStarVisibility() {
        long time = elapsedMillis;

        if (time < 205_000L) {
            return 0.0;
        }

        if (time < 240_000L) {
            return smoothstep(progress(time, 205_000L, 240_000L));
        }

        if (time < 315_000L) {
            return 1.0;
        }

        return 1.0 - smoothstep(progress(time, 315_000L, CYCLE_DURATION_MS));
    }

    public double getAmbientFactor() {
        long time = elapsedMillis;

        if (time < 165_000L) {
            return 1.0;
        }

        if (time < 240_000L) {
            return 1.0 - 0.65 * smoothstep(progress(time, 165_000L, 240_000L));
        }

        if (time < 315_000L) {
            return 0.35;
        }

        return 0.35 + 0.65 * smoothstep(progress(time, 315_000L, CYCLE_DURATION_MS));
    }

    public CycleSnapshot snapshot() {
        return new CycleSnapshot(
                getSkyColor(),
                getPhase(),
                getWindowIntensity(),
                getStarVisibility(),
                getAmbientFactor(),
                elapsedMillis);
    }

    private static double progress(long time, long start, long end) {
        return Math.max(0.0, Math.min(1.0, (time - start) / (double) (end - start)));
    }

    private static double smoothstep(double t) {
        return t * t * (3.0 - 2.0 * t);
    }

    private static Color interpolate(Color start, Color end, double t) {
        t = smoothstep(t);

        int red = (int) Math.round(start.getRed() + (end.getRed() - start.getRed()) * t);
        int green = (int) Math.round(start.getGreen() + (end.getGreen() - start.getGreen()) * t);
        int blue = (int) Math.round(start.getBlue() + (end.getBlue() - start.getBlue()) * t);

        return new Color(red, green, blue);
    }
}
