import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Path2D;

class PanelEspiral extends JPanel implements Runnable {

    private volatile double currentAngle = 0.0; 
    private final double maxAngle = 30 * Math.PI; //the rounds it makes to draw
    private final double angleStep = 0.05;
    private final int delayMs = 20;
    private final Thread animationThread = new Thread(this);

    public PanelEspiral() {
        setBackground(Color.WHITE);
        animationThread.setDaemon(true);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (!animationThread.isAlive()) {
            animationThread.start();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(1.2f));
        g2.setColor(Color.BLACK);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        double a = 3.0; // Separation between rounds

        //two arcs
        Path2D firstArm = new Path2D.Double();
        Path2D secondArm = new Path2D.Double();

        boolean startFirst = true;
        boolean startSecond = true;

        for (double theta = 0; theta <= currentAngle; theta += angleStep) {
            // First arc (starts on theta angle)
            //Achimides formula
            //The further the angle rotates, the farther the point 
            //moves from the center, forming the spiral.
            double radius1 = a * theta;
            //Polar to pixels
            double x1 = centerX + radius1 * Math.cos(theta);
            double y1 = centerY + radius1 * Math.sin(theta);

            if (startFirst) {
                firstArm.moveTo(x1, y1);
                startFirst = false;
            } else {
                firstArm.lineTo(x1, y1);
            }

            // Second arc (separated exactly 180 grades / Math.PI to create the double effect )
            double radius2 = a * theta;
            double x2 = centerX + radius2 * Math.cos(theta + Math.PI);
            double y2 = centerY + radius2 * Math.sin(theta + Math.PI);

            if (startSecond) {
                secondArm.moveTo(x2, y2);
                startSecond = false;
            } else {
                secondArm.lineTo(x2, y2);
            }
        }

        // Draw the two arcs
        g2.draw(firstArm);
        g2.draw(secondArm);
    }

    @Override
    public void run() {
        while (currentAngle < maxAngle) {
            currentAngle += angleStep;
            repaint();
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
