/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.media.effect.Effect;
import android.support.v4.view.NestedScrollingChild;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EdgeEffect;
import android.widget.ScrollView;

import java.lang.reflect.Field;

import cn.tcl.weather.utils.LogUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-11-8.
 * $desc
 */
public class ColorScrollView extends ScrollView implements NestedScrollingChild{
    private final static String TAG = "ColorScrollView";

    private final static String EDGE_GOLOW_BOTTOM = "mEdgeGlowBottom";
    private final static String EDGE_GOLOW_TOP = "mEdgeGlowTop";

    private EdgeEffect mBottomEdgeEffect, mTopEdgeEffect;
    private int mColor = 0x4c9C9C9C;

    public ColorScrollView(Context context) {
        super(context);
        init();
    }

    public ColorScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public ColorScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setNestedScrollingEnabled(true);
        setEdgeColor(mColor);
    }


    @Override
    public void setOverScrollMode(int mode) {
        super.setOverScrollMode(mode);
        mBottomEdgeEffect = getEffectFiled(EDGE_GOLOW_BOTTOM);
        mTopEdgeEffect = getEffectFiled(EDGE_GOLOW_TOP);
    }


    private EdgeEffect getEffectFiled(String name) {
        EdgeEffect effect = null;
        try {
            Field field = ScrollView.class.getDeclaredField(name);
            if (null != field) {
                field.setAccessible(true);
                effect = (EdgeEffect) field.get(this);
                field.setAccessible(false);
            }
        } catch (Exception e) {
            LogUtils.d(TAG, "can't find edge effect: " + e.toString());
        }
        return effect;
    }


    public void setEdgeColor(int color) {
        mColor = color;
        if (0 != color) {
            if (null != mBottomEdgeEffect)
                mBottomEdgeEffect.setColor(color);
            if (null != mTopEdgeEffect)
                mTopEdgeEffect.setColor(Color.TRANSPARENT);
        }
    }
}
