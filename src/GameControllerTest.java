import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


public class GameControllerTest {
    private GameController controller;
    private MovieDatabase database;
    private GameUI fakeUI;
    private Player player;
    private Movie movie1;
    private Movie movie2;

    private GameState testState;


    // to make this easier we want to make it so the player can meet their win
    // condition after one move.
    // we manually set the win condition to be something achievable, and have
    // their first move be valid to making progress

    @BeforeEach
    public void setup() throws IOException {
        movie1 = new Movie(1, "Movie One", 2000, "Dir A", "Comp A",
                Collections.singletonList("Actor X"), Collections.singletonList("Writer A"),
                Collections.singletonList("Cine A"), Collections.singletonList("Genre A"));

        movie2 = new Movie(2, "Movie Two", 2001, "Dir A", "Comp A",
                Collections.singletonList("Actor X"), Collections.singletonList("Writer B"),
                Collections.singletonList("Cine B"), Collections.singletonList("Genre B"));

        database = new MovieDatabase() {
            @Override
            public Movie getRandomMovie() {
                return movie1;
            }

            @Override
            public Movie getMovieByTitle(String title) {
                if (title.equalsIgnoreCase("movie two")) {
                    return movie2;
                }
                return null;
            }
        };

        player = new Player("Player 0", new WinCondition(Move.ConnectionType.ACTOR, "Actor X", 1));

        List<Player> players = Collections.singletonList(player);
        testState = new GameState(database, players);  // use the class field


        // fake/rigged UI that always returns "movie two" as input
        fakeUI = new GameUI(testState) {
            boolean shownEnd = false;

            @Override
            public void showGameState(GameState state) {
                // skip GUI logic here, we dont rlly need
            }

            @Override
            public String promptPlayer(Player currentPlayer) {
                return "movie two";
            }

            @Override
            public void showError(String msg) {
                fail("Shouldn't hit showError in valid move test");
            }

            @Override
            public void showGameEnd(Player winner) {
                shownEnd = true;
            }
        };

        controller = new GameController(testState, fakeUI);
    }

    @Test
    public void testGameControllerAppliesValidMove() throws IOException {
        testState.initialGameState();

        controller.start();
        // should process one move and end game since win condition is met
        assertTrue(player.getWinCondition().isMet(),
                "Player should meet win condition after valid move");
    }






}
