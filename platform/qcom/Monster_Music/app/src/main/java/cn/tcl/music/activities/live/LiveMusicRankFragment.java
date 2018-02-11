package cn.tcl.music.activities.live;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiami.sdk.utils.ImageUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.activities.FragmentArgs;
import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.fragments.RecyclerFragment;
import cn.tcl.music.model.live.LiveMusicRank;
import cn.tcl.music.model.live.LiveMusicRankItem;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.network.DataRequest;
import cn.tcl.music.network.ILoadData;
import cn.tcl.music.network.LiveMusicPlayTask;
import cn.tcl.music.network.LiveMusicRankTask;
import cn.tcl.music.network.RankDetailTask;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.ToastUtil;
import cn.tcl.music.util.Util;
import cn.tcl.music.view.AuditionAlertDialog;

public class LiveMusicRankFragment extends RecyclerFragment {
    private static String TAG = LiveMusicRankFragment.class.getSimpleName();
    private Handler mHandler = new Handler();
    private static ILoadData mListener;
    private List<SongDetailBean> mSongDetailBeanList;
    private boolean mIsFirstClick = true;

    public static void launch(Activity activity, List<LiveMusicRankItem> ranks) {
        FragmentArgs args = new FragmentArgs();
        args.add("ranks", (Serializable) ranks);
        FragmentContainerActivityV2.launch(activity, LiveMusicRankFragment.class, args);
    }

    @Override
    protected String getTitle() {
        return getTheString(R.string.rank_list);
    }

    @Override
    protected void initViews() {
        super.initViews();
        Bundle bundle = getArguments();
        List<LiveMusicRankItem> ranks = null;

        if (bundle != null) {
            try {
                ranks = (List<LiveMusicRankItem>) bundle.getSerializable("ranks");
            } catch (Exception e) {

            }
        }

        if (ranks != null && ranks.size() > 0) {
            addData2RecyclerView(ranks);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideLoading();
                    showContent();
                }
            }, 300);
        } else {
            new LiveMusicRankTask(getActivity(), this).executeMultiTask();
        }
    }

    @Override
    public RecyclerView.ViewHolder onItemHolderCreated() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.item_live_music_rank, null);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
        int margin = Util.dip2px(getActivity(), 4);
        params.leftMargin = margin;
        params.rightMargin = margin;
        params.topMargin = margin;
        params.setMarginStart(margin);
        params.setMarginEnd(margin);
        view.setLayoutParams(params);
        return new RankHolder(view);
    }

    @Override
    public void onItemHolderBinded(RecyclerView.ViewHolder holder, int position, Object item) {
        if (holder != null && item != null && holder instanceof RankHolder) {
            RankHolder rankHolder = (RankHolder) holder;
            final LiveMusicRankItem rankItem = (LiveMusicRankItem) item;
            if (!MusicApplication.getApp().isDataSaver()) {
                Glide.with(this)
                        .load(ImageUtil.transferImgUrl(rankItem.logo, 200))
                        .placeholder(R.drawable.default_cover_ranking)
                        .into(rankHolder.logoImg);
            } else {
                Glide.with(this).load("").placeholder(R.drawable.default_cover_ranking).into(rankHolder.logoImg);
            }
            rankHolder.nameTv.setText(Util.replaceStringTags(getActivity(), rankItem.title));
            bindItemData(rankHolder, rankItem);

            rankHolder.logoImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener = new ILoadData() {
                        @Override
                        public void onLoadSuccess(int dataType, List datas) {
                            //todo cancel task!!!
                            LogUtil.i("yshhot", "gotoLatestSongDetail onLoadSuccess   " + datas.size());
                            if (datas != null && datas.size() > 0) {
                                LogUtil.i("yshhot", "dataType== " + dataType);
                                if (DataRequest.Type.TYPE_LIVE_RANK_DETAIL == dataType) {
                                    LogUtil.i("yshhot", "DataRequest.Type.TYPE_LIVE_ALBUM_DETAIL == dataType");
                                    LiveMusicRankItem liveMusicRankItem = (LiveMusicRankItem) datas.get(0);
                                    if (liveMusicRankItem.songs == null || liveMusicRankItem.songs.size() == 0) {
                                        return;
                                    }
                                    if (mSongDetailBeanList == null) {
                                        mSongDetailBeanList = new ArrayList<>();
                                    }
                                    mSongDetailBeanList.clear();
                                    mSongDetailBeanList.addAll(liveMusicRankItem.songs);

                                    if (mSongDetailBeanList != null) {
                                        LogUtil.d(TAG, "songDetailBeanList加入");
                                        AuditionAlertDialog.getInstance(getActivity()).showWrapper(false, new AuditionAlertDialog.OnSelectedListener() {
                                            @Override
                                            public void onPlay() {
                                                new LiveMusicPlayTask(getActivity()).playNow(mSongDetailBeanList, 0, mIsFirstClick);
                                                mIsFirstClick = false;
                                            }
                                        });
                                    } else {
                                        LogUtil.i(TAG, "songDetailBeanList is null");
                                    }
                                }
                            } else {
                                return;
                            }
                        }

                        @Override
                        public void onLoadFail(int dataType, String message) {
                            ToastUtil.showToast(getActivity(), getActivity().getString(R.string.play_track_failed));
                        }
                    };
                    new RankDetailTask(getActivity(), mListener, rankItem.type).executeMultiTask();
                }
            });

            //点击进入详情
            rankHolder.itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if (isAdded() && !getActivity().isFinishing()) {
//                        final MobileNetworkDialog dialog = MobileNetworkDialog.getInstance(getActivity());
//                        if (null != dialog && dialog.showWrapper()) {
//                            return;
//                        }
//                    }
                    String title = Util.replaceStringTags(getActivity(), rankItem.title);
                    RankDetailActivity.launch(getActivity(), rankItem.type, title, rankItem.total);
                }
            });

            rankHolder.rankMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if (isAdded() && !getActivity().isFinishing()) {
//                        final MobileNetworkDialog dialog = MobileNetworkDialog.getInstance(getActivity());
//                        if (null != dialog && dialog.showWrapper()) {
//                            return;
//                        }
//                    }
                    String title = Util.replaceStringTags(getActivity(), rankItem.title);
                    RankDetailActivity.launch(getActivity(), rankItem.type, title, rankItem.total);
                    LogUtil.e(TAG, "RANK_lIST_MORE");
                }
            });
        }
    }

    @Override
    public void onItemRecycled(RecyclerView.ViewHolder holder) {
        super.onItemRecycled(holder);
        if (holder != null && holder instanceof RankHolder) {

        }
    }

    class RankHolder extends RecyclerView.ViewHolder {
        public ImageView logoImg;
        public TextView nameTv;
        public TextView songTv1;
        public TextView songTv2;
        public TextView songTv3;
        public TextView songTv4;
        public TextView singerTv1;
        public TextView singerTv2;
        public TextView singerTv3;
        public TextView singerTv4;
        public View songLay1;
        public View songLay2;
        public View songLay3;
        public View songLay4;
        public View rankMore;
        public View itemLayout;

        public RankHolder(View itemView) {
            super(itemView);
            logoImg = (ImageView) itemView.findViewById(R.id.rank_logo);
            nameTv = (TextView) itemView.findViewById(R.id.rank_name);
            songTv1 = (TextView) itemView.findViewById(R.id.rank_song1);
            songTv2 = (TextView) itemView.findViewById(R.id.rank_song2);
            songTv3 = (TextView) itemView.findViewById(R.id.rank_song3);
            songTv4 = (TextView) itemView.findViewById(R.id.rank_song4);
            singerTv1 = (TextView) itemView.findViewById(R.id.rank_singer1);
            singerTv2 = (TextView) itemView.findViewById(R.id.rank_singer2);
            singerTv3 = (TextView) itemView.findViewById(R.id.rank_singer3);
            singerTv4 = (TextView) itemView.findViewById(R.id.rank_singer4);
            songLay1 = itemView.findViewById(R.id.song_lay1);
            songLay2 = itemView.findViewById(R.id.song_lay2);
            songLay3 = itemView.findViewById(R.id.song_lay3);
            songLay4 = itemView.findViewById(R.id.song_lay4);
            rankMore = itemView.findViewById(R.id.rank_more);
            itemLayout = itemView;
        }
    }

    @Override
    public void onLoadFail(int dataType, String message) {
        super.onLoadFail(dataType, message);
        showFail();
        finishBottomLoading();
    }

    @Override
    public void onLoadSuccess(int dataType, List datas) {
        super.onLoadSuccess(dataType, datas);
        hideLoading();
        showContent();

        if (datas == null || datas.size() == 0) {
            showNoData();
            return;
        }

        showData(datas);
    }

    private void showData(List<LiveMusicRank> list) {
        int size = list.size();
        List<LiveMusicRankItem> rankItems = new ArrayList<LiveMusicRankItem>();

        for (int i = 0; i < size; i++) {
            addRankToTemp(rankItems, list.get(i).items);
        }

        addData2RecyclerView(rankItems);
    }

    private void addRankToTemp(List<LiveMusicRankItem> temps, List<LiveMusicRankItem> list) {
        if (list != null) {
            LiveMusicRankItem item = null;
            String filter1 = getTheString(R.string.rank_filter1);
            String filter2 = getTheString(R.string.rank_filter2);
            for (int i = 0; i < list.size(); i++) {
                item = list.get(i);

                if (item != null && !filter1.equals(item.title) && !filter2.equals(item.title)) {
                    temps.add(item);
                }
            }
        }
    }

    private void bindItemData(RankHolder rankHolder, LiveMusicRankItem rankItem) {
        String song1 = "";
        String song2 = "";
        String song3 = "";
        String song4 = "";
        String singer1 = "";
        String singer2 = "";
        String singer3 = "";
        String singer4 = "";

        try {
            song1 = rankItem.songs.get(0).song_name;
            song2 = rankItem.songs.get(1).song_name;
            song3 = rankItem.songs.get(2).song_name;
            song4 = rankItem.songs.get(3).song_name;
            singer1 = rankItem.songs.get(0).singers;
            singer2 = rankItem.songs.get(1).singers;
            singer3 = rankItem.songs.get(2).singers;
            singer4 = rankItem.songs.get(3).singers;
        } catch (Exception e) {

        }

        if (!TextUtils.isEmpty(song1)) {
            rankHolder.songTv1.setText("TOP1 " + song1);
            Log.d("sameName", "LiveMusicRankFragment bindItemData songTv1 =" + song1);
            rankHolder.songLay1.setVisibility(View.VISIBLE);
        } else {
            rankHolder.songLay1.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(song2)) {
            rankHolder.songTv2.setText("TOP2 " + song2);
            rankHolder.songLay2.setVisibility(View.VISIBLE);
        } else {
            rankHolder.songLay2.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(song3)) {
            rankHolder.songTv3.setText("TOP3 " + song3);
            rankHolder.songLay3.setVisibility(View.VISIBLE);
        } else {
            rankHolder.songLay3.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(song4)) {
            rankHolder.songTv4.setText("TOP4 " + song4);
            rankHolder.songLay4.setVisibility(View.VISIBLE);
        } else {
            rankHolder.songLay4.setVisibility(View.GONE);
        }
    }

    @Override
    protected void doReloadData() {
        showLoading();
        new LiveMusicRankTask(getActivity(), this).executeMultiTask();
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

}
