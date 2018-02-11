package cn.tcl.weather;

import android.os.Bundle;
import android.view.MotionEvent;

import cn.tcl.weather.utils.CommonUtils;
import cn.tcl.weather.utils.LogUtils;

/**
 * Created on 16-9-26.
 */
public class TclBaseActivity extends mst.app.MstActivity {
    private final static String TAG = "TclBaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CommonUtils.setStateBarColor(this, R.color.white);
        changeVirtualKeyToWhite();
        super.onCreate(savedInstanceState);
    }

    private void changeVirtualKeyToWhite() {
        int vi = getWindow().getDecorView().getSystemUiVisibility();
        getWindow().getDecorView().setSystemUiVisibility(vi | 0x00000010);
        getWindow().setNavigationBarColor(0xFFFAFAFA);
    }

    private boolean isInterceptEvent;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final boolean isIntercept = isInterceptEvent;
        if (ev.getPointerCount() > 1) {
            if (!isIntercept) {
                isInterceptEvent = true;
                MotionEvent evup = MotionEvent.obtain(ev);
                evup.setAction(MotionEvent.ACTION_UP);
                superDispatchTouchEvent(evup);
                evup.recycle();
            }
            return false;
        }

        if (isIntercept) {
            final int action = ev.getAction() & MotionEvent.ACTION_MASK;
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)
                isInterceptEvent = false;
        }
        if (isIntercept)
            return true;

        return superDispatchTouchEvent(ev);
    }

    private boolean superDispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            LogUtils.d(TAG, "dispatchTouchEvent:" + ev.toString());
        }
        return false;
    }
}
