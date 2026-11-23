package city.scene;

import engine.lighting.Material;

import java.awt.Color;

public final class DynamicMaterial extends Material {

    private final double dayAmbient;
    private final double dayDiffuse;

    private volatile double ambient;
    private volatile double diffuse;

    public DynamicMaterial(
            Color baseColor,
            double ambient,
            double diffuse,
            double specular,
            double shininess) {

        super(baseColor, ambient, diffuse, specular, shininess);
        this.dayAmbient = ambient;
        this.dayDiffuse = diffuse;
        this.ambient = ambient;
        this.diffuse = diffuse;
    }

    public void setDaylightFactor(double factor) {
        double clamped = Math.max(0.0, Math.min(1.0, factor));
        ambient = dayAmbient * clamped;
        diffuse = dayDiffuse;
    }

    public double getDayAmbient() {
        return dayAmbient;
    }

    public double getDayDiffuse() {
        return dayDiffuse;
    }

    @Override
    public double getAmbient() {
        return ambient;
    }

    @Override
    public double getDiffuse() {
        return diffuse;
    }
}
