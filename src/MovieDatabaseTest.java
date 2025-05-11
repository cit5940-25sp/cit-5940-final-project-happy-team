
import org.junit.Before;
import org.junit.Test;


import java.io.IOException;
import java.util.*;
import static org.junit.Assert.*;

public class MovieDatabaseTest {
    private MovieDatabase db;

    @Before
    public void setUp() throws Exception {
        db = new MovieDatabase();
        String moviesPath = "tmdb_5000_movies.csv";
        String creditsPath = "tmdb_5000_credits.csv";
        db.loadAll(moviesPath,creditsPath);
        db.buildIndexes();
    }
    @Test
    public void testGetMovieById() {
        Movie avatar = db.getMovieById(19995);
        assertNotNull("Should get Avatar by id", avatar);
        assertEquals("Avatar", avatar.getTitle());
        assertEquals(2009, avatar.getReleaseYear());

        Movie pirates = db.getMovieById(285);
        assertNotNull("Should get Avatar by id", pirates);
        assertEquals("Pirates of the Caribbean: At World's End", pirates.getTitle());
        assertEquals(2007, pirates.getReleaseYear());

        //test non_exist movie
        Movie nonExistent = db.getMovieById(9999999);
        assertNull("SHould return null for non-existent movie id", nonExistent);

    }
    @Test
    public void testGetRandomMovie() {
        //test multiple random movies
        for (int i = 0; i < 5; i++) {
            Movie randomMovie = db.getRandomMovie();
            assertNotNull("Random movie should not be null", randomMovie);
            assertTrue("Random movie should have valid id", randomMovie.getId() > 0);
            assertNotNull("Random movie should have title", randomMovie.getTitle());
            assertTrue("Random movie should have valid year",randomMovie.getReleaseYear() >= 1900);

        }
        //test the same movie isn't return twice as they marked as used
        Set<Integer> movieIds = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            Movie randomMovie = db.getRandomMovie();
            if (randomMovie != null) {
                assertFalse("Random movie should not be repeated", movieIds.contains(randomMovie.getId()));
                movieIds.add(randomMovie.getId());
            }
        }
    }
    @Test
    public void testSearchByTitlePrefix() {
        //test searching for Avatar
        List<String> avatarResults = db.searchByTitlePrefix("Ava");
        assertTrue("Search results should contain Avatar", avatarResults.contains("Avatar"));
        assertTrue("Search results should not exceed 5 items", avatarResults.size() <= 5);

        //test searching for pirates
        List<String> piratesResults = db.searchByTitlePrefix("Pirate");
        boolean found = false;
        for (String title : piratesResults) {
            if (title.startsWith("Pirates of the Caribbean")) {
                found = true;
                break;
            }
        }
        assertTrue("Search results should contain Pirates of Caribbean",
                found);
        //test searching for empty prefix
        List<String> emptyResults = db.searchByTitlePrefix("");
        assertTrue("Empty prefix should return empty list",emptyResults.isEmpty());
        //test with null prefix
        List<String> nullResults = db.searchByTitlePrefix(null);
        assertTrue("Null prefix should return empty list", nullResults.isEmpty());

        //test case insensityvity
        List<String> lower = db.searchByTitlePrefix("ava");
        List<String> upper = db.searchByTitlePrefix("AVA");
        assertEquals("Search should be case insensitive", lower,upper);
    }
    @Test
    public void testGetMovieByActor() {
        //test for Sam Worthington(Avatar)
        Set<Movie> sam = db.getMovieByActor("Sam Worthington");
        assertNotNull("Actor index should exist for Sam Worthington", sam);
        boolean found = false;
        for (Movie m : sam) {
            if ("Avatar".equals(m.getTitle())) {
                found = true;
                break;
            }
        }
        assertTrue("Sam Worthington should be in Avatar", found);

        //test for Zoe saldana (Avatar)
        Set<Movie> zoe = db.getMovieByActor("Zoe Saldana");
        assertNotNull("Actor index should exist for Zoe Saldana", zoe);
        boolean found1 = false;
        for (Movie m : zoe) {
            if ("Avatar".equals(m.getTitle())) {
                found1 = true;
                break;
            }
        }
        assertTrue("Zoe Saldana should be in Avatar", found1);
        //test for non-existent actor
        Set<Movie> nonExist = db.getMovieByActor("NonExistentActor12345");
        assertNull("Should return null for non-existent actor", nonExist);
    }
    @Test
    public void testGetMovieByDirector() {
        //test for James Cameron (avatar)
        Set<Movie> james = db.getMovieByDirector("James Cameron");
        assertNotNull("Director index should exist for James Cameron",james);
        boolean found = false;
        for (Movie m : james) {
            if ("Avatar".equals(m.getTitle())) {
                found = true;
                break;
            }
        }
        assertTrue("James Cameron should have directed Avatar", found);

        //test for non-existent director
        Set<Movie> nonExistent = db.getMovieByDirector("NonExistentDirector1357");
        assertNull("Should return null for non-existent director", nonExistent);
    }
    @Test
    public void testGetMovieByComposer() {
        Set<Movie> composerMovies = db.getMovieByComposer("James Horner");
        assertNotNull("Composer index should exist for James Horner",composerMovies);
        boolean found = false;
        for (Movie m : composerMovies) {
            if ("Avatar".equals(m.getTitle())) {
                found = true;
                break;
            }
        }
        assertTrue("James Horner should have compsoed for Avatar", found);
        Set<Movie> nonExist = db.getMovieByComposer("NonExistentComposer123456");
        assertNull("Should return null for non-existent composer", nonExist);

    }
    @Test
    public void testMovieGenres() {
        //test if genres are propery loaded for Avatar
        Movie avatar = db.getMovieById(19995);
        List<String> genres = avatar.getGenres();

        //check if genres list contains expected genres
        assertTrue("Avatar should have Action genre",genres.contains("Action"));
        assertTrue("Avatar should have Adventure genre",genres.contains("Adventure"));
        assertTrue("Avatar should have Fantasy genre",genres.contains("Fantasy"));
        assertTrue("Avatar should have Science Fiction genre", genres.contains("Science Fiction"));
    }

    @Test
    public void testMovieCredits() {
        //test if actors are loaded for avatar
        Movie avatar = db.getMovieById(19995);
        List<String> actors = avatar.getActors();
        //check if main actors are in the list
        assertTrue("Avatar should have Sam Worthington", actors.contains("Sam Worthington"));
        assertTrue("Avatar should have Zoe Saldana", actors.contains("Zoe Saldana"));
        assertTrue("Avatar should have Sigourney Weaver", actors.contains("Sigourney Weaver"));

        //test if director is properly loaded
        assertEquals("James Cameron should be director","James Cameron", avatar.getDirector());
        //test if writers include James Cameron
        List<String> writers = avatar.getWriters();
        assertTrue("James Cameron should be a writer", writers.contains("James Cameron"));
        assertEquals("James Horner should be composer","James Horner", avatar.getComposer());

    }

    @Test
    public void testLoadAllAndIndexes() throws IOException {
        //create new database to test loading
        MovieDatabase testDb = new MovieDatabase();
        //test individual loading methods
        testDb.loadMovies("tmdb_5000_movies.csv");
        //check if movies are loaded
        assertNotNull("Avatar should be loaded",testDb.getMovieById(19995));
        assertEquals("Avatar title should be correct","Avatar",testDb.getMovieById(19995).getTitle());

        //load credits
        testDb.loadCredits("tmdb_5000_credits.csv");
        //check if credits are loaded
        Movie avatar = testDb.getMovieById(19995);
        assertFalse("Avatar should have actors", avatar.getActors().isEmpty());
        //build indexes
        testDb.buildIndexes();
        //check if indexes are built
        assertNotNull("Actor index should exist for Sam Worthington",testDb.getMovieByActor("Sam Worthington"));
        assertNotNull("Director index should exist for James Cameron",testDb.getMovieByDirector("James Cameron"));
    }
    @Test
    public void testGetMovieByWriter() {
        Set<Movie> writerMovies = db.getMovieByWriter("James Cameron");
        assertNotNull("Writer index should exist for James Cameron", writerMovies);
        boolean found = false;
        for (Movie m : writerMovies) {
            if ("Avatar".equals(m.getTitle())) {
                found = true;
                break;
            }
        }
        assertTrue("James Cameron should have written Avatar", found);
        Set<Movie> nonExist = db.getMovieByWriter("NonExistentWriter14678");
        assertNull("Should return null for non-existent writer",nonExist);
    }

    @Test
    public void testGetMovieBycines() {
        Set<Movie> cines = db.getMovieByCines("Mauro Fiore");
        assertNotNull("Cinematographer index should exist for Mauro Fiore", cines);
        boolean found = false;
        for (Movie m : cines) {
            if ("Avatar".equals(m.getTitle())) {
                found = true;
                break;
            }
        }
        assertTrue("Mauro FIore should have been cinematographer for Avatar", found);
        Set<Movie> nonExist = db.getMovieByCines("NonExistentCinematographer 13567");
        assertNull("Should return null for non-existent cinematographer", nonExist);
    }

    //compelte test
    @Test
    public void testFull() throws IOException {
    MovieDatabase igDb = new MovieDatabase();
    igDb.loadAll("tmdb_5000_movies.csv","tmdb_5000_credits.csv");
    igDb.buildIndexes();

    //assert get by id
    Movie avatar = igDb.getMovieById(19995);
    assertNotNull("Avatar should be loaded by ID", avatar);
    assertEquals("Avatar", avatar.getTitle());
    assertEquals(2009, avatar.getReleaseYear());

    //assert prefix search
    List<String> prefix = igDb.searchByTitlePrefix("Pirate");
    assertFalse("Prefix search for 'Pirate' should not be empty",prefix.isEmpty());
    boolean found = false;
    for (String title : prefix) {
        if (title.startsWith("Pirates of the Caribbean")) {
            found = true;
            break;
        }
    }
    assertTrue("Search results should include 'Pirates of the Caribbean'", found);
    //assert actor index
    Set<Movie> sam = igDb.getMovieByActor("Sam Worthington");
    assertNotNull("Sam Worthington should be indexed", sam);
    boolean found1 = false;
    for (Movie m : sam) {
        if ("Avatar".equals(m.getTitle())) {
            found1 = true;
            break;
        }
    }
    assertTrue("Sam Worthington should appear in Avatar", found1);

    //assert director index
    Set<Movie> cam = igDb.getMovieByDirector("James Cameron");
    assertNotNull("James Cameron should be indexed", cam);
    boolean found2 = false;
    for (Movie m : cam) {
        if ("Avatar".equals(m.getTitle())) {
            found2 = true;
            break;
        }
    }
    assertTrue("James Cameron should have directed Avatar", found2);

    //assert composer index
    Set<Movie> hornerMovies = igDb.getMovieByComposer("James Horner") ;
    assertNotNull("James Horner should be indexed", hornerMovies);
    boolean found3 = false;
    for (Movie m : hornerMovies) {
        if ("Avatar".equals(m.getTitle())) {
            found3 = true;
            break;
        }
    }
    assertTrue("James Horner should have composed Avatar", found3);




}

}
