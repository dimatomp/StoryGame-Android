package ru.ifmo.ctddev.games.messages;

/**
 * Created by pva701 on 9/18/14.
 */
public class MoveBroadcastMessage {
    private String userName;
    int x, y;
    int dx, dy;

    public MoveBroadcastMessage(String userName, int x, int y, int dx, int dy) {
        this.userName = userName;
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
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

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
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

    public void setDx(int dx) {
        this.dx = dx;
    }

    public void setDy(int dy) {
        this.dy = dy;
    }
}
