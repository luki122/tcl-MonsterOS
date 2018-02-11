package cn.tcl.music.activities;

import android.app.Fragment;
import android.app.FragmentManager;

import cn.tcl.music.util.ToastUtil;
import mst.app.dialog.ProgressDialog;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.DBUtil;
import cn.tcl.music.database.PathUtils;
import cn.tcl.music.fragments.PlayLyricFragment;
import cn.tcl.music.fragments.PortraitPlayerFragment;
import cn.tcl.music.fragments.QueueFragment;
import cn.tcl.music.media.MediaPlaylist;
import cn.tcl.music.model.MediaInfo;
import cn.tcl.music.model.PlayMode;
import cn.tcl.music.service.MusicPlayBackService;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.SystemUtility;
import cn.tcl.music.view.PlayMusicButton;
import mst.app.MstActivity;
import mst.widget.FragmentStatePagerAdapter;
import mst.widget.ViewPager;

public class PlayingActivity extends MstActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private static final String TAG = PlayingActivity.class.getSimpleName();
    private ViewPager mViewPager;
    private List<Fragment> mFragmentList;
    private QueueFragment mQueueFragment;
    private PortraitPlayerFragment mPortraitPlayerFragment;
    private PlayLyricFragment mPlayLyricFragment;
    protected ImageView mFavoriteImageView;
    protected ImageView mPlayModeImageView;
    private ImageView mPlayPreviousImageView;
    private PlayMusicButton mPlayMusicButton;
    private ImageView mPlayNextImageView;
    private TextView mPlayTimeTextView;
    private TextView mTotalTimeTextView;
    private SeekBar mProgressSeekBar;
    private ImageView mLeftImageView;
    private ImageView mCenterImageView;
    private ImageView mRightImageView;
    private ImageView mBackImageView;
    private ImageView mShareImageView;
    private RelativeLayout mEmptyRelative;
    private RelativeLayout mPlayingRelative;
    private ImageView mEmptyImageView;
    private Window mWindow;
    private RelativeLayout mTopRelative;

    private long mCurrentClickTime = System.currentTimeMillis();
    private long mSumDeltaTime = 0;
    private long mLeftSumDeltaTime = 0;
    private long mPlayTime = 0;
    private int mCurrentTab = -1;
    private boolean mIsUserChangingTrack;
    private MusicPlayBackService.MusicBinder mService;
    private ChangeFavoriteTask mChangeFavoriteTask;

    public static final int MSG_REFRESH = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH:
                    if (mService != null) {
                        refreshView();
                    }
                    mHandler.sendEmptyMessageDelayed(MSG_REFRESH, 500);
            }
        }
    };
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = (MusicPlayBackService.MusicBinder) MusicPlayBackService.MusicBinder.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
        }
    };
    private BroadcastReceiver mLocalTrackChangeListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            if (CommonConstants.META_CHANGED.equals(intent.getAction())) {
                if (mQueueFragment != null) {
                    mQueueFragment.onCurrentMediaMetaChanged(MusicPlayBackService.getArtistName(), MusicPlayBackService.getTitle());
                    setPlayOrPauseButton();
                    mFavoriteImageView.setImageResource(MusicPlayBackService.getCurrentMediaInfo() == null ? R.drawable.favorite_image : (MusicPlayBackService.getCurrentMediaInfo().Favorite ? R.drawable.favorite_image2 : R.drawable.favorite_image));
                }
                if (mPortraitPlayerFragment != null) {
                    mPortraitPlayerFragment.onCurrentMediaMetaChanged(MusicPlayBackService.getArtistName(), MusicPlayBackService.getTitle());
                }
                if (mPlayLyricFragment != null) {
                    mPlayLyricFragment.onCurrentMediaChanged();
                }
            } else if (CommonConstants.PLAY_STATE_CHANGED.equals(intent.getAction())) {
                setPlayOrPauseButton();
                if (mPortraitPlayerFragment != null) {
                    mPortraitPlayerFragment.onCurrentMediaPlayStateChanged(MusicPlayBackService.isPlaying());
                }
            }
        }
    };

    public void registListener() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(CommonConstants.META_CHANGED);
        filter.addAction(CommonConstants.PLAY_STATE_CHANGED);
        registerReceiver(mLocalTrackChangeListener, filter);
        mLocalTrackChangeListener.onReceive(null, null);
    }

    public void unRegistListener() {
        try {
            unregisterReceiver(mLocalTrackChangeListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);
        initView();
        bindService(new Intent(this, MusicPlayBackService.class), mConnection, Service.BIND_AUTO_CREATE);
    }

    private void refreshView() {
        try {
            if (!mIsUserChangingTrack) {
                mPlayTimeTextView.setText(convertLengthToString(mService.position() > mService.duration() ? mService.duration() : mService.position()));
                mProgressSeekBar.setProgress(mService.position() / (mService.duration() < 1000 ? 1000 : mService.duration() / 1000));
            }
            mTotalTimeTextView.setText(convertLengthToString(mService.duration()));
            if (mPlayLyricFragment != null) {
                mPlayLyricFragment.onCurrentMediaPositionChanged(mService.position());
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "refresh view error ,maybe service died");
        }
    }

    private String convertLengthToString(int time) {
        return "" + (time / 3600000 > 0 ? (time / 3600000 + ":") : "") + (time % 3600000 / 60000 < 10 ? "0" : "") + time % 3600000 / 60000 + ":" + (time % 3600000 % 60000 / 1000 < 10 ? "0" : "") + time % 3600000 % 60000 / 1000;
    }

    private void setModeButtonView() {
        switch (PlayMode.getMode(this)) {
            case PlayMode.PLAY_MODE_REPEAT:
                mPlayModeImageView.setImageResource(R.drawable.picto_repeat_one);
                break;
            case PlayMode.PLAY_MODE_RANDOM:
                mPlayModeImageView.setImageResource(R.drawable.picto_shuffle);
                break;
            default:
                mPlayModeImageView.setImageResource(R.drawable.picto_repeat_all);
                break;
        }
    }

    private void setPlayOrPauseButton() {
        mPlayMusicButton.stopLoading();
        if (MusicPlayBackService.isPlaying()) {
            mPlayMusicButton.setPlayState(true);
        } else {
            mPlayMusicButton.setPlayState(false);
        }
    }

    @Override
    protected void onStart() {
        mHandler.sendEmptyMessage(MSG_REFRESH);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unRegistListener();
        mHandler.removeMessages(MSG_REFRESH);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!MusicPlayBackService.isPlaying()) {
            mPlayMusicButton.stopLoading();
        }
        if (MusicPlayBackService.getCurrentMediaInfo() == null) {
            mEmptyRelative.setVisibility(View.VISIBLE);
            mPlayingRelative.setVisibility(View.INVISIBLE);
        } else {
            mEmptyRelative.setVisibility(View.INVISIBLE);
            mPlayingRelative.setVisibility(View.VISIBLE);
            mFavoriteImageView.setImageResource(MusicPlayBackService.getCurrentMediaInfo().Favorite ? R.drawable.favorite_image2 : R.drawable.favorite_image);
        }
        if (mFragmentList == null || mFragmentList.isEmpty()) {
            mFragmentList = new ArrayList<Fragment>();
            mQueueFragment = new QueueFragment();
            mPortraitPlayerFragment = new PortraitPlayerFragment();
            mPlayLyricFragment = new PlayLyricFragment();
            mFragmentList.add(mQueueFragment);
            mFragmentList.add(mPortraitPlayerFragment);
            mFragmentList.add(mPlayLyricFragment);
            mViewPager.setAdapter(new MyFrageStatePagerAdapter(getFragmentManager()));
            mViewPager.setCurrentItem(1);
            mLeftImageView.setImageResource(R.drawable.playing_grey);
            mCenterImageView.setImageResource(R.drawable.playing_black);
            mRightImageView.setImageResource(R.drawable.playing_grey);
            mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
        }
        setPlayOrPauseButton();
        // sometimes activity be destroyed and its context will be none;
        try {
            updateMyFavouriteButton();
        } catch (Exception e) {
            e.printStackTrace();
        }
        registListener();
    }

    private void initView() {
        mEmptyRelative = (RelativeLayout) findViewById(R.id.layout_empty);
        mPlayingRelative = (RelativeLayout) findViewById(R.id.layout_playing);
        if (MusicPlayBackService.getCurrentMediaInfo() == null) {
            mEmptyRelative.setVisibility(View.VISIBLE);
            mPlayingRelative.setVisibility(View.INVISIBLE);
        } else {
            mEmptyRelative.setVisibility(View.INVISIBLE);
            mPlayingRelative.setVisibility(View.VISIBLE);
        }
        mWindow = getWindow();
        mWindow.setStatusBarColor(getResources().getColor(R.color.statusbar_transparent));
        View layout_bottom = findViewById(R.id.layout_bottom);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mFavoriteImageView = (ImageView) layout_bottom.findViewById(R.id.iv_favorite);
        mPlayModeImageView = (ImageView) layout_bottom.findViewById(R.id.play_mode_btn);
        mPlayTimeTextView = (TextView) layout_bottom.findViewById(R.id.tv_play_time);
        mTotalTimeTextView = (TextView) layout_bottom.findViewById(R.id.tv_total_time);
        mProgressSeekBar = (SeekBar) layout_bottom.findViewById(R.id.tv_seek_progress);
        mLeftImageView = (ImageView) findViewById(R.id.icon_left);
        mCenterImageView = (ImageView) findViewById(R.id.icon_center);
        mRightImageView = (ImageView) findViewById(R.id.icon_right);
        mBackImageView = (ImageView) findViewById(R.id.back_image_btn);
        mShareImageView = (ImageView) findViewById(R.id.share_image_btn);
        mEmptyImageView = (ImageView) findViewById(R.id.back_empty_btn);
        mTopRelative = (RelativeLayout) findViewById(R.id.layout_top1);
        mProgressSeekBar.setOnSeekBarChangeListener(this);
        mPlayPreviousImageView = (ImageView) layout_bottom.findViewById(R.id.iv_play_previous);
        mPlayMusicButton = (PlayMusicButton) layout_bottom.findViewById(R.id.iv_play_pause);
        mPlayNextImageView = (ImageView) layout_bottom.findViewById(R.id.iv_play_next);
        mPlayMusicButton.setPlayBarImg(R.drawable.play_pause_btn_selector);
        mPlayMusicButton.setProgressImg(R.drawable.picto_play_loading);

        mFragmentList = new ArrayList<Fragment>();
        mQueueFragment = new QueueFragment();
        mPortraitPlayerFragment = new PortraitPlayerFragment();
        mPlayLyricFragment = new PlayLyricFragment();
        mFragmentList.add(mQueueFragment);
        mFragmentList.add(mPortraitPlayerFragment);
        mFragmentList.add(mPlayLyricFragment);
        mViewPager.setAdapter(new MyFrageStatePagerAdapter(getFragmentManager()));
        mViewPager.setCurrentItem(1);
        mLeftImageView.setImageResource(R.drawable.playing_grey);
        mCenterImageView.setImageResource(R.drawable.playing_black);
        mRightImageView.setImageResource(R.drawable.playing_grey);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());

        mFavoriteImageView.setOnClickListener(this);
        mPlayModeImageView.setOnClickListener(this);
        mPlayPreviousImageView.setOnClickListener(this);
        mPlayMusicButton.setOnClickListener(this);
        mPlayNextImageView.setOnClickListener(this);
        mBackImageView.setOnClickListener(this);
        mShareImageView.setOnClickListener(this);
        mEmptyImageView.setOnClickListener(this);
        setStatusBarState(0);
        mProgressSeekBar.setMax(1000);
        setModeButtonView();
        setPlayOrPauseButton();
    }

    public void setStatusBarState(int pageIndex) {
        LogUtil.d(TAG, "setStatusBarState  pageIndex : " + pageIndex);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ((int) getResources().getDimension(R.dimen.height_control_bar_widget)));
        layoutParams.setMargins(0, SystemUtility.getStatusBarHeight(), 0, 0);
        mTopRelative.setLayoutParams(layoutParams);
        RelativeLayout.LayoutParams emptyLayoutParams = (RelativeLayout.LayoutParams) mEmptyRelative.getLayoutParams();
        emptyLayoutParams.setMargins(0, SystemUtility.getStatusBarHeight(), 0, 0);
        mWindow.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }


    @Override
    public void onClick(View view) {
        long delta_time = System.currentTimeMillis() - mCurrentClickTime;
        mSumDeltaTime = mSumDeltaTime + delta_time;
        mLeftSumDeltaTime = mLeftSumDeltaTime + delta_time;
        mPlayTime = mPlayTime + delta_time;
        mCurrentClickTime = System.currentTimeMillis();

        switch (view.getId()) {
            case R.id.iv_favorite:
                onClickFavorite();
                break;
            case R.id.play_mode_btn:
                onClickPlayMode();
                break;
            case R.id.iv_play_previous:
                if (mLeftSumDeltaTime >= 1000) {
                    mLeftSumDeltaTime = 200;
                    onClickPlayRrevious();
                }
                break;
            case R.id.iv_play_pause:
                if (mPlayTime >= 1000) {
                    mPlayTime = 200;
                    LogUtil.i(TAG, "click pause");
                    onClickPauseOrPlay(view);
                }
                break;
            case R.id.iv_play_next:
                if (mSumDeltaTime >= 1000) {
                    mSumDeltaTime = 200;
                    LogUtil.i(TAG, "click next");
                    onClickPlayNext();
                }
                break;
            case R.id.back_image_btn:
                onBackPressed();
                break;
            case R.id.back_empty_btn:
                onBackPressed();
                break;
            case R.id.share_image_btn:
                MediaInfo info = MusicPlayBackService.getCurrentMediaInfo();
                PathUtils.shareLocalMedia(info, this);
                break;
        }
    }

    private void onClickPauseOrPlay(View view) {
        if (mService == null) {
            return;
        }
        mPlayMusicButton.startLoading();
        if (mService.isPlaying()) {
            mService.pause();
        } else {
            mService.play();
        }
    }

    private void onClickPlayNext() {
        if (mService == null) {
            return;
        }
        mService.next();
    }

    private void onClickPlayRrevious() {
        if (mService == null) {
            return;
        }
        mService.prev();
    }

    private void onClickPlayMode() {
        PlayMode.setMode(this, (PlayMode.getMode(this) + 1) % 3);
        setModeButtonView();
        int toastID;
        switch (PlayMode.getMode(this)) {
            case PlayMode.PLAY_MODE_REPEAT:
                toastID = R.string.play_mode_single_cycle;
                break;
            case PlayMode.PLAY_MODE_RANDOM:
                toastID = R.string.play_mode_random;
                break;
            default:
                toastID = R.string.play_mode_listing;
                break;
        }
        ToastUtil.showToast(this, toastID);
    }

    private void onClickFavorite() {
        if (MusicPlayBackService.getCurrentMediaInfo() == null) {
            return;
        }
        mChangeFavoriteTask = new ChangeFavoriteTask();
        mChangeFavoriteTask.execute();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (mService == null) {
            return;
        }
        if (b) {
            mIsUserChangingTrack = true;
            mPlayTimeTextView.setText(convertLengthToString(mService.duration() / 1000 * seekBar.getProgress()));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mIsUserChangingTrack = false;
        if (mService == null) {
            return;
        }
        mService.seek(seekBar.getProgress() * (mService.duration() / 1000));
    }

    class MyFrageStatePagerAdapter extends FragmentStatePagerAdapter {

        public MyFrageStatePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            super.finishUpdate(container);
            int currentItem = mViewPager.getCurrentItem();
            if (currentItem == mCurrentTab) {
                return;
            }
            mCurrentTab = mViewPager.getCurrentItem();
        }
    }

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            setStatusBarState(position);
            if (position == 0) {
                mLeftImageView.setImageResource(R.drawable.playing_black);
                mCenterImageView.setImageResource(R.drawable.playing_grey);
                mRightImageView.setImageResource(R.drawable.playing_grey);
                mWindow.setStatusBarColor(getResources().getColor(R.color.statusbar_grey));
            } else if (position == 1) {
                mLeftImageView.setImageResource(R.drawable.playing_grey);
                mCenterImageView.setImageResource(R.drawable.playing_black);
                mRightImageView.setImageResource(R.drawable.playing_grey);
                mWindow.setStatusBarColor(Color.TRANSPARENT);
                mWindow.setStatusBarColor(getResources().getColor(R.color.statusbar_transparent));
            } else {
                mLeftImageView.setImageResource(R.drawable.playing_grey);
                mCenterImageView.setImageResource(R.drawable.playing_grey);
                mRightImageView.setImageResource(R.drawable.playing_black);
                mWindow.setStatusBarColor(getResources().getColor(R.color.statusbar_grey));
            }

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    public void clickItem(MediaInfo info) {
        if (mService == null) {
            return;
        }
        try {
            mService.playByMediaInfo(info);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private ProgressDialog mProgressDialog;

    private void showProgressDialog() {
        if (null == mProgressDialog) {
            mProgressDialog = new ProgressDialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage(getString(R.string.operating));
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setCancelable(false);
        } else {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
        mProgressDialog.show();
    }

    private void showToast(int resId) {
        ToastUtil.showToast(this, resId);
    }

    private class ChangeFavoriteTask extends AsyncTask<Void, Object, Boolean> {
        private MediaInfo mMediaInfo;

        public ChangeFavoriteTask() {
            this.mMediaInfo = new MediaInfo();
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            LogUtil.d(TAG, "doInBackground and favorite is " + MusicPlayBackService.getCurrentMediaInfo().Favorite);
            this.mMediaInfo.audioId = MusicPlayBackService.getCurrentMediaInfo().audioId;
            mMediaInfo.Favorite = !MusicPlayBackService.getCurrentMediaInfo().Favorite;
            return (MediaPlaylist.saveToFavouriteOnlyIfExist(PlayingActivity.this, mMediaInfo) == 1);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mProgressDialog.dismiss();
            //it's possible happening stop playing or play next
            if (result && null != MusicPlayBackService.getCurrentMediaInfo() && mMediaInfo.audioId == MusicPlayBackService.getCurrentMediaInfo().audioId) {
                MusicPlayBackService.getCurrentMediaInfo().Favorite = !MusicPlayBackService.getCurrentMediaInfo().Favorite;
                mFavoriteImageView.setSelected(MusicPlayBackService.getCurrentMediaInfo().Favorite);
                if (MusicPlayBackService.getCurrentMediaInfo().Favorite) {
                    showToast(R.string.add_favorite);
                    mFavoriteImageView.setImageResource(R.drawable.favorite_image2);
                } else {
                    showToast(R.string.cancel_favorite);
                    mFavoriteImageView.setImageResource(R.drawable.favorite_image);
                }
                if (mQueueFragment != null) {
                    mQueueFragment.notifyFavouriteHasChanged();
                }
            } else {
                showToast(R.string.operation_failed);
            }
        }
    }

    public void updateMyFavouriteButton() {
        long currentID = MusicPlayBackService.getMediaID();
        MediaInfo info = DBUtil.getMediaInfoWithMediaId(this, currentID);
        if (info != null && MusicPlayBackService.getCurrentMediaInfo() != null) {
            MusicPlayBackService.getCurrentMediaInfo().Favorite = info.Favorite;
        }
        mFavoriteImageView.setImageResource(MusicPlayBackService.getCurrentMediaInfo() == null ? R.drawable.favorite_image : (MusicPlayBackService.getCurrentMediaInfo().Favorite ? R.drawable.favorite_image2 : R.drawable.favorite_image));
    }

    public void notityQueueFragment() {
        if (mQueueFragment != null) {
            mQueueFragment.notifyFavouriteHasChanged();
        }
    }
}
