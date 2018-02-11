package com.android.deskclock.timer;

public class MyTime {

    public int hour;
    public int min;
    public int sec;
    public int hundred;

    public void setTime(long time, boolean showHundredths, boolean update) {

        boolean neg = false;
        if (time < 0) {
//            time = -time;
//            neg = true;
            hour = 0;
            min = 0;
            sec = 0;
            hundred = 0;
            return;
        }
        long hundreds, seconds, minutes, hours;
        seconds = time / 1000;
        hundreds = (time - seconds * 1000) / 10;
        minutes = seconds / 60;
        seconds = seconds - minutes * 60;
        hours = minutes / 60;
        minutes = minutes - hours * 60;
        if (hours > 999) {
            hours = 0;
        }
        if (!showHundredths) {
            if (!neg && hundreds != 0) {
                seconds++;
                if (seconds == 60) {
                    seconds = 0;
                    minutes++;
                    if (minutes == 60) {
                        minutes = 0;
                        hours++;
                    }
                }
            }
        }
        hour = (int) hours;
        min = (int) minutes;
        sec = (int) seconds;
        hundred = (int) hundreds;
    }
}
