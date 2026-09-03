import javax.swing.JFrame;

public class Arquimides extends JFrame {

    public Arquimides() {

        JFrame frame = new JFrame("Espiral de Arquimides");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(1000, 800);

        frame.setLocationRelativeTo(null);

        frame.add(new PanelEspiral());

        frame.setVisible(true);
    }

    public static void main(String[] args) {

        new Arquimides();
    }
}
