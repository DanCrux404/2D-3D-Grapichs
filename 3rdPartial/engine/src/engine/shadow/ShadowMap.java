package engine.shadow;

import java.util.Arrays;

/**
 * Stores depth information from the light's point of view.
 *
 * <p>Larger depth values represent fragments closer to the light.
 */
public class ShadowMap {

    private final double[][] depths;
    private final int width;
    private final int height;

    /**
     * Creates a shadow map with the specified dimensions.
     *
     * @param width shadow map width
     * @param height shadow map height
     */
    public ShadowMap(int width, int height) {

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                "Width and height must be positive."
            );
        }

        this.width = width;
        this.height = height;
        this.depths = new double[height][width];

        clear();
    }

    /**
     * Resets all depth values.
     */
    public void clear() {
        for (double[] row : depths) {
            Arrays.fill(row, Double.NEGATIVE_INFINITY);
        }
    }

    /**
     * Stores a depth value if it is closer than the current one.
     *
     * @param x pixel x coordinate
     * @param y pixel y coordinate
     * @param depth fragment depth
     * @return true if the depth was stored
     */
    public boolean setDepth(int x, int y, double depth) {

        if (!Double.isFinite(depth)) {
            throw new IllegalArgumentException(
                "Depth must be finite."
            );
        }

        if (!isInside(x, y)) {
            return false;
        }

        if (depth > depths[y][x]) {
            depths[y][x] = depth;
            return true;
        }

        return false;
    }

    /**
     * Determines whether a fragment is occluded by another fragment.
     *
     * @param x pixel x coordinate
     * @param y pixel y coordinate
     * @param depth current fragment depth
     * @param bias depth tolerance to reduce shadow artifacts
     * @return true if another fragment blocks the light
     */
    public boolean isInShadow(
        int x,
        int y,
        double depth,
        double bias
    ) {

        if (!Double.isFinite(depth)
                || !Double.isFinite(bias)
                || bias < 0.0) {
            throw new IllegalArgumentException(
                "Depth must be finite and bias must be non-negative."
            );
        }

        if (!isInside(x, y)) {
            return false;
        }

        double storedDepth = depths[y][x];

        return storedDepth > depth + bias;
    }

    /**
     * Returns the stored depth at a pixel.
     *
     * @param x pixel x coordinate
     * @param y pixel y coordinate
     * @return stored depth
     */
    public double getDepth(int x, int y) {

        if (!isInside(x, y)) {
            throw new IndexOutOfBoundsException(
                "Pixel coordinates are outside the shadow map."
            );
        }

        return depths[y][x];
    }

    private boolean isInside(int x, int y) {
        return x >= 0 && x < width
            && y >= 0 && y < height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}

