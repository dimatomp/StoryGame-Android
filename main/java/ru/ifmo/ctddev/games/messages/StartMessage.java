package ru.ifmo.ctddev.games.messages;

/**
 * Created by Aksenov239 on 28.08.2014.
 */
public class StartMessage {
    private boolean success;
    private int field[][];

    public StartMessage() {}

    public StartMessage(boolean success) {
        this.success = success;
        field = null;
    }

    public StartMessage(boolean success, int field[][]) {
        this.success = success;
        this.field = field;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int[][] getField() {
        return field;
    }

    public void setField(int[][] f) {
        field = f;
    }
}
