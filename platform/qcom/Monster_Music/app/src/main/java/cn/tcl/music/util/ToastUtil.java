package cn.tcl.music.util;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class ToastUtil {

    private static Toast mToast;
    private static long mLastToastTime = 0;
    private static String mLastToastContent = "";

    public static void showToast(Context mContext, String text) {
        if (mLastToastTime != 0 && !mLastToastContent.isEmpty() && mToast != null) {
            if (text.equals(mLastToastContent) && System.currentTimeMillis() - mLastToastTime < 1000) {
                return;
            }
        }
        if (mToast != null) {
            mToast.setText(text);
        } else {
            mToast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
        }
        mToast.setGravity(Gravity.BOTTOM, 0, 10);
        mToast.show();
        mLastToastContent = text;
        mLastToastTime = System.currentTimeMillis();

    }

    public static void showToast(Context mContext, int resId) {
        showToast(mContext, mContext.getResources().getString(resId));
    }

}
