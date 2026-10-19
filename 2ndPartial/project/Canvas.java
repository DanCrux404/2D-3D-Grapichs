package project;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import project.animation.AnimationController;

public class Canvas {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private JFrame frame;
    private JPanel panel;

    private final BufferedImage buffer;
    private final CityScene scene;
    private final AnimationController animationController;

    public Canvas() {
        buffer = new BufferedImage(
            WIDTH,
            HEIGHT,
            BufferedImage.TYPE_INT_RGB
        );

        scene = new CityScene(WIDTH, HEIGHT);

        initComponents();
        setFrame();

        // Draw the initial scene.
        scene.draw(buffer);

        // Connect the controller to the frame update method.
        animationController = new AnimationController(
            this::updateFrame
        );

        // Stop the animation when the window is closing.
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                animationController.stop();
            }
        });
    }

    /**
     * Initializes the graphical components.
     */
    private void initComponents() {
        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(buffer, 0, 0, null);
            }
        };

        panel.setPreferredSize(
            new Dimension(WIDTH, HEIGHT)
        );

        panel.setDoubleBuffered(true);
    }

    /**
     * Configures the application window.
     */
    private void setFrame() {
        frame = new JFrame("City - 2D Graphics");

        frame.setDefaultCloseOperation(
            JFrame.EXIT_ON_CLOSE
        );

        frame.setResizable(false);
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    /**
     * Displays the application window and starts the animation.
     */
    public void show() {
        if (SwingUtilities.isEventDispatchThread()) {
            frame.setVisible(true);
            animationController.start();
        } else {
            SwingUtilities.invokeLater(() -> {
                frame.setVisible(true);
                animationController.start();
            });
        }
    }

    /**
     * Updates and redraws one animation frame.
     */
    private void updateFrame(long deltaMillis) {
        scene.update(deltaMillis);
        scene.draw(buffer);
        panel.repaint();
    }
}

