package cn.tcl.music.fragments.live;

import mst.app.dialog.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import cn.tcl.music.activities.live.SingerDetailActivity;
import cn.tcl.music.adapter.live.SingerSongAdapter;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.network.LiveMusicPlayTask;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.Util;
import cn.tcl.music.view.EmptyLayoutV2;
import cn.tcl.music.view.FooterView;
import cn.tcl.music.view.OnDetailItemClickListener;

//import com.umeng.analytics.MobclickAgent;

/**
 * @author zengtao.kuang
 * @Description:
 * @date 2015/11/9 16:08
 * @copyright TCL-MIE
 */
public class SingerSongFragment extends Fragment implements View.OnClickListener
        ,PopupMenu.OnMenuItemClickListener,OnDetailItemClickListener,PopupMenu.OnDismissListener{

    public static final String TAG = "SingerSongFragment";
    SingerSongAdapter singerSongAdapter;
    RecyclerView recyclerView;
    ViewGroup batchOperateLayout;
    private EmptyLayoutV2 emptyLayoutV2;
    private boolean isFirstClick = true;
    private ImageView playAll;
    private TextView playAllText; // MODIFIED by beibei.yang, 2016-06-17,BUG-2343725,2203366

    public static SingerSongFragment newInstance(){
        return new SingerSongFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtil.d(TAG, this.toString());
        View rootView =  inflater.inflate(R.layout.fragment_singer_song, container, false);
        recyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerview);
        emptyLayoutV2 = (EmptyLayoutV2)rootView.findViewById(R.id.empty_view_container);
        emptyLayoutV2.setOnClickListener(this);
        singerSongAdapter = new SingerSongAdapter(rootView.getContext());
        singerSongAdapter.setOnDetailItemClickListener(this);
        batchOperateLayout = (ViewGroup)rootView.findViewById(R.id.batch_operate_layout);
        final TextView batchOperateTV = (TextView)rootView.findViewById(R.id.batch_operate);
        batchOperateTV.setOnClickListener(this);
        playAll = (ImageView) rootView.findViewById(R.id.network_play_all_image);
        playAll.setOnClickListener(this);
        /* MODIFIED-BEGIN by beibei.yang, 2016-06-17,BUG-2343725,2203366*/
        playAllText = (TextView) rootView.findViewById(R.id.network_play_all);
        playAllText.setOnClickListener(this);
        /* MODIFIED-END by beibei.yang,BUG-2343725,2203366*/
        recyclerView.setAdapter(singerSongAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            private int totalDy = 0;
            private boolean t = false;
            private int tmpDy = 0;
            private int lastVisibleItem;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                SingerSongAdapter adapter = (SingerSongAdapter) recyclerView.getAdapter();
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter.getItemCount()) {
                    SingerDetailActivity singerDetailActivity = (SingerDetailActivity) getActivity();
                    if (singerDetailActivity.isArtistHotSongMore()) {
                        adapter.setIsMore(true);
                        adapter.setState(FooterView.STATE_LOADING);
                        adapter.notifyItemChanged(lastVisibleItem, true);
                        singerDetailActivity.loadSingerSongData();
                    } else {
                        adapter.setIsMore(false);
                        adapter.setState(FooterView.STATE_NOMOREDATA);
                        adapter.notifyItemChanged(lastVisibleItem, true);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                batchOperateLayout.setTranslationY(totalDy);
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
        super.onDestroy();
    }
    //[BUGFIX]-ADD by yanjia.li, 2016-06-18,BUG-2197064 end

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SingerDetailActivity associatedActivity = (SingerDetailActivity)getActivity();
        List<SongDetailBean> songDetailBeanList = associatedActivity.getSongDetailBeanList();
        if(songDetailBeanList==null){
            associatedActivity.loadSingerSongData();
        }else {
            updateArtistHotSongs(songDetailBeanList);
        }
    }

    /**
     * 更新展示状态
     */
    public void updateStatus(int totalSongCount, int realSongCount){
        if(!isAdded()){
            return;
        }
        if(singerSongAdapter ==null){
            return;
        }
        SingerDetailActivity associatedActivity = (SingerDetailActivity)getActivity();
        if(associatedActivity==null){
            return;
        }
        int loadSingerSongStatus = associatedActivity.getLoadSingerSongStatus();
        if(loadSingerSongStatus==3){
            if(associatedActivity.getArtistHotSongPage()==1){
                emptyLayoutV2.setErrorType(EmptyLayoutV2.NETWORK_ERROR);
            }
            singerSongAdapter.setState(FooterView.STATE_FAILED);
        }else if(loadSingerSongStatus==2){
            if(totalSongCount > 0 && realSongCount == 0){
                singerSongAdapter.setState(FooterView.STATE_NORMAL);
                emptyLayoutV2.setErrorType(EmptyLayoutV2.NO_VALID_SONG);
                return;
            }

            emptyLayoutV2.setErrorType(EmptyLayoutV2.HIDE_LAYOUT);
            if (associatedActivity.isArtistHotSongMore()) {
                if(realSongCount == 0){
                    singerSongAdapter.setState(FooterView.STATE_OTHER);
                    return;
                }
                singerSongAdapter.setIsMore(true);
                singerSongAdapter.setState(FooterView.STATE_NORMAL);
            } else {
                singerSongAdapter.setIsMore(false);
                singerSongAdapter.setState(FooterView.STATE_NOMOREDATA);
            }
        }else if(loadSingerSongStatus==1){
            if(associatedActivity.getArtistHotSongPage()==1){
                emptyLayoutV2.setErrorType(EmptyLayoutV2.NETWORK_LOADING);
            }
            singerSongAdapter.setState(FooterView.STATE_LOADING);
        }
    }

    public void updateArtistHotSongs(List<SongDetailBean> songDetailBeanList){
        if(!isAdded()){
            return;
        }
        if(singerSongAdapter ==null){
            return;
        }
        SingerDetailActivity associatedActivity = (SingerDetailActivity)getActivity();
        if(associatedActivity==null){
            return;
        }
        singerSongAdapter.setIsMore(associatedActivity.isArtistHotSongMore());
        singerSongAdapter.addDataList(songDetailBeanList);
        singerSongAdapter.notifyDataSetChanged();
        if(songDetailBeanList.size()==0){
            batchOperateLayout.setVisibility(View.INVISIBLE);
            emptyLayoutV2.setErrorType(EmptyLayoutV2.NODATA_ENABLE_CLICK);
        }else{
            emptyLayoutV2.setErrorType(EmptyLayoutV2.HIDE_LAYOUT);
        }
    }

    @Override
    public void onClick(View v) {
        if(!isAdded()){
            return;
        }
        Context context = getContext();
        int id = v.getId();
//        if(id== R.id.batch_operate){
//            //MobclickAgent.onEvent(getActivity(), MobConfig.SINGER_SONG_DETAIL_BATCH_OPERATION);
//            Intent intent = new Intent(getActivity(), NetworkBatchActivity.class);
//            Bundle bundle = new Bundle();
//            bundle.putString("name", ((SingerDetailActivity) getActivity()).getArtistName());
//            intent.putExtras(bundle);
//            MusicUtils.setSongDetailData(singerSongAdapter.getDataList());
//            startActivity(intent);
//        }else if(id== R.id.empty_view_container){
//            if (SystemUtility.getNetworkType() == SystemUtility.NetWorkType.none) {
//                Toast.makeText(v.getContext(), R.string.network_error_prompt, Toast.LENGTH_SHORT).show();
//                return;
//            }
//            SingerDetailActivity associatedActivity = (SingerDetailActivity)getActivity();
//            if(associatedActivity!=null){
//                associatedActivity.loadSingerSongData();
//                updateStatus(0, 0);
//            }
//        } else if(id == R.id.network_play_all_image || id == R.id.network_play_all) { // MODIFIED by beibei.yang, 2016-06-17,BUG-2343725,2203366
//            if(singerSongAdapter ==null){
//                return;
//            }
//            final List<SongDetailBean> dataList = singerSongAdapter.getDataList();
//            if(dataList != null){
//                AuditionAlertDialog.getInstance(getActivity()).showWrapper(false, new AuditionAlertDialog.OnSelectedListener() {
//                    @Override
//                    public void onPlay() {
//                        new LiveMusicPlayTask(getActivity()).playNow(dataList,0, isFirstClick);
//                        isFirstClick = false;
//                    }
//                });
//            }
//        }
    }

    @Override
    public void onClick(View v, Object object, int position) {
        if(!isAdded()){
            return;
        }
//        Context context = getContext();
//        int id = v.getId();
//        if(id== R.id.item_menu_image_button){
//            //MobclickAgent.onEvent(getActivity(), MobConfig.SINGER_SONG_DETAIL_ITEM_MENU);
//            operateSongDetailBean = (SongDetailBean)object;
//            PopupMenu popup = new PopupMenu(context, v, Gravity.CENTER);
//            MenuInflater menuInflater = popup.getMenuInflater();
////            Menu menu = popup.getMenu();
////            menuInflater.inflate(R.menu.song_operate1, menu);
////            popup.setOnMenuItemClickListener(this);
////            popup.setOnDismissListener(this);
////            popup.show();
//            AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity(),android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
//            View mLocalMediaDialogView ;
//            mLocalMediaDialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_hotmusicrecommend, null);
//            final MediaInfo info = LibraryNavigationUtil.getMediaInfoBySong(context, operateSongDetailBean, position, Util.getTransionId(), MediaInfo.SRC_TYPE_DEEZER, false);
//            mBuilder.setView(mLocalMediaDialogView);
//            final AlertDialog mLocalMediaDialog = mBuilder.create();
//
//            final Menu menu1 = popup.getMenu();
//            menuInflater.inflate(R.menu.song_operate2, menu1);
//
//            View.OnClickListener dialogListener = new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    MenuItem item = menu1.findItem(R.id.action_favorite_playlist);
//                    switch (v.getId()){
//                        case R.id.action_favorite_playlist:
//                            //MobclickAgent.onEvent(getActivity(), MobConfig.SINGER_SONG_DETAIL_ITEM_ADDTOSONGLIST);
//                            new LiveMusicPlayTask(getActivity()).save2Favourite(operateSongDetailBean);
//                            break;
//                        case R.id.cancle_favorite_playlist:
//                            LibraryNavigationUtil.saveFavourite(getContext(),info);
//                            break;
//                        case R.id.action_play_next:
//                            //item = menu1.findItem(R.id.action_play_next);
//                            if(operateSongDetailBean != null){
//                                //MobclickAgent.onEvent(getActivity(), MobConfig.SINGER_SONG_DETAIL_ITEM_PLAYNEXT);
//                                new LiveMusicPlayTask(getActivity()).playAtNext(operateSongDetailBean);
//                            }
//                            else{
//                                NLog.i(TAG, "detail is null");
//                            }
//                            break;
//                        case R.id.action_download:
////                            item = menu1.findItem(R.id.action_download);
////                            LibraryNavigationUtil.manageMenuItem(LibraryNavigationUtil.ItemMediaType.SINGLE_MEDIA, item, getActivity(), c);
//                            //MobclickAgent.onEvent(getActivity(), MobConfig.SINGER_SONG_DETAIL_ITEM_DOWNLOAD);
//                            IDownloader downloader = DownloadManager.getInstance(getActivity()).getDownloader();
//                            operateSongDetailBean.quality = getDefaultMode();
//                            downloader.startMusicDownload(operateSongDetailBean.song_id);
//                            break;
//                        case R.id.action_go_to_artist:
//                            //item = menu1.findItem(R.id.action_go_to_artist);
//                            //SingerDetailActivity.launch(getActivity(), songDetailBean.artist_id, songDetailBean.artist_name, 0, 0);
//                            break;
//                        case R.id.action_go_to_album:
//                            // item = menu1.findItem(R.id.action_go_to_album);
//                            //MobclickAgent.onEvent(getActivity(), MobConfig.SINGER_SONG_DETAIL_ITEM_GOTOALBUM);
//                            LocalAlbumDetailActivity.launch(getActivity(), operateSongDetailBean.album_id, operateSongDetailBean.album_name, 0);
//                            break;
//                        case R.id.action_add_to_song_list:
//                            // item = menu1.findItem(R.id.action_add_to_song_list);
//                            //MobclickAgent.onEvent(getActivity(), MobConfig.SINGER_SONG_DETAIL_ITEM_ADDTOSONGLIST);
//                            new LiveMusicPlayTask(getActivity()).add2SongList(operateSongDetailBean);
//                            break;
//                        case R.id.action_add_to_play_queue:
//                            //  item = menu1.findItem(R.id.action_add_to_play_queue);
//                            if(operateSongDetailBean != null){
//                                //MobclickAgent.onEvent(getActivity(), MobConfig.SINGER_SONG_DETAIL_ITEM_ADDTOPLAYQUEUE);
//                                List<SongDetailBean> list = new ArrayList<SongDetailBean>();
//                                list.add(operateSongDetailBean);
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
//            action_go_to_artist.setVisibility(View.GONE);
//            TextView action_go_to_album = (TextView)mLocalMediaDialogView.findViewById(R.id.action_go_to_album);
//            TextView action_add_to_song_list = (TextView)mLocalMediaDialogView.findViewById(R.id.action_add_to_song_list);
//            TextView action_add_to_play_queue = (TextView)mLocalMediaDialogView.findViewById(R.id.action_add_to_play_queue);
//            TextView cancle_favorite_playlist =(TextView)mLocalMediaDialogView.findViewById(R.id.cancle_favorite_playlist);
//            if (LibraryNavigationUtil.isLLiveSongSaved(context,operateSongDetailBean,MediaInfo.SRC_TYPE_DEEZER)){
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
//            action_go_to_album.setOnClickListener(dialogListener);
//            action_add_to_song_list.setOnClickListener(dialogListener);
//            action_add_to_play_queue.setOnClickListener(dialogListener);
//            cancle_favorite_playlist.setOnClickListener(dialogListener);
//            mLocalMediaDialog.show();
//
//        }
//        else if(id == R.id.item_view){
//            SongDetailBean detail = (SongDetailBean)object;
//
//            if(detail != null){
//                //MobclickAgent.onEvent(getActivity(), MobConfig.SINGER_DETAIL_SONG_ITEM_PLAYNOW);
//                if (singerSongAdapter.getDataList() != null && position < singerSongAdapter.getDataList().size()){
//                    NLog.d(TAG, "SingerSongFragment songs = " + singerSongAdapter.getDataList().get(position));
//                }
//
//                final int pos = position;
//                AuditionAlertDialog.getInstance(getActivity()).showWrapper(false, new AuditionAlertDialog.OnSelectedListener() {
//                    @Override
//                    public void onPlay() {
//                        new LiveMusicPlayTask(getActivity()).playNow(singerSongAdapter.getDataList(), pos, isFirstClick);
//                        isFirstClick = false;
//                    }
//                });
//            }
//        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_favorite_playlist:
//                //MobclickAgent.onEvent(getActivity(), MobConfig.SINGER_SONG_DETAIL_ITEM_ADDTOSONGLIST);
//                new LiveMusicPlayTask(getActivity()).save2Favourite(operateSongDetailBean);
//                break;
//            case R.id.action_play_next:
//                if(operateSongDetailBean != null){
//                    //MobclickAgent.onEvent(getActivity(), MobConfig.SINGER_SONG_DETAIL_ITEM_PLAYNEXT);
//                    new LiveMusicPlayTask(getActivity()).playAtNext(operateSongDetailBean);
//                }
//                else{
//                    NLog.i(TAG, "detail is null");
//                }
//                break;
//            case R.id.action_download:
//                NLog.d(TAG, operateSongDetailBean.toString());
//                //MobclickAgent.onEvent(getActivity(), MobConfig.SINGER_SONG_DETAIL_ITEM_DOWNLOAD);
//                IDownloader downloader = DownloadManager.getInstance(this.getActivity()).getDownloader();
//                operateSongDetailBean.quality = getDefaultMode();
//                downloader.startMusicDownload(operateSongDetailBean.song_id);
//                break;
//            case R.id.action_add_to_song_list:
//                //MobclickAgent.onEvent(getActivity(), MobConfig.SINGER_SONG_DETAIL_ITEM_ADDTOSONGLIST);
//                new LiveMusicPlayTask(getActivity()).add2SongList(operateSongDetailBean);
//                break;
//            case R.id.action_add_to_play_queue:
//                if(operateSongDetailBean != null){
//                    //MobclickAgent.onEvent(getActivity(), MobConfig.SINGER_SONG_DETAIL_ITEM_ADDTOPLAYQUEUE);
//                    List<SongDetailBean> list = new ArrayList<SongDetailBean>();
//                    list.add(operateSongDetailBean);
//                    new LiveMusicPlayTask(getActivity()).add2Queue(list);
//                }
//                else{
//                    NLog.i(TAG, "detail is null");
//                }
//                break;
//            case R.id.action_go_to_album:
//                //MobclickAgent.onEvent(getActivity(), MobConfig.SINGER_SONG_DETAIL_ITEM_GOTOALBUM);
//                LocalAlbumDetailActivity.launch(getActivity(), operateSongDetailBean.album_id, operateSongDetailBean.album_name, 0);
//                break;
//            case R.id.action_batch_add_to_song_list://批量添加歌单
//                //MobclickAgent.onEvent(getActivity(), MobConfig.SINGER_SONG_BATCH_OPERATION_ADDTOSONGLIST);
//                new LiveMusicPlayTask(getActivity()).add2SongList(singerSongAdapter.getDataList());
//                break;
//            case R.id.action_batch_add_to_play_queue://批量播放列表
//                //MobclickAgent.onEvent(getActivity(), MobConfig.SINGER_SONG_BATCH_OPERATION_ADDTOPLAYQUEUE);
//                new LiveMusicPlayTask(getActivity()).add2Queue(singerSongAdapter.getDataList());
//                break;
//            case R.id.action_batch_download://批量下载：
//                NLog.e(TAG,"batch_download");
//               // MobclickAgent.onEvent(getActivity(), MobConfig.SINGER_SONG_BATCH_OPERATION_DOWNLOAD);
//                IDownloader batchDownloader = DownloadManager.getInstance(this.getActivity()).getDownloader();
//                batchDownloader.startBatchMusicDownload(singerSongAdapter.getDataList());
//                break;
//        }
        return false;
    }

    @Override
    public void onDismiss(PopupMenu menu) {
        operateSongDetailBean = null;
    }

    private SongDetailBean operateSongDetailBean;
    public String getDefaultMode(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int mode = sharedPreferences.getInt("quality_download_default", 0);
        Log.d("test", " mode = " + mode);
        switch (mode){
            case 0:
                return getString(R.string.auto_download);

            case 1:
                return getString(R.string.standard_download);

            case 2:
                return getString(R.string.high_quality);

        }
        return null;
    }
}
