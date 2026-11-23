package city.scene;

import engine.core.Point3D;
import engine.lighting.Material;

import java.awt.Color;

/**
 * A world-space triangle with a flat color.
 *
 * <p>Geometry is stored in world coordinates so the render pipeline can apply the camera view
 * transform and near-plane clipping each frame. The color is mutable so animated elements (for
 * example, windows at night) can update without rebuilding geometry.
 */
public class WorldTriangle {

    private final Point3D a;
    private final Point3D b;
    private final Point3D c;
    private final SurfaceKind kind;
    private final Material material;
    private Color color;

    /**
     * Creates a triangle.
     *
     * @param a first world-space vertex
     * @param b second world-space vertex
     * @param c third world-space vertex
     * @param color flat fill color
     */
    public WorldTriangle(Point3D a, Point3D b, Point3D c, Color color) {
        this(a, b, c, color, SurfaceKind.UNLIT, null);
    }

    public WorldTriangle(
            Point3D a,
            Point3D b,
            Point3D c,
            Color color,
            SurfaceKind kind,
            Material material) {

        if (a == null || b == null || c == null || color == null || kind == null) {
            throw new IllegalArgumentException("Triangle vertices, color, and kind cannot be null.");
        }

        this.a = a;
        this.b = b;
        this.c = c;
        this.color = color;
        this.kind = kind;
        this.material = material;
    }

    public Point3D getA() {
        return a;
    }

    public Point3D getB() {
        return b;
    }

    public Point3D getC() {
        return c;
    }

    public Color getColor() {
        return color;
    }

    public SurfaceKind getKind() {
        return kind;
    }

    public Material getMaterial() {
        return material;
    }

    public Point3D getNormal() {
        double ux = b.getX() - a.getX();
        double uy = b.getY() - a.getY();
        double uz = b.getZ() - a.getZ();

        double vx = c.getX() - a.getX();
        double vy = c.getY() - a.getY();
        double vz = c.getZ() - a.getZ();

        double nx = uy * vz - uz * vy;
        double ny = uz * vx - ux * vz;
        double nz = ux * vy - uy * vx;

        double length = Math.hypot(Math.hypot(nx, ny), nz);

        if (length < 1e-12) {
            return new Point3D(0.0, 0.0, 0.0);
        }

        return new Point3D(nx / length, ny / length, nz / length);
    }

    /**
     * Replaces the fill color. The render thread reads it when drawing the next frame.
     *
     * @param color new fill color
     */
    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("Color cannot be null.");
        }

        this.color = color;
    }
}
