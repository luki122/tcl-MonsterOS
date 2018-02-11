package com.monster.market.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

import com.monster.market.R;
import com.monster.market.utils.DensityUtil;


public class ToCircleView extends View {
	
	public static int TYPE_TO_CIRCLE = 1;
	public static int TYPE_TO_RECT = 2;
	
	public static int DURATION_TIME = 400;
	public static float ALPHA_START = 0.8f; 
	
	private Paint paint;
	private Paint startPaint;
	private Paint endPaint;
	private int allViewWidth;
	private int allViewHeight;
	
	private CustomAnimation animation;
	private CustomAnimCallBack selfCallBack;
	private CustomAnimCallBack outCallBack;
	
	private int type = TYPE_TO_CIRCLE;
	private RectF rectF = new RectF(); // RectF对象
	private int rx = 5;
	private int maxRpx = 0;
	private int minRdp = 1;
	private int minRpx = 0;
	private int left = 0;
	private int right = 0;
	private float roundWidth = 0;
	private boolean drawEndPaint = true;

	public ToCircleView(Context context) {
		super(context);
		init();
	}

	public ToCircleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ToCircleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		roundWidth = getResources().getDimension(R.dimen.progressLineWidth);
		
		// 正常圆角矩形边框
		paint = new Paint();
		paint.setAntiAlias(true);                       //设置画笔为无锯齿  
	    paint.setColor(getResources().getColor(R.color.progressRoundGreyColor));    //设置画笔颜色
	    paint.setStrokeWidth(roundWidth);              //线宽  
	    paint.setStyle(Style.STROKE);                   //空心效果  
	    
	    // 开始动画辅助Paint
	    startPaint = new Paint();
	    startPaint.setAntiAlias(true);                       //设置画笔为无锯齿  
	    startPaint.setColor(getResources().getColor(R.color.progressRoundGreyColor));    //设置画笔颜色  
	    startPaint.setStrokeWidth(roundWidth);              //线宽  
	    startPaint.setStyle(Style.STROKE);                   //空心效果 
	    
	    // 结束动画辅助Paint
	    endPaint = new Paint();
	    endPaint.setAntiAlias(true);                       //设置画笔为无锯齿  
	    endPaint.setColor(getResources().getColor(R.color.progressRoundGreyColor));    //设置画笔颜色
	    endPaint.setStrokeWidth(0);              //线宽  
	    endPaint.setStyle(Style.FILL);                   //实心效果  
	    
	    minRpx = DensityUtil.dip2px(getContext(), minRdp);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		rectF.left = left + roundWidth; // 左边
		rectF.top = 0 + roundWidth; // 上边
		rectF.right = right - roundWidth; // 右边
		rectF.bottom = allViewHeight - roundWidth; // 下边
		
		if (type == TYPE_TO_CIRCLE) {
			canvas.drawRoundRect(rectF, rx, rx, startPaint);
		} else {
			if (drawEndPaint) {
				canvas.drawRoundRect(rectF, rx, rx, endPaint);
			}
		}
		
		canvas.drawRoundRect(rectF, rx, rx, paint); // 绘制圆角矩形
	}
	
	public void setAllViewWidthAndHeight(int width, int height, int type) {
		allViewWidth = width;
		allViewHeight = height;
		this.type = type;
		if (type == TYPE_TO_CIRCLE) {
			left = 0;
			right = width;
			maxRpx = allViewHeight / 2;
		} else {
			rx = maxRpx = allViewHeight / 2;
			left = (int) ((allViewWidth - allViewHeight) * 1.0f / 2);
			right = allViewWidth - left;
		}
	}
	
	public void startAnim(CustomAnimCallBack animCallBack) {
		startAnim(animCallBack, true);
	}
	
	public void startAnim(CustomAnimCallBack animCallBack, boolean drawEndPaint) {
		this.drawEndPaint = drawEndPaint;
		outCallBack = animCallBack;
		selfCallBack = new CustomAnimCallBack() {
			@Override
			public void callBack(float interpolatedTime, Transformation t) {
				if (type == TYPE_TO_CIRCLE) {
					rx = (int) (maxRpx * interpolatedTime);
					if (rx < minRpx) {
						rx = minRpx;
					}
					left = (int) ((allViewWidth - allViewHeight) * 1.0f / 2 * interpolatedTime);
					right = allViewWidth - left;
					if (interpolatedTime > ALPHA_START) {
						float v = 1 - ALPHA_START;
						paint.setAlpha((int) (255 * (v - (interpolatedTime - ALPHA_START)) / v));
					} else {
						paint.setAlpha(255);
					}
				} else {
					rx = (int) (maxRpx - maxRpx * interpolatedTime);
					if (rx < minRpx) {
						rx = minRpx;
					}
					left = (int) ((allViewWidth - allViewHeight) * 1.0f / 2 * (1 - interpolatedTime));
					right = allViewWidth - left;
					if (ToCircleView.this.drawEndPaint) {
						paint.setAlpha((int)(255 * (1 - interpolatedTime)));
					} else {
						paint.setAlpha(255);
					}
					endPaint.setAlpha((int)(255 * interpolatedTime));
				}
				postInvalidate();
				
				if (outCallBack != null) {
					outCallBack.callBack(interpolatedTime, t);
				}
			}
		};
		animation = new CustomAnimation(selfCallBack);
		animation.setDuration(DURATION_TIME);
		animation.setInterpolator(new DecelerateInterpolator());
		clearAnimation();
		startAnimation(animation);
	}

}
