# Task Tracker

Legend: `[ ]` not started · `[~]` in progress · `[x]` completed and validated · `[!]` blocked / needs human decision.

## Phase 0 — Repository Reconnaissance and Planning

- [ ] Inspect and summarize repository tree.
- [ ] Inspect `run.sh`, `README.md`, and build conventions.
- [ ] Inventory engine packages, public classes, and relevant methods.
- [ ] Inspect `TestFigures/` usage examples.
- [ ] Verify build and launch command.
- [ ] Document buffer lifecycle, pixel path, projections, transforms, depth, lighting, and shadows.
- [ ] Inspect existing double-buffering and frame-presentation mechanisms.
- [ ] Inspect existing animation loops and threading mechanisms, if any.
- [ ] Identify unknowns, technical constraints, and potential risks.
- [ ] Propose game source layout for approval.
- [ ] Verify soundtrack source, licensing terms, and permitted uses.
- [ ] Document whether the desired soundtrack can legally be included and redistributed.
- [ ] Obtain human approval before implementation.

## Phase 1 — Application Skeleton and User Interface

- [x] Create a separate application entry point.
- [x] Establish repeatable build and launch commands.
- [ ] Establish the application and game-state structure.
- [ ] Create the retro title menu.
- [ ] Implement a reusable bitmap font.
- [ ] Render menu text using pixel operations.
- [ ] Implement PLAY and EXIT options.
- [ ] Implement difficulty selection.
- [ ] Implement title-to-difficulty-to-gameplay navigation.
- [ ] Implement a controlled update and render loop.
- [ ] Implement double buffering using the approved rendering strategy.
- [ ] Verify back-buffer lifecycle and frame presentation.
- [ ] Verify stable presentation without flicker or partially rendered frames.
- [ ] Design an optional audio service independent of the rendering loop.
- [ ] Implement optional audio playback if an approved audio track is available.

## Phase 2 — World and First-Person Camera

- [ ] Document world axes, units, and camera conventions.
- [ ] Create a test room with floor, ceiling, and walls.
- [ ] Implement first-person movement.
- [ ] Implement yaw and pitch (looking left, right, up, and down).
- [ ] Implement collision detection.
- [ ] Add elevation changes.
- [ ] Demonstrate perspective projection.
- [ ] Demonstrate parallel projection where appropriate.
- [ ] Verify camera behavior and projection near walls and corners.

## Phase 3 — Doors and Level Design

- [ ] Create connected rooms and corridors.
- [ ] Implement interactive doors.
- [ ] Animate door opening and closing.
- [ ] Implement interaction detection.
- [ ] Implement level progression.
- [ ] Create the initial playable level layout.

## Phase 4 — Combat, Enemies, and Game Flow

- [ ] Implement player health and damage.
- [ ] Implement weapons, firing, and ammunition.
- [ ] Implement hitscan weapon firing using raycasting or equivalent instantaneous hit detection.
- [ ] Verify that weapons do not render visible traveling bullets or projectile meshes.
- [ ] Implement enemy movement, attacks, damage, and death.
- [ ] Implement enemy-player hit detection.
- [ ] Implement pickups and resources.
- [ ] Implement five data-driven difficulty settings.
- [ ] Configure enemy behavior and resource availability for each difficulty.
- [ ] Implement player death detection.
- [ ] Implement death-to-title navigation.
- [ ] Implement win, pause, and restart flows.

## Phase 5 — Lighting and Shadows

- [ ] Integrate the engine lighting system.
- [ ] Add environmental light sources.
- [ ] Integrate shadow mapping.
- [ ] Verify shadows on floors and walls.
- [ ] Add weapon muzzle-flash effects.
- [ ] Verify lighting and shadows during gameplay.

## Phase 6 — Animation and Coursework Coverage

- [ ] Animate enemies and doors.
- [ ] Animate pickups and environmental elements.
- [ ] Demonstrate translation.
- [ ] Demonstrate scaling.
- [ ] Demonstrate rotation.
- [ ] Demonstrate curves.
- [ ] Demonstrate surfaces.
- [ ] Audit all third-partial topics against the coursework rubric.
- [ ] Identify independent animated elements suitable for concurrent updates.
- [ ] Implement controlled multithreading where appropriate.
- [ ] Synchronize access to shared game state and rendering resources.
- [ ] Verify that concurrent updates do not cause race conditions or rendering artifacts.
- [ ] Verify stable double-buffered presentation during concurrent animation.
- [ ] Measure performance and avoid unnecessary thread creation.

## Phase 7 — Validation and Delivery

- [ ] Clean build succeeds.
- [ ] Launcher works from the documented directory.
- [ ] No uncaught exceptions during normal gameplay.
- [ ] Double buffering verified without visible flicker.
- [ ] Continuous animation verified under normal gameplay conditions.
- [ ] All difficulty levels tested.
- [ ] Menu navigation and game-state transitions tested.
- [ ] Hitscan combat tested.
- [ ] Player death and return-to-title flow tested.
- [ ] Audio behavior tested when enabled and disabled.
- [ ] Thread synchronization and shutdown tested.
- [ ] Coursework coverage matrix completed.
- [ ] Controls and architecture documented.
- [ ] Final evidence captured.
- [ ] Human leads approve delivery.

## Current Status

**Current phase:** Phase 0 — Repository Reconnaissance and Planning.

**Game implementation:** Not started.

**Existing engine:** Completed baseline. Treat it as protected and reusable.

**Next action:** Run the reconnaissance-only workflow described in `agent-instructions.md`. Do not create or modify game implementation code until the human leads approve the reconnaissance report.
