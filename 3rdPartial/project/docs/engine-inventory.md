# Engine Inventory

This is an orientation map based on the previously completed graphics-engine work. The coding agent must verify every path, package, signature, and capability against the current repository before relying on it. Report discrepancies rather than guessing.

## Known third-partial structure
```text
engine/
  src/
    engine/
      core/
        Point3D.java
        ProjectedPoint.java
        DepthBuffer.java
      shapes/
        Line3D.java
        Cube.java
        ExplicitCurve3D.java
        ParametricCurve3D.java
        SurfaceMesh.java
        SurfaceMesh3D.java
        TriangleMesh3D.java
        MeshType.java
        ExplicitSurface3D.java
        ParametricSurface3D.java
      transform/
        ParallelProjection.java
        PerspectiveProjection.java
        Projection3D.java
        Transform3D.java
      util/
        ColorUtils.java
    raster/
      Rasterizer.java
  lighting/
    Light.java
    Material.java
    PhongLighting.java
  shadow/
    ShadowMap.java
    ShadowRenderer.java
```
The repository reuses second-partial engine classes too. Inspect their actual paths and APIs.

## Known capabilities to verify
- 3D points and projected points with depth.
- Parallel and perspective projection through `Projection3D`.
- Homogeneous 3D translation, scaling, and rotations.
- 3D lines and cube geometry.
- Explicit and parametric curves.
- Surface meshes, including rectangular and triangular modes.
- Depth buffer and depth-aware line/triangle rasterization.
- Flat, Gouraud, and per-pixel Phong shading paths.
- Point-light/material Phong illumination.
- Shadow-map storage and triangle-based shadow-map rendering.
- Color interpolation/rainbow utilities.

## Historical conventions to verify
- `Transform3D` uses homogeneous column vectors: `P' = T * P`.
- Greater depth means closer to the camera.
- Perspective depth uses reciprocal distance in the implemented projection.
- Rasterization writes pixels through the existing pixel mechanism.
- Existing 2D engine classes are reused; do not duplicate them.

## TestFigures usage reference
Inspect for projection setup, image/depth initialization, rasterizer construction, transformations, curves/surfaces, lighting/shading, shadow passes, buffer presentation, and input handling.

## Maintenance
After reconnaissance, add verified signatures and exact paths needed by the game. Keep this concise: summarize capabilities and point to source files instead of copying entire classes.
