package cn.tcl.music.activities;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;

import cn.tcl.music.R;
import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.service.MusicPlayBackService;
import cn.tcl.music.util.PermissionsUtil;
import cn.tcl.music.util.PreferenceUtil;
import cn.tcl.music.view.DataSaverDialog;
import mst.app.MstActivity;

public abstract class BaseMusicActivity extends MstActivity {
    protected void setContentView() {
    }

    protected Activity getMainActivity() {
        return null;
    }

    public FloatingActionButton mPlayingButton;
    private AnimationDrawable mAnimationDrawable;
    private BroadcastReceiver mLocalTrackChangeListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            if (CommonConstants.META_CHANGED.equals(intent.getAction())) {
                onCurrentMusicMetaChanged();
                onCurrentMediaChanged();
            } else if (CommonConstants.PLAY_STATE_CHANGED.equals(intent.getAction())) {
                onCurrentPlayStateChanged(MusicPlayBackService.isPlaying());
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PermissionsUtil.shouldRequestPermissions(this)) {
            Intent i = new Intent(BaseMusicActivity.this, MusicWelcomeActivity.class);
            startActivity(i);
            finish();
            return;
        }
        setContentView();
        mPlayingButton = (FloatingActionButton) findViewById(R.id.fab_playing);
        mPlayingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BaseMusicActivity.this, PlayingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MusicApplication.getApp().isDataSaver()) {
            if (!MusicApplication.getApp().mIsNetworkDialogShowed) {
                MusicApplication.getApp().mIsNetworkDialogShowed = true;
                DataSaverDialog dialog = DataSaverDialog.getInstance(this);
                dialog.showWrapper();
            }
        }
        if (mAnimationDrawable != null) {
            mAnimationDrawable.start();
        }
        registListener();
        onCurrentPlayStateChanged(MusicPlayBackService.isPlaying());
    }

    public abstract void onCurrentMusicMetaChanged();

    @Override
    protected void onPause() {
        super.onPause();
        if (mAnimationDrawable != null) {
            mAnimationDrawable.stop();
        }
        unRegistListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onCurrentMediaChanged() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MusicPlayBackService.getCurrentMediaInfo() == null) {
                    mAnimationDrawable = null;
                    mPlayingButton.setImageResource(R.drawable.icon_floating_button_music);
                }
            }
        });
    }

    public void onCurrentPlayStateChanged(final boolean isPlaying) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isPlaying && mAnimationDrawable == null) {
                    mAnimationDrawable = (AnimationDrawable) getResources().getDrawable(R.anim.floating_bar_animation);
                    mPlayingButton.setImageDrawable(mAnimationDrawable);
                    mAnimationDrawable.start();
                } else if (!isPlaying) {
                    mAnimationDrawable = null;
                    mPlayingButton.setImageResource(R.drawable.icon_floating_button_music);
                }
            }
        });
    }
//
//    @Override
//    public void onCurrentMediaPositionChanged(double position) {
//
//    }
//
//    @Override
//    public void onCurrentMediaIsPreparing(MediaInfo media, int deckIndex) {
//
//    }
//
//    @Override
//    public void onCurrentMediaChanged(final MediaInfo media, int deckIndex) {
//        Log.d(TAG,"onCurrentMediaChanged and media is null = " + (null == media));
//        this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (media == null) {
//                    mAnimationDrawable = null;
//                    mPlayingButton.setImageResource(R.drawable.icon_floating_button_music);
//                }
//            }
//        });
//    }
//
//    @Override
//    public void onCurrentMediaPlayStateChanged(final boolean isPlaying) {
//        Log.d(TAG, "onCurrentMediaPlayStateChanged and isPlaying is " + isPlaying + " and mAnimationDrawable is null =" +
//                " " + (null == mAnimationDrawable));
//        this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (isPlaying && mAnimationDrawable == null) {
//                    mAnimationDrawable = (AnimationDrawable) getResources().getDrawable(R.anim.floating_bar_animation);
//                    mPlayingButton.setImageDrawable(mAnimationDrawable);
//                    mAnimationDrawable.start();
//                } else if (!isPlaying) {
//                    mAnimationDrawable = null;
//                    mPlayingButton.setImageResource(R.drawable.icon_floating_button_music);
//                }
//            }
//        });
//    }
}