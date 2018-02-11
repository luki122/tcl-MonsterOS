package cn.tcl.music.network;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.live.ArtistBean;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.util.LogUtil;

/**
 * @author zengtao.kuang
 * @Description:
 * @date 2015/11/10 10:21
 * @copyright TCL-MIE
 */
public class LiveMusicArtistDetailTask extends RequestSongTask{
    private static final String TAG = "LiveMusicArtistDetailTask";
    private ILoadData mListener;
    private String artistId = "";
    private boolean fullDes = true;//默认获取完整的专辑描述

    private LiveMusicArtistDetailTask(){
        super(null, null, null);
    }

    public LiveMusicArtistDetailTask(Context context, ILoadData listener
            , String artistId){
        super(DataRequest.Method.METHOD_LIVE_ARTIST_DETAIL, context.getApplicationContext(), ArtistBean.class);
        mListener = listener;
        this.artistId = artistId;
    }

    public LiveMusicArtistDetailTask(Context context, ILoadData listener
            , String artistId, boolean fullDes){
        super(DataRequest.Method.METHOD_LIVE_ARTIST_DETAIL, context.getApplicationContext(), ArtistBean.class);
        mListener = listener;
        this.artistId = artistId;
        this.fullDes = fullDes;
    }

    @Override
    protected HashMap<String, Object> getParams() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("artist_id", artistId);
        values.put("full_des", fullDes);
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
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_ARTIST_DETAIL, mContext.getString(R.string.load_fail));
                return;
            }

            //请求成功
            if(baseSong.returnCode == 0){
                List<ArtistBean> list = new ArrayList<ArtistBean>();
                list.add((ArtistBean)baseSong);
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_ARTIST_DETAIL, list);
            }
            else{
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_ARTIST_DETAIL, baseSong.returnMessage);
            }
        }
        else{
            LogUtil.d(TAG, "mListener is null");
        }

    }
}
