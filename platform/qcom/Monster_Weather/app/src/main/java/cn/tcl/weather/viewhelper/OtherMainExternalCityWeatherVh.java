/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.viewhelper;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;

import com.gapp.common.animation.IPainterView;
import com.gapp.common.animation.scenes.AbsScenes;
import com.gapp.common.utils.BitmapManager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.tcl.weather.MainActivity;
import cn.tcl.weather.R;
import cn.tcl.weather.bean.City;
import cn.tcl.weather.bean.CityWeatherInfo;
import cn.tcl.weather.bean.DayWeather;
import cn.tcl.weather.internet.StatusWarning;
import cn.tcl.weather.internet.StatusWeather;
import cn.tcl.weather.internet.UrlConnector;
import cn.tcl.weather.utils.CommonUtils;
import cn.tcl.weather.utils.DateFormatUtils;
import cn.tcl.weather.utils.LogUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-5.
 * $desc
 */
public class OtherMainExternalCityWeatherVh extends AbsMainCityWeatherVh implements View.OnClickListener {

    private final String PARTENER_CODE = "1000001019";
    private final String FIFTEEN_DAYS_WEATHER = "http://mobile.weathercn.com/15d.do?id=areaID&partner=partenerCode";

    private static final String TAG = OtherMainExternalCityWeatherVh.class.getName();

    public final static int FIRST_DAY = 0;
    public final static int SECOND_DAY = 1;
    public final static int THIRD_DAY = 2;
    public final static int FORTH_DAY = 3;
    public final static int FIFTH_DAY = 4;

    public final static int MONDAY = 1;
    public final static int Tuesday = 2;
    public final static int Wednesday = 3;
    public final static int Thursday = 4;
    public final static int Friday = 5;
    public final static int Saturday = 6;
    public final static int Sunday = 0;

    private Activity mActivity;
    private City mCity;
    private BitmapManager mBitmapManager;
    private AbsScenes mScenens;
    private boolean isResume;
    private UrlConnector mUrlConnector;

    public OtherMainExternalCityWeatherVh(MainActivity activity, BitmapManager manager) {
        super(activity, R.layout.other_external_city_weather_layout);
        mActivity = activity;
        mBitmapManager = manager;
    }


    public void init() {
        mUrlConnector = new UrlConnector(mActivity);
        findViewById(R.id.external_weather_head_layout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        findViewById(R.id.city_weather_lately).setOnClickListener(this);

        //show warning choice
        findViewById(R.id.data_source).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StatusWarning.showWarningChecker(mActivity);
            }
        });

        // show weather state choice
        findViewById(R.id.city_weather_state).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StatusWeather.showWeatherchecker(mActivity);
            }
        });

        findViewById(R.id.city_weather_city_name).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mScenens) {
                    mScenens.toggleAnmate();
                }
            }
        });
    }

    public void resume() {
        isResume = true;
        mScenens.resume();
    }

    public void pause() {
        mScenens.pause();
        isResume = false;
    }

    private void setScenens(CityWeatherInfo weatherInfo) {
        Class<?> cls = StatusWeather.getWeaatherScences(weatherInfo.weatherNo);

        if (null != mScenens) {
            if (!mScenens.getClass().equals(cls)) {
                if (isResume) {
                    mScenens.pause();
                }
                mScenens.recycle();
                mScenens = null;
            }
        }

        if (null == mScenens) {
            mScenens = StatusWeather.newWeatherScences(cls, (IPainterView) findViewById(R.id.city_weather_pfv), mBitmapManager);
            mScenens.init();
            if (isResume) {
                mScenens.resume();
            }
        }
    }

    /**
     * set the weather info of city
     *
     * @param city
     */
    public void setCityInfo(City city) {
        mCity = city;

        CityWeatherInfo weatherInfo = city.getCityWeatherInfo();
        // set animation bg
        setScenens(weatherInfo);

        // set the temprature
        setTextToCustomMainPageTempView(R.id.city_weather_temp_layout, weatherInfo);

        //set the weather state
        setTextToTextView(R.id.city_weather_state, StatusWeather.getWeatherStatus(weatherInfo.weatherNo));

        setWeatherTip(city, weatherInfo);


        //set the city name
        setTextToTextView(R.id.city_weather_city_name, city.getCountyName());

        //set the temprature for animation
        setTextToTextView(R.id.city_weather_anim_temp, weatherInfo.getTempWithSymbol());


        setCityLatelyDayWeatherInfo(city);

    }

    private void setWeatherTip(City city, CityWeatherInfo weatherInfo) {
        //set weather condition remind, if data is out of date , replace it with the tip words.
        if (city.isRefreshSucceed) {
            String weatherTip[] = weatherInfo.weatherConditionRemind.split("\n");
            setTextToTextView(R.id.city_weather_tip_top, weatherTip[0]);
            setTextToTextView(R.id.city_weather_tip_bottom, weatherTip[1]);
        } else {
            String dateOverdueStr = String.format(mActivity.getResources().getString(R.string.date_overdue_tip), DateFormatUtils.parseDateToString(city.refreshTimeMills));
            String refreshFailTip[] = dateOverdueStr.split("\n");
            setTextToTextView(R.id.city_weather_tip_top, refreshFailTip[0]);
            setTextToTextView(R.id.city_weather_tip_bottom, refreshFailTip[1]);
        }
    }

    /**
     * set lately day weather info
     *
     * @param city
     */
    private void setCityLatelyDayWeatherInfo(City city) {
        List<DayWeather> dayWeathers = city.getCityWeatherInfo().getDayWeathers();
        if (null != dayWeathers && dayWeathers.size() >= 5) {
            for (int i = 0; i < 5; i++) {
                DayWeather currentDayWeather = dayWeathers.get(i);               
                String weekStr = changeToWeekStr(new Date(DateFormatUtils.parseStringToDate(currentDayWeather.date).getTime()));

                String tempStr = CommonUtils.getTempString(currentDayWeather.nightTemp, currentDayWeather.dayTemp);
                LogUtils.i(TAG, weekStr + "    " + tempStr);

                String weatherNo = currentDayWeather.dayWeatherPhenomena;
                switch (i) {
                    case FIRST_DAY:
                        setImageDrawableToImageView(R.id.img_weather_first_day, StatusWeather.getWeather5DayIconByNo(weatherNo));
                        setTextToTextView(R.id.tv_temp_first_day, tempStr);//set the day weather temperature txt
                        continue;
                    case SECOND_DAY:
                        setTextToTextView(R.id.tv_second_day, weekStr);//set the day weather date txt
                        setImageDrawableToImageView(R.id.img_weather_second_day, StatusWeather.getWeather5DayIconByNo(weatherNo));
                        setTextToTextView(R.id.tv_temp_second_day, tempStr);//set the day weather temperature txt
                        continue;
                    case THIRD_DAY:
                        setTextToTextView(R.id.tv_third_day, weekStr);//set the day weather date txt
                        setImageDrawableToImageView(R.id.img_weather_third_day, StatusWeather.getWeather5DayIconByNo(weatherNo));
                        setTextToTextView(R.id.tv_temp_third_day, tempStr);//set the day weather temperature txt
                        continue;
                    case FORTH_DAY:
                        setTextToTextView(R.id.tv_forth_day, weekStr);//set the day weather date txt
                        setImageDrawableToImageView(R.id.img_weather_forth_day, StatusWeather.getWeather5DayIconByNo(weatherNo));
                        setTextToTextView(R.id.tv_temp_forth_day, tempStr);//set the day weather temperature txt
                        continue;
                    case FIFTH_DAY:
                        setTextToTextView(R.id.tv_fifth_day, weekStr);//set the day weather date txt
                        setImageDrawableToImageView(R.id.img_weather_fifth_day, StatusWeather.getWeather5DayIconByNo(weatherNo));
                        setTextToTextView(R.id.tv_temp_fifth_day, tempStr);//set the day weather temperature txt
                        continue;
                    default:
                        continue;
                }
            }
        }
    }

    public void recycle() {
        mScenens.recycle();
    }

    /**
     * get the weather info of city
     *
     * @return
     */
    public City getCityInfo() {
        return mCity;
    }

    /**
     * change the int date to string week
     *
     * @param date
     * @return
     */
    private String changeToWeekStr(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int week = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int dayOfMonth = cal.get(Calendar.DATE);
        LogUtils.i(TAG, dayOfMonth + " @@@ " + week);
        switch (week) {
            case MONDAY:
                return mActivity.getResources().getString(R.string.day_weather_monday);
            case Tuesday:
                return mActivity.getResources().getString(R.string.day_weather_tuesday);
            case Wednesday:
                return mActivity.getResources().getString(R.string.day_weather_wednesday);
            case Thursday:
                return mActivity.getResources().getString(R.string.day_weather_thursday);
            case Friday:
                return mActivity.getResources().getString(R.string.day_weather_friday);
            case Saturday:
                return mActivity.getResources().getString(R.string.day_weather_saturday);
            case Sunday:
                return mActivity.getResources().getString(R.string.day_weather_sunday);
            default:
                return mActivity.getResources().getString(R.string.day_weather_sunday);
        }
    }

    public boolean canScrollHorizontally(int direction, MotionEvent downEvent) {
        return false;
    }

    @Override
    public View getIgnoreView() {
        return findViewById(R.id.city_weather_ll_cardlayout);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.city_weather_lately:
                mUrlConnector.loadUrl(FIFTEEN_DAYS_WEATHER, mCity.getCityWeatherInfo().areaId, PARTENER_CODE);
                break;
        }
    }

}
