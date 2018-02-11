package cn.tcl.music.view;

import android.content.Context;
import android.util.AttributeSet;

import cn.tcl.music.R;
import cn.tcl.music.view.mixvibes.AudioListenerCircularSeekBar;

public class JogSeekBar extends AudioListenerCircularSeekBar {

	public JogSeekBar(Context context) {
		super(context);
	}

	public JogSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public JogSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	@Override
	public void setPressed(boolean pressed) {
		super.setPressed(pressed);
		if (pressed) {
		} else {
		}
	}

}
