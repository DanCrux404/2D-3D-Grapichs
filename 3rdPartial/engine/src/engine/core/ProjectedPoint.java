package engine.core;

/**
 * Represents a 3D point after projection onto the 2D screen,
 * including its depth information.
 */
public class ProjectedPoint {

    private final double x;
    private final double y;
    private final double depth;

    /**
     * Creates a projected point.
     *
     * @param x screen horizontal coordinate
     * @param y screen vertical coordinate
     * @param depth proximity value; greater values represent
     *              points closer to the viewer
     */
    public ProjectedPoint(double x, double y, double depth) {
        if (!Double.isFinite(x)
                || !Double.isFinite(y)
                || !Double.isFinite(depth)) {
            throw new IllegalArgumentException(
                "Projected coordinates and depth must be finite."
            );
        }

        this.x = x;
        this.y = y;
        this.depth = depth;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getDepth() {
        return depth;
    }
}

