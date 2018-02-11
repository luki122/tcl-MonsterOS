package cn.tcl.music.network;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.live.AddCollectBean;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.util.LogUtil;


public class LiveMusicAddCollectsTask extends RequestSongTask{
    private static final String TAG = LiveMusicAddCollectsTask.class.getSimpleName();
    private ILoadData mListener;
    private String mListId = "";

    public LiveMusicAddCollectsTask(Context context, ILoadData listener
            , String listId){
        super(DataRequest.Method.METHOD_LIVE_ADD_COLLECTS, context, AddCollectBean.class);
        mListener = listener;
        mListId = listId;
    }

    public LiveMusicAddCollectsTask(Context context, ILoadData listener
            , String listId, boolean fullDes){
        super(DataRequest.Method.METHOD_LIVE_ADD_COLLECTS, context.getApplicationContext(), AddCollectBean.class);
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
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_ADD_COLLECTS, mContext.getString(R.string.load_fail));
                return;
            }

            //请求成功
            if(baseSong.returnCode == 0){
                List<AddCollectBean> list = new ArrayList<>();
                list.add((AddCollectBean) baseSong);
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_ADD_COLLECTS, list);
            }
            else{
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_ADD_COLLECTS, baseSong.returnMessage);
            }
        }
        else{
            LogUtil.d(TAG, "mListener is null");
        }

    }
}
