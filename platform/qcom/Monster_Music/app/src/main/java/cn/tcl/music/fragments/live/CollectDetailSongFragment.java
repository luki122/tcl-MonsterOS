package cn.tcl.music.fragments.live;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.activities.live.OnlinePlayListDetailActivity;
import cn.tcl.music.activities.live.NetworkBatchActivity;
import cn.tcl.music.adapter.live.DetailSongAdapter;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.network.LiveMusicPlayTask;
import cn.tcl.music.util.SystemUtility;
import cn.tcl.music.util.ToastUtil;
import cn.tcl.music.util.live.OnlineUtil;
import cn.tcl.music.view.AuditionAlertDialog;
import cn.tcl.music.view.EmptyLayoutV2;
import cn.tcl.music.view.OnDetailItemClickListener;

public class CollectDetailSongFragment extends Fragment implements View.OnClickListener, PopupMenu.OnMenuItemClickListener, OnDetailItemClickListener {

    private static final String TAG = CollectDetailSongFragment.class.getSimpleName();
    private DetailSongAdapter mDetailSongAdapter;
    private RecyclerView mRecyclerView;
    private EmptyLayoutV2 mEmptyLayoutV2;
    private boolean mIsFirstClick;
    private TextView mSongCount;

    public static CollectDetailSongFragment newInstance() {
        return new CollectDetailSongFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mIsFirstClick = true;
        View rootView = inflater.inflate(R.layout.fragment_singer_song, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        mEmptyLayoutV2 = (EmptyLayoutV2) rootView.findViewById(R.id.empty_view_container);
        mSongCount = (TextView) rootView.findViewById(R.id.detail_total_num_tv);
        mEmptyLayoutV2.setOnClickListener(this);
        mDetailSongAdapter = new DetailSongAdapter(rootView.getContext());
        mDetailSongAdapter.setOnDetailItemClickListener(this);
        //batchOperateLayout = (ViewGroup)rootView.findViewById(R.id.batch_operate_layout);
        //TextView batchOperateTV = (TextView)rootView.findViewById(R.id.batch_operate);
        //batchOperateTV.setOnClickListener(this);
        //playAll = (ImageView) rootView.findViewById(R.id.network_play_all_image);
        //playAll.setOnClickListener(this);
        /* MODIFIED-BEGIN by beibei.yang, 2016-06-17,BUG-2343725,2203366*/
        //playAllText = (TextView) rootView.findViewById(R.id.network_play_all);
        //playAllText.setOnClickListener(this);
        /* MODIFIED-END by beibei.yang,BUG-2343725,2203366*/
        mRecyclerView.setAdapter(mDetailSongAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            private int totalDy = 0;
            private boolean t = false;
            private int tmpDy = 0;
            private int lastVisibleItem;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                DetailSongAdapter adapter = (DetailSongAdapter) recyclerView.getAdapter();
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter.getItemCount()) {

                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                //batchOperateLayout.setTranslationY(totalDy);
                return;
//                float translationY = batchOperateLayout.getTranslationY();
//
//                totalDy -= dy;
//                if (dy >= 0) {
//                    if(Math.abs(translationY)<batchOperateLayout.getHeight()){
//                        batchOperateLayout.setTranslationY(batchOperateLayout.getTranslationY()-dy);
//                    }else {
//                        batchOperateLayout.setTranslationY(totalDy);
//                    }
//
//                    t = false;
//                    tmpDy = 0;
//                    return;
//                }
//
//                tmpDy += dy;
//                if (dy < -100) {
//                    t = true;
//                }
//
//                if (tmpDy < -100) {
//                    t = true;
//                }
//                if (t) {
//                    batchOperateLayout.setTranslationY(0);
//                    return;
//                }
//                if (-totalDy <= recyclerView.getPaddingTop()) {
//                    batchOperateLayout.setTranslationY(totalDy);
//                    return;
//                }

            }

        });

        return rootView;
    }

    //[BUGFIX]-ADD by yanjia.li, 2016-06-18,BUG-2197064 begin
    @Override
    public void onDestroy() {
        if (mDetailSongAdapter != null) {
            mDetailSongAdapter.unRegisterListener();
        }
        super.onDestroy();
    }
    //[BUGFIX]-ADD by yanjia.li, 2016-06-18,BUG-2197064 end

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        OnlinePlayListDetailActivity associatedActivity = (OnlinePlayListDetailActivity) getActivity();
        List<SongDetailBean> songDetailBeanList = associatedActivity.getSongDetailBeanList();
        if (songDetailBeanList == null) {
            associatedActivity.loadCollectDetailData();
        } else {
            updateCollectDetailSong(songDetailBeanList);
        }

    }

    public void updateCollectDetailSong(List<SongDetailBean> songDetailBeanList) {
        if (!isAdded()) {
            return;
        }
        if (mDetailSongAdapter == null) {
            return;
        }
        OnlinePlayListDetailActivity associatedActivity = (OnlinePlayListDetailActivity) getActivity();
        if (associatedActivity == null) {
            return;
        }
        mDetailSongAdapter.setIsMore(associatedActivity.isCollectDetailSongMore());
        mDetailSongAdapter.addDataList(songDetailBeanList);
        mDetailSongAdapter.notifyDataSetChanged();
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
        if (mDetailSongAdapter == null) {
            return;
        }
        OnlinePlayListDetailActivity associatedActivity = (OnlinePlayListDetailActivity) getActivity();
        if (associatedActivity == null) {
            return;
        }
        int loadCollectDetailStatus = associatedActivity.getLoadCollectDetailStatus();
        if (loadCollectDetailStatus == 3) {
            mEmptyLayoutV2.setErrorType(EmptyLayoutV2.NETWORK_ERROR);
        } else if (loadCollectDetailStatus == 2) {
            if (totalSongCount > 0 && realSongCount == 0) {
                mEmptyLayoutV2.setErrorType(EmptyLayoutV2.NO_VALID_SONG);
                return;
            }
            mEmptyLayoutV2.setErrorType(EmptyLayoutV2.HIDE_LAYOUT);
        } else if (loadCollectDetailStatus == 1) {
            mEmptyLayoutV2.setErrorType(EmptyLayoutV2.NETWORK_LOADING);
        }
    }

    public void updateSongCount(int count) {
        if (!isAdded()) {
            return;
        }
        if (mDetailSongAdapter == null) {
            return;
        }
        OnlinePlayListDetailActivity associatedActivity = (OnlinePlayListDetailActivity) getActivity();
        if (associatedActivity == null) {
            return;
        }
        mSongCount.setText(getActivity().getResources().getQuantityString(R.plurals.number_of_folder_detail_songs, count, count));

    }


    @Override
    public void onClick(View v) {
        if (!isAdded()) {
            return;
        }
        int id = v.getId();
        if (id == R.id.batch_operate) {
            //MobclickAgent.onEvent(getActivity(), MobConfig.ESSENCE_DETAIL_BATCH);
            Intent intent = new Intent(getActivity(), NetworkBatchActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("name", ((OnlinePlayListDetailActivity) getActivity()).getCollectName());
            intent.putExtras(bundle);
            OnlineUtil.setSongDetailData(mDetailSongAdapter.getDataList());
            startActivity(intent);
        } else if (id == R.id.empty_view_container) {
            if (SystemUtility.getNetworkType() == SystemUtility.NetWorkType.none) {
                ToastUtil.showToast(v.getContext(), R.string.network_error_prompt);
                return;
            }
            OnlinePlayListDetailActivity associatedActivity = (OnlinePlayListDetailActivity) getActivity();
            if (associatedActivity != null) {
                associatedActivity.loadCollectDetailData();
                updateStatus(0, 0);
            }
        } else if (id == R.id.network_play_all_image || id == R.id.network_play_all) { // MODIFIED by beibei.yang, 2016-06-17,BUG-2343725,2203366
            if (mDetailSongAdapter == null) {
                return;
            }
            final List<SongDetailBean> dataList = mDetailSongAdapter.getDataList();
            if (dataList != null) {

                AuditionAlertDialog.getInstance(getActivity()).showWrapper(false, new AuditionAlertDialog.OnSelectedListener() {
                    @Override
                    public void onPlay() {
                        new LiveMusicPlayTask(getActivity()).playNow(dataList, 0, mIsFirstClick);
                        mIsFirstClick = false;
                    }
                });
            }
        }

    }

    @Override
    public void onClick(View v, Object object, int position) {
        if (!isAdded()) {
            return;
        }

        int id = v.getId();
        Context context = getContext();
//        if(id== R.id.item_menu_image_button){
//            currentClickSong = (SongDetailBean)object;
////            PopupMenu popup = new PopupMenu(context, v, Gravity.CENTER);
////            MenuInflater menuInflater = popup.getMenuInflater();
////            Menu menu = popup.getMenu();
////            menuInflater.inflate(R.menu.song_operate2, menu);
////            popup.setOnMenuItemClickListener(this);
////            popup.show();
//            final MediaInfo info = LibraryNavigationUtil.getMediaInfoBySong(context, currentClickSong, position, Util.getTransionId(), MediaInfo.SRC_TYPE_DEEZER, false);
//            AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
//            View mOnlineSearchSongDialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_search_song, null);
//            mBuilder.setView(mOnlineSearchSongDialogView);
//            final AlertDialog mOnlineSearchSongDialog = mBuilder.create();
//            mOnlineSearchSongDialog.show();
//            View.OnClickListener dialogListener = new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    switch (v.getId()) {
//                        case R.id.favorite_playlist:
//                            new LiveMusicPlayTask(getActivity()).save2Favourite(currentClickSong);
//                            break;
//                        case R.id.cancle_favorite_playlist:
//                            LibraryNavigationUtil.saveFavourite(getContext(),info);
//                            break;
//                        case R.id.action_play_next://下一首播放
//                            if(currentClickSong != null){
//                                new LiveMusicPlayTask(getActivity()).playAtNext(currentClickSong);
//                            }
//                            else{
//                                NLog.i(TAG, "detail is null");
//                            }
//                            break;
//                        case R.id.action_download://下载
//                            IDownloader downloader = DownloadManager.getInstance(getActivity()).getDownloader();
//                            downloader.startMusicDownload(currentClickSong.song_id);
//                            break;
//
//                        case R.id.action_add_to_playlist://歌单
//                            new LiveMusicPlayTask(getActivity()).add2SongList(currentClickSong);
//                            break;
//
//                        case R.id.action_add_to_queue://添加到播放队列
//                            if(currentClickSong != null){
//                                new LiveMusicPlayTask(getActivity()).add2Queue(currentClickSong);
//                            }
//                            else{
//                                NLog.i(TAG, "detail is null");
//                            }
//                            break;
//                        case R.id.action_go_to_artist:
//                            SingerDetailActivity.launch(getActivity(), currentClickSong.artist_id
//                                    , currentClickSong.artist_name, 0, 0);
//                            break;
//                        case R.id.action_go_to_album:
//                            LocalAlbumDetailActivity.launch(getActivity(), currentClickSong.album_id, currentClickSong.album_name, 0);
//                            break;
//
//                    }
//                    mOnlineSearchSongDialog.dismiss();
//                }
//            };
//            TextView favorite_playlist =(TextView)mOnlineSearchSongDialogView.findViewById(R.id.favorite_playlist);
//            TextView action_play_next =(TextView)mOnlineSearchSongDialogView.findViewById(R.id.action_play_next);
//            TextView action_add_to_playlist = (TextView)mOnlineSearchSongDialogView.findViewById(R.id.action_add_to_playlist);
//            TextView action_go_to_artist = (TextView)mOnlineSearchSongDialogView.findViewById(R.id.action_go_to_artist);
//            TextView action_go_to_album = (TextView)mOnlineSearchSongDialogView.findViewById(R.id.action_go_to_album);
//            TextView action_download = (TextView)mOnlineSearchSongDialogView.findViewById(R.id.action_download);
//            TextView action_add_to_queue = (TextView)mOnlineSearchSongDialogView.findViewById(R.id.action_add_to_queue);
//            TextView cancle_favorite_playlist =(TextView)mOnlineSearchSongDialogView.findViewById(R.id.cancle_favorite_playlist);
//            if (LibraryNavigationUtil.isLLiveSongSaved(context,currentClickSong,MediaInfo.SRC_TYPE_DEEZER)){
//                favorite_playlist.setVisibility(View.GONE);
//                cancle_favorite_playlist.setVisibility(View.VISIBLE);
//            } else{
//                favorite_playlist.setVisibility(View.VISIBLE);
//                cancle_favorite_playlist.setVisibility(View.GONE);
//            }
//            favorite_playlist.setOnClickListener(dialogListener);
//            action_play_next.setOnClickListener(dialogListener);
//            action_add_to_playlist.setOnClickListener(dialogListener);
//            action_go_to_artist.setOnClickListener(dialogListener);
//            action_go_to_album.setOnClickListener(dialogListener);
//            action_download.setOnClickListener(dialogListener);
//            action_add_to_queue.setOnClickListener(dialogListener);
//            cancle_favorite_playlist.setOnClickListener(dialogListener);
//        }
//        else if(id == R.id.item_view){
//            //MobclickAgent.onEvent(getActivity(), MobConfig.ESSENCE_DETAIL_PLAY);
//            SongDetailBean detail = (SongDetailBean)object;
//
//            if(detail != null){
//                final int pos = position;
//                NLog.i(TAG, "position is :"+position);
//                AuditionAlertDialog.getInstance(getActivity()).showWrapper(false, new AuditionAlertDialog.OnSelectedListener() {
//                    @Override
//                    public void onPlay() {
//                        new LiveMusicPlayTask(getActivity()).playNow(detailSongAdapter.getDataList(), pos, isFirstClick);
//                        isFirstClick = false;
//                    }
//                });
//            }
//            else{
//                NLog.i(TAG, "detail is null");
//            }
//        }else if(id== R.id.empty_view_container){
//            if (SystemUtility.getNetworkType() == SystemUtility.NetWorkType.none) {
//                Toast.makeText(v.getContext(), R.string.network_error_prompt, Toast.LENGTH_SHORT).show();
//                return;
//            }
//            CollectDetailActivity associatedActivity = (CollectDetailActivity)getActivity();
//            if(associatedActivity!=null){
//                associatedActivity.loadCollectDetailData();
//            }
//        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
//        switch (item.getItemId()){
//            case R.id.action_favorite_playlist:
//                //MobclickAgent.onEvent(getActivity(), MobConfig.SINGER_SONG_DETAIL_ITEM_ADDTOSONGLIST);
//                new LiveMusicPlayTask(getActivity()).save2Favourite(currentClickSong);
//                break;
//            case R.id.action_play_next://下一首播放
//                //MobclickAgent.onEvent(getActivity(), MobConfig.HOT_MUSICIAN_MORE_PLAY_NEXT);
//                if(currentClickSong != null){
//                    new LiveMusicPlayTask(getActivity()).playAtNext(currentClickSong);
//                }
//                else{
//                    NLog.i(TAG, "detail is null");
//                }
//                break;
//            case R.id.action_download://下载
//                //MobclickAgent.onEvent(getActivity(), MobConfig.ESSENCE_DETAIL_MORE_DOWNLOAD);
//                IDownloader downloader = DownloadManager.getInstance(this.getActivity()).getDownloader();
//                downloader.startMusicDownload(currentClickSong.song_id);
//                break;
//
//            case R.id.action_add_to_song_list://歌单
//                //MobclickAgent.onEvent(getActivity(), MobConfig.ESSENCE_DETAIL_MORE_ADD_TO_PLAY_LIST);
//                new LiveMusicPlayTask(getActivity()).add2SongList(currentClickSong);
//                break;
//
//            case R.id.action_add_to_play_queue://添加到播放队列
//                //MobclickAgent.onEvent(getActivity(), MobConfig.ESSENCE_DETAIL_MORE_ADD_TO_PLAY_QUEUE);
//                if(currentClickSong != null){
//                    new LiveMusicPlayTask(getActivity()).add2Queue(currentClickSong);
//                }
//                else{
//                    NLog.i(TAG, "detail is null");
//                }
//                break;
//
//
//            case R.id.action_go_to_artist:
//               // MobclickAgent.onEvent(getActivity(), MobConfig.ESSENCE_DETAIL_MORE_SHOW_MUSICIAN_INFO);
//                SingerDetailActivity.launch(getActivity(), currentClickSong.artist_id
//                        , currentClickSong.artist_name, 0, 0);
//                break;
//            case R.id.action_go_to_album:
//                //MobclickAgent.onEvent(getActivity(), MobConfig.ESSENCE_DETAIL_MORE_SHOW_ALBUM_INFO);
//                LocalAlbumDetailActivity.launch(getActivity(), currentClickSong.album_id, currentClickSong.album_name, 0);
//                break;
//
//
//
//
//
//
//            /****************击批量菜单弹出的菜单开始****************/
//            case R.id.action_batch_download://批量下载：
//                //MobclickAgent.onEvent(getActivity(), MobConfig.ESSENCE_DETAIL_BATCH_DOWNLOAD);
//                NLog.e(TAG,"batch_download");
//                IDownloader batchDownloader = DownloadManager.getInstance(this.getActivity()).getDownloader();
//                batchDownloader.startBatchMusicDownload(detailSongAdapter.getDataList());
//                break;
//            case R.id.action_batch_add_to_song_list://批量添加歌单
//                //MobclickAgent.onEvent(getActivity(), MobConfig.ESSENCE_DETAIL_ADD_TO_PLAY_LIST);
//                new LiveMusicPlayTask(getActivity()).add2SongList(detailSongAdapter.getDataList());
//                break;
//            case R.id.action_batch_add_to_play_queue://批量播放列表
//                //MobclickAgent.onEvent(getActivity(), MobConfig.ESSENCE_DETAIL_ADD_TO_PLAY_QUEUE);
//                new LiveMusicPlayTask(getActivity()).add2Queue(detailSongAdapter.getDataList());
//                break;
//            /****************击批量菜单弹出的菜单结束****************/
//
//            default:
//                break;
//        }
        return false;
    }

}
