/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {
    /**
     * format ms to hhmmss
     *
     * @param time
     * @return
     */
    public static String formatTime(long time) {
        time = time / 1000;
        int sec = (int) time % 60;
        int hours = (int) time / (60 * 60);
        int min = (int) (time - hours * 60 * 60) / 60;
        String formatTime = "" + hours + ":" + getTwoLength(min) + ":" + getTwoLength(sec);
        return formatTime;
    }

    private static String getTwoLength(int data) {
        if (data < 10) {
            return "0" + data;
        } else {
            return "" + data;
        }
    }

    public static String formatCurrentTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        return simpleDateFormat.format(new Date());
    }
}
