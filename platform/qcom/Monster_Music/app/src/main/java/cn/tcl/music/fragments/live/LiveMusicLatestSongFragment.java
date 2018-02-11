package cn.tcl.music.fragments.live;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tcl.framework.log.NLog;
import com.xiami.sdk.utils.ImageUtil;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.activities.live.AlbumDetailActivity;
import cn.tcl.music.activities.live.FragmentContainerActivityV2;
import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.fragments.RecyclerFragment;
import cn.tcl.music.model.live.AlbumBean;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.network.AlbumDetailTask;
import cn.tcl.music.network.DataRequest;
import cn.tcl.music.network.ILoadData;
import cn.tcl.music.network.LiveMusicLatestSongTask;
import cn.tcl.music.network.LiveMusicPlayTask;
import cn.tcl.music.util.ToastUtil;
import cn.tcl.music.util.Util;
import cn.tcl.music.view.AuditionAlertDialog;
import cn.tcl.music.view.PlayMusicButton;

public class LiveMusicLatestSongFragment extends RecyclerFragment
        implements RecyclerFragment.OnLoadMoreListener {
    private int mPageIndex = 1;
    private PlayMusicButton mPlayStopBtn;
    private static ILoadData mListener;
    private List<SongDetailBean> songDetailBeanList;
    private String description;
    private String albumName;
    private boolean isFirstClick = true;

    public static void launch(Activity activity) {
        FragmentContainerActivityV2.launch(activity, LiveMusicLatestSongFragment.class);
    }

    @Override
    protected String getTitle() {
        return getTheString(R.string.latest_music_first);
    }


    @Override
    protected void initViews() {
        super.initViews();
        addLoadMoreListener(this);
        new LiveMusicLatestSongTask(getActivity(), this, 1).executeMultiTask();
        mPageIndex = 1;
    }

    @Override
    public RecyclerView.ViewHolder onItemHolderCreated() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.item_music_lastest_song, null);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
        int margin = Util.dip2px(getActivity(), 4);
        params.leftMargin = margin;
        params.rightMargin = margin;
        params.topMargin = margin;
        params.setMarginStart(margin);
        params.setMarginEnd(margin);
        view.setLayoutParams(params);
        return new LatestSongHolder(view);
    }

    @Override
    public void onItemHolderBinded(RecyclerView.ViewHolder holder, int position, Object item) {
        if (holder != null && item != null && holder instanceof LatestSongHolder) {
            LatestSongHolder essenceViewHolder = (LatestSongHolder) holder;
            final AlbumBean album = (AlbumBean) item;
            if (!MusicApplication.getApp().isDataSaver()) {
                Glide.with(this)
                        .load(ImageUtil.transferImgUrl(album.album_logo, 200))
                        .placeholder(R.drawable.default_cover_ranking)
                        .into(essenceViewHolder.iconImg);
            } else {
                Glide.with(this).load("").placeholder(R.drawable.default_cover_ranking).into(essenceViewHolder.iconImg);
            }
            essenceViewHolder.nameTv.setText(album.album_name);
            essenceViewHolder.artist_nameTv.setText(album.artist_name);
            essenceViewHolder.album_categoryTv.setText(album.album_category + "   " + Util.timestamp2DateString(album.gmt_publish));
            View.OnClickListener mPlayListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getId() == R.id.network_play_all_image || v.getId() == R.id.icon) {
                        mListener = new ILoadData() {
                            @Override
                            public void onLoadSuccess(int dataType, List datas) {
                                //todo cancel task!!!
                                boolean hasfound = false;
                                if (datas != null && datas.size() > 0) {
                                    if (DataRequest.Type.TYPE_LIVE_ALBUM_DETAIL == dataType) {

                                        AlbumBean collectionBean = (AlbumBean) datas.get(0);
                                        if (null == collectionBean.songs) {
                                            return;
                                        }
                                        if (songDetailBeanList == null) {
                                            songDetailBeanList = new ArrayList<SongDetailBean>();
                                        }
                                        description = collectionBean.description;
                                        albumName = collectionBean.album_name;

                                        if (TextUtils.isEmpty(albumName)) {
                                            albumName = getString(R.string.unknown);
                                        }
                                        songDetailBeanList.clear();
                                        songDetailBeanList.addAll(collectionBean.songs);
                                        if (songDetailBeanList != null) {
                                            AuditionAlertDialog.getInstance(getActivity()).showWrapper(false, new AuditionAlertDialog.OnSelectedListener() {
                                                @Override
                                                public void onPlay() {
                                                    new LiveMusicPlayTask(getActivity()).playNow(songDetailBeanList, 0, isFirstClick);
                                                    isFirstClick = false;
                                                }
                                            });
                                        } else {
                                            NLog.i("yshlv", "songDetailBeanList is null");
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
                        new AlbumDetailTask(getActivity(), mListener, album.album_id).executeMultiTask();
                    }
                }
            };

            //点击进入详情
            final int pos = position;
            essenceViewHolder.playImg.setOnClickListener(mPlayListener);
            essenceViewHolder.iconImg.setOnClickListener(mPlayListener);
            essenceViewHolder.itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if (isAdded() && !getActivity().isFinishing()) {
//                        final MobileNetworkDialog dialog = MobileNetworkDialog.getInstance(getActivity());
//                        if (null != dialog && dialog.showWrapper()) {
//                            return;
//                        }
//                    }
                    AlbumDetailActivity.launch(getActivity(), album.album_id, album.album_name, album.song_count);
                }
            });
        }
    }

    @Override
    public void onItemRecycled(RecyclerView.ViewHolder holder) {
        super.onItemRecycled(holder);
        if (holder != null && holder instanceof LatestSongHolder) {

        }
    }

    class LatestSongHolder extends RecyclerView.ViewHolder {
        public ImageView iconImg;
        public TextView nameTv;
        public TextView artist_nameTv, album_categoryTv, gmt_publishTv;
        public View itemLayout;
        public ImageView playImg;

        public LatestSongHolder(View itemView) {
            super(itemView);
            iconImg = (ImageView) itemView.findViewById(R.id.icon);
            nameTv = (TextView) itemView.findViewById(R.id.albun_name);
            artist_nameTv = (TextView) itemView.findViewById(R.id.artist_name);
            album_categoryTv = (TextView) itemView.findViewById(R.id.album_category);
            playImg = (ImageView) itemView.findViewById(R.id.network_play_all_image);
            itemLayout = itemView;
        }
    }

    @Override
    public void onLoadSuccess(int dataType, List datas) {
        super.onLoadSuccess(dataType, datas);

        //首次加载
        if (mPageIndex == 1) {
            hideLoading();
            showContent();

            if (datas == null || datas.size() == 0) {
                showNoData();
                return;
            }
        }
        //加载更多
        else {
            finishBottomLoading();
        }

        if (datas == null || datas.size() == 0) {
            return;
        }

        showData(datas);
        ++mPageIndex;
    }

    @Override
    public void onLoadFail(int dataType, String message) {
        super.onLoadFail(dataType, message);
        showFail();
        finishBottomLoading();
    }

    @Override
    public void onLoadMore() {
        new LiveMusicLatestSongTask(getActivity(), this, mPageIndex).executeMultiTask();
    }

    /**
     * show data
     */
    private void showData(List<AlbumBean> datas) {
        addData2RecyclerView(datas);
    }

    @Override
    protected void doReloadData() {
        mPageIndex = 1;
        new LiveMusicLatestSongTask(getActivity(), this, 1).executeMultiTask();
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }
}