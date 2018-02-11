package cn.tcl.music.fragments.live;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiami.sdk.utils.ImageUtil;

import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.activities.live.FragmentContainerActivityV2;
import cn.tcl.music.activities.live.ScenesDetailActivity;
import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.fragments.RecyclerFragment;
import cn.tcl.music.model.live.LiveMusicSceneListBean;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.network.ILoadData;
import cn.tcl.music.network.LiveMusicSceneBannerTask;
import cn.tcl.music.util.Util;

public class LiveMusicScenesListFragment extends RecyclerFragment implements RecyclerFragment.OnLoadMoreListener {
    private static final String TAG = LiveMusicEssenceFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private int mPageIndex = 1;
    private static ILoadData mListener;
    private List<SongDetailBean> mSongDetailBeanList;
    private String mAlbumName;
    private boolean isFirstClick = true;
    private String mCollectName;

    private LiveMusicSceneBannerTask mScenceBannerTask;

    public static void launch(Activity activity) {
        FragmentContainerActivityV2.launch(activity, LiveMusicScenesListFragment.class);
    }

    @Override
    protected void findViewByIds(View parent) {
        super.findViewByIds(parent);
        mRecyclerView = (RecyclerView) parent.findViewById(R.id.recycle_view);
        if (mRecyclerView != null) {
            LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            parms.setMarginEnd(Util.dip2px(getContext(), 10));
            parms.setMarginStart(Util.dip2px(getContext(), 10));
            parms.bottomMargin = Util.dip2px(getContext(), 20);
            final int toolbarHeight = Util.dip2px(getContext(), 40);
            mRecyclerView.setPadding(0, toolbarHeight + Util.dip2px(getContext(), 20),0,0);

            mRecyclerView.setLayoutParams(parms);

            mRecyclerView.setVerticalScrollBarEnabled(false);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setGridLayoutManager();
//        setCustomView();
    }

//    protected void setCustomView(){
//        super.setCustomView();
//        addSearchToolbar();
//    }
    /**
     * 设置标题
     */
    @Override
    protected String getTitle() {
        return getTheString(R.string.scenes_music_title);
    }
    @Override
    protected void initViews() {
        super.initViews();
        addLoadMoreListener(this);
        cancelTask(mScenceBannerTask);
        mScenceBannerTask = new LiveMusicSceneBannerTask(getActivity(), this);
        mScenceBannerTask.executeMultiTask();
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

        //首次加载
        if (mPageIndex == 1) {
            hideLoading();
            showContent();

            if (datas == null || datas.size() == 0) {
                showNoData();
                return;
            }
        }
       /* //加载更多
        else {
            finishBottomLoading();
        }*/

        if (datas == null || datas.size() == 0) {
            return;
        }

        showData(datas);
        ++mPageIndex;
    }

    @Override
    public void onLoadMore() {
    }

    /**
     * 创建item view
     *
     * @return
     */
    @Override
    public RecyclerView.ViewHolder onItemHolderCreated() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.item_scenes_radio_list, null);
        final int imgWidth = (getResources().getDisplayMetrics().widthPixels - Util.dip2px(getContext(), 60)/2);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(imgWidth, RecyclerView.LayoutParams.WRAP_CONTENT);
        //RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
        int margin = Util.dip2px(getActivity(), 10);
        params.topMargin = Util.dip2px(getActivity(), 20);
        params.leftMargin = Util.dip2px(getActivity(), 20);
        params.setMarginEnd(margin);
        params.setMarginStart(margin);
        view.setLayoutParams(params);
        return new RadioListViewHolder(view);
    }

    /**
     * 绑定item view
     */
    @Override
    public void onItemHolderBinded(RecyclerView.ViewHolder holder, int position, Object item) {
        if (holder != null && item != null && holder instanceof RadioListViewHolder) {
            RadioListViewHolder radioListViewHolder = (RadioListViewHolder) holder;
            final LiveMusicSceneListBean radioScenebean = (LiveMusicSceneListBean) item;
            if (!MusicApplication.getApp().isDataSaver()) {
                Glide.with(this)
                        .load(ImageUtil.transferImgUrl(radioScenebean.logo, 200))
                        .placeholder(R.drawable.default_cover_ranking)
                        .into(radioListViewHolder.logoImg);
            } else {
                Glide.with(this).load("").placeholder(R.drawable.default_cover_ranking).into(radioListViewHolder.logoImg);
            }
            radioListViewHolder.stateImg.setBackgroundResource(R.drawable.img_play_icon);
            radioListViewHolder.titleTv.setText(radioScenebean.title);

            //点击进入详情
            radioListViewHolder.itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if (isAdded() && !getActivity().isFinishing()) {
//                        final MobileNetworkDialog dialog = MobileNetworkDialog.getInstance(getActivity());
//                        if (null != dialog && dialog.showWrapper()) {
//                            return;
//                        }
//                    }
                    ScenesDetailActivity.launch(getActivity(), radioScenebean);
                }
            });
        }
    }

    /**
     * item view 回收
     */
    @Override
    public void onItemRecycled(RecyclerView.ViewHolder holder) {
        super.onItemRecycled(holder);

        if (holder != null && holder instanceof RadioListViewHolder) {

        }
    }

    /**
     * item view
     */
    class RadioListViewHolder extends RecyclerView.ViewHolder {
        public ImageView logoImg;
        public TextView titleTv;
        public ImageView stateImg;
        public View itemLayout;

        public RadioListViewHolder(View itemView) {
            super(itemView);
            final int imgWidth = (getResources().getDisplayMetrics().widthPixels - Util.dip2px(getContext(), 60))/2;
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(imgWidth, imgWidth);
            logoImg = (ImageView) itemView.findViewById(R.id.online_scenes_radio_logo);
            logoImg.setLayoutParams(params);
            titleTv = (TextView) itemView.findViewById(R.id.online_scenes_radio_title);
            stateImg = (ImageView) itemView.findViewById(R.id.online_scenes_radio_state);
            itemLayout = itemView;
        }
    }

    /**
     * show data
     */
    private void showData(List<LiveMusicSceneListBean> datas) {
        addData2RecyclerView(datas);
    }

    @Override
    protected void doReloadData() {
        showLoading();
        mPageIndex = 1;
        cancelTask(mScenceBannerTask);
        mScenceBannerTask = new LiveMusicSceneBannerTask(getActivity(), this);
        mScenceBannerTask.executeMultiTask();
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
        cancelTask(mScenceBannerTask);
    }

}
