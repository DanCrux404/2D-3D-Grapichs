# Architecture

## Expected repository layout
```text
project/
  build/
  engine/
  README.md
  run.sh
  TestFigures/
  docs/
```
Treat `build/` as generated output unless repository evidence says otherwise. `TestFigures/` is a reference/demo, not the home of game code.

## Boundaries
### `engine/` — protected engine
Read and understand it first. Reuse its public classes and methods. Do not copy engine classes into the game and do not modify files under `engine/` during normal development. If a real defect or missing capability blocks progress, stop, provide a reproducible case and minimal proposed fix, and ask the human leads before editing.

### `TestFigures/` — usage reference
Inspect it to learn real construction, rendering, projection, lighting, shadows, buffer, and input patterns. Do not modify it without explicit approval.

### Game/application code
New game code belongs outside `engine/` and `TestFigures/`. Inspect `run.sh`, source layout, and compilation conventions before proposing the smallest suitable layout. Do not create packages blindly.

## Rendering rules
- Use the existing engine for projection, transformations, geometry, rasterization, depth, lighting, and shadows.
- Do not introduce a second renderer or duplicate drawing primitives.
- Do not use Java2D drawing calls such as `Graphics.drawLine`, `fillRect`, `drawImage`, or `drawPolygon` for game geometry.
- Follow the instructor's exception: use the existing pixel-writing mechanism.
- Swing/AWT may be used for windowing, input/event handling, and presenting the completed image if consistent with the existing app. Inspect before deciding.
- Preserve engine coordinate, projection, depth, and matrix conventions.

## Frame lifecycle
Inspect `TestFigures/Canvas.java` and `run.sh` before implementing the game loop. Target lifecycle:
1. Capture input.
2. Update game state using elapsed time.
3. Clear/prepare back buffer and depth buffer.
4. Render the complete frame through the engine.
5. Present/swap buffers without exposing a partially rendered frame.

Do not claim double buffering is correct until verified. Avoid blocking the UI/event thread.



## Threads
First achieve a correct, stable single controlled update/render loop. Do not create one thread per pixel or tiny primitive. If threads per graphical element are pursued for extra credit, propose bounded ownership/synchronization of mutable state and buffers, then obtain approval. Never allow concurrent writes to the same image/depth buffer without an explicit design.

## Double Buffering

Double buffering is a mandatory rendering requirement.

- Use a back buffer to render the next complete frame.
- Present only completed frames to the screen.
- Never expose partially rendered frames.
- Coordinate buffer access and presentation to prevent flickering, tearing, and race conditions.
- Reuse the existing engine's pixel-writing and image-presentation mechanisms whenever possible.
- Inspect the existing TestFigures implementation before designing a new buffering strategy.
- Do not introduce unnecessary rendering delays.

The implementation must be tested under continuous animation to verify stable, flicker-free presentation.

## Multithreading and Animation

Multithreading is an important project requirement and should be used where it provides meaningful independent animation or update work.
- Avoid creating an uncontrolled number of threads.
- Use a controlled concurrency strategy and document its design.
- Synchronize access to shared game state and rendering resources.
- Worker threads must never write directly to the buffer currently being presented.
- Ensure that the rendering process receives a consistent snapshot of the game state.
- Handle thread interruption and application shutdown safely.
- Avoid race conditions, deadlocks, and unnecessary CPU usage.

The implementation must demonstrate concurrent animation without compromising rendering stability or game responsiveness.

## Thread Management and Concurrency

### ExecutorService Requirement

All application-managed background execution threads must be managed through Java's `ExecutorService` API.

Creating and managing application threads directly using `new Thread()` is not permitted.

### Swing Event Dispatch Thread

The Swing Event Dispatch Thread (EDT) remains responsible for:
- Window management.
- Processing keyboard and other UI events.
- Presenting completed frames.

The EDT must not perform game-state updates or rendering operations.

### Thread Safety

- Input data must be communicated safely between the EDT and the game loop.
- Shared rendering resources must be synchronized appropriately.
- The EDT must never read a framebuffer while the game loop is modifying it.
- Game-state mutation must remain exclusively owned by the game loop.

### Executor Lifecycle

The application must manage the executor's lifecycle explicitly.

- Start execution when the application starts.
- Prevent duplicate game-loop submissions.
- Shut down the executor when the application closes.
- Use `shutdown()` and an appropriate termination strategy.
- Avoid leaving background execution threads running after application shutdown.

### Concurrency Scope

Additional executors, thread pools, or concurrent workers must only be introduced when a concrete requirement justifies them.

Creating one thread per entity or animation is not permitted.

Concurrency must remain controlled, documented, and synchronized.
