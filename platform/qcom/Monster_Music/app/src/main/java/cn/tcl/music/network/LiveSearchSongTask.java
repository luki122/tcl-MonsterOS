package cn.tcl.music.network;

import android.content.Context;
import android.util.Log;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.model.live.LiveMusicSearchSongBean;
import cn.tcl.music.util.LogUtil;


public class LiveSearchSongTask extends RequestSongTask {
    private static final String TAG = LiveSearchSongTask.class.getSimpleName();
    private ILoadData mListener;
    private String mLimit = LiveMusicSearchSingerByKeywordTask.DEFAULT_PAGE_LIMIT;
    private String mKey;

    public LiveSearchSongTask(Context context, ILoadData listener, String key) {
        super(DataRequest.Method.METHOD_LIVE_SEARCH_SONG, context.getApplicationContext(), LiveMusicSearchSongBean.class);
        mListener = listener;
        mKey = key;
    }

    public LiveSearchSongTask(Context context) {
        super(DataRequest.Method.METHOD_LIVE_SEARCH_SONG, context.getApplicationContext(), LiveMusicSearchSongBean.class);
    }

    @Override
    public HashMap<String, Object> getParams() {
        HashMap<String, Object> values = new HashMap<>();
        values.put("key", mKey);
//        values.put("limit", mLimit);
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
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_SONG_DETAIL, mContext.getString(R.string.load_fail));
                return;
            }
            if (baseSong.returnCode == 0) {
                List<LiveMusicSearchSongBean> list = new ArrayList<LiveMusicSearchSongBean>();
                list.add((LiveMusicSearchSongBean) baseSong);
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_SONG_DETAIL, list);
            } else {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_SONG_DETAIL, baseSong.returnMessage);
            }
        } else {
            LogUtil.d(TAG, "mListener is null");
        }
    }
}
