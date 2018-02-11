package com.mst.smartringer;

import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

public class VolumeManager extends Handler implements GyroscopeListener.Listener {
	private static final String LOG_TAG = "VolumeManager";
	
	private int mCurrentVolume;
	private int mMaxVolume;
	private int mMinVolume;
	private AudioManager mAudioManager;
	private double mUpRate = 0.1;
	private final static int VOLUME_LENGTH = 1000;
	private int mLowerDuration;
	private int mCurrentUpLevel = 3;
	private GyroscopeListener mGyroscopeListener;

	public VolumeManager(Context context) {
		mAudioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
		mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
		mLowerDuration = (int) (2000 / (mMaxVolume - mMaxVolume * 0.3));
		mGyroscopeListener = new GyroscopeListener(context, this);
	}

	public void start() {
		mAudioManager.setStreamVolume(AudioManager.STREAM_RING,
				(int) (mMaxVolume * 0.3), 0);
		sendEmptyMessageDelayed(UP_VOLUME, VOLUME_LENGTH);
		mGyroscopeListener.enable(true); 
	}
	
	public void end() {
		removeMessages(UP_VOLUME);
		removeMessages(DOWN_VOLUME);
		mCurrentUpLevel = 3;
		mAudioManager.setStreamVolume(AudioManager.STREAM_RING, mCurrentVolume,
				0);
		mGyroscopeListener.enable(false);
	}
	

	private static final int UP_VOLUME = 0;
	private static final int DOWN_VOLUME = 1;
	@Override
	public void handleMessage(Message msg) {
		int currentVolume = getCurrentVolume();
		log(" currentVolume =" + currentVolume + " mMaxVolume = " + mMaxVolume);
		switch (msg.what) {
			case UP_VOLUME:
				log("- UP_VOLUME");
				if (currentVolume < mMaxVolume) {
					++mCurrentUpLevel;
					int nextVolume = (int) (mCurrentUpLevel * mUpRate * mMaxVolume);
					log("nextVolume = " + nextVolume);
					if (nextVolume > currentVolume) {
						mAudioManager.adjustStreamVolume(AudioManager.STREAM_RING,
								AudioManager.ADJUST_RAISE, 0);
					}
					sendEmptyMessageDelayed(UP_VOLUME, VOLUME_LENGTH);
				}
				break;
			case DOWN_VOLUME:
				log("- DOWN_VOLUME");
				if (currentVolume > mMaxVolume * 0.3) {
					mAudioManager.adjustStreamVolume(AudioManager.STREAM_RING,
							AudioManager.ADJUST_LOWER, 0);
					sendEmptyMessageDelayed(DOWN_VOLUME, mLowerDuration);
				}
				break;
			default:
				break;
		}
	}


	public void onAction() {	
        log("- onAction");
		if (getCurrentVolume ()  > mMaxVolume * 0.3) {
			mGyroscopeListener.enable(false);
			removeMessages(UP_VOLUME);
			sendEmptyMessageDelayed(DOWN_VOLUME, mLowerDuration);
		}
	}
	
	private int getCurrentVolume () {
		return mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
	}
	
    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
