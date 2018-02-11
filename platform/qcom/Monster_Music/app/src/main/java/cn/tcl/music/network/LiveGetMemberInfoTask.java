package cn.tcl.music.network;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.model.live.XiamiMemberInfo;
import cn.tcl.music.util.LogUtil;

public class LiveGetMemberInfoTask extends RequestSongTask{
    private final String TAG = LiveGetMemberInfoTask.class.getSimpleName();
    private ILoadData mListener;
    private long mUserId;

    public LiveGetMemberInfoTask(String requestMethod, Context context, Class beanClass) {
        super(requestMethod, context, beanClass);
    }

    public LiveGetMemberInfoTask(Context context, ILoadData listener, long userId){
        super(DataRequest.Method.METHOD_GET_MEMBER_INFO, context.getApplicationContext(), XiamiMemberInfo.class);
        mListener = listener;
        mUserId = userId;
    }

    @Override
    protected HashMap<String, Object> getParams() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("user_id", mUserId);
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
                List<XiamiMemberInfo> list = new ArrayList<XiamiMemberInfo>();
                list.add((XiamiMemberInfo)baseSong);
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
