package com.monster.launcher;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

public class LauncherRootView extends InsettableFrameLayout {
    public LauncherRootView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        if(insets!=null){
            if(insets.top>0){
                WindowGlobalValue.setStatusbarHeight(insets.top);
            }
            if(insets.bottom>0){
                WindowGlobalValue.setNavigatebarHeight(insets.bottom);
            }
        }
        setInsets(insets);
        return true; // I'll take it from here
    }
}