# Engine 2D

Mini library in Java for drawing primitive shapes (lines, circles, and ovals) on a `BufferedImage`, without depending on any window or graphical framework.

The idea is to separate the **drawing code** (reusable) from the **projects that test it**, so the same classes aren't reimplemented in every assignment and the projects stay clean: you just import the engine packages and call them.

## Contents

- **`engine.core`**: base utilities. Includes `Pixel`, which handles painting a pixel in the buffer (with bounds validation), so no shape has to implement it on its own.

- **`engine.shapes`**: primitive shapes. For now `Line`, which groups different line-drawing implementations (line equation, DDA, Bresenham). Circle and oval will be added gradually.

## Project structure

```
2ndPartial/
├── engine/
│   └── src/engine/
│       ├── core/Pixel.java
│       └── shapes/Line.java
│
└── TestFigures/
    ├── src/Main.java
    ├── src/Window.java   <- window with the buffer where engine calls are tested
    └── run.sh
```

`TestFigures` is a test project that consumes the engine. Each project can have its own `Window`; the only thing the engine needs is the `BufferedImage` to paint on.

## Usage

```java
import engine.shapes.Line;
import java.awt.Color;

public class Main {
    public static void main(String[] args) {
        Window w = new Window("Test", 500, 500);
        Line.bresenham(w.getBuffer(), 10, 10, 300, 200, Color.RED);
        w.setVisible(true);
    }
}
```

## Compile and run

From the folder of the project that uses the engine (`2ndPartial`), `run.sh` compiles the engine directly from its source code, so the latest version is always used without generating a `.jar`:

```bash
./run.sh (name of folder)
```

## Goal

To have a set of already-tested, ready-to-reuse primitive shape implementations, so each new project can focus on *what* it draws and not on *how* it gets painted.
