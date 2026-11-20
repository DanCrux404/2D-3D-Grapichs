import engine.core.Point3D;
import engine.shapes.Cube;
import engine.shapes.ExplicitCurve3D;
import engine.shapes.ExplicitSurface3D;
import engine.shapes.Line3D;
import engine.shapes.MeshType;
import engine.shapes.ParametricCurve3D;
import engine.shapes.ParametricSurface3D;
import engine.transform.ParallelProjection;
import engine.transform.PerspectiveProjection;
import engine.transform.Transform3D;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

public class Canvas {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private final JFrame frame;
    private final JPanel panel;
    private final BufferedImage buffer;
    private static final double TRANSLATION_STEP = 10.0;
    private Cube perspectiveCube;
    private ExplicitCurve3D explicitCurve;
    private Timer curveTimer;
    private ParametricCurve3D parametricCurve;
    private Timer parametricTimer;
    private ExplicitSurface3D explicitSurface;
    private ParametricSurface3D parametricSurface;

    /** Timer responsible for the automatic cube rotation. */
    private Timer animationTimer;

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
        // drawPerspectiveCube();
        // testCubeScaling();
        // startAnimation();
        // testScaling();
        // Keyboard-controlled rotation:
        // testExplicitCurveKeyboard();

        // Automatic rotation:
        // testExplicitCurveAutomatic();
        // testParametricCurveKeyboard();
        // testParametricCurveAutomatic();
        // testExplicitSurface();
        // testParametricSurface();
        setupCylinder();
        redrawCylinderScene();
        animateCylinder();
        configureFrame();
        // configureKeyBindings();
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

    private void testScaling() {

        Point3D point = new Point3D(10, 20, 30);

        double[][] matrix = Transform3D.scaling(2, 2, 2);

        Point3D scaledPoint = Transform3D.apply(point, matrix);

        System.out.println(
                "Original: (" + point.getX() + ", " + point.getY() + ", " + point.getZ() + ")");

        System.out.println(
                "Escalado: ("
                        + scaledPoint.getX()
                        + ", "
                        + scaledPoint.getY()
                        + ", "
                        + scaledPoint.getZ()
                        + ")");
    }

    private void testCubeScaling() {

        PerspectiveProjection projection = new PerspectiveProjection(700, 450, -500);

        Point3D center = new Point3D(400, 300, 200);

        perspectiveCube = new Cube(center, 20, projection);

        // Apply scaling.
        // perspectiveCube.scale(1, 0.5, 1);

        // perspectiveCube.rotateX(30);
        // perspectiveCube.rotateY(30);
        perspectiveCube.rotateZ(30);

        perspectiveCube.draw(buffer, Color.RED);

        perspectiveCube.draw(buffer, Color.RED);
    }

    /** Starts the automatic rotation of the cube around its three axes. */
    private void startAnimation() {
        animationTimer =
                new Timer(
                        // Why 16? because is on miliseconds
                        // between updates that is closer to
                        // 60 updates peer second.
                        16,
                        e -> {

                            // Rotate around the X axis.
                            // perspectiveCube.rotateX(0.5);

                            // Rotate around the Y axis.
                            // perspectiveCube.rotateY(1.0);

                            // Rotate around the Z axis.
                            perspectiveCube.rotateZ(2.0);

                            // Redraw the scene with the updated rotation.
                            redrawScene();
                        });

        animationTimer.start();
    }

    private void setupExplicitCurve() {
        PerspectiveProjection projection = new PerspectiveProjection(400, 300, -500);

        explicitCurve =
                new ExplicitCurve3D(x -> Math.cos(x), -2 * Math.PI, 2 * Math.PI, 100, projection);

        // Adjust the curve's dimensions.
        explicitCurve.scale(40, 40, 1);

        // Position the curve in the 3D scene.
        explicitCurve.translate(400, 300, 200);
    }

    private void redrawCurveScene() {
        Graphics2D g = buffer.createGraphics();

        g.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());

        g.dispose();

        explicitCurve.draw(buffer, Color.BLACK);

        panel.repaint();
    }

    /** Initializes the explicit curve for keyboard-controlled rotation. */
    private void testExplicitCurveKeyboard() {
        setupExplicitCurve();

        bindCurveRotationKey("W", 2, 0, 0);
        bindCurveRotationKey("S", -2, 0, 0);

        bindCurveRotationKey("A", 0, 2, 0);
        bindCurveRotationKey("D", 0, -2, 0);

        bindCurveRotationKey("Q", 0, 0, 2);
        bindCurveRotationKey("E", 0, 0, -2);

        redrawCurveScene();
    }

    /**
     * Binds a keyboard key to a curve rotation.
     *
     * @param key keyboard key
     * @param angleX rotation increment around X
     * @param angleY rotation increment around Y
     * @param angleZ rotation increment around Z
     */
    private void bindCurveRotationKey(String key, double angleX, double angleY, double angleZ) {

        String actionName = "curveRotation" + key;

        panel.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke(key), actionName);

        panel.getActionMap()
                .put(
                        actionName,
                        new javax.swing.AbstractAction() {
                            @Override
                            public void actionPerformed(java.awt.event.ActionEvent e) {

                                explicitCurve.rotateX(angleX);
                                explicitCurve.rotateY(angleY);
                                explicitCurve.rotateZ(angleZ);

                                redrawCurveScene();
                            }
                        });
    }

    /** Starts the automatic rotation of the explicit curve. */
    private void testExplicitCurveAutomatic() {
        setupExplicitCurve();

        curveTimer =
                new Timer(
                        16,
                        e -> {
                            explicitCurve.rotateX(0.5);
                            explicitCurve.rotateY(0.5);
                            explicitCurve.rotateZ(0.5);

                            redrawCurveScene();
                        });

        curveTimer.start();
    }

    private void setupParametricCurve() {

        PerspectiveProjection projection = new PerspectiveProjection(400, 300, -500);

        parametricCurve =
                new ParametricCurve3D(
                        t -> 100 * Math.sin(3 * t),
                        t -> 100 * Math.sin(4 * t),
                        t -> 100 * Math.sin(5 * t),
                        0,
                        2 * Math.PI,
                        1000,
                        projection);

        parametricCurve.translate(400, 300, 200);
    }

    private void redrawParametricScene() {

        Graphics2D g = buffer.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());

        g.dispose();

        parametricCurve.draw(buffer, Color.BLUE);

        panel.repaint();
    }

    private void testParametricCurveKeyboard() {

        setupParametricCurve();

        bindParametricRotationKey("W", 2, 0, 0);
        bindParametricRotationKey("S", -2, 0, 0);

        bindParametricRotationKey("A", 0, 2, 0);
        bindParametricRotationKey("D", 0, -2, 0);

        bindParametricRotationKey("Q", 0, 0, 2);
        bindParametricRotationKey("E", 0, 0, -2);

        redrawParametricScene();
    }

    private void bindParametricRotationKey(
            String key, double angleX, double angleY, double angleZ) {

        String actionName = "parametricRotation" + key;

        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("pressed " + key), actionName);

        panel.getActionMap()
                .put(
                        actionName,
                        new AbstractAction() {

                            @Override
                            public void actionPerformed(ActionEvent e) {

                                parametricCurve.rotateX(angleX);
                                parametricCurve.rotateY(angleY);
                                parametricCurve.rotateZ(angleZ);

                                redrawParametricScene();
                            }
                        });
    }

    private void testParametricCurveAutomatic() {

        setupParametricCurve();

        parametricTimer =
                new Timer(
                        16,
                        e -> {
                            parametricCurve.rotateX(0.5);
                            parametricCurve.rotateY(0.5);
                            parametricCurve.rotateZ(0.5);

                            redrawParametricScene();
                        });

        parametricTimer.start();
    }

    private void setupExplicitSurface() {

        PerspectiveProjection projection = new PerspectiveProjection(400, 300, -500);

        explicitSurface =
                new ExplicitSurface3D(
                        (x, y) -> 80 * Math.sin(x / 50.0) * Math.cos(y / 50.0),
                        -150,
                        150,
                        -150,
                        150,
                        40,
                        40,
                        projection,
                        MeshType.TRIANGULAR);

        explicitSurface.translate(400, 300, 200);
    }

    private void redrawExplicitSurfaceScene() {

        Graphics2D g = buffer.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
        g.dispose();

        explicitSurface.draw(buffer, Color.BLUE);

        panel.repaint();
    }

    private void setupParametricSurface() {

        PerspectiveProjection projection = new PerspectiveProjection(400, 300, -500);

        double majorRadius = 100;
        double minorRadius = 40;

        parametricSurface =
                new ParametricSurface3D(
                        (s, t) -> (majorRadius + minorRadius * Math.cos(s)) * Math.cos(t),
                        (s, t) -> (majorRadius + minorRadius * Math.cos(s)) * Math.sin(t),
                        (s, t) -> minorRadius * Math.sin(s),
                        0,
                        2 * Math.PI,
                        0,
                        2 * Math.PI,
                        50,
                        50,
                        projection,
                        MeshType.RECTANGULAR);

        parametricSurface.translate(400, 300, 200);
    }

    private void redrawParametricSurfaceScene() {

        Graphics2D g = buffer.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
        g.dispose();

        parametricSurface.draw(buffer, Color.BLUE);

        panel.repaint();
    }

    private void bindSurfaceRotationKey(String key, String axis, double angle, boolean parametric) {
        String actionName = "surface_" + axis + "_" + key;

        panel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("pressed " + key), actionName);

        panel.getActionMap()
                .put(
                        actionName,
                        new AbstractAction() {

                            @Override
                            public void actionPerformed(ActionEvent e) {

                                if (parametric) {
                                    switch (axis) {
                                        case "X" -> parametricSurface.rotateX(angle);
                                        case "Y" -> parametricSurface.rotateY(angle);
                                        case "Z" -> parametricSurface.rotateZ(angle);
                                    }

                                    redrawParametricSurfaceScene();

                                } else {
                                    switch (axis) {
                                        case "X" -> explicitSurface.rotateX(angle);
                                        case "Y" -> explicitSurface.rotateY(angle);
                                        case "Z" -> explicitSurface.rotateZ(angle);
                                    }

                                    redrawExplicitSurfaceScene();
                                }
                            }
                        });
    }

    private void testExplicitSurface() {

        setupExplicitSurface();

        bindSurfaceRotationKey("W", "X", 5, false);
        bindSurfaceRotationKey("S", "X", -5, false);

        bindSurfaceRotationKey("A", "Y", 5, false);
        bindSurfaceRotationKey("D", "Y", -5, false);

        bindSurfaceRotationKey("Q", "Z", 5, false);
        bindSurfaceRotationKey("E", "Z", -5, false);

        redrawExplicitSurfaceScene();
    }

    private void testParametricSurface() {

        setupParametricSurface();

        bindSurfaceRotationKey("W", "X", 5, true);
        bindSurfaceRotationKey("S", "X", -5, true);

        bindSurfaceRotationKey("A", "Y", 5, true);
        bindSurfaceRotationKey("D", "Y", -5, true);

        bindSurfaceRotationKey("Q", "Z", 5, true);
        bindSurfaceRotationKey("E", "Z", -5, true);

        redrawParametricSurfaceScene();
    }

    private ParametricSurface3D cylinder;

    private void setupCylinder() {
        PerspectiveProjection projection = new PerspectiveProjection(400, 300, -500);

        cylinder =
                new ParametricSurface3D(
                        (s, t) -> 50 * (2 + Math.cos(s)) * Math.cos(t),
                        (s, t) -> 50 * (2 + Math.cos(s)) * Math.sin(t),
                        (s, t) -> 50 * s,
                        0,
                        2 * Math.PI,
                        0,
                        2 * Math.PI,
                        60,
                        60,
                        projection,
                        MeshType.TRIANGULAR);

        // Colocamos la figura en el centro del Canvas.
        cylinder.translate(400, 300, 40);

        // Orientamos su eje longitudinal verticalmente.
        cylinder.rotateX(90);
    }

    private void redrawCylinderScene() {
        Graphics2D g = buffer.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
        g.dispose();

        cylinder.drawRainbow(buffer);

        panel.repaint();
    }

    private void animateCylinder() {
        Timer timer =
                new Timer(
                        16,
                        e -> {
                            cylinder.rotateY(1);
                            redrawCylinderScene();
                        });

        timer.start();
    }
}
