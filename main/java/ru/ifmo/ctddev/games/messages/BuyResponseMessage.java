package ru.ifmo.ctddev.games.messages;
/**
 * Created by pva701 on 9/18/14.
 */
public class BuyResponseMessage {
    private boolean success;
    private int money;
    public BuyResponseMessage() {}
    public BuyResponseMessage(boolean success, int money) {
        this.success = success;
        this.money = money;
    }
    public boolean isSuccess() {
        return success;
    }
    public int getMoney() {
        return money;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    public void setMoney(int money) {
        this.money = money;
    }
}