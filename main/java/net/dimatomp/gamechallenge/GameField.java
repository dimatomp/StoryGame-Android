package net.dimatomp.gamechallenge;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import ru.ifmo.ctddev.games.messages.MoveResponseMessage;

import static net.dimatomp.gamechallenge.GameDatabaseColumns.CHOSEN;
import static net.dimatomp.gamechallenge.GameDatabaseColumns.TITLE;

public class GameField extends Activity implements AdapterView.OnItemClickListener {
    private static final String TAG = "GameField";
    private static final String SAVED_INSTANCE_FIELD = "net.dimatomp.gamechallenge.GameField.SAVED_INSTANCE_FIELD";
    private static final String SAVED_INSTANCE_DIALOG = "net.dimatomp.gamechallenge.GameField.SAVED_INSTANCE_DIALOG";
    private static final String SAVED_INSTANCE_DIALOG_SHOWN = "net.dimatomp.gamechallenge.GameField.SAVED_INSTANCE_DIALOG_SHOWN";
    FieldView field;
    boolean justStarted;
    SimpleCursorAdapter pollListAdapter;
    RadioGroupAdapter pollChoicesAdapter;
    AlertDialog.Builder pollChoiceDialogBuilder;
    AlertDialog pollChoiceDialog;
    PollLoaderCallbacks pollLoaderCallbacks = new PollLoaderCallbacks();
    AdapterRadioGroup dialogRadioGroup;
    MoneyPicker dialogPicker;
    private HandlerConnection connection = new HandlerConnection();
    private String dialogShown;

    private void setupTabs() {
        TabHost host = (TabHost) findViewById(R.id.tabHost);
        host.setup();
        TabHost.TabSpec childView = host.newTabSpec("mapView");
        createIndicator(host, childView, getString(R.string.tab_map_view),
                getResources().getDrawable(android.R.drawable.ic_dialog_map));
        childView.setContent(R.id.fieldView);
        host.addTab(childView);
        childView = host.newTabSpec("pollListView");
        createIndicator(host, childView, getString(R.string.tab_polls),
                getResources().getDrawable(android.R.drawable.ic_menu_agenda));
        childView.setContent(R.id.pollsTab);
        host.addTab(childView);
        childView = host.newTabSpec("statusView");
        createIndicator(host, childView, getString(R.string.tab_status),
                getResources().getDrawable(android.R.drawable.ic_dialog_info));
        childView.setContent(R.id.statusView);
        host.addTab(childView);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            host.getTabWidget().setOrientation(LinearLayout.VERTICAL);
    }

    private void createIndicator(TabHost host, TabHost.TabSpec childView, String text, Drawable icon) {
        View result = getLayoutInflater().inflate(R.layout.tab_indicator, host.getTabWidget(), false);
        TextView textView = (TextView) result.findViewById(R.id.title);
        textView.setText(text);
        ImageView iconView = (ImageView) result.findViewById(R.id.icon);
        iconView.setImageDrawable(icon);
        childView.setIndicator(result);
    }

    private void setupVotesList() {
        ListView view = (ListView) findViewById(R.id.pollsTab);
        view.setAdapter(pollListAdapter);
        view.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Bundle args = new Bundle();
        String pollName = ((TextView) view).getText().toString();
        args.putString(PollLoaderCallbacks.ARG_POLL_NAME, pollName);
        getLoaderManager().restartLoader(1, args, pollLoaderCallbacks);
        // TODO move this to AsyncTask
        Cursor findChoice = GameDatabase.getPollDataByName(this, pollName);
        pollChoicesAdapter.setPlayerChoice(findChoice.getString(findChoice.getColumnIndex(CHOSEN)));

        createDialog(getDialogView());
        dialogShown = ((TextView) view).getText().toString();
        showDialog();
    }

    private void showDialog() {
        pollChoiceDialog.show();
        pollChoiceDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    private View getDialogView() {
        View theView = getLayoutInflater().inflate(R.layout.voting_dialog, null);
        dialogRadioGroup = (AdapterRadioGroup) theView.findViewById(R.id.poll_choices);
        dialogRadioGroup.setAdapter(pollChoicesAdapter);
        dialogPicker = (MoneyPicker) theView.findViewById(R.id.amount_invested);
        dialogRadioGroup.setMoneyPicker(dialogPicker);
        return theView;
    }

    private void createDialog(View view) {
        pollChoiceDialog = pollChoiceDialogBuilder.setView(view).create();
    }

    public void enableOkButton() {
        pollChoiceDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
    }

    private void setupVoteDialog() {
        pollChoiceDialogBuilder = new AlertDialog.Builder(this);
        pollChoiceDialogBuilder.setTitle(R.string.vote_dialog_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String optionName = ((TextView) dialogRadioGroup.getSelectedView()).getText().toString();
                        int amountInvested = dialogPicker.getSpecifiedAmount();
                        Log.v(TAG, "Voted for " + optionName + ", paid " + amountInvested);
                        connection.sendVoteMessage(optionName, amountInvested);
                        dialogShown = null;
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialogShown = null;
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialogShown = null;
                    }
                });
    }

    public void updateVoteInfo() {
        getLoaderManager().restartLoader(0, null, pollLoaderCallbacks);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        justStarted = (savedInstanceState == null);
        if (!bindService(new Intent(this, ServerConnectionHandler.class), connection, 0))
            Log.e(TAG, "Could not bind to the service");
        setContentView(R.layout.activity_game_field);

        setupTabs();
        pollListAdapter = new SimpleCursorAdapter(
                this, android.R.layout.simple_list_item_1, null, new String[]{TITLE},
                new int[]{android.R.id.text1}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        pollChoicesAdapter = new RadioGroupAdapter(this, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        setupVotesList();
        setupVoteDialog();

        field = (FieldView) findViewById(R.id.fieldView);
        if (savedInstanceState != null) {
            field.setField((int[][]) savedInstanceState.getSerializable(SAVED_INSTANCE_FIELD), false);
            if (savedInstanceState.containsKey(SAVED_INSTANCE_DIALOG_SHOWN)) {
                dialogShown = savedInstanceState.getString(SAVED_INSTANCE_DIALOG_SHOWN);
                pollChoiceDialog = pollChoiceDialogBuilder.setView(getDialogView()).create();
                pollChoiceDialog.onRestoreInstanceState(savedInstanceState.getBundle(SAVED_INSTANCE_DIALOG));
            }
            justStarted = false;
        } else
            justStarted = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dialogShown != null)
            pollChoiceDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dialogShown != null)
            pollChoiceDialog.dismiss();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVED_INSTANCE_FIELD, field.getField());
        if (dialogShown != null) {
            outState.putString(SAVED_INSTANCE_DIALOG_SHOWN, dialogShown);
            outState.putBundle(SAVED_INSTANCE_DIALOG, pollChoiceDialog.onSaveInstanceState());
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(connection);
        super.onDestroy();
    }

    public void sendMoveMessage(MoveDirection direction) {
        connection.sendMoveRequest(direction);
    }

    public void handleMoveResponse(MoveResponseMessage object) {
        if (object.getSuccess()) {
            // TODO Hardcoded speed
            field.addLayer(MoveDirection.getDirection(object.getDx(), object.getDy()), object.getLayer(), 100);
        } else
            field.handleNextMove(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            TabWidget widget = (TabWidget) findViewById(android.R.id.tabs);
            if (widget.getVisibility() == View.GONE)
                widget.setVisibility(View.VISIBLE);
            else
                widget.setVisibility(View.GONE);
        }
        super.onKeyDown(keyCode, event);
        return true;
    }

    public void finish(View view) {
        finish();
    }

    public enum MoveDirection {
        RIGHT(1, 0), DOWN(0, 1), LEFT(-1, 0), UP(0, -1);

        final int dx, dy;

        MoveDirection(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        static MoveDirection getDirection(int dx, int dy) {
            for (MoveDirection direction : values())
                if (dx == direction.dx && dy == direction.dy)
                    return direction;
            return null;
        }
    }

    class PollLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        private static final String ARG_POLL_NAME = "net.dimatomp.gamechallenge.GameField.PollLoaderCallbacks.ARG_POLL_NAME";

        @Override
        public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
            if (id == 0)
                return new SimpleCursorLoader(GameField.this) {
                    @Override
                    public Cursor loadInBackground() {
                        return GameDatabase.getPolls(getContext());
                    }
                };
            final String pollName = args.getString(ARG_POLL_NAME);
            return new SimpleCursorLoader(GameField.this) {
                @Override
                public Cursor loadInBackground() {
                    return GameDatabase.getPollOptions(GameField.this, pollName);
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (loader.getId() == 0)
                pollListAdapter.swapCursor(data);
            else
                pollChoicesAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            if (loader.getId() == 0)
                pollListAdapter.swapCursor(null);
            else
                pollChoicesAdapter.swapCursor(null);
        }
    }

    private class HandlerConnection implements ServiceConnection {
        ServerConnectionHandler.ServerConnectionBinder service;

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            this.service = (ServerConnectionHandler.ServerConnectionBinder) service;
            this.service.setGameField(GameField.this);
            if (justStarted)
                field.setField(this.service.getStartInfo().getField(), false);
        }

        public void sendVoteMessage(String optionName, int amount) {
            service.sendVoteMessage(dialogShown, optionName, amount);
        }

        public void sendMoveRequest(MoveDirection direction) {
            service.sendMoveRequest(direction.dx, direction.dy);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Service disconnected");
            service.setGameField(null);
        }
    }
}
