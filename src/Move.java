public class Move
{

   // fields
    private Player player;
    private Movie moviePlayed;
    private String connectionValue;
    private long timestamp;

    // we are using this ConnectionType in multiple other classes.
    // to access, Move.ConnectionType
    public enum ConnectionType {
        GENRE, ACTOR, DIRECTOR, WRITER, CINES, COMPOSER
    }


    private ConnectionType connectionType;

    // constructor for a Move object
    // represents a Player's Move
    public Move(Player player, Movie moviePlayed, ConnectionType connectionType, String connectionValue){
        // we dont fill in the timestamp when we create the obj, because the timestamp is set to whatever
        // time is current time and in GameContoller and gamestate when we determine
        // if a move has passed time limit, we use this

        this.player = player;
        this.moviePlayed = moviePlayed;
        this.connectionValue = connectionValue;
        this.timestamp = System.currentTimeMillis();
        this.connectionType = connectionType;
    }


    // getters
    public long getTimestamp() {
        return timestamp;
    }


    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public Movie getMoviePlayed() {
        return moviePlayed;
    }

    public Player getPlayer() {
        return player;
    }

    public String getConnectionValue() {
        return connectionValue;
    }



    // setters (maybe just for connection type and connection value?)
        // the setter for connectionType will be used in gamecontroller/gamestate when we update
        // the actual connection type that this Move creates

        // we also need a setter for connection value, because similarly, in gamecontroller/gamestate
        // we will know what actor/genre/director etc is what connection

    public void setConnectionValue(String connectionValue) {
        this.connectionValue = connectionValue;
    }

    public void setConnectionType(ConnectionType connectionType){
        this.connectionType = connectionType;
    }

    // * NOTE: MAKE SURE WE NEVER TOUCH THE CONNECTIONVALUE AND CONNECTIONTYPE of the Moves
    // prior to this Move during game

    // toDisplayString (this is an encapsulation of all the info we need to display from a
    // move when we are in the gameUI, to let the opponent see what you put down)
    @Override
    public String toString(){
        // will look kinda like this "Player 1 plays Titanic via ACTOR (Leonardo DiCaprio)"
        return player.getName() + " plays " + moviePlayed.getTitle() + " via " + connectionType + " (" + connectionValue + ")";
    }




}

