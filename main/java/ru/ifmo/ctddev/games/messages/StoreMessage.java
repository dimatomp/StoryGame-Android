package ru.ifmo.ctddev.games.messages;

import java.util.Map;

import ru.ifmo.ctddev.games.state.Item;

/**
 * Created by pva701 on 9/18/14.
 */
public class StoreMessage {
    private boolean success;
    private Map<Integer, Item> items;

    public StoreMessage() {
    }

    public StoreMessage(boolean success) {
        this.success = success;
    }

    public StoreMessage(boolean success, Map<Integer, Item> items) {
        this.success = success;
        this.items = items;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Map<Integer, Item> getItems() {
        return items;
    }

    public void setItems(Map<Integer, Item> items) {
        this.items = items;
    }
}