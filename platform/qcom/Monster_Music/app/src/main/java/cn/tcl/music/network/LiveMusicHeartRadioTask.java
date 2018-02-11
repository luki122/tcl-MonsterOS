package cn.tcl.music.network;

import android.content.Context;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.model.live.LiveMusicRadio;
import cn.tcl.music.util.LogUtil;


/**
 * Created by jiangyuanxi on 3/4/16.
 */
public class LiveMusicHeartRadioTask extends RequestSongTask {
    private static final String TAG = "LiveMusicRadioTask";
    private ILoadData mListener;
    private String mCateId = "";
    private String mPage = "";
    private int mPageSize;
    //默认值设置成3的整数倍 方便用listview来显示gridview;
    public static final int REQUEST_RADIO_PAGE_SIZE = 12;

    private LiveMusicHeartRadioTask() {
        super(null, null, null);
    }

    public LiveMusicHeartRadioTask(Context context, ILoadData listener) {
        super(DataRequest.Method.METHOD_LIVE_RADIO, context.getApplicationContext(), LiveMusicRadio.class);
        mListener = listener;
        mCateId = "4";
        mPage = "1";
        mPageSize = REQUEST_PAGE_FIRST;
    }

    /**
     * @param context
     * @param listener
     * @param cateId   电台分类id
     * @param page
     */
    public LiveMusicHeartRadioTask(Context context, ILoadData listener, String cateId, String page) {
        super(DataRequest.Method.METHOD_LIVE_RADIO, context.getApplicationContext(), LiveMusicRadio.class);
        mListener = listener;
        mCateId = cateId;
        mPage = page;
        mPageSize = REQUEST_RADIO_PAGE_SIZE;
    }

    @Override
    public HashMap<String, Object> getParams() {
        HashMap<String, Object> values = new HashMap<>();
        values.put("category_id", mCateId);
        values.put("limit", mPageSize);
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
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_HEART_RADIO, mContext.getString(R.string.load_fail));
                return;
            }
            if (baseSong.returnCode == 0) {
                List<LiveMusicRadio> list = new ArrayList<>();
                list.add((LiveMusicRadio) baseSong);
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_HEART_RADIO, list);
            } else {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_HEART_RADIO, baseSong.returnMessage);
            }
        } else {
            LogUtil.d(TAG, "mListener is null");
        }
    }
}
