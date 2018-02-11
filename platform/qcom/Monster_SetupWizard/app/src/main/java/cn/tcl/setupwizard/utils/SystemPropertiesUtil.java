/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.setupwizard.utils;

import java.lang.reflect.Method;

/**
 * the util for system properties
 *
 */
public class SystemPropertiesUtil {
    private static Class<?> mClassType = null;
    private static Method mGetMethod = null;
    private static Method mGetBooleanMethod = null;
    private static Method mGetIntMethod = null;

    // String SystemProperties.get(String key)
    public static String get(String key) {
        init();

        String value = null;

        try {
            value = (String) mGetMethod.invoke(mClassType.newInstance(), key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }

    // int SystemProperties.get(String key, int def)
    public static int getInt(String key, int def) {
        init();

        int value = def;
        try {
            Integer v = (Integer) mGetIntMethod.invoke(mClassType.newInstance(), key, def);
            value = v.intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static boolean getBoolean(String key, boolean def) {
        init();

        boolean value = def;
        try {
            Boolean v = (Boolean) mGetBooleanMethod.invoke(mClassType.newInstance(), key, def);
            value = v.booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    private static void init() {
        try {
            if (mClassType == null) {
                mClassType = Class.forName("android.os.SystemProperties");
                mGetBooleanMethod = mClassType.getDeclaredMethod("getBoolean", String.class,
                        boolean.class);
                mGetMethod = mClassType.getDeclaredMethod("get", String.class);
                mGetIntMethod = mClassType.getDeclaredMethod("getInt", String.class, int.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
