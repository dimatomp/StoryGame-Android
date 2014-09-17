package net.dimatomp.gamechallenge;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.RadioButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import static net.dimatomp.gamechallenge.PollDatabaseColumns.OPTION_NAME;

/**
 * Created by dimatomp on 17.09.14.
 */
public class RadioGroupAdapter extends SimpleCursorAdapter {
    private String playerChoice;

    public RadioGroupAdapter(Context context, int flags) {
        super(context, R.layout.adapter_radio_button, null, new String[]{OPTION_NAME}, new int[]{R.id.adapter_radio_button}, flags);
    }

    public void setPlayerChoice(String playerChoice) {
        this.playerChoice = playerChoice;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        String viewText = ((TextView) view).getText().toString();
        if (viewText.equals(playerChoice))
            ((RadioButton) view).setChecked(true);
    }
}
