package ru.ifmo.ctddev.games.messages;

/**
 * Created by pva701 on 9/18/14.
 */
public class DigResponseMessage {
    private int type;
    private int amount;
    private int energy;

    public DigResponseMessage() {}

    public DigResponseMessage(int type, int amount, int energy) {
        this.type = type;
        this.amount = amount;
        this.energy = energy;
    }

    public int getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public int getEnergy() {
        return energy;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }
}
