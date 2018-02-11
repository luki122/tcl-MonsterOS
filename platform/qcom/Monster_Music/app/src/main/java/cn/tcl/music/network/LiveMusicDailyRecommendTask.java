package cn.tcl.music.network;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.model.live.LiveMusicDailyRecommend;
import cn.tcl.music.util.LogUtil;


/**
 * Created by jiangyuanxi on 3/4/16.
 */
public class LiveMusicDailyRecommendTask extends RequestSongTask {
    private static final String TAG = LiveMusicDailyRecommendTask.class.getSimpleName();

    private ILoadData mListener;
    private int mPageSize;

    private LiveMusicDailyRecommendTask() {
        super(null, null, null);
    }

    public LiveMusicDailyRecommendTask(Context context, ILoadData listener) {
        super(DataRequest.Method.METHOD_LIVE_RECOMMEND_DAILY, context.getApplicationContext(), LiveMusicDailyRecommend.class);
        mListener = listener;
        mPageSize = REQUEST_PAGE_FIRST;
    }

    /**
     * @param context
     * @param listener
     * @param pageSize
     */
    public LiveMusicDailyRecommendTask(Context context, ILoadData listener, int pageSize) {
        super(DataRequest.Method.METHOD_LIVE_RECOMMEND_DAILY, context.getApplicationContext(), LiveMusicDailyRecommend.class);
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
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_RECOMMEND_DAILY, mContext.getString(R.string.load_fail));
                return;
            }

            //请求成功
            if (baseSong.returnCode == 0) {
                List<LiveMusicDailyRecommend> list = new ArrayList<LiveMusicDailyRecommend>();
                list.add((LiveMusicDailyRecommend) baseSong);
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_RECOMMEND_DAILY, list);
            } else {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_RECOMMEND_DAILY, baseSong.returnMessage);
            }
        } else {
            LogUtil.d(TAG, "mListener is null");
        }
    }
}
