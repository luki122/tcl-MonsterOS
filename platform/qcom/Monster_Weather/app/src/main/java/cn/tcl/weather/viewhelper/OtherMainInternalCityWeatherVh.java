/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.viewhelper;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gapp.common.animation.IPainterView;
import com.gapp.common.animation.scenes.AbsScenes;
import com.gapp.common.utils.BitmapManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.tcl.weather.ActivityFactory;
import cn.tcl.weather.MainActivity;
import cn.tcl.weather.R;
import cn.tcl.weather.TclWeatherWarningActivity;
import cn.tcl.weather.bean.City;
import cn.tcl.weather.bean.CityLifeIndexInfo;
import cn.tcl.weather.bean.CityWeatherInfo;
import cn.tcl.weather.bean.CityWeatherWarning;
import cn.tcl.weather.bean.DayWeather;
import cn.tcl.weather.bean.HourWeather;
import cn.tcl.weather.internet.ServerConstant;
import cn.tcl.weather.internet.StatusAir;
import cn.tcl.weather.internet.StatusWarning;
import cn.tcl.weather.internet.StatusWeather;
import cn.tcl.weather.internet.StatusWindDirection;
import cn.tcl.weather.internet.StatusWindPower;
import cn.tcl.weather.internet.UrlConnector;
import cn.tcl.weather.utils.CommonUtils;
import cn.tcl.weather.utils.DateFormatUtils;
import cn.tcl.weather.utils.LogUtils;
import cn.tcl.weather.view.WeatherDetailAdapter;
import cn.tcl.weather.view.WeatherDetailView;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-5.
 * $desc
 */
public class OtherMainInternalCityWeatherVh extends AbsMainCityWeatherVh implements View.OnClickListener {

    private final String PARTENER_CODE = "1000001019";
    private final String WEATHER_AIR_STATE = "http://mobile.weathercn.com/aqi.do?id=areaID&partner=partenerCode";
    private final String FIFTEEN_DAYS_WEATHER = "http://mobile.weathercn.com/15d.do?id=areaID&partner=partenerCode";
    private final String TWEENTY_FOUR_HOURS_WEATHER = "http://mobile.weathercn.com/eachhours.do?id=areaID&partner=partenerCode";
    private final String LIFE = "http://mobile.weathercn.com/livingindex.do?id=areaID&partner=partenerCode";

    private static final String TAG = OtherMainInternalCityWeatherVh.class.getName();

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

    OtherMainInternalCityWeatherVh(MainActivity activity, BitmapManager manager) {
        super(activity, R.layout.other_weather_nestscroll_layout);
        mActivity = activity;
        mBitmapManager = manager;
    }

    @Override
    public View getIgnoreView() {
        return findViewById(R.id.city_weather_ll_cardlayout);
    }

    public void init() {
        ViewGroup vg = findViewById(R.id.tcl_weather_ll_head);
        View.inflate(mActivity, R.layout.other_city_weather_layout_head, vg);
        vg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        vg = findViewById(R.id.tcl_weather_ll_bottom);
        vg.addView(View.inflate(mActivity, R.layout.other_city_weather_layout_bottom, null));
        mUrlConnector = new UrlConnector(mActivity);
        findViewById(R.id.city_weather_air_state).setOnClickListener(this);
        findViewById(R.id.wdv_24hours).setOnClickListener(this);
        findViewById(R.id.city_weather_lately).setOnClickListener(this);
        findViewById(R.id.wear_index).setOnClickListener(this);
        findViewById(R.id.wear_index_text).setOnClickListener(this);
        findViewById(R.id.sport_index).setOnClickListener(this);
        findViewById(R.id.sport_index_text).setOnClickListener(this);


        // show warning choice
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
        findViewById(R.id.tcl_weather_sv).setScrollY(0);// scroll current scroll view to 0
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

        //set air status str
        setTextToTextView(R.id.city_weather_air_state, StatusAir.getAirStrByAQIValue(weatherInfo.aqiValue));//set air state

        //set air status icon
        ImageView airStateImageView = findViewById(R.id.city_weather_air_state_icon);
        airStateImageView.setImageDrawable(StatusAir.getAirDrawableByAQIValue(weatherInfo.aqiValue));

        setWeatherTip(city, weatherInfo);


        //set the city name
        setTextToTextView(R.id.city_weather_city_name, city.getCountyName());

        //set the temprature for animation
        setTextToTextView(R.id.city_weather_anim_temp, weatherInfo.getTempWithSymbol());

        setWarningInfo(city);

        setCityLatelyDayWeatherInfo(city);

        setWindAndHumidityInfo(city);

        set24HoursWeatherInfo(city, city.getCityWeatherInfo().getHourWeathers());

        setLifeIndexInfo(city);

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

    private void setWarningInfo(City city) {
//        if (CommonUtils.IS_DEBUG) {
//            String warnText = StatusWarning.getWarningText(mActivity);
//            setTextToTextView(R.id.city_weather_warn, warnText);
//
//            TextView warn = findViewById(R.id.city_weather_warn);
//            warn.setTextColor(StatusWarning.getWaringTextColor(mActivity));
//
//        } else {
        if (city.hasWeatherWarning()) {//if has warning
            final CityWeatherWarning warningInfo = city.getCityWeatherWarnings().get(0);
            String warnText = warningInfo.getWarnCategoryName();
            setTextToTextView(R.id.city_weather_warn, warnText);

            // Set text color
            TextView warn = findViewById(R.id.city_weather_warn);
            warn.setTextColor(warningInfo.getWarningTextColor());

            if (!TextUtils.isEmpty(warnText)) {
                findViewById(R.id.city_weather_warn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(TclWeatherWarningActivity.WARN_PARAM, warningInfo);
                        ActivityFactory.jumpToActivity(ActivityFactory.WEATHER_WARNING_ACTIVITY, mActivity, bundle);
                    }
                });
            }
        } else {// there is no warning
            setTextToTextView(R.id.city_weather_warn, "");
            findViewById(R.id.city_weather_warn).setOnClickListener(null);
        }
//        }
    }

    private void set24HoursWeatherInfo(City city, List<HourWeather> hours) {
        WeatherDetailView detailView = findViewById(R.id.wdv_24hours);
        WeatherDetailAdapter adapter = new WeatherDetailAdapter();

        // set hour weather now
        HourWeather weather1 = new HourWeather();
        try {
            weather1.temperature = Float.valueOf(city.getCityWeatherInfo().temperature);
        } catch (Exception e) {
            weather1.temperature = 25f;//default to 25
        }
        weather1.icon = StatusWeather.getWeather24HourIconByNo(city.getCityWeatherInfo().weatherNo);

        if (hours.size() > 0) {// if we has hourweather
            HourWeather weather = hours.get(0);
            weather1.time = weather.getOffsetTime(-1);

            if (TextUtils.isEmpty(weather1.time)) {
                weather1.time = ServerConstant.createCurrentDateYYYYMMDDHHSS();//set current time "yyMMddHHSS"
            }
        } else {
            weather1.time = ServerConstant.createCurrentDateYYYYMMDDHHSS();//set current time "yyMMddHHSS"
        }

        // end set hour weather now

        WeatherDetailView.DrawingItem item;

        List<DayWeather> dayWeathers = city.getCityWeatherInfo().getDayWeathers();
        if (null != dayWeathers && dayWeathers.size() > 1) {//if size more than 1, then add other time
            DayWeather w1 = dayWeathers.get(0);// get current day's weather status
            DayWeather w2 = dayWeathers.get(1);// get next day's weather status

            for (HourWeather weather2 : hours) {
                item = new WeatherDetailView.DrawingItem(weather1, weather2);

                item.setIcon(mBitmapManager.generateBitmap(weather1.icon), mBitmapManager.generateBitmap(weather2.icon));
                weather1 = weather2;

                WeatherDetailAdapter.WeatherDetailItem txtItem = checkSunriseAndSunset(item, w1.sunriseTime, w1.sunsetTime);// if today has sunrise or sun set
                if (null == txtItem)
                    txtItem = checkSunriseAndSunset(item, w2.sunriseTime, w2.sunsetTime);// if tomorrow has sunrise or sun set
                if (null != txtItem)
                    adapter.addItem(txtItem);// add sunrise or sun set item
                adapter.addItem(item);
            }
        } else {
            for (HourWeather weather2 : hours) {
                item = new WeatherDetailView.DrawingItem(weather1, weather2);
                adapter.addItem(item);
                item.setIcon(mBitmapManager.generateBitmap(weather1.icon), mBitmapManager.generateBitmap(weather2.icon));
                weather1 = weather2;
            }
        }

        detailView.setAdpater(adapter);
    }

    /**
     * judge if time is in [startTime,endTime)
     *
     * @param time
     * @param startTime
     * @param endTime
     * @return true if time is in [startTime,endTime), otherwise false
     */
    private static boolean isTimeInTimes(String time, String startTime, String endTime) {
        if (!TextUtils.isEmpty(time) && !TextUtils.isEmpty(startTime) && !TextUtils.isEmpty(endTime)) {
            int i = startTime.compareTo(time);
            if (i <= 0) {
                return endTime.compareTo(time) > 0;
            }
        }
        return false;
    }

    private WeatherDetailAdapter.WeatherDetailItem checkSunriseAndSunset(WeatherDetailView.DrawingItem item, String sunrise, String sunSet) {
        HourWeather center = null;
        String text = "";
        if (isTimeInTimes(sunrise, item.mWeatherStart.time, item.mWeatherEnd.time)) {
            final int length = sunrise.length();
            String riseStr = sunrise.substring(length - 4);
            center = new HourWeather();
            center.time = riseStr;
            text = mActivity.getString(R.string.sunrise);

        } else if (isTimeInTimes(sunSet, item.mWeatherStart.time, item.mWeatherEnd.time)) {
            final int length = sunSet.length();
            String setStr = sunSet.substring(length - 4);
            center = new HourWeather();
            center.time = setStr;
            text = mActivity.getString(R.string.sunset);
        }


        if (null != center) {
            center.icon = item.mWeatherStart.icon;
            center.temperature = (item.mWeatherStart.temperature + item.mWeatherEnd.temperature) / 2.0f;

            WeatherDetailView.TxtItem currentItem = new WeatherDetailView.TxtItem(item.mWeatherStart, center, text);
            currentItem.setIcon(mBitmapManager.generateBitmap(item.mWeatherStart.icon), mBitmapManager.generateBitmap(item.mWeatherEnd.icon));
            item.mWeatherStart = center;//change current to next
            return currentItem;
        }
        return null;
    }


    /**
     * set lately day weather info
     *
     * @param city
     */
    private void setCityLatelyDayWeatherInfo(City city) {
        DayWeather yesterdayWeather = city.getCityWeatherInfo().yesterdayWeatherInfo;
        List<DayWeather> dayWeathers = city.getCityWeatherInfo().getDayWeathers();
        dayWeathers.add(0, yesterdayWeather);
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
                        //setTextToTextView(R.id.tv_second_day, weekStr);//set the day weather date txt
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

    private void setWindAndHumidityInfo(City city) {
        CityWeatherInfo info = city.getCityWeatherInfo();
        setTextToTextView(R.id.city_weather_wind_direction,
                StatusWindDirection.getWindDirectionStrByNo(info.windDirectionNo));
        setTextToTextView(R.id.city_weather_wind_grade,
                StatusWindPower.getWindPowerStrByNo(info.windGrade));
        setTextToTextView(R.id.city_weather_humidity_value, info.getHumidityWithSymbol());
    }

    private void setLifeIndexInfo(City city) {


        ArrayList<CityLifeIndexInfo> lifeIndexInfos = city.getCityWeatherInfo().getLifeIndexsInfo();

        if (null != lifeIndexInfos && lifeIndexInfos.size() >= 4) {

            CityLifeIndexInfo dressIndexInfo = null;
            CityLifeIndexInfo sunScreenIndexInfo = null;
            CityLifeIndexInfo sportIndexInfo = null;
            CityLifeIndexInfo carWashIndexInfo = null;
            for (CityLifeIndexInfo cityLifeIndexInfo : lifeIndexInfos) {
                String indexShortName = cityLifeIndexInfo.indexShortName;
                switch (indexShortName) {
                    case "ct":
                        dressIndexInfo = cityLifeIndexInfo;
                        setTextToTextView(R.id.city_weather_dress_index_value, dressIndexInfo.indexValueCurrentDay);
                        setTextToTextView(R.id.dress_index_tip, dressIndexInfo.indexDescribeCurrentDay);
                        continue;
                    case "fs":
                        sunScreenIndexInfo = cityLifeIndexInfo;
                        setTextToTextView(R.id.city_weather_sunscreen_index_value, sunScreenIndexInfo.indexValueCurrentDay);
                        setTextToTextView(R.id.sunscreen_index_tip, sunScreenIndexInfo.indexDescribeCurrentDay);
                        continue;
                    case "yd":
                        sportIndexInfo = cityLifeIndexInfo;
                        setTextToTextView(R.id.city_weather_sport_index_value, sportIndexInfo.indexValueCurrentDay);
                        setTextToTextView(R.id.sport_index_tip, sportIndexInfo.indexDescribeCurrentDay);
                        continue;
                    case "xc":
                        carWashIndexInfo = cityLifeIndexInfo;
                        setTextToTextView(R.id.city_weather_wash_car_index_value, carWashIndexInfo.indexValueCurrentDay);
                        setTextToTextView(R.id.wash_car_index_tip, carWashIndexInfo.indexDescribeCurrentDay);
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
        Rect rect = new Rect();
        View view = findViewById(R.id.wdv_24hours);
        int[] position = new int[2];
        view.getLocationOnScreen(position);
        rect.set(position[0], position[1], position[0] + view.getWidth(), position[1] + view.getHeight());
        if (rect.contains((int) downEvent.getRawX(), (int) downEvent.getRawY())) {
            return true;// when touch event is in this view ,  never change page
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.city_weather_air_state:
                mUrlConnector.loadUrl(WEATHER_AIR_STATE, mCity.getCityWeatherInfo().areaId, PARTENER_CODE);
                break;
            case R.id.city_weather_lately:
                mUrlConnector.loadUrl(FIFTEEN_DAYS_WEATHER, mCity.getCityWeatherInfo().areaId, PARTENER_CODE);
                break;
            case R.id.wdv_24hours:
                mUrlConnector.loadUrl(TWEENTY_FOUR_HOURS_WEATHER, mCity.getCityWeatherInfo().areaId, PARTENER_CODE);
                break;
            case R.id.wear_index:
            case R.id.wear_index_text:
            case R.id.sport_index:
            case R.id.sport_index_text:
                mUrlConnector.loadUrl(LIFE, mCity.getCityWeatherInfo().areaId, PARTENER_CODE);
                break;
        }
    }
}
