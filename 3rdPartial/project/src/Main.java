import doom.app.GameApplication;

import javax.swing.SwingUtilities;

/**
 * Entry point of the game application.
 *
 * <p>The existing launcher expects a class named {@code Main} in the default package, so this class
 * contains no logic other than starting {@link GameApplication} on Swing's Event Dispatch Thread.
 */
public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(
                () -> {
                    GameApplication application = new GameApplication();
                    application.show();
                });
    }
}
