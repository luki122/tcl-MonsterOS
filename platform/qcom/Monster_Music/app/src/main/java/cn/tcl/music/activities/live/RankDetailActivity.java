package cn.tcl.music.activities.live;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
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
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.fragments.live.RankDetailFragment;
import cn.tcl.music.model.live.LiveMusicRankItem;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.network.DataRequest;
import cn.tcl.music.network.ILoadData;
import cn.tcl.music.network.RankDetailTask;
import cn.tcl.music.util.Util;
import mst.widget.toolbar.Toolbar;

public class RankDetailActivity extends BaseMusicActivity implements ILoadData ,View.OnClickListener {

    public static final String TAG = "RankDetailActivity";
    public static final String TYPE = "type";
    public static final String RANK_TITLE = "rankTitle";
    public static final String RANK_SONG_COUNT = "rankSongCount";
    private RankDetailFragment mRankDetailFragment;
    private RankDetailTask mRankDetailTask;
    private int mLoadRankDetailStatus = 0;//0表示pending,1表示running,2表示success,3表示fail

    private List<SongDetailBean> mSongDetailBeanList;
    private String mLogoUrl;
    private String mRankTitle;//榜单名字
    private String mType;
    private int mTotalSongCount;

    public static void launch(Activity from, String type, String rankTitle, int totalSongCount) {
        Intent intent = new Intent(from, RankDetailActivity.class);
        intent.putExtra(TYPE, type);
        intent.putExtra(RANK_TITLE, rankTitle);
        intent.putExtra(RANK_SONG_COUNT, totalSongCount);
        from.startActivity(intent);
    }

    //add for music performance
    @Override
    protected Activity getMainActivity() {
        return this;
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_rank_songs_list);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initToolBar();
        Intent intent = getIntent();
        mType = intent.getStringExtra(TYPE);
        mRankTitle = intent.getStringExtra(RANK_TITLE);
        mTotalSongCount = intent.getIntExtra(RANK_SONG_COUNT, 0);
        if (TextUtils.isEmpty(mType)) {
            finish();
            return;
        }
        if (TextUtils.isEmpty(mRankTitle)) {
            mRankTitle = getString(R.string.unknown);
        }
        setListener();
        mRankDetailFragment = new RankDetailFragment();
        getFragmentManager().beginTransaction().replace(R.id.main_container, mRankDetailFragment).commit();
        loadRankDetail();
    }

    private void setListener(){
        findViewById(R.id.detail_download_tv).setOnClickListener(this);
        findViewById(R.id.detail_add_tv).setOnClickListener(this);
        findViewById(R.id.detail_share_tv).setOnClickListener(this);
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.playlist_detail_toolbar);
        toolbar.setTitle(getResources().getString(R.string.rank_list));
        toolbar.setTitleTextAppearance(RankDetailActivity.this,R.style.ToolbarTitle);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
    public String getRankTitle() {
        return mRankTitle;
    }

    public int getLoadRankDetailStatus() {
        return mLoadRankDetailStatus;
    }

    public void loadRankDetail() {
        if (mLoadRankDetailStatus == CommonConstants.RUNNING) {
            return;
        }
        if (mRankDetailTask != null && (mRankDetailTask.getStatus() != AsyncTask.Status.FINISHED)) {
            mRankDetailTask = null;
            return;
        }
        mLoadRankDetailStatus = CommonConstants.RUNNING;
        Context context = getApplicationContext();
        mRankDetailTask = new RankDetailTask(context, this, mType);
        mRankDetailTask.executeMultiTask();
    }

    public List<SongDetailBean> getSongDetailBeanList() {
        return mSongDetailBeanList;
    }

    @Override
    public void onLoadFail(int dataType, String message) {
        if (DataRequest.Type.TYPE_LIVE_RANK_DETAIL == dataType) {
            mLoadRankDetailStatus = CommonConstants.FAIL;
            mRankDetailFragment.updateStatus(0, 0);
        }
    }

    @Override
    public void onLoadSuccess(int dataType, List datas) {
        if (DataRequest.Type.TYPE_LIVE_RANK_DETAIL == dataType) {
            if (datas == null) {
                return;
            }
            if (datas.size() == 0) {
                return;
            }

            LiveMusicRankItem liveMusicRankItem = (LiveMusicRankItem) datas.get(0);
            if (liveMusicRankItem.songs == null || liveMusicRankItem.songs.size() == 0) {
                return;
            }
            if (mSongDetailBeanList == null) {
                mSongDetailBeanList = new ArrayList<>();
            }
            if (TextUtils.isEmpty(liveMusicRankItem.title)) {
                mRankTitle = getString(R.string.unknown);
            } else {
                mRankTitle = Util.replaceStringTags(this, liveMusicRankItem.title);
            }
            mSongDetailBeanList.clear();
            mSongDetailBeanList.addAll(liveMusicRankItem.songs);

            updateUI(liveMusicRankItem);
            mLogoUrl = liveMusicRankItem.logo;
            mLoadRankDetailStatus = CommonConstants.SUCCESS;
            if (mRankDetailFragment.isAdded()) { // add for 2345572
                mRankDetailFragment.updateRankSongs(mSongDetailBeanList);
                mRankDetailFragment.updateStatus(mTotalSongCount, mSongDetailBeanList.size());
                mRankDetailFragment.updateSongCount(mSongDetailBeanList.size());
            }
        }
        NLog.d(TAG, datas.toString());


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
            case R.id.detail_share_tv:
                break;
            default:
                break;
        }
    }
    private void updateUI(LiveMusicRankItem liveMusicRankItem) {
        ImageView logoImg = (ImageView) findViewById(R.id.artwork_image_view);
        TextView nameTv = (TextView) findViewById(R.id.playlist_detail_title_tv);
        if(logoImg !=null){
            Glide.with(this)
                    .load(ImageUtil.transferImgUrl(liveMusicRankItem.logo, 330))
                    .placeholder(R.drawable.default_cover_details_small)
                    .into(logoImg);
            logoImg.setVisibility(View.VISIBLE);
        }
        nameTv.setText(liveMusicRankItem.title);
    }

    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCurrentMusicMetaChanged() {

    }

    public void onPause() {
        super.onPause();
    }

}
