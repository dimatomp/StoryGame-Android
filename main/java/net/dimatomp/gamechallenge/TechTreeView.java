package net.dimatomp.gamechallenge;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import ru.ifmo.ctddev.games.messages.TreeMessage;

/**
 * Created by dimatomp on 21.09.14.
 */
public class TechTreeView extends View {
    TreeMessage tree;
    Path progressPath, ellipsePath, linePath;
    Paint progressPaint, linePaint, ellipsePaint, textPaint;
    GestureDetector detector;
    ScaleGestureDetector scaleDetector;
    Matrix matrix = new Matrix();
    float[] textCrd;
    float textScale = 1;
    int root;
    Map<Integer, Integer> idToIndex;
    LinkedList<Integer> children[];

    public TechTreeView(Context context) {
        super(context);
        initialize();
    }

    public TechTreeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public TechTreeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        super.onSaveInstanceState();
        onSizeChanged(0, 0, getWidth(), getHeight());
        return new SavedInstanceState(tree, matrix, textScale);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(null);
        SavedInstanceState savedInstanceState = (SavedInstanceState) state;
        matrix.setValues(savedInstanceState.values);
        textScale = savedInstanceState.textScale;
        textPaint.setTextSize(textPaint.getTextSize() * textScale);
        setTree(savedInstanceState.tree);
    }

    private void transformBy(Matrix matrix, boolean restore) {
        if (!restore)
            this.matrix.postConcat(matrix);
        if (tree == null)
            return;
        progressPath.transform(matrix);
        ellipsePath.transform(matrix);
        linePath.transform(matrix);
        matrix.mapPoints(textCrd);
        invalidate();
    }

    private void scaleBy(float x, float y, float factor) {
        textPaint.setTextSize(textPaint.getTextSize() * factor);
        textScale *= factor;
        Matrix matrix = new Matrix();
        matrix.setScale(factor, factor, x, y);
        transformBy(matrix, false);
    }

    private void moveBy(float dx, float dy) {
        Matrix matrix = new Matrix();
        matrix.setTranslate(dx, dy);
        transformBy(matrix, false);
    }

    public void setTree(TreeMessage tree) {
        this.tree = tree;
        children = new LinkedList[tree.getId().length];
        idToIndex = new HashMap<>();
        for (int i = 0; i < children.length; i++) {
            idToIndex.put(tree.getId()[i], i);
            children[i] = new LinkedList<>();
        }
        for (int i = 0; i < children.length; i++) {
            Integer parent = idToIndex.get(tree.getParent()[i]);
            if (parent != null)
                children[parent].add(i);
            else
                root = i;
        }
        buildProgressPath(root);
        if (progressPath != null)
            progressPath.close();
        else
            progressPath = new Path();
        float width = getResources().getDimension(R.dimen.tree_node_width);
        float height = getResources().getDimension(R.dimen.tree_node_height);
        RectF ovalRect = new RectF(-width / 2, -height / 2, width / 2, height / 2);
        ellipsePath = new Path();
        linePath = new Path();
        textCrd = new float[children.length * 2];
        for (int i = 0; i < children.length; i++) {
            textCrd[2 * i] = tree.getX()[i];
            textCrd[2 * i + 1] = tree.getY()[i];
            ovalRect.offset((float) tree.getX()[i], (float) tree.getY()[i]);
            ellipsePath.addOval(ovalRect, Path.Direction.CW);
            ovalRect.offset(-(float) tree.getX()[i], -(float) tree.getY()[i]);
            Integer parent = idToIndex.get(tree.getParent()[i]);
            if (parent != null) {
                linePath.moveTo(tree.getX()[parent], tree.getY()[parent]);
                linePath.lineTo(tree.getX()[i], tree.getY()[i]);
            }
        }
        transformBy(matrix, true);
    }

    private void buildProgressPath(int cNode) {
        if (tree.getProgress()[cNode] < 100) {
            int parent = idToIndex.get(tree.getParent()[cNode]);
            double x = tree.getX()[parent] + (tree.getX()[cNode] - tree.getX()[parent]) * tree.getProgress()[cNode] / 100;
            double y = tree.getY()[parent] + (tree.getY()[cNode] - tree.getY()[parent]) * tree.getProgress()[cNode] / 100;
            if (progressPath == null) {
                progressPath = new Path();
                progressPath.moveTo((float) x, (float) y);
            } else
                progressPath.lineTo((float) x, (float) y);
        } else
            for (int child : children[cNode])
                buildProgressPath(child);
    }

    private void initialize() {
        progressPaint = new Paint();
        progressPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        progressPaint.setColor(getResources().getColor(android.R.color.holo_blue_bright));
        progressPaint.setAlpha(127);
        progressPaint.setAntiAlias(true);
        ellipsePaint = new Paint();
        ellipsePaint.setColor(getResources().getColor(android.R.color.holo_green_light));
        ellipsePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        ellipsePaint.setAntiAlias(true);
        linePaint = new Paint();
        linePaint.setColor(getResources().getColor(android.R.color.white));
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        textPaint = new Paint();
        textPaint.setTextSize(5);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        detector = new GestureDetector(getContext(), new MoveListener());
        scaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        moveBy((w - oldw) / 2f, (h - oldh) / 2f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (tree == null)
            return;
        canvas.drawPath(linePath, linePaint);
        canvas.drawPath(progressPath, progressPaint);
        canvas.drawPath(ellipsePath, ellipsePaint);
        for (int i = 0; i < tree.getId().length; i++)
            canvas.drawText(tree.getName()[i] + String.format(": %.0f%%", tree.getProgress()[i]), textCrd[2 * i], textCrd[2 * i + 1] - (textPaint.descent() + textPaint.ascent()) / 2, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean move = detector.onTouchEvent(event);
        boolean scale = scaleDetector.onTouchEvent(event);
        return super.onTouchEvent(event) || move || scale;
    }

    static class SavedInstanceState implements Parcelable {
        public static final Creator<SavedInstanceState> CREATOR = new Creator<SavedInstanceState>() {
            @Override
            public SavedInstanceState createFromParcel(Parcel source) {
                TreeMessage tree = source.readParcelable(TreeMessage.class.getClassLoader());
                float[] values = new float[9];
                source.readFloatArray(values);
                return new SavedInstanceState(tree, values, source.readFloat());
            }

            @Override
            public SavedInstanceState[] newArray(int size) {
                return new SavedInstanceState[size];
            }
        };
        final TreeMessage tree;
        final float values[];
        final float textScale;

        SavedInstanceState(TreeMessage tree, Matrix matrix, float textScale) {
            this.tree = tree;
            values = new float[9];
            matrix.getValues(values);
            this.textScale = textScale;
        }

        SavedInstanceState(TreeMessage tree, float[] values, float textScale) {
            this.tree = tree;
            this.values = values;
            this.textScale = textScale;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(tree, flags);
            dest.writeFloatArray(values);
            dest.writeFloat(textScale);
        }
    }

    class MoveListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            moveBy(-distanceX, -distanceY);
            return true;
        }
    }

    class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleBy(detector.getFocusX(), detector.getFocusY(), detector.getScaleFactor());
            return true;
        }
    }
}
