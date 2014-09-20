package ru.ifmo.ctddev.games.state;
/**
 * Created by pva701 on 9/18/14.
 */
public class Item {
    private int id;
    private String name;
    private int costBuy;
    private int costSell;
    private int type;
    public Item() {}
    public Item(int id, String name, int type, int costBuy, int costSell) {
        this.id = id;
        this.name = name;
        this.costBuy = costBuy;
        this.costSell = costSell;
    }
    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public int getCostBuy() {
        return costBuy;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setCostBuy(int costBuy) {
        this.costBuy = costBuy;
    }
    public void setCostSell(int costSell) {
        this.costSell = costSell;
    }
    public int getCostSell() {
        return costSell;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
}