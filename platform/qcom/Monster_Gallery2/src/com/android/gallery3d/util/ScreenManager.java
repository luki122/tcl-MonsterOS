/* ----------|----------------------|----------------------|----------------- */
/* 04/27/2015| jian.pan1            | PR950449             |[Android5.0][Gallery_v5.1.9.1.0103.0][Monitor]][Force Close]Gallery force close when clicking back key twice
/* ----------|----------------------|----------------------|----------------- */
/* 29/06/2015 |    jialiang.ren     |      PR-1031240         |[SW][Gallery][FC][Translation]Gallery will FC when switch language*/
/*------------|---------------------|-------------------------|------------------------------------------------------------------*/

/**
 * This file is added by TCT, annotated by ShenQianfeng on 2016.08.18
 */
package com.android.gallery3d.util;

import java.util.Stack;

import android.app.Activity;

public class ScreenManager {

    private String TAG = "ScreenManager";

    private Stack<Activity> activityStack;

    private static ScreenManager instance;

    private ScreenManager() {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
    }

    public static ScreenManager getScreenManager() {
        if (instance == null) {
            instance = new ScreenManager();
        }
        return instance;
    }

    private void pushActivity(Activity activity) {
        activityStack.add(activity);
    }

    /**
     * finish and remove all stack activities
     */
    private void clearAllStack() {
        if (activityStack != null && !activityStack.isEmpty()) {
            Log.i(TAG, "activityStack.size()" + activityStack.size());
            for (int i = 0; i < activityStack.size(); i++) {
                Activity act = activityStack.elementAt(i);
                //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-06-29,PR1031240 begin
                if(act != null && !act.isDestroyed()) {
                    act.finish();
                    act = null;
                }
                //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-06-29,PR1031240 end
                activityStack.remove(i);
            }
        }
    }

    /**
     * reset current activity
     * 
     * @param activity
     *            the activity which need be reseted
     */
    public void resetCurrentActivity(Activity activity) {
        Log.i(TAG, "resetCurrentActivity is in");
        clearAllStack();
        pushActivity(activity);
    }
}
