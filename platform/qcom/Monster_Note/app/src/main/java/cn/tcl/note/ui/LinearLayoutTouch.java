/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import cn.tcl.note.util.NoteLog;

public class LinearLayoutTouch extends LinearLayout {
    private final static String TAG = LinearLayoutTouch.class.getSimpleName();

    public LinearLayoutTouch(Context context) {
        super(context);
    }

    public LinearLayoutTouch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LinearLayoutTouch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LinearLayoutTouch(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        NoteLog.d(TAG, "touch event=" + event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            return true;
        }
        return super.onTouchEvent(event);
    }
}
