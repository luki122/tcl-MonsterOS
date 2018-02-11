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
import com.xiami.sdk.utils.ImageUtil;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.activities.live.OnlinePlayListDetailActivity;
import cn.tcl.music.activities.live.FragmentContainerActivityV2;
import cn.tcl.music.fragments.RecyclerFragment;
import cn.tcl.music.model.live.CollectionBean;
import cn.tcl.music.model.live.LiveMusicEssence;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.network.DataRequest;
import cn.tcl.music.network.ILoadData;
import cn.tcl.music.network.LiveMusicCollectDetailTask;
import cn.tcl.music.network.LiveMusicEssenceTask;
import cn.tcl.music.network.LiveMusicPlayTask;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.ToastUtil;
import cn.tcl.music.util.Util;
import cn.tcl.music.view.AuditionAlertDialog;

public class LiveMusicEssenceFragment extends RecyclerFragment implements RecyclerFragment.OnLoadMoreListener {
    private static final String TAG = LiveMusicEssenceFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private int mPageIndex = 1;
    private static ILoadData mListener;
    private List<SongDetailBean> mSongDetailBeanList;
    private String mDescription;
    private String mAlbumName;
    private boolean mIsFirstClick;
    private String mCollectName;

    private LiveMusicEssenceTask mLiveMusicEssenceTask;

    public static void launch(Activity activity) {
        FragmentContainerActivityV2.launch(activity, LiveMusicEssenceFragment.class);
    }

    @Override
    protected String getTitle() {
        return getTheString(R.string.essence_music);
    }

    @Override
    protected void initViews() {
        super.initViews();
        addLoadMoreListener(this);
        cancelTask(mLiveMusicEssenceTask);
        mLiveMusicEssenceTask = new LiveMusicEssenceTask(getActivity(), this, 1);
        mLiveMusicEssenceTask.executeMultiTask();
        mPageIndex = 1;
        mIsFirstClick = true;
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

        if (mPageIndex == 1) {
            hideLoading();
            showContent();

            if (datas == null || datas.size() == 0) {
                showNoData();
                return;
            }
        } else {
            finishBottomLoading();
        }

        if (datas == null || datas.size() == 0) {
            return;
        }

        showData(datas);
        ++mPageIndex;
    }

    @Override
    public void onLoadMore() {
        cancelTask(mLiveMusicEssenceTask);
        mLiveMusicEssenceTask = new LiveMusicEssenceTask(getActivity(), this, mPageIndex);
        mLiveMusicEssenceTask.executeMultiTask();
    }

    /**
     * create item view
     *
     * @return
     */
    @Override
    public RecyclerView.ViewHolder onItemHolderCreated() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.item_live_music_essence, null);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
        int margin = Util.dip2px(getActivity(), 4);
        params.topMargin = margin;
        params.leftMargin = margin;
        params.rightMargin = margin;
        params.setMarginEnd(margin);
        params.setMarginStart(margin);
        view.setLayoutParams(params);
        return new EssenceViewHolder(view);
    }

    /**
     * bind item view
     */
    @Override
    public void onItemHolderBinded(RecyclerView.ViewHolder holder, int position, Object item) {
        if (holder != null && item != null && holder instanceof EssenceViewHolder) {
            EssenceViewHolder essenceViewHolder = (EssenceViewHolder) holder;
            CollectionBean collect = (CollectionBean) item;
            Glide.with(this)
                    .load(ImageUtil.transferImgUrl(collect.collect_logo, 200))
                    .placeholder(R.drawable.default_cover_ranking)
                    .into(essenceViewHolder.iconImg);
            essenceViewHolder.nameTv.setText(collect.collect_name);
            essenceViewHolder.descripTv.setText(collect.description.trim());

            //点击进入详情
            final String list_id = collect.list_id;
            final String collect_name = collect.collect_name;
            final int collect_count = collect.song_count;
            essenceViewHolder.playImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getId() == R.id.network_play_all_image) {
                        mListener = new ILoadData() {
                            @Override
                            public void onLoadSuccess(int dataType, List datas) {
                                //todo cancel task!!!
                                boolean hasfound = false;
                                if (datas != null && datas.size() > 0) {
                                    if (DataRequest.Type.TYPE_LIVE_COLLECT_DETAIL == dataType) {

                                        CollectionBean collectionBean = (CollectionBean) datas.get(0);
                                        if (null == collectionBean.songs) {
                                            return;
                                        }
                                        if (mSongDetailBeanList == null) {
                                            mSongDetailBeanList = new ArrayList<SongDetailBean>();
                                        }

                                        if (TextUtils.isEmpty(collectionBean.collect_name)) {
                                            mCollectName = getString(R.string.unknown);
                                        } else {
                                            mCollectName = collectionBean.collect_name;
                                        }
                                        mDescription = collectionBean.description;
                                        mSongDetailBeanList.clear();
                                        mSongDetailBeanList.addAll(collectionBean.songs);
                                        if (mSongDetailBeanList != null) {
                                            AuditionAlertDialog.getInstance(getActivity()).showWrapper(false, new AuditionAlertDialog.OnSelectedListener() {
                                                @Override
                                                public void onPlay() {
                                                    new LiveMusicPlayTask(getActivity()).playNow(mSongDetailBeanList, 0, mIsFirstClick);
                                                    mIsFirstClick = false;
                                                }
                                            });
                                        } else {
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
                        new LiveMusicCollectDetailTask(getActivity(), mListener, list_id).executeMultiTask();
                    }
                }
            });

            essenceViewHolder.itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if (isAdded() && !getActivity().isFinishing()) {
//                        final MobileNetworkDialog dialog = MobileNetworkDialog.getInstance(getActivity());
//                        if (null != dialog && dialog.showWrapper()) {
//                            return;
//                        }
//                    }
                    OnlinePlayListDetailActivity.launch(getActivity(), list_id, collect_name, collect_count);
                }
            });
        }
    }

    /**
     * item view Recycled
     */
    @Override
    public void onItemRecycled(RecyclerView.ViewHolder holder) {
        super.onItemRecycled(holder);

        if (holder != null && holder instanceof EssenceViewHolder) {

        }
    }

    /**
     * item view
     */
    class EssenceViewHolder extends RecyclerView.ViewHolder {
        public ImageView iconImg;
        public TextView nameTv;
        public TextView descripTv;
        public View itemLayout;
        public ImageView playImg;

        public EssenceViewHolder(View itemView) {
            super(itemView);
            iconImg = (ImageView) itemView.findViewById(R.id.icon);
            nameTv = (TextView) itemView.findViewById(R.id.name);
            descripTv = (TextView) itemView.findViewById(R.id.descrip);
            playImg = (ImageView) itemView.findViewById(R.id.network_play_all_image);
            itemLayout = itemView;
        }
    }

    /**
     * show data
     */
    private void showData(List<LiveMusicEssence> datas) {
        addData2RecyclerView(datas.get(0).collects);
    }

    @Override
    protected void doReloadData() {
        showLoading();
        mPageIndex = 1;
        cancelTask(mLiveMusicEssenceTask);
        mLiveMusicEssenceTask = new LiveMusicEssenceTask(getActivity(), this, 1);
        mLiveMusicEssenceTask.executeMultiTask();
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
        cancelTask(mLiveMusicEssenceTask);
    }

}
