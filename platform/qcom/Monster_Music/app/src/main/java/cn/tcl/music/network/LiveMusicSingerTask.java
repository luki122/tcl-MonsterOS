package cn.tcl.music.network;

import android.content.Context;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.model.live.LiveMusicSinger;
import cn.tcl.music.util.LogUtil;


/**
 * Created by dongdong.huang on 2015/11/5.
 */
public class LiveMusicSingerTask extends RequestSongTask {
    private static final String TAG = "LiveMusicSingerTask";
    private ILoadData mListener;
    private String mType = "";
    private String mPage = "1";
    private int mLimit;
    public static int DEFAULT_PAGE_LIMIT = 12; //默认值设置成3的整数倍 方便用listview来显示gridview;

    private LiveMusicSingerTask() {
        super(null, null, null);
    }


    public LiveMusicSingerTask(Context context, ILoadData listener) {
        super(DataRequest.Method.METHOD_LIVE_SINGER, context.getApplicationContext(), LiveMusicSinger.class);
        mListener = listener;
        mType = "chinese_F";
        mPage = "1";
        mLimit = REQUEST_PAGE_FIRST;
    }


    public LiveMusicSingerTask(Context context, ILoadData listener, String type, String page) {
        super(DataRequest.Method.METHOD_LIVE_SINGER, context.getApplicationContext(), LiveMusicSinger.class);
        mListener = listener;
        mType = type;
        mPage = page;
        mLimit = DEFAULT_PAGE_LIMIT;
    }

    @Override
    public HashMap<String, Object> getParams() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("type", mType);
        values.put("limit", mLimit);
        values.put("page", mPage);
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
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_SINGER, mContext.getString(R.string.load_fail));
                return;
            }
            if (baseSong.returnCode == 0) {
                List<LiveMusicSinger> list = new ArrayList<>();
                list.add((LiveMusicSinger) baseSong);
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_SINGER, list);
            } else {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_SINGER, baseSong.returnMessage);
            }
        } else {
            LogUtil.d(TAG, "mListener is null");
        }
    }
}
