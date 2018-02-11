package com.monster.netmanage.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * 圆周运动
 * @author zhaolaichao
 *
 */
public class CircleView extends View {
	/**
	 * 正常颜色
	 */
	public static final int CIRVIEW_COLOR = Color.rgb(104, 180, 216);
	/**
	 * 超过设定最大值颜色
	 */
	public static final int CIRVIEW_WARN_COLOR = Color.rgb(251, 200, 134);
	/**
	 * 圆1
	 */
	private static int CIRCLE_1 = 1;
	/**
	 * 圆2
	 */
	private static int CIRCLE_2 = 2;
	/**
	 * 圆3
	 */
	private static int CIRCLE_3 = 3;
	/**
	 * 圆1运动轨迹时长：6s
	 */
	private final static long CIRCLE_1_TIME = 17;
	/**
	 * 圆2运动轨迹时长：7s
	 */
	private final static long CIRCLE_2_TIME = 20;
	/**
	 * 圆3运动轨迹时长：8s
	 */
	private final static long CIRCLE_3_TIME = 23;
	/**
	 * 圆心运行路径所在圆的圆心
	 */
	private double mR = 30;
	private double mR1 = 622.5;
	private double mR2 = 604.5;
	private double mR3 = 624.5;

	/**
	 * 圆1运行路径所在圆的圆心坐标
	 */
	private double mC11X = 1152.2;
	private double mC11Y = -369;
	/**
	 * 圆2运行路径所在圆的圆心坐标
	 */
	private double mC21X = 1056.2;
	private double mC21Y = -337;
	/**
	 * 圆3运行路径所在圆的圆心坐标
	 */
	private double mC31X = 888.2;
	private double mC31Y = -431;
	/**
	 * 运行角度
	 */
	private int mAngle1 = 0;
	private int mAngle2 = 0;
	private int mAngle3 = 0;

	/**
	 * 圆的实时坐标
	 */
	public double mC1X = 1152.2;
	public double mC1Y = -399;
	public double mC2X = 1056.2;
	public double mC2Y = -367;
	public double mC3X = 888.2;
	public double mC3Y = -431;
	/**
	 * 圆1的圆心轨迹
	 */
	private Paint mPaint11;
	/**
	 * 圆2的圆心轨迹
	 */
	private Paint mPaint21;
	/**
	 * 圆3的圆心轨迹
	 */
	private Paint mPaint31;
	private Paint mPaint1;
	private Paint mPaint2;
	private Paint mPaint3;
	private Context mContext;
	private int mColor;
	private boolean mIsMove;

	public CircleView(Context context) {
		this(context, null);
	}

	public CircleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		mColor = CIRVIEW_COLOR;
		mIsMove = true;
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		initPaint();
		setMove();
	}

	/**
	 * 设置画笔颜色
	 * 
	 * @param color
	 */
	public void setColor(int color) {
		mColor = color;
		mPaint1.setColor(mColor);
		mPaint1.setAlpha(60);
		mPaint2.setColor(mColor);
		mPaint2.setAlpha(40);
		mPaint3.setColor(mColor);
		mPaint3.setAlpha(25);
		invalidate();
	}

	/**
	 * 是否停止移动
	 * 
	 * @param isStop
	 */
	public void setMove(boolean isMove) {
		mIsMove = isMove;
	}

	/**
	 * 初始化画笔
	 */
	private void initPaint() {
		// 实例化画笔并打开抗锯齿
		mPaint11 = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint11.setStyle(Paint.Style.STROKE);
		mPaint11.setStrokeWidth(5);
		mPaint11.setAntiAlias(true);// 抗锯齿

		// 实例化画笔并打开抗锯齿
		mPaint21 = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint21.setStyle(Paint.Style.STROKE);
		mPaint21.setStrokeWidth(5);
		mPaint21.setAntiAlias(true);// 抗锯齿
		// 实例化画笔并打开抗锯齿
		mPaint31 = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint31.setStyle(Paint.Style.STROKE);
		mPaint31.setStrokeWidth(5);
		mPaint11.setAntiAlias(true);// 抗锯齿

		mPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint1.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaint1.setColor(mColor);
		mPaint1.setAntiAlias(true);// 抗锯齿
		mPaint1.setAlpha(60);

		mPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint2.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaint2.setColor(mColor);
		mPaint2.setAntiAlias(true);// 抗锯齿
		mPaint2.setAlpha(40);

		mPaint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint3.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaint3.setColor(mColor);
		mPaint3.setAntiAlias(true);// 抗锯齿
		mPaint3.setAlpha(25);
		DisplayMetrics metric = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(metric);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// 绘制 圆1圆心运行路径所在圆
		canvas.drawCircle((float) mC11X, (float) mC11Y, (float) mR, mPaint11);
		// 绘制 圆2圆心运行路径所在圆
		canvas.drawCircle((float) mC21X, (float) mC21Y, (float) mR, mPaint21);
		// 绘制 圆3圆心运行路径所在圆
		canvas.drawCircle((float) mC31X, (float) mC31Y, (float) mR, mPaint31);

		// 确定圆的位置
		canvas.drawCircle((float) mC1X, (float) mC1Y, (float) mR1, mPaint1);
		canvas.drawCircle((float) mC2X, (float) mC2Y, (float) mR2, mPaint2);
		canvas.drawCircle((float) mC3X, (float) mC3Y, (float) mR3, mPaint3);
	}

	/**
	 * 设置移动中圆的位置坐标
	 * 
	 * @param index
	 *            索引
	 * @param angle
	 *            转动角度
	 */
	private void setCirCleCenter(int index, int angle, double cx, double cy) {
		long sleepTime = 0;
		double currentCx = 0;
		double currentCy = 0;
		while (mIsMove && angle <= 360) {
			if (!mIsMove) {
				return;
			}
			angle++;
			if (angle >= 0 && angle <= 90) {
				currentCx = cx - Math.sin(angle * Math.PI / 180) * mR;
				currentCy = cy + Math.cos(angle * Math.PI / 180) * mR;
			} else if (angle > 90 && angle <= 180) {
				currentCx = cx - Math.sin(angle * Math.PI / 180) * mR;
				currentCy = cy - Math.abs(Math.cos(angle * Math.PI / 180)) * mR;
			} else if (angle > 180 && angle <= 270) {
				currentCx = cx + Math.abs(Math.sin(angle * Math.PI / 180)) * mR;
				currentCy = cy - Math.abs(Math.cos(angle * Math.PI / 180)) * mR;
			} else if (angle > 270 && angle <= 360) {
				if (angle == 360) {
					angle = 0;
				}
				currentCx = cx + Math.abs(Math.sin(angle * Math.PI / 180)) * mR;
				currentCy = cy + Math.abs(Math.cos(angle * Math.PI / 180)) * mR;
			}
			if (index == CIRCLE_1) {
				mC1X = currentCx;
				mC1Y = currentCy;
				sleepTime = CIRCLE_1_TIME;
			} else if (index == CIRCLE_2) {
				mC2X = currentCx;
				mC2Y = currentCy;
				sleepTime = CIRCLE_2_TIME;
			} else if (index == CIRCLE_3) {
				mC3X = currentCx;
				mC3Y = currentCy;
				sleepTime = CIRCLE_3_TIME;
			}
			postInvalidate();
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 设置移动
	 */
	private void setMove() {
		CirCleRunnable cirCleR1 = new CirCleRunnable(CIRCLE_1, mAngle1, mC11X, mC11Y);
		CirCleRunnable cirCleR2 = new CirCleRunnable(CIRCLE_2, mAngle2, mC21X, mC21Y);
		CirCleRunnable cirCleR3 = new CirCleRunnable(CIRCLE_3, mAngle3, mC31X, mC31Y);
		Thread thread1 = new Thread(cirCleR1);
		Thread thread2 = new Thread(cirCleR2);
		Thread thread3 = new Thread(cirCleR3);
		thread1.start();
		thread2.start();
		thread3.start();
	}

	class CirCleRunnable implements Runnable {

		private int index;
		private int angle;
		private double cx;
		private double cy;

		public CirCleRunnable(int index, int angle, double cx, double cy) {
			super();
			this.index = index;
			this.angle = angle;
			this.cx = cx;
			this.cy = cy;
		}

		@Override
		public void run() {
			setCirCleCenter(index, angle, cx, cy);
		}

	}

}
