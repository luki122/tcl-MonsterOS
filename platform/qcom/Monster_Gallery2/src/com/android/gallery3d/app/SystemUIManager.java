package com.android.gallery3d.app;
import java.lang.reflect.Method;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.util.LogUtil;
import java.util.ArrayList;

import android.graphics.Color;

public class SystemUIManager {
    
    private static final String TAG = "SystemUIManager";
    
    private AbstractGalleryActivity mActivity;
    private boolean mHasVirtualKeys;//i.e. System shows navigation bar. 
    private int mNavigationBarHeight;
    private int mStatusBarHeight;
    private int mActionBarHeight;
    
    public static final int DO_NOT_SET = -2;
    
    public interface SystemUIFlagChangeListener {
        public void onNavigationBarHidden();
        public void onNavigationBarShown();
    }
    
    //private ArrayList<SystemUIFlagChangeListener> mSystemUIFlagChangeListeners = new ArrayList<SystemUIFlagChangeListener>();
    
    public SystemUIManager(AbstractGalleryActivity activity) {
        mActivity = activity;
        mHasVirtualKeys = checkVirutalKey();
        mNavigationBarHeight = getNavigationBarHeightByResource();
        mStatusBarHeight = getStatusBarHeightByResource();
        mActionBarHeight = getActionBarHeightByResource();
    }

    /*
    public void registerSystemUIFlagChangeListener(SystemUIFlagChangeListener listener) {
        if(null == mSystemUIFlagChangeListeners) {
            return;
        }
        if( ! mSystemUIFlagChangeListeners.contains(listener)) {
            mSystemUIFlagChangeListeners.add(listener);
        }
    }

    public void unregisterSystemUIFlagChangeListener(SystemUIFlagChangeListener listener) {
        if(null == mSystemUIFlagChangeListeners || mSystemUIFlagChangeListeners.isEmpty()) {
            return;
        }
        if(mSystemUIFlagChangeListeners.contains(listener)) {
            mSystemUIFlagChangeListeners.remove(listener);
        }
    }
    
    public void notifyNavigationBarShown() {
        for(SystemUIFlagChangeListener listener : mSystemUIFlagChangeListeners) {
            listener.onNavigationBarShown();
        }
    }
    
    public void notifyNavigationBarHidden() {
        for(SystemUIFlagChangeListener listener : mSystemUIFlagChangeListeners) {
            listener.onNavigationBarHidden();
        }
    }
    */
    
    public void setFlag(boolean lightsOut, boolean occupyNavigationBar, int navigationBarColor) {
        /*
        LogUtil.i2(TAG, "SystemUIManager::setFlag lightsOut:" + lightsOut + 
                " occupyNavigationBar:" + occupyNavigationBar + 
                " navigationBarColor:" +navigationBarColor );*/ 
        GLRoot glRoot = mActivity.getGLRoot();
        if (!ApiHelper.HAS_SET_SYSTEM_UI_VISIBILITY) return;
        int flags = 0;
        boolean isPortrait = mActivity.isPortrait();
        if(occupyNavigationBar) {
            flags |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
        if(lightsOut) {
            flags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | 
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | 
                    View.SYSTEM_UI_FLAG_IMMERSIVE |
                    View.SYSTEM_UI_FLAG_FULLSCREEN | 
                    View.STATUS_BAR_HIDDEN;
            
            if(isPortrait) {
                flags |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }
        } else {
            flags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | 
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | 
                    View.SYSTEM_UI_FLAG_IMMERSIVE;
        }
        glRoot.applySystemUIVisibility(flags);
        if(navigationBarColor != DO_NOT_SET) {
            setNavigationBarColor(navigationBarColor);
        }
        //mActivity.getWindow().getDecorView().setSystemUiVisibility(flags);
    }
    

    /**
     * if we add FLAG_LAYOUT_NO_LIMITS to window, we can't set navigation bar color to WHITE.
     * in this situation, the navigation bar is transparent by default.
     * so if we want to change navigation bar color to WHITE, we should clear FLAG_LAYOUT_NO_LIMITS in advance.
     * @param color color to set
     */
    public void setNavigationBarColor(int color) {
        //LogUtil.d(TAG, "setNavigationBarColor : color: " + color );
        Window window = mActivity.getWindow();
        window.setNavigationBarColor(color);
    }
    
    /*
    public void addOrClearFlagLayoutNoLimits(boolean layoutNoLimits) {
        //LogUtil.i(TAG, " addOrClearFlagLayoutNoLimits layoutNoLimits:" + layoutNoLimits);
        LogUtil.d(TAG, "addOrClearFlagLayoutNoLimits : layoutNoLimits " + layoutNoLimits );
        if(layoutNoLimits) {
            mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }
    */
    
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setStatusBarColor(int color) {    
        //LogUtil.d(TAG, "setStatusBarColor : color: " + color );
        Window window = mActivity.getWindow();    
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);   
        window.setStatusBarColor(color);
    }
    
    /*
    public void showStatusBar(boolean show) {
        Window window = mActivity.getWindow();
        if( ! show) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }
    */
    
    private int getActionBarHeightByResource() {
        final TypedArray styledAttributes = mActivity.getTheme().obtainStyledAttributes(new int[] { android.R.attr.actionBarSize });
        mActionBarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        return mActionBarHeight;
    }

    private int getStatusBarHeightByResource() {
        int result = 0;
        int resourceId = mActivity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = mActivity.getResources().getDimensionPixelSize(resourceId);
        } 
        return result;
    }
    
    public int getActionBarHeight() {
        return mActionBarHeight;
    }
    
    public int getStatusBarHeight() {
        return mStatusBarHeight;
    }

    public int getNavigationBarHeight() {
        return mNavigationBarHeight;
    }

    private int getNavigationBarHeightByResource() {
        if( ! mHasVirtualKeys) return 0;
        Resources resources = mActivity.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }
    
    public boolean hasVirtualKeys() {
        return mHasVirtualKeys;
    }

    private boolean checkVirutalKey() {
        return getNoHasVirtualKeyHeight() != getHasVirtualKeyHeight();
    }
    
    /**
     * get screen size,not including virtual keys height
     * @return 
     */
    private int getNoHasVirtualKeyHeight() {
        int height = mActivity.getWindowManager().getDefaultDisplay().getHeight();
        return height;
    }

    private int getHasVirtualKeyHeight() {
        int dpi = 0;
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        @SuppressWarnings("rawtypes")
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            dpi = dm.heightPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dpi;
    }
    
    
}
