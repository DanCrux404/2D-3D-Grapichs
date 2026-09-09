import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

class MouseEventsPanel extends JPanel implements MouseListener, MouseMotionListener {

    public MouseEventsPanel() {
        setBackground(Color.WHITE);
        // Register the listeners on the panel itself
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    // --- MOUSE LISTENER (Clicks and States) ---

    @Override
    public void mouseClicked(MouseEvent e) {
        System.out.println("2. Mouse clicked (full click: press and release quickly) at X=" + e.getX() + ", Y=" + e.getY());
    }

    @Override
    public void mousePressed(MouseEvent e) {
        System.out.println("3. Mouse pressed (button held down) at X=" + e.getX() + ", Y=" + e.getY());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        System.out.println("5. Mouse released (button let go) at X=" + e.getX() + ", Y=" + e.getY());
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        System.out.println("1. Mouse entered (cursor entered the frame/panel area)");
    }

    @Override
    public void mouseExited(MouseEvent e) {
        System.out.println("4. Mouse exited (cursor left the frame/panel area)");
    }

    // --- MOUSE MOTION LISTENER (Movement and Dragging) ---

    @Override
    public void mouseDragged(MouseEvent e) {
        System.out.println("6. Mouse dragged (moving the mouse while holding a button down) at X=" + e.getX() + ", Y=" + e.getY());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        System.out.println("7. Mouse moved (moving the mouse freely without pressing buttons) at X=" + e.getX() + ", Y=" + e.getY());
    }
}

public class MouseEventsApp extends JFrame {

    public MouseEventsApp() {
        setTitle("Mouse Events Monitor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        add(new MouseEventsPanel());
        setVisible(true);
    }

    public static void main(String[] args) {
        new MouseEventsApp();
    }
}
