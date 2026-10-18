
package engine.clipping;

/**
 * Implements the Cohen-Sutherland line clipping algorithm
 * for rectangular clipping windows.
 */
public class CohenSutherland {

    private static final int INSIDE = 0;
    private static final int LEFT   = 1;
    private static final int RIGHT  = 2;
    private static final int BOTTOM = 4;
    private static final int TOP    = 8;

    /**
     * Clips a line segment against a rectangular clipping window.
     *
     * @param x1 the x-coordinate of the first endpoint
     * @param y1 the y-coordinate of the first endpoint
     * @param x2 the x-coordinate of the second endpoint
     * @param y2 the y-coordinate of the second endpoint
     * @param xmin the minimum x-coordinate of the clipping window
     * @param ymin the minimum y-coordinate of the clipping window
     * @param xmax the maximum x-coordinate of the clipping window
     * @param ymax the maximum y-coordinate of the clipping window
     * @return an array containing the clipped endpoints
     *         {x1, y1, x2, y2}, or null if the line is invisible
     */
    public static int[] clip(
            int x1, int y1, int x2, int y2,
            int xmin, int ymin, int xmax, int ymax) {

        if (xmin > xmax || ymin > ymax) {
            throw new IllegalArgumentException(
                "Invalid clipping window boundaries."
            );
        }

        double ax = x1;
        double ay = y1;
        double bx = x2;
        double by = y2;

        while (true) {
            int codeA = computeCode(ax, ay, xmin, ymin, xmax, ymax);
            int codeB = computeCode(bx, by, xmin, ymin, xmax, ymax);

            // Both endpoints are inside the window.
            if ((codeA | codeB) == 0) {
                return new int[] {
                    (int) Math.round(ax),
                    (int) Math.round(ay),
                    (int) Math.round(bx),
                    (int) Math.round(by)
                };
            }

            // Both endpoints are outside the same region.
            if ((codeA & codeB) != 0) {
                return null;
            }

            // Select an endpoint outside the clipping window.
            int codeOut = (codeA != 0) ? codeA : codeB;

            double x;
            double y;

            // Calculate the intersection with a window boundary.
            if ((codeOut & TOP) != 0) {
                x = ax + (bx - ax) * (ymax - ay) / (by - ay);
                y = ymax;
            } else if ((codeOut & BOTTOM) != 0) {
                x = ax + (bx - ax) * (ymin - ay) / (by - ay);
                y = ymin;
            } else if ((codeOut & RIGHT) != 0) {
                y = ay + (by - ay) * (xmax - ax) / (bx - ax);
                x = xmax;
            } else {
                y = ay + (by - ay) * (xmin - ax) / (bx - ax);
                x = xmin;
            }

            // Replace the outside endpoint with the intersection.
            if (codeOut == codeA) {
                ax = x;
                ay = y;
            } else {
                bx = x;
                by = y;
            }
        }
    }

    /**
     * Computes the region code of a point relative to the window.
     *
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     * @param xmin the minimum x-coordinate of the window
     * @param ymin the minimum y-coordinate of the window
     * @param xmax the maximum x-coordinate of the window
     * @param ymax the maximum y-coordinate of the window
     * @return the four-bit region code; zero indicates an inside point
     */
    private static int computeCode(
            double x, double y,
            int xmin, int ymin, int xmax, int ymax) {

        int code = INSIDE;

        if (x < xmin) {
            code |= LEFT;
        } else if (x > xmax) {
            code |= RIGHT;
        }

        if (y < ymin) {
            code |= BOTTOM;
        } else if (y > ymax) {
            code |= TOP;
        }

        return code;
    }
}

