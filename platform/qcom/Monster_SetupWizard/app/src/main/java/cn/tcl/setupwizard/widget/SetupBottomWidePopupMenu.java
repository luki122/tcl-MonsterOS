/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.setupwizard.widget;

import android.content.Context;

import cn.tcl.setupwizard.utils.LogUtils;
import cn.tcl.setupwizard.utils.SystemBarHelper;
import mst.view.menu.BottomWidePopupMenu;

public class SetupBottomWidePopupMenu extends BottomWidePopupMenu {

    private static final String TAG = "SetupBottomWidePopupMenu";

    public SetupBottomWidePopupMenu(Context context) {
        super(context);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        LogUtils.d(TAG, "hasFocus: " + hasFocus);
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            SystemBarHelper.hideSystemBarsWithFocus(this);
        }
    }
}
