/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.util;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import cn.tcl.transfer.R;
import mst.app.dialog.AlertDialog;
import mst.app.dialog.ProgressDialog;


public class DialogBuilder {

    public static Dialog createLoadingDialog(Context context,View.OnClickListener positiveListener) {
        ViewGroup contentView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.dialog_loading_progress, null);
        Button btnCancel = (Button)contentView.findViewById(R.id.cancel);
        btnCancel.setOnClickListener(positiveListener);
        Dialog dialog = new Dialog(context, R.style.exitAppDialog);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        dialog.setContentView(contentView, params);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public static Dialog createConfirmDialog(Context context,DialogInterface.OnClickListener positiveListener) {
        Dialog dialog = new AlertDialog.Builder(context).
                setMessage(R.string.text_connect_fail).
                setPositiveButton(R.string.text_confirm,positiveListener).
                setCancelable(false).
                create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public static Dialog createConfirmDialog(Context context,DialogInterface.OnClickListener positiveListener,DialogInterface.OnClickListener negativeListener,String message) {
        Dialog dialog = new AlertDialog.Builder(context).
                setMessage(message).
                setPositiveButton(R.string.text_confirm,positiveListener).
                setNegativeButton(R.string.text_cancel,negativeListener).
                setCancelable(false).
                create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public static Dialog createProgressDialog(Context context, String message) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public static Dialog createSmsConfirmDialog(Context context,OnClickListener positiveListener,String message) {
        Dialog dialog = new AlertDialog.Builder(context).
                setMessage(message).
                setPositiveButton(R.string.text_confirm, positiveListener).
                setCancelable(false).
                create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

}
