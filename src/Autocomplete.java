import java.util.ArrayList;
import java.util.List;

public class Autocomplete {
    private TrieNode root;

    // constructor - root node is a TrieNode
    public Autocomplete(List<String> movieTitles) {
        root = new TrieNode();
        // insert all titles
        for (String title : movieTitles) {
            insert(title);
        }
    }


    // methods

    // add word into Trie
    private void insert(String word) {
        // insert from the root
        TrieNode node = root;

        for (char c : word.toLowerCase().toCharArray()) {
            // if the node's children do not contain char, create child Node for it
            if (!node.children.containsKey(c)) {
                node.children.put(c, new TrieNode());
            }
            // update node
            node = node.children.get(c);
        }

        // at the end of the word
        node.isEndOfWord = true;
        node.fullWord = word;
    }

    // to be used in GameUI
    public List<String> getSuggestions(String prefix, int maxSuggestions) {
        List<String> results = new ArrayList<>();
        TrieNode prefixNode = getNode(prefix.toLowerCase());
        if (prefixNode == null) {
            return results;
        }

        dfs(prefixNode, results, maxSuggestions);
        return results;
    }

    // find suggestions using dfs
    public void dfs(TrieNode node, List<String> results, int max) {
        if (results.size() == max) {
            return;
        }
        if (node.isEndOfWord) {
            results.add(node.fullWord);
        }

        // sort
        node.children.keySet().stream().sorted()
                .forEach(c -> dfs(node.children.get(c), results, max));
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
