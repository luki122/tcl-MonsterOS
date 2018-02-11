package cn.tcl.music.network;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.model.live.LiveMusicAutoTipsBean;
import cn.tcl.music.model.live.LiveMusicAutoTipsListBean;
import cn.tcl.music.util.LogUtil;

public class LiveMusicAutoTipsBannerTask extends RequestSongTask {
    private static final String TAG = LiveMusicAutoTipsBannerTask.class.getSimpleName();
    private ILoadData mListener;
    private String mKey;

    public LiveMusicAutoTipsBannerTask(Context context, ILoadData listener, String key) {
        super(DataRequest.Method.SEARCH_AUTO_TIPS, context.getApplicationContext(), LiveMusicAutoTipsListBean.class);
        mListener = listener;
        mKey = key;
    }

    @Override
    public HashMap<String, Object> getParams() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("key", mKey);
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
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_SEARCH_SONG, mContext.getString(R.string.load_fail));
                return;
            }
            if (baseSong.returnCode == 0) {
                LiveMusicAutoTipsListBean listBean = (LiveMusicAutoTipsListBean) baseSong;
                List<LiveMusicAutoTipsBean> list = new ArrayList<>();
                list.addAll(listBean.object_list);
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_AUTO_TIPS, list);
            } else {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_AUTO_TIPS, baseSong.returnMessage);
            }
        } else {
            LogUtil.d(TAG, "mListener is null");
        }
    }
}