# Rubric Checklist (3rd Partial)

Every item below must be visibly demonstrated in the running application using only `engine/` and the pixel-writing mechanism.

## Minimum Requirements (70 pts)
- [x] **Parallel Projection:** Toggleable orthographic/parallel view (`ParallelProjection`); `P` toggle, parallel tower framing, `scene.center()` recentering, and sky-body hiding in the top-down overview verified by probes.
- [x] **Perspective Projection:** First-person 3D view (`PerspectiveProjection`); verified by spawn and yaw/pitch sweeps.
- [x] **3D Transformations:** Translation, Scaling, and Rotation (`Transform3D`) applied to buildings and camera.
- [x] **3D Curves:** `ParametricCurve3D` sampled by `EngineShapeSampler` for `SunMoonOrbit` light motion and the 12-segment `CelestialDisc` sun/moon placement; `y > 900` horizon hiding and player-relative sky locking verified by `PolishProbe`.
- [x] **3D Surfaces:** `ParametricSurface3D` sampled by `EngineShapeSampler` for the mountain ring and five drifting clouds, including two above the city blocks; verified by `Phase2Probe`/`Phase4Probe`/`PolishProbe`.
- [x] **Hidden Surface Removal:** `DepthBuffer` working cleanly without Z-fighting or inverted faces; verified by geometry/pixel probes.
- [x] **Double Buffering:** `FramePresenter` swaps completed frames; verified by concurrency stress with no partial-frame failures.
- [x] **Zero Errors/Exceptions:** Clean startup, runtime, shutdown, probes, and lit yaw sweep.
- [x] **Custom Pixel Method Only:** Geometry written through `Rasterizer`/`DepthBuffer` and pixel-array clear; zero Java2D geometry/text drawing calls.

## Extra Credit / Higher Evaluation
- [x] **Lighting & Shading:** `PhongLighting`, `Light`, `Material` driven by a 360-second smoothstep day/afternoon/sunset/night cycle with peak daylight intensity `2.5`; per-pixel on streets, centroid on ground/buildings.
- [x] **Shadows:** 512×512 `ShadowMap` + `ShadowRenderer` casting dynamic building-wall shadows with standard `PhongLighting` and dynamic warm ambient; ground/street receive shadows, buildings cast without wall darkening, verified by `PolishProbe` in both perspective and parallel views.
- [x] **Multithreading:** Independent `OrbitTask`, `CloudTask`, and `StarTask` run on a bounded 3-thread `ThreadPoolExecutor`; immutable `AnimationSnapshot` records publish results; `Phase4Probe` and `ConcurrencyProbe` verify bounds, drift/twinkle, and clean shutdown.
- [x] **Animation & Visual Complexity:** Cohesive cozy 3D city with 15 block-aligned buildings, varied 2×2/2×3/3×3/3×4 window grids, interactive first-person exploration, mountains/clouds, 12-segment sky-locked celestial discs, 81 naturally scattered twinkling stars, and day/night lighting transitions; 1546 maximum triangles stay under the 1600 ceiling.
- [x] **Final Verification:** `T-050` passes with `./run.sh project`, all probes, `PolishProbe`, Bench, and `ConcurrencyProbe`; bottom rows remain filled at `−15°` and `+5°` across the walkable grid and yaw range, with scattered star elevations and parallel sky-body filtering checked explicitly.
