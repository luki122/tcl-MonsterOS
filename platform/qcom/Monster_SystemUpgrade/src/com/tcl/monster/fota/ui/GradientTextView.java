package com.tcl.monster.fota.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.tcl.monster.fota.R;

public class GradientTextView extends TextView
{
	private Paint mPaint;
	private Matrix mGradientMatrix;
	private LinearGradient mLinearGradient;

	private int mViewWidth = 0;
	private float gradientX = 0.0f;
	private boolean isGradient = false;

	private GradientAnimatorListener mGradientListener;

	private int startColor = 0;
	private int MiddleColor = 0;
	private int endColor = 0;

	public GradientTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context, attrs);
	}

	public GradientTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.GradientTextView);
		startColor = ta.getColor(R.styleable.GradientTextView_startColor, Color.BLACK);
		MiddleColor = ta.getColor(R.styleable.GradientTextView_middleClolor, Color.BLACK);
		endColor = ta.getColor(R.styleable.GradientTextView_endColor, Color.WHITE);
	}

	public void addListener(GradientAnimatorListener listener) {
		mGradientListener = listener;
	}

	public void startAnimation() {
		measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		float fromX = 0;
		final float toX = mViewWidth * 2;
		ensureMatrix();
		mGradientMatrix.reset();
		setGradient(true);
		setGradientX(0);
		setVisibility(View.VISIBLE);
		Animator animator = ObjectAnimator.ofFloat(this, "gradientX", fromX, toX).setDuration(220);
		animator.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {

			}

			@Override
			public void onAnimationEnd(Animator animator) {
				setGradient(false);
				if (mGradientListener != null) {
					mGradientListener.onAnimationEnd(animator);
				}
				mGradientListener = null;
			}

			@Override
			public void onAnimationCancel(Animator animator) {

			}

			@Override
			public void onAnimationRepeat(Animator animator) {

			}
		});
		animator.start();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mViewWidth = this.getMeasuredWidth();
	}

	public float getGradientX() {
		return gradientX;
	}

	public void setGradientX(float gradientX) {
		this.gradientX = gradientX;
		invalidate();
	}


	public boolean isGradient() {
		return isGradient;
	}

	public void setGradient(boolean mGradient) {
		this.isGradient = mGradient;
	}

	@Override
	public void setVisibility(int visibility) {
		super.setVisibility(visibility);
		invalidate();
	}

	private void ensureMatrix() {
		if(mGradientMatrix == null) {
			mGradientMatrix = new Matrix();
		}
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		if(mLinearGradient == null) {
			mLinearGradient = new LinearGradient(-mViewWidth, 0, 0, 0,
					new int[]{startColor, MiddleColor, endColor},
					new float[]{0, 0.5f, 1}, Shader.TileMode.CLAMP);
		}
		mPaint = getPaint();
		if (isGradient) {
			ensureMatrix();
			mPaint.setShader(mLinearGradient);
			mGradientMatrix.setTranslate(gradientX, 0);
			mLinearGradient.setLocalMatrix(mGradientMatrix);

		} else {
			mPaint.setShader(null);
		}
		super.onDraw(canvas);
	}

	public interface GradientAnimatorListener {
		void onAnimationEnd(Animator var1);
	}
}