package cn.tcl.music.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class ImageMenuButton extends ImageButton {
	public ImageMenuButton(Context context) {
		super(context);
	}

	public ImageMenuButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ImageMenuButton(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public ImageMenuButton(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	
	@Override
	public boolean requestRectangleOnScreen(Rect rectangle) {
		return false;
	}
	
	@Override
	public boolean requestRectangleOnScreen(Rect rectangle, boolean immediate) {
		return false;
	}
}
