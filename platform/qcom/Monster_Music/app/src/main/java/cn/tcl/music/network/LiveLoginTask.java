package cn.tcl.music.network;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.model.live.LiveLoginBean;
import cn.tcl.music.util.LogUtil;

public class LiveLoginTask extends RequestSongTask{
    private final String TAG = LiveLoginTask.class.getSimpleName();
    private String mTclToken;
    private ILoadData mListener;

    public LiveLoginTask(String requestMethod, Context context, Class beanClass) {
        super(requestMethod, context, LiveLoginTask.class);
    }

    public LiveLoginTask(Context context, ILoadData listener, String tclToken){
        super(DataRequest.Method.METHOD_LIVE_ACCOUNT_LOGIN, context.getApplicationContext(), LiveLoginBean.class);
        mListener = listener;
        mTclToken = tclToken;
    }

    @Override
    protected HashMap<String, Object> getParams() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("token", mTclToken);
        return values;
    }

    @Override
    protected void postInBackground(BaseSong baseSong) {
        LogUtil.d(TAG,baseSong.toString());
        super.onPostExecute(baseSong);
        if (mListener != null) {
            if (baseSong == null) {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_ACCOUNT_LOGIN, mContext.getString(R.string.load_fail));
                return;
            }
            //请求成功
            if(baseSong.returnCode == 0){
                List<LiveLoginBean> list = new ArrayList<LiveLoginBean>();
                list.add((LiveLoginBean)baseSong);
                LogUtil.d(TAG,baseSong.toString());
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_ACCOUNT_LOGIN, list);
            } else{
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_ACCOUNT_LOGIN, baseSong.returnMessage);
            }
        } else{
            LogUtil.d(TAG, "mListener is null");
        }
    }


}
