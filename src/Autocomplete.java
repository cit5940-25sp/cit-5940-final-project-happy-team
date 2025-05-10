import java.util.ArrayList;
import java.util.List;

public class Autocomplete {
    private TrieNode root;

    // 1. We have made a TrieNode class that contains the TrieNode
    // structure, to help you

    // 2. PLEASE SEE THE EMAIL FROM the TAs -- it has information on Lanterna
    // which will be used WITH the Autocomplete class. It's basically a plugin
    // with its own methods that will help us format the UI of the game

    /* 3. Notes:
    Hi! This is the class for Autocomplete, it's basically the same
    logic as the previous autocomplete assignment, except instead of
    a dictionary, we use the Movie titles that our database gives us

    The goal is for the player to be able to see autocompleted words
    that give them relevant suggestions in ALPHABETICAL ORDER

    Example 1: if the player types "Harry" or "Har" or "Ha"

    then the autocomplete suggestions should give "Harry Potter and the
    Chamber of Secrets" (Or just all of the harry potter films)
    in alphabetical order

    Example 2: if the player types "a"

    then the autocomplete suggestions should give "abstinence", then "accolades"
    , then "atonement", etc.... depending on how many suggestions we want

    (pretend these are all actual movie names)

     */

    // constructor for an autocomplete object
    // root node is a TrieNode
    public Autocomplete(List<String> movieTitles){
        root = new TrieNode();
        // for all titles in all the movietitles
        // insert it
        for(String title : movieTitles){
            insert(title);
        }
    }


    // methods


    // add this word into the Trie
    private void insert(String word){
        // start insertion at the root
        TrieNode node = root;
        // for all the characters in this word
        for (char c : word.toLowerCase().toCharArray()){
            // if the node's children do not contain c
            // create one child Node and put it there
            if (!node.children.containsKey(c)){
                node.children.put(c, new TrieNode());
            }
            // update node (the one we are on) to the node
            // that the last child was found at
            node = node.children.get(c);
        }

        // we are at the end of the word, so flip it to true
        node.isEndOfWord = true;
        // fullWord is the full word given (for easy access)
        node.fullWord = word;

    }


    public List<String> getSuggestions(String prefix, int maxSuggestions) {
        List<String> results = new ArrayList<>();
        TrieNode prefixNode = getNode(prefix.toLowerCase());
        if (prefixNode == null) {
            return results;
        }

        dfs(prefixNode, results, maxSuggestions);
        return results;
    }


    public void dfs (TrieNode node, List<String> results, int max){
        if (results.size() == max) {
            return;
        }
        if (node.isEndOfWord) {
            results.add(node.fullWord);
        }

        // Sort
        node.children.keySet().stream().sorted().forEach(c -> dfs(node.children.get(c), results, max));
    }

    // get node representing end of prefix
    private TrieNode getNode(String prefix) {
        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return null;
            }
            node = node.children.get(c);
        }
        return node;
    }
}
