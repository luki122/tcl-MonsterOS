package cn.tcl.music.network;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.live.ArtistHotMusicDataBean;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.util.LogUtil;

/**
 * @author zengtao.kuang
 * @Description: 艺人热门歌曲
 * @date 2015/11/10 17:21
 * @copyright TCL-MIE
 */
public class LiveMusicArtistHotSongsTask extends RequestSongTask{
    private static final String TAG = "LiveMusicArtistHotSongsTask";
    private ILoadData mListener;
    private String artistId = "";
    private int limit = 50;
    private int page = 1;

    public LiveMusicArtistHotSongsTask(Context context, ILoadData listener
            , String artistId){
        super(DataRequest.Method.METHOD_LIVE_ARTIST_HOT_SONGS, context.getApplicationContext(), ArtistHotMusicDataBean.class);
        mListener = listener;
        this.artistId = artistId;
    }

    public LiveMusicArtistHotSongsTask(Context context, ILoadData listener
            , String artistId, int page){
        super(DataRequest.Method.METHOD_LIVE_ARTIST_HOT_SONGS, context.getApplicationContext(), ArtistHotMusicDataBean.class);
        mListener = listener;
        this.artistId = artistId;
        this.page = page;
    }

    public LiveMusicArtistHotSongsTask(Context context, ILoadData listener
            , String artistId, int page, int limit){
        super(DataRequest.Method.METHOD_LIVE_ARTIST_HOT_SONGS, context.getApplicationContext(), ArtistHotMusicDataBean.class);
        mListener = listener;
        this.artistId = artistId;
        this.page = page;
        this.limit = limit;
    }

    @Override
    protected HashMap<String, Object> getParams() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("artist_id", artistId);
        values.put("limit", limit);
        values.put("page", page);
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
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_ARTIST_HOT_SONGS, mContext.getString(R.string.load_fail));
                return;
            }

            //请求成功
            if(baseSong.returnCode == 0){
                List<ArtistHotMusicDataBean> list = new ArrayList<ArtistHotMusicDataBean>();
                list.add((ArtistHotMusicDataBean)baseSong);
                LogUtil.d(TAG,baseSong.toString());
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_ARTIST_HOT_SONGS, list);
            }
            else{
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_ARTIST_HOT_SONGS, baseSong.returnMessage);
            }
        }
        else{
            LogUtil.d(TAG, "mListener is null");
        }

    }
}
