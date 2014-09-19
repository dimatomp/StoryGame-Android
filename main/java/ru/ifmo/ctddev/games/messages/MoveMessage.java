package ru.ifmo.ctddev.games.messages;

/**
 * Created by Aksenov239 on 28.08.2014.
 */
public class MoveMessage {
    private int dx;
    private int dy;

    public MoveMessage() {
    }

    public MoveMessage(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int getDx() {
        return dx;
    }
    public int getDy() {
        return dy;
    }

    public void setDx(int dx) {
        this.dx = dx;
    }

    public void setDy(int dy) {
        this.dy = dy;
    }
}
