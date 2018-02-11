package cn.tcl.music.network;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.model.live.LiveMusicSceneBanner;
import cn.tcl.music.model.live.LiveMusicSceneListBean;
import cn.tcl.music.util.LogUtil;


/**
 * Created by dongdong.huang on 2015/11/4.
 */
public class LiveMusicSceneBannerTask extends RequestSongTask {
    private static final String TAG = LiveMusicSceneBannerTask.class.getSimpleName();
    private ILoadData mListener;

    private LiveMusicSceneBannerTask() {
        super(null, null, null);
    }

    public LiveMusicSceneBannerTask(Context context, ILoadData listener) {
        super(DataRequest.Method.METHOD_LIVE_RECOMMEND_SCENE, context.getApplicationContext(), LiveMusicSceneBanner.class);
        mListener = listener;
    }


    @Override
    public HashMap<String, Object> getParams() {
        HashMap<String, Object> values = new HashMap<String, Object>();
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
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_SCENE_BANNER, mContext.getString(R.string.load_fail));
                return;
            }
            if (baseSong.returnCode == 0) {
                LiveMusicSceneBanner banner = (LiveMusicSceneBanner) baseSong;
                List list = new ArrayList<LiveMusicSceneListBean>();
                list.addAll(banner.list);
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_SCENE_BANNER, list);
            } else {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_SCENE_BANNER, baseSong.returnMessage);
            }
        } else {
            LogUtil.d(TAG, "mListener is null");
        }
    }
}
