package ru.ifmo.ctddev.games.messages;

import java.util.Map;

import ru.ifmo.ctddev.games.state.InventoryItem;

/**
 * Created by pva701 on 9/19/14.
 */
public class InventoryMessage {
    private Map<Integer, InventoryItem> inventory;

    public InventoryMessage() {
    }

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
