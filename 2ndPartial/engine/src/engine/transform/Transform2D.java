package engine.transform;

/**
 * Provides basic two-dimensional geometric transformations.
 */
public final class Transform2D {
   /**
     * Translates a point by dx and dy.
     *
     * P' = P + T
     */
    public static Point2D translate(
            Point2D point, double dx, double dy) {

        return new Point2D(
            point.getX() + dx,
            point.getY() + dy
        );
    }

    /**
     * Scales a point relative to the origin.
     *
     * P' = P * S
     */
    public static Point2D scale(
            Point2D point, double sx, double sy) {

        return new Point2D(
            point.getX() * sx,
            point.getY() * sy
        );
    }

    /**
     * Rotates a point around the origin.
     *
     * P' = P * R
     * The angle is specified in degrees.
     */
    public static Point2D rotate(
            Point2D point, double angleDegrees) {

        double theta = Math.toRadians(angleDegrees);

        double cos = Math.cos(theta);
        double sin = Math.sin(theta);

        double x = point.getX() * cos
                 - point.getY() * sin;

        double y = point.getX() * sin
                 + point.getY() * cos;

        return new Point2D(x, y);
    }
}

