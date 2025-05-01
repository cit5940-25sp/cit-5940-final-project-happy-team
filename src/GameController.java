import java.util.List;

public class GameController {

    // fields
    private MovieDatabase database;
    private GameUI ui;
    private GameState state;



    // constructs a controller with required info
    public GameController(MovieDatabase database, GameUI ui, List<Player> players){
        this.database = database;
        this.ui = ui;
        this.state = new GameState(database, players);
    }


    // game loop start (this is the method that starts the game loop)
    public void start(){
        // call the initial game state (random first movie)
        state.initialGameState();
        while(!state.isGameOver()){
            ui.showGameState(state);
            String input = ui.promptPlayer(state.getCurrentPlayer());



            // TODOOOOOOO: parse input into Move or Command, validate, and apply
            // input is the player string input, parse it into a Move (movie)
            // split it into movie name, actor, etc....
            // then use that to create a Move
            // if player wants to use a PowerUp, do that here

            // its either going to be a Move or a Command(powerup)




            state.nextTurn();
        }
        ui.showGameEnd(state.getWinner());
    }

}
