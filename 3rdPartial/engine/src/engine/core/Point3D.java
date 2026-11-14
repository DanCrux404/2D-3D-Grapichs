
package engine.core;

/**
 * Represents a point in three-dimensional space.
 *
 * <p>A point is defined by three coordinates:
 * x, y and z.
 */
public class Point3D {

    private final double x;
    private final double y;
    private final double z;

    /**
     * Creates a three-dimensional point.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param z the z-coordinate
     */
    public Point3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Returns the x-coordinate.
     *
     * @return the x-coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the y-coordinate.
     *
     * @return the y-coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Returns the z-coordinate.
     *
     * @return the z-coordinate
     */
    public double getZ() {
        return z;
    }
}

