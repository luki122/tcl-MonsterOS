package cn.tcl.music.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceUtil {
    //search history
    public static final String NODE_SEARCH_HISTORY = "SEARCH_HISTORY";
    public static final String KEY_SEARCH_HISTORY_COUNT = "KEY_SEARCH_HISTORY_COUNT";
    public static final String KEY_SEARCH_HISTORY_UPDATE = "KEY_SEARCH_HISTORY_UPDATE";

    /****************************** Media Queue Begin****************************/
    public static final String NODE_MEDIA_QUEUE = "media_queue";
    public static final String KEY_MEDIA_QUEUE_MODE = "preference_queue_mode";
    /****************************** Media Queue End****************************/

    /****************************** Setting activity Begin****************************/
    public static final String NODE_COUNT_TIME = "node_count_time";
    public static final String KEY_COUNT_TIME = "key_count_time";

    public static final String NODE_NETWORK_SWITCH = "node_network_switch";
    public static final String KEY_NETWORK_SWITCH = "key_network_switch";

    public static final String NODE_DATA_SAVER_CHOICE = "node_data_saver_choice";
    public static final String KEY_DATA_SAVER_CHOICE = "key_data_saver_choice";

    public static final String NODE_AUDITION_QUALITY = "node_audition_quality";
    public static final String KEY_AUDITION_QUALITY = "key_audition_quality";

    public static final String NODE_DOWNLOAD_QUALITY = "node_download_quality";
    public static final String KEY_DOWNLOAD_QUALITY = "key_download_quality";
    /****************************** Setting activity End****************************/

    /****************************** Welcome activity Begin****************************/
    public static final String NODE_IS_AGREED = "node_is_agreed";
    public static final String KEY_IS_AGREED = "key_is_agreed";
    /****************************** Welcome activity End****************************/

    /****************************** xiami member info Begin****************************/
    public static final String NODE_XIAMI_MEMBER_INFO = "node_xiami_member_info";
    public static final String KEY_USER_ID = "key_user_id";
    public static final String KEY_NICK_NAME = "key_nick_name";
    public static final String KEY_AVATAR = "key_avatar";
    public static final String KEY_GENDER = "key_gender";
    public static final String KEY_DESCRIPTION = "key_description";
    public static final String KEY_GMT_CREATE = "key_gmt_create";
    public static final String KEY_SIGNATURE = "key_signature";
    public static final String KEY_FANS = "key_fans";
    public static final String KEY_FOLLOWERS = "key_followers";
    public static final String KEY_LISTENS = "key_listens";
    public static final String KEY_COLLECT_COUNT = "key_collect_count";
    public static final String KEY_IS_VIP = "key_is_vip";
    public static final String KEY_VIP_BEGIN = "key_vip_begin";
    public static final String KEY_VIP_FINISH = "key_vip_finish";
    public static final String KEY_IS_SELF = "key_is_self";
    public static final String KEY_FRIENDSHIP = "key_friendship";
    /****************************** xiami member info end****************************/

    public static int getValue(Context context, String node, String key, int defaultValue) {
        return context.getSharedPreferences(node, Context.MODE_PRIVATE).getInt(key, defaultValue);
    }

    public static void saveValue(Context context, String node, String key, int value) {
        SharedPreferences.Editor sp = context.getSharedPreferences(node, Context.MODE_PRIVATE).edit();
        sp.putInt(key, value);
        sp.commit();
    }

    public static String getValue(Context context, String node, String key, String defaultValue) {
        return context.getSharedPreferences(node, Context.MODE_PRIVATE).getString(key, defaultValue);
    }

    public static void saveValue(Context context, String node, String key, String value) {
        SharedPreferences.Editor sp = context.getSharedPreferences(node, Context.MODE_PRIVATE).edit();
        sp.putString(key, value);
        sp.commit();
    }

    public static boolean getValue(Context context, String node, String key, boolean defaultValue) {
        return context.getSharedPreferences(node, Context.MODE_PRIVATE).getBoolean(key, defaultValue);
    }

    public static void saveValue(Context context, String node, String key, boolean value) {
        SharedPreferences.Editor sp = context.getSharedPreferences(node, Context.MODE_PRIVATE).edit();
        sp.putBoolean(key, value);
        sp.commit();
    }

    public static long getValue(Context context, String node, String key, long defaultValue) {
        return context.getSharedPreferences(node, Context.MODE_PRIVATE).getLong(key, defaultValue);
    }

    public static void saveValue(Context context, String node, String key, long value) {
        SharedPreferences.Editor sp = context.getSharedPreferences(node, Context.MODE_PRIVATE).edit();
        sp.putLong(key, value);
        sp.commit();
    }

}
