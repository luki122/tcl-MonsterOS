package com.monster.cloud.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.telecom.Log;

import com.monster.cloud.constants.Constant;

import java.io.Serializable;

/**
 * Created by yubai on 16-10-31.
 */
public interface Preferences {
    SharedPreferences getSharedPreferences();

    /**
     * 获取保存的数据
     * @param context
     * @param key
     * @param c
     * @param <T>
     * @return
     */
    <T> T get(Context context, String key, Class<T> c);

    /**
     * 保存数据，持久化
     * @param context
     * @param key
     * @param obj
     * @return
     */
    boolean save(Context context, String key, Serializable obj);

    void setOpenId(String openId);
    void setToken(String token);
    void setLoginLabel(boolean isLogin);

    String getOpenId();
    String getToken();
    boolean getLoginLabel();

    final class Factory {
        public static final Preferences getInstance(Context context, int type) {
            if (type == Constant.FILE_TYPE) {
                return FilePreferences.getInstance(context);
            } else if (type == Constant.MEMORY_TYPE) {
                //有需要可以替换
                return null;
            }
            return null;
        }
    }
}
