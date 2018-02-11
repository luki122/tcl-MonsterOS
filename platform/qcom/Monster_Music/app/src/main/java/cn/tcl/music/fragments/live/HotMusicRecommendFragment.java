package cn.tcl.music.fragments.live;

import cn.tcl.music.util.ToastUtil;
import mst.app.dialog.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.app.Fragment;
import android.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import mst.widget.toolbar.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.xiami.sdk.utils.ImageUtil;

import java.util.ArrayList;
import java.util.List;

import cn.download.mie.base.util.DownloadManager;
import cn.download.mie.downloader.IDownloader;
import cn.tcl.music.R;
import cn.tcl.music.activities.live.AlbumDetailActivity;
import cn.tcl.music.activities.live.HotMusicRecommendActivity;
import cn.tcl.music.activities.live.NetworkBatchActivity;
import cn.tcl.music.activities.live.SingerDetailActivity;
import cn.tcl.music.adapter.live.HotMusicRecommendDetailAdapter;
import cn.tcl.music.model.MediaInfo;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.network.LiveMusicPlayTask;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.MusicUtil;
import cn.tcl.music.util.SystemUtility;
import cn.tcl.music.util.Util;
import cn.tcl.music.util.live.OnlineUtil;
import cn.tcl.music.view.AuditionAlertDialog;
import cn.tcl.music.view.EmptyLayoutV2;
import cn.tcl.music.view.OnDetailItemClickListener;

public class HotMusicRecommendFragment extends Fragment implements View.OnClickListener, PopupMenu.OnMenuItemClickListener, OnDetailItemClickListener, PopupMenu.OnDismissListener {
    public static final String TAG = HotMusicRecommendFragment.class.getSimpleName();
    private static final int RECOMMEND_SONGS_COUNT = 30;
    private RecyclerView recyclerView;
    private HotMusicRecommendDetailAdapter hotMusicRecommendDetailAdapter;
    private ImageView logoIV;
    private ImageView logoLloading;  //loading时显示的图片
//    private ViewGroup batchOperateLayout;
//    private EmptyLayoutV2 emptyLayoutV2;
    private boolean isFirstClick = true;
    private String mAction = null;
//    private ImageView playAll;
//    private TextView playAllText;
    private RelativeLayout mPlayAllRelative;
    private TextView mNumberTextView;

    private String mRecommendArtWork;

    public String getRecommendArtWork() {
        return mRecommendArtWork;
    }

    public void setRecommendArtWork(String recommendArtWork) {
        mRecommendArtWork = recommendArtWork;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HotMusicRecommendActivity associatedActivity = (HotMusicRecommendActivity) getActivity();
        View rootView = inflater.inflate(R.layout.fragment_hot_music_recommend, container, false);
//        emptyLayoutV2 = (EmptyLayoutV2) rootView.findViewById(R.id.empty_view_container);
//        emptyLayoutV2.setOnClickListener(this);
//        final Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            mAction = intent.getAction();
        }
//        associatedActivity.setActionBar(toolbar);
//        ActionBar actionBar = associatedActivity.getActionBar();
//        actionBar.hide();
//        final TextView TextBar = (TextView) rootView.findViewById(R.id.textbar);
//        if (mAction != null && mAction.equalsIgnoreCase("daily_recommend")) {
//            TextBar.setText(getActivity().getResources().getString(R.string.daily_recommend));
//        } else {
//            TextBar.setText(getActivity().getResources().getString(R.string.hot_music_recommend));
//        }
//
//        final CollapsingToolbarLayout collapsingToolbar =
//                (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);
//        collapsingToolbar.setTitleEnabled(false);
//        batchOperateLayout = (ViewGroup) rootView.findViewById(R.id.batch_operate_layout);
//        TextView batchOperateTV = (TextView) rootView.findViewById(R.id.batch_operate);
//        batchOperateTV.setOnClickListener(this);
//        playAll = (ImageView) rootView.findViewById(R.id.network_play_all_image);
//        playAll.setOnClickListener(this);
//        playAllText = (TextView) rootView.findViewById(R.id.network_play_all);
//        playAllText.setOnClickListener(this);
        logoLloading = (ImageView) rootView.findViewById(R.id.logo_loading);
        logoIV = (ImageView) rootView.findViewById(R.id.logo);
        logoIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<SongDetailBean> dataList = hotMusicRecommendDetailAdapter.getDataList();
                if (dataList != null) {
                    AuditionAlertDialog.getInstance(getActivity()).showWrapper(false,
                            new AuditionAlertDialog.OnSelectedListener() {
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
        });

        mPlayAllRelative = (RelativeLayout) rootView.findViewById(R.id.recommend_play_all);
        mNumberTextView = (TextView) rootView.findViewById(R.id.recommend_total_num_tv);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        hotMusicRecommendDetailAdapter = new HotMusicRecommendDetailAdapter(associatedActivity);
        mNumberTextView.setText(getActivity().getResources().getQuantityString(
                R.plurals.number_of_folder_detail_songs,RECOMMEND_SONGS_COUNT, RECOMMEND_SONGS_COUNT));
        mPlayAllRelative.setOnClickListener(this);
        hotMusicRecommendDetailAdapter.setOnDetailItemClickListener(this);
        recyclerView.setAdapter(hotMusicRecommendDetailAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            private int totalDy = 0;
            private boolean t = false;
            private int tmpDy = 0;
            private int lastVisibleItem;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                HotMusicRecommendDetailAdapter adapter = (HotMusicRecommendDetailAdapter) recyclerView.getAdapter();
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter.getItemCount()) {

                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
//                batchOperateLayout.setTranslationY(totalDy);
                return;
            }
        });
        return rootView;
    }

    @Override
    public void onDestroy() {
        try {
            if (hotMusicRecommendDetailAdapter != null) {
                hotMusicRecommendDetailAdapter.unRegisterListener();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        HotMusicRecommendActivity associatedActivity = (HotMusicRecommendActivity) getActivity();
        List<SongDetailBean> songDetailBeanList = associatedActivity.getSongDetailBeanList();
        if (songDetailBeanList != null) {
            updateRecommendHotSongs(songDetailBeanList);
        } else {
            associatedActivity.loadRecommendHotSongs();
        }
    }

    public void updateLogo(String logoUrl) {
        if (logoUrl == null) {
            return;
        }
        HotMusicRecommendActivity hotMusicRecommendActivity = (HotMusicRecommendActivity) getActivity();
        if (logoIV != null) {
            setRecommendArtWork(ImageUtil.transferImgUrl(logoUrl, 330));
            hotMusicRecommendActivity.getArtwork();
            logoLloading.setVisibility(View.GONE);
        }
    }

    public void updateRecommendHotSongs(List<SongDetailBean> songDetailBeanList) {
        if (!isAdded()) {
            return;
        }
        if (hotMusicRecommendDetailAdapter == null) {
            return;
        }
        if (songDetailBeanList == null) {
            // numTV.setText(getString(R.string.num_songs,0));
        } else if (isAdded()) {
            //numTV.setText(getString(R.string.num_songs,songDetailBeanList.size()));
        }
        hotMusicRecommendDetailAdapter.addDataList(songDetailBeanList);
        hotMusicRecommendDetailAdapter.setIsMore(false);
        hotMusicRecommendDetailAdapter.notifyDataSetChanged();

    }

    public void updateStatus() {
        if (!isAdded()) {
            return;
        }
        HotMusicRecommendActivity associatedActivity = (HotMusicRecommendActivity) getActivity();
        if (associatedActivity == null) {
            return;
        }
        int loadRecommendHotSongsStatus = associatedActivity.getLoadRecommendHotSongsStatus();
        if (loadRecommendHotSongsStatus == 3) {
//            emptyLayoutV2.setErrorType(EmptyLayoutV2.NETWORK_ERROR);
        } else if (loadRecommendHotSongsStatus == 2) {
//            emptyLayoutV2.setErrorType(EmptyLayoutV2.HIDE_LAYOUT);
        } else if (loadRecommendHotSongsStatus == 1) {
//            emptyLayoutV2.setErrorType(EmptyLayoutV2.NETWORK_LOADING);
        }
    }

    @Override
    public void onClick(View v) {
        if (!isAdded()) {
            return;
        }
        Context context = getContext();
        switch (v.getId()) {
            case R.id.batch_operate: {
                String title = null;
                if (mAction != null && mAction.equalsIgnoreCase("daily_recommend")) {
                    title = getActivity().getResources().getString(R.string.daily_recommend);
                } else {
                    title = getActivity().getResources().getString(R.string.hot_music_recommend);
                }
                Intent intent = new Intent(getActivity(), NetworkBatchActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("name", title);
                intent.putExtras(bundle);
                OnlineUtil.setSongDetailData(hotMusicRecommendDetailAdapter.getDataList());
                startActivity(intent);
            }
            break;
            case R.id.empty_view_container: {
                if (SystemUtility.getNetworkType() == SystemUtility.NetWorkType.none) {
                    ToastUtil.showToast(v.getContext(), R.string.network_error_prompt);
                    return;
                }
                HotMusicRecommendActivity associatedActivity = (HotMusicRecommendActivity) getActivity();
                if (associatedActivity != null) {
                    associatedActivity.loadRecommendHotSongs();
                    updateStatus();
                }
            }
            break;
            case R.id.fab: {
                final List<SongDetailBean> dataList = hotMusicRecommendDetailAdapter.getDataList();
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
            break;
            case R.id.network_play_all_image: {
                final List<SongDetailBean> dataList = hotMusicRecommendDetailAdapter.getDataList();
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
            break;
            case R.id.network_play_all: {
                final List<SongDetailBean> dataList = hotMusicRecommendDetailAdapter.getDataList();
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
            break;
            case R.id.recommend_play_all: {
                //TODO play all
            }
            break;
        }
    }

    @Override
    public void onClick(View v, Object object, int position) {
        if (!isAdded()) {
            return;
        }
        Context context = getContext();
        int id = v.getId();
        if (id == R.id.item_menu_image_button) {
            songDetailBean = (SongDetailBean) object;
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
            View mLocalMediaDialogView;
            mLocalMediaDialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_hotmusicrecommend, null);

            mBuilder.setView(mLocalMediaDialogView);
            final AlertDialog mLocalMediaDialog = mBuilder.create();
//            final MediaInfo info = MusicUtil.getMediaInfoBySong(context, songDetailBean, position, Util.getTransionId(), MediaInfo.SRC_TYPE_DEEZER, false);
            final MediaInfo info = new MediaInfo();
            PopupMenu popup = new PopupMenu(context, v, Gravity.CENTER);
            MenuInflater menuInflater = popup.getMenuInflater();
            final Menu menu1 = popup.getMenu();
            menuInflater.inflate(R.menu.song_operate2, menu1);

            View.OnClickListener dialogListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MenuItem item = menu1.findItem(R.id.action_favorite_playlist);
                    switch (v.getId()) {
                        case R.id.action_favorite_playlist:
                            new LiveMusicPlayTask(getActivity()).save2Favourite(songDetailBean);
                            break;
                        case R.id.cancle_favorite_playlist:
                            MusicUtil.saveFavourite(getContext(), info);
                            break;
                        case R.id.action_play_next:
                            if (songDetailBean != null) {
                                new LiveMusicPlayTask(getActivity()).playAtNext(songDetailBean);
                            } else {
                                LogUtil.i(TAG, "detail is null");
                            }
                            break;
                        case R.id.action_download:
                            IDownloader downloader = DownloadManager.getInstance(getActivity()).getDownloader();
                            downloader.startMusicDownload(songDetailBean.song_id);
                            break;
                        case R.id.action_go_to_artist:
                            SingerDetailActivity.launch(getActivity(), songDetailBean.artist_id, songDetailBean.artist_name, 0, 0);
                            break;
                        case R.id.action_go_to_album:
                            AlbumDetailActivity.launch(getActivity(), songDetailBean.album_id, songDetailBean.album_name, 0);
                            break;
                        case R.id.action_add_to_song_list:
                            new LiveMusicPlayTask(getActivity()).add2SongList(songDetailBean);
                            break;
                        case R.id.action_add_to_play_queue:
                            if (songDetailBean != null) {
                                List<SongDetailBean> list = new ArrayList<SongDetailBean>();
                                list.add(songDetailBean);
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
                LogUtil.i(TAG, "position is :" + position);
                final int pos = position;
                AuditionAlertDialog.getInstance(getActivity()).showWrapper(false, new AuditionAlertDialog.OnSelectedListener() {
                    @Override
                    public void onPlay() {
                        new LiveMusicPlayTask(getActivity()).playNow(hotMusicRecommendDetailAdapter.getDataList(), pos, isFirstClick);
                        isFirstClick = false;
                    }
                });
            } else {
                LogUtil.i(TAG, "detail is null");
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorite_playlist:
                new LiveMusicPlayTask(getActivity()).save2Favourite(songDetailBean);
                break;
            case R.id.action_play_next:
                if (songDetailBean != null) {
                    new LiveMusicPlayTask(getActivity()).playAtNext(songDetailBean);
                } else {
                    LogUtil.i(TAG, "detail is null");
                }
                break;
            case R.id.action_download:
                IDownloader downloader = DownloadManager.getInstance(this.getActivity()).getDownloader();
                downloader.startMusicDownload(songDetailBean.song_id);
                break;
            case R.id.action_add_to_song_list:
                new LiveMusicPlayTask(getActivity()).add2SongList(songDetailBean);
                break;
            case R.id.action_add_to_play_queue:
                if (songDetailBean != null) {
                    List<SongDetailBean> list = new ArrayList<SongDetailBean>();
                    list.add(songDetailBean);
                    new LiveMusicPlayTask(getActivity()).add2Queue(list);
                } else {
                    LogUtil.i(TAG, "detail is null");
                }
                break;
            case R.id.action_go_to_artist:
                SingerDetailActivity.launch(getActivity(), songDetailBean.artist_id, songDetailBean.artist_name, 0, 0);
                break;
            case R.id.action_go_to_album:
                AlbumDetailActivity.launch(getActivity(), songDetailBean.album_id, songDetailBean.album_name, 0);
                break;

//            case R.id.action_batch_download://批量下载：
//                LogUtil.e(TAG, "batch_download");
//                IDownloader batchDownloader = DownloadManager.getInstance(this.getActivity()).getDownloader();
//                batchDownloader.startBatchMusicDownload(hotMusicRecommendDetailAdapter.getDataList());
//                break;
//            case R.id.action_batch_add_to_song_list://批量添加歌单
//                new LiveMusicPlayTask(getActivity()).add2SongList(hotMusicRecommendDetailAdapter.getDataList());
//                break;
//            case R.id.action_batch_add_to_play_queue://批量播放列表
//                new LiveMusicPlayTask(getActivity()).add2Queue(hotMusicRecommendDetailAdapter.getDataList());
//                break;
        }
        return false;
    }

    private SongDetailBean songDetailBean;

    @Override
    public void onDismiss(PopupMenu menu) {
        songDetailBean = null;
    }
}

