public class BlockCommand implements Command{
    private Player target;

    // BlockCommand will always be called so that it blocks the opposite player, so no setup needed
    public BlockCommand() {
    }


    // uses getOpponentPlayer from GameState to always have the target (the player we want to block) on other player
    @Override
    public void execute(GameState state) {
        Player opposingPlayer = state.getOpponentPlayer();
        state.blockPlayer(opposingPlayer);
    }

    @Override
    public String getName() {
        return "Block";
    }
}
