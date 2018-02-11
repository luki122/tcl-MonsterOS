package cn.tcl.music.activities.live;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.MenuItem;
import java.util.ArrayList;
import java.util.List;
import cn.tcl.music.R;
import cn.tcl.music.activities.BaseMusicActivity;
import cn.tcl.music.fragments.live.SingerDetailFrameFragment;
import cn.tcl.music.model.live.AlbumBean;
import cn.tcl.music.model.live.ArtistAlbumsDataBean;
import cn.tcl.music.model.live.ArtistBean;
import cn.tcl.music.model.live.ArtistHotMusicDataBean;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.network.DataRequest;
import cn.tcl.music.network.ILoadData;
import cn.tcl.music.network.LiveMusicArtistAlbumsTask;
import cn.tcl.music.network.LiveMusicArtistDetailTask;
import cn.tcl.music.network.LiveMusicArtistHotSongsTask;

//import com.umeng.analytics.MobclickAgent;

/**
 * @author zengtao.kuang
 * @Description: 歌手详情主框架Activity
 * @date 2015/11/5 20:26
 * @copyright TCL-MIE
 */
public class SingerDetailActivity extends BaseMusicActivity implements ILoadData{

    public static final String TAG = "SingerActivity";
    public static final String ARTIST_ID = "artistId";
    public static final String ARTIST_NAME = "artistName";
    public static final String ARTIST_SONG_COUNT = "artistSongCount";
    public static final String ARTIST_ALBUM_COUNT = "artistAlbumCount";
    private SingerDetailFrameFragment singerDetailFrameFragment;
    private ArtistBean artistBean;
    private List<SongDetailBean> songDetailBeanList;
    private List<AlbumBean> albumBeanList;
    private LiveMusicArtistDetailTask liveMusicArtistDetailTask;
    private LiveMusicArtistHotSongsTask liveMusicArtistHotSongsTask;
    private LiveMusicArtistAlbumsTask liveMusicArtistAlbumsTask;
    private int loadSingerSongStatus = 0;//0表示pending,1表示running,2表示success,3表示fail
    private int loadSingerAlbumStatus = 0;//0表示pending,1表示running,2表示success,3表示fail
    private int loadSingerDetailStatus = 0;//0表示pending,1表示running,2表示success,3表示fail
    private int artistHotSongPage = 1;
    private int artistAlbumPage = 1;
    private boolean artistHotSongMore = true;//艺人的热门歌曲是否还有下一页
    private boolean artistAlbumMore = true;//艺人的专辑是否还有下一页
    private String artistId;
    private String artistName;
    private int artistSongCount = 0;
    private int artistAlbumCount = 0;


    public static void launch(Activity from, String artistId, String artistName, int artistSongCount, int artistAlbumCount){
        //MobclickAgent.onEvent(from, MobConfig.SINGER_DETAIL_BROWSE);
        Intent intent_singer = new Intent(from, SingerDetailActivity.class);
        intent_singer.putExtra(ARTIST_ID, artistId);
        intent_singer.putExtra(ARTIST_NAME, artistName);
        intent_singer.putExtra(ARTIST_SONG_COUNT, artistSongCount);
        intent_singer.putExtra(ARTIST_ALBUM_COUNT, artistAlbumCount);
        from.startActivity(intent_singer);
    }
   //add for music performance 
    @Override
    protected Activity getMainActivity() {
        return this;
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_singer_detail);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_singer_detail);
        Intent intent = getIntent();
        artistId = intent.getStringExtra(ARTIST_ID);
        artistName = intent.getStringExtra(ARTIST_NAME);
        artistSongCount = intent.getIntExtra(ARTIST_SONG_COUNT, 0);
        artistAlbumCount = intent.getIntExtra(ARTIST_ALBUM_COUNT, 0);

        if(TextUtils.isEmpty(artistId)){
            finish();
            return;
        }
		//add for music performance 
        //setHideActionBar(true); // MODIFIED by beibei.yang, 2016-05-18,BUG-2104905
        singerDetailFrameFragment = new SingerDetailFrameFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARTIST_SONG_COUNT, artistSongCount);
        bundle.putInt(ARTIST_ALBUM_COUNT, artistAlbumCount);
        singerDetailFrameFragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.sliding_up_external_container, singerDetailFrameFragment).commit();
        //remove for music performance 
        //postManageNowPlayingFragmentVisibity();

        loadSingerDetailData();
        loadSingerSongData();
        loadSingerAlbumData();
    }
      //remove for music performance 
      // @Override
      //protected void manageContentView() {
      //    setContentView(R.layout.activity_singer_detail);
      // }

    public void loadSingerDetailData(){
        if(loadSingerDetailStatus==1){
            return;
        }
        if(artistBean!=null){
            return;
        }
        if(liveMusicArtistDetailTask!=null&&(liveMusicArtistDetailTask.getStatus()!= AsyncTask.Status.FINISHED)){
            liveMusicArtistDetailTask = null;
            return;
        }
        loadSingerDetailStatus = 1;
        Context context = getApplicationContext();
        liveMusicArtistDetailTask = new LiveMusicArtistDetailTask(context,this,artistId);
        liveMusicArtistDetailTask.executeMultiTask();
    }

    public void loadSingerSongData(){
        if(loadSingerSongStatus==1){
            return;
        }
        if(liveMusicArtistHotSongsTask!=null&&(liveMusicArtistHotSongsTask.getStatus()!= AsyncTask.Status.FINISHED)){
            liveMusicArtistHotSongsTask = null;
            return;
        }
        loadSingerSongStatus = 1;
        Context context = getApplicationContext();
        liveMusicArtistHotSongsTask = new LiveMusicArtistHotSongsTask(context,this,artistId,artistHotSongPage);
        liveMusicArtistHotSongsTask.executeMultiTask();
    }

    public void loadSingerAlbumData(){
        if(loadSingerAlbumStatus==1){
            return;
        }
        if(liveMusicArtistAlbumsTask!=null&&(liveMusicArtistAlbumsTask.getStatus()!= AsyncTask.Status.FINISHED)){
            liveMusicArtistAlbumsTask = null;
            return;
        }
        loadSingerAlbumStatus = 1;
        Context context = getApplicationContext();
        liveMusicArtistAlbumsTask = new LiveMusicArtistAlbumsTask(context,this,artistId,artistAlbumPage);
        liveMusicArtistAlbumsTask.executeMultiTask();
    }

    public List<SongDetailBean> getSongDetailBeanList(){
        return songDetailBeanList;
    }

    public List<AlbumBean> getAlbumBeanList(){
        return albumBeanList;
    }

    public ArtistBean getArtistBean(){
        return artistBean;
    }

    public boolean isArtistHotSongMore(){
        return artistHotSongMore;
    }

    public boolean isArtistAlbumMore(){
        return artistAlbumMore;
    }

    public String getArtistName(){
        return artistName;
    }

    public int getArtistHotSongCount(){
        if(songDetailBeanList==null){
            return 0;
        }
        return songDetailBeanList.size();
    }

    public int getArtistAlbumCount(){
        if(albumBeanList==null){
            return 0;
        }
        return albumBeanList.size();
    }

    public int getArtistHotSongPage() {
        return artistHotSongPage;
    }

    public int getArtistAlbumPage() {
        return artistAlbumPage;
    }

    public int getLoadSingerSongStatus() {
        return loadSingerSongStatus;
    }

    public int getLoadSingerAlbumStatus() {
        return loadSingerAlbumStatus;
    }

    public int getLoadSingerDetailStatus() {
        return loadSingerDetailStatus;
    }




    @Override
    public void onLoadFail(int dataType, String message) {
        if(DataRequest.Type.TYPE_LIVE_ARTIST_DETAIL==dataType){
            loadSingerDetailStatus = 3;
            singerDetailFrameFragment.updateSingerDetailStatus();
        }else if(DataRequest.Type.TYPE_LIVE_ARTIST_HOT_SONGS==dataType){
            loadSingerSongStatus = 3;
            singerDetailFrameFragment.updateSingerSongStatus();
        }else if(DataRequest.Type.TYPE_LIVE_ARTIST_ALBUMS==dataType){
            loadSingerAlbumStatus = 3;
            singerDetailFrameFragment.updateSingerAlbumStatus();
        }
    }

    @Override
    public void onLoadSuccess(int dataType, List datas) {
        if(DataRequest.Type.TYPE_LIVE_ARTIST_DETAIL==dataType){
            artistBean = (ArtistBean)datas.get(0);
            singerDetailFrameFragment.updateSingerDetail(artistBean);
            loadSingerDetailStatus = 2;
            //[BUGFIX]-Add-BEGIN by Peng.Tian,Defect 1940794,2016/04/25
            if(TextUtils.isEmpty(artistBean.artist_name)){
                artistName = getString(R.string.unknown);
            }else {
                artistName = artistBean.artist_name;
            }
            singerDetailFrameFragment.updateTitle();
            singerDetailFrameFragment.updateSingerDetailStatus();
        }else if(DataRequest.Type.TYPE_LIVE_ARTIST_HOT_SONGS==dataType){
            if(songDetailBeanList==null){
                songDetailBeanList = new ArrayList<>();
            }
            ArtistHotMusicDataBean artistHotMusicDataBean = (ArtistHotMusicDataBean)datas.get(0);
            if(artistHotMusicDataBean.songs==null){
                return;
            }
            artistHotSongMore = artistHotMusicDataBean.more;
            if(artistHotMusicDataBean.songs.size()!=0){
                artistHotSongPage++;
                SongDetailBean songDetailBean = artistHotMusicDataBean.songs.get(0);
                if(TextUtils.isEmpty(songDetailBean.artist_name)){
                    artistName = getString(R.string.unknown);
                }else {
                    artistName = songDetailBean.artist_name;
                }
            }
            songDetailBeanList.addAll(artistHotMusicDataBean.songs);
            singerDetailFrameFragment.updateSingerSong(songDetailBeanList);
            singerDetailFrameFragment.updateTitle();
            loadSingerSongStatus = 2;
            singerDetailFrameFragment.updateSingerSongStatus(artistSongCount, songDetailBeanList.size());
        }else if(DataRequest.Type.TYPE_LIVE_ARTIST_ALBUMS==dataType){
            if(albumBeanList==null){
                albumBeanList = new ArrayList<>();
            }
            ArtistAlbumsDataBean artistAlbumsDataBean = (ArtistAlbumsDataBean)datas.get(0);
            if(artistAlbumsDataBean.albums==null){
                return;
            }
            artistAlbumMore = artistAlbumsDataBean.more;
            if(artistAlbumsDataBean.albums.size()!=0){
                artistAlbumPage++;
            }
            if(TextUtils.isEmpty(artistAlbumsDataBean.albums.get(0).artist_name)){
                artistName = getString(R.string.unknown);
            }else {
                artistName = artistAlbumsDataBean.albums.get(0).artist_name;
            }
            albumBeanList.addAll(artistAlbumsDataBean.albums);
            if (singerDetailFrameFragment.isAdded()) {
                singerDetailFrameFragment.updateSingerAlbum(albumBeanList);
                singerDetailFrameFragment.updateTitle();
                //[BUGFIX]-Add-END by Peng.Tian,Defect 1940794,2016/04/25
                loadSingerAlbumStatus = 2;
                singerDetailFrameFragment.updateSingerAlbumStatus(artistAlbumCount, albumBeanList.size());
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //add by lili for umeng 2015/12/1
    public void onResume() {
        super.onResume();
       // MobclickAgent.onResume(this);
    }

    @Override
    public void onCurrentMusicMetaChanged() {

    }

    public void onPause() {
        super.onPause();
        //MobclickAgent.onPause(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        liveMusicArtistDetailTask.cancel(true);
        liveMusicArtistHotSongsTask.cancel(true);
        liveMusicArtistHotSongsTask.cancel(true);
    }
}
