package city.scene;

import engine.core.Point3D;
import engine.shapes.ParametricCurve3D;

import java.util.Collections;
import java.util.List;

public final class CelestialDisc {

    public static final int SEGMENTS = 12;
    public static final double SUN_RADIUS = 180.0;
    public static final double MOON_RADIUS = 135.0;

    private static final List<Point3D> SUN_RING = sample(SUN_RADIUS);
    private static final List<Point3D> MOON_RING = sample(MOON_RADIUS);

    private CelestialDisc() {}

    public static List<Point3D> sunRing() {
        return SUN_RING;
    }

    public static List<Point3D> moonRing() {
        return MOON_RING;
    }

    private static List<Point3D> sample(double radius) {
        EngineShapeSampler sampler = new EngineShapeSampler();

        ParametricCurve3D curve =
                new ParametricCurve3D(
                        t -> radius * Math.cos(t),
                        t -> radius * Math.sin(t),
                        t -> 0.0,
                        0.0,
                        2.0 * Math.PI,
                        SEGMENTS + 1,
                        sampler);

        List<Point3D> samples = sampler.sampleCurve(curve);

        if (samples.size() < SEGMENTS) {
            throw new IllegalStateException("Celestial disc curve sampled too few points.");
        }

        return Collections.unmodifiableList(samples.subList(0, SEGMENTS));
    }
}
