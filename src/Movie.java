import java.util.Arrays;
import java.util.List;

public class Movie {

    // private fields for each Movie object
    // each Movie object will have these fields that act like their attributes

    // our ID part of the Movie object is their numerical 1,2,3,4... numbering
    // no particular order, it's the order they were parsed in.
    // this makes it so the largest id is also equal to the number of Movies we have
    // in our database
    private int id;
    private String title;
    private int releaseYear;
    private String director;
    private String composer;
    private List<String> actors;
    private List<String> writers;
    private List<String> cines;
    private List<String> genres;


    // Constructor for Movie object (we create one Movie object for each movie in the database)
    // and we parse and fill this Movie object with the information we parsed
    public Movie(int id, String title, int releaseYear, String director,
                 String composer, List<String> actors, List<String> writers,
                 List<String> cines, List<String> genres) {
        this.id = id;
        this.title = title;
        this.releaseYear = releaseYear;
        this.director = director;
        this.composer = composer;
        this.actors = actors;
        this.writers = writers;
        this.cines = cines;
        this.genres = genres;
    }



    // check if this movie has a specific connection value of the given type
    //used in gameState to determine if a move is valid
    public boolean hasConnection(Move.ConnectionType type, String value) {
        if (value == null) {
            return false; //return false if no connection
        }
        switch (type) {
            case ACTOR:
                return actors != null && actors.contains(value);
            case DIRECTOR:
                return director != null && !director.isEmpty() &&
                        director.equals(value);
            case WRITER:
                return writers != null && writers.contains(value);
            case COMPOSER:
                return composer != null &&  !composer.isEmpty() &&
                        composer.equals(value);
            case CINES:
                return cines != null && cines.contains(value);
            case GENRE:
                return genres != null && genres.contains(value);
            default:
                return false;
        }
    }

    //return a list of all values for a specific connection type
    public List<String> getConnections(Move.ConnectionType type) {
        switch (type) {
            case ACTOR:
                return actors;
            case DIRECTOR:
                return Arrays.asList(director);
            case WRITER:
                return writers;
            case COMPOSER:
                return Arrays.asList(composer);
            case CINES:
                return cines;
            case GENRE:
                return genres;
            default:
                return Arrays.asList(); // empty list
        }
    }

    // getters (9 getters for the 9 fields)
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public List<String> getActors() {
        return actors;
    }

    public List<String> getCines() {
        return cines;
    }

    public String getComposer() {
        return composer;
    }

    public String getDirector() {
        return director;
    }

    public List<String> getGenres() {
        return genres;
    }

    public List<String> getWriters() {
        return writers;
    }

    public void setDirector(String director) {
        this.director = director;
    }
    public void setComposer(String composer) {
        this.composer = composer;
    }


}
