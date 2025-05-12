public class Player {

    // fields
    private String name;
    private int score = 0;
    private WinCondition winCondition;


    // constructor
    public Player(String name, WinCondition winCondition) {
        this.name = name;
        this.winCondition = winCondition;
    }


    // getters
    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public WinCondition getWinCondition() {
        return winCondition;
    }


    //increments player's score and also records progress toward their win condition
    // should be called when a player makes a valid move that contributes to their win condition
    public void incrementScore() {
        score++;
        winCondition.recordProgress();
    }


    // no setters needed - because when we create a Player object, the only thing
    // that changes is the score (name and winCondition are set from creation)
    // we use incrementScore to increase the score of this player during play


}
