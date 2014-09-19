package ru.ifmo.ctddev.games.messages;

/**
 * Created by Aksenov239 on 28.08.2014.
 */
public class MoveResponseMessage {

    private boolean success;
    private int[] layer;
    private int dx, dy;

    public MoveResponseMessage() {}

    public MoveResponseMessage(boolean success) {
        this.success = success;
        layer = null;
    }

    public MoveResponseMessage(boolean success, int[] layer, int dx, int dy) {
        this.success = success;
        this.layer = layer;
        this.dx = dx;
        this.dy = dy;
    }

    public void setSuccessful(boolean successful) {
        this.success = successful;
    }

    public boolean getSuccess() {
        return success;
    }

    public int[] getLayer() {
        return layer;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setLayer(int[] layer) {
        this.layer = layer;
    }

    public void setDx(int dx) {
        this.dx = dx;
    }

    public void setDy(int dy) {
        this.dy = dy;
    }
}
