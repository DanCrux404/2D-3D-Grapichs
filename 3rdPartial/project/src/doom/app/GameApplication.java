package doom.app;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * Application window, executor lifecycle, and presentation wiring for Phase 1.
 *
 * <p>Owns the single-thread {@link ExecutorService} that runs the {@link GameLoop}, the
 * {@link FramePresenter} that performs application-level double buffering, and the shared
 * {@link InputState}. Swing UI operations and presentation stay on the Event Dispatch Thread;
 * all game-state updates and rendering happen on the game-loop executor thread.
 *
 * <p>No frame content is drawn from this class: {@code paintComponent} only blits the published
 * buffer, as permitted by the architecture document.
 */
public class GameApplication {

    /** Frame width in pixels. */
    public static final int WIDTH = 800;

    /** Frame height in pixels. */
    public static final int HEIGHT = 600;

    /**
     * Bounded EDT wait during shutdown. The loop exits within one frame (~17 ms typical), so
     * this only blocks briefly and guarantees the pool does not outlive application shutdown.
     */
    private static final long SHUTDOWN_WAIT_MILLIS = 500L;

    /** Extra bounded wait after the {@code shutdownNow()} fallback. */
    private static final long SHUTDOWN_NOW_WAIT_MILLIS = 200L;

    private final JFrame frame;
    private final JPanel panel;
    private final FramePresenter presenter;
    private final InputState inputState;
    private final GameLoop gameLoop;
    private final ExecutorService executor;

    private final AtomicBoolean loopSubmitted = new AtomicBoolean(false);
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private Future<?> loopFuture;

    /**
     * Creates the buffers, panel, input state, game loop, and single-thread executor, then
     * configures the window. Must be called on the Event Dispatch Thread.
     */
    public GameApplication() {

        panel =
                new JPanel() {

                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);

                        // Presentation only: blit the completed image published by the loop.
                        presenter.paintPublished(g, this);
                    }
                };

        panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        panel.setDoubleBuffered(true);

        presenter = new FramePresenter(WIDTH, HEIGHT, panel);
        inputState = new InputState();
        gameLoop = new GameLoop(presenter, inputState, this::requestShutdown, this::onFatalError);

        // Single-thread executor: owns the one and only game-loop task for the app lifetime.
        executor = Executors.newSingleThreadExecutor();

        frame = new JFrame("Project DOOM - Phase 1");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);

        frame.addWindowListener(
                new WindowAdapter() {

                    @Override
                    public void windowClosing(WindowEvent e) {
                        stop();
                    }
                });

        // Held keys must not stick when the window loses focus.
        frame.addWindowFocusListener(
                new WindowAdapter() {

                    @Override
                    public void windowLostFocus(WindowEvent e) {
                        inputState.clearAll();
                    }
                });

        configureKeyBindings();
    }

    /**
     * Wires press and release events of the arrow keys, WASD, ENTER, and ESC to the shared
     * {@link InputState}. Bindings use {@code WHEN_IN_FOCUSED_WINDOW} so they work regardless
     * of which child component has focus.
     */
    private void configureKeyBindings() {
        InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = panel.getActionMap();

        bindKey(inputMap, actionMap, KeyEvent.VK_UP, "up", InputState.Key.UP);
        bindKey(inputMap, actionMap, KeyEvent.VK_W, "up", InputState.Key.UP);
        bindKey(inputMap, actionMap, KeyEvent.VK_DOWN, "down", InputState.Key.DOWN);
        bindKey(inputMap, actionMap, KeyEvent.VK_S, "down", InputState.Key.DOWN);
        bindKey(inputMap, actionMap, KeyEvent.VK_LEFT, "left", InputState.Key.LEFT);
        bindKey(inputMap, actionMap, KeyEvent.VK_A, "left", InputState.Key.LEFT);
        bindKey(inputMap, actionMap, KeyEvent.VK_RIGHT, "right", InputState.Key.RIGHT);
        bindKey(inputMap, actionMap, KeyEvent.VK_D, "right", InputState.Key.RIGHT);
        bindKey(inputMap, actionMap, KeyEvent.VK_ENTER, "confirm", InputState.Key.CONFIRM);
        bindKey(inputMap, actionMap, KeyEvent.VK_ESCAPE, "back", InputState.Key.BACK);
    }

    /**
     * Binds the press and release events of one physical key to the same logical key, so both
     * continuous held-key state and one-shot presses are reported to the {@link InputState}.
     */
    private void bindKey(InputMap inputMap,
                         ActionMap actionMap,
                         int keyCode,
                         String name,
                         InputState.Key key) {

        inputMap.put(KeyStroke.getKeyStroke(keyCode, 0), name + ".press");
        actionMap.put(
                name + ".press",
                new AbstractAction() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        inputState.onKeyDown(key);
                    }
                });

        inputMap.put(KeyStroke.getKeyStroke(keyCode, 0, true), name + ".release");
        actionMap.put(
                name + ".release",
                new AbstractAction() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        inputState.onKeyUp(key);
                    }
                });
    }

    /** Displays the window and submits the game loop exactly once. EDT only. */
    public void show() {
        frame.setVisible(true);

        if (stopped.get()) {
            return;
        }
        if (loopSubmitted.compareAndSet(false, true)) {
            loopFuture = executor.submit(gameLoop);
            System.out.println("[App] game loop submitted to single-thread executor");
        }
    }

    /**
     * Requests a clean application shutdown. Safe to call from any thread; the actual shutdown
     * runs on the EDT through the normal window-closing path (no {@code System.exit} here).
     */
    public void requestShutdown() {
        if (SwingUtilities.isEventDispatchThread()) {
            dispatchWindowClosing();
        } else {
            SwingUtilities.invokeLater(this::dispatchWindowClosing);
        }
    }

    /**
     * Routes ESC and fatal errors through the same path as the window close button: a synthetic
     * {@code WINDOW_CLOSING} event triggers {@link #stop()} and the frame's default close
     * operation.
     */
    private void dispatchWindowClosing() {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    /** Called on the game-loop thread after an unexpected failure has been logged. */
    private void onFatalError(Throwable error) {
        System.err.println("[App] fatal game-loop error — initiating controlled shutdown: " + error);
        requestShutdown();
    }

    /**
     * Idempotent controlled shutdown; runs on the EDT via the window-closing listener.
     *
     * <p>Stops the loop first, then requests graceful executor termination, waits a bounded
     * time so no pool threads outlive the application, and falls back to {@code shutdownNow()}
     * if the loop does not stop in time. Interruption restores the interrupt flag.
     */
    private void stop() {
        if (!stopped.compareAndSet(false, true)) {
            return;
        }
        System.out.println("[App] shutdown requested — stopping game loop");

        gameLoop.requestStop();
        executor.shutdown();

        boolean terminated = false;
        try {
            terminated =
                    executor.awaitTermination(SHUTDOWN_WAIT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException interrupted) {
            // Restore the flag, then interrupt the pacing sleep as a fallback.
            Thread.currentThread().interrupt();
            executor.shutdownNow();
            System.out.println("[App] shutdown interrupted — interrupt flag restored, "
                    + "shutdownNow() issued");
            reportLoopFutureOutcome(false);
            return;
        }

        if (!terminated) {
            System.out.println("[App] graceful stop timed out — shutdownNow() fallback");
            executor.shutdownNow();
            try {
                terminated =
                        executor.awaitTermination(SHUTDOWN_NOW_WAIT_MILLIS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                System.out.println("[App] post-shutdownNow wait interrupted — "
                        + "interrupt flag restored");
            }
        }

        reportLoopFutureOutcome(terminated);
        System.out.println("[App] shutdown complete — executor "
                + (executor.isTerminated() ? "terminated" : "not terminated"));
    }

    /**
     * Surfaces failures captured in the submitted {@link Future} (for example an {@code Error}
     * that escaped the loop's own handlers) instead of silently swallowing them.
     */
    private void reportLoopFutureOutcome(boolean terminated) {
        Future<?> future = loopFuture;
        if (!terminated || future == null) {
            return;
        }
        try {
            future.get(); // Task already finished: returns immediately or throws.
        } catch (ExecutionException failure) {
            System.err.println("[App] game-loop task failed:");
            failure.getCause().printStackTrace(System.err);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        } catch (CancellationException cancelled) {
            // Task was cancelled before it ran; nothing further to report.
        }
    }
}
