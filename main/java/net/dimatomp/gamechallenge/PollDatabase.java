package net.dimatomp.gamechallenge;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import ru.ifmo.ctddev.games.messages.UserVote;
import ru.ifmo.ctddev.games.state.Poll;

import static net.dimatomp.gamechallenge.PollDatabaseColumns.CHOSEN;
import static net.dimatomp.gamechallenge.PollDatabaseColumns.MAIN_TABLE;
import static net.dimatomp.gamechallenge.PollDatabaseColumns.MINIMAL_AMOUNT;
import static net.dimatomp.gamechallenge.PollDatabaseColumns.MONEY;
import static net.dimatomp.gamechallenge.PollDatabaseColumns.OPTION_NAME;
import static net.dimatomp.gamechallenge.PollDatabaseColumns.POLL_ID;
import static net.dimatomp.gamechallenge.PollDatabaseColumns.PRIORITY;
import static net.dimatomp.gamechallenge.PollDatabaseColumns.TITLE;
import static net.dimatomp.gamechallenge.PollDatabaseColumns.VOTE_OPTIONS;
import static net.dimatomp.gamechallenge.PollDatabaseColumns._ID;

/**
 * Created by dimatomp on 13.09.14.
 */
public class PollDatabase extends SQLiteOpenHelper {
    public static final String[] POLL_COLUMNS = new String[]{_ID, TITLE};
    public static final String[] OPTION_COLUMNS = new String[]{_ID, OPTION_NAME};
    private static final String TAG = "PollDatabase";
    private static PollDatabase instance;

    private PollDatabase(Context context) {
        super(context, null, null, 1);
    }

    public static PollDatabase getInstance(Context context) {
        if (instance == null)
            instance = new PollDatabase(context.getApplicationContext());
        return instance;
    }

    private static void dropEverything(SQLiteDatabase db) {
        db.execSQL("DELETE FROM " + MAIN_TABLE + ";");
        db.execSQL("DELETE FROM " + VOTE_OPTIONS + ";");
    }

    public static void dropEverything(Context context) {
        Log.d(TAG, "Drop method called");
        dropEverything(getInstance(context).getWritableDatabase());
    }

    public static void addPoll(Context context, Poll poll, UserVote chosen) {
        // TODO how about transactions?
        SQLiteDatabase db = getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(_ID, poll.getId());
        values.put(TITLE, poll.getQuestion());
        values.put(PRIORITY, poll.getPriority());
        if (chosen != null) {
            values.put(CHOSEN, poll.getOptions()[chosen.getOption()]);
            values.put(MONEY, chosen.getMoney());
        }
        db.insert(MAIN_TABLE, null, values);
        values.clear();
        values.put(POLL_ID, poll.getId());
        for (int i = 0; i < poll.getOptions().length; i++) {
            values.put(OPTION_NAME, poll.getOptions()[i]);
            values.put(MINIMAL_AMOUNT, poll.getMinimalAmount()[i]);
            db.insert(VOTE_OPTIONS, null, values);
        }
    }

    public static Cursor getPolls(Context context) {
        SQLiteDatabase db = getInstance(context).getReadableDatabase();
        return db.query(MAIN_TABLE, POLL_COLUMNS, null, null, null, null, PRIORITY + " DESC");
    }

    public static Cursor getPollOptions(Context context, String title) {
        SQLiteDatabase db = getInstance(context).getReadableDatabase();
        Cursor findId = db.query(MAIN_TABLE, new String[]{_ID}, TITLE + " = '" + title + "'", null, null, null, null);
        findId.moveToFirst();
        return db.query(VOTE_OPTIONS, OPTION_COLUMNS, POLL_ID + " = " +
                findId.getInt(findId.getColumnIndex(_ID)), null, null, null, null);
    }

    public static void chooseOption(Context context, int pollID, int optionID, int money) {
        SQLiteDatabase db = getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CHOSEN, optionID);
        values.put(MONEY, money);
        db.update(MAIN_TABLE, values, _ID + " = " + pollID, null);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropEverything(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO add foreign keys
        db.execSQL("CREATE TABLE " + MAIN_TABLE + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                TITLE + " TEXT NOT NULL, " +
                PRIORITY + " INTEGER NOT NULL, " +
                CHOSEN + " TEXT NULL, " +
                MONEY + " INTEGER NULL);");
        db.execSQL("CREATE TABLE " + VOTE_OPTIONS + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                POLL_ID + " INTEGER NOT NULL, " +
                OPTION_NAME + " TEXT NOT NULL, " +
                MINIMAL_AMOUNT + " INTEGER NOT NULL);");
    }
}
