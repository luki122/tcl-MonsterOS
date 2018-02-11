package cn.tcl.music.view.mixvibes;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class WaitingImageButton extends ImageButton {
	
	private static final int[] WAITING_STATE_SET = {com.mixvibes.mvlib.R.attr.state_waiting};
	private boolean mIsWaiting;

	public WaitingImageButton(Context context) {
		super(context);
	}

	public WaitingImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public WaitingImageButton(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public WaitingImageButton(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
	
	public void setWaiting(boolean isWaiting)
	{
		if(mIsWaiting == isWaiting)
			return;
		
		mIsWaiting = isWaiting;
		refreshDrawableState();
	}
	
	public boolean isWaiting()
	{
		return mIsWaiting;
	}
	
	@Override
	public int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if(mIsWaiting)
			mergeDrawableStates(drawableState, WAITING_STATE_SET);
		return drawableState;
	}
	
	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		
		Drawable d = getDrawable().getCurrent();
		if (d instanceof AnimationDrawable)
			((AnimationDrawable) d).start();
	}

}
