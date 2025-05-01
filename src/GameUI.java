import com.googlecode.lanterna.*;
import com.googlecode.lanterna.input.*;
import com.googlecode.lanterna.screen.*;
import com.googlecode.lanterna.terminal.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class GameUI {

    // fields - see Lanterna for details
    private Terminal terminal;
    private Screen screen;
    private TextColor foreground = TextColor.ANSI.WHITE;
    private TextColor background = TextColor.ANSI.BLACK;

    private ScheduledExecutorService scheduler;
    // we give them 30 seconds before time is up
    private int secondsRemaining = 30;
    private boolean timerRunning = true;


    // constructor
    public GameUI() throws IOException{
        // initialize the game ui

        // set up the terminal
        // set up the screen
        // set up the startScreen

        // make the scheduler do work
        // update timer display

    }

    // methods

    // print a certain string of text with the set columns and rows
    private void printString(int column, int row, String text){

    }

    // update the timer display with the time remainitng
    private void updateTimerDisplay() throws IOException{

    }

    // displays their win condition for the players to see, including
    // their current progress towards it
    public void displayWinCondition (Player player){

    }

    // show current GameState on our screen
    public void showGameState(GameState state) {
    }

    // shows current autocomplete suggestions (SEE LANTERNA)
    public void displayAutocompleteSuggestions (List<String> suggestions) throws IOException{

    }

    // prompt the player for an action
    public String promptPlayer(Player currentPlayer) {
        // prompt the player for action
        return null;
    }

    public void showGameEnd(Player winner) {

    }

    // this is so if the user types in something that doesn't connect
    // they get an error prompt
    public void showError(String message) throws IOException{
        // we were inspired from online forms where there would be
        // "please try again" in red letters when we have a typo or invalid input
    }


    // VERY IMPORTANT:
    // WE READ THE PLAYER INPUTS IN THIS METHOD :) and have it show LIVE (REAL TIME) on screen
    // (This is the only method in our code where we read player input)
    private String captureInput(int startCol, int row) throws IOException{
        // return the string in which the player typed in, so that we can
        // parse it and use it to create a Movie object and Move object

        // also have the user's entries show up LIVE on the screen (see Lanterna)
        return null;
    }


    // destructor! this cleans up the ui when the game is done
    public void closeUI() throws IOException{

        // set timerRunning to false
        // shutdown all the scheduler, screen, etc

        timerRunning = false;
        scheduler.shutdown();
        screen.stopScreen();
        terminal.close();
    }








}
