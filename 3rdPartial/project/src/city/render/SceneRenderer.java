package city.render;

import city.camera.Camera;
import city.scene.SurfaceKind;
import city.scene.WorldTriangle;

import engine.core.Point3D;
import engine.core.ProjectedPoint;
import engine.core.DepthBuffer;
import engine.raster.Rasterizer;
import engine.shadow.ShadowMap;
import engine.transform.ParallelProjection;
import engine.transform.PerspectiveProjection;
import engine.transform.Projection3D;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders world-space triangles through the engine pipeline.
 *
 * <p>Per frame: clear the depth buffer, transform vertices into the camera frame, clip against the
 * near plane, project with the active engine {@link Projection3D}, and rasterize with the engine
 * {@link Rasterizer}. The camera frame is offset by the screen center because the engine
 * projections operate around their center of projection at (width/2, height/2).
 *
 * <p>The near plane sits just in front of the perspective center of projection (z = −focal), not
 * at the view origin, so ground directly beneath the camera is kept and the bottom of the frame
 * never shows sky.
 *
 * <p>Only the render thread may call {@link #render}. The projection mode is likewise owned by the
 * render thread.
 */
public class SceneRenderer {

    private static final double FOCAL_DISTANCE = 500.0;

    private static final double NEAR_PLANE = -FOCAL_DISTANCE + 1.0;

    private final double centerX;
    private final double centerY;
    private final DepthBuffer depthBuffer;
    private final Rasterizer rasterizer;
    private final PerspectiveProjection perspectiveProjection;
    private final ParallelProjection parallelProjection;

    private ProjectionMode mode = ProjectionMode.PERSPECTIVE;

    public SceneRenderer(int width, int height) {
        centerX = width / 2.0;
        centerY = height / 2.0;

        depthBuffer = new DepthBuffer(width, height);
        rasterizer = new Rasterizer(depthBuffer);
        perspectiveProjection = new PerspectiveProjection(centerX, centerY, -FOCAL_DISTANCE);
        parallelProjection = new ParallelProjection(0.5, 0.5, 1.0);
    }

    /** Switches between perspective and parallel projection. Render thread only. */
    public void toggleMode() {
        mode = mode == ProjectionMode.PERSPECTIVE
                ? ProjectionMode.PARALLEL
                : ProjectionMode.PERSPECTIVE;
    }

    /** Returns the active projection mode. */
    public ProjectionMode getMode() {
        return mode;
    }

    public void render(
            List<WorldTriangle> triangles,
            Camera camera,
            BufferedImage image,
            Point3D parallelFocus) {

        render(triangles, camera, image, parallelFocus, null);
    }

    public void render(
            List<WorldTriangle> triangles,
            Camera camera,
            BufferedImage image,
            Point3D parallelFocus,
            LightingContext lighting) {

        render(triangles, List.of(), camera, image, parallelFocus, lighting);
    }

    public void render(
            List<WorldTriangle> staticTriangles,
            List<WorldTriangle> dynamicTriangles,
            Camera camera,
            BufferedImage image,
            Point3D parallelFocus,
            LightingContext lighting) {

        depthBuffer.clear();

        Projection3D projection;
        double offsetX = 0.0;
        double offsetY = 0.0;

        if (mode == ProjectionMode.PERSPECTIVE) {
            projection = perspectiveProjection;
        } else {
            projection = parallelProjection;

            Point3D focus = camera.toCameraSpace(parallelFocus);
            offsetX = parallelProjection.getXp() * focus.getZ() / parallelProjection.getZp()
                    - focus.getX();
            offsetY = parallelProjection.getYp() * focus.getZ() / parallelProjection.getZp()
                    - focus.getY();
        }

        ShadowMap shadowMap = null;
        Projection3D lightProjection = null;

        if (lighting != null && lighting.shadows() != null) {
            shadowMap = lighting.shadows().getShadowMap();
            lightProjection = lighting.shadows().getLightProjection();
        }

        List<WorldTriangle> frameTriangles =
                new ArrayList<>(staticTriangles.size() + dynamicTriangles.size());
        frameTriangles.addAll(staticTriangles);
        frameTriangles.addAll(dynamicTriangles);

        for (WorldTriangle triangle : frameTriangles) {
            Point3D normal = triangle.getNormal();
            SurfaceVertex a =
                    new SurfaceVertex(
                            camera.toCameraSpace(triangle.getA()), triangle.getA(), normal);
            SurfaceVertex b =
                    new SurfaceVertex(
                            camera.toCameraSpace(triangle.getB()), triangle.getB(), normal);
            SurfaceVertex c =
                    new SurfaceVertex(
                            camera.toCameraSpace(triangle.getC()), triangle.getC(), normal);

            for (SurfaceVertex[] clipped : AttributeClipper.clipTriangle(a, b, c, NEAR_PLANE)) {
                ProjectedVertex pa =
                        new ProjectedVertex(
                                project(projection, clipped[0].camera(), offsetX, offsetY),
                                clipped[0]);
                ProjectedVertex pb =
                        new ProjectedVertex(
                                project(projection, clipped[1].camera(), offsetX, offsetY),
                                clipped[1]);
                ProjectedVertex pc =
                        new ProjectedVertex(
                                project(projection, clipped[2].camera(), offsetX, offsetY),
                                clipped[2]);

                for (ProjectedVertex[] screen :
                        AttributeClipper.clipToScreen(
                                pa, pb, pc, image.getWidth(), image.getHeight())) {

                    ProjectedPoint p1 = screen[0].screen();
                    ProjectedPoint p2 = screen[1].screen();
                    ProjectedPoint p3 = screen[2].screen();
                    SurfaceVertex v1 = screen[0].vertex();
                    SurfaceVertex v2 = screen[1].vertex();
                    SurfaceVertex v3 = screen[2].vertex();

                    if (lighting == null
                            || triangle.getMaterial() == null
                            || triangle.getKind() == SurfaceKind.UNLIT
                            || triangle.getKind() == SurfaceKind.WINDOW) {

                        rasterizer.fillTriangle(image, p1, p2, p3, triangle.getColor());
                        continue;
                    }

                    Point3D centroid = centroid(v1.world(), v2.world(), v3.world());
                    Point3D viewDirection = direction(centroid, lighting.viewPoint());

                    if (triangle.getKind() == SurfaceKind.STREET) {

                        rasterizer.fillTrianglePhong(
                                image,
                                p1,
                                p2,
                                p3,
                                v1.world(),
                                v2.world(),
                                v3.world(),
                                v1.normal(),
                                v2.normal(),
                                v3.normal(),
                                lighting.light(),
                                triangle.getMaterial(),
                                lighting.phong(),
                                viewDirection,
                                mode == ProjectionMode.PERSPECTIVE,
                                shadowMap,
                                lightProjection,
                                shadowMap == null ? 0.0 : lighting.shadowBias());
                        continue;
                    }

                    double visibility = 1.0;

                    if (shadowMap != null
                            && (triangle.getKind() == SurfaceKind.GROUND
                                    || triangle.getKind() == SurfaceKind.STREET)) {
                        ProjectedPoint lightPoint = lightProjection.projectWithDepth(centroid);
                        int shadowX = (int) Math.round(lightPoint.getX());
                        int shadowY = (int) Math.round(lightPoint.getY());

                        if (shadowMap.isInShadow(
                                shadowX, shadowY, lightPoint.getDepth(), lighting.shadowBias())) {
                            visibility = 0.0;
                        }
                    }

                    Color shaded =
                            lighting.phong().calculateColor(
                                    centroid,
                                    v1.normal(),
                                    viewDirection,
                                    lighting.light(),
                                    triangle.getMaterial(),
                                    visibility);

                    rasterizer.fillTriangle(image, p1, p2, p3, shaded);
                }
            }
        }
    }

    private ProjectedPoint project(
            Projection3D projection, Point3D cameraSpace, double offsetX, double offsetY) {
        Point3D screenFrame =
                new Point3D(
                        cameraSpace.getX() + centerX + offsetX,
                        cameraSpace.getY() + centerY + offsetY,
                        cameraSpace.getZ());

        return projection.projectWithDepth(screenFrame);
    }

    private static Point3D centroid(Point3D a, Point3D b, Point3D c) {
        return new Point3D(
                (a.getX() + b.getX() + c.getX()) / 3.0,
                (a.getY() + b.getY() + c.getY()) / 3.0,
                (a.getZ() + b.getZ() + c.getZ()) / 3.0);
    }

    private static Point3D direction(Point3D from, Point3D to) {
        if (to == null) {
            return new Point3D(0.0, 1.0, 0.0);
        }

        Point3D delta =
                new Point3D(
                        to.getX() - from.getX(),
                        to.getY() - from.getY(),
                        to.getZ() - from.getZ());
        double length = Math.sqrt(
                delta.getX() * delta.getX()
                        + delta.getY() * delta.getY()
                        + delta.getZ() * delta.getZ());

        if (length < 1e-12) {
            return new Point3D(0.0, 1.0, 0.0);
        }

        return new Point3D(
                delta.getX() / length, delta.getY() / length, delta.getZ() / length);
    }
}
