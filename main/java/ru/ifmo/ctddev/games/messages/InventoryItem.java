package ru.ifmo.ctddev.games.state;

/**
 * Created by pva701 on 9/19/14.
 */
public class InventoryItem {
    private int id;
    private String name;
    private int costSell;
    private int type;
    private int count;

    public InventoryItem() {
    }

    public InventoryItem(int id, String name, int costSell, int type, int count) {
        this.id = id;
        this.name = name;
        this.costSell = costSell;
        this.type = type;
        this.count = count;
    }

    public InventoryItem(int id, String name, int costSell, int type) {
        this.id = id;
        this.name = name;
        this.costSell = costSell;
        this.type = type;
        count = 0;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void addCount(int x) {
        count += x;
    }

    public int getId() {

        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
