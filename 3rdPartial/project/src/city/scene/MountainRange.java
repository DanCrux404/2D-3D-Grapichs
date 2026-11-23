package city.scene;

import engine.core.Point3D;
import engine.shapes.MeshType;
import engine.shapes.ParametricSurface3D;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MountainRange {

    private static final int ROWS = 3;
    private static final int COLUMNS = 20;
    private static final double OUTER_RADIUS = 9000.0;
    private static final double INWARD_SLOPE = 600.0;
    private static final double PEAK_HEIGHT = 1100.0;

    private static final Color LOWER = new Color(126, 158, 132);
    private static final Color UPPER = new Color(158, 176, 168);

    private MountainRange() {}

    public static List<WorldTriangle> triangles() {
        EngineShapeSampler sampler = new EngineShapeSampler();

        ParametricSurface3D surface =
                new ParametricSurface3D(
                        (s, t) -> (OUTER_RADIUS - INWARD_SLOPE * s) * Math.cos(t),
                        (s, t) -> PEAK_HEIGHT
                                * Math.sin(Math.PI * s)
                                * (0.65 + 0.35 * Math.cos(5.0 * t)),
                        (s, t) -> (OUTER_RADIUS - INWARD_SLOPE * s) * Math.sin(t),
                        0.0,
                        1.0,
                        0.0,
                        2.0 * Math.PI,
                        ROWS,
                        COLUMNS,
                        sampler,
                        MeshType.RECTANGULAR);

        Point3D[][] grid = sampler.sampleSurface(surface, ROWS, COLUMNS);
        List<WorldTriangle> geometry = new ArrayList<>();

        for (int row = 0; row < ROWS - 1; row++) {
            Color color = row == 0 ? LOWER : UPPER;

            for (int column = 0; column < COLUMNS - 1; column++) {
                Point3D p1 = grid[row][column];
                Point3D p2 = grid[row][column + 1];
                Point3D p3 = grid[row + 1][column + 1];
                Point3D p4 = grid[row + 1][column];

                geometry.add(
                        new WorldTriangle(p1, p2, p3, color, SurfaceKind.MOUNTAIN, null));
                geometry.add(
                        new WorldTriangle(p1, p3, p4, color, SurfaceKind.MOUNTAIN, null));
            }
        }

        return Collections.unmodifiableList(geometry);
    }
}
