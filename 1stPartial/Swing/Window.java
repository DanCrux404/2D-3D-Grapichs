import javax.swing.JFrame;
import java.awt.Dimension;

public class Window extends JFrame {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Just a JFrame window");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(new Dimension(400, 300));

        frame.setLocationRelativeTo(null);


        frame.setVisible(true);
    }
}
