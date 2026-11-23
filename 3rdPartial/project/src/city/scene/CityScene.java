package city.scene;

import engine.core.Point3D;
import engine.lighting.Material;
import engine.transform.Transform3D;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CityScene {

    private static final double MID_HALF = 6000.0;
    private static final Point3D CENTER = new Point3D(0.0, 0.0, 0.0);

    private static final double WINDOW_WIDTH = 0.30;
    private static final double WINDOW_HEIGHT = 0.18;
    private static final double WINDOW_OFFSET = 0.01;

    private final List<WorldTriangle> triangles;
    private final List<WorldTriangle> shadowCasters;
    private final List<Window> windows;
    private final List<DynamicMaterial> materials;

    public CityScene() {
        List<WorldTriangle> geometry = new ArrayList<>();
        List<Window> sceneWindows = new ArrayList<>();
        List<DynamicMaterial> sceneMaterials = new ArrayList<>();

        geometry.addAll(GroundMesh.triangles());
        geometry.addAll(StreetGrid.triangles());
        geometry.addAll(MountainRange.triangles());

        for (BuildingSpec spec : buildings()) {
            if (!StreetGrid.isInsideBlock(
                    spec.x(),
                    spec.z(),
                    spec.footprintHalfX(),
                    spec.footprintHalfZ())) {

                throw new IllegalStateException("Building overlaps a street or sidewalk.");
            }

            addBuilding(geometry, sceneWindows, sceneMaterials, spec);
        }

        triangles = Collections.unmodifiableList(geometry);
        windows = Collections.unmodifiableList(sceneWindows);
        materials = List.copyOf(sceneMaterials);
        setDaylightFactor(1.0);

        List<WorldTriangle> casters = new ArrayList<>();

        for (WorldTriangle triangle : triangles) {
            if (triangle.getKind() == SurfaceKind.BUILDING) {
                casters.add(triangle);
            }
        }

        shadowCasters = Collections.unmodifiableList(casters);
    }

    public List<WorldTriangle> triangles() {
        return triangles;
    }

    public List<WorldTriangle> shadowCasters() {
        return shadowCasters;
    }

    public Point3D focus() {
        return new Point3D(0, 0, 650);
    }

    public Point3D center() {
        return CENTER;
    }

    public double groundHalfSize() {
        return MID_HALF;
    }

    public void setNightIntensity(float intensity) {
        for (Window window : windows) {
            window.setNightIntensity(intensity);
        }
    }

    public void setDaylightFactor(double factor) {
        GroundMesh.setDaylightFactor(factor);
        StreetGrid.setDaylightFactor(factor);

        for (DynamicMaterial material : materials) {
            material.setDaylightFactor(factor);
        }
    }

    public static List<BuildingSpec> buildings() {
        Color terra = new Color(198, 112, 92);
        Color sage = new Color(142, 168, 132);
        Color cream = new Color(226, 210, 178);
        Color blue = new Color(128, 152, 182);
        Color ochre = new Color(206, 174, 112);
        Color rose = new Color(196, 142, 150);
        Color mint = new Color(150, 184, 158);
        Color lavender = new Color(170, 150, 190);

        return List.of(
                building(250, 750, 160, 1200, 150, 0, terra),
                building(-250, 750, 150, 1050, 160, 90, blue),
                building(750, 750, 140, 820, 140, 0, sage),
                building(-750, 750, 150, 700, 130, 0, ochre),
                building(1250, 750, 130, 650, 150, 90, rose),
                building(250, 250, 145, 950, 145, 0, cream),
                building(-750, 250, 140, 620, 150, 90, mint),
                building(750, -250, 150, 760, 140, 0, blue),
                building(-250, -250, 145, 880, 145, 0, terra),
                building(-1250, -250, 130, 580, 150, 90, sage),
                building(250, -750, 150, 720, 140, 0, ochre),
                building(-750, -750, 140, 600, 140, 0, cream),
                building(1250, -750, 145, 700, 150, 90, rose),
                building(250, -1250, 140, 560, 140, 0, mint),
                building(-750, -1250, 150, 640, 130, 0, lavender));
    }

    private static BuildingSpec building(
            double x,
            double z,
            double scaleX,
            double scaleY,
            double scaleZ,
            double rotationY,
            Color wall) {

        int rows = windowRows(scaleY);
        int columns = windowColumns(Math.max(scaleX, scaleZ), rows);

        return new BuildingSpec(
                x, z, scaleX, scaleY, scaleZ, rotationY, wall, columns, rows);
    }

    private static int windowColumns(double widestHalf, int rows) {
        return widestHalf >= 145.0 && rows >= 3 ? 3 : 2;
    }

    private static int windowRows(double height) {
        if (height >= 900.0) {
            return 4;
        }

        if (height >= 650.0) {
            return 3;
        }

        return 2;
    }

    private void addBuilding(
            List<WorldTriangle> geometry,
            List<Window> sceneWindows,
            List<DynamicMaterial> sceneMaterials,
            BuildingSpec spec) {

        double[][] scale = Transform3D.scaling(spec.scaleX(), spec.scaleY(), spec.scaleZ());
        double[][] rotation = Transform3D.rotationY(spec.rotationY());
        double[][] translation = Transform3D.translation(spec.x(), 0, spec.z());

        Color side = spec.wall().darker();
        DynamicMaterial wallMaterial =
                new DynamicMaterial(spec.wall(), 0.58, 0.95, 0.14, 24.0);
        DynamicMaterial sideMaterial =
                new DynamicMaterial(side, 0.58, 0.95, 0.14, 24.0);
        sceneMaterials.add(wallMaterial);
        sceneMaterials.add(sideMaterial);

        List<WorldTriangle> prism = new ArrayList<>();
        addHollowPrism(prism, 0, 0, 1, 1, spec.wall(), side, wallMaterial, sideMaterial);

        for (WorldTriangle triangle : prism) {
            geometry.add(transformTriangle(triangle, scale, rotation, translation));
        }

        List<Window> localWindows = new ArrayList<>();
        addWindows(localWindows, 0, 0, 1, 1, spec.windowColumns(), spec.windowRows());

        for (Window window : localWindows) {
            WorldTriangle first = window.triangles().get(0);
            WorldTriangle second = window.triangles().get(1);

            Point3D p1 = transform(first.getA(), scale, rotation, translation);
            Point3D p2 = transform(first.getB(), scale, rotation, translation);
            Point3D p3 = transform(first.getC(), scale, rotation, translation);
            Point3D p4 = transform(second.getC(), scale, rotation, translation);

            Window transformed = new Window(p1, p2, p3, p4);
            sceneWindows.add(transformed);
            geometry.addAll(transformed.triangles());
        }
    }

    private static WorldTriangle transformTriangle(
            WorldTriangle triangle,
            double[][] scale,
            double[][] rotation,
            double[][] translation) {

        return new WorldTriangle(
                transform(triangle.getA(), scale, rotation, translation),
                transform(triangle.getB(), scale, rotation, translation),
                transform(triangle.getC(), scale, rotation, translation),
                triangle.getColor(),
                triangle.getKind(),
                triangle.getMaterial());
    }

    private static Point3D transform(
            Point3D point,
            double[][] scale,
            double[][] rotation,
            double[][] translation) {

        Point3D transformed = Transform3D.apply(point, scale);
        transformed = Transform3D.apply(transformed, rotation);
        return Transform3D.apply(transformed, translation);
    }

    private void addHollowPrism(
            List<WorldTriangle> geometry,
            double centerX,
            double centerZ,
            double halfSize,
            double height,
            Color color,
            Color side,
            Material wallMaterial,
            Material sideMaterial) {

        double left = centerX - halfSize;
        double right = centerX + halfSize;
        double front = centerZ - halfSize;
        double back = centerZ + halfSize;

        addQuad(
                geometry,
                new Point3D(left, 0, front),
                new Point3D(left, height, front),
                new Point3D(right, height, front),
                new Point3D(right, 0, front),
                color,
                wallMaterial);

        addQuad(
                geometry,
                new Point3D(right, 0, back),
                new Point3D(right, height, back),
                new Point3D(left, height, back),
                new Point3D(left, 0, back),
                color,
                wallMaterial);

        addQuad(
                geometry,
                new Point3D(left, 0, back),
                new Point3D(left, height, back),
                new Point3D(left, height, front),
                new Point3D(left, 0, front),
                side,
                sideMaterial);

        addQuad(
                geometry,
                new Point3D(right, 0, front),
                new Point3D(right, height, front),
                new Point3D(right, height, back),
                new Point3D(right, 0, back),
                side,
                sideMaterial);
    }

    private void addWindows(
            List<Window> windows,
            double centerX,
            double centerZ,
            double halfSize,
            double height,
            int columns,
            int rows) {

        double left = centerX - halfSize;
        double right = centerX + halfSize;
        double front = centerZ - halfSize;
        double back = centerZ + halfSize;

        double marginX = (2 * halfSize - columns * WINDOW_WIDTH) / (columns + 1);
        double marginY = (height - rows * WINDOW_HEIGHT) / (rows + 1);

        addWindowGrid(windows, left, 0, front - WINDOW_OFFSET,
                1, 0, 0, 0, 1, 0, marginX, marginY, columns, rows);
        addWindowGrid(windows, right, 0, back + WINDOW_OFFSET,
                -1, 0, 0, 0, 1, 0, marginX, marginY, columns, rows);
        addWindowGrid(windows, left - WINDOW_OFFSET, 0, back,
                0, 0, -1, 0, 1, 0, marginX, marginY, columns, rows);
        addWindowGrid(windows, right + WINDOW_OFFSET, 0, front,
                0, 0, 1, 0, 1, 0, marginX, marginY, columns, rows);
    }

    private void addWindowGrid(
            List<Window> windows,
            double originX,
            double originY,
            double originZ,
            double acrossX,
            double acrossY,
            double acrossZ,
            double upX,
            double upY,
            double upZ,
            double marginX,
            double marginY,
            int columns,
            int rows) {

        double stepX = WINDOW_WIDTH + marginX;
        double stepY = WINDOW_HEIGHT + marginY;

        for (int column = 0; column < columns; column++) {
            for (int row = 0; row < rows; row++) {
                double u = marginX + column * stepX;
                double v = marginY + row * stepY;

                Point3D p1 = windowCorner(originX, originY, originZ, acrossX, acrossY, acrossZ,
                        upX, upY, upZ, u, v);
                Point3D p2 = windowCorner(originX, originY, originZ, acrossX, acrossY, acrossZ,
                        upX, upY, upZ, u + WINDOW_WIDTH, v);
                Point3D p3 = windowCorner(originX, originY, originZ, acrossX, acrossY, acrossZ,
                        upX, upY, upZ, u + WINDOW_WIDTH, v + WINDOW_HEIGHT);
                Point3D p4 = windowCorner(originX, originY, originZ, acrossX, acrossY, acrossZ,
                        upX, upY, upZ, u, v + WINDOW_HEIGHT);

                windows.add(new Window(p1, p4, p3, p2));
            }
        }
    }

    private static Point3D windowCorner(
            double originX, double originY, double originZ,
            double acrossX, double acrossY, double acrossZ,
            double upX, double upY, double upZ,
            double u, double v) {

        return new Point3D(
                originX + acrossX * u + upX * v,
                originY + acrossY * u + upY * v,
                originZ + acrossZ * u + upZ * v);
    }

    private void addQuad(
            List<WorldTriangle> geometry,
            Point3D p1,
            Point3D p2,
            Point3D p3,
            Point3D p4,
            Color color,
            Material material) {

        geometry.add(
                new WorldTriangle(
                        p1, p2, p3, color, SurfaceKind.BUILDING, material));
        geometry.add(
                new WorldTriangle(
                        p1, p3, p4, color, SurfaceKind.BUILDING, material));
    }
}
