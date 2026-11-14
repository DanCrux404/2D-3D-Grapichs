import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        // Start the application on Swing's Event Dispatch Thread.
        SwingUtilities.invokeLater(
                () -> {
                    Canvas canvas = new Canvas();
                    canvas.show();
                });
    }
}
