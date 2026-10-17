
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
}

