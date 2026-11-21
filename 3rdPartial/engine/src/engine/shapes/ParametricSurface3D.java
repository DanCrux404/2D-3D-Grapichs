package engine.shapes;

import engine.core.Point3D;
import engine.raster.Rasterizer;
import engine.transform.Projection3D;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.function.DoubleBinaryOperator;

public class ParametricSurface3D {

    private final SurfaceMesh mesh;

    public ParametricSurface3D(
            DoubleBinaryOperator xFunction,
            DoubleBinaryOperator yFunction,
            DoubleBinaryOperator zFunction,
            double minS,
            double maxS,
            double minT,
            double maxT,
            int numberOfPointsS,
            int numberOfPointsT,
            Projection3D projection,
            MeshType meshType) {
        mesh =
                createMesh(
                        xFunction,
                        yFunction,
                        zFunction,
                        minS,
                        maxS,
                        minT,
                        maxT,
                        numberOfPointsS,
                        numberOfPointsT,
                        projection,
                        meshType);
    }

    private static SurfaceMesh createMesh(
            DoubleBinaryOperator xFunction,
            DoubleBinaryOperator yFunction,
            DoubleBinaryOperator zFunction,
            double minS,
            double maxS,
            double minT,
            double maxT,
            int numberOfPointsS,
            int numberOfPointsT,
            Projection3D projection,
            MeshType meshType) {
        if (projection == null || meshType == null) {
            throw new IllegalArgumentException("Projection and mesh type cannot be null.");
        }

        Point3D[][] vertices =
                generateVertices(
                        xFunction,
                        yFunction,
                        zFunction,
                        minS,
                        maxS,
                        minT,
                        maxT,
                        numberOfPointsS,
                        numberOfPointsT);

        return switch (meshType) {
            case RECTANGULAR -> new SurfaceMesh3D(vertices, projection);

            case TRIANGULAR -> new TriangleMesh3D(vertices, projection);
        };
    }

    private static Point3D[][] generateVertices(
            DoubleBinaryOperator xFunction,
            DoubleBinaryOperator yFunction,
            DoubleBinaryOperator zFunction,
            double minS,
            double maxS,
            double minT,
            double maxT,
            int numberOfPointsS,
            int numberOfPointsT) {
        if (xFunction == null || yFunction == null || zFunction == null) {
            throw new IllegalArgumentException("Surface functions cannot be null.");
        }

        if (!Double.isFinite(minS)
                || !Double.isFinite(maxS)
                || !Double.isFinite(minT)
                || !Double.isFinite(maxT)
                || minS >= maxS
                || minT >= maxT) {
            throw new IllegalArgumentException("Invalid parameter intervals.");
        }

        if (numberOfPointsS < 2
                || numberOfPointsT < 2
                || (long) numberOfPointsS * numberOfPointsT > 1_000_000) {
            throw new IllegalArgumentException("Invalid number of surface points.");
        }

        Point3D[][] vertices = new Point3D[numberOfPointsS][numberOfPointsT];

        double stepS = (maxS - minS) / (numberOfPointsS - 1);

        double stepT = (maxT - minT) / (numberOfPointsT - 1);

        for (int i = 0; i < numberOfPointsS; i++) {

            double s = (i == numberOfPointsS - 1) ? maxS : minS + i * stepS;

            for (int j = 0; j < numberOfPointsT; j++) {

                double t = (j == numberOfPointsT - 1) ? maxT : minT + j * stepT;

                double x = xFunction.applyAsDouble(s, t);
                double y = yFunction.applyAsDouble(s, t);
                double z = zFunction.applyAsDouble(s, t);

                if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
                    throw new IllegalArgumentException("Surface coordinates must be finite.");
                }

                vertices[i][j] = new Point3D(x, y, z);
            }
        }

        return vertices;
    }

    public void translate(double dx, double dy, double dz) {
        mesh.translate(dx, dy, dz);
    }

    public void scale(double sx, double sy, double sz) {
        mesh.scale(sx, sy, sz);
    }

    public void rotateX(double angle) {
        mesh.rotateX(angle);
    }

    public void rotateY(double angle) {
        mesh.rotateY(angle);
    }

    public void rotateZ(double angle) {
        mesh.rotateZ(angle);
    }

    public void draw(BufferedImage buffer, Color color) {
        mesh.draw(buffer, color);
    }

    /**
     * Draws the parametric surface using Z-buffer visibility.
     *
     * @param buffer image where the surface will be drawn
     * @param color surface color
     * @param rasterizer depth-aware rasterizer
     */
    public void draw(BufferedImage buffer, Color color, Rasterizer rasterizer) {

        mesh.draw(buffer, color, rasterizer);
    }

    /**
     * Draws the parametric surface with a rainbow gradient.
     *
     * @param buffer image where the surface will be drawn
     */
    public void drawRainbow(BufferedImage buffer) {
        mesh.drawRainbow(buffer);
    }

    public void drawGradient(BufferedImage buffer, Color startColor, Color endColor) {
        mesh.drawGradient(buffer, startColor, endColor);
    }
}
