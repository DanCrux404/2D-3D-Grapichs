
package engine.shapes;

import engine.core.Pixel;
import java.awt.Color;
import java.awt.image.BufferedImage;

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

}

