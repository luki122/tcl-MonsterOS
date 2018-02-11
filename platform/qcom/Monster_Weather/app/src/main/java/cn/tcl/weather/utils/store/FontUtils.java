package cn.tcl.weather.utils.store;

import android.graphics.Typeface;

/**
 * Created on 16-10-28.
 */
public class FontUtils {

    public final static String FONT_FAMILY_NORMAL = "monster-normal";
    public final static String FONT_FAMILY_MEDIUM = "monster-medium";
    public final static String FONT_FAMILY_THIN = "monster-thin";

    public final static Typeface TEXT_TYPEFACE_NORMAL;//font size <= 24sp
    public final static Typeface TEXT_TYPEFACE_MEDIUM;//24sp <= font size < 33sp
    public final static Typeface TEXT_TYPEFACE_THIN;//font size>=33sp

    static {
        TEXT_TYPEFACE_NORMAL = Typeface.create(FONT_FAMILY_NORMAL, Typeface.NORMAL);
        TEXT_TYPEFACE_MEDIUM = Typeface.create(FONT_FAMILY_MEDIUM, Typeface.NORMAL);
        TEXT_TYPEFACE_THIN = Typeface.create(FONT_FAMILY_THIN, Typeface.NORMAL);
    }


    public static Typeface getTxtTypeface(float sp) {
        if (sp <= 24) {//font size <= 24sp
            return TEXT_TYPEFACE_NORMAL;
        } else if (sp < 33) {//24sp < font size < 33sp
            return TEXT_TYPEFACE_MEDIUM;
        }
        return TEXT_TYPEFACE_THIN;
    }
}
