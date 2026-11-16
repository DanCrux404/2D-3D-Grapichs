package engine.shapes;

import engine.core.Point3D;
import engine.transform.Projection3D;

import java.awt.Color;
import java.awt.image.BufferedImage;
import engine.transform.Transform3D;

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

    /**
     * Creates a cube with the specified center, size and projection.
     *
     * @param center the geometric center of the cube
     * @param size the cube size in logical units
     * @param projection the parallel projection used to draw the cube
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
        double halfSize = (size * PIXELS_PER_UNIT) / 2.0;

        double cx = center.getX();
        double cy = center.getY();
        double cz = center.getZ();

        // Define the eight vertices relative to the center.
        Point3D p1 = new Point3D(cx - halfSize, cy - halfSize, cz - halfSize);
        Point3D p2 = new Point3D(cx + halfSize, cy - halfSize, cz - halfSize);
        Point3D p3 = new Point3D(cx + halfSize, cy + halfSize, cz - halfSize);
        Point3D p4 = new Point3D(cx - halfSize, cy + halfSize, cz - halfSize);

        Point3D p5 = new Point3D(cx - halfSize, cy - halfSize, cz + halfSize);
        Point3D p6 = new Point3D(cx + halfSize, cy - halfSize, cz + halfSize);
        Point3D p7 = new Point3D(cx + halfSize, cy + halfSize, cz + halfSize);
        Point3D p8 = new Point3D(cx - halfSize, cy + halfSize, cz + halfSize);

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
}
