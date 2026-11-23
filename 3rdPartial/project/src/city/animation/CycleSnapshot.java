package city.animation;

import java.awt.Color;

public record CycleSnapshot(
        Color skyColor,
        DayNightCycle.Phase phase,
        double windowIntensity,
        double starVisibility,
        double ambientFactor,
        long elapsedMillis) {
}
