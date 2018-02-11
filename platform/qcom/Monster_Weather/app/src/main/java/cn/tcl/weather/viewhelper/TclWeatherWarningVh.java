package cn.tcl.weather.viewhelper;

import android.content.res.Resources;
import android.view.View;

import cn.tcl.weather.R;
import cn.tcl.weather.TclWeatherWarningActivity;
import cn.tcl.weather.bean.CityWeatherWarning;
import mst.widget.toolbar.Toolbar;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-10-10.
 * TCL rom use
 */
public class TclWeatherWarningVh extends AbsWeatherWarningVh{

    public TclWeatherWarningActivity mActivity;
    private Toolbar mToolbar;

    TclWeatherWarningVh(TclWeatherWarningActivity activity) {
        super(activity, R.layout.tcl_weather_warning_layout);
        mActivity = activity;
    }

    @Override
    public void init() {
        mToolbar = findViewById(R.id.toolbar);

        // Set the navigation icon
        mToolbar.setNavigationIcon(com.mst.R.drawable.ic_toolbar_back);
        mToolbar.setTitle(mActivity.getResources().getString(R.string.early_warning_information));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.finish();
            }
        });
    }

    public void setCityWeatherWearning(CityWeatherWarning warning) {
        Resources resource = mActivity.getResources();
        if (null != warning) {
            setTextToTextView(R.id.warning_title,
                    ((warning.warnCity.equals("")) ? warning.warnProvince : warning.warnCity) +
                            resource.getString(R.string.release) +
                            warning.getWarnCategoryName() + warning.warnGradeName +
                            resource.getString(R.string.alarm));
            setTextToTextView(R.id.warning_content, "\u3000\u3000" + warning.warnContent);
            setTextToTextView(R.id.warning_time_content, warning.warnTime);
        } else {
            mActivity.finish();
        }
    }

    @Override
    public void recycle() {
    }
}
