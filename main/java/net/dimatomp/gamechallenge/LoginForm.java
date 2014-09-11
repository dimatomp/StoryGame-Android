package net.dimatomp.gamechallenge;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class LoginForm extends Activity {
    private static final String TAG = "LoginForm";
    ProgressDialog dialog;
    private HandlerConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_form);
    }

    private void unbindTheService() {
        if (connection != null) {
            unbindService(connection);
            connection = null;
        }
    }

    public void tryLogin(View view) {
        dialog = ProgressDialog.show(this, "", getString(R.string.logging_in), false, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                unbindTheService();
            }
        });
        String userName = ((TextView) findViewById(R.id.usernamePrompt)).getText().toString();
        String serverAddress = ((TextView) findViewById(R.id.serverAddressPrompt)).getText().toString();
        unbindTheService();
        connection = new HandlerConnection(userName, serverAddress);
        if (!bindService(new Intent(this, ServerConnectionHandler.class), connection, BIND_AUTO_CREATE)) {
            connection = null;
            Log.e(TAG, "Failed to bind to the connection handler");
        }
    }

    public void startGame() {
        dialog.dismiss();
        startActivity(new Intent(this, GameField.class));
    }

    public void showError(int res) {
        dialog.cancel();
        Toast.makeText(this, res, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        unbindTheService();
        super.onDestroy();
    }

    class HandlerConnection implements ServiceConnection {
        final String userName, serverAddress;
        ServerConnectionHandler.ServerConnectionBinder service;

        HandlerConnection(String userName, String serverAddress) {
            this.userName = userName;
            this.serverAddress = serverAddress;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            this.service = (ServerConnectionHandler.ServerConnectionBinder) service;
            this.service.logIn(LoginForm.this, serverAddress, userName);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.w(TAG, "Socket.io session service disconnected");
        }
    }
}
