package cn.tcl.music.network;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.live.AddCollectBean;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.util.LogUtil;


public class LiveMusicAddAlbumTask extends RequestSongTask {
    private static final String TAG = LiveMusicCollectDetailTask.class.getSimpleName();
    private ILoadData mListener;
    private String mListId = "";

    public LiveMusicAddAlbumTask(Context context, ILoadData listener
            , String listId) {
        super(DataRequest.Method.METHOD_LIVE_ADD_ALBUMS, context, AddCollectBean.class);
        mListener = listener;
        mListId = listId;
    }

    @Override
    protected HashMap<String, Object> getParams() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("ids", mListId);
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
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_ADD_ALBUMS, mContext.getString(R.string.load_fail));
                return;
            }

            //请求成功
            if (baseSong.returnCode == 0) {
                List<AddCollectBean> list = new ArrayList<AddCollectBean>();
                list.add((AddCollectBean) baseSong);
                LogUtil.d(TAG, "success = "+list.get(0).success+" ");
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_ADD_ALBUMS, list);
            } else {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_ADD_ALBUMS, baseSong.returnMessage);
            }
        } else {
            LogUtil.d(TAG, "mListener is null");
        }

    }
}
