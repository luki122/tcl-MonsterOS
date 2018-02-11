package cn.tcl.weather.viewhelper;

import android.content.Context;
import android.view.View;

import com.leon.tools.view.UiController;

import cn.tcl.weather.bean.City;
import cn.tcl.weather.service.UpdateService;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-10-10.
 * $desc
 */
public abstract class AbsAddCityVh extends UiController {
    public AbsAddCityVh(View view) {
        super(view);
    }

    public AbsAddCityVh(Context context, int resLayout) {
        super(context, resLayout);
    }


    public abstract void setUpdateService(UpdateService updateService);

    public abstract void init();

    public abstract void recycle();

    public abstract void addCity(City city);
}
