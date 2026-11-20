package engine.util;

import java.awt.Color;

/**
 * Utility methods for color operations.
 */
public final class ColorUtils {

    private ColorUtils() {
        // Prevent instantiation.
    }

    /**
     * Interpolates between two colors.
     *
     * @param start initial color
     * @param end final color
     * @param fraction interpolation position (0.0 to 1.0)
     * @return interpolated color
     */
    public static Color interpolate(
        Color start,
        Color end,
        double fraction
    ) {
        if (start == null || end == null) {
            throw new IllegalArgumentException(
                "Colors cannot be null."
            );
        }

        if (!Double.isFinite(fraction)
                || fraction < 0.0
                || fraction > 1.0) {
            throw new IllegalArgumentException(
                "Fraction must be between 0.0 and 1.0."
            );
        }

        int red = (int) Math.round(
            start.getRed()
            + (end.getRed() - start.getRed()) * fraction
        );

        int green = (int) Math.round(
            start.getGreen()
            + (end.getGreen() - start.getGreen()) * fraction
        );

        int blue = (int) Math.round(
            start.getBlue()
            + (end.getBlue() - start.getBlue()) * fraction
        );

        int alpha = (int) Math.round(
            start.getAlpha()
            + (end.getAlpha() - start.getAlpha()) * fraction
        );

        return new Color(red, green, blue, alpha);
    }

    /**
     * Generates a rainbow color.
     *
     * @param fraction normalized position (0.0 to 1.0)
     * @return corresponding rainbow color
     */
    public static Color rainbow(double fraction) {
        if (!Double.isFinite(fraction)
                || fraction < 0.0
                || fraction > 1.0) {
            throw new IllegalArgumentException(
                "Fraction must be between 0.0 and 1.0."
            );
        }

        float hue = (float) (0.85 * (1.0 - fraction));

        return Color.getHSBColor(hue, 1.0f, 1.0f);
    }
}

