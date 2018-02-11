package com.mst.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.content.res.ColorStateList;
import com.android.incallui.R;

public class InCallCompoundButton extends CompoundButton {

	private Drawable mImageDrawable;
	private String mTextStr;
	private int mTopMargin1, mTopMargin2;
	private int mTextSize;
	// private int textcolor;
	private ColorStateList mTextColor;
	private boolean mIsShowImage;
	private Context mContext;
	private int r = 20 * 3;
	private Paint mBgPaint;
	private Paint mTextPaint;

	public InCallCompoundButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.incallbutton, 0, 0);
		mImageDrawable = a.getDrawable(R.styleable.incallbutton_mst_image);
		mTextStr = a.getString(R.styleable.incallbutton_mst_text);
		mTopMargin1 = a.getDimensionPixelSize(
				R.styleable.incallbutton_mst_topmargin1, 0);
		mTopMargin2 = a.getDimensionPixelSize(
				R.styleable.incallbutton_mst_topmargin2, 0);
		mTextSize = a.getDimensionPixelSize(
				R.styleable.incallbutton_mst_textsize, 0);
		mTextColor = a.getColorStateList(R.styleable.incallbutton_mst_textcolor);
		a.recycle();
		mContext = context;
		mIsShowImage = true;
		mBgPaint = new Paint();
		mBgPaint.setColor(0xFF5B5B5B);
		mBgPaint.setAntiAlias(true);
		mBgPaint.setDither(true);
		mBgPaint.setStrokeWidth(1);
		mBgPaint.setStyle(Paint.Style.FILL);
		// TODO Auto-generated constructor stub
		
		mTextPaint = new Paint();
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTextSize(mTextSize);
		Typeface typeface = Typeface.create("monster-normal", Typeface.NORMAL);
		mTextPaint.setTypeface(typeface);
	}

	public InCallCompoundButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public InCallCompoundButton(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);

	}

	public void setDrawableEnabled(boolean enable) {
		mIsShowImage = enable;
		invalidate();
	}

	public void setDrawable(int resid) {
		mImageDrawable = mContext.getResources().getDrawable(resid);
		mImageDrawable.setState(getDrawableState());
		invalidate();
	}

	public Drawable getDrawable() {
		return mImageDrawable;
	}

	public void setCustomText(int resid) {
		mTextStr = mContext.getResources().getString(resid);
		invalidate();
	}

	public void setCustomText(String tex) {
		mTextStr = tex;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub

		super.onDraw(canvas);
		final Drawable buttonDrawable = mImageDrawable;
		Log.v("InCallCompoundButton", "mImageDrawable=" + mImageDrawable);
		if (buttonDrawable != null) {
			final int drawableHeight = buttonDrawable.getIntrinsicHeight();
			final int drawableWidth = buttonDrawable.getIntrinsicWidth();

			int top = 0;
			top = mTopMargin1;
			int bottom = top + drawableHeight;
			int left = (getWidth() - drawableWidth) / 2;
			int right = (getWidth() + drawableWidth) / 2;

			buttonDrawable.setBounds(left, top, right, bottom);
			Log.v("InCallCompoundButton", "left=" + left + "top=" + top
					+ "right=" + right + "bottom=" + bottom);
			if (mIsShowImage) {
				if(isPressed() || isChecked()) {
					canvas.drawCircle(left + r, top + r, r, mBgPaint);
				}
				buttonDrawable.draw(canvas);
			}
			if (mTextStr != null) {
				mTextPaint.setColor(mTextColor.getColorForState(getDrawableState(),
						0x80FFFFFF));
				float textwidth = mTextPaint.measureText(mTextStr);
				canvas.drawText(mTextStr, (getWidth() - textwidth) / 2, bottom
						+ mTopMargin2 + mTextSize, mTextPaint);

			}
		}

	}

	@Override
	protected void drawableStateChanged() {
		// TODO Auto-generated method stub
		super.drawableStateChanged();
		if (mImageDrawable != null) {
			int[] myDrawableState = getDrawableState();
			// Set the state of the Drawable
			mImageDrawable.setState(myDrawableState);
			invalidate();
		}
	}

	@Override
	public void jumpDrawablesToCurrentState() {
		// TODO Auto-generated method stub
		super.jumpDrawablesToCurrentState();
		if (mImageDrawable != null)
			mImageDrawable.jumpToCurrentState();
	}

}
