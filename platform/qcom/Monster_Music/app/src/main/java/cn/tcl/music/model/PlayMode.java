package cn.tcl.music.model;

import android.content.Context;

import cn.tcl.music.util.PreferenceUtil;

public class PlayMode {
    public static final int PLAY_MODE_NORMAL = 0;
    public static final int PLAY_MODE_RANDOM = 1;
    public static final int PLAY_MODE_REPEAT = 2;
    public final static String PREFERENCE_SAVED_MODE = "cn.tcl.music.mode";

    public static int getMode(Context context) {
        return PreferenceUtil.getValue(context,PreferenceUtil.NODE_MEDIA_QUEUE,PreferenceUtil.KEY_MEDIA_QUEUE_MODE,PLAY_MODE_NORMAL);
    }

    public static void setMode(Context context,int mode) {
        PreferenceUtil.saveValue(context,PreferenceUtil.NODE_MEDIA_QUEUE,PreferenceUtil.KEY_MEDIA_QUEUE_MODE,mode);
    }
}
