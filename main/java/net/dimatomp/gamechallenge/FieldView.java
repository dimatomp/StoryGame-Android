package net.dimatomp.gamechallenge;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.util.Log;
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
    private static final String TAG = "FieldView";
    Bitmap tiles[], arrow;
    int sideLength;
    RectF rectUp, rectLeft, rectRight, rectDown;
    Paint arrowPaint;
    LevelListDrawable portraitArrow;
    BitmapDrawable landscapeArrowPad;
    private int[][] field;
    private float xDown, yDown, dx, dy, fingerDelta;
    private boolean alreadyMoved, moving;
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public FieldView(Context context) {
        super(context);
    }

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

    public int[][] getField() {
        return field;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setField(field, true);
        arrow = ((BitmapDrawable) getResources().getDrawable(R.drawable.arrow)).getBitmap();
        arrowPaint = new Paint();
        arrowPaint.setAlpha(127);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            updateRects();
        else
            updateArrow();
    }

    public void updateArrow() {
        fingerDelta = getResources().getDimension(R.dimen.portrait_finger_move_delta);
        portraitArrow = new LevelListDrawable();

        Path circlePath = new Path();
        circlePath.addCircle(fingerDelta * 1.5f, fingerDelta * 1.5f, fingerDelta * 0.8f, Path.Direction.CW);
        ShapeDrawable circleDrawable = new ShapeDrawable(new PathShape(circlePath, fingerDelta * 3, fingerDelta * 3));
        circleDrawable.getPaint().setColor(Color.RED);
        circleDrawable.getPaint().setStyle(Paint.Style.STROKE);
        circleDrawable.getPaint().setStrokeWidth(getResources().getDimension(R.dimen.portrait_circle_thickness));
        circleDrawable.getPaint().setAlpha(arrowPaint.getAlpha());
        //circleDrawable.setBounds(0, 0, 2 * (int) fingerDelta, 2 * (int) fingerDelta);
        portraitArrow.addLevel(1, 1, circleDrawable);

        arrow = Bitmap.createScaledBitmap(arrow, (int) fingerDelta, (int) fingerDelta, false);
        for (int i = 2; i <= 5; i++) {
            Canvas canvas = new Canvas();
            Bitmap bitmap = Bitmap.createBitmap(3 * (int) fingerDelta, 3 * (int) fingerDelta, Bitmap.Config.ARGB_8888);
            canvas.setBitmap(bitmap);
            Matrix matrix = moveMatrix(3 * fingerDelta - arrow.getWidth(), fingerDelta * 1.5f - arrow.getHeight() / 2);
            matrix.postRotate(90 * (i - 2), fingerDelta * 1.5f, fingerDelta * 1.5f);
            canvas.drawBitmap(arrow, matrix, arrowPaint);
            canvas.drawCircle(fingerDelta * 1.5f, fingerDelta * 1.5f, fingerDelta * 0.8f, circleDrawable.getPaint());
            BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
            //drawable.setBounds(0, 0, 2 * (int) fingerDelta, 2 * (int) fingerDelta);
            portraitArrow.addLevel(i, i, drawable);
        }
        //portraitArrow.setBounds(0, 0, 2 * (int) fingerDelta, 2 * (int) fingerDelta);
    }

    private void updateRects() {
        int h = getHeight();
        float rectSide = getResources().getDimension(R.dimen.landscape_button_size);
        rectUp = new RectF(rectSide, h - 3 * rectSide, 2 * rectSide, h - 2 * rectSide);
        rectLeft = new RectF(0, h - 2 * rectSide, rectSide, h - rectSide);
        rectRight = new RectF(2 * rectSide, h - 2 * rectSide, 3 * rectSide, h - rectSide);
        rectDown = new RectF(rectSide, h - rectSide, 2 * rectSide, h);
        arrow = Bitmap.createScaledBitmap(arrow, (int) rectSide, (int) rectSide, false);

        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap((int) rectSide * 3, (int) rectSide * 3, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        Matrix matrix = moveMatrix(2 * rectSide, rectSide);
        for (int i = 0; i < 4; i++) {
            canvas.drawBitmap(arrow, matrix, arrowPaint);
            matrix.postRotate(90, rectSide * 1.5f, rectSide * 1.5f);
        }
        landscapeArrowPad = new BitmapDrawable(getResources(), bitmap);
        landscapeArrowPad.setBounds(0, h - 3 * (int) rectSide, 3 * (int) rectSide, h);
    }

    Matrix moveMatrix(float dx, float dy) {
        Matrix matrix = new Matrix();
        matrix.setTranslate(dx, dy);
        return matrix;
    }

    private void sendMoveRequest() {
        if (moving && !alreadyMoved) {
            GameField field = (GameField) getContext();
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (Math.max(Math.abs(dx), Math.abs(dy)) > getResources().getDimension(R.dimen.portrait_finger_move_delta)) {
                    alreadyMoved = true;
                    Log.d(TAG, "Sending move request, orientation: portrait");
                    if (Math.abs(dx) > Math.abs(dy)) {
                        field.sendMoveMessage(dx > 0 ? MoveDirection.RIGHT : MoveDirection.LEFT);
                        portraitArrow.setLevel(dx > 0 ? 2 : 4);
                    } else {
                        field.sendMoveMessage(dy > 0 ? MoveDirection.DOWN : MoveDirection.UP);
                        portraitArrow.setLevel(dy > 0 ? 3 : 5);
                    }
                } else
                    portraitArrow.setLevel(1);
                invalidatePortraitArrow();
            } else {
                Log.d(TAG, "Sending move request, orientation: landscape");
                alreadyMoved = true;
                if (rectUp == null || rectLeft == null || rectRight == null || rectDown == null)
                    updateRects();
                if (rectUp.contains(xDown, yDown))
                    field.sendMoveMessage(MoveDirection.UP);
                else if (rectLeft.contains(xDown, yDown))
                    field.sendMoveMessage(MoveDirection.LEFT);
                else if (rectRight.contains(xDown, yDown))
                    field.sendMoveMessage(MoveDirection.RIGHT);
                else if (rectDown.contains(xDown, yDown))
                    field.sendMoveMessage(MoveDirection.DOWN);
                else
                    alreadyMoved = false;
            }
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

    private void invalidatePortraitArrow() {
        invalidate((int) (xDown - fingerDelta * 1.5f), (int) (yDown - fingerDelta * 1.5f),
                (int) (xDown + fingerDelta * 1.5f), (int) (yDown + fingerDelta * 1.5f));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        //Log.v(TAG, "Caught touch event " + event);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    xDown = event.getX();
                    yDown = event.getY();
                    portraitArrow.setLevel(1);
                    invalidatePortraitArrow();
                    moving = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    dx = event.getX() - xDown;
                    dy = event.getY() - yDown;
                    sendMoveRequest();
                    break;
                case MotionEvent.ACTION_UP:
                    moving = false;
                    portraitArrow.setLevel(0);
                    invalidatePortraitArrow();
                    break;
            }
        } else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    moving = true;
                case MotionEvent.ACTION_MOVE:
                    xDown = event.getX();
                    yDown = event.getY();
                    sendMoveRequest();
                    break;
                case MotionEvent.ACTION_UP:
                    moving = false;
                    break;
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (field != null && tiles != null) {
            canvas.save();
            canvas.concat(moveMatrix((getWidth() - sideLength * field.length) / 2f, (getHeight() - sideLength * field[0].length) / 2f));
            for (int i = 0; i < field.length; i++)
                for (int j = 0; j < field[i].length; j++)
                    canvas.drawBitmap(tiles[field[i][j]], moveMatrix(sideLength * i, sideLength * j), null);
            canvas.restore();
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            landscapeArrowPad.draw(canvas);
        else {
            portraitArrow.setBounds(
                    (int) (xDown - fingerDelta * 1.5f),
                    (int) (yDown - fingerDelta * 1.5f),
                    (int) (xDown + fingerDelta * 1.5f),
                    (int) (yDown + fingerDelta * 1.5f));
            portraitArrow.draw(canvas);
        }
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
                                System.arraycopy(field[i], 0, newField[i], 1, field[i].length - 1);
                            for (int i = 0; i < field.length; i++)
                                newField[i][0] = layer[i];
                            break;
                        case DOWN:
                            newField = new int[field.length][field[0].length];
                            for (int i = 0; i < field.length; i++)
                                System.arraycopy(field[i], 1, newField[i], 0, field[i].length - 1);
                            for (int i = 0; i < field.length; i++)
                                newField[i][field[i].length - 1] = layer[i];
                            break;
                        case LEFT:
                            newField = new int[field.length][];
                            newField[0] = layer;
                            System.arraycopy(field, 0, newField, 1, field.length - 1);
                            break;
                        case RIGHT:
                            newField = new int[field.length][];
                            System.arraycopy(field, 1, newField, 0, field.length - 1);
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
}
