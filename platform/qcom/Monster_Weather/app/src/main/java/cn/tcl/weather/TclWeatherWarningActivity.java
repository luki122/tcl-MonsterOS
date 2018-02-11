/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import cn.tcl.weather.bean.CityWeatherWarning;
import cn.tcl.weather.viewhelper.AbsWeatherWarningVh;
import cn.tcl.weather.viewhelper.VhFactory;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * <p/>
 * <p/>
 * Get Weather warning information
 */
public class TclWeatherWarningActivity extends TclBaseActivity {
    public final static String WARN_PARAM = "weather_warning_param";
    private AbsWeatherWarningVh mWaringVh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        initWindow();
        mWaringVh = VhFactory.newVhInstance(VhFactory.CITY_WARNING_VH, this);
        mWaringVh.init();
        setContentView(mWaringVh.getView());
        mWaringVh.setCityWeatherWearning((CityWeatherWarning) getIntent().getParcelableExtra(WARN_PARAM));
    }

    @Override
    protected void onDestroy() {
        mWaringVh.recycle();
        super.onDestroy();
    }

    private void initWindow() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
    }


}
