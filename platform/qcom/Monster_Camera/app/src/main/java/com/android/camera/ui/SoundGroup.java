package com.android.camera.ui;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.android.camera.CameraActivity;
import com.android.camera.debug.Log;
import com.android.camera.widget.SoundAction;
import com.tct.camera.R;

/**
 * Created by wenhua.tu on 11/4/15.
 */
public class SoundGroup extends FrameLayout implements View.OnClickListener {

    private final static Log.Tag TAG = new Log.Tag("SoundGroup");
    private SoundAction mSoundLayout;
    private RotateImageView mKidCat;
    private RotateImageView mKidHarp;
    private RotateImageView mKidLaser;
    private RotateImageView mKidSheep;
    private RotateImageView mKidTrain;
    private CameraActivity mContext;
    private MediaPlayer player = null;
    private View mCurrentSoundView;

    public SoundGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = (CameraActivity) context;
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        mSoundLayout = (SoundAction) findViewById(R.id.sound_action);

        mKidCat = (RotateImageView) findViewById(R.id.kid_cat);
        mKidCat.setOnClickListener(this);

        mKidHarp = (RotateImageView) findViewById(R.id.kid_harp);
        mKidHarp.setOnClickListener(this);

        mKidLaser = (RotateImageView) findViewById(R.id.kid_laser);
        mKidLaser.setOnClickListener(this);

        mKidSheep = (RotateImageView) findViewById(R.id.kid_sheep);
        mKidSheep.setOnClickListener(this);

        mKidTrain = (RotateImageView) findViewById(R.id.kid_train);
        mKidTrain.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.kid_cat:
                playKidSound(v, R.raw.cat);
                break;
            case R.id.kid_harp:
                playKidSound(v, R.raw.harp);
                break;
            case R.id.kid_laser:
                playKidSound(v, R.raw.laser);
                break;
            case R.id.kid_sheep:
                playKidSound(v, R.raw.sheep);
                break;
            case R.id.kid_train:
                playKidSound(v, R.raw.train);
                break;
            default:
        }
    }

    private void playKidSound(View v, int resId) {
        if (mCurrentSoundView != null && mCurrentSoundView.isSelected()) {
            Log.w(TAG, "stop play current sound");
            toggleViewPlaying(resId, true);
            if (mCurrentSoundView.equals(v)) {
                mCurrentSoundView = null;
                return;
            }
        }
        Log.w(TAG, "click new sound");
        mCurrentSoundView = v;
        toggleViewPlaying(resId, false);
    }

    private void toggleViewPlaying(int resId, boolean isStop) {
        if (mCurrentSoundView == null) {
            return;
        }
        if (isStop) {
            Log.w(TAG, "tap again, stop playing");
            mCurrentSoundView.setSelected(false);
            if (player != null && player.isPlaying()) {
                try {
                    player.stop();
                    player.release();
                } catch (Exception e) {
                    Log.e(TAG, "stop or release player failed", e);
                } finally {
                    player = null;
                }
            }
        } else {
            Log.w(TAG, "start playing");
            mCurrentSoundView.setSelected(true);
            player = MediaPlayer.create(mContext, resId);
            player.setLooping(true);
            player.start();
        }
    }

    public void hideKidSound() {
        mSoundLayout.collapse();
    }

    public void finishKidSound() {
        hideKidSound();
        toggleViewPlaying(0, true);
    }

    public void addRotatableToListenerPool() {
        int childCount = mSoundLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            RotateImageView child = (RotateImageView) mSoundLayout.getChildAt(i);
            mContext.addRotatableToListenerPool(new Rotatable.RotateEntity(child, true));
        }
    }

    public void removeRotatableToListenerPool() {
        int childCount = mSoundLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            RotateImageView child = (RotateImageView) mSoundLayout.getChildAt(i);
            mContext.removeRotatableFromListenerPool(child.hashCode());
        }
    }

    public boolean isSoundPlaying() {
        return player != null && player.isPlaying();
    }
}