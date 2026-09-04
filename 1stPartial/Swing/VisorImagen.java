import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Dimension;
import java.awt.Toolkit;

class Pantalla extends JPanel {

    private Image imagen;

    public Pantalla(Image imagen) {
        this.imagen = imagen;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (imagen != null) {
            Dimension tam = new Dimension(imagen.getWidth(this), imagen.getHeight(this));
            setPreferredSize(tam);
            setMinimumSize(tam);
            setMaximumSize(tam);
            setSize(tam);
            update(g);
        }
    }

    public void update(Graphics g) {
        g.drawImage(imagen, 0, 0, this);
    }
}

public class VisorImagen extends JFrame {

    private JScrollPane panel;
    private Pantalla pantalla;

    public VisorImagen(String archivo) {
        super("Visor imagen");

        Image img = Toolkit.getDefaultToolkit().getImage(archivo);
        pantalla = new Pantalla(img);

        panel = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        getContentPane().add(panel);
        panel.setViewportView(pantalla);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        String rutaImagen = (args.length > 0) ? args[0] : "imagen.jpg";
        new VisorImagen(rutaImagen);
    }
}
