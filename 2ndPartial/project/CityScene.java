package project;

import engine.fill.ScanLine;
import engine.shapes.Line;

import project.animation.DayNightCycle;
import project.elements.Building;
import project.elements.Cloud;
import project.elements.Moon;
import project.elements.Star;
import project.elements.Sun;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Random;

/** Coordinates the rendering of the city scene. */
public class CityScene {

    private final int width;
    private final int height;
    private final Building[] buildings;
    private final Random random = new Random();
    private final Sun sun;
    private final Moon moon;
    private final Star[] stars;
    private final Cloud[] clouds;
    private final DayNightCycle dayNightCycle;
    private final double[] cloudX;

    public CityScene(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Scene dimensions must be positive.");
        }

        this.width = width;
        this.height = height;

        sun = new Sun(width - 100, 85, 35, new Color(255, 200, 40));

        moon = new Moon(150, 90, 30, new Color(245, 240, 200), new Color(135, 206, 235));

        stars =
                new Star[] {
                    new Star(100, 45, 7, Color.WHITE),
                    new Star(220, 110, 5, new Color(255, 240, 180)),
                    new Star(320, 55, 8, Color.WHITE),
                    new Star(430, 125, 6, new Color(255, 240, 180)),
                    new Star(540, 40, 5, Color.WHITE),
                    new Star(650, 105, 8, new Color(255, 240, 180)),
                    new Star(730, 55, 6, Color.WHITE)
                };

        clouds =
                new Cloud[] {
                    new Cloud(100, 125, 55, new Color(245, 245, 240)),
                    new Cloud(280, 145, 65, new Color(245, 245, 240)),
                    new Cloud(460, 95, 45, new Color(235, 240, 250)),
                    new Cloud(570, 185, 85, new Color(225, 230, 240)),
                    new Cloud(730, 130, 60, new Color(245, 240, 235)),
                    new Cloud(390, 200, 50, new Color(235, 235, 245))
                };

        cloudX = new double[clouds.length];

        for (int i = 0; i < clouds.length; i++) {
            cloudX[i] = clouds[i].getX();
        }

        dayNightCycle = new DayNightCycle();

        buildings = generateBuildings();
    }

    /**
     * Draws the static city into the provided image buffer.
     *
     * @param buffer the image buffer used for rendering
     */
    public void draw(BufferedImage buffer) {
        if (buffer == null || buffer.getWidth() != width || buffer.getHeight() != height) {
            throw new IllegalArgumentException("Buffer dimensions must match the scene.");
        }

        drawSky(buffer);
        drawStars(buffer);
        drawMoon(buffer);
        drawSunIfVisible(buffer);
        drawClouds(buffer);
        drawGround(buffer);
        drawRoad(buffer);
        drawBuildings(buffer);
    }

    private void drawSky(BufferedImage buffer) {
        Color skyColor = dayNightCycle.getSkyColor();

        fillRectangle(buffer, 0, 0, width - 1, height * 3 / 4, skyColor);
    }

    public void update(long deltaMillis) {
        dayNightCycle.update(deltaMillis);

        updateSunPosition();
        updateCloudPositions(deltaMillis);
        updateCloudColors();
        updateBuildingLights();
    }

    private void drawGround(BufferedImage buffer) {
        int groundY = height * 3 / 4;

        fillRectangle(buffer, 0, groundY, width - 1, height - 1, new Color(75, 125, 75));
    }

    private void drawRoad(BufferedImage buffer) {
        int roadTop = height * 5 / 6;

        fillRectangle(buffer, 0, roadTop, width - 1, height - 1, new Color(55, 55, 60));

        // Dashed road markings.
        int centerY = roadTop + (height - roadTop) / 2;

        for (int x = 15; x < width; x += 70) {
            Line.bresenham(buffer, x, centerY, x + 35, centerY, new Color(245, 225, 150));
        }
    }

    private void drawSun(BufferedImage buffer) {
        sun.draw(buffer);
    }

    /** Fills an axis-aligned rectangle using the engine scanline algorithm. */
    private void fillRectangle(BufferedImage buffer, int x1, int y1, int x2, int y2, Color color) {

        int[][] vertices = {
            {x1, y1},
            {x2, y1},
            {x2, y2},
            {x1, y2}
        };

        ScanLine.fill(buffer, vertices, color);
    }

    /**
     * Generates a city skyline with randomized building dimensions.
     *
     * @return the generated buildings
     */
    private Building[] generateBuildings() {
        int groundY = height * 3 / 4;

        int minWidth = 60;
        int maxWidth = 110;

        int minHeight = 100;
        int maxHeight = Math.min(290, groundY - 20);

        int minGap = 5;
        int maxGap = 15;

        Color[] facadeColors = {
            new Color(180, 100, 85),
            new Color(100, 120, 155),
            new Color(155, 135, 115),
            new Color(105, 135, 125),
            new Color(115, 120, 150),
            new Color(160, 125, 100)
        };

        java.util.List<Building> generated = new java.util.ArrayList<>();

        int x = 10;

        while (x + minWidth <= width) {
            int buildingWidth = random.nextInt(maxWidth - minWidth + 1) + minWidth;

            // Keep the last building inside the scene.
            buildingWidth = Math.min(buildingWidth, width - x);

            if (buildingWidth < minWidth) {
                break;
            }

            int buildingHeight = random.nextInt(maxHeight - minHeight + 1) + minHeight;

            Color facadeColor = facadeColors[random.nextInt(facadeColors.length)];

            generated.add(new Building(x, groundY, buildingWidth, buildingHeight, facadeColor));

            int gap = random.nextInt(maxGap - minGap + 1) + minGap;
            x += buildingWidth + gap;
        }

        return generated.toArray(new Building[0]);
    }

    private void drawBuildings(BufferedImage buffer) {
        for (Building building : buildings) {
            building.draw(buffer);
        }
    }

    private void drawStars(BufferedImage buffer) {
        for (int i = 0; i < stars.length; i++) {
            double brightness = dayNightCycle.getStarBrightness(i);

            stars[i].draw(buffer, brightness);
        }
    }

    private void drawClouds(BufferedImage buffer) {
        for (Cloud cloud : clouds) {
            cloud.draw(buffer);
        }
    }

    private void updateSunPosition() {
        long time = dayNightCycle.getElapsedMillis();

        int groundY = height * 3 / 4;
        int radius = sun.getRadius();

        // Morning and sunset: travel across the visible sky.
        if (time < 160_000L) {
            double progress = time / 160_000.0;

            int x = (int) Math.round(60 + (width - 120) * progress);

            int y = (int) Math.round(groundY - radius + 5 - 280 * Math.sin(Math.PI * progress));

            sun.setPosition(x, y);
            return;
        }

        // Night: travel back underneath the horizon.
        if (time < 220_000L) {
            double progress = (time - 160_000L) / 60_000.0;

            int x = (int) Math.round(width - 60 - (width * progress));

            int y = groundY + radius + 30;

            sun.setPosition(x, y);
            return;
        }

        // Sunrise: reappear from the left and rise.
        double progress = (time - 220_000L) / 20_000.0;

        int x = (int) Math.round(-60 + 120 * progress);

        int y = (int) Math.round(groundY + radius + 30 - (radius + 35) * progress);

        sun.setPosition(x, y);
    }

    private void updateCloudPositions(long deltaMillis) {
        double[] speeds = {10.0, 12.0, 7.0, 8.0, 9.0, 6.0};

        for (int i = 0; i < clouds.length; i++) {
            cloudX[i] += speeds[i] * deltaMillis / 1000.0;

            if (cloudX[i] > width + 180) {
                cloudX[i] = -100;
            }

            clouds[i].setPosition((int) Math.round(cloudX[i]), clouds[i].getY());
        }
    }

    private boolean isSunVisible() {
        long time = dayNightCycle.getElapsedMillis();

        return time < 160_000L || time >= 220_000L;
    }

    private void drawSunIfVisible(BufferedImage buffer) {
        if (isSunVisible()) {
            drawSun(buffer);
        }
    }

    private void drawMoon(BufferedImage buffer) {
        long time = dayNightCycle.getElapsedMillis();

        // Show the moon during twilight and night.
        if (time >= 150_000L && time < 225_000L) {
            moon.draw(buffer, dayNightCycle.getSkyColor());
        }
    }

    private void updateCloudColors() {
        long time = dayNightCycle.getElapsedMillis();

        Color cloudColor;

        if (time < 80_000L) {
            // Soft morning white with a hint of blue.
            cloudColor = new Color(235, 243, 248);
        } else if (time < 120_000L) {
            // Warm peach during golden hour.
            double t = (time - 80_000L) / 40_000.0;

            cloudColor = blendColor(new Color(235, 243, 248), new Color(244, 190, 168), t);
        } else if (time < 160_000L) {
            // Dusty rose transitioning into twilight.
            double t = (time - 120_000L) / 40_000.0;

            cloudColor = blendColor(new Color(244, 190, 168), new Color(135, 130, 165), t);
        } else if (time < 220_000L) {
            // Cool blue-gray at night.
            cloudColor = new Color(105, 115, 145);
        } else {
            // Gradually return to morning tones.
            double t = (time - 220_000L) / 20_000.0;

            cloudColor = blendColor(new Color(105, 115, 145), new Color(235, 243, 248), t);
        }

        for (Cloud cloud : clouds) {
            cloud.setColor(cloudColor);
        }
    }

    private Color blendColor(Color a, Color b, double t) {
        t = Math.max(0.0, Math.min(1.0, t));

        int r = (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int blue = (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t);

        return new Color(r, g, blue);
    }

    private void updateBuildingLights() {
        long time = dayNightCycle.getElapsedMillis();
        double intensity;

        // Día y mañana: todas las ventanas apagadas.
        if (time < 100_000L) {
            intensity = 0.0;
        }
        // Atardecer: las luces empiezan a encenderse.
        else if (time < 160_000L) {
            double t = (time - 100_000L) / 60_000.0;
            intensity = smoothstep(t);
        }
        // Noche: intensidad máxima.
        else if (time < 220_000L) {
            intensity = 1.0;
        }
        // Amanecer: las luces se van apagando.
        else {
            double t = (time - 220_000L) / 20_000.0;
            intensity = 1.0 - smoothstep(t);
        }

        for (Building building : buildings) {
            building.setNightIntensity(intensity);
        }
    }

    private double smoothstep(double t) {
        t = Math.max(0.0, Math.min(1.0, t));
        return t * t * (3.0 - 2.0 * t);
    }
}
