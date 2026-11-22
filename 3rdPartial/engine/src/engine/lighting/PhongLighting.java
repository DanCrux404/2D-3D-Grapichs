package engine.lighting;

import engine.core.Point3D;

import java.awt.Color;

/** Calculates illumination using the Phong reflection model. */
public class PhongLighting {

    /**
     * Calculates the illuminated color at a surface point.
     *
     * @param point position of the surface point
     * @param normal normal vector of the surface
     * @param viewDirection direction toward the viewer
     * @param light point light source
     * @param material surface material
     * @return resulting illuminated color
     */
    public Color calculateColor(
            Point3D point, Point3D normal, Point3D viewDirection, Light light, Material material) {

        // Por defecto, el punto recibe toda la iluminación directa.
        return calculateColor(point, normal, viewDirection, light, material, 1.0);
    }

    public Color calculateColor(
            Point3D point,
            Point3D normal,
            Point3D viewDirection,
            Light light,
            Material material,
            double visibility) {

        if (point == null
                || normal == null
                || viewDirection == null
                || light == null
                || material == null) {
            throw new IllegalArgumentException("Lighting parameters cannot be null.");
        }

        if (!Double.isFinite(visibility) || visibility < 0.0 || visibility > 1.0) {
            throw new IllegalArgumentException("Visibility must be between 0 and 1.");
        }

        double[] n = normalize(normal);
        double[] l = normalize(subtract(light.getPosition(), point));
        double[] v = normalize(viewDirection);

        double diffuseFactor = Math.max(0.0, dot(n, l));

        double[] reflected = {
            2.0 * diffuseFactor * n[0] - l[0],
            2.0 * diffuseFactor * n[1] - l[1],
            2.0 * diffuseFactor * n[2] - l[2]
        };

        double specularFactor = 0.0;

        if (diffuseFactor > 0.0) {
            specularFactor =
                    Math.pow(Math.max(0.0, dot(normalize(reflected), v)), material.getShininess());
        }

        double distance = distance(point, light.getPosition());
        double attenuation = 1.0 / (1.0 + 0.000005 * distance * distance);

        Color base = material.getBaseColor();
        Color lightColor = light.getColor();

        // La iluminación ambiental permanece independiente de las sombras.
        double ambient = material.getAmbient();

        double diffuse =
                material.getDiffuse()
                        * diffuseFactor
                        * light.getIntensity()
                        * attenuation
                        * visibility;

        double specular =
                material.getSpecular()
                        * specularFactor
                        * light.getIntensity()
                        * attenuation
                        * visibility;

        int red = calculateChannel(base.getRed(), lightColor.getRed(), ambient, diffuse, specular);

        int green =
                calculateChannel(
                        base.getGreen(), lightColor.getGreen(), ambient, diffuse, specular);

        int blue =
                calculateChannel(base.getBlue(), lightColor.getBlue(), ambient, diffuse, specular);

        return new Color(red, green, blue);
    }

    private int calculateChannel(
            int base, int light, double ambient, double diffuse, double specular) {

        double result = base * ambient + base * (light / 255.0) * diffuse + light * specular;

        return (int) Math.round(Math.min(255.0, Math.max(0.0, result)));
    }

    private double[] normalize(Point3D vector) {
        double magnitude =
                Math.sqrt(
                        vector.getX() * vector.getX()
                                + vector.getY() * vector.getY()
                                + vector.getZ() * vector.getZ());

        if (magnitude == 0.0) {
            throw new IllegalArgumentException("Direction vectors cannot have zero magnitude.");
        }

        return new double[] {
            vector.getX() / magnitude, vector.getY() / magnitude, vector.getZ() / magnitude
        };
    }

    private double[] normalize(double[] vector) {
        double magnitude =
                Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2]);

        if (magnitude == 0.0) {
            return new double[] {0.0, 0.0, 0.0};
        }

        return new double[] {vector[0] / magnitude, vector[1] / magnitude, vector[2] / magnitude};
    }

    private double[] subtract(Point3D a, Point3D b) {
        return new double[] {a.getX() - b.getX(), a.getY() - b.getY(), a.getZ() - b.getZ()};
    }

    private double dot(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    private double distance(Point3D a, Point3D b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double dz = a.getZ() - b.getZ();

        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
