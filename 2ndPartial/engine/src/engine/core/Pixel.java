package engine.core;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Pixel {
    public static void put(BufferedImage buffer, int x, int y, Color c) {
        if (x < 0 || y < 0 || x >= buffer.getWidth() || y >= buffer.getHeight()) return;
        buffer.setRGB(x, y, c.getRGB());
    }
}
