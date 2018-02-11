/******************************************************************************/
/*                                                               Date:10/2013 */
/*                                PRESENTATION                                */
/*                                                                            */
/*       Copyright 2013 TCL Communication Technology Holdings Limited.        */
/*                                                                            */
/* This material is company confidential, cannot be reproduced in any form    */
/* without the written permission of TCL Communication Technology Holdings    */
/* Limited.                                                                   */
/*                                                                            */
/* -------------------------------------------------------------------------- */
/*  Author :  Fan.Hu                                                          */
/*  Email  :   fan.hu@tct.com                                                 */
/*  Role   :                                                                  */
/*  Reference documents :                                                     */
/* -------------------------------------------------------------------------- */
/*  Comments :                                                                */
/*  File     :                                                                */
/*  Labels   :                                                                */
/* -------------------------------------------------------------------------- */
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/*    date   |        author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 12/05/2013|        Fan.Hu        |                      |Creation          */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

package com.tct.libs.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;

/**
 * According to the new coding rule, we must use TctLog instead of android
 * default one. This class is created to auto adapter between different
 * platforms, so that the application can both run on platform with TctLog and
 * one without TctLog
 */
public class TLog {
    public static final String TAG = "TLog";
    public static final int VERSION = 2;

    // Log level
    private static final int LEVEL_D = 0;
    private static final int LEVEL_I = 1;
    private static final int LEVEL_W = 2;
    private static final int LEVEL_E = 3;
    private static final int LEVEL_V = 4;

    // Native log types
    public static final int TYPE_DEFAULT = -1;
    public static final int TYPE_TCTLOG = 0;
    public static final int TYPE_XLOG = 1;

    // Native log class
    private static final String[] NATIVE_CLASS = {
            "android.util.TctLog",      // TctLog
            "com.mediatek.xlog.Xlog"    // XLog
    };

    // Native log available?
    private static int mAvailableType = TYPE_DEFAULT;

    // Flag for initializing
    private static boolean mNotInitialized = true;

    // Native class, method, args
    private static Class<?> mNativeClass = null;
    private static Method[] mNativeMethod = new Method[5];
    private static Object[] mNativeArgs = new Object[2];

    private TLog() {
    }

    /**
     * Get current working log type
     * @return type
     * @see #TYPE_DEFAULT
     * @see #TYPE_TCTLOG
     * @see #TYPE_XLOG
     */
    public static int getCurrentLogType() {
        init();

        return mAvailableType;
    }

    /**
     * Only create class & method once, so that it can run faster.<br>
     * If we obtain the method every time, it will cost almost 4 times duration
     * than directly call TctLog. With this solution, only spend less than
     * another half time.
     */
    private static synchronized void init() {
        if (mNotInitialized) {
            mNotInitialized = false;

            for (int i = 0; i < NATIVE_CLASS.length; i++) {
                if (i == TYPE_TCTLOG) {
                    initTctLog();
                } else {
                    init(i);
                }

                if (mAvailableType != TYPE_DEFAULT) {
                    break;
                }
            }
        }
    }

    /**
     * initialize for TctLog
     */
    private static void initTctLog() {
        try {
            mNativeClass = Class.forName(NATIVE_CLASS[TYPE_TCTLOG]);
            mNativeMethod[LEVEL_D] = mNativeClass.getMethod("d", new Class<?>[] {
                    String.class, String.class
            });
            mNativeMethod[LEVEL_I] = mNativeClass.getMethod("i", new Class<?>[] {
                    String.class, String.class
            });
            mNativeMethod[LEVEL_W] = mNativeClass.getMethod("w", new Class<?>[] {
                    String.class, String.class
            });
            mNativeMethod[LEVEL_E] = mNativeClass.getMethod("e", new Class<?>[] {
                    String.class, String.class
            });
            mNativeMethod[LEVEL_V] = mNativeMethod[LEVEL_I]; // no v level on
            // TctLog, use i level
            // instead
        } catch (NoSuchMethodException e) {
        } catch (ClassNotFoundException e) {
        } catch (NullPointerException e) {
        } finally {
            // The class is now useless, so release it.
            if (mNativeClass != null) {
                mAvailableType = TYPE_TCTLOG;
                mNativeClass = null;
            }
        }
    }

    /**
     * Default implement of initializing according to the type
     */
    private static void init(int type) {
        try {
            mNativeClass = Class.forName(NATIVE_CLASS[type]);
            mNativeMethod[LEVEL_D] = mNativeClass.getMethod("d", new Class<?>[] {
                    String.class, String.class
            });
            mNativeMethod[LEVEL_I] = mNativeClass.getMethod("i", new Class<?>[] {
                    String.class, String.class
            });
            mNativeMethod[LEVEL_W] = mNativeClass.getMethod("w", new Class<?>[] {
                    String.class, String.class
            });
            mNativeMethod[LEVEL_E] = mNativeClass.getMethod("e", new Class<?>[] {
                    String.class, String.class
            });
            mNativeMethod[LEVEL_V] = mNativeClass.getMethod("v", new Class<?>[] {
                    String.class, String.class
            });
        } catch (NoSuchMethodException e) {
        } catch (ClassNotFoundException e) {
        } catch (NullPointerException e) {
        } finally {
            // The class is now useless, so release it.
            if (mNativeClass != null) {
                mAvailableType = type;
                mNativeClass = null;
            }
        }
    }

    /**
     * Invoke native log method (i.e. TctLog, XLog)
     * @param level log level
     * @param tag tag
     * @param msg message
     * @return -1: TctLog not supported
     * @see #LOG_D
     * @see #LOG_I
     * @see #LOG_W
     * @see #LOG_E
     */
    private static int Invoke_NativeLog(int level, String tag, String msg) {
        init();

        if (mAvailableType == TYPE_DEFAULT) {
            return -1;
        }

        try {
            synchronized (mNativeArgs) {
                mNativeArgs[0] = tag;
                mNativeArgs[1] = msg;
                return (Integer) mNativeMethod[level].invoke(null, mNativeArgs);
            }
        } catch (IllegalArgumentException e) {
            return -1;
        } catch (IllegalAccessException e) {
            return -1;
        } catch (InvocationTargetException e) {
            return -1;
        }
    }

    /**
     * Log with debug level
     * @param tag
     * @param msg
     * @return
     */
    public static int d(String tag, String msg) {
        int result = Invoke_NativeLog(LEVEL_D, tag, msg);
        if (result == -1) {
            // use default
            return android.util.Log.d(tag, msg);
        } else {
            return result;
        }
    }

    /**
     * Log with debug level
     * @param tag
     * @param msg
     * @param tr An exception to log
     * @return
     */
    public static int d(String tag, String msg, Throwable tr) {
        return d(tag, msg + '\n' + getStackTraceString(tr));
    }

    /**
     * Log with information level
     * @param tag
     * @param msg
     * @return
     */
    public static int i(String tag, String msg) {
        int result = Invoke_NativeLog(LEVEL_I, tag, msg);
        if (result == -1) {
            // use default
            return android.util.Log.i(tag, msg);
        } else {
            return result;
        }
    }

    /**
     * Log with information level
     * @param tag
     * @param msg
     * @param tr An exception to log
     * @return
     */
    public static int i(String tag, String msg, Throwable tr) {
        return i(tag, msg + '\n' + getStackTraceString(tr));
    }

    /**
     * Log with warning level
     * @param tag
     * @param msg
     * @return
     */
    public static int w(String tag, String msg) {
        int result = Invoke_NativeLog(LEVEL_W, tag, msg);
        if (result == -1) {
            // use default
            return android.util.Log.w(tag, msg);
        } else {
            return result;
        }
    }

    /**
     * Log with warning level
     * @param tag
     * @param msg
     * @param tr An exception to log
     * @return
     */
    public static int w(String tag, String msg, Throwable tr) {
        return w(tag, msg + '\n' + getStackTraceString(tr));
    }

    /**
     * Log with error level
     * @param tag
     * @param msg
     * @return
     */
    public static int e(String tag, String msg) {
        int result = Invoke_NativeLog(LEVEL_E, tag, msg);
        if (result == -1) {
            // use default
            return android.util.Log.e(tag, msg);
        } else {
            return result;
        }
    }

    /**
     * Log with error level
     * @param tag
     * @param msg
     * @param tr An exception to log
     * @return
     */
    public static int e(String tag, String msg, Throwable tr) {
        return e(tag, msg + '\n' + getStackTraceString(tr));
    }

    /**
     * Log with v level
     * @param tag
     * @param msg
     * @return
     */
    public static int v(String tag, String msg) {
        int result = Invoke_NativeLog(LEVEL_V, tag, msg);
        if (result == -1) {
            // use default
            return android.util.Log.v(tag, msg);
        } else {
            return result;
        }
    }

    /**
     * Log with v level
     * @param tag
     * @param msg
     * @param tr An exception to log
     * @return
     */
    public static int v(String tag, String msg, Throwable tr) {
        return v(tag, msg + '\n' + getStackTraceString(tr));
    }

    /**
     * Handy function to get a loggable stack trace from a Throwable
     * @param tr An exception to log
     */
    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }

        // This is to reduce the amount of log spew that apps do in the
        // non-error condition of the network being unavailable.
        Throwable t = tr;
        while (t != null) {
            if (t instanceof UnknownHostException) {
                return "";
            }
            t = t.getCause();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        return sw.toString();
    }
}
