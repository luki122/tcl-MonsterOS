/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;

import cn.tcl.transfer.R;
import cn.tcl.transfer.operator.wifi.APAdmin;
import cn.tcl.transfer.util.AppUtils;
import cn.tcl.transfer.util.DialogBuilder;
import cn.tcl.transfer.util.NotificationUtils;
import cn.tcl.transfer.util.Utils;

public class CompleteActivity extends BaseActivity {

    private static final String TAG = "CompleteActivity";
    private Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.complete_layout);
        boolean isSend = getIntent().getBooleanExtra(Utils.IS_SEND, true);
        TextView info = (TextView)findViewById(R.id.info);
        if(isSend) {
            Toast.makeText(this, R.string.notify_send_success, Toast.LENGTH_SHORT).show();
            info.setText(R.string.send_sucess);
        } else {
            Toast.makeText(this, R.string.notify_receive_success, Toast.LENGTH_SHORT).show();
            info.setText(R.string.recover_sucess);
        }
        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                //android.os.Process.killProcess(android.os.Process.myPid());
            }

        });
        APAdmin mApm;
        mApm = new APAdmin(this);
        if (mApm != null) {
            mApm.closeWifiAp();
        }
    }
    @Override
    public void onNavigationClicked(View view) {
        this.finish();
    }
}
