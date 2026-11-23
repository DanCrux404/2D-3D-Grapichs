# Architecture and Design Decisions

Record decisions made by the human leads. Do not silently reverse a decision; propose a change with rationale and wait for approval.

## D-001 — Existing engine is protected
**Decision:** Reuse the completed engine as-is. Do not modify `engine/` or `TestFigures/`. A suspected bug must be reported with a reproducible case and proposed minimal fix; approval is required before editing.
**Reason:** Preserve completed graphics work and avoid regressions.

## D-002 — Engine is the only graphics renderer
**Decision:** Do not use Java graphics drawing APIs or introduce another renderer. Use existing engine APIs and pixel-writing mechanism.
**Reason:** Coursework restriction and consistency.

## D-004 — Plan before implementation
**Decision:** Begin with reconnaissance and approved architecture. Implement in small phases and update `tasks.md`.
**Reason:** Keep the agent grounded and prevent speculative changes.

## D-005 — Game source layout
**Decision:** New city code lives in `project/src` (default-package `Main.java` + `city.*` packages), launched with `./run.sh project`. Approved by human leads after Phase 0.
**Reason:** Matches `run.sh` compilation conventions and keeps game code outside `engine/` and `TestFigures/`.

## D-006 — DOOM-era requirements dropped
**Decision:** Remove bitmap-font menus, title/difficulty screens, hitscan combat, and DOOM audio requirements. The app launches straight into the 3D cozy city. Obsolete sections removed from `agent-instructions.md`.
**Reason:** Project scope changed from the DOOM-style practice to the cozy city (see `project-overview.md`).

## D-007 — App-side camera pipeline over engine primitives
**Decision:** The engine has no view transform or near-plane clipping, and shapes bake in their projection. The app transforms world vertices with `Transform3D` into the fixed engine camera frame, clips triangles against the near plane (`city.camera.NearPlaneClipper`), then projects with `Projection3D.projectWithDepth` and rasterizes with `Rasterizer`. Projection toggle switches the engine projection instance used by this pipeline.
**Reason:** Required for first-person controls and runtime projection toggle without modifying the protected engine; still uses only engine projection/rasterization/depth primitives.

## D-008 — Near plane is relative to the engine center of projection
**Decision:** Perspective near-plane clipping happens just in front of the engine COP (`z = -focal + 1`, i.e. −499 for focal 500), not at the view origin. Ground uses three LOD tiers out to ±60000 while the camera is bounded to the detailed ±6000 area.
**Reason:** The engine COP is 500 units behind the screen plane; clipping at z ≥ 2 discarded the ground under the camera and left sky at the bottom of the frame. A coarse far skirt keeps the horizon filled at low triangle cost.

## D-009 — Arrow-key input uses component focus
**Decision:** The render panel is focusable, requests focus on show and click, and movement/look bindings live in `JComponent.WHEN_FOCUSED` rather than `WHEN_IN_FOCUSED_WINDOW`.
**Reason:** Arrow keys were consumed by ancestor Swing input maps before the window-level binding; a focused component's binding has priority and now receives all four arrows.

## D-010 — Building scale and mutable window colors
**Decision:** The first building is scaled ~1.9× (half-size 210, height 610) with 3×3 window grids offset 1 unit outside each wall. Windows use mutable `WorldTriangle` colors so `setNightIntensity()` can transition them from blue-gray to warm yellow.
**Reason:** The Phase 1 prism looked toy-sized at 165-unit eye height, and Phase 3 needs window lighting without rebuilding geometry.

## D-011 — Hollow buildings use an app-side prism, not engine `Cube`
**Decision:** `engine.shapes.Cube` is not used for city buildings. `CityScene` keeps a canonical hollow unit prism with outward-facing wall winding and transforms it with `Transform3D`; windows are separate quads.
**Reason:** Source inspection showed `Cube` is a closed solid box with six faces, private vertices computed at draw time, and projection baked in. It has no hollow/top-bottom filtering and cannot provide clean world-space vertices for windows or Phase 3 lighting.

## D-012 — Phase 2 pitch range is `[-25°, 0°]`
**Decision:** `Camera.MAX_PITCH = 25.0` remains the downward limit, while upward pitch is clamped to `MAX_UP_PITCH = 0.0`. This corrects the earlier symmetric ±25° expectation after implementation testing.
**Reason:** With the engine COP 500 units behind the screen plane, positive pitch makes the ground horizon rise above the frame center and leaves a sky band along the bottom, the exact defect Phase 1.5 fixed. Downward pitch gives the intended bird's-eye view without exposing the gap.

## D-013 — World-space vertices and outward winding are Phase 3 inputs
**Decision:** All Phase 2 geometry keeps clean world-space `Point3D` vertices in `WorldTriangle`. `getNormal()` returns the normalized `(b−a) × (c−a)` cross product; ground/streets use +Y winding, walls use horizontal outward normals, arch ribbons and the hill face upward/outward. `EngineShapeSampler` records world points emitted by `ParametricCurve3D`/`ParametricSurface3D` through a throwaway 1×1 `Projection3D` target without using their baked projection.
**Reason:** `PhongLighting`, `DepthBuffer`, `ShadowMap`, and `ShadowRenderer` in Phase 3 need unprojected vertices and consistent normals; engine shapes cannot supply them directly.

## D-014 — CPU budget optimizations for the Phase 2 city
**Decision:** The Phase 2 visible ground is a horizon-color pixel backdrop filled by `SceneRenderer`; a separate clean ±6000 world-space quad stays available via `CityScene.shadowGroundTriangles()` for Phase 3. Streets are split into short segments between `{-1500,-1000,-500,0,500,1000,1500}` and sidewalks are kept only on the central avenue. Window grids are 3×3 on two hero buildings and 2×2 on the other eight. Projected triangles outside the image are culled before rasterization.
**Reason:** A few enormous ground/street triangles dominated CPU time despite low triangle counts (e.g. 60 street triangles cost ~18 ms/frame before segmentation). The final scene renders in 15.21 ms/frame and sustains 104 frames in the 5 s concurrency stress probe while preserving phase-3-ready geometry.

## D-014 — Superseded by D-015 (Phase 3 real ground and lighting)

**Status:** Superseded. Kept for history only; do not reintroduce the 2D ground backdrop or `CityScene.shadowGroundTriangles()`.

## D-015 — Phase 3 ground, lighting, and shadow strategy

**Decision:** The visible ground is a 64-triangle world-space `GroundMesh`; `SceneRenderer` no longer fills a ground backdrop. Arches and the hill were removed from the city scene. `SunMoonOrbit` uses `ParametricCurve3D` + `EngineShapeSampler` to drive engine `Light` state; no sun/moon geometry is rendered in Phase 3. `fillTrianglePhong` is used on streets, while ground and building walls use centroid `PhongLighting` with `Material`. A 512×512 `ShadowPass` renders only `SurfaceKind.BUILDING` wall triangles into `ShadowMap` and shades receivers with a fixed bias. Near-plane and screen-rectangle clipping carry world position and normal attributes into shading. Window grids remain 3×3/2×2 and emissive; final geometry is 688 triangles.

**Reason:** The old backdrop disappeared under some downward views and was not real 3D geometry. Profiling showed per-pixel Phong on the large near-ground fragments cost ~43 ms/frame alone, so ground uses cheaper centroid shading while retaining `PhongLighting`, `Light`, and `Material`. Ceiling is 700 triangles, so roofs and celestial quads are deferred to Phase 4.

## D-016 — Concurrency probe threshold for the lit path

**Decision:** The concurrency stress probe requires ≥60 frames and ≥100 paints in 5 seconds while rendering the full lit/shadow path; the Phase 2 threshold was 100 frames for the flat path.

**Reason:** The lit path is intentionally heavier (33.39 ms/frame benchmark) and the hostile tight painter thread competes for CPU. All failures remain fatal; only the throughput threshold changed.

## D-017 — Phase 4 camera and city refinements

**Decision:** Spawn at `(0,95,-250)` with pitch range `[-25°, +18°]`. The parallel overview is `(0,1150,-1150,0,-28,0.20)` and recenters on `scene.center()` at `(0,0,0)` via `offset = projectionDirection * focus.cameraZ / projectionZ - focus.cameraXY`. The city has 15 buildings whose centers are street-block centers and whose rotated footprints are validated with `StreetGrid.isInsideBlock`; two hero buildings keep 3×3 windows and thirteen fillers use 1×2. This supersedes D-012 and the building-count/window-grid portions of D-014/D-015.

**Reason:** The lower eye height and upward look make the taller skyline readable in first person, while the centered overview keeps the city framed after the building changes.

## D-018 — Bounded concurrent animation pipeline

**Decision:** `AnimationController` uses a prestarted `ThreadPoolExecutor(3,3,0,MILLISECONDS,ArrayBlockingQueue(3),daemonFactory,AbortPolicy)`. `OrbitTask`, `CloudTask`, and `StarTask` are independent `Supplier`s launched with `CompletableFuture.supplyAsync`; `allOf` publishes an immutable `AnimationSnapshot` on the completing worker. Rejected ticks are skipped silently; only genuine worker failures set `lastError()`. `stop()` shuts down, waits up to 2 seconds, then forcibly terminates if needed.

**Reason:** Keeps animation off the render thread without unbounded queues, avoids a fourth queued aggregator task, and preserves clean window-close behavior.

## D-019 — Phase 4 geometry budget and stress threshold

**Decision:** The approved ceiling is 850 triangles. The scene renders 756 static triangles (ground 64, streets 144, mountains 76, walls 120, windows 352) and up to 58 night-dynamic triangles (clouds 36, one celestial body 2, stars 20), totaling 814. The lit-path concurrency probe threshold is ≥30 frames and ≥100 paints in 5 seconds, replacing D-016's ≥60-frame target.

**Reason:** The taller 15-building city and sky geometry nearly double rasterized area versus Phase 3; all failures remain fatal, while throughput alone is adjusted to the measured heavier path.

## D-020 — Phase 5 visual polish

**Decision:** Downward pitch is `−15°` (`MAX_PITCH = 15.0`) and upward pitch remains `+18°`; the walkable grid, all yaw directions, and bottom image rows are verified by `PolishProbe`. Sun and moon are 12-segment `ParametricCurve3D` discs with `BASE_HEIGHT = −150`, `HORIZON_Y = 450`, and visibility disabled at `y <= 450`. The sky uses 20 brighter dome stars and five 3×3 `ParametricSurface3D` clouds, two of them above the city blocks. Light comes from an effective pose toward `scene.center()` with radius `900`, minimum height `650`, peak day intensity `2.0`, sunset `1.2`, and moon `0.55`; material ambient/diffuse values are ground `0.45/0.90`, roads `0.44/0.86`, sidewalks `0.48/0.82`, and buildings `0.46/0.86`. This supersedes the pitch clause of D-017, the dynamic-count portion of D-019, and the Phase 3 sky-material values in D-015.

**Reason:** The final visual inspection exposed square celestial bodies, an early horizon sink, dim materials/lighting, too few distributed sky elements, and a bottom-row sky risk at steep downward pitch.

## D-021 — Phase 5 cycle and final verification

**Decision:** The day cycle lasts 360 seconds with phases morning `0–90 s`, afternoon `90–165 s`, sunset `165–240 s`, and night `240–360 s`; sky color, windows, and stars use smoothstep keyframes on that same timeline. Maximum geometry is 848 triangles (756 static, 40 cloud, 12 one-body celestial, 40 star) under the unchanged 850 ceiling. `T-050` passes only with `./run.sh project`, `Phase2Probe`, `Phase3Probe`, `Phase4Probe`, `PolishProbe`, `InputProbe`, Bench, and `ConcurrencyProbe` green.

**Reason:** A longer, coherent cycle removes visible time discontinuities, and the final audit needs repeatable automated evidence before sign-off.

## D-022 — Focused sky and shadow polish

**Decision:** Upward pitch is reduced to `+10°` while downward pitch stays `−15°`. Visible sun/moon discs are translated by the player camera's `x/z` (skybox-style) while lighting remains world-space, and `HORIZON_Y` rises to `900` with `ENTRY_ANGLE = 0.46` so bodies stay above ground in perspective and parallel views. The star field grows to 45 dome quads sized `38–52`. Peak daylight rises to `2.5`; material ambient/diffuse move to approximately `0.35/0.95`; and `ShadowContrastPhongLighting` strengthens practical shadow attenuation by lowering ambient to 20% and scaling diffuse/specular by `visibility²` inside shadows. Maximum geometry becomes 898 triangles (756 static, 40 cloud, 12 celestial, 90 star) under a revised 900-triangle ceiling. This supersedes the pitch, horizon, star-count, lighting, material, and ceiling clauses of D-020/D-021.

**Reason:** Final visual inspection still showed a bottom sky band at `+18°`, reachable-looking celestial discs, a sparse night sky, and low-contrast building shadows. The engine stays untouched; the stronger shadow falloff is implemented through an app-side `PhongLighting` subclass.

## D-023 — Natural soft shadows and final sky budget

**Decision:** Use the engine's standard `PhongLighting` only; remove the `ShadowContrastPhongLighting` subclass. Day/night ambient is scaled from `1.0` to `0.35` by `DayNightCycle.getAmbientFactor()` through app-side `DynamicMaterial` instances, while warm daytime ambient/diffuse values are ground `0.60/0.95`, roads `0.56/0.95`, sidewalks `0.62/0.94`, and buildings `0.58/0.95`. Shadow-map lookups apply only to `GROUND` and `STREET` receivers; `BUILDING` triangles cast shadows but do not sample them. Downward pitch remains `−15°` and upward pitch is tightened to `+5°`. The star field contains 81 dome quads, giving 970 maximum triangles (756 static, 40 cloud, 12 celestial, 162 star) under a revised 1000-triangle ceiling. This supersedes the pitch, shadow-attenuation, star-count, and ceiling clauses of D-022.

**Reason:** The contrast subclass made shadowed walls unnaturally dark, `+10°` still exposed bottom-row sky risk, and 45 stars remained sparse. Standard Phong with dynamic ambient preserves soft warm shadow detail without touching the protected engine.

## D-024 — Final star scatter, window variety, and parallel sky filtering

**Decision:** `StarField` places 81 stars with golden-angle azimuths and a deterministic irrational elevation scramble over the upper hemisphere; stars span 60 distinct integer-elevation bands and all eight azimuth sectors. Window grids are derived from building footprint width and height, yielding 2×2, 2×3, 3×3, and 3×4 patterns instead of mostly 1×2; the 15 buildings contain 928 window triangles. In `ProjectionMode.PARALLEL`, `SkyGeometry` emits clouds but omits sun, moon, and stars so the architectural overview shows only city, clouds, and shadows. Maximum geometry becomes 1546 triangles (1332 static, 40 cloud, 12 celestial, 162 star) under a revised 1600-triangle ceiling. This supersedes the star-placement, window-grid, parallel-visibility, and ceiling clauses of D-023.

**Reason:** Fixed elevation rings made the stars look like necklaces, sparse 1×2 grids underfilled wider buildings, and sky-dome discs flattened onto the grass in the top-down overview. The changes stay entirely in app-side code and keep the protected engine untouched.

## Pending decisions
