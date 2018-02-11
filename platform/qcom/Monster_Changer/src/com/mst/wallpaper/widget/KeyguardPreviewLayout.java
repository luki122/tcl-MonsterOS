package com.mst.wallpaper.widget;

import mst.widget.ViewPager;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import com.mst.wallpaper.R;
public class KeyguardPreviewLayout extends RelativeLayout {
	private PreviewCycleTimeView mCycleTimeView;
	private TimeWidget mTimeWidget;
	private boolean mIsSingle = false;

	public KeyguardPreviewLayout(Context context) {
		this(context, null);
	}

	public KeyguardPreviewLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public KeyguardPreviewLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mCycleTimeView = (PreviewCycleTimeView) findViewById(R.id.preview_time);
		mTimeWidget = (TimeWidget)findViewById(R.id.time_layout);
	}

	public void onPageSelected(int position) {
		mTimeWidget.onPageSelected(position);
	}

	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
	}

	public void setViewPager(ViewPager viewPager) {
		mCycleTimeView.setViewPager(viewPager);
	}

	public void setOnPageChangeListener(
			ViewPager.OnPageChangeListener onPageChangeListener) {
		mCycleTimeView.setOnPageChangeListener(onPageChangeListener);
	}

	public void setSingle(boolean bool) {
		mIsSingle = bool;
		if (!mIsSingle) {
			mCycleTimeView.setVisibility(View.VISIBLE);
		} else {
			mCycleTimeView.setVisibility(View.GONE);
		}
	}

	public boolean getSingle() {
		return this.mIsSingle;
	}

	public void updateSingleTime() {
		mTimeWidget.updateClock();
	}

	public void setBlackStyle(boolean bool, int color) {
		mTimeWidget.setBlackStyle(bool, color);
	}
}