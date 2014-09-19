package ru.ifmo.ctddev.games.messages;

/**
 * Created by Aksenov239 on 28.08.2014.
 */
public class VoteResultMessage {
    private boolean successful;

    public VoteResultMessage(boolean successful) {
        this.successful = successful;
    }

    public boolean getSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
}
