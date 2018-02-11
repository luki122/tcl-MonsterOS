package cn.tcl.music.view.mixvibes;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class LinearGridLayout extends ViewGroup {
	
	private int mColumns = 1;
	private int mRows = 1;
	
	private int mHorizontalSpacing = 0;
	private int mVerticalSpacing = 0;
	
	private int mVerticalPadding = 0;
	private int mHorizontalPadding = 0;
	
	private boolean mShouldBeSquared = false;

	public LinearGridLayout(Context context) {
		super(context);
		init(context, null);
	}
	
	public LinearGridLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}
	
	public LinearGridLayout(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public LinearGridLayout(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs);
	}
	
	private void init(Context context, AttributeSet attrs)
	{
		if (attrs == null)
			return;
		
		TypedArray a = context.obtainStyledAttributes(attrs, com.mixvibes.mvlib.R.styleable.LinearGridLayout);
		mColumns = a.getInt(com.mixvibes.mvlib.R.styleable.LinearGridLayout_numColumns, 1);
		mRows = a.getInt(com.mixvibes.mvlib.R.styleable.LinearGridLayout_numRows, 1);
		
		if (mColumns <= 0)
			mColumns = 1;
		if (mRows <= 0)
			mRows = 1;

		mVerticalSpacing = a.getDimensionPixelSize(com.mixvibes.mvlib.R.styleable.LinearGridLayout_android_verticalSpacing, 0);
		mHorizontalSpacing = a.getDimensionPixelSize(com.mixvibes.mvlib.R.styleable.LinearGridLayout_android_horizontalSpacing, 0);
		
		mShouldBeSquared = a.getBoolean(com.mixvibes.mvlib.R.styleable.LinearGridLayout_squaredChildren, false);
		a.recycle();
	}
	
	@Override
	public LayoutParams generateLayoutParams(
			AttributeSet attrs) {
		return new MarginLayoutParams(getContext(), attrs);
	}
	
	@Override
	protected void onMeasure(int widthSpec, int heightSpec) {
		//super.onMeasure(widthSpec, heightSpec);
		
		int estimatedWidth = MeasureSpec.getSize(widthSpec);
		int estimatedHeight = MeasureSpec.getSize(heightSpec);
		
		final int paddingLeft = getPaddingLeft();
		final int paddingTop = getPaddingTop();
		final int paddingRight = getPaddingRight();
		final int paddingBottom = getPaddingBottom();
		int estimatedWidthForChildren = (estimatedWidth - mHorizontalSpacing * (mColumns - 1) - paddingLeft - paddingRight) / mColumns;
		int estimatedHeightForChildren = (estimatedHeight - mVerticalSpacing * (mRows - 1) - paddingTop - paddingBottom) / mRows;
		
		if (mShouldBeSquared)
		{
			final int estimatedSquareSizeForChildren = estimatedWidthForChildren > estimatedHeightForChildren ? estimatedHeightForChildren : estimatedWidthForChildren;
			estimatedWidthForChildren = estimatedSquareSizeForChildren;
			estimatedHeightForChildren = estimatedSquareSizeForChildren;
		}
		
		final int numChildren = getChildCount();
		
		mVerticalPadding = (estimatedHeight -  estimatedHeightForChildren * mRows - mVerticalSpacing * (mRows - 1)) >> 1;
		mHorizontalPadding = (estimatedWidth - estimatedWidthForChildren * mColumns - mHorizontalSpacing * (mColumns - 1)) >> 1;
		for (int i = 0; i < numChildren; i++)
		{
			View child = getChildAt(i);
			
			int widthMeasure = MeasureSpec.makeMeasureSpec(estimatedWidthForChildren, MeasureSpec.EXACTLY);
			int heightMeasure = MeasureSpec.makeMeasureSpec(estimatedHeightForChildren, MeasureSpec.EXACTLY);
			
			child.measure(widthMeasure, heightMeasure);
		}
		
		setMeasuredDimension(estimatedWidth, estimatedHeight);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int yOffset = mVerticalPadding;
		View childView = null;
		
		int indexChildren = 0;
		for (int indexRow = 0; indexRow < mRows ; ++indexRow)
		{
			int xOffset = mHorizontalPadding;
			childView = getChildAt(indexChildren);
			
			int childWidth = childView.getMeasuredWidth();
			int childHeight = childView.getMeasuredHeight();
			childView.layout(xOffset, yOffset, xOffset + childWidth, yOffset + childHeight);
			xOffset += childWidth + mHorizontalSpacing;
			indexChildren++;
			
			for (int i = 1; i < mColumns; i++)
			{
				childView = getChildAt(indexChildren);
				
				childWidth = childView.getMeasuredWidth();
				childHeight = childView.getMeasuredHeight();
				childView.layout(xOffset, yOffset, xOffset + childWidth, yOffset + childHeight);
				xOffset += childWidth + mHorizontalSpacing;
				indexChildren++;
			}
			
			//xOffset += childWidth + horizontalSpacingPx; 
			yOffset += childHeight + mVerticalSpacing;
		}
	}

}
