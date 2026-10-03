import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Window extends JFrame {
    private BufferedImage buffer;
    private Graphics pixelGraphics;

    public Window() {
        setTitle("Line Equation Algorithm - Optimized");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        buffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        pixelGraphics = (Graphics2D) buffer.createGraphics();
    }

    public void putPixel(int x, int y, Color color) {
        buffer.setRGB(0, 0, color.getRGB());
        this.getGraphics().drawImage(buffer, x, y, this);
    }

    // Optimized method based on the line equation (Incremental)
    public void drawLine(int x0, int y0, int x1, int y1, Color color) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        // Check whether the line is wider than it is tall, or taller than it is wide
        if (dx >= dy) {
            // HORIZONTAL OR SHALLOW LINES (Iterate over X)
            if (x0 > x1) {
                // Swap points to go from left to right
                int tempX = x0;
                x0 = x1;
                x1 = tempX;

                int tempY = y0;
                y0 = y1;
                y1 = tempY;
            }

            double slope = (double) (y1 - y0) / (x1 - x0);
            double y = y0; // Start at y0

            for (int x = x0; x <= x1; x++) {
                // Fast cast (int)(y + 0.5) instead of Math.round()
                putPixel(x, (int) (y + 0.5), color);
                y += slope; // Incremental addition, avoiding the multiplication mx + b
            }
        } else {
            // VERY STEEP OR VERTICAL LINES (Iterate over Y)
            if (y0 > y1) {
                // Swap points to go from top to bottom
                int tempX = x0;
                x0 = x1;
                x1 = tempX;

                int tempY = y0;
                y0 = y1;
                y1 = tempY;
            }

            // Calculate the "inverse slope" (how much X changes for each step in Y)
            double inverseSlope = (double) (x1 - x0) / (y1 - y0);
            double x = x0; // Start at x0

            for (int y = y0; y <= y1; y++) {
                putPixel((int) (x + 0.5), y, color);
                x += inverseSlope; // Incremental addition in X
            }
        }
    }

    public static void main(String[] args) {
        Window window = new Window();
        window.setVisible(true);

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Normal line
        window.drawLine(50, 100, 350, 250, Color.BLUE);

        // Very steep line (previously left gaps, now it is drawn correctly)
        window.drawLine(100, 50, 150, 400, Color.RED);

        // Vertical line (previously failed due to division by zero)
        window.drawLine(400, 50, 400, 400, Color.GREEN);
    }
}

