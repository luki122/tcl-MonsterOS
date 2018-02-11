package cn.tcl.weather.utils;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

/**
 * Created by thundersoft on 16-9-7.
 */
public class ToastUtils {
    public static final int LENGTH_SHORT = android.widget.Toast.LENGTH_SHORT;
    public static final int LENGTH_LONG = android.widget.Toast.LENGTH_LONG;

    private static Toast toast;
    private static Handler handler = new Handler();

    private static Runnable run = new Runnable() {
        public void run() {
            toast.cancel();
        }
    };

    private static void toast(Context ctx, CharSequence msg, int duration) {
        handler.removeCallbacks(run);
        switch (duration) {
            case LENGTH_SHORT:
                duration = 1000;
                break;
            case LENGTH_LONG:
                duration = 3000;
                break;
            default:
                break;
        }
        if (null != toast) {
            toast.setText(msg);
        } else {
            toast = Toast.makeText(ctx, msg, duration);
        }
        handler.postDelayed(run, duration);
        toast.show();
    }

    /**
     * @param ctx
     * @param msg
     * @param duration
     */
    public static void show(Context ctx, CharSequence msg, int duration)
            throws NullPointerException {
        if (null == ctx) {
            throw new NullPointerException("The ctx is null!");
        }
        if (0 > duration) {
            duration = LENGTH_SHORT;
        }
        toast(ctx, msg, duration);
    }

    /**
     * @param ctx
     * @param resId
     * @param duration
     */
    public static void show(Context ctx, int resId, int duration)
            throws NullPointerException {
        if (null == ctx) {
            throw new NullPointerException("The ctx is null!");
        }
        if (0 > duration) {
            duration = LENGTH_SHORT;
        }
        toast(ctx, ctx.getResources().getString(resId), duration);
    }

}