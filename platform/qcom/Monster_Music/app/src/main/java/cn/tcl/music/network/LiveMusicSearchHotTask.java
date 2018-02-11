package cn.tcl.music.network;

import android.content.Context;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.model.live.LiveMusicSearchHotBean;
import cn.tcl.music.util.LogUtil;


/**
 * Created by xiangxiang.liu on 2015/11/5.
 */
public class LiveMusicSearchHotTask extends RequestSongTask {
    private static final String TAG = LiveMusicSearchHotTask.class.getSimpleName();
    private ILoadData mListener;
    private String mLimit = "48";


    public LiveMusicSearchHotTask(Context context, ILoadData listener) {
        super(DataRequest.Method.METHOD_LIVE_SEARCH_HOT_WORDS, context.getApplicationContext(), LiveMusicSearchHotBean.class);
        mListener = listener;
    }

    @Override
    public HashMap<String, Object> getParams() {
        HashMap<String, Object> values = new HashMap<>();
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
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_HOT_SEARCH, mContext.getString(R.string.load_fail));
                return;
            }
            if (baseSong.returnCode == 0) {
                List<LiveMusicSearchHotBean> list = new ArrayList<>();
                list.add((LiveMusicSearchHotBean) baseSong);
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_HOT_SEARCH, list);
            } else {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_HOT_SEARCH, baseSong.returnMessage);
            }
        } else {
            LogUtil.d(TAG, "mListener is null");
        }
    }
}
