import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.util.Random;

class PieChartPanel extends JPanel {

    private double[] values;
    private Color[] colors;

    public PieChartPanel(String[] args) {
        setBackground(Color.WHITE);
        parseValues(args);
        generateColors();
    }

    private void parseValues(String[] args) {
        values = new double[args.length];
        try {
            for (int i = 0; i < args.length; i++) {
                values[i] = Double.parseDouble(args[i]);
            }
        } catch (NumberFormatException e) {
            values = new double[]{30.0, 30.0, 40.0};
        }
    }

    private void generateColors() {
        colors = new Color[values.length];
        Random random = new Random(42);
        for (int i = 0; i < values.length; i++) {
            colors[i] = new Color(random.nextInt(180) + 40, random.nextInt(180) + 40, random.nextInt(180) + 40);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (values == null || values.length == 0) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double totalSum = 0;
        for (double val : values) {
            totalSum += val;
        }

        if (totalSum <= 0) return;

        int width = getWidth();
        int height = getHeight();
        int diameter = Math.min(width, height) - 100;
        int x = (width - diameter) / 2;
        int y = (height - diameter) / 2;

        int startAngle = 0;

        for (int i = 0; i < values.length; i++) {
            int arcAngle = (int) Math.round((values[i] / totalSum) * 360);

            if (i == values.length - 1) {
                arcAngle = 360 - startAngle;
            }

            g2.setColor(colors[i]);
            g2.fillArc(x, y, diameter, diameter, startAngle, arcAngle);

            g2.setColor(Color.WHITE);
            g2.drawArc(x, y, diameter, diameter, startAngle, arcAngle);

            startAngle += arcAngle;
        }
    }
}
