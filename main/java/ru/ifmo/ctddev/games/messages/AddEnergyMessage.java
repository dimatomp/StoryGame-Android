package ru.ifmo.ctddev.games.messages;

/**
 * Created by pva701 on 9/20/14.
 */
public class AddEnergyMessage {
    private int value;

    public AddEnergyMessage() {
    }

    public AddEnergyMessage(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}