package project.elements;

import engine.fill.ScanLine;
import engine.shapes.Rectangle;

import java.awt.Color;
import java.awt.image.BufferedImage;

/** Represents a building in the city. */
public class Building {

    private final int x;
    private final int baseY;
    private final int width;
    private final int height;
    private final Color facadeColor;

    private double nightIntensity;

    private static final Color OUTLINE_COLOR = Color.BLACK;
    private static final Color DAY_WINDOW_COLOR = new Color(255, 225, 145);
    private static final Color NIGHT_WINDOW_COLOR = new Color(255, 195, 95);
    private static final Color DARK_WINDOW_COLOR = new Color(48, 55, 75);

    public Building(int x, int baseY, int width, int height, Color facadeColor) {

        if (width < 40 || height < 60) {
            throw new IllegalArgumentException(
                    "Building width must be at least 40 and height at least 60.");
        }

        if (facadeColor == null) {
            throw new IllegalArgumentException("Facade color cannot be null.");
        }

        this.x = x;
        this.baseY = baseY;
        this.width = width;
        this.height = height;
        this.facadeColor = facadeColor;
        this.nightIntensity = 0.0;
    }

    /** Sets the night-lighting intensity from 0.0 to 1.0. */
    public void setNightIntensity(double intensity) {
        nightIntensity = Math.max(0.0, Math.min(1.0, intensity));
    }

    /** Compatibility method for callers using the old night-mode API. */
    public void setNightMode(boolean nightMode) {
        setNightIntensity(nightMode ? 1.0 : 0.0);
    }

    public void draw(BufferedImage buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException("Image buffer cannot be null.");
        }

        int left = x;
        int top = baseY - height;
        int right = x + width;
        int bottom = baseY;

        int[][] vertices = {
            {left, top},
            {right, top},
            {right, bottom},
            {left, bottom}
        };

        ScanLine.fill(buffer, vertices, facadeColor);

        Rectangle.draw(buffer, left, top, right, bottom, OUTLINE_COLOR);

        drawWindows(buffer, left, top, right, bottom);
    }

    private void drawWindows(BufferedImage buffer, int left, int top, int right, int bottom) {

        int windowWidth = 12;
        int windowHeight = 18;
        int horizontalSpacing = 24;
        int verticalSpacing = 30;

        Color windowColor = blendColor(DAY_WINDOW_COLOR, NIGHT_WINDOW_COLOR, nightIntensity);

        for (int row = 0, wy = top + 15;
                wy + windowHeight < bottom - 8;
                row++, wy += verticalSpacing) {

            for (int col = 0, wx = left + 10;
                    wx + windowWidth < right - 8;
                    col++, wx += horizontalSpacing) {

                // Stable pattern: some windows remain dark.
                int pattern = Math.abs((x * 31 + row * 17 + col * 13) % 7);

                boolean normallyLit = pattern < 4;

                // During the day, use a subtle pale window color.
                // At night, illuminate only selected windows.
                Color actualColor;

                if (nightIntensity <= 0.0) {
                    actualColor = DARK_WINDOW_COLOR;
                } else if (!normallyLit) {
                    actualColor = DARK_WINDOW_COLOR;
                } else {
                    actualColor = blendColor(DAY_WINDOW_COLOR, NIGHT_WINDOW_COLOR, nightIntensity);
                }

                Rectangle.draw(buffer, wx, wy, wx + windowWidth, wy + windowHeight, actualColor);
            }
        }
    }

    private static Color blendColor(Color start, Color end, double t) {

        t = Math.max(0.0, Math.min(1.0, t));

        int red = (int) Math.round(start.getRed() + (end.getRed() - start.getRed()) * t);

        int green = (int) Math.round(start.getGreen() + (end.getGreen() - start.getGreen()) * t);

        int blue = (int) Math.round(start.getBlue() + (end.getBlue() - start.getBlue()) * t);

        return new Color(red, green, blue);
    }

    public int getX() {
        return x;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getBaseY() {
        return baseY;
    }
}
