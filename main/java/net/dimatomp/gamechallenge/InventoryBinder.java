package net.dimatomp.gamechallenge;

import android.content.res.Resources;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import static net.dimatomp.gamechallenge.GameDatabaseColumns.*;

/**
 * Created by dimatomp on 19.09.14.
 */
public class InventoryBinder implements SimpleCursorAdapter.ViewBinder {
    private Resources resources;
    private static final int itemTypeRes[] = new int[]{R.string.item_weapon, R.string.item_potion, R.string.item_ore};
    private static final int iconResources[] = new int[]{R.drawable.sword, R.drawable.potion, R.drawable.ore};

    public InventoryBinder(Resources resources) {
        this.resources = resources;
    }

    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        switch (cursor.getColumnName(columnIndex)) {
            case ITEM_NAME:
                ((TextView) view).setText(cursor.getString(columnIndex));
                break;
            case ITEM_TYPE:
                if (view instanceof ImageView)
                    ((ImageView) view).setImageResource(iconResources[cursor.getInt(columnIndex)]);
                else
                    ((TextView) view).setText(
                            resources.getString(itemTypeRes[cursor.getInt(columnIndex)]));
                break;
            case ITEM_COUNT:
            case ITEM_COST:
                // TODO Display this stuff!
                return false;
        }
        return true;
    }
}
