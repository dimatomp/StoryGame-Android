package ru.ifmo.ctddev.games.messages;

/**
 * Created by Aksenov239 on 28.08.2014.
 */
public class VoteMessage {
    private int id;
    private String option;
    private int amount;

    public VoteMessage() {}

    public VoteMessage(int id, String option, int amount) {
        this.id = id;
        this.option = option;
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
