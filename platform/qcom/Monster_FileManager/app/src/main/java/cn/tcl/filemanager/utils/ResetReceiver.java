/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.filemanager.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class ResetReceiver extends BroadcastReceiver {
    public static final String PREF_VIEW_BY = "pref_view_by";
    public static final String LIST_MODE = "listMode";
    public static final String PREF_BY = "sort_item";
    private String prefsName = "activity.FileBrowserActivity";
    private String prefsNames = "com.jrdcom.filemanager_preferences";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefss = context.getSharedPreferences(prefsNames, Context.MODE_PRIVATE);
        SharedPreferences.Editor comeditor = prefss.edit();
        comeditor.putInt(PREF_BY, 0);
        comeditor.commit();
        SharedPreferences prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("pref_sort_by", 0);
        editor.putString(PREF_VIEW_BY, LIST_MODE);
        editor.commit();
        SharedPreferenceUtils.removeShowHiddenPref(context);
    }

}
