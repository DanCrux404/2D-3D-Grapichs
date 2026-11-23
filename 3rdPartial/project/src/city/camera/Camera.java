package city.camera;

import engine.core.Point3D;
import engine.transform.Transform3D;

import java.awt.event.KeyEvent;

/**
 * First-person camera with an app-side view transform built from {@link Transform3D}.
 *
 * <p>World space is right-handed with Y up. The engine's perspective projection expects a camera
 * at a fixed center looking toward +z with screen Y pointing down, so this class transforms world
 * points into that camera frame: translate by the negative position, undo yaw around Y, undo pitch
 * around X, then flip Y (and apply the uniform view scale).
 */
public class Camera {

    private static final double MOVE_SPEED = 300.0;
    private static final double LOOK_SPEED = 100.0;
    private static final double MAX_PITCH = 15.0;
    private static final double MAX_UP_PITCH = 5.0;

    private double x;
    private double y;
    private double z;
    private double yawDegrees;
    private double pitchDegrees;
    private final double viewScale;
    private double worldLimit = Double.POSITIVE_INFINITY;

    private double[][] translation;
    private double[][] yawRotation;
    private double[][] pitchRotation;
    private double[][] flipAndScale;

    /**
     * Creates a camera.
     *
     * @param x world X position
     * @param y world Y position (up)
     * @param z world Z position
     * @param yawDegrees rotation around the world Y axis; 0 looks toward +z
     * @param pitchDegrees elevation angle; positive looks up
     * @param viewScale uniform scale applied in camera space (1 for first-person, smaller for
     *     overview cameras)
     */
    public Camera(double x, double y, double z, double yawDegrees, double pitchDegrees,
            double viewScale) {
        if (viewScale <= 0 || !Double.isFinite(viewScale)) {
            throw new IllegalArgumentException("View scale must be positive and finite.");
        }

        this.x = x;
        this.y = y;
        this.z = z;
        this.yawDegrees = yawDegrees;
        this.pitchDegrees = clampPitch(pitchDegrees);
        this.viewScale = viewScale;

        rebuildMatrices();
    }

    /**
     * Updates the camera from the input snapshot.
     *
     * @param input current input state
     * @param deltaMillis elapsed milliseconds since the previous update
     */
    public void update(InputState input, long deltaMillis) {
        double seconds = deltaMillis / 1000.0;
        double yawRadians = Math.toRadians(yawDegrees);

        double forwardX = Math.sin(yawRadians);
        double forwardZ = Math.cos(yawRadians);
        double rightX = Math.cos(yawRadians);
        double rightZ = -Math.sin(yawRadians);

        double moveX = 0.0;
        double moveZ = 0.0;

        if (input.isDown(KeyEvent.VK_W)) {
            moveX += forwardX;
            moveZ += forwardZ;
        }

        if (input.isDown(KeyEvent.VK_S)) {
            moveX -= forwardX;
            moveZ -= forwardZ;
        }

        if (input.isDown(KeyEvent.VK_D)) {
            moveX += rightX;
            moveZ += rightZ;
        }

        if (input.isDown(KeyEvent.VK_A)) {
            moveX -= rightX;
            moveZ -= rightZ;
        }

        double length = Math.hypot(moveX, moveZ);

        if (length > 0.0) {
            x += moveX / length * MOVE_SPEED * seconds;
            z += moveZ / length * MOVE_SPEED * seconds;

            x = clampToWorld(x);
            z = clampToWorld(z);
        }

        if (input.isDown(KeyEvent.VK_LEFT)) {
            yawDegrees -= LOOK_SPEED * seconds;
        }

        if (input.isDown(KeyEvent.VK_RIGHT)) {
            yawDegrees += LOOK_SPEED * seconds;
        }

        if (input.isDown(KeyEvent.VK_UP)) {
            pitchDegrees = clampPitch(pitchDegrees + LOOK_SPEED * seconds);
        }

        if (input.isDown(KeyEvent.VK_DOWN)) {
            pitchDegrees = clampPitch(pitchDegrees - LOOK_SPEED * seconds);
        }

        rebuildMatrices();
    }

    /**
     * Transforms a world-space point into the camera frame expected by the engine projections
     * (before the screen-center offset).
     *
     * @param world world-space point
     * @return camera-space point: X right, Y down, Z forward
     */
    public Point3D toCameraSpace(Point3D world) {
        Point3D point = Transform3D.apply(world, translation);
        point = Transform3D.apply(point, yawRotation);
        point = Transform3D.apply(point, pitchRotation);
        return Transform3D.apply(point, flipAndScale);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getYawDegrees() {
        return yawDegrees;
    }

    public double getPitchDegrees() {
        return pitchDegrees;
    }

    /**
     * Limits horizontal movement so the camera stays over the ground mesh.
     *
     * @param limit maximum absolute X and Z coordinate
     */
    public void setWorldLimit(double limit) {
        if (!Double.isFinite(limit) || limit <= 0.0) {
            throw new IllegalArgumentException("World limit must be positive and finite.");
        }

        worldLimit = limit;
        x = clampToWorld(x);
        z = clampToWorld(z);
        rebuildMatrices();
    }

    private double clampToWorld(double value) {
        return Math.max(-worldLimit, Math.min(worldLimit, value));
    }

    private void rebuildMatrices() {
        translation = Transform3D.translation(-x, -y, -z);
        yawRotation = Transform3D.rotationY(-yawDegrees);
        pitchRotation = Transform3D.rotationX(pitchDegrees);
        flipAndScale = Transform3D.scaling(viewScale, -viewScale, viewScale);
    }

    private static double clampPitch(double pitch) {
        return Math.max(-MAX_PITCH, Math.min(MAX_UP_PITCH, pitch));
    }
}
