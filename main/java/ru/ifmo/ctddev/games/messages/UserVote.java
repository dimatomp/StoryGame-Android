package ru.ifmo.ctddev.games.messages;

/**
 * Created by dimatomp on 14.09.14.
 */
public class UserVote {
    private int idPoll;
    private String optionName;
    private int amount;

    public UserVote() {}

    public UserVote(int idPoll, String optionName, int amount) {
        this.idPoll = idPoll;
        this.optionName = optionName;
        this.amount = amount;
    }

    public int getIdPoll() {
        return idPoll;
    }

    public String getOptionName() {
        return optionName;
    }

    public int getAmount() {
        return amount;
    }

    public void setIdPoll(int idPoll) {
        this.idPoll = idPoll;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
