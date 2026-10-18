package engine.core;

import java.awt.Color;

/**
 * Defines an operation for plotting a colored pixel.
 */
@FunctionalInterface
public interface PixelPlotter {

    /**
     * Plots a pixel at the specified coordinates.
     *
     * @param x the pixel's x-coordinate
     * @param y the pixel's y-coordinate
     * @param color the pixel color
     */
    void plot(int x, int y, Color color);
}

