package com.example.gesturelock.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.gesturelock.R;

/**
 * Created by Wang Gensheng on 2015/5/10.
 */
public class GestureDetector extends View implements GestureLock.OnGestureEventListener {

    private static final String DEBUG_TAG = "GestureDetector";

    /**
     * 实时显示模式，即实时监控手势绘图；
     */
    public static final int MODE_SHOW_REAL_TIME = 1;
    /**
     * 单次显示模式：即手势完成后显示；
     */
    public static final int MODE_SHOW_ONCE = 2;

    private int showMode = MODE_SHOW_REAL_TIME;

    private Bitmap mBitmapDefault;

    private Bitmap mBitmapDetected;

    private GestureLock gestureLock;

    private int mBlockWidth;

    private static final int DOT_GAP = 12;

    private int depth = GestureLock.DEPTH;

    private boolean[] detectedPositions;

    private Paint paint = new Paint();

    public GestureDetector(Context context) {
        this(context, null);
    }

    public GestureDetector(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureDetector(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mBitmapDefault = getBitmapFor(R.drawable.ic_dot_indi_off);
        mBitmapDetected = getBitmapFor(R.drawable.ic_dot_indi_on);

        detectedPositions = new boolean[depth * depth];
        for (int i = 0; i < detectedPositions.length; i++) {
            detectedPositions[i] = false;
        }
    }

    private Bitmap getBitmapFor(int resId) {
        return BitmapFactory.decodeResource(getContext().getResources(), resId);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < detectedPositions.length; i++) {
            Bitmap temp = detectedPositions[i] ? mBitmapDetected : mBitmapDefault;
            int left = (i % depth) * (mBlockWidth + DOT_GAP);
            int top = (i / depth) * (mBlockWidth + DOT_GAP);
            canvas.drawBitmap(temp,left, top,paint);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int lockWidth = mBitmapDefault.getWidth() > mBitmapDetected.getWidth() ? mBitmapDefault.getWidth() : mBitmapDetected.getWidth();
        int lockHeight = mBitmapDefault.getHeight() > mBitmapDetected.getHeight() ? mBitmapDefault.getHeight() : mBitmapDetected.getHeight();
        int lockSize = lockWidth > lockHeight ? lockHeight : lockWidth;
        mBlockWidth = lockWidth = lockHeight = lockSize;
        lockWidth = depth * lockWidth + (depth - 1) * DOT_GAP;
        lockHeight = depth * lockHeight + (depth - 1) * DOT_GAP;
        setMeasuredDimension(lockWidth, lockHeight);
    }

    public GestureLock getGestureLock() {
        return gestureLock;
    }

    public void setGestureLock(GestureLock gestureLock) {
        this.gestureLock = gestureLock;
        this.gestureLock.setOnGestureEventListener(this);
    }

    @Override
    public void onBlockSelected(int position) {
        if (showMode == MODE_SHOW_REAL_TIME) {
            detectedPositions[position] = true;
            invalidate();
        }
    }

    @Override
    public void onGestureEvent(boolean matched) {

    }

    @Override
    public void onPatternComplete(String patternCode) {
        Log.d(DEBUG_TAG, "patternCode:" + patternCode);

        boolean[] dp = detectedPositions;
        for (int i = 0; i < dp.length; i++) {
            dp[i] = false;
        }

        if (showMode == MODE_SHOW_ONCE) {
            int[] patternMetrix = GestureLock.parseCode(patternCode);
            for (int m : patternMetrix) {
                dp[m] = true;
            }
        }
        invalidate();
    }

    @Override
    public void onPatternClear() {
        clearDetection();
    }

    @Override
    public void onUnmatchedExceedBoundary() {

    }

    private void clearDetection() {
        boolean[] dp = detectedPositions;
        for (int i = 0; i < dp.length; i++) {
            dp[i] = false;
        }
        invalidate();
    }

    public int getShowMode() {
        return showMode;
    }

    public void setShowMode(int showMode) {
        this.showMode = showMode;
    }
}
