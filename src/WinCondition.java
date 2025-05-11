import java.util.List;
import java.util.Random;
import java.util.Set;

public class WinCondition {
    //use enum to make sure the win condition type is one of the fixed set
    //of values

    private Move.ConnectionType type;
    private String value;
    private int target;
    private int progress = 0;

    //constructs a win condition of the given type, target, and value
    public WinCondition (Move.ConnectionType type, String value, int target) {
        this.type = type;
        this.value = value;
        this.target = target;
    }

    //record one successful move toward the condition
    public void recordProgress() {
        if (progress < target) {
            progress++;
        }
    }
    //check if win condition has been met
    public boolean isMet() {
        return progress >= target;
    }

    public String getDescription() {
        return "Reach " + target + " movies of " + type
                + ": " + value;
    }

    //random generate the win condition based on the movie database
    //default is genre based , but can also genreate other types of win condition
    //-true is default version
    public static WinCondition random(MovieDatabase database, int targetCount) {

        return random(database, targetCount, true);
    }

    //random generaes a win condition based on movie database
    //allow specifying whehter to use only default (gener based) win conditions
    //or to include all possible win condition types
    public static WinCondition random(MovieDatabase database, int targetCount, boolean defaultOnly) {
        Random rand = new Random();
        //decide which type of win condition to generate
        Move.ConnectionType selectedType;

        //default to genre based as write up requirements
        if (defaultOnly) {
            selectedType = Move.ConnectionType.GENRE;
        } else {
            Move.ConnectionType[] types = Move.ConnectionType.values();
            selectedType = types[rand.nextInt(types.length)];

        }
        //value to be selected for the chosen type
        String selectedValue = null;
        Set<Movie> moviesWithValue = null;

        if (selectedType == Move.ConnectionType.GENRE) {
            //for genre-based win condition
            String[] commonGenres = {
                    "Action", "Adventure","Animation","Comedy", "Crime",
                    "Documentary", "Drama", "Family", "Fantasy", "History",
                    "Horror", "Music", "Mystery", "Romance", "Science Fiction",
                    "Thriller", "War", "Western"
            };
            //try to find a genre with enough movies
            for (int attempt = 0; attempt < 10; attempt ++) {
                String candidateGenre = commonGenres[rand.nextInt(commonGenres.length)];
                Set<Movie> moviesWithGenre = database.getMovieByGenre(candidateGenre);

                if (moviesWithGenre != null && moviesWithGenre.size() >= targetCount) {
                    selectedValue = candidateGenre;
                    moviesWithValue = moviesWithGenre;
                    break;
                }
            }
            //if nop suitable genre was found, defualt to Drama
            if (selectedValue == null) {
                selectedValue = "Drama";
                moviesWithValue = database.getMovieByGenre(selectedValue);
                //adjust target if necessary
                if (moviesWithValue != null && moviesWithValue.size() < targetCount) {
                    targetCount = Math.min(targetCount, moviesWithValue.size());
                }
            }

        } else {
            //for non-genre win conditions (actor, director...)
            int maxAttempts = 20;
            for (int attempt = 0; attempt < maxAttempts; attempt++) {
                //get a random movie
                Movie randomMovie = database.getRandomMovie();
                if (randomMovie == null) {
                    continue;
                }
                //get conenctions of the selected type
                List<String> connections = randomMovie.getConnections(selectedType);
                if (connections == null || connections.isEmpty()) {
                    continue;
                }
                //choose a random connection
                String candidateValue = connections.get(rand.nextInt(connections.size()));
                if (candidateValue == null || candidateValue.isEmpty()) {
                    continue;
                }
                //check how many movies have this conenction
                Set<Movie> moviesWithCandidate = null;

                switch (selectedType) {
                    case ACTOR:
                        moviesWithCandidate = database.getMovieByActor(candidateValue);
                        break;
                    case DIRECTOR:
                        moviesWithCandidate = database.getMovieByDirector(candidateValue);
                        break;
                    case WRITER:
                        moviesWithCandidate = database.getMovieByWriter(candidateValue);
                        break;
                    case COMPOSER:
                        moviesWithCandidate = database.getMovieByComposer(candidateValue);
                        break;
                    case CINES:
                        moviesWithCandidate = database.getMovieByCines(candidateValue);
                        break;
                    default:
                        break;
                }

                //if we found enough movies, use this value
                if (moviesWithCandidate != null && moviesWithCandidate.size() >= targetCount) {
                    selectedValue = candidateValue;
                    moviesWithValue = moviesWithCandidate;
                    break;
                }
            }
            //if no suitable value was found, fall back to genre base condition
            if (selectedValue == null) {
                return random(database, targetCount, true);
            }
        }
        //create and return win condition
        return new WinCondition(selectedType, selectedValue, targetCount);

    }
    public Move.ConnectionType getType() {
        return type;
    }
    public String getValue() {
        return value;
    }

    //get target number of movies needed to meet the win condition
    //will be used in unit test
    public int getTarget() {
        return target;
    }
    public  int getProgress() {
        return progress;
    }



}
