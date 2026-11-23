package city.render;

import engine.core.Point3D;

public record SurfaceVertex(Point3D camera, Point3D world, Point3D normal) {
}
