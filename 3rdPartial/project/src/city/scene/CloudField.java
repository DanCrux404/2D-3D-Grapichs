package city.scene;

import engine.core.Point3D;
import engine.shapes.MeshType;
import engine.shapes.ParametricSurface3D;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public final class CloudField {

    public record Base(double x, double y, double z, double speed) {}

    private static final int ROWS = 3;
    private static final int COLUMNS = 3;
    private static final double WRAP_LIMIT = 3200.0;
    private static final double WRAP_SPAN = 6400.0;

    public static final Color CLOUD_COLOR = new Color(236, 242, 246);

    private static final List<Base> BASES = List.of(
            new Base(-2400.0, 1500.0, 3200.0, 55.0),
            new Base(-800.0, 1750.0, 5000.0, 80.0),
            new Base(1800.0, 1450.0, 6200.0, 40.0),
            new Base(300.0, 1250.0, 250.0, 35.0),
            new Base(-450.0, 1400.0, -300.0, 45.0));

    private CloudField() {}

    public static Color color() {
        return CLOUD_COLOR;
    }

    public static List<Base> bases() {
        return BASES;
    }

    public static double wrapX(double x) {
        double wrapped = x;

        while (wrapped > WRAP_LIMIT) {
            wrapped -= WRAP_SPAN;
        }

        while (wrapped < -WRAP_LIMIT) {
            wrapped += WRAP_SPAN;
        }

        return wrapped;
    }

    public static List<List<Point3D>> localGrid() {
        EngineShapeSampler sampler = new EngineShapeSampler();

        ParametricSurface3D surface =
                new ParametricSurface3D(
                        (u, v) -> 320.0 * u,
                        (u, v) -> 90.0 * (1.0 - u * u) * (1.0 - v * v),
                        (u, v) -> 120.0 * v,
                        -1.0,
                        1.0,
                        -1.0,
                        1.0,
                        ROWS,
                        COLUMNS,
                        sampler,
                        MeshType.RECTANGULAR);

        Point3D[][] sampled = sampler.sampleSurface(surface, ROWS, COLUMNS);
        List<List<Point3D>> grid = new ArrayList<>(ROWS);

        for (Point3D[] row : sampled) {
            grid.add(List.of(row));
        }

        return List.copyOf(grid);
    }
}
