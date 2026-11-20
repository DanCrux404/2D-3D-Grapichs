package engine.shapes;

import engine.core.Point3D;
import engine.transform.Projection3D;
import engine.transform.Transform3D;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Represents a three-dimensional cube.
 *
 * <p>The cube is defined by its center point and size. Its eight vertices are calculated when the
 * cube is drawn.
 *
 * <p>Each size unit corresponds to ten coordinate units. The twelve edges are drawn using Line3D.
 */
public class Cube {

    private static final double PIXELS_PER_UNIT = 10.0;

    private Point3D center;
    private final double size;
    private final Line3D line;
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
     * @throws IllegalArgumentException if the center or projection is null, or if the size is not
     *     positive and finite
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
        this.line = new Line3D(projection);
    }

    /**
     * Draws the twelve edges of the cube.
     *
     * <p>The eight vertices are calculated using the center and half the cube's side length.
     *
     * @param buffer the image where the cube will be drawn
     * @param color the color of the cube edges
     */
    public void draw(BufferedImage buffer, Color color) {

        if (buffer == null || color == null) {
            throw new IllegalArgumentException("Buffer and color cannot be null.");
        }

        // Calculate half the cube's side length.
        double halfX = (size * PIXELS_PER_UNIT * scaleX) / 2.0;
        double halfY = (size * PIXELS_PER_UNIT * scaleY) / 2.0;
        double halfZ = (size * PIXELS_PER_UNIT * scaleZ) / 2.0;

        double cx = center.getX();
        double cy = center.getY();
        double cz = center.getZ();

        // Define the eight vertices relative to the center.
        Point3D p1 = new Point3D(cx - halfX, cy - halfY, cz - halfZ);
        Point3D p2 = new Point3D(cx + halfX, cy - halfY, cz - halfZ);
        Point3D p3 = new Point3D(cx + halfX, cy + halfY, cz - halfZ);
        Point3D p4 = new Point3D(cx - halfX, cy + halfY, cz - halfZ);

        Point3D p5 = new Point3D(cx - halfX, cy - halfY, cz + halfZ);
        Point3D p6 = new Point3D(cx + halfX, cy - halfY, cz + halfZ);
        Point3D p7 = new Point3D(cx + halfX, cy + halfY, cz + halfZ);
        Point3D p8 = new Point3D(cx - halfX, cy + halfY, cz + halfZ);

        p1 = rotatePoint(p1);
        p2 = rotatePoint(p2);
        p3 = rotatePoint(p3);
        p4 = rotatePoint(p4);
        p5 = rotatePoint(p5);
        p6 = rotatePoint(p6);
        p7 = rotatePoint(p7);
        p8 = rotatePoint(p8);

        // Bottom face.
        line.draw(buffer, p1, p2, color);
        line.draw(buffer, p2, p3, color);
        line.draw(buffer, p3, p4, color);
        line.draw(buffer, p4, p1, color);

        // Top face.
        line.draw(buffer, p5, p6, color);
        line.draw(buffer, p6, p7, color);
        line.draw(buffer, p7, p8, color);
        line.draw(buffer, p8, p5, color);

        // Connect both faces.
        line.draw(buffer, p1, p5, color);
        line.draw(buffer, p2, p6, color);
        line.draw(buffer, p3, p7, color);
        line.draw(buffer, p4, p8, color);
    }

    /**
     * Translates the cube using a homogeneous transformation matrix.
     *
     * <p>The translation is applied to the cube's center. Its vertices are recalculated when the
     * cube is drawn.
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
     * <p>The scaling is applied relative to the cube's center.
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
            throw new IllegalArgumentException("Scaling factors must be positive and finite.");
        }

        double[][] matrix = Transform3D.scaling(sx, sy, sz);

        Point3D scaled = Transform3D.apply(new Point3D(scaleX, scaleY, scaleZ), matrix);

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
                Transform3D.translation(-center.getX(), -center.getY(), -center.getZ());

        Point3D localPoint = Transform3D.apply(point, toOrigin);

        // Apply the accumulated rotations.
        double[][] rx = Transform3D.rotationX(rotationX);
        double[][] ry = Transform3D.rotationY(rotationY);
        double[][] rz = Transform3D.rotationZ(rotationZ);

        localPoint = Transform3D.apply(localPoint, rx);
        localPoint = Transform3D.apply(localPoint, ry);
        localPoint = Transform3D.apply(localPoint, rz);

        // Return the vertex to its original position.
        double[][] toCenter = Transform3D.translation(center.getX(), center.getY(), center.getZ());

        return Transform3D.apply(localPoint, toCenter);
    }
}
