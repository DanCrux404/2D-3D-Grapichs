import javax.swing.JFrame;

public class PieChartApp extends JFrame {

    public PieChartApp(String[] args) {
        setTitle("Pie Chart Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        setLocationRelativeTo(null);
        add(new PieChartPanel(args));
        setVisible(true);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[]{"40", "25", "20", "15"};
        }
        new PieChartApp(args);
    }
}
