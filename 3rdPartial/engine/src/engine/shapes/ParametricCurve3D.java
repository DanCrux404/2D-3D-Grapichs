package engine.shapes;

import engine.core.Point3D;
import engine.transform.Projection3D;
import engine.transform.Transform3D;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class ParametricCurve3D {

    private final List<Point3D> points;
    private final Point3D center;
    private final Line3D line;

    private double translationX = 0.0;
    private double translationY = 0.0;
    private double translationZ = 0.0;

    private double scaleX = 1.0;
    private double scaleY = 1.0;
    private double scaleZ = 1.0;

    private double rotationX = 0.0;
    private double rotationY = 0.0;
    private double rotationZ = 0.0;

    public ParametricCurve3D(
            DoubleUnaryOperator xFunction,
            DoubleUnaryOperator yFunction,
            DoubleUnaryOperator zFunction,
            double min,
            double max,
            int numberOfPoints,
            Projection3D projection) {

        // Validations
        if (xFunction == null || yFunction == null || zFunction == null) {
            throw new IllegalArgumentException("Curve functions cannot be null.");
        }

        if (!Double.isFinite(min) || !Double.isFinite(max) || min >= max) {
            throw new IllegalArgumentException("Invalid parameter interval.");
        }

        if (numberOfPoints < 2 || numberOfPoints > 1_000_000) {
            throw new IllegalArgumentException("Number of points must be between 2 and 1,000,000.");
        }

        if (projection == null) {
            throw new IllegalArgumentException("Projection cannot be null.");
        }

        // Generate points
        points = new ArrayList<>();
        double step = (max - min) / (numberOfPoints - 1);
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;

        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        double minZ = Double.POSITIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < numberOfPoints; i++) {

            double t = (i == numberOfPoints - 1) ? max : min + i * step;

            double x = xFunction.applyAsDouble(t);
            double y = yFunction.applyAsDouble(t);
            double z = zFunction.applyAsDouble(t);

            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {

                throw new IllegalArgumentException("Curve coordinates must be finite.");
            }

            points.add(new Point3D(x, y, z));

            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);

            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);

            minZ = Math.min(minZ, z);
            maxZ = Math.max(maxZ, z);
        }

        // Calculate center
        center = new Point3D((minX + maxX) / 2.0, (minY + maxY) / 2.0, (minZ + maxZ) / 2.0);

        // Initialize Line3D
        line = new Line3D(projection);
    }

    public void translate(double dx, double dy, double dz) {

        if (!Double.isFinite(dx) || !Double.isFinite(dy) || !Double.isFinite(dz)) {

            throw new IllegalArgumentException("Translation values must be finite.");
        }

        translationX += dx;
        translationY += dy;
        translationZ += dz;
    }

    public void scale(double sx, double sy, double sz) {

        if (!Double.isFinite(sx)
                || !Double.isFinite(sy)
                || !Double.isFinite(sz)
                || sx <= 0
                || sy <= 0
                || sz <= 0) {

            throw new IllegalArgumentException("Scale values must be positive and finite.");
        }

        scaleX *= sx;
        scaleY *= sy;
        scaleZ *= sz;
    }

    public void rotateX(double angle) {
        if (!Double.isFinite(angle)) {
            throw new IllegalArgumentException("Rotation angle must be finite.");
        }

        rotationX += angle;
    }

    public void rotateY(double angle) {
        if (!Double.isFinite(angle)) {
            throw new IllegalArgumentException("Rotation angle must be finite.");
        }

        rotationY += angle;
    }

    public void rotateZ(double angle) {
        if (!Double.isFinite(angle)) {
            throw new IllegalArgumentException("Rotation angle must be finite.");
        }

        rotationZ += angle;
    }

    private Point3D transformPoint(Point3D point) {

        double[][] toOrigin =
                Transform3D.translation(-center.getX(), -center.getY(), -center.getZ());

        double[][] scaling = Transform3D.scaling(scaleX, scaleY, scaleZ);

        double[][] rx = Transform3D.rotationX(rotationX);
        double[][] ry = Transform3D.rotationY(rotationY);
        double[][] rz = Transform3D.rotationZ(rotationZ);

        double[][] toPosition =
                Transform3D.translation(
                        center.getX() + translationX,
                        center.getY() + translationY,
                        center.getZ() + translationZ);

        Point3D result = Transform3D.apply(point, toOrigin);
        result = Transform3D.apply(result, scaling);
        result = Transform3D.apply(result, rx);
        result = Transform3D.apply(result, ry);
        result = Transform3D.apply(result, rz);
        result = Transform3D.apply(result, toPosition);

        return result;
    }

    public void draw(BufferedImage buffer, Color color) {

        if (buffer == null || color == null) {
            throw new IllegalArgumentException("Buffer and color cannot be null.");
        }

        List<Point3D> transformedPoints = new ArrayList<>();

        for (Point3D point : points) {
            transformedPoints.add(transformPoint(point));
        }

        for (int i = 0; i < transformedPoints.size() - 1; i++) {

            Point3D p1 = transformedPoints.get(i);
            Point3D p2 = transformedPoints.get(i + 1);

            line.draw(buffer, p1, p2, color);
        }
    }
}
