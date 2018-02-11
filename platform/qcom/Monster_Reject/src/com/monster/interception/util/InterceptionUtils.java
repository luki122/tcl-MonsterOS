package com.monster.interception.util;

import android.net.Uri;
import android.widget.Toast;

public class InterceptionUtils {

    public static final int SLIDER_BTN_POSITION_DELETE = 1;
    public static boolean isNoneDigit(String number) {
        boolean isDigit = false;
        for (int i = 0; i < number.length(); i++) {
            if (Character.isDigit(number.charAt(i))) {
                isDigit = true;
            }
        }
        if (number.indexOf('+', 1) > 0) {
            isDigit = false;
        }
        if (!isDigit) {
            return true;
        }
        return false;
    }
}