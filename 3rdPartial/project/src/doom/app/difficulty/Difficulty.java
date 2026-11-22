package doom.app.difficulty;

public enum Difficulty {

    TOO_YOUNG_TO_DIE("I'M TOO YOUNG TO DIE"),
    NOT_TOO_ROUGH("HEY, NOT TOO ROUGH"),
    HURT_ME_PLENTY("HURT ME PLENTY"),
    ULTRA_VIOLENCE("ULTRA-VIOLENCE"),
    NIGHTMARE("NIGHTMARE");

    private final String displayName;

    Difficulty(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}