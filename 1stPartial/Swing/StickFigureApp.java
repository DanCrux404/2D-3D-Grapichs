import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;

class StickFigurePanel extends JPanel {

    public StickFigurePanel() {
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.BLACK);
        g.drawString("Demo de gráficos", 20, 30);

        g.drawArc(50, 60, 50, 50, 0, 360);
        g.drawArc(60, 70, 30, 30, 180, 180);
        g.fillOval(65, 75, 5, 5);
        g.fillOval(80, 75, 5, 5);

        g.drawLine(75, 110, 75, 200);

        g.drawLine(75, 120, 45, 160);
        g.drawLine(75, 120, 105, 160);

        g.drawLine(75, 200, 45, 240);
        g.drawLine(75, 200, 105, 240);
    }
}

public class StickFigureApp extends JFrame {

    public StickFigureApp() {
        setTitle("Stick Figure Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 320);
        setLocationRelativeTo(null);
        add(new StickFigurePanel());
        setVisible(true);
    }

    public static void main(String[] args) {
        new StickFigureApp();
    }
}
