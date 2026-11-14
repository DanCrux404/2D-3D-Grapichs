
package engine.transform;

import engine.core.Point3D;

/**
 * Performs parallel projection of three-dimensional points
 * onto the XY plane.
 *
 * <p>The projection is defined by a direction vector (xp, yp, zp).
 * A line is extended from the original point in the projection
 * direction until it intersects the XY plane, where z = 0.
 *
 * <p>The projected coordinates are calculated using:
 *
 * <pre>
 * x2 = x1 - (xp * z1) / zp
 * y2 = y1 - (yp * z1) / zp
 * </pre>
 *
 * <p>This class only performs mathematical calculations.
 * It does not draw pixels or interact with graphical components.
 */
public class ParallelProjection {

    private double xp;
    private double yp;
    private double zp;

    /**
     * Creates a parallel projection using the specified
     * direction vector.
     *
     * @param xp the x component of the projection direction
     * @param yp the y component of the projection direction
     * @param zp the z component of the projection direction
     * @throws IllegalArgumentException if any component is not finite
     *                                   or zp is zero
     */
    public ParallelProjection(double xp, double yp, double zp) {
        setDirection(xp, yp, zp);
    }

    /**
     * Projects a three-dimensional point onto the XY plane.
     *
     * @param point the original three-dimensional point
     * @return the projected point as a Point2D
     * @throws IllegalArgumentException if the point is null
     */
    public Point2D project(Point3D point) {

        if (point == null) {
            throw new IllegalArgumentException(
                "The point cannot be null."
            );
        }

        double x1 = point.getX();
        double y1 = point.getY();
        double z1 = point.getZ();

        double x2 = x1 - (xp * z1) / zp;
        double y2 = y1 - (yp * z1) / zp;

        return new Point2D(x2, y2);
    }

    /**
     * Updates the direction vector used for projection.
     *
     * @param xp the new x component
     * @param yp the new y component
     * @param zp the new z component
     * @throws IllegalArgumentException if any component is not finite
     *                                   or zp is zero
     */
    public void setDirection(double xp, double yp, double zp) {

        if (!Double.isFinite(xp)
                || !Double.isFinite(yp)
                || !Double.isFinite(zp)
                || zp == 0.0) {
            throw new IllegalArgumentException(
                "Projection direction must be finite and zp must not be zero."
            );
        }

        this.xp = xp;
        this.yp = yp;
        this.zp = zp;
    }

    /**
     * Returns the x component of the projection direction.
     *
     * @return the x component
     */
    public double getXp() {
        return xp;
    }

    /**
     * Returns the y component of the projection direction.
     *
     * @return the y component
     */
    public double getYp() {
        return yp;
    }

    /**
     * Returns the z component of the projection direction.
     *
     * @return the z component
     */
    public double getZp() {
        return zp;
    }
}

