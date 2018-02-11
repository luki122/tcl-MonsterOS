package cn.tcl.weather.internet;

import android.app.Activity;
import android.app.Dialog;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.gapp.common.animation.IPainterView;
import com.gapp.common.animation.scenes.AbsScenes;
import com.gapp.common.animation.scenes.CloudySecenes;
import com.gapp.common.animation.scenes.DustScenes;
import com.gapp.common.animation.scenes.DustStormScenes;
import com.gapp.common.animation.scenes.FoggyScenes;
import com.gapp.common.animation.scenes.HazeScenes;
import com.gapp.common.animation.scenes.HeavyRainScenes;
import com.gapp.common.animation.scenes.HeavySnowScenes;
import com.gapp.common.animation.scenes.HeavyStormScenes;
import com.gapp.common.animation.scenes.LightRainScenes;
import com.gapp.common.animation.scenes.LightSnowScenes;
import com.gapp.common.animation.scenes.ModerateRainScenes;
import com.gapp.common.animation.scenes.ModerateSnowScenes;
import com.gapp.common.animation.scenes.NormalSecenes;
import com.gapp.common.animation.scenes.OvercastSecenes;
import com.gapp.common.animation.scenes.RainScenes;
import com.gapp.common.animation.scenes.SandStormScenes;
import com.gapp.common.animation.scenes.SevereStormScenes;
import com.gapp.common.animation.scenes.SleetScenes;
import com.gapp.common.animation.scenes.SnowStormScenes;
import com.gapp.common.animation.scenes.StormScenes;
import com.gapp.common.animation.scenes.SunnyScenes;
import com.gapp.common.animation.scenes.ThunderShowerScenes;
import com.gapp.common.animation.scenes.ThunderShowerWithHailScenes;
import com.gapp.common.utils.BitmapManager;

import java.lang.reflect.Constructor;

import cn.tcl.weather.R;
import cn.tcl.weather.WeatherCNApplication;
import cn.tcl.weather.utils.CommonUtils;
import cn.tcl.weather.utils.LogUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * Created on 16-8-31.
 */
public enum StatusWeather {

    /**/
    SUNNY                         (0, R.string.weather_sunny,                           R.drawable.wd_sunny,                 R.drawable.ad_sunny,                 SunnyScenes.class,        R.drawable.aaa_sunny,           R.drawable.cm_sunny,        R.string.sunny_tip,                R.drawable.ww_sunny,                    R.drawable.wb_sunny),
    CLOUDY                        (1, R.string.weather_cloudy,                          R.drawable.wd_cloudy,                R.drawable.ad_cloudy,                CloudySecenes.class,      R.drawable.aaa_cloudy,          R.drawable.cm_cloudy,       R.string.cloudy_tip,                R.drawable.ww_cloudy,                   R.drawable.wb_cloudy),
    OVERCAST                      (2, R.string.weather_overcast,                        R.drawable.wd_overcast,              R.drawable.ad_overcast,              OvercastSecenes.class,      R.drawable.aaa_cloudy,          R.drawable.cm_overcast,     R.string.overcast_tip,                R.drawable.ww_overcast,                 R.drawable.wb_overcast),
    /**/
    SHOWER                        (3, R.string.weather_shower,                          R.drawable.wd_shower,                R.drawable.ad_shower,                HeavyRainScenes.class,         R.drawable.aaa_lightrain,       R.drawable.cm_rain,         R.string.shower_tip,                R.drawable.ww_shower,                   R.drawable.wb_shower),
    THUNDER_SHOWER                (4, R.string.weather_thunder_shower,                  R.drawable.wd_thundershower,         R.drawable.ad_thundershower,         ThunderShowerScenes.class,         R.drawable.aaa_lightrain,       R.drawable.cm_rain,         R.string.thunder_shower_tip,         R.drawable.ww_thundershower,            R.drawable.wb_thundershower),
    THUNDER_SHOWER_WITH_HAIL      (5, R.string.weather_thunder_shower_with_hail,        R.drawable.wd_thundershowerwithhail, R.drawable.ad_thundershowerwithhail, ThunderShowerWithHailScenes.class,         R.drawable.aaa_lightrain,       R.drawable.cm_rain,         R.string.thunder_shower_with_hail_tip,         R.drawable.ww_thundershowerwithhail,    R.drawable.wb_thundershowerwithhail),
    SLEET                         (6, R.string.weather_sleet,                           R.drawable.wd_sleet,                 R.drawable.ad_sleet,                 SleetScenes.class,         R.drawable.aaa_snow,            R.drawable.cm_rain,         R.string.sleet_tip,              R.drawable.ww_sleet,                    R.drawable.wb_sleet),
    /**/

    LIGHT_RAIN                    (7, R.string.weather_light_rain,                      R.drawable.wd_lightrain,             R.drawable.ad_lightrain,             LightRainScenes.class,         R.drawable.aaa_lightrain,       R.drawable.cm_rain,         R.string.light_rain_tip,              R.drawable.ww_lightrain,                R.drawable.wb_lightrain),
    MODERATE_RAIN                 (8, R.string.weather_moderate_rain,                   R.drawable.wd_moderaterain,          R.drawable.ad_moderaterain,          ModerateRainScenes.class,         R.drawable.aaa_lightrain,       R.drawable.cm_rain,         R.string.moderate_rain_tip,              R.drawable.ww_moderaterain,             R.drawable.wb_moderaterain),
    /**/
    HEAVY_RAIN                    (9, R.string.weather_heavy_rain,                      R.drawable.wd_moderaterain,          R.drawable.ad_moderaterain,          HeavyRainScenes.class,         R.drawable.aaa_lightrain,       R.drawable.cm_rain,         R.string.heavy_rain_tip,              R.drawable.ww_moderaterain,             R.drawable.wb_moderaterain),
    STORM                         (10, R.string.weather_storm,                          R.drawable.wd_shower,                R.drawable.ad_shower,                StormScenes.class,        R.drawable.aaa_lightrain,       R.drawable.cm_rain,         R.string.storm_tip,              R.drawable.ww_shower,                   R.drawable.wb_shower),
    HEAVY_STORM                   (11, R.string.weather_heavy_storm,                    R.drawable.wd_shower,                R.drawable.ad_shower,                HeavyStormScenes.class,   R.drawable.aaa_lightrain,       R.drawable.cm_rain,         R.string.heavy_storm_tip,            R.drawable.ww_shower,                   R.drawable.wb_shower),
    SEVERE_STORM                  (12, R.string.weather_severe_storm,                   R.drawable.wd_shower,                R.drawable.ad_shower,                SevereStormScenes.class,  R.drawable.aaa_lightrain,       R.drawable.cm_rain,         R.string.severe_storm_tip,            R.drawable.ww_shower,                   R.drawable.wb_shower),
    SNOW_FLURRY                   (13, R.string.weather_snow_flurry,                    R.drawable.wd_heavysnow,             R.drawable.ad_heavysnow,             ModerateSnowScenes.class, R.drawable.aaa_snow,            R.drawable.cm_snow,         R.string.snow_flurry_tip,                R.drawable.ww_heavysnow,                R.drawable.wb_heavysnow),
    LIGHT_SNOW                    (14, R.string.weather_light_snow,                     R.drawable.wd_snow,                  R.drawable.ad_snow,                  LightSnowScenes.class,    R.drawable.aaa_snow,            R.drawable.cm_snow,         R.string.light_snow_tip,                R.drawable.ww_snow,                     R.drawable.wb_snow),
    MODERATE_SNOW                 (15, R.string.weather_moderate_snow,                  R.drawable.wd_snow,                  R.drawable.ad_snow,                  ModerateSnowScenes.class, R.drawable.aaa_snow,            R.drawable.cm_snow,         R.string.moderate_snow_tip,                R.drawable.ww_snow,                     R.drawable.wb_snow),
    HEAVY_SNOW                    (16, R.string.weather_heavy_snow,                     R.drawable.wd_heavysnow,             R.drawable.ad_heavysnow,             HeavySnowScenes.class,    R.drawable.aaa_snow,            R.drawable.cm_snow,         R.string.heavy_snow_tip,         R.drawable.ww_heavysnow,                R.drawable.wb_heavysnow),
    SNOW_STORM                    (17, R.string.weather_snow_storm,                     R.drawable.wd_heavysnow,             R.drawable.ad_heavysnow,             SnowStormScenes.class,    R.drawable.aaa_snow,            R.drawable.cm_snow,         R.string.snow_storm_tip,         R.drawable.ww_heavysnow,                R.drawable.wb_heavysnow),
    FOGGY                         (18, R.string.weather_foggy,                          R.drawable.wd_foggy,                 R.drawable.ad_foggy,                 FoggyScenes.class,        R.drawable.aaa_foggy,           R.drawable.cm_overcast,     R.string.foggy_tip,       R.drawable.ww_foggy,                    R.drawable.wb_foggy),
    ICE_RAIN                      (19, R.string.weather_ice_rain,                       R.drawable.wd_icerain,               R.drawable.ad_icerain,               RainScenes.class,         R.drawable.aaa_lightrain,       R.drawable.cm_rain,         R.string.ice_rain_tip,            R.drawable.ww_icerain,                   R.drawable.wb_icerain),
    DUST_STORM                    (20, R.string.weather_dust_storm,                     R.drawable.wd_duststorm,             R.drawable.ad_duststorm,             DustStormScenes.class,    R.drawable.aaa_duststorm,       R.drawable.cm_dust,         R.string.dust_storm_tip,       R.drawable.ww_duststorm,                 R.drawable.wb_duststorm),
    LIGHT_TO_MODERATE_RAIN        (21, R.string.weather_light_to_moderate_rain,         R.drawable.wd_lightrain,             R.drawable.ad_lightrain,             ModerateRainScenes.class,         R.drawable.aaa_lightrain,       R.drawable.cm_rain,         R.string.light_to_moderate_rain_tip,              R.drawable.ww_lightrain,                 R.drawable.wb_lightrain),
    MODERATE_TO_HEAVY_RAIN        (22, R.string.weather_moderate_to_heavy_rain,         R.drawable.wd_moderaterain,          R.drawable.ad_moderaterain,          HeavyRainScenes.class,         R.drawable.aaa_lightrain,       R.drawable.cm_rain,         R.string.moderate_to_heavy_rain_tip,              R.drawable.ww_moderaterain,              R.drawable.wb_moderaterain),
    HEAVY_RAIN_TO_STORM           (23, R.string.weather_heavy_rain_to_storm,            R.drawable.wd_shower,                R.drawable.ad_shower,                StormScenes.class,         R.drawable.aaa_lightrain,       R.drawable.cm_rain,         R.string.heavy_rain_to_storm_tip,            R.drawable.ww_shower,                    R.drawable.wb_shower),
    STORM_TO_HEAVY                (24, R.string.weather_storm_to_heavy,                 R.drawable.wd_shower,                R.drawable.ad_shower,                StormScenes.class,         R.drawable.aaa_lightrain,       R.drawable.cm_rain,         R.string.storm_to_heavy_tip,            R.drawable.ww_shower,                    R.drawable.wb_shower),
    HEAVY_TO_SERVER_STORM         (25, R.string.weather_heavy_to_server_storm,          R.drawable.wd_shower,                R.drawable.ad_shower,                StormScenes.class,         R.drawable.aaa_lightrain,       R.drawable.cm_rain,         R.string.heavy_to_server_storm_tip,            R.drawable.ww_shower,                    R.drawable.wb_shower),
    LIGHT_TO_MODERATE_SNOW        (26, R.string.weather_light_to_moderate_snow,         R.drawable.wd_snow,                  R.drawable.ad_snow,                  ModerateSnowScenes.class,    R.drawable.aaa_snow,            R.drawable.cm_snow,         R.string.light_to_moderate_snow_tip,                R.drawable.ww_snow,                      R.drawable.wb_snow),
    MODERATE_SNOW_TO_MODERATE_SNOW(27, R.string.weather_moderate_snow_to_heavy_snow,         R.drawable.wd_heavysnow,             R.drawable.ad_heavysnow,             HeavySnowScenes.class,    R.drawable.aaa_snow,            R.drawable.cm_snow,         R.string.moderate_snow_to_moderate_snow_tip,         R.drawable.ww_heavysnow,                 R.drawable.wb_heavysnow),
    SNOW_HEAVY_SNOW_TO_SNOW_STORM (28, R.string.weather_snow_heavy_snow_to_snow_storm,       R.drawable.wd_heavysnow,             R.drawable.ad_heavysnow,             SnowStormScenes.class,    R.drawable.aaa_snow,            R.drawable.cm_snow,         R.string.snow_heavy_snow_to_snow_storm_tip,         R.drawable.ww_heavysnow,                 R.drawable.wb_heavysnow),
    DUST                          (29, R.string.weather_dust,            R.drawable.wd_dust,                  R.drawable.ad_dust,                  DustScenes.class,         R.drawable.aaa_duststorm,       R.drawable.cm_dust,         R.string.dust_tip,       R.drawable.ww_dust,                      R.drawable.wb_dust),
    SAND                          (30, R.string.weather_sand,                           R.drawable.wd_sand,                  R.drawable.ad_sand,                  DustScenes.class,         R.drawable.aaa_duststorm,       R.drawable.cm_dust,         R.string.sand_tip,       R.drawable.ww_sand,                      R.drawable.wb_sand),
    SAND_STORM                    (31, R.string.weather_sand_storm,                     R.drawable.wd_sandstorm,             R.drawable.ad_sandstorm,             SandStormScenes.class,    R.drawable.aaa_duststorm,       R.drawable.cm_dust,         R.string.sand_storm_tip,       R.drawable.ww_sandstorm,                 R.drawable.wb_sandstorm),
    DENSE_FOGGY                   (32, R.string.weather_dense_foggy,                    R.drawable.wd_foggy,                 R.drawable.ad_foggy,                 FoggyScenes.class,        R.drawable.aaa_foggy,           R.drawable.cm_overcast,     R.string.dense_foggy_tip,       R.drawable.ww_foggy,                     R.drawable.wb_foggy),
    SNOW                          (33, R.string.weather_snow,                           R.drawable.wd_snow,                  R.drawable.ad_snow,                  ModerateSnowScenes.class,    R.drawable.aaa_snow,            R.drawable.cm_snow,         R.string.snow_tip,       R.drawable.ww_snow,                      R.drawable.wb_snow),
    MODERATE_FOGGY                (49, R.string.weather_moderate_foggy,                 R.drawable.wd_foggy,                 R.drawable.ad_foggy,                 FoggyScenes.class,        R.drawable.aaa_foggy,           R.drawable.cm_overcast,     R.string.moderate_foggy_tip,       R.drawable.ww_foggy,                     R.drawable.wb_foggy),
    HAZE                          (53, R.string.weather_haze,                           R.drawable.wd_haze,                  R.drawable.ad_haze,                  HazeScenes.class,         R.drawable.aaa_foggy,           R.drawable.cm_haze,         R.string.haze_tip,       R.drawable.ww_haze,                      R.drawable.wb_haze),
    MODERATE_HAZE                 (54, R.string.weather_moderate_haze,                  R.drawable.wd_haze,                  R.drawable.ad_haze,                  HazeScenes.class,         R.drawable.aaa_foggy,           R.drawable.cm_haze,         R.string.moderate_haze_tip,       R.drawable.ww_haze,                      R.drawable.wb_haze),
    HEAVY_HAZE                    (55, R.string.weather_heavy_haze,                  R.drawable.wd_haze,                  R.drawable.ad_haze,                  HazeScenes.class,         R.drawable.aaa_foggy,           R.drawable.cm_haze,         R.string.heavy_haze_tip,       R.drawable.ww_haze,                      R.drawable.wb_haze),
    SEVERE_HAZE                   (56, R.string.weather_severe_haze,                    R.drawable.wd_haze,                  R.drawable.ad_haze,                  HazeScenes.class,         R.drawable.aaa_foggy,           R.drawable.cm_haze,         R.string.severe_haze_tip,       R.drawable.ww_haze,                      R.drawable.wb_haze),
    HEAVY_FOGGY                   (57, R.string.weather_heavy_foggy,                    R.drawable.wd_foggy,                 R.drawable.ad_foggy,                 FoggyScenes.class,        R.drawable.aaa_foggy,           R.drawable.cm_overcast,     R.string.heavy_foggy_tip,       R.drawable.ww_foggy,                     R.drawable.wb_foggy),
    SEVERE_FOGGY                  (58, R.string.weather_severe_foggy,                   R.drawable.wd_foggy,                 R.drawable.ad_foggy,                 FoggyScenes.class,        R.drawable.aaa_foggy,           R.drawable.cm_overcast,     R.string.severe_foggy_tip,       R.drawable.ww_foggy,                     R.drawable.wb_foggy),
    UNKNOWN                       (99, R.string.weather_unknown,                        R.drawable.default_weather_icon,     R.drawable.ad_sunny,                 NormalSecenes.class,      R.drawable.aaa_weather,         R.drawable.cm_sunny,        R.string.unknown_tip,            R.drawable.ww_sunny,                     R.drawable.wb_sunny);



    private static StatusWeather sDebugWeather;

    public static void showWeatherchecker(Activity activity) {
        if (CommonUtils.IS_DEBUG) {
            final StatusWeather[] weathers = StatusWeather.values();
            String strs[] = new String[weathers.length];
            Dialog dialog = new Dialog(activity);
            ListView listView = new ListView(activity);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            for (int i = strs.length - 1; i >= 0; i--) {
                strs[i] = activity.getResources().getString(weathers[i].resId);
            }
            listView.setAdapter(new ArrayAdapter<String>(activity,android.R.layout.simple_list_item_1,strs));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    sDebugWeather = weathers[position];
                    dialog.hide();
                }
            });
            dialog.addContentView(listView,lp);
            dialog.show();
        }
    }

    final int key;
    final int resId;
    final int drawable5DayId;
    final int drawable24HourId;
    final Class<?> scenes;
    final int dynamicIconId;
    final int backgroundID;
    final int weatherTipID;
    final int whiteWidgetIconID;
    final int blackWidgetIconID;

    StatusWeather(int key, int resId, int drawable5DayId, int drawable24HourId, Class<?> scenes, int dynamicIconId,
                  int backgroundID, int weatherTipID, int whiteWidgetIconID, int blackWidgetIconID) {
        if (scenes == null)
            scenes = NormalSecenes.class;
        this.key = key;
        this.resId = resId;
        this.drawable5DayId = drawable5DayId;
        this.drawable24HourId = drawable24HourId;
        this.scenes = scenes;
        this.dynamicIconId = dynamicIconId;
        this.backgroundID = backgroundID;
        this.weatherTipID = weatherTipID;
        this.whiteWidgetIconID = whiteWidgetIconID;
        this.blackWidgetIconID = blackWidgetIconID;
    }

    private static int getWeatherNo(String no) {
        if (CommonUtils.IS_DEBUG && null != sDebugWeather) {
            return sDebugWeather.key;
        } else if (!TextUtils.isEmpty(no)) {
            return Integer.parseInt(no);
        }
        return -1;
    }


    public static String getWeatherStatus(String no) {
        try {
            int intNo = getWeatherNo(no);
            for (StatusWeather ws : StatusWeather.values()) {
                if (ws.key == intNo) {
                    return WeatherCNApplication.getWeatherCnApplication().getResources().getString(ws.resId);
                }
            }
        } catch (Exception e) {
            LogUtils.e("StatusWeather", "getWeatherStatus", e);
        }
        //if weather no is empty or is not a digit, return "--"
        return "--";
    }

    public static int getWeather5DayIconByNo(String no) {
        try {
            int intNo = getWeatherNo(no);
            for (StatusWeather ws : StatusWeather.values()) {
                if (ws.key == intNo) {
                    return ws.drawable5DayId;
                }
            }
        } catch (Exception e) {
            LogUtils.e("StatusWeather", "getWeatherIconByNo", e);
        }
        return R.drawable.default_weather_icon;
    }

    public static int getWeather24HourIconByNo(String no) {
        try {
            int intNo = getWeatherNo(no);
            for (StatusWeather ws : StatusWeather.values()) {
                if (ws.key == intNo) {
                    return ws.drawable24HourId;
                }
            }
        } catch (Exception e) {
            LogUtils.e("StatusWeather", "getWeatherIconByNo", e);
        }
        return R.drawable.ad_sunny;
    }

    public static int getWeatherBackgroundNo(String no) {
        try {
            int intNo = getWeatherNo(no);
            for (StatusWeather ws : StatusWeather.values()) {
                if (ws.key == intNo) {
                    return ws.backgroundID;
                }
            }
        } catch (Exception e) {
            LogUtils.e("StatusWeather", "getWeatherBackgroundNo", e);
        }
        return R.drawable.cm_overcast;
    }

    public static Class<?> getWeaatherScences(String no) {
        try {
            int intNo = getWeatherNo(no);
            for (StatusWeather ws : StatusWeather.values()) {
                if (ws.key == intNo) {
                    return ws.scenes;
                }
            }
        } catch (Exception e) {
            LogUtils.e("StatusWeather", "getWeaatherScences", e);
        }
        return NormalSecenes.class;
    }

    public static String getWeatherStatusTipByNo(String no) {
        try {
            int intNo = getWeatherNo(no);
            for (StatusWeather ws : StatusWeather.values()) {
                if (ws.key == intNo) {
                    return WeatherCNApplication.getWeatherCnApplication().getResources().getString(ws.weatherTipID);
                }
            }
        } catch (Exception e) {
            LogUtils.e("StatusWeather", "getWeatherStatusTipByNo", e);
        }
        //if weather no is empty or is not a digit, return "--"
        return "--";
    }

    public static int getWidgetwhiteIconByNo(String no) {
        try {
            int intNo = getWeatherNo(no);
            for (StatusWeather ws : StatusWeather.values()) {
                if (ws.key == intNo) {
                    return ws.whiteWidgetIconID;
                }
            }
        } catch (Exception e) {
            LogUtils.e("StatusWeather", "getWidgetwhiteIconByNo", e);
        }
        return R.drawable.ww_sunny;
    }

    public static int getWidgetBlackIconByNo(String no) {
        try {
            int intNo = getWeatherNo(no);
            for (StatusWeather ws : StatusWeather.values()) {
                if (ws.key == intNo) {
                    return ws.blackWidgetIconID;
                }
            }
        } catch (Exception e) {
            LogUtils.e("StatusWeather", "getWidgetBlackIconByNo", e);
        }
        return R.drawable.wb_sunny;
    }

    public static int getDynamicIconByNo(String no) {
        try {
            int intNo = getWeatherNo(no);
            for (StatusWeather ws : StatusWeather.values()) {
                if (ws.key == intNo) {
                    return ws.dynamicIconId;
                }
            }
        } catch (Exception e) {
            LogUtils.e("StatusWeather", "getDynamicIconByNo", e);
        }
        return R.drawable.aaa_weather;
    }

    public static AbsScenes newWeatherScences(Class<?> cls, IPainterView painterView, BitmapManager bmpManager) {
        try {
            Constructor<?> constructor = cls.getDeclaredConstructor(IPainterView.class, BitmapManager.class);
            return (AbsScenes) constructor.newInstance(painterView, bmpManager);
        } catch (Exception e) {
            LogUtils.e("StatusWeather", "newWeatherScences", e);
        }
        return null;
    }
}
