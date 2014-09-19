package ru.ifmo.ctddev.games.messages;

import ru.ifmo.ctddev.games.state.InventoryItem;

import java.util.Map;

/**
 * Created by pva701 on 9/19/14.
 */
public class InventoryMessage {
    private Map<Integer, InventoryItem> inventory;
    public InventoryMessage() {}

    public InventoryMessage(Map<Integer, InventoryItem> inventory) {
        this.inventory = inventory;
    }

    public Map<Integer, InventoryItem> getInventory() {
        return inventory;
    }

    public void setInventory(Map<Integer, InventoryItem> inventory) {
        this.inventory = inventory;
    }
}
