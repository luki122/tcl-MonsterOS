package com.monster.launcher;

/**
 * Created by antino on 16-6-18.
 */
public class Log {
    private static boolean DEBUG_ALL = true;
    private static boolean DEBUG_V = true;
    private static boolean DEBUG_I = true;
    private static boolean DEBUG_D = true;
    private static boolean DEBUG_W = true;
    private static boolean DEBUG_E = true;

    public static boolean DEBUG_UNREAD = true;//lijun add for unread

    /**
     * Priority constant for the println method; use Log.v.
     */
    public static final int VERBOSE = 2;

    /**
     * Priority constant for the println method; use Log.d.
     */
    public static final int DEBUG = 3;

    /**
     * Priority constant for the println method; use Log.i.
     */
    public static final int INFO = 4;

    /**
     * Priority constant for the println method; use Log.w.
     */
    public static final int WARN = 5;

    /**
     * Priority constant for the println method; use Log.e.
     */
    public static final int ERROR = 6;

    /**
     * Priority constant for the println method.
     */
    public static final int ASSERT = 7;

    private Log() {
    }

    public static int v(String tag, String msg) {
        return ((DEBUG_ALL || DEBUG_V) ? android.util.Log.v(tag, msg) : -1);
    }

    public static int v(String tag, String msg, Throwable tr) {
        return ((DEBUG_ALL || DEBUG_V) ? android.util.Log.v(tag, msg, tr) : -1);
    }

    public static int i(String tag, String msg) {
        return ((DEBUG_ALL || DEBUG_I) ? android.util.Log.i(tag, msg) : -1);
    }

    public static int i(String tag, String msg, Throwable tr) {
        return ((DEBUG_ALL || DEBUG_I) ? android.util.Log.i(tag, msg, tr) : -1);
    }

    public static int d(String tag, String msg) {
        return ((DEBUG_ALL || DEBUG_D) ? android.util.Log.d(tag, msg) : -1);
    }

    public static int d(String tag, String msg, Throwable tr) {
        return ((DEBUG_ALL || DEBUG_D) ? android.util.Log.d(tag, msg, tr) : -1);
    }

    public static int w(String tag, String msg) {
        return ((DEBUG_ALL || DEBUG_W) ? android.util.Log.w(tag, msg) : -1);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return ((DEBUG_ALL || DEBUG_W) ? android.util.Log.w(tag, msg, tr) : -1);
    }

    public static int w(String msg, Throwable tr) {
        return ((DEBUG_ALL || DEBUG_I) ? android.util.Log.w( msg, tr) : -1);
    }

    public static int e(String tag, String msg) {
        return ((DEBUG_ALL || DEBUG_E) ? android.util.Log.e(tag, msg) : -1);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return ((DEBUG_ALL || DEBUG_E) ? android.util.Log.e(tag, msg, tr) : -1);
    }

    public static void  wtf(String tag,String msg){
        android.util.Log.wtf(tag,msg);
    }

    public static boolean isLoggable(String tag,int priority){
        return android.util.Log.isLoggable(tag,priority);
    }


}
