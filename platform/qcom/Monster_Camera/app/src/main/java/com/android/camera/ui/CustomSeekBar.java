package com.android.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.tct.camera.R;

public class CustomSeekBar extends RelativeLayout {
    private LayoutInflater mInflater;
    private SeekBar mSeekBar;

    public CustomSeekBar(Context context) {
        super(context);
    }

    public CustomSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        mInflater = LayoutInflater.from(context);
        View view = mInflater.inflate(R.layout.custom_seekbar, this);
        mSeekBar = (SeekBar) view.findViewById(R.id.customseekbar);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            requestDisallowInterceptTouchEvent(true);
        }
        return false;
    }

    public interface EnableStateChangedCallback {
        public void onEnableStateChanged(boolean enable);
    }

    private boolean mEnableOnTouch = false;
    private EnableStateChangedCallback mCallback;
    public void setEnableOnTouch(boolean enable, EnableStateChangedCallback callback) {
        mEnableOnTouch = enable;
        mCallback = callback;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isShown()) {
            return super.onTouchEvent(event);
        }
        mSeekBar.onTouchEvent(event);
        if (!mSeekBar.isEnabled() && mEnableOnTouch) {
            mSeekBar.setEnabled(true);
            if (mCallback != null) {
                mCallback.onEnableStateChanged(true);
            }
        }
        return true;
    }
}
