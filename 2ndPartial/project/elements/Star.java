package project.elements;

import java.awt.Color;
import java.awt.image.BufferedImage;

import engine.fill.ScanLine;
import engine.shapes.Line;

/**
 * Represents a four-pointed star in the night sky.
 */
public class Star {

    private int x;
    private int y;
    private final int size;
    private final Color color;

    /**
     * Creates a star at the specified position.
     *
     * @param x the horizontal center coordinate
     * @param y the vertical center coordinate
     * @param size the distance from the center to each tip
     * @param color the star color
     */
    public Star(int x, int y, int size, Color color) {
        if (size < 2) {
            throw new IllegalArgumentException(
                "Star size must be at least 2."
            );
        }

        if (color == null) {
            throw new IllegalArgumentException(
                "Star color cannot be null."
            );
        }

        this.x = x;
        this.y = y;
        this.size = size;
        this.color = color;
    }

    /**
     * Draws a filled four-pointed star.
     *
     * @param buffer the image buffer to draw into
     */
    public void draw(BufferedImage buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException(
                "Buffer cannot be null."
            );
        }

        int half = Math.max(1, size / 3);

        int[][] vertices = {
            {x, y - size},
            {x + half, y - half},
            {x + size, y},
            {x + half, y + half},
            {x, y + size},
            {x - half, y + half},
            {x - size, y},
            {x - half, y - half}
        };

        ScanLine.fill(buffer, vertices, color);

        // Draw the outline to keep the star crisp.
        for (int i = 0; i < vertices.length; i++) {
            int[] current = vertices[i];
            int[] next = vertices[(i + 1) % vertices.length];

            Line.bresenham(
                buffer,
                current[0], current[1],
                next[0], next[1],
                color
            );
        }
    }

    public void draw(BufferedImage buffer, double brightness) {
        if (buffer == null) {
            throw new IllegalArgumentException(
                "Buffer cannot be null."
            );
        }

        brightness = Math.max(0.0, Math.min(1.0, brightness));

        if (brightness <= 0.0) {
            return;
        }

        Color visibleColor = new Color(
            (int) Math.round(color.getRed() * brightness),
            (int) Math.round(color.getGreen() * brightness),
            (int) Math.round(color.getBlue() * brightness)
        );

        int half = Math.max(1, size / 3);

        int[][] vertices = {
            {x, y - size},
            {x + half, y - half},
            {x + size, y},
            {x + half, y + half},
            {x, y + size},
            {x - half, y + half},
            {x - size, y},
            {x - half, y - half}
        };

        ScanLine.fill(buffer, vertices, visibleColor);

        for (int i = 0; i < vertices.length; i++) {
            int[] current = vertices[i];
            int[] next = vertices[(i + 1) % vertices.length];

            Line.bresenham(
                buffer,
                current[0], current[1],
                next[0], next[1],
                visibleColor
            );
        }
    }

    /**
     * Updates the star position.
     *
     * @param x the new horizontal coordinate
     * @param y the new vertical coordinate
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
}

