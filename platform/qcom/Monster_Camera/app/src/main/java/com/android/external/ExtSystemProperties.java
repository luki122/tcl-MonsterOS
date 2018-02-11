package com.android.external;

import java.lang.reflect.Method;

public class ExtSystemProperties {
    private static Class<?> mClassType = null;
    private static Method mGetMethod = null;
    private static Method mGetIntMethod = null;
    private static Method mGetBooleanMethod = null;

    private static void init() {
        try {
            if (mClassType == null) {
                mClassType = Class.forName("android.os.SystemProperties");

                mGetMethod = mClassType.getDeclaredMethod("get", String.class);
                mGetIntMethod = mClassType.getDeclaredMethod("getInt", String.class, int.class);
                mGetBooleanMethod = mClassType.getDeclaredMethod("getBoolean", String.class, boolean.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // String SystemProperties.get(String key){}
    public static String get(String key) {
        init();

        String value = null;

        try {
            value = (String) mGetMethod.invoke(mClassType, key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }

    //int SystemProperties.get(String key, int def){}
    public static int getInt(String key, int def) {
        init();
        
        int value = def;
        try {
            Integer v = (Integer) mGetIntMethod.invoke(mClassType, key, def);
            value = v.intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    //boolean SystemProperties.getBoolean(String key, boolean def){}
    public static boolean getBoolean(String key) {
        init();
        
        boolean value = false;
        try {
            Boolean v = (Boolean) mGetBooleanMethod.invoke(mClassType, key, value);
            value = v.booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }
}