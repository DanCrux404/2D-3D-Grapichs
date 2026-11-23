package city.scene;

import engine.core.Point3D;

import java.util.ArrayList;
import java.util.List;

public final class StarField {

    public record Star(Point3D position, double phase, double size) {}

    public static final double DOME_RADIUS = 9000.0;

    private static final int STAR_COUNT = 81;
    private static final double GOLDEN_ANGLE = 2.399963229728653;
    private static final double ELEVATION_STEP = 0.7548776662466927;
    private static final double PHASE_STEP = 0.6180339887498949;
    private static final double SIZE_STEP = 0.4142135623730950;
    private static final double MIN_HEIGHT = 0.06;
    private static final double MAX_HEIGHT = 0.96;

    private static final List<Star> STARS = buildStars();

    private StarField() {}

    public static List<Star> stars() {
        return STARS;
    }

    private static List<Star> buildStars() {
        List<Star> stars = new ArrayList<>();

        for (int i = 0; i < STAR_COUNT; i++) {
            double sample = i + 0.5;
            double azimuth = i * GOLDEN_ANGLE + 0.35;
            double height = MIN_HEIGHT + (MAX_HEIGHT - MIN_HEIGHT)
                    * fraction(sample * ELEVATION_STEP + 0.19);
            double elevation = Math.asin(height);
            double phase = 2.0 * Math.PI * fraction(sample * PHASE_STEP + 0.31);
            double size = 38.0 + 14.0 * fraction(sample * SIZE_STEP + 0.07);

            stars.add(onDome(azimuth, elevation, phase, size));
        }

        return List.copyOf(stars);
    }

    private static double fraction(double value) {
        return value - Math.floor(value);
    }

    private static Star onDome(double azimuth, double elevation, double phase, double size) {
        double horizontal = DOME_RADIUS * Math.cos(elevation);

        return new Star(
                new Point3D(
                        horizontal * Math.sin(azimuth),
                        DOME_RADIUS * Math.sin(elevation),
                        horizontal * Math.cos(azimuth)),
                phase,
                size);
    }
}
