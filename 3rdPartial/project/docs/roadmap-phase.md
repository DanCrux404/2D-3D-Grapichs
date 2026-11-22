# Roadmap and Phases

Phases are sequential. Do not start a later phase until exit criteria are met and the human leads approve.

## Phase 0 — Repository reconnaissance
- Inspect repository tree, launcher, README, source packages, and build conventions.
- Scan engine APIs and inspect TestFigures usage examples.
- Identify pixel-writing path, buffer lifecycle, projection/matrix/depth conventions, lighting/shadows, Java version, and run command.
- Report verified capabilities, unknowns, and proposed game source layout.
- Do not modify existing source files.
**Exit:** human leads approve repository map, engine inventory, source layout, and plan.

## Phase 1 — Application skeleton
- Create a separate application entry point.
- Reuse existing build/launcher conventions.
- Render a stable test scene through the existing engine.
- Verify image/depth clearing, presentation, and double buffering.
- Add a controlled update/render loop.
**Exit:** repeatable launch, stable presentation, no flicker or uncaught exceptions.

## Phase 2 — World and camera
- Define world coordinate conventions.
- Build a test room with floor, ceiling, walls, and elevation changes.
- Implement first-person movement, yaw, pitch, and collision.
- Demonstrate perspective projection; explicitly demonstrate parallel projection if not naturally visible in gameplay.
**Exit:** player navigates, looks up/down, collides, and traverses elevation changes.

## Phase 3 — Doors and level structure
- Implement interactive animated doors.
- Create connected rooms and corridors.
- Implement interaction and progression rules.
**Exit:** doors and level navigation work reliably.

## Phase 4 — Combat and enemies
- Implement health, damage, weapons, firing, ammunition, enemy movement/attacks/death, pickups.
- Implement five data-driven difficulty configurations.
- Implement win, lose, pause, and restart.
**Exit:** complete combat loop works.

## Phase 5 — Lighting and shadows
- Integrate existing lighting and shadow capabilities.
- Add readable environmental lights.
- Verify shadows on floors/walls and moving entities where feasible.
- Add muzzle flashes, projectiles, and restrained effects.
**Exit:** stable visible lighting/shadows without unacceptable artifacts or delays.

## Phase 6 — Animation and coursework audit
- Animate doors, enemies, projectiles, pickups, and environment.
- Demonstrate translation, scaling, rotation, curves, and surfaces meaningfully.
- Audit every third-partial topic against the rubric.
- Consider threading only after correctness and approval.
**Exit:** every required topic has a demonstrable use.

## Phase 7 — Validation and delivery
- Clean build and repeatable launch.
- Test all difficulty levels and game-state transitions.
- Check exceptions, artifacts, flicker, and input.
- Document controls, architecture, engine reuse, and rubric coverage.
- Capture required evidence.
**Exit:** requirements verified and human leads approve delivery.
