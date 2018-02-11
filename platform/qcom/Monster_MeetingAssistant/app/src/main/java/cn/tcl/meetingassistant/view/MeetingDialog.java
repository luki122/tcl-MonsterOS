/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.app.AlertDialog;

/**
 * Created on 16-9-2.
 */
public class MeetingDialog extends AlertDialog {

    protected MeetingDialog(@NonNull Context context) {
        super(context);
    }

    protected MeetingDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }

    protected MeetingDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

}
