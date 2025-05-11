import java.time.LocalDate;
import java.util.*;
import com.google.gson.*;
import java.io.BufferedReader;
import java.io.*;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;



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
    private  TreeSet<String> titleSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    private  Gson gson = new Gson();
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M/dd/yyyy");
    private Set<Integer> usedMovieIds = new HashSet<>();

    //load movie, credit csv and then build indexes
    public void loadAll(String moviesCsvFile, String creditsCsvFile) throws IOException {
        loadMovies(moviesCsvFile);
        loadCredits(creditsCsvFile);
        buildIndexes();
    }


    // load data from a file to be indexed
    //load basic movie data: id, title, year, genres
    public void loadMovies (String path) throws IOException {
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
                Movie m = new Movie(id, title, year,"",""
                        ,new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        genres);
                titleSet.add(title);
                moviesById.put(id,m);
            }


        }
    }

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
                //String crewJson = rec.get(3);
                try {
                    JsonArray arr = JsonParser.parseString(castJson).getAsJsonArray();
                    for (JsonElement el : arr) {
                        String name = el.getAsJsonObject().get("name").getAsString();
                        m.getActors().add(name);
                    }
                } catch (Exception ignore) {

                }
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
    public void buildIndexes(){
        for (Movie m : moviesById.values()) {
            for (String a : m.getActors()) {
                Set<Movie> s = actorIndex.get(a);
                if ( s == null) {
                    s = new HashSet<>();
                    actorIndex.put(a,s);
                }
                s.add(m);
            }
            String d = m.getDirector();
            if (!d.isEmpty()) {
                Set<Movie> s = directorIndex.get(d);
                if (s == null) {
                    s = new HashSet<>();
                    directorIndex.put(d,s);
                }
                s.add(m);
            }
            for (String w : m.getWriters()) {
                Set<Movie> s = writerIndex.get(w);
                if (s == null) {
                    s = new HashSet<>();
                    writerIndex.put(w,s);
                }
                s.add(m);
            }
            for (String c : m.getCines()) {
                Set<Movie> s = cinesIndex.get(c);
                if (s == null) {
                    s = new HashSet<>();
                    cinesIndex.put(c,s);
                }
                s.add(m);
            }
            String comp = m.getComposer();
            if (!comp.isEmpty()) {
                Set<Movie> s = composerIndex.get(comp);
                if (s == null) {
                    s = new HashSet<>();
                    composerIndex.put(comp,s);
                }
                s.add(m);
            }
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



    public List<String> searchByTitlePrefix(String prefix){
        if (prefix == null || prefix.isEmpty()) {
            return Collections.emptyList();
        }
        String from = prefix;
        String to = prefix + "{";

        NavigableSet<String> range = titleSet.subSet(from,true,to,false);
        List<String> suggestions = new ArrayList<>(5);
        int count = 0;
        for (String t : range) {
            suggestions.add(t);
            if (count++ >= 5) {
                break;
            }
        }
        return suggestions;

    }


    // getters (accessing set of Movies that have this element)


    // retrieves Movie by its ID (it returns whatever Movie has that id)
    public Movie getMovieById(int id){
        return moviesById.get(id);
    }

    //get movie randomly
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
