package net.dimatomp.gamechallenge;

import android.provider.BaseColumns;

/**
 * Created by dimatomp on 13.09.14.
 */
public interface PollDatabaseColumns extends BaseColumns {
    String MAIN_TABLE = "polls_table";
    String TITLE = "PollTitle";
    String PRIORITY = "PollStatus";
    String CHOSEN = "ChosenOption";
    String MONEY = "MoneyInvested";

    String VOTE_OPTIONS = "options_table";
    String POLL_NAME = "PollName";
    String OPTION_NAME = "OptionName";
    String MINIMAL_AMOUNT = "MinimalAmount";
}
