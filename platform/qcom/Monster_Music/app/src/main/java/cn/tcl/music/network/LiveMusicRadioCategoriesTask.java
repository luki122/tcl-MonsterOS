package cn.tcl.music.network;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.model.live.RadioCatetoryResult;
import cn.tcl.music.util.LogUtil;

public class LiveMusicRadioCategoriesTask extends RequestSongTask {
    private static final String TAG = LiveMusicRadioCategoriesTask.class.getSimpleName();
    private ILoadData mListener;

    public LiveMusicRadioCategoriesTask(Context context, ILoadData listener) {
        super(DataRequest.Method.METHOD_LIVE_RADIO_CATEGORIES, context.getApplicationContext(), RadioCatetoryResult.class);
        mListener = listener;
    }

    @Override
    public HashMap<String, Object> getParams() {
        return new HashMap<>();
    }

    @Override
    public void postInBackground(BaseSong baseSong) {

    }

    @Override
    protected void onPostExecute(BaseSong baseSong) {
        super.onPostExecute(baseSong);
        if (mListener != null) {
            if (baseSong == null) {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_RADIO_CATEGORES, mContext.getString(R.string.load_fail));
                return;
            }
            if (baseSong.returnCode == 0) {
                RadioCatetoryResult catetoryResult = (RadioCatetoryResult) baseSong;
                List list = new ArrayList<>();
                list.addAll(catetoryResult.categories);
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_RADIO_CATEGORES, list);
            } else {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_RADIO_CATEGORES, baseSong.returnMessage);
            }
        } else {
            LogUtil.d(TAG, "mListener is null");
        }

    }
}
