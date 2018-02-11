package cn.tcl.music.fragments.live;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiami.sdk.utils.ImageUtil;

import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.activities.live.OnlinePlayListDetailActivity;
import cn.tcl.music.activities.live.FragmentContainerActivityV2;
import cn.tcl.music.fragments.RecyclerFragment;
import cn.tcl.music.model.live.CollectionBean;
import cn.tcl.music.model.live.LiveMusicCollectListBean;
import cn.tcl.music.network.LiveMusicCollectListTask;
import cn.tcl.music.util.Util;

public class LiveMusicCollectSongListFragment extends RecyclerFragment implements RecyclerFragment.OnLoadMoreListener {
    private static final String TAG = LiveMusicCollectSongListFragment.class.getSimpleName();
    private int mPageIndex = 1;

    private static final int MARGIN = 4;

    private LiveMusicCollectListTask mLiveMusicCollectListTask;

    public static void launch(Activity activity) {
        FragmentContainerActivityV2.launch(activity, LiveMusicCollectSongListFragment.class);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setCustomView();
    }

    @Override
    protected String getTitle() {
        return getTheString(R.string.hidden_list);
    }

    @Override
    protected void initViews() {
        super.initViews();
        addLoadMoreListener(this);
        cancelTask(mLiveMusicCollectListTask);
        mLiveMusicCollectListTask = new LiveMusicCollectListTask(getActivity(), this);
        mLiveMusicCollectListTask.executeMultiTask();
        mPageIndex = 1;
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

        //first load
        if (mPageIndex == 1) {
            hideLoading();
            showContent();

            if (datas == null || datas.size() == 0) {
                showNoData();
                return;
            }
        }
        //load more
        else {
            finishBottomLoading();
        }

        if (null == datas) {
            return;
        }

        if (datas.isEmpty()) {
            return;
        }

        showData(datas);
        ++mPageIndex;
    }

    @Override
    public void onLoadMore() {
        cancelTask(mLiveMusicCollectListTask);
        mLiveMusicCollectListTask = new LiveMusicCollectListTask(getActivity(), this);
        mLiveMusicCollectListTask.executeMultiTask();
    }

    /**
     * create item view
     *
     * @return
     */
    @Override
    public RecyclerView.ViewHolder onItemHolderCreated() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.item_online_collect_song_list, null);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
        int margin = Util.dip2px(getActivity(), MARGIN);
        params.topMargin = margin;
        params.leftMargin = margin;
        params.rightMargin = margin;
        params.setMarginEnd(margin);
        params.setMarginStart(margin);
        view.setLayoutParams(params);
        return new SongListViewHolder(view);
    }

    /**
     * bind item view
     */
    @Override
    public void onItemHolderBinded(RecyclerView.ViewHolder holder, int position, Object item) {
        if (holder != null && item != null && holder instanceof SongListViewHolder) {
            SongListViewHolder songListViewHolder = (SongListViewHolder) holder;
            CollectionBean collect = (CollectionBean) item;
            Glide.with(this)
                    .load(ImageUtil.transferImgUrl(collect.collect_logo, 200))
                    .placeholder(R.drawable.default_cover_ranking)
                    .into(songListViewHolder.coverImg);
            songListViewHolder.songListNameTv.setText(collect.collect_name);
            songListViewHolder.playCountTv.setText(collect.play_count);
            songListViewHolder.userNameTv.setText(collect.user_name.trim());

            //click and go to detail
            final String list_id = collect.list_id;
            final String collect_name = collect.collect_name;
            final int collect_count = collect.song_count;

            songListViewHolder.itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OnlinePlayListDetailActivity.launch(getActivity(), list_id, collect_name, collect_count);
                }
            });
        }
    }

    /**
     * recycle item view
     */
    @Override
    public void onItemRecycled(RecyclerView.ViewHolder holder) {
        super.onItemRecycled(holder);

        if (holder != null && holder instanceof SongListViewHolder) {
            // TODO: 16-11-21
        }
    }

    /**
     * item view
     */
    private class SongListViewHolder extends RecyclerView.ViewHolder {
        public ImageView coverImg;
        public TextView songListNameTv;
        public TextView userNameTv;
        public View itemLayout;
        public TextView playCountTv;

        public SongListViewHolder(View itemView) {
            super(itemView);
            coverImg = (ImageView) itemView.findViewById(R.id.online_popular_song_list_cover);
            songListNameTv = (TextView) itemView.findViewById(R.id.online_popular_song_list_name);
            userNameTv = (TextView) itemView.findViewById(R.id.online_popular_song_list_user_name);
            playCountTv = (TextView) itemView.findViewById(R.id.online_popular_song_list_play_number);
            itemLayout = itemView;
        }
    }

    /**
     * show data
     */
    private void showData(List<LiveMusicCollectListBean> datas) {
        addData2RecyclerView(datas.get(0).list);
    }

    @Override
    protected void doReloadData() {
        showLoading();
        mPageIndex = 1;
        cancelTask(mLiveMusicCollectListTask);
        mLiveMusicCollectListTask = new LiveMusicCollectListTask(getActivity(), this);
        mLiveMusicCollectListTask.executeMultiTask();
    }

    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelTask(mLiveMusicCollectListTask);
    }

}
