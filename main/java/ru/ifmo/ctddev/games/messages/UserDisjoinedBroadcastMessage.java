package ru.ifmo.ctddev.games.messages;

/**
 * Created by pva701 on 9/18/14.
 */
public class UserDisjoinedBroadcastMessage {
    private String userName;
    private int x, y;

    public UserDisjoinedBroadcastMessage() {}

    public UserDisjoinedBroadcastMessage(String userName, int x, int y) {
        this.userName = userName;
        this.x = x;
        this.y = y;
    }

    public String getUserName() {
        return userName;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
}
