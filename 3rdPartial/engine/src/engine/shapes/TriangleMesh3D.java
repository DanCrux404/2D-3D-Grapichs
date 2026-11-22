package engine.shapes;

import engine.core.Point3D;
import engine.core.ProjectedPoint;
import engine.lighting.Light;
import engine.lighting.Material;
import engine.lighting.PhongLighting;
import engine.raster.Rasterizer;
import engine.shadow.ShadowMap;
import engine.shadow.ShadowRenderer;
import engine.transform.PerspectiveProjection;
import engine.transform.Projection3D;
import engine.transform.Transform3D;
import engine.util.ColorUtils;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class TriangleMesh3D implements SurfaceMesh {

    private final Point3D[][] vertices;
    private final Point3D center;
    private final Line3D line;
    private final Projection3D projection;

    private double translationX = 0.0;
    private double translationY = 0.0;
    private double translationZ = 0.0;

    private double scaleX = 1.0;
    private double scaleY = 1.0;
    private double scaleZ = 1.0;

    private double rotationX = 0.0;
    private double rotationY = 0.0;
    private double rotationZ = 0.0;

    public TriangleMesh3D(Point3D[][] vertices, Projection3D projection) {
        if (vertices == null || vertices.length < 2) {
            throw new IllegalArgumentException("A surface must have at least 2 rows.");
        }

        if (projection == null) {
            throw new IllegalArgumentException("Projection cannot be null.");
        }

        if (vertices[0] == null || vertices[0].length < 2) {
            throw new IllegalArgumentException("A surface must have at least 2 columns.");
        }

        int columns = vertices[0].length;

        this.vertices = new Point3D[vertices.length][columns];

        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < vertices.length; i++) {

            if (vertices[i] == null || vertices[i].length != columns) {
                throw new IllegalArgumentException("Surface mesh must be rectangular.");
            }

            for (int j = 0; j < columns; j++) {

                Point3D point = vertices[i][j];

                if (point == null) {
                    throw new IllegalArgumentException("Surface vertices cannot be null.");
                }

                this.vertices[i][j] = point;

                minX = Math.min(minX, point.getX());
                maxX = Math.max(maxX, point.getX());

                minY = Math.min(minY, point.getY());
                maxY = Math.max(maxY, point.getY());

                minZ = Math.min(minZ, point.getZ());
                maxZ = Math.max(maxZ, point.getZ());
            }
        }

        center = new Point3D((minX + maxX) / 2.0, (minY + maxY) / 2.0, (minZ + maxZ) / 2.0);
        this.projection = projection;
        line = new Line3D(projection);
    }

    @Override
    public void translate(double dx, double dy, double dz) {
        if (!Double.isFinite(dx) || !Double.isFinite(dy) || !Double.isFinite(dz)) {
            throw new IllegalArgumentException("Translation values must be finite.");
        }

        translationX += dx;
        translationY += dy;
        translationZ += dz;
    }

    @Override
    public void scale(double sx, double sy, double sz) {
        if (!Double.isFinite(sx)
                || !Double.isFinite(sy)
                || !Double.isFinite(sz)
                || sx <= 0
                || sy <= 0
                || sz <= 0) {
            throw new IllegalArgumentException("Scale values must be positive and finite.");
        }

        scaleX *= sx;
        scaleY *= sy;
        scaleZ *= sz;
    }

    @Override
    public void rotateX(double angle) {
        if (!Double.isFinite(angle)) {
            throw new IllegalArgumentException("Rotation angle must be finite.");
        }

        rotationX += angle;
    }

    @Override
    public void rotateY(double angle) {
        if (!Double.isFinite(angle)) {
            throw new IllegalArgumentException("Rotation angle must be finite.");
        }

        rotationY += angle;
    }

    @Override
    public void rotateZ(double angle) {
        if (!Double.isFinite(angle)) {
            throw new IllegalArgumentException("Rotation angle must be finite.");
        }

        rotationZ += angle;
    }

    private Point3D transformPoint(Point3D point) {

        double[][] toOrigin =
                Transform3D.translation(-center.getX(), -center.getY(), -center.getZ());

        double[][] scaling = Transform3D.scaling(scaleX, scaleY, scaleZ);

        double[][] rx = Transform3D.rotationX(rotationX);
        double[][] ry = Transform3D.rotationY(rotationY);
        double[][] rz = Transform3D.rotationZ(rotationZ);

        double[][] toPosition =
                Transform3D.translation(
                        center.getX() + translationX,
                        center.getY() + translationY,
                        center.getZ() + translationZ);

        Point3D transformed = Transform3D.apply(point, toOrigin);
        transformed = Transform3D.apply(transformed, scaling);
        transformed = Transform3D.apply(transformed, rx);
        transformed = Transform3D.apply(transformed, ry);
        transformed = Transform3D.apply(transformed, rz);
        transformed = Transform3D.apply(transformed, toPosition);

        return transformed;
    }

    @Override
    public void draw(BufferedImage buffer, Color color) {

        if (buffer == null || color == null) {
            throw new IllegalArgumentException("Buffer and color cannot be null.");
        }

        int rows = vertices.length;
        int columns = vertices[0].length;

        Point3D[][] transformedVertices = new Point3D[rows][columns];

        // Transform all vertices.
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                transformedVertices[i][j] = transformPoint(vertices[i][j]);
            }
        }

        // Draw horizontal and vertical edges.
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {

                if (j < columns - 1) {
                    line.draw(
                            buffer,
                            transformedVertices[i][j],
                            transformedVertices[i][j + 1],
                            color);
                }

                if (i < rows - 1) {
                    line.draw(
                            buffer,
                            transformedVertices[i][j],
                            transformedVertices[i + 1][j],
                            color);
                }
            }
        }

        // Draw the diagonal of every cell.
        for (int i = 0; i < rows - 1; i++) {
            for (int j = 0; j < columns - 1; j++) {

                line.draw(
                        buffer,
                        transformedVertices[i][j],
                        transformedVertices[i + 1][j + 1],
                        color);
            }
        }
    }

    /**
     * Draws the triangular surface using depth-aware rasterization.
     *
     * <p>Each mesh cell is divided into two triangles, following the same diagonal used by the
     * traditional wireframe rendering.
     *
     * @param buffer image where the surface will be drawn
     * @param color surface color
     * @param rasterizer depth-aware rasterizer
     */
    @Override
    public void draw(BufferedImage buffer, Color color, Rasterizer rasterizer) {

        if (buffer == null || color == null || rasterizer == null) {
            throw new IllegalArgumentException("Buffer, color and rasterizer cannot be null.");
        }

        int rows = vertices.length;
        int columns = vertices[0].length;

        // Transform all vertices.
        Point3D[][] transformedVertices = new Point3D[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                transformedVertices[i][j] = transformPoint(vertices[i][j]);
            }
        }

        // Project all vertices while preserving their depth.
        ProjectedPoint[][] projectedVertices = new ProjectedPoint[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                projectedVertices[i][j] = projection.projectWithDepth(transformedVertices[i][j]);
            }
        }

        // Fill each cell using two triangles.
        for (int i = 0; i < rows - 1; i++) {
            for (int j = 0; j < columns - 1; j++) {

                ProjectedPoint p1 = projectedVertices[i][j];
                ProjectedPoint p2 = projectedVertices[i][j + 1];
                ProjectedPoint p3 = projectedVertices[i + 1][j + 1];
                ProjectedPoint p4 = projectedVertices[i + 1][j];

                // First triangle.
                rasterizer.fillTriangle(buffer, p1, p2, p3, color);

                // Second triangle.
                rasterizer.fillTriangle(buffer, p1, p3, p4, color);
            }
        }
    }

    /**
     * Draws the triangular mesh using a rainbow color gradient.
     *
     * @param buffer image where the mesh will be drawn
     */
    @Override
    public void drawRainbow(BufferedImage buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException("Buffer cannot be null.");
        }

        int rows = vertices.length;
        int columns = vertices[0].length;

        Point3D[][] transformedVertices = new Point3D[rows][columns];

        // Transform all vertices first.
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                transformedVertices[i][j] = transformPoint(vertices[i][j]);
            }
        }

        // Draw horizontal, vertical and diagonal edges.
        for (int i = 0; i < rows; i++) {
            double fraction = (double) i / (rows - 1);
            Color color = ColorUtils.rainbow(fraction);
            ;

            for (int j = 0; j < columns; j++) {

                // Horizontal edge.
                if (j + 1 < columns) {
                    line.draw(
                            buffer,
                            transformedVertices[i][j],
                            transformedVertices[i][j + 1],
                            color);
                }

                if (i + 1 < rows) {
                    double nextFraction = (double) (i + 1) / (rows - 1);

                    Color nextColor = ColorUtils.rainbow(fraction);
                    ;

                    // Vertical edge.
                    line.draw(
                            buffer,
                            transformedVertices[i][j],
                            transformedVertices[i + 1][j],
                            nextColor);

                    // Diagonal edge.
                    if (j + 1 < columns) {
                        line.draw(
                                buffer,
                                transformedVertices[i][j],
                                transformedVertices[i + 1][j + 1],
                                nextColor);
                    }
                }
            }
        }
    }

    @Override
    public void drawGradient(BufferedImage buffer, Color startColor, Color endColor) {
        if (buffer == null || startColor == null || endColor == null) {
            throw new IllegalArgumentException("Buffer and colors cannot be null.");
        }

        int rows = vertices.length;
        int columns = vertices[0].length;

        Point3D[][] transformedVertices = new Point3D[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                transformedVertices[i][j] = transformPoint(vertices[i][j]);
            }
        }

        for (int i = 0; i < rows; i++) {
            double fraction = (double) i / (rows - 1);

            Color color = ColorUtils.interpolate(startColor, endColor, fraction);

            Color nextColor =
                    i + 1 < rows
                            ? ColorUtils.interpolate(
                                    startColor, endColor, (double) (i + 1) / (rows - 1))
                            : color;

            Color middleColor = ColorUtils.interpolate(startColor, endColor, fraction);

            for (int j = 0; j < columns; j++) {

                if (j + 1 < columns) {
                    line.draw(
                            buffer,
                            transformedVertices[i][j],
                            transformedVertices[i][j + 1],
                            color);
                }

                if (i + 1 < rows) {
                    line.draw(
                            buffer,
                            transformedVertices[i][j],
                            transformedVertices[i + 1][j],
                            middleColor);

                    if (j + 1 < columns) {
                        line.draw(
                                buffer,
                                transformedVertices[i][j],
                                transformedVertices[i + 1][j + 1],
                                middleColor);
                    }
                }
            }
        }
    }

    /**
     * Draws the mesh using Phong illumination and Z-buffering.
     *
     * @param buffer image where the mesh will be drawn
     * @param rasterizer rasterizer responsible for depth testing
     * @param light point light source
     * @param material surface material
     * @param lighting Phong illumination calculator
     * @param viewDirection direction toward the viewer
     */
    public void draw(
            BufferedImage buffer,
            Rasterizer rasterizer,
            Light light,
            Material material,
            PhongLighting lighting,
            Point3D viewDirection) {

        if (buffer == null
                || rasterizer == null
                || light == null
                || material == null
                || lighting == null
                || viewDirection == null) {
            throw new IllegalArgumentException("Rendering parameters cannot be null.");
        }

        Point3D[][] transformed = new Point3D[vertices.length][vertices[0].length];
        ProjectedPoint[][] projected = new ProjectedPoint[vertices.length][vertices[0].length];

        // Transform and project all mesh vertices.
        for (int i = 0; i < vertices.length; i++) {
            for (int j = 0; j < vertices[i].length; j++) {

                transformed[i][j] = transformPoint(vertices[i][j]);

                projected[i][j] = projection.projectWithDepth(transformed[i][j]);
            }
        }

        // Calculate illumination and rasterize each cell.
        for (int i = 0; i < vertices.length - 1; i++) {
            for (int j = 0; j < vertices[i].length - 1; j++) {

                Point3D p1 = transformed[i][j];
                Point3D p2 = transformed[i][j + 1];
                Point3D p3 = transformed[i + 1][j + 1];
                Point3D p4 = transformed[i + 1][j];

                ProjectedPoint pp1 = projected[i][j];
                ProjectedPoint pp2 = projected[i][j + 1];
                ProjectedPoint pp3 = projected[i + 1][j + 1];
                ProjectedPoint pp4 = projected[i + 1][j];

                drawLitTriangle(
                        buffer,
                        rasterizer,
                        p1,
                        p2,
                        p3,
                        pp1,
                        pp2,
                        pp3,
                        light,
                        material,
                        lighting,
                        viewDirection);

                drawLitTriangle(
                        buffer,
                        rasterizer,
                        p1,
                        p3,
                        p4,
                        pp1,
                        pp3,
                        pp4,
                        light,
                        material,
                        lighting,
                        viewDirection);
            }
        }
    }

    /**
     * Draws the mesh using Gouraud shading and Z-buffering.
     *
     * @param buffer destination image
     * @param rasterizer depth-aware rasterizer
     * @param light point light source
     * @param material surface material
     * @param lighting Phong illumination calculator
     * @param viewDirection direction toward the viewer
     */
    public void drawGouraud(
            BufferedImage buffer,
            Rasterizer rasterizer,
            Light light,
            Material material,
            PhongLighting lighting,
            Point3D viewDirection) {

        if (buffer == null
                || rasterizer == null
                || light == null
                || material == null
                || lighting == null
                || viewDirection == null) {
            throw new IllegalArgumentException("Rendering parameters cannot be null.");
        }

        int rows = vertices.length;
        int columns = vertices[0].length;

        Point3D[][] transformed = new Point3D[rows][columns];
        ProjectedPoint[][] projected = new ProjectedPoint[rows][columns];

        // Transform and project all vertices.
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                transformed[i][j] = transformPoint(vertices[i][j]);
                projected[i][j] = projection.projectWithDepth(transformed[i][j]);
            }
        }

        // Calculate the accumulated normals at every vertex.
        double[][][] normalSums = new double[rows][columns][3];

        for (int i = 0; i < rows - 1; i++) {
            for (int j = 0; j < columns - 1; j++) {

                Point3D p1 = transformed[i][j];
                Point3D p2 = transformed[i][j + 1];
                Point3D p3 = transformed[i + 1][j + 1];
                Point3D p4 = transformed[i + 1][j];

                // First triangle: p1, p2, p3.
                Point3D normal1 = calculateNormal(p1, p2, p3);

                if (normal1 != null) {
                    accumulateNormal(normalSums, i, j, normal1);
                    accumulateNormal(normalSums, i, j + 1, normal1);
                    accumulateNormal(normalSums, i + 1, j + 1, normal1);
                }

                // Second triangle: p1, p3, p4.
                Point3D normal2 = calculateNormal(p1, p3, p4);

                if (normal2 != null) {
                    accumulateNormal(normalSums, i, j, normal2);
                    accumulateNormal(normalSums, i + 1, j + 1, normal2);
                    accumulateNormal(normalSums, i + 1, j, normal2);
                }
            }
        }

        // Normalize the accumulated normals and calculate vertex colors.
        Point3D[][] vertexNormals = new Point3D[rows][columns];
        Color[][] vertexColors = new Color[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {

                vertexNormals[i][j] = normalizeAccumulatedNormal(normalSums[i][j]);

                if (vertexNormals[i][j] != null) {
                    vertexColors[i][j] =
                            lighting.calculateColor(
                                    transformed[i][j],
                                    vertexNormals[i][j],
                                    viewDirection,
                                    light,
                                    material);
                }
            }
        }

        // Rasterize every cell using its interpolated vertex colors.
        for (int i = 0; i < rows - 1; i++) {
            for (int j = 0; j < columns - 1; j++) {

                ProjectedPoint p1 = projected[i][j];
                ProjectedPoint p2 = projected[i][j + 1];
                ProjectedPoint p3 = projected[i + 1][j + 1];
                ProjectedPoint p4 = projected[i + 1][j];

                Color c1 = vertexColors[i][j];
                Color c2 = vertexColors[i][j + 1];
                Color c3 = vertexColors[i + 1][j + 1];
                Color c4 = vertexColors[i + 1][j];

                // First triangle: p1, p2, p3.
                if (c1 != null && c2 != null && c3 != null) {
                    rasterizer.fillTriangle(buffer, p1, p2, p3, c1, c2, c3);
                }

                // Second triangle: p1, p3, p4.
                if (c1 != null && c3 != null && c4 != null) {
                    rasterizer.fillTriangle(buffer, p1, p3, p4, c1, c3, c4);
                }
            }
        }
    }

    /** Adds a triangle normal to the accumulated normal of a vertex. */
    private void accumulateNormal(double[][][] normalSums, int row, int column, Point3D normal) {

        normalSums[row][column][0] += normal.getX();
        normalSums[row][column][1] += normal.getY();
        normalSums[row][column][2] += normal.getZ();
    }

    /** Normalizes the accumulated normal of a vertex. */
    private Point3D normalizeAccumulatedNormal(double[] normalSum) {

        double nx = normalSum[0];
        double ny = normalSum[1];
        double nz = normalSum[2];

        double magnitude = Math.sqrt(nx * nx + ny * ny + nz * nz);

        if (magnitude < 1e-12) {
            return null;
        }

        return new Point3D(nx / magnitude, ny / magnitude, nz / magnitude);
    }

    /** Calculates the flat illumination of a triangle and draws it. */
    private void drawLitTriangle(
            BufferedImage buffer,
            Rasterizer rasterizer,
            Point3D a,
            Point3D b,
            Point3D c,
            ProjectedPoint pa,
            ProjectedPoint pb,
            ProjectedPoint pc,
            Light light,
            Material material,
            PhongLighting lighting,
            Point3D viewDirection) {

        Point3D normal = calculateNormal(a, b, c);

        // Ignore degenerate triangles.
        if (normal == null) {
            return;
        }

        Point3D center =
                new Point3D(
                        (a.getX() + b.getX() + c.getX()) / 3.0,
                        (a.getY() + b.getY() + c.getY()) / 3.0,
                        (a.getZ() + b.getZ() + c.getZ()) / 3.0);

        Color illuminatedColor =
                lighting.calculateColor(center, normal, viewDirection, light, material);

        rasterizer.fillTriangle(buffer, pa, pb, pc, illuminatedColor);
    }

    /** Calculates the normalized normal vector of a triangle. */
    private Point3D calculateNormal(Point3D a, Point3D b, Point3D c) {

        double abX = b.getX() - a.getX();
        double abY = b.getY() - a.getY();
        double abZ = b.getZ() - a.getZ();

        double acX = c.getX() - a.getX();
        double acY = c.getY() - a.getY();
        double acZ = c.getZ() - a.getZ();

        double nx = abY * acZ - abZ * acY;
        double ny = abZ * acX - abX * acZ;
        double nz = abX * acY - abY * acX;

        double magnitude = Math.sqrt(nx * nx + ny * ny + nz * nz);

        if (magnitude < 1e-12) {
            return null;
        }

        return new Point3D(nx / magnitude, ny / magnitude, nz / magnitude);
    }

    /**
     * Draws the mesh using per-pixel Phong shading and Z-buffering.
     *
     * @param buffer destination image
     * @param rasterizer depth-aware rasterizer
     * @param light point light source
     * @param material surface material
     * @param lighting Phong illumination calculator
     * @param viewDirection direction toward the viewer
     */
    public void drawPhong(
            BufferedImage buffer,
            Rasterizer rasterizer,
            Light light,
            Material material,
            PhongLighting lighting,
            Point3D viewDirection) {

        drawPhong(buffer, rasterizer, light, material, lighting, viewDirection, null, null, 0.0);
    }

    /**
     * Draws the mesh using per-pixel Phong shading, Z-buffering and shadow mapping.
     *
     * @param buffer destination image
     * @param rasterizer depth-aware rasterizer
     * @param light point light source
     * @param material surface material
     * @param lighting Phong illumination calculator
     * @param viewDirection direction toward the viewer
     * @param shadowMap shadow depth map
     * @param lightProjection projection used by the light
     * @param shadowBias depth bias to reduce shadow acne
     */
    public void drawPhong(
            BufferedImage buffer,
            Rasterizer rasterizer,
            Light light,
            Material material,
            PhongLighting lighting,
            Point3D viewDirection,
            ShadowMap shadowMap,
            Projection3D lightProjection,
            double shadowBias) {

        if (buffer == null
                || rasterizer == null
                || light == null
                || material == null
                || lighting == null
                || viewDirection == null) {
            throw new IllegalArgumentException("Rendering parameters cannot be null.");
        }

        if ((shadowMap == null) != (lightProjection == null)) {
            throw new IllegalArgumentException(
                    "Shadow map and light projection must be provided together.");
        }

        if (!Double.isFinite(shadowBias) || shadowBias < 0.0) {
            throw new IllegalArgumentException("Shadow bias must be finite and non-negative.");
        }

        int rows = vertices.length;
        int columns = vertices[0].length;

        Point3D[][] transformed = new Point3D[rows][columns];
        ProjectedPoint[][] projected = new ProjectedPoint[rows][columns];

        // Transform and project all vertices.
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                transformed[i][j] = transformPoint(vertices[i][j]);
                projected[i][j] = projection.projectWithDepth(transformed[i][j]);
            }
        }

        // Accumulate triangle normals at every vertex.
        double[][][] normalSums = new double[rows][columns][3];

        for (int i = 0; i < rows - 1; i++) {
            for (int j = 0; j < columns - 1; j++) {

                Point3D p1 = transformed[i][j];
                Point3D p2 = transformed[i][j + 1];
                Point3D p3 = transformed[i + 1][j + 1];
                Point3D p4 = transformed[i + 1][j];

                Point3D normal1 = calculateNormal(p1, p2, p3);

                if (normal1 != null) {
                    accumulateNormal(normalSums, i, j, normal1);
                    accumulateNormal(normalSums, i, j + 1, normal1);
                    accumulateNormal(normalSums, i + 1, j + 1, normal1);
                }

                Point3D normal2 = calculateNormal(p1, p3, p4);

                if (normal2 != null) {
                    accumulateNormal(normalSums, i, j, normal2);
                    accumulateNormal(normalSums, i + 1, j + 1, normal2);
                    accumulateNormal(normalSums, i + 1, j, normal2);
                }
            }
        }

        // Normalize the accumulated vertex normals.
        Point3D[][] vertexNormals = new Point3D[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                vertexNormals[i][j] = normalizeAccumulatedNormal(normalSums[i][j]);
            }
        }

        // Perspective projection requires perspective-correct interpolation.
        boolean perspectiveCorrect = projection instanceof PerspectiveProjection;

        // Rasterize each cell as two triangles.
        for (int i = 0; i < rows - 1; i++) {
            for (int j = 0; j < columns - 1; j++) {

                int nextRow = i + 1;
                int nextColumn = j + 1;

                Point3D p1 = transformed[i][j];
                Point3D p2 = transformed[i][nextColumn];
                Point3D p3 = transformed[nextRow][nextColumn];
                Point3D p4 = transformed[nextRow][j];

                Point3D n1 = vertexNormals[i][j];
                Point3D n2 = vertexNormals[i][nextColumn];
                Point3D n3 = vertexNormals[nextRow][nextColumn];
                Point3D n4 = vertexNormals[nextRow][j];

                ProjectedPoint pp1 = projected[i][j];
                ProjectedPoint pp2 = projected[i][nextColumn];
                ProjectedPoint pp3 = projected[nextRow][nextColumn];
                ProjectedPoint pp4 = projected[nextRow][j];

                // First triangle: p1, p2, p3.
                if (n1 != null && n2 != null && n3 != null) {
                    rasterizer.fillTrianglePhong(
                            buffer,
                            pp1,
                            pp2,
                            pp3,
                            p1,
                            p2,
                            p3,
                            n1,
                            n2,
                            n3,
                            light,
                            material,
                            lighting,
                            viewDirection,
                            perspectiveCorrect,
                            shadowMap,
                            lightProjection,
                            shadowBias);
                }

                // Second triangle: p1, p3, p4.
                if (n1 != null && n3 != null && n4 != null) {
                    rasterizer.fillTrianglePhong(
                            buffer,
                            pp1,
                            pp3,
                            pp4,
                            p1,
                            p3,
                            p4,
                            n1,
                            n3,
                            n4,
                            light,
                            material,
                            lighting,
                            viewDirection,
                            perspectiveCorrect,
                            shadowMap,
                            lightProjection,
                            shadowBias);
                }
            }
        }
    }

    /**
     * Renders the mesh from the light's point of view and stores its depth in the shadow map.
     *
     * <p>Each mesh cell is divided into two triangles, following the same diagonal used by the
     * other rendering methods.
     *
     * @param shadowRenderer renderer responsible for generating the shadow map
     */
    public void renderShadowMap(ShadowRenderer shadowRenderer) {

        if (shadowRenderer == null) {
            throw new IllegalArgumentException("Shadow renderer cannot be null.");
        }

        int rows = vertices.length;
        int columns = vertices[0].length;

        // Transform all vertices using the mesh transformations.
        Point3D[][] transformedVertices = new Point3D[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                transformedVertices[i][j] = transformPoint(vertices[i][j]);
            }
        }

        // Render every cell as two triangles.
        for (int i = 0; i < rows - 1; i++) {
            for (int j = 0; j < columns - 1; j++) {

                Point3D p1 = transformedVertices[i][j];
                Point3D p2 = transformedVertices[i][j + 1];
                Point3D p3 = transformedVertices[i + 1][j + 1];
                Point3D p4 = transformedVertices[i + 1][j];

                // First triangle.
                shadowRenderer.renderTriangle(p1, p2, p3);

                // Second triangle.
                shadowRenderer.renderTriangle(p1, p3, p4);
            }
        }
    }
}
