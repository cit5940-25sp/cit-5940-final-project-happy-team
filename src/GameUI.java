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


//    private ScheduledExecutorService scheduler;
//    // we give them 30 seconds before time is up
//    private int secondsRemaining = 30;
//    private boolean timerRunning = true;

    // constructor
    public GameUI(GameState gameState) throws IOException {
        this.gameState = gameState;

        // initialize the terminal and screen
        terminal = new DefaultTerminalFactory().createTerminal();
        screen = new TerminalScreen(terminal);
        screen.startScreen();

        // create GUI
        gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));

        buildUI();
        setUpListeners();
        showMainWindow();
        setUpTimer();
        startTurnTimer();
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

        mainWindow.setComponent(panel);
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
    private void showMainWindow() {
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
    public void displayWinCondition (Player player){
        gui.getGUIThread().invokeLater(() -> {
            // create panel for win condition display
            Panel winConditionPanel = new Panel();
            winConditionPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
            WinCondition wc = player.getWinCondition();

            // clear previous components if they exist
            winConditionPanel.removeAllComponents();

            // player name
            winConditionPanel.addComponent(new Label(player.getName() + "'s Win Condition")
                    .setForegroundColor(background));

            // progress bar
            String prog = wc.getDescription();
            winConditionPanel.addComponent(new Label(prog).setForegroundColor(foreground));

            Panel mainPanel = (Panel) mainWindow.getComponent();
            mainPanel.addComponent(winConditionPanel.setLayoutData(
                    GridLayout.createLayoutData(
                            GridLayout.Alignment.FILL, GridLayout.Alignment.BEGINNING,
                            true, false,
                            2, 1
                    )
            ));

            // refresh UI
            try {
                screen.refresh();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    // show current GameState on our screen
    public void showGameState(GameState state) {
        Player current = state.getCurrentPlayer();
        gui.getGUIThread().invokeLater(() -> {
            playerInfoLabel.setText(current.getName() + "'s turn - ");
            displayWinCondition(current);

            // update game history
            StringBuilder history = new StringBuilder("Recent movies: \n");
            List<Movie> recentMovies = state.getPlayedMoviesHistory();
            // display last 5 movies
            int start = Math.max(0, recentMovies.size() - 5);
            for (int i = start; i < recentMovies.size(); i++) {
                Movie movie = recentMovies.get(i);
                history.append(movie.getTitle()).append(" (").append(movie.getReleaseYear()).append(")\n");
            }
            historyLabel.setText(history.toString());

            // start timer
            startTurnTimer();
        });
    }

    // prompt the player for an action
    public String promptPlayer(Player currentPlayer) {
        // clear input box
        gui.getGUIThread().invokeLater(() -> {
            inputBox.setText("");
            inputBox.takeFocus();
        });

        try {
            screen.refresh();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // wait for player
        while (true) {
            // check for non-blocking
            KeyStroke keyStroke = null;
            try {
                keyStroke = screen.pollInput();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (keyStroke != null) {
                if (keyStroke.getKeyType() == KeyType.Enter) {
                    return inputBox.getText();
                } else if (keyStroke.getKeyType() == KeyType.Escape) {
                    return null;
                }
            }
            try {
                Thread.sleep(50); // to reduce CPU usage
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
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
            panel.addComponent(new Label("Final Score: " + winner.getScore())
                    .setLayoutData(GridLayout.createLayoutData(
                            GridLayout.Alignment.CENTER,
                            GridLayout.Alignment.CENTER,
                            true, true)));

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
    public void showError(String message) throws IOException{
        // we were inspired from online forms where there would be
        // "please try again" in red letters when we have a typo or invalid input
        gui.getGUIThread().invokeLater(() -> {
            // show error
            Label error = new Label(message).setForegroundColor(TextColor.ANSI.RED);

            // create popup
            Panel errorPanel = new Panel();
            errorPanel.setLayoutManager(new GridLayout(1));
            errorPanel.addComponent(error);
            errorPanel.addComponent(new Button("OK", () -> {
            }));

            BasicWindow errorWindow = new BasicWindow("Error");
            errorWindow.setComponent(errorPanel);
            errorWindow.setHints(Collections.singletonList(Window.Hint.CENTERED));
            gui.addWindow(errorWindow);
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
