package inbal.dolev.ticktacktoeinbal;

public class Player {

    private String uid;
    private String name;
    private String email;
    private int score;

    public Player(String uid) {
        this.uid = uid;
    }

    public Player(String uid, String name, String email, int score) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.score = score;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getScore() {
        return score;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
