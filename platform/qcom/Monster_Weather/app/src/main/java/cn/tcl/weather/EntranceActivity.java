package cn.tcl.weather;

import android.app.Activity;
import android.os.Bundle;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-10-14.
 * Decide which activity to entrance
 */
public class EntranceActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // Jump to rom's correspoding main activity
        ActivityFactory.jumpToActivity(ActivityFactory.MAIN_ACTIVITY, this, null);

        // After main activity start, this activity should be destroy
        finish();
    }

}
