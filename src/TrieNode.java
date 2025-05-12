import java.util.Map;
import java.util.TreeMap;

public class TrieNode {
    // TreeMap because we may be using Autocomplete via alphabetical order
    Map<Character, TrieNode> children = new TreeMap<>();
    boolean isEndOfWord = false;

    // this holds the full name of the movie (that this node "finishes"
    // For "titanic", the node that has 'c' will have fullWord = Titanic, for easy
    // access
    String fullWord = null;
}
