import org.junit.Test;
import org.junit.Before;
import java.util.*;
import static org.junit.Assert.*;

import static org.junit.Assert.assertEquals;

public class MovieTest {
    private MovieDatabase db;
    private Movie avatar; //id : 19995
    private Movie pirates; //id : 285

    @Before
    public void setUp() throws Exception {
        db = new MovieDatabase();
        String movies = "tmdb_5000_movies.csv";
        String credits = "tmdb_5000_credits.csv";
        db.loadAll(movies, credits);

        //get sample movies to test
        avatar = db.getMovieById(19995);
        pirates = db.getMovieById(285);
    }

    @Test
    public void testMovieBasicInfo() {
        //test basic movie info
        assertEquals("Avatar ID should be 19995", 19995, avatar.getId());
        assertEquals("Avatar title should be correct","Avatar", avatar.getTitle());
        assertEquals("Avatar year should be 2009", 2009, avatar.getReleaseYear());

        assertEquals("Pirates ID should be 285", 285, pirates.getId());
        assertEquals("Pirates title should be correct", "Pirates of the Caribbean: At World's End",
                pirates.getTitle());
        assertEquals("Pirates year should be 2007", 2007, pirates.getReleaseYear());

    }
    @Test
    public void testHasConnectionWithActors() {
        //test hasConnection with actors
        assertTrue("Avatar should have Sam Worthington",
                avatar.hasConnection(Move.ConnectionType.ACTOR, "Sam Worthington"));
        assertTrue("Avatar should have Zoe Saldana",
                avatar.hasConnection(Move.ConnectionType.ACTOR, "Zoe Saldana"));
        assertTrue("Avatar should have Sigourney Weaver",
                avatar.hasConnection(Move.ConnectionType.ACTOR, "Sigourney Weaver"));

    }

    @Test
    public void testFindCommonConnections() {
        //test finding common connection between 2 movies
        List<String> avatarActors = avatar.getConnections(Move.ConnectionType.ACTOR);
        List<String> piratesActors = pirates.getConnections(Move.ConnectionType.ACTOR);

        //find common actors if exist
        Set<String> commonActors = new HashSet<>(avatarActors);
        commonActors.retainAll(piratesActors);
        //find common genres
        List<String> avatarGenres = avatar.getConnections(Move.ConnectionType.GENRE);
        List<String> piratesGenres = pirates.getConnections(Move.ConnectionType.GENRE);
        Set<String> com = new HashSet<>(avatarActors);
        commonActors.retainAll(piratesGenres);
        assertTrue("Avatar and Pirates should share at least one genre",
                !com.isEmpty());
    }

}
