# Tasks

Status legend: `[ ]` Pending | `[~]` In Progress | `[x]` Completed & Verified

## Phase 0 — Reconnaissance
- [x] **T-001:** Inspect `/home/dante/Documents_Storage/GitHub/2D, 3D Grapichs/2ndPartial/project` (structure, `CityScene`, `DayNightCycle`, `AnimationController`, `elements/`).
- [x] **T-002:** Scan `engine/` public classes, constructors, and methods; update `engine-inventory.md`.
- [x] **T-003:** Inspect `TestFigures/` to document exact usage of projections, `Rasterizer`, `DepthBuffer`, `PhongLighting`, `ShadowRenderer`, and canvas presentation.
- [x] **T-004:** Run existing `TestFigures` via `run.sh` to confirm build/run environment works.
- [x] **T-005:** Propose new 3D city package layout and wait for human lead approval.

## Phase 1 — Core Loop & Camera
- [x] **T-010:** Implement double-buffered canvas and `ExecutorService`-managed game loop.
- [x] **T-011:** Implement first-person camera (WASD + yaw/pitch) and near-plane clipping.
- [x] **T-012:** Implement runtime toggle between Perspective and Parallel projection.

## Phase 1.5 — Refinements (approved)
- [x] **T-015:** Move the near plane to the engine COP (`z = -focal + 1`) so ground under the camera fills the bottom of the frame; extend ground with a coarse far skirt and clamp movement to the detailed area.
- [x] **T-016:** Make the Swing panel focusable and request focus on show/click; bind movement keys in `WHEN_FOCUSED` so arrow keys work without ancestor-map interception.
- [x] **T-017:** Scale the prism to a skyline-scale building and add reusable window quads with mutable day/night color.
- [x] **T-018:** Verify via headless pixel probes, synthetic Swing key events, and concurrency stress; update docs.

## Phase 2 — City Geometry
- [x] **T-020:** Create flat ground plane and street layout.
- [x] **T-021:** Create `Building` geometry (hollow prisms) with `Transform3D` placement.
- [x] **T-022:** Add 3D curve and surface elements to the scene.
- [x] **T-023:** Verify Phase 2 via pixel/geometry probes, input probe, concurrency stress, benchmark, and docs.

## Phase 3 — Lighting, Day/Night & Shadows
- [x] **T-030:** Port `DayNightCycle` color/time progression to 3D.
- [x] **T-031:** Integrate `Light`, `Material`, and `PhongLighting`.
- [x] **T-032:** Integrate `ShadowMap` and `ShadowRenderer`.

## Phase 4 — Multithreaded Animations
- [x] **T-040:** Add concurrent animated `Cloud`, `Sun`, `Moon`, and `Star` elements using a bounded `ThreadPoolExecutor` with immutable `AnimationSnapshot` records; recenter the parallel overview on `scene.center()`, raise spawn eye height to 95, allow upward pitch to +18°, add 15 block-aligned buildings, parametric mountains/clouds, visible sun/moon, twinkling stars, and a scene-centered 512×512 shadow pass.

## Phase 5 — Final Verification & Polish
- [x] **T-050:** Full rubric audit (`rubric-checklist.md`), visual polish pass, stability test, and clean shutdown check — verified by `Phase2Probe`, `Phase3Probe`, `Phase4Probe`, `PolishProbe`, `InputProbe`, `Bench`, `ConcurrencyProbe`, and `./run.sh project`.
