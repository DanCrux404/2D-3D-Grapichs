package city.render;

import engine.core.Point3D;
import engine.core.ProjectedPoint;

import java.util.ArrayList;
import java.util.List;

public final class AttributeClipper {

    private AttributeClipper() {}

    public static List<SurfaceVertex[]> clipTriangle(
            SurfaceVertex a, SurfaceVertex b, SurfaceVertex c, double near) {

        List<SurfaceVertex> polygon = new ArrayList<>(4);
        SurfaceVertex[] vertices = {a, b, c};

        for (int i = 0; i < 3; i++) {
            SurfaceVertex current = vertices[i];
            SurfaceVertex next = vertices[(i + 1) % 3];

            boolean currentInside = current.camera().getZ() >= near;
            boolean nextInside = next.camera().getZ() >= near;

            if (currentInside) {
                polygon.add(current);
            }

            if (currentInside != nextInside) {
                polygon.add(intersect(current, next, near));
            }
        }

        List<SurfaceVertex[]> triangles = new ArrayList<>(2);

        for (int i = 1; i + 1 < polygon.size(); i++) {
            triangles.add(
                    new SurfaceVertex[] {
                        polygon.get(0), polygon.get(i), polygon.get(i + 1)
                    });
        }

        return triangles;
    }

    public static List<ProjectedVertex[]> clipToScreen(
            ProjectedVertex a,
            ProjectedVertex b,
            ProjectedVertex c,
            int width,
            int height) {

        List<ProjectedVertex> polygon = new ArrayList<>(4);
        polygon.add(a);
        polygon.add(b);
        polygon.add(c);

        for (ScreenEdge edge : ScreenEdge.values()) {
            polygon = clipEdge(polygon, edge, width, height);

            if (polygon.size() < 3) {
                return List.of();
            }
        }

        List<ProjectedVertex[]> triangles = new ArrayList<>(2);

        for (int i = 1; i + 1 < polygon.size(); i++) {
            triangles.add(
                    new ProjectedVertex[] {
                        polygon.get(0), polygon.get(i), polygon.get(i + 1)
                    });
        }

        return triangles;
    }

    private static List<ProjectedVertex> clipEdge(
            List<ProjectedVertex> polygon, ScreenEdge edge, int maxX, int maxY) {

        List<ProjectedVertex> clipped = new ArrayList<>(polygon.size() + 1);

        for (int i = 0; i < polygon.size(); i++) {
            ProjectedVertex current = polygon.get(i);
            ProjectedVertex next = polygon.get((i + 1) % polygon.size());

            boolean currentInside = edge.inside(current, maxX, maxY);
            boolean nextInside = edge.inside(next, maxX, maxY);

            if (currentInside) {
                clipped.add(current);
            }

            if (currentInside != nextInside) {
                clipped.add(edge.intersect(current, next, maxX, maxY));
            }
        }

        return clipped;
    }

    private static ProjectedVertex interpolate(ProjectedVertex from, ProjectedVertex to, double t) {
        ProjectedPoint a = from.screen();
        ProjectedPoint b = to.screen();

        return new ProjectedVertex(
                new ProjectedPoint(
                        a.getX() + (b.getX() - a.getX()) * t,
                        a.getY() + (b.getY() - a.getY()) * t,
                        a.getDepth() + (b.getDepth() - a.getDepth()) * t),
                interpolate(from.vertex(), to.vertex(), t));
    }

    private static SurfaceVertex interpolate(SurfaceVertex from, SurfaceVertex to, double t) {
        return new SurfaceVertex(
                interpolate(from.camera(), to.camera(), t),
                interpolate(from.world(), to.world(), t),
                interpolate(from.normal(), to.normal(), t));
    }

    private static SurfaceVertex intersect(SurfaceVertex from, SurfaceVertex to, double near) {
        double fromZ = from.camera().getZ();
        double toZ = to.camera().getZ();
        double t = (near - fromZ) / (toZ - fromZ);

        return new SurfaceVertex(
                interpolate(from.camera(), to.camera(), t),
                interpolate(from.world(), to.world(), t),
                interpolate(from.normal(), to.normal(), t));
    }

    private static Point3D interpolate(Point3D from, Point3D to, double t) {
        return new Point3D(
                from.getX() + (to.getX() - from.getX()) * t,
                from.getY() + (to.getY() - from.getY()) * t,
                from.getZ() + (to.getZ() - from.getZ()) * t);
    }

    private enum ScreenEdge {
        LEFT {
            boolean inside(ProjectedVertex vertex, int maxX, int maxY) {
                return vertex.screen().getX() >= 0.0;
            }

            ProjectedVertex intersect(ProjectedVertex from, ProjectedVertex to, int maxX, int maxY) {
                return interpolate(
                        from,
                        to,
                        (0.0 - from.screen().getX())
                                / (to.screen().getX() - from.screen().getX()));
            }
        },
        RIGHT {
            boolean inside(ProjectedVertex vertex, int maxX, int maxY) {
                return vertex.screen().getX() <= maxX;
            }

            ProjectedVertex intersect(ProjectedVertex from, ProjectedVertex to, int maxX, int maxY) {
                return interpolate(
                        from,
                        to,
                        (maxX - from.screen().getX())
                                / (to.screen().getX() - from.screen().getX()));
            }
        },
        TOP {
            boolean inside(ProjectedVertex vertex, int maxX, int maxY) {
                return vertex.screen().getY() >= 0.0;
            }

            ProjectedVertex intersect(ProjectedVertex from, ProjectedVertex to, int maxX, int maxY) {
                return interpolate(
                        from,
                        to,
                        (0.0 - from.screen().getY())
                                / (to.screen().getY() - from.screen().getY()));
            }
        },
        BOTTOM {
            boolean inside(ProjectedVertex vertex, int maxX, int maxY) {
                return vertex.screen().getY() <= maxY;
            }

            ProjectedVertex intersect(ProjectedVertex from, ProjectedVertex to, int maxX, int maxY) {
                return interpolate(
                        from,
                        to,
                        (maxY - from.screen().getY())
                                / (to.screen().getY() - from.screen().getY()));
            }
        };

        abstract boolean inside(ProjectedVertex vertex, int maxX, int maxY);

        abstract ProjectedVertex intersect(
                ProjectedVertex from, ProjectedVertex to, int maxX, int maxY);
    }
}
