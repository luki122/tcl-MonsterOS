/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import cn.tcl.note.util.NoteLog;

public class RecyclerViewHelper extends RecyclerView {
    private final String TAG = RecyclerViewHelper.class.getSimpleName();
    private OnResizeListener resizeListener;

    public RecyclerViewHelper(Context context) {
        super(context);
    }

    public RecyclerViewHelper(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewHelper(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        NoteLog.d(TAG, "RecyclerView size change, w=" + w + " h=" + h + "  oldw" + oldw + " oldh=" + oldh);
        super.onSizeChanged(w, h, oldw, oldh);
        if (h < oldh) {
            //when size change small,need move current line to seen.
            resizeListener.afterResize();
        }
    }

    public void setResizeListener(OnResizeListener listener) {
        resizeListener = listener;
    }

    public interface OnResizeListener {
        /**
         * after size changed,do what.
         */
        void afterResize();

        boolean onTouchBlank();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        NoteLog.d(TAG, "touch action=" + event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (resizeListener.onTouchBlank()) {
                return true;
            }
        }
        return super.onTouchEvent(event);
    }
}
