import org.junit.Test;
import org.junit.Before;

import java.util.ArrayList;
import java.util.Arrays;
import static org.junit.Assert.*;

public class MoveTest {
    private Player p1;
    private Player p2;
    private Movie testMovie1;
    private Movie testMovie2;

    @Before
    public void setUp() {
        //create test win conditions
        WinCondition w1 = new WinCondition(Move.ConnectionType.GENRE, "Action", 3);
        WinCondition w2 = new WinCondition(Move.ConnectionType.ACTOR, "Tom Hanks", 4);

        //create test
        p1 = new Player("Player 1", w1);
        p2 = new Player("Player 2", w2);

        //create test movies
        testMovie1 = new Movie(1,
                "Test Movie 1", 2003, "Steven Spielberg",
                "Jphn Williams",
                new ArrayList<>(Arrays.asList("Tom Hanks", "Meryl Streep")),
                new ArrayList<>(Arrays.asList("Aaron Sorkin")),
                new ArrayList<>(Arrays.asList("Janusz Kamiński")),
                new ArrayList<>(Arrays.asList("Drama", "Thriller")));

        //create test movies
        testMovie2 = new Movie(2,
                "Test Movie 2", 2023, "Christopher Nolan",
                "Hans Zimmer",
                new ArrayList<>(Arrays.asList("Christian Bale", "Tom Hardy")),
                new ArrayList<>(Arrays.asList("Jonathan Nolan")),
                new ArrayList<>(Arrays.asList("Hoyte van Hoytema")),
                new ArrayList<>(Arrays.asList("Action", "Sci-Fi")));

    }

    @Test
    public void testMoveCreation() {
        Move move = new Move(p1, testMovie1, Move.ConnectionType.ACTOR, "Tom Hanks");

        assertEquals("Move should store the player", p1, move.getPlayer());
        assertEquals("Move should store the movie", testMovie1, move.getMoviePlayed());
        assertEquals("Move should store the connection type",
                Move.ConnectionType.ACTOR, move.getConnectionType());
        assertEquals("Move should tore the connection value",
                "Tom Hanks", move.getConnectionValue());
        //check timestamp is semt
        long currenT = System.currentTimeMillis();
        long moveT = move.getTimestamp();
        //allow 1 s for timestamp
        assertTrue("Timestamp should be close to current time",
                Math.abs(currenT - moveT) < 1000);

    }
    @Test
    public void testMovesWithSharedCOnnections() {
        //create third movie that shares connections with both test movies
        Movie testMovie3 = new Movie(3, "Test Movie 3", 2023,
                "Steven Spielberg", "Hans Zimmer",
                new ArrayList<>(Arrays.asList("Tom Hanks", "Christian Bale")),
                new ArrayList<>(Arrays.asList("Aaron Sorkin")),
                new ArrayList<>(Arrays.asList("Janusz Kamiński")),
                new ArrayList<>(Arrays.asList("Action", "Drama")));

        Move move1 = new Move(p1, testMovie1, Move.ConnectionType.ACTOR, "Tom Hanks");
        Move move2 = new Move(p2, testMovie3, Move.ConnectionType.ACTOR, "Tom Hanks");

        //check both movies have shared actor
        assertTrue("First movie should have the shared actor",
                testMovie1.hasConnection(move1.getConnectionType(), move1.getConnectionValue()));

        //test different shared connection (director)
        Move move3 = new Move(p1, testMovie1, Move.ConnectionType.DIRECTOR, "Steven Spielberg");
        Move move4 = new Move(p2, testMovie3, Move.ConnectionType.DIRECTOR, "Steven Spielberg");

        //check both movies have the shared director
        assertTrue("First movie should have the shared director",
                testMovie1.hasConnection(move3.getConnectionType(), move3.getConnectionValue()));
        assertTrue("Third movie should have the same shared director",
                testMovie3.hasConnection(move4.getConnectionType(), move4.getConnectionValue()));

    }

}
