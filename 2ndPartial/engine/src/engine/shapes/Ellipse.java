
package engine.shapes;

import engine.core.Pixel;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class Ellipse {

    /**
     * Draws an ellipse using the midpoint ellipse algorithm.
     *
     * @param buffer  the image buffer where the ellipse will be drawn
     * @param xc      the x-coordinate of the center
     * @param yc      the y-coordinate of the center
     * @param radiusX the horizontal radius
     * @param radiusY the vertical radius
     * @param color   the color used to draw the ellipse
     */
    public static void midpoint(
            BufferedImage buffer,
            int xc,
            int yc,
            int radiusX,
            int radiusY,
            Color color) {

        // Radii cannot be negative.
        if (radiusX < 0 || radiusY < 0) {
            throw new IllegalArgumentException(
                    "Ellipse radii cannot be negative.");
        }

        // A zero radius produces a line or a single point.
        if (radiusX == 0) {
            for (int y = yc - radiusY; y <= yc + radiusY; y++) {
                Pixel.put(buffer, xc, y, color);
            }
            return;
        }

        if (radiusY == 0) {
            for (int x = xc - radiusX; x <= xc + radiusX; x++) {
                Pixel.put(buffer, x, yc, color);
            }
            return;
        }

        // Use squared radii in the ellipse equation.
        long rx2 = (long) radiusX * radiusX;
        long ry2 = (long) radiusY * radiusY;

        // Start at the rightmost point of the first quadrant.
        long x = 0;
        long y = radiusY;

        // Derivatives help determine where the slope changes regions.
        long dx = 0;
        long dy = 2 * rx2 * y;

        // Initial midpoint decision for Region 1.
        double p1 = ry2 - rx2 * radiusY
                + 0.25 * rx2;

        // Region 1: the contour is relatively flat.
        while (dx < dy) {
            plotFour(buffer, xc, yc, (int) x, (int) y, color);

            x++;
            dx += 2 * ry2;

            // Decide whether y should also move inward.
            if (p1 < 0) {
                p1 += dx + ry2;
            } else {
                y--;
                dy -= 2 * rx2;
                p1 += dx - dy + ry2;
            }
        }

        // Initial midpoint decision for Region 2.
        double p2 = ry2 * (x + 0.5) * (x + 0.5)
                + rx2 * (y - 1.0) * (y - 1.0)
                - rx2 * ry2;

        // Region 2: the contour is relatively steep.
        while (y >= 0) {
            plotFour(buffer, xc, yc, (int) x, (int) y, color);

            y--;
            dy -= 2 * rx2;

            // Decide whether x should also move inward.
            if (p2 > 0) {
                p2 += rx2 - dy;
            } else {
                x++;
                dx += 2 * ry2;
                p2 += dx - dy + rx2;
            }
        }
    }

    /**
     * Plots the four points reflected across the ellipse axes.
     */
    private static void plotFour(
            BufferedImage buffer,
            int xc,
            int yc,
            int x,
            int y,
            Color color) {

        Pixel.put(buffer, xc + x, yc - y, color);
        Pixel.put(buffer, xc - x, yc - y, color);
        Pixel.put(buffer, xc + x, yc + y, color);
        Pixel.put(buffer, xc - x, yc + y, color);
    }

    /**
    * Returns polygon vertices approximating an ellipse.
    *
    * @param xc the x-coordinate of the center
    * @param yc the y-coordinate of the center
    * @param radiusX the horizontal radius
    * @param radiusY the vertical radius
    * @param segments the number of vertices used for approximation
    * @return the vertices in contour order
    */
    public static int[][] vertices(
            int xc, int yc, int radiusX, int radiusY, int segments) {

        if (radiusX < 0 || radiusY < 0) {
            throw new IllegalArgumentException(
                    "Ellipse radii cannot be negative.");
        }

        if (segments < 3) {
            throw new IllegalArgumentException(
                    "At least three segments are required.");
        }

        int[][] vertices = new int[segments][2];

        for (int i = 0; i < segments; i++) {
            // Calculate the angle of the current vertex.
            double angle = 2.0 * Math.PI * i / segments;

            // Calculate the point on the ellipse.
            vertices[i][0] =
                    xc + (int) Math.round(radiusX * Math.cos(angle));

            vertices[i][1] =
                    yc + (int) Math.round(radiusY * Math.sin(angle));
        }

        return vertices;
    }

    /**
    * Draws a translated or scaled ellipse using the midpoint algorithm.
    *
    * Transformation types:
    * 1 = translation
    * 2 = rotation of the center around the origin
    * 3 = uniform scaling around the origin
    *
    * Note: Type 2 rotates the center only. The ellipse itself
    * remains aligned with the coordinate axes.
    *
    * @param buffer the image buffer
    * @param xc the original center x-coordinate
    * @param yc the original center y-coordinate
    * @param radiusX the original horizontal radius
    * @param radiusY the original vertical radius
    * @param transformation the transformation type
    * @param color the ellipse color
    */
    public static void drawTransformed(
            BufferedImage buffer,
            int xc, int yc, int radiusX, int radiusY,
            int transformation,
            Color color) {

        double newX = xc;
        double newY = yc;
        double newRadiusX = radiusX;
        double newRadiusY = radiusY;

        switch (transformation) {
            case 1:
                newX += 80;
                newY += 40;
            break;

            case 2:
                double angle = Math.toRadians(30);

                newX = xc * Math.cos(angle)
                    - yc * Math.sin(angle);

                newY = xc * Math.sin(angle)
                    + yc * Math.cos(angle);
            break;

            case 3:
                newX = xc * 1.5;
                newY = yc * 1.5;
                newRadiusX = radiusX * 1.5;
                newRadiusY = radiusY * 1.5;
            break;

            default:
                throw new IllegalArgumentException(
                    "Unknown transformation type."
                );
        }

        Ellipse.midpoint(
            buffer,
            (int) Math.round(newX),
            (int) Math.round(newY),
            (int) Math.round(newRadiusX),
            (int) Math.round(newRadiusY),
            color
        );
    }

}

