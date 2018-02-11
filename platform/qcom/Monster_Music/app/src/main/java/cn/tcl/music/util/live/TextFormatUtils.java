package cn.tcl.music.util.live;

import android.content.Context;
import android.icu.text.NumberFormat;

import java.util.Locale;

import cn.tcl.music.R;

public class TextFormatUtils {

    public static String formatPalyCount(Context context,int count) {
        if (Locale.getDefault().getCountry().contains("CN")) {
            final int pos = count / 10000;
            if (pos > 1) {
                return context.getResources().getString(R.string.live_songs_play_count, pos);
            }
        }
        return String.valueOf(count);
    }
}
