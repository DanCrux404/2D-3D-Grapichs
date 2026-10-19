import project.animation.DayNightCycle;

public class TestDayNightCycle {

    public static void main(String[] args) {
        DayNightCycle cycle = new DayNightCycle();

        // Simulate 90 seconds.
        cycle.update(90_000L);

        System.out.println("Phase: " + cycle.getPhase());
        System.out.println("Sky: " + cycle.getSkyColor());
        System.out.println(
            "Star visibility: " + cycle.getStarVisibility()
        );

        // Simulate another 60 seconds.
        cycle.update(60_000L);

        System.out.println("Phase: " + cycle.getPhase());
        System.out.println("Sky: " + cycle.getSkyColor());
        System.out.println(
            "Star visibility: " + cycle.getStarVisibility()
        );
    }
}

