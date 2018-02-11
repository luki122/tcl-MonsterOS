package com.android.phone;

import android.app.Activity;
import mst.app.dialog.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.telephony.TelephonyManager;

public class ShowUssdWarning extends Activity{
    private static final String LOG_TAG = "ShowUssdWarning";
    public static final String PACKAGE = "PACKAGE";
    public static final String TARGET_CLASS = "TARGET_CLASS";
    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.e(LOG_TAG, "------oncreate------");
        showDialog();
    }

    private void showDialog() {
        AlertDialog alterDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.ussd_dialog_title)
        .setMessage(R.string.ussd_dialog_text)
                .setPositiveButton(R.string.ussd_dialog_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(dialog != null) {
                                dialog.dismiss();
                            }
                            if(TelephonyManager.getDefault().isMultiSimEnabled()) {
                                //Begin fix Defect 1544738, zhi-zhang@tcl.com, 2016/2/23
                                intent = new Intent("android.intent.action.USSD");
                                intent.putExtra(TARGET_CLASS, "com.android.phone.MobileNetworkSubSettings");
                                //End fix Defect 1544738, zhi-zhang@tcl.com, 2016/2/23
                                intent.putExtra(PACKAGE, "com.android.phone");
                            } else {
                                intent = new Intent("android.intent.action.USSD");
                            }
                            startActivity(intent);
                            finish();
                        }})
                        .setNegativeButton("OK",
                        new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }})
                        .setOnDismissListener(new OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                finish();
                            }
                        });
        alterDialog =  builder.show();
        alterDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setAllCaps(false);
        alterDialog.getButton(DialogInterface.BUTTON_POSITIVE).setAllCaps(false);
    }

}
