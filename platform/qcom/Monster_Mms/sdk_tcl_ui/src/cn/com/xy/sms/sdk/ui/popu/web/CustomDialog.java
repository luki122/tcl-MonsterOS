package cn.com.xy.sms.sdk.ui.popu.web;

import android.app.Dialog;
import android.content.Context;

public class CustomDialog extends Dialog {

    public CustomDialog(Context context, int dialogLayout, int dialogStyle) {
        super(context, dialogStyle);
        // set content
        setContentView(dialogLayout);
    }
}