package engine.transform;

import engine.core.Point3D;
import engine.core.ProjectedPoint;

/** Defines the behavior of a three-dimensional projection. */
public interface Projection3D {

    /**
     * Projects a 3D point into a 2D point.
     *
     * @param point the 3D point to project
     * @return the projected 2D point
     */
    Point2D project(Point3D point);
    ProjectedPoint projectWithDepth(Point3D point);
}
