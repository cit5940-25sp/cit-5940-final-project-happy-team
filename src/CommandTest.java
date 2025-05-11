import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;


public class CommandTest {
    private GameState state;
    private Player player1;
    private Player player2;
    private MovieDatabase db;

    @BeforeEach
    public void setup() {
        // Manually create fake movie database with two movies that share the same actor
        ArrayList<String> sharedActors = new ArrayList<>(Arrays.asList("Shared Actor"));
        ArrayList<String> writers1 = new ArrayList<>(Arrays.asList("Writer A"));
        ArrayList<String> writers2 = new ArrayList<>(Arrays.asList("Writer B"));
        ArrayList<String> cines = new ArrayList<>(Arrays.asList("Cine A"));
        ArrayList<String> genres = new ArrayList<>(Arrays.asList("Genre A"));

        Movie movie1 = new Movie(1, "Movie One", 2000, "Dir A", "Comp A", sharedActors, writers1, cines, genres);
        Movie movie2 = new Movie(2, "Movie Two", 2001, "Dir B", "Comp B", sharedActors, writers2, cines, genres);

        db = new MovieDatabase() {
            private List<Movie> fakeMovies = Arrays.asList(movie1, movie2);

            @Override
            public Movie getRandomMovie() {
                return movie2;
            }
        };

        WinCondition wc1 = new WinCondition(Move.ConnectionType.ACTOR, "Shared Actor", 1);
        WinCondition wc2 = new WinCondition(Move.ConnectionType.DIRECTOR, "Dir B", 1);

        player1 = new Player("Alice", wc1);
        player2 = new Player("Bob", wc2);

        state = new GameState(db, Arrays.asList(player1, player2));
        state.setCurrentMovie(movie1); // simulate game start
    }

    @Test
    public void testSkipCommandSkipsTurn() {
        Player before = state.getCurrentPlayer();
        new SkipCommand().execute(state);
        Player after = state.getCurrentPlayer();
        assertNotEquals(before, after, "SkipCommand should skip to next player");
    }

    @Test
    public void testEscapeCommandChangesMovie() {
        Movie before = state.getPlayedMoviesHistory().get(state.getPlayedMoviesHistory().size() - 1);

        EscapeCommand escape = new EscapeCommand(db);
        escape.execute(state);
        Movie after = state.getPlayedMoviesHistory().get(state.getPlayedMoviesHistory().size() - 1);

        assertNotEquals(before.getId(), after.getId(), "EscapeCommand should change the current movie");
    }

    @Test
    public void testBlockCommandSkipsBlockedPlayer() {
        // Make player2 the current player
        state.skipPlayer(); // Alice → Bob

        // Block Alice (Bob's opponent)
        new BlockCommand().execute(state); // Alice should be blocked and skipped

        // Now the current player should loop back to Bob
        assertEquals(player2, state.getCurrentPlayer(), "BlockCommand should skip opponent's turn");
    }


}
