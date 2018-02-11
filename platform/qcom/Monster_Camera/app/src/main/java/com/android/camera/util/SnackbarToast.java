/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.camera.util;

import android.content.Context;
import android.view.Gravity;
/* MODIFIED-BEGIN by bin-liu3, 2016-11-08,BUG-3253898*/
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.tct.camera.R;

public class SnackbarToast {
    private  Toast mToast;
    private  static SnackbarToast snackbarToast=null;
    public  static int LENGTH_LONG = Toast.LENGTH_LONG;
    public  static int LENGTH_SHORT = Toast.LENGTH_SHORT;

    public static int DEFAULT_Y_OFFSET = 400;

    /**
     * Pop the custom toast.
     * @param context
     * @param message
     * @param showtime
     * @param yOffset offset with center of screen.
     */
    public  void showToast(Context context, String message,int showtime,int yOffset) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.snackbar_layout, null);
        TextView text = (TextView) view.findViewById(R.id.snack_text);
        text.setText(message);
        if (mToast == null) {
            mToast = new Toast(context);
            // xOffset MUST not change.
            mToast.setGravity(Gravity.CENTER, 0, yOffset);
            mToast.setDuration(showtime);
            mToast.setView(view);
        /* MODIFIED-BEGIN by bin-liu3, 2016-11-14,BUG-3445710*/
        }else {
            text.setText(message);
            mToast.setGravity(Gravity.CENTER, 0, yOffset);
            mToast.setDuration(showtime);
            mToast.setView(view);
            /* MODIFIED-END by bin-liu3,BUG-3445710*/
        }
        mToast.show();
    }
    public static SnackbarToast getSnackbarToast (){
        if(snackbarToast == null){
            snackbarToast = new SnackbarToast();
        }
        return snackbarToast;
    }
    private SnackbarToast(){
    }
    public void cancle(){
        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }
    }
}
