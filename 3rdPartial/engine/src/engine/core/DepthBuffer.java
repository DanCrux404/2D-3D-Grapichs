package engine.core;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class DepthBuffer {

    private final double[][] depths;
    private final int width;
    private final int height;

    /**
     * Creates a depth buffer with the specified dimensions.
     *
     * @param width buffer width
     * @param height buffer height
     */
    public DepthBuffer(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                "Width and height must be positive"
            );
        }

        this.width = width;
        this.height = height;
        this.depths = new double[height][width];

        clear();
    }

    /**
     * Resets every pixel to the initial depth.
     * Larger depth values represent closer fragments.
     */
    public void clear() {
        for (double[] row : depths) {
            Arrays.fill(row, Double.NEGATIVE_INFINITY);
        }
    }

    /**
     * Tests a fragment against the stored depth.
     * If it is closer, its depth and color are stored.
     *
     * @param x pixel x coordinate
     * @param y pixel y coordinate
     * @param depth fragment depth
     * @param color fragment color
     * @param image destination image
     * @return true if the fragment was written
     */
    public boolean testAndSet(
        int x,
        int y,
        double depth,
        Color color,
        BufferedImage image
    ) {
        if (image == null || color == null) {
            throw new IllegalArgumentException(
                "Image and color cannot be null"
            );
        }

        if (!Double.isFinite(depth)) {
            throw new IllegalArgumentException(
                "Depth must be finite"
            );
        }

        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }

        if (image.getWidth() != width || image.getHeight() != height) {
            throw new IllegalArgumentException(
                "Image dimensions must match the depth buffer"
            );
        }

        if (depth > depths[y][x]) {
            depths[y][x] = depth;
            image.setRGB(x, y, color.getRGB());
            return true;
        }

        return false;
    }

    /**
     * Returns the stored depth at a pixel.
     *
     * @param x pixel x coordinate
     * @param y pixel y coordinate
     * @return stored depth
     */
    public double getDepth(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IndexOutOfBoundsException(
                "Pixel coordinates are outside the buffer"
            );
        }

        return depths[y][x];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}

