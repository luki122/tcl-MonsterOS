/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.android.camera.debug.Log;

public class LockRotatableButton extends RotatableButton {
    private static final String PHOTO_MODULE_STRING_ID = "LockRotatableButton";
    private static final Log.Tag TAG = new Log.Tag(PHOTO_MODULE_STRING_ID);
    public LockRotatableButton(Context context, AttributeSet attrs) {
        super(context,attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isLocked()) {
            return super.onTouchEvent(event);
        }
        final int action = event.getAction();
        if (isEnabled()) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    if(mOnTouchListener!=null) {
                        mOnTouchListener.onTouchDown();
                        mWaitForTouchDown = true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if(mWaitForTouchDown){
                        if(mOnTouchListener!=null){
                            mOnTouchListener.onTouchUp();
                        }
                        mWaitForTouchDown=false;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(mWaitForTouchDown && !isPointInView(event.getX(), event.getY())) {
                        if(mOnTouchListener!=null) {
                            mOnTouchListener.onTouchUp();
                        }
                        mWaitForTouchDown=false;
                    }
                    break;
            }
            return super.onTouchEvent(event);
        } else {
            switch (action) {
                case MotionEvent.ACTION_UP:
                    if (mOnUnhandledClickListener != null) {
                        mOnUnhandledClickListener.unhandledClick();
                    }
                    break;
            }
            return super.onTouchEvent(event);
        }
    }

}
