# Roadmap Phases

## Phase 0 — Reconnaissance & Inventory
- Inspect `2ndPartial/project` to understand the original 2D city architecture and `DayNightCycle` logic.
- Inspect `3rdPartial/engine` and `TestFigures/` to verify exact APIs, projection conventions, depth buffer, rasterizer, lighting, and shadows.
- Update `engine-inventory.md` and propose the minimal package layout for the new 3D city.

## Phase 1 — Core Loop, Double Buffering & Camera
- Set up the application window, `ExecutorService` game loop, and verified flicker-free double buffering.
- Implement the first-person camera (WASD + yaw/pitch) with near-plane clipping.
- Implement projection toggle: Perspective (`PerspectiveProjection`) and Parallel (`ParallelProjection`).
- Render a minimal test scene (ground plane + 1 cube) to verify depth buffer and controls.

## Phase 2 — Static City Geometry & Transformations
- Build the flat city map (ground plane + street grid).
- Create reusable `Building` elements (hollow rectangular prisms) placed via `Transform3D` (translation, scaling, rotation).
- Add small city details using 3D curves and surfaces (e.g., curved street arcs/cables, curved roofs or park terrain).

## Phase 3 — Day/Night Cycle, Lighting & Shadows
- Port and adapt `DayNightCycle` from the 2nd-partial project to 3D.
- Connect the Sun/Moon orbital path (parametric curve) to `Light` position and color.
- Apply `Material` and `PhongLighting` (Flat/Gouraud/Phong shading) across buildings and ground.
- Integrate `ShadowMap` and `ShadowRenderer` for dynamic shadows as the light moves.

## Phase 4 — Concurrent Animated Elements
- Implement independent animated elements: drifting 3D `Cloud` surfaces, orbiting `Sun`/`Moon`, twinkling `Star` elements, and optional animated city props (e.g., rotating sign/windmill).
- Drive independent animation updates via a bounded `ExecutorService` with safe state snapshots for the render thread.

## Phase 5 — Polish & Rubric Verification
- Verify zero exceptions, clean shutdown of all executors, and stable FPS.
- Audit every item in `rubric-checklist.md`.
- Record final validation in `changes.md`.
