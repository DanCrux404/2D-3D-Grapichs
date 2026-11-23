package city.camera;

import engine.core.Point3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Clips camera-space triangles against the near plane {@code z >= near}.
 *
 * <p>The engine's {@code PerspectiveProjection.projectWithDepth} throws for points at or behind
 * the center of projection, so triangles crossing the near plane must be clipped before
 * projection. Uses Sutherland-Hodgman clipping against a single plane and fans the resulting
 * polygon back into triangles.
 */
public final class NearPlaneClipper {

    private NearPlaneClipper() {}

    /**
     * Clips one triangle.
     *
     * @param a first camera-space vertex
     * @param b second camera-space vertex
     * @param c third camera-space vertex
     * @param near minimum camera-space Z kept
     * @return zero, one, or two triangles fully in front of the near plane
     */
    public static List<Point3D[]> clipTriangle(Point3D a, Point3D b, Point3D c, double near) {
        List<Point3D> polygon = new ArrayList<>(4);
        Point3D[] vertices = {a, b, c};

        for (int i = 0; i < 3; i++) {
            Point3D current = vertices[i];
            Point3D next = vertices[(i + 1) % 3];

            boolean currentInside = current.getZ() >= near;
            boolean nextInside = next.getZ() >= near;

            if (currentInside) {
                polygon.add(current);
            }

            if (currentInside != nextInside) {
                polygon.add(intersect(current, next, near));
            }
        }

        List<Point3D[]> triangles = new ArrayList<>(2);

        for (int i = 1; i + 1 < polygon.size(); i++) {
            triangles.add(new Point3D[] {polygon.get(0), polygon.get(i), polygon.get(i + 1)});
        }

        return triangles;
    }

    private static Point3D intersect(Point3D from, Point3D to, double near) {
        double t = (near - from.getZ()) / (to.getZ() - from.getZ());

        return new Point3D(
                from.getX() + (to.getX() - from.getX()) * t,
                from.getY() + (to.getY() - from.getY()) * t,
                near);
    }
}
