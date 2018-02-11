package com.monster.appmanager.widget;

import com.monster.appmanager.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class ScanParent extends View {
	private Bitmap bitmap;
	private int imageRight;
	public ScanParent(Context context, AttributeSet attrs) {
		super(context, attrs);
		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.scan_img);
	}
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int left = imageRight - bitmap.getWidth()+50;
		if(left>0){
			canvas.drawBitmap(bitmap, left, 0, null);
		}		
	}

	public void invalidate(int imageRight) {
		this.imageRight = imageRight;
		invalidate();
	}
}
