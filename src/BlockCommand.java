public class BlockCommand implements Command{
    private Player target;

    public BlockCommand(Player target) {
        this.target = target;

    }

    @Override
    public void execute(GameState state) {
        Player next = state.getCurrentPlayer();
        //TODO: write the block logic in Gamestate
        //state.blockPlayer(next) //need to implement blockPlayer in GameState

    }
    @Override
    public String getName() {
        return "Block";
    }
}
