
package com.android.camera.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import com.android.internal.util.XmlUtils;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class CustomUtil {
    private final String TAG = "CustomUtil";
    private String FILE = "/custpack/plf/TctCamera/isdm_TctCamera_defaults.xml";
    private final String SPKEY = "isdm_TctCamera_defaults";
    private Context mContext = null;
    private SharedPreferences mSharedPreferences = null;
    private String mPackageName = null;
    /**
     * tag please check the file /res/values/isdm_TctCamera_defaults.xml
     */
    private final String START_TAG = "resources";
    private final String STRING = "string";
    private final String INTEGER = "integer";
    private final String BOOL = "bool";
    private final String HAS_PARSERED = "has_parsered";
    //private final String APK_VERSION_CODE = "apk_version_code";
    private final String SYSTEM_VERSION = "system_version";
    //  [JrdLauncher][APK DEV] Custom wallpaper in JrdLauncher
    public static final String CUST_PACKAGE_NAME = "com.android.tctcamera.resource";

    private static CustomUtil sCustomUtil = null;

    /**
     * signal instance
     * 
     * @param context
     */
    private CustomUtil(Context context) {
        mContext = context;
        mPackageName = mContext.getPackageName();
        mSharedPreferences = context.getSharedPreferences(SPKEY, Context.MODE_PRIVATE);
    }

    /**
     * single instance.
     * @param context
     * @return
     */
    public static CustomUtil getInstance(Context context) {
        if (sCustomUtil == null) {
            sCustomUtil = new CustomUtil(context);
        }
        return sCustomUtil;
    }

    /**
     * single instance.
     * @param context
     * @return
     */
    public static CustomUtil getInstance() {
        return sCustomUtil;
    }

    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    /*
     * public void release () { mSharedPreferences = null; mContext = null;
     * sISDMParserUtil = null; }
     */

    /**
     * Check the file exist or not.
     * 
     * @return
     */
    private boolean systemCustFileExist() {
        File file = new File(FILE);
        Log.i("Custom","File = " + file.exists());
        return file.exists();
    }

    /**
     * anyone of them satisfied need parser the System file. if
     * /custpack/plf/JrdLauncher/isdm_JrdLauncher_defaults.xml do not parser. if
     * apk upgrade need parser again. if system upgrade need parser again.
     * 
     * @return
     */
    private boolean needParserFile() {
        String systemVersion = Build.FINGERPRINT;
        // never parsered before need parser or upgrade need parser.
        return !mSharedPreferences.getBoolean(HAS_PARSERED, false)
                || (!systemVersion.equals(mSharedPreferences.getString(SYSTEM_VERSION, "default")));
    }

    /**
     * Parser file
     * 
     * @return
     */
    private HashMap<String, String> parserFile() {
        InputStream inputStream = null;
        try {
            File file = new File(FILE);
            inputStream = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(inputStream, "utf-8");
            XmlUtils.beginDocument(parser, START_TAG);
            final int depth = parser.getDepth();
            int type;
            HashMap<String, String> hm = new HashMap<String, String>(10);
            while (((type = parser.next()) != XmlPullParser.END_TAG || parser
                    .getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {
                if (type != XmlPullParser.START_TAG) {
                    continue;
                }
                // String dataType = parser.getName();
                String key = parser.getAttributeValue(null, "name");
                String value = parser.nextText();
                hm.put(key, value);
            }
            return hm;
        } catch (Exception e) {
            Log.e(TAG, "error! when parser " + FILE);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    /**
     * save all of the value into SharedPreference.
     * 
     * @param hm
     */
    private void saveInSharedPreference(HashMap<String, String> hm) {
        if (mSharedPreferences == null || hm == null) {
            Log.d(TAG, "Maybe HashMap is null. has parsered before");
            return;
        }
        Editor e = mSharedPreferences.edit();
        for (String str : hm.keySet()) {
            e.putString(str, hm.get(str));
        }
        e.putBoolean(HAS_PARSERED, true);
        e.putString(SYSTEM_VERSION, Build.FINGERPRINT);
        e.apply();
        e.commit();
    }

    /**
     * this method only for engineer debug. when debug please change private to
     * public and call this method
     */
    private void testForEngineer() {
        Log.d(TAG, " get all :" + mSharedPreferences.getAll().toString());
    }
    
    public static void TraceLog(String msg){
    	Log.w("CustomUtil", msg);
    }

    public void setCustomFromSystem() {
        if (systemCustFileExist() && needParserFile()) {
            Log.d(TAG, "start to parser file");
            HashMap<String, String> hm = parserFile();
            saveInSharedPreference(hm);
        }
    }

    public int getInt(String key, int defaultValue) {
        if (mSharedPreferences.contains(key)) {
            String value = mSharedPreferences.getString(key, defaultValue+"");
            try {
                return Integer.parseInt(value);
            } catch (Exception e) {
            }
        } else {
            Resources res = mContext.getResources();
            int id = res.getIdentifier(key, INTEGER, mPackageName);
            if (id > 0) {
                return res.getInteger(id);
            }
        }
        TraceLog("key = "+key+" defaultValue="+defaultValue);
        return defaultValue;
    }

    public String getString(String key, String defaultValue) {
        if (mSharedPreferences.contains(key)) {
            return mSharedPreferences.getString(key, defaultValue);
        } else {
            Resources res = mContext.getResources();
            int id = res.getIdentifier(key, STRING, mPackageName);
            if (id > 0) {
                return res.getString(id);
            }
        }
        TraceLog("key = "+key+" defaultValue="+defaultValue);
        return defaultValue;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        if (mSharedPreferences.contains(key)) {
            String value = mSharedPreferences.getString(key, "");
            try {
                return Boolean.parseBoolean(value);
            } catch (Exception e) {
            }
        } else {
            Resources res = mContext.getResources();
            int id = res.getIdentifier(key, BOOL, mPackageName);
            if (id > 0) {
                return res.getBoolean(id);
            }
        }
        TraceLog("key = "+key+" defaultValue="+defaultValue);
        return defaultValue;
    }

}
