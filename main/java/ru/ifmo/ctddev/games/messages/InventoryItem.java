package ru.ifmo.ctddev.games.state;

import ru.ifmo.ctddev.games.state.Item;

/**
 * Created by pva701 on 9/19/14.
 */
public class InventoryItem {
    private int id;
    private String name;
    private int costSell;
    private int type;
    private int count;

    public InventoryItem() {}

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

    public InventoryItem(Item it) {
        id = it.getId();
        name = it.getName();
        type = it.getType();
        costSell = it.getCostSell();
        count = 0;
    }

    public InventoryItem(Item it, int count) {
        id = it.getId();
        name = it.getName();
        type = it.getType();
        costSell = it.getCostSell();
        this.count = count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void addCount(int x) {
        count += x;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCostSell(int costSell) {
        this.costSell = costSell;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {

        return id;
    }

    public String getName() {
        return name;
    }

    public int getCostSell() {
        return costSell;
    }

    public int getType() {
        return type;
    }
}
