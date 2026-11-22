package engine.lighting;

import java.awt.Color;

/**
 * Represents the optical properties of a material.
 */
public class Material {

    private final Color baseColor;
    private final double ambient;
    private final double diffuse;
    private final double specular;
    private final double shininess;

    /**
     * Creates a material with Phong reflection coefficients.
     *
     * @param baseColor base color of the material
     * @param ambient ambient reflection coefficient
     * @param diffuse diffuse reflection coefficient
     * @param specular specular reflection coefficient
     * @param shininess specular shininess exponent
     */
    public Material(
            Color baseColor,
            double ambient,
            double diffuse,
            double specular,
            double shininess) {

        if (baseColor == null) {
            throw new IllegalArgumentException(
                    "Base color cannot be null.");
        }

        if (!isValidCoefficient(ambient)
                || !isValidCoefficient(diffuse)
                || !isValidCoefficient(specular)) {
            throw new IllegalArgumentException(
                    "Reflection coefficients must be between 0 and 1.");
        }

        if (!Double.isFinite(shininess) || shininess <= 0) {
            throw new IllegalArgumentException(
                    "Shininess must be finite and positive.");
        }

        this.baseColor = baseColor;
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.shininess = shininess;
    }

    private boolean isValidCoefficient(double value) {
        return Double.isFinite(value) && value >= 0 && value <= 1;
    }

    public Color getBaseColor() {
        return baseColor;
    }

    public double getAmbient() {
        return ambient;
    }

    public double getDiffuse() {
        return diffuse;
    }

    public double getSpecular() {
        return specular;
    }

    public double getShininess() {
        return shininess;
    }
}

