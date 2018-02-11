package cn.tcl.music.network;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.model.live.LiveMusicCollectListBean;
import cn.tcl.music.util.LogUtil;


public class LiveMusicCollectListTask extends RequestSongTask {
    private static final String TAG = LiveMusicCollectListTask.class.getSimpleName();
    private ILoadData mListener;
    private int mOrderType = 0;
    private int mPageSize;
    private int mPage = 1;

    public LiveMusicCollectListTask(Context context, ILoadData listener) {
        super(DataRequest.Method.METHOD_LIVE_COLLECT_LIST, context, LiveMusicCollectListBean.class);
        mListener = listener;
        mPage = 1;
        mPageSize = REQUEST_PAGE_SIZE;
    }

    public LiveMusicCollectListTask(Context context, ILoadData listener, int orderType, int pageSize, int page) {
        super(DataRequest.Method.METHOD_LIVE_COLLECT_LIST, context.getApplicationContext(), LiveMusicCollectListBean.class);
        mListener = listener;
        mOrderType = orderType;
        mPageSize = pageSize;
        mPage = page;
    }

    @Override
    protected HashMap<String, Object> getParams() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("order_type", mOrderType);
        values.put("limit", mPageSize);
        values.put("page", mPage);
        LogUtil.d(TAG,"limit is " + mPageSize + " and page is " + mPage);
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
            if (baseSong.returnCode == 0) {
                List<LiveMusicCollectListBean> list = new ArrayList<>();
                list.add((LiveMusicCollectListBean) baseSong);
                LogUtil.d(TAG, baseSong.toString());
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_COLLECT_DETAIL, list);
            } else {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_COLLECT_DETAIL, baseSong.returnMessage);
            }
        } else {
            LogUtil.d(TAG, "mListener is null");
        }

    }
}
