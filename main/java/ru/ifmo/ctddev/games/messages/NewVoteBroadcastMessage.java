package ru.ifmo.ctddev.games.messages;

import ru.ifmo.ctddev.games.state.Poll;

/**
 * Created by pva701 on 9/18/14.
 */
public class NewVoteBroadcastMessage {
    private Poll poll;
    public NewVoteBroadcastMessage() {}
    public NewVoteBroadcastMessage(Poll poll) {
        this.poll = poll;
    }

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }
}
