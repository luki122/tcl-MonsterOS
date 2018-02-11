package com.monster.cloud.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.Serializable;

/**
 * Created by yubai on 16-10-31.
 * 预留内存存贮类  可以根据需要重写
 */
public class MemoryPreferences implements Preferences {

    @Override
    public SharedPreferences getSharedPreferences() {
        return null;
    }

    @Override
    public <T> T get(Context context, String key, Class<T> c) {
        return null;
    }

    @Override
    public boolean save(Context context, String key, Serializable obj) {
        return false;
    }

    @Override
    public void setOpenId(String openId) {

    }

    @Override
    public void setToken(String token) {

    }

    @Override
    public String getOpenId() {
        return null;
    }

    @Override
    public String getToken() {
        return null;
    }

    @Override
    public void setLoginLabel(boolean isLogin) {

    }

    @Override
    public boolean getLoginLabel() {
        return false;
    }
}
