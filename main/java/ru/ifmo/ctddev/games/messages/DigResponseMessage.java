package ru.ifmo.ctddev.games.messages;

/**
 * Created by pva701 on 9/18/14.
 */
public class DigResponseMessage {
    private int itemId;
    private String name;
    private int costSell;
    private int count;
    private int type;
    private int energy;

    public DigResponseMessage() {
    }

    public DigResponseMessage(int itemId, String name, int costSell, int count, int type, int energy) {
        this.itemId = itemId;
        this.name = name;
        this.costSell = costSell;
        this.count = count;
        this.type = type;
        this.count = count;
        this.energy = energy;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCostSell() {
        return costSell;
    }

    public void setCostSell(int costSell) {
        this.costSell = costSell;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}