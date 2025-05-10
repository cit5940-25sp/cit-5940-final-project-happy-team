import java.time.LocalDate;
import java.util.*;
import com.google.gson.*;
import java.io.BufferedReader;
import java.io.*;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;



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
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("mm/dd/yyyy");

    //load movie, credit csv and then build indexes
    public void loadAll(String moviesCsvFile, String creditsCsvFile) throws IOException {
        loadMovies(moviesCsvFile);
        loadCredits(creditsCsvFile);
        buildIndexes();
    }


    // load data from a file to be indexed
    //load basic movie data: id, title, year, genres
    public void loadMovies (String path) throws IOException{
        try (BufferedReader br = Files.newBufferedReader(Paths.get(path))) {
            br.readLine();//skip header
            String line;
            while ((line = br.readLine()) != null) {
                //split into 19 parts
                String [] fields = line.split(",",19);
                int id = Integer.parseInt(fields[3].trim());
                String title = fields[17].trim();
                //parse release data - year
                int year = 0;
                try {
                    LocalDate d = LocalDate.parse(fields[11].trim(),dateTimeFormatter);
                    year = d.getYear();
                } catch (Exception ignore) {
                }

                Movie m = new Movie(id, title, year,"","",
                        new ArrayList<String>(),
                        new ArrayList<String>(),
                        new ArrayList<String>(),
                        new ArrayList<String>()
                        );
                //record title for autocomplete
                titleSet.add(title);
                //parse genres JSON array
                try {
                    JsonArray arr = JsonParser.parseString(fields[1]).getAsJsonArray();
                    for (JsonElement el : arr) {
                        String genreName = el.getAsJsonObject().get("name").getAsString();
                    }

                } catch (Exception ignore) {

                }
                moviesById.put(id,m);
            }
        } catch (IOException e) {
            System.err.println("Failed to load movies: " + e.getMessage());
        }

    }

    //load movie credits:cast and crew info
    public void loadCredits(String path) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(Paths.get(path))){
            br.readLine();//skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",",4);
                int id = Integer.parseInt(p[0].trim());
                Movie m = moviesById.get(id);
                if (m == null) {
                    continue;
                }
                //parse cast (up to 10 actors)
                try {
                    JsonArray castArr = JsonParser.parseString(p[2]).getAsJsonArray();
                    for (JsonElement el : castArr) {
                        if (m.getActors().size() >= 10) {
                            break;
                        }
                        String actorName = el.getAsJsonObject().get("name").getAsString();
                        m.getActors().add(actorName);
                    }
                } catch (Exception ignore) {

                }
                //parse crew: director, composer, writers, cinematographers
                try {
                    JsonArray crewArr = JsonParser.parseString(p[3]).getAsJsonArray();
                    for (JsonElement el : crewArr) {
                        JsonObject obj = el.getAsJsonObject();
                        String jobName = obj.get("job").getAsString();
                        String person = obj.get("name").getAsString();
                        switch (jobName) {
                            case "Director":
                                m.setDirector(person);
                                break;
                            case "Original Music Composer":
                                m.setComposer(person);
                                break;
                            case "Writer":
                                m.getWriters().add(person);
                                break;
                            //case "Cinematographer"
                            case "Director of Photography":
                                m.getCines().add(person);
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
                    actorIndex.put(w,s);
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
        List<Integer> ids = new ArrayList<>(moviesById.keySet());
        int idx = new Random().nextInt(ids.size());
        return moviesById.get(ids.get(idx));
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
