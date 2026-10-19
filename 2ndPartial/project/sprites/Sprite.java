package project.sprites;

import java.awt.Color;
import java.awt.image.BufferedImage;

import engine.core.Pixel;

/**
 * Represents a pixel-art sprite using a color-index matrix.
 *
 * The value -1 represents a transparent cell.
 * Other values index the supplied color palette.
 */
public class Sprite {

    private final int[][] pixels;
    private final Color[] palette;

    /**
     * Creates a sprite from a pixel matrix and color palette.
     *
     * @param pixels the matrix of color indices
     * @param palette the colors used by the sprite
     */
    public Sprite(int[][] pixels, Color[] palette) {
        if (pixels == null || palette == null || pixels.length == 0) {
            throw new IllegalArgumentException(
                "Sprite matrix and palette must be valid."
            );
        }

        int width = pixels[0].length;

        if (width == 0) {
            throw new IllegalArgumentException(
                "Sprite rows cannot be empty."
            );
        }

        for (int[] row : pixels) {
            if (row == null || row.length != width) {
                throw new IllegalArgumentException(
                    "All sprite rows must have the same width."
                );
            }

            for (int index : row) {
                if (index < -1 || index >= palette.length) {
                    throw new IllegalArgumentException(
                        "Invalid sprite palette index."
                    );
                }
            }
        }

        this.pixels = pixels;
        this.palette = palette;
    }

    /**
     * Draws the sprite into the image buffer.
     *
     * @param buffer the destination image
     * @param x the top-left x-coordinate
     * @param y the top-left y-coordinate
     * @param scale the size of each sprite cell in pixels
     */
    public void draw(
            BufferedImage buffer,
            int x,
            int y,
            int scale) {

        if (buffer == null || scale <= 0) {
            throw new IllegalArgumentException(
                "Buffer must be valid and scale must be positive."
            );
        }

        for (int row = 0; row < pixels.length; row++) {
            for (int col = 0; col < pixels[row].length; col++) {

                int index = pixels[row][col];

                if (index == -1) {
                    continue;
                }

                Color color = palette[index];

                if (color == null) {
                    continue;
                }

                int pixelX = x + col * scale;
                int pixelY = y + row * scale;

                for (int dy = 0; dy < scale; dy++) {
                    for (int dx = 0; dx < scale; dx++) {
                        Pixel.put(
                            buffer,
                            pixelX + dx,
                            pixelY + dy,
                            color
                        );
                    }
                }
            }
        }
    }
}

