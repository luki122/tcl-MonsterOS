package com.android.systemui.tcl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import mst.app.MstActivity;

public class BaseActivity extends MstActivity {
    private final String TAG = getClass().getSimpleName();
    public static final String ACTION_FINISH_ACTIVITY = "action_finish_activity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG,getClass().getSimpleName()+" onCreate()...");
        IntentFilter filter = new IntentFilter(ACTION_FINISH_ACTIVITY);
        registerReceiver(receiver,filter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG,getClass().getSimpleName()+" onStart()...");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG,getClass().getSimpleName()+" onResume()...");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG,getClass().getSimpleName()+" onPause()...");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG,getClass().getSimpleName()+" onStop()...");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG,getClass().getSimpleName()+" onDestroy()...");
        unregisterReceiver(receiver);
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case ACTION_FINISH_ACTIVITY:
                    finish();
                    break;
                default:
                    break;
            }
        }
    };
}
