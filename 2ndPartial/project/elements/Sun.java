package project.elements;

import java.awt.Color;
import java.awt.image.BufferedImage;

import engine.fill.FloodFill;
import engine.shapes.Circle;

/**
 * Represents the sun in the city scene.
 */
public class Sun {

    private int x;
    private int y;
    private final int radius;
    private final Color color;

    /**
     * Creates a sun at the specified position.
     *
     * @param x the horizontal center coordinate
     * @param y the vertical center coordinate
     * @param radius the sun radius
     * @param color the sun color
     */
    public Sun(int x, int y, int radius, Color color) {
        if (radius <= 0) {
            throw new IllegalArgumentException(
                "Sun radius must be positive."
            );
        }

        if (color == null) {
            throw new IllegalArgumentException(
                "Sun color cannot be null."
            );
        }

        this.x = x;
        this.y = y;
        this.radius = radius;
        this.color = color;
    }

    /**
     * Draws the sun using the graphics engine.
     *
     * @param buffer the image buffer to draw into
     */
    public void draw(BufferedImage buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException(
                "Buffer cannot be null."
            );
        }

        Circle.midpoint(buffer, x, y, radius, color);
        FloodFill.spanFill(buffer, x, y, color);
    }

    /**
     * Updates the sun position.
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

