package net.dimatomp.gamechallenge;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.RadioButton;

/**
 * Created by dimatomp on 15.09.14.
 */
public class AdapterRadioGroup extends AdapterView<CursorAdapter> implements CheckBox.OnCheckedChangeListener {
    CursorAdapter adapter;

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
            int selView = -1;
            int minWidth = 0;
            int minHeight = 0;
            for (int i = 0; i < adapter.getCount(); i++) {
                View newChild = adapter.getView(i, null, this);
                if (((RadioButton) newChild).isChecked())
                    selView = i;
                ((RadioButton) newChild).setOnCheckedChangeListener(this);
                addAndMeasureChild(newChild);
                minWidth = Math.max(minWidth, newChild.getMeasuredWidth());
                minHeight += newChild.getMeasuredHeight();
            }
            setMinimumWidth(minWidth);
            setMinimumHeight(minHeight);
            if (selView != -1)
                setSelection(selView);
        }
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
        }
    }
}
