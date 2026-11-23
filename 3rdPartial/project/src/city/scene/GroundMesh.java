package city.scene;

import engine.core.Point3D;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GroundMesh {

    public static final Color GRASS = new Color(112, 162, 100);

    private static final double INNER_HALF = 750.0;
    private static final double MID_HALF = 6000.0;
    private static final double FAR_HALF = 60000.0;

    private static final DynamicMaterial GROUND_MATERIAL =
            new DynamicMaterial(GRASS, 0.60, 0.95, 0.02, 8.0);

    private GroundMesh() {}

    public static void setDaylightFactor(double factor) {
        GROUND_MATERIAL.setDaylightFactor(factor);
    }

    public static List<WorldTriangle> triangles() {
        List<WorldTriangle> geometry = new ArrayList<>();

        double[] inner = {-INNER_HALF, 0.0, INNER_HALF};
        addGrid(geometry, inner, inner);

        double[] middle = {-MID_HALF, -3000.0, -INNER_HALF, INNER_HALF, 3000.0, MID_HALF};

        for (int i = 0; i < middle.length - 1; i++) {
            for (int j = 0; j < middle.length - 1; j++) {
                if (middle[i] == -INNER_HALF && middle[j] == -INNER_HALF) {
                    continue;
                }

                addQuad(
                        geometry,
                        new Point3D(middle[i], 0, middle[j]),
                        new Point3D(middle[i], 0, middle[j + 1]),
                        new Point3D(middle[i + 1], 0, middle[j + 1]),
                        new Point3D(middle[i + 1], 0, middle[j]));
            }
        }

        addQuad(
                geometry,
                new Point3D(-FAR_HALF, 0, -FAR_HALF),
                new Point3D(-FAR_HALF, 0, -MID_HALF),
                new Point3D(FAR_HALF, 0, -MID_HALF),
                new Point3D(FAR_HALF, 0, -FAR_HALF));
        addQuad(
                geometry,
                new Point3D(-FAR_HALF, 0, MID_HALF),
                new Point3D(-FAR_HALF, 0, FAR_HALF),
                new Point3D(FAR_HALF, 0, FAR_HALF),
                new Point3D(FAR_HALF, 0, MID_HALF));
        addQuad(
                geometry,
                new Point3D(-FAR_HALF, 0, -MID_HALF),
                new Point3D(-FAR_HALF, 0, MID_HALF),
                new Point3D(-MID_HALF, 0, MID_HALF),
                new Point3D(-MID_HALF, 0, -MID_HALF));
        addQuad(
                geometry,
                new Point3D(MID_HALF, 0, -MID_HALF),
                new Point3D(MID_HALF, 0, MID_HALF),
                new Point3D(FAR_HALF, 0, MID_HALF),
                new Point3D(FAR_HALF, 0, -MID_HALF));

        return Collections.unmodifiableList(geometry);
    }

    private static void addGrid(List<WorldTriangle> geometry, double[] xs, double[] zs) {
        for (int i = 0; i < xs.length - 1; i++) {
            for (int j = 0; j < zs.length - 1; j++) {
                addQuad(
                        geometry,
                        new Point3D(xs[i], 0, zs[j]),
                        new Point3D(xs[i], 0, zs[j + 1]),
                        new Point3D(xs[i + 1], 0, zs[j + 1]),
                        new Point3D(xs[i + 1], 0, zs[j]));
            }
        }
    }

    private static void addQuad(
            List<WorldTriangle> geometry,
            Point3D p1,
            Point3D p2,
            Point3D p3,
            Point3D p4) {

        geometry.add(
                new WorldTriangle(
                        p1, p2, p3, GRASS, SurfaceKind.GROUND, GROUND_MATERIAL));
        geometry.add(
                new WorldTriangle(
                        p1, p3, p4, GRASS, SurfaceKind.GROUND, GROUND_MATERIAL));
    }
}
