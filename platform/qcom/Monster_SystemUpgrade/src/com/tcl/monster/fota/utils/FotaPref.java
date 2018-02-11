package com.tcl.monster.fota.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.tcl.monster.fota.R;

/**
 * Saves and restores Single Download task settings, which is 
 * currently downloading task settings. Include :
 * 1)auto download,
 * 2)auto install 
 * 3)postpone time left
 * 4)Dir of saving update package
 * 5)Download ID
 * 
 * Every time delete the task, clear() method must be called.
 */
public class FotaPref {

    public static final String TAG = FotaPref.class.getSimpleName();

    public final static String POSTPONE_TIMES = "postponetimes";
    public final static String CLEAR_STATUS = "clearstatus";
    public final static String UPDATE_RESULT_RETRY_TIMES = "retrytimes";
    public final static int MAX_RETRY_TIMES = 10;

    Context mcontext;
    SharedPreferences mPref;
    SharedPreferences.Editor mEditor;
    private static Object mLock = new Object();

    private FotaPref(Context context) {
        this.mcontext = context;
        mPref = mcontext.getSharedPreferences(TAG, Context.MODE_PRIVATE);
    }

    private static FotaPref sInstance;

    /**
     * Singleton FotaPref
     *
     * @param context
     * @return
     */
    public static FotaPref getInstance(Context context) {
        if (sInstance == null) {
            synchronized (mLock) {
                if (sInstance == null) {
                    sInstance = new FotaPref(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    private SharedPreferences.Editor getEditor() {
        if (mEditor != null) {
            return mEditor;
        }
        SharedPreferences.Editor editor = mPref.edit();
        return editor;
    }

    public void setFloat(String name, float value) {
        getEditor().putFloat(name, value).apply();
    }

    public float getFloat(String name, float value) {
        return mPref.getFloat(name, value);
    }

    public void setLong(String name, long value) {
        getEditor().putLong(name, value).apply();
    }

    public void setInt(String name, int value) {
        getEditor().putInt(name, value).apply();
    }

    public void setString(String name, String value) {
        boolean commitFlag = false;
        commitFlag = getEditor().putString(name, value).commit();
        if (!commitFlag) {
            FotaLog.d(TAG, "commitFlag = " + commitFlag + ", name = " + name + ", value = " + value);
        }
    }

    public void setBoolean(String name, boolean value) {
        getEditor().putBoolean(name, value).apply();
    }

    public int getInt(String name, int defValue) {
        return mPref.getInt(name, defValue);
    }

    public long getLong(String name, long defValue) {
        return mPref.getLong(name, defValue);
    }

    public String getString(String name, String defValue) {
        return mPref.getString(name, defValue);
    }

    public boolean getBoolean(String name, boolean defValue) {
        return mPref.getBoolean(name, defValue);
    }

    /**
     * get saved postpone times
     *
     * @return
     */
    public int getPostponeTimes() {
        return getInt(POSTPONE_TIMES, 0);
    }

    /**
     * Save postpone times
     *
     * @param times
     */
    public void savePostponeTimes(int times) {
        setInt(POSTPONE_TIMES, times);
    }

    public int getUpdateResultTimes() {
        return getInt(UPDATE_RESULT_RETRY_TIMES, 0);
    }

    /**
     * Save update result times
     *
     * @param times
     */
    public void saveUpdateResultTimes(int times) {
        setInt(UPDATE_RESULT_RETRY_TIMES, times);
    }

    /**
     * Set clear status .
     *
     * @param value
     */
    public void setClearStatus(boolean value) {
        setBoolean(CLEAR_STATUS, value);
    }

    /**
     * Get clear status .
     */
    public boolean getClearStatus() {
        return getBoolean(CLEAR_STATUS,
                mcontext.getResources().getBoolean(R.bool.def_jrdfota_is_clear_status));
    }

    /**
     * Clear all saved informations about the task.
     */
    public void clear() {
        getEditor().clear().apply();
    }
}
