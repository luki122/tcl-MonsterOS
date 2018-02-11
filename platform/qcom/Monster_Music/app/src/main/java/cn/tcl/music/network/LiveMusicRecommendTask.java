package cn.tcl.music.network;

import android.content.Context;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.model.live.LiveMusicRecommend;
import cn.tcl.music.util.LogUtil;


/**
 * Created by dongdong.huang on 2015/11/5.
 */
public class LiveMusicRecommendTask extends RequestSongTask {
    private static final String TAG = "LiveMusicRecommendTask";
    private ILoadData mListener;
    private int mPageSize;

    private LiveMusicRecommendTask() {
        super(null, null, null);
    }

    public LiveMusicRecommendTask(Context context, ILoadData listener) {
        super(DataRequest.Method.METHOD_LIVE_RECOMMEND, context.getApplicationContext(), LiveMusicRecommend.class);
        mListener = listener;
        mPageSize = 10; //modify this for 2361009
    }

    /**
     * @param context
     * @param listener
     * @param pageSize
     */
    public LiveMusicRecommendTask(Context context, ILoadData listener, int pageSize) {
        super(DataRequest.Method.METHOD_LIVE_RECOMMEND, context.getApplicationContext(), LiveMusicRecommend.class);
        mListener = listener;
        mPageSize = pageSize;
    }

    @Override
    protected HashMap<String, Object> getParams() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("limit", mPageSize);
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
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_RECOMMEND, mContext.getString(R.string.load_fail));
                return;
            }
            if (baseSong.returnCode == 0) {
                List<LiveMusicRecommend> list = new ArrayList<LiveMusicRecommend>();
                list.add((LiveMusicRecommend) baseSong);
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_RECOMMEND, list);
            } else {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_RECOMMEND, baseSong.returnMessage);
            }
        } else {
            LogUtil.d(TAG, "mListener is null");
        }
    }
}
