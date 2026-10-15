
import engine.shapes.Line;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class Canvas extends JPanel {

    private final BufferedImage buffer;

    public Canvas(int width, int height) {
        buffer = new BufferedImage(
            width,
            height,
            BufferedImage.TYPE_INT_RGB
        );

        setPreferredSize(new Dimension(width, height));

        // Clear the buffer with a white background.
        Graphics g = buffer.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.dispose();

        // Draw a line using the DDA algorithm.
        Line.dda(
            buffer,
            100, 100,
            650, 450,
            Color.RED
        );
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(buffer, 0, 0, null);
    }
}

