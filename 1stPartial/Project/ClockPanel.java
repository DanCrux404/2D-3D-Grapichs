import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.time.LocalTime;
import javax.swing.JPanel;

/**
 * Custom JPanel handling double buffering, thread animation, and rendering logic.
 */
public class ClockPanel extends JPanel implements Runnable {

    private final int width;
    private final int height;

    private Thread animationThread;
    private boolean running;

    // Double Buffering Images
    private BufferedImage staticBackgroundBuffer;
    private BufferedImage offscreenBuffer;

    // Time Tracking & Audio
    private int lastSecond = -1;
    private final SoundManager soundManager;

    public ClockPanel(int width, int height) {
        this.width = width;
        this.height = height;
        this.setPreferredSize(new Dimension(width, height));
        this.soundManager = new SoundManager();

        initializeBuffers();
    }

    /**
     * Pre-renders the static background into an off-screen image buffer.
     * Rendered once to optimize drawing efficiency.
     */
    private void initializeBuffers() {
        staticBackgroundBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        offscreenBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = staticBackgroundBuffer.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(width, height) / 2 - 30;

        // Draw Outer Rim
        g2d.setColor(new Color(20, 24, 30));
        g2d.fillRect(0, 0, width, height);

        g2d.setColor(new Color(45, 52, 65));
        g2d.fillOval(centerX - radius - 15, centerY - radius - 15, (radius + 15) * 2, (radius + 15) * 2);

        // Draw Dial Face
        g2d.setColor(new Color(15, 18, 24));
        g2d.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        // Draw Brand / Logo Text
        g2d.setColor(new Color(180, 190, 205));
        g2d.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 16));
        FontMetrics fm = g2d.getFontMetrics();
        String brand = "CHRONOGRAPH";
        g2d.drawString(brand, centerX - (fm.stringWidth(brand) / 2), centerY - (radius / 3));

        // Draw Hour Ticks and Numbers
        g2d.setFont(new Font("SansSerif", Font.BOLD, 18));
        fm = g2d.getFontMetrics();

        for (int i = 0; i < 60; i++) {
            double angle = Math.toRadians(i * 6 - 90);
            int innerR;

            if (i % 5 == 0) {
                innerR = radius - 25;
                g2d.setStroke(new BasicStroke(3));
                g2d.setColor(new Color(212, 175, 55)); // Gold accent

                // Draw Hour Number
                int hourNum = (i == 0) ? 12 : i / 5;
                String numStr = String.valueOf(hourNum);
                int textR = radius - 45;
                int textX = (int) (centerX + textR * Math.cos(angle)) - (fm.stringWidth(numStr) / 2);
                int textY = (int) (centerY + textR * Math.sin(angle)) + (fm.getAscent() / 3);
                g2d.drawString(numStr, textX, textY);
            } else {
                innerR = radius - 12;
                g2d.setStroke(new BasicStroke(1));
                g2d.setColor(new Color(90, 100, 115));
            }

            int x1 = (int) (centerX + radius * Math.cos(angle));
            int y1 = (int) (centerY + radius * Math.sin(angle));
            int x2 = (int) (centerX + innerR * Math.cos(angle));
            int y2 = (int) (centerY + innerR * Math.sin(angle));

            g2d.drawLine(x1, y1, x2, y2);
        }

        g2d.dispose();
    }

    /**
     * Starts the clock execution thread safely.
     */
    public synchronized void startClock() {
        if (running) return;
        running = true;
        animationThread = new Thread(this, "Clock-Animation-Thread");
        animationThread.start();
    }

    /**
     * Main thread loop handling timing updates and frame rendering.
     */
    @Override
    public void run() {
        while (running) {
            LocalTime now = LocalTime.now();
            int currentSecond = now.getSecond();

            // Play tick sound when the second changes
            if (currentSecond != lastSecond) {
                lastSecond = currentSecond;
                soundManager.playTickSound();
            }

            // Render current frame offscreen
            renderOffscreenFrame(now);

            // Trigger Swing repaint cycle
            repaint();

            try {
                // Short sleep interval for smooth continuous rendering (~50 ms)
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }

    /**
     * Core Double Buffering rendering logic:
     * 1. Draw pre-computed static background.
     * 2. Calculate and draw dynamic clock hands on top.
     */
    private void renderOffscreenFrame(LocalTime time) {
        Graphics2D g2d = offscreenBuffer.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Step 1: Draw static background buffer
        g2d.drawImage(staticBackgroundBuffer, 0, 0, null);

        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(width, height) / 2 - 30;

        // Extract time values
        int hours = time.getHour() % 12;
        int minutes = time.getMinute();
        int seconds = time.getSecond();

        // Angles in radians
        double secondAngle = Math.toRadians((seconds * 6) - 90);
        double minuteAngle = Math.toRadians(((minutes + seconds / 60.0) * 6) - 90);
        double hourAngle = Math.toRadians(((hours + minutes / 60.0) * 30) - 90);

        // Step 2: Draw Hour Hand
        int hourLength = (int) (radius * 0.50);
        g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(new Color(220, 225, 235));
        g2d.drawLine(centerX, centerY,
                (int) (centerX + hourLength * Math.cos(hourAngle)),
                (int) (centerY + hourLength * Math.sin(hourAngle)));

        // Step 3: Draw Minute Hand
        int minuteLength = (int) (radius * 0.75);
        g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(new Color(180, 195, 215));
        g2d.drawLine(centerX, centerY,
                (int) (centerX + minuteLength * Math.cos(minuteAngle)),
                (int) (centerY + minuteLength * Math.sin(minuteAngle)));

        // Step 4: Draw Second Hand
        int secondLength = (int) (radius * 0.85);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(new Color(235, 75, 75));
        g2d.drawLine(centerX, centerY,
                (int) (centerX + secondLength * Math.cos(secondAngle)),
                (int) (centerY + secondLength * Math.sin(secondAngle)));

        // Center Gold Cap
        g2d.setColor(new Color(212, 175, 55));
        g2d.fillOval(centerX - 7, centerY - 7, 14, 14);

        g2d.dispose();
    }

    /**
     * Projects the pre-rendered offscreen buffer to the display component.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (offscreenBuffer != null) {
            g.drawImage(offscreenBuffer, 0, 0, null);
        }
    }
}
