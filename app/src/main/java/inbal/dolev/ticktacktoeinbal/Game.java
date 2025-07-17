package inbal.dolev.ticktacktoeinbal;



public class Game {
    private String winner;
    private String timestamp;

    public Game() {
        // Default constructor required for calls to DataSnapshot.getValue(Game.class)
        this.winner = "";
        this.timestamp = "";
    }

    public Game(String winner, String timestamp) {
        this.winner = winner;
        this.timestamp = timestamp;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

