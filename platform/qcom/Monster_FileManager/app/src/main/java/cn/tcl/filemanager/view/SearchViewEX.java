/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.MstSearchView;

/**
 * Created on 16-9-20.
 */
public class SearchViewEX extends MstSearchView {

    public SearchViewEX(Context context) {
        super(context);
    }

    public SearchViewEX(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(21)
    public SearchViewEX(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public SearchViewEX(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isEnabled()) {
            return super.dispatchTouchEvent(ev);
        }
        return false;
    }
}
