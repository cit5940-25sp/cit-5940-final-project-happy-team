import com.googlecode.lanterna.*;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.ListBox;
import com.googlecode.lanterna.input.*;
import com.googlecode.lanterna.screen.*;
import com.googlecode.lanterna.terminal.*;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class GameUI {

    // fields - see Lanterna for details
    private Terminal terminal;
    private Screen screen;
    private MultiWindowTextGUI gui;
    private Window mainWindow;
    private TextBox inputBox;
    private ListBox<String> suggestions;
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

//    private ScheduledExecutorService scheduler;
//    // we give them 30 seconds before time is up
//    private int secondsRemaining = 30;
//    private boolean timerRunning = true;

    // constructor
    public GameUI() throws IOException{
        // initialize the terminal and screen
        terminal = new DefaultTerminalFactory().createTerminal();
        screen = new TerminalScreen(terminal);
        screen.startScreen();

        // create GUI
        gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));

        // create main window
        mainWindow = new BasicWindow("Movie Name Game");
        mainWindow.setHints(List.of(Window.Hint.FULL_SCREEN, Window.Hint.NO_DECORATIONS));

        // create panel with layout
        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(2));

        // add components
        playerInfoLabel = new Label("30 seconds remaining");
        panel.addComponent(timerLabel);
        historyLabel = new Label("Game history will appear here");
        panel.addComponent(historyLabel.setLayoutData(GridLayout.createLayoutData(
                GridLayout.Alignment.FILL, GridLayout.Alignment.FILL, true,
                true, 2, 1)));

        inputBox = new TextBox(new TerminalSize(30, 1));
        panel.addComponent(new Label("Enter movie title: "));
        panel.addComponent(inputBox);

        suggestions = new ListBox<>();
        panel.addComponent(new Label("Suggestions: "));
        panel.addComponent(suggestions);

        // update player input live
        inputBox.setTextChangeListener((newText, changedByUser) -> {
            if (changedByUser) {
                displayAutocompleteSuggestions(
                        database.searchByTitlePrefix(newText)
                );
            }
        })

        mainWindow.setComponent(panel);
        gui.addWindowAndWait(mainWindow);

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
                Duration timeElapsed = Duration.between(turnStartTime. Instant.now(gameClock));
                int remaining - (int) (30 - timeElapsed.getSeconds());
                secondsRemaining.set(remaining > 0 ? remaining : 0);

                try {
                    gui.getGUIThread().invokeLater(() -> {
                        timerLabel.setText("Time remaining: " + secondsRemaining.get());
                        if (secondsRemaining.get() <= 0) {
                            timerRunning = false;
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000); // update every second
    }

    // display autocomplete suggestions
    public void displayAutocompleteSuggestions(final List<String> allSuggestions) {
        gui.getGUIThread().invokeLater(() -> {
            suggestions.clearItems();
            allSuggestions.forEach(suggestionsList::addItem);
            if(!allSuggestions.isEmpty()) {
                allSuggestions.setSelectedIndex(0);
            }
        });
    }

//    // print a certain string of text with the set columns and rows
//    private void printString(int column, int row, String text){
//
//    }
//
//    // update the timer display with the time remainitng
//    private void updateTimerDisplay() throws IOException{
//
//    }
//
//    // displays their win condition for the players to see, including
//    // their current progress towards it
//    public void displayWinCondition (Player player){
//
//    }

    // show current GameState on our screen
    public void showGameState(GameState state) {
        Player current = state.getCurrentPlayer();
        gui.getGUIThread().invokeLater(() -> {
            playerInfoLabel.setText(current.getName() + "'s turn - " + current.getWinCondition().getDescription());

            // update game history
            StringBuilder history = new StringBuilder("Recent movies: \n");
            List<Movie> recentMovies = state.getPlayedMoviesHistory();
            // display last 5 movies
            int start = Math.max(0, historyMovies.size() - 5);
            for (int i = start, i < recentMovies.size(); i++) {
                Movie movie = historyMovies.get(i);
                history.append(movie.getTitle()).appeand(" (").append(movie.getReleaseYear()).append(")\n");
            }
            historyLabel.setText(history.toString());

            // start timer
            startTurnTimer();
        });
    }

    // prompt the player for an action
    public String promptPlayer(Player currentPlayer) {
        // clear input box
        inputBox.setText("");
        inputBox.takeFocus();
        screen.refresh();

        // wait for player to enter
        while (true) {
            // non-blocking check
            KeyStroke keyStroke = screen.pollInput();

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
            endWindow.setHints(List.of(Window.Hint.CENTERED));
            gui.addWindow(endWindow);
        });
    }

    // this is so if the user types in something that doesn't connect
    // they get an error prompt
//    public void showError(String message) throws IOException{
//        // we were inspired from online forms where there would be
//        // "please try again" in red letters when we have a typo or invalid input
//    }


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








}
