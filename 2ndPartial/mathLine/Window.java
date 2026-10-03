import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Window extends JFrame {
    private BufferedImage buffer;
    private Graphics pixelGraphics;

    public Window() {
        setTitle("Line Equation Algorithm");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        buffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        pixelGraphics = (Graphics2D) buffer.createGraphics();
    }

    public void putPixel(int x, int y, Color c) {
        buffer.setRGB(0, 0, c.getRGB());
        this.getGraphics().drawImage(buffer, x, y, this);
    }

    public void drawLine(int x0, int y0, int x1, int y1, Color c) {
        // Validation 1: Vertical line (dx = 0). Prevents division by zero.
        if (x0 == x1) {
            int minY = Math.min(y0, y1);
            int maxY = Math.max(y0, y1);
            for (int y = minY; y <= maxY; y++) {
                putPixel(x0, y, c);
            }
            return;
        }

        // Validation 2: If x0 is greater than x1, swap the points 
        // so that the 'for' loop always advances from left to right.
        if (x0 > x1) {
            int tempX = x0; x0 = x1; x1 = tempX;
            int tempY = y0; y0 = y1; y1 = tempY;
        }

        // Calculate m = (y1 - y0) / (x1 - x0)
        // Use double to prevent integer truncation from ruining the calculation
        double m = (double) (y1 - y0) / (x1 - x0);
        
        // Calculate b = y0 - (m * x0)
        double b = y0 - (m * x0);

        // Loop from x = x0 to x = x1
        for (int x = x0; x <= x1; x++) {
            // y = mx + b
            double y = (m * x) + b;
            
            // Draw pixel(x, round(y))
            putPixel(x, (int) Math.round(y), c);
        }
    }

    public static void main(String[] args) {
        Window w = new Window();
        w.setVisible(true);

        // Graphics in Java sometimes need the window to be 
        // fully visible before drawing using getGraphics()
        try {
            Thread.sleep(200); 
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Draw a test line
        w.drawLine(50, 100, 350, 250, Color.BLUE);
        w.drawLine(100, 50, 150, 400, Color.RED);
        w.drawLine(400, 50, 400, 400, Color.GREEN);
    }
}
