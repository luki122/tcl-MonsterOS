package cn.tcl.music.activities.live;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiami.sdk.utils.ImageUtil;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.activities.BaseMusicActivity;
import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.PathUtils;
import cn.tcl.music.fragments.live.CollectDetailSongFragment;
import cn.tcl.music.model.live.CollectionBean;
import cn.tcl.music.model.live.LiveCollectsBean;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.network.DataRequest;
import cn.tcl.music.network.ILoadData;
import cn.tcl.music.network.LiveGetMusicCollectsTask;
import cn.tcl.music.network.LiveMusicAddCollectsTask;
import cn.tcl.music.network.LiveMusicCollectDetailTask;
import cn.tcl.music.network.LiveMusicRemoveCollectsTask;
import cn.tcl.music.util.ToastUtil;
import cn.tcl.music.util.Util;
import mst.app.dialog.AlertDialog;
import mst.widget.toolbar.Toolbar;

public class OnlinePlayListDetailActivity extends BaseMusicActivity implements ILoadData, View.OnClickListener {
    private static final String TAG = OnlinePlayListDetailActivity.class.getSimpleName();
    private static final int COLLECT_ICON_WIDTH = 15;
    private static final int FAVORITE = 1;
    private String mListId;
    private String mCollectName;
    private boolean mIsCollectDetailSongMore;

    private List<SongDetailBean> mSongDetailBeanList;
    private String mDescription;

    private LiveMusicCollectDetailTask mLiveMusicCollectDetailTask;
    private LiveMusicAddCollectsTask mLiveMusicAddAlbumTask;
    private LiveMusicRemoveCollectsTask mLiveMusicRemoveCollectsTask;
    private LiveGetMusicCollectsTask mLiveGetMusicCollectsTask;

    private int mLoadCollectDetailStatus = CommonConstants.PENDING;//0 is pending,1 is running,2 is success,3 is fail
    private int mTotalSongCount = 0;
    private boolean mIsCollectAlbum;
    private CollectDetailSongFragment mCollectDetailSongFragment;
    private LayoutInflater mLayoutInflater;
    private TextView mDescriptionTextView;
    private TextView mAlbumNameTextView;
    private ImageView mAlbumCoverTextView;
    private TextView mCollectAlbumTextView;
    private TextView mShareAlbumTextView;
    private TextView mDetailAlbumTextView;
    private CollectionBean mCollectionBean;

    public static void launch(Activity from, String listId, String collectName, int collectSongCount) {
        Intent intent = new Intent(from, OnlinePlayListDetailActivity.class);
        intent.putExtra(CommonConstants.LIST_ID, listId);
        intent.putExtra(CommonConstants.COLLECT_NAME, collectName);
        intent.putExtra(CommonConstants.COLLECT_SONG_COUNT, collectSongCount);
        from.startActivity(intent);
    }

    @Override
    protected Activity getMainActivity() {
        return this;
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_online_play_list);
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.playlist_detail_toolbar);
        toolbar.setTitle(getResources().getString(R.string.playlist));
        toolbar.setTitleTextAppearance(OnlinePlayListDetailActivity.this, R.style.ToolbarTitle);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(this);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initToolBar();
        mIsCollectDetailSongMore = false;
        Intent intent = getIntent();
        mListId = intent.getStringExtra(CommonConstants.LIST_ID);
        mCollectName = intent.getStringExtra(CommonConstants.COLLECT_NAME);
        mTotalSongCount = intent.getIntExtra(CommonConstants.COLLECT_SONG_COUNT, 0);
        if (TextUtils.isEmpty(mListId)) {
            finish();
            return;
        }
        init();
        //remove for music performance
        //setHideActionBar(true); // MODIFIED by beibei.yang, 2016-05-18,BUG-2104905
        mCollectDetailSongFragment = CollectDetailSongFragment.newInstance();
        getFragmentManager().beginTransaction().replace(R.id.main_container, mCollectDetailSongFragment).commit();
        loadCollectDetailData();
    }

    public String getCollectName() {
        return mCollectName;
    }

    public String getDescription() {
        return mDescription;
    }

    public List<SongDetailBean> getSongDetailBeanList() {
        return mSongDetailBeanList;
    }

    public int getSongDetailBeanListCount() {
        int count = 0;
        if (null != mSongDetailBeanList) {
            count = mSongDetailBeanList.size();
        }
        return count;
    }

    public boolean isCollectDetailSongMore() {
        return mIsCollectDetailSongMore;
    }

    public int getLoadCollectDetailStatus() {
        return mLoadCollectDetailStatus;
    }

    public void loadCollectDetailData() {
        if (mLoadCollectDetailStatus == CommonConstants.RUNNING) {
            return;
        }
        if (mLiveMusicCollectDetailTask != null && (mLiveMusicCollectDetailTask.getStatus() != AsyncTask.Status.FINISHED)) {
            mLiveMusicCollectDetailTask = null;
            return;
        }
        mLoadCollectDetailStatus = CommonConstants.RUNNING;
        mLiveMusicCollectDetailTask = new LiveMusicCollectDetailTask(getApplicationContext(), this, mListId);
        mLiveMusicCollectDetailTask.executeMultiTask();

        mLiveGetMusicCollectsTask = new LiveGetMusicCollectsTask(getApplicationContext(), this, mListId);
        mLiveGetMusicCollectsTask.executeMultiTask();

    }

    @Override
    public void onLoadFail(int dataType, String message) {
        if (DataRequest.Type.TYPE_LIVE_COLLECT_DETAIL == dataType) {
            mLoadCollectDetailStatus = CommonConstants.FAIL;
            //collectDetailMainFrameFragment.updateSingerSongStatus(0, 0);
            //collectDetailMainFrameFragment.updateDescriptionStatus();
            mCollectDetailSongFragment.updateStatus(0, 0);
        } else if (DataRequest.Type.TYPE_LIVE_ADD_COLLECTS == dataType) {
            ToastUtil.showToast(this, message);
        }
    }

    @Override
    public void onLoadSuccess(int dataType, List datas) {

        if (null == datas) {
            return;
        }
        if (datas.isEmpty()) {
            return;
        }
        if (DataRequest.Type.TYPE_LIVE_COLLECT_DETAIL == dataType) {
            if (mLoadCollectDetailStatus == CommonConstants.SUCCESS) {
                return;
            }
            mCollectionBean = (CollectionBean) datas.get(0);
            if (null == mCollectionBean.songs) {
                return;
            }
            if (null == mSongDetailBeanList) {
                mSongDetailBeanList = new ArrayList<SongDetailBean>();
            }
            if (TextUtils.isEmpty(mCollectionBean.collect_name)) {
                mCollectName = getString(R.string.unknown);
            } else {
                mCollectName = mCollectionBean.collect_name;
            }

            updateUI(mCollectionBean);

            mDescription = mCollectionBean.description;
            mSongDetailBeanList.addAll(mCollectionBean.songs);
            mLoadCollectDetailStatus = CommonConstants.SUCCESS;
            mCollectDetailSongFragment.updateCollectDetailSong(mSongDetailBeanList);
            mCollectDetailSongFragment.updateStatus(mTotalSongCount, mSongDetailBeanList.size());
            mCollectDetailSongFragment.updateSongCount(mCollectionBean.songs.size());
        } else if (DataRequest.Type.TYPE_LIVE_ADD_COLLECTS == dataType) {
            changeStateToFavorite();
            ToastUtil.showToast(this, getResources().getString(R.string.song_had_been_added_to_favouriteList));
        } else if (DataRequest.Type.TYPE_LIVE_REMOVE_COLLECTS == dataType) {
            changeStateToNormal();
            ToastUtil.showToast(this, getResources().getString(R.string.cancel_favorite));
        } else if (DataRequest.Type.TYPE_LIVE_GET_COLLECTS_FAVORITE == dataType) {
            LiveCollectsBean bean = (LiveCollectsBean) datas.get(0);
            if (bean.favorite == FAVORITE) {
                changeStateToFavorite();
            } else {
                changeStateToNormal();
            }
        }
    }

    private void changeStateToFavorite() {
        mIsCollectAlbum = true;
        int iconWidth = Util.dip2px(this, COLLECT_ICON_WIDTH);
        Drawable drawable = getDrawable(R.drawable.online_collected);
        drawable.setBounds(0, 0, iconWidth, iconWidth);
        mCollectAlbumTextView.setCompoundDrawables(null, drawable, null, null);
        mCollectAlbumTextView.setText(R.string.add_favorite);
    }

    private void changeStateToNormal() {
        mIsCollectAlbum = false;
        int iconWidth = Util.dip2px(this, COLLECT_ICON_WIDTH);
        Drawable drawable = getDrawable(R.drawable.online_collect);
        drawable.setBounds(0, 0, iconWidth, iconWidth);
        mCollectAlbumTextView.setCompoundDrawables(null, drawable, null, null);
        mCollectAlbumTextView.setText(R.string.favorite_playlist);
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
        mLiveMusicCollectDetailTask.cancel(true);
    }

    private void init() {
        mLayoutInflater = getLayoutInflater();
        mAlbumNameTextView = (TextView) findViewById(R.id.playlist_detail_title_tv);
        mAlbumCoverTextView = (ImageView) findViewById(R.id.artwork_image_view);
        mCollectAlbumTextView = (TextView) findViewById(R.id.detail_collect_tv);
        mShareAlbumTextView = (TextView) findViewById(R.id.detail_share_tv);
        mDetailAlbumTextView = (TextView) findViewById(R.id.detail_detail_tv);
        mShareAlbumTextView.setOnClickListener(this);
        mCollectAlbumTextView.setOnClickListener(this);
        mDetailAlbumTextView.setOnClickListener(this);
    }

    private void updateUI(CollectionBean collectionBean) {
        mAlbumNameTextView.setText(collectionBean.collect_name);
        if (!MusicApplication.getApp().isDataSaver() && mAlbumCoverTextView != null) {
            Glide.with(this)
                    .load(ImageUtil.transferImgUrl(collectionBean.collect_logo, 330))
                    .into(mAlbumCoverTextView);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.detail_collect_tv:
                if (mCollectionBean != null) {
                    if (mIsCollectAlbum) {
                        removeCollects();
                    } else {
                        addCollects();
                    }
                } else {
                    ToastUtil.showToast(this, getResources().getString(R.string.load_fail));
                }
                break;
            case R.id.detail_share_tv:
                if (mCollectionBean != null) {
                    PathUtils.shareOnline(this, CommonConstants.URL_COLLECT_SHARE + mCollectionBean.list_id);
                } else {
                    ToastUtil.showToast(this, getResources().getString(R.string.load_fail));
                }
                break;
            case R.id.detail_detail_tv:
                if (mCollectionBean != null) {
                    View view = mLayoutInflater.inflate(R.layout.dialog_collect_detail, (ViewGroup) findViewById(R.id.rl_collect_description));
                    mDescriptionTextView = (TextView) view.findViewById(R.id.tv_collect_description);
                    mDescriptionTextView.setText(mCollectionBean.description);
                    new AlertDialog.Builder(this).setTitle(mCollectionBean.collect_name).setView(view).setPositiveButton("ok", null).show();
                } else {
                    ToastUtil.showToast(this, getResources().getString(R.string.load_fail));
                }
                break;
            default:
                break;
        }
    }

    private void addCollects() {
        mLiveMusicAddAlbumTask = new LiveMusicAddCollectsTask(getApplicationContext(), this, mListId);
        mLiveMusicAddAlbumTask.executeMultiTask();
    }

    private void removeCollects() {
        mLiveMusicRemoveCollectsTask = new LiveMusicRemoveCollectsTask(getApplicationContext(), this, mListId);
        mLiveMusicRemoveCollectsTask.executeMultiTask();
    }
}
