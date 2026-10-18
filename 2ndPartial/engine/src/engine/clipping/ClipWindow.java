
package engine.clipping;

/**
 * Represents a rectangular clipping window for point-based clipping.
 */
public class ClipWindow {

    private final int xmin;
    private final int ymin;
    private final int xmax;
    private final int ymax;

    /**
     * Creates a rectangular clipping window.
     *
     * @param xmin the minimum x-coordinate
     * @param ymin the minimum y-coordinate
     * @param xmax the maximum x-coordinate
     * @param ymax the maximum y-coordinate
     */
    public ClipWindow(int xmin, int ymin, int xmax, int ymax) {
        if (xmin > xmax || ymin > ymax) {
            throw new IllegalArgumentException(
                "Invalid clipping window boundaries."
            );
        }

        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;
    }

    /**
     * Checks whether a point lies inside the clipping window.
     *
     * @param x the point's x-coordinate
     * @param y the point's y-coordinate
     * @return true if the point is inside or on the boundary
     */
    public boolean contains(int x, int y) {
        return x >= xmin && x <= xmax
            && y >= ymin && y <= ymax;
    }
}

