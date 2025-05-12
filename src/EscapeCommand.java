public class EscapeCommand implements Command {
    private MovieDatabase database;

    public EscapeCommand(MovieDatabase database) {
        this.database = database;

    }

    @Override
    public void execute(GameState state) {
        Movie newRandomMovie = database.getRandomMovie();

        // escape gives us a new random movie that is played, like a free change
        state.setCurrentMovie(newRandomMovie);


        System.out.println("Escape power-up used! " +
                "The new movie is " + newRandomMovie.getTitle() + "!");
    }
    @Override
    public String getName() {
        return "Escape";
    }
}

