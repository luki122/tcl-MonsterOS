package com.monster.market.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import com.monster.market.R;


public class RoundProgressView extends View {

	/**
	 * 画笔对象的引用
	 */
	private Paint paint;

	/**
	 * 圆环的颜色
	 */
	private int roundColor;

	/**
	 * 圆环进度的颜色
	 */
	private int roundProgressColor;

	/**
	 * 圆环的宽度
	 */
	private float roundWidth;
	
	/**
	 * 当前进度
	 */
	private int progress;
	
	/**
	 * 最大进度
	 */
	private int max;
	
	private RectF oval;
	private int centre;
	private float radius;
	
	private boolean needClear = false;;
	
	public RoundProgressView(Context context) {
		this(context, null);
	}

	public RoundProgressView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RoundProgressView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		paint = new Paint();

		TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
				R.styleable.RoundProgressView);

		// 获取自定义属性和默认值
		roundColor = mTypedArray.getColor(
				R.styleable.RoundProgressView_roundColor, Color.TRANSPARENT);
		roundProgressColor = mTypedArray.getColor(
				R.styleable.RoundProgressView_roundProgressColor, Color.GREEN);
		roundWidth = mTypedArray.getDimension(
				R.styleable.RoundProgressView_roundWidth, 5);
		progress = mTypedArray.getInteger(R.styleable.RoundProgressView_progress, 0);
		max = mTypedArray.getInteger(R.styleable.RoundProgressView_max, 100);

		mTypedArray.recycle();
	}
	
	private void init() {
		if (centre == 0) {
			centre = getWidth() / 2; // 获取圆心的x坐标
			radius = centre - roundWidth * 1.0f / 2; // 圆环的半径
			oval = new RectF(); // 用于定义的圆弧的形状和大小的界限
			oval.left = centre - radius;
			oval.top = centre - radius;
			oval.right = centre + radius;
			oval.bottom = centre + radius;
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		init();
		clear();
		
		/**
		 * 画最外层的大圆环
		 */
		if (roundColor != Color.TRANSPARENT) {
			paint.setColor(roundColor); // 设置圆环的颜色
			paint.setStyle(Paint.Style.STROKE); // 设置空心
			paint.setStrokeWidth(roundWidth); // 设置圆环的宽度
			paint.setAntiAlias(true); // 消除锯齿
			canvas.drawCircle(centre, centre, radius, paint); // 画出圆环
		}

		/**
		 * 画圆弧 ，画圆环的进度
		 */
		paint.setStrokeWidth(roundWidth); // 设置圆环的宽度
		paint.setColor(roundProgressColor); // 设置进度的颜色
		paint.setStyle(Paint.Style.STROKE); // 设置空心
		paint.setAntiAlias(true); // 消除锯齿
		canvas.drawArc(oval, -90, 360 * progress / max, false, paint); // 根据进度画圆弧
	}
	
	private void clear() {
		if (needClear) {
			Canvas canvas = new Canvas();
			Paint paint = new Paint();
			paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
			canvas.drawPaint(paint);
			paint.setXfermode(new PorterDuffXfermode(Mode.SRC));
			invalidate();
		}
	}

	public int getMax() {
		return max;
	}

	/**
	 * 设置进度的最大值
	 * 
	 * @param max
	 */
	public void setMax(int max) {
		if (max < 0) {
			throw new IllegalArgumentException("max not less than 0");
		}
		this.max = max;
	}

	/**
	 * 获取进度.需要同步
	 * 
	 * @return
	 */
	public int getProgress() {
		return progress;
	}

	/**
	 * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步 刷新界面调用postInvalidate()能在非UI线程刷新
	 * 
	 * @param progress
	 */
	public void setProgress(int progress) {
		if (progress < 0) {
			throw new IllegalArgumentException("progress not less than 0");
		}
		if (progress > max) {
			progress = max;
		}
		if (progress < this.progress) {
			needClear = true;
		} else {
			needClear = false;
		}
		if (progress <= max) {
			this.progress = progress;
			postInvalidate();
		}
	}
	
	/**
	 * 设置进度并动画，此为线程安全控件，由于考虑多线的问题，需要同步 刷新界面调用postInvalidate()能在非UI线程刷新
	 * 
	 */
	public void setProgressAnim(int afterProgress, int duration) {
		if (afterProgress < 0) {
			throw new IllegalArgumentException("progress not less than 0");
		}
		if (afterProgress > max) {
			afterProgress = max;
		}
		if (afterProgress == progress) {
			return;
		}
		
		final int oldProgress = progress;
		final int newProgress = afterProgress;
		needClear = false;
		
		// 如果是进度回滚，不动画显示
		if (newProgress < oldProgress) {
			setProgress(newProgress);
			return;
		}
		
		CustomAnimation animation = new CustomAnimation(new CustomAnimCallBack() {
			@Override
			public void callBack(float interpolatedTime, Transformation t) {
				
				if (oldProgress < newProgress) {
					progress = oldProgress + (int) ((newProgress - oldProgress) * interpolatedTime);
				} else if (oldProgress > newProgress) {
					progress = oldProgress - (int) ((oldProgress - newProgress) * interpolatedTime);
				}
				postInvalidate();
			}
		});
		animation.setDuration(duration);
		animation.setInterpolator(new LinearInterpolator());
		clearAnimation();
		startAnimation(animation);
	}

	public int getCricleColor() {
		return roundColor;
	}

	public void setCricleColor(int cricleColor) {
		this.roundColor = cricleColor;
	}

	public int getCricleProgressColor() {
		return roundProgressColor;
	}

	public void setCricleProgressColor(int cricleProgressColor) {
		this.roundProgressColor = cricleProgressColor;
	}

	public float getRoundWidth() {
		return roundWidth;
	}

	public void setRoundWidth(float roundWidth) {
		this.roundWidth = roundWidth;
	}

}