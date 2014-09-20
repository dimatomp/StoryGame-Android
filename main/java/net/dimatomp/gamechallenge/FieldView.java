package net.dimatomp.gamechallenge;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static net.dimatomp.gamechallenge.GameField.MoveDirection;

/**
 * Created by dimatomp on 08.09.14.
 */
public class FieldView extends View {
    private static final String TAG = "FieldView";
    BitmapDrawable tiles[];
    Bitmap player;
    Bitmap arrow;
    int sideLength;
    RectF rectUp, rectLeft, rectRight, rectDown, rectDig;
    Paint arrowPaint;
    Paint textPaint = new Paint();
    LevelListDrawable portraitArrow;
    BitmapDrawable landscapeArrowPad;
    private int[][] field;
    private float xDown, yDown, dx, dy, fingerDelta;
    private boolean alreadyMoved, moving;
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private GestureDetector longPressDetector;
    private Map<String, Coordinate> players;
    private String userName;

    public void setUserName(String name) {
        userName = name;
    }

    static class Coordinate {
        int x, y;

        Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public FieldView(Context context) {
        super(context);
        initialize();
    }

    public FieldView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public FieldView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    private void initialize() {
        arrow = ((BitmapDrawable) getResources().getDrawable(R.drawable.arrow)).getBitmap();
        arrowPaint = new Paint();
        arrowPaint.setAlpha(127);
        arrowPaint.setStyle(Paint.Style.STROKE);
        arrowPaint.setColor(Color.RED);
        arrowPaint.setStrokeWidth(getResources().getDimension(R.dimen.circle_thickness));
        longPressDetector = new GestureDetector(getContext(), new OnLongPressListener());
        players = new HashMap<>();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        super.onSaveInstanceState();
        return new SavedInstanceState(field, players);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(null);
        players = ((SavedInstanceState) state).players;
        setField(((SavedInstanceState) state).field, false);
    }

    static class SavedInstanceState implements Parcelable {
        int[][] field;
        Map<String, Coordinate> players;

        SavedInstanceState(int[][] field, Map<String, Coordinate> players) {
            this.field = field;
            this.players = players;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(field.length);
            dest.writeInt(field[0].length);
            for (int[] a: field)
                dest.writeIntArray(a);
            dest.writeInt(players.size());
            for (Map.Entry<String, Coordinate> entry: players.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeInt(entry.getValue().x);
                dest.writeInt(entry.getValue().y);
            }
        }

        public static final Creator<SavedInstanceState> CREATOR = new Creator<SavedInstanceState>() {
            @Override
            public SavedInstanceState createFromParcel(Parcel source) {
                int[][] field = new int[source.readInt()][source.readInt()];
                for (int i = 0; i < field.length; i++)
                    source.readIntArray(field[i]);
                Map<String, Coordinate> players = new HashMap<>();
                for (int i = source.readInt(); i > 0; i--) {
                    players.put(source.readString(), new Coordinate(source.readInt(), source.readInt()));
                }
                return new SavedInstanceState(field, players);
            }

            @Override
            public SavedInstanceState[] newArray(int size) {
                return new SavedInstanceState[size];
            }
        };
    }

    private void loadTileImages(int sideLength) {
        if (sideLength == 0)
            return;
        this.sideLength = sideLength;
        Bitmap tiles[] = new Bitmap[]{
                BitmapFactory.decodeResource(getResources(), R.drawable.ground0),
                BitmapFactory.decodeResource(getResources(), R.drawable.ground1),
                BitmapFactory.decodeResource(getResources(), R.drawable.ground2),
                BitmapFactory.decodeResource(getResources(), R.drawable.shop),
                BitmapFactory.decodeResource(getResources(), R.drawable.water),
        };
        Canvas canvas = new Canvas();
        this.tiles = new BitmapDrawable[tiles.length];
        for (int i = 0; i < tiles.length; i++) {
            Bitmap bitmap;
            switch (i) {
                case 0:
                case 1:
                    bitmap = Bitmap.createScaledBitmap(tiles[i].copy(Bitmap.Config.ARGB_8888, true), 150, 150, true);
                    bitmap.setHasAlpha(true);
                    for (int x = 0; x < 25; x++)
                        for (int y = x; y < 150 - x; y++) {
                            bitmap.setPixel(x, y, bitmap.getPixel(x, y) & 0xffffff | ((0xff * x / 25) << 24));
                            bitmap.setPixel(y, x, bitmap.getPixel(y, x) & 0xffffff | ((0xff * x / 25) << 24));
                            bitmap.setPixel(149 - x, 149 - y, bitmap.getPixel(149 - x, 149 - y) & 0xffffff | ((0xff * x / 25) << 24));
                            bitmap.setPixel(149 - y, 149 - x, bitmap.getPixel(149 - y, 149 - x) & 0xffffff | ((0xff * x / 25) << 24));
                        }
                    this.tiles[i] = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, sideLength * 5 / 4, sideLength * 5 / 4, true));
                    break;
                case 2:
                case 4:
                    this.tiles[i] = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(tiles[i], sideLength, sideLength, false));
                    break;
                case 3:
                    tiles[i] = Bitmap.createScaledBitmap(tiles[i], sideLength, sideLength, true);
                    tiles[i].setHasAlpha(true);
                    bitmap = this.tiles[0].getBitmap().copy(Bitmap.Config.ARGB_8888, true);
                    bitmap.setHasAlpha(true);
                    canvas.setBitmap(bitmap);
                    canvas.drawBitmap(tiles[i], sideLength / 8, sideLength / 8, null);
                    this.tiles[i] = new BitmapDrawable(getResources(), bitmap);
                    break;
            }
        }
        player = BitmapFactory.decodeResource(getResources(), R.drawable.player);
        player = Bitmap.createScaledBitmap(player, sideLength, sideLength, true);
    }

    public void setField(int[][] field, boolean sizeChanged) {
        if (field != null && (this.field == null || sizeChanged || this.field.length != field.length || this.field[0].length != field.length))
            loadTileImages(Math.min(getWidth() / field.length, getHeight() / field[0].length));
        this.field = field;
        if (field != null) {
            if (field[field.length / 2][field.length / 2] == 3)
                ((GameField) getContext()).setStoreVisible(true);
            else if (((GameField) getContext()).isStoreVisible())
                ((GameField) getContext()).setStoreVisible(false);
        }
        invalidate();
    }

    public int[][] getField() {
        return field;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setField(field, true);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            updateRects();
        else
            updateArrow();
    }

    public void updateArrow() {
        if (portraitArrow != null)
            return;
        fingerDelta = getResources().getDimension(R.dimen.portrait_finger_move_delta);
        portraitArrow = new LevelListDrawable();

        Path circlePath = new Path();
        circlePath.addCircle(fingerDelta * 1.5f, fingerDelta * 1.5f, fingerDelta * 0.8f, Path.Direction.CW);
        ShapeDrawable circleDrawable = new ShapeDrawable(new PathShape(circlePath, fingerDelta * 3, fingerDelta * 3));
        circleDrawable.getPaint().set(arrowPaint);
        circleDrawable.setAlpha(arrowPaint.getAlpha());
        portraitArrow.addLevel(1, 1, circleDrawable);

        arrow = Bitmap.createScaledBitmap(arrow, (int) fingerDelta, (int) fingerDelta, false);
        for (int i = 2; i <= 5; i++) {
            Canvas canvas = new Canvas();
            Bitmap bitmap = Bitmap.createBitmap(3 * (int) fingerDelta, 3 * (int) fingerDelta, Bitmap.Config.ARGB_8888);
            bitmap.setHasAlpha(true);
            canvas.setBitmap(bitmap);
            Matrix matrix = moveMatrix(3 * fingerDelta - arrow.getWidth(), fingerDelta * 1.5f - arrow.getHeight() / 2);
            matrix.postRotate(90 * (i - 2), fingerDelta * 1.5f, fingerDelta * 1.5f);
            canvas.drawBitmap(arrow, matrix, arrowPaint);
            canvas.drawCircle(fingerDelta * 1.5f, fingerDelta * 1.5f, fingerDelta * 0.8f, circleDrawable.getPaint());
            BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
            portraitArrow.addLevel(i, i, drawable);
        }
    }

    private void updateRects() {
        if (landscapeArrowPad != null)
            return;
        int h = getHeight();
        float rectSide = getResources().getDimension(R.dimen.landscape_button_size);
        rectUp = new RectF(rectSide, h - 3 * rectSide, 2 * rectSide, h - 2 * rectSide);
        rectLeft = new RectF(0, h - 2 * rectSide, rectSide, h - rectSide);
        rectRight = new RectF(2 * rectSide, h - 2 * rectSide, 3 * rectSide, h - rectSide);
        rectDown = new RectF(rectSide, h - rectSide, 2 * rectSide, h);
        rectDig = new RectF(rectSide, rectSide, 2 * rectSide, rectSide);
        arrow = Bitmap.createScaledBitmap(arrow, (int) rectSide, (int) rectSide, false);

        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap((int) rectSide * 3, (int) rectSide * 3, Bitmap.Config.ARGB_8888);
        bitmap.setHasAlpha(true);
        canvas.setBitmap(bitmap);
        Matrix matrix = moveMatrix(2 * rectSide, rectSide);
        for (int i = 0; i < 4; i++) {
            canvas.drawBitmap(arrow, matrix, arrowPaint);
            matrix.postRotate(90, rectSide * 1.5f, rectSide * 1.5f);
        }
        canvas.drawCircle(rectSide * 1.5f, rectSide * 1.5f, rectSide / 2.5f, arrowPaint);
        canvas.drawPoint(rectSide * 1.5f, rectSide * 1.5f, arrowPaint);
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
                        field.sendMoveMessage(dx > 0 ? MoveDirection.DOWN : MoveDirection.UP);
                        portraitArrow.setLevel(dx > 0 ? 2 : 4);
                    } else {
                        field.sendMoveMessage(dy > 0 ? MoveDirection.RIGHT : MoveDirection.LEFT);
                        portraitArrow.setLevel(dy > 0 ? 3 : 5);
                    }
                } else
                    portraitArrow.setLevel(1);
                invalidatePortraitArrow();
            } else {
                alreadyMoved = true;
                if (rectUp == null || rectLeft == null || rectRight == null || rectDown == null)
                    updateRects();
                if (rectUp.contains(xDown, yDown))
                    field.sendMoveMessage(MoveDirection.LEFT);
                else if (rectLeft.contains(xDown, yDown))
                    field.sendMoveMessage(MoveDirection.UP);
                else if (rectRight.contains(xDown, yDown))
                    field.sendMoveMessage(MoveDirection.DOWN);
                else if (rectDown.contains(xDown, yDown))
                    field.sendMoveMessage(MoveDirection.RIGHT);
                else
                    alreadyMoved = false;
                if (alreadyMoved)
                    Log.d(TAG, "Sending move request, orientation: landscape");
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
            longPressDetector.onTouchEvent(event);
        } else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    moving = true;
                case MotionEvent.ACTION_MOVE:
                    xDown = event.getX();
                    yDown = event.getY();
                    sendMoveRequest();
                    if (!(rectUp.contains(event.getX(), event.getY()) ||
                            rectLeft.contains(event.getX(), event.getY()) ||
                            rectDown.contains(event.getX(), event.getY()) ||
                            rectRight.contains(event.getX(), event.getY())))
                        longPressDetector.onTouchEvent(event);
                    break;
                case MotionEvent.ACTION_UP:
                    moving = false;
                    break;
            }
        }
        return true;
    }

    public void applyUserPos(String name, int x, int y) {
        boolean invalid = false;
        if (!name.equals(userName)) {
            Coordinate old = players.get(name);
            Coordinate mine = players.get(userName);
            if (old == null || (mine != null && (Math.abs(old.x - x) < field.length / 2 || Math.abs(old.y - y) < field.length / 2 ||
                    Math.abs(mine.x - x) < field.length / 2 || Math.abs(mine.y - y) < field.length / 2)))
                invalid = true;
        }
        players.put(name, new Coordinate(x, y));
        if (invalid)
            // TODO invalidateRect?
            invalidate();
    }

    public void removeUser(String name) {
        Coordinate cur = players.get(name);
        Coordinate mine = players.get(userName);
        boolean invalid = cur != null && mine != null && (Math.abs(cur.x - mine.x) < field.length / 2 || Math.abs(cur.y - mine.y) < field.length / 2);
        players.remove(name);
        if (invalid)
            // TODO invalidateRect?
            invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (field != null && tiles != null) {
            canvas.save();
            int left = (getWidth() - sideLength * field.length) / 2;
            int top = (getHeight() - sideLength * field[0].length) / 2;
            canvas.clipRect(left, top, getWidth() - left, getHeight() - top);
            canvas.concat(moveMatrix(left, top));
            for (int i = 0; i < field.length; i++)
                for (int j = 0; j < field[i].length; j++) {
                    if (field[i][j] < 2 || field[i][j] == 3)
                        tiles[field[i][j]].setBounds(sideLength * j - sideLength / 8, sideLength * i - sideLength / 8,
                                sideLength * (j + 1) + sideLength / 8, sideLength * (i + 1) + sideLength / 8);
                    else
                        tiles[field[i][j]].setBounds(sideLength * j, sideLength * i,
                                sideLength * (j + 1), sideLength * (i + 1));
                    tiles[field[i][j]].draw(canvas);
                }
            Coordinate myCrd = players.get(userName);
            if (myCrd != null)
                for (Map.Entry<String, Coordinate> entry: players.entrySet()) {
                    int screenX = sideLength * (entry.getValue().x - myCrd.x + field.length / 2);
                    int screenY = sideLength * (entry.getValue().y - myCrd.y + field[0].length / 2);
                    canvas.drawBitmap(player, screenY, screenX, null);
                    canvas.drawText(entry.getKey(), screenY, screenX, textPaint);
                }
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
                    Coordinate crd = players.get(userName);
                    crd.x += direction.dx;
                    crd.y += direction.dy;
                    setField(newField, false);
                    handleNextMove(true);
                }
            });
        }
    }

    class OnLongPressListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            ((GameField) getContext()).sendDigEvent();
        }
    }
}
