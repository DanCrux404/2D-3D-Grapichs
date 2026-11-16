package engine.transform;

import engine.core.Point3D;

/**
 * Performs perspective projection of three-dimensional points onto the XY plane.
 *
 * <p>The projection is calculated using a center of projection located at (xc, yc, zc).
 *
 * <p>The projected point is obtained by finding the intersection between the line from the center
 * of projection to the 3D point and the projection plane (z = 0).
 */
public class PerspectiveProjection implements Projection3D{

    private final double xc;
    private final double yc;
    private final double zc;

    /**
     * Creates a perspective projection with the specified center of projection.
     *
     * @param xc the X coordinate of the projection center
     * @param yc the Y coordinate of the projection center
     * @param zc the Z coordinate of the projection center
     */
    public PerspectiveProjection(double xc, double yc, double zc) {

        if (!Double.isFinite(xc) || !Double.isFinite(yc) || !Double.isFinite(zc)) {

            throw new IllegalArgumentException("Projection center coordinates must be finite.");
        }

        this.xc = xc;
        this.yc = yc;
        this.zc = zc;
    }

    /**
     * Projects a three-dimensional point onto the XY plane.
     *
     * @param point the 3D point to project
     * @return the resulting two-dimensional point
     * @throws IllegalArgumentException if the point is null or lies on the projection plane or
     *     center of projection
     */
    public Point2D project(Point3D point) {

        if (point == null) {
            throw new IllegalArgumentException("Point cannot be null.");
        }

        double x1 = point.getX();
        double y1 = point.getY();
        double z1 = point.getZ();

        double denominator = z1 - zc;

        if (denominator == 0.0) {
            throw new IllegalArgumentException("Point cannot be projected from the given center.");
        }

        double u = -zc / denominator;

        double x = xc + (x1 - xc) * u;
        double y = yc + (y1 - yc) * u;

        return new Point2D(x, y);
    }

    public double getXc() {
        return xc;
    }

    public double getYc() {
        return yc;
    }

    public double getZc() {
        return zc;
    }
}
