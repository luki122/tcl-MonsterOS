package com.monster.launcher.widget;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.monster.launcher.IChangeColors;
import com.monster.launcher.Log;

/**
 * Created by antino on 16-8-22.
 */
public class MenuItemView extends TextView implements IChangeColors.IItemColorChange {
    public MenuItemView(Context context) {
        super(context);
    }

    public MenuItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MenuItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void changeColors(int[] colors) {
        setTextColor(colors[0]);
    }
}