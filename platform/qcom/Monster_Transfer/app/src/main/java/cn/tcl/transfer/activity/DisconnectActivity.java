/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Telephony;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;

import cn.tcl.transfer.R;
import cn.tcl.transfer.operator.wifi.APAdmin;
import cn.tcl.transfer.util.AppUtils;
import cn.tcl.transfer.util.NotificationUtils;
import cn.tcl.transfer.util.Utils;
import mst.app.MstActivity;

public class DisconnectActivity extends MstActivity {
    Button mBtn_return;
    APAdmin mApm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_disconnect);
        boolean isFail = getIntent().getBooleanExtra(Utils.IS_FAIL, false);
        TextView info = (TextView)findViewById(R.id.info);
        if(isFail) {
            Toast.makeText(this, R.string.notify_send_success, Toast.LENGTH_SHORT).show();
            info.setText(R.string.qq_text_receive_fail);
        } else {
            Toast.makeText(this, R.string.text_disconnected, Toast.LENGTH_SHORT).show();
            info.setText(R.string.text_disconnected);
        }
        mBtn_return = (Button)findViewById(R.id.cancel);
        mBtn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DisconnectActivity.this.finish();
            }
        });
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
