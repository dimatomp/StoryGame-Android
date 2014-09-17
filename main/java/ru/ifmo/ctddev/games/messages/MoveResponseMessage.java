package ru.ifmo.ctddev.games.messages;

/**
 * Created by Aksenov239 on 28.08.2014.
 */
public class MoveResponseMessage {

    private boolean successful;
    private int[] layer;
    private int speed;
    private int direction;

    public MoveResponseMessage() {
    }

    public MoveResponseMessage(boolean successful, int[] layer, int speed, int direction) {
        super();
        this.successful = successful;
        this.layer = layer;
        this.speed = speed;
        this.direction = direction;
    }

    public boolean getSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public int[] getLayer() {
        return layer;
    }

    public void setLayer(int[] layer) {
        this.layer = layer;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }
}
