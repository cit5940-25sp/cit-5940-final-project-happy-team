
import org.junit.Before;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class WinConditionTest {
    private MovieDatabase db;

    @Before
    public void setUp() throws Exception {
        // initialize the movie database with test data
        db = new MovieDatabase();
        String movies = "tmdb_5000_movies.csv";
        String credits = "tmdb_5000_credits.csv";
        db.loadAll(movies, credits);
    }

    @Test
    public void testWinConditionConstruction() {
        WinCondition cond = new WinCondition(Move.ConnectionType.GENRE, "Action", 5);
        assertEquals("Type should be GENRE", Move.ConnectionType.GENRE, cond.getType());
        assertEquals("Value should be Action", "Action", cond.getValue());
        assertEquals("Target should be 5", 5, cond.getTarget());
        //intial progress should be 0
        assertEquals("Intiial progress should be 0", 0, cond.getProgress());
        assertFalse("New condition should not be met", cond.isMet());
    }

    @Test
    public void testRecordProgressAndIsMet() {
        WinCondition cond = new WinCondition(Move.ConnectionType.ACTOR, "Tom Hanks", 3);
        //record progress multiple times
        assertFalse("Condition should not be met initially", cond.isMet());
        cond.recordProgress();
        assertEquals("Progress should be 1", 1, cond.getProgress());
        assertFalse("Condition should not be met after 1 progress", cond.isMet());

        cond.recordProgress();
        assertEquals("Progress should be 2", 2, cond.getProgress());
        assertFalse("Condition should not be met after 2 progress", cond.isMet());

        cond.recordProgress();
        assertEquals("Progress should be 3", 3, cond.getProgress());
        assertTrue("Condition should not be met after 3 progress", cond.isMet());
        //extra progress should not exceed target
        cond.recordProgress();
        assertEquals("Progress should still be 3", 3, cond.getProgress());
        assertTrue("Condition should still be met", cond.isMet());


    }

    @Test
    public void testRandomNonDefault() {
        //test random generation with non-default win conditions
        for (int i = 0; i < 5; i++) {
            WinCondition cond = WinCondition.random(db, 3, false);

            Move.ConnectionType type = cond.getType();
            String value = cond.getValue();

            //check if type and value are not null
            assertNotNull("Type should not be null", type);
            assertNotNull("Value should not be null", value);
            assertFalse("Value should not be empty", value.isEmpty());

            //check if we have enough movies to satisfy this condition
            Set<Movie> moviesWithValue = null;
            switch (type) {
                case ACTOR:
                    moviesWithValue = db.getMovieByActor(value);
                    break;
                case DIRECTOR:
                    moviesWithValue = db.getMovieByDirector(value);
                    break;
                case WRITER:
                    moviesWithValue = db.getMovieByWriter(value);
                    break;
                case COMPOSER:
                    moviesWithValue = db.getMovieByComposer(value);
                    break;
                case CINES:
                    moviesWithValue = db.getMovieByCines(value);
                    break;
                case GENRE:
                    moviesWithValue = db.getMovieByGenre(value);
                    break;
                default:
                    fail("Unknown connection type");
            }
            assertNotNull("Value should exist in database", moviesWithValue);

            //check if we have enough movies
            assertTrue("Should have enough movies for the win condition",
                    moviesWithValue.size() >= cond.getTarget());
        }
    }

    @Test
    public void testMultipleRandomConditions() {
        //test genrating multiple win vondition to ensure variety
        Set<String> uniqueVal = new HashSet<>();
        Set<Move.ConnectionType> uniTypes = new HashSet<>();

        //generate 20 random conditions and check for variety
        for (int i = 0; i < 20; i++) {
            WinCondition cond = WinCondition.random(db, 3, false);
            uniqueVal.add(cond.getValue());
            uniTypes.add(cond.getType());
        }
        assertTrue("Should generate multiple unique condition values", uniqueVal.size() > 1);

    }

    @Test
    public void testWinConditionInGame() {
        //create a win condition for "Action" genre with target 3
        WinCondition cond = new WinCondition(Move.ConnectionType.GENRE, "Action",3);
        //get some action movies from the database
        Set<Movie> actionMovies = db.getMovieByGenre("Action");
        assertNotNull("Should have Action movies in database", actionMovies);
        assertTrue("Should have at least 3 Action movies", actionMovies.size() >= 3);

        //convert to array for easier access
        Movie[] movies = actionMovies.toArray(new Movie[0]);

        //simulate playing 3 action movies
        for (int i  = 0; i < 3; i++) {
            Movie movie = movies[i];
            //check the movie has the right genre
            assertTrue("movie should have Action genre",
                    movie.hasConnection(Move.ConnectionType.GENRE, "Action"));

            //record progress
            cond.recordProgress();
            assertEquals("Progress should be " + (i + 1), i + 1, cond.getProgress());
            //check if condition is met
            if (i < 2) {
                assertFalse("Condition should not be met yet", cond.isMet());
            } else {
                assertTrue("Condition should be met after 3 Action movies", cond.isMet());
            }
        }
    }

    @Test
    public void testEdgeCase() {
        //test very high target count
        WinCondition highTarget = WinCondition.random(db, 100);
        assertTrue("Target should be achievable", highTarget.getTarget() <= 100);

        //check that genrated conditionhas enough movies
        Set<Movie> movies = db.getMovieByGenre(highTarget.getValue());
        if (movies != null) {
            assertTrue("Should have enough movies for the condition",
                    movies.size() >= highTarget.getTarget());
        }


    }
    @Test
    public void testGetDescription() {
        WinCondition condition = new WinCondition(Move.ConnectionType.DIRECTOR,
                "Christopher Nolan", 4);
        String des = condition.getDescription();

        assertTrue("Description should include target count", des.contains("4"));
        assertTrue("Description should include connection type", des.contains("DIRECTOR"));
        assertTrue("Description should include value", des.contains("Christopher Nolan"));

    }

}
