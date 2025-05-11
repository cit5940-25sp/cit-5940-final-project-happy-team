import org.junit.Test;

import static org.junit.Assert.*;


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
    @Test
    public void testPlayerWithSameWinCOndition() {
        //test two players wiht the same win condition type but different values
        WinCondition horr = new WinCondition(Move.ConnectionType.GENRE, "Horror", 3);
        WinCondition actionCondition = new WinCondition(Move.ConnectionType.GENRE, "Action",3);

        Player p1 = new Player("Player1", horr);
        Player p2 = new Player("Player2", actionCondition);
        //both should have the same type of win condition
        assertEquals("Both palyers should hvae smae win condition type",
                p1.getWinCondition().getType(), p2.getWinCondition().getType());

        //But different values
        assertNotEquals("Players should have different win condition values",
                p1.getWinCondition().getValue(), p2.getWinCondition().getValue());
        //but different values
        assertNotEquals("Players should have different win condition values",
                p1.getWinCondition().getValue(), p2.getWinCondition().getValue());
        //scores should be tracked separately
        p1.incrementScore();
        p1.incrementScore();
        p1.incrementScore(); //player 1 has reached their win condition

        p2.incrementScore(); //player 2 has not
        assertEquals("Player 1 should have score 3", 3, p1.getScore());
        assertEquals("Player 2 should have score 1", 1, p2.getScore());

        assertTrue("Player 1's win condition should be met",
                p1.getWinCondition().isMet());
        assertFalse("Player 2's win condition should be met",
                p2.getWinCondition().isMet());

    }
}
