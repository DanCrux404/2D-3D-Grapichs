package engine.shapes;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

import engine.core.Point3D;
import engine.transform.Projection3D;
import engine.transform.Transform3D;

/**
 * Represents and draws an explicit mathematical curve in 3D.
 *
 * The curve is defined by a function y = f(x), an interval,
 * and a specified number of sample points. Consecutive points
 * are connected using 3D line segments.
 *
 * The class supports translation, scaling, and rotation
 * around the center of its original bounding box.
 */
public class ExplicitCurve3D {

    /** Original sampled points of the curve. */
    private final List<Point3D> points;

    /** Center of the original curve's bounding box. */
    private final Point3D center;

    /** Line renderer used to connect consecutive points. */
    private final Line3D line;

    /** Translation values along each coordinate axis. */
    private double translationX = 0.0;
    private double translationY = 0.0;
    private double translationZ = 0.0;

    /** Accumulated scaling factors along each coordinate axis. */
    private double scaleX = 1.0;
    private double scaleY = 1.0;
    private double scaleZ = 1.0;

    /** Accumulated rotation angles in degrees. */
    private double rotationX = 0.0;
    private double rotationY = 0.0;
    private double rotationZ = 0.0;

    /**
     * Creates an explicit 3D curve from a mathematical function.
     *
     * @param function function that defines y = f(x)
     * @param min minimum value of the independent variable
     * @param max maximum value of the independent variable
     * @param numberOfPoints number of sample points
     * @param projection 3D projection used to draw the curve
     * @throws IllegalArgumentException if an argument is invalid
     */
    public ExplicitCurve3D(
            DoubleUnaryOperator function,
            double min,
            double max,
            int numberOfPoints,
            Projection3D projection) {

        if (function == null || projection == null) {
            throw new IllegalArgumentException(
                "Function and projection cannot be null."
            );
        }

        if (!Double.isFinite(min) || !Double.isFinite(max)
                || min >= max) {
            throw new IllegalArgumentException(
                "Interval limits must be finite and min must be less than max."
            );
        }

        if (numberOfPoints < 2 || numberOfPoints > 1_000_000) {
            throw new IllegalArgumentException(
                "The number of points must be between 2 and 1,000,000."
            );
        }

        points = new ArrayList<>();

        double step = (max - min) / (numberOfPoints - 1);

        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        // Generate the curve's sample points.
        for (int i = 0; i < numberOfPoints; i++) {

            // Ensure the last point is exactly at the upper limit.
            double x = (i == numberOfPoints - 1)
                    ? max
                    : min + i * step;

            double y = function.applyAsDouble(x);

            if (!Double.isFinite(y)) {
                throw new IllegalArgumentException(
                    "The function must produce finite values in the interval."
                );
            }

            points.add(new Point3D(x, y, 0.0));

            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }

        // Calculate the center of the curve's bounding box.
        center = new Point3D(
            (min + max) / 2.0,
            (minY + maxY) / 2.0,
            0.0
        );

        line = new Line3D(projection);
    }

    /**
     * Translates the curve along the three coordinate axes.
     *
     * @param dx displacement along X
     * @param dy displacement along Y
     * @param dz displacement along Z
     */
    public void translate(double dx, double dy, double dz) {
        if (!Double.isFinite(dx) || !Double.isFinite(dy)
                || !Double.isFinite(dz)) {
            throw new IllegalArgumentException(
                "Translation values must be finite."
            );
        }

        translationX += dx;
        translationY += dy;
        translationZ += dz;
    }

    /**
     * Scales the curve relative to its own center.
     *
     * @param sx scaling factor along X
     * @param sy scaling factor along Y
     * @param sz scaling factor along Z
     */
    public void scale(double sx, double sy, double sz) {
        if (!Double.isFinite(sx) || !Double.isFinite(sy)
                || !Double.isFinite(sz)
                || sx <= 0.0 || sy <= 0.0 || sz <= 0.0) {
            throw new IllegalArgumentException(
                "Scaling factors must be positive and finite."
            );
        }

        scaleX *= sx;
        scaleY *= sy;
        scaleZ *= sz;
    }

    /**
     * Accumulates a rotation around the X axis.
     *
     * @param angle rotation angle in degrees
     */
    public void rotateX(double angle) {
        if (!Double.isFinite(angle)) {
            throw new IllegalArgumentException(
                "Rotation angle must be finite."
            );
        }

        rotationX += angle;
    }

    /**
     * Accumulates a rotation around the Y axis.
     *
     * @param angle rotation angle in degrees
     */
    public void rotateY(double angle) {
        if (!Double.isFinite(angle)) {
            throw new IllegalArgumentException(
                "Rotation angle must be finite."
            );
        }

        rotationY += angle;
    }

    /**
     * Accumulates a rotation around the Z axis.
     *
     * @param angle rotation angle in degrees
     */
    public void rotateZ(double angle) {
        if (!Double.isFinite(angle)) {
            throw new IllegalArgumentException(
                "Rotation angle must be finite."
            );
        }

        rotationZ += angle;
    }

    /**
     * Applies the current transformations to an original point.
     *
     * @param point original curve point
     * @param toOrigin translation matrix to move the center to the origin
     * @param scaling scaling matrix
     * @param rx rotation matrix around X
     * @param ry rotation matrix around Y
     * @param rz rotation matrix around Z
     * @param toPosition translation matrix to restore the center
     * @return transformed point
     */
    private Point3D transformPoint(
            Point3D point,
            double[][] toOrigin,
            double[][] scaling,
            double[][] rx,
            double[][] ry,
            double[][] rz,
            double[][] toPosition) {

        Point3D result = Transform3D.apply(point, toOrigin);

        result = Transform3D.apply(result, scaling);
        result = Transform3D.apply(result, rx);
        result = Transform3D.apply(result, ry);
        result = Transform3D.apply(result, rz);

        return Transform3D.apply(result, toPosition);
    }

    /**
     * Draws the curve by connecting its consecutive transformed points.
     *
     * @param buffer image buffer where the curve is drawn
     * @param color color used to draw the curve
     */
    public void draw(BufferedImage buffer, Color color) {

        double[][] toOrigin = Transform3D.translation(
            -center.getX(),
            -center.getY(),
            -center.getZ()
        );

        double[][] scaling = Transform3D.scaling(
            scaleX,
            scaleY,
            scaleZ
        );

        double[][] rx = Transform3D.rotationX(rotationX);
        double[][] ry = Transform3D.rotationY(rotationY);
        double[][] rz = Transform3D.rotationZ(rotationZ);

        double[][] toPosition = Transform3D.translation(
            center.getX() + translationX,
            center.getY() + translationY,
            center.getZ() + translationZ
        );

        // Transform all original points.
        List<Point3D> transformedPoints = new ArrayList<>();

        for (Point3D point : points) {
            transformedPoints.add(
                transformPoint(
                    point,
                    toOrigin,
                    scaling,
                    rx,
                    ry,
                    rz,
                    toPosition
                )
            );
        }

        // Connect consecutive points.
        for (int i = 0; i < transformedPoints.size() - 1; i++) {
            line.draw(
                buffer,
                transformedPoints.get(i),
                transformedPoints.get(i + 1),
                color
            );
        }
    }
}

