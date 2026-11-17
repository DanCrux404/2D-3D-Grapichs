package engine.transform;

import engine.core.Point3D;

/**
 * Provides three-dimensional geometric transformations using homogeneous coordinates.
 *
 * <p>Points are represented as column vectors, and transformations are applied through matrix
 * multiplication.
 */
public final class Transform3D {

    private Transform3D() {}

    /**
     * Creates a three-dimensional translation matrix.
     *
     * @param dx translation distance along the X axis
     * @param dy translation distance along the Y axis
     * @param dz translation distance along the Z axis
     * @return the 4x4 translation matrix
     */
    public static double[][] translation(double dx, double dy, double dz) {

        return new double[][] {
            {1, 0, 0, dx},
            {0, 1, 0, dy},
            {0, 0, 1, dz},
            {0, 0, 0, 1}
        };
    }

    /**
     * Creates a three-dimensional scaling matrix.
     *
     * @param sx scaling factor along the X axis
     * @param sy scaling factor along the Y axis
     * @param sz scaling factor along the Z axis
     * @return the 4x4 scaling matrix
     */
    public static double[][] scaling(double sx, double sy, double sz) {

        return new double[][] {
            {sx, 0, 0, 0},
            {0, sy, 0, 0},
            {0, 0, sz, 0},
            {0, 0, 0, 1}
        };
    }

    /**
     * Applies a homogeneous transformation matrix to a 3D point.
     *
     * <p>The point is represented as a column vector: [x, y, z, 1].
     *
     * @param point the point to transform
     * @param matrix the 4x4 transformation matrix
     * @return a new point containing the transformed coordinates
     * @throws IllegalArgumentException if the point or matrix is invalid
     */
    public static Point3D apply(Point3D point, double[][] matrix) {

        if (point == null || matrix == null || matrix.length != 4) {
            throw new IllegalArgumentException("Point and a 4x4 matrix are required.");
        }

        for (double[] row : matrix) {
            if (row == null || row.length != 4) {
                throw new IllegalArgumentException("Transformation matrix must be 4x4.");
            }
        }

        // Represent the 3D point using homogeneous coordinates.
        double[] vector = {point.getX(), point.getY(), point.getZ(), 1};

        double[] result = new double[4];

        // Multiply the transformation matrix by the point vector.
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i] += matrix[i][j] * vector[j];
            }
        }

        return new Point3D(result[0] / result[3], result[1] / result[3], result[2] / result[3]);
    }
}
