package city.scene;

import engine.core.Point3D;
import engine.util.ColorUtils;

import java.awt.Color;
import java.util.List;

/**
 * A reusable window quad on a building wall.
 *
 * <p>The quad is built slightly outside its wall to avoid Z-fighting. Its color transitions from a
 * cool daytime blue-gray to warm yellow through {@link #setNightIntensity(float)}, so the
 * day/night cycle can drive it in later phases without rebuilding geometry.
 */
public class Window {

    private static final Color DAY_COLOR = new Color(60, 85, 110);
    private static final Color NIGHT_COLOR = new Color(255, 215, 130);

    private final List<WorldTriangle> triangles;
    private float nightIntensity;

    /**
     * Creates a window from its four world-space corners in clockwise order.
     *
     * @param p1 first corner
     * @param p2 second corner
     * @param p3 third corner
     * @param p4 fourth corner
     */
    public Window(Point3D p1, Point3D p2, Point3D p3, Point3D p4) {
        triangles =
                List.of(
                        new WorldTriangle(p1, p2, p3, DAY_COLOR, SurfaceKind.WINDOW, null),
                        new WorldTriangle(p1, p3, p4, DAY_COLOR, SurfaceKind.WINDOW, null));
    }

    /** Returns the two triangles that make up this window. */
    public List<WorldTriangle> triangles() {
        return triangles;
    }

    /**
     * Sets how much the night color contributes.
     *
     * @param intensity 0 for daytime blue-gray, 1 for warm night yellow
     */
    public void setNightIntensity(float intensity) {
        if (!Float.isFinite(intensity)) {
            throw new IllegalArgumentException("Night intensity must be finite.");
        }

        nightIntensity = Math.max(0.0f, Math.min(1.0f, intensity));

        Color color = ColorUtils.interpolate(DAY_COLOR, NIGHT_COLOR, nightIntensity);

        for (WorldTriangle triangle : triangles) {
            triangle.setColor(color);
        }
    }

    /** Returns the current night intensity. */
    public float getNightIntensity() {
        return nightIntensity;
    }
}
