package cn.tcl.music.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import cn.tcl.music.R;
import cn.tcl.music.util.LogUtil;

public final class JogWheelLayout extends ViewGroup {

	public static final String TAG = JogWheelLayout.class.getSimpleName();
	private View mJogWheel;
	private View mCircularSeekBar;
	private View mJogPlayBtn;

	private final static float JOG_PROGRESS_RATIO = 0.335f;

	public JogWheelLayout(Context context) {
		super(context);
	}

	public JogWheelLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public JogWheelLayout(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public JogWheelLayout(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		// Children have been added, let's retrieve them
		mJogWheel = findViewById(R.id.jog_wheel);
		mCircularSeekBar = findViewById(R.id.jog_seek_bar);
		mJogPlayBtn = findViewById(R.id.jog_play_btn);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	int totalHeight = MeasureSpec.getSize(heightMeasureSpec);
    	int totalWidth = MeasureSpec.getSize(widthMeasureSpec);
    	
    	final int totalSize = Math.min(totalWidth, totalHeight);
		
		final int paddingLeft = getPaddingLeft();
		final int paddingTop = getPaddingTop();
		final int paddingRight = getPaddingRight();
		final int paddingBottom = getPaddingBottom();
    	
    	final int jogSize = Math.min(totalSize - (paddingLeft + paddingRight),
    								 totalSize - (paddingTop + paddingBottom));
    	
    	mJogWheel.measure(MeasureSpec.makeMeasureSpec(jogSize, MeasureSpec.EXACTLY),
    				      MeasureSpec.makeMeasureSpec(jogSize, MeasureSpec.EXACTLY));
    	
    	final int minProgressSize = (int) getResources().getDisplayMetrics().density * 100;

		LogUtil.d(TAG, "jogSize * JOG_PROGRESS_RATIO = " +jogSize * JOG_PROGRESS_RATIO +", minProgressSize = " +minProgressSize);
    	final int progressSize = (int) Math.max(jogSize * JOG_PROGRESS_RATIO, minProgressSize);
    	mCircularSeekBar.measure(MeasureSpec.makeMeasureSpec(progressSize, MeasureSpec.EXACTLY),
    				      		 MeasureSpec.makeMeasureSpec(progressSize, MeasureSpec.EXACTLY));

		final int minPlayBtnSize = (int) getResources().getDisplayMetrics().density * 70;
    	final int playBtnSize = (int) Math.min(progressSize * 0.5f, minPlayBtnSize);
    	mJogPlayBtn.measure(MeasureSpec.makeMeasureSpec(playBtnSize, MeasureSpec.EXACTLY),
	      		 MeasureSpec.makeMeasureSpec(playBtnSize, MeasureSpec.EXACTLY));

    	setMeasuredDimension(totalSize, totalSize);
	}
	
	@Override
	public LayoutParams generateLayoutParams(
			AttributeSet attrs) {
		// TODO Auto-generated method stub
		return new MarginLayoutParams(getContext(), attrs);
	}
	

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		//Everything is centered , no need to check each orientation/gravity/etc...
		final int centeredChildCount = getChildCount();
		final int centerWidth = (r - l) / 2;
		final int centerHeight = (b - t) / 2;
		
		for (int i = 0; i < centeredChildCount; i++) {
			View child = getChildAt(i);
			final int width = child.getMeasuredWidth();
			final int height = child.getMeasuredHeight();
			child.layout(centerWidth - width/2, centerHeight - height /2 , centerWidth + width /2, centerHeight + height /2);
		}
	}
}
