import city.app.CityApplication;

import javax.swing.SwingUtilities;

/** Entry point for the 3D cozy city. */
public class Main {

    /** Launches the application on Swing's Event Dispatch Thread. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CityApplication().show());
    }
}
