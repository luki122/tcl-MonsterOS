package com.monster.appmanager.widget;

import com.monster.appmanager.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * 默认软件listview的item
 * 
 * @author liuqin
 *
 */
public class DefaultAppListItem extends LinearLayout {
	private Bitmap bitmap;
	private int selectedViewLeft;
	private int selectedViewTop;

	public DefaultAppListItem(Context context) {
		super(context);
		init();
	}

	public DefaultAppListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public DefaultAppListItem(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		setWillNotDraw(false);
		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_app_item_select);
	}

	@Override
	public void onDrawForeground(Canvas arg0) {
		super.onDrawForeground(arg0);
		if (selectedViewLeft > 0 || selectedViewTop > 0) {
			arg0.drawBitmap(bitmap, selectedViewLeft, selectedViewTop, null);
		}
	}

	public void invalidSelectedView(int left, int top) {
		selectedViewLeft = left - bitmap.getWidth() / 2;
		selectedViewTop = top - bitmap.getHeight() / 2;
		invalidate();
	}
}
