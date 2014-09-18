package net.dimatomp.gamechallenge;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.Map;

/**
 * Created by dimatomp on 15.09.14.
 */
public class AdapterRadioGroup extends AdapterView<CursorAdapter> implements CheckBox.OnCheckedChangeListener {
    CursorAdapter adapter;
    MoneyPicker moneyPicker;
    Map<String, Integer> minimalAmounts;
    private DataSetObserver observer = new DataSetObserver() {
        @Override
        public void onChanged() {
            removeAllViewsInLayout();
            updateChildren();
            requestLayout();
        }

        @Override
        public void onInvalidated() {
            removeAllViewsInLayout();
        }
    };
    private RadioButton selectedView;

    public AdapterRadioGroup(Context context) {
        super(context);
    }

    public AdapterRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdapterRadioGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public CursorAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void setAdapter(CursorAdapter adapter) {
        if (this.adapter != null)
            this.adapter.unregisterDataSetObserver(observer);
        this.adapter = adapter;
        if (this.adapter != null)
            this.adapter.registerDataSetObserver(observer);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int dy = getHeight() / (getChildCount() + 1);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            child.layout(0, dy * (i + 1) - height / 2, width, dy * (i + 1) - height / 2 + height);
        }
    }

    private void updateChildren() {
        if (getChildCount() == 0) {
            minimalAmounts = new ArrayMap<>();
            for (int i = 0; i < adapter.getCount(); i++) {
                View newChild = adapter.getView(i, null, this);
                minimalAmounts.put(((TextView) newChild).getText().toString(),
                        ((RadioGroupAdapter) adapter).getMinAmount());
                ((RadioButton) newChild).setOnCheckedChangeListener(this);
                addAndMeasureChild(newChild);
            }
        }
        int selView = -1;
        int minWidth = 0;
        int minHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            minWidth = Math.max(minWidth, getChildAt(i).getMeasuredWidth());
            minHeight += getChildAt(i).getMeasuredHeight();
            if (((RadioButton) getChildAt(i)).isChecked())
                selView = i;
        }
        if (selView != -1 && selectedView == null)
            setSelection(selView);
        setMinimumWidth(minWidth);
        setMinimumHeight(minHeight);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setSelection(getPositionForView(buttonView));
    }

    private void addAndMeasureChild(View view) {
        LayoutParams params = view.getLayoutParams();
        if (params == null)
            params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        addViewInLayout(view, -1, params);
        // TODO Specify AT_MOST for width
        view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
    }

    @Override
    public View getSelectedView() {
        return selectedView;
    }

    @Override
    public void setSelection(int position) {
        if (selectedView == null) {
            selectedView = (RadioButton) getChildAt(position);
            selectedView.setChecked(true);
            for (int i = 0; i < getChildCount(); i++)
                if (getChildAt(i) != selectedView)
                    getChildAt(i).setEnabled(false);
            if (moneyPicker != null)
                showMoneyPicker();
            ((GameField) getContext()).enableOkButton();
        }
    }

    private void showMoneyPicker() {
        moneyPicker.setVisibility(VISIBLE);
        moneyPicker.setMinimalAmount(minimalAmounts.get(selectedView.getText().toString()));
    }

    public void setMoneyPicker(MoneyPicker moneyPicker) {
        this.moneyPicker = moneyPicker;
        if (selectedView != null)
            showMoneyPicker();
        else
            moneyPicker.setVisibility(INVISIBLE);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        super.onSaveInstanceState();
        String[] texts = new String[getChildCount()];
        boolean[] checked = new boolean[getChildCount()];
        int[] amounts = new int[getChildCount()];
        for (int i = 0; i < getChildCount(); i++) {
            texts[i] = ((RadioButton) getChildAt(i)).getText().toString();
            checked[i] = ((RadioButton) getChildAt(i)).isChecked();
            amounts[i] = minimalAmounts.get(texts[i]);
        }
        return new SavedInstanceState(texts, checked, amounts);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(null);
        SavedInstanceState savedInstanceState = (SavedInstanceState) state;
        minimalAmounts = new ArrayMap<>();
        for (int i = 0; i < savedInstanceState.texts.length; i++) {
            RadioButton button = new RadioButton(getContext());
            button.setText(savedInstanceState.texts[i]);
            button.setChecked(savedInstanceState.checked[i]);
            button.setOnCheckedChangeListener(this);
            minimalAmounts.put(savedInstanceState.texts[i], savedInstanceState.minAmounts[i]);
            addAndMeasureChild(button);
        }
        updateChildren();
    }

    static class SavedInstanceState implements Parcelable {

        public static final Creator<SavedInstanceState> CREATOR = new Creator<SavedInstanceState>() {
            @Override
            public SavedInstanceState createFromParcel(Parcel source) {
                String[] texts = new String[source.readInt()];
                source.readStringArray(texts);
                boolean[] checked = new boolean[texts.length];
                source.readBooleanArray(checked);
                int[] minAmounts = new int[source.readInt()];
                source.readIntArray(minAmounts);
                return new SavedInstanceState(texts, checked, minAmounts);
            }

            @Override
            public SavedInstanceState[] newArray(int size) {
                return new SavedInstanceState[size];
            }
        };

        final String[] texts;
        final boolean[] checked;
        final int[] minAmounts;

        SavedInstanceState(String[] texts, boolean[] checked, int[] minAmounts) {
            this.texts = texts;
            this.checked = checked;
            this.minAmounts = minAmounts;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(texts.length);
            dest.writeStringArray(texts);
            dest.writeBooleanArray(checked);
            dest.writeIntArray(minAmounts);
        }
    }
}
