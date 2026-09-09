import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;

public class MiniPaint extends JFrame implements ActionListener, MouseListener, MouseMotionListener {

    private ButtonGroup modos;
    private JPanel area;
    private JLabel status;
    private Image buffer;
    private Image temporal;

    private final int PUNTOS = 1;
    private final int LINEAS = 2;
    private final int RECTANGULOS = 3;
    private final int CIRCULOS = 4;
    private int modo;
    private int x, y;

    public MiniPaint() {
        super("MiniPaint 1.0");

        JMenuBar menuBar = new JMenuBar();
        // Menu Archivo
        JMenu menuArchivo = new JMenu("Archivo");

        // Opcion nuevo
        JMenuItem opcionNuevo = new JMenuItem("Nuevo", 'N');
        opcionNuevo.addActionListener(this);
        opcionNuevo.setActionCommand("Nuevo");
        menuArchivo.add(opcionNuevo);

        menuArchivo.addSeparator();
        // Opcion Salir
        JMenuItem opcionSalir = new JMenuItem("Salir", 'S');
        opcionSalir.addActionListener(this);
        opcionSalir.setActionCommand("Salir");
        menuArchivo.add(opcionSalir);

        menuBar.add(menuArchivo);

        modos = new ButtonGroup();
        // Menu Modo
        JMenu menuModo = new JMenu("Modo");
        // Opcion Puntos
        JRadioButtonMenuItem opcionPuntos = new JRadioButtonMenuItem("Puntos", true);
        opcionPuntos.addActionListener(this);
        opcionPuntos.setActionCommand("Puntos");
        menuModo.add(opcionPuntos);
        modos.add(opcionPuntos);
        // Opcion Lineas
        JRadioButtonMenuItem opcionLineas = new JRadioButtonMenuItem("Líneas");
        opcionLineas.addActionListener(this);
        opcionLineas.setActionCommand("Lineas");
        menuModo.add(opcionLineas);
        modos.add(opcionLineas);

        // Opcion Rectangulos
        JRadioButtonMenuItem opcionRectangulos = new JRadioButtonMenuItem("Rectángulos");
        opcionRectangulos.addActionListener(this);
        opcionRectangulos.setActionCommand("Rectangulos");
        menuModo.add(opcionRectangulos);
        modos.add(opcionRectangulos);
        
        // Opcion Círculos
        JRadioButtonMenuItem opcionCirculos = new JRadioButtonMenuItem("Círculos");
        opcionCirculos.addActionListener(this);
        opcionCirculos.setActionCommand("Circulos");
        menuModo.add(opcionCirculos);
        modos.add(opcionCirculos);

        menuBar.add(menuModo);

        area = new JPanel();
        area.addMouseListener(this);
        area.addMouseMotionListener(this);
        
        // Listener para crear el buffer de forma segura cuando el panel ya tenga dimensiones reales
        area.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = area.getWidth();
                int h = area.getHeight();
                if (w > 0 && h > 0) {
                    Image nuevoBuffer = area.createImage(w, h);
                    Graphics g = nuevoBuffer.getGraphics();
                    g.setColor(Color.WHITE);
                    g.fillRect(0, 0, w, h);
                    if (buffer != null) {
                        g.drawImage(buffer, 0, 0, null);
                    }
                    buffer = nuevoBuffer;
                }
            }
        });

        status = new JLabel("Status", JLabel.LEFT);

        // Asignar barra menues
        setJMenuBar(menuBar);
        // Agregar zona grafica
        getContentPane().add(area, BorderLayout.CENTER);
        // Agregar barra de estado
        getContentPane().add(status, BorderLayout.SOUTH);

        modo = PUNTOS;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String comando = e.getActionCommand();
        if (comando.equals("Nuevo")) {
            if (buffer != null) {
                Graphics g = buffer.getGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, area.getWidth(), area.getHeight());
                g.setColor(Color.BLACK);
                area.getGraphics().drawImage(buffer, 0, 0, this);
            }
        } else if (comando.equals("Salir")) {
            if (JOptionPane.showConfirmDialog(this, "¿En verdad desea salir?",
                    "Confirmación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                dispose();
                System.exit(0);
            }
        } else if (comando.equals("Puntos")) {
            modo = PUNTOS;
        } else if (comando.equals("Lineas")) {
            modo = LINEAS;
        } else if (comando.equals("Rectangulos")) {
            modo = RECTANGULOS;
        } else if (comando.equals("Circulos")) {
            modo = CIRCULOS;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        x = e.getX();
        y = e.getY();
        if (buffer != null) {
            temporal = area.createImage(area.getWidth(), area.getHeight());
            temporal.getGraphics().drawImage(buffer, 0, 0, this);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (buffer != null && temporal != null) {
            buffer.getGraphics().drawImage(temporal, 0, 0, this);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        setCursor(Cursor.getDefaultCursor());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (temporal == null) return;
        Graphics g = temporal.getGraphics();
        switch (modo) {
            case PUNTOS:
                g.fillOval(e.getX(), e.getY(), 2, 2);
                area.getGraphics().drawImage(temporal, 0, 0, this);
                break;
            case LINEAS:
                g.drawImage(buffer, 0, 0, area);
                g.drawLine(x, y, e.getX(), e.getY());
                area.getGraphics().drawImage(temporal, 0, 0, this);
                break;
            case RECTANGULOS:
                g.drawImage(buffer, 0, 0, area);
                int xMin = Math.min(x, e.getX());
                int yMin = Math.min(y, e.getY());
                int width = Math.abs(e.getX() - x);
                int height = Math.abs(e.getY() - y);
                g.drawRect(xMin, yMin, width, height);
                area.getGraphics().drawImage(temporal, 0, 0, this);
                break;
            case CIRCULOS:
                g.drawImage(buffer, 0, 0, area);
                int cxMin = Math.min(x, e.getX());
                int cyMin = Math.min(y, e.getY());
                int cWidth = Math.abs(e.getX() - x);
                int cHeight = Math.abs(e.getY() - y);
                g.drawOval(cxMin, cyMin, cWidth, cHeight);
                area.getGraphics().drawImage(temporal, 0, 0, this);
                break;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        status.setText("x=" + e.getX() + ",y=" + e.getY());
    }

    public static void main(String[] args) {
        new MiniPaint();
    }
}
