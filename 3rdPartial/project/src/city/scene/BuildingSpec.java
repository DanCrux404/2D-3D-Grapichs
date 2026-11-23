package city.scene;

import java.awt.Color;

public final class BuildingSpec {

    private final double x;
    private final double z;
    private final double scaleX;
    private final double scaleY;
    private final double scaleZ;
    private final double rotationY;
    private final Color wall;
    private final int windowColumns;
    private final int windowRows;

    public BuildingSpec(
            double x,
            double z,
            double scaleX,
            double scaleY,
            double scaleZ,
            double rotationY,
            Color wall,
            int windowColumns,
            int windowRows) {

        this.x = x;
        this.z = z;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
        this.rotationY = rotationY;
        this.wall = wall;
        this.windowColumns = windowColumns;
        this.windowRows = windowRows;
    }

    public double x() {
        return x;
    }

    public double z() {
        return z;
    }

    public double scaleX() {
        return scaleX;
    }

    public double scaleY() {
        return scaleY;
    }

    public double scaleZ() {
        return scaleZ;
    }

    public double rotationY() {
        return rotationY;
    }

    public Color wall() {
        return wall;
    }

    public int windowColumns() {
        return windowColumns;
    }

    public int windowRows() {
        return windowRows;
    }

    public double footprintHalfX() {
        return Math.abs(Math.cos(Math.toRadians(rotationY))) * scaleX
                + Math.abs(Math.sin(Math.toRadians(rotationY))) * scaleZ;
    }

    public double footprintHalfZ() {
        return Math.abs(Math.sin(Math.toRadians(rotationY))) * scaleX
                + Math.abs(Math.cos(Math.toRadians(rotationY))) * scaleZ;
    }
}
