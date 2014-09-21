package ru.ifmo.ctddev.games.messages;
/**
 * Created by pva701 on 9/18/14.
 */
public class BuyItemMessage {
    private int itemId;
    private int count;
    public BuyItemMessage() {}
    public BuyItemMessage(int itemId, int count) {
        this.itemId = itemId;
        this.count = count;
    }
    public int getItemId() {
        return itemId;
    }
    public int getCount() {
        return count;
    }
    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
    public void setCount(int count) {
        this.count = count;
    }
}