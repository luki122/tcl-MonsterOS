package cn.tcl.music.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.activities.live.OnlinePlayListDetailActivity;
import cn.tcl.music.activities.live.HotMusicRecommendActivity;
import cn.tcl.music.activities.live.LiveMusicRankFragment;
import cn.tcl.music.activities.live.RankDetailActivity;
import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.fragments.live.ATabTitlePagerFragment;
import cn.tcl.music.fragments.live.HeartRadioListFragment;
import cn.tcl.music.fragments.live.LiveMusicEssenceFragment;
import cn.tcl.music.fragments.live.LiveMusicRadioFragment;
import cn.tcl.music.fragments.live.LiveMusicLatestSongFragment;
import cn.tcl.music.fragments.live.LiveMusicScenesListFragment;
import cn.tcl.music.fragments.live.LiveMusicSongListFragment;
import cn.tcl.music.fragments.live.SingerPageFragment;
import cn.tcl.music.model.live.AlbumBean;
import cn.tcl.music.model.live.ArtistBean;
import cn.tcl.music.model.live.ArtistHotMusicDataBean;
import cn.tcl.music.model.live.CollectionBean;
import cn.tcl.music.model.live.LiveMusicBannerItem;
import cn.tcl.music.model.live.LiveMusicEssence;
import cn.tcl.music.model.live.LiveMusicRadio;
import cn.tcl.music.model.live.LiveMusicRank;
import cn.tcl.music.model.live.LiveMusicRankItem;
import cn.tcl.music.model.live.LiveMusicRecommend;
import cn.tcl.music.model.live.LiveMusicSceneListBean;
import cn.tcl.music.model.live.LiveMusicSinger;
import cn.tcl.music.model.live.RadioBean;
import cn.tcl.music.model.live.RadioCategoryBean;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.network.AlbumDetailTask;
import cn.tcl.music.network.DataRequest;
import cn.tcl.music.network.ILoadData;
import cn.tcl.music.network.LiveMusicArtistHotSongsTask;
import cn.tcl.music.network.LiveMusicBannerTask;
import cn.tcl.music.network.LiveMusicEssenceTask;
import cn.tcl.music.network.LiveMusicPlayTask;
import cn.tcl.music.network.LiveMusicRadioCategoriesTask;
import cn.tcl.music.network.LiveMusicRadioDetailTask;
import cn.tcl.music.network.LiveMusicRadioTask;
import cn.tcl.music.network.LiveMusicRankTask;
import cn.tcl.music.network.LiveMusicRecommendTask;
import cn.tcl.music.network.LiveMusicSceneBannerTask;
import cn.tcl.music.network.RankDetailTask;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.PreferenceUtil;
import cn.tcl.music.util.ToastUtil;
import cn.tcl.music.util.Util;
import cn.tcl.music.util.live.OrderUtils;
import cn.tcl.music.util.live.TextFormatUtils;
import cn.tcl.music.view.AuditionAlertDialog;
import cn.tcl.music.view.BannerFlipView;
import cn.tcl.music.view.BannerFlipView_v1;
import cn.tcl.music.view.LiveMusicItemLayout_v3;
import cn.tcl.music.view.LiveMusicItemLayout_v4;
import cn.tcl.music.view.LiveMusicItemLayout_v5;
import cn.tcl.music.view.LiveMusicItemLayout_v6;

public class OnlineFragment extends NetWorkBaseFragment
        implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = OnlineFragment.class.getSimpleName();
    private static final int ESSENCE_COLLECTION_PAGE = 1;
    private static final int RADIO_LIST_SIZE = 50;
    private List<LiveMusicRankItem> mTempRankItems = new ArrayList<LiveMusicRankItem>();
    private int mLoadedCount = 0;

    private List mBannerDatas;
    private List mSceneBannerDatas;
    private SwipeRefreshLayout mRefreshLayout;

    private static ILoadData mListener;
    private List<SongDetailBean> mSongDetailBeanList;
    private List<AlbumBean> mLatestDatas;
    private List<LiveMusicSinger> mSingerDatas;
    private List<LiveMusicRadio> mRadioDatas;
    private List<LiveMusicEssence> mEssenceDatas;

    private BannerFlipView mBannerView;
    private LiveMusicItemLayout_v3 mTopLay;
    private BannerFlipView_v1 mSceneBannerView;
    private LiveMusicItemLayout_v5 mRadioLay;
    private LiveMusicItemLayout_v4 mEssenceLay;
    private LiveMusicItemLayout_v6 mPopRankLay;

    private LiveMusicBannerTask mBannerTask;
    private LiveMusicRankTask mRankTask;
    private LiveMusicSceneBannerTask mScenceBannerTask;
    private LiveMusicRadioTask mLiveMusicRadioTask;
    private LiveMusicEssenceTask mEssenceTask;
    private LiveMusicRadioCategoriesTask mRadioCategoriesTask;

    @Override
    protected int getSubContentLayout() {
        return R.layout.fragment_online;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            mLoadedCount = 0;
            showLoading();
            showContent();
            loadData();
        } else {
            if (mBannerTask != null) {
                mBannerTask.cancel(true);
            }
            if (mScenceBannerTask != null) {
                mScenceBannerTask.cancel(true);
            }
            if (mRankTask != null) {
                mRankTask.cancel(true);
            }
            if (mLiveMusicRadioTask != null) {
                mLiveMusicRadioTask.cancel(true);
            }
            if (mEssenceTask != null) {
                mEssenceTask.cancel(true);
            }
            if (mRadioCategoriesTask != null) {
                mRadioCategoriesTask.cancel(true);
            }
        }
    }
    //[BUGFIX]-ADD-END by yuanxi.jiang for PR1930069 on 2016/5/16

    @Override
    protected void findViewByIds(View parent) {
        super.findViewByIds(parent);
        mBannerView = (BannerFlipView) parent.findViewById(R.id.banner_view);

        mTopLay = (LiveMusicItemLayout_v3) parent.findViewById(R.id.top_songs);
        mSceneBannerView = (BannerFlipView_v1) parent.findViewById(R.id.commend_banner_view);

        mEssenceLay = (LiveMusicItemLayout_v4) parent.findViewById(R.id.recommend_hot_songs_lay);
        mEssenceLay.setChildCount(6);

        mRadioLay = (LiveMusicItemLayout_v5) parent.findViewById(R.id.pop_radio_lay);
        mRadioLay.setChildCount(6);

        mPopRankLay = (LiveMusicItemLayout_v6) parent.findViewById(R.id.pop_rank_lay);

        mRefreshLayout = (SwipeRefreshLayout) parent.findViewById(R.id.refresh_layout);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeResources(
                android.R.color.holo_green_light,
                android.R.color.holo_red_light,
                android.R.color.holo_blue_bright,
                android.R.color.holo_orange_light);

        parent.findViewById(R.id.latest_song_icon_lay).setOnClickListener(this);
        parent.findViewById(R.id.rank_list_icon_lay).setOnClickListener(this);
        parent.findViewById(R.id.radio_icon_lay).setOnClickListener(this);
        parent.findViewById(R.id.scenes_music_more).setOnClickListener(this);
        parent.findViewById(R.id.scenes_music_more_img).setOnClickListener(this);
        parent.findViewById(R.id.recommend_hot_songs_more).setOnClickListener(this);
        parent.findViewById(R.id.recommend_hot_songs_more_img).setOnClickListener(this);
        parent.findViewById(R.id.pop_rank_more).setOnClickListener(this);
        parent.findViewById(R.id.pop_rank_more_img).setOnClickListener(this);
        parent.findViewById(R.id.pop_radio_more).setOnClickListener(this);
        parent.findViewById(R.id.pop_radio_more_img).setOnClickListener(this);

        setImgClickListener();

        if (mTempRankItems == null) {
            mTempRankItems = new ArrayList<LiveMusicRankItem>();
        }
        mTempRankItems.clear();
    }


    @Override
    protected void initViews() {
        super.initViews();

    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        getActivity().registerReceiver(mNetworkStateChangeReceiver, filter);

        //start banner
        if (mBannerView != null) {
            mBannerView.startFlip();
        }
        if (mSceneBannerView != null) {
            mSceneBannerView.startFlip();
        }
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(mNetworkStateChangeReceiver);
        //stop banner
        if (mBannerView != null) {
            mBannerView.stopFlip();
        }
        if (mSceneBannerView != null) {
            mSceneBannerView.stopFlip();
        }
        super.onPause();
    }

    @Override
    public void onClick(View v) {
//        MobileNetworkDialog dialog = null;
//        if (isAdded() && !getActivity().isFinishing()) {
//            dialog = MobileNetworkDialog.getInstance(getActivity());
//            if (null != dialog && dialog.showWrapper()) {
//                return;
//            }
//        }
        switch (v.getId()) {
            case R.id.latest_song_icon_lay:
                //gotoLatestSongList();
                gotoSongList();
                break;
            case R.id.rank_list_icon_lay:
                gotoRankList();
                break;
            case R.id.radio_icon_lay:
                gotoRadioList();
                break;
            case R.id.scenes_music_more:
            case R.id.scenes_music_more_img:
                gotoScenesRadioList();
                break;
            case R.id.recommend_hot_songs_more:
            case R.id.recommend_hot_songs_more_img:
                //gotoEssenceList();
                gotoSongList();
                break;
            case R.id.pop_rank_more:
            case R.id.pop_rank_more_img:
                gotoRankList();
                break;
            case R.id.pop_radio_more:
            case R.id.pop_radio_more_img:
                gotoRadioList();
                break;

        }

    }


    ArrayList<RadioCategoryBean> categoryBeans = new ArrayList<RadioCategoryBean>();

    @Override
    public void onLoadSuccess(int dataType, List datas) {
        super.onLoadSuccess(dataType, datas);
        ++mLoadedCount;

        if (mLoadedCount > 2) {
            hideLoading();

            if (mRefreshLayout != null) {
                mRefreshLayout.setRefreshing(false);
            }
        }

        if (datas == null || datas.size() == 0 || isDetached() || isRemoving() || getActivity() == null) {
            return;
        }

        switch (dataType) {
            case DataRequest.Type.TYPE_LIVE_BANNER:
                mBannerDatas = datas;
                showBanner(datas);
                break;
            case DataRequest.Type.TYPE_LIVE_SCENE_BANNER:
                mSceneBannerDatas = datas;
                LiveMusicSceneListBean bean = (LiveMusicSceneListBean) datas.get(0);
                showSceneBanner(datas);
                break;
            case DataRequest.Type.TYPE_LIVE_RANK:
                showRank(datas);
                break;
            case DataRequest.Type.TYPE_LIVE_RADIO:
                mRadioDatas = datas;
                showRadio(datas);
                break;
            case DataRequest.Type.TYPE_LIVE_ESSENCE:
                mEssenceDatas = datas;
                showEssence(datas);
                break;
            case DataRequest.Type.TYPE_LIVE_RADIO_CATEGORES:
                ArrayList<RadioCategoryBean> tempData = (ArrayList<RadioCategoryBean>) datas;
                categoryBeans.clear();
                categoryBeans.add(tempData.get(0));
                categoryBeans.add(tempData.get(1));
                categoryBeans.add(tempData.get(2));
                categoryBeans.add(tempData.get(3));
                break;
            default:
                break;
        }

    }

    @Override
    public void onLoadFail(int dataType, String message) {
        super.onLoadFail(dataType, message);
        ++mLoadedCount;
        hideLoading();

        if (mRefreshLayout != null) {
            mRefreshLayout.setRefreshing(false);
        }

        LogUtil.i(TAG, "--onLoadFail--" + message);
    }

    @Override
    public void onRefresh() {
        loadData();
    }

    /**
     * Load network data
     */
    private void loadData() {
        showTopLay();

        mBannerTask = new LiveMusicBannerTask(getActivity(), this);
        mBannerTask.executeMultiTask();


        mRankTask = new LiveMusicRankTask(getActivity(), this);
        mRankTask.executeMultiTask();

        mScenceBannerTask = new LiveMusicSceneBannerTask(getActivity(), this);
        mScenceBannerTask.executeMultiTask();

        mLiveMusicRadioTask = new LiveMusicRadioTask(getActivity(), this, RADIO_LIST_SIZE);
        mLiveMusicRadioTask.executeMultiTask();

        mEssenceTask = new LiveMusicEssenceTask(getActivity(), this, ESSENCE_COLLECTION_PAGE);
        mEssenceTask.executeMultiTask();

        mRadioCategoriesTask = new LiveMusicRadioCategoriesTask(getActivity(), this);
        mRadioCategoriesTask.executeMultiTask();
    }

    private void showTopLay() {
        LiveMusicItemLayout_v3.LiveItem item = null;
        if (mTopLay != null) {
            item = new LiveMusicItemLayout_v3.LiveItem();
            item.name = getContext().getResources().getString(R.string.live_new_album_title);
            item.imgId = R.drawable.new_album_icon;
            mTopLay.bindChild(item, 0, getActivity());

            item = new LiveMusicItemLayout_v3.LiveItem();
            item.name = getContext().getResources().getString(R.string.wechat_shared_rank_title);
            item.imgId = R.drawable.weixin_rank_icon;
            mTopLay.bindChild(item, 1, getActivity());

            item = new LiveMusicItemLayout_v3.LiveItem();
            item.name = getContext().getResources().getString(R.string.live_own_radio_title);
            item.imgId = R.drawable.owner_radio_icon;
            mTopLay.bindChild(item, 2, getActivity());
        }
    }

    /**
     * set banner
     */
    private void showBanner(List<LiveMusicBannerItem> datas) {
        if (mBannerView != null) {
            List<LiveMusicBannerItem> list = new ArrayList<>();
            list.addAll(datas);
            mBannerView.setDisplayImgs(list);
            mBannerView.startFlip();
        }
    }

    /**
     * set scene recommend banner
     */
    private void showSceneBanner(List<LiveMusicSceneListBean> datas) {
        if (mSceneBannerView != null) {
            List<LiveMusicSceneListBean> list = new ArrayList<>();

            final int size = datas.size();
            for (int i = 0; i < size; i++) {
                final String title = datas.get(i).title;
                final int id = datas.get(i).radio_id;
                final int type = datas.get(i).radio_type;

                if ((id == 964 && type == 13) || (id == 8 && type == 17) || (id == 4 && type == 17)) {
                    list.add(datas.get(i));
                }
            }
            mSceneBannerView.setDisplayImgs(list);

            mSceneBannerView.startFlip();
        }
    }

    /**
     * set Essence
     */
    private void showEssence(List<LiveMusicEssence> datas) {
        LiveMusicEssence data = datas.get(0);
        int size = data.collects != null ? data.collects.size() : 0;
        List<LiveMusicItemLayout_v4.LiveItem> list = new ArrayList<LiveMusicItemLayout_v4.LiveItem>();
        LiveMusicItemLayout_v4.LiveItem item = null;

        OrderUtils.orderCollectByPalycount(data.collects);
        for (int i = 0; i < 6; i++) {
            item = new LiveMusicItemLayout_v4.LiveItem();
            if (!MusicApplication.getApp().isDataSaver()) {
                item.imgUrl = data.collects.get(i).collect_logo;
            }
            item.name = data.collects.get(i).collect_name;
            item.count = TextFormatUtils.formatPalyCount(getContext(), Integer.parseInt(data.collects.get(i).play_count));
            list.add(item);
        }

        if (mEssenceLay != null) {
            mEssenceLay.showData(list, getActivity());
        }
    }

    /**
     * set Rank
     */
    private void showRank(List<LiveMusicRank> ranks) {
        int size = ranks.size();
        List<LiveMusicItemLayout_v6.LiveItem> list = new ArrayList<LiveMusicItemLayout_v6.LiveItem>();
        LiveMusicItemLayout_v6.LiveItem item = null;
        LiveMusicItemLayout_v6.SongsDetail itemDetail = null;
        LiveMusicRank rank = null;
        mTempRankItems.clear();

        for (int i = 0; i < size; i++) {
            rank = ranks.get(i);

            if (rank == null) {
                continue;
            }

            addRankToTemp(rank.items);
        }

        for (int i = 0; i < mTempRankItems.size(); i++) {
            final String type = mTempRankItems.get(i).type;
            if (CommonConstants.LIVE_RANK_CATE_MUSIC_COLLECT.equals(type)
                    | CommonConstants.LIVE_RANK_CATE_XIAMI_WEIBO.equals(type)
                    | CommonConstants.LIVE_RANK_CATE_MUSIC_ORIGINAL.equals(type)) {
                LiveMusicRankItem rankItem = mTempRankItems.get(i);
                item = new LiveMusicItemLayout_v6.LiveItem();
//                item.imgUrl = rankItem.logo;
                item.title = rankItem.title;
                item.type = type;
                for (int j = 0; j < 3; j++) {
                    itemDetail = new LiveMusicItemLayout_v6.SongsDetail();
                    itemDetail.name = rankItem.songs.get(j).song_name;
                    itemDetail.artistName = rankItem.songs.get(j).artist_name;
                    itemDetail.imgUrl = rankItem.songs.get(j).album_logo;
                    item.songsDetail.add(itemDetail);
                }
                if (!MusicApplication.getApp().isDataSaver()) {
                    item.imgUrl = item.songsDetail.get(0).imgUrl;
                }
                list.add(item);
            }
        }

        if (mPopRankLay != null) {
            mPopRankLay.showData(list, getActivity());
        }
    }

    private LiveMusicRankItem weixinRankitem;

    private void addRankToTemp(List<LiveMusicRankItem> list) {
        if (list != null) {
            LiveMusicRankItem item = null;
            String filter1 = getTheString(R.string.rank_filter1);
            String filter2 = getTheString(R.string.rank_filter2);
            for (int i = 0; i < list.size(); i++) {
                item = list.get(i);
                if (item != null && item.songs.size() > 0) {
                }
                if (item != null && !filter1.equals(item.title) && !filter2.equals(item.title)) {
                    mTempRankItems.add(item);
                    if (CommonConstants.LIVE_RANK_CATE_XIAMI_WECHAT.equals(item.type)) {
                        weixinRankitem = new LiveMusicRankItem();
                        weixinRankitem.title = item.title;
                        weixinRankitem.type = item.type;
                        weixinRankitem.total = item.total;
                    }
                }
            }
        }
    }

    /**
     * set Radio
     */
    private void showRadio(List<LiveMusicRadio> datas) {
        LiveMusicRadio radio = datas.get(0);
        int size = radio.radios != null ? radio.radios.size() : 0;
        List<LiveMusicItemLayout_v5.LiveItem> list = new ArrayList<LiveMusicItemLayout_v5.LiveItem>();
        LiveMusicItemLayout_v5.LiveItem item;
        List<RadioBean> radios = radio.radios;
        OrderUtils.orderRadioByPalycount(radios);

        for (int i = 0; i < 6; i++) {
            item = new LiveMusicItemLayout_v5.LiveItem();
            if (!MusicApplication.getApp().isDataSaver()) {
                item.imgUrl = radios.get(i).radio_logo;
            }
            item.name = radios.get(i).radio_name;
            list.add(item);
        }

        if (mRadioLay != null) {
            mRadioLay.showData(list, getActivity());
        }
    }

    private void showCachedDatas() {
        if (mBannerDatas != null) {
            if (mBannerView != null) {
                mBannerView.stopFlip();
            }

            showBanner(mBannerDatas);
        }

        if (mSceneBannerDatas != null) {
            showSceneBanner(mSceneBannerDatas);
        }

        if (mRadioDatas != null) {
            showRadio(mRadioDatas);
        }

        if (mEssenceDatas != null) {
            showEssence(mEssenceDatas);
        }
    }


    private void setImgClickListener() {
        if (isAdded() && !getActivity().isFinishing()) {
            mTopLay.addImgClickListener(new LiveMusicItemLayout_v3.LiveMusicClickListener() {
                @Override
                public void onImgClick(Context context, int position) {
                    if (position == 0) {
                        //MobclickAgent.onEvent(getActivity(),MobConfig.MAIN_HOT_MUSIC_IMAGE_ONE);
                    } else {
                        //MobclickAgent.onEvent(getActivity(),MobConfig.MAIN_HOT_MUSIC_IMAGE_TWO);
                    }

//                    MobileNetworkDialog dialog = MobileNetworkDialog.getInstance(context);
//
//                    if (null != dialog && dialog.showWrapper()) {
//                        return;
//                    }

                    switch (position) {
                        case 0:
                            gotoLatestSongList();
                            break;
                        case 1:
                            if (weixinRankitem != null) {
                                String title = Util.replaceStringTags(getActivity(), weixinRankitem.title);
                                RankDetailActivity.launch(getActivity(), weixinRankitem.type, title, weixinRankitem.total);
                                weixinRankitem = null;
                            }
                            break;
                        case 2:
                            //Play Music
                            break;
                    }
                }
            });

            mEssenceLay.addImgClickListener(new LiveMusicItemLayout_v4.LiveMusicClickListener() {
                @Override
                public void onImgClick(Context context, int position) {
                    if (position == 0) {
                    } else {
                    }

//                    MobileNetworkDialog dialog = MobileNetworkDialog.getInstance(context);
//
//                    if (null != dialog && dialog.showWrapper()) {
//                        return;
//                    }

                    gotoEssenceDetail(position);
                }
            });
        }
    }

    private void gotoSongList() {
        LiveMusicSongListFragment.launch(getActivity());
    }

    private void gotoLatestSongList() {
        LiveMusicLatestSongFragment.launch(getActivity());
    }

    private void gotoEssenceDetail(int position) {
        LiveMusicEssence essence = mEssenceDatas != null ? mEssenceDatas.get(0) : null;

        if (essence != null) {
            int size = essence.collects != null ? essence.collects.size() : 0;

            if (size > 0 && position > -1 && position < size) {
                //click into detail
                CollectionBean collectionBean = essence.collects.get(position);
                OnlinePlayListDetailActivity.launch(getActivity(), collectionBean.list_id, collectionBean.collect_name, collectionBean.song_count);

            }
        }

    }

    private void gotoRankDetail(int position) {
        if (mTempRankItems != null) {
            int size = mTempRankItems.size();

            if (size > 0 && position > -1 && position < size) {
                LiveMusicRankItem rank = mTempRankItems.get(position);

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
                                    LogUtil.i("yshhot", "songDetailBeanList加入");
                                    AuditionAlertDialog.getInstance(getActivity()).showWrapper(false, new AuditionAlertDialog.OnSelectedListener() {
                                        @Override
                                        public void onPlay() {
                                            new LiveMusicPlayTask(getActivity()).playNow(mSongDetailBeanList, 0, true);
                                            //isFirstClick = false;
                                        }
                                    });
                                } else {
                                    LogUtil.i("yshhot", "songDetailBeanList is null");
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
                new RankDetailTask(getActivity(), mListener, rank.type).executeMultiTask();

            }
        }
    }

    private void gotoLatestSongDetail(int position) {
        int size = mLatestDatas != null ? mLatestDatas.size() : 0;

        if (position > -1 && position < size && size > 0) {
            AlbumBean albumBean = mLatestDatas.get(position);
            LogUtil.i("yshhot", "gotoLatestSongDetail albumBean: " + albumBean.album_id);
            mListener = new ILoadData() {
                @Override
                public void onLoadSuccess(int dataType, List datas) {
                    //todo cancel task!!!
                    LogUtil.i("yshhot", "gotoLatestSongDetail onLoadSuccess   " + datas.size());
                    if (datas != null && datas.size() > 0) {
                        LogUtil.i("yshhot", "dataType== " + dataType);
                        if (DataRequest.Type.TYPE_LIVE_ALBUM_DETAIL == dataType) {
                            LogUtil.i("yshhot", "DataRequest.Type.TYPE_LIVE_ALBUM_DETAIL == dataType");
                            AlbumBean collectionBean = (AlbumBean) datas.get(0);
                            if (null == collectionBean.songs) {
                                return;
                            }
                            if (mSongDetailBeanList == null) {
                                mSongDetailBeanList = new ArrayList<SongDetailBean>();
                            }
                            mSongDetailBeanList.clear();
                            mSongDetailBeanList.addAll(collectionBean.songs);

                            if (mSongDetailBeanList != null) {
                                LogUtil.i("yshhot", "songDetailBeanList加入");
                                AuditionAlertDialog.getInstance(getActivity()).showWrapper(false, new AuditionAlertDialog.OnSelectedListener() {
                                    @Override
                                    public void onPlay() {
                                        new LiveMusicPlayTask(getActivity()).playNow(mSongDetailBeanList, 0, true);
                                        //isFirstClick = false;
                                    }
                                });
                            } else {
                                LogUtil.i("yshhot", "songDetailBeanList is null");
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
            new AlbumDetailTask(getActivity(), mListener, albumBean.album_id).executeMultiTask();

        }
    }

    private void gotoRadioList(int position) {
        LogUtil.i("yshhot", "gotoRadioList");
        if (mRadioDatas != null) {
            LiveMusicRadio radio = mRadioDatas.get(0);

            if (radio != null) {
                int size = radio.radios != null ? radio.radios.size() : 0;

                if (size > 0 && position > -1 && position < size) {
                    RadioBean radioBeanBean = radio.radios.get(position);
                    LogUtil.i("yshhot", "radioBeanBean  " + radioBeanBean.radio_id + " type: " + radioBeanBean.category_type);
                    mListener = new ILoadData() {
                        @Override
                        public void onLoadSuccess(int dataType, List datas) {
                            //todo cancel task!!!
                            LogUtil.i("yshhot", "gotoRadioList onLoadSuccess   " + datas.size());
                            if (datas != null && datas.size() > 0) {
                                LogUtil.i("yshhot", "dataType== " + dataType);
                                if (DataRequest.Type.TYPE_LIVE_RADIO_DETAIL == dataType) {
                                    final List<SongDetailBean> songs = datas;

                                    if (songs != null && songs.size() > 0) {
                                        if (isAdded()) {
                                            AuditionAlertDialog.getInstance(getActivity()).showWrapper(false, new AuditionAlertDialog.OnSelectedListener() {
                                                @Override
                                                public void onPlay() {
                                                    new LiveMusicPlayTask(getActivity()).playNow(songs, 0, true);
                                                }
                                            });
                                        }
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
                    new LiveMusicRadioDetailTask(getActivity(), mListener, radioBeanBean.radio_id, radioBeanBean.category_type).executeMultiTask();
                }
            }
        }

    }


    private void gotoSingerDetail(int position) {
        if (mSingerDatas != null) {
            LiveMusicSinger singer = mSingerDatas.get(0);

            if (singer != null) {
                int size = singer.artists != null ? singer.artists.size() : 0;

                if (size > 0 && position > -1 && position < size) {
                    ArtistBean artistBean = singer.artists.get(position);
                    mListener = new ILoadData() {
                        @Override
                        public void onLoadSuccess(int dataType, List datas) {
                            //todo cancel task!!!
                            if (datas != null && datas.size() > 0) {
                                if (DataRequest.Type.TYPE_LIVE_ARTIST_HOT_SONGS == dataType) {
                                    ArtistHotMusicDataBean artistHotMusicDataBean = (ArtistHotMusicDataBean) datas.get(0);
                                    if (artistHotMusicDataBean.songs == null) {
                                        return;
                                    }
                                    if (mSongDetailBeanList == null) {
                                        mSongDetailBeanList = new ArrayList<>();
                                    }
                                    mSongDetailBeanList.clear();
                                    mSongDetailBeanList.addAll(artistHotMusicDataBean.songs);

                                    if (mSongDetailBeanList != null) {
                                        AuditionAlertDialog.getInstance(getActivity()).showWrapper(false, new AuditionAlertDialog.OnSelectedListener() {
                                            @Override
                                            public void onPlay() {
                                                new LiveMusicPlayTask(getActivity()).playNow(mSongDetailBeanList, 0, true);
                                                //isFirstClick = false;
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
                    new LiveMusicArtistHotSongsTask(getActivity(), mListener, artistBean.artist_id, 1).executeMultiTask();
                }
            }
        }
    }

    private void gotoEssenceList() {
        LiveMusicEssenceFragment.launch(getActivity());
    }

    private void gotoRankList() {
        LiveMusicRankFragment.launch(getActivity(), mTempRankItems);
    }

    private void gotoRadioList() {
        //RaidoPageFragment.launch(getActivity(), categoryBeans);
        LiveMusicRadioFragment.launch(getActivity());
    }

    private void gotoScenesRadioList() {
        LiveMusicScenesListFragment.launch(getActivity());
    }

    private void gotoHotSongRecommendList(int position) {
        final int mposition = position;

        mListener = new ILoadData() {
            @Override
            public void onLoadSuccess(int dataType, List datas) {
                //todo cancel task!!!
                if (datas != null && datas.size() > 0) {
                    if (DataRequest.Type.TYPE_LIVE_RECOMMEND == dataType) {

                        LiveMusicRecommend liveMusicRecommend = (LiveMusicRecommend) datas.get(0);
                        if (liveMusicRecommend.songs == null || liveMusicRecommend.songs.size() == 0) {
                            return;
                        }
                        if (mSongDetailBeanList == null) {
                            mSongDetailBeanList = new ArrayList<SongDetailBean>();
                        }
                        mSongDetailBeanList.clear();
                        mSongDetailBeanList.addAll(liveMusicRecommend.songs);

                        if (mSongDetailBeanList != null) {
                            AuditionAlertDialog.getInstance(getActivity()).showWrapper(false, new AuditionAlertDialog.OnSelectedListener() {
                                @Override
                                public void onPlay() {
                                    new LiveMusicPlayTask(getActivity()).playNow(mSongDetailBeanList, mposition, true);
                                    //isFirstClick = false;
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
        new LiveMusicRecommendTask(getActivity(), mListener, 50).executeMultiTask();
    }


    private void gotoHotSongRecommendListDetail(int position) {
        Intent intent = new Intent(getContext(), HotMusicRecommendActivity.class);
        intent.putExtra("position", position);
        startActivity(intent);
    }

    private void gotoDailyRecommendList() {
        Intent intent = new Intent(getContext(), HotMusicRecommendActivity.class);
        intent.putExtra("position", 0);
        intent.setAction("daily_recommend");
        startActivity(intent);
    }

    private void gotoHeartRadioList() {
        ATabTitlePagerFragment.TabTitlePagerBean bean = new ATabTitlePagerFragment.TabTitlePagerBean();
        bean.setTitle(getString(R.string.heart_radio));
        bean.setType(String.valueOf(4));
        HeartRadioListFragment.launch(getActivity(), bean);
    }

    private void gotoSingerList() {
        SingerPageFragment.launch(getActivity());
    }

    private BroadcastReceiver mNetworkStateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isBreak = intent.getBooleanExtra(
                    ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            if (!isBreak) {
                showLoading();
                loadData();
                if (Util.getNetworkType() != Util.NETTYPE_WIFI) {
                    hideLoading();
                }
            }
        }
    };
}