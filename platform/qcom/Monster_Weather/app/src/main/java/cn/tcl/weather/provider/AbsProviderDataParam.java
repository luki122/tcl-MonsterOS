/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.provider;

/**
 * Created by thundersoft on 16-7-28.
 */
public abstract class AbsProviderDataParam extends DataParam {

    private RequestError mCancelErr;

    final void dataScan(ProviderDataService service) {
        if (null == mCancelErr) {
            onDataScan(service);
        } else {
            service.requestCallback(this, mCancelErr);
            mCancelErr = null;
        }
    }

    protected abstract void onDataScan(ProviderDataService service);

    @Override
    protected void requestDataCallback(RequestError err) {
        super.requestDataCallback(err);
    }

    /**
     * cancel the request
     */
    public final void cancelRequest() {
        mCancelErr = new RequestError(RequestError.ERR_TYPE_CANCEL, RequestError.ERR_CANCEL_STR);
    }


}
