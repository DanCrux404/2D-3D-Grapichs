
package engine.fill;

import engine.core.Pixel;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;

public class FloodFill {

    /**
     * Fills a connected region with the same color as the starting pixel.
     *
     * @param buffer the image buffer to modify
     * @param startX the x-coordinate of the starting pixel
     * @param startY the y-coordinate of the starting pixel
     * @param fillColor the replacement color
     */
    public static void fill(
            BufferedImage buffer,
            int startX,
            int startY,
            Color fillColor) {

        // Validate the starting position.
        if (startX < 0 || startY < 0
                || startX >= buffer.getWidth()
                || startY >= buffer.getHeight()) {
            return;
        }

        // Read the original color of the starting pixel.
        int targetColor = buffer.getRGB(startX, startY);
        int replacementColor = fillColor.getRGB();

        // If both colors match, no filling is necessary.
        if (targetColor == replacementColor) {
            return;
        }

        // Store pixels that still need to be processed.
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{startX, startY});

        while (!stack.isEmpty()) {

            // Get the next pixel from the stack.
            int[] point = stack.pop();
            int x = point[0];
            int y = point[1];

            // Skip pixels outside the image.
            if (x < 0 || y < 0
                    || x >= buffer.getWidth()
                    || y >= buffer.getHeight()) {
                continue;
            }

            // Only replace pixels belonging to the target region.
            if (buffer.getRGB(x, y) != targetColor) {
                continue;
            }

            // Recolor this pixel so it will not be processed again.
            Pixel.put(buffer, x, y, fillColor);

            // Add the four neighboring pixels to the stack.
            stack.push(new int[]{x + 1, y});
            stack.push(new int[]{x - 1, y});
            stack.push(new int[]{x, y + 1});
            stack.push(new int[]{x, y - 1});
        }
    }

    /**
    * Fills a connected region using a scan-line span-based flood fill.
    *
    * <p>Instead of storing individual pixels, the algorithm processes
    * horizontal spans and searches the adjacent rows for additional spans.
    * This reduces stack operations when filling large, continuous regions.</p>
    *
    * <p>The fill uses four-connected pixel adjacency and compares pixels
    * against the original color of the seed pixel.</p>
    *
    * @param buffer the image buffer where the region will be filled
    * @param startX the x-coordinate of the seed pixel
    * @param startY the y-coordinate of the seed pixel
     * @param fillColor the color used to fill the region
    */
    public static void spanFill(
        BufferedImage buffer, int startX, int startY, Color fillColor) {

        int width = buffer.getWidth();
        int height = buffer.getHeight();

        if (startX < 0 || startY < 0
                || startX >= width || startY >= height) {
            return;
        }

        int targetColor = buffer.getRGB(startX, startY);
        int replacementColor = fillColor.getRGB();

        if (targetColor == replacementColor) return;

        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{startX, startY});

        while (!stack.isEmpty()) {
            int[] point = stack.pop();
            int x = point[0];
            int y = point[1];

            // Ignore seeds that are outside the image or already filled.
            if (y < 0 || y >= height || x < 0 || x >= width) {
                continue;
            }

            if (buffer.getRGB(x, y) != targetColor) {
                continue;
            }

            // Expand the seed to the left edge of its horizontal span.
            int left = x;
            while (left > 0
                    && buffer.getRGB(left - 1, y) == targetColor) {
                left--;
            }

            // Expand the seed to the right edge of its horizontal span.
            int right = x;
            while (right < width - 1
                    && buffer.getRGB(right + 1, y) == targetColor) {
                right++;
            }

            // Fill the entire horizontal span.
            for (int px = left; px <= right; px++) {
                Pixel.put(buffer, px, y, fillColor);
            }

            // Search the rows above and below for new spans.
            for (int adjacentY : new int[]{y - 1, y + 1}) {
                if (adjacentY < 0 || adjacentY >= height) {
                    continue;
                }

                int px = left;

                while (px <= right) {

                    // Skip pixels that do not belong to the target region.
                    while (px <= right
                            && buffer.getRGB(px, adjacentY) != targetColor) {
                        px++;
                    }

                    if (px <= right) {
                        // Save one seed for this neighboring horizontal run.
                        stack.push(new int[]{px, adjacentY});

                        // Skip the rest of this run to avoid adding
                        // multiple seeds for the same run.
                        while (px <= right
                                && buffer.getRGB(px, adjacentY) == targetColor) {
                            px++;
                        }
                    }
                }
            }
        }
    }
}

