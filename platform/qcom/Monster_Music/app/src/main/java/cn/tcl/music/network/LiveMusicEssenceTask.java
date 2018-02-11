package cn.tcl.music.network;

import android.content.Context;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.model.live.LiveMusicEssence;
import cn.tcl.music.util.LogUtil;


/**
 * Created by dongdong.huang on 2015/11/6.
 */
public class LiveMusicEssenceTask extends RequestSongTask {
    private static final String TAG = "LiveMusicEssenceTask";
    private ILoadData mListener;
    private int mPage = 1;
    private int mPageSize;


    private LiveMusicEssenceTask() {
        super(null, null, null);
    }

    public LiveMusicEssenceTask(Context context, ILoadData listener) {
        super(DataRequest.Method.METHOD_LIVE_ESSENCE, context.getApplicationContext(), LiveMusicEssence.class);
        mListener = listener;
        mPage = 1;
        mPageSize = REQUEST_PAGE_FIRST;
    }

    public LiveMusicEssenceTask(Context context, ILoadData listener, int page) {
        super(DataRequest.Method.METHOD_LIVE_ESSENCE, context.getApplicationContext(), LiveMusicEssence.class);
        mListener = listener;
        mPage = page;
        mPageSize = REQUEST_PAGE_SIZE;
    }

    @Override
    protected HashMap<String, Object> getParams() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("page", mPage);
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
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_ESSENCE, mContext.getString(R.string.load_fail));
                return;
            }

            //请求成功
            if (baseSong.returnCode == 0) {
                List<LiveMusicEssence> list = new ArrayList<LiveMusicEssence>();
                list.add((LiveMusicEssence) baseSong);
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_ESSENCE, list);
            } else {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_ESSENCE, baseSong.returnMessage);
            }
        } else {
            LogUtil.d(TAG, "mListener is null");
        }
    }

}
