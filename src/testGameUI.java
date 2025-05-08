import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import java.io.IOException;

public class testGameUI {
    public static void main(String[] args) {
        try {
            MovieDatabase testDB = new MovieDatabase();
            GameState testState = new GameState();
            Player testPlayer = new Player("Player 1");
            System.out.println("Starting GameUI");

            GameUI testUI = new GameUI(testDB);
            System.out.println("Testing showGameState");
            ui.showGameState(testState);

            String userInput = ui.promptPlayer(testPlayer);
            System.out.println("You entered: " + userInput);

            ui.showError("Error message test");

            ui.showGameEnd(testPlayer);

            Thread.sleep(50);

            ui.closeUI();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
