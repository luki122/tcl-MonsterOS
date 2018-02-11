package cn.tcl.music.network;

import android.content.Context;

import java.util.HashMap;

import cn.tcl.music.R;
import cn.tcl.music.model.live.AlbumBean;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.util.LogUtil;


/**
 * Created by dongdong.huang on 2015/11/4.
 * 新碟首发
 */
public class LiveMusicLatestSongTask extends RequestSongTask {
    private static final String TAG = "LiveMusicLatestSongTask";
    private ILoadData mListener;
    private int mPage = 1;
    private int mPageSize;

    private LiveMusicLatestSongTask() {
        super(null, null, null);
    }

    public LiveMusicLatestSongTask(Context context, ILoadData listener) {
        super(DataRequest.Method.METHOD_LIVE_LATEST, context.getApplicationContext(), AlbumBean.class);
        mListener = listener;
        mPage = 1;
        mPageSize = REQUEST_PAGE_FIRST;
    }

    public LiveMusicLatestSongTask(Context context, ILoadData listener, int page) {
        super(DataRequest.Method.METHOD_LIVE_LATEST, context.getApplicationContext(), AlbumBean.class);
        mListener = listener;
        mPage = page;
        mPageSize = REQUEST_PAGE_SIZE;
    }

    @Override
    public HashMap<String, Object> getParams() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("type", "all");
        values.put("page", mPage);
        values.put("limit", mPageSize);
        return values;
    }

    @Override
    public void postInBackground(BaseSong baseSong) {
        //save to db
    }

    @Override
    protected void onPostExecute(BaseSong baseSong) {
        super.onPostExecute(baseSong);
        if (mListener != null) {
            if (baseSong == null) {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_LATEST, mContext.getString(R.string.load_fail));
                return;
            }
            if (baseSong.returnCode == 0) {
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_LATEST, baseSong.returnArray);
            } else {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_LATEST, baseSong.returnMessage);
            }
        } else {
            LogUtil.d(TAG, "mListener is null");
        }
    }
}
