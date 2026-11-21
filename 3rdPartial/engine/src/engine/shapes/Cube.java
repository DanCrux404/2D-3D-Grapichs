package engine.shapes;

import engine.core.Point3D;
import engine.core.ProjectedPoint;
import engine.raster.Rasterizer;
import engine.transform.Projection3D;
import engine.transform.Transform3D;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Represents a three-dimensional cube.
 *
 * <p>The cube s defined by its center point and size. Its eight vertices are calculated when
 * the cube is drawn.
 *
 * <p>Each size unit corresponds to ten coordinate units.
 */
public class Cube {

    private static final double PIXELS_PER_UNIT = 10.0;

    private Point3D center;
    private final double size;
    private final Line3D line;
    private final Projection3D projection;

    private double scaleX = 1.0;
    private double scaleY = 1.0;
    private double scaleZ = 1.0;

    /** Accumulated rotation angle around the X axis. */
    private double rotationX = 0.0;

    /** Accumulated rotation angle around the Y axis. */
    private double rotationY = 0.0;

    /** Accumulated rotation angle around the Z axis. */
    private double rotationZ = 0.0;

    /**
     * Creates a cube with the specified center, size and projection.
     *
     * @param center the geometric center of the cube
     * @param size the cube size in logical units
     * @param projection the 3D projection used to draw the cube
     * @throws IllegalArgumentException if an argument is invalid
     */
    public Cube(Point3D center, double size, Projection3D projection) {

        if (center == null) {
            throw new IllegalArgumentException("Cube center cannot be null.");
        }

        if (size <= 0 || !Double.isFinite(size)) {
            throw new IllegalArgumentException("Cube size must be positive and finite.");
        }

        if (projection == null) {
            throw new IllegalArgumentException("Projection cannot be null.");
        }

        this.center = center;
        this.size = size;
        this.projection = projection;
        this.line = new Line3D(projection);
    }

    /**
     * Draws the twelve edges of the cube using traditional line rasterization.
     *
     * @param buffer the image where the cube will be drawn
     * @param color the color of the cube edges
     */
    public void draw(BufferedImage buffer, Color color) {

        validateDrawingArguments(buffer, color);

        Point3D[] vertices = calculateVertices();

        // Bottom face.
        line.draw(buffer, vertices[0], vertices[1], color);
        line.draw(buffer, vertices[1], vertices[2], color);
        line.draw(buffer, vertices[2], vertices[3], color);
        line.draw(buffer, vertices[3], vertices[0], color);

        // Top face.
        line.draw(buffer, vertices[4], vertices[5], color);
        line.draw(buffer, vertices[5], vertices[6], color);
        line.draw(buffer, vertices[6], vertices[7], color);
        line.draw(buffer, vertices[7], vertices[4], color);

        // Connect both faces.
        line.draw(buffer, vertices[0], vertices[4], color);
        line.draw(buffer, vertices[1], vertices[5], color);
        line.draw(buffer, vertices[2], vertices[6], color);
        line.draw(buffer, vertices[3], vertices[7], color);
    }

    /**
     * Draws the six filled faces of the cube using the Z-buffer.
     *
     * <p>Each face is divided into two triangles. The rasterizer determines which
     * fragments are visible according to their depth.
     *
     * @param buffer the image where the cube will be drawn
     * @param color the cube face color
     * @param rasterizer the rasterizer used for depth-aware triangle filling
     */
    public void draw(BufferedImage buffer, Color color, Rasterizer rasterizer) {

        validateDrawingArguments(buffer, color);

        if (rasterizer == null) {
            throw new IllegalArgumentException("Rasterizer cannot be null.");
        }

        Point3D[] vertices = calculateVertices();

        // Project all eight vertices while preserving their depth.
        ProjectedPoint[] projected = new ProjectedPoint[8];

        for (int i = 0; i < vertices.length; i++) {
            projected[i] = projection.projectWithDepth(vertices[i]);
        }

        // Bottom face: p1, p2, p3, p4.
        drawFace(buffer, rasterizer, color,
                projected[0], projected[1], projected[2], projected[3]);

        // Top face: p5, p6, p7, p8.
        drawFace(buffer, rasterizer, color,
                projected[4], projected[5], projected[6], projected[7]);

        // Side face: p1, p2, p6, p5.
        drawFace(buffer, rasterizer, color,
                projected[0], projected[1], projected[5], projected[4]);

        // Side face: p2, p3, p7, p6.
        drawFace(buffer, rasterizer, color,
                projected[1], projected[2], projected[6], projected[5]);

        // Side face: p3, p4, p8, p7.
        drawFace(buffer, rasterizer, color,
                projected[2], projected[3], projected[7], projected[6]);

        // Side face: p4, p1, p5, p8.
        drawFace(buffer, rasterizer, color,
                projected[3], projected[0], projected[4], projected[7]);
    }

    /**
     * Divides a quadrilateral face into two triangles and fills them.
     *
     * @param buffer the image where the face will be drawn
     * @param rasterizer the depth-aware rasterizer
     * @param color the face color
     * @param p1 first vertex
     * @param p2 second vertex
     * @param p3 third vertex
     * @param p4 fourth vertex
     */
    private void drawFace(
            BufferedImage buffer,
            Rasterizer rasterizer,
            Color color,
            ProjectedPoint p1,
            ProjectedPoint p2,
            ProjectedPoint p3,
            ProjectedPoint p4) {

        rasterizer.fillTriangle(buffer, p1, p2, p3, color);
        rasterizer.fillTriangle(buffer, p1, p3, p4, color);
    }

    /**
     * Calculates the cube's eight vertices, applying its accumulated rotations.
     *
     * @return the eight transformed vertices
     */
    private Point3D[] calculateVertices() {

        double halfX = (size * PIXELS_PER_UNIT * scaleX) / 2.0;
        double halfY = (size * PIXELS_PER_UNIT * scaleY) / 2.0;
        double halfZ = (size * PIXELS_PER_UNIT * scaleZ) / 2.0;

        double cx = center.getX();
        double cy = center.getY();
        double cz = center.getZ();

        Point3D[] vertices = {
            new Point3D(cx - halfX, cy - halfY, cz - halfZ),
            new Point3D(cx + halfX, cy - halfY, cz - halfZ),
            new Point3D(cx + halfX, cy + halfY, cz - halfZ),
            new Point3D(cx - halfX, cy + halfY, cz - halfZ),

            new Point3D(cx - halfX, cy - halfY, cz + halfZ),
            new Point3D(cx + halfX, cy - halfY, cz + halfZ),
            new Point3D(cx + halfX, cy + halfY, cz + halfZ),
            new Point3D(cx - halfX, cy + halfY, cz + halfZ)
        };

        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = rotatePoint(vertices[i]);
        }

        return vertices;
    }

    /**
     * Validates common drawing arguments.
     */
    private void validateDrawingArguments(BufferedImage buffer, Color color) {

        if (buffer == null || color == null) {
            throw new IllegalArgumentException("Buffer and color cannot be null.");
        }
    }

    /**
     * Translates the cube using a homogeneous transformation matrix.
     *
     * @param dx translation distance along the X axis
     * @param dy translation distance along the Y axis
     * @param dz translation distance along the Z axis
     */
    public void translate(double dx, double dy, double dz) {

        double[][] matrix = Transform3D.translation(dx, dy, dz);

        center = Transform3D.apply(center, matrix);
    }

    /**
     * Scales the cube along its three axes.
     *
     * @param sx scaling factor along the X axis
     * @param sy scaling factor along the Y axis
     * @param sz scaling factor along the Z axis
     */
    public void scale(double sx, double sy, double sz) {

        if (!Double.isFinite(sx)
                || !Double.isFinite(sy)
                || !Double.isFinite(sz)
                || sx <= 0
                || sy <= 0
                || sz <= 0) {
            throw new IllegalArgumentException(
                    "Scaling factors must be positive and finite.");
        }

        double[][] matrix = Transform3D.scaling(sx, sy, sz);

        Point3D scaled =
                Transform3D.apply(new Point3D(scaleX, scaleY, scaleZ), matrix);

        scaleX = scaled.getX();
        scaleY = scaled.getY();
        scaleZ = scaled.getZ();
    }

    /**
     * Rotates the cube around the X axis.
     *
     * @param angle rotation angle in degrees
     */
    public void rotateX(double angle) {
        rotationX += angle;
    }

    /**
     * Rotates the cube around the Y axis.
     *
     * @param angle rotation angle in degrees
     */
    public void rotateY(double angle) {
        rotationY += angle;
    }

    /**
     * Rotates the cube around the Z axis.
     *
     * @param angle rotation angle in degrees
     */
    public void rotateZ(double angle) {
        rotationZ += angle;
    }

    /**
     * Applies the accumulated rotations to a vertex around the cube's own center.
     *
     * @param point original vertex
     * @return rotated vertex
     */
    private Point3D rotatePoint(Point3D point) {

        // Move the vertex to the cube's local origin.
        double[][] toOrigin =
                Transform3D.translation(
                        -center.getX(), -center.getY(), -center.getZ());

        Point3D localPoint = Transform3D.apply(point, toOrigin);

        // Apply the accumulated rotations in X, Y, Z order.
        double[][] rx = Transform3D.rotationX(rotationX);
        double[][] ry = Transform3D.rotationY(rotationY);
        double[][] rz = Transform3D.rotationZ(rotationZ);

        localPoint = Transform3D.apply(localPoint, rx);
        localPoint = Transform3D.apply(localPoint, ry);
        localPoint = Transform3D.apply(localPoint, rz);

        // Return the vertex to its original position.
        double[][] toCenter =
                Transform3D.translation(
                        center.getX(), center.getY(), center.getZ());

        return Transform3D.apply(localPoint, toCenter);
    }
}

