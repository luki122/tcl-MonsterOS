package com.android.camera.util;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.DimenRes;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;
import com.android.camera.debug.Log;
import com.tct.camera.BuildConfig;
import com.tct.camera.R;

/**
 * Created by wenhua.tu on 11/30/15.
 */
public class ToastUtil {

    private static final Log.Tag TAG = new Log.Tag("ToastUtil");
    private static Toast mToast;
    private static TextView mToastBackground;
    private static int mDuration;
    private static Handler mHandler = new Handler();
    private static Runnable mCancelRunnable = new Runnable() {
        @Override
        public void run() {
            mToast.cancel();
        }
    };

    private static Toast makeText(Context context, String text, int duration) {
        if (mToastBackground == null) {
            mToastBackground = new TextView(context);
            if(ApiHelper.isMOrHigher() ){
                mToastBackground.setTextAppearance(R.style.ToastStyle);
                mToastBackground.setGravity(Gravity.CENTER_HORIZONTAL);
                mToastBackground.setBackgroundResource(R.drawable.bg_micro_shoot_tip);
                mToastBackground.setPadding(getDimension(context, R.dimen.toast_padding_left),
                        getDimension(context, R.dimen.toast_padding_top),
                        getDimension(context, R.dimen.toast_padding_right),
                        getDimension(context, R.dimen.toast_padding_bottom));
            }
        }

        if (mToast == null) {
            mToast = Toast.makeText(context, text, duration);
            mToast.setGravity(Gravity.CENTER_HORIZONTAL, 0, getDimension(context, R.dimen.toast_margin_Bottom));
            if(ApiHelper.isMOrHigher() ) {
                mToast.setView(mToastBackground);
            }
        }

        mToastBackground.setText(text);
        mDuration = duration;
        if (duration == Toast.LENGTH_SHORT) {
            mDuration = 2000;
        } else if (duration == Toast.LENGTH_LONG) {
            mDuration = 3500;
        }

        return mToast;
    }

    private static void show() {
        mHandler.removeCallbacks(mCancelRunnable);
        mHandler.postDelayed(mCancelRunnable, mDuration);

        mToast.show();
    }

    public static void showToast(Context context, String text, int duration) {
        makeText(context, text, duration);
        show();
    }

    public static void showToast(Context context, int id, int duration) {
        String text=context.getResources().getString(id);
        showToast(context, text, duration);
    }

    public static void cancelToast() {
        mHandler.removeCallbacks(mCancelRunnable);
        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }
    }
    private static int getDimension(Context context, @DimenRes int id) {
        return (int) context.getResources().getDimension(id);
    }
}
