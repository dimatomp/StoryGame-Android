package net.dimatomp.gamechallenge;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
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
        String s = ((TextView) findViewById(R.id.usernamePrompt)).getText().toString();
        dialog = ProgressDialog.show(this, "", getString(R.string.logging_in));
        new ServerConnection("http://ctddev.ifmo.ru:9092", s, this);
    }

    public void startGame(ServerConnection connection, Object... args) {
        dialog.cancel();
        // Dummy implementation
        Toast.makeText(this, "Connected and joined successfully!", Toast.LENGTH_SHORT).show();
        connection.logOut();
    }

    public void showError(int res) {
        dialog.cancel();
        Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
    }
}
