/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.internet.requester;


import android.text.TextUtils;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.leon.tools.IStringBean;
import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;

import cn.tcl.weather.internet.IWeatherNetHolder;
import cn.tcl.weather.internet.ServerConstant;
import cn.tcl.weather.utils.Debugger;
import cn.tcl.weather.utils.LogUtils;

/**
 * Created on 16-8-17.
 */
public abstract class BaseWeatherRequester<Bean extends IStringBean> implements Response.Listener<String>, Response.ErrorListener {

    private static final String TAG = BaseWeatherRequester.class.getName();

    private final static int REQUEST_COUNT = 3;

    public static final int STATE_FAILED_NETWORK = 1;
    public static final int STATE_FAILED_REFLECT = 2;
    public static final int STATE_FAILED_PARSER = 3;


    private RequestQueue mRequestQueue;
    private IStringRequest mRequester;
    protected HashMap<String, String> mParams = new HashMap<>(8);
    private String mType;

    private int i = 0;

    private IWeatherNetHolder.IWeatherNetCallback mCb;

    /**
     * constructor to int request type and request queue
     *
     * @param type
     * @param requestQueue
     */
    public BaseWeatherRequester(RequestQueue requestQueue, String type, IWeatherNetHolder.IWeatherNetCallback cb) {
        mType = type;
        mRequestQueue = requestQueue;
        mCb = cb;
    }

    /**
     * reset request
     *
     * @return
     */
    public BaseWeatherRequester resetRequest() {
        mParams.clear();
        if (!TextUtils.isEmpty(mType))
            addParam(ServerConstant.TYPE, mType);
        addParam(ServerConstant.DATE, ServerConstant.createCurrentDateYYYYMMDDHHSS());
        addParam(ServerConstant.APP_ID, ServerConstant.APP_ID_VALUE_KEY);
        return this;
    }


    /**
     * add request param
     * <p/>
     *
     * @param key
     * @param value
     * @return
     */
    public BaseWeatherRequester addParam(String key, String value) {
        mParams.put(key, value);
        return this;
    }

    public String getParamByKey(String key) {
        String value = mParams.get(key);
        return value == null ? "" : value;
    }

    /**
     * add requester to request queue and send request to server
     */
    public void request() {
        final String publicKey = ServerConstant.buildUrl(getRootUrl(), mParams);
        addParam(ServerConstant.KEY, ServerConstant.createSignature(publicKey));
        addParam(ServerConstant.APP_ID, ServerConstant.APP_ID_VALUE);
        mRequester = new IStringRequest(ServerConstant.buildUrl(getRootUrl(), mParams), this, this);
        LogUtils.i(TAG, "request: " + mRequester.getUrl());
        i = 0;
        mRequestQueue.add(mRequester);
    }

    protected String getRootUrl() {
        return ServerConstant.SERVER_URL_DATA;
    }

    private void rerequest() {
        if (null != mRequester) {
            mRequestQueue.add(mRequester);
            LogUtils.i(TAG, "rerequest: " + mRequester.getUrl());
        }
    }

    private Bean newBean() {
        try {
            Bean bean;
            ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
            Class<?> cls = (Class<?>) type.getActualTypeArguments()[0];
            Constructor<?> constructor = cls.getDeclaredConstructor();
            constructor.setAccessible(true);
            bean = (Bean) constructor.newInstance();
            constructor.setAccessible(false);
            return bean;
        } catch (Exception e) {

        }
        return null;
    }


    @Override
    public final void onErrorResponse(VolleyError volleyError) {
        onFailed(STATE_FAILED_NETWORK, volleyError.getMessage());
    }

    @Override
    public final void onResponse(String s) {
        LogUtils.i(TAG, "onResponse : " + mRequester.getUrl() + " \ndata:" + s);
        mErrorBean.reset();
        mErrorBean.parserString(s);
        if (ERR_STATUS_NULL == mErrorBean.status) {
            Bean bean = newBean();
            if (null != bean) {
                if (bean.parserString(s)) {
                    onSucceed(bean);
                } else {
                    onFailed(STATE_FAILED_PARSER, "");
                }
            } else {
                onFailed(STATE_FAILED_REFLECT, "");
            }
        } else {
            onFailed(mErrorBean.msg, s);
        }
    }

    protected void onSucceed(final Bean bean) {
        mCb.onReceivedData(mType, bean);
    }

    protected void onFailed(int state, String msg) {
        Debugger.DEBUG_D(true, TAG, "rerequest failed: ", mRequester.getUrl(), " msg:", msg);
        if ((++i) >= REQUEST_COUNT) {
            mCb.onFailed(mType, state);
        } else {
            rerequest();
        }
    }

    private final static int ERR_STATUS_NULL = 0;

    private ErrorBean mErrorBean = new ErrorBean();

    private class ErrorBean extends JSONBean {

        @JSONBeanField(name = "status")
        private int status = ERR_STATUS_NULL;

        @JSONBeanField(name = "msg")
        private int msg;

        private void reset() {
            status = ERR_STATUS_NULL;
            msg = 0;
        }

    }
}
