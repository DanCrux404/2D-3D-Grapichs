
package engine.shapes;

import engine.core.Pixel;
import java.awt.Color;
import java.awt.image.BufferedImage;
import engine.core.PixelPlotter;

public class Circle {

    /**
     * Draws a circle using the mathematical equation of a circle.
     *
     * <p>For each x-coordinate within the circle's horizontal
     * range, the corresponding y-coordinates are calculated
     * using the square root formula. The resulting pixels are
     * rounded to the nearest integer.</p>
     *
     * @param buffer the image buffer where the circle will be drawn
     * @param xc     the x-coordinate of the circle's center
     * @param yc     the y-coordinate of the circle's center
     * @param radius the radius of the circle
     * @param color  the color used to draw the circle
     */
    public static void draw(
            BufferedImage buffer,
            int xc,
            int yc,
            int radius,
            Color color) {

        if (radius < 0) {
            throw new IllegalArgumentException(
                    "Radius cannot be negative.");
        }

        for (int x = xc - radius; x <= xc + radius; x++) {
            //Dx is the diference between current pixel and center on x
            double dx = x - xc;
            //Radicand is the value inside square root
            double radicand = (double) radius * radius - dx * dx;
            //Vertical distance from center to circle border
            int yOffset = (int) Math.round(Math.sqrt(radicand));

            // Upper half
            Pixel.put(buffer, x, yc - yOffset, color);

            // Lower half
            if (yOffset != 0) {
                Pixel.put(buffer, x, yc + yOffset, color);
            }
        }
    }

    /**
    * Draws a circle using the polar coordinate equations.
    *
    * <p>The method iterates through angles from 0 to 2*PI,
    * calculates the corresponding x and y coordinates,
    * and rounds them to the nearest pixel.</p>
    *
    * @param buffer the image buffer where the circle will be drawn
    * @param xc     the x-coordinate of the circle's center
    * @param yc     the y-coordinate of the circle's center
    * @param radius the radius of the circle
    * @param color  the color used to draw the circle
    */
    public static void polar(
            BufferedImage buffer,
            int xc,
            int yc,
            int radius,
            Color color) {

        if (radius < 0) {
            throw new IllegalArgumentException(
                "Radius cannot be negative.");
        }

        //angular increment increments radius so the calculated dots.
        double step = 1.0 / radius;

        if (radius == 0) {
            Pixel.put(buffer, xc, yc, color);
            return;
        }

        for (double t = 0; t <= 2 * Math.PI; t += step) {
            int x = (int) Math.round(xc + radius * Math.sin(t));
            int y = (int) Math.round(yc + radius * Math.cos(t));

            Pixel.put(buffer, x, y, color);
        }
    }

    
    /**
    * Draws a circle using eight-way symmetry and the
    * mathematical equation of a circle.
    *
    * <p>Only one octant is calculated. The remaining
    * seven octants are drawn by reflecting each point
    * across the axes and diagonals.</p>
    *
    * @param buffer the image buffer where the circle will be drawn
    * @param xc     the x-coordinate of the circle's center
    * @param yc     the y-coordinate of the circle's center
    * @param radius the radius of the circle
    * @param color  the color used to draw the circle
    */
    public static void symmetry8(
            BufferedImage buffer,
            int xc,
            int yc,
            int radius,
            Color color) {

        if (radius < 0) {
            throw new IllegalArgumentException(
                    "Radius cannot be negative.");
        }

        // Start at the top of the circle, relative to its center.
        int x = 0;
        int y = radius;

        // Calculate only one octant, from x = 0 until x > y.
        while (x <= y) {

            // Draw this point and its seven symmetric counterparts.
            plotSymmetricPoints(buffer, xc, yc, x, y, color);

            // Move to the next horizontal coordinate.
            x++;

            // Calculate the radicand: radius² - x².
            // This is the value inside the square root when
            // solving the circle equation for y.
            double radicand = (double) radius * radius
                    - (double) x * x;

            // Calculate the corresponding vertical coordinate.
            // Math.sqrt obtains the square root, and Math.round
            // converts the result to the nearest whole pixel.
            y = (int) Math.round(Math.sqrt(radicand));
        }
    }

    /**
    * Plots the eight symmetric points of a circle.
    */
    private static void plotSymmetricPoints(
            BufferedImage buffer,
            int xc,
            int yc,
            int x,
            int y,
            Color color) {

        Pixel.put(buffer, xc + x, yc + y, color);
        Pixel.put(buffer, xc + y, yc + x, color);
        Pixel.put(buffer, xc + y, yc - x, color);
        Pixel.put(buffer, xc + x, yc - y, color);

        Pixel.put(buffer, xc - x, yc - y, color);
        Pixel.put(buffer, xc - y, yc - x, color);
        Pixel.put(buffer, xc - y, yc + x, color);
        Pixel.put(buffer, xc - x, yc + y, color);
    }

    /**
    * Draws a circle using the Midpoint Circle Algorithm.
    *
    * <p>The algorithm calculates points in one octant and uses
    * eight-way symmetry to draw the entire circle. It uses an
    * integer decision parameter to choose the next pixel,
    * avoiding square roots and trigonometric functions.</p>
    *
    * @param buffer the image buffer where the circle will be drawn
    * @param xc     the x-coordinate of the circle's center
    * @param yc     the y-coordinate of the circle's center
    * @param radius the radius of the circle
    * @param color  the color used to draw the circle
    */
/*
    public static void midpoint(
            BufferedImage buffer,
            int xc,
            int yc,
            int radius,
            Color color) {

        if (radius < 0) {
            throw new IllegalArgumentException(
                    "Radius cannot be negative.");
        }

        // Start at the top of the circle, relative to its center.
        int x = 0;
        int y = radius;

        // Initial decision parameter for an integer radius.
        int p = 1 - radius;

        // Calculate one octant; symmetry will draw the other seven.
        while (x <= y) {

            // Draw the current point and its seven symmetric points.
            plotSymmetricPoints(buffer, xc, yc, x, y, color);

            // Decide which pixel is closer to the ideal circle.
            if (p < 0) {

                // Choose the pixel directly to the right: (x + 1, y) horizontal.
                // Update the decision parameter for the next iteration.
                p += 2 * x + 3;

            } else {

                // Choose the diagonal pixel: (x + 1, y - 1).
                // Update the decision parameter and move down one row.
                p += 2 * (x - y) + 5;
                y--;
            }

            // Advance one column in the first octant.
            x++;
        }
    }
*/

    /**
    * Draws a circle using the Midpoint Circle Algorithm.
    *
    * @param buffer the image buffer where the circle will be drawn
    * @param xc the x-coordinate of the circle's center
    * @param yc the y-coordinate of the circle's center
    * @param radius the circle radius
    * @param color the circle color
    */
    public static void midpoint(
            BufferedImage buffer,
            int xc,
            int yc,
            int radius,
            Color color) {

        // Use the default plotter to draw every generated pixel.
        midpoint(
            xc, yc, radius, color,
            (x, y, pixelColor) ->
                Pixel.put(buffer, x, y, pixelColor)
        );
    }

    public static void midpoint(
            int xc,
            int yc,
            int radius,
            Color color,
            PixelPlotter plotter) {

        if (radius < 0) {
            throw new IllegalArgumentException(
                "Radius cannot be negative."
            );
        }

        if (plotter == null) {
            throw new IllegalArgumentException(
                "Pixel plotter cannot be null."
            );
        }

        int x = 0;
        int y = radius;
        int p = 1 - radius;

        while (x <= y) {

            plotSymmetricPoints(
                plotter, xc, yc, x, y, color
            );

            if (p < 0) {
                p += 2 * x + 3;
            } else {
                p += 2 * (x - y) + 5;
                y--;
            }

            x++;
        }
    }

    private static void plotSymmetricPoints(
            PixelPlotter plotter,
            int xc,
            int yc,
            int x,
            int y,
            Color color) {

        plotter.plot(xc + x, yc + y, color);
        plotter.plot(xc + y, yc + x, color);
        plotter.plot(xc + y, yc - x, color);
        plotter.plot(xc + x, yc - y, color);

        plotter.plot(xc - x, yc - y, color);
        plotter.plot(xc - y, yc - x, color);
        plotter.plot(xc - y, yc + x, color);
        plotter.plot(xc - x, yc + y, color);
    }

    /**
    * Draws a circle using Bresenham's circle algorithm.
    *
    * <p>The algorithm calculates points in one octant and uses
    * eight-way symmetry to draw the entire circle. An integer
    * decision parameter determines whether the next point
    * advances horizontally or diagonally.</p>
    *
    * @param buffer the image buffer where the circle will be drawn
    * @param xc     the x-coordinate of the circle's center
    * @param yc     the y-coordinate of the circle's center
    * @param radius the radius of the circle
    * @param color  the color used to draw the circle
    */
    public static void bresenham(
            BufferedImage buffer,
            int xc,
            int yc,
            int radius,
            Color color) {

        if (radius < 0) {
            throw new IllegalArgumentException(
                    "Radius cannot be negative.");
        }

        // Start at the top of the circle, relative to its center.
        int x = 0;
        int y = radius;

        // Initial decision parameter for Bresenham's circle algorithm.
        //Math derivaion for the desition where to put the next pixel
        int decision = 3 - 2 * radius;

        // Calculate one octant; symmetry will draw the other seven.
        while (x <= y) {

            // Draw the current point and its seven symmetric points.
            plotSymmetricPoints(buffer, xc, yc, x, y, color);

            // Decide whether to move diagonally or horizontally.
            if (decision < 0) {

                // Choose the next horizontal pixel.
                decision += 4 * x + 6;

            } else {

                // Choose the next diagonal pixel.
                decision += 4 * (x - y) + 10;
                y--;
            }

            // Advance one column in the first octant.
            x++;
        }
    }

    /**
    * Draws a circle using a repeating pixel mask.
    *
    * @param buffer the image buffer where the circle will be drawn
    * @param xc the x-coordinate of the center
    * @param yc the y-coordinate of the center
    * @param radius the circle radius
    * @param mask a non-empty pattern containing only '1' and '0'
    * @param color the color used to draw the circle
    */
    public static void drawMasked(
            BufferedImage buffer,
            int xc,
            int yc,
            int radius,
            String mask,
            Color color) {

        // Validate the radius and mask.
        if (radius < 0) {
            throw new IllegalArgumentException(
                "Circle radius cannot be negative.");
        }

        if (mask == null || mask.isEmpty()
                || !mask.matches("[01]+")) {
            throw new IllegalArgumentException(
                    "Mask must contain only '1' and '0'.");
        }

        // A circle with radius zero is a single point.
        if (radius == 0) {
            if (mask.charAt(0) == '1') {
                Pixel.put(buffer, xc, yc, color);
            }
            return;
        }

        // Estimate the number of samples needed around the circumference.
        int steps = (int) Math.ceil(2.0 * Math.PI * radius);

        // Walk around the circle in angular order.
        for (int i = 0; i < steps; i++) {

            // Check whether the current mask position should be drawn.
            int maskIndex = i % mask.length();

            if (mask.charAt(maskIndex) == '1') {

                // Convert the current angle into pixel coordinates.
                double angle = 2.0 * Math.PI * i / steps;

                int x = xc + (int) Math.round(radius * Math.cos(angle));
                int y = yc + (int) Math.round(radius * Math.sin(angle));

                Pixel.put(buffer, x, y, color);
            }
        }
    }

    /**
    * Draws a circle with a configurable thickness using a virtual brush.
    * The circle contour is generated with the midpoint circle algorithm.
    *
    * @param buffer the image buffer where the circle will be drawn
    * @param xc the x-coordinate of the center
    * @param yc the y-coordinate of the center
    * @param radius the radius of the circle
    * @param thickness the brush diameter in pixels
    * @param color the color used to draw the circle
    */
    public static void thick(
            BufferedImage buffer,
            int xc,
            int yc,
            int radius,
            int thickness,
            Color color) {

        // Validate the circle radius.
        if (radius < 0) {
            throw new IllegalArgumentException(
                    "Circle radius cannot be negative.");
        }

        // The brush must have a positive diameter.
        if (thickness <= 0) {
            throw new IllegalArgumentException(
                    "Thickness must be greater than zero.");
        }

        // The decision parameter used by the midpoint algorithm.
        int x = 0;
        int y = radius;
        int decision = 1 - radius;

        // Handle a circle with radius zero.
        if (radius == 0) {
            paintBrush(buffer, xc, yc, thickness, color);
            return;
        }

        // Calculate one octant and reflect its points around the center.
        while (x <= y) {
            plotThickSymmetricPoints(
                    buffer, xc, yc, x, y, thickness, color);

            x++;

            // Choose the next pixel using the midpoint decision parameter.
            if (decision < 0) {
                decision += 2 * x + 1;
            } else {
                y--;
                decision += 2 * (x - y) + 1;
            }
        }
    }

    /**
    * Plots the eight symmetric points of a circle using the virtual brush.
    */
    private static void plotThickSymmetricPoints(
            BufferedImage buffer,
            int xc,
            int yc,
            int x,
            int y,
            int thickness,
            Color color) {

        // Reflect each point into the other octants of the circle.
        paintBrush(buffer, xc + x, yc + y, thickness, color);
        paintBrush(buffer, xc + y, yc + x, thickness, color);
        paintBrush(buffer, xc + y, yc - x, thickness, color);
        paintBrush(buffer, xc + x, yc - y, thickness, color);
        paintBrush(buffer, xc - x, yc - y, thickness, color);
        paintBrush(buffer, xc - y, yc - x, thickness, color);
        paintBrush(buffer, xc - y, yc + x, thickness, color);
        paintBrush(buffer, xc - x, yc + y, thickness, color);
    }

    /**
    * Moves a circular virtual brush to a position and paints its mask.
    * Pixels outside the brush mask remain unchanged.
    */
    private static void paintBrush(
            BufferedImage buffer,
            int centerX,
            int centerY,
            int thickness,
            Color color) {

        // Center the brush around its current contour point.
        int start = -(thickness - 1) / 2;
        int end = thickness / 2;

        // Use the brush radius to create a mask with rounded corners.
        double brushRadius = thickness / 2.0;
        double brushRadiusSquared = brushRadius * brushRadius;

        for (int offsetY = start; offsetY <= end; offsetY++) {
            for (int offsetX = start; offsetX <= end; offsetX++) {

                // Measure the squared distance from the brush center.
                double distanceSquared =
                        (double) offsetX * offsetX
                        + (double) offsetY * offsetY;

                // Paint only the pixels inside the circular brush mask.
                if (distanceSquared <= brushRadiusSquared) {
                    Pixel.put(
                            buffer,
                            centerX + offsetX,
                            centerY + offsetY,
                            color);
                }
            }
        }
    }

    /**
    * Returns polygon vertices approximating a circle.
    *
    * @param xc the x-coordinate of the center
    * @param yc the y-coordinate of the center
    * @param radius the circle radius
    * @param segments the number of vertices used for approximation
    * @return the vertices in contour order
    */
    public static int[][] vertices(
        int xc, int yc, int radius, int segments) {

        if (radius < 0) {
            throw new IllegalArgumentException(
                    "Circle radius cannot be negative.");
        }

        if (segments < 3) {
            throw new IllegalArgumentException(
                    "At least three segments are required.");
        }

        int[][] vertices = new int[segments][2];

        for (int i = 0; i < segments; i++) {
            // Calculate the angle of the current vertex.
            double angle = 2.0 * Math.PI * i / segments;

            // Convert the point from polar to Cartesian coordinates.
            vertices[i][0] = xc + (int) Math.round(radius * Math.cos(angle));
            vertices[i][1] = yc + (int) Math.round(radius * Math.sin(angle));
        }

        return vertices;
    }

    /**
    * Draws a transformed circle using the midpoint algorithm.
    *
    * Transformation types:
    * 1 = translation
    * 2 = rotation around the origin
    * 3 = uniform scaling around the origin
    *
    * @param buffer the image buffer
    * @param xc the original center x-coordinate
    * @param yc the original center y-coordinate
    * @param radius the original radius
    * @param transformation the transformation type
    * @param color the circle color
    */
    public static void drawTransformed(
            BufferedImage buffer,
            int xc, int yc, int radius,
            int transformation,
            Color color) {

        double newX = xc;
        double newY = yc;
        double newRadius = radius;

        switch (transformation) {
            case 1:
                newX += 80;
                newY += 40;
            break;

            case 2:
                double angle = Math.toRadians(30);

                double rotatedX =
                    xc * Math.cos(angle) - yc * Math.sin(angle);

                double rotatedY =
                    xc * Math.sin(angle) + yc * Math.cos(angle);

                newX = rotatedX;
                newY = rotatedY;
            break;

            case 3:
                newX = xc * 1.5;
                newY = yc * 1.5;
                newRadius = radius * 1.5;
            break;

            default:
                throw new IllegalArgumentException(
                    "Unknown transformation type."
                );
        }

        Circle.midpoint(
            buffer,
            (int) Math.round(newX),
            (int) Math.round(newY),
            (int) Math.round(newRadius),
            color
        );
    }

}

