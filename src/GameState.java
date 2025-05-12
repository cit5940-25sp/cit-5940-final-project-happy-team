import java.util.*;

public class GameState {

    // fields
    private MovieDatabase database;
    // tracks currently played (top of pile) movie
    private Movie currentMovie;
    private List<Player> players;
    private Set<Player> blockedPlayers = new HashSet<>();

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
    private List<Move> moveHistory = new ArrayList<>();


    // tracks the available power Ups
    // Player = player 1 or 2
    // List<command> = list of commands they have left (out of the 3)
    private final Map<Player, List<Command>> availablePowerUps = new HashMap<>();

    private int roundsPlayed = 0;

    private boolean timeExpired = false;


    // constructor
    public GameState(MovieDatabase database, List<Player> players){
        this.database = database;
        this.players = players;

        for (Player player : players) {
            // both player 1 and 2 have the same initial list of powerups
            // with block, skip, escape. one each
            List<Command> listOfPowerUps = new ArrayList<>();
            listOfPowerUps.add(new BlockCommand());
            listOfPowerUps.add(new SkipCommand());
            listOfPowerUps.add(new EscapeCommand(database));
            availablePowerUps.put(player,listOfPowerUps);
        }
        for (Move.ConnectionType ct : Move.ConnectionType.values()) {
            connectionUsage.put(ct, new HashMap<>());
        }
    }

    // starts the game by selecting an initial movie (randomly select from database)
    public void initialGameState(){
        // 1. Create "starting move"

        // select random movie from database
        Movie initialMovie = database.getRandomMovie();

        // set this initialMovie to be the current movie of this gamestate
        this.currentMovie = initialMovie;

        // add this movie to "used" lists
        usedMovieIds.add(initialMovie.getId());
        playedMoviesHistory.add(initialMovie);

        // I'm NOT going to create a Move for this initial movie, because it's technically not a Move (its not
        // player-made)
    }

    public List<Movie> getPlayedMoviesHistory() {
        return new ArrayList<>(playedMoviesHistory);
    }


    public MovieDatabase getDatabase() {
        return database;
    }


    // returns a Move (or empty) of a move that is valid for this movie and previous one
    // this validated Move will contain all the things we need to fill in the missing connection related values
    public Optional<Move> tryBuildMove(Player player, Movie nextMovie){
        // PRIORITIZES / checks for win condition first
        WinCondition wc = player.getWinCondition();
        if (currentMovie.hasConnection(wc.getType(), wc.getValue())
                && nextMovie.hasConnection(wc.getType(), wc.getValue())){

            Move candidate = new Move(player, nextMovie, wc.getType(), wc.getValue());
            if (isValidMove(candidate)) {
                return Optional.of(candidate);
            }
        }

        // otherwise search for any connection that works
        for(Move.ConnectionType eachType: Move.ConnectionType.values()){
            List<String> nextMovieConnections = nextMovie.getConnections(eachType);
            for (String value: nextMovieConnections){
                if (currentMovie.hasConnection(eachType, value)){
                    Move candidate = new Move (player, nextMovie, eachType, value);
                    if (isValidMove(candidate)){
                        return Optional.of(candidate);
                    }
                }
            }
        }

        // no valid move found
        return Optional.empty();
    }


    // determines if this current Move is valid, based on gamestate.
    // if already used, or if surpasses connection usage limit, or the connection has no match, then  = false
    public boolean isValidMove(Move move){
        // check if this move is a valid connection by comparing the current movie (prev) and the one from the move
        // that the player wants to play (getMoviePlayed from the Move)
        Movie prev = currentMovie;
        Movie next = move.getMoviePlayed();


        // Check usedMovieIds, connectionUsage limits, and actual connection validity

        // check if usedMovieIds already has this movie in it, then it's false, not valid Move
        if (usedMovieIds.contains(move.getMoviePlayed().getId())) {
            return false;
        }

        // if this EXACT CONNECTION has been used 3 times prior, this move is not valid
        Map<String, Integer> usageMap = connectionUsage.get(move.getConnectionType());
        int alreadyUsed = usageMap.getOrDefault(move.getConnectionValue(),0);
        if (alreadyUsed >= 3){
            return false;
        }


        // check if both THIS move and the previous move actually has this connection
        Move.ConnectionType cType = move.getConnectionType();
        String value = move.getConnectionValue();

        if (!prev.hasConnection(cType, value) || !next.hasConnection(cType, value)) {
            return false;
        }

        // NOTE: if isValidMove returns true, it means this Move will be used to make our next Move :)
        return true;
    }



    // if tryBuildMove was able to make a new valid Move, then this Move is "applied" to gameState
    // pass in the newly made Move that was output from tryBuildMove
    // also, if this Move counts for a player's winCondition, add progress to it
    public void applyMove(Move move){
        // update currentMovie
        // enter this movie into usedMovieIds
        // enter this movie into playedMoviesHistory
        // enter this Move into moveHistory
        // update the connectionUsage map to include/increment usage

        Map<String, Integer> usageType = connectionUsage.get((move.getConnectionType()));
        String value = move.getConnectionValue();
        int alreadyUsed = usageType.getOrDefault(value, 0);
        usageType.put(value, alreadyUsed + 1);

        // the currentMovie is updated to this new valid Move's
        this.currentMovie = move.getMoviePlayed();

        usedMovieIds.add(currentMovie.getId());
        playedMoviesHistory.add(currentMovie);
        moveHistory.add(move);


        // if this Move matches the player's win condition, add to this player's win condition progress
        Player currentPlayer = getCurrentPlayer();
        WinCondition wc = currentPlayer.getWinCondition();

        if (move.getConnectionType() == wc.getType() && move.getConnectionValue().equals(wc.getValue())) {
            wc.recordProgress();
        }
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



    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }





    public void setTimeExpired(boolean expired) {
        this.timeExpired = expired;
    }


    // check if Game has reached Over conditions
    // true if game is now over
    // false if game is not over and we should continue
    public boolean isGameOver(){
        // check if:
        // player winCondition is met
        // time has run out

        // if timer is out
        if (timeExpired){
            return true;    // game is over if time is expired
        }

        for (Player bothPlayers: players){
            if (bothPlayers.getWinCondition().isMet()){
                return true;
            }
        }

        return false;
    }


    // return the Player that is the winner
    public Player getWinner(){
        // return Player who's winCondition was met
        // or
        // return the Player who's timer has not run out
        // (aka the opponent of the "out" player)

        if (timeExpired){
            return getOpponentPlayer();
        }

        for (Player player : players){
            if (player.getWinCondition().isMet()){
                return player;
            }
        }

        return null;
    }




    /////////////// Powerup related methods ///////////////////


    // applies this certain command to the gamestate
    // how this gets executed, and how it changes the gamestate, we will
    // fill it out in Command (pass in this gamestate so it can change it)
    public boolean applyCommand(Player player, Command cmd){
       List<Command> available = availablePowerUps.get(player);
       // if the list is not empty and this cmd is able to be removed
        if (available != null) {
            for (Command c : available) {
                if (c.getClass().equals(cmd.getClass())) {
                    available.remove(c);
                    c.execute(this);
                    System.out.println("Command " + c.getClass().getSimpleName() + " applied.");
                    return true;
                }
            }
        }
        System.out.println("Command not found or already used.");
        return false;

    }

    // not suppppperrrrr necessary, but a getter for:
    // return a list of still available Commands/powerups (that we can't touch, unmodifiable) for this player
    public List<Command> getPowerUpsFor (Player player){
        return Collections.unmodifiableList(availablePowerUps.getOrDefault(player, Collections.emptyList())
        );
    }

    public void skipPlayer() {
        nextTurn(); //go to next player
    }

    // block player is used in Command (block) to block opposite player
    public void blockPlayer(Player player) {
        //mark the player as blocked
        blockedPlayers.add(player);

        //if the blocked player is about to play, skip them
        Player curr = players.get(currentPlayerIndex);
        if (curr.equals(player)) {
            blockedPlayers.remove(player); //clear block
            nextTurn(); //skip to other player
        }
    }

    // used when setting current movie (like in escapeCommand)
    public void setCurrentMovie(Movie currentMovie) {

        System.out.println("setCurrentMovie called with: " + currentMovie.getTitle());

        this.currentMovie = currentMovie;
        playedMoviesHistory.add(currentMovie);




    }

    // used when i need to quickly get the opponent player to the current player
    public Player getOpponentPlayer() {
        return players.get((currentPlayerIndex + 1) % players.size());
    }


    public int getRoundsPlayed() {
        return roundsPlayed;
    }
}
