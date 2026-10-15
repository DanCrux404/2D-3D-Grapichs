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
        // 1. Calculate m = (y1 - y0) / (x1 - x0)
        double m = (double) (y1 - y0) / (x1 - x0);
        
        // 2. Calculate b = y0 - (m * x0)
        double b = y0 - (m * x0);

        // 3. Cicle x = x0 until x = x1
        for (int x = x0; x <= x1; x++) {
            // y = mx + b
            double y = (m * x) + b;
            
            // Paint pixel(x, round(y))
            putPixel(x, (int) Math.round(y), c);
        }
    }

    public static void main(String[] args) {
        Window w = new Window();
        w.setVisible(true);

        try {
            Thread.sleep(200); 
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 1. Works (left to right)
        w.drawLine(50, 100, 350, 250, Color.BLUE);
        // 2. It is drawn, but like point
        w.drawLine(100, 50, 150, 400, Color.RED);
        //It is not drawn
        w.drawLine(400, 50, 400, 400, Color.GREEN);
    }
}
