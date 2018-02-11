package cn.tcl.music.network;

import android.content.Context;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.model.live.LiveMusicSearchAllBean;
import cn.tcl.music.util.LogUtil;


/**
 * Created by xiangxiang.liu on 2015/11/5.
 */
public class LiveMusicSearchAllTask extends RequestSongTask {
    private static final String TAG = LiveMusicSearchAllTask.class.getSimpleName();
    private ILoadData mListener;
    private String mKey;
    private String mLimit = "20";


    public LiveMusicSearchAllTask(Context context, ILoadData listener, String key) {
        super(DataRequest.Method.METHOD_LIVE_SEARCH_ALL, context.getApplicationContext(), LiveMusicSearchAllBean.class);
        mListener = listener;
        mKey = key;
    }

    @Override
    public HashMap<String, Object> getParams() {
        HashMap<String, Object> values = new HashMap<>();
        values.put("key", mKey);
        values.put("limit", mLimit);
        return values;
    }

    @Override
    public void postInBackground(BaseSong baseSong) {

    }

    @Override
    protected void onPostExecute(BaseSong baseSong) {
        super.onPostExecute(baseSong);
        if (mListener != null) {
            if (baseSong == null) {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_ALL_SEARCH, mContext.getString(R.string.load_fail));
                return;
            }
            if (baseSong.returnCode == 0) {
                List<LiveMusicSearchAllBean> list = new ArrayList<>();
                list.add((LiveMusicSearchAllBean) baseSong);
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_ALL_SEARCH, list);
            } else {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_ALL_SEARCH, baseSong.returnMessage);
            }
        } else {
            LogUtil.d(TAG, "mListener is null");
        }
    }
}
