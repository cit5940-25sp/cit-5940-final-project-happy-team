import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class PlayerTest {
    @Test
    public void intialize() {
        //given a new wincondition, player name
        WinCondition wc = new WinCondition(Move.ConnectionType.DIRECTOR,"Nolan",3);
        Player p = new Player("Charlie",wc);

        //player's name should match what we have
        assertEquals(
                "getName() should return the constructor argument",
                "Charlie",
                p.getName()
        );
        //starting score should be zero
        assertEquals(
                "New player's score must start at 0",
                0,
                p.getScore()
        );
        //wincondition description should match
        String expected = "Reach 3 movies of DIRECTOR: Nolan";
        assertEquals("WinCondition.getDescription() should reflect the type, value and target",
                expected,
                p.getWinCondition().getDescription());
    }

    @Test
    public void incrementScoreSHouldIncreaseScoreByOneEachCall() {
        Player p = new Player("Dana",new WinCondition(Move.ConnectionType.GENRE,
                "Action",2));
        p.incrementScore();
        assertEquals(
                "Score should be 1 after one increment",
                1,
                p.getScore()

        );
        p.incrementScore();
        assertEquals("Score should be 2 after two increments",
                2,p.getScore());
        p.incrementScore();
        assertEquals("Score should be 3 after three increments",
                3,p.getScore());
    }
}
