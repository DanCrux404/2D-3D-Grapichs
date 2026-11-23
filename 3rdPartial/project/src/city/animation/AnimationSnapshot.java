package city.animation;

import city.scene.SunMoonOrbit;

import engine.core.Point3D;

import java.util.List;
import java.util.Objects;

public record AnimationSnapshot(
        long elapsedMillis,
        OrbitSnapshot orbit,
        CloudSnapshot clouds,
        StarSnapshot stars) {

    public record OrbitSnapshot(
            SunMoonOrbit.BodyPose sun,
            SunMoonOrbit.BodyPose moon) {

        public OrbitSnapshot {
            Objects.requireNonNull(sun, "sun cannot be null");
            Objects.requireNonNull(moon, "moon cannot be null");
        }
    }

    public record CloudSnapshot(List<Point3D> positions) {

        public CloudSnapshot {
            positions = List.copyOf(positions);
        }
    }

    public record StarSnapshot(List<Double> brightness) {

        public StarSnapshot {
            brightness = List.copyOf(brightness);
        }
    }

    public AnimationSnapshot {
        Objects.requireNonNull(orbit, "orbit cannot be null");
        Objects.requireNonNull(clouds, "clouds cannot be null");
        Objects.requireNonNull(stars, "stars cannot be null");
    }
}
