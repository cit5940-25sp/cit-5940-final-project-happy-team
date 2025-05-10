public class SkipCommand implements Command{
    private Player target;
    public SkipCommand() {

    }
    @Override
    public void execute(GameState state) {
        Player next = state.getCurrentPlayer();
        //TODO: implement skip logic in gamestate
        //state.skipPlayer(next); //need to implement this in gameState


    } // im gonna do this today

    @Override
    public String getName() {
        return "Skip";
    }
}
