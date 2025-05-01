public class EscapeCommand implements Command{
    private MovieDatabase database;

    public EscapeCommand (MovieDatabase database) {
        this.database = database;

    }

    @Override
    public void execute(GameState state) {
        //TODO:escape logic (pick random movie, update state)
    }
    @Override
    public String getName() {
        return "Escape";
    }
}

