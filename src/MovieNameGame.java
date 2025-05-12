import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MovieNameGame {
    public static void main(String[] args) throws IOException {
        //initialize the database
        //use loadAll to loadMovies and build index
        //quit the program if it cannot load data


        // Set Lanterna to use Swing terminal
        System.setProperty("com.googlecode.lanterna.terminal" +
                ".DefaultTerminalFactory.DefaultTerminalType", "swing");
        System.setProperty("java.awt.headless", "false");

        try {

            MovieDatabase database = new MovieDatabase();
            String moviesPath = "tmdb_5000_movies.csv";
            String creditsPath = "tmdb_5000_credits.csv";
            database.loadAll(moviesPath, creditsPath);

            //show player name: : player 1;  player 2
            String name1 = "Player 1";
            String name2 = "Player 2";

            // generate the winCondition randomly
            //true: default genre
            //false: get winCondition randomly
            WinCondition wc1 = WinCondition.random(database,5,true);
            WinCondition wc2 = WinCondition.random(database,5,true);

            //build player list - based on player 1 and 2
            //list of players
            Player p1 = new Player(name1,wc1);
            Player p2 = new Player(name2, wc2);
            List<Player> players = new ArrayList<>();
            players.add(p1);
            players.add(p2);

            //first create gameState
            GameState gameState = new GameState(database, players);
            gameState.initialGameState();

            //create UI
            GameUI ui = new GameUI(gameState);

            // Get all movie titles for autocomplete
            List<String> allMovieTitles = new ArrayList<>();
            for (int id : database.getAllMovieIds()) {
                Movie movie = database.getMovieById(id);
                if (movie != null) {
                    allMovieTitles.add(movie.getTitle());
                }
            }
            // Create and set autocomplete
            Autocomplete autocomplete = new Autocomplete(allMovieTitles);
            ui.setAutocomplete(autocomplete);

            ui.showGameState(gameState);

            //create controller and start the game
            GameController controller = new GameController(gameState, ui);

           // GameController controller = new GameController(database, ui, players);

            //THREAD
            // Start the controller in a new thread
            new Thread(() -> {
                try {
                    controller.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();


            ui.showMainWindow();

            //controller.start();

            //exit and clean
            //ui.closeUI();
        } catch (Exception e) {
            System.err.println("Error loading database files: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
