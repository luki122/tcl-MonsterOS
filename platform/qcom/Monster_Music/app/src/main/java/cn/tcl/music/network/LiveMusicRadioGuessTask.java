package cn.tcl.music.network;

import android.content.Context;

import java.util.HashMap;

import cn.tcl.music.R;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.model.live.RadioDetailBean;
import cn.tcl.music.util.LogUtil;

public class LiveMusicRadioGuessTask extends RequestSongTask {
    private static final String TAG = LiveMusicRadioGuessTask.class.getSimpleName();
    private ILoadData mListener;

    private static final int DEFAULT_LIMIT = 50;

    public LiveMusicRadioGuessTask(Context context, ILoadData listener) {
        super(DataRequest.Method.METHOD_GUESS_RADIO, context.getApplicationContext(), RadioDetailBean.class);
        mListener = listener;
    }

    @Override
    protected HashMap<String, Object> getParams() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        /*values.put("limit",DEFAULT_LIMIT);
        values.put("context","");
        values.put("like","");
        values.put("unlike","");
        values.put("listen","");*/
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
                mListener.onLoadFail(DataRequest.Type.TYPE_GUESS_RADIO, mContext.getString(R.string.load_fail));
                return;
            }
            if (baseSong.returnCode == 0) {
                RadioDetailBean detail = (RadioDetailBean) baseSong;
                mListener.onLoadSuccess(DataRequest.Type.TYPE_GUESS_RADIO, detail.songs);
            } else {
                mListener.onLoadFail(DataRequest.Type.TYPE_GUESS_RADIO, baseSong.returnMessage);
            }
        } else {
            LogUtil.d(TAG, "mListener is null");
        }
    }
}
