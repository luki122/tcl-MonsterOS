package cn.tcl.music.network;

import android.content.Context;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.model.live.LiveMusicSearchMatchSongBean;
import cn.tcl.music.util.LogUtil;


/**
 * Created by zheng.ding on 2016/04/27.
 */
public class LiveMusicMatchSongByKeywordTask extends RequestSongTask {
    private static final String TAG = LiveMusicMatchSongByKeywordTask.class.getSimpleName();
    private ILoadData mListener;
    private String song_list;


    public LiveMusicMatchSongByKeywordTask(Context context, ILoadData listener, String key) {

        super(DataRequest.Method.METHOD_SEARCH_MATCH_SONGS, context.getApplicationContext(), LiveMusicSearchMatchSongBean.class);
        mListener = listener;
        song_list = key;
    }

    @Override
    public HashMap<String, Object> getParams() {
        HashMap<String, Object> values = new HashMap<>();
        values.put("song_list", song_list);
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
                mListener.onLoadFail(DataRequest.Type.METHOD_SEARCH_MATCH_SONGS, mContext.getString(R.string.load_fail));
                return;
            }
            if (baseSong.returnCode == 0) {
                List<LiveMusicSearchMatchSongBean> list = new ArrayList<>();
                list.add((LiveMusicSearchMatchSongBean) baseSong);
                mListener.onLoadSuccess(DataRequest.Type.METHOD_SEARCH_MATCH_SONGS, list);
            } else {
                mListener.onLoadFail(DataRequest.Type.METHOD_SEARCH_MATCH_SONGS, baseSong.returnMessage);
            }
        } else {
            LogUtil.d(TAG, "mListener is null");
        }
    }
}
