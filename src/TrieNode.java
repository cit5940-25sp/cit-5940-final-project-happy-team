import java.util.HashMap;
import java.util.Map;

public class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    boolean isEndOfWord = false;

    // this holds the full name of the movie (that this node "finishes"
    // For "titanic", the node that has 'c' will have fullWord = Titanic, for easy
    // access
    String fullWord = null;




}
