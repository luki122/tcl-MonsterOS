package cn.tcl.music.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

public class BlackenedImageView extends ImageView {

	public BlackenedImageView(Context context) {
		super(context);
	}

	public BlackenedImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BlackenedImageView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (getDrawable() != null)
		{
			canvas.drawColor(0xFF43525b);
		}
		super.onDraw(canvas);
	}

}
