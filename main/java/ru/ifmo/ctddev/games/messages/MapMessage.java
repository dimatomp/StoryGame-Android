package ru.ifmo.ctddev.games.messages;

/**
 * Created by pva701 on 9/17/14.
 */
public class MapMessage {
    private int[][] field;

    public MapMessage() {}

    public MapMessage(int[][] field) {
        this.field = field;
    }

    public int[][] getField() {
        return field;
    }

    public void setField(int[][] field) {
        this.field = field;
    }
}
