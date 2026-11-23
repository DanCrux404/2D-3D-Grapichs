package city.app;

import city.animation.AnimationController;
import city.animation.AnimationSnapshot;
import city.animation.CycleSnapshot;
import city.camera.Camera;
import city.camera.InputState;
import city.render.LightingContext;
import city.render.ProjectionMode;
import city.render.SceneRenderer;
import city.render.ShadowPass;
import city.render.SkyGeometry;
import city.scene.CityScene;
import city.scene.SunMoonOrbit;

import engine.core.Point3D;
import engine.lighting.Light;
import engine.lighting.PhongLighting;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * Swing shell for the 3D cozy city.
 *
 * <p>The EDT owns the window, key events, and presentation of completed frames. The game loop
 * thread owns input consumption, camera updates, rendering, and buffer swaps.
 */
public class CityApplication {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final Color SKY_COLOR = new Color(145, 198, 225);

    private final JFrame frame;
    private final JPanel panel;
    private final FramePresenter presenter;
    private final GameLoop gameLoop;
    private final InputState input = new InputState();
    private final CityScene scene = new CityScene();
    private final SunMoonOrbit orbit = new SunMoonOrbit();
    private final AnimationController animation = new AnimationController();
    private final ShadowPass shadows = new ShadowPass(512, scene.center());
    private final PhongLighting phongLighting = new PhongLighting();
    private final SceneRenderer renderer = new SceneRenderer(WIDTH, HEIGHT);
    private final Camera playerCamera = new Camera(0, 95, -250, 0, 0, 1.0);
    private final Camera overviewCamera = new Camera(0, 1150, -1150, 0, -28, 0.20);

    /** Builds the window, presenter, and game loop without showing them. */
    public CityApplication() {
        panel =
                new JPanel() {
                    @Override
                    protected void paintComponent(Graphics graphics) {
                        super.paintComponent(graphics);
                        presenter.paint(graphics);
                    }
                };

        presenter = new FramePresenter(WIDTH, HEIGHT, panel::repaint);
        gameLoop = new GameLoop(this::updateAndRenderFrame);

        initComponents();
        configureKeyBindings();

        playerCamera.setWorldLimit(scene.groundHalfSize() - 200.0);

        frame = new JFrame("3D Cozy City");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent event) {
                        gameLoop.stop();
                        animation.stop();
                    }
                });
    }

    /** Shows the window and starts the game loop. Call on the EDT. */
    public void show() {
        frame.setVisible(true);
        panel.requestFocusInWindow();
        gameLoop.start();
    }

    private void initComponents() {
        panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        panel.setDoubleBuffered(true);
        panel.setBackground(SKY_COLOR);
        panel.setFocusable(true);

        panel.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent event) {
                        panel.requestFocusInWindow();
                    }
                });
    }

    private void updateAndRenderFrame(long deltaMillis) {
        if (input.consumeProjectionToggle()) {
            renderer.toggleMode();
        }

        playerCamera.update(input, deltaMillis);
        animation.update(deltaMillis);

        CycleSnapshot cycle = animation.cycleSnapshot();
        AnimationSnapshot animationSnapshot = animation.snapshot();
        scene.setNightIntensity((float) cycle.windowIntensity());
        scene.setDaylightFactor(cycle.ambientFactor());

        SunMoonOrbit.LightPose lightPose = orbit.lightAt(cycle.elapsedMillis());
        Light light =
                new Light(
                        lightPose.position(), lightPose.color(), lightPose.intensity());

        shadows.setLight(light, scene.center());
        shadows.render(scene.shadowCasters());

        Camera activeCamera =
                renderer.getMode() == ProjectionMode.PERSPECTIVE ? playerCamera : overviewCamera;

        presenter.clearBackBuffer(cycle.skyColor());
        renderer.render(
                scene.triangles(),
                SkyGeometry.triangles(
                        animationSnapshot,
                        cycle,
                        playerCamera.getX(),
                        playerCamera.getZ(),
                        renderer.getMode()),
                activeCamera,
                presenter.backBuffer(),
                scene.center(),
                new LightingContext(
                        light,
                        phongLighting,
                        shadows,
                        new Point3D(
                                activeCamera.getX(), activeCamera.getY(), activeCamera.getZ()),
                        2.0));
        presenter.present();
    }

    private void configureKeyBindings() {
        int[] heldKeys = {
            KeyEvent.VK_W, KeyEvent.VK_A, KeyEvent.VK_S, KeyEvent.VK_D,
            KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT
        };

        for (int keyCode : heldKeys) {
            bindHold(keyCode);
        }

        panel.getInputMap(JComponent.WHEN_FOCUSED)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "toggleProjection");
        panel.getActionMap()
                .put(
                        "toggleProjection",
                        new AbstractAction() {
                            @Override
                            public void actionPerformed(java.awt.event.ActionEvent event) {
                                input.requestProjectionToggle();
                            }
                        });
    }

    private void bindHold(int keyCode) {
        String name = "key_" + keyCode;

        panel.getInputMap(JComponent.WHEN_FOCUSED)
                .put(KeyStroke.getKeyStroke(keyCode, 0, false), name + "_pressed");
        panel.getActionMap()
                .put(
                        name + "_pressed",
                        new AbstractAction() {
                            @Override
                            public void actionPerformed(java.awt.event.ActionEvent event) {
                                input.press(keyCode);
                            }
                        });

        panel.getInputMap(JComponent.WHEN_FOCUSED)
                .put(KeyStroke.getKeyStroke(keyCode, 0, true), name + "_released");
        panel.getActionMap()
                .put(
                        name + "_released",
                        new AbstractAction() {
                            @Override
                            public void actionPerformed(java.awt.event.ActionEvent event) {
                                input.release(keyCode);
                            }
                        });
    }
}
