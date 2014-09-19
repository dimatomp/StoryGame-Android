package net.dimatomp.gamechallenge;

import android.provider.BaseColumns;

/**
 * Created by dimatomp on 13.09.14.
 */
public interface GameDatabaseColumns extends BaseColumns {
    String POLLS_TABLE = "polls_table";
    String TITLE = "PollTitle";
    String PRIORITY = "PollStatus";
    String CHOSEN = "ChosenOption";
    String MONEY = "MoneyInvested";

    String VOTE_OPTIONS = "options_table";
    String POLL_NAME = "PollName";
    String OPTION_NAME = "OptionName";
    String MINIMAL_AMOUNT = "MinimalAmount";

    String INVENTORY_TABLE = "inventory_table";
    String ITEM_NAME = "ItemName";
    String ITEM_TYPE = "ItemType";
    String ITEM_COST = "ItemCost";
}
