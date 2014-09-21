package ru.ifmo.ctddev.games.messages;
/**
 * Created by pva701 on 9/21/14.
 */
public class ThrowOutResponseMessage {
    private boolean success;
    public ThrowOutResponseMessage() {}
    public ThrowOutResponseMessage(boolean success) {
        this.success = success;
    }
    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
}