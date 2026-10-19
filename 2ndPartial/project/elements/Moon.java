package project.elements;

import java.awt.Color;
import java.awt.image.BufferedImage;

import engine.fill.FloodFill;
import engine.shapes.Circle;

/**
 * Represents the moon in the city scene.
 */
public class Moon {

    private int x;
    private int y;
    private final int radius;
    private final Color moonColor;
    private final Color skyColor;

    /**
     * Creates a crescent moon at the specified position.
     *
     * @param x the horizontal center coordinate
     * @param y the vertical center coordinate
     * @param radius the moon radius
     * @param moonColor the moon color
     * @param skyColor the color used to create the crescent
     */
    public Moon(
            int x,
            int y,
            int radius,
            Color moonColor,
            Color skyColor) {

        if (radius <= 0) {
            throw new IllegalArgumentException(
                "Moon radius must be positive."
            );
        }

        if (moonColor == null || skyColor == null) {
            throw new IllegalArgumentException(
                "Moon colors cannot be null."
            );
        }

        this.x = x;
        this.y = y;
        this.radius = radius;
        this.moonColor = moonColor;
        this.skyColor = skyColor;
    }

    /**
     * Draws a crescent moon using the graphics engine.
     *
     * @param buffer the image buffer to draw into
     */
    public void draw(BufferedImage buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException(
                "Buffer cannot be null."
            );
        }

        // Draw and fill the main moon.
        Circle.midpoint(buffer, x, y, radius, moonColor);
        FloodFill.spanFill(buffer, x, y, moonColor);

        // Cover part of the moon to create a crescent.
        int offsetX = radius / 2;
        int offsetY = -radius / 4;

        Circle.midpoint(
            buffer,
            x + offsetX,
            y + offsetY,
            radius,
            skyColor
        );

        FloodFill.spanFill(
            buffer,
            x + offsetX,
            y + offsetY,
            skyColor
        );
    }

        public void draw(BufferedImage buffer, Color currentSkyColor) {
        if (buffer == null || currentSkyColor == null) {
            throw new IllegalArgumentException(
                "Buffer and sky color cannot be null."
            );
        }

        Circle.midpoint(buffer, x, y, radius, moonColor);
        FloodFill.spanFill(buffer, x, y, moonColor);

        int offsetX = radius / 2;
        int offsetY = -radius / 4;

        Circle.midpoint(
            buffer,
            x + offsetX,
            y + offsetY,
            radius,
            currentSkyColor
        );

        FloodFill.spanFill(
            buffer,
            x + offsetX,
            y + offsetY,
            currentSkyColor
        );
    }

    /**
     * Updates the moon position.
     *
     * @param x the new horizontal center coordinate
     * @param y the new vertical center coordinate
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getRadius() {
        return radius;
    }
}

