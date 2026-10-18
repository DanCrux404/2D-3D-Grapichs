package engine.fill;

import engine.core.Pixel;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class ScanLine {

    /**
     * Fills a polygon using the Scan-Line algorithm.
     *
     * @param buffer the image buffer where the polygon will be filled
     * @param vertices the polygon vertices in contour order
     * @param color the fill color
     */
    public static void fill(
            BufferedImage buffer,
            int[][] vertices,
            Color color) {

        // A polygon needs at least three vertices.
        if (vertices == null || vertices.length < 3) {
            throw new IllegalArgumentException(
                    "A polygon needs at least three vertices.");
        }

        // Find the vertical limits of the polygon.
        int minY = vertices[0][1];
        int maxY = vertices[0][1];

        for (int[] vertex : vertices) {
            if (vertex == null || vertex.length < 2) {
                throw new IllegalArgumentException(
                        "Each vertex must contain x and y coordinates.");
            }

            minY = Math.min(minY, vertex[1]);
            maxY = Math.max(maxY, vertex[1]);
        }

        // Process the polygon one horizontal row at a time.
        for (int y = minY; y <= maxY; y++) {

            double[] intersections = new double[vertices.length];
            int count = 0;

            // Check every edge, including the closing edge.
            for (int i = 0; i < vertices.length; i++) {

                int[] current = vertices[i];
                int[] next = vertices[(i + 1) % vertices.length];

                int x1 = current[0];
                int y1 = current[1];
                int x2 = next[0];
                int y2 = next[1];

                // Ignore horizontal edges to avoid duplicate intersections.
                if (y1 == y2) {
                    continue;
                }

                // Use a half-open interval to count shared vertices once.
                if (y >= Math.min(y1, y2)
                        && y < Math.max(y1, y2)) {

                    // Calculate where this edge crosses the current row.
                    double xIntersection = x1
                            + (double) (y - y1) * (x2 - x1)
                            / (y2 - y1);

                    intersections[count] = xIntersection;
                    count++;
                }
            }

            // Sort intersections from left to right.
            Arrays.sort(intersections, 0, count);

            // Fill between pairs of intersections.
            for (int i = 0; i + 1 < count; i += 2) {

                int startX = (int) Math.ceil(intersections[i]);
                int endX = (int) Math.floor(intersections[i + 1]);

                for (int x = startX; x <= endX; x++) {
                    Pixel.put(buffer, x, y, color);
                }
            }
        }
    }
}

