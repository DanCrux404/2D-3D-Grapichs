
import java.awt.BorderLayout;
import javax.swing.JFrame;

public class Window {

    public Window() {
        JFrame frame = new JFrame("DDA Line");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        Canvas canvas = new Canvas(800, 600);
        frame.add(canvas, BorderLayout.CENTER);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

