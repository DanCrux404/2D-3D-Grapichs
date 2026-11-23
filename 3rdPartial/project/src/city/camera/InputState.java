package city.camera;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread-safe input snapshot shared between the EDT (key events) and the game loop.
 *
 * <p>The EDT only records key presses/releases and edge-triggered requests; the game loop reads
 * them once per tick.
 */
public class InputState {

    private final Set<Integer> pressedKeys = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean projectionToggleRequested = new AtomicBoolean(false);

    /** Records a key press. Called on the EDT. */
    public void press(int keyCode) {
        pressedKeys.add(keyCode);
    }

    /** Records a key release. Called on the EDT. */
    public void release(int keyCode) {
        pressedKeys.remove(keyCode);
    }

    /** Returns whether the key is currently held. */
    public boolean isDown(int keyCode) {
        return pressedKeys.contains(keyCode);
    }

    /** Requests a projection toggle (edge-triggered). Called on the EDT. */
    public void requestProjectionToggle() {
        projectionToggleRequested.set(true);
    }

    /**
     * Consumes a pending projection toggle request. Called on the game loop thread.
     *
     * @return true if a toggle was requested since the last consumption
     */
    public boolean consumeProjectionToggle() {
        return projectionToggleRequested.getAndSet(false);
    }
}
