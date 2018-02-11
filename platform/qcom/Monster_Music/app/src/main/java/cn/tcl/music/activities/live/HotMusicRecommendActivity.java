package cn.tcl.music.activities.live;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.activities.BaseMusicActivity;
import cn.tcl.music.activities.SettingsActivity;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.fragments.live.HotMusicRecommendFragment;
import cn.tcl.music.model.live.LiveMusicDailyRecommend;
import cn.tcl.music.model.live.LiveMusicRecommend;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.network.DataRequest;
import cn.tcl.music.network.ILoadData;
import cn.tcl.music.network.LiveMusicDailyRecommendTask;
import cn.tcl.music.network.LiveMusicRecommendTask;
import cn.tcl.music.util.LogUtil;
import mst.widget.toolbar.Toolbar;

public class HotMusicRecommendActivity extends BaseMusicActivity implements ILoadData, View.OnClickListener {

    public static final String TAG = HotMusicRecommendActivity.class.getSimpleName();

    private static final String POSITION = "position";
    private static final String DAILY_RECOMMEND = "daily_recommend";
    private List<SongDetailBean> mSongDetailBeanList;
    private String mLogoUrl;
    private HotMusicRecommendFragment mHotMusicRecommendFragment;
    private LiveMusicRecommendTask mLiveMusicRecommendTask;
    private LiveMusicDailyRecommendTask mLiveMusicDailyRecommendTask;
    private int mLoadRecommendHotSongsStatus = CommonConstants.PENDING;//0表示pending,1表示running,2表示success,3表示fail
    private int mEnterItemPosition = 0;

    private String mAction = null;
    private ImageView mArtworkImageView;
    private TextView mAddTextView;
    private TextView mDownloadTextView;
    private TextView mShareTextView;

    //add  for music performance
    @Override
    protected Activity getMainActivity() {
        return this;
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_hot_music_recommend);
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.recommend_toolbar);
        toolbar.inflateMenu(R.menu.memu_other_music);
        toolbar.setTitle(getResources().getString(R.string.playlist));
        toolbar.setTitleTextAppearance(HotMusicRecommendActivity.this, R.style.ToolbarTitle);
        toolbar.setOnMenuItemClickListener(onMenuItemClick);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.other_setting:
                    Intent intent = new Intent(HotMusicRecommendActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    break;
            }
            return true;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initToolBar();
        initView();
        mHotMusicRecommendFragment = new HotMusicRecommendFragment();
        getFragmentManager().beginTransaction().replace(R.id.sliding_up_external_container, mHotMusicRecommendFragment).commit();
        Intent intent = getIntent();
        if (intent != null) {
            mEnterItemPosition = intent.getIntExtra(POSITION, 0);
            mAction = intent.getAction();
        }
        if (mAction != null && mAction.equalsIgnoreCase(DAILY_RECOMMEND)) {
            locadDailyRecommendSongs();
        } else {
            loadRecommendHotSongs();
        }
    }

    private void initView(){
        mArtworkImageView = (ImageView) findViewById(R.id.artwork_image_view);
        mAddTextView = (TextView) findViewById(R.id.recommend_add_tv);
        mDownloadTextView = (TextView) findViewById(R.id.recommend_download_tv);
        mShareTextView = (TextView) findViewById(R.id.recommend_share_tv);
        mAddTextView.setOnClickListener(this);
        mDownloadTextView.setOnClickListener(this);
        mShareTextView.setOnClickListener(this);
    }

    public void getArtwork(){
        String artWork = mHotMusicRecommendFragment.getRecommendArtWork();
        Glide.with(this)
                .load(artWork)
                .placeholder(R.drawable.empty_album)
                .into(mArtworkImageView);
    }

    public int getLoadRecommendHotSongsStatus() {
        return mLoadRecommendHotSongsStatus;
    }

    public void locadDailyRecommendSongs() {
        if (mLoadRecommendHotSongsStatus == CommonConstants.RUNNING) {
            return;
        }
        if (mLiveMusicDailyRecommendTask != null && (mLiveMusicDailyRecommendTask.getStatus() != AsyncTask.Status.FINISHED)) {
            mLiveMusicDailyRecommendTask = null;
            return;
        }
        mLoadRecommendHotSongsStatus = CommonConstants.RUNNING;
        Context context = getApplicationContext();
        mLiveMusicDailyRecommendTask = new LiveMusicDailyRecommendTask(context, this, 50);
        mLiveMusicDailyRecommendTask.executeMultiTask();
    }

    public void loadRecommendHotSongs() {
        if (mLoadRecommendHotSongsStatus == CommonConstants.RUNNING) {
            return;
        }
        if (mLiveMusicRecommendTask != null && (mLiveMusicRecommendTask.getStatus() != AsyncTask.Status.FINISHED)) {
            mLiveMusicRecommendTask = null;
            return;
        }
        mLoadRecommendHotSongsStatus = CommonConstants.RUNNING;
        Context context = getApplicationContext();
        mLiveMusicRecommendTask = new LiveMusicRecommendTask(context, this, 50);
        mLiveMusicRecommendTask.executeMultiTask();
    }

    public List<SongDetailBean> getSongDetailBeanList() {
        return mSongDetailBeanList;
    }

    @Override
    public void onLoadFail(int dataType, String message) {
        if (DataRequest.Type.TYPE_LIVE_RECOMMEND == dataType) {
            mLoadRecommendHotSongsStatus = CommonConstants.FAIL;
            mHotMusicRecommendFragment.updateStatus();
        } else if (DataRequest.Type.TYPE_LIVE_RECOMMEND_DAILY == dataType) {
            mLoadRecommendHotSongsStatus = CommonConstants.FAIL;
            mHotMusicRecommendFragment.updateStatus();
        }
    }

    @Override
    public void onLoadSuccess(int dataType, List datas) {
        if (DataRequest.Type.TYPE_LIVE_RECOMMEND == dataType) {
            if (datas == null) {
                return;
            }
            if (datas.size() == 0) {
                return;
            }
            LiveMusicRecommend liveMusicRecommend = (LiveMusicRecommend) datas.get(0);
            if (liveMusicRecommend.songs == null || liveMusicRecommend.songs.size() == 0) {
                return;
            }
            if (mSongDetailBeanList == null) {
                mSongDetailBeanList = new ArrayList<SongDetailBean>();
            }
            mSongDetailBeanList.addAll(liveMusicRecommend.songs);
            mLogoUrl = liveMusicRecommend.logo;
            int size = mSongDetailBeanList.size();

            if (mEnterItemPosition > -1 && mEnterItemPosition < size) {
                mLogoUrl = mSongDetailBeanList.get(mEnterItemPosition).album_logo;
            }

            mHotMusicRecommendFragment.updateLogo(mLogoUrl);
            mHotMusicRecommendFragment.updateRecommendHotSongs(mSongDetailBeanList);
            mLoadRecommendHotSongsStatus = CommonConstants.SUCCESS;
            mHotMusicRecommendFragment.updateStatus();
        } else if (DataRequest.Type.TYPE_LIVE_RECOMMEND_DAILY == dataType) {
            if (datas == null) {
                return;
            }
            if (datas.size() == 0) {
                return;
            }
            LiveMusicDailyRecommend liveMusicDailyRecommend = (LiveMusicDailyRecommend) datas.get(0);
            if (liveMusicDailyRecommend.songs == null || liveMusicDailyRecommend.songs.size() == 0) {
                return;
            }
            if (mSongDetailBeanList == null) {
                mSongDetailBeanList = new ArrayList<SongDetailBean>();
            }
            mSongDetailBeanList.addAll(liveMusicDailyRecommend.songs);
            mLogoUrl = liveMusicDailyRecommend.logo;
            int size = mSongDetailBeanList.size();

            if (mEnterItemPosition > -1 && mEnterItemPosition < size) {
                mLogoUrl = mSongDetailBeanList.get(mEnterItemPosition).album_logo;
            }
            mHotMusicRecommendFragment.updateLogo(mLogoUrl);
            mHotMusicRecommendFragment.updateRecommendHotSongs(mSongDetailBeanList);
            mLoadRecommendHotSongsStatus = CommonConstants.SUCCESS;
            mHotMusicRecommendFragment.updateStatus();
        }
        LogUtil.d(TAG, datas.toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onCurrentMusicMetaChanged() {

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLiveMusicDailyRecommendTask != null && (mLiveMusicDailyRecommendTask.getStatus() == AsyncTask.Status.RUNNING)) {
            mLiveMusicDailyRecommendTask.cancel(true);
            mLiveMusicDailyRecommendTask = null;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.recommend_add_tv:
                //TODO add
                break;
            case R.id.recommend_download_tv:
                //TODO download
                break;
            case R.id.recommend_share_tv:
                //TODO share
                break;
        }
    }
}
