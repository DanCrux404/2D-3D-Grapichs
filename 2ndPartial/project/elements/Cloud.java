package project.elements;

import engine.core.Pixel;
import engine.fill.FloodFill;
import engine.shapes.Ellipse;

import java.awt.Color;
import java.awt.image.BufferedImage;

/** Represents a fluffy cloud in the city scene. */
public class Cloud {

    private int x;
    private int y;
    private final int size;
    private Color color;

    /**
     * Creates a cloud at the specified position.
     *
     * @param x the horizontal center coordinate
     * @param y the vertical center coordinate
     * @param size the approximate cloud size
     * @param color the cloud color
     */
    public Cloud(int x, int y, int size, Color color) {
        if (size < 12) {
            throw new IllegalArgumentException("Cloud size must be at least 12.");
        }

        if (color == null) {
            throw new IllegalArgumentException("Cloud color cannot be null.");
        }

        this.x = x;
        this.y = y;
        this.size = size;
        this.color = color;
    }

    /** Draws a fluffy cloud using overlapping filled ellipses. */
    public void draw(BufferedImage buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException("Buffer cannot be null.");
        }

        int baseY = y + size / 4;

        // Main body of the cloud
        drawFilledEllipse(buffer, x, baseY, size / 2, size / 4);

        // Four rounded bumps
        drawFilledEllipse(buffer, x - size / 3, y, size / 3, size / 3);

        drawFilledEllipse(buffer, x - size / 8, y - size / 6, size / 3, size / 2);

        drawFilledEllipse(buffer, x + size / 5, y - size / 12, size / 3, size / 3);
    }

    /** Draws and fills an ellipse. */
    private void drawFilledEllipse(
            BufferedImage buffer, int centerX, int centerY, int radiusX, int radiusY) {
        int padding = 2;

        int tempWidth = radiusX * 2 + padding * 2 + 1;
        int tempHeight = radiusY * 2 + padding * 2 + 1;

        BufferedImage temp = new BufferedImage(tempWidth, tempHeight, BufferedImage.TYPE_INT_RGB);

        Color background = new Color(1, 2, 3);

        // Temporal background draw
        for (int py = 0; py < tempHeight; py++) {
            for (int px = 0; px < tempWidth; px++) {
                Pixel.put(temp, px, py, background);
            }
        }

        int localX = radiusX + padding;
        int localY = radiusY + padding;

        // Draw edge and fill single ellipse.
        Ellipse.midpoint(temp, localX, localY, radiusX, radiusY, color);

        FloodFill.spanFill(temp, localX, localY, color);

        // Copy just cloud pixels.
        for (int py = 0; py < tempHeight; py++) {
            for (int px = 0; px < tempWidth; px++) {
                if (temp.getRGB(px, py) == color.getRGB()) {
                    int destX = centerX - radiusX - padding + px;
                    int destY = centerY - radiusY - padding + py;

                    Pixel.put(buffer, destX, destY, color);
                }
            }
        }
    }

    /**
     * Updates the cloud position.
     *
     * @param x the new horizontal coordinate
     * @param y the new vertical coordinate
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("Cloud color cannot be null.");
        }

        this.color = color;
    }
}
