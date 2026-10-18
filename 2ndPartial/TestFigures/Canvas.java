import engine.shapes.Line;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import engine.shapes.Rectangle;
import engine.shapes.Circle;
import engine.shapes.Ellipse;
import engine.fill.ScanLine;
import engine.fill.FloodFill;
import engine.clipping.CohenSutherland;
import engine.clipping.PointClipping;
import engine.clipping.ClipWindow;

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

        /*
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
        */

        /*
        // Continuous circle.
        Circle.drawMasked(
            buffer, 150, 180, 70,
            "11111111", Color.BLACK
        );

        // Dashed circle.
        Circle.drawMasked(
            buffer, 350, 180, 70,
            "1111000", Color.BLUE
        );

        // Dotted circle.
        Circle.drawMasked(
            buffer, 550, 180, 70,
            "10", Color.RED
        );

        // Dash-dot style.
        Circle.drawMasked(
            buffer, 350, 400, 90,
            "111100010", Color.MAGENTA
        );
        */

        /*
        // Thin circle.
        Circle.thick(buffer, 140, 150, 55, 1, Color.BLACK);

        // Medium thickness.
        Circle.thick(buffer, 320, 150, 55, 3, Color.BLUE);

        // Thick circle.
        Circle.thick(buffer, 500, 150, 55, 7, Color.RED);

        // Very thick circle.
        Circle.thick(buffer, 320, 350, 75, 13, Color.MAGENTA);
        */

        /*
        // Define a polygon using its vertices.
        int[][] vertices = {
            {100, 100},
            {250, 80},
            {300, 180},
            {220, 260},
            {120, 220}
        };

        // Fill the polygon first.
        ScanLine.fill(buffer, vertices, Color.CYAN);

        // Draw its outline afterward.
        for (int i = 0; i < vertices.length; i++) {
            int[] a = vertices[i];
            int[] b = vertices[(i + 1) % vertices.length];

            Line.bresenham(
                buffer, a[0], a[1], b[0], b[1], Color.BLACK);
        }
        */

        /*
        // Define the polygon vertices.
        int[][] vertices = {
            {100, 100},
            {250, 80},
            {300, 180},
            {220, 260},
            {120, 220}
        };

        // Draw the closed outline first.
        for (int i = 0; i < vertices.length; i++) {
        int[] a = vertices[i];
        int[] b = vertices[(i + 1) % vertices.length];

        Line.bresenham(
            buffer, a[0], a[1], b[0], b[1], Color.BLACK);
        }

        // Fill from a point inside the polygon.
        FloodFill.fill(buffer, 180, 160, Color.CYAN);
        */
        /*
        // Draw the rectangle outline.
        Rectangle.draw(
            buffer, 50, 50, 250, 200, Color.BLACK
        );

        // Fill the enclosed region using span-based flood fill.
        FloodFill.spanFill(
            buffer, 100, 100, Color.RED
        );
        */

        /*
        // Clipping window boundaries.
        int xmin = 150;
        int ymin = 100;
        int xmax = 450;
        int ymax = 350;

        // Case AB: Partially visible.
        // Code 1 = 0001 (left), Code 2 = 1000 (top).
        drawClippedLine(buffer, 80, 200, 250, 50,
            xmin, ymin, xmax, ymax, Color.RED);

        // Case CD: Completely visible.
        // Code 1 = 0000, Code 2 = 0000.
        drawClippedLine(buffer, 200, 150, 400, 300,
            xmin, ymin, xmax, ymax, Color.GREEN);

        // Case EF: Partially visible.
        // Code 1 = 0100 (bottom), Code 2 = 0000 (inside).
        drawClippedLine(buffer, 300, 420, 350, 250,
            xmin, ymin, xmax, ymax, Color.BLUE);

        // Case GH: Trivially invisible.
        // Code 1 = 0010 (right), Code 2 = 0010 (right).
        drawClippedLine(buffer, 500, 150, 550, 300,
            xmin, ymin, xmax, ymax, Color.MAGENTA);

        // Case IJ: Invisible, but not trivially rejected.
        // Code 1 = 1000 (top), Code 2 = 0010 (right).
        drawClippedLine(buffer, 250, 50, 500, 200,
            xmin, ymin, xmax, ymax, Color.ORANGE);

        // Draw the clipping window LAST to keep its border visible.
        Rectangle.draw(
            buffer, xmin, ymin, xmax, ymax, Color.BLACK
        );
        */

        // Clipping window boundaries.
        int xmin = 150;
        int ymin = 100;
        int xmax = 450;
        int ymax = 350;

        // Create the clipping window.
        ClipWindow window = new ClipWindow(
            xmin, ymin, xmax, ymax
        );

        // Circle A: partially visible on the left.
        PointClipping.drawCircle(
            buffer, 180, 200, 80, Color.RED, window
        );

        // Circle B: completely visible.
        PointClipping.drawCircle(
            buffer, 300, 220, 60, Color.GREEN, window
        );

        // Circle C: partially visible at the top.
        PointClipping.drawCircle(
            buffer, 350, 100, 70, Color.BLUE, window
        );

        // Circle D: completely outside the window.
        PointClipping.drawCircle(
            buffer, 520, 220, 40, Color.MAGENTA, window
        );

        // Circle E: partially visible on the right.
        PointClipping.drawCircle(
            buffer, 430, 300, 65, Color.ORANGE, window
        );

        // Draw the clipping window LAST to keep its border visible.
        Rectangle.draw(
            buffer, xmin, ymin, xmax, ymax, Color.BLACK
        );

    }

    /**
    * Clips a line and draws its visible segment.
    *
    * @param buffer the image buffer
    * @param x1 the first endpoint x-coordinate
    * @param y1 the first endpoint y-coordinate
    * @param x2 the second endpoint x-coordinate
    * @param y2 the second endpoint y-coordinate
    * @param xmin the clipping window left boundary
    * @param ymin the clipping window top boundary
    * @param xmax the clipping window right boundary
    * @param ymax the clipping window bottom boundary
    * @param color the line color
    */
    private static void drawClippedLine(
            BufferedImage buffer,
            int x1, int y1, int x2, int y2,
            int xmin, int ymin, int xmax, int ymax,
            Color color) {

        int[] clippedLine = CohenSutherland.clip(
            x1, y1, x2, y2,
            xmin, ymin, xmax, ymax
        );

        if (clippedLine != null) {
            Line.bresenham(
                buffer,
                clippedLine[0], clippedLine[1],
                    clippedLine[2], clippedLine[3],
                    color
            );
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(buffer, 0, 0, null);
    }
}
