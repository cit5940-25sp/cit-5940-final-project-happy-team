import org.junit.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class AutocompleteTest {

    private Autocomplete ac;
    private List<String> movies = Arrays.asList("Avatar", "Avengers", "Accolades", "Activate",
            "Ace in the Hole", "Alice in Wonderland", "Divergent", "Doctors", "Diamond",
            "Die Hard", "Despicable Me", "It's a Wonderful Life", "3000 Miles to Graceland");

    @Test
    public void testEmptyPrefix() {
        ac = new Autocomplete(movies);
        List<String> sugg = ac.getSuggestions("", 4);
        List<String> exp = Arrays.asList("3000 Miles to Graceland",
                "Accolades", "Ace in the Hole", "Activate");
        assertEquals(exp, sugg);
    }

    @Test
    public void testPrefixNoMatch() {
        ac = new Autocomplete(movies);
        List<String> sugg = ac.getSuggestions("Bam", 3);
        assertTrue(sugg.isEmpty());
    }

    @Test
    public void testPrefixMatched() {
        ac = new Autocomplete(movies);
        List<String> sugg = ac.getSuggestions("di", 5);
        List<String> exp = Arrays.asList("Diamond", "Die Hard", "Divergent");
        assertEquals(exp, sugg);
    }

    @Test
    public void testEntireMovie() {
        ac = new Autocomplete(movies);
        List<String> sugg = ac.getSuggestions("Alice in Wonderland", 5);
        List<String> exp = Arrays.asList("Alice in Wonderland");
        assertEquals(exp, sugg);
    }

    @Test
    public void testNumbers() {
        ac = new Autocomplete(movies);
        List<String> sugg = ac.getSuggestions("30", 2);
        List<String> exp = Arrays.asList("3000 Miles to Graceland");
        assertEquals(exp, sugg);
    }
}
