public class SkipCommand implements Command{
   // private Player target;
    public SkipCommand() {

    }

    // this executes specific gamestate based on skip command (player gets skipped)
    // gamestate is now different, and everything should reflect that
    @Override
    public void execute(GameState state) {
        Player next = state.getCurrentPlayer();

        // this is implemented in gameState
        // skips current player's turn
        state.skipPlayer();

    }

    @Override
    public String getName() {
        return "Skip";
    }
}
