import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;

public class KeyEventsConsole extends JFrame implements KeyListener {

    public KeyEventsConsole() {
        setTitle("Keyword Monitor");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        addKeyListener(this);

        setVisible(true);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        System.out.println("KeyPressed: " + KeyEvent.getKeyText(e.getKeyCode()) 
                + " (Code: " + e.getKeyCode() + ")");
    }

    @Override
    public void keyReleased(KeyEvent e) {
        System.out.println("KeyReleased: " + KeyEvent.getKeyText(e.getKeyCode()));
    }

    @Override
    public void keyTyped(KeyEvent e) {
        System.out.println("KeyTyped: " + e.getKeyChar());
    }

    public static void main(String[] args) {
        new KeyEventsConsole();
    }
}
