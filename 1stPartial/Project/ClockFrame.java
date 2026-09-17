import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Main window frame for the Analog Clock application.
 */
public class ClockFrame extends JFrame {

    public ClockFrame() {
        setTitle("Analog Clock - Chronograph");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        ClockPanel clockPanel = new ClockPanel(600, 600);
        add(clockPanel);
        pack();
        setLocationRelativeTo(null);

        // Start the execution thread
        clockPanel.startClock();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClockFrame frame = new ClockFrame();
            frame.setVisible(true);
        });
    }
}
