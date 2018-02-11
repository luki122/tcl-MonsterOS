/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;

import cn.tcl.meetingassistant.log.MeetingLog;
import mst.app.dialog.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import cn.tcl.meetingassistant.R;
import mst.app.dialog.ProgressDialog;

public class DialogHelper {

    private static String TAG = DialogHelper.class.getSimpleName();
    /**
     * base dialog,you need provide all info.
     *
     * @param context
     * @param listener
     * @param title
     * @param message
     * @param positive
     * @param negative
     */
    public static void showDialog(Context context, DialogInterface.OnClickListener listener,
                                  String title, String message,
                                  String positive, String negative) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setPositiveButton(positive, listener);
        if (negative != null) {
            dialog.setNegativeButton(negative, listener);
        }
        MeetingLog.i(TAG,"showDialog --> msg is " + message);
        dialog.show();
    }

    public static void showDialog(Context context, DialogInterface.OnClickListener listener,
                                  int title, int message,
                                  int positive, int negative) {
        String titleStr = context.getString(title);
        String messageStr = context.getString(message);
        String positiveStr = context.getString(positive);
        String negativeStr;
        if (negative != 0) {
            negativeStr = context.getString(negative);
        } else {
            negativeStr = null;
        }
        showDialog(context, listener, titleStr, messageStr, positiveStr, negativeStr);
    }

    /**
     * use the method,you need not provide positive and negative.They use default value ok and cancel.
     *
     * @param context
     * @param listener
     * @param title
     * @param message
     */
    public static void showDialog(Context context, DialogInterface.OnClickListener listener,
                                  String title, String message) {
        Resources resources = context.getResources();
        String positive = resources.getString(R.string.Confirm);
        String negative = resources.getString(R.string.cancel);
        showDialog(context, listener, title, message, positive, negative);
    }

    public static AlertDialog showProgressDialog(Context context,String msg){
        View view = LayoutInflater.from(context).inflate(R.layout.layout_progress_dialog,null);
        AlertDialog mAlertDialog = new AlertDialog.Builder(context).setCancelable(false).setView(view).create();
        TextView mTextView = (TextView) view.findViewById(R.id.progress_TextView_animation);
        mTextView.setText(msg);
        return mAlertDialog;
    }
}
