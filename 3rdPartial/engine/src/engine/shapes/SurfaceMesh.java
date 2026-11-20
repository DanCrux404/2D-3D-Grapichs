package engine.shapes;

import java.awt.Color;
import java.awt.image.BufferedImage;

public interface SurfaceMesh {

    void translate(double dx, double dy, double dz);

    void scale(double sx, double sy, double sz);

    void rotateX(double angle);

    void rotateY(double angle);

    void rotateZ(double angle);

    void draw(BufferedImage buffer, Color color);
}

