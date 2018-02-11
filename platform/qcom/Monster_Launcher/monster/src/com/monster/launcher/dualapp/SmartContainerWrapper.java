package com.monster.launcher.dualapp;

/**
 * for dual app
 * Created by antino on 16-10-12.
 */

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.app.Activity;

import com.monster.launcher.compat.UserHandleCompat;

public class SmartContainerWrapper {
    private static String TAG = "SmartContainerWrapper";

    public static boolean WITH_OUT_ALL;
    public static boolean WITH_OUT_VIRTUAL_BOX;
    public static boolean WITH_OUT_CROSS_BOX_SHARE;
    public static boolean WITH_OUT_APP_CLONE;
    public static int CI_FLAG_VIRTUAL_BOX;
    public static String ACTION_CONTAINER_CREATED;

    private static Object manager;
    private static Class<?> managerClass;

    static {
        init();
    }

    public static void init() {
        WITH_OUT_ALL = getConfig("WITH_OUT_ALL", true);
        WITH_OUT_VIRTUAL_BOX = getConfig("WITH_OUT_VIRTUAL_BOX", true);
        WITH_OUT_APP_CLONE = getConfig("WITH_OUT_APP_CLONE", true);
        WITH_OUT_CROSS_BOX_SHARE = getConfig("WITH_OUT_CROSS_BOX_SHARE", true);
        CI_FLAG_VIRTUAL_BOX = getIntField("com.cmx.cmplus.ContainerInfo", "CI_FLAG_VIRTUAL_BOX", 0);
        ACTION_CONTAINER_CREATED = getStringField("ACTION_CONTAINER_CREATED", "com.cmx.cmplus.ACTION_CONTAINER_CREATED");

        try {
            manager = invoke("com.cmx.cmplus.SmartContainerManagerNative", "get", null);
            managerClass = Class.forName("com.cmx.cmplus.ISmartContainerManager");
        } catch (Exception e) {
            Log.e(TAG, "Failed to get manager class\n" + e);
            e.printStackTrace();
        }

        Log.d(TAG, "WITH_OUT_ALL " + WITH_OUT_ALL);
        Log.d(TAG, "WITH_OUT_VIRTUAL_BOX " + WITH_OUT_VIRTUAL_BOX);
        Log.d(TAG, "WITH_OUT_APP_CLONE " + WITH_OUT_APP_CLONE);
        Log.d(TAG, "WITH_OUT_CROSS_BOX_SHARE " + WITH_OUT_CROSS_BOX_SHARE);
        Log.d(TAG, "CI_FLAG_VIRTUAL_BOX " + CI_FLAG_VIRTUAL_BOX);
        Log.d(TAG, "ACTION_CONTAINER_CREATED " + ACTION_CONTAINER_CREATED);
    }

    static private Object getField(String className, String fieldName) {
        try {
            Class<?> configClass = Class.forName(className);
            Field field = configClass.getField(fieldName);
            field.setAccessible(true);
            return field.get(null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get " + className + "." + fieldName + "\n" + e);
            e.printStackTrace();
        }
        return null;
    }

    static private String getStringField(String name, String def_val) {
        Object config = getField("com.cmx.cmplus.SmartContainerConfig", name);
        if (config != null)
            return (String) config;
        return def_val;
    }

    static private boolean getConfig(String name, boolean def_val) {
        Object config = getField("com.cmx.cmplus.SmartContainerConfig", name);
        if (config != null)
            return (Boolean) config;
        return def_val;
    }

    static private int getIntField(String className, String fieldName, int def_val) {
        Object field = getField(className, fieldName);
        if (field != null)
            return (Integer) field;
        return def_val;
    }

    static private Method getMethod(Class<?> clazz, String methodName, Object... args) {
        try {
            Method method;
            if (args.length == 0)
                method = clazz.getMethod(methodName);
            else {
                Class<?>[] param = new Class<?>[args.length];
                for (int i = 0; i < args.length; i++) {
                    param[i] = args[i].getClass();
                }
                method = clazz.getMethod(methodName, param);
            }
            return method;
        } catch (Exception e) {
            Log.e(TAG, "Failed to get method " + clazz.getName() + "." + methodName + "()\n" + e);
            e.printStackTrace();
        }

        return null;
    }

    static private Object invoke(String className, String methodName, Object obj, Object... args) {
        try {
            Class<?> clazz = Class.forName(className);
            Method method = getMethod(clazz, methodName, args);
            method.setAccessible(true);
            return method.invoke(obj, args);
        } catch (Exception e) {
            Log.e(TAG, "Failed to invoke " + className + "." + methodName + "()\n" + e);
            e.printStackTrace();
        }

        return null;
    }

    static public Object invoke(String methodName, Object... args) {
        try {
            Method method = getMethod(managerClass, methodName, args);
            method.setAccessible(true);
            return method.invoke(manager, args);
        } catch (Exception e) {
            Log.e(TAG, "Failed to invoke manager " + methodName + "()\n" + e);
            e.printStackTrace();
        }

        return null;
    }

    //=======================================================================================
    //launcher specific functions
    static public UserHandleCompat getUserHandleCompat(Intent data, UserHandleCompat user_def) {
        UserHandleCompat user = user_def;
        Bundle cloneExtras = null;
        final String EXTRA_CLONE_CALLINGUID_KEY = "extra-clone-callinguid";
        {
            Class cls = data.getClass();
            try {
                Field fd = cls.getDeclaredField("mCloneExtras");
                if (fd != null) {
                    fd.setAccessible(true);
                    cloneExtras = (Bundle) fd.get(data);
                }
            } catch (NoSuchFieldException e) {
                Log.e(TAG, "No valid mCloneExtra found in intent. " + e);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse mCloneExtra in intent. " + e);
            }
        }
        if (cloneExtras != null) {
            int callingUid = cloneExtras.getInt(EXTRA_CLONE_CALLINGUID_KEY, -1);
            if (callingUid > 0) {
                //handle getUserId function
                try {
                    //get userid from callingUid
                    Class<UserHandle> c = UserHandle.class;
                    Method method = c.getMethod("getUserId", int.class);
                    method.setAccessible(true);
                    int userid = (int) method.invoke(null, callingUid);

                    //generate UserHandleCompat
                    Constructor ctor = UserHandle.class.getConstructor(int.class);
                    ctor.setAccessible(true);
                    UserHandle userhandle = (UserHandle) ctor.newInstance(userid);
                    user = UserHandleCompat.fromUser(userhandle);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to generate UserHandleCompat. " + e);
                }
            }
        }
        return user;
    }

    public static void startActivityAsUser(Object obj, Intent intent, Bundle options, UserHandle user) {
        Log.d(TAG, "startActivityAsUser E");
        try {
            Class<Activity> c = Activity.class;
            Method method = c.getMethod("startActivityAsUser", Intent.class, Bundle.class, UserHandle.class);
            method.setAccessible(true);
            method.invoke(obj, intent, options, user);
        } catch (Exception e) {
            Log.e(TAG, "Failed to call startActivityAsUser. " + e);
        }
    }
}

