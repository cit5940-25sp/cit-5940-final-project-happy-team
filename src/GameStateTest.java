import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class GameStateTest {
    private GameState state;
    private Player player1;
    private Player player2;
    private MovieDatabase db;
    private Movie movie1;
    private Movie movie2;
    private int movieCallCount;

    // set up some things (similar to CommandTest)
    @BeforeEach
    public void setup() {
        ArrayList<String> sharedActors = new ArrayList<>(Collections.singletonList("Shared Actor"));
        ArrayList<String> writers1 = new ArrayList<>(Collections.singletonList("Writer A"));
        ArrayList<String> writers2 = new ArrayList<>(Collections.singletonList("Writer B"));
        ArrayList<String> cines = new ArrayList<>(Collections.singletonList("Cine A"));
        ArrayList<String> genres = new ArrayList<>(Collections.singletonList("Genre A"));

        movie1 = new Movie(1, "Movie One", 2000, "Dir A",
                "Comp A", sharedActors, writers1, cines, genres);
        movie2 = new Movie(2, "Movie Two", 2001, "Dir B",
                "Comp B", sharedActors, writers2, cines, genres);

        movieCallCount = 0;
        db = new MovieDatabase() {
            @Override
            public Movie getRandomMovie() {
                movieCallCount++;
                return (movieCallCount == 1) ? movie1 : movie2;
            }
        };

        WinCondition wc1 = new WinCondition(Move.ConnectionType.ACTOR, "Shared Actor", 1);
        WinCondition wc2 = new WinCondition(Move.ConnectionType.DIRECTOR, "Dir B", 1);

        player1 = new Player("Player 0", wc1);
        player2 = new Player("Player 1", wc2);

        List<Player> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);

        state = new GameState(db, players);
        state.initialGameState();
    }




    /////////////// TEST CODE BLOCKS BELOW /////////////////



    @Test
    public void testInitialGameStateSetsMovie() {
        assertNotNull(state.getPlayedMoviesHistory().get(0));
    }

    @Test
    public void testGetPlayedMoviesHistoryReturnsCopy() {
        List<Movie> history = state.getPlayedMoviesHistory();
        history.clear();
        assertFalse(state.getPlayedMoviesHistory().isEmpty());
    }

    @Test
    public void testIsValidMoveRejectsUsedMovie() {
        Move move = new Move(player1, movie1, Move.ConnectionType.ACTOR, "Shared Actor");
        assertFalse(state.isValidMove(move));
    }

    @Test
    public void testIsValidMoveRejectsOverusedConnection() {
        Move move = new Move(player1, movie2, Move.ConnectionType.ACTOR, "Shared Actor");
        for (int i = 0; i < 3; i++) {
            state.applyMove(move);
            state = new GameState(db, Arrays.asList(player1, player2));
            state.initialGameState();
        }
        assertFalse(state.isValidMove(move));
    }

    @Test
    public void testApplyMoveUpdatesState() {
        Move move = new Move(player1, movie2, Move.ConnectionType.ACTOR, "Shared Actor");
        state.applyMove(move);
        assertEquals(movie2, state.getPlayedMoviesHistory()
                .get(state.getPlayedMoviesHistory().size() - 1));
    }

    @Test
    public void testNextTurnSwitchesPlayer() {
        Player before = state.getCurrentPlayer();
        state.nextTurn();
        assertNotEquals(before, state.getCurrentPlayer());
    }

    @Test
    public void testSetTimeExpiredMakesGameOverTrue() {
        state.setTimeExpired(true);
        assertTrue(state.isGameOver());
    }

    @Test
    public void testGetWinnerReturnsCorrectPlayerByWinCondition() {
        player1.getWinCondition().recordProgress();
        assertEquals(player1, state.getWinner());
    }

    @Test
    public void testApplyCommandRemovesAndExecutes() {
        Command skip = null;
        for (Command cmd : state.getPowerUpsFor(player1)) {
            if (cmd instanceof SkipCommand) {
                skip = cmd;
                break;
            }
        }

        // before using it, it should exist
        assertNotNull(skip, "SkipCommand should be present initially");
        // applying the command should be executed (it returns true if it is executed)
        assertTrue(state.applyCommand(player1, skip), "Command should execute and be removed");
        // this is after using it, so it should no longer be available
        assertFalse(state.getPowerUpsFor(player1).contains(skip),
                "Command should be removed after execution");
    }


    @Test
    public void testSkipPlayerAdvancesTurn() {
        Player before = state.getCurrentPlayer();
        state.skipPlayer();
        assertNotEquals(before, state.getCurrentPlayer());
    }

    @Test
    public void testBlockPlayerSkipsOpponentTurn() {
        state.skipPlayer();
        Player before = state.getCurrentPlayer();
        state.blockPlayer(state.getOpponentPlayer());
        assertEquals(before, state.getCurrentPlayer());
    }



    @Test
    public void testSetCurrentMovieAddsToHistory() {
        Movie newMovie = new Movie(3, "New Movie", 2022, "Dir C",
                "Comp C", new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>());
        state.setCurrentMovie(newMovie);
        assertEquals(newMovie, state.getPlayedMoviesHistory()
                .get(state.getPlayedMoviesHistory().size() - 1));
    }

    @Test
    public void testGetOpponentPlayerReturnsOther() {
        // player 1 is us, player 2 is the other player (opponent)
        Player p1 = state.getCurrentPlayer();
        Player p2 = state.getOpponentPlayer();

        // player 2 should be our opponent (using getOpponentPlayer())
        assertEquals(p2, state.getOpponentPlayer(),
                "Opponent should be player 2 initially");
        // if we advance to next turn, (it's our opponents turn now)
        state.nextTurn();

        // the opponent player from the opponent's perspective is player 1
        assertEquals(p1, state.getOpponentPlayer(),
                "After switching turn, opponent should be player 1");
    }


    // COMMAND TESTS

    @Test
    public void escapeCommandUpdatesHistoryAndMovie() {
        Movie before = state.getPlayedMoviesHistory()
                .get(state.getPlayedMoviesHistory().size() - 1);
        EscapeCommand escape = new EscapeCommand(db);
        escape.execute(state);
        Movie after = state.getPlayedMoviesHistory()
                .get(state.getPlayedMoviesHistory().size() - 1);

        assertNotEquals(before.getId(), after.getId());
        assertEquals(after, state.getPlayedMoviesHistory()
                .get(state.getPlayedMoviesHistory().size() - 1));
    }


    // edge case -- it should select a different movie (don't use repeat one)
    @Test
    public void testEscapeCommandDoesNotRepeatUsedMovie() {
        Movie before = state.getPlayedMoviesHistory()
                .get(state.getPlayedMoviesHistory().size() - 1);
        EscapeCommand escape = new EscapeCommand(db);
        escape.execute(state);
        Movie after = state.getPlayedMoviesHistory()
                .get(state.getPlayedMoviesHistory().size() - 1);

        // Even if it's technically random, make sure we're not just replaying the same movie
        assertNotEquals(before.getId(), after.getId(),
                "Escape should switch to a different movie");
    }

    @Test
    public void blockCommandPreventsOpponentTurn() {
        Player before = state.getCurrentPlayer();
        state.blockPlayer(state.getOpponentPlayer());
        Player after = state.getCurrentPlayer();
        assertEquals(before, after);
    }

    @Test
    public void skipCommandAdvancesTurnAndRound() {
        // make roundsPlayed public or use a getter
        int beforeRound = state.getRoundsPlayed();
        Player before = state.getCurrentPlayer();
        new SkipCommand().execute(state);
        Player after = state.getCurrentPlayer();

        assertNotEquals(before, after);
        assertEquals(beforeRound + 1, state.getRoundsPlayed());
    }



    @Test
    public void testTryBuildMoveReturnsValidMove() {
        // movie1 has "Shared Actor", movie2 also has "Shared Actor"
        // currentMovie is movie1, we are trying to build a Move to movie2
        Optional<Move> maybeMove = state.tryBuildMove(player1, movie2);

        assertTrue(maybeMove.isPresent(),
                "tryBuildMove should return a valid Move if a connection exists");

        Move move = maybeMove.get();

        // Verify the fields of the Move
        assertEquals(player1, move.getPlayer(),
                "Move should be for the correct player");
        assertEquals(movie2, move.getMoviePlayed(),
                "Move should target movie2");
        assertEquals(Move.ConnectionType.ACTOR, move.getConnectionType(),
                "Connection type should match shared attribute");
        assertEquals("Shared Actor", move.getConnectionValue(),
                "Connection value should match shared attribute");
    }







}
