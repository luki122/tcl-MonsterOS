package cn.tcl.weather.viewhelper;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import com.leon.tools.view.UiController;

import cn.tcl.weather.bean.City;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-10-10.
 * $desc
 */
public abstract class AbsMainCityWeatherVh extends UiController {

    public AbsMainCityWeatherVh(View view) {
        super(view);
    }

    public AbsMainCityWeatherVh(Context context, int resLayout) {
        super(context, resLayout);
    }

    public abstract void init();

    public abstract void pause();

    public abstract void resume();

    public abstract void recycle();

    public abstract void setCityInfo(City city);

    public abstract City getCityInfo();

    public abstract boolean canScrollHorizontally(int direction, MotionEvent downEvent);


    /**
     * the view which TouchEvent will be not intercept
     *
     * @return
     */
    public abstract View getIgnoreView();

}
