public interface Command {
    // execute this power-up on current game state
    void execute(GameState gameState);

    //show the name of power-up
    String getName();

}
