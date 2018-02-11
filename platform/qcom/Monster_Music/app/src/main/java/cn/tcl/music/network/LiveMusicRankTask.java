package cn.tcl.music.network;

import android.content.Context;

import java.util.HashMap;

import cn.tcl.music.R;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.model.live.LiveMusicRank;
import cn.tcl.music.util.LogUtil;


/**
 * Created by dongdong.huang on 2015/11/5.
 */
public class LiveMusicRankTask extends RequestSongTask {
    private static final String TAG = "LiveMusicRankTask";
    private ILoadData mListener;

    private LiveMusicRankTask() {
        super(null, null, null);
    }

    public LiveMusicRankTask(Context context, ILoadData listener) {
        super(DataRequest.Method.METHOD_LIVE_RANK, context.getApplicationContext(), LiveMusicRank.class);
        mListener = listener;
    }

    @Override
    public HashMap<String, Object> getParams() {
        return new HashMap<String, Object>();
    }

    @Override
    public void postInBackground(BaseSong baseSong) {

    }

    @Override
    protected void onPostExecute(BaseSong baseSong) {
        super.onPostExecute(baseSong);
        if (mListener != null) {
            if (baseSong == null) {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_RANK, mContext.getString(R.string.load_fail));
                return;
            }
            if (baseSong.returnCode == 0) {
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_RANK, baseSong.returnArray);
            } else {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_RANK, baseSong.returnMessage);
            }
        } else {
            LogUtil.d(TAG, "mListener is null");
        }

    }
}
