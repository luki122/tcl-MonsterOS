package cn.tcl.weather.viewhelper;

import android.content.Context;
import android.view.View;

import com.leon.tools.view.UiController;

import cn.tcl.weather.service.UpdateService;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-10-10.
 * $desc
 */
public abstract class AbsWeatherCityManagerVh extends UiController {

    public AbsWeatherCityManagerVh(View view) {
        super(view);
    }

    public AbsWeatherCityManagerVh(Context context, int resLayout) {
        super(context, resLayout);
    }


    public abstract void init();


    public abstract void recycle();


    public abstract void onDonwTouch();


    public abstract void setUpdateService(UpdateService updateService);


}
