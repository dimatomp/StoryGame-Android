package ru.ifmo.ctddev.games.messages;

/**
 * Created by pva701 on 9/17/14.
 */
public class StateMessage {
    int money;
    int energy;
    int x, y;

    public StateMessage() {}

    public StateMessage(int money, int energy, int x, int y) {
        this.money = money;
        this.energy = energy;
        this.x = x;
        this.y = y;
    }

    public int getMoney() {
        return money;
    }

    public int getX() {
        return x;
    }

    public int getEnergy() {
        return energy;
    }

    public int getY() {
        return y;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
}
