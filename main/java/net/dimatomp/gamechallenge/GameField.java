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
import android.widget.NumberPicker;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import ru.ifmo.ctddev.games.messages.MoveResponseMessage;

import static net.dimatomp.gamechallenge.PollDatabaseColumns.TITLE;

public class GameField extends Activity implements AdapterView.OnItemClickListener {
    private static final String TAG = "GameField";
    private static final String SAVED_INSTANCE_FIELD = "net.dimatomp.gamechallenge.GameField.SAVED_INSTANCE_FIELD";
    private static final String SAVED_INSTANCE_DIALOG_SHOWN = "net.dimatomp.gamechallenge.GameField.SAVED_INSTANCE_DIALOG_SHOWN";
    FieldView field;
    boolean justStarted;
    SimpleCursorAdapter pollListAdapter;
    RadioGroupAdapter pollChoicesAdapter;
    AlertDialog.Builder pollChoiceDialogBuilder;
    AlertDialog pollChoiceDialog;
    PollLoaderCallbacks pollLoaderCallbacks = new PollLoaderCallbacks();
    private HandlerConnection connection = new HandlerConnection();
    private boolean dialogShown;

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
                getResources().getDrawable(android.R.drawable.checkbox_on_background));
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
        args.putString(PollLoaderCallbacks.ARG_POLL_NAME, ((TextView) view).getText().toString());
        getLoaderManager().restartLoader(1, args, pollLoaderCallbacks);

        // TODO handle orientation change properly
        View theView = getDialogView();

        createDialog(theView);
        showDialog();
    }

    private View getDialogView() {
        View theView = getLayoutInflater().inflate(R.layout.voting_dialog, null);
        ((AdapterRadioGroup) theView.findViewById(R.id.poll_choices)).setAdapter(pollChoicesAdapter);
        final NumberPicker amountInvested = (NumberPicker) theView.findViewById(R.id.amount_invested);
        amountInvested.setMinValue(0);
        amountInvested.setMaxValue(11);
        amountInvested.setWrapSelectorWheel(false);
        amountInvested.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            final String[] values = new String[12];
            int numberPickerStep = 1;

            {
                updateValues();
                amountInvested.setDisplayedValues(values);
            }

            private void updateValues() {
                values[0] = Integer.toString(numberPickerStep / 10 * 9);
                for (int i = 1; i <= 10; i++)
                    values[i] = Integer.toString(numberPickerStep * i);
                values[11] = Integer.toString(numberPickerStep * 20);
            }

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (newVal == picker.getMaxValue()) {
                    if (numberPickerStep * 10 < Integer.MAX_VALUE / 10) {
                        numberPickerStep *= 10;
                        updateValues();
                        picker.setValue(2);
                    }
                } else if (newVal == picker.getMinValue() && numberPickerStep > 1) {
                    numberPickerStep /= 10;
                    updateValues();
                    picker.setValue(picker.getMaxValue() - 2);
                }
            }
        });
        return theView;
    }

    private void createDialog(View view) {
        pollChoiceDialog = pollChoiceDialogBuilder.setView(view).create();
    }

    private void showDialog() {
        dialogShown = true;
        pollChoiceDialog.show();
    }

    private void setupVoteDialog() {
        pollChoiceDialogBuilder = new AlertDialog.Builder(this);
        pollChoiceDialogBuilder.setTitle(R.string.vote_dialog_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO FIRE ZE MISSILES !!!
                        dialogShown = false;
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialogShown = false;
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialogShown = false;
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
        getLoaderManager().initLoader(0, null, pollLoaderCallbacks);
        setupVoteDialog();

        field = (FieldView) findViewById(R.id.fieldView);
        if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_INSTANCE_FIELD)) {
            field.setField((int[][]) savedInstanceState.getSerializable(SAVED_INSTANCE_FIELD), false);
            dialogShown = savedInstanceState.getBoolean(SAVED_INSTANCE_DIALOG_SHOWN);
            pollChoiceDialog = pollChoiceDialogBuilder.setView(getDialogView()).create();
            pollChoiceDialog.onRestoreInstanceState(savedInstanceState);
            justStarted = false;
        } else
            justStarted = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dialogShown)
            pollChoiceDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dialogShown)
            pollChoiceDialog.dismiss();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVED_INSTANCE_FIELD, field.getField());
        outState.putBoolean(SAVED_INSTANCE_DIALOG_SHOWN, dialogShown);
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
        if (object.getSuccessful()) {
            field.addLayer(MoveDirection.values()[object.getDirection()], object.getLayer(), object.getSpeed());
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
        RIGHT, DOWN, LEFT, UP
    }

    class PollLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        private static final String ARG_POLL_NAME = "net.dimatomp.gamechallenge.GameField.PollLoaderCallbacks.ARG_POLL_NAME";

        @Override
        public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
            if (id == 0)
                return new SimpleCursorLoader(GameField.this) {
                    @Override
                    public Cursor loadInBackground() {
                        return PollDatabase.getPolls(getContext());
                    }
                };
            final String pollName = args.getString(ARG_POLL_NAME);
            return new SimpleCursorLoader(GameField.this) {
                @Override
                public Cursor loadInBackground() {
                    return PollDatabase.getPollOptions(GameField.this, pollName);
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

        public void sendMoveRequest(MoveDirection direction) {
            service.sendMoveRequest(direction.ordinal());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Service disconnected");
            service.setGameField(null);
        }
    }
}
