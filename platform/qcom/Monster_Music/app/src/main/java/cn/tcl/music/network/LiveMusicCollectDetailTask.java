package cn.tcl.music.network;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.model.live.CollectionBean;
import cn.tcl.music.util.LogUtil;


/**
 * @author zengtao.kuang
 * @Description: 精选集详情
 * @date 2015/11/10 17:21
 * @copyright TCL-MIE
 */
public class LiveMusicCollectDetailTask extends RequestSongTask{
    private static final String TAG = "LiveMusicCollectDetailTask";
    private ILoadData mListener;
    private String listId = "";
    private boolean fullDes = true;

    public LiveMusicCollectDetailTask(Context context, ILoadData listener
            , String listId){
        super(DataRequest.Method.METHOD_LIVE_COLLECT_DETAIL, context, CollectionBean.class);
        mListener = listener;
        this.listId = listId;
    }

    public LiveMusicCollectDetailTask(Context context, ILoadData listener
            , String listId, boolean fullDes){
        super(DataRequest.Method.METHOD_LIVE_COLLECT_DETAIL, context.getApplicationContext(), CollectionBean.class);
        mListener = listener;
        this.listId = listId;
        this.fullDes = fullDes;
    }

    @Override
    protected HashMap<String, Object> getParams() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("list_id", listId);
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
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_COLLECT_DETAIL, mContext.getString(R.string.load_fail));
                return;
            }

            //请求成功
            if(baseSong.returnCode == 0){
                List<CollectionBean> list = new ArrayList<CollectionBean>();
                list.add((CollectionBean)baseSong);
                LogUtil.d(TAG,baseSong.toString());
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_COLLECT_DETAIL, list);
            }
            else{
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_COLLECT_DETAIL, baseSong.returnMessage);
            }
        }
        else{
            LogUtil.d(TAG, "mListener is null");
        }

    }
}
