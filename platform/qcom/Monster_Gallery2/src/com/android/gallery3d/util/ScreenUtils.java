/* 06/03/2015|    jialiang.ren     |      PR-937067       |[5.0][Gallery] pitch-to-zoom is not correctly 2x max */
/* ----------|---------------------|----------------------|---------------------------------------------------- */
/* 31/03/2015|    jialiang.ren     |      PR-962959       |[Android5.0][Gallery_v5.1.9.1.0109.0]    */
/*                                                         The picture will zoom out when zooming in*/
/* ----------|---------------------|----------------------|-----------------------------------------*/
/* 17/04/2015|    qiang.ding1       |      PR-959021       | [Android5.0][Gallery_v5.1.9.1.0107.0][REG][Monitor]There is display empty in title bar*/
/* ----------|--------------------- |----------------------|----------------------------------------------*/
/* 13/05/2015 |    jialiang.ren     |      PR-995626       |[Android][Gallery_v5.1.13.1.0201.0]The */
/*                                                          delete notice box is not in the middle */
/*------------|---------------------|----------------------|---------------------------------------*/
/* 18/06/2015 |    su.jiang         |      PR-1025516      |[Android 5.1][Gallery_v5.1.13.1.0208.0]The operation bar is not in */
/*------------|---------------------|-------------------   |the middle when playing the video----------------------------------*/
/* 05/10/2015|dongliang.feng        |PR512437              |[Android 5.1][Gallery_v5.2.0.1.1.0303.0]The icons disappeared after stopping slideshow */
/* ----------|----------------------|----------------------|----------------- */

/**
 * This file is added by TCT, ShenQianfeng annotated on 2016.08.18
 */
package com.android.gallery3d.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Toolbar;

import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryActivity;

public class ScreenUtils {

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-06,PR937067
    public static final String TAG = "ScreenUtils";

    public static int STATUSBAR_HEIGHT = 0;

    public static int ACTIONBAR_HEIGHT = 0;

    public static int TABBAR_HEIGHT = 0;

    public static int ALBUM_MIN_SCROLL = 0;

    public static int ALBUMSET_MIN_SCROLL = 0;

    public static float SCALE_FIX = 0f;

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-31,PR962959
    public static float MAX_LIMIT = 0f;

    public static final long ANIM_TIME = 280;// MODIFIED by jian.pan1, 2016-03-24,BUG-1845735

    public static void initData(Context context) {
        getStatusBarHeight(context);
        getActionBarHeight(context);
        getTabBarHeight(context);
        getScaleFix((Activity)context);

        ALBUM_MIN_SCROLL = -STATUSBAR_HEIGHT - ACTIONBAR_HEIGHT;
        ALBUMSET_MIN_SCROLL = -STATUSBAR_HEIGHT - ACTIONBAR_HEIGHT - TABBAR_HEIGHT;
    }

    public static void getStatusBarHeight(Context context) {
        STATUSBAR_HEIGHT = (int)context.getResources().getDimension(R.dimen.status_bar_height);
    }

    public static void getActionBarHeight(Context context) {
        ACTIONBAR_HEIGHT = context.getResources().getDimensionPixelSize(R.dimen.action_bar_height);
    }

    public static void getTabBarHeight(Context context) {
        TABBAR_HEIGHT = context.getResources().getDimensionPixelSize(R.dimen.tab_height);
    }

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-05-13,PR995626 begin
    public static int getNavigationBarHeight(Activity activity) {
        DisplayMetrics realM = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(realM);

        DisplayMetrics actM = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(actM);

        if(realM.widthPixels == actM.widthPixels) {
            return realM.heightPixels - actM.heightPixels;
        } else {
            return realM.widthPixels - actM.widthPixels;
        }
    }
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-05-13,PR995626 end

    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-06-18,PR1025516 begin
    public static boolean isNavigationAtBottom(Activity activity) {
        boolean isAtBottom = false;
        DisplayMetrics realM = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(realM);
        DisplayMetrics actM = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(actM);
        if(realM.heightPixels > actM.heightPixels) {
            isAtBottom = true;
        }
        return isAtBottom;
    }
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-06-18,PR1025516 end
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-06,PR937067 begin
    public static void getScaleFix(Activity activity) {
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        SCALE_FIX = 18 / metric.density;
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-31,PR962959 begin
        if(metric.density == 2) {
            MAX_LIMIT = 720 * 2.0f / 2304;
        } else {
            MAX_LIMIT = 1080 * 2.0f / 2304;
        }
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-31,PR962959 end
    }
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-06,PR937067 end

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-01-22,PR904487 begin
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-11-05, PR512437 begin
    /* MODIFIED-BEGIN by jian.pan1, 2016-03-24,BUG-1845735 */
    public static void showSystemUI(GalleryActivity activity, long animateDuration) {
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-21,PR955623 begin
        try {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            animatorShowToolbar(true, activity.getToolbar(), animateDuration);
        } catch(Exception e) {
            e.printStackTrace();
        }
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-21,PR955623 end
    }

    public static void hideSystemUI(GalleryActivity activity, long animateDuration) {
    /* MODIFIED-END by jian.pan1,BUG-1845735 */
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-21,PR955623 begin
        try {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            animatorShowToolbar(false, activity.getToolbar(), animateDuration);// MODIFIED by jian.pan1, 2016-03-24,BUG-1845735
        } catch(Exception e) {
            e.printStackTrace();
        }
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-21,PR955623 end
    }
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-01-22,PR904487 end

    private static ObjectAnimator objectAnimator = null;
    private static void animatorShowToolbar(boolean show, final Toolbar toolbar, long animateDuration) {// MODIFIED by jian.pan1, 2016-03-24,BUG-1845735
        final float wantAlpha = show ? 1 : 0;
        float alpha = toolbar.getAlpha();
        if ((alpha < 1 && alpha > 0) || alpha == wantAlpha) {
            return;
        }

        calcelAnim(toolbar, !show);
        objectAnimator = ObjectAnimator.ofFloat(toolbar, "alpha", wantAlpha);
        objectAnimator.setDuration(animateDuration);// MODIFIED by jian.pan1, 2016-03-24,BUG-1845735
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (wantAlpha == 1) {
                    if (toolbar.getTranslationY() != 0) toolbar.setTranslationY(0);
                    if (toolbar.getVisibility() != View.VISIBLE) toolbar.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                if (wantAlpha == 0) {
                    toolbar.setVisibility(View.GONE);
                }
            }
        });
        objectAnimator.start();
    }
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-11-05, PR512437 end

    public static void calcelAnim(Toolbar toolbar, boolean show) {
        if(objectAnimator != null) {
            objectAnimator.cancel();
            objectAnimator = null;
            toolbar.setVisibility(View.VISIBLE);
            toolbar.setAlpha(show ? 1.0f : 0f);
        }
    }
}
