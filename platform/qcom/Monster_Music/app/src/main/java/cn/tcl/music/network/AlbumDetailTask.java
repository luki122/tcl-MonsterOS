package cn.tcl.music.network;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.live.AlbumBean;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.util.LogUtil;

/**
 * @author zengtao.kuang
 * @Description: 专辑详情
 * @date 2015/11/20 09:15
 * @copyright TCL-MIE
 */
public class AlbumDetailTask extends RequestSongTask{
    private static final String TAG = "AlbumDetailTask";
    private ILoadData mListener;
    private String albumId;
    private boolean fullDes = true;

    public AlbumDetailTask(Context context, ILoadData listener
            , String albumId){
        super(DataRequest.Method.METHOD_LIVE_ALBUM_DETAIL, context, AlbumBean.class);
        mListener = listener;
        this.albumId = albumId;
    }

    public AlbumDetailTask(Context context, ILoadData listener, String albumId, boolean fullDes){
        super(DataRequest.Method.METHOD_LIVE_ALBUM_DETAIL, context.getApplicationContext(), AlbumBean.class);
        mListener = listener;
        this.albumId = albumId;
        this.fullDes = fullDes;
    }

    @Override
    protected HashMap<String, Object> getParams() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("album_id", albumId);
        values.put("full_des", fullDes);
        return values;
    }

    @Override
    protected void postInBackground(BaseSong baseSong) {

    }

    @Override
    protected void onPostExecute(BaseSong baseSong) {
        LogUtil.d(TAG,baseSong.toString());
        super.onPostExecute(baseSong);
        if (mListener != null) {
            if (baseSong == null) {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_ALBUM_DETAIL, mContext.getString(R.string.load_fail));
                return;
            }

            //请求成功
            if(baseSong.returnCode == 0){
                List<AlbumBean> list = new ArrayList<AlbumBean>();
                list.add((AlbumBean)baseSong);
                LogUtil.d(TAG,baseSong.toString());
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_ALBUM_DETAIL, list);
            }
            else{
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_ALBUM_DETAIL, baseSong.returnMessage);
            }
        }
        else{
            LogUtil.d(TAG, "mListener is null");
        }
    }
}
