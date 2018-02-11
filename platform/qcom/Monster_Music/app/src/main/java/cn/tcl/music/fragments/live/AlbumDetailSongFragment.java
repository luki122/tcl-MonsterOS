package cn.tcl.music.fragments.live;

import cn.tcl.music.util.ToastUtil;
import mst.app.dialog.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
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


import com.bumptech.glide.Glide;
import com.xiami.sdk.utils.ImageUtil;

import java.util.ArrayList;
import java.util.List;

import cn.download.mie.base.util.DownloadManager;
import cn.download.mie.downloader.IDownloader;
import cn.tcl.music.R;
import cn.tcl.music.activities.live.AlbumDetailActivity;
import cn.tcl.music.activities.live.NetworkBatchActivity;
import cn.tcl.music.activities.live.ScenesDetailActivity;
import cn.tcl.music.adapter.live.DetailSongAdapter;
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

//import com.umeng.analytics.MobclickAgent;

/**
 * @author zengtao.kuang
 * @Description:
 * @date 2015/11/12 21:02
 * @copyright TCL-MIE
 */
public class AlbumDetailSongFragment extends Fragment implements View.OnClickListener,PopupMenu.OnMenuItemClickListener,OnDetailItemClickListener {

    private static final String TAG = AlbumDetailSongFragment.class.getSimpleName();
    private DetailSongAdapter mDetailSongAdapter;
    private RecyclerView mRecyclerView;
    //ViewGroup batchOperateLayout;
    private SongDetailBean mSongDetailBean;
    private EmptyLayoutV2 mEmptyLayoutV2;
    private boolean mFirstClick = true;
    private TextView mSongCount;
    private ImageView mPlayAll;
    private TextView mPlayAllText; // MODIFIED by beibei.yang, 2016-06-17,BUG-2343725,2203366

    public static AlbumDetailSongFragment newInstance(){
        return new AlbumDetailSongFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtil.d(TAG, this.toString());
        View rootView =  inflater.inflate(R.layout.fragment_singer_song, container, false);
        mEmptyLayoutV2 = (EmptyLayoutV2)rootView.findViewById(R.id.empty_view_container);
        mEmptyLayoutV2.setOnClickListener(this);
        mSongCount = (TextView) rootView.findViewById(R.id.detail_total_num_tv);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerview);
        mDetailSongAdapter = new DetailSongAdapter(rootView.getContext());
        mDetailSongAdapter.setCategory(DetailSongAdapter.ALBUM_CATEGORY);
        mDetailSongAdapter.setIsMore(false);
        mDetailSongAdapter.setOnDetailItemClickListener(this);
        //batchOperateLayout = (ViewGroup)rootView.findViewById(R.id.batch_operate_layout);
        mPlayAll = (ImageView) rootView.findViewById(R.id.detail_play_all_image);
        mPlayAll.setOnClickListener(this);
        /* MODIFIED-BEGIN by beibei.yang, 2016-06-17,BUG-2343725,2203366*/
        mPlayAllText = (TextView) rootView.findViewById(R.id.detail_play_all_tv);
        mPlayAllText.setOnClickListener(this);
        /* MODIFIED-END by beibei.yang,BUG-2343725,2203366*/
        //TextView batchOperateTV = (TextView)rootView.findViewById(R.id.batch_operate);
        //batchOperateTV.setOnClickListener(this);
        mRecyclerView.setAdapter(mDetailSongAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            private int totalDy = 0;
            private boolean t = false;
            private int tmpDy = 0;
            private int lastVisibleItem;

            @Override
            public void onScrollStateChanged(RecyclerView mRecyclerView, int newState) {
                DetailSongAdapter adapter = (DetailSongAdapter)mRecyclerView.getAdapter();
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter.getItemCount()) {

                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AlbumDetailActivity associatedActivity = (AlbumDetailActivity)getActivity();
        List<SongDetailBean> songDetailBeanList = associatedActivity.getSongDetailBeanList();
        if(songDetailBeanList==null){
            associatedActivity.loadAlbumDetailData();
        }else {
            updateAlbumDetailSong(songDetailBeanList);
        }
    }

    public void updateAlbumDetailSong(List<SongDetailBean> songDetailBeanList){
        if(!isAdded()){
            return;
        }
        if(mDetailSongAdapter ==null){
            return;
        }
        AlbumDetailActivity associatedActivity = (AlbumDetailActivity)getActivity();
        if(associatedActivity==null){
            return;
        }
        mDetailSongAdapter.setIsMore(associatedActivity.isCollectDetailSongMore());
        mDetailSongAdapter.addDataList(songDetailBeanList);
        mDetailSongAdapter.addAlbumName(((AlbumDetailActivity) getActivity()).getAlbumName());
        mDetailSongAdapter.notifyDataSetChanged();

    }

    public void updateStatus(int totalSongCount, int realSongCount){
        if(!isAdded()){
            return;
        }
        if(mDetailSongAdapter ==null){
            return;
        }
        AlbumDetailActivity associatedActivity = (AlbumDetailActivity)getActivity();
        if(associatedActivity==null){
            return;
        }
        int loadAlbumDetailStatus = associatedActivity.getLoadAlbumDetailStatus();
        if(loadAlbumDetailStatus==3){
            mEmptyLayoutV2.setErrorType(EmptyLayoutV2.NETWORK_ERROR);
        }else if(loadAlbumDetailStatus==2){
            if(totalSongCount > 0 && realSongCount == 0){
                mEmptyLayoutV2.setErrorType(EmptyLayoutV2.NO_VALID_SONG);//无效歌曲
            }
            else{
                mEmptyLayoutV2.setErrorType(EmptyLayoutV2.HIDE_LAYOUT);
            }
        }else if(loadAlbumDetailStatus==1){
            mEmptyLayoutV2.setErrorType(EmptyLayoutV2.NETWORK_LOADING);
        }
    }
    public void updateSongCount(int count){
        if(!isAdded()){
            return;
        }
        if(mDetailSongAdapter ==null){
            return;
        }
        AlbumDetailActivity associatedActivity = (AlbumDetailActivity)getActivity();
        if(associatedActivity==null){
            return;
        }
        mSongCount.setText(getActivity().getResources().getQuantityString(R.plurals.number_of_folder_detail_songs, count, count));
    }

    @Override
    public void onClick(View v) {
        if(!isAdded()){
            return;
        }
        int id = v.getId();
        Context context = getContext();
        if(id== R.id.batch_operate){
            Intent intent = new Intent(getActivity(), NetworkBatchActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("name", ((AlbumDetailActivity) getActivity()).getAlbumName());
            bundle.putString("album_name", ((AlbumDetailActivity) getActivity()).getAlbumName());
            intent.putExtras(bundle);
            OnlineUtil.setSongDetailData(mDetailSongAdapter.getDataList());
            startActivity(intent);
        }else if(id == R.id.empty_view_container){
            if (SystemUtility.getNetworkType() == SystemUtility.NetWorkType.none) {
                ToastUtil.showToast(v.getContext(), R.string.network_error_prompt);
                return;
            }
            AlbumDetailActivity associatedActivity = (AlbumDetailActivity)getActivity();
            if(associatedActivity!=null){
                associatedActivity.loadAlbumDetailData();
                updateStatus(0, 0);
            }
        } else if(id == R.id.detail_play_all_image || id == R.id.detail_play_all_tv) { // MODIFIED by beibei.yang, 2016-06-17,BUG-2343725,2203366
            if(mDetailSongAdapter ==null){
                return;
            }
            final List<SongDetailBean> dataList = mDetailSongAdapter.getDataList();
            if(dataList != null){

                AuditionAlertDialog.getInstance(getActivity()).showWrapper(false, new AuditionAlertDialog.OnSelectedListener() {
                    @Override
                    public void onPlay() {
                        new LiveMusicPlayTask(getActivity()).playNow(dataList,0, mFirstClick);
                        mFirstClick = false;
                    }
                });
            }
        }
    }
/* MODIFIED-BEGIN by yanjia.li, 2016-06-18,BUG-2197064*/

    //[BUGFIX]-ADD by yanjia.li, 2016-06-18,BUG-2197064 begin
    @Override
    public void onDestroy() {
        if(mDetailSongAdapter != null){
            mDetailSongAdapter.unRegisterListener();
        }
        super.onDestroy();
    }
    //[BUGFIX]-ADD by yanjia.li, 2016-06-18,BUG-2197064 end
    /* MODIFIED-END by yanjia.li,BUG-2197064*/

    @Override
    public void onClick(View v, Object object, int position) {
        if(!isAdded()){
            return;
        }
        int id = v.getId();
        Context context = getContext();
        if(id== R.id.item_menu_image_button){
//            mSongDetailBean =(SongDetailBean)object;
//            PopupMenu popup = new PopupMenu(context, v, Gravity.CENTER);
//            MenuInflater menuInflater = popup.getMenuInflater();
//
//            AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity(),android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
//            View mLocalMediaDialogView ;
//            mLocalMediaDialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_hotmusicrecommend, null);
//
//            mBuilder.setView(mLocalMediaDialogView);
//            final AlertDialog mLocalMediaDialog = mBuilder.create();
//            final MediaInfo info = LibraryNavigationUtil.getMediaInfoBySong(context, songDetailBean, position, Util.getTransionId(), MediaInfo.SRC_TYPE_DEEZER, false);
//            final Menu menu1 = popup.getMenu();
//            menuInflater.inflate(R.menu.song_operate2, menu1);
//
//            View.OnClickListener dialogListener = new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    MenuItem item = menu1.findItem(R.id.action_favorite_playlist);
//                    switch (v.getId()){
//                        case R.id.action_favorite_playlist:
//                            new LiveMusicPlayTask(getActivity()).save2Favourite(songDetailBean);
//                            break;
//                        case R.id.cancle_favorite_playlist:
//                            LibraryNavigationUtil.saveFavourite(getContext(),info);
//                            break;
//                        case R.id.action_play_next:
//                            //item = menu1.findItem(R.id.action_play_next);
//                            if(songDetailBean != null){
//                                new LiveMusicPlayTask(getActivity()).playAtNext(songDetailBean);
//                            }
//                            else{
//                                NLog.i(TAG, "detail is null");
//                            }
//                            break;
//                        case R.id.action_download:
////                            item = menu1.findItem(R.id.action_download);
////                            LibraryNavigationUtil.manageMenuItem(LibraryNavigationUtil.ItemMediaType.SINGLE_MEDIA, item, getActivity(), c);
//                            //MobclickAgent.onEvent(getActivity(), MobConfig.HOT_MUSICIAN_MORE_DOWNLOAD);
//                            IDownloader downloader = DownloadManager.getInstance(getActivity()).getDownloader();
//                            downloader.startMusicDownload(songDetailBean.song_id);
//                            break;
//                        case R.id.action_go_to_artist:
//                            //item = menu1.findItem(R.id.action_go_to_artist);
//                            SingerDetailActivity.launch(getActivity(), songDetailBean.artist_id, songDetailBean.artist_name, 0, 0);
//                            break;
//                        case R.id.action_go_to_album:
//                            // item = menu1.findItem(R.id.action_go_to_album);
//                            //LocalAlbumDetailActivity.launch(getActivity(), songDetailBean.album_id, songDetailBean.album_name, 0);
//                            break;
//                        case R.id.action_add_to_song_list:
//                            // item = menu1.findItem(R.id.action_add_to_song_list);
//                            new LiveMusicPlayTask(getActivity()).add2SongList(songDetailBean);
//                            break;
//                        case R.id.action_add_to_play_queue:
//                            //  item = menu1.findItem(R.id.action_add_to_play_queue);
//                            if(songDetailBean != null){
//                                List<SongDetailBean> list = new ArrayList<SongDetailBean>();
//                                list.add(songDetailBean);
//                                new LiveMusicPlayTask(getActivity()).add2Queue(list);
//                            }
//                            else{
//                                NLog.i(TAG, "detail is null");
//                            }
//                            break;
//
//                    }
//                    mLocalMediaDialog.dismiss();
//                }
//            };
//
//            TextView favorite_playlist =(TextView)mLocalMediaDialogView.findViewById(R.id.action_favorite_playlist);
//            TextView action_play_next =(TextView)mLocalMediaDialogView.findViewById(R.id.action_play_next);
//            TextView action_download = (TextView)mLocalMediaDialogView.findViewById(R.id.action_download);
//            TextView action_go_to_artist = (TextView)mLocalMediaDialogView.findViewById(R.id.action_go_to_artist);
//            TextView action_go_to_album = (TextView)mLocalMediaDialogView.findViewById(R.id.action_go_to_album);
//            action_go_to_album.setVisibility(View.GONE);
//            TextView action_add_to_song_list = (TextView)mLocalMediaDialogView.findViewById(R.id.action_add_to_song_list);
//            TextView action_add_to_play_queue = (TextView)mLocalMediaDialogView.findViewById(R.id.action_add_to_play_queue);
//            TextView cancle_favorite_playlist =(TextView)mLocalMediaDialogView.findViewById(R.id.cancle_favorite_playlist);
//
//            if (LibraryNavigationUtil.isLLiveSongSaved(context,songDetailBean,MediaInfo.SRC_TYPE_DEEZER)){
//                favorite_playlist.setVisibility(View.GONE);
//                cancle_favorite_playlist.setVisibility(View.VISIBLE);
//            } else{
//                favorite_playlist.setVisibility(View.VISIBLE);
//                cancle_favorite_playlist.setVisibility(View.GONE);
//            }
//            favorite_playlist.setOnClickListener(dialogListener);
//            action_play_next.setOnClickListener(dialogListener);
//            action_download.setOnClickListener(dialogListener);
//            action_go_to_artist.setOnClickListener(dialogListener);
//          //  action_go_to_album.setOnClickListener(dialogListener);
//            action_add_to_song_list.setOnClickListener(dialogListener);
//            action_add_to_play_queue.setOnClickListener(dialogListener);
//            cancle_favorite_playlist.setOnClickListener(dialogListener);
//            mLocalMediaDialog.show();

        }
        else if(id == R.id.item_view){
//            SongDetailBean detail = (SongDetailBean)object;
//            final int pos = position;
//            NLog.d(TAG, "onClick detail = "+detail );
//            if(detail != null){
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
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
//            case R.id.action_favorite_playlist:
//                //MobclickAgent.onEvent(getActivity(), MobConfig.SINGER_SONG_DETAIL_ITEM_ADDTOSONGLIST);
//                new LiveMusicPlayTask(getActivity()).save2Favourite(songDetailBean);
//                break;
//            case R.id.action_play_next:
//                if(songDetailBean != null){
//                    new LiveMusicPlayTask(getActivity()).playAtNext(songDetailBean);
//                }
//                else{
//                    NLog.i(TAG, "detail is null");
//                }
//                break;
//            case R.id.action_download:
//                LogUtil.d(TAG,songDetailBean.toString());
//                IDownloader downloader = DownloadManager.getInstance(this.getActivity()).getDownloader();
//                downloader.startMusicDownload(songDetailBean.song_id);
//                break;
//            case R.id.action_add_to_song_list:
//                new LiveMusicPlayTask(getActivity()).add2SongList(songDetailBean);
//                break;
//            case R.id.action_add_to_play_queue:
//                new LiveMusicPlayTask(getActivity()).add2Queue(songDetailBean);
//                break;
//            case R.id.action_go_to_artist:
//                SingerDetailActivity.launch(getActivity(), songDetailBean.artist_id, songDetailBean.artist_name, 0, 0);
//                break;
//            case R.id.action_batch_add_to_song_list://批量添加歌单
//                new LiveMusicPlayTask(getActivity()).add2SongList(detailSongAdapter.getDataList());
//                break;
//            case R.id.action_batch_add_to_play_queue://批量播放列表
//                new LiveMusicPlayTask(getActivity()).add2Queue(detailSongAdapter.getDataList());
//                break;
//            case R.id.action_batch_download://批量下载：
//                NLog.i(TAG,"batch_download");
//                IDownloader batchDownloader = DownloadManager.getInstance(this.getActivity()).getDownloader();
//                batchDownloader.startBatchMusicDownload(detailSongAdapter.getDataList());
//                break;
        }
        return false;
    }

}
