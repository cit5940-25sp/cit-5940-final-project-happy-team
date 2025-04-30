public class WinCondition {
    //use enum to make sure the win condition type is one of the fixed set
    //of values
    public enum Type { GENRE, ACTOR, DIRECTOR, WRITER, CINES, COMPOSER}

    private Type type;
    private String value;
    private int target;
    private int progress = 0;

    //constructs a win condition of the given type, target, and value
    public WinCondition (Type type, String value, int target) {
        this.type = type;
        this.value = value;
        this.target = target;
    }

    //record one successful move toward the condition
    public void recordProgress() {
        if (progress < target) {
            progress++;
        }
    }
    //check if win condition has been met
    public boolean isMet() {
        return progress >= target;
    }

    public String getDescription() {
        return "Reach " + target + " movies of " + type
                + ": " + value;
    }
}
