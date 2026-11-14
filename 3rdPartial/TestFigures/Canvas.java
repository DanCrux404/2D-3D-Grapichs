import engine.core.Point3D;
import engine.shapes.Cube;
import engine.shapes.Line3D;
import engine.transform.ParallelProjection;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Canvas {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private final JFrame frame;
    private final JPanel panel;
    private final BufferedImage buffer;

    public Canvas() {

        buffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        frame = new JFrame("3D Graphics - TestFigures");

        panel =
                new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);

                        // Display the off-screen image.
                        g.drawImage(buffer, 0, 0, this);
                    }
                };

        initComponents();
        initializeBuffer();
        drawTestLine();
        drawTestCube();
        configureFrame();
    }

    private void initComponents() {

        panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        panel.setDoubleBuffered(true);
        panel.setBackground(Color.WHITE);
        frame.add(panel);
    }

    private void initializeBuffer() {

        Graphics g = buffer.getGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.dispose();
    }

    private void configureFrame() {

        frame.setTitle("3D Graphics - TestFigures");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    public void show() {
        frame.setVisible(true);
    }

    public BufferedImage getBuffer() {
        return buffer;
    }

    public void refresh() {
        panel.repaint();
    }

    /**
     * Draws a test line using parallel projection.
     *
     * <p>This method verifies the connection between the 3D projection system and the existing 2D
     * Bresenham algorithm.
     */
    private void drawTestLine() {

        // Define the projection direction.
        ParallelProjection projection = new ParallelProjection(2, 1, 5);

        // Create the 3D line renderer.
        Line3D line = new Line3D(projection);

        // Define the endpoints of the 3D line.
        Point3D p1 = new Point3D(250, 250, 0);
        Point3D p2 = new Point3D(350, 350, 100);

        // Project and draw the line.
        line.draw(buffer, p1, p2, Color.BLACK);
    }

    /** Draws a test cube using parallel projection. */
    private void drawTestCube() {

        ParallelProjection projection = new ParallelProjection(2, 1, 5);

        Cube cube = new Cube(150, 250, 200, projection);

        cube.draw(buffer, Color.RED);
    }
}
