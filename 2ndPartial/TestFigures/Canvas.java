import engine.shapes.Line;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import engine.shapes.Rectangle;
import engine.shapes.Circle;
import engine.shapes.Ellipse;

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
        /*
        Line.dda(
            buffer,
            100, 100,
            650, 450,
            Color.RED
        );
        */

        //Draw a line using Bresenham algorithm.
        /*
        Line.bresenham(
            buffer,
            100, 100,
            650, 450,
            Color.BLACK
        );
        */

        //Draws a line using middle point algorithm.
        /*
        Line.midpoint(
            buffer,
            100, 100,
            650, 450,
            Color.MAGENTA
        );
        */

        //Draws a rectangle using bresemham algorithm.
        /*
        Rectangle.draw(
            buffer,
            100, 100,
            400, 300,
            Color.BLUE
        );
        */

        //Draws a circle with the math model
        /*
        Circle.draw(buffer, 400, 300, 150, Color.BLUE);
        Circle.draw(buffer, 200, 200, 80, Color.RED);
        Circle.draw(buffer, 600, 200, 80, Color.GREEN);
        */

        //Draws a circle with polar coordinates
        //Circle.polar(buffer, 400, 300, 150, Color.BLUE);

        //Draws a circle with the 8 side simmetry
        //Circle.symmetry8(buffer, 400, 300, 150, Color.MAGENTA);
        
        //Draws a circle using midpoint
        //Circle.midpoint(buffer, 400, 300, 150, Color.GREEN);
    
        //Draw a circile with bresenham
        //Circle.bresenham(buffer, 400, 300, 150, Color.ORANGE);
        
        //Draw a ellipse
        //Ellipse.midpoint(buffer, 400, 300, 150, 80, Color.CYAN);
        
        //Draw some figures for practice 12
        /*
            Line.midpoint(
            buffer,
            50, 50,
            150, 150,
            Color.BLACK
        );

        Line.midpoint(
            buffer,
            180, 100,
            330, 100,
            Color.BLUE
        );

        Line.midpoint(
            buffer,
            350, 150,
            450, 50,
            Color.MAGENTA
        );
        
        Line.midpoint(
            buffer,
            630, 100,
            480, 100,
            Color.RED
        );

        Circle.bresenham(buffer, 130, 300, 10, Color.GREEN);
        Circle.bresenham(buffer, 130, 300, 30, Color.GREEN);
        Circle.bresenham(buffer, 130, 300, 50, Color.GREEN);
        Circle.bresenham(buffer, 130, 300, 70, Color.GREEN);

        Rectangle.draw(
            buffer,
            250, 350,
            450, 250,
            Color.BLUE
        );

        Rectangle.draw(
            buffer,
            420, 320,
            280, 280,
            Color.RED
        );

        Ellipse.midpoint(buffer, 630, 300, 70, 10, Color.GRAY);
        Ellipse.midpoint(buffer, 630, 300, 100, 30, Color.GRAY);
        Ellipse.midpoint(buffer, 630, 300, 130, 40, Color.GRAY);
        Ellipse.midpoint(buffer, 630, 300, 150, 50, Color.GRAY);
        */

        // Dashed line: four pixels on, three off.
        /*
        Line.drawMasked(
            buffer, 80, 160, 700, 160,
            Line.DASHED, Color.BLUE
        );

        // Dash-dot style.
        Line.drawMasked(
            buffer, 80, 220, 700, 220,
            Line.DASH_DOT, Color.RED
        );

        // Dashed diagonal line.
        Line.drawMasked(
            buffer, 80, 280, 700, 450,
            Line.DASHED, Color.MAGENTA
        );
        */

        // Thin line.
        Line.thick(buffer, 80, 100, 700, 100, 1, Color.BLACK);

        // Thick horizontal line.
        Line.thick(buffer, 80, 170, 700, 170, 7, Color.BLUE);

        // Thick diagonal line.
        Line.thick(buffer, 80, 250, 700, 400, 7, Color.RED);

        // Thick, steep line.
        Line.thick(buffer, 150, 480, 300, 280, 5, Color.MAGENTA);

        // Vertical line.
        Line.thick(buffer, 700, 100, 700, 500, 9, Color.ORANGE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(buffer, 0, 0, null);
    }
}
