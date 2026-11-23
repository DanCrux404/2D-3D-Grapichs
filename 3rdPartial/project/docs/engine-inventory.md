# Engine Inventory (verified against source, Phase 0)

All paths below exist and every signature was read from source. The 3rd-partial
engine depends on the 2nd-partial engine (`../2ndPartial/engine/src`): `Point2D`,
`Line.bresenham`, and `Pixel` are reused, not duplicated. `run.sh` compiles both
engines plus the selected practice directory and runs default-package `Main`.

## engine.core (`engine/src/engine/core/`)
- `Point3D(double x, double y, double z)` — immutable; `getX/getY/getZ`.
- `ProjectedPoint(double x, double y, double depth)` — immutable; `getX/getY/getDepth`.
- `DepthBuffer(int width, int height)` — `clear()` (fills −∞),
  `boolean testAndSet(int x, int y, double depth, Color color, BufferedImage image)`,
  `getDepth(x,y)`, `getWidth/getHeight`. **Greater depth = closer.** Out-of-bounds
  writes return false; image dimensions must match.

## engine.transform (`engine/src/engine/transform/`)
- `Projection3D` (interface): `Point2D project(Point3D)`,
  `ProjectedPoint projectWithDepth(Point3D)`. `Point2D` comes from the 2nd engine
  (same package, co-compiled).
- `PerspectiveProjection(double xc, double yc, double zc)` — fixed center of
  projection, plane z=0, camera looks toward +z (use zc < 0).
  `projectWithDepth`: depth = 1/(z−zc); **throws IllegalArgumentException if
  z−zc ≤ 0** (point at/behind camera). No near-plane clipping in engine.
  Getters `getXc/getYc/getZc`; no setters.
- `ParallelProjection(double xp, double yp, double zp)` — oblique onto z=0:
  x' = x − xp·z/zp, y' = y − yp·z/zp. `setDirection(xp,yp,zp)` mutable (shared
  instances see updates); depth = −|z/zp| (greater = closer to plane). zp ≠ 0.
- `Transform3D` (static, homogeneous column vectors P' = M·P, angles in degrees):
  `translation(dx,dy,dz)`, `scaling(sx,sy,sz)`, `rotationX/Y/Z(angle)` → `double[4][4]`;
  `Point3D apply(Point3D, double[][])`. No matrix-multiply helper; compose by
  applying matrices sequentially. rotationY(θ): x' = c·x + s·z, z' = −s·x + c·z.
  rotationX(θ): y' = c·y − s·z, z' = s·y + c·z.

## engine.raster (`engine/src/engine/raster/Rasterizer.java`)
- `Rasterizer(DepthBuffer)` — all writes go through `DepthBuffer.testAndSet`.
- `drawLine(BufferedImage, ProjectedPoint, ProjectedPoint, Color)`.
- `fillTriangle(image, p1, p2, p3, Color)` — flat, barycentric, screen-space
  depth interpolation (subdivide large perspective triangles to avoid artifacts).
- `fillTriangle(image, p1, p2, p3, Color c1, Color c2, Color c3)` — Gouraud.
- `fillTrianglePhong(image, p1, p2, p3, pos1, pos2, pos3, n1, n2, n3, light,
  material, lighting, viewDirection, boolean perspectiveCorrect, shadowMap,
  lightProjection, double shadowBias)` — per-pixel Phong; shadowMap and
  lightProjection must be provided together (or both null); bias ≥ 0.

## engine.lighting (`engine/src/engine/lighting/`)
- `Light(Point3D position, Color color, double intensity)` — **immutable**;
  recreate per frame for a moving sun. Getters only.
- `Material(Color base, double ambient, double diffuse, double specular, double shininess)`.
- `PhongLighting()` — `Color calculateColor(point, normal, viewDirection, light,
  material[, double visibility 0..1])`. Point light, attenuation
  1/(1+5e-6·d²); ambient term unaffected by visibility/shadow. Zero-length
  normal/view vectors throw.

## engine.shadow (`engine/src/engine/shadow/`)
- `ShadowMap(int width, int height)` — `clear()`, `setDepth(x,y,depth)`,
  `isInShadow(...)`, `getDepth`, dimensions.
- `ShadowRenderer(ShadowMap, Projection3D lightProjection)` — `clear()`,
  `renderTriangle(Point3D a, b, c)`, `getShadowMap()`.
- Verified two-pass flow: `TestFigures/Canvas.java` `testShadowMapping()` —
  pass 1 `renderShadowMap` per mesh, pass 2 `drawPhong(..., shadowMap,
  lightProjection, bias)`.

## engine.shapes (`engine/src/engine/shapes/`)
All shape classes bake in the `Projection3D` at construction (final field) and
accumulate `translate/scale/rotateX/Y/Z` (degrees, about bounding-box center).
- `Line3D(Projection3D)` — `draw(buffer, Point3D p1, Point3D p2, Color)` (Bresenham).
- `Cube(Point3D center, double size, Projection3D)` — `draw(buffer, color)`
  wireframe; `draw(buffer, color, Rasterizer)` filled.
- `ExplicitCurve3D(DoubleUnaryOperator f, min, max, int n, Projection3D)` —
  y=f(x), z=0; `draw(buffer, color[, Rasterizer])`.
- `ParametricCurve3D(fx, fy, fz, min, max, int n, Projection3D)` —
  `DoubleUnaryOperator` components; same draw overloads.
- `ExplicitSurface3D(DoubleBinaryOperator f, minX, maxX, minY, maxY, nX, nY,
  Projection3D, MeshType)` — z=f(x,y).
- `ParametricSurface3D(fx, fy, fz, minS, maxS, minT, maxT, nS, nT, Projection3D,
  MeshType)` — `DoubleBinaryOperator` components.
- `MeshType { RECTANGULAR, TRIANGULAR }` — surfaces delegate to
  `SurfaceMesh3D` (RECTANGULAR: wireframe/gradient, **no lighting/shadows**) or
  `TriangleMesh3D` (TRIANGULAR: full pipeline).
- `TriangleMesh3D(Point3D[][] grid, Projection3D)` — `draw(buffer,color[,rasterizer])`,
  `draw(buffer, rasterizer, light, material, lighting, viewDirection)` flat-lit,
  `drawGouraud(...)`, `drawPhong(...[, shadowMap, lightProjection, bias])`,
  `renderShadowMap(ShadowRenderer)`, `drawRainbow/drawGradient`.
- `SurfaceMesh3D(Point3D[][] grid, Projection3D)` — wireframe + gradient only.
- `SurfaceMesh` (interface) — shared mesh contract.

## engine.util
- `ColorUtils.interpolate(...)`, `ColorUtils.rainbow(double)`.

## Verified constraints and risks for the city app
1. No camera/view transform and no near-plane clipping in the engine; mesh draw
   methods project every vertex and throw if any is behind the camera. The app
   must transform world vertices into the fixed camera frame (`Transform3D`)
   and clip triangles against the near plane before projecting/rasterizing.
2. Projection is fixed per shape instance → runtime projection toggle is handled
   by the app-side pipeline (`Projection3D.projectWithDepth` + `Rasterizer`),
   not by mutating engine shapes.
3. Depth scales differ per projection (1/(z−zc) vs −|z/zp|); never mix in one
   buffer; `DepthBuffer.clear()` every frame.
4. `Light` immutable → rebuild per frame. `ParallelProjection.setDirection` can
   track the sun for the shadow pass.
5. Per-pixel Phong + shadow pass is CPU-heavy at 800×600; keep meshes coarse and
   prefer Gouraud/flat for distant geometry.
6. Presentation `Graphics.drawImage` is the sanctioned Swing exception; buffer
   clears must use pixel writes (no `Graphics2D.fillRect`).

## Build/run (verified)
- `./run.sh TestFigures` and `./run.sh project` — javac/java 21, compiles
  `../2ndPartial/engine/src` + `engine/src` + practice dir, runs `Main`.
- Game code lives in `project/src` (default-package `Main.java` + `city.*`).
