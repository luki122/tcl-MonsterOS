package com.monster.launcher.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.monster.launcher.IChangeColors;
import com.monster.launcher.IChangeLauncherColor;
import com.monster.launcher.LauncherAppState;
import com.monster.launcher.Log;
import com.monster.launcher.R;

/**
 * Created by antino on 16-8-22.
 */

public class LauncherLinearLayout extends LinearLayout implements IChangeColors.IItemsColorChange,IChangeLauncherColor {

    public LauncherLinearLayout(Context context) {
        super(context);
    }

    public LauncherLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LauncherLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void changeItemColors(int[] colors) {
         int count = this.getChildCount();
        for(int i= 0;i<count;i++){
            View v=this.getChildAt(i);
            if(v instanceof  IChangeColors.IItemColorChange){
                ((IChangeColors.IItemColorChange)v).changeColors(colors);
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void changeImageColors(int textColor) {
        MenuItemView cleanupView = (MenuItemView)this.findViewById(R.id.clearup_button);
        MenuItemView widgetView = (MenuItemView)this.findViewById(R.id.widget_button);
        MenuItemView wallpaperView = (MenuItemView)this.findViewById(R.id.wallpaper_button);
        MenuItemView arrangeView = (MenuItemView)this.findViewById(R.id.arrange_button);
        if(textColor!=-1){
            if(cleanupView!=null)cleanupView.setCompoundDrawablesRelativeWithIntrinsicBounds(null,this.getResources().getDrawable(R.drawable.clearup_button_black),null,null);;
            if(widgetView!=null)widgetView.setCompoundDrawablesRelativeWithIntrinsicBounds(null,this.getResources().getDrawable(R.drawable.widget_button_black),null,null);
            if(wallpaperView!=null)wallpaperView.setCompoundDrawablesRelativeWithIntrinsicBounds(null,this.getResources().getDrawable(R.drawable.wallpaper_button_black),null,null);
            if(arrangeView!=null)arrangeView.setCompoundDrawablesRelativeWithIntrinsicBounds(null,this.getResources().getDrawable(R.drawable.arrange_button_black),null,null);
        }else{
            if(cleanupView!=null)cleanupView.setCompoundDrawablesRelativeWithIntrinsicBounds(null,this.getResources().getDrawable(R.drawable.clearup_button),null,null);
            if(widgetView!=null)widgetView.setCompoundDrawablesRelativeWithIntrinsicBounds(null,this.getResources().getDrawable(R.drawable.widget_button),null,null);
            if(wallpaperView!=null)wallpaperView.setCompoundDrawablesRelativeWithIntrinsicBounds(null,this.getResources().getDrawable(R.drawable.wallpaper_button),null,null);
            if(arrangeView!=null)arrangeView.setCompoundDrawablesRelativeWithIntrinsicBounds(null,this.getResources().getDrawable(R.drawable.arrange_button),null,null);
        }
    }

    @Override
    public void onColorChanged(int[] colors) {
        changeItemColors(colors);
        changeImageColors(colors[0]);
    }
}
