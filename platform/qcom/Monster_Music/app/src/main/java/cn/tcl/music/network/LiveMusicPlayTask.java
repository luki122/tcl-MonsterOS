package cn.tcl.music.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import cn.tcl.music.R;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.PlaylistManager;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.MusicUtil;
import cn.tcl.music.util.Util;

/**
 * Created by dongdong.huang on 2015/11/18.
 * 歌曲播放任务
 */
public class LiveMusicPlayTask{
    private static final String TAG = "LiveMusicPlayTask";
    public static final String MSG_PLAY_MUSIC = "play_music_now";
    private Context mContext;
    private PlaylistManager.AddTypes mAddType;
    private int mSourceType;
    private int mSongListSize = 0;
    private int mTransitionId;
    private boolean mFirstClick;
    private int mPlayType;
    private int mPlayPosition;
    private int mAddedCount = 0;//添加为list时，已添加到播放队列数

    private static final int PLAY_NOW = 1;    //立即播放,点击列表Item
    private static final int PLAY_AT_NEXT = 2;//下一首播放
    private static final int PLAY_AT_LAST = 3;//加入播放列表，末尾播放
    private static final int SAVE_TO_FAVOURITE = 4;//保存到收藏
    private static final int ADD_TO_SONG_LIST = 5;//添加到歌单


    public LiveMusicPlayTask(Context context){
        mContext = context;
        mAddedCount = 0;
        mAddType = PlaylistManager.AddTypes.DEEZER;
        mSourceType = CommonConstants.SRC_TYPE_DEEZER;
    }

    /**
     * 根据songId播放
     */
    public void playBySongId(String songId){
        if(!TextUtils.isEmpty(songId)){
            new LiveMusicSongDetailTask(mContext, new ILoadData() {
                @Override
                public void onLoadSuccess(int dataType, List datas) {
                    if(datas == null || datas.size() == 0){
                        LogUtil.d(TAG, "get no song detail");
                        return;
                    }

                    List<SongDetailBean> list = datas;
                    playSingleNow(list.get(0));
                }

                @Override
                public void onLoadFail(int dataType, String message) {
                    LogUtil.d(TAG, "get song detail fail : " + message);
                }
            }, songId).executeMultiTask();
        }
        else{
            LogUtil.d(TAG, "lack of song id");
        }
    }

    public LiveMusicPlayTask(Context context, int sourceType, PlaylistManager.AddTypes addType){
        mContext = context;
        mAddedCount = 0;
        mAddType = addType;
        mSourceType = sourceType;
    }

    /**
     * 立即播放
     * @param firstClick 是否首次点击列表：是，将所有列表添加到队列
     * @param position 播放item位置
     */
    public void playNow(List<SongDetailBean> songs, int position, boolean firstClick){
        int size = songs != null ? songs.size() : 0;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if(size > 0 && position < size){
//            if (mSourceType == MediaInfo.SRC_TYPE_DOWNLOADED) {
//                LibraryNavigationUtil.playLiveNow(mContext, songs, position, firstClick, mSourceType, mAddType);
//                //sendPlayMsg(songs.get(position)); // when playing the downloaded song ,did not let paly/pause icon display progress
//                return;
//            }
//            if (sharedPreferences.getBoolean("try_WLAN_only",false)){
//                if (Util.getNetworkType() == Util.NETTYPE_WIFI) {
//                    LibraryNavigationUtil.playLiveNow(mContext, songs, position, firstClick, mSourceType, mAddType);
//                    sendPlayMsg(songs.get(position));
//                }else{
//                    //[BUGFIX]-Modified by TCTNJ,yuanxi.jiang, 2016-03-18,PR1761228 begin
//                    Toast.makeText(mContext, R.string.settings_open_WLAN, Toast.LENGTH_SHORT).show();
//                    //[BUGFIX]-Modified by TCTNJ,yuanxi.jiang, 2016-03-18,PR1761228 end
//                    return;
//                }
//            }else{
//                LibraryNavigationUtil.playLiveNow(mContext, songs, position, firstClick, mSourceType, mAddType);
//                sendPlayMsg(songs.get(position));
//            }
        }
    }

    public void playSongNow(List<SongDetailBean> songs, int position, boolean firstClick){
        int size = songs != null ? songs.size() : 0;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if(size > 0 && position < size){

//            if (sharedPreferences.getBoolean("try_WLAN_only",false)){
//                if (Util.getNetworkType() == Util.NETTYPE_WIFI) {
//                    LibraryNavigationUtil.playLiveNow(mContext, songs, position, firstClick, mSourceType, mAddType);
//                    sendPlayMsg(songs.get(position));
//                }else{
//                    //[BUGFIX]-Modified by TCTNJ,yuanxi.jiang, 2016-03-18,1816998  begin
//                    Toast.makeText(mContext, R.string.settings_open_WLAN, Toast.LENGTH_SHORT).show();
//                    //[BUGFIX]-Modified by TCTNJ,yuanxi.jiang, 2016-03-18,1816998  end
//                    return;
//                }
//            }else{
//                LibraryNavigationUtil.playLiveNow(mContext, songs, position, firstClick, mSourceType, mAddType);
//                sendPlayMsg(songs.get(position));
//            }
        }

    }

    /**
     * 播放单独的歌曲,不在歌曲list中
     */
    public void playSingleNow(SongDetailBean song){
        if(song != null){
//            List<SongDetailBean> songs = new ArrayList<SongDetailBean>();
//            songs.add(song);
//            LibraryNavigationUtil.playLiveNow(mContext, songs, 0, false, MediaInfo.SRC_TYPE_DEEZER, PlaylistManager.AddTypes.MEDIA);
//            sendPlayMsg(song);
        }
    }

    /**
     * 下一首播放
     */
    public void playAtNext(SongDetailBean song){
//        LibraryNavigationUtil.playLiveAtNext(mContext, song, mSourceType);
    }

    /**
     * 加入播放队列
     */
    public void add2Queue(List<SongDetailBean> songs){
        int size = songs != null ? songs.size() : 0;

//        if(size > 0){
//            LibraryNavigationUtil.playLiveAtEnd(mContext, songs, mSourceType);
//        }
    }

    public void add2Queue(SongDetailBean song){
        if(song != null){
            List<SongDetailBean> list = new ArrayList<SongDetailBean>();
            list.add(song);
            add2Queue(list);
        }
    }

    /**
     * 保存到收藏
     */
    public void save2Favourite(SongDetailBean song){
        MusicUtil.save2Favourite(mContext, song, mSourceType);
    }

    /**
     * 添加到歌单
     */
    public void add2SongList(List<SongDetailBean> songs){
//        LibraryNavigationUtil.addToSongList(mContext, songs, mSourceType);
    }

    public void add2SongList(SongDetailBean song){
        if(song != null){
            List<SongDetailBean> list = new ArrayList<SongDetailBean>();
            list.add(song);
            add2SongList(list);
        }
    }

    private void sendPlayMsg(SongDetailBean song){
//        MediaInfo mediaInfo = new MediaInfo();
//        mediaInfo.title = song.song_name;
//        mediaInfo.artist = song.artist_name;
//        mediaInfo.artworkPath = song.album_logo;
//        NotificationCenter.defaultCenter().publish(MSG_PLAY_MUSIC, mediaInfo);
    }
}
