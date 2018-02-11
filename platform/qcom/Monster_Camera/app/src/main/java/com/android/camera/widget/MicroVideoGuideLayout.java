package com.android.camera.widget;

import android.content.Context;
/*MODIFIED-BEGIN by wenhua.tu, 2016-04-19,BUG-1962840*/
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
/*MODIFIED-END by wenhua.tu,BUG-1962840*/
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.camera.debug.Log;
import com.android.camera.util.ApiHelper;
import com.tct.camera.R;

/**
 * Created by wenhua.tu on 11/23/15.
 */
public class MicroVideoGuideLayout extends FrameLayout {

    private static final Log.Tag TAG = new Log.Tag("MircoVideoGuideLayout");
    /*MODIFIED-BEGIN by wenhua.tu, 2016-04-19,BUG-1962840*/
    private static final String URI_SCHEME_STRING = "android.resource://";
    private static final int SURFACE_BACKGROUND_FRAME_TIME = 500;
    private static final int CLEAR_SURFACE_BACKFROUND = 1;
    private static final int CLEAR_SURFACE_BACKFROUND_DELAY = SURFACE_BACKGROUND_FRAME_TIME;
    /*MODIFIED-END by wenhua.tu,BUG-1962840*/
    private SurfaceView mSurfaceView;
    private TextView mTitle;
    private TextView mDescription;
    private CheckBox mCheckBox;
    private TextView mButton;
    private boolean mCheckBoxChecked;
    private SurfaceHolder mSurfaceHolder;
    private GuideSelectionListener mListener;
    private MediaPlayer mMediaPlayer;
    private Context mContext; //MODIFIED by wenhua.tu, 2016-04-19,BUG-1962840

    private SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.i(TAG, "Microvideo guide surface created");
            playMicroVideoGuide();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.i(TAG, "Microvideo guide surface changed");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.i(TAG, "Microvideo guide surface destoryed");
            mHandler.removeMessages(CLEAR_SURFACE_BACKFROUND); //MODIFIED by wenhua.tu, 2016-04-19,BUG-1962840
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
                releaseAudioFocus();
            }
        }
    };

    public interface GuideSelectionListener {
        void onGuideSelected(boolean selected);
    }

    public void setGuideSelectionListener(GuideSelectionListener listener) {
        mListener = listener;
    }

    public MicroVideoGuideLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        /*MODIFIED-BEGIN by wenhua.tu, 2016-04-19,BUG-1962840*/
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSurfaceView = (SurfaceView) findViewById(R.id.micro_video_guide_surfaceview);
        setSurfaceViewBackground();
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setFixedSize((int) getResources().getDimension(R.dimen.micro_guide_video_width),
                (int) getResources().getDimension(R.dimen.micro_guide_video_height));
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        /*MODIFIED-END by wenhua.tu,BUG-1962840*/
        mSurfaceHolder.addCallback(mSurfaceCallback);
        mTitle = (TextView) findViewById(R.id.micro_video_guide_title);
        mDescription = (TextView) findViewById(R.id.micro_video_guide_description);
        mDescription.setMovementMethod(new ScrollingMovementMethod());
        mCheckBox = (CheckBox) findViewById(R.id.micro_video_guide_checkbox);
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCheckBoxChecked = isChecked;
            }
        });
        mButton = (TextView) findViewById(R.id.micro_video_guide_button);
        mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(TAG, "micro video guide button clicked"); //MODIFIED by wenhua.tu, 2016-04-19,BUG-1962840
                mListener.onGuideSelected(!mCheckBoxChecked);
                mCheckBoxChecked = false;
            }
        });

        Typeface typeface;
        if (ApiHelper.HAS_ROBOTO_MEDIUM_FONT) {
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL);
        } else {
            // Load roboto_regular typeface from assets.
            typeface = Typeface.createFromAsset(getResources().getAssets(),
                    "Roboto-Regular.ttf");
        }
        mTitle.setTypeface(typeface);
        mDescription.setTypeface(typeface);
        mCheckBox.setTypeface(typeface);

        if (!ApiHelper.HAS_ROBOTO_REGULAR_FONT) {
            // Load roboto_medium typeface from assets.
            typeface = Typeface.createFromAsset(getResources().getAssets(),
                    "Roboto-Medium.ttf");
        }
        mButton.setTypeface(typeface);
    }

    /*MODIFIED-BEGIN by wenhua.tu, 2016-04-19,BUG-1962840*/
    private void setSurfaceViewBackground() {
        Uri mediaPath = Uri.parse(URI_SCHEME_STRING + mContext.getPackageName() + "/" + R.raw.microvideo_guide);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(mContext, mediaPath);
        Bitmap bitmap = retriever.getFrameAtTime(SURFACE_BACKGROUND_FRAME_TIME);
        retriever.release();
        mSurfaceView.setBackground(new BitmapDrawable(bitmap));
    }

    private void playMicroVideoGuide() {
        Log.i(TAG, "play MicroVideo guide");
        mMediaPlayer = MediaPlayer.create(getContext(), R.raw.microvideo_guide);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.setDisplay(mSurfaceHolder);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                requestAudioFocus();
                mMediaPlayer.start();
                mHandler.sendEmptyMessageDelayed(CLEAR_SURFACE_BACKFROUND, CLEAR_SURFACE_BACKFROUND_DELAY);

            }
        });

        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.d(TAG, "OnError - Error code: " + what + " Extra code: " + extra);
                mButton.performClick();
                return false;
            }
        });
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case CLEAR_SURFACE_BACKFROUND:
                    mSurfaceView.setBackground(null);
                    break;
            }
        }
    };
    /*MODIFIED-END by wenhua.tu,BUG-1962840*/

    public void changeVisibility(int visible) {
        if (visible == View.VISIBLE) {
            mSurfaceView.setZOrderOnTop(true);
        } else {
            mSurfaceView.setZOrderOnTop(false);
        }

        mSurfaceView.setVisibility(visible);
        setVisibility(visible);
    }

    public void stopPlaying() {
        Log.i(TAG, "stop playing MicroVideo guide");
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            releaseAudioFocus();
        }
    }

    public void startPlaying() {
        Log.i(TAG, "start playing MicroVideo guide");
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            requestAudioFocus();
            mMediaPlayer.seekTo(0);
            mMediaPlayer.start();
        }
    }

    /*
     * Make sure we're not audio playing in the background, ask the
     * MediaPlaybackService to pause playback.
     */
    private void requestAudioFocus() {
        AudioManager am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(mAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    }

    private void releaseAudioFocus(){
        AudioManager am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        am.abandonAudioFocus(mAudioFocusChangeListener);
    }

    private final AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                    if (mMediaPlayer != null) {
                        mMediaPlayer.start();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                    }
                    break;
            }
        }
    };

}
