package com.tcl.monster.fota.utils;

import android.content.Context;
import android.content.DialogInterface;

import mst.app.dialog.AlertDialog;

public class AlertDialogUtil {

    private static AlertDialogUtil instance = null;

    private AlertDialog.Builder builder;

    private AlertDialogUtil() {
    }

    public static AlertDialogUtil getInstance() {
        if (instance == null) {
            instance = new AlertDialogUtil();
        }
        return instance;
    }

    public void show(Context context, String title, String message,
                     String negativeButtonTxt, DialogInterface.OnClickListener negativeButtonListener,
                     String positiveButtonTxt, DialogInterface.OnClickListener positiveButtonListener) {
        builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(true);
        if (negativeButtonTxt != null && negativeButtonTxt.endsWith("")) {
            builder.setNegativeButton(negativeButtonTxt, negativeButtonListener);
        }
        builder.setPositiveButton(positiveButtonTxt, positiveButtonListener);
        builder.create().show();
    }

    public void show(Context context, int titleRes, int messageRes,
                     int negativeButtonTxtRes, DialogInterface.OnClickListener negativeButtonListener,
                     int positiveButtonTxtRes, DialogInterface.OnClickListener positiveButtonListener) {
        builder = new AlertDialog.Builder(context);
        builder.setTitle(titleRes);
        builder.setMessage(messageRes);
        builder.setCancelable(true);
        builder.setNegativeButton(negativeButtonTxtRes, negativeButtonListener);
        builder.setPositiveButton(positiveButtonTxtRes, positiveButtonListener);
        builder.create().show();
    }

    public void showPositive(Context context, int titleRes, int messageRes,
                             int positiveButtonTxtRes,
                             DialogInterface.OnClickListener positiveButtonListener,
                             boolean cancelable) {
        builder = new AlertDialog.Builder(context);
        builder.setTitle(titleRes);
        builder.setMessage(messageRes);
        builder.setCancelable(cancelable);
        builder.setPositiveButton(positiveButtonTxtRes, positiveButtonListener);
        builder.create().show();
    }
}