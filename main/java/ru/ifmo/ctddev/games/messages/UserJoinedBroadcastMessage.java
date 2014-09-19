package ru.ifmo.ctddev.games.messages;

/**
 * Created by pva701 on 9/18/14.
 */
public class UserJoinedBroadcastMessage {
    private String userName;
    private int x, y;

    public UserJoinedBroadcastMessage() {}

    public UserJoinedBroadcastMessage(String userName, int y, int x) {
        this.userName = userName;
        this.y = y;
        this.x = x;
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
