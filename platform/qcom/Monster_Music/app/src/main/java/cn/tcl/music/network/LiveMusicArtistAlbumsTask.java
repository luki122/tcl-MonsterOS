package cn.tcl.music.network;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.live.ArtistAlbumsDataBean;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.util.LogUtil;


/**
 * @author zengtao.kuang
 * @Description: 艺人的专辑列表
 * @date 2015/11/10 20:36
 * @copyright TCL-MIE
 */
public class LiveMusicArtistAlbumsTask extends RequestSongTask{
    private static final String TAG = "LiveMusicArtistAlbumsTask";
    private ILoadData mListener;
    private String artistId = "";
    private int limit = 50;
    private int page = 1;

    public LiveMusicArtistAlbumsTask(Context context, ILoadData listener
            , String artistId){
        super(DataRequest.Method.METHOD_LIVE_ARTIST_ALBUMS, context.getApplicationContext(), ArtistAlbumsDataBean.class);
        mListener = listener;
        this.artistId = artistId;
    }

    public LiveMusicArtistAlbumsTask(Context context, ILoadData listener
            , String artistId, int page){
        super(DataRequest.Method.METHOD_LIVE_ARTIST_ALBUMS, context.getApplicationContext(), ArtistAlbumsDataBean.class);
        mListener = listener;
        this.artistId = artistId;
        this.page = page;
    }

    public LiveMusicArtistAlbumsTask(Context context, ILoadData listener
            , String artistId, int page, int limit){
        super(DataRequest.Method.METHOD_LIVE_ARTIST_ALBUMS, context.getApplicationContext(), ArtistAlbumsDataBean.class);
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
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_ARTIST_ALBUMS, mContext.getString(R.string.load_fail));
                return;
            }

            //请求成功
            if(baseSong.returnCode == 0){
                List<ArtistAlbumsDataBean> list = new ArrayList<ArtistAlbumsDataBean>();
                list.add((ArtistAlbumsDataBean)baseSong);
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_ARTIST_ALBUMS, list);
            }
            else{
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_ARTIST_ALBUMS, baseSong.returnMessage);
            }
        }
        else{
            LogUtil.d(TAG, "mListener is null");
        }

    }
}
