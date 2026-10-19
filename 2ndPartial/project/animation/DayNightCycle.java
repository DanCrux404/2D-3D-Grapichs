package project.animation;

import java.awt.Color;

/**
 * Controls the day, sunset, and night cycle.
 * All time values are measured in milliseconds.
 */
public class DayNightCycle {

    public enum Phase {
        MORNING,
        SUNSET,
        NIGHT
    }

    private static final long CYCLE_DURATION_MS = 240_000L;
    private static final long PHASE_DURATION_MS =
        CYCLE_DURATION_MS / 3;

    private long elapsedMillis;

    // Sky colors
    private static final Color MORNING_SKY =
        new Color(145, 198, 225);

    private static final Color GOLDEN_SKY =
        new Color(244, 211, 162);

    private static final Color ORANGE_SKY =
        new Color(232, 177, 155);

    private static final Color PURPLE_SKY =
        new Color(185, 154, 181);

    private static final Color NIGHT_SKY =
        new Color(37, 45, 80);

    public DayNightCycle() {
        elapsedMillis = 0;
    }

    /**
     * Advances the cycle by the specified elapsed time.
     */
    public void update(long deltaMillis) {
        if (deltaMillis < 0) {
            throw new IllegalArgumentException(
                "Elapsed time cannot be negative."
            );
        }

        elapsedMillis =
            (elapsedMillis + deltaMillis % CYCLE_DURATION_MS)
            % CYCLE_DURATION_MS;
    }

    /**
     * Returns the current phase of the cycle.
     */
    public Phase getPhase() {
        if (elapsedMillis < PHASE_DURATION_MS) {
            return Phase.MORNING;
        }

        if (elapsedMillis < 2 * PHASE_DURATION_MS) {
            return Phase.SUNSET;
        }

        return Phase.NIGHT;
    }

    /**
     * Returns progress through the current phase, from 0 to 1.
     */
    public double getPhaseProgress() {
        return (elapsedMillis % PHASE_DURATION_MS)
            / (double) PHASE_DURATION_MS;
    }

    /**
     * Returns the interpolated sky color.
     */
    public Color getSkyColor() {
        long time = elapsedMillis;

        // Morning: keep the sky blue.
        if (time < 80_000L) {
            return MORNING_SKY;
        }

        // Blue to golden yellow.
        if (time < 95_000L) {
            return interpolate(
                MORNING_SKY,
                GOLDEN_SKY,
                progress(time, 80_000L, 95_000L)
            );
        }

        // Golden yellow to nostalgic orange.
        if (time < 120_000L) {
            return interpolate(
                GOLDEN_SKY,
                ORANGE_SKY,
                progress(time, 95_000L, 120_000L)
            );
        }

        // Orange to twilight purple.
        if (time < 140_000L) {
            return interpolate(
                ORANGE_SKY,
                PURPLE_SKY,
                progress(time, 120_000L, 140_000L)
            );
        }

        // Purple to deep night blue.
        if (time < 160_000L) {
            return interpolate(
                PURPLE_SKY,
                NIGHT_SKY,
                progress(time, 140_000L, 160_000L)
            );
        }

        // Peaceful night.
        if (time < 220_000L) {
            return NIGHT_SKY;
        }

        // Night gradually transitions into morning.
        return interpolate(
            NIGHT_SKY,
            MORNING_SKY,
            progress(time, 220_000L, CYCLE_DURATION_MS)
        );
    }

    /**
     * Returns star visibility, from 0 (hidden) to 1 (visible).
     */
    public double getStarVisibility() {
        long time = elapsedMillis;

        // Stars appear gradually during twilight.
        if (time < 140_000L) {
            return 0.0;
        }

        if (time < 160_000L) {
            return smoothstep(
                progress(time, 140_000L, 160_000L)
            );
        }

        // Stars remain visible during the night.
        if (time < 220_000L) {
            return 1.0;
        }

        // Stars fade as morning approaches.
        return 1.0 - smoothstep(
            progress(time, 220_000L, CYCLE_DURATION_MS)
        );
    }

    /**
     * Returns a subtle, smoothly varying brightness for each star.
     */
    public double getStarBrightness(int starIndex) {
        double visibility = getStarVisibility();

        double wave = Math.sin(
            elapsedMillis / 380.0 + starIndex * 1.7
        );

        // Brightness varies gently between 78% and 100%.
        double twinkle = 0.89 + 0.11 * wave;

        return visibility * twinkle;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }

    public long getCycleDurationMillis() {
        return CYCLE_DURATION_MS;
    }

    private static double progress(
        long time,
        long start,
        long end
    ) {
        return Math.max(
            0.0,
            Math.min(
                1.0,
                (time - start) / (double) (end - start)
            )
        );
    }

    /**
     * Smoothstep avoids abrupt changes in interpolation speed.
     */
    private static double smoothstep(double t) {
        return t * t * (3.0 - 2.0 * t);
    }

    /**
     * Linearly interpolates between two RGB colors.
     */
    private static Color interpolate(
        Color start,
        Color end,
        double t
    ) {
        t = smoothstep(t);

        int red = (int) Math.round(
            start.getRed()
                + (end.getRed() - start.getRed()) * t
        );

        int green = (int) Math.round(
            start.getGreen()
                + (end.getGreen() - start.getGreen()) * t
        );

        int blue = (int) Math.round(
            start.getBlue()
                + (end.getBlue() - start.getBlue()) * t
        );

        return new Color(red, green, blue);
    }
}

