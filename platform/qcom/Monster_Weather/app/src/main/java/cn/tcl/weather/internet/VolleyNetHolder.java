/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.internet;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import cn.tcl.weather.bean.City;
import cn.tcl.weather.internet.requester.CityObserveRequester;
import cn.tcl.weather.internet.requester.CityWeather5DayRequester;
import cn.tcl.weather.internet.requester.CityWeatherAirRequester;
import cn.tcl.weather.internet.requester.CityWeatherGeoRequester;
import cn.tcl.weather.internet.requester.CityWeatherHourRequester;
import cn.tcl.weather.internet.requester.CityWeatherLifeIndexRequester;
import cn.tcl.weather.internet.requester.CityWeatherObserveRequester;
import cn.tcl.weather.internet.requester.CityWeatherPastRequester;
import cn.tcl.weather.internet.requester.CityWeatherWarnRequester;
import cn.tcl.weather.internet.requester.ObserveBeanArray;


/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-15.
 */

public class VolleyNetHolder implements IWeatherNetHolder {

    private static final String TAG = VolleyNetHolder.class.getName();


    private RequestQueue mRequestQueue;
    private Context mContext;

    private Handler mMainHandler;

    public VolleyNetHolder(Context context, Handler mainHandler) {
        mRequestQueue = Volley.newRequestQueue(context);
        mContext = context;
        mMainHandler = mainHandler;
    }

    @Override
    public void requestCityByCity(City city, String lang, IWeatherNetCallback cb) {
        if (city.hasRequestCityName()) {
            new RequestCityByNameHolder().request(IWeatherNetCallback.ACTION_REQUEST_CITY_LIST_BY_CITY, city.getRequestCityName(), lang, cb);
        }
    }

    @Override
    public void requestCityListByName(String name, String lang, IWeatherNetCallback cb) {
        new RequestCityByNameHolder().request(name, lang, cb);
    }

    @Override
    public void requestCityBylocationKey(String locationKey, String lang, IWeatherNetCallback cb) {
//        if (TextUtils.isEmpty(locationKey)) {
//            locationKey = "101090205";//for test
//        }
//        new RequestCityHolder().request(locationKey, lang, cb);
    }

    @Override
    public void requestCityByGeo(String lon, String lat, String lang, IWeatherNetCallback cb) {
        new RequestCityGeoHolder().request(lon, lat, lang, cb);
    }

    @Override
    public void init() {
//        try {
//            SSLContext sslContext = SSLContext.getInstance("TLS");
//            sslContext.int(null, new TrustManager[]{new AllTrustManager()}, new SecureRandom());
//            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
//            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifierALL());
//        } catch (Exception e) {
//        }
//  canceled https
        mRequestQueue = Volley.newRequestQueue(mContext);
        mRequestQueue.start();
    }

    @Override
    public void recycle() {
        if (null != mRequestQueue) {
            mRequestQueue.stop();
        }
    }

    @Override
    public void onTrimMemory(int level) {

    }


    private class RequestCityGeoHolder implements IWeatherNetCallback {

        private CityWeatherGeoRequester mCityGeoRequester = new CityWeatherGeoRequester(mRequestQueue, this);
        private RequestCityHolder mRequestCityHolder = new RequestCityHolder();
        private String mLang;
        private boolean isCityInfoRequest;
        private IWeatherNetCallback mCb;
        private String mAction = ACTION_REQUEST_CITY_BY_GEO;


        public void request(String lon, String lat, String lang, IWeatherNetCallback cb) {
            isCityInfoRequest = false;
            mLang = lang;
            mCb = cb;
            mCityGeoRequester.request(lon, lat);

        }

        public void request(String action, String lon, String lat, String lang, IWeatherNetCallback cb) {
            mAction = action;
            request(lon, lat, lang, cb);
        }

        @Override
        public void onReceivedData(String action, final Object obj) {
            if (isCityInfoRequest) {
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCb.onReceivedData(mAction, obj);
                    }
                });
            } else {
                isCityInfoRequest = true;
                CityWeatherGeoRequester.Bean bean = (CityWeatherGeoRequester.Bean) obj;
                mRequestCityHolder.request(bean.bean.areaId, mLang, this);
            }
        }

        @Override
        public void onFailed(String action, final int state) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCb.onFailed(mAction, state);
                }
            });
        }
    }

    private class RequestCityByNameHolder implements IWeatherNetCallback {

        private CityObserveRequester mCityObserveRequester = new CityObserveRequester(mRequestQueue, this);
        private RequestCityHolder mRequestCityHolder = new RequestCityHolder();
        private String mLang;
        private boolean isCityInfoRequest;
        private IWeatherNetCallback mCb;
        private String mAction = ACTION_REQUEST_CITY_LIST_BY_NAME;

        public void request(String cityName, String lang, IWeatherNetCallback cb) {
            isCityInfoRequest = false;
            mLang = lang;
            mCb = cb;
            mCityObserveRequester.request(cityName);
        }

        public void request(String action, String cityName, String lang, IWeatherNetCallback cb) {
            mAction = action;
            request(cityName, lang, cb);
        }

        @Override
        public void onReceivedData(String action, final Object obj) {
            if (isCityInfoRequest) {
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCb.onReceivedData(mAction, obj);
                    }
                });
            } else {
                isCityInfoRequest = true;
                ObserveBeanArray beanArray = (ObserveBeanArray) obj;
                ObserveBeanArray.BeanObserve bean = beanArray.getBeaObserve();
                mRequestCityHolder.request(bean.info.areaid, mLang, this);
            }
        }

        @Override
        public void onFailed(String action, final int state) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCb.onFailed(mAction, state);
                }
            });
        }
    }

    private class RequestCityHolder implements IWeatherNetCallback {
        private final static String TAG = "RequestCityHolder";
        private final static int STATE_NULL = 0;
        private final static int STATE_OBSERVE_OK = 1;
        private final static int STATE_7DAY_OK = 2;
        private final static int STATE_24_HOUR_OK = 4;
        private final static int STATE_AIR_OK = 8;
        private final static int STATE_LIFE_INDEX_OK = 16;
        private final static int STATE_WARN_OK = 32;
        private final static int STATE_PAST_WEATHER_OK = 64;

        private final static int STATE_ALL_OK = STATE_OBSERVE_OK | STATE_7DAY_OK | STATE_24_HOUR_OK | STATE_AIR_OK | STATE_LIFE_INDEX_OK | STATE_WARN_OK | STATE_PAST_WEATHER_OK;
        //private final static int STATE_ALL_OK = STATE_OBSERVE_OK | STATE_7DAY_OK | STATE_24_HOUR_OK | STATE_AIR_OK | STATE_LIFE_INDEX_OK | STATE_WARN_OK;

        //because external city only have the two state of : STATE_OBSERVE_OK and STATE_7DAY_OK
        private final static int STATE_EXTERNAL_OK = STATE_OBSERVE_OK | STATE_7DAY_OK;

        private CityWeatherObserveRequester mObserveRequester = new CityWeatherObserveRequester(mRequestQueue, this);

        private CityWeatherAirRequester mAirRequester = new CityWeatherAirRequester(mRequestQueue, this);

        private CityWeatherWarnRequester mWarnRequester = new CityWeatherWarnRequester(mRequestQueue, this);

        private CityWeather5DayRequester m7DayRequester = new CityWeather5DayRequester(mRequestQueue, this);

        private CityWeatherHourRequester mWeatherHourRequester = new CityWeatherHourRequester(mRequestQueue, this);

        private CityWeatherLifeIndexRequester mWeatherLifeIndexRequester = new CityWeatherLifeIndexRequester(mRequestQueue, this);

        private CityWeatherPastRequester mPastRequester = new CityWeatherPastRequester(mRequestQueue, this);

        private IWeatherNetCallback mCb;
        private int mState = STATE_NULL;
        private City mCity = new City();
        private String mLang;
        private String mAction = ACTION_REQUEST_CITY_BY_LOCATION_KEY;

        private int mCheckState = STATE_ALL_OK;

        public void request(String areaId, String lang, IWeatherNetCallback cb) {
            mState = STATE_NULL;
            mLang = lang;
            mCb = cb;
            mCity.getCityWeatherInfo().areaId = areaId;
            mCity.setLanguage(lang);

            if (!TextUtils.isDigitsOnly(areaId)) {// if areaid is not just digits, it is a external city
                mCheckState = STATE_EXTERNAL_OK;
                mCity.setCityType(City.CITY_TYPE_EXTERNAL);
                mObserveRequester.request(areaId);
                m7DayRequester.request(areaId);
            } else {
                mCheckState = STATE_ALL_OK;
                mCity.setCityType(City.CITY_TYPE_INTERNAL);
                mObserveRequester.request(areaId);
                mAirRequester.request(areaId);
                mWarnRequester.request(areaId);
                m7DayRequester.request(areaId);
                mWeatherHourRequester.request(areaId);
                mWeatherLifeIndexRequester.request(areaId);
                mPastRequester.request(areaId);
            }
        }

        public void request(String action, String locationKey, String lang, IWeatherNetCallback cb) {
            mAction = action;
            request(locationKey, lang, cb);
        }

        public void addState(int state) {
            if (state != mState) {
                mState |= state;
                if (mState == mCheckState) {
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // set the city type
                            mCb.onReceivedData(mAction, mCity);
                        }
                    });
                }
            }
        }

        @Override
        public void onReceivedData(String action, Object obj) {
            if (ServerConstant.TYPE_OBSERVE.equals(action)) {
                ObserveBeanArray beanArray = (ObserveBeanArray) obj;
                ObserveBeanArray.BeanObserve bean = beanArray.getBeaObserve();
                mCity.combineWeatherInfo(bean.l.getCityWeatherInfo(mLang));
                addState(STATE_OBSERVE_OK);
            } else if (ServerConstant.TYPE_AIR.equals(action)) {
                CityWeatherAirRequester.BeanAir bean = (CityWeatherAirRequester.BeanAir) obj;
                mCity.combineWeatherInfo(bean.p.getCityAirInfo(mLang));
                addState(STATE_AIR_OK);
            } else if (ServerConstant.TYPE_ALARM.equals(action)) {
                CityWeatherWarnRequester.BeanWarn bean = (CityWeatherWarnRequester.BeanWarn) obj;
                mCity.combineCityWeatherWarning(bean.getWarnInfos(mLang));
                addState(STATE_WARN_OK);
            } else if (ServerConstant.TYPE_FORECAST_5_DAY.equals(action)) {
                CityWeather5DayRequester.Bean7Day bean = (CityWeather5DayRequester.Bean7Day) obj;
                mCity.conbineCity(bean.beanC.getCity(mLang));
                mCity.getCityWeatherInfo().combineDayWeathers(bean.weather.getDayWeathers(mLang));
                addState(STATE_7DAY_OK);
            } else if (ServerConstant.TYPE_HOUR_FC.equals(action)) {
                CityWeatherHourRequester.BeanHour bean = (CityWeatherHourRequester.BeanHour) obj;
                mCity.getCityWeatherInfo().combineHourWeathers(bean.get24HourWeahters(mLang));
                addState(STATE_24_HOUR_OK);
            } else if (ServerConstant.TYPE_INDEX.equals(action)) {
                CityWeatherLifeIndexRequester.BeanLifeIndex bean = (CityWeatherLifeIndexRequester.BeanLifeIndex) obj;
                mCity.getCityWeatherInfo().combineCityLifeIndexInfos(bean.getLifeIndexInfo(mLang));
                addState(STATE_LIFE_INDEX_OK);
            } else if (ServerConstant.TYPE_PAST_WEATHER.equals(action)) {
                CityWeatherPastRequester.BeanPastWeather bean = (CityWeatherPastRequester.BeanPastWeather) obj;
                mCity.getCityWeatherInfo().combineYesterdayWeather(bean.getPastWeather(mLang));
                addState(STATE_PAST_WEATHER_OK);
            }
        }

        @Override
        public void onFailed(String action, final int state) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCb.onFailed(ACTION_REQUEST_CITY_BY_LOCATION_KEY, state);
                }
            });
        }
    }

}

/**
 * for https request
 */
class HostnameVerifierALL implements HostnameVerifier {
    @Override
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }

}

/**
 * for https request
 */
class AllTrustManager implements X509TrustManager {
    @Override
    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
    }

    @Override
    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
    }

    @Override
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return null;
    }

}
