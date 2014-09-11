package net.dimatomp.gamechallenge;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class GameField extends Activity {
    private static final String TAG = "GameField";
    private static final String SAVED_INSTANCE_FIELD = "net.dimatomp.gamechallenge.GameField.SAVED_INSTANCE_FIELD";
    FieldView field;
    boolean justStarted;
    private HandlerConnection connection = new HandlerConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        justStarted = (savedInstanceState == null);
        if (!bindService(new Intent(this, ServerConnectionHandler.class), connection, 0))
            Log.e(TAG, "Could not bind to the service");
        field = new FieldView(this);
        if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_INSTANCE_FIELD)) {
            field.setField((int[][]) savedInstanceState.getSerializable(SAVED_INSTANCE_FIELD), false);
            justStarted = false;
        } else
            justStarted = true;
        setContentView(field);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVED_INSTANCE_FIELD, field.getField());
    }

    @Override
    protected void onDestroy() {
        unbindService(connection);
        super.onDestroy();
    }

    public void sendMoveMessage(MoveDirection direction) {
        if (!connection.service.sendMoveRequest(direction.ordinal())) {
            Log.e(TAG, "JSONException when sending move request");
            // TODO find a better solution
            field.handleNextMove(false);
        }
    }

    public void handleMoveResponse(JSONObject object) {
        try {
            if (object.getBoolean("successful")) {
                JSONArray jsonLayer = object.getJSONArray("layer");
                int[] layer = new int[jsonLayer.length()];
                for (int i = 0; i < layer.length; i++)
                    layer[i] = jsonLayer.getInt(i);
                field.addLayer(MoveDirection.values()[object.getInt("direction")], layer, object.getInt("speed"));
            } else
                field.handleNextMove(true);
        } catch (JSONException e) {
            Log.e(TAG, "Could not parse MoveResponseMessage");
            field.handleNextMove(true);
        }
    }

    public enum MoveDirection {
        RIGHT, DOWN, LEFT, UP
    }

    private class HandlerConnection implements ServiceConnection {
        ServerConnectionHandler.ServerConnectionBinder service;

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            this.service = (ServerConnectionHandler.ServerConnectionBinder) service;
            this.service.setGameField(GameField.this);
            if (justStarted) {
                try {
                    JSONArray parcel = this.service.getStartInfo().getJSONArray("field");
                    int[][] fieldArr = new int[parcel.length()][];
                    for (int i = 0; i < fieldArr.length; i++) {
                        JSONArray col = parcel.getJSONArray(i);
                        fieldArr[i] = new int[col.length()];
                        for (int j = 0; j < fieldArr[i].length; j++)
                            fieldArr[i][j] = col.getInt(j);
                    }
                    field.setField(fieldArr, false);
                } catch (JSONException e) {
                    Toast.makeText(GameField.this, R.string.error_json_exception_receive, Toast.LENGTH_LONG).show();
                    finish(); // TODO maybe not finish?
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Service disconnected");
        }
    }
}
