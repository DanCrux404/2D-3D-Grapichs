# Change Log

Record completed, approved changes in reverse chronological order.

Use this format:

```text
## YYYY-MM-DD — Short change title

- **Phase:** Phase N
- **Files:** `path/to/file`, `path/to/other-file`
- **Summary:** What changed and why.
- **Validation:** Exact command(s) run and result.
- **Notes:** Known limitations or follow-up work.
```

Record only changes that actually happened. Distinguish implemented, tested, and planned work. Keep entries concise and link them to tasks where useful. This file does not replace `tasks.md`.

## 2026-11-21 — Phase 1 Step 1 correction: engine-drawn initial frame

- **Phase:** Phase 1
- **Files:** `project/src/doom/app/GameApplication.java`
- **Summary:** Architectural review correction. Removed the hand-rolled `fillArea()` / `drawBorder()` pixel loops and the background/border constants from `GameApplication`, which now only owns the window, buffer, lifecycle, and presentation. The initial frame is drawn with the existing engine abstraction `engine.shapes.Rectangle.draw(buffer, 8, 8, WIDTH - 9, HEIGHT - 9, Color.RED)` on the default (black) buffer, so no full-screen fill is performed. Replaces the ~496,000 `Pixel.put` startup calls with ~2,800.
- **Validation:** `./run.sh project` compiled and launched twice without errors; `xwininfo -root -tree` reported the 800x600 window; `xwd` capture showed the black frame with the red rectangle outline; `WM_DELETE_WINDOW` closed both instances with `LAUNCHER_EXIT=0` and `APP_EXIT=0` and no stack traces.
- **Notes:** Approved with a standing policy: application layer = window/lifecycle/presentation, game rendering/UI layer = frame composition, engines = reusable 2D/3D graphics operations; do not duplicate engine functionality. Per-frame clearing strategy for Step 2 must be measured with the existing `Pixel.put` path first; bulk `setRGB`, `DataBufferInt`, or any other optimization requires separate explicit approval. `Main.java`, both engines, `TestFigures/`, `README.md`, `run.sh`, and the second partial remain untouched.

## 2026-11-21 — Phase 1 Step 1: application entry point and window skeleton

- **Phase:** Phase 1
- **Files:** `project/src/Main.java`, `project/src/doom/app/GameApplication.java`, `project/docs/tasks.md`, `project/docs/changes.md`
- **Summary:** Added the game entry point (`Main`, default package, as required by `run.sh`) and `doom.app.GameApplication`, which owns the 800x600 `TYPE_INT_RGB` buffer, renders the initial frame exclusively through `engine.core.Pixel.put`, and presents it from `paintComponent` via `g.drawImage`. No game loop, double buffering, menu, font, audio, or gameplay code was added.
- **Validation:** `./run.sh project` compiled both engines plus `project/src` into `build/project` and launched without errors; `xwininfo -root -tree` reported the `Project DOOM - Phase 1` window at 800x600; `xwd` capture showed the expected dark background with a 6-pixel red border; a `WM_DELETE_WINDOW` message closed the window and the JVM exited with code 0 and no stack traces.
- **Notes:** `build/project/` only; `build/TestFigures/`, `engine/`, `TestFigures/`, `README.md`, and `run.sh` were not touched. Frame content uses `Pixel.put` per the approved Q3 decision; `g.drawImage` is used solely to present the completed image, as permitted by `architecture.md`. Next: Step 2 (game loop, ping-pong double buffering, input).

## 2026-11-24 — Title Menu and Difficulty Selection (Phase 1 Step 4)

- **Phase:** Phase 1
- **Files:** `project/src/doom/app/GameLoop.java`
- **Summary:** Implemented title menu with PLAY/EXIT options and difficulty selection screen with five exact difficulty names in order (I'M TOO YOUNG TO DIE, HEY, NOT TOO ROUGH, HURT ME PLENTY, ULTRA-VIOLENCE, NIGHTMARE). Title screen: UP/DOWN navigates selection, ENTER confirms (PLAY → DIFFICULTY, EXIT → shutdown via existing shutdown mechanism). Difficulty screen: circular UP/DOWN navigation, ENTER starts gameplay with selected difficulty, ESC returns to title. HURT ME PLENTY is default. All menu text rendered via BitmapFont.renderString() using Pixel.put(). Existing GameLoop state ownership, InputState key bindings, ExecutorService, double-buffering architecture preserved unchanged.
- **Validation:** `./run.sh project` compiles with zero errors; stable FPS ~55-57; BitmapFontTest reports PASSED: 21 FAILED: 0; state transitions verified via code review; menu navigation and state transitions verified through manual testing; no rectangle appears or moves; no old rectangle test messages ([GameLoop] ENTER test action, [GameLoop] ESC pressed — requesting clean shutdown) appear; application shuts down cleanly via existing shutdown lifecycle.
- **Notes:** Menu leverages existing GameLoop state architecture (currentGameState, selectedDifficulty), InputState actions (UP/DOWN/CONFIRM/BACK), and GameState enum (TITLE, DIFFICULTY, PLAYING, GAME_OVER). No new threads, executors, dependencies, or rendering APIs introduced. EXIT uses existing shutdown mechanism (AtomicBoolean guard → requestShutdown → dispatchWindowClosing → stop). No frozen files were modified (BitmapFont.java, BitmapFontTest.java, Difficulty.java, GameState.java, engine sources, run.sh, TestFigures/).
