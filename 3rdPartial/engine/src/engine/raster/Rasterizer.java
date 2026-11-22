package engine.raster;

import engine.core.DepthBuffer;
import engine.core.Point3D;
import engine.core.ProjectedPoint;
import engine.lighting.Light;
import engine.lighting.Material;
import engine.lighting.PhongLighting;
import engine.shadow.ShadowMap;
import engine.transform.Projection3D;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Converts projected geometric primitives into pixels, using a depth buffer to determine
 * visibility.
 */
public class Rasterizer {

    private final DepthBuffer depthBuffer;

    /**
     * Creates a rasterizer using the specified depth buffer.
     *
     * @param depthBuffer buffer used for depth testing
     */
    public Rasterizer(DepthBuffer depthBuffer) {
        if (depthBuffer == null) {
            throw new IllegalArgumentException("Depth buffer cannot be null");
        }

        this.depthBuffer = depthBuffer;
    }

    /**
     * Draws a depth-aware line between two projected points.
     *
     * @param image destination image
     * @param p1 first projected point
     * @param p2 second projected point
     * @param color line color
     */
    public void drawLine(BufferedImage image, ProjectedPoint p1, ProjectedPoint p2, Color color) {
        validateArguments(image, color, p1, p2);

        int x0 = (int) Math.round(p1.getX());
        int y0 = (int) Math.round(p1.getY());

        int x1 = (int) Math.round(p2.getX());
        int y1 = (int) Math.round(p2.getY());

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int error = dx - dy;
        int totalSteps = Math.max(dx, dy);
        int currentStep = 0;

        while (true) {

            double t = totalSteps == 0 ? 0.0 : (double) currentStep / totalSteps;

            double depth = p1.getDepth() + t * (p2.getDepth() - p1.getDepth());

            depthBuffer.testAndSet(x0, y0, depth, color, image);

            if (x0 == x1 && y0 == y1) {
                break;
            }

            int error2 = 2 * error;

            if (error2 > -dy) {
                error -= dy;
                x0 += sx;
            }

            if (error2 < dx) {
                error += dx;
                y0 += sy;
            }

            currentStep++;
        }
    }

    /**
     * Fills a projected triangle using barycentric coordinates.
     *
     * @param image destination image
     * @param p1 first vertex
     * @param p2 second vertex
     * @param p3 third vertex
     * @param color triangle color
     */
    public void fillTriangle(
            BufferedImage image,
            ProjectedPoint p1,
            ProjectedPoint p2,
            ProjectedPoint p3,
            Color color) {
        validateArguments(image, color, p1, p2, p3);

        double x1 = p1.getX();
        double y1 = p1.getY();

        double x2 = p2.getX();
        double y2 = p2.getY();

        double x3 = p3.getX();
        double y3 = p3.getY();

        double denominator = (y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3);

        // Degenerate triangle: its vertices are collinear
        // or the triangle has zero area.
        if (Math.abs(denominator) < 1e-10) {
            return;
        }

        int minX = Math.max(0, (int) Math.floor(Math.min(x1, Math.min(x2, x3))));

        int maxX = Math.min(image.getWidth() - 1, (int) Math.ceil(Math.max(x1, Math.max(x2, x3))));

        int minY = Math.max(0, (int) Math.floor(Math.min(y1, Math.min(y2, y3))));

        int maxY = Math.min(image.getHeight() - 1, (int) Math.ceil(Math.max(y1, Math.max(y2, y3))));

        final double epsilon = 1e-10;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {

                // Evaluate at the center of the pixel.
                double px = x + 0.5;
                double py = y + 0.5;

                double alpha = ((y2 - y3) * (px - x3) + (x3 - x2) * (py - y3)) / denominator;

                double beta = ((y3 - y1) * (px - x3) + (x1 - x3) * (py - y3)) / denominator;

                double gamma = 1.0 - alpha - beta;

                // The pixel center must be inside the triangle.
                if (alpha >= -epsilon && beta >= -epsilon && gamma >= -epsilon) {

                    double depth =
                            alpha * p1.getDepth() + beta * p2.getDepth() + gamma * p3.getDepth();

                    depthBuffer.testAndSet(x, y, depth, color, image);
                }
            }
        }
    }

    /**
     * Fills a projected triangle using Gouraud shading. Vertex colors are interpolated across the
     * triangle using barycentric coordinates.
     *
     * @param image destination image
     * @param p1 first projected vertex
     * @param p2 second projected vertex
     * @param p3 third projected vertex
     * @param color1 illumination color at the first vertex
     * @param color2 illumination color at the second vertex
     * @param color3 illumination color at the third vertex
     */
    public void fillTriangle(
            BufferedImage image,
            ProjectedPoint p1,
            ProjectedPoint p2,
            ProjectedPoint p3,
            Color color1,
            Color color2,
            Color color3) {

        validateArguments(image, color1, p1, p2, p3);

        if (color2 == null || color3 == null) {
            throw new IllegalArgumentException("Vertex colors cannot be null");
        }

        double x1 = p1.getX();
        double y1 = p1.getY();

        double x2 = p2.getX();
        double y2 = p2.getY();

        double x3 = p3.getX();
        double y3 = p3.getY();

        double denominator = (y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3);

        if (Math.abs(denominator) < 1e-10) {
            return;
        }

        int minX = Math.max(0, (int) Math.floor(Math.min(x1, Math.min(x2, x3))));

        int maxX = Math.min(image.getWidth() - 1, (int) Math.ceil(Math.max(x1, Math.max(x2, x3))));

        int minY = Math.max(0, (int) Math.floor(Math.min(y1, Math.min(y2, y3))));

        int maxY = Math.min(image.getHeight() - 1, (int) Math.ceil(Math.max(y1, Math.max(y2, y3))));

        final double epsilon = 1e-10;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {

                double px = x + 0.5;
                double py = y + 0.5;

                double alpha = ((y2 - y3) * (px - x3) + (x3 - x2) * (py - y3)) / denominator;

                double beta = ((y3 - y1) * (px - x3) + (x1 - x3) * (py - y3)) / denominator;

                double gamma = 1.0 - alpha - beta;

                if (alpha >= -epsilon && beta >= -epsilon && gamma >= -epsilon) {

                    double depth =
                            alpha * p1.getDepth() + beta * p2.getDepth() + gamma * p3.getDepth();

                    int red =
                            interpolateChannel(
                                    alpha,
                                    beta,
                                    gamma,
                                    color1.getRed(),
                                    color2.getRed(),
                                    color3.getRed());

                    int green =
                            interpolateChannel(
                                    alpha,
                                    beta,
                                    gamma,
                                    color1.getGreen(),
                                    color2.getGreen(),
                                    color3.getGreen());

                    int blue =
                            interpolateChannel(
                                    alpha,
                                    beta,
                                    gamma,
                                    color1.getBlue(),
                                    color2.getBlue(),
                                    color3.getBlue());

                    Color interpolatedColor = new Color(red, green, blue);

                    depthBuffer.testAndSet(x, y, depth, interpolatedColor, image);
                }
            }
        }
    }

    /** Interpolates a color channel using barycentric coordinates. */
    private int interpolateChannel(
            double alpha, double beta, double gamma, int value1, int value2, int value3) {

        double value = alpha * value1 + beta * value2 + gamma * value3;

        return Math.max(0, Math.min(255, (int) Math.round(value)));
    }

    /** Validates the common arguments used by rasterization methods. */
    private void validateArguments(BufferedImage image, Color color, ProjectedPoint... points) {
        if (image == null || color == null) {
            throw new IllegalArgumentException("Image and color cannot be null");
        }

        if (image.getWidth() != depthBuffer.getWidth()
                || image.getHeight() != depthBuffer.getHeight()) {
            throw new IllegalArgumentException("Image dimensions must match the depth buffer");
        }

        for (ProjectedPoint point : points) {
            if (point == null) {
                throw new IllegalArgumentException("Projected points cannot be null");
            }

            if (!Double.isFinite(point.getX())
                    || !Double.isFinite(point.getY())
                    || !Double.isFinite(point.getDepth())) {
                throw new IllegalArgumentException(
                        "Projected point coordinates and depth must be finite");
            }
        }
    }

    /**
     * Fills a projected triangle using per-pixel Phong shading.
     *
     * <p>Surface positions and normals are interpolated across the triangle. The illumination model
     * is evaluated independently for every covered pixel.
     *
     * @param image destination image
     * @param p1 first projected vertex
     * @param p2 second projected vertex
     * @param p3 third projected vertex
     * @param position1 world-space position of the first vertex
     * @param position2 world-space position of the second vertex
     * @param position3 world-space position of the third vertex
     * @param normal1 normal at the first vertex
     * @param normal2 normal at the second vertex
     * @param normal3 normal at the third vertex
     * @param light point light source
     * @param material surface material
     * @param lighting Phong illumination calculator
     * @param viewDirection direction toward the viewer
     * @param perspectiveCorrect whether perspective-correct interpolation is required
     */
    public void fillTrianglePhong(
            BufferedImage image,
            ProjectedPoint p1,
            ProjectedPoint p2,
            ProjectedPoint p3,
            Point3D position1,
            Point3D position2,
            Point3D position3,
            Point3D normal1,
            Point3D normal2,
            Point3D normal3,
            Light light,
            Material material,
            PhongLighting lighting,
            Point3D viewDirection,
            boolean perspectiveCorrect,
            ShadowMap shadowMap,
            Projection3D lightProjection,
            double shadowBias) {

        validateArguments(image, Color.WHITE, p1, p2, p3);

        if (position1 == null
                || position2 == null
                || position3 == null
                || normal1 == null
                || normal2 == null
                || normal3 == null
                || light == null
                || material == null
                || lighting == null
                || viewDirection == null) {
            throw new IllegalArgumentException("Phong shading parameters cannot be null.");
        }

        if ((shadowMap == null) != (lightProjection == null)) {
            throw new IllegalArgumentException(
                    "Shadow map and light projection must be provided together.");
        }

        if (!Double.isFinite(shadowBias) || shadowBias < 0.0) {
            throw new IllegalArgumentException("Shadow bias must be finite and non-negative.");
        }

        double x1 = p1.getX();
        double y1 = p1.getY();

        double x2 = p2.getX();
        double y2 = p2.getY();

        double x3 = p3.getX();
        double y3 = p3.getY();

        double denominator = (y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3);

        if (Math.abs(denominator) < 1e-10) {
            return;
        }

        int minX = Math.max(0, (int) Math.floor(Math.min(x1, Math.min(x2, x3))));

        int maxX = Math.min(image.getWidth() - 1, (int) Math.ceil(Math.max(x1, Math.max(x2, x3))));

        int minY = Math.max(0, (int) Math.floor(Math.min(y1, Math.min(y2, y3))));

        int maxY = Math.min(image.getHeight() - 1, (int) Math.ceil(Math.max(y1, Math.max(y2, y3))));

        final double epsilon = 1e-10;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {

                double px = x + 0.5;
                double py = y + 0.5;

                double alpha = ((y2 - y3) * (px - x3) + (x3 - x2) * (py - y3)) / denominator;

                double beta = ((y3 - y1) * (px - x3) + (x1 - x3) * (py - y3)) / denominator;

                double gamma = 1.0 - alpha - beta;

                if (alpha < -epsilon || beta < -epsilon || gamma < -epsilon) {
                    continue;
                }

                // Depth remains linearly interpolated in screen space.
                double depth = alpha * p1.getDepth() + beta * p2.getDepth() + gamma * p3.getDepth();

                double w1 = alpha;
                double w2 = beta;
                double w3 = gamma;

                // Correct the interpolation of 3D attributes under perspective.
                if (perspectiveCorrect) {
                    double denominatorWeights =
                            alpha * p1.getDepth() + beta * p2.getDepth() + gamma * p3.getDepth();

                    if (Math.abs(denominatorWeights) < 1e-12) {
                        continue;
                    }

                    w1 = alpha * p1.getDepth() / denominatorWeights;
                    w2 = beta * p2.getDepth() / denominatorWeights;
                    w3 = gamma * p3.getDepth() / denominatorWeights;
                }

                Point3D interpolatedPosition =
                        interpolatePoint(position1, position2, position3, w1, w2, w3);

                Point3D interpolatedNormal =
                        interpolatePoint(normal1, normal2, normal3, w1, w2, w3);

                // By default, the pixel receives direct lighting.
                double visibility = 1.0;

                if (shadowMap != null) {

                    ProjectedPoint lightPoint =
                            lightProjection.projectWithDepth(interpolatedPosition);

                    int shadowX = (int) Math.round(lightPoint.getX());
                    int shadowY = (int) Math.round(lightPoint.getY());

                    if (shadowMap.isInShadow(shadowX, shadowY, lightPoint.getDepth(), shadowBias)) {

                        visibility = 0.0;
                    }
                }

                Color pixelColor =
                        lighting.calculateColor(
                                interpolatedPosition,
                                interpolatedNormal,
                                viewDirection,
                                light,
                                material,
                                visibility);

                depthBuffer.testAndSet(x, y, depth, pixelColor, image);
            }
        }
    }

    /** Interpolates three 3D vectors using the supplied weights. */
    private Point3D interpolatePoint(
            Point3D p1, Point3D p2, Point3D p3, double w1, double w2, double w3) {

        double x = w1 * p1.getX() + w2 * p2.getX() + w3 * p3.getX();

        double y = w1 * p1.getY() + w2 * p2.getY() + w3 * p3.getY();

        double z = w1 * p1.getZ() + w2 * p2.getZ() + w3 * p3.getZ();

        return new Point3D(x, y, z);
    }
}
