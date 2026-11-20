package engine.shapes;

import engine.core.Point3D;
import engine.transform.Projection3D;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.function.DoubleBinaryOperator;

public class ExplicitSurface3D {

    private final SurfaceMesh mesh;

    public ExplicitSurface3D(
            DoubleBinaryOperator function,
            double minX,
            double maxX,
            double minY,
            double maxY,
            int numberOfPointsX,
            int numberOfPointsY,
            Projection3D projection,
            MeshType meshType) {
        mesh =
                createMesh(
                        function,
                        minX,
                        maxX,
                        minY,
                        maxY,
                        numberOfPointsX,
                        numberOfPointsY,
                        projection,
                        meshType);
    }

    private static SurfaceMesh createMesh(
            DoubleBinaryOperator function,
            double minX,
            double maxX,
            double minY,
            double maxY,
            int numberOfPointsX,
            int numberOfPointsY,
            Projection3D projection,
            MeshType meshType) {
        if (projection == null || meshType == null) {
            throw new IllegalArgumentException("Projection and mesh type cannot be null.");
        }

        Point3D[][] vertices =
                generateVertices(
                        function, minX, maxX, minY, maxY, numberOfPointsX, numberOfPointsY);

        return switch (meshType) {
            case RECTANGULAR -> new SurfaceMesh3D(vertices, projection);

            case TRIANGULAR -> new TriangleMesh3D(vertices, projection);
        };
    }

    private static Point3D[][] generateVertices(
            DoubleBinaryOperator function,
            double minX,
            double maxX,
            double minY,
            double maxY,
            int numberOfPointsX,
            int numberOfPointsY) {
        if (function == null) {
            throw new IllegalArgumentException("Surface function cannot be null.");
        }

        if (!Double.isFinite(minX)
                || !Double.isFinite(maxX)
                || !Double.isFinite(minY)
                || !Double.isFinite(maxY)
                || minX >= maxX
                || minY >= maxY) {
            throw new IllegalArgumentException("Invalid surface domain.");
        }

        if (numberOfPointsX < 2
                || numberOfPointsY < 2
                || (long) numberOfPointsX * numberOfPointsY > 1_000_000) {
            throw new IllegalArgumentException("Invalid number of surface points.");
        }

        Point3D[][] vertices = new Point3D[numberOfPointsX][numberOfPointsY];

        double stepX = (maxX - minX) / (numberOfPointsX - 1);

        double stepY = (maxY - minY) / (numberOfPointsY - 1);

        for (int i = 0; i < numberOfPointsX; i++) {

            double x = (i == numberOfPointsX - 1) ? maxX : minX + i * stepX;

            for (int j = 0; j < numberOfPointsY; j++) {

                double y = (j == numberOfPointsY - 1) ? maxY : minY + j * stepY;

                double z = function.applyAsDouble(x, y);

                if (!Double.isFinite(z)) {
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

    public void drawGradient(BufferedImage buffer, Color startColor, Color endColor) {
        mesh.drawGradient(buffer, startColor, endColor);
    }
}
