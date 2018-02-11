/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import cn.tcl.transfer.R;
import cn.tcl.transfer.util.CalculateSizeTask;
import cn.tcl.transfer.zxing.client.android.CaptureActivity;
import mst.app.MstActivity;

public class OldNoteActivity extends MstActivity {
    private static final int MIN_BATTERY = 35;
    private static final int BATTERY_SCALE_DEFAULT = 100;
    private static final int PERMISSION_CODE = 1;
    private static final String LEVEL = "level";
    private static final String SCALE = "scale";
    private TextView mTextBattery;
    private TextView mTextBatteryNote;
    private Button mBtnCancel;
    private Button mBtnContinue;
    private ImageView mBatteryIcon;
    private ImageView mTopBackground;
    private BatteryReceiver batteryReceiver = new BatteryReceiver();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_note);
        mTextBattery = (TextView)findViewById(R.id.battery_low);
        mTextBatteryNote = (TextView)findViewById(R.id.battery_state);
        mBatteryIcon = (ImageView)findViewById(R.id.battery_image);
        mTopBackground = (ImageView)findViewById(R.id.top_image);
        mBtnCancel = (Button)findViewById(R.id.cancel);
        mBtnContinue = (Button)findViewById(R.id.ok);
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, intentFilter);
        mBtnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent intent = new Intent(OldNoteActivity.this, CaptureActivity.class);
                    startActivity(intent);
                    finish();
            }
        });
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    class BatteryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())){
                int level = intent.getIntExtra(LEVEL, 0);
                int scale = intent.getIntExtra(SCALE, BATTERY_SCALE_DEFAULT);
                if ((level*BATTERY_SCALE_DEFAULT)/scale >= MIN_BATTERY) {
                    mTextBatteryNote.setTextColor(getResources().getColor(R.color.note_text_color));
                    mTextBattery.setVisibility(View.INVISIBLE);
                    mBatteryIcon.setVisibility(View.INVISIBLE);
                    mTopBackground.setImageResource(R.drawable.note_bg);
                    mBtnContinue.setEnabled(true);
                } else {
                    mTextBatteryNote.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                    mTextBattery.setVisibility(View.VISIBLE);
                    mBatteryIcon.setVisibility(View.VISIBLE);
                    mTopBackground.setImageResource(R.drawable.note_low_battery);
                    mBtnContinue.setEnabled(false);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(batteryReceiver);
        super.onDestroy();
    }
    @Override
    public void onNavigationClicked(View view) {
        this.finish();
    }
}
