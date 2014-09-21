package ru.ifmo.ctddev.games.messages;
/**
 * Created by pva701 on 9/21/14.
 */
public class ThrowOutMessage {
    private String name;
    private int count;
    public ThrowOutMessage() {}
    public ThrowOutMessage(String name, int count) {
        this.name = name;
        this.count = count;
    }
    public String getName() {
        return name;
    }
    public int getCount() {
        return count;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setCount(int count) {
        this.count = count;
    }
}