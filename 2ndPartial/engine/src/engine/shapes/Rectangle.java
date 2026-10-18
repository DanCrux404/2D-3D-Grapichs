
package engine.shapes;

import java.awt.Color;
import java.awt.image.BufferedImage;
import engine.transform.Point2D;
import engine.transform.Transform2D;

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

    /**
    * Draws a rectangle after applying a geometric transformation.
    *
    * @param buffer the image buffer
    * @param x1 the first horizontal coordinate
    * @param y1 the first vertical coordinate
    * @param x2 the second horizontal coordinate
    * @param y2 the second vertical coordinate
    * @param color the rectangle color
    * @param transformation the transformation type:
    *                       1 = translation, 2 = scaling, 3 = rotation
    */
    public static void drawTransformed(
            BufferedImage buffer,
            int x1, int y1, int x2, int y2,
            Color color,
            int transformation) {

        Point2D[] vertices = {
            new Point2D(x1, y1),
            new Point2D(x2, y1),
            new Point2D(x2, y2),
            new Point2D(x1, y2)
        };

        for (int i = 0; i < vertices.length; i++) {
            Point2D p = vertices[i];

            switch (transformation) {
                case 1:
                    vertices[i] = Transform2D.translate(p, 80, 40);
                    break;
                case 2:
                    vertices[i] = Transform2D.scale(p, 1.5, 1.5);
                    break;
                case 3:
                    vertices[i] = Transform2D.rotate(p, 30);
                    break;
                default:
                    throw new IllegalArgumentException(
                        "Unknown transformation type."
                    );
            }
        }

        for (int i = 0; i < vertices.length; i++) {
            Point2D a = vertices[i];
            Point2D b = vertices[(i + 1) % vertices.length];

            Line.bresenham(
                buffer,
                (int) Math.round(a.getX()),
                (int) Math.round(a.getY()),
                (int) Math.round(b.getX()),
                (int) Math.round(b.getY()),
                color
            );
        }
    }
}

