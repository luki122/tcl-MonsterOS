/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import cn.tcl.meetingassistant.DecisionListActivity;
import cn.tcl.meetingassistant.R;
import cn.tcl.meetingassistant.log.MeetingLog;
import mst.app.MstActivity;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-10-11.
 * the background
 */
public class BackGroundActivity extends AbsMeetingActivity {
    private String TAG = BackGroundActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MeetingLog.i(TAG,"onCreate start");
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.background_activity);
        getToolbar().setVisibility(View.GONE);
        Intent intent = new Intent(BackGroundActivity.this,DecisionListActivity.class);
        startActivityForResult(intent,0);
        overridePendingTransition(R.anim.slide_in_bottom, 0);
        MeetingLog.i(TAG,"onCreate end");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==0){
            BackGroundActivity.this.finish();
        }
    }
}
