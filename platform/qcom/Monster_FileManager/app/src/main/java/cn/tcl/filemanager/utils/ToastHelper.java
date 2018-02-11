/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastHelper {

    private final Context mContext;
   // private Toast mToast = null;
    private Toast mToast;

    /**
     * Constructor for ToastHelper, construct a ToastHelper with certain context
     *
     * @param context The context to use, there will be the associated activity.
     */
    public ToastHelper(Context context) {
//        if (context == null) {
//            throw new IllegalArgumentException();
//        }
        mContext = context;
    }

    /**
     * Show a Toast(Toast.LENGTH_SHORT).
     *
     * @param text the content shown on the Toast.
     */
    public void showToast(String text) {
        if (mToast == null) {
            mToast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
        }
        mToast.show();
    }

    /**
     * Show a Toast(Toast.LENGTH_SHORT).
     *
     * @param resId the content from Resource(strings.xml) shown on the Toast.
     */
    public void showToast(int resId) {
        if (mToast == null) {
            mToast = Toast.makeText(mContext, resId, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(resId);
        }
        mToast.show();
    }

}
