
package engine.shapes;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Rectangle {

    /**
     * Draws a rectangle on the specified image buffer using
     * the selected line drawing algorithm.
     *
     * <p>The rectangle is defined by two opposite corners.
     * Its four sides are drawn using the selected algorithm
     * from the Line class. If the width and height are equal,
     * the resulting rectangle is a square.</p>
     *
     * @param buffer the image buffer where the rectangle will be drawn
     * @param x0     the x-coordinate of the first corner
     * @param y0     the y-coordinate of the first corner
     * @param x1     the x-coordinate of the opposite corner
     * @param y1     the y-coordinate of the opposite corner
     * @param color  the color used to draw the rectangle
     */
    public static void draw(
            BufferedImage buffer,
            int x0, int y0,
            int x1, int y1,
            Color color) {

        // Top side
        Line.bresenham(buffer, x0, y0, x1, y0, color);

        // Right side
        Line.bresenham(buffer, x1, y0, x1, y1, color);

        // Bottom side
        Line.bresenham(buffer, x1, y1, x0, y1, color);

        // Left side
        Line.bresenham(buffer, x0, y1, x0, y0, color);
    }

    /**
    * Returns the four vertices of the rectangle.
    *
    * @param x0 the x-coordinate of the first corner
    * @param y0 the y-coordinate of the first corner
    * @param x1 the x-coordinate of the opposite corner
    * @param y1 the y-coordinate of the opposite corner
    * @return the rectangle vertices in contour order
    */
    public static int[][] vertices(
            int x0, int y0, int x1, int y1) {

        return new int[][] {
            {x0, y0},
            {x1, y0},
            {x1, y1},
            {x0, y1}
        };
    }
}

