package cn.tcl.music.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.MusicUtil;


/**
 * Created by dongdong.huang on 2015/11/7.
 * 获取歌曲详情
 */
public class LiveMusicSongDetailTask extends RequestSongTask {
    private static final String TAG = "LiveMusicSongDetailTask";
    private ILoadData mListener;
    private String mSongId = "";
    private String mQuality = "";
    private Context mcontext;

    public LiveMusicSongDetailTask(Context context, ILoadData listener
            , String song_id) {
        super(DataRequest.Method.METHOD_LIVE_SONG_DETAIL, context.getApplicationContext(), SongDetailBean.class);
        mcontext = context;
        mListener = listener;
        mSongId = song_id;
        //[FEATURE]Modified by TSNJ,yuanxi.jiang, 2016-02-25, 1652362 begin
        mQuality = getDefaultQuality();//默认低品质
        //[FEATURE]Modified by TSNJ,yuanxi.jiang, 2016-02-25, 1652362 end
    }

    public LiveMusicSongDetailTask(Context context, ILoadData listener
            , String song_id, String quality) {
        super(DataRequest.Method.METHOD_LIVE_SONG_DETAIL, context.getApplicationContext(), SongDetailBean.class);
        mListener = listener;
        mSongId = song_id;
        mQuality = quality;
    }

    @Override
    protected HashMap<String, Object> getParams() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("song_id", mSongId);
        values.put("quality", mQuality);
        values.put("lyric_type", Integer.parseInt(MusicUtil.getLiricType()));//歌词类型
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
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_SONG_DETAIL, mContext.getString(R.string.load_fail));
                return;
            }
            if (baseSong.returnCode == 0) {
                List<SongDetailBean> list = new ArrayList<SongDetailBean>();
                list.add((SongDetailBean) baseSong);
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_SONG_DETAIL, list);
            } else {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_SONG_DETAIL, baseSong.returnMessage);
            }
        } else {
            LogUtil.d(TAG, "mListener is null");
        }

    }

    //[FEATURE]Add by TSNJ,yuanxi.jiang, 2016-02-25, 1652362 begin
    public String getDefaultQuality() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mcontext);
        int mode = sharedPreferences.getInt("quality_download_default", 0);
        Log.d("test11", " mode = " + mode);
        switch (mode) {
            case 0:
                return "l";

            case 1:
                return "l";

            case 2:
                return "h";

        }
        return "l";
    }
    //[FEATURE]Add by TSNJ,yuanxi.jiang, 2016-02-25, 1652362 end
}
