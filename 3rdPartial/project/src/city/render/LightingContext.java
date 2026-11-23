package city.render;

import engine.core.Point3D;
import engine.lighting.Light;
import engine.lighting.PhongLighting;

public record LightingContext(
        Light light,
        PhongLighting phong,
        ShadowPass shadows,
        Point3D viewPoint,
        double shadowBias) {
}
