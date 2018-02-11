package cn.tcl.weather.viewhelper;

import java.lang.reflect.Constructor;

import cn.tcl.weather.WeatherCNApplication;
import cn.tcl.weather.utils.LogUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-10-10.
 * $desc
 */
public enum VhFactory {

    TCL_VH(WeatherCNApplication.SYSTEM_TYPE_LONDON, OtherFirstRunVh.class, OtherMainActivityVh.class, OtherMainInternalCityWeatherVh.class, TclAddCityVh.class, TclWeatherWarningVh.class, TclWeatherCityManagerVh.class, OtherMainExternalCityWeatherVh.class),
    OTHER_VH(WeatherCNApplication.SYSTEM_TYPE_NULL, OtherFirstRunVh.class, OtherMainActivityVh.class, OtherMainInternalCityWeatherVh.class, OtherAddCityVh.class, OtherWeatherWarningVh.class, OtherWeatherCityManagerVh.class, OtherMainExternalCityWeatherVh.class);


    private final static String TAG = "VhFactory";

    private static VhFactory mCurrentVhFactory;


    public final static int FIRST_RUN_VH = 0;
    public final static int MAIN_ACTIVITY_VH = 1;
    public final static int MAIN_INTERNAL_CITY_WEATHER_VH = 2;
    public final static int ADD_CITY_VH = 3;
    public final static int CITY_WARNING_VH = 4;
    public final static int CITY_MANAGER_VH = 5;
    public final static int MAIN_EXTERNAL_CITY_WEATHER_VH = 6;


    private final int mSystemType;
    private final Class<?>[] mVhClses;

    private VhFactory(int systemType, Class<?>... cls) {
        mSystemType = systemType;
        mVhClses = cls;
    }

    public static <VH> VH newVhInstance(int vhType) {
        return newVhInstance(vhType, new Class<?>[0], new Object[0]);
    }

    public static <VH> VH newVhInstance(int vhType, Object... objects) {
        if (objects.length > 0) {
            Class<?>[] clses = new Class[objects.length];
            for (int i = objects.length - 1; i >= 0; i--) {
                clses[i] = objects[i].getClass();
            }
            return newVhInstance(vhType, clses, objects);
        }
        return newVhInstance(vhType);
    }


//    public static <VH> VH newVhInstance(int vhType, Class<?> cls, Object instance) {
//        return newVhInstance(vhType, new Class<?>[]{cls}, new Object[]{instance});
//    }


    public static <VH> VH newVhInstance(int vhType, Class<?>[] parameterTypes, Object[] parameters) {
        final int systemType = WeatherCNApplication.getWeatherCnApplication().getCurrentSystemType();
        VhFactory factory = mCurrentVhFactory;
        if (null == mCurrentVhFactory) {
            for (VhFactory vhFactory : VhFactory.values()) {
                if (vhFactory.mSystemType == systemType) {
                    factory = vhFactory;
                    break;
                }
            }
            mCurrentVhFactory = factory;
        }

        if (null != factory && vhType < factory.mVhClses.length) {
            try {
                Class<?> cls = factory.mVhClses[vhType];
                if (null != parameterTypes && parameterTypes.length > 0) {
                    Constructor<?> constructor = cls.getDeclaredConstructor(parameterTypes);
                    return (VH) constructor.newInstance(parameters);
                } else {
                    return (VH) cls.newInstance();
                }
            } catch (Exception e) {
                LogUtils.e(TAG, "newVhInstance failed: ", e);
            }
        }
        return null;
    }
}
