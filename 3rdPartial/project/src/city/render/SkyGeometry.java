package city.render;

import city.animation.AnimationSnapshot;
import city.animation.CycleSnapshot;
import city.scene.CelestialDisc;
import city.scene.CloudField;
import city.scene.StarField;
import city.scene.SunMoonOrbit;
import city.scene.SurfaceKind;
import city.scene.WorldTriangle;

import engine.core.Point3D;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public final class SkyGeometry {

    private static final List<List<Point3D>> CLOUD_GRID = CloudField.localGrid();

    private SkyGeometry() {}

    public static List<WorldTriangle> triangles(
            AnimationSnapshot animation, CycleSnapshot cycle) {

        return triangles(animation, cycle, 0.0, 0.0);
    }

    public static List<WorldTriangle> triangles(
            AnimationSnapshot animation,
            CycleSnapshot cycle,
            double cameraX,
            double cameraZ) {

        return triangles(animation, cycle, cameraX, cameraZ, ProjectionMode.PERSPECTIVE);
    }

    public static List<WorldTriangle> triangles(
            AnimationSnapshot animation,
            CycleSnapshot cycle,
            double cameraX,
            double cameraZ,
            ProjectionMode mode) {

        List<WorldTriangle> geometry = new ArrayList<>();

        addClouds(geometry, animation.clouds().positions());

        if (mode == ProjectionMode.PARALLEL) {
            return geometry;
        }

        addCelestialBody(
                geometry,
                SunMoonOrbit.skyLocked(animation.orbit().sun(), cameraX, cameraZ),
                CelestialDisc.sunRing(),
                CelestialDisc.SUN_RADIUS);
        addCelestialBody(
                geometry,
                SunMoonOrbit.skyLocked(animation.orbit().moon(), cameraX, cameraZ),
                CelestialDisc.moonRing(),
                CelestialDisc.MOON_RADIUS);
        addStars(geometry, animation, cycle.starVisibility());

        return geometry;
    }

    private static void addClouds(List<WorldTriangle> geometry, List<Point3D> origins) {
        Color color = CloudField.color();

        for (Point3D origin : origins) {
            for (int row = 0; row < CLOUD_GRID.size() - 1; row++) {
                for (int column = 0; column < CLOUD_GRID.get(row).size() - 1; column++) {
                    Point3D p1 = translate(CLOUD_GRID.get(row).get(column), origin);
                    Point3D p2 = translate(CLOUD_GRID.get(row).get(column + 1), origin);
                    Point3D p3 = translate(CLOUD_GRID.get(row + 1).get(column + 1), origin);
                    Point3D p4 = translate(CLOUD_GRID.get(row + 1).get(column), origin);

                    geometry.add(new WorldTriangle(p1, p2, p3, color, SurfaceKind.CLOUD, null));
                    geometry.add(new WorldTriangle(p1, p3, p4, color, SurfaceKind.CLOUD, null));
                }
            }
        }
    }

    private static void addStars(
            List<WorldTriangle> geometry,
            AnimationSnapshot animation,
            double visibility) {

        if (visibility <= 0.0) {
            return;
        }

        List<StarField.Star> stars = StarField.stars();
        List<Double> brightness = animation.stars().brightness();

        for (int i = 0; i < stars.size(); i++) {
            StarField.Star star = stars.get(i);
            double level = brightness.get(i) * visibility;
            int red = (int) Math.round(255 * level);
            int green = (int) Math.round(250 * level);
            int blue = (int) Math.round(225 * level);
            Color color = new Color(red, green, blue);

            addQuad(geometry, star.position(), star.size(), star.size(), color, SurfaceKind.STAR);
        }
    }

    private static void addCelestialBody(
            List<WorldTriangle> geometry,
            SunMoonOrbit.BodyPose pose,
            List<Point3D> ring,
            double radius) {

        if (!pose.visible()) {
            return;
        }

        for (int i = 0; i < ring.size(); i++) {
            Point3D first = translate(ring.get(i), pose.position());
            Point3D second = translate(ring.get((i + 1) % ring.size()), pose.position());

            geometry.add(
                    new WorldTriangle(
                            pose.position(),
                            first,
                            second,
                            pose.color(),
                            SurfaceKind.CELESTIAL,
                            null));
        }
    }

    private static void addQuad(
            List<WorldTriangle> geometry,
            Point3D center,
            double halfX,
            double halfY,
            Color color,
            SurfaceKind kind) {

        Point3D p1 =
                new Point3D(
                        center.getX() - halfX, center.getY() - halfY, center.getZ());
        Point3D p2 =
                new Point3D(
                        center.getX() + halfX, center.getY() - halfY, center.getZ());
        Point3D p3 =
                new Point3D(
                        center.getX() + halfX, center.getY() + halfY, center.getZ());
        Point3D p4 =
                new Point3D(
                        center.getX() - halfX, center.getY() + halfY, center.getZ());

        geometry.add(new WorldTriangle(p1, p2, p3, color, kind, null));
        geometry.add(new WorldTriangle(p1, p3, p4, color, kind, null));
    }

    private static Point3D translate(Point3D point, Point3D offset) {
        return new Point3D(
                point.getX() + offset.getX(),
                point.getY() + offset.getY(),
                point.getZ() + offset.getZ());
    }
}
