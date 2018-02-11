package cn.tcl.music.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageButton;

import cn.tcl.music.R;

public class TransitionModeButton extends ImageButton{
	
	public TransitionModeButton(Context context) {
		super(context);
	}
	
	public TransitionModeButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public TransitionModeButton(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public TransitionModeButton(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
	private static final int[] AUTO_TRANSITION_SET = {R.attr.state_auto};
	private static final int[] MANUAL_TRANSITION_SET = {R.attr.state_manual};

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}
	
	@Override
	public int[] onCreateDrawableState(int extraSpace) {
		int[] drawableState = null;
		drawableState = super.onCreateDrawableState(extraSpace);
		return drawableState;
	}

}
