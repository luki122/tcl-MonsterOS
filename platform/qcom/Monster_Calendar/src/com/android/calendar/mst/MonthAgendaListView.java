package com.android.calendar.mst;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import mst.widget.MstListView;

public class MonthAgendaListView extends MstListView {

	private OnTouchEventHandleListener mHandleListener;

	public MonthAgendaListView(Context context) {
		super(context);
	}

	public MonthAgendaListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MonthAgendaListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mHandleListener != null) {
			mHandleListener.onTouchEventHandle(ev);
		}
		return super.onTouchEvent(ev);
	}

	public interface OnTouchEventHandleListener{
		boolean onTouchEventHandle(MotionEvent event);
	}

	public void setOnTouchEventHandleListener(OnTouchEventHandleListener handleListener) {
		mHandleListener = handleListener;
	}

}
