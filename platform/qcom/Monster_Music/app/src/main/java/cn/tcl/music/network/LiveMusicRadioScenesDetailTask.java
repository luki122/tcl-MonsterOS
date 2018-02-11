package cn.tcl.music.network;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;

import cn.tcl.music.R;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.model.live.RadioDetailBean;
import cn.tcl.music.util.LogUtil;


/**
 * Created by dongdong.huang on 2015/11/25.
 * 获取电台详情
 */
public class LiveMusicRadioScenesDetailTask extends RequestSongTask {
    private static final String TAG = LiveMusicRadioDetailTask.class.getSimpleName();
    private ILoadData mListener;
    private String mId;
    private String mType;

    public LiveMusicRadioScenesDetailTask(Context context, ILoadData listener, String id, String type) {
        super(DataRequest.Method.METHOD_LIVE_RADIO_DETAIL, context.getApplicationContext(), RadioDetailBean.class);
        mListener = listener;
        mId = id;
        mType = type;
    }


    @Override
    protected HashMap<String, Object> getParams() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("object_id", mId);
        values.put("type", mType);
        values.put("limit", 20);
        return values;
    }

    @Override
    protected void postInBackground(BaseSong baseSong) {

    }

    @Override
    protected void onPostExecute(BaseSong baseSong) {
        super.onPostExecute(baseSong);
        if (mListener != null) {
            if (baseSong == null) {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_RADIO_DETAIL, mContext.getString(R.string.load_fail));
                return;
            }
            if (baseSong.returnCode == 0) {
                RadioDetailBean detail = (RadioDetailBean) baseSong;
                ArrayList list = new ArrayList<RadioDetailBean>();
                list.add(detail);
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_RADIO_DETAIL, list);
            } else {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_RADIO_DETAIL, baseSong.returnMessage);
            }
        } else {
            LogUtil.d(TAG, "mListener is null");
        }
    }
}
