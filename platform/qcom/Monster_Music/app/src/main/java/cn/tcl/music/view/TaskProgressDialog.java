package cn.tcl.music.view;

import android.content.Context;

import mst.app.dialog.ProgressDialog;

public class TaskProgressDialog extends ProgressDialog{

    OnBackPressListener mOnBackPressListener;

    public interface OnBackPressListener{
        void onBackPress();
    }

    public TaskProgressDialog(Context context) {
        super(context);
    }

    public TaskProgressDialog(Context context, int theme) {
        super(context,theme);
    }

    public TaskProgressDialog(Context context,OnBackPressListener listener) {
        super(context);
        mOnBackPressListener = listener;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(null != mOnBackPressListener) {
            mOnBackPressListener.onBackPress();
        }
    }
}
