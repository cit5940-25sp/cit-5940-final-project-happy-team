import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MovieNameGame {
    public static void main(String[] args) throws IOException {
        //intialize the database
        MovieDatabase database = new MovieDatabase();
        database.loadMovies("");//path
        database.buildIndexes();
        GameState gameState = null; // we initialize the initial gamestate using the initializeGameState method

        //create UI
        GameUI ui = new GameUI(gameState);

        //show palyer name : computer: palyer 1; human: player 2
        String name1 = "player 1";
        String name2 = "player 2";


        // generate the winCondition randomly
        WinCondition wc1 = WinCondition.random(database,3);
        WinCondition wc2 = WinCondition.random(database,3);

        //build player list - based on player 1 and 2
        //list of players
        Player p1 = new Player(name1,wc1);
        Player p2 = new Player(name2, wc2);
        List<Player> players = new ArrayList<Player>();
        players.add(p1);
        players.add(p2);

        //call controller
        GameController controller = new GameController(database,ui,players);
        controller.start();

        //exit and clean
        ui.closeUI();

    }
}
