package net.dimatomp.gamechallenge;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static net.dimatomp.gamechallenge.GameField.MoveDirection;

/**
 * Created by dimatomp on 08.09.14.
 */
public class FieldView extends View {
    private int[][] field;
    Bitmap tiles[];
    int sideLength;

    private void loadTileImages(int sideLength) {
        if (sideLength == 0)
            return;
        this.sideLength = sideLength;
        tiles = new Bitmap[] {
                ((BitmapDrawable) getResources().getDrawable(R.drawable.ground0)).getBitmap(),
                ((BitmapDrawable) getResources().getDrawable(R.drawable.ground1)).getBitmap(),
                ((BitmapDrawable) getResources().getDrawable(R.drawable.ground2)).getBitmap()
        };
        for (int i = 0; i < tiles.length; i++)
            tiles[i] = Bitmap.createScaledBitmap(tiles[i], sideLength, sideLength, false);
    }

    public void setField(int[][] field, boolean sizeChanged) {
        if (field != null && (this.field == null || sizeChanged || this.field.length != field.length || this.field[0].length != field.length))
            loadTileImages(Math.min(getWidth() / field.length, getHeight() / field[0].length));
        this.field = field;
        invalidate();
    }

    public FieldView(Context context) {
        super(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setField(field, true);
    }

    Matrix moveMatrix(float dx, float dy) {
        Matrix matrix = new Matrix();
        matrix.setTranslate(dx, dy);
        return matrix;
    }

    private static final String TAG = "FieldView";

    private float xDown, yDown, dx, dy;
    private boolean alreadyMoved;

    private void sendMoveRequest() {
        if (!alreadyMoved && Math.max(Math.abs(dx), Math.abs(dy)) > getResources().getDimension(R.dimen.finger_move_delta)) {
            alreadyMoved = true;
            if (Math.abs(dx) > Math.abs(dy))
                ((GameField) getContext()).sendMoveMessage(dx > 0 ? MoveDirection.RIGHT : MoveDirection.LEFT);
            else
                ((GameField) getContext()).sendMoveMessage(dy > 0 ? MoveDirection.DOWN : MoveDirection.UP);
        }
    }

    public void handleNextMove(boolean immediately) {
        alreadyMoved = false;
        if (immediately)
            sendMoveRequest();
    }

    public void addLayer(MoveDirection direction, int[] layer, int speed) {
        executorService.schedule(new DelayedMove(direction, layer), speed, TimeUnit.MILLISECONDS);
    }

    class DelayedMove implements Runnable {
        final MoveDirection direction;
        final int[] layer;

        DelayedMove(MoveDirection direction, int[] layer) {
            this.direction = direction;
            this.layer = layer;
        }

        @Override
        public void run() {
            post(new Runnable() {
                @Override
                public void run() {
                    int[][] newField;
                    switch (direction) {
                        case UP:
                            newField = new int[field.length][field[0].length];
                            for (int i = 0; i < field.length; i++)
                                for (int j = 0; j < field[i].length - 1; j++)
                                    newField[i][j + 1] = field[i][j];
                            for (int i = 0; i < field.length; i++)
                                newField[i][0] = layer[i];
                            break;
                        case DOWN:
                            newField = new int[field.length][field[0].length];
                            for (int i = 0; i < field.length; i++)
                                for (int j = 1; j < field[i].length; j++)
                                    newField[i][j - 1] = field[i][j];
                            for (int i = 0; i < field.length; i++)
                                newField[i][field[i].length - 1] = layer[i];
                            break;
                        case LEFT:
                            newField = new int[field.length][];
                            newField[0] = layer;
                            for (int i = 1; i < newField.length; i++)
                                newField[i] = field[i - 1];
                            break;
                        case RIGHT:
                            newField = new int[field.length][];
                            for (int i = 0; i < newField.length - 1; i++)
                                newField[i] = field[i + 1];
                            newField[newField.length - 1] = layer;
                            break;
                        default:
                            newField = null;
                            break;
                    }
                    setField(newField, false);
                    handleNextMove(true);
                }
            });
        }
    }

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        //Log.v(TAG, "Caught touch event " + event);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    xDown = event.getX();
                    yDown = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    dx = event.getX() - xDown;
                    dy = event.getY() - yDown;
                    sendMoveRequest();
                    break;
                case MotionEvent.ACTION_UP:
                    dx = 0;
                    dy = 0;
                    break;
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (field != null && tiles != null) {
            canvas.concat(moveMatrix((getWidth() - sideLength * field.length) / 2f, (getHeight() - sideLength * field[0].length) / 2f));
            for (int i = 0; i < field.length; i++)
                for (int j = 0; j < field[i].length; j++)
                    canvas.drawBitmap(tiles[field[i][j]], moveMatrix(sideLength * i, sideLength * j), null);
        }
    }
}
