package doom.app;

import java.util.Arrays;

/**
 * Thread-safe keyboard state shared between the EDT and the game loop.
 *
 * <p>The EDT reports key presses and releases as they arrive; the game-loop thread reads them
 * through {@link #poll()}, which returns an immutable {@link Snapshot} covering exactly one
 * frame and consumes one-shot press events. All mutable state is guarded by this monitor, and
 * the lock is held only for the tiny snapshot copy, so input wiring never blocks rendering.
 */
public final class InputState {

    /** Logical keys understood by the temporary test animation. */
    public enum Key {
        UP, DOWN, LEFT, RIGHT, CONFIRM, BACK
    }

    private static final int KEY_COUNT = Key.values().length;

    /** True while the physical key is held down. */
    private final boolean[] down = new boolean[KEY_COUNT];

    /** One-shot press events waiting for the next {@link #poll()}. */
    private final boolean[] pressed = new boolean[KEY_COUNT];

    /** EDT: records a key-down; auto-repeat press events are ignored as edges. */
    public synchronized void onKeyDown(Key key) {
        int index = key.ordinal();
        if (!down[index]) {
            down[index] = true;
            pressed[index] = true;
        }
    }

    /** EDT: records a key-up. */
    public synchronized void onKeyUp(Key key) {
        down[key.ordinal()] = false;
    }

    /** EDT: drops all state when the window loses focus so keys cannot stick. */
    public synchronized void clearAll() {
        Arrays.fill(down, false);
        Arrays.fill(pressed, false);
    }

    /**
     * Game-loop thread: returns the keys held down plus every press since the previous poll,
     * then clears the one-shot press events for the next frame.
     */
    public synchronized Snapshot poll() {
        int held = 0;
        int edges = 0;
        for (int i = 0; i < KEY_COUNT; i++) {
            if (down[i]) {
                held |= (1 << i);
            }
            if (pressed[i]) {
                edges |= (1 << i);
                pressed[i] = false;
            }
        }
        return new Snapshot(held, edges);
    }

    /** Immutable per-frame input snapshot; safe to read from the loop without locking. */
    public static final class Snapshot {

        private final int heldMask;
        private final int pressedMask;

        private Snapshot(int heldMask, int pressedMask) {
            this.heldMask = heldMask;
            this.pressedMask = pressedMask;
        }

        /** True while the key is held down. */
        public boolean isDown(Key key) {
            return (heldMask & (1 << key.ordinal())) != 0;
        }

        /** True if the key was pressed since the previous snapshot (one-shot). */
        public boolean wasPressed(Key key) {
            return (pressedMask & (1 << key.ordinal())) != 0;
        }
    }
}
