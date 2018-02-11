/* Copyright (C) 2016 Tcl Corporation Limited */
package com.leon.tools.view;

import android.content.Context;
import android.content.res.Resources;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cn.tcl.weather.WeatherCNApplication;
import cn.tcl.weather.bean.CityWeatherInfo;
import cn.tcl.weather.view.CustomMainPageTempView;


/**
 * @author zhanghong
 */
public class UiController {
    private final static String TAG = "UIControler";

    private final View mView;
    private SparseArray<View> mViews = new SparseArray<View>();

    public UiController(View view) {
        mView = view;
    }

    public UiController(Context context, int resLayout) {
        this(View.inflate(context, resLayout, null));
    }

    /**
     * @return
     */
    public final View getView() {
        return mView;
    }

    /**
     * @param viewId
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends View> T findViewById(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = mView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    /**
     * @param viewId
     * @param cs
     */
    public void setTextToTextView(int viewId, CharSequence cs) {
        TextView tv = findViewById(viewId);
        if (null != tv) {
            tv.setText(cs);
        }
    }

    /**
     * @param viewId
     * @param color
     */
    public void setTextColorToTextView(int viewId, int color) {
        TextView tv = findViewById(viewId);
        if (null != tv) {
            tv.setTextColor(color);
        }
    }

    /**
     * @param viewId
     * @param resId
     */
    public void setTextToTextView(int viewId, int resId) {
        TextView tv = findViewById(viewId);
        if (null != tv) {
            tv.setText(resId);
        }
    }

    /**
     * @param viewId
     * @param alphaValue
     */
    public void setAlphaToTextView(int viewId, float alphaValue) {
        TextView tv = findViewById(viewId);
        if (null != tv) {
            tv.setAlpha(alphaValue);
        }
    }

    /**
     * @param viewId
     * @param resId
     */
    public void setImageDrawableToImageView(int viewId, int resId) {
        ImageView iv = findViewById(viewId);
        Resources res = WeatherCNApplication.getWeatherCnApplication().getApplicationContext().getResources();
        if (null != iv) {
            iv.setImageDrawable(res.getDrawable(resId));
        }
    }

    /**
     * @param viewId
     * @param weatherInfo
     */
    public void setTextToCustomMainPageTempView(int viewId, CityWeatherInfo weatherInfo) {
        CustomMainPageTempView view = findViewById(viewId);
        if (null != view) {
            view.setCityWeatherTemp(weatherInfo);
        }
    }

}
