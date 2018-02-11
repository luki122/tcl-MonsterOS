package com.monster.cloud.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by yubai on 16-10-31.
 */
public class FilePreferences implements Preferences {
    private static final String PREFERENCE_NAME = "Cloud_Preference";
    private static final String PARAM_OPEN_ID = "qq_open_id";
    private static final String PARAM_TOKEN = "qq_token";
    private static final String PARAM_IS_QQ_SDK_LOGIN = "is_qq_sdk_login";

    //has synchronized
    public static final String PARAM_IS_CTT_AUTO_SYNC = "is_contact_sync";
    public static final String PARAM_IS_SMS_AUTO_SYNC = "is_sms_sync";
    public static final String PARAM_IS_RCD_AUTO_SYNC = "is_record_sync";
    public static final String PARAM_IS_APP_AUTO_SYNC = "is_app_list_sync";

    //sync time
    private static final String PARAM_CTT_SYNC_TIME = "contact_sync_time";
    private static final String PARAM_SMS_SYNC_TIME = "sms_sync_time";
    private static final String PARAM_RCD_SYNC_TIME = "record_sync_time";
    private static final String PARAM_APP_SYNC_TIME = "app_list_sync_time";

    //contact changed
    private static final String PARAM_CTT_CHG_TAG = "contact_change_label";

    //sync only when wifi on
    private static final String PARAM_IS_SYNC_WHEN_WIFI = "is_sync_when_wifi";

    //singleton
    private static Preferences preferences;
    private static SharedPreferences sharedPreferences;

    protected FilePreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
    }

    public static synchronized Preferences getInstance(Context context) {
        if (preferences == null) {
            preferences = new FilePreferences(context.getApplicationContext());
        }
        return preferences;
    }

    @Override
    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    @Override
    public <T> T get(Context context, String key, Class<T> c) {
        synchronized (key) {
            ObjectInputStream in = null;
            try {
                File file = context.getFileStreamPath(key);
                in = new ObjectInputStream(new FileInputStream(file));
                Object o = in.readObject();

                if (o != null && c.isInstance(o)) {
                    return (T) o;
                }

            } catch (Exception e) {

            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {

                }
            }
            return  null;
        }
    }

    @Override
    public boolean save(Context context, String key, Serializable obj) {
        synchronized (key) {
            ObjectOutputStream out = null;
            try {
                File file = context.getFileStreamPath(key);
                out = new ObjectOutputStream(new FileOutputStream(file));
                out.writeObject(obj);
                out.flush();
                return true;
            } catch (Exception e) {

            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {

                }
            }
        }
        return false;
    }

    @Override
    public void setOpenId(String openId) {
        getSharedPreferences().edit().putString(PARAM_OPEN_ID, openId).commit();
    }

    @Override
    public void setToken(String token) {
        getSharedPreferences().edit().putString(PARAM_TOKEN, token).commit();
    }

    @Override
    public String getOpenId() {
        return getSharedPreferences().getString(PARAM_OPEN_ID, null);
    }

    @Override
    public String getToken() {
        return getSharedPreferences().getString(PARAM_TOKEN, null);
    }

    public void setContactSyncLabel (boolean isContactSync) {
        getSharedPreferences().edit().putBoolean(PARAM_IS_CTT_AUTO_SYNC, isContactSync).commit();
    }

    public void setSmsSyncLabel (boolean isSmsSync) {
        getSharedPreferences().edit().putBoolean(PARAM_IS_SMS_AUTO_SYNC, isSmsSync).commit();
    }

    public void setRecordSyncLabel (boolean isRrcSync) {
        getSharedPreferences().edit().putBoolean(PARAM_IS_RCD_AUTO_SYNC, isRrcSync).commit();
    }

    public void setAppListSyncLabel (boolean isListSync) {
        getSharedPreferences().edit().putBoolean(PARAM_IS_APP_AUTO_SYNC, isListSync).commit();
    }

    public boolean getContactSyncLabel () {
        return getSharedPreferences().getBoolean(PARAM_IS_CTT_AUTO_SYNC, false);
    }

    public boolean getSmsSyncLabel () {
        return getSharedPreferences().getBoolean(PARAM_IS_SMS_AUTO_SYNC, false);
    }

    public boolean getRecordSyncLabel () {
        return getSharedPreferences().getBoolean(PARAM_IS_RCD_AUTO_SYNC, false);
    }

    public boolean getAppListSyncLabel () {
        return getSharedPreferences().getBoolean(PARAM_IS_APP_AUTO_SYNC, false);
    }

    public void setContactSyncTime (long contactSyncTime) {
        getSharedPreferences().edit().putLong(PARAM_CTT_SYNC_TIME, contactSyncTime).commit();
    }

    public void setSmsSyncTime (long smsSyncTime) {
        getSharedPreferences().edit().putLong(PARAM_SMS_SYNC_TIME, smsSyncTime).commit();
    }

    public void setRecordSyncTime (long recordSyncTime) {
        getSharedPreferences().edit().putLong(PARAM_RCD_SYNC_TIME, recordSyncTime).commit();
    }

    public void setAppListSyncTime (long listSyncTime) {
        getSharedPreferences().edit().putLong(PARAM_APP_SYNC_TIME, listSyncTime).commit();
    }

    public long getContactSyncTime () {
        return getSharedPreferences().getLong(PARAM_CTT_SYNC_TIME, 0l);
    }

    public long getSmsSyncTime () {
        return getSharedPreferences().getLong(PARAM_SMS_SYNC_TIME, 0l);
    }

    public long getRecordSyncTime () {
        return getSharedPreferences().getLong(PARAM_RCD_SYNC_TIME, 0l);
    }

    public long getAppListSyncTime () {
        return getSharedPreferences().getLong(PARAM_APP_SYNC_TIME, 0l);
    }

    public void setContactChangedLabel(boolean isChanged) {
        getSharedPreferences().edit().putBoolean(PARAM_CTT_CHG_TAG, isChanged).commit();
    }

    public boolean getContactChangedLabel() {
        return getSharedPreferences().getBoolean(PARAM_CTT_CHG_TAG, false);
    }

    //default: sync only when wifi on
    public void setSyncWhenWifiLabel(boolean isOnlyWifi) {
        getSharedPreferences().edit().putBoolean(PARAM_IS_SYNC_WHEN_WIFI, isOnlyWifi).commit();
    }

    public boolean getSyncWhenWifiLabel() {
        return getSharedPreferences().getBoolean(PARAM_IS_SYNC_WHEN_WIFI, true);
    }

    @Override
    public void setLoginLabel(boolean isLogin) {
        getSharedPreferences().edit().putBoolean(PARAM_IS_QQ_SDK_LOGIN, isLogin).commit();
    }

    @Override
    public boolean getLoginLabel() {
        return getSharedPreferences().getBoolean(PARAM_IS_QQ_SDK_LOGIN, false);
    }

}
