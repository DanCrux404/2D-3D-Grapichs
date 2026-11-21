package engine.raster;

import engine.core.DepthBuffer;
import engine.core.ProjectedPoint;

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
}
