package cn.tcl.music.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import cn.tcl.music.util.LogUtil;

import com.nineoldandroids.animation.ObjectAnimator;
import com.tcl.framework.notification.NotificationCenter;
import com.tcl.framework.notification.TopicSubscriber;

/**
 * Created by dongdong.huang on 2015/12/23.
 * 碟片播放界面播放button
 */
public class PlayMusicButton extends FrameLayout{
    public static final String TAG = "PlayMusicButton";
    private ImageView mPlayImg;
    private ImageView mProgressImg;
    private ObjectAnimator mProgressAnimator;
    private TopicSubscriber playMusicSubscriber = new PlayMusicSubscriber();
    public static final String CLICK_NEXT = "click_next";
    public static final String CLICK_PREVIOUS = "click_previous";
    public static final String CLICK_TO_PAUSE = "click_to_pause";
    public static final String CLICK_TO_PLAY = "click_to_play";
    public static final String CLICK_TO_PLAY_AT_NEXT = "click_to_play_at_next";
    public static final String CURRENT_MEDIA_CHANGED = "current_media_changed";

    public static final String UPDATE_PLAY_STATE = "change_button_state";

//    Uri mUri = MusicContentProvider.VipCallItem.CONTENT_URI;
    private Context mContext;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UPDATE_PLAY_STATE.equals(intent.getAction())) {
                // remove for 1955548  performance issue
                //setPlayState(true);
                //NotificationCenter.defaultCenter().publish(PlayMusicButton.TAG, PlayMusicButton.CLICK_TO_PLAY);
            }
        }
    };
    public PlayMusicButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        /*[BUGFIX]-ADD by yanjia.li, 2016-06-02,BUG-1990113 begin*/
        mContext = context;
        /*[BUGFIX]-ADD by yanjia.li, 2016-06-02,BUG-1990113 end*/
        init();
    }

    private void init(){
        mPlayImg = new ImageView(getContext());
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        mPlayImg.setLayoutParams(params);

        mProgressImg = new ImageView(getContext());
        mProgressImg.setLayoutParams(params);
        addView(mPlayImg);
        addView(mProgressImg);

        NotificationCenter.defaultCenter().subscriber(TAG, playMusicSubscriber);
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_PLAY_STATE);
        getContext().registerReceiver(mBroadcastReceiver, filter);
    }

    private void initProgressAnimation(){
        if(mProgressAnimator == null){
            View progressView = getChildAt(1);

            if(progressView != null){
                mProgressAnimator = ObjectAnimator.ofFloat(progressView, "rotation", 0f, 360f);
                mProgressAnimator.setInterpolator(new LinearInterpolator());
                mProgressAnimator.setRepeatCount(-1);
                mProgressAnimator.setDuration(2000);
            }
        }
    }

    /**
     * 设置播放按钮
     */
    public void setPlayBarImg(int resId){
        if(mPlayImg != null){
            mPlayImg.setImageResource(resId);
        }
    }

    /**
     * 设置播放loading
     */
    public void setProgressImg(int resId){
        if(mProgressImg != null){
            mProgressImg.setImageResource(resId);
        }
    }

    /**
     * 开始加载进度
     */
    public void startLoading(){
        if(mProgressAnimator == null){
            initProgressAnimation();
        }
        final ObjectAnimator progressAnimator = mProgressAnimator;
        if(progressAnimator==null){
            return;
        }
        progressAnimator.cancel();

        post(new Runnable() {
            @Override
            public void run() {
                View progressView = getChildAt(1);

                if (progressView != null) {
                    progressView.setVisibility(View.VISIBLE);
                    progressAnimator.start();
                }
            }
        });

    }

    /**
     * 停止加载进度
     */
    public void stopLoading(){
        if(mProgressAnimator != null){
            mProgressAnimator.cancel();
        }

        post(new Runnable() {
            @Override
            public void run() {
                View progressView = getChildAt(1);
                if(progressView != null){
                    progressView.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    /**
     * 设置播放状态
     */
    public void setPlayState(final boolean state){
        LogUtil.i(TAG, "--setPlayState--"+state);
        if(mPlayImg != null){
            post(new Runnable() {
                @Override
                public void run() {
                    mPlayImg.setSelected(state);
                }
            });
        }
        //[BUGFIX]-MODIFY by yanjia.li, 2016-06-18,BUG-2197064 begin
        Intent intent = new Intent("cn.tcl.music.sendstate");
        intent.putExtra("state", state?1:0);
        mContext.sendOrderedBroadcast(intent, null);
        //[BUGFIX]-MODIFY by yanjia.li, 2016-06-18,BUG-2197064 end
    }

    public void playOrPause(Activity activity){
//        if(mPlayImg != null && activity != null){
//            if(mPlayImg.isSelected()){
//                MixSession.getInstance().autoMixEngine().stop(true);
//                setPlayState(false);
//                NotificationCenter.defaultCenter().publish(PlayMusicButton.TAG, PlayMusicButton.CLICK_TO_PAUSE);
//            }
//            else{
//                if(MediaQueue.mCurrentMedia.getCurrentMedia()!=null){
//                    /*[BUGFIX]-MODIFIED by yanjia.li, 2016-06-02,BUG-1990113 begin*/
//                    AuditionAlertDialog.getInstance(activity).showWrapper(MediaQueue.mCurrentMedia.getCurrentMedia().isLocal(), new AuditionAlertDialog.OnSelectedListener() {
//                        @Override
//                        public void onPlay() {
//                            MixSession.getInstance().autoMixEngine().start();
//                            setPlayState(true);
//                            NotificationCenter.defaultCenter().publish(PlayMusicButton.TAG, PlayMusicButton.CLICK_TO_PLAY); // MODIFIED by yanjia.li, 2016-06-20,BUG-2197064
//                        }
//                    });
//                   /*[BUGFIX]-MODIFIED by yanjia.li, 2016-06-02,BUG-1990113 begin*/
//                }
//
//            }
//        }
//        stopLoading();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopLoading();
        mProgressAnimator = null;

        if(playMusicSubscriber != null){
            NotificationCenter.defaultCenter().unsubscribe(TAG, playMusicSubscriber);
        }

        getContext().unregisterReceiver(mBroadcastReceiver);

        super.onDetachedFromWindow();
    }

    class PlayMusicSubscriber implements TopicSubscriber {

        @Override
        public void onEvent(String s, Object o) {

            if(!TAG.equals(s)){
                return;
            }

            String tag = String.valueOf(o);

            //下一首
            if(CLICK_NEXT.equals(tag)){
                startLoading();
            }
            //上一首
            else if(CLICK_PREVIOUS.equals(tag)){
                startLoading();
            }
            else if(CURRENT_MEDIA_CHANGED.equals(tag)){
                stopLoading();
            }
            //点击播放
            else if(CLICK_TO_PLAY.equals(tag)){
                stopLoading();
                setPlayState(true);
            }
            //点击暂停
            else if(CLICK_TO_PAUSE.equals(tag)){
                stopLoading();
                setPlayState(false);
            }
            //选择菜单下一首播放
            else if(CLICK_TO_PLAY_AT_NEXT.equals(tag)){
//                try {
//                    if(MediaQueue.getInstance().getCount() > 1){
//                        /*MODIFIED-BEGIN by lei.liu2, 2016-04-13,BUG-1850608*/
//                        if(!MixSession.isAnySamplerOrMusicPlaying()) {
//                            setPlayState(false);
//                        }else {
//                            setPlayState(true);
//                        }
//                        /*MODIFIED-END by lei.liu2,BUG-1850608*/
//                    } else{
//                        //setPlayState(false);
//                        if(!MixSession.isAnySamplerOrMusicPlaying()) {
//                            setPlayState(false);
//                        }else {
//                            setPlayState(true);
//                        }
//                    }
//                } catch (Exception e) {
//
//                }
            }
        }
    }
}
