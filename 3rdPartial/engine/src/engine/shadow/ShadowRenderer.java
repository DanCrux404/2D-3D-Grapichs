package engine.shadow;

import engine.core.Point3D;
import engine.core.ProjectedPoint;
import engine.transform.Projection3D;

/**
 * Renders triangle geometry from the light's point of view
 * and stores its depth in a ShadowMap.
 */
public class ShadowRenderer {

    private static final double EPSILON = 1e-9;

    private final ShadowMap shadowMap;
    private final Projection3D lightProjection;

    /**
     * Creates a shadow renderer.
     *
     * @param shadowMap destination shadow map
     * @param lightProjection projection used to view the scene from the light
     */
    public ShadowRenderer(
        ShadowMap shadowMap,
        Projection3D lightProjection
    ) {

        if (shadowMap == null || lightProjection == null) {
            throw new IllegalArgumentException(
                "Shadow map and light projection cannot be null."
            );
        }

        this.shadowMap = shadowMap;
        this.lightProjection = lightProjection;
    }

    /**
     * Clears the shadow map.
     */
    public void clear() {
        shadowMap.clear();
    }

    /**
     * Projects and rasterizes a triangle into the shadow map.
     *
     * @param a first vertex
     * @param b second vertex
     * @param c third vertex
     */
    public void renderTriangle(
        Point3D a,
        Point3D b,
        Point3D c
    ) {

        if (a == null || b == null || c == null) {
            throw new IllegalArgumentException(
                "Triangle vertices cannot be null."
            );
        }

        ProjectedPoint p1 = lightProjection.projectWithDepth(a);
        ProjectedPoint p2 = lightProjection.projectWithDepth(b);
        ProjectedPoint p3 = lightProjection.projectWithDepth(c);

        rasterizeTriangle(p1, p2, p3);
    }

    /**
     * Rasterizes a projected triangle and stores its interpolated depth.
     */
    private void rasterizeTriangle(
        ProjectedPoint p1,
        ProjectedPoint p2,
        ProjectedPoint p3
    ) {

        double x1 = p1.getX();
        double y1 = p1.getY();

        double x2 = p2.getX();
        double y2 = p2.getY();

        double x3 = p3.getX();
        double y3 = p3.getY();

        double d1 = p1.getDepth();
        double d2 = p2.getDepth();
        double d3 = p3.getDepth();

        double denominator =
            (y2 - y3) * (x1 - x3)
            + (x3 - x2) * (y1 - y3);

        if (Math.abs(denominator) < EPSILON) {
            return;
        }

        int minX = Math.max(
            0,
            (int) Math.ceil(Math.min(x1, Math.min(x2, x3)) - 0.5)
        );

        int maxX = Math.min(
            shadowMap.getWidth() - 1,
            (int) Math.floor(Math.max(x1, Math.max(x2, x3)) - 0.5)
        );

        int minY = Math.max(
            0,
            (int) Math.ceil(Math.min(y1, Math.min(y2, y3)) - 0.5)
        );

        int maxY = Math.min(
            shadowMap.getHeight() - 1,
            (int) Math.floor(Math.max(y1, Math.max(y2, y3)) - 0.5)
        );

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {

                double px = x + 0.5;
                double py = y + 0.5;

                double alpha = (
                    (y2 - y3) * (px - x3)
                    + (x3 - x2) * (py - y3)
                ) / denominator;

                double beta = (
                    (y3 - y1) * (px - x3)
                    + (x1 - x3) * (py - y3)
                ) / denominator;

                double gamma = 1.0 - alpha - beta;

                if (alpha >= -EPSILON
                        && beta >= -EPSILON
                        && gamma >= -EPSILON) {

                    double depth =
                        alpha * d1
                        + beta * d2
                        + gamma * d3;

                    shadowMap.setDepth(x, y, depth);
                }
            }
        }
    }

    public ShadowMap getShadowMap() {
        return shadowMap;
    }
}

