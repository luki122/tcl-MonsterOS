/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.viewhelper;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.TextView;

import cn.tcl.weather.R;
import cn.tcl.weather.WeatherCNApplication;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 *
 * Created on 16-8-5.
 */
public class ProgressDialogVh extends Dialog{
    private TextView mMessage;
    private Window mWindow;

    public ProgressDialogVh(Context context) {
        super(context);
        initView();
    }

    public ProgressDialogVh(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected ProgressDialogVh(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void initView(){
        // Set transparent background
        mWindow = getWindow();
        mWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mWindow.requestFeature(Window.FEATURE_NO_TITLE);

        // Judge system's type
        if(WeatherCNApplication.getWeatherCnApplication().getCurrentSystemType()==WeatherCNApplication.SYSTEM_TYPE_LONDON){
            setContentView(R.layout.tcl_progress_dialog_layout);
        }else{
            setContentView(R.layout.other_progress_dialog_layout);
            mMessage = (TextView) findViewById(R.id.progress_message);
        }
    }

    public void showDialog(String message){
        if(mMessage!=null)
            mMessage.setText(message);
        show();
    }

    public void hideDialog(){
        hide();
    }
}
