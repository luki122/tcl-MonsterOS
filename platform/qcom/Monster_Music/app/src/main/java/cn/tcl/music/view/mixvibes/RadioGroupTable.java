package cn.tcl.music.view.mixvibes;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

public class RadioGroupTable extends RadioGroup {
	
	private int mNumRadioBtnsPerColumn = DEFAULT_NUM_RADIO_PER_COL;
	private int mMarginBetweenCols = 0;
	
	private final static int DEFAULT_NUM_RADIO_PER_COL = 5;
	
	public RadioGroupTable(Context context) {
		super(context);
	}

	public RadioGroupTable(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a = context.obtainStyledAttributes(attrs, com.mixvibes.mvlib.R.styleable.RadioGroupTable);
		
		mNumRadioBtnsPerColumn = a.getInteger(com.mixvibes.mvlib.R.styleable.RadioGroupTable_numRadioBtnPerColumns, DEFAULT_NUM_RADIO_PER_COL);
		mMarginBetweenCols = a.getDimensionPixelSize(com.mixvibes.mvlib.R.styleable.RadioGroupTable_marginBetweenColumns,0);
		
		a.recycle();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int numChildren = getChildCount();
		
		int totalHeight = getPaddingBottom() + getPaddingTop();
		final int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
		
		View child = getChildAt(0);
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) child.getLayoutParams();
		
		totalHeight = totalHeight + lp.height * mNumRadioBtnsPerColumn;
		final int numColumns = (int) Math.ceil(numChildren /((float) mNumRadioBtnsPerColumn));
		final int marginOffset = (mMarginBetweenCols) / 2; 
		final int widthPerColumn = (measuredWidth) / numColumns - marginOffset * (numColumns - 1);
		
		for(int i = 0; i < numChildren; i++)
		{
			child = getChildAt(i);
			lp = (LinearLayout.LayoutParams) child.getLayoutParams();
			child.measure(MeasureSpec.makeMeasureSpec(widthPerColumn, MeasureSpec.EXACTLY),
					      MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY));
			
			
		}
		
		
		setMeasuredDimension(measuredWidth, totalHeight);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int numChildren = getChildCount();
		final int numColumns = (int) Math.ceil(numChildren /((float) mNumRadioBtnsPerColumn));
		final int widthPerColumn = (r - l - mMarginBetweenCols * (numColumns - 1)) / numColumns;
		
		for (int i = 0; i < numColumns; i++)
		{
			int childOffset = (i * mNumRadioBtnsPerColumn);
			int totalHeight = 0;
			for (int j = 0; j < mNumRadioBtnsPerColumn &&
						j + childOffset < numChildren; j++)
			{
				View child = getChildAt(j + childOffset);
				final int height = child.getMeasuredHeight();
				child.layout((mMarginBetweenCols + widthPerColumn) * i , totalHeight, mMarginBetweenCols * i + widthPerColumn * (i + 1), totalHeight + height);
				totalHeight += height;
			}
		}
		
	}
	
	

}
