package cn.tcl.music.fragments.live;

import cn.tcl.music.util.ToastUtil;
import mst.app.dialog.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.download.mie.base.util.DownloadManager;
import cn.download.mie.downloader.IDownloader;
import cn.tcl.music.R;
import cn.tcl.music.activities.live.AlbumDetailActivity;
import cn.tcl.music.activities.live.NetworkBatchActivity;
import cn.tcl.music.activities.live.RankDetailActivity;
import cn.tcl.music.activities.live.SingerDetailActivity;
import cn.tcl.music.adapter.live.RankDetailAdapter;
import cn.tcl.music.model.MediaInfo;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.network.LiveMusicPlayTask;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.MusicUtil;
import cn.tcl.music.util.SystemUtility;
import cn.tcl.music.util.live.OnlineUtil;
import cn.tcl.music.view.AuditionAlertDialog;
import cn.tcl.music.view.EmptyLayoutV2;
import cn.tcl.music.view.OnDetailItemClickListener;

public class RankDetailFragment extends Fragment implements
        View.OnClickListener, PopupMenu.OnMenuItemClickListener, OnDetailItemClickListener {

    private static final String TAG = RankDetailFragment.class.getSimpleName();
    private View mRootView;
    private RecyclerView mRrecyclerView;
    private RankDetailAdapter mRankDetailAdapter;

    private SongDetailBean mSongDetailBean;
    private EmptyLayoutV2 mEmptyLayoutV2;
    private boolean mFirstClick = true;

    private ImageView mPlayAll;
    private TextView mPlayAllText;
    private TextView mSongCount;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final RankDetailActivity associatedActivity = (RankDetailActivity) getActivity();
        mRootView = inflater.inflate(R.layout.fragment_singer_song, container, false);
        mEmptyLayoutV2 = (EmptyLayoutV2) mRootView.findViewById(R.id.empty_view_container);
        mEmptyLayoutV2.setOnClickListener(this);


        mSongCount = (TextView) mRootView.findViewById(R.id.detail_total_num_tv);
        mPlayAll = (ImageView) mRootView.findViewById(R.id.detail_play_all_image);
        mPlayAll.setOnClickListener(this);
        mPlayAllText = (TextView) mRootView.findViewById(R.id.detail_play_all_tv);
        mPlayAllText.setOnClickListener(this);
/*        final Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        associatedActivity.setActionBar(toolbar);
        ActionBar actionBar = associatedActivity.getActionBar();
        actionBar.hide();
        final TextView TextBar = (TextView) rootView.findViewById(R.id.textbar);
        TextBar.setText(associatedActivity.getRankTitle());

        updateDateTV = (TextView) rootView.findViewById(R.id.update_date);
        batchOperateLayout = (ViewGroup) rootView.findViewById(R.id.batch_operate_layout);
        TextView batchOperateTV = (TextView) rootView.findViewById(R.id.batch_operate);
        batchOperateTV.setOnClickListener(this);
        playAll = (ImageView) rootView.findViewById(R.id.network_play_all_image);
        playAll.setOnClickListener(this);
        playAllText = (TextView) rootView.findViewById(R.id.network_play_all);
        playAllText.setOnClickListener(this);
        final View tabshadow = rootView.findViewById(R.id.tabshadow);
        final float tabshadowAlpha = tabshadow.getAlpha();

        final ImageButton fab = (ImageButton) rootView.findViewById(R.id.fab);
        final float fabAlpha = fab.getAlpha();
        final AppBarLayout appBarLayout = (AppBarLayout) rootView.findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                LogUtil.d(TAG, "onOffsetChanged " + verticalOffset + " getTotalScrollRange " + appBarLayout.getTotalScrollRange());
                int totalScrollRange = appBarLayout.getTotalScrollRange();
                if (totalScrollRange == 0) {
                    return;
                }
                int t = totalScrollRange + verticalOffset;
                if (t <= fab.getHeight() / 2 + 1) {
                    fab.setVisibility(View.GONE);
                } else {
                    fab.setVisibility(View.GONE);
                }

                logoIV.setAlpha((t * 1.0f) / totalScrollRange);
                tabshadow.setAlpha(tabshadowAlpha * ((t * 1.0f) / totalScrollRange));
                fab.setAlpha(fabAlpha * ((t * 1.0f) / totalScrollRange));
            }
        });

        actionBar.setBackgroundDrawable(new ColorDrawable(0x33f8f8f8));
        actionBar.setHomeAsUpIndicator(R.drawable.return_image);

        logoLloading = (ImageView) rootView.findViewById(R.id.logo_loading);
        logoIV = (ImageView) rootView.findViewById(R.id.logo);
        logoIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (associatedActivity != null) {
                    final List<SongDetailBean> dataList = associatedActivity.getSongDetailBeanList();
                    if (dataList != null) {
                        AuditionAlertDialog.getInstance(getActivity()).showWrapper(false, new AuditionAlertDialog.OnSelectedListener() {
                            @Override
                            public void onPlay() {
                                new LiveMusicPlayTask(getActivity()).playNow(dataList, 0, isFirstClick);
                                isFirstClick = false;
                            }
                        });
                    } else {
                        LogUtil.i(TAG, "dataList is null");
                    }
                }
            }
        });*/
        mRrecyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerview);
        mRrecyclerView.setLayoutManager(new LinearLayoutManager(mRrecyclerView.getContext()));
        mRankDetailAdapter = new RankDetailAdapter(associatedActivity);
        mRankDetailAdapter.setOnDetailItemClickListener(this);
        mRrecyclerView.setAdapter(mRankDetailAdapter);

        mRrecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            private int totalDy = 0;
            private boolean t = false;
            private int tmpDy = 0;
            private int lastVisibleItem;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                RankDetailAdapter adapter = (RankDetailAdapter) recyclerView.getAdapter();
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter.getItemCount()) {

                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                return;
            }

        });

        //fab.setOnClickListener(this);
        return mRootView;
    }

    @Override
    public void onDestroy() {
        if (mRankDetailAdapter != null) {
            mRankDetailAdapter.unRegisterListener();
        }
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final RankDetailActivity associatedActivity = (RankDetailActivity) getActivity();
        List<SongDetailBean> songDetailBeanList = associatedActivity.getSongDetailBeanList();
        if (songDetailBeanList != null) {
            updateRankSongs(songDetailBeanList);
        } else {
            associatedActivity.loadRankDetail();
        }
    }

    public void updateSongCount(int count){
        if(!isAdded()){
            return;
        }
        if(mRankDetailAdapter ==null){
            return;
        }
        RankDetailActivity associatedActivity = (RankDetailActivity)getActivity();
        if(associatedActivity==null){
            return;
        }
        mSongCount.setText(getActivity().getResources().getQuantityString(R.plurals.number_of_folder_detail_songs, count, count));

    }

    public void updateRankSongs(List<SongDetailBean> songDetailBeanList) {
        if (!isAdded()) {
            return;
        }
        if (mRankDetailAdapter == null) {
            return;
        }
        if (songDetailBeanList == null) {
            //numTV.setText(getString(R.string.num_songs,0));
        } else {
            //numTV.setText(getString(R.string.num_songs,songDetailBeanList.size()));
        }
        mRankDetailAdapter.addDataList(songDetailBeanList);
        mRankDetailAdapter.setIsMore(false);
        mRankDetailAdapter.notifyDataSetChanged();
        if (songDetailBeanList.size() == 0) {
            mEmptyLayoutV2.setErrorType(EmptyLayoutV2.NODATA_ENABLE_CLICK);
        } else {
            mEmptyLayoutV2.setErrorType(EmptyLayoutV2.HIDE_LAYOUT);
        }

    }

    public void updateStatus(int totalSongCount, int realSongCount) {
        if (!isAdded()) {
            return;
        }
        if (mRankDetailAdapter == null) {
            return;
        }
        RankDetailActivity associatedActivity = (RankDetailActivity) getActivity();
        if (associatedActivity == null) {
            return;
        }
        int loadRankDetailStatus = associatedActivity.getLoadRankDetailStatus();
        if (loadRankDetailStatus == 3) {
            mEmptyLayoutV2.setErrorType(EmptyLayoutV2.NETWORK_ERROR);
        } else if (loadRankDetailStatus == 2) {
            if (totalSongCount > 0 && realSongCount == 0) {
                mEmptyLayoutV2.setErrorType(EmptyLayoutV2.NO_VALID_SONG);
                return;
            }
            mEmptyLayoutV2.setErrorType(EmptyLayoutV2.HIDE_LAYOUT);
        } else if (loadRankDetailStatus == 1) {
            mEmptyLayoutV2.setErrorType(EmptyLayoutV2.NETWORK_LOADING);
        }
    }

    @Override
    public void onClick(View v) {
        if (!isAdded()) {
            return;
        }
        int id = v.getId();
        Context context = getContext();
        if (id == R.id.batch_operate) {
            Intent intent = new Intent(getActivity(), NetworkBatchActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("name", ((RankDetailActivity) getActivity()).getRankTitle());
            intent.putExtras(bundle);
            if (mRankDetailAdapter != null && mRankDetailAdapter.getDataList() != null) {
                OnlineUtil.setSongDetailData(mRankDetailAdapter.getDataList());
                startActivity(intent);
            }
        } else if (id == R.id.empty_view_container) {
            if (SystemUtility.getNetworkType() == SystemUtility.NetWorkType.none) {
                ToastUtil.showToast(v.getContext(), R.string.network_error_prompt);
                return;
            }
            RankDetailActivity associatedActivity = (RankDetailActivity) getActivity();
            if (associatedActivity != null) {
                associatedActivity.loadRankDetail();
                updateStatus(0, 0);
            }
        } else if (id == R.id.fab) {
            RankDetailActivity associatedActivity = (RankDetailActivity) getActivity();
            if (associatedActivity != null) {
                final List<SongDetailBean> dataList = associatedActivity.getSongDetailBeanList();
                if (dataList != null) {
                    AuditionAlertDialog.getInstance(getActivity()).showWrapper(false, new AuditionAlertDialog.OnSelectedListener() {
                        @Override
                        public void onPlay() {
                            new LiveMusicPlayTask(getActivity()).playNow(dataList, 0, mFirstClick);
                            mFirstClick = false;
                        }
                    });
                } else {
                    LogUtil.i(TAG, "dataList is null");
                }
            }

        } else if (id == R.id.network_play_all_image || id == R.id.network_play_all) {
            RankDetailActivity associatedActivity = (RankDetailActivity) getActivity();
            if (associatedActivity != null) {
                final List<SongDetailBean> dataList = associatedActivity.getSongDetailBeanList();
                if (dataList != null) {
                    AuditionAlertDialog.getInstance(getActivity()).showWrapper(false, new AuditionAlertDialog.OnSelectedListener() {
                        @Override
                        public void onPlay() {
                            new LiveMusicPlayTask(getActivity()).playNow(dataList, 0, mFirstClick);
                            mFirstClick = false;
                        }
                    });
                } else {
                    LogUtil.i(TAG, "dataList is null");
                }
            }
        }
    }

    public void onClick(View v, Object object, int position) {
        if (!isAdded()) {
            return;
        }
        final int id = v.getId();
        Context context = getContext();
        if (id == R.id.item_menu_image_button) {
            mSongDetailBean = (SongDetailBean) object;
            PopupMenu popup = new PopupMenu(context, v, Gravity.CENTER);
            MenuInflater menuInflater = popup.getMenuInflater();
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
            View mLocalMediaDialogView;
            mLocalMediaDialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_hotmusicrecommend, null);
//            final MediaInfo info = MusicUtil.getMediaInfoBySong(context, songDetailBean, position, Util.getTransionId(), MediaInfo.SRC_TYPE_DEEZER, false);
            final MediaInfo info = new MediaInfo();
            mBuilder.setView(mLocalMediaDialogView);
            final AlertDialog mLocalMediaDialog = mBuilder.create();

            final Menu menu1 = popup.getMenu();
            menuInflater.inflate(R.menu.song_operate2, menu1);

            View.OnClickListener dialogListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MenuItem item = menu1.findItem(R.id.action_favorite_playlist);
                    switch (v.getId()) {
                        case R.id.action_favorite_playlist:
                            new LiveMusicPlayTask(getActivity()).save2Favourite(mSongDetailBean);
                            break;
                        case R.id.cancle_favorite_playlist:
                            MusicUtil.saveFavourite(getContext(), info);
                            break;
                        case R.id.action_play_next:
                            if (mSongDetailBean != null) {
                                new LiveMusicPlayTask(getActivity()).playAtNext(mSongDetailBean);
                            } else {
                                LogUtil.i(TAG, "detail is null");
                            }
                            break;
                        case R.id.action_download:
                            IDownloader downloader = DownloadManager.getInstance(getActivity()).getDownloader();
                            downloader.startMusicDownload(mSongDetailBean.song_id);
                            break;
                        case R.id.action_go_to_artist:
                            SingerDetailActivity.launch(getActivity(), mSongDetailBean.artist_id, mSongDetailBean.artist_name, 0, 0);
                            break;
                        case R.id.action_go_to_album:
                            AlbumDetailActivity.launch(getActivity(), mSongDetailBean.album_id, mSongDetailBean.album_name, 0);
                            break;
                        case R.id.action_add_to_song_list:
                            new LiveMusicPlayTask(getActivity()).add2SongList(mSongDetailBean);
                            break;
                        case R.id.action_add_to_play_queue:
                            if (mSongDetailBean != null) {
                                List<SongDetailBean> list = new ArrayList<SongDetailBean>();
                                list.add(mSongDetailBean);
                                new LiveMusicPlayTask(getActivity()).add2Queue(list);
                            } else {
                                LogUtil.i(TAG, "detail is null");
                            }
                            break;

                    }
                    mLocalMediaDialog.dismiss();
                }
            };

            TextView favorite_playlist = (TextView) mLocalMediaDialogView.findViewById(R.id.action_favorite_playlist);
            TextView action_play_next = (TextView) mLocalMediaDialogView.findViewById(R.id.action_play_next);
            TextView action_download = (TextView) mLocalMediaDialogView.findViewById(R.id.action_download);
            TextView action_go_to_artist = (TextView) mLocalMediaDialogView.findViewById(R.id.action_go_to_artist);
            TextView action_go_to_album = (TextView) mLocalMediaDialogView.findViewById(R.id.action_go_to_album);
            TextView action_add_to_song_list = (TextView) mLocalMediaDialogView.findViewById(R.id.action_add_to_song_list);
            TextView action_add_to_play_queue = (TextView) mLocalMediaDialogView.findViewById(R.id.action_add_to_play_queue);
            TextView cancle_favorite_playlist = (TextView) mLocalMediaDialogView.findViewById(R.id.cancle_favorite_playlist);
            favorite_playlist.setVisibility(View.VISIBLE);
            cancle_favorite_playlist.setVisibility(View.GONE);
            favorite_playlist.setOnClickListener(dialogListener);
            action_play_next.setOnClickListener(dialogListener);
            action_download.setOnClickListener(dialogListener);
            action_go_to_artist.setOnClickListener(dialogListener);
            action_go_to_album.setOnClickListener(dialogListener);
            action_add_to_song_list.setOnClickListener(dialogListener);
            action_add_to_play_queue.setOnClickListener(dialogListener);
            cancle_favorite_playlist.setOnClickListener(dialogListener);
            mLocalMediaDialog.show();

        } else if (id == R.id.item_view) {
            SongDetailBean detail = (SongDetailBean) object;

            if (detail != null) {
                final int pos = position;
                AuditionAlertDialog.getInstance(getActivity()).showWrapper(false, new AuditionAlertDialog.OnSelectedListener() {
                    @Override
                    public void onPlay() {
                        new LiveMusicPlayTask(getActivity()).playNow(mRankDetailAdapter.getDataList(), pos, mFirstClick);
                        mFirstClick = false;
                    }
                });
            } else {
                LogUtil.e(TAG, "detail is null ");
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorite_playlist:
                new LiveMusicPlayTask(getActivity()).save2Favourite(mSongDetailBean);
                break;
            case R.id.action_play_next:
                if (mSongDetailBean != null) {
                    new LiveMusicPlayTask(getActivity()).playAtNext(mSongDetailBean);
                } else {
                    LogUtil.i(TAG, "detail is null");
                }
                break;
            case R.id.action_download:
                IDownloader downloader = DownloadManager.getInstance(this.getActivity()).getDownloader();
                downloader.startMusicDownload(mSongDetailBean.song_id);
                break;
            case R.id.action_add_to_song_list:
                new LiveMusicPlayTask(getActivity()).add2SongList(mSongDetailBean);
                break;
            case R.id.action_add_to_play_queue:
                if (mSongDetailBean != null) {
                    List<SongDetailBean> list = new ArrayList<SongDetailBean>();
                    list.add(mSongDetailBean);
                    new LiveMusicPlayTask(getActivity()).add2Queue(list);
                } else {
                    LogUtil.i(TAG, "detail is null");
                }
                break;
            case R.id.action_go_to_artist:
                SingerDetailActivity.launch(getActivity(), mSongDetailBean.artist_id, mSongDetailBean.artist_name, 0, 0);
                break;
            case R.id.action_go_to_album:
                AlbumDetailActivity.launch(getActivity(), mSongDetailBean.album_id, mSongDetailBean.album_name, 0);
                break;
//            case R.id.action_batch_add_to_song_list://批量添加歌单
//                new LiveMusicPlayTask(getActivity()).add2SongList(rankDetailAdapter.getDataList());
//                break;
//            case R.id.action_batch_add_to_play_queue://批量播放列表
//                new LiveMusicPlayTask(getActivity()).add2Queue(rankDetailAdapter.getDataList());
//                break;
//            case R.id.action_batch_download://批量下载：
//                LogUtil.e(TAG, "batch_download");
//                IDownloader batchDownloader = DownloadManager.getInstance(this.getActivity()).getDownloader();
//                batchDownloader.startBatchMusicDownload(rankDetailAdapter.getDataList());
//                break;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
