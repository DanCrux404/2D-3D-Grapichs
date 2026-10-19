## Project structure 

proyect/
└── src/
    └── proyect/
        ├── Main.java
        ├── Canvas.java
        ├── CityScene.java
        │
        ├── elements/
        │   ├── Building.java
        │   ├── Sun.java
        │   ├── Moon.java
        │   ├── Cloud.java
        │   └── Star.java
        │
        ├── sprites/
        │   └── Sprite.java
        │
        └── animation/
            ├── DayNightCycle.java
            └── AnimationController.java

This project consumes everything from engine.
The idea is to make an animation, in this case
a city with transitions of morning, evening and night.
