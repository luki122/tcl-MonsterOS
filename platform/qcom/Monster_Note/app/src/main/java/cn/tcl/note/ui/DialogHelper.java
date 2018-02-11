/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;

import cn.tcl.note.R;
import cn.tcl.note.activity.NoteEditorAdapter;
import mst.app.dialog.AlertDialog;

public class DialogHelper {
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
        String positive = resources.getString(R.string.dialog_ok);
        String negative = resources.getString(R.string.dialog_cancel);
        showDialog(context, listener, title, message, positive, negative);
    }

    public static void showDialog(Context context, DialogInterface.OnClickListener listener,
                                  int title, int message) {
        int positive = R.string.dialog_ok;
        int negative = R.string.dialog_cancel;
        showDialog(context, listener, title, message, positive, negative);
    }

    public static void showDelDialog(Context context, DialogInterface.OnClickListener listener,
                                     NoteEditorAdapter.NoteAttachView noteAttachView) {
        Resources resources = context.getResources();
        String title = resources.getString(R.string.dialog_del_title);
        String msg;
        if (noteAttachView instanceof NoteEditorAdapter.NotePicView) {
            msg = resources.getString(R.string.dialog_del_img_msg);
        } else {
            msg = resources.getString(R.string.dialog_del_audio_msg);
        }
        showDialog(context, listener, title, msg);
    }

    public static void showOneDialog(Context context) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(context.getString(R.string.dialog_back_title));
        dialog.setMessage(context.getString(R.string.dialog_space_50m_stop));
        dialog.setNegativeButton(context.getString(R.string.dialog_i_know), null);
        dialog.show();
    }

    private static AlertDialog mAlertDialog;

    public static void showProgressDialog(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_progress_dialog, null);
        mAlertDialog = new AlertDialog.Builder(context).setCancelable(false).setView(view).create();
//        TextView mTextView = (TextView) view.findViewById(R.id.progress_TextView_animation);
//        mTextView.setText(msg);
        mAlertDialog.show();
    }

    public static void disProgressDialog() {
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }

    }
}
