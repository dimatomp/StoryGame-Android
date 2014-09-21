package ru.ifmo.ctddev.games.messages;

/**
 * Created by pva701 on 9/18/14.
 */
public class SellResponseMessage {
    private boolean success;
    private int money;

    public SellResponseMessage() {
    }

    public SellResponseMessage(boolean success, int money) {
        this.success = success;
        this.money = money;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }
}