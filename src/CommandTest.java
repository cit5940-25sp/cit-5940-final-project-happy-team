import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CommandTest {

    private GameState state;
    private Player player1;
    private Player player2;
    private MovieDatabase db;

    private Movie movie1;
    private Movie movie2;
    private int movieCallCount;

    @BeforeEach
    public void setup() {
        // test data for us to test on
        ArrayList<String> sharedActors = new ArrayList<>(Collections.singletonList("Shared Actor"));
        ArrayList<String> writers1 = new ArrayList<>(Collections.singletonList("Writer A"));
        ArrayList<String> writers2 = new ArrayList<>(Collections.singletonList("Writer B"));
        ArrayList<String> cines = new ArrayList<>(Collections.singletonList("Cine A"));
        ArrayList<String> genres = new ArrayList<>(Collections.singletonList("Genre A"));

        movie1 = new Movie(1, "Movie One", 2000, "Dir A", "Comp A", sharedActors, writers1, cines, genres);
        movie2 = new Movie(2, "Movie Two", 2001, "Dir B", "Comp B", sharedActors, writers2, cines, genres);

        // fake temp MovieDatabase that returns movie1 on first call and movie2 on second
        movieCallCount = 0;
        db = new MovieDatabase() {
            @Override
            public Movie getRandomMovie() {
                movieCallCount++;
                if (movieCallCount == 1) {
                    return movie1;
                } else {
                    return movie2;
                }
            }
        };

        // create win conditions for players
        WinCondition wc1 = new WinCondition(Move.ConnectionType.ACTOR, "Shared Actor", 1);
        WinCondition wc2 = new WinCondition(Move.ConnectionType.DIRECTOR, "Dir B", 1);

        player1 = new Player("Player 0", wc1);
        player2 = new Player("Player 1", wc2);

        // create GameState
        List<Player> players = new ArrayList<Player>();
        players.add(player1);
        players.add(player2);

        state = new GameState(db, players);
        state.initialGameState();  // sets movie1 as the first current movie
    }


    //////////////////////// TEST CODE BLOCKS BELOW /////////////////////////


    // test skip command and see if it skips the player's turn
    @Test
    public void testingSkipCommandSkipsTurn() {
        Player before = state.getCurrentPlayer();
        new SkipCommand().execute(state);
        Player after = state.getCurrentPlayer();
        assertNotEquals(before, after, "SkipCommand should skip to next player");
    }


    // test escape command and see if it gives a new Movie as the current Movie
    @Test
    public void testingEscapeCommandChangesMovie() {
        Movie before = state.getPlayedMoviesHistory().get(state.getPlayedMoviesHistory().size() - 1);

        EscapeCommand escape = new EscapeCommand(db);
        escape.execute(state);

        Movie after = state.getPlayedMoviesHistory().get(state.getPlayedMoviesHistory().size() - 1);

        assertNotEquals(before.getId(), after.getId(), "EscapeCommand should change the current movie");
    }



    // test block command and see if the player can repeat a turn (because they've blocked the opponent's
    // next turn
    @Test
    public void testingBlockCommandSkipsBlockedPlayer() {
        state.skipPlayer();

        new BlockCommand().execute(state);

        assertEquals(player2, state.getCurrentPlayer(), "BlockCommand should skip opponent's turn");
    }
}
