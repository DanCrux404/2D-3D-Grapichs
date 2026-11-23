package city.render;

import city.scene.WorldTriangle;

import engine.core.Point3D;
import engine.lighting.Light;
import engine.shadow.ShadowMap;
import engine.shadow.ShadowRenderer;
import engine.transform.Projection3D;

import java.awt.Color;
import java.util.List;

public final class ShadowPass {

    private final ShadowMap shadowMap;
    private LightViewProjection lightProjection;
    private ShadowRenderer shadowRenderer;

    public ShadowPass(int mapSize, Point3D target) {
        shadowMap = new ShadowMap(mapSize, mapSize);
        setLight(new Light(new Point3D(0.0, 3000.0, 650.0), new Color(255, 255, 255), 1.0), target);
    }

    public void setLight(Light light, Point3D target) {
        if (light == null || target == null) {
            throw new IllegalArgumentException("Light and target cannot be null.");
        }

        lightProjection = new LightViewProjection(light.getPosition(), target, shadowMap.getWidth());
        shadowRenderer = new ShadowRenderer(shadowMap, lightProjection);
    }

    public void render(List<WorldTriangle> casters) {
        shadowRenderer.clear();

        for (WorldTriangle triangle : casters) {
            shadowRenderer.renderTriangle(
                    triangle.getA(), triangle.getB(), triangle.getC());
        }
    }

    public ShadowMap getShadowMap() {
        return shadowMap;
    }

    public Projection3D getLightProjection() {
        return lightProjection;
    }
}
