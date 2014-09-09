package net.dimatomp.gamechallenge;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class LoginForm extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_form);
    }

    ProgressDialog dialog;

    public void tryLogin(View view) {
        dialog = ProgressDialog.show(this, "", getString(R.string.logging_in));
        String userName = ((TextView) findViewById(R.id.usernamePrompt)).getText().toString();
        connection = new HandlerConnection(userName);
        if (!bindService(new Intent(this, ServerConnectionHandler.class), connection, BIND_AUTO_CREATE)) {
            connection = null;
            Log.e(TAG, "Failed to bind to the connection handler");
        }
    }

    public void startGame() {
        dialog.cancel();
        startActivity(new Intent(this, GameField.class));
    }

    public void showError(int res) {
        dialog.cancel();
        Toast.makeText(this, res, Toast.LENGTH_LONG).show();
        unbindService(connection);
        connection = null;
    }

    @Override
    protected void onDestroy() {
        if (connection != null) {
            unbindService(connection);
            connection = null;
        }
        super.onDestroy();
    }

    private static final String TAG = "LoginForm";

    private HandlerConnection connection;

    class HandlerConnection implements ServiceConnection {
        final String userName;
        ServerConnectionHandler.ServerConnectionBinder service;

        HandlerConnection(String userName) {
            this.userName = userName;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            this.service = (ServerConnectionHandler.ServerConnectionBinder) service;
            this.service.logIn(LoginForm.this, "http://192.168.1.4:9092", userName);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.w(TAG, "Socket.io session service disconnected");
        }
    }
}
