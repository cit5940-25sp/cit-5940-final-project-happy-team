import java.util.*;

public class GameState {
    private MovieDatabase database;
    // tracks currently played (top of pile) movie
    private Movie currentMovie;
    private List<Player> players;

    // it's either the player or the computer
    private int currentPlayerIndex = 0;
    // track the ids (aka movies) that were already played
    private Set<Integer> usedMovieIds = new HashSet<>();

    // this tracks the number of times that this type of connection
    // has been used in this game.
    // String = connection type
    // Integer = how many times this connection types has been used so far
    // we need to fill out this map with our different connection types and their initial values (0)
    private Map<Move.ConnectionType, Map<String,Integer>> connectionUsage = new HashMap<>();

    private List<Movie> playedMoviesHistory = new ArrayList<>();

    // tracks the available power Ups
    // Player = player 1 or 2
    // List<command> = list of commands they have left (out of the 3)
    private final Map<Player, List<Command>> availablePowerUps = new HashMap<>();

    private int roundsPlayed = 0;



    // constructor
    public GameState(MovieDatabase database, List<Player> players){
        this.database = database;
        this.players = players;

        BlockCommand powerupBlock = new BlockCommand();
        SkipCommand powerupSkip = new SkipCommand();
        EscapeCommand powerupEscape = new EscapeCommand();

        // both player 1 and 2 have the same initial list of powerups
        // with block, skip, escape. one each
        List<Command> listOfPowerUps = new ArrayList<>();
        listOfPowerUps.add(powerupBlock);
        listOfPowerUps.add(powerupSkip);
        listOfPowerUps.add(powerupEscape);

        for (Player player: players){
            // since the constructor initializes a GameState obj (only happens once)
            // we can fill out the initial state of the array
            availablePowerUps.put(player, listOfPowerUps);
        }

        for (Move.ConnectionType ct : Move.ConnectionType.values()){
            connectionUsage.put(ct, new HashMap<>());
        }

    }

    // starts the game by selecting an initial movie (randomly select from database)
    public void initialGameState(){
        // 1. Create "starting move"
        // select movie from database
        // create Movie object
        // make a Move object from the Movie information. Not yet assigned connection info
        // mark this movie as used (put that movie into usedMovieIds and playedMoviesHistory)
    }


    // determines if this current Move is valid, based on gamestate.
    // if used, or if surpasses connection usage limit, or the connection has no match
    // then false
    public boolean isValidMove(Move move){
        // Check usedMovieIds, connectionUsage limits, and actual connection validity
        // for connectionUsage -- if the Integer

        Map<String, Integer> type = connectionUsage.get(move.getConnectionType());
        int alreadyUsed = type.getOrDefault(move.getConnectionValue(),0);
        if (alreadyUsed >= 3){
            // if this EXACT CONNECTION has been used 3 times prior, this move is not valid
            return false;
        }

        return true;
    }

    // if isValidMove, then this Move is "applied"
    public void applyMove(Move move){
        // update currentMovie
        // enter this movie into usedMovieIds
        // enter this movie into playedMoviesHistory
        // update the connectionUsage map to include/increment usage

        Map<String, Integer> type = connectionUsage.get((move.getConnectionType()));
        String value = move.getConnectionValue();
        int alreadyUsed = type.getOrDefault(value, 0);
        type.put(value, alreadyUsed + 1);


        this.currentMovie = move.getMoviePlayed();
        usedMovieIds.add(currentMovie.getId());
        playedMoviesHistory.add(currentMovie);

    }


    // moves the currentPlayerIndex to the next one (in our case, it bounces between 2 players)
    // advances the num of rounds played, because if nextTurn, then its a new round
    public void nextTurn(){
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        roundsPlayed++;
    }

    // return the Player (player object) that is the current player
   public Player getCurrentPlayer(){
        return players.get(currentPlayerIndex);
   }


   // check if Game has reached Over conditions
    public boolean isGameOver(){
        // check if:
        // player winCondition is met
        // time has run out
        return false;
    }

    public Player getWinner(){
        // return Player who's winCondition was met
        // or
        // return the Player who's timer has not run out
        // (aka the opponent of the "out" player)
        return null;
    }




    /////////////// Powerup related methods ///////////////////


    // applies this certain command to the gamestate
    // how this gets executed, and how it changes the gamestate, we will
    // fill it out in Command (pass in this gamestate so it can change it)
    public boolean applyCommand(Player player, Command cmd){
       List<Command> available = availablePowerUps.get(player);
       // if the list is not empty and this cmd is able to be removed
       if (available != null && available.remove(cmd)){
           // execute it
           cmd.execute(this);
           return true;
       }
       return false;
    }

    // not suppppperrrrr necessary, but a getter for:
    // return a list of still available Commands/powerups (that we can't touch, unmodifiable) for this player
    public List<Command> getPowerUpsFor (Player player){
        return Collections.unmodifiableList(availablePowerUps.getOrDefault(player, Collections.emptyList())
        );
    }













    // method that fills out and then keeps track of connectionUsage







}
