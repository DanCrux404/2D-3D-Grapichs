package engine.shapes;

import engine.core.Point3D;
import engine.transform.Point2D;
import engine.transform.Projection3D;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Represents a three-dimensional line.
 *
 * <p>The endpoints are projected onto a two-dimensional plane before being drawn using the existing
 * Bresenham algorithm.
 */
public class Line3D {

    private final Projection3D projection;

    /**
     * Creates a 3D line using the specified projection.
     *
     * @param projection the projection used to transform 3D points
     */
    public Line3D(Projection3D projection) {

        if (projection == null) {
            throw new IllegalArgumentException("Projection cannot be null.");
        }

        this.projection = projection;
    }

    /**
     * Draws a 3D line by projecting its endpoints and connecting them with Bresenham's algorithm.
     *
     * @param buffer the image where the line will be drawn
     * @param p1 the first endpoint
     * @param p2 the second endpoint
     * @param color the line color
     */
    public void draw(BufferedImage buffer, Point3D p1, Point3D p2, Color color) {

        if (buffer == null || p1 == null || p2 == null || color == null) {
            throw new IllegalArgumentException("Arguments cannot be null.");
        }

        Point2D projected1 = projection.project(p1);
        Point2D projected2 = projection.project(p2);

        int x1 = (int) Math.round(projected1.getX());
        int y1 = (int) Math.round(projected1.getY());

        int x2 = (int) Math.round(projected2.getX());
        int y2 = (int) Math.round(projected2.getY());

        Line.bresenham(buffer, x1, y1, x2, y2, color);
    }
}
