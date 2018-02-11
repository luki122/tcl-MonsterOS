package cn.tcl.music.util;

public class LogUtil {
    public static boolean mIsShow = true;

    public static void v(String tag, String msg) {
        if (mIsShow) {
            StackTraceElement ste = new Throwable().getStackTrace()[1];
            String traceInfo = ste.getClassName() + "::";
            traceInfo += ste.getMethodName();
            traceInfo += "@" + ste.getLineNumber() + ">>>";
            android.util.Log.v(tag, traceInfo + msg);
        }
    }

    public static void v(String tag, String msg, Throwable tr) {
        if (mIsShow) {
            android.util.Log.v(tag, msg, tr);
        }
    }


    public static void d(String tag, String msg) {
        if (mIsShow) {
            StackTraceElement ste = new Throwable().getStackTrace()[1];
            String traceInfo = ste.getClassName() + "::";
            traceInfo += ste.getMethodName();
            traceInfo += "@" + ste.getLineNumber() + ">>>";
            android.util.Log.d(tag, traceInfo + msg);
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (mIsShow) {
            android.util.Log.d(tag, msg, tr);
        }
    }

    public static void i(String tag, String msg) {
        if (mIsShow) {
            StackTraceElement ste = new Throwable().getStackTrace()[1];
            String traceInfo = ste.getClassName() + "::";
            traceInfo += ste.getMethodName();
            traceInfo += "@" + ste.getLineNumber() + ">>>";
            android.util.Log.i(tag, traceInfo + msg);
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (mIsShow) {
            android.util.Log.i(tag, msg, tr);
        }
    }

    public static void e(String tag, String msg) {
        if (mIsShow) {
            StackTraceElement ste = new Throwable().getStackTrace()[1];
            String traceInfo = ste.getClassName() + "::";
            traceInfo += ste.getMethodName();
            traceInfo += "@" + ste.getLineNumber() + ">>>";
            android.util.Log.e(tag, traceInfo + msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (mIsShow) {
            android.util.Log.e(tag, msg, tr);
        }
    }
}
