package engine.lighting;

import engine.core.Point3D;
import java.awt.Color;

/**
 * Represents a point light source in a 3D scene.
 */
public class Light {

    private final Point3D position;
    private final Color color;
    private final double intensity;

    /**
     * Creates a point light source.
     *
     * @param position position of the light in 3D space
     * @param color color emitted by the light
     * @param intensity intensity of the light
     */
    public Light(Point3D position, Color color, double intensity) {

        if (position == null || color == null) {
            throw new IllegalArgumentException(
                    "Position and color cannot be null.");
        }

        if (!Double.isFinite(intensity) || intensity < 0) {
            throw new IllegalArgumentException(
                    "Light intensity must be finite and non-negative.");
        }

        this.position = position;
        this.color = color;
        this.intensity = intensity;
    }

    /**
     * Returns the light position.
     *
     * @return light position
     */
    public Point3D getPosition() {
        return position;
    }

    /**
     * Returns the light color.
     *
     * @return light color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Returns the light intensity.
     *
     * @return light intensity
     */
    public double getIntensity() {
        return intensity;
    }
}

