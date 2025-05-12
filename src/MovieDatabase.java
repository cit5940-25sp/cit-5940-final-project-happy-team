import java.util.*;
import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

//we use JSON parsing (GSon) because some fields in the CSV are stored as JSON arrays inside
//a single CSV cell,  we use csv file paths so that loading from both IDE
// and simple String.split() would make a mess

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
    //a sorted set of all movie titles for prefix search
    private  TreeSet<String> titleSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    //GSOn instance for parsing JSON- csv fields
    private  Gson gson = new Gson();
    //Formatter for release date strings
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M/dd/yyyy");
    //keep track of which movies we've returned randomly
    private Set<Integer> usedMovieIds = new HashSet<>();
    private Map<String, Movie> moviesByTitle = new HashMap<>();

    //load movie, credit csv and then build indexes
    public void loadAll(String moviesCsvFile, String creditsCsvFile) throws IOException {
        loadMovies(moviesCsvFile);
        loadCredits(creditsCsvFile);
        buildIndexes();
    }


    // load data from a file to be indexed
    //load basic movie data: id, title, year, genres
    //genres come in as a JSON string, so we parse them with Gson
    public void loadMovies(String path) throws IOException {
        Path csv = Paths.get(path);
        if (!Files.exists(csv)) {
            csv = Paths.get("src",path);
        }
        try (
                Reader in = Files.newBufferedReader(csv);
                CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT.withFirstRecordAsHeader());
        ) {
            for (CSVRecord rec : parser) {
                int id = Integer.parseInt(rec.get("id").trim());
                String title = rec.get("title").trim();
                //parse release year from either M/D/YYYY or YYYY-MM-DD
                int year = 0;
                String rd = rec.get("release_date").trim();
                if (!rd.isEmpty()) {
                    String[] parts = rd.split("/");
                    if (parts.length == 3) {
                        year = Integer.parseInt(parts[2]);
                    } else {
                        //try alternatively format YYYY-MM-DD
                        parts = rd.split("-");
                        if (parts.length == 3) {
                            year = Integer.parseInt(parts[0]);
                        }
                    }
                   // System.out.println("Movie id: " + id + ", Title: " + title +
                           // " , Release Date: " + rd + ", Parsed yr: " + year);
                }

                //try to parse genres if exist
                List<String> genres = new ArrayList<>();
                String genresStr = rec.get("genres");
                if (genresStr != null && !genresStr.isEmpty()) {
                    try {
                        JsonArray genresArr = JsonParser.parseString(genresStr).getAsJsonArray();
                        for (JsonElement el : genresArr) {
                            String genreName = el.getAsJsonObject().get("name").getAsString();
                            genres.add(genreName);
                        }
                    } catch (Exception ignore) {

                    }
                }
                //create a movie with empty crew/actors for now
                Movie m = new Movie(id, title, year,"",""
                        ,new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        genres);
                titleSet.add(title);
                moviesById.put(id,m);
                moviesByTitle.put(title.toLowerCase(),m);
            }


        }
    }

    //read the credits CSV< parse cast (actors) and crew roles:
    //Director, Original Music Composer, Writer, Director of Photography
    public void loadCredits(String path) throws IOException {
        Path csv = Paths.get(path);
        if (!Files.exists(csv)) {
            csv = Paths.get("src",path);
        }
        try (
                Reader in = Files.newBufferedReader(csv);
                CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT.withFirstRecordAsHeader());
        ) {
            for (CSVRecord rec : parser) {
                int id = Integer.parseInt(rec.get(0).trim());
                Movie m = moviesById.get(id);
                if (m == null) {
                    continue;
                }
                String castJson = rec.get("cast");

                //Parse cast JSON array for actor names
                try {
                    JsonArray arr = JsonParser.parseString(castJson).getAsJsonArray();
                    for (JsonElement el : arr) {
                        String name = el.getAsJsonObject().get("name").getAsString();
                        m.getActors().add(name);
                    }
                } catch (Exception ignore) {

                }
                //parse crew JSON array for specific jobs
                String crewJson = rec.get("crew");
                try {
                    JsonArray arr = JsonParser.parseString(crewJson).getAsJsonArray();
                    for (JsonElement el : arr) {
                        String job = el.getAsJsonObject().get("job").getAsString();
                        String name = el.getAsJsonObject().get("name").getAsString();
                        switch (job) {
                            case "Director":
                                m.setDirector(name);
                                break;
                            case "Original Music Composer":
                                m.setComposer(name);
                                break;
                            case "Writer":
                                m.getWriters().add(name);
                                break;
                            case "Director of Photography":
                                m.getCines().add(name);
                                break;
                            default:
                                break;
                        }
                    }
                } catch (Exception ignore) {

                }
            }
        }
    }

    // build index maps (lookup index) for actor, dir, writer, etc
    public void buildIndexes() {
        for (Movie m : moviesById.values()) {
            //index actors
            for (String a : m.getActors()) {
                Set<Movie> s = actorIndex.get(a);
                if (s == null) {
                    s = new HashSet<>();
                    actorIndex.put(a,s);
                }
                s.add(m);
            }
            //index director
            String d = m.getDirector();
            if (!d.isEmpty()) {
                Set<Movie> s = directorIndex.get(d);
                if (s == null) {
                    s = new HashSet<>();
                    directorIndex.put(d,s);
                }
                s.add(m);
            }
            //index writers
            for (String w : m.getWriters()) {
                Set<Movie> s = writerIndex.get(w);
                if (s == null) {
                    s = new HashSet<>();
                    writerIndex.put(w,s);
                }
                s.add(m);
            }
            //index cinematographers
            for (String c : m.getCines()) {
                Set<Movie> s = cinesIndex.get(c);
                if (s == null) {
                    s = new HashSet<>();
                    cinesIndex.put(c,s);
                }
                s.add(m);
            }
            //index composer
            String comp = m.getComposer();
            if (!comp.isEmpty()) {
                Set<Movie> s = composerIndex.get(comp);
                if (s == null) {
                    s = new HashSet<>();
                    composerIndex.put(comp,s);
                }
                s.add(m);
            }
            //index genres
            for (String g : m.getGenres()) {
                Set<Movie> s = genreIndex.get(g);
                if (s == null) {
                    s = new HashSet<>();
                    genreIndex.put(g,s);
                }
                s.add(m);
            }
        }
    }


    //return up to 5 movie titles that start with the given prefix (case-insensitive)
    public List<String> searchByTitlePrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return Collections.emptyList();
        }
        //define an exclusive upper bound by appending '{'
        String from = prefix;
        String to = prefix + "{";

        NavigableSet<String> range = titleSet.subSet(from,true,to,false);
        List<String> suggestions = new ArrayList<>(5);
        int count = 0;
        for (String t : range) {
            suggestions.add(t);
            if (count++ >= 4) { //show up to 5 results
                break;
            }
        }
        return suggestions;

    }


    // getters (accessing set of Movies that have this element)

    // retrieves Movie by its ID (it returns whatever Movie has that id)
    public Movie getMovieById(int id) {
        return moviesById.get(id);
    }

    //get movie randomly, once movie is used, return null
    public Movie getRandomMovie() {
        List<Integer> unused = new ArrayList<>();
        for (int id : moviesById.keySet()) {
            if (!usedMovieIds.contains(id)) {
                unused.add(id);
            }

        }
        //if we have exhausted all movies, return null
        if (unused.isEmpty()) {
            return null;
        }
        //pick one at random
        int idx = new Random().nextInt(unused.size());
        int chosenID = unused.get(idx);
        //mark it as used
        usedMovieIds.add(chosenID);
        //return the movie
        return moviesById.get(chosenID);

    }

    //getters for each index
    public Set<Movie> getMovieByGenre(String genre) {
        return genreIndex.get(genre);
    }

    public Set<Movie> getMovieByActor(String actor) {
        return actorIndex.get(actor);
    }

    public Set<Movie> getMovieByDirector(String director) {
        return directorIndex.get(director);
    }

    public Set<Movie> getMovieByComposer(String composer) {
        return composerIndex.get(composer);
    }

    public Set<Movie> getMovieByCines(String cines) {
        return cinesIndex.get(cines);
    }

    public Set<Movie> getMovieByWriter(String writer) {
        return writerIndex.get(writer);
    }

    public Set<Integer> getAllMovieIds() {
        return new HashSet<>(moviesById.keySet());

    }
    public Movie getMovieByTitle(String title) {
        if (title == null || title.isEmpty()) {
            return null;
        }
        return moviesByTitle.get(title.toLowerCase());
    }

}
