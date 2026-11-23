package city.scene;

import engine.core.Point3D;
import engine.shapes.ParametricCurve3D;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

public final class SunMoonOrbit {

    private static final double RADIUS = 3200.0;
    private static final double HEIGHT = 2400.0;
    private static final double CENTER_Z = 650.0;
    private static final double BASE_HEIGHT = -150.0;
    private static final double HORIZON_Y = 900.0;
    private static final double ENTRY_ANGLE = 0.46;
    private static final double LIGHT_RADIUS = 900.0;
    private static final double MIN_LIGHT_HEIGHT = 650.0;

    private static final long SUN_SET_END = 210_000L;
    private static final long MOON_END = 330_000L;
    private static final long SUNRISE_END = 360_000L;

    private static final Color SUN_COLOR = new Color(255, 246, 220);
    private static final Color SUNSET_COLOR = new Color(255, 170, 120);
    private static final Color MOON_COLOR = new Color(160, 190, 240);

    private final List<Point3D> samples;

    public record CelestialState(
            Point3D position,
            Color color,
            double intensity,
            boolean sun) {
    }

    public record BodyPose(
            Point3D position, Color color, double intensity, boolean visible) {
    }

    public record LightPose(Point3D position, Color color, double intensity, boolean sun) {
    }

    public SunMoonOrbit() {
        EngineShapeSampler sampler = new EngineShapeSampler();

        ParametricCurve3D curve = new ParametricCurve3D(
                t -> RADIUS * Math.cos(t),
                t -> BASE_HEIGHT + HEIGHT * Math.sin(t),
                t -> CENTER_Z + RADIUS * Math.sin(t),
                0.0,
                2.0 * Math.PI,
                64,
                sampler);

        samples = Collections.unmodifiableList(sampler.sampleCurve(curve));
    }

    public List<Point3D> sampledPoints() {
        return samples;
    }

    public CelestialState stateAt(long elapsedMillis) {
        BodyPose sun = sunAt(elapsedMillis);

        if (sun.visible() || elapsedMillis >= MOON_END) {
            return new CelestialState(
                    sun.position(), sun.color(), sun.intensity(), true);
        }

        BodyPose moon = moonAt(elapsedMillis);

        return new CelestialState(
                moon.position(), moon.color(), moon.intensity(), false);
    }

    public BodyPose sunAt(long elapsedMillis) {
        long time = normalizeElapsed(elapsedMillis);

        if (time < SUN_SET_END) {
            double progress = time / (double) SUN_SET_END;
            double angle = ENTRY_ANGLE
                    + progress * (Math.PI - 2.0 * ENTRY_ANGLE);
            Point3D position = sample(angle);
            Color color = blend(SUN_COLOR, SUNSET_COLOR, sunsetFactor(time));

            return new BodyPose(
                    position,
                    color,
                    sunIntensity(time),
                    aboveHorizon(position));
        }

        if (time < MOON_END) {
            return new BodyPose(samples.get(0), SUN_COLOR, 0.0, false);
        }

        double progress = (time - MOON_END) / (double) (SUNRISE_END - MOON_END);
        double angle = 2.0 * Math.PI - ENTRY_ANGLE
                + progress * 2.0 * ENTRY_ANGLE;
        Point3D position = sampleWrapped(angle);

        return new BodyPose(
                position,
                blend(MOON_COLOR, SUN_COLOR, smoothstep(progress)),
                0.55 + 1.45 * smoothstep(progress),
                aboveHorizon(position));
    }

    public BodyPose moonAt(long elapsedMillis) {
        long time = normalizeElapsed(elapsedMillis);

        if (time < SUN_SET_END || time >= MOON_END) {
            return new BodyPose(samples.get(0), MOON_COLOR, 0.0, false);
        }

        double progress = (time - SUN_SET_END) / (double) (MOON_END - SUN_SET_END);
        double angle = Math.PI + ENTRY_ANGLE
                + progress * (Math.PI - 2.0 * ENTRY_ANGLE);
        Point3D curvePoint = sample(angle);
        Point3D position =
                new Point3D(
                        curvePoint.getX(), -curvePoint.getY(), curvePoint.getZ());

        return new BodyPose(position, MOON_COLOR, 0.55, aboveHorizon(position));
    }

    public LightPose lightAt(long elapsedMillis) {
        long time = normalizeElapsed(elapsedMillis);
        BodyPose sun = sunAt(time);
        BodyPose source = sun.visible() || time >= MOON_END ? sun : moonAt(time);
        Point3D position = effectiveLightPosition(source.position());

        return new LightPose(
                position, source.color(), source.intensity(), source == sun);
    }

    public static BodyPose skyLocked(
            BodyPose pose, double cameraX, double cameraZ) {

        if (!pose.visible()) {
            return pose;
        }

        Point3D position = pose.position();

        return new BodyPose(
                new Point3D(
                        position.getX() + cameraX,
                        position.getY(),
                        position.getZ() + cameraZ),
                pose.color(),
                pose.intensity(),
                true);
    }

    private Point3D effectiveLightPosition(Point3D bodyPosition) {
        double length = Math.sqrt(
                bodyPosition.getX() * bodyPosition.getX()
                        + bodyPosition.getY() * bodyPosition.getY()
                        + bodyPosition.getZ() * bodyPosition.getZ());

        if (length < 1e-12) {
            return new Point3D(0.0, MIN_LIGHT_HEIGHT, 0.0);
        }

        return new Point3D(
                bodyPosition.getX() / length * LIGHT_RADIUS,
                Math.max(
                        MIN_LIGHT_HEIGHT,
                        bodyPosition.getY() / length * LIGHT_RADIUS),
                bodyPosition.getZ() / length * LIGHT_RADIUS);
    }

    private static boolean aboveHorizon(Point3D position) {
        return position.getY() > HORIZON_Y;
    }

    private static double sunIntensity(long elapsedMillis) {
        if (elapsedMillis < SUN_SET_END) {
            double progress = elapsedMillis / (double) SUN_SET_END;
            return 0.6 + 1.9 * Math.sin(Math.PI * progress);
        }

        if (elapsedMillis < MOON_END) {
            return 0.0;
        }

        double progress =
                (elapsedMillis - MOON_END) / (double) (SUNRISE_END - MOON_END);
        return 0.55 + 1.95 * smoothstep(progress);
    }

    private static double sunsetFactor(long elapsedMillis) {
        if (elapsedMillis < 165_000L) {
            return 0.0;
        }

        if (elapsedMillis >= SUN_SET_END) {
            return 1.0;
        }

        return smoothstep((elapsedMillis - 165000.0) / 45000.0);
    }

    private static long normalizeElapsed(long elapsedMillis) {
        long normalized = elapsedMillis % SUNRISE_END;
        return normalized < 0L ? normalized + SUNRISE_END : normalized;
    }

    private Point3D sample(double angle) {
        return sampleWrapped(angle);
    }

    private Point3D sampleWrapped(double angle) {
        double turns = angle / (2.0 * Math.PI);
        double wrapped = ((turns % 1.0) + 1.0) % 1.0;
        double index = wrapped * (samples.size() - 1);
        int low = (int) Math.floor(index);
        int high = (int) Math.ceil(index);

        if (low == high) {
            return samples.get(low);
        }

        double t = index - low;
        Point3D a = samples.get(low);
        Point3D b = samples.get(high);

        return new Point3D(
                a.getX() + (b.getX() - a.getX()) * t,
                a.getY() + (b.getY() - a.getY()) * t,
                a.getZ() + (b.getZ() - a.getZ()) * t);
    }

    private static Color blend(Color start, Color end, double t) {
        int red = (int) Math.round(start.getRed() + (end.getRed() - start.getRed()) * t);
        int green = (int) Math.round(start.getGreen() + (end.getGreen() - start.getGreen()) * t);
        int blue = (int) Math.round(start.getBlue() + (end.getBlue() - start.getBlue()) * t);

        return new Color(red, green, blue);
    }

    private static double smoothstep(double t) {
        double clamped = Math.max(0.0, Math.min(1.0, t));
        return clamped * clamped * (3.0 - 2.0 * clamped);
    }
}
