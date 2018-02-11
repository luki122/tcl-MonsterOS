/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.utils.store;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by thundersoft on 16-8-1.
 * use sharedPreference to store Strings
 */
public class SharedPreferenceStore implements IStore<String> {
    private SharedPreferences mSharedPreferences;

    public SharedPreferenceStore(Context context, String name) {
        mSharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    @Override
    public void store(String key, String item) {
        mSharedPreferences.edit().putString(key, item).commit();
    }

    @Override
    public String read(String key) {
        return mSharedPreferences.getString(key, "");
    }

    @Override
    public void remove(String key) {
        mSharedPreferences.edit().remove(key).commit();
    }

    @Override
    public void clearAll() {
        mSharedPreferences.edit().clear().commit();
    }

    @Override
    public void init() {

    }

    @Override
    public void recycle() {

    }

    @Override
    public void onTrimMemory(int level) {

    }
}
