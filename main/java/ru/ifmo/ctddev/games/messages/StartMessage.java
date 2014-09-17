package ru.ifmo.ctddev.games.messages;

/**
 * Created by Aksenov239 on 28.08.2014.
 */
public class StartMessage {
    private boolean accepted;
    private int[][] field;

    public StartMessage() {
    }

    public StartMessage(boolean accepted, int[][] field) {
        super();
        this.accepted = accepted;
        this.field = field;
    }

    public boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public int[][] getField() {
        return field;
    }

    public void setField(int[][] field) {
        this.field = field;
    }

}
