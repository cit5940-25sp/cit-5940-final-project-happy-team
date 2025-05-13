import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MovieNameGame {
    public static void main(String[] args) throws IOException {

        // Set Lanterna to use Swing terminal
        System.setProperty("com.googlecode.lanterna.terminal" +
                ".DefaultTerminalFactory.DefaultTerminalType", "swing");
        System.setProperty("java.awt.headless", "false");

        // initialize the database
        try {
            MovieDatabase database = new MovieDatabase();
            String moviesPath = "tmdb_5000_movies.csv";
            String creditsPath = "tmdb_5000_credits.csv";
            database.loadAll(moviesPath, creditsPath);

            //show player name: : player 1;  player 2
            String name1 = "Player 1";
            String name2 = "Player 2";

            // generate winCondition randomly: true - default genre;
            // false- get winCondition randomly
            WinCondition wc1 = WinCondition.random(database,5,true);
            WinCondition wc2 = WinCondition.random(database,5,true);

            // build player list - based on player 1 and 2
            Player p1 = new Player("Player 1", wc1);
            Player p2 = new Player("Player 2", wc2);
            List<Player> players = new ArrayList<>();
            players.add(p1);
            players.add(p2);


            // create gameState
            GameState gameState = new GameState(database, players);
            gameState.initialGameState();

            // create UI
            GameUI ui = new GameUI(gameState);

            // get all movie titles for autocomplete
            List<String> allMovieTitles = new ArrayList<>();
            for (int id : database.getAllMovieIds()) {
                Movie movie = database.getMovieById(id);
                if (movie != null) {
                    allMovieTitles.add(movie.getTitle());
                }
            }
            // create and set autocomplete
            Autocomplete autocomplete = new Autocomplete(allMovieTitles);
            ui.setAutocomplete(autocomplete);

            ui.showGameState(gameState);

            // create controller and start the game
            GameController controller = new GameController(gameState, ui);

            // Start the controller in a new thread
            new Thread(() -> {
                try {
                    controller.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();


            ui.showMainWindow();

        } catch (Exception e) {
            System.err.println("Error loading database files: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
