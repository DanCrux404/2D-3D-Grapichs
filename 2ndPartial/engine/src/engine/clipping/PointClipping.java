
package engine.clipping;

import engine.core.Pixel;
import engine.shapes.Circle;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Implements point-based clipping for rasterized circles.
 *
 * Only pixels inside the clipping window are drawn.
 */
public class PointClipping {

    /**
     * Draws the visible portion of a circle using point clipping.
     *
     * @param buffer the image buffer where the circle is drawn
     * @param xc the x-coordinate of the circle's center
     * @param yc the y-coordinate of the circle's center
     * @param radius the circle radius
     * @param color the circle color
     * @param window the rectangular clipping window
     */
    public static void drawCircle(
            BufferedImage buffer,
            int xc,
            int yc,
            int radius,
            Color color,
            ClipWindow window) {

        if (window == null) {
            throw new IllegalArgumentException(
                "Clipping window cannot be null."
            );
        }

        // Generate circle pixels and draw only those inside the window.
        Circle.midpoint(
            xc, yc, radius, color,
            (x, y, pixelColor) -> {
                if (window.contains(x, y)) {
                    Pixel.put(buffer, x, y, pixelColor);
                }
            }
        );
    }
}

