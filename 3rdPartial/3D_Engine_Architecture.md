# 3D Graphics Engine

## 1. Overview

The third partial extends the existing 2D graphics engine with
three-dimensional geometry and parallel projection.

The main design principle is **reuse instead of duplication**: the 3D
engine handles 3D points and projection, while the 2D engine continues
to handle rasterization.

A 3D line is not rasterized directly. Its endpoints are first projected
onto the XY plane, and the resulting 2D line is drawn with the existing
Bresenham implementation.

## 2. Project architecture

The project is organized into two engines and a test application:

``` text
2ndPartial/
  engine/
    src/engine/
      core/
        Pixel.java
        PixelPlotter.java
      shapes/
        Line.java
        ...
      transform/
        Point2D.java
        Transform2D.java

3rdPartial/
  engine/
    src/engine/
      core/
        Point3D.java
      shapes/
        Line3D.java
        Cube.java
      transform/
        ParallelProjection.java

  TestFigures/
    src/
      Main.java
      Canvas.java
  run.sh
```

The physical source files are in different project directories, but
classes can belong to the same Java package when they declare the same
`package` statement and are compiled together. The `run.sh` script
compiles the sources from both engines into the third partial's build
directory.

The third partial does **not** copy `Pixel.java`, `Line.java`,
`Point2D.java`, or `Transform2D.java`.

## 3. Responsibilities of the classes

### Existing 2D engine

  -----------------------------------------------------------------------
  Class                               Responsibility
  ----------------------------------- -----------------------------------
  `Pixel`                             Writes a colored pixel into a
                                      `BufferedImage`.

  `Line`                              Rasterizes a 2D line. Its
                                      `bresenham()` method is reused by
                                      the 3D engine.

  `Point2D`                           Represents an immutable point with
                                      `x` and `y` coordinates.

  `Transform2D`                       Provides 2D translation, scaling,
                                      and rotation operations.
  -----------------------------------------------------------------------

### New 3D engine

  -----------------------------------------------------------------------
  Class                               Responsibility
  ----------------------------------- -----------------------------------
  `Point3D`                           Represents an immutable point with
                                      `x`, `y`, and `z` coordinates.

  `ParallelProjection`                Projects a `Point3D` onto the XY
                                      plane and returns a `Point2D`.

  `Line3D`                            Projects two 3D endpoints and draws
                                      the resulting 2D line using the
                                      existing Bresenham algorithm.
  -----------------------------------------------------------------------

### Test application

  -----------------------------------------------------------------------
  Class                               Responsibility
  ----------------------------------- -----------------------------------
  `Main`                              Starts the Swing application on the
                                      Event Dispatch Thread.

  `Canvas`                            Owns the drawing buffer and
                                      displays it in a Swing window.

  -----------------------------------------------------------------------

The engine classes contain geometry and drawing logic. Swing-specific
responsibilities remain in the test application.

## 4. Parallel projection

Parallel projection maps a 3D point onto the XY plane (`z = 0`) along a
fixed projection direction:

\[ D = (x_p, y_p, z_p) \]

For an original point:

\[ P_1 = (x_1, y_1, z_1) \]

The projection line can be written parametrically as:

\[ P(u) = P_1 + uD \]

Its z-coordinate is:

\[ z_1 + uz_p \]

At the intersection with the XY plane, `z = 0`:

\[ z_1 + uz_p = 0 \]

Therefore:

\[ u = -`\frac{z_1}{z_p}`{=tex} \]

Substituting this parameter into the x and y coordinates gives the
professor's equations:

\[ `\boxed{x_2=x_1-\frac{x_pz_1}{z_p}}`{=tex} \]

\[ `\boxed{y_2=y_1-\frac{y_pz_1}{z_p}}`{=tex} \]

The projected point is:

\[ P_2 = (x_2,y_2) \]

The direction's z component must not be zero. If `zp = 0`, the
projection direction is parallel to the XY plane, so a unique
intersection is not generally defined.

### Implementation

`ParallelProjection` stores the direction vector and exposes:

-   `project(Point3D point)`: calculates and returns a `Point2D`.
-   `setDirection(xp, yp, zp)`: changes the projection direction.
-   `getXp()`, `getYp()`, and `getZp()`: retrieve the current direction
    components.

The class performs mathematical calculations only. It does not draw
pixels or depend on Swing.

## 5. Drawing a 3D line with Bresenham

A 3D line is defined by two endpoints:

\[ P_1=(x_1,y_1,z_1) \]

\[ P_2=(x_2,y_2,z_2) \]

`Line3D` performs these operations:

1.  Receives a `BufferedImage`, two `Point3D` endpoints, and a color.
2.  Projects the first endpoint using `ParallelProjection`.
3.  Projects the second endpoint using the same projection.
4.  Rounds the projected floating-point coordinates to integer pixel
    positions.
5.  Calls the existing `Line.bresenham()` method.

Conceptually:

``` java
Point2D projectedP1 = projection.project(p1);
Point2D projectedP2 = projection.project(p2);

Line.bresenham(
    buffer,
    (int) Math.round(projectedP1.getX()),
    (int) Math.round(projectedP1.getY()),
    (int) Math.round(projectedP2.getX()),
    (int) Math.round(projectedP2.getY()),
    color
);
```

This is why the 3D engine does not need its own pixel or line
rasterization algorithm.

## 6. Sharing the projection

`Line3D` receives a `ParallelProjection` object through its constructor
and keeps a reference to that same object.

This is useful because all edges of a figure can share one projection
instance. If the direction changes, every edge will use the updated
direction the next time it is drawn.

For example:

``` java
ParallelProjection projection =
    new ParallelProjection(2, 1, 5);

Line3D line = new Line3D(projection);

// Later, update the shared projection:
projection.setDirection(3, 1, 5);
```

The mouse interaction planned for the test application can update this
shared projection. The engine itself does not need to know about mouse
events.

## 7. Planned cube implementation

A cube has:

-   8 vertices.
-   6 faces.
-   12 unique edges.

The cube will store its eight vertices as `Point3D` objects. Its twelve
edges will be represented as pairs of vertex indices.

Each edge will be drawn by calling `Line3D.draw()` with the
corresponding two vertices.

An edge list avoids drawing each shared face edge more than once.

## 8. Rendering pipeline

The complete drawing flow is:

``` text
Cube
  |
  | supplies pairs of Point3D vertices
  v
Line3D
  |
  | projects both endpoints
  v
ParallelProjection
  |
  | calculates x2 and y2
  v
Point2D
  |
  | supplies 2D coordinates
  v
Line.bresenham()
  |
  | rasterizes pixels
  v
Pixel
  |
  v
BufferedImage
  |
  v
Canvas displays the image
```

`Canvas` owns and displays the image buffer. The engine draws into that
buffer but does not create or manage the Swing window.

## 9. Current implementation status

-   [x] Reuse the existing 2D engine.
-   [x] Reuse `Point2D`.
-   [x] Create `Point3D`.
-   [x] Implement parallel projection onto the XY plane.
-   [x] Implement `Line3D` using the existing Bresenham algorithm.
-   [ ] Draw and verify one projected 3D line in `TestFigures`.
-   [ ] Implement the cube with 8 vertices and 12 edges.
-   [ ] Add mouse interaction to change the projection direction.

## 10. Build and run

From the `3rdPartial` directory:

``` bash
./run.sh TestFigures
```

The script compiles the sources from the second partial engine, the
third partial engine, and the selected practice before launching `Main`.

This keeps the 3D engine independent from Swing while allowing the test
application to reuse both engines.
