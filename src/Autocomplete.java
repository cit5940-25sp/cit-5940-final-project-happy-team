import java.util.List;

public class Autocomplete {
    private TrieNode root;


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


    public List<String> getSuggestions(String prefix, int max)





}
