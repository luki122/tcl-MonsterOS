/*Copyright (C) 2016 Tcl Corporation Limited*/
package cn.tcl.meetingassistant.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.TelephonyManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import cn.tcl.meetingassistant.log.MeetingLog;

public class PlayerService extends Service implements AudioManager.OnAudioFocusChangeListener {

    public static final int STATE_IDLE = 1;
    public static final int STATE_PLAYING = 2;
    public static final int STATE_PAUSE_PLYING = 3;
    public static final int STATE_STOPED = 4;
    public static final String KEY_FILE = "filename";
    private static final String TAG = PlayerService.class.getSimpleName();
    private int mCurrentState = STATE_IDLE;

    private MediaPlayer mPlayer;
    private PlayerBinder mBinder = new PlayerBinder();
    private String mCurrentAudioFile = "";

    //from start to pause total time
    private long mPlayTime = 0;
    //the latest pause time
    private long mStartTime = 0;
    //the played time
    private long mPlayedTime = 0;
    private AudioManager mAudioManager;
    private int mCurrentVolume = -1;
    private OnRefreshUiListener mOnRefreshUiListener;
    private BroadcastReceiver mPhoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
                MeetingLog.d(TAG, "out going a call");
                pausePlay();
                mOnRefreshUiListener.onPausePlay();
            } else {
                MeetingLog.d(TAG, "phone state change");
                TelephonyManager tManager =
                        (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
                switch (tManager.getCallState()) {
                    case TelephonyManager.CALL_STATE_RINGING:
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        pausePlay();
                        mOnRefreshUiListener.onPausePlay();
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        break;
                }
            }
        }
    };
    private Handler mRefreshUiHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(mCurrentState == STATE_IDLE){
                return;
            }
            mRefreshUiHandle.sendEmptyMessageDelayed(1, 17);
            mPlayedTime = System.currentTimeMillis() - mStartTime + mPlayTime;
            if (mOnRefreshUiListener != null) {
                mOnRefreshUiListener.onRefreshProgressUi(mPlayedTime);
                mOnRefreshUiListener.onRefreshTimeUi(mPlayedTime);
            }
        }
    };

    public PlayerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        MeetingLog.d(TAG, "player service onBind");
        if (intent != null) {
            mCurrentAudioFile = intent.getStringExtra(KEY_FILE);
            try {
                MeetingLog.d(TAG, "play audio file is " + mCurrentAudioFile);
                mPlayer.reset();
                File file = new File(mCurrentAudioFile);
                FileInputStream fis = new FileInputStream(file);
                mPlayer.setDataSource(fis.getFD());
                mPlayer.prepare();

                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
                intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
                registerReceiver(mPhoneReceiver, intentFilter);
            } catch (IOException e) {
                MeetingLog.e(TAG, "play audio error", e);
            }
        }
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MeetingLog.d(TAG, "play service create");
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopPlay();
                mOnRefreshUiListener.onCompletionPlay();
            }
        });
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return 0;
    }


    public int getCurrentState() {
        return mCurrentState;
    }

    public int getDuration() {
        return mPlayer.getDuration();
    }

    private boolean requestAudioFocus() {
        int result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        MeetingLog.d(TAG, "request audio focus result is " + result);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private boolean releaseAudioFocus() {
        mAudioManager.abandonAudioFocus(this);
        return true;
    }

    public void startPlay() {
        MeetingLog.d(TAG, "play service start");
        if (mPlayer.isPlaying()) {
            return;
        }
        if (!requestAudioFocus()) {
            MeetingLog.e(TAG, "don't request audio focus");
        }
        mPlayer.start();
        mCurrentState = STATE_PLAYING;
        mPlayTime = mPlayer.getCurrentPosition();
        mStartTime = System.currentTimeMillis();
        mRefreshUiHandle.sendEmptyMessage(1);
        MeetingLog.d(TAG, "play service start end");
    }

    public void pausePlay() {
        MeetingLog.d(TAG, "play service pause");
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            mRefreshUiHandle.removeMessages(1);
            mCurrentState = STATE_PAUSE_PLYING;
        }
    }

    public void pausePlayPiece() {
        MeetingLog.d(TAG, "play service pause");
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            mRefreshUiHandle.removeMessages(1);
        }
    }

    public void stopPlay() {
        MeetingLog.d(TAG, "play service stop");
        releaseAudioFocus();
        if(null != mPlayer && mPlayer.isPlaying()){
            mPlayer.stop();
        }
        mRefreshUiHandle.removeMessages(1);
        mCurrentState = STATE_IDLE;
//        unregisterReceiver(mPhoneReceiver);
    }

    public void setSeekTo(int millimeter) {
        MeetingLog.d(TAG, "set seek to " + millimeter);
        mPlayer.seekTo(millimeter);
        if (mCurrentState == STATE_PLAYING) {
            pausePlay();
            startPlay();
        } else if (mCurrentState == STATE_PAUSE_PLYING) {
            mOnRefreshUiListener.onRefreshTimeUi(millimeter);
            pausePlay();
        }
    }


    @Override
    public void onDestroy() {
        MeetingLog.i(TAG,"onDestroy");
        unregisterReceiver(mPhoneReceiver);
        if (mPlayer != null) {
            stopPlay();
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
            if(null != mOnRefreshUiListener){
                mOnRefreshUiListener.onDestroy();
            }
        }
        System.gc();
        super.onDestroy();
    }

    public void setOnRefreshUiListener(OnRefreshUiListener onRefreshUiListener) {
        this.mOnRefreshUiListener = onRefreshUiListener;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                MeetingLog.d(TAG, "audio focus loss");
                //stopPlay();
               // mOnRefreshUiListener.onCompletionPlay();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                MeetingLog.d(TAG, "audio focus loss TRANSIENT");
//                mOnRefreshUiListener.onPausePlay();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                MeetingLog.d(TAG, "audio focus loss TRANSIENT_CAN_DUCK");
                /*if(mCurrentVolume == -1) {
                    mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                }
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,(int)(mCurrentVolume*0.5),0);*/
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                MeetingLog.d(TAG, "audio focus GAIN");
//                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,mCurrentVolume,0);
                break;
        }
    }

    public interface OnRefreshUiListener {
        void onRefreshTimeUi(long time);

        void onRefreshProgressUi(long progress);

        void onCompletionPlay();

        void onPausePlay();

        void onDestroy();
    }

    public class PlayerBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    public long getPlayedTime(){
        return mPlayedTime;
    }

    public String getCurrentAudioFile(){
        return mCurrentAudioFile;
    }
}
