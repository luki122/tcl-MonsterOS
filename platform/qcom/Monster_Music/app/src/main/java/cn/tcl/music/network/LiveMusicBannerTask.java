package cn.tcl.music.network;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.model.live.LiveMusicBanner;
import cn.tcl.music.model.live.LiveMusicBannerItem;
import cn.tcl.music.util.LogUtil;


/**
 * Created by dongdong.huang on 2015/11/4.
 */
public class LiveMusicBannerTask extends RequestSongTask {
    private static final String TAG = "LiveMusicBannerTask";
    private ILoadData mListener;

    private LiveMusicBannerTask(){
        super(null, null, null);
    }

    public LiveMusicBannerTask(Context context, ILoadData listener){
        super(DataRequest.Method.METHOD_LIVE_BANNER, context.getApplicationContext(), LiveMusicBanner.class);
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
        LogUtil.i(TAG, "--onPostExecute--load--");

        if (mListener != null) {
            if (baseSong == null) {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_BANNER, mContext.getString(R.string.load_fail));
                return;
            }

            //请求成功
            if(baseSong.returnCode == 0){
                LiveMusicBanner banner = (LiveMusicBanner)baseSong;
                List list = new ArrayList<LiveMusicBannerItem>();
                list.addAll(banner.imgs);
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_BANNER, list);
            }
            else{
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_BANNER, baseSong.returnMessage);
            }
        }
        else{
            LogUtil.d(TAG, "mListener is null");
        }
    }
}
