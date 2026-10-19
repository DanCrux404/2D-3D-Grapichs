import javax.swing.SwingUtilities;
import project.Canvas;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Canvas canvas = new Canvas();

            canvas.show();
        });
    }
}
