/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.utils;

import android.util.Log;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-2.
 * <p>
 * this class is used for print debug log
 */
public class Debugger {
    private final static String TAG = "Debugger";
    private final static boolean IS_DEBUG = true;//if is true, method DEBUG_D will work, otherwise it will not work

    /**
     * print the debug messages
     *
     * @param isDebug // if you need print this debug messages
     * @param tag
     * @param strs    // the message you want to print through debugger
     */
    public final static void DEBUG_D(boolean isDebug, String tag, String... strs) {
        if (IS_DEBUG && isDebug && strs.length > 0) {
            String log = "";
            if (strs.length <= 1) {
                log = strs[0];
            } else {
                // this is better than "str1" + "str2" + ....+"str n"
                StringBuilder builder = new StringBuilder();
                for (String str : strs) {
                    builder.append(str);
                }
                log = builder.toString();
            }
            Log.d(tag, log);
        }
    }
}
