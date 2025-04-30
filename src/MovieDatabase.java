import java.util.*;

public class MovieDatabase {

    // fields

    // These maps will be filled out string (person name) to a Movie
    private Map<Integer, Movie> moviesById = new HashMap<>();
    private Map<String, Set<Movie>> actorIndex = new HashMap<>();
    private Map<String, Set<Movie>> directorIndex = new HashMap<>();
    private Map<String, Set<Movie>> writerIndex = new HashMap<>();
    private Map<String, Set<Movie>> cinesIndex = new HashMap<>();
    private Map<String, Set<Movie>> composerIndex = new HashMap<>();
    private Map<String, Set<Movie>> genreIndex = new HashMap<>();

    // load data from a file to be indexed
    public void loadMovies (String csvFilePath){
        // will call buildIndexes
    }

    // build index maps (lookup index) for actor, dir, writer, etc
    public void buildIndexes(){
    }


    // THIS MAY HAVE TO DO WITH AUTOCOMPLETE (SEE TA EMAIL)
    public List<String> searchByTitlePrefix(String prefix){
        return new ArrayList<>();
    }

    // getters (accessing set of Movies that have this element)


    // retrieves Movie by its ID (it returns whatever Movie has that id)
    public Movie getMovieById(int id){
        return moviesById.get(id);
    }


    public Set<Movie> getMovieByGenre(String genre){
        return genreIndex.get(genre);
    }

    public Set<Movie> getMovieByActor (String actor){
        return actorIndex.get(actor);
    }

    public Set<Movie> getMovieByDirector (String director){
        return directorIndex.get(director);
    }

    public Set<Movie> getMovieByComposer (String composer){
        return composerIndex.get(composer);
    }

    public Set<Movie> getMovieByCines (String cines){
        return cinesIndex.get(cines);
    }

    public Set<Movie> getMovieByWriter (String writer){
        return writerIndex.get(writer);
    }





}
