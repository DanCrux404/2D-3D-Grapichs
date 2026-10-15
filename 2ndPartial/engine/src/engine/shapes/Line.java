package engine.shapes;

import engine.core.Pixel;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class Line{

/**
    * Draws a line on the specified image buffer using the
    * Digital Differential Analyzer (DDA) algorithm.
    *
    * <p>The algorithm calculates the horizontal and vertical
    * increments based on the maximum distance between the
    * starting and ending points. It then iteratively computes
    * intermediate coordinates and rounds them to the nearest
    * pixel position.</p>
    *
    * <p>If both endpoints are identical, a single pixel is drawn.
    * Pixels outside the buffer boundaries are ignored.</p>
    *
    * @param buffer the image buffer where the line will be drawn
    * @param x0     the x-coordinate of the starting point
    * @param y0     the y-coordinate of the starting point
    * @param x1     the x-coordinate of the ending point
    * @param y1     the y-coordinate of the ending point
    * @param color  the color used to draw the line
*/
    public static void dda(
            BufferedImage buffer,
            int x0, int y0,
            int x1, int y1,
            Color color) {

        int dx = x1 - x0;
        int dy = y1 - y0;

        int steps = Math.max(Math.abs(dx), Math.abs(dy));

        // Special case: Line is just a single pixel.
        if (steps == 0) {
            Pixel.put(buffer, x0, y0, color);
            return;
        }

        double xinc = (double) dx / steps;
        double yinc = (double) dy / steps;

        double x = x0;
        double y = y0;

        Pixel.put(buffer, (int) Math.round(x),
                           (int) Math.round(y), color);

        for (int k = 1; k <= steps; k++) {
            x += xinc;
            y += yinc;

            Pixel.put(buffer, (int) Math.round(x),
                               (int) Math.round(y), color);
        }
    }
}
