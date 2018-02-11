/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.app;

import mst.widget.toolbar.Toolbar;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.gallery3d.R;

/**
 * The common playback controller for the Movie Player or Video Trimming.
 */
public abstract class CommonControllerOverlay extends FrameLayout implements
        ControllerOverlay,
        OnClickListener,
        TimeBar.Listener {

    protected enum State {
        PLAYING,
        PAUSED,
        ENDED,
        ERROR,
        LOADING
    }

    private static final float ERROR_MESSAGE_RELATIVE_PADDING = 1.0f / 6;

    protected Listener mListener;

    protected final View mBackground;
    protected TimeBar mTimeBar;

    protected View mMainView;
    protected final LinearLayout mLoadingView;
    protected final TextView mErrorView;
    protected ImageView mPlayPauseReplayView;
    
    // TCL ShenQianfeng Begin on 2016.08.05
    //mBottomControlBar includes play/pause and time bar;
    protected View mBottomControlBar;
    private int mBottomControlHeight; 
    // TCL ShenQianfeng End on 2016.08.05

    // TCL BaiYuan Begin on 2016.10.25
    protected Toolbar mToolbar;
    // TCL BaiYuan End on 2016.10.25
    
    protected State mState;

    protected boolean mCanReplay = true;

    public void setSeekable(boolean canSeek) {
        mTimeBar.setSeekable(canSeek);
    }

    public CommonControllerOverlay(Context context) {
        super(context);

        mState = State.LOADING;
        // TODO: Move the following layout code into xml file.
        LayoutParams wrapContent =
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LayoutParams matchParent =
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        mBackground = new View(context);
        mBackground.setBackgroundColor(context.getResources().getColor(R.color.darker_transparent));
        addView(mBackground, matchParent);

        // Depending on the usage, the timeBar can show a single scrubber, or
        // multiple ones for trimming.
        createTimeBar(context);
        
        // TCL ShenQianfeng Begin on 2016.08.05
        mBottomControlHeight = context.getResources().getDimensionPixelSize(R.dimen.mst_movie_bottom_bar_height);
        createBottomBar(context);
        // TCL BaiYuan Begin on 2016.10.25
        createToolbar(context);
        // TCL BaiYuan End on 2016.10.25
        if(null != mBottomControlBar) {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 
                    mBottomControlHeight, Gravity.BOTTOM);
            mBottomControlBar.setLayoutParams(lp);
            addView(mBottomControlBar);
        }
        // TCL ShenQianfeng End on 2016.08.05
        
        // TCL ShenQianfeng Begin on 2016.08.05
        // Annotated Below:
        // addView(mTimeBar, wrapContent);
        // TCL ShenQianfeng End on 2016.08.05
        
        mTimeBar.setContentDescription(
                context.getResources().getString(R.string.accessibility_time_bar));
        mLoadingView = new LinearLayout(context);
        mLoadingView.setOrientation(LinearLayout.VERTICAL);
        mLoadingView.setGravity(Gravity.CENTER_HORIZONTAL);
        ProgressBar spinner = new ProgressBar(context);
        spinner.setIndeterminate(true);
        mLoadingView.addView(spinner, wrapContent);
        TextView loadingText = createOverlayTextView(context);
        loadingText.setText(R.string.loading_video);
        mLoadingView.addView(loadingText, wrapContent);
        addView(mLoadingView, wrapContent);
        
        // TCL ShenQianfeng Begin on 2016.08.05
        // Annotated Below:
        /*
        mPlayPauseReplayView = new ImageView(context);
        mPlayPauseReplayView.setImageResource(R.drawable.ic_vidcontrol_play);
        mPlayPauseReplayView.setContentDescription(
                context.getResources().getString(R.string.accessibility_play_video));
        mPlayPauseReplayView.setBackgroundResource(R.drawable.bg_vidcontrol);
        mPlayPauseReplayView.setScaleType(ScaleType.CENTER);
        mPlayPauseReplayView.setFocusable(true);
        mPlayPauseReplayView.setClickable(true);
        mPlayPauseReplayView.setOnClickListener(this);
        addView(mPlayPauseReplayView, wrapContent);
        */
        // TCL ShenQianfeng End on 2016.08.05
        
        mErrorView = createOverlayTextView(context);
        addView(mErrorView, matchParent);

        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        setLayoutParams(params);
        hide();
    }

    abstract protected void createTimeBar(Context context);
    
    // TCL ShenQianfeng Begin on 2016.08.05
    abstract protected void createBottomBar(Context context);
    // TCL ShenQianfeng End on 2016.08.05
    
    // TCL BaiYUan Begin on 2016.10.25
    abstract protected void createToolbar(Context context);
    // TCL BaiYuan End on 2016.10.25

    private TextView createOverlayTextView(Context context) {
        TextView view = new TextView(context);
        view.setGravity(Gravity.CENTER);
        view.setTextColor(0xFFFFFFFF);
        view.setPadding(0, 15, 0, 15);
        return view;
    }

    @Override
    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    @Override
    public void setCanReplay(boolean canReplay) {
        this.mCanReplay = canReplay;
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void showPlaying() {
        mState = State.PLAYING;
        // TCL ShenQianfeng Begin on 2016.08.05
        // Original:
        //showMainView(mPlayPauseReplayView);
        // Modify to:
        updatePlayPauseIcon();
        // TCL ShenQianfeng End on 2016.08.05
        
    }

    @Override
    public void showPaused() {
        mState = State.PAUSED;
        // TCL ShenQianfeng Begin on 2016.08.05
        // Original:
        //showMainView(mPlayPauseReplayView);
        // Modify to:
        updatePlayPauseIcon();
        // TCL ShenQianfeng End on 2016.08.05
    }

    @Override
    public void showEnded() {
        mState = State.ENDED;
        // TCL ShenQianfeng Begin on 2016.08.05
        // Original:
        //if (mCanReplay) showMainView(mPlayPauseReplayView);
        // Modify to:
        updatePlayPauseIcon();
        // TCL ShenQianfeng End on 2016.08.05
        
    }

    @Override
    public void showLoading() {
        mState = State.LOADING;
        showMainView(mLoadingView);
    }

    @Override
    public void showErrorMessage(String message) {
        mState = State.ERROR;
        int padding = (int) (getMeasuredWidth() * ERROR_MESSAGE_RELATIVE_PADDING);
        mErrorView.setPadding(
                padding, mErrorView.getPaddingTop(), padding, mErrorView.getPaddingBottom());
        mErrorView.setText(message);
        showMainView(mErrorView);
    }

    @Override
    public void setTimes(int currentTime, int totalTime,
            int trimStartTime, int trimEndTime) {
        mTimeBar.setTime(currentTime, totalTime, trimStartTime, trimEndTime);
    }

    public void hide() {

        mLoadingView.setVisibility(View.INVISIBLE);
        mBackground.setVisibility(View.INVISIBLE);

        // TCL ShenQianfeng Begin on 2016.08.05
        // Original:
        //mPlayPauseReplayView.setVisibility(View.INVISIBLE);
        //mTimeBar.setVisibility(View.INVISIBLE);
        // Modify To:
        if(mBottomControlBar != null) {
            mBottomControlBar.setVisibility(View.INVISIBLE);
        } else {
            mPlayPauseReplayView.setVisibility(View.INVISIBLE);
            mTimeBar.setVisibility(View.INVISIBLE);
        }
        // TCL ShenQianfeng End on 2016.08.05
        // TCL BaiYuan Begin on 2016.10.25
        if(null != mToolbar){
            mToolbar.setVisibility(Toolbar.INVISIBLE);
        }
        // TCL BaiYuan End on 2016.10.25
        setVisibility(View.INVISIBLE);
        setFocusable(true);
        requestFocus();
    }
    
    // TCL ShenQianfeng Begin on 2016.08.05
    private void updatePlayPauseIcon() {
        show();
    }
    // TCL ShenQianfeng End on 2016.08.05

    private void showMainView(View view) {
        mMainView = view;
        mErrorView.setVisibility(mMainView == mErrorView ? View.VISIBLE : View.INVISIBLE);
        mLoadingView.setVisibility(mMainView == mLoadingView ? View.VISIBLE : View.INVISIBLE);
        // TCL ShenQianfeng Begin on 2016.08.05
        // Annotated Below:
        /*
        mPlayPauseReplayView.setVisibility(
                mMainView == mPlayPauseReplayView ? View.VISIBLE : View.INVISIBLE);
        */
        // Modify To:
        if(mBottomControlBar != null) {
            
        } else {
            mPlayPauseReplayView.setVisibility(
                    mMainView == mPlayPauseReplayView ? View.VISIBLE : View.INVISIBLE);
        }
        // TCL ShenQianfeng End on 2016.08.05
        show();
    }

    @Override
    public void show() {
        updateViews();
        setVisibility(View.VISIBLE);
        setFocusable(false);
    }

    @Override
    public void onClick(View view) {
        if (mListener != null) {
            if (view == mPlayPauseReplayView) {
                if (mState == State.ENDED) {
                    if (mCanReplay) {
                        mListener.onReplay();
                    }
                } else if (mState == State.PAUSED || mState == State.PLAYING) {
                    mListener.onPlayPause();
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (super.onTouchEvent(event)) {
            return true;
        }
        return false;
    }

    // The paddings of 4 sides which covered by system components. E.g.
    // +-----------------+\
    // | Action Bar | insets.top
    // +-----------------+/
    // | |
    // | Content Area | insets.right = insets.left = 0
    // | |
    // +-----------------+\
    // | Navigation Bar | insets.bottom
    // +-----------------+/
    // Please see View.fitSystemWindows() for more details.
    private final Rect mWindowInsets = new Rect();

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        // We don't set the paddings of this View, otherwise,
        // the content will get cropped outside window
        mWindowInsets.set(insets);
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Rect insets = mWindowInsets;
        int pl = insets.left; // the left paddings
        int pr = insets.right;
        int pt = insets.top;
        int pb = insets.bottom;

        int h = bottom - top;
        int w = right - left;
        boolean error = mErrorView.getVisibility() == View.VISIBLE;
        
        // TCL BaiYuan Begin on 2016.10.25
        // Original:
        /*
        int y = h - pb;
         */
        // Modify To:
        int y = h;
        // TCL BaiYuan End on 2016.10.25
        
        // TCL ShenQianfeng Begin on 2016.08.05
        // Original:
        /*
        // Put both TimeBar and Background just above the bottom system
        // component.
        // But extend the background to the width of the screen, since we don't
        // care if it will be covered by a system component and it looks better.
        mBackground.layout(0, y - mTimeBar.getBarHeight(), w, y);
        mTimeBar.layout(pl, y - mTimeBar.getPreferredHeight(), w - pr, y);

        // Put the play/pause/next/ previous button in the center of the screen
        layoutCenteredView(mPlayPauseReplayView, 0, 0, w, h);
        */
        // Modify To:
        
        if(mBottomControlBar != null) {
            int bottomControlbarHeight = mBottomControlHeight;
            mBackground.layout(0, y - bottomControlbarHeight, w, y);
            // TCL BaiYuan Begin on 2016.10.28
            // Original:
            /*
            mBottomControlBar.layout(pl, y - bottomControlbarHeight, w - pr, y);
            */
            // Modify To:
            mBottomControlBar.layout(pl, y - bottomControlbarHeight, w, y);
            // TCL BaiYuan End on 2016.10.28
        } else {
            mBackground.layout(0, y - mTimeBar.getBarHeight(), w, y);
            // TCL BaiYuan Begin on 2016.10.28
            // Original:
            /*
            mTimeBar.layout(pl, y - mTimeBar.getPreferredHeight(), w - pr, y);
            */
            // Modify To:
            mTimeBar.layout(pl, y - mTimeBar.getPreferredHeight(), w, y);
            // TCL BaiYuan End on 2016.10.28
            // Put the play/pause/next/ previous button in the center of the screen
            layoutCenteredView(mPlayPauseReplayView, 0, 0, w, h);
        }
        // TCL ShenQianfeng End on 2016.08.05
        
        

        if (mMainView != null) {
            layoutCenteredView(mMainView, 0, 0, w, h);
        }
    }

    private void layoutCenteredView(View view, int l, int t, int r, int b) {
        int cw = view.getMeasuredWidth();
        int ch = view.getMeasuredHeight();
        int cl = (r - l - cw) / 2;
        int ct = (b - t - ch) / 2;
        view.layout(cl, ct, cl + cw, ct + ch);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    protected void updateViews() {
        mBackground.setVisibility(View.VISIBLE);
        // TCL ShenQianfeng Begin on 2016.08.05
        // Original:
        /*
        mTimeBar.setVisibility(View.VISIBLE);
        Resources resources = getContext().getResources();
        int imageResource = R.drawable.ic_vidcontrol_reload;
        String contentDescription = resources.getString(R.string.accessibility_reload_video);
        if (mState == State.PAUSED) {
            imageResource = R.drawable.ic_vidcontrol_play;
            contentDescription = resources.getString(R.string.accessibility_play_video);
        } else if (mState == State.PLAYING) {
            imageResource = R.drawable.ic_vidcontrol_pause;
            contentDescription = resources.getString(R.string.accessibility_pause_video);
        }
        
        mPlayPauseReplayView.setImageResource(imageResource);
        mPlayPauseReplayView.setContentDescription(contentDescription);
        mPlayPauseReplayView.setVisibility(
                (mState != State.LOADING && mState != State.ERROR &&
                !(mState == State.ENDED && !mCanReplay))
                ? View.VISIBLE : View.GONE);
        */
        // Modify To:
        if(mBottomControlBar != null) {
            mBottomControlBar.setVisibility(View.VISIBLE);
            Resources resources = getContext().getResources();
            // TCL BaiYuan Begin on 2016.10.18
            // Original:
            /*
            int imageResource = R.drawable.mst_movie_play;
            String contentDescription = resources.getString(R.string.accessibility_reload_video);
            if (mState == State.PAUSED) {
                imageResource = R.drawable.mst_movie_play;
                contentDescription = resources.getString(R.string.accessibility_play_video);
            } else if (mState == State.PLAYING) {
                imageResource = R.drawable.mst_movie_pause;
                contentDescription = resources.getString(R.string.accessibility_pause_video);
            }
            */
            // Modify To:
            int imageResource = R.drawable.mst_video_play;
            String contentDescription = resources.getString(R.string.accessibility_reload_video);
            if (mState == State.PAUSED) {
                imageResource = R.drawable.mst_video_play;
                contentDescription = resources.getString(R.string.accessibility_play_video);
            } else if (mState == State.PLAYING) {
                imageResource = R.drawable.mst_video_pause;
                contentDescription = resources.getString(R.string.accessibility_pause_video);
            }
            // TCL BaiYuan End on 2016.10.18
            mPlayPauseReplayView.setImageResource(imageResource);
            mPlayPauseReplayView.setContentDescription(contentDescription);
            mPlayPauseReplayView.setVisibility(
                    (mState != State.LOADING && mState != State.ERROR &&
                    !(mState == State.ENDED && !mCanReplay))
                    ? View.VISIBLE : View.GONE);
        } else {
            mTimeBar.setVisibility(View.VISIBLE);
            Resources resources = getContext().getResources();
            int imageResource = R.drawable.ic_vidcontrol_reload;
            String contentDescription = resources.getString(R.string.accessibility_reload_video);
            if (mState == State.PAUSED) {
                imageResource = R.drawable.ic_vidcontrol_play;
                contentDescription = resources.getString(R.string.accessibility_play_video);
            } else if (mState == State.PLAYING) {
                imageResource = R.drawable.ic_vidcontrol_pause;
                contentDescription = resources.getString(R.string.accessibility_pause_video);
            }
            
            mPlayPauseReplayView.setImageResource(imageResource);
            mPlayPauseReplayView.setContentDescription(contentDescription);
            mPlayPauseReplayView.setVisibility(
                    (mState != State.LOADING && mState != State.ERROR &&
                    !(mState == State.ENDED && !mCanReplay))
                    ? View.VISIBLE : View.GONE);
        }
        // TCL ShenQianfeng End on 2016.08.05
        // TCL BaiYuan Begin on 2016.10.25
        if(null != mToolbar){
            mToolbar.setVisibility(Toolbar.VISIBLE);
        }
        // TCL BaiYuan End on 2016.10.25
        requestLayout();
    }

    // TimeBar listener

    @Override
    public void onScrubbingStart() {
        mListener.onSeekStart();
    }

    @Override
    public void onScrubbingMove(int time) {
        mListener.onSeekMove(time);
    }

    @Override
    public void onScrubbingEnd(int time, int trimStartTime, int trimEndTime) {
        mListener.onSeekEnd(time, trimStartTime, trimEndTime);
    }
}
