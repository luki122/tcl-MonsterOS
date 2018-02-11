package cn.tcl.music.util;

import mst.app.dialog.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.text.Selection;
import android.text.Spannable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import cn.tcl.music.R;

public final class Utils {

    public static String SETTING_FIRST_LAUNCH = "first_launch";
    public static String MUSIC_STATUS_KEY = "music_status";
    public static String MIX_PREFS_NAME = "mix_coverPref";

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean hasLollipop() {

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean hasJellyBeanMr2() {

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2; // API 18
    }

    public static boolean hasJellyBeanMr1() {

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1; // API 18
    }

    public static String convertTimeToPresentableString(long timeInMs, boolean needZeroForMinutes) {
        if (timeInMs < 0) return "";
        int durationInSecs = ((int) timeInMs / 1000) % 60;
        int durationInMin = (((int) timeInMs / 1000) / 60) % 60;
        int durationInHours = ((int) timeInMs / 1000) / 3600;
        String ret = "";
        if (durationInHours > 0)
            ret += durationInHours + ":";

        if (durationInMin < 10 && needZeroForMinutes)
            ret += "0";

        ret += durationInMin + ":";

        if (durationInSecs < 10)
            ret += "0";

        ret += durationInSecs;

        return ret;
    }

    public static void invertLeftToRightLayout(View v) {
        final ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        int oldAnchor = mlp.leftMargin;
        mlp.leftMargin = mlp.rightMargin;
        mlp.rightMargin = oldAnchor;
        if (mlp instanceof RelativeLayout.LayoutParams) {
            final RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mlp;
            final int alignParentRight = lp.getRules()[RelativeLayout.ALIGN_PARENT_RIGHT];
            final int alignParentLeft = lp.getRules()[RelativeLayout.ALIGN_PARENT_LEFT];
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, alignParentRight);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, alignParentLeft);
            oldAnchor = lp.getRules()[RelativeLayout.RIGHT_OF];
            lp.addRule(RelativeLayout.RIGHT_OF, lp.getRules()[RelativeLayout.LEFT_OF]);
            lp.addRule(RelativeLayout.LEFT_OF, oldAnchor);
            oldAnchor = lp.getRules()[RelativeLayout.ALIGN_LEFT];
            lp.addRule(RelativeLayout.ALIGN_LEFT, lp.getRules()[RelativeLayout.ALIGN_RIGHT]);
            lp.addRule(RelativeLayout.ALIGN_RIGHT, oldAnchor);
        }
    }

    public static void setImageViewEnabled(ImageView imageView, boolean enabled) {
        if (enabled) {
            imageView.setEnabled(true);
            imageView.clearColorFilter();
        } else {
            imageView.setEnabled(false);
            imageView.setColorFilter(0xAA000000);
        }
    }

    /**
     * Convert Dp to Pixel
     */
    public static int dpToPx(float dp, Resources resources) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return (int) px;
    }

    public static int getRelativeTop(View myView) {
        if (myView.getId() == android.R.id.content) {
            return myView.getTop();
        } else {
            return myView.getTop() + getRelativeTop((View) myView.getParent());
        }
    }

    public static int getRelativeLeft(View myView) {
        if (myView.getId() == android.R.id.content) {
            return myView.getLeft();
        } else {
            return myView.getLeft() + getRelativeLeft((View) myView.getParent());
        }
    }

    public static final void showMessage(String title, String message, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        builder.create().show();
    }


    public static boolean isModeLite(Context mContext) {
        boolean result = false;
        Resources res = mContext.getResources();
        int id = res.getIdentifier("self_music_mode", "string", mContext.getPackageName());

        String appMode = "";
        if (id != 0)
            appMode = mContext.getResources().getString(id);
        if (appMode != null && "MODE_LITE".equalsIgnoreCase(appMode)) {
            result = true;
        }
        return result;
    }

    public static void setEditTextCursorLocation(EditText editText) {
        if (null == editText) {
            return;
        }
        CharSequence text = editText.getText();
        if (text instanceof Spannable) {
            Spannable spanText = (Spannable) text;
            Selection.setSelection(spanText, text.length());
        }
    }

    public static void setCoverPref(Context context, boolean isPlaying) {
        SharedPreferences coverPref = context.getSharedPreferences(MIX_PREFS_NAME, Context.MODE_WORLD_READABLE);
        SharedPreferences.Editor ed = coverPref.edit();
        ed.putBoolean(MUSIC_STATUS_KEY, isPlaying);
        ed.commit();
    }

    public static boolean isFromAddAndPlayNow = false;

    public static boolean needLoadTrack() {
        if (isFromAddAndPlayNow) {
            isFromAddAndPlayNow = false;
            return false;
        }
        return true;
    }

    public static boolean isSDK23() {
        return Integer.valueOf(android.os.Build.VERSION.SDK) >= 23;
    }

    public static boolean isSDK24() {
        return Integer.valueOf(Build.VERSION.SDK_INT) >= 24;
    }
}
