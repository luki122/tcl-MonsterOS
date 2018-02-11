package cn.tcl.music.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Pair;
import android.widget.ImageButton;

import cn.tcl.music.R;
import cn.tcl.music.util.LogUtil;

import java.util.List;

public class PlayModeButton extends ImageButton {

	private static final int[] SHUFFLE_STATE_SET = {R.attr.state_shuffle};
	private static final int[] REPEAT_ALL_STATE_SET = {R.attr.state_repeat_all};
	private static final int[] REPEAT_ONE_STATE_SET = {R.attr.state_repeat_one};
	private static final String TAG = PlayModeButton.class.getSimpleName();

	public PlayModeButton(Context context) {
		super(context);
	}

	public PlayModeButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PlayModeButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public PlayModeButton(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

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
//		switch(mStateMode)
//		{
//		case LOOPED:
//			drawableState = super.onCreateDrawableState(extraSpace + 1);
//			mergeDrawableStates(drawableState, REPEAT_ALL_STATE_SET);
//			break;
//		case REPEAT_ONETRACK:
//			drawableState = super.onCreateDrawableState(extraSpace + 1);
//			mergeDrawableStates(drawableState, REPEAT_ONE_STATE_SET);
//			break;
//		case SHUFFELIZE_QUEUE:
//			drawableState = super.onCreateDrawableState(extraSpace + 1);
//			mergeDrawableStates(drawableState, SHUFFLE_STATE_SET);
//			break;
//			default:
//				drawableState = super.onCreateDrawableState(extraSpace);
//				break;
//		}
		return drawableState;
	}



}
