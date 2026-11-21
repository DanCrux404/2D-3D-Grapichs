package engine.shapes;

import engine.core.Point3D;
import engine.core.ProjectedPoint;
import engine.raster.Rasterizer;
import engine.transform.Projection3D;
import engine.transform.Transform3D;
import engine.util.ColorUtils;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class SurfaceMesh3D implements SurfaceMesh {

    private final Point3D[][] vertices;
    private final Point3D center;
    private final Line3D line;
    private final Projection3D projection;

    private double translationX = 0.0;
    private double translationY = 0.0;
    private double translationZ = 0.0;

    private double scaleX = 1.0;
    private double scaleY = 1.0;
    private double scaleZ = 1.0;

    private double rotationX = 0.0;
    private double rotationY = 0.0;
    private double rotationZ = 0.0;

    public SurfaceMesh3D(Point3D[][] vertices, Projection3D projection) {

        if (vertices == null || vertices.length < 2) {
            throw new IllegalArgumentException("A surface must have at least 2 rows.");
        }

        if (projection == null) {
            throw new IllegalArgumentException("Projection cannot be null.");
        }

        int columns = vertices[0].length;

        if (columns < 2) {
            throw new IllegalArgumentException("A surface must have at least 2 columns.");
        }

        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        this.vertices = new Point3D[vertices.length][columns];

        for (int i = 0; i < vertices.length; i++) {

            if (vertices[i] == null || vertices[i].length != columns) {
                throw new IllegalArgumentException("Surface mesh must be rectangular.");
            }

            for (int j = 0; j < columns; j++) {

                Point3D point = vertices[i][j];

                if (point == null) {
                    throw new IllegalArgumentException("Surface vertices cannot be null.");
                }

                this.vertices[i][j] = point;

                minX = Math.min(minX, point.getX());
                maxX = Math.max(maxX, point.getX());

                minY = Math.min(minY, point.getY());
                maxY = Math.max(maxY, point.getY());

                minZ = Math.min(minZ, point.getZ());
                maxZ = Math.max(maxZ, point.getZ());
            }
        }

        center = new Point3D(
                (minX + maxX) / 2.0,
                (minY + maxY) / 2.0,
                (minZ + maxZ) / 2.0
        );

        this.projection = projection;
        line = new Line3D(projection);
    }

    /**
     * Draws the surface as a traditional wireframe.
     */
    @Override
    public void draw(BufferedImage buffer, Color color) {

        validateDrawingArguments(buffer, color);

        Point3D[][] transformedVertices = calculateTransformedVertices();

        int rows = vertices.length;
        int columns = vertices[0].length;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {

                if (j < columns - 1) {
                    line.draw(
                            buffer,
                            transformedVertices[i][j],
                            transformedVertices[i][j + 1],
                            color
                    );
                }

                if (i < rows - 1) {
                    line.draw(
                            buffer,
                            transformedVertices[i][j],
                            transformedVertices[i + 1][j],
                            color
                    );
                }
            }
        }
    }

    /**
     * Draws the surface as filled triangles using the Z-buffer.
     *
     * <p>Each quadrilateral cell is divided into two triangles. Their fragments
     * are rendered according to their interpolated depth.
     *
     * @param buffer image where the surface will be drawn
     * @param color surface color
     * @param rasterizer depth-aware rasterizer
     */
    public void draw(
            BufferedImage buffer,
            Color color,
            Rasterizer rasterizer) {

        validateDrawingArguments(buffer, color);

        if (rasterizer == null) {
            throw new IllegalArgumentException("Rasterizer cannot be null.");
        }

        Point3D[][] transformedVertices = calculateTransformedVertices();

        int rows = vertices.length;
        int columns = vertices[0].length;

        // Project all vertices while preserving their depth.
        ProjectedPoint[][] projectedVertices =
                new ProjectedPoint[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                projectedVertices[i][j] =
                        projection.projectWithDepth(transformedVertices[i][j]);
            }
        }

        // Fill every cell using two triangles.
        for (int i = 0; i < rows - 1; i++) {
            for (int j = 0; j < columns - 1; j++) {

                ProjectedPoint p1 = projectedVertices[i][j];
                ProjectedPoint p2 = projectedVertices[i][j + 1];
                ProjectedPoint p3 = projectedVertices[i + 1][j + 1];
                ProjectedPoint p4 = projectedVertices[i + 1][j];

                // First triangle.
                rasterizer.fillTriangle(
                        buffer, p1, p2, p3, color
                );

                // Second triangle.
                rasterizer.fillTriangle(
                        buffer, p1, p3, p4, color
                );
            }
        }
    }

    /**
     * Calculates all vertices after applying the current transformations.
     */
    private Point3D[][] calculateTransformedVertices() {

        int rows = vertices.length;
        int columns = vertices[0].length;

        Point3D[][] transformedVertices = new Point3D[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                transformedVertices[i][j] = transformPoint(vertices[i][j]);
            }
        }

        return transformedVertices;
    }

    private void validateDrawingArguments(BufferedImage buffer, Color color) {

        if (buffer == null || color == null) {
            throw new IllegalArgumentException("Buffer and color cannot be null.");
        }
    }

    @Override
    public void translate(double dx, double dy, double dz) {

        if (!Double.isFinite(dx) || !Double.isFinite(dy) || !Double.isFinite(dz)) {
            throw new IllegalArgumentException("Translation values must be finite.");
        }

        translationX += dx;
        translationY += dy;
        translationZ += dz;
    }

    @Override
    public void scale(double sx, double sy, double sz) {

        if (!Double.isFinite(sx)
                || !Double.isFinite(sy)
                || !Double.isFinite(sz)
                || sx <= 0
                || sy <= 0
                || sz <= 0) {
            throw new IllegalArgumentException("Scale values must be positive and finite.");
        }

        scaleX *= sx;
        scaleY *= sy;
        scaleZ *= sz;
    }

    @Override
    public void rotateX(double angle) {

        if (!Double.isFinite(angle)) {
            throw new IllegalArgumentException("Rotation angle must be finite.");
        }

        rotationX += angle;
    }

    @Override
    public void rotateY(double angle) {

        if (!Double.isFinite(angle)) {
            throw new IllegalArgumentException("Rotation angle must be finite.");
        }

        rotationY += angle;
    }

    @Override
    public void rotateZ(double angle) {

        if (!Double.isFinite(angle)) {
            throw new IllegalArgumentException("Rotation angle must be finite.");
        }

        rotationZ += angle;
    }

    private Point3D transformPoint(Point3D point) {

        double[][] toOrigin =
                Transform3D.translation(
                        -center.getX(),
                        -center.getY(),
                        -center.getZ()
                );

        double[][] scaling =
                Transform3D.scaling(scaleX, scaleY, scaleZ);

        double[][] rx = Transform3D.rotationX(rotationX);
        double[][] ry = Transform3D.rotationY(rotationY);
        double[][] rz = Transform3D.rotationZ(rotationZ);

        double[][] toPosition =
                Transform3D.translation(
                        center.getX() + translationX,
                        center.getY() + translationY,
                        center.getZ() + translationZ
                );

        Point3D transformed = Transform3D.apply(point, toOrigin);
        transformed = Transform3D.apply(transformed, scaling);
        transformed = Transform3D.apply(transformed, rx);
        transformed = Transform3D.apply(transformed, ry);
        transformed = Transform3D.apply(transformed, rz);
        transformed = Transform3D.apply(transformed, toPosition);

        return transformed;
    }

    /**
     * Draws the wireframe using a color gradient.
     */
    @Override
    public void drawGradient(
            BufferedImage buffer,
            Color startColor,
            Color endColor) {

        if (buffer == null || startColor == null || endColor == null) {
            throw new IllegalArgumentException("Buffer and colors cannot be null.");
        }

        Point3D[][] transformedVertices = calculateTransformedVertices();

        int rows = vertices.length;
        int columns = vertices[0].length;

        for (int i = 0; i < rows; i++) {

            double fraction = (double) i / (rows - 1);
            Color color = ColorUtils.interpolate(
                    startColor, endColor, fraction
            );

            for (int j = 0; j < columns; j++) {

                if (j + 1 < columns) {
                    line.draw(
                            buffer,
                            transformedVertices[i][j],
                            transformedVertices[i][j + 1],
                            color
                    );
                }

                if (i + 1 < rows) {
                    line.draw(
                            buffer,
                            transformedVertices[i][j],
                            transformedVertices[i + 1][j],
                            color
                    );
                }
            }
        }
    }
}

