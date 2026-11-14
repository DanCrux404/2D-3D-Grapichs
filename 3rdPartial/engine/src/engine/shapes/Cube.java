package engine.shapes;

import engine.core.Point3D;
import engine.transform.ParallelProjection;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Represents a three-dimensional cube.
 *
 * <p>The cube is defined by eight vertices and twelve edges. Each edge is drawn using the Line3D
 * class, which projects its endpoints and reuses the Bresenham algorithm.
 */
public class Cube {

    private final Point3D p1;
    private final Point3D p2;
    private final Point3D p3;
    private final Point3D p4;
    private final Point3D p5;
    private final Point3D p6;
    private final Point3D p7;
    private final Point3D p8;

    private final Line3D line;

    /**
     * Creates a cube with the specified size, position and projection.
     *
     * @param size the length of each cube edge
     * @param x the initial x-coordinate
     * @param y the initial y-coordinate
     * @param projection the parallel projection used to draw the cube
     */
    public Cube(double size, double x, double y, ParallelProjection projection) {

        if (size <= 0 || !Double.isFinite(size) || !Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException(
                    "Cube dimensions and position must be finite, and size must be positive.");
        }

        if (projection == null) {
            throw new IllegalArgumentException("Projection cannot be null.");
        }

        // Define the eight vertices of the cube.
        p1 = new Point3D(x, y, 0);
        p2 = new Point3D(x + size, y, 0);
        p3 = new Point3D(x + size, y + size, 0);
        p4 = new Point3D(x, y + size, 0);

        p5 = new Point3D(x, y, size);
        p6 = new Point3D(x + size, y, size);
        p7 = new Point3D(x + size, y + size, size);
        p8 = new Point3D(x, y + size, size);

        line = new Line3D(projection);
    }

    /**
     * Draws the twelve edges of the cube.
     *
     * @param buffer the image where the cube will be drawn
     * @param color the color of the cube edges
     */
    public void draw(BufferedImage buffer, Color color) {

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
}
