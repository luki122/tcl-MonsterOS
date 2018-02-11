package cn.tcl.music.util;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.activities.LocalMusicActivity;

public class ActivitiesManager {
    private static final String TAG = ActivitiesManager.class.getSimpleName();
    private static List<Activity> activities = new ArrayList<>();

    public static void clearActivities(boolean includeMain) {
        Log.d(TAG, "clearActivities");
        if (activities == null) {
            return;
        }
        for (Activity activity : activities) {
            if (activity != null) {
                if (!includeMain && activity instanceof LocalMusicActivity) {
                    Log.d(TAG, "continue");
                    continue;
                }
                Log.d(TAG, "activity " + activity.getClass().getSimpleName() + " finish()");
                activity.finish();
            }
        }
    }

    public static void pushActivity(Activity activity) {
        if (activities != null && !activities.contains(activity)) {
            Log.d(TAG, "push " + activity.getClass().getSimpleName());
            activities.add(activity);
        }
    }

    public static void popActivity(Activity activity) {
        if (activities != null && activities.contains(activity)) {
            Log.d(TAG, "pop " + activity.getClass().getSimpleName());
            activities.remove(activity);
        }
    }
}
