import engine.core.Point3D;
import engine.shapes.Cube;
import engine.shapes.Line3D;
import engine.transform.ParallelProjection;
import engine.transform.PerspectiveProjection;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class Canvas {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private final JFrame frame;
    private final JPanel panel;
    private final BufferedImage buffer;
    private static final double TRANSLATION_STEP = 10.0;
    private Cube perspectiveCube;

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
        // drawTestLine();
        // drawTestCube();
        // drawPerspectiveLine();
        drawPerspectiveCube();
        configureFrame();
        configureKeyBindings();
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

    private void drawTestCube() {

        ParallelProjection projection = new ParallelProjection(2, 1, 5);

        Point3D center = new Point3D(400, 300, 0);

        Cube cube = new Cube(center, 10, projection);

        cube.draw(buffer, Color.RED);
    }

    private void drawPerspectiveLine() {

        PerspectiveProjection projection = new PerspectiveProjection(400, 300, -500);

        Line3D line = new Line3D(projection);

        Point3D p1 = new Point3D(150, 150, 100);
        Point3D p2 = new Point3D(350, 250, 300);

        line.draw(buffer, p1, p2, Color.BLUE);
    }

    private void drawPerspectiveCube() {

        PerspectiveProjection projection = new PerspectiveProjection(400, 300, -500);

        Point3D center = new Point3D(400, 300, 200);

        perspectiveCube = new Cube(center, 20, projection);

        perspectiveCube.draw(buffer, Color.RED);
    }

    /**
     * Configures keyboard controls for translating the cube.
     *
     * <p>Arrow keys translate the cube along the X and Y axes. Page Up and Page Down translate it
     * along the Z axis.
     */
    private void configureKeyBindings() {

        bindKey(KeyEvent.VK_LEFT, "moveLeft", -TRANSLATION_STEP, 0, 0);
        bindKey(KeyEvent.VK_RIGHT, "moveRight", TRANSLATION_STEP, 0, 0);

        bindKey(KeyEvent.VK_UP, "moveUp", 0, -TRANSLATION_STEP, 0);
        bindKey(KeyEvent.VK_DOWN, "moveDown", 0, TRANSLATION_STEP, 0);

        bindKey(KeyEvent.VK_E, "moveForward", 0, 0, -TRANSLATION_STEP);
        bindKey(KeyEvent.VK_R, "moveBackward", 0, 0, TRANSLATION_STEP);
    }

    /**
     * Associates a keyboard key with a cube translation.
     *
     * @param keyCode the keyboard key code
     * @param actionName the name assigned to the action
     * @param dx translation distance along the X axis
     * @param dy translation distance along the Y axis
     * @param dz translation distance along the Z axis
     */
    private void bindKey(int keyCode, String actionName, double dx, double dy, double dz) {

        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(keyCode, 0), actionName);

        panel.getActionMap()
                .put(
                        actionName,
                        new AbstractAction() {

                            @Override
                            public void actionPerformed(ActionEvent e) {

                                perspectiveCube.translate(dx, dy, dz);

                                redrawScene();
                            }
                        });
    }

    /** Clears the buffer and redraws the scene. */
    private void redrawScene() {

        initializeBuffer();

        perspectiveCube.draw(buffer, Color.RED);

        panel.repaint();
    }
}
