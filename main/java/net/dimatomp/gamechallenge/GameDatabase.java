package net.dimatomp.gamechallenge;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import ru.ifmo.ctddev.games.messages.UserVote;
import ru.ifmo.ctddev.games.state.InventoryItem;
import ru.ifmo.ctddev.games.state.Item;
import ru.ifmo.ctddev.games.state.Poll;

import static net.dimatomp.gamechallenge.GameDatabaseColumns.CHOSEN;
import static net.dimatomp.gamechallenge.GameDatabaseColumns.GOODS_COST_BUY;
import static net.dimatomp.gamechallenge.GameDatabaseColumns.GOODS_COST_SELL;
import static net.dimatomp.gamechallenge.GameDatabaseColumns.GOODS_NAME;
import static net.dimatomp.gamechallenge.GameDatabaseColumns.GOODS_TYPE;
import static net.dimatomp.gamechallenge.GameDatabaseColumns.INVENTORY_TABLE;
import static net.dimatomp.gamechallenge.GameDatabaseColumns.ITEM_COST;
import static net.dimatomp.gamechallenge.GameDatabaseColumns.ITEM_COUNT;
import static net.dimatomp.gamechallenge.GameDatabaseColumns.ITEM_NAME;
import static net.dimatomp.gamechallenge.GameDatabaseColumns.ITEM_TYPE;
import static net.dimatomp.gamechallenge.GameDatabaseColumns.MINIMAL_AMOUNT;
import static net.dimatomp.gamechallenge.GameDatabaseColumns.MONEY;
import static net.dimatomp.gamechallenge.GameDatabaseColumns.OPTION_NAME;
import static net.dimatomp.gamechallenge.GameDatabaseColumns.POLLS_TABLE;
import static net.dimatomp.gamechallenge.GameDatabaseColumns.POLL_NAME;
import static net.dimatomp.gamechallenge.GameDatabaseColumns.PRIORITY;
import static net.dimatomp.gamechallenge.GameDatabaseColumns.STORE_TABLE;
import static net.dimatomp.gamechallenge.GameDatabaseColumns.TITLE;
import static net.dimatomp.gamechallenge.GameDatabaseColumns.VOTE_OPTIONS;
import static net.dimatomp.gamechallenge.GameDatabaseColumns._ID;

/**
 * Created by dimatomp on 13.09.14.
 */
public class GameDatabase extends SQLiteOpenHelper {
    public static final String[] POLL_COLUMNS = new String[]{_ID, TITLE};
    public static final String[] OPTION_COLUMNS = new String[]{_ID, OPTION_NAME, MINIMAL_AMOUNT};
    public static final String[] POLL_DATA = new String[]{_ID, CHOSEN, MONEY};
    public static final String[] STORE_COLUMNS = new String[]{_ID, GOODS_NAME, GOODS_TYPE, GOODS_COST_BUY, GOODS_COST_SELL};
    public static final String[] INVENTORY_COLUMNS = new String[]{_ID, ITEM_NAME, ITEM_TYPE, ITEM_COUNT, ITEM_COST};
    private static final String TAG = "PollDatabase";
    private static GameDatabase instance;

    private GameDatabase(Context context) {
        super(context, null, null, 1);
    }

    public static GameDatabase getInstance(Context context) {
        if (instance == null)
            instance = new GameDatabase(context.getApplicationContext());
        return instance;
    }

    public static void dropEverything(Context context) {
        Log.d(TAG, "Drop method called");
        SQLiteDatabase db = getInstance(context).getWritableDatabase();
        db.execSQL("DELETE FROM " + POLLS_TABLE + ";");
        db.execSQL("DELETE FROM " + VOTE_OPTIONS + ";");
        db.execSQL("DELETE FROM " + INVENTORY_TABLE + ";");
        db.execSQL("DELETE FROM " + STORE_TABLE + ";");
    }

    public static void cleanupTable(Context context, String name) {
        getInstance(context).getWritableDatabase().execSQL("DELETE FROM " + name + ";");
    }

    public static void addInvItem(Context context, InventoryItem item) {
        Cursor cursor = getInstance(context).getReadableDatabase()
                .query(INVENTORY_TABLE, new String[]{ITEM_COUNT},
                        ITEM_NAME + " = '" + item.getName() + "'", null, null, null, null);
        boolean alreadyExists = (cursor.getCount() > 0);
        SQLiteDatabase db = getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        if (!alreadyExists) {
            values.put(_ID, item.getId());
            values.put(ITEM_NAME, item.getName());
            values.put(ITEM_TYPE, item.getType());
            values.put(ITEM_COST, item.getCostSell());
            values.put(ITEM_COUNT, item.getCount());
            db.insert(INVENTORY_TABLE, null, values);
        } else {
            cursor.moveToFirst();
            values.put(ITEM_COUNT, item.getCount() + cursor.getInt(cursor.getColumnIndex(ITEM_COUNT)));
            db.update(INVENTORY_TABLE, values, ITEM_NAME + " = '" + item.getName() + "'", null);
        }
    }

    public static int getInvItemId(Context context, String itemName) {
        Cursor findId = getInstance(context).getReadableDatabase().query(INVENTORY_TABLE, new String[]{_ID}, ITEM_NAME + " = '" + itemName + "'", null, null, null, null);
        findId.moveToFirst();
        return findId.getInt(findId.getColumnIndex(_ID));
    }

    public static void removeInvItem(Context context, String itemName) {
        SQLiteDatabase db = getInstance(context).getReadableDatabase();
        Cursor findId = db.query(INVENTORY_TABLE, new String[]{ITEM_COUNT}, ITEM_NAME + " = '" + itemName + "'", null, null, null, null);
        findId.moveToFirst();
        db = getInstance(context).getWritableDatabase();
        int itemCount = findId.getInt(findId.getColumnIndex(ITEM_COUNT));
        if (findId.getInt(findId.getColumnIndex(ITEM_COUNT)) > 1) {
            ContentValues values = new ContentValues();
            values.put(ITEM_COUNT, itemCount - 1);
            db.update(INVENTORY_TABLE, values, ITEM_NAME + " = '" + itemName + "'", null);
        } else
            db.delete(INVENTORY_TABLE, ITEM_NAME + " = '" + itemName + "'", null);
    }

    public static void addStoreItem(Context context, Item item) {
        SQLiteDatabase db = getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(_ID, item.getId());
        values.put(GOODS_NAME, item.getName());
        values.put(GOODS_TYPE, item.getType());
        values.put(GOODS_COST_BUY, item.getCostBuy());
        values.put(GOODS_COST_SELL, item.getCostSell());
        db.insert(STORE_TABLE, null, values);
    }

    public static Cursor getStore(Context context) {
        return getInstance(context).getReadableDatabase().query(STORE_TABLE, STORE_COLUMNS, null, null, null, null, null);
    }

    public static Cursor getInventory(Context context) {
        return getInstance(context).getReadableDatabase().query(INVENTORY_TABLE, INVENTORY_COLUMNS, null, null, null, null, null);
    }

    public static void addPoll(Context context, Poll poll, UserVote chosen) {
        // TODO how about transactions?
        SQLiteDatabase db = getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(_ID, poll.getId());
        values.put(TITLE, poll.getQuestion());
        values.put(PRIORITY, poll.getPriority());
        if (chosen != null) {
            values.put(CHOSEN, chosen.getOptionName());
            values.put(MONEY, chosen.getAmount());
        } else
            values.put(MONEY, 0);
        db.insert(POLLS_TABLE, null, values);
        values.clear();
        values.put(POLL_NAME, poll.getQuestion());
        for (int i = 0; i < poll.getOptionsName().length; i++) {
            values.put(OPTION_NAME, poll.getOptionsName()[i]);
            values.put(MINIMAL_AMOUNT, chosen == null ? poll.getMinimalAmount()[i] : 0);
            db.insert(VOTE_OPTIONS, null, values);
        }
    }

    public static Cursor getPolls(Context context) {
        SQLiteDatabase db = getInstance(context).getReadableDatabase();
        return db.query(POLLS_TABLE, POLL_COLUMNS, null, null, null, null, PRIORITY + " DESC");
    }

    public static Cursor getPollOptions(Context context, String title) {
        SQLiteDatabase db = getInstance(context).getReadableDatabase();
        return db.query(VOTE_OPTIONS, OPTION_COLUMNS, POLL_NAME + " = '" + title + "'", null, null, null, null);
    }

    public static Cursor getPollDataByName(Context context, String title) {
        SQLiteDatabase db = getInstance(context).getReadableDatabase();
        Cursor findId = db.query(POLLS_TABLE, POLL_DATA, TITLE + " = '" + title + "'", null, null, null, null);
        findId.moveToFirst();
        return findId;
    }

    public static void chooseOption(Context context, String pollName, String optionName, int money) {
        Cursor findMoney = getPollDataByName(context, pollName);
        int cMoney = findMoney.getInt(findMoney.getColumnIndex(MONEY)) + money;
        SQLiteDatabase db = getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CHOSEN, optionName);
        values.put(MONEY, cMoney);
        db.update(POLLS_TABLE, values, TITLE + " = '" + pollName + "'", null);
        values.clear();
        values.put(MINIMAL_AMOUNT, 0);
        db.update(VOTE_OPTIONS, values, POLL_NAME + " = '" + pollName + "'", null);
    }

    public static InventoryItem getStoreItemToBuy(Context context, String name) {
        Cursor findId = getInstance(context).getReadableDatabase().query(STORE_TABLE, STORE_COLUMNS, GOODS_NAME + " = '" + name + "'", null, null, null, null);
        findId.moveToFirst();
        return new InventoryItem(
                findId.getInt(findId.getColumnIndex(_ID)),
                name, findId.getInt(findId.getColumnIndex(GOODS_COST_SELL)),
                findId.getInt(findId.getColumnIndex(GOODS_TYPE)), 1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DELETE TABLE IF EXISTS " + POLLS_TABLE + ";");
        db.execSQL("DELETE TABLE IF EXISTS " + VOTE_OPTIONS + ";");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + POLLS_TABLE + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                TITLE + " TEXT NOT NULL, " +
                PRIORITY + " INTEGER NOT NULL, " +
                CHOSEN + " TEXT NULL, " +
                MONEY + " INTEGER NULL);");
        db.execSQL("CREATE TABLE " + VOTE_OPTIONS + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                POLL_NAME + " TEXT NOT NULL, " +
                OPTION_NAME + " TEXT NOT NULL, " +
                MINIMAL_AMOUNT + " INTEGER NOT NULL);");
        db.execSQL("CREATE TABLE " + INVENTORY_TABLE + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                ITEM_NAME + " TEXT NOT NULL UNIQUE, " +
                ITEM_TYPE + " INTEGER NOT NULL, " +
                ITEM_COST + " INTEGER NOT NULL, " +
                ITEM_COUNT + " INTEGER NOT NULL);");
        db.execSQL("CREATE TABLE " + STORE_TABLE + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                GOODS_NAME + " TEXT NOT NULL UNIQUE, " +
                GOODS_TYPE + " INTEGER NOT NULL, " +
                GOODS_COST_BUY + " INTEGER NOT NULL, " +
                GOODS_COST_SELL + " INTEGER NOT NULL);");
    }
}
