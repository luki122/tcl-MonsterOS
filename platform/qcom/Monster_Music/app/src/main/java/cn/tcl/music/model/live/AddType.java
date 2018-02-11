package cn.tcl.music.model.live;


import android.content.Context;

import cn.tcl.music.util.PreferenceUtil;

public class AddType {
    public static final String PREFERENCE_KEY_PLAYLIST_MUSIC_RESOURCE = "preference_key_playlist_music_resource";
    public static final String PREFERENCE_KEY_PLAYLIST_MUSIC_ID = "preference_key_playlist_music_id";
    public static final int ADD_TYPE_LOCAL_MUSIC = 0;
    public static final int ADD_TYPE_MY_FAVOURITE = 1;
    public static final int ADD_TYPE_FOLDER = 2;
    public static final int ADD_TYPE_ALBUM = 3;
    public static final int ADD_TYPE_RECENT_PLAYED = 4;
    public static final int ADD_TYPE_USER_OWN_PLAYLIST = 5;
    public static final int ADD_TYPE_CONTAIN_SEARCH_SONG = 6;
    public static final int ADD_TYPE_SCENE_DETAIL_OTHERS = 7;

    public static void setAddType(Context context, int type) {
        PreferenceUtil.saveValue(context, PREFERENCE_KEY_PLAYLIST_MUSIC_RESOURCE, PREFERENCE_KEY_PLAYLIST_MUSIC_RESOURCE, type);
    }

    public static int getAddType(Context context) {
        return PreferenceUtil.getValue(context, PREFERENCE_KEY_PLAYLIST_MUSIC_RESOURCE, PREFERENCE_KEY_PLAYLIST_MUSIC_RESOURCE, -1);
    }

    public static void setPlayListID(Context context, long id) {
        PreferenceUtil.saveValue(context, PREFERENCE_KEY_PLAYLIST_MUSIC_ID, PREFERENCE_KEY_PLAYLIST_MUSIC_ID, id);
    }

    public static long getPlayListID(Context context) {
        return PreferenceUtil.getValue(context, PREFERENCE_KEY_PLAYLIST_MUSIC_ID, PREFERENCE_KEY_PLAYLIST_MUSIC_ID, -1L);
    }
}
