package net.dimatomp.gamechallenge;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.NumberPicker;

/**
 * Created by dimatomp on 17.09.14.
 */
public class MoneyPicker extends NumberPicker {
    private int minimalAmount = 0;
    private OnMoneyChangedListener onMoneyChangedListener = new OnMoneyChangedListener();

    public MoneyPicker(Context context) {
        super(context);
        setOnValueChangedListener(onMoneyChangedListener);
        setMinValue(0);
        setMaxValue(11);
        setWrapSelectorWheel(false);
    }

    public MoneyPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnValueChangedListener(onMoneyChangedListener);
        setMinValue(0);
        setMaxValue(11);
        setWrapSelectorWheel(false);
    }

    public MoneyPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnValueChangedListener(onMoneyChangedListener);
        setMinValue(0);
        setMaxValue(11);
        setWrapSelectorWheel(false);
    }

    public void setMinimalAmount(int minimalAmount) {
        this.minimalAmount = minimalAmount;
        onMoneyChangedListener.updateValues();
    }

    public int getSpecifiedAmount() {
        return Integer.parseInt(onMoneyChangedListener.values[getValue()]);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        super.onSaveInstanceState();
        return new SavedInstanceState(minimalAmount, onMoneyChangedListener.numberPickerStep, getValue());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(null);
        onMoneyChangedListener.numberPickerStep = ((SavedInstanceState) state).numberPickerStep;
        onMoneyChangedListener.updateValues();
        setValue(((SavedInstanceState) state).cValue);
    }

    static class SavedInstanceState implements Parcelable {
        public static final Creator<SavedInstanceState> CREATOR = new Creator<SavedInstanceState>() {
            @Override
            public SavedInstanceState createFromParcel(Parcel source) {
                return new SavedInstanceState(source.readInt(), source.readInt(), source.readInt());
            }

            @Override
            public SavedInstanceState[] newArray(int size) {
                return new SavedInstanceState[size];
            }
        };
        final int minimalAmount;
        final int numberPickerStep;
        final int cValue;

        SavedInstanceState(int minimalAmount, int numberPickerStep, int cValue) {
            this.minimalAmount = minimalAmount;
            this.numberPickerStep = numberPickerStep;
            this.cValue = cValue;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(minimalAmount);
            dest.writeInt(numberPickerStep);
            dest.writeInt(cValue);
        }
    }

    class OnMoneyChangedListener implements OnValueChangeListener {
        final String[] values = new String[12];
        int numberPickerStep = 1;

        {
            updateValues();
            setDisplayedValues(values);
        }

        private void updateValues() {
            values[0] = Integer.toString(minimalAmount + numberPickerStep / 10 * 9);
            for (int i = 1; i <= 10; i++)
                values[i] = Integer.toString(minimalAmount + numberPickerStep * i);
            values[11] = Integer.toString(minimalAmount + numberPickerStep * 20);
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
    }
}
