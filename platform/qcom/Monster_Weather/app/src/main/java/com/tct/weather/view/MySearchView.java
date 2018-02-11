/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.weather.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import cn.tcl.weather.R;

public class MySearchView extends EditText {
    private Drawable mClearDrawable;

    public MySearchView(Context context) {
        this(context, null);
    }

    public MySearchView(Context context, AttributeSet attrs) {
        //this constructor is important, if on this some attrs are not defined at XML
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public MySearchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mClearDrawable = getCompoundDrawablesRelative()[2];
        if (mClearDrawable == null) {
            mClearDrawable = getResources().getDrawable(R.drawable.text_clear);
        }

        mClearDrawable.setBounds(0, 0, mClearDrawable.getIntrinsicWidth(), mClearDrawable.getIntrinsicHeight());
        //[BUGFIX]-Add-BEGIN by TSCD.peng.du,12/09/2015,1070346,
        //[Android6.0][Weather_v5.2.8.1.0301.0]It display "x" when has nothing in search box
        setClearIconVisible(true);
        //[BUGFIX]-Add-END by TSCD.peng.du

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (getCompoundDrawablesRelative()[2] != null) {
                int i = (int) event.getX();
                boolean touchable = false;
                if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                    touchable = i < getTotalPaddingEnd()
                            && (i > getPaddingEnd());
                } else {
                    touchable = i > (getWidth() - getTotalPaddingEnd())
                            && (i < ((getWidth() - getPaddingEnd())));
                }

                /*boolean touchable = event.getX() > (getWidth() - getTotalPaddingEnd())
                        && (event.getX() < ((getWidth() - getPaddingEnd())));*/
                if (touchable) {
                    this.setText("");
                    //[BUGFIX]-Add-BEGIN by TSCD.peng.du,12/09/2015,1070346,
                    //[Android6.0][Weather_v5.2.8.1.0301.0]It display "x" when has nothing in search box
                    setClearIconVisible(false);
                    //[BUGFIX]-Add-END by TSCD.peng.du
                }
            }
        }

        return super.onTouchEvent(event);
    }

    /**
     * set clear button's show and hide, call setCompoundDrawables
     *
     * @param visible
     */
    public void setClearIconVisible(boolean visible) {
        Drawable right = visible ? mClearDrawable : null;
        setCompoundDrawablesRelative(getCompoundDrawablesRelative()[0],
                getCompoundDrawablesRelative()[1], right, getCompoundDrawablesRelative()[3]);
    }

}
