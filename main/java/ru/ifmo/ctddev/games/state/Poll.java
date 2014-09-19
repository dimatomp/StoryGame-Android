package ru.ifmo.ctddev.games.state;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Aksenov239 on 30.08.2014.
 */
public class Poll {
    private int id;
    private String question;
    private String[] optionsName;

    private int[] minimalAmount;
    private int priority = 0;
    private Integer result;
    private String date;
    private int[] investedMoney;

    public Poll() {
    }

    public Poll(int id, String question, String[] optionsName, int[] minimalAmounts, int[] investedMoney) {
        this.id = id;
        this.question = question;
        this.optionsName = optionsName;
        this.minimalAmount = minimalAmounts;
        this.investedMoney = investedMoney;
    }

    public void vote(int option, int amount) {
        investedMoney[option] += amount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String[] getOptionsName() {
        return optionsName;
    }

    public void setOptionsName(String[] optionsName) {
        this.optionsName = optionsName;
    }

    public int[] getMinimalAmount() {
        return minimalAmount;
    }

    public int getMinimalAmountById(int id) {
        return minimalAmount[id];
    }

    public int getMinimalAmountByName(String name) {
        for (int i = 0; i < optionsName[i].length(); ++i)
            if (optionsName[i].equals(name))
                return minimalAmount[i];
        return -1;
    }

    public int getOptionId(String name) {
        for (int i = 0; i < optionsName[i].length(); ++i)
            if (optionsName[i].equals(name))
                return i;
        return -1;
    }

    public void setMinimalAmount(int[] minimalAmount) {
        this.minimalAmount = minimalAmount;
    }

    public void addInvestedMoney(String optionName, int x) {
        for (int i = 0; i < optionsName.length; ++i)
            if (optionsName[i].equals(optionName))
                investedMoney[i] += x;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setInvestedMoney(int[] investedMoney) {
        this.investedMoney = investedMoney;
    }

    public int[] getInvestedMoney() {
        return investedMoney;
    }
}
