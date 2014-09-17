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
    private static final String TAG = "LoginForm";
    ProgressDialog dialog;
    private HandlerConnection connection = new HandlerConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_form);
        bindService(new Intent(this, ServerConnectionHandler.class), connection, BIND_AUTO_CREATE);
    }

    public void tryLogin(View view) {
        dialog = ProgressDialog.show(this, "", getString(R.string.logging_in), false, true);
        String userName = ((TextView) findViewById(R.id.usernamePrompt)).getText().toString();
        String serverAddress = ((TextView) findViewById(R.id.serverAddressPrompt)).getText().toString();
        connection.logIn(serverAddress, userName);
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
    protected void onResume() {
        super.onResume();
        PollDatabase.dropEverything(this);
    }

    @Override
    protected void onDestroy() {
        unbindService(connection);
        super.onDestroy();
    }

    class HandlerConnection implements ServiceConnection {
        ServerConnectionHandler.ServerConnectionBinder service;

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            this.service = (ServerConnectionHandler.ServerConnectionBinder) service;
            this.service.setLoginForm(LoginForm.this);
            if (this.service.isConnecting())
                dialog = ProgressDialog.show(LoginForm.this, "", getString(R.string.logging_in), false, true);
        }

        public void logIn(String serverAddress, String userName) {
            service.logIn(serverAddress, userName);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.w(TAG, "Socket.io session service disconnected");
            service.setLoginForm(null);
        }
    }
}
