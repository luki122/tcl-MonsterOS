package com.android.calculator2;
import android.content.Context;
import android.view.WindowManager;
import mst.app.dialog.AlertDialog;
public class MyAlertDialog extends AlertDialog{

    public MyAlertDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public MyAlertDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public MyAlertDialog(Context context) {
        super(context);
    }
    
    @Override
    public void show() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        // Show the dialog with NavBar hidden.
        super.show();

        // Set the dialog to focusable again.
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

}
