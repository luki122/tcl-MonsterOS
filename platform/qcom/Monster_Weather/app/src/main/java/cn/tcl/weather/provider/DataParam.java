/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.provider;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created by thundersoft on 16-7-28.
 * <p>
 * Data acquisition param,by{@link IDataService#requestData(DataParam)} to request data
 */
public abstract class DataParam {


    private OnRequestDataListener mListener;


    /**
     * request call back
     *
     * @param l
     */
    public void setOnRequestDataListener(OnRequestDataListener l) {
        mListener = l;
    }

    /**
     * evoked by when call back
     *
     * @param err
     */
    protected void requestDataCallback(RequestError err) {
        if (null != mListener) {
            if (null == err) {
                mListener.onRequestDataCallback(this);
            } else {
                mListener.onRequestDataFailed(this, err);
            }
        }
    }


    /**
     * DataParam listener
     */
    public static interface OnRequestDataListener {

        /**
         * when data return success , evoke this method
         *
         * @param param
         */
        void onRequestDataCallback(DataParam param);

        /**
         * @param param
         * @param error
         */
        void onRequestDataFailed(DataParam param, RequestError error);
    }


    public static class RequestError {
        public final static int ERR_TYPE_CANCEL = 1;
        public final static int ERR_TYPE_NO_PARAM = 2;
        public final static int ERR_TYPE_DB_ERR = 3;
        public final static int ERR_TYPE_DB_NULL = 4;

        public final static String ERR_CANCEL_STR = "request canceled";
        public final static String ERR_NO_PARAM_STR = "no param";
        public final static String ERR_TYPE_DB_NULL_STR = "no such item in db";

        public final int type;
        public final String msg;

        public RequestError(int type, String msg) {
            this.type = type;
            this.msg = msg;
        }
    }

}
