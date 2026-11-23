package city.render;

import engine.core.Point3D;
import engine.core.ProjectedPoint;
import engine.transform.ParallelProjection;
import engine.transform.Point2D;
import engine.transform.Projection3D;

public final class LightViewProjection implements Projection3D {

    private static final double DEPTH_OFFSET = 12000.0;
    private static final double WORLD_EXTENT = 7000.0;

    private final double[][] worldToLight;
    private final Projection3D delegate;

    public LightViewProjection(Point3D lightPosition, Point3D target, int mapSize) {
        if (lightPosition == null || target == null || mapSize <= 0) {
            throw new IllegalArgumentException("Light, target, and positive map size are required.");
        }

        Point3D forward = normalize(subtract(target, lightPosition));
        Point3D hint = Math.abs(forward.getY()) > 0.99
                ? new Point3D(0.0, 0.0, 1.0)
                : new Point3D(0.0, 1.0, 0.0);
        Point3D right = normalize(cross(hint, forward));
        Point3D up = normalize(cross(forward, right));
        double scale = mapSize / (2.0 * WORLD_EXTENT);

        worldToLight =
                new double[][] {
                    {
                        right.getX() * scale,
                        right.getY() * scale,
                        right.getZ() * scale,
                        -dot(right, target) * scale + mapSize / 2.0
                    },
                    {
                        up.getX() * scale,
                        up.getY() * scale,
                        up.getZ() * scale,
                        -dot(up, target) * scale + mapSize / 2.0
                    },
                    {
                        forward.getX(),
                        forward.getY(),
                        forward.getZ(),
                        -dot(forward, target) + DEPTH_OFFSET
                    },
                    {0.0, 0.0, 0.0, 1.0}
                };

        delegate = new ParallelProjection(0.0, 0.0, 1.0);
    }

    @Override
    public Point2D project(Point3D point) {
        ProjectedPoint projected = projectWithDepth(point);
        return new Point2D(projected.getX(), projected.getY());
    }

    @Override
    public ProjectedPoint projectWithDepth(Point3D point) {
        double x = point.getX();
        double y = point.getY();
        double z = point.getZ();

        return delegate.projectWithDepth(
                new Point3D(
                        worldToLight[0][0] * x
                                + worldToLight[0][1] * y
                                + worldToLight[0][2] * z
                                + worldToLight[0][3],
                        worldToLight[1][0] * x
                                + worldToLight[1][1] * y
                                + worldToLight[1][2] * z
                                + worldToLight[1][3],
                        worldToLight[2][0] * x
                                + worldToLight[2][1] * y
                                + worldToLight[2][2] * z
                                + worldToLight[2][3]));
    }

    private static Point3D subtract(Point3D a, Point3D b) {
        return new Point3D(a.getX() - b.getX(), a.getY() - b.getY(), a.getZ() - b.getZ());
    }

    private static Point3D cross(Point3D a, Point3D b) {
        return new Point3D(
                a.getY() * b.getZ() - a.getZ() * b.getY(),
                a.getZ() * b.getX() - a.getX() * b.getZ(),
                a.getX() * b.getY() - a.getY() * b.getX());
    }

    private static double dot(Point3D a, Point3D b) {
        return a.getX() * b.getX() + a.getY() * b.getY() + a.getZ() * b.getZ();
    }

    private static Point3D normalize(Point3D vector) {
        double length = Math.sqrt(dot(vector, vector));

        if (length < 1e-12) {
            throw new IllegalArgumentException("Light basis vectors cannot be zero.");
        }

        return new Point3D(
                vector.getX() / length, vector.getY() / length, vector.getZ() / length);
    }
}
