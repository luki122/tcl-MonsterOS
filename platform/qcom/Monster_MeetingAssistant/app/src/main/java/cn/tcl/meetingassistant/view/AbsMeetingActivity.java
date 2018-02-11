/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;

import android.os.Build;
import android.view.View;
import android.view.Window;

import mst.app.MstActivity;

/**
 * Created on 16-9-9.
 */
public class AbsMeetingActivity extends MstActivity {

    private final String TAG = AbsMeetingActivity.class.getSimpleName();

    @Override
    protected void onResume() {
        super.onResume();
        //initStatusBar();
    }

    /**
     * change status bar background color
     */
    private void initStatusBar() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

}
