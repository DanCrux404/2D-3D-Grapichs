package city.scene;

import engine.core.Point3D;
import engine.lighting.Material;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class StreetGrid {

    private static final double[] LINES = {-1000, -500, 0, 500, 1000};
    private static final double[] BREAKPOINTS = {-1500, -1000, -500, 0, 500, 1000, 1500};
    private static final double[] SIDEWALK_BREAKPOINTS = {-1500, -500, 500, 1500};
    private static final double ROAD_HALF = 35.0;
    private static final double SIDEWALK_WIDTH = 12.0;
    private static final double HORIZONTAL_Y = 0.5;
    private static final double VERTICAL_Y = 0.6;

    private static final Color ROAD = new Color(146, 143, 132);
    private static final Color SIDEWALK = new Color(186, 181, 166);

    private static final DynamicMaterial ROAD_MATERIAL =
            new DynamicMaterial(ROAD, 0.56, 0.95, 0.05, 12.0);
    private static final DynamicMaterial SIDEWALK_MATERIAL =
            new DynamicMaterial(SIDEWALK, 0.62, 0.94, 0.04, 10.0);

    private StreetGrid() {}

    public static void setDaylightFactor(double factor) {
        ROAD_MATERIAL.setDaylightFactor(factor);
        SIDEWALK_MATERIAL.setDaylightFactor(factor);
    }

    public static List<WorldTriangle> triangles() {
        List<WorldTriangle> geometry = new ArrayList<>();

        for (double line : LINES) {
            for (int i = 0; i < BREAKPOINTS.length - 1; i++) {
                double from = BREAKPOINTS[i];
                double to = BREAKPOINTS[i + 1];

                addBand(geometry, from, to, line - ROAD_HALF, line + ROAD_HALF,
                        HORIZONTAL_Y, ROAD, ROAD_MATERIAL);
                addBand(geometry, line - ROAD_HALF, line + ROAD_HALF, from, to,
                        VERTICAL_Y, ROAD, ROAD_MATERIAL);
            }

            if (line == 0) {
                for (int i = 0; i < SIDEWALK_BREAKPOINTS.length - 1; i++) {
                    double from = SIDEWALK_BREAKPOINTS[i];
                    double to = SIDEWALK_BREAKPOINTS[i + 1];

                    addBand(geometry, from, to, line - ROAD_HALF - SIDEWALK_WIDTH,
                            line - ROAD_HALF, HORIZONTAL_Y, SIDEWALK, SIDEWALK_MATERIAL);
                    addBand(geometry, from, to, line + ROAD_HALF,
                            line + ROAD_HALF + SIDEWALK_WIDTH, HORIZONTAL_Y, SIDEWALK,
                            SIDEWALK_MATERIAL);
                    addBand(geometry, line - ROAD_HALF - SIDEWALK_WIDTH, line - ROAD_HALF,
                            from, to, VERTICAL_Y, SIDEWALK, SIDEWALK_MATERIAL);
                    addBand(geometry, line + ROAD_HALF, line + ROAD_HALF + SIDEWALK_WIDTH,
                            from, to, VERTICAL_Y, SIDEWALK, SIDEWALK_MATERIAL);
                }
            }
        }

        return Collections.unmodifiableList(geometry);
    }

    public static boolean isInsideBlock(
            double x, double z, double halfX, double halfZ) {

        for (double line : LINES) {
            if (overlaps(x - halfX, x + halfX, line - ROAD_HALF, line + ROAD_HALF)) {
                return false;
            }

            if (overlaps(z - halfZ, z + halfZ, line - ROAD_HALF, line + ROAD_HALF)) {
                return false;
            }

            if (line == 0.0) {
                double sidewalkLow = line - ROAD_HALF - SIDEWALK_WIDTH;
                double sidewalkHigh = line + ROAD_HALF + SIDEWALK_WIDTH;

                if (overlaps(x - halfX, x + halfX, sidewalkLow, sidewalkHigh)
                        || overlaps(z - halfZ, z + halfZ, sidewalkLow, sidewalkHigh)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean overlaps(
            double minA, double maxA, double minB, double maxB) {

        return maxA > minB && minA < maxB;
    }

    private static void addBand(
            List<WorldTriangle> geometry,
            double x0,
            double x1,
            double z0,
            double z1,
            double y,
            Color color,
            Material material) {

        addQuad(
                geometry,
                new Point3D(x0, y, z0),
                new Point3D(x0, y, z1),
                new Point3D(x1, y, z1),
                new Point3D(x1, y, z0),
                color,
                material);
    }

    private static void addQuad(
            List<WorldTriangle> geometry,
            Point3D p1,
            Point3D p2,
            Point3D p3,
            Point3D p4,
            Color color,
            Material material) {

        geometry.add(new WorldTriangle(p1, p2, p3, color, SurfaceKind.STREET, material));
        geometry.add(new WorldTriangle(p1, p3, p4, color, SurfaceKind.STREET, material));
    }
}
