package cn.tcl.music.activities.live;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tcl.framework.log.NLog;
import com.xiami.sdk.utils.ImageUtil;

import java.util.ArrayList;
import java.util.List;

import cn.download.mie.base.util.DownloadManager;
import cn.download.mie.downloader.IDownloader;
import cn.tcl.music.R;
import cn.tcl.music.activities.BaseMusicActivity;
import cn.tcl.music.activities.SettingsActivity;
import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.fragments.live.ScenesDetailSongFragment;
import cn.tcl.music.model.live.LiveMusicSceneListBean;
import cn.tcl.music.model.live.RadioDetailBean;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.network.DataRequest;
import cn.tcl.music.network.ILoadData;
import cn.tcl.music.network.LiveMusicRadioScenesDetailTask;
import mst.widget.toolbar.Toolbar;

//import com.umeng.analytics.MobclickAgent;

/**
 * @author zengtao.kuang
 * @Description: 精选集详类
 * @date 2015/11/12 11:25
 * @copyright TCL-MIE
 */
public class ScenesDetailActivity extends BaseMusicActivity implements ILoadData ,View.OnClickListener{
    private static final String TAG = ScenesDetailActivity.class.getSimpleName();
    private static final String RADIO_TYPE = "radion_type";
    private static final String RADIO_ID = "radion_id";
    private static final String RADIO_TITLE = "radion_title";
    private static final String RADIO_LOGO = "radion_logo";
    private String mRadioId;
    private String mRadioType;
    private boolean mRadioDetailSongMore = false;

    private List<SongDetailBean> mSongDetailBeanList;
    private String mRadioName;
    private String mRadioLogoUrl;


    private LiveMusicRadioScenesDetailTask mLiveMusicRadioScenesDetailTask;

    private int mLoadRadioDetailStatus = CommonConstants.PENDING;//0 is pending,1 is running,2 is success,3 is fail
    private int mTotalSongCount = 0;

    private ScenesDetailSongFragment mRadioDetailSongFragment;

    private TextView mRadioNameTv;
    private ImageView mRadioLogo;

    public static void launch(Activity from, LiveMusicSceneListBean bean){
        Intent intent = new Intent(from, ScenesDetailActivity.class);
        intent.putExtra(RADIO_ID, String.valueOf(bean.radio_id));
        intent.putExtra(RADIO_TYPE,String.valueOf(bean.radio_type));
        intent.putExtra(RADIO_TITLE,bean.title);
        intent.putExtra(RADIO_LOGO,bean.logo);
        from.startActivity(intent);
    }

  //add for music performance
    @Override
    protected Activity getMainActivity() {
        return this;
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_radio_songs_list);
    }

    public String getRadioName() {
        return mRadioName;
    }
    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.playlist_detail_toolbar);
        toolbar.inflateMenu(R.menu.memu_other_music);
        toolbar.setTitle(getResources().getString(R.string.scenes_detail_title));
        toolbar.setTitleTextAppearance(ScenesDetailActivity.this,R.style.ToolbarTitle);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent intent = new Intent(ScenesDetailActivity.this, SettingsActivity.class);
                startActivity(intent);
                return false;
            }
        });
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initToolBar();
        Intent intent = getIntent();
        mRadioId = intent.getStringExtra(RADIO_TYPE);
        mRadioType = intent.getStringExtra(RADIO_TYPE);
        mRadioName = intent.getStringExtra(RADIO_TITLE);
        mRadioLogoUrl = intent.getStringExtra(RADIO_LOGO);
        if(TextUtils.isEmpty(mRadioId)){
            finish();
            return;
        }
        init();
        mRadioDetailSongFragment = ScenesDetailSongFragment.newInstance();
        getFragmentManager().beginTransaction().replace(R.id.main_container, mRadioDetailSongFragment).commit();
        loadRadioDetailData();
    }

    public List<SongDetailBean> getSongDetailBeanList(){
        return mSongDetailBeanList;
    }

    public int getSongDetailBeanListCount(){
        if(mSongDetailBeanList==null){
            return 0;
        }
        return mSongDetailBeanList.size();
    }

    public boolean isRadioDetailSongMore(){
        return mRadioDetailSongMore;
    }

    public int getLoadRadioDetailStatus() {
        return mLoadRadioDetailStatus;
    }

    public void loadRadioDetailData(){
        if (mLoadRadioDetailStatus == CommonConstants.RUNNING) {
            return;
        }
        if(mLiveMusicRadioScenesDetailTask !=null&&(mLiveMusicRadioScenesDetailTask.getStatus()!= AsyncTask.Status.FINISHED)){
            mLiveMusicRadioScenesDetailTask = null;
            return;
        }
        mLoadRadioDetailStatus = CommonConstants.RUNNING;
        Context context = getApplicationContext();
        mLiveMusicRadioScenesDetailTask = new LiveMusicRadioScenesDetailTask(context,this, mRadioId, mRadioType);
        mLiveMusicRadioScenesDetailTask.executeMultiTask();

    }

    @Override
    public void onLoadFail(int dataType, String message) {
        NLog.d(TAG, String.valueOf(message));
        if(DataRequest.Type.TYPE_LIVE_RADIO_DETAIL==dataType){
            mLoadRadioDetailStatus = CommonConstants.FAIL;
            mRadioDetailSongFragment.updateStatus(0,0);
        }
    }

    @Override
    public void onLoadSuccess(int dataType, List datas) {
        NLog.d(TAG, datas.toString());
        if(datas==null){
            return;
        }
        if(datas.size()==0){
            return;
        }
        if(DataRequest.Type.TYPE_LIVE_RADIO_DETAIL==dataType){
            if (mLoadRadioDetailStatus == CommonConstants.SUCCESS) {
                return;
            }

            RadioDetailBean radioDetailBean = (RadioDetailBean) datas.get(0);
            if(null==radioDetailBean.songs){
                return;
            }
            if(mSongDetailBeanList==null){
                mSongDetailBeanList = new ArrayList<SongDetailBean>();
            }
            if(TextUtils.isEmpty(mRadioName)){
                mRadioName = getString(R.string.unknown);
            }

            updateUI(radioDetailBean);

            mSongDetailBeanList.clear();
            mSongDetailBeanList.addAll(radioDetailBean.songs);
            mLoadRadioDetailStatus = CommonConstants.SUCCESS;
            mRadioDetailSongFragment.updateCollectDetailSong(mSongDetailBeanList);
            mRadioDetailSongFragment.updateStatus(mTotalSongCount,mSongDetailBeanList.size());
            mRadioDetailSongFragment.updateSongCount(radioDetailBean.songs.size());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //MobclickAgent.onResume(this);
    }

    @Override
    public void onCurrentMusicMetaChanged() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        //MobclickAgent.onPause(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLiveMusicRadioScenesDetailTask != null) {
            mLiveMusicRadioScenesDetailTask.cancel(true);
        }
    }

    private void init(){
        mRadioNameTv = (TextView) findViewById(R.id.playlist_detail_title_tv);
        mRadioLogo = (ImageView) findViewById(R.id.artwork_image_view);
        findViewById(R.id.detail_download_tv).setOnClickListener(this);
        findViewById(R.id.detail_add_tv).setOnClickListener(this);
        findViewById(R.id.detail_change_tv).setOnClickListener(this);
    }
    private void updateUI(RadioDetailBean radioDetailBean){
        mRadioNameTv.setText(mRadioName);
        if(!MusicApplication.getApp().isDataSaver() && mRadioLogo !=null && !TextUtils.isEmpty(mRadioLogoUrl)){
            Glide.with(this)
                    .load(ImageUtil.transferImgUrl(mRadioLogoUrl, 330))
                    .into(mRadioLogo);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.detail_download_tv:
                IDownloader batchDownloader = DownloadManager.getInstance(this).getDownloader();
                batchDownloader.startBatchMusicDownload(mSongDetailBeanList);
                break;
            case R.id.detail_add_tv:
                break;
            case R.id.detail_change_tv:
                loadRadioDetailData();
                break;
            default:
                break;
        }
    }
}
