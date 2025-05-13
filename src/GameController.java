import java.io.IOException;
import java.util.Optional;

public class GameController {

    // fields
    private MovieDatabase database;
    private GameUI ui;
    private GameState state;



    // constructs a controller with required info
    public GameController(GameState state, GameUI ui) {
        this.state = state;
        this.ui = ui;
        this.database = state.getDatabase();
    }



    // game loop start (this is the method that starts the game loop)
    // initializes the game state
    public void start() throws IOException {
        // call the initial game state (random first movie)
       // state.initialGameState();

        // while it's not isGameOver yet, keep looping this
        while (!state.isGameOver()) {
            // UI -- show the game state + prompt the current player for input
            // ui.showGameState(state);
            Player currentPlayer = state.getCurrentPlayer();
            String input = ui.promptPlayer(state.getCurrentPlayer());

            // parse input to determine if its a powerUP (skip, block, escape) or a Movie
            // convert the input string into a corresponding Command object
            // --- Parse logic
            if (input == null || input.trim().isEmpty()) {
                // if the input is null or without spaces its empty (nothing but spaces)
                ui.showError("Invalid input. Please try again!");
                // keep looping until it no longer comes into this loop
                continue;
            }
            // if code gets here, we know that the input is valid
            // convert input to lowercase
            input = input.trim().toLowerCase();


            // if its a command, call state.appluCommand(player, command)
            Optional<Command> commandMaybe = getCommandFromInput(input);

            // SUPER IMPORTANT -- some powerups consume a "next turn" within themselves.
            // We make sure we don't call double next turn
            boolean mustCallNextTurn = false;

            // if there is something in commandMaybe, then its a command
            if (commandMaybe.isPresent()) {
                Command command = commandMaybe.get();
                currentPlayer = state.getCurrentPlayer();

                if (state.applyCommand(currentPlayer, command)) {
                    // don't call nextTurn here
                    ui.showGameState(state);
                }



            // if there is nothing in commandMaybe, then it's a MOVIE
            } else {
                Movie guessedMovie = database.getMovieByTitle(input);

                // if the guessedMovie is null, the movie doesn't exist in the database
                if (guessedMovie == null) {
                    ui.showError("That movie doesn't exist. Please try again!");
                    continue;
                }

                currentPlayer = state.getCurrentPlayer();
                // try building a Move with this Movie
                Optional<Move> move = state.tryBuildMove(currentPlayer, guessedMovie);
                Move newMove;
                // If it returns Optional.empty(), show error via
                // ui.showError("Invalid move. Try again.")
                if (!move.isPresent()) {
                    ui.showError("Invalid move. Please try again!");
                    continue;
                } else {
                    // if it IS present, then we can make newMove with this move
                    newMove = move.get();
                }
                // If present, continue, it means it's a valid move and the game
                // should continue like usual call this state.applyMove(move);
                state.applyMove(newMove);
                mustCallNextTurn = true;
                ui.showGameState(state);
            }




        }

        // if loop ends it means the game ended, and you do ui.showGameEnd(state.getWinner());
        ui.showGameEnd(state.getWinner());
    }


    // helper func to parse command from input string
    // I will use print statements here just for visibility
    // (it goes against checkstyle, but terminal is
    // separate from our game window anyway, so it won't be a nuisance
    private Optional<Command> getCommandFromInput(String input) {
        if (!input.startsWith("!")) {
            System.out.println("Not a command: " + input);
            return Optional.empty();
        }

        switch (input.toLowerCase()) {
            case "!skip":
                System.out.println("Parsed command: !skip");
                return Optional.of(new SkipCommand());
            case "!block":
                System.out.println("Parsed command: !block");
                return Optional.of(new BlockCommand());
            case "!escape":
                System.out.println("Parsed command: !escape");
                return Optional.of(new EscapeCommand(database));
            default:
                System.out.println("Unknown command: " + input);
                return Optional.empty();
        }
    }

}
