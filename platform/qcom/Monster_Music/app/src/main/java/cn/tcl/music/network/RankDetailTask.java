package cn.tcl.music.network;

import android.content.Context;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.model.live.LiveMusicRankItem;
import cn.tcl.music.util.LogUtil;


/**
 * @author zengtao.kuang
 * @Description: 榜单详情任务
 * @date 2015/11/10 17:21
 * @copyright TCL-MIE
 */
public class RankDetailTask extends RequestSongTask {
    private static final String TAG = "RankDetailTask";
    private ILoadData mListener;
    private String type;
    private int time = 0;

    public RankDetailTask(Context context, ILoadData listener
            , String type) {
        super(DataRequest.Method.METHOD_LIVE_RANK_DETAIL, context.getApplicationContext(), LiveMusicRankItem.class);
        mListener = listener;
        this.type = type;
    }

    public RankDetailTask(Context context, ILoadData listener
            , String type, int time) {
        super(DataRequest.Method.METHOD_LIVE_RANK_DETAIL, context.getApplicationContext(), LiveMusicRankItem.class);
        mListener = listener;
        this.type = type;
        this.time = time;
    }

    @Override
    protected HashMap<String, Object> getParams() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("type", type);
        values.put("time", time);
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
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_RANK_DETAIL, mContext.getString(R.string.load_fail));
                return;
            }
            if (baseSong.returnCode == 0) {
                List<LiveMusicRankItem> list = new ArrayList<LiveMusicRankItem>();
                list.add((LiveMusicRankItem) baseSong);
                LogUtil.d(TAG, baseSong.toString());
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_RANK_DETAIL, list);
            } else {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_RANK_DETAIL, baseSong.returnMessage);
            }
        } else {
            LogUtil.d(TAG, "mListener is null");
        }

    }
}
