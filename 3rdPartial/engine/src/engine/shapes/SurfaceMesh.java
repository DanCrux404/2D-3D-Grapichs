package engine.shapes;

import java.awt.Color;
import java.awt.image.BufferedImage;

public interface SurfaceMesh {

    void translate(double dx, double dy, double dz);

    void scale(double sx, double sy, double sz);

    void rotateX(double angle);

    void rotateY(double angle);

    void rotateZ(double angle);

    void draw(BufferedImage buffer, Color color);

    default void drawRainbow(BufferedImage buffer) {
        draw(buffer, Color.BLUE);
    }

    /**
     * Draws the mesh using a gradient between two colors.
     *
     * @param buffer image where the mesh will be drawn
     * @param startColor initial gradient color
     * @param endColor final gradient color
     */
    default void drawGradient(BufferedImage buffer, Color startColor, Color endColor) {
        if (startColor == null || endColor == null) {
            throw new IllegalArgumentException("Gradient colors cannot be null.");
        }

        draw(buffer, startColor);
    }
}
