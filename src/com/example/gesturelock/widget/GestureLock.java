package com.example.gesturelock.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.gesturelock.util.MD5Util;

/**
 * 支付宝手势逻辑：
 * <ul>
 * <li>登录时必须解锁</li>
 * <li>2分钟内不加锁（时间尚未确定）</li>
 * </ul>
 */
@SuppressWarnings("unused")
public class GestureLock extends RelativeLayout {

	private static final String DEBUG_TAG = "GestureLock";
	
	private GestureLockView[] lockers;

//	private static final int MAX_WIDTH = 700;

	/**
	 * 普通锁屏模式
	 */
	public static final int MODE_NORMAL = 0;

	/**
	 * 设置模式：该模式下手势绘制过的点会有箭头指向，用于标记手势图的有向性。
	 */
	public static final int MODE_EDIT = 1;
	
	private int mode = MODE_NORMAL;
	
	public static final int DEPTH = 3;

	private boolean saveMode = false;
	private int mSavingStep;
	private static final int SAVEING_STEP_1 = 1;
	private static final int SAVING_STEP_2 = 2;

	private int[] defaultGestures = new int[]{0, 1, 2, 4, 6};
	private String securePatternCode;

	private int[] negativeGestures;			// [-1,-1,-1,-1,-1,-1,-1,-1,-1]
	private int[] gesturesContainer;

	private int gestureCursor = 0;

	public static final int FLAG_NON_DETECTED = -1;

	private Path gesturePath;
	private int lastX;
	private int lastY;
	private int lastPathX;

	private int lastPathY;
	private int blockWidth;
	private int blockGap;

	private static final int BLOCK_WIDTH_MIN = 72;
	private static final int BLOCK_GAP_MIN = 48;

	private int gestureWidth;
	private float mGapBlockRate = (float) (2.0 / 3);
	private float mBlockGapRate = 1.5F;
	private float mDrawingStrokeRate = 0.072F;

	private int drawingStrokeWidth;
	private static final int COLOR_DRAWING = 0xFFFFFFFF;

	private static final int COLOR_ERROR = 0xFFFF0000;
	private Paint paint;

	private Paint vertexDotPaint;
	private int unmatchedCount;

	private static final int unmatchedBoundary = 5;

	private boolean touchable;

//	private OnGestureEventListener onGestureEventListener;

	private OnGestureEventListener onGestureEventListener;
	private OnBlockSelectedListener onBlockSelectedListener;
	private OnGestureCompleteListener onGestureCompleteListener;


	public void setOnGestureCompleteListener(OnGestureCompleteListener onGestureCompleteListener) {
		this.onGestureCompleteListener = onGestureCompleteListener;
	}

	public void setOnBlockSelectedListener(OnBlockSelectedListener onBlockSelectedListener) {
		this.onBlockSelectedListener = onBlockSelectedListener;
	}

	public void setSaveMode(boolean saveMode) {
		this.saveMode = saveMode;
		mSavingStep = SAVEING_STEP_1;
	}

	public void reset() {
		if (saveMode) {
			mSavingStep = SAVEING_STEP_1;
		}
	}

	public interface OnGestureEventListener {
		void OnGestureEvent(boolean match);
	}

	public interface OnGestureCompleteListener {
		void onGestureComplete(String patternCode);
	}

	public interface OnBlockSelectedListener {
		void onBlockSelected(int position);
	}

	public GestureLock(Context context){
		this(context, null);
	}
	
	public GestureLock(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}
	
	public GestureLock(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		
		negativeGestures = new int[DEPTH * DEPTH];
		for(int i = 0; i < negativeGestures.length; i++) negativeGestures[i] = FLAG_NON_DETECTED;
		gesturesContainer = negativeGestures.clone();
		
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(20);				// DEFAULT
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeJoin(Paint.Join.ROUND);

		vertexDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		vertexDotPaint.setStyle(Paint.Style.FILL);
		
		unmatchedCount = 0;
		
		touchable = true;
	}
	
	public void setTouchable(boolean touchable){
		this.touchable = touchable;
	}
	
	public void rewindUnmatchedCount(){
		unmatchedCount = 0;
	}
	
	public void setOnGestureEventListener(OnGestureEventListener onGestureEventListener){
		this.onGestureEventListener = onGestureEventListener;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
//		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (heightMeasureSpec == 0) {
			heightMeasureSpec = widthMeasureSpec;
		}

		int length = 0;
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		width = width - getPaddingLeft() - getPaddingRight();
		height = height - getPaddingTop() - getPaddingBottom();
		length = width > height ? height : width;

		if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
			blockGap = BLOCK_GAP_MIN;
			blockWidth = BLOCK_WIDTH_MIN;
		} else {
			blockGap = (int) (length / (DEPTH * mBlockGapRate + DEPTH - 1));
			blockWidth = (int) (mBlockGapRate * blockGap);
		}

		gestureWidth = blockWidth * DEPTH + blockGap * (DEPTH - 1);
		drawingStrokeWidth = (int) (blockWidth * mDrawingStrokeRate);
		paint.setStrokeWidth(drawingStrokeWidth);

		width = gestureWidth + getPaddingLeft() + getPaddingRight();
		height = gestureWidth + getPaddingTop() + getPaddingBottom();
//		setMeasuredDimension(width, height);
		super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));

		if(lockers == null) {
			// As you can see:
			// block/gap => 3/2 => 1.5
			// depth * block + (depth - 1) * gap => depth * blockGapRate * gap + (depth - 1) * gap = length
			// which equals that:

			lockers = new GestureLockView[DEPTH * DEPTH];
			for(int i = 0; i < lockers.length; i++) {
				lockers[i] = new GestureLockView(getContext());
				lockers[i].setId(i + 1);
				
				LayoutParams lockerParams = new LayoutParams(blockWidth, blockWidth);
				if(i % DEPTH != 0) lockerParams.addRule(RelativeLayout.RIGHT_OF, lockers[i - 1].getId());
				if(i > (DEPTH - 1)) lockerParams.addRule(RelativeLayout.BELOW, lockers[i - DEPTH].getId());
				int rightMargin = 0;
				int bottomMargin = 0;
				if((i + 1) % DEPTH != 0) rightMargin = blockGap;
				if(i < DEPTH * (DEPTH - 1)) bottomMargin = blockGap;
				
				lockerParams.setMargins(0, 0, rightMargin, bottomMargin);
				
				addView(lockers[i], lockerParams);
				Log.d(DEBUG_TAG, "i:" + i);
				lockers[i].setMode(GestureLockView.MODE_NORMAL);
			}
		}

	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		if(touchable){
			switch(event.getActionMasked()){
			case MotionEvent.ACTION_DOWN:
				for(int i = 0; i < getChildCount(); i++){
					View c = getChildAt(i);
					if(c instanceof GestureLockView){
						((GestureLockView) c).setMode(GestureLockView.MODE_NORMAL);
					}
				}
				
				gesturePath = null;
				
				lastX = (int) event.getX();
				lastY = (int) event.getY();
				lastPathX = lastX;
				lastPathY = lastY;
				
				paint.setColor(COLOR_DRAWING);
				vertexDotPaint.setColor(COLOR_DRAWING);
				
				break;
			case MotionEvent.ACTION_MOVE:
				
				lastX = (int) event.getX();
				lastY = (int) event.getY();
				
				int cId = calculateChildIdByCoords(lastX, lastY);
				
				View child = findViewById(cId + 1);
				boolean checked = false;
				for(int id : gesturesContainer){
					if(id == cId){
						checked = true;
						break;
					}
				}
				
				if(child != null && child instanceof GestureLockView && checkChildInCoords(lastX, lastY, child)){

					if(!checked){

						int checkedX = child.getLeft() + child.getWidth() / 2;
						int checkedY = child.getTop() + child.getHeight() / 2;
						if(gesturePath == null){
							gesturePath = new Path();
							gesturePath.moveTo(checkedX, checkedY);
						}else{
							gesturePath.lineTo(checkedX, checkedY);
						}
						gesturesContainer[gestureCursor] = cId;
						gestureCursor++;

						int mode = GestureLockView.MODE_SELECTED;
						int hDirect = getDirection(checkedX, lastPathX);
						int vDirect = getDirection(checkedY, lastPathY);
						mode |= getDrawingDirection(hDirect, vDirect);

						((GestureLockView) child).setMode(mode);
						
						lastPathX = checkedX;
						lastPathY = checkedY;
						
						if(onBlockSelectedListener != null) onBlockSelectedListener.onBlockSelected(cId);
					}
				}
				
				invalidate();
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				
				if(gesturesContainer[0] != FLAG_NON_DETECTED){
					if (!saveMode) {
						boolean matched = true;
						/*
						if (gestureCursor == defaultGestures.length) {
							for (int j = 0; j < defaultGestures.length; j++) {
								if (gesturesContainer[j] != defaultGestures[j]) {
									matched = false;
									break;
								}
							}
						} else {
							matched = false;
						}*/
						matched = MD5Util.compareMD5String(this.securePatternCode, patternToSecureCode());

						if (!matched && mode != MODE_EDIT) {
							unmatchedCount++;
							paint.setColor(COLOR_ERROR);
							vertexDotPaint.setColor(COLOR_ERROR);
							for (int k : gesturesContainer) {
								View selectedChild = findViewById(k + 1);
								if (selectedChild != null && selectedChild instanceof GestureLockView) {
									((GestureLockView) selectedChild).setMode(GestureLockView.MODE_ERROR);
								}
							}

							postDelayed(new Runnable() {
								@Override
								public void run() {
									clearPattern();
								}
							}, 2000);
						} else {
							unmatchedCount = 0;
						}


						if (onGestureEventListener != null) {
							onGestureEventListener.OnGestureEvent(matched);
//						if(unmatchedCount >= unmatchedBoundary){
//							onGestureEventListener.onUnmatchedExceedBoundary();
//							unmatchedCount = 0;
//						}
						}
					} else {
						if (gestureCursor < 4) {
							Toast.makeText(getContext(), "至少连接4个点，请重新输入。", Toast.LENGTH_SHORT).show();
						} else {
							if (onGestureCompleteListener != null) {
								onGestureCompleteListener.onGestureComplete(patternToCode());
							}
							setSecurePatternCode(patternToSecureCode());
							saveMode = false;
//							mSavingStep = SAVING_STEP_2;
						}
						clearPattern();
						break;
					}
				}

				gestureCursor = 0;
				gesturesContainer = negativeGestures.clone();

				lastX = lastPathX;
				lastY = lastPathY;

				invalidate();

				break;
			}
		}
		
		return true;
	}

	private int getDirection(int checkedX, int lastPathX) {
		int dir = checkedX - lastPathX;
		if (dir < 0) {
			dir = -1;
		} else if (dir > 0) {
			dir = 1;
		} else {
			dir = 0;
		}
		return dir;
	}

	private int getDrawingDirection(int hDirect, int vDirect) {
		int arrow;
		if (hDirect == -1) {
			if (vDirect == -1) {
				arrow = GestureLockView.ARROW_LEFT_TOP;
			} else if (vDirect == 0) {
				arrow = GestureLockView.ARROW_LEFT;
			} else {
				arrow = GestureLockView.ARROW_BOTTOM_LEFT;
			}
		} else if (hDirect == 0) {
			if (vDirect == -1) {
				arrow = GestureLockView.ARROW_TOP;
			} else if (vDirect == 0) {
				arrow = GestureLockView.ARROW_SELF;
			} else {
				arrow = GestureLockView.ARROW_BOTTOM;
			}
		} else {
			if (vDirect == -1) {
				arrow = GestureLockView.ARROW_TOP_RIGHT;
			} else if (vDirect == 0) {
				arrow = GestureLockView.ARROW_RIGHT;
			} else {
				arrow = GestureLockView.ARROW_RIGHT_BOTTOM;
			}
		}
		return arrow;
	}

	public void clearPattern() {
		for(int i = 0; i < getChildCount(); i++){
			View c = getChildAt(i);
			if(c instanceof GestureLockView){
				((GestureLockView) c).setMode(GestureLockView.MODE_NORMAL);
			}
		}
		gestureCursor = 0;
		gesturesContainer = negativeGestures.clone();

		lastX = lastPathX = -1;
		lastY = lastPathY = -1;

		gesturePath = null;
		invalidate();
	}

	/**
	 *
	 * @return
	 */
	@Deprecated
	public String patternToCode() {
		StringBuilder builder = new StringBuilder();
		builder.append(DEPTH + "@");
		int[] cells = gesturesContainer;
		int fixedWidth = new String("" + DEPTH * DEPTH).length();
		for (int i = 0; i < cells.length; i++) {
			if (cells[i] != FLAG_NON_DETECTED) {
				builder.append(String.format("%" + fixedWidth + "d", cells[i]));
			}
		}
		return builder.toString();
	}

	public String patternToSecureCode() {
		return MD5Util.md5(patternToCode());
	}

	@Deprecated
	public static int[] parseCode(String code) {
		int flag;
		if (TextUtils.isEmpty(code) || (flag = code.indexOf("@")) <= 0) {
			throw new IllegalArgumentException();
		}
		int depth = Integer.parseInt(code.substring(0, flag));
		int fixedWidth = new String("" + depth * depth).length();

		if (depth < 0) {
			throw new NumberFormatException();
		} else if (depth == 0){
			return null;
		}

		String rawCode = code.substring(flag + 1, code.length());
		int[] res = new int[rawCode.length() / fixedWidth];
		for (int i = 0; i<res.length; i++) {
			res[i] = Integer.parseInt(rawCode.substring(i * fixedWidth, (i + 1) * fixedWidth));
		}

		return res;
	}

	@Deprecated
	public void setCorrectGesture(int[] correctGestures){
		defaultGestures = correctGestures;
	}

	public void setSecurePatternCode(String spc) {
		this.securePatternCode = spc;
	}

	@Deprecated
	public void setCorrectGestureCode(String code) {
		if (code.indexOf("@") == -1) {
			code = String.format("%d@%s", DEPTH, code);
		}
		setCorrectGesture(parseCode(code));
	}
	
	public void setMode(int mode){
		this.mode = mode;
	}
	
	private int calculateChildIdByCoords(int x, int y){
		if(x >= 0 && x <= gestureWidth && y >= 0 && y <= gestureWidth){
			int rowX = (int) (((float) x / (float) gestureWidth) * DEPTH);
			int rowY = (int) (((float) y / (float) gestureWidth) * DEPTH);
			
			return rowX + (rowY * DEPTH);
		}
		
		return -1;
	}
	
	private boolean checkChildInCoords(int x, int y, View child){
		if(child != null){
			int centerX = child.getLeft() + child.getWidth() / 2;
			int centerY = child.getTop() + child.getHeight() / 2;
			
			int dx = centerX - x;
			int dy = centerY - y;
			
			int radius = child.getWidth() > child.getHeight() ? child.getHeight() : child.getWidth();
			radius /= 2;
			if(dx * dx + dy * dy < radius * radius) return true;
		}
		
		return false;
	}
	
	@Override
	public void dispatchDraw(Canvas canvas){
		super.dispatchDraw(canvas);
		
		if(gesturePath != null){
			 canvas.drawPath(gesturePath, paint);
		}
		
		if(gesturesContainer[0] != FLAG_NON_DETECTED) canvas.drawLine(lastPathX, lastPathY, lastX, lastY, paint);
	}
}
