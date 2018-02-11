/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.setupwizard.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Build.VERSION;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.View.OnApplyWindowInsetsListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager.LayoutParams;

import cn.tcl.setupwizard.R;

/**
 * hide the system navigation bar
 */
public class SystemBarHelper {

    @SuppressLint({"InlinedApi"})
    private static final int DEFAULT_IMMERSIVE_FLAGS = 5634;
    @SuppressLint({"InlinedApi"})
    private static final int DIALOG_IMMERSIVE_FLAGS = 4098;
    private static final int STATUS_BAR_DISABLE_BACK = 4194304;

    public SystemBarHelper() {
    }

    public static void hideSystemBars(Dialog dialog) {
        if(VERSION.SDK_INT >= 21) {
            Window window = dialog.getWindow();
            temporarilyDisableDialogFocus(window);
            addImmersiveFlagsToWindow(window, 4098);
            addImmersiveFlagsToDecorView(window, new Handler(), 4098);
        }

    }
    /* MODIFIED-BEGIN by xinlei.sheng, 2016-11-18,BUG-3356295*/
    public static void hideSystemBarsWithFocus(Dialog dialog) {
        if(VERSION.SDK_INT >= 21) {
            Window window = dialog.getWindow();
            addImmersiveFlagsToWindow(window, 4098);
            addImmersiveFlagsToDecorView(window, new Handler(), 4098);
        }
    }
    /* MODIFIED-END by xinlei.sheng,BUG-3356295*/

    public static void hideSystemBars(Window window) {
        if(VERSION.SDK_INT >= 21) {
            addImmersiveFlagsToWindow(window, 5634);
            addImmersiveFlagsToDecorView(window, new Handler(), 5634);
        }

    }

    public static void addVisibilityFlag(View view, int flag) {
        if(VERSION.SDK_INT >= 11) {
            int vis = view.getSystemUiVisibility();
            view.setSystemUiVisibility(vis | flag);
        }

    }

    public static void addVisibilityFlag(Window window, int flag) {
        if(VERSION.SDK_INT >= 11) {
            LayoutParams attrs = window.getAttributes();
            attrs.systemUiVisibility |= flag;
            window.setAttributes(attrs);
        }

    }

    public static void removeVisibilityFlag(View view, int flag) {
        if(VERSION.SDK_INT >= 11) {
            int vis = view.getSystemUiVisibility();
            view.setSystemUiVisibility(vis & ~flag);
        }

    }

    public static void removeVisibilityFlag(Window window, int flag) {
        if(VERSION.SDK_INT >= 11) {
            LayoutParams attrs = window.getAttributes();
            attrs.systemUiVisibility &= ~flag;
            window.setAttributes(attrs);
        }

    }

    public static void setBackButtonVisible(Window window, boolean visible) {
        if(VERSION.SDK_INT >= 11) {
            if(visible) {
                removeVisibilityFlag(window, 4194304);
            } else {
                addVisibilityFlag(window, 4194304);
            }
        }

    }

    public static void setImeInsetView(View view) {
        if(VERSION.SDK_INT >= 21) {
            view.setOnApplyWindowInsetsListener(new SystemBarHelper.WindowInsetsListener(view.getContext()));
        }

    }

    @TargetApi(21)
    private static void addImmersiveFlagsToDecorView(final Window window, final Handler handler, final int vis) {
        View decorView = window.peekDecorView();
        if(decorView != null) {
            addVisibilityFlag(decorView, vis);
        } else {
            handler.post(new Runnable() {
                public void run() {
                    SystemBarHelper.addImmersiveFlagsToDecorView(window, handler, vis);
                }
            });
        }

    }

    @TargetApi(21)
    private static void addImmersiveFlagsToWindow(Window window, int vis) {
        LayoutParams attrs = window.getAttributes();
        attrs.systemUiVisibility |= vis;
        window.setAttributes(attrs);
        window.setNavigationBarColor(0);
        window.setStatusBarColor(0);
    }

    private static void temporarilyDisableDialogFocus(final Window window) {
        window.setFlags(8, 8);
        window.setSoftInputMode(256);
        (new Handler()).post(new Runnable() {
            public void run() {
                window.clearFlags(8);
            }
        });
    }

    @TargetApi(21)
    private static class WindowInsetsListener implements OnApplyWindowInsetsListener {
        private int mNavigationBarHeight;

        public WindowInsetsListener(Context context) {
            this.mNavigationBarHeight = context.getResources().getDimensionPixelSize(R.dimen.suw_navbar_height);
        }

        public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
            int bottomInset = insets.getSystemWindowInsetBottom();
            int bottomMargin = Math.max(insets.getSystemWindowInsetBottom() - this.mNavigationBarHeight, 0);
            MarginLayoutParams lp = (MarginLayoutParams)view.getLayoutParams();
            if(bottomMargin < lp.bottomMargin + view.getHeight()) {
                lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, bottomMargin);
                view.setLayoutParams(lp);
                bottomInset = 0;
            }

            return insets.replaceSystemWindowInsets(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), bottomInset);
        }
    }
}
