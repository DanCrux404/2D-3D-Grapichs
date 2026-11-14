
package engine.shapes;

import java.awt.Color;
import java.awt.image.BufferedImage;

import engine.core.Point3D;
import engine.transform.Point2D;
import engine.transform.ParallelProjection;

/**
 * Represents and draws a three-dimensional line using parallel projection.
 *
 * <p>The endpoints of the line are projected onto the XY plane.
 * The resulting two-dimensional line is rasterized using the
 * Bresenham algorithm from the second partial engine.
 *
 * <p>This class connects the three-dimensional geometry with the
 * existing two-dimensional drawing engine.
 */
public class Line3D {

    private final ParallelProjection projection;

    /**
     * Creates a three-dimensional line renderer using the specified
     * parallel projection.
     *
     * @param projection the projection used to transform 3D points
     * @throws IllegalArgumentException if the projection is null
     */
    public Line3D(ParallelProjection projection) {

        if (projection == null) {
            throw new IllegalArgumentException(
                "Projection cannot be null."
            );
        }

        this.projection = projection;
    }

    /**
     * Projects two three-dimensional endpoints and draws the resulting
     * two-dimensional line using Bresenham's algorithm.
     *
     * @param buffer the image where the line will be drawn
     * @param p1 the first endpoint
     * @param p2 the second endpoint
     * @param color the line color
     * @throws IllegalArgumentException if any argument is null
     */
    public void draw(
            BufferedImage buffer,
            Point3D p1,
            Point3D p2,
            Color color) {

        if (buffer == null || p1 == null || p2 == null || color == null) {
            throw new IllegalArgumentException(
                "Buffer, points and color cannot be null."
            );
        }

        // Project both endpoints onto the XY plane.
        Point2D projectedP1 = projection.project(p1);
        Point2D projectedP2 = projection.project(p2);

        // Convert projected coordinates to integer pixel positions.
        int x1 = (int) Math.round(projectedP1.getX());
        int y1 = (int) Math.round(projectedP1.getY());

        int x2 = (int) Math.round(projectedP2.getX());
        int y2 = (int) Math.round(projectedP2.getY());

        // Reuse the Bresenham implementation from the second partial.
        Line.bresenham(buffer, x1, y1, x2, y2, color);
    }
}

