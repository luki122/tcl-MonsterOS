/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Telephony;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import cn.tcl.transfer.R;
import cn.tcl.transfer.receiver.ReceiveBackupDataService;
import cn.tcl.transfer.send.SendBackupDataService;
import cn.tcl.transfer.systemApp.CalendarSysApp;
import cn.tcl.transfer.systemApp.ContactsSysApp;
import cn.tcl.transfer.systemApp.DialerSysApp;
import cn.tcl.transfer.systemApp.MmsSysApp;
import cn.tcl.transfer.util.AppUtils;
import cn.tcl.transfer.util.DataManager;
import mst.app.MstActivity;

public class MainActivity extends MstActivity {

    private static final String TAG = "MainActivity";
    private Button mNewPhone;
    private Button mOldPhone;

    private static final int PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_send_or_receive);
        mNewPhone = (Button)findViewById(R.id.new_phone);
        mOldPhone = (Button)findViewById(R.id.old_phone);
        mNewPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,ChooseModeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        mOldPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,OldNoteActivity.class);
                startActivity(intent);
                finish();
            }
        });
        AppUtils.setDefaultSms(getApplicationContext(),getPackageName());
        resetTransferData();
        DataManager.getInstance(this).init();
    }

    private void resetTransferData() {

        ReceiveBackupDataService.startRecvTime = 0;
        ReceiveBackupDataService.mTotalReceiveSize = 0;
        ReceiveBackupDataService.mCurrentReceiveSize = 0;
        ReceiveBackupDataService.isCancelled = false;
        ReceivingActivity.hasLauncher = false;

        SendBackupDataService.mCurrentSendType = 0;
        SendBackupDataService.isCancelled = false;
        SendBackupDataService.startSendTime = 0;
        SendBackupDataService.mCurrentTotalSendSize = 0;
        SendBackupDataService.mCurrentSendSize = 0;
        SendBackupDataService.mTotalSize = 0;
        ReceivingActivity.initDataList(getApplicationContext());

        CalendarSysApp.startTime = 0;
        CalendarSysApp.localCount = 0;
        CalendarSysApp.recvCount = 0;
        CalendarSysApp.inertCount = 0;

        ContactsSysApp.startTime = 0;
        ContactsSysApp.localCount = 0;
        ContactsSysApp.recvCount = 0;
        ContactsSysApp.inertCount = 0;

        DialerSysApp.startTime = 0;
        DialerSysApp.localCount = 0;
        DialerSysApp.recvCount = 0;
        DialerSysApp.inertCount = 0;

        MmsSysApp.smsStartTime = 0;
        MmsSysApp.mmsStartTime = 0;
        MmsSysApp.smsCount = 0;
        MmsSysApp.mmsCount = 0;
        MmsSysApp.smsRecvCount = 0;
        MmsSysApp.mmsRecvCount = 0;
        MmsSysApp.smsInertCount = 0;
        MmsSysApp.mmsInertCount = 0;
    }

    @Override
    public void onNavigationClicked(View view) {
        this.finish();
    }

}
