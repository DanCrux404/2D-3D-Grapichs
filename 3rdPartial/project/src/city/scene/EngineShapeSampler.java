package city.scene;

import engine.core.DepthBuffer;
import engine.core.Point3D;
import engine.core.ProjectedPoint;
import engine.raster.Rasterizer;
import engine.shapes.ParametricCurve3D;
import engine.shapes.ParametricSurface3D;
import engine.transform.Point2D;
import engine.transform.Projection3D;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public final class EngineShapeSampler implements Projection3D {

    private final List<Point3D> points = new ArrayList<>();

    @Override
    public Point2D project(Point3D point) {
        points.add(point);
        return new Point2D(0.0, 0.0);
    }

    @Override
    public ProjectedPoint projectWithDepth(Point3D point) {
        points.add(point);
        return new ProjectedPoint(0.0, 0.0, 1.0);
    }

    public List<Point3D> sampleCurve(ParametricCurve3D curve) {
        points.clear();
        curve.draw(image(), Color.WHITE, rasterizer());
        return new ArrayList<>(points);
    }

    public Point3D[][] sampleSurface(ParametricSurface3D surface, int rows, int columns) {
        points.clear();
        surface.draw(image(), Color.WHITE, rasterizer());

        if (points.size() != rows * columns) {
            throw new IllegalStateException(
                    "Expected " + (rows * columns) + " surface points but recorded " + points.size());
        }

        Point3D[][] grid = new Point3D[rows][columns];

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                grid[row][column] = points.get(row * columns + column);
            }
        }

        return grid;
    }

    private static BufferedImage image() {
        return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    }

    private static Rasterizer rasterizer() {
        return new Rasterizer(new DepthBuffer(1, 1));
    }
}
