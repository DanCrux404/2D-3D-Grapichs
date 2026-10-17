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
    
    /**
     * Draws a line on the specified image buffer using
     * Bresenham's line drawing algorithm.
     *
     * <p>The algorithm uses an integer decision parameter
     * to determine which pixels best approximate the
     * desired line, avoiding floating-point calculations.</p>
     *
     * <p>This implementation supports all line directions
     * and slopes, including horizontal, vertical, steep,
     * negative-slope, and single-pixel lines.</p>
     *
     * <p>Pixels outside the buffer boundaries are ignored.</p>
     *
     * @param buffer the image buffer where the line will be drawn
     * @param x0     the x-coordinate of the starting point
     * @param y0     the y-coordinate of the starting point
     * @param x1     the x-coordinate of the ending point
     * @param y1     the y-coordinate of the ending point
     * @param color  the color used to draw the line
    */
    public static void bresenham(
            BufferedImage buffer,
            int x0, int y0,
            int x1, int y1,
            Color color) {

        //Distances
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        //Inicates if we move right or left
        int sx = (x0 < x1) ? 1 : -1;
        //Indicates if we up or down
        int sy = (y0 < y1) ? 1 : -1;

        //Calculates error to decide next pixel
        int error = dx - dy;

        while (true) {
            Pixel.put(buffer, x0, y0, color);

            if (x0 == x1 && y0 == y1) {
                break;
            }

            //Evaluates horizontal or vertical for each iteration
            int error2 = 2 * error;

            if (error2 > -dy) {
                error -= dy;
                x0 += sx;
            }

            if (error2 < dx) {
                error += dx;
                y0 += sy;
            }
        }
    }

    
    /**
     * Draws a line on the specified image buffer using
     * the Midpoint line drawing algorithm.
     *
     * <p>The algorithm selects the next pixel by evaluating
     * a decision parameter derived from the midpoint between
     * two candidate pixels. The major axis is processed
     * incrementally, while the minor axis is adjusted when
     * the decision parameter indicates that the line has
     * crossed the midpoint.</p>
     *
     * <p>This implementation supports all line directions
     * and slopes, including horizontal, vertical, steep,
     * negative-slope, and single-pixel lines.</p>
     *
     * <p>Pixels outside the buffer boundaries are ignored.</p>
     *
     * @param buffer the image buffer where the line will be drawn
     * @param x0     the x-coordinate of the starting point
     * @param y0     the y-coordinate of the starting point
     * @param x1     the x-coordinate of the ending point
     * @param y1     the y-coordinate of the ending point
     * @param color  the color used to draw the line
    */
    public static void midpoint(
            BufferedImage buffer,
            int x0, int y0,
            int x1, int y1,
            Color color) {

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        boolean steep = dy > dx;

        // Swap axes for steep lines.
        if (steep) {
            int temp = x0;
            x0 = y0;
            y0 = temp;

            temp = x1;
            x1 = y1;
            y1 = temp;
        }

        // Ensure that the major axis increases.
        if (x0 > x1) {
            int temp = x0;
            x0 = x1;
            x1 = temp;

            temp = y0;
            y0 = y1;
            y1 = temp;
        }

        dx = x1 - x0;
        dy = Math.abs(y1 - y0);

        int sy = (y0 < y1) ? 1 : -1;

        // Midpoint decision parameter.
        // We use double to keep the half pixel
        double p = dy - dx / 2.0;

        int y = y0;

        for (int x = x0; x <= x1; x++) {

            // Restore the original axes if they were swapped.
            if (steep) {
                Pixel.put(buffer, y, x, color);
            } else {
                Pixel.put(buffer, x, y, color);
            }

            //P update
            if (p >= 0) {
                y += sy;
                p -= dx;
            }

            p += dy;
        }
    }


    /**
    * Common line masks.
    * '1' means draw the pixel; '0' means skip it.
    */
    public static final String DASHED = "1111000";
    public static final String DASH_DOT = "111100010";

    /**
    * Draws a line using Bresenham's algorithm and a repeating mask.
    *
    * @param buffer the image buffer where the line will be drawn
    * @param x0 the starting x-coordinate
    * @param y0 the starting y-coordinate
    * @param x1 the ending x-coordinate
    * @param y1 the ending y-coordinate
    * @param mask a non-empty string containing only '1' and '0'
    * @param color the color used to draw the line
    * @throws IllegalArgumentException if the mask is empty or invalid
    */
    public static void drawMasked(
            BufferedImage buffer,
            int x0,
            int y0,
            int x1,
            int y1,
            String mask,
            Color color) {

        // Make sure the mask contains a valid repeating pattern.
        if (mask == null || mask.isEmpty()
                || !mask.matches("[01]+")) {
            throw new IllegalArgumentException(
                    "Mask must contain only '1' and '0'.");
        }

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        // Determine the direction of movement on each axis.
        int sx = (x0 < x1) ? 1 : -1;
        int sy = (y0 < y1) ? 1 : -1;

        int error = dx - dy;

        // Tracks the current position within the mask.
        int maskIndex = 0;

        while (true) {

            // Draw only when the current mask bit is '1'.
            if (mask.charAt(maskIndex) == '1') {
                Pixel.put(buffer, x0, y0, color);
            }

            // Stop after processing the final pixel.
            if (x0 == x1 && y0 == y1) {
                break;
            }

            // Advance along the line using Bresenham's algorithm.
            int error2 = 2 * error;

            if (error2 > -dy) {
                error -= dy;
                x0 += sx;
            }

            if (error2 < dx) {
                error += dx;
                y0 += sy;
            }

            // Repeat the mask when its end is reached.
            maskIndex = (maskIndex + 1) % mask.length();
        }
    }

    /**
    * Draws a thick line using Bresenham's algorithm.
    *
    * @param buffer the image buffer where the line will be drawn
    * @param x0 the starting x-coordinate
    * @param y0 the starting y-coordinate
    * @param x1 the ending x-coordinate
    * @param y1 the ending y-coordinate
    * @param thickness the line thickness in pixels
    * @param color the color used to draw the line
    */
    public static void thick(
            BufferedImage buffer,
            int x0, int y0,
            int x1, int y1,
            int thickness,
            Color color) {

        if (thickness < 1) {
            throw new IllegalArgumentException(
                    "Line thickness must be at least 1.");
        }

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        // Determine the direction of movement.
        int sx = (x0 < x1) ? 1 : -1;
        int sy = (y0 < y1) ? 1 : -1;

        int error = dx - dy;

        // Center the brush around the line.
        int start = -(thickness - 1) / 2;
        int end = thickness / 2;

        // A vertical brush is used for shallow slopes.
        boolean verticalBrush = dx >= dy;

        while (true) {

            // Paint a section perpendicular to the line.
            for (int offset = start; offset <= end; offset++) {
                if (verticalBrush) {
                    Pixel.put(buffer, x0, y0 + offset, color);
                } else {
                    Pixel.put(buffer, x0 + offset, y0, color);
                }
            }

            // Stop after drawing the endpoint.
            if (x0 == x1 && y0 == y1) {
                break;
            }

            // Advance to the next pixel using Bresenham.
            int error2 = 2 * error;

            if (error2 > -dy) {
                error -= dy;
                x0 += sx;
            }

            if (error2 < dx) {
                error += dx;
                y0 += sy;
            }
        }
    }
}
