import com.googlecode.lanterna.*;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.input.*;
import com.googlecode.lanterna.screen.*;
import com.googlecode.lanterna.terminal.*;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class GameUI {

    // fields - see Lanterna for details
    private Terminal terminal;
    private Screen screen;
    private MultiWindowTextGUI gui;
    private Window mainWindow;
    private TextBox inputBox;
    private ActionListBox suggestions;
    private Label timerLabel;
    private Label playerInfoLabel;
    private Label historyLabel;
    private TextColor foreground = TextColor.ANSI.WHITE;
    private TextColor background = TextColor.ANSI.BLACK;

    private Timer gameTimer;
    private Clock gameClock;
    private AtomicInteger secondsRemaining = new AtomicInteger(30);
    private volatile boolean timerRunning = false;
    private Instant turnStartTime;

    private Autocomplete ac;
    private GameState gameState;

    private Label player1WinConditionLabel;
    private Label player2WinConditionLabel;
    private Label player1PowerupsLabel;
    private Label player2PowerupsLabel;



//    private ScheduledExecutorService scheduler;
//    // we give them 30 seconds before time is up
//    private int secondsRemaining = 30;
//    private boolean timerRunning = true;

    // constructor
    public GameUI(GameState gameState) throws IOException {
        this.gameState = gameState;

        // initialize the terminal and screen
        //terminal = new DefaultTerminalFactory().createTerminal();


        terminal = new DefaultTerminalFactory()
                .setTerminalEmulatorTitle("Movie Name Game")
                .setForceTextTerminal(false)
                .setPreferTerminalEmulator(true)
                .createTerminal();

        screen = new TerminalScreen(terminal);
        screen.startScreen();

        // create GUI
        gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));

        buildUI();
        setUpListeners();
        //showMainWindow();
        setUpTimer();
        //startTurnTimer();
    }


    public MultiWindowTextGUI getGui() {
        return gui;
    }

    public Window getMainWindow() {
        return mainWindow;
    }


    public void setAutocomplete(Autocomplete ac) {
        this.ac = ac;
    }


    private void buildUI() {
        // create main window
        mainWindow = new BasicWindow("Movie Name Game");
        mainWindow.setHints(Arrays.asList(Window.Hint.FULL_SCREEN, Window.Hint.NO_DECORATIONS));

        // create panel with layout
        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(2));

        // player info, timer, history labels
        playerInfoLabel = new Label("Waiting for player...");
        panel.addComponent(playerInfoLabel);
        timerLabel = new Label("30 seconds remaining");
        panel.addComponent(timerLabel);

        player1WinConditionLabel = new Label("Player 1 Win Condition will appear here")
                .setForegroundColor(TextColor.ANSI.GREEN);
        player2WinConditionLabel = new Label("Player 2 Win Condition will appear here")
                .setForegroundColor(TextColor.ANSI.GREEN);

        panel.addComponent(player1WinConditionLabel);
        panel.addComponent(player2WinConditionLabel);

        historyLabel = new Label("Game history will appear here");
        panel.addComponent(historyLabel).setLayoutData(GridLayout.createLayoutData(
                GridLayout.Alignment.FILL, GridLayout.Alignment.FILL, true,
                true, 2, 1));

        // input box
        inputBox = new TextBox(new TerminalSize(30, 1));
        panel.addComponent(new Label("Enter movie title:"));
        panel.addComponent(inputBox);

        // sugg box
        suggestions = new ActionListBox(new TerminalSize(30, 5));
        panel.addComponent(new Label("Suggestions:"));
        panel.addComponent(suggestions);


        player1PowerupsLabel = new Label("Player 1 Powerups:").setForegroundColor(TextColor.ANSI.YELLOW);
        player2PowerupsLabel = new Label("Player 2 Powerups:").setForegroundColor(TextColor.ANSI.YELLOW);

        panel.addComponent(player1PowerupsLabel);
        panel.addComponent(player2PowerupsLabel);


        mainWindow.setComponent(panel);
        inputBox.takeFocus();
    }





    // update player input live
    private void setUpListeners() {
        inputBox.setTextChangeListener((newText, changedByUser) -> {
            if (changedByUser && ac != null) {
                displayAutocompleteSuggestions(
                        ac.getSuggestions(newText, 5)
                );
            }
        });
    }
    public void showMainWindow() {
        gui.addWindowAndWait(mainWindow);
    }

    private void setUpTimer() {
        // Timer and clock
        gameTimer = new Timer(true);
        gameClock = Clock.systemDefaultZone();
    }

    // methods

    // control timer
    public void startTurnTimer() {
        timerRunning = true;
        secondsRemaining.set(30);
        turnStartTime = Instant.now(gameClock);

        gameTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (!timerRunning) {
                    this.cancel();
                    return;
                }
                Duration timeElapsed = Duration.between(turnStartTime, Instant.now(gameClock));
                int remaining = (int) (30 - timeElapsed.getSeconds());
                secondsRemaining.set(Math.max(remaining, 0));

                gui.getGUIThread().invokeLater(() -> {
                    timerLabel.setText("Time remaining: " + secondsRemaining.get());
                    if (secondsRemaining.get() <= 0) {
                        timerRunning = false;
                        handleTimeUp();
                    }
                });
            }
        }, 0, 1000); // update every second
    }

    private void handleTimeUp() {
        gui.getGUIThread().invokeLater(() -> {
            // Display time's up
            timerLabel.setText("Time's up!");
            gameState.setTimeExpired(true);

            // immediately show winner (the opponent)
            Player winner = gameState.getOpponentPlayer();
            showGameEnd(winner);
        });
    }

    public void stopTimer() {
        timerRunning = false;
    }

    // display autocomplete suggestions
    public void displayAutocompleteSuggestions(final List<String> allSuggestions) {
        gui.getGUIThread().invokeLater(() -> {
            suggestions.clearItems();
            if (!allSuggestions.isEmpty()) {
                String first = allSuggestions.get(0);
                suggestions.addItem(first, () -> useSuggestion(first));
                allSuggestions.stream().skip(1).forEach(s -> suggestions.addItem(s, () -> useSuggestion(s)));
            }
        });
    }

    private void useSuggestion(String sugg) {
        inputBox.setText(sugg);
        inputBox.takeFocus();
    }

//    // print a certain string of text with the set columns and rows
//    private void printString(int column, int row, String text){
//
//    }
//
//    // update the timer display with the time remaining
//    private void updateTimerDisplay() throws IOException{
//
//    }
//
//    // displays their win condition for the players to see, including
//    // their current progress towards it
public void displayAllWinConditions(List<Player> players) {
    gui.getGUIThread().invokeLater(() -> {
        // Create a panel that spans both columns to hold all win conditions
        Panel allWinConditionsPanel = new Panel();
        allWinConditionsPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));

        for (Player player : players) {
            WinCondition wc = player.getWinCondition();
            String title = player.getName() + "'s Win Condition: " + wc.getType() + " – " + wc.getValue();
            String progress = "Progress: " + wc.getProgress() + "/" + wc.getTarget();

            Label titleLabel = new Label(title).setForegroundColor(TextColor.ANSI.GREEN);
            Label progressLabel = new Label(progress).setForegroundColor(TextColor.ANSI.BLACK);

            Panel singleWinPanel = new Panel();
            singleWinPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
            singleWinPanel.addComponent(titleLabel);
            singleWinPanel.addComponent(progressLabel);

            allWinConditionsPanel.addComponent(singleWinPanel);
        }

        // Add to main window
        Panel mainPanel = (Panel) mainWindow.getComponent();
        mainPanel.addComponent(allWinConditionsPanel.setLayoutData(
                GridLayout.createLayoutData(
                        GridLayout.Alignment.FILL,
                        GridLayout.Alignment.BEGINNING,
                        true, false,
                        2, 1
                )
        ));

        try {
            screen.refresh();
        } catch (IOException e) {
            e.printStackTrace();
        }
    });
}

    public void showGameState(GameState state) {
        Player currentPlayer = state.getCurrentPlayer();
        Player opponentPlayer = state.getOpponentPlayer(); // Assuming this method exists

        gui.getGUIThread().invokeLater(() -> {
            // Update player turn info
            playerInfoLabel.setText(currentPlayer.getName() + "'s turn");

            // Update both players' win condition displays
            updateWinConditionDisplay(player1WinConditionLabel, state.getPlayers().get(0));
            updateWinConditionDisplay(player2WinConditionLabel, state.getPlayers().get(1));



            // Update game history
            StringBuilder history = new StringBuilder("Recent movies: \n");
            List<Movie> recentMovies = state.getPlayedMoviesHistory();
            // display last 5 movies
            int start = Math.max(0, recentMovies.size() - 5);
            for (int i = start; i < recentMovies.size(); i++) {
                Movie movie = recentMovies.get(i);
                history.append(movie.getTitle()).append(" (").append(movie.getReleaseYear()).append(")\n");
            }

            // show last connection made
            List<Move> moveHistory = state.getMoveHistory();
            if (!moveHistory.isEmpty()) {
                Move lastMove = moveHistory.get(moveHistory.size() - 1);
                String connectionType = lastMove.getConnectionType().toString().toLowerCase();
                String connectionValue = lastMove.getConnectionValue();
                history.append("Last connection: ").append(connectionType).append(" – ").append(connectionValue).append("\n");
            }

            historyLabel.setText(history.toString());


            List<Player> players = state.getPlayers();

            List<Command> p1Commands = state.getPowerUpsFor(players.get(0));
            if (p1Commands.isEmpty()) {
                player1PowerupsLabel.setText("Player 1 Powerups: none");
                player1PowerupsLabel.setForegroundColor(TextColor.ANSI.BLACK);
            } else {
                StringBuilder p1Powerups = new StringBuilder("Player 1 Powerups: ");
                for (Command cmd : p1Commands) {
                    p1Powerups.append("!").append(cmd.getClass().getSimpleName().replace("Command", "").toLowerCase()).append(" ");
                }
                player1PowerupsLabel.setText(p1Powerups.toString());
                player1PowerupsLabel.setForegroundColor(TextColor.ANSI.YELLOW);
            }


            List<Command> p2Commands = state.getPowerUpsFor(players.get(1));
            if (p2Commands.isEmpty()) {
                player2PowerupsLabel.setText("Player 2 Powerups: none");
                player2PowerupsLabel.setForegroundColor(TextColor.ANSI.BLACK);
            } else {
                StringBuilder p2Powerups = new StringBuilder("Player 2 Powerups: ");
                for (Command cmd : p2Commands) {
                    p2Powerups.append("!").append(cmd.getClass().getSimpleName().replace("Command", "").toLowerCase()).append(" ");
                }
                player2PowerupsLabel.setText(p2Powerups.toString());
                player2PowerupsLabel.setForegroundColor(TextColor.ANSI.YELLOW);
            }







            // Always restart the timer on a new turn
            stopTimer();
            startTurnTimer();


            inputBox.takeFocus();
        });
    }


    // Helper method to update a win condition label
    private void updateWinConditionDisplay(Label label, Player player) {
        WinCondition wc = player.getWinCondition();
        String type = wc.getType().toString();
        String value = wc.getValue();
        int progress = wc.getProgress();
        int required = wc.getTarget();

        label.setText(player.getName() + "'s Win Condition: " + type + " – " + value
                + " | Progress: " + progress + "/" + required);
    }





    public String promptPlayer(Player currentPlayer) {
        // clear input box
        gui.getGUIThread().invokeLater(() -> {
            inputBox.setText("");
            inputBox.takeFocus();
            System.out.println("Input box cleared and focused for " + currentPlayer.getName());
        });

        try {
            screen.refresh();
            System.out.println("Screen refreshed, waiting for input...");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create a final container for the result
        final String[] result = new String[1];
        final boolean[] done = new boolean[1];

        // Set up a special listener for the Enter key
        inputBox.setInputFilter((interactionInfo, keyStroke) -> {
            if (keyStroke.getKeyType() == KeyType.Enter) {
                // Capture the current text when Enter is pressed
                result[0] = inputBox.getText();
                done[0] = true;
                System.out.println("Enter pressed! Input text: " + result[0]);
                return false; // Don't pass Enter to the text box
            }
            // For EOF signals, treat them as possible Enter keys
            if (keyStroke.getKeyType() == KeyType.EOF) {
                System.out.println("EOF detected, checking if it's Enter");

                // If there's text in the input box, treat EOF as Enter
                if (inputBox.getText() != null && !inputBox.getText().isEmpty()) {
                    result[0] = inputBox.getText();
                    done[0] = true;
                    System.out.println("Treating EOF as Enter. Input text: " + result[0]);
                    return false;
                }
            }
            return true; // Pass other keys to the text box
        });

        // Wait for the result
        while (!done[0]) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // Remove the special input filter when done
        inputBox.setInputFilter(null);

        return result[0];
    }

    public void showGameEnd(Player winner) {
        stopTimer();

        gui.getGUIThread().invokeLater(() -> {
            // create modal dialog
            Panel panel = new Panel();
            panel.setLayoutManager(new GridLayout(1));

            // announce winner
            panel.addComponent(new Label(winner.getName() + " wins the game!")
                    .setLayoutData(GridLayout.createLayoutData(
                            GridLayout.Alignment.CENTER,
                            GridLayout.Alignment.CENTER,
                            true, true)));

            // game summary
//            panel.addComponent(new Label("Final Score: " + winner.getScore())
//                    .setLayoutData(GridLayout.createLayoutData(
//                            GridLayout.Alignment.CENTER,
//                            GridLayout.Alignment.CENTER,
//                            true, true)));

            // play again button
            Button quitButton = new Button("Quit", () -> {
                try {
                    closeUI();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            panel.addComponent(quitButton);

            // display the dialog
            BasicWindow endWindow = new BasicWindow("Game Over");
            endWindow.setComponent(panel);
            endWindow.setHints(Collections.singletonList(Window.Hint.CENTERED));
            gui.addWindow(endWindow);
        });
    }

//     this is so if the user types in something that doesn't connect
//     they get an error prompt
public void showError(String message) throws IOException {
    // IMPORTANT: Don't stop or reset the timer when showing an error
    gui.getGUIThread().invokeLater(() -> {
        // Show error
        Label error = new Label(message).setForegroundColor(TextColor.ANSI.RED);

        // Create popup
        Panel errorPanel = new Panel();
        errorPanel.setLayoutManager(new GridLayout(1));
        errorPanel.addComponent(error);

        // Create the window first so we can reference it in the button action
        BasicWindow errorWindow = new BasicWindow("Error");

        // Create OK button with action to close the window
        Button okButton = new Button("OK", () -> {
            gui.removeWindow(errorWindow);
            try {
                // Refresh the screen but DON'T call showGameState or reset timer
                screen.refresh();
                // Ensure input focus returns to the main input box
                inputBox.takeFocus();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        errorPanel.addComponent(okButton);

        // Set window properties
        errorWindow.setComponent(errorPanel);
        errorWindow.setHints(Collections.singletonList(Window.Hint.CENTERED));

        // Add window to GUI
        gui.addWindow(errorWindow);

        // Give focus to OK button
        okButton.takeFocus();

        // Ensure screen updates
        try {
            screen.refresh();
        } catch (IOException e) {
            e.printStackTrace();
        }
    });
}


    // VERY IMPORTANT:
    // WE READ THE PLAYER INPUTS IN THIS METHOD :) and have it show LIVE (REAL TIME) on screen
    // (This is the only method in our code where we read player input)
//    private String captureInput(int startCol, int row) throws IOException{
//        // return the string in which the player typed in, so that we can
//        // parse it and use it to create a Movie object and Move object
//
//        // also have the user's entries show up LIVE on the screen (see Lanterna)
//        return null;
//    }


    // destructor! this cleans up the ui when the game is done
    public void closeUI() throws IOException{
        stopTimer();
        gameTimer.cancel();
        // set timerRunning to false
        timerRunning = false;

        // shutdown all the scheduler, screen, etc
        screen.stopScreen();
        terminal.close();
    }

   /* public static void main(String[] args) {
        try {
            List<String> movies = Arrays.asList("Avengers", "Divergent", "Avatar", "Lion King");
            Autocomplete ac = new Autocomplete(movies);
            GameUI ui = new GameUI();
            ui.ac = ac;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

}
