package cn.tcl.music.activities.live;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiami.sdk.utils.ImageUtil;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.activities.BaseMusicActivity;
import cn.tcl.music.activities.SettingsActivity;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.PathUtils;
import cn.tcl.music.fragments.live.AlbumDetailSongFragment;
import cn.tcl.music.model.live.AlbumBean;
import cn.tcl.music.model.live.LiveAlbumsBean;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.network.AlbumDetailTask;
import cn.tcl.music.network.DataRequest;
import cn.tcl.music.network.ILoadData;
import cn.tcl.music.network.LiveGetMusicAlbumsTask;
import cn.tcl.music.network.LiveMusicAddAlbumTask;
import cn.tcl.music.network.LiveMusicRemoveAlbumTask;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.ToastUtil;
import cn.tcl.music.util.Util;
import mst.widget.toolbar.Toolbar;


public class AlbumDetailActivity extends BaseMusicActivity implements ILoadData, View.OnClickListener {

    private static final String TAG = AlbumDetailActivity.class.getSimpleName();
    private static final String ALBUM_ID = "album_id";
    private static final String ALBUM_NAME = "album_name";
    public static final String ALBUM_SONG_COUNT = "album_song_count";
    private static final int FAVORITE = 1;
    private static final int COLLECT_ICON_WIDTH = 15;
    private String mAlbumId;
    private String mAlbumName;
    private int mAlbumSongCount = 0;
    private boolean mCollectDetailSongMore = false;
    private boolean mIsCollectAlbum;
    private Toolbar mToolbar;
    private ImageView mLogoImageView;
    private TextView mArtistnameTextView;
    private TextView mDateTv;
    private TextView mFavoriteTextView;
    private TextView mDownloadTextView;
    private TextView mShareTextView;
    private AlbumBean mAlbumBean;
    private List<SongDetailBean> mSongDetailBeanList;
    private String mDescription;
    private AlbumDetailTask mAlbumDetailTask;
    private AlbumDetailSongFragment mAlbumDetailFragment;
    private LiveMusicAddAlbumTask mLiveMusicAddAlbumTask;
    private LiveMusicRemoveAlbumTask mLiveMusicRemoveAlbumTask;
    private LiveGetMusicAlbumsTask mLiveGetMusicAlbumsTask;
    private int mLoadAlbumDetailStatus = CommonConstants.PENDING;//0:pending,1:running,2:success,3:fail

    public static void launch(Activity from, String albumId, String albumName, int songCount) {
        Intent intent_album = new Intent(from, AlbumDetailActivity.class);
        intent_album.putExtra(ALBUM_ID, albumId);
        intent_album.putExtra(ALBUM_NAME, albumName);
        intent_album.putExtra(ALBUM_SONG_COUNT, songCount);
        from.startActivity(intent_album);
    }

    //add for music performance
    @Override
    protected Activity getMainActivity() {
        return this;
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_online_play_list);
        findViewById(R.id.detail_detail_tv).setVisibility(View.GONE);
    }

    private void initToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.playlist_detail_toolbar);
        mToolbar.inflateMenu(R.menu.memu_other_music);
        mToolbar.setTitle(getResources().getString(R.string.album_title));
        mToolbar.setTitleTextAppearance(this, R.style.ToolbarTitle);
        mToolbar.setOnMenuItemClickListener(onMenuItemClick);
        mToolbar.setNavigationIcon(R.drawable.back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
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
                case R.id.action_setting:
                    Intent intent = new Intent(AlbumDetailActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    break;
            }
            return true;
        }
    };

    private void init() {
        mLogoImageView = (ImageView) findViewById(R.id.artwork_image_view);
        mArtistnameTextView = (TextView) findViewById(R.id.playlist_detail_title_tv);
        mDateTv = (TextView) findViewById(R.id.playlist_detail_date_tv);
        mFavoriteTextView = (TextView) findViewById(R.id.detail_collect_tv);
        mDownloadTextView = (TextView) findViewById(R.id.detail_download_tv);
        mShareTextView = (TextView) findViewById(R.id.detail_share_tv);
        mFavoriteTextView.setOnClickListener(this);
        mDownloadTextView.setOnClickListener(this);
        mShareTextView.setOnClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mAlbumId = intent.getStringExtra(ALBUM_ID);
        mAlbumName = intent.getStringExtra(ALBUM_NAME);
        mAlbumSongCount = intent.getIntExtra(ALBUM_SONG_COUNT, 0);
        if (TextUtils.isEmpty(mAlbumId)) {
            finish();
            return;
        }
        init();
        initToolBar();
        //remove for music performance
        //setHideActionBar(true); // MODIFIED by beibei.yang, 2016-05-18,BUG-2104905
        mAlbumDetailFragment = new AlbumDetailSongFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ALBUM_SONG_COUNT, mAlbumSongCount);
        mAlbumDetailFragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.main_container, mAlbumDetailFragment).commit();
        loadAlbumDetailData();
    }

    public String getAlbumName() {
        return mAlbumName;
    }

    public String getDescription() {
        return mDescription;
    }

    public List<SongDetailBean> getSongDetailBeanList() {
        return mSongDetailBeanList;
    }

    public int getSongDetailBeanListCount() {
        if (mSongDetailBeanList == null) {
            return 0;
        }
        return mSongDetailBeanList.size();
    }

    public boolean isCollectDetailSongMore() {
        return mCollectDetailSongMore;
    }

    public int getLoadAlbumDetailStatus() {
        return mLoadAlbumDetailStatus;
    }

    public void loadAlbumDetailData() {
        if (mLoadAlbumDetailStatus == CommonConstants.RUNNING) {
            return;
        }
        if (mAlbumDetailTask != null && (mAlbumDetailTask.getStatus() != AsyncTask.Status.FINISHED)) {
            mAlbumDetailTask = null;
            return;
        }
        mLoadAlbumDetailStatus = CommonConstants.RUNNING;
        Context context = getApplicationContext();
        mAlbumDetailTask = new AlbumDetailTask(context, this, mAlbumId);
        mAlbumDetailTask.executeMultiTask();

        mLiveGetMusicAlbumsTask = new LiveGetMusicAlbumsTask(this, this, mAlbumId);
        mLiveGetMusicAlbumsTask.executeMultiTask();

    }

    @Override
    public void onLoadFail(int dataType, String message) {
        LogUtil.d(TAG, String.valueOf(message));
        if (DataRequest.Type.TYPE_LIVE_ALBUM_DETAIL == dataType) {
            mLoadAlbumDetailStatus = CommonConstants.FAIL;
            mAlbumDetailFragment.updateStatus(0, 0);
            //albumDetailFragment.updateAlbumDescriptionStatus();
        } else if (DataRequest.Type.TYPE_LIVE_ADD_ALBUMS == dataType) {
            ToastUtil.showToast(this, message);
        }
    }

    @Override
    public void onLoadSuccess(int dataType, List datas) {
        LogUtil.d(TAG, datas.toString());
        if (datas == null) {
            return;
        }
        if (datas.size() == 0) {
            return;
        }
        if (DataRequest.Type.TYPE_LIVE_ALBUM_DETAIL == dataType) {
            if (mLoadAlbumDetailStatus == CommonConstants.SUCCESS) {
                return;
            }
            mAlbumBean = (AlbumBean) datas.get(0);
            if (null == mAlbumBean.songs) {
                return;
            }
            if (mSongDetailBeanList == null) {
                mSongDetailBeanList = new ArrayList<SongDetailBean>();
            }
            //[BUGFIX]-Add-BEGIN by Peng.Tian,Defect 1940794,2016/04/25
            mDescription = mAlbumBean.description;
            mAlbumName = mAlbumBean.album_name;

            if (TextUtils.isEmpty(mAlbumName)) {
                mAlbumName = getString(R.string.unknown);
            }

            updateUI(mAlbumBean);

            mSongDetailBeanList.addAll(mAlbumBean.songs);

            mLoadAlbumDetailStatus = CommonConstants.SUCCESS;

            mAlbumDetailFragment.updateAlbumDetailSong(mSongDetailBeanList);
            mAlbumDetailFragment.updateStatus(mAlbumSongCount, mSongDetailBeanList.size());
            mAlbumDetailFragment.updateSongCount(mAlbumBean.songs.size());
        } else if (DataRequest.Type.TYPE_LIVE_ADD_ALBUMS == dataType) {
            changeStateToFavorite();
            ToastUtil.showToast(this, getResources().getString(R.string.song_had_been_added_to_favouriteList));
        } else if (DataRequest.Type.TYPE_LIVE_REMOVE_ALBUMS == dataType) {
            changeStateToNormal();
            ToastUtil.showToast(this, getResources().getString(R.string.cancel_favorite));
        } else if (DataRequest.Type.TYPE_LIVE_GET_ALBUMS_FAVORITE == dataType) {
            LiveAlbumsBean bean = (LiveAlbumsBean) datas.get(0);
            LogUtil.d(TAG, "favorite = " + bean.favorite);
            if (bean.favorite == FAVORITE) {
                changeStateToFavorite();
            } else {
                changeStateToNormal();
            }
        }
    }

    private void updateUI(AlbumBean album) {
        if (mLogoImageView != null) {
            Glide.with(this)
                    .load(ImageUtil.transferImgUrl(album.album_logo, 330))
                    .placeholder(R.drawable.default_cover_details_small)
                    .into(mLogoImageView);
            mLogoImageView.setVisibility(View.VISIBLE);
        }

        final String date = Util.timestamp2DateString(album.gmt_publish);
        mDateTv.setText(getResources().getString(R.string.gmt_publish, date));
        mArtistnameTextView.setText(album.artist_name);
        mToolbar.setTitle(album.album_name);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAlbumDetailTask.cancel(true);
        /*mLiveMusicAddAlbumTask.cancel(true);
        mLiveMusicRemoveAlbumTask.cancel(true);
        mLiveGetMusicAlbumsTask.cancel(true);*/
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.detail_collect_tv:

                if (mAlbumBean != null) {
                    if (mIsCollectAlbum) {
                        removeCollects();
                    } else {
                        addCollects();
                    }
                } else {
                    ToastUtil.showToast(this, getResources().getString(R.string.load_fail));
                }
                break;
            case R.id.detail_download_tv:
                break;
            case R.id.detail_share_tv:
                PathUtils.shareOnline(this, CommonConstants.URL_ALBUM_SHARE + mAlbumId);
                break;
            default:
                break;
        }
    }

    private void addCollects() {
        mLiveMusicAddAlbumTask = new LiveMusicAddAlbumTask(this, this, mAlbumId);
        mLiveMusicAddAlbumTask.executeMultiTask();
    }

    private void removeCollects() {
        mLiveMusicRemoveAlbumTask = new LiveMusicRemoveAlbumTask(this, this, mAlbumId);
        mLiveMusicRemoveAlbumTask.executeMultiTask();
    }


    private void changeStateToFavorite() {
        mIsCollectAlbum = true;
        int iconWidth = Util.dip2px(this, COLLECT_ICON_WIDTH);
        Drawable drawable = getDrawable(R.drawable.online_collected);
        drawable.setBounds(0, 0, iconWidth, iconWidth);
        mFavoriteTextView.setCompoundDrawables(null, drawable, null, null);
        mFavoriteTextView.setText(R.string.add_favorite);
    }

    private void changeStateToNormal() {
        mIsCollectAlbum = false;
        int iconWidth = Util.dip2px(this, COLLECT_ICON_WIDTH);
        Drawable drawable = getDrawable(R.drawable.online_collect);
        drawable.setBounds(0, 0, iconWidth, iconWidth);
        mFavoriteTextView.setCompoundDrawables(null, drawable, null, null);
        mFavoriteTextView.setText(R.string.favorite_playlist);
    }
}
