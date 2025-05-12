public class EscapeCommand implements Command {
    private MovieDatabase database;

    public EscapeCommand(MovieDatabase database) {
        this.database = database;

    }

    @Override
    public void execute(GameState state) {
        Movie newRandomMovie = database.getRandomMovie();

        state.setCurrentMovie(newRandomMovie);

        // add this here if it doesn't clash with GameUI
        System.out.println("Escape power-up used! " +
                "The new movie is " + newRandomMovie.getTitle() + "!");
    }
    @Override
    public String getName() {
        return "Escape";
    }
}

