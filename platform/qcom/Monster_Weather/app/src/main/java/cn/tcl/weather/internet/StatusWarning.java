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

import cn.tcl.weather.R;
import cn.tcl.weather.WeatherCNApplication;
import cn.tcl.weather.bean.CityWeatherWarning;
import cn.tcl.weather.utils.CommonUtils;
import cn.tcl.weather.utils.LogUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * created on 16-8-31.
 */
public enum StatusWarning {
    TYPHOON("01", R.string.warning_typhoon, R.color.clr_warning_typhoon),
    RAINSTORM("02", R.string.warning_rainstorm, R.color.clr_warning_rainstorm),
    BLIZZARD("03", R.string.warning_blizzard, R.color.clr_blizzard),
    COLD_WAVE("04", R.string.warning_cold_wave, R.color.clr_cold),
    GUSTY_WINDS("05", R.string.warning_gusty_winds, R.color.clr_gusty_winds),
    SANDSTORM("06", R.string.warning_sandstorm, R.color.clr_sand_storm),
    HIGH_TEMPERATURE("07", R.string.warning_high_temperature, R.color.clr_warning_high_temperature),
    DROUGHT("08", R.string.warning_drought, R.color.clr_drought),
    THUNDER("09", R.string.warning_thunder, R.color.clr_thunder),
    HAIL("10", R.string.warning_hail, R.color.clr_warning_hail),
    FROST("11", R.string.warning_frost, R.color.clr_frost),
    HEAVY_FOGGY("12", R.string.warning_heavy_foggy, R.color.clr_warning_heavy_foggy),
    HAZE("13", R.string.warning_haze, R.color.clr_normal_haze),
    ICY_ROAD("14", R.string.warning_icy_road, R.color.clr_warning_icy_road),
    GD_RAINSTORM("gd01", R.string.gd_rainstorm, R.color.clr_warning_rainstorm),
    GD_HIGH_TEMPERATURE("gd02", R.string.gd_high_temperature, R.color.clr_warning_high_temperature),
    GD_HAIL("gd03", R.string.gd_hail, R.color.clr_warning_hail),
    GD_HEAVY_FOGGY("gd04", R.string.gd_heavy_foggy, R.color.clr_warning_heavy_foggy),
    GD_ICY_ROAD("gd05", R.string.gd_icy_road, R.color.clr_warning_icy_road),
    GD_HAZE("gd06", R.string.gd_haze, R.color.clr_warning_haze),
    GD_COLD_WAVE("gd07", R.string.gd_cold_wave, R.color.clr_warning_cold_wave),
    GD_THUNDERSTORM_GUSTY_WINDS("gd08", R.string.gd_thunderstorm_gusty_winds, R.color.clr_thunderstorm_gusty_winds),
    GD_FOREST_FIRE("gd09", R.string.gd_forest_fire, R.color.clr_warning_forest_fire),
    GD_TYPHOON("gd10", R.string.gd_typhoon, R.color.clr_warning_typhoon),

    NONSTANDARD_COLD_WAVE(WeatherCNApplication.getWeatherCnApplication().getResources().getString(R.string.gd_cold_wave), R.string.gd_cold_wave, R.color.clr_warning_cold_wave),
    NONSTANDARD_HAZE(WeatherCNApplication.getWeatherCnApplication().getResources().getString(R.string.gd_haze), R.string.gd_haze, R.color.clr_warning_haze),
    NONSTANDARD_THUNERSTORM_GUSTY_WINDS(WeatherCNApplication.getWeatherCnApplication().getResources().getString(R.string.gd_thunderstorm_gusty_winds), R.string.gd_thunderstorm_gusty_winds, R.color.clr_thunderstorm_gusty_winds),
    NONSTANDARD_FOREST_FIRE(WeatherCNApplication.getWeatherCnApplication().getResources().getString(R.string.gd_forest_fire), R.string.gd_forest_fire, R.color.clr_warning_forest_fire),
    NONSTANDARD_LOWER_TEMPERATURE(WeatherCNApplication.getWeatherCnApplication().getResources().getString(R.string.lower_temperature), R.string.lower_temperature, R.color.clr_lower_temperature),
    NONSTANDARD_ICY_ROAD(WeatherCNApplication.getWeatherCnApplication().getResources().getString(R.string.warning_icy_road), R.string.warning_icy_road, R.color.clr_warning_icy_road),
    NONSTANDARD_DRY_AND_HOT_WIND(WeatherCNApplication.getWeatherCnApplication().getResources().getString(R.string.dry_and_hot_wind), R.string.dry_and_hot_wind, R.color.clr_dry_and_hot_wind),
    NONSTANDARD_LOW_TEMPERATURE(WeatherCNApplication.getWeatherCnApplication().getResources().getString(R.string.low_temperature), R.string.low_temperature, R.color.clr_low_temperature),
    NONSTANDARD_FROZEN(WeatherCNApplication.getWeatherCnApplication().getResources().getString(R.string.frozen), R.string.frozen, R.color.clr_frozen);

    public static StatusWarning sDebugWarning;

    public static void showWarningChecker(Activity activity) {
        if (CommonUtils.IS_DEBUG) {
            final StatusWarning[] warnings = StatusWarning.values();
            String strs[] = new String[warnings.length];
            Dialog dialog = new Dialog(activity);
            ListView listView = new ListView(activity);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            for (int i = strs.length - 1; i >= 0; i--) {
                strs[i] = activity.getResources().getString(warnings[i].warningStrID);
            }
            listView.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, strs));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    sDebugWarning = warnings[position];
                    dialog.hide();
                }
            });
            dialog.addContentView(listView, lp);
            dialog.show();
        }
    }


    public final String warningNo;
    public final int warningStrID;
    private int warningTextColorID;
    static String TAG = "StatusWarning";
    private static final String NONSTANDARD_WARNING = "00";

    StatusWarning(String warningNo, int warningStrID, int warningTextColorID) {
        this.warningNo = warningNo;
        this.warningStrID = warningStrID;
        this.warningTextColorID = warningTextColorID;
    }

    public static String getWarningStrByNo(String warningNo) {
        try {
            if (!TextUtils.isEmpty(warningNo)) {
                for (StatusWarning sw : StatusWarning.values()) {
                    if (sw.warningNo.equals(warningNo)) {
                        return WeatherCNApplication.getWeatherCnApplication().getResources().getString(sw.warningStrID);
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "getWarningStrByNo", e);
        }
        //if warning no is empty or is not a digit , return "--"
        return "--";
    }

    /**
     * Get warn text color id by warnNO or warnCategoryName
     *
     * @param info warning information
     * @return The color
     */
    public static int getWarningColorByInfo(CityWeatherWarning info) {

        int colorID = R.color.clr_warning_default;
        try {
            if (!TextUtils.isEmpty(info.warnNo)) {
                // If the warnNo is "00", its type is nonstandardly international
                // So we get its color by category name
                if (info.warnNo.equals(NONSTANDARD_WARNING)) {
                    colorID = getWarningByWarnCategoryName(info.getWarnCategoryName());
                } else { // If warnNo isn't "00", its type is guangdong or standardly international
                    colorID = getWarningByWarnNo(info.warnNo);
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Get warning color failed", e);
        }

        int color = WeatherCNApplication.getWeatherCnApplication().getResources().getColor(colorID);
        return color;
    }


    /**
     * Find color id
     *
     * @param warnCategoryName the warnCategoryName
     * @return color id
     */
    private static int getWarningByWarnCategoryName(String warnCategoryName) {
        WeatherCNApplication app = WeatherCNApplication.getWeatherCnApplication();
        for (StatusWarning sw : StatusWarning.values()) {
            String name = app.getResources().getString(sw.warningStrID);
            if (name.equals(warnCategoryName)) {
                return sw.warningTextColorID;
            }
        }
        return R.color.clr_warning_default;
    }


    /**
     * Find color id
     *
     * @param warnNo the warnNo
     * @return color id
     */
    private static int getWarningByWarnNo(String warnNo) {

        for (StatusWarning sw : StatusWarning.values()) {
            if (sw.warningNo.equals(warnNo)) {
                return sw.warningTextColorID;
            }
        }
        return R.color.clr_warning_default;
    }


//    public String getWarningText(Activity activity) {
//        return activity.getResources().getString(warningStrID);
//    }
//
//    public int getWaringTextColor(Activity activity) {
//        return activity.getResources().getColor(warningTextColorID);
//    }
}