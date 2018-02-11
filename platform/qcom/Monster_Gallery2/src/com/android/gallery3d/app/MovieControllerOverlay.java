/*
 * Copyright (C) 2011 The Android Open Source Project
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

import mst.app.MstActivity;
import mst.widget.toolbar.Toolbar;
import android.content.Context;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.android.gallery3d.R;
import com.android.gallery3d.common.ApiHelper;

/**
 * The playback controller for the Movie Player.
 */
public class MovieControllerOverlay extends CommonControllerOverlay implements
        AnimationListener {

    private boolean hidden;

    private final Handler handler;
    private final Runnable startHidingRunnable;
    private final Animation hideAnimation;


    public MovieControllerOverlay(Context context) {
        super(context);

        handler = new Handler();
        startHidingRunnable = new Runnable() {
                @Override
            public void run() {
                startHiding();
            }
        };

        hideAnimation = AnimationUtils.loadAnimation(context, R.anim.player_out);
        hideAnimation.setAnimationListener(this);
        createToolbar(context);
        // TCL BaiYuan Begin on 2016.10.25
        hide();
        // TCL BaiYuan End on 2016.10.25
    }

    @Override
    protected void createTimeBar(Context context) {
        //mTimeBar = new TimeBar(context, this);
    }
    
    // TCL ShenQianfeng Begin on 2016.08.05
    @Override
    protected void createBottomBar(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        mBottomControlBar = inflater.inflate(R.layout.mst_movie_controller_bottom_bar, null);
        mTimeBar = (MovieTimeBar)mBottomControlBar.findViewById(R.id.mst_movie_time_bar);
        mPlayPauseReplayView = (ImageView)mBottomControlBar.findViewById(R.id.mst_movie_play_pause_button);
        mTimeBar.setScrubbingListener(this);
        mPlayPauseReplayView.setOnClickListener(this);
    }
    // TCL ShenQianfeng End on 2016.08.05

    // TCL BaiYuan Begin on 2016.10.25
    @Override
    protected void createToolbar(Context context){
        if (context instanceof MstActivity) {
            mToolbar = ((MstActivity)context).getToolbar();
        }
    }
    // TCL BaiYuan End on 2016.10.25
    
    @Override
    public void hide() {
        boolean wasHidden = hidden;
        hidden = true;
        super.hide();
        if (mListener != null && wasHidden != hidden) {
            mListener.onHidden();
        }
    }


    @Override
    public void show() {
        boolean wasHidden = hidden;
        hidden = false;
        super.show();
        if (mListener != null && wasHidden != hidden) {
            mListener.onShown();
        }
        maybeStartHiding();
    }

    private void maybeStartHiding() {
        cancelHiding();
        if (mState == State.PLAYING) {
            handler.postDelayed(startHidingRunnable, 2500);
        }
    }

    private void startHiding() {
        startHideAnimation(mBackground);
        
        // TCL ShenQianfeng Begin on 2016.08.05
        // Original:
        /*
         startHideAnimation(mTimeBar);
         startHideAnimation(mPlayPauseReplayView);
         */
        // Modify To:
        if(mBottomControlBar != null) {
            startHideAnimation(mBottomControlBar);
        } else {
            startHideAnimation(mTimeBar);
            startHideAnimation(mPlayPauseReplayView);
        }
        // TCL ShenQianfeng End on 2016.08.05
        // TCL BaiYuan Begin on 2016.10.25
        if (null != mToolbar) {
            startHideAnimation(mToolbar);
        }
        // TCL BaiYuan End on 2016.10.25
    }

    private void startHideAnimation(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            view.startAnimation(hideAnimation);
        }
    }

    private void cancelHiding() {
        handler.removeCallbacks(startHidingRunnable);
        mBackground.setAnimation(null);
        // TCL ShenQianfeng Begin on 2016.08.05
        // Original:
        /*
        mTimeBar.setAnimation(null);
        mPlayPauseReplayView.setAnimation(null);
        */
        // Modify To:
        if(mBottomControlBar != null) {
            mBottomControlBar.setAnimation(null);
        } else {
            mTimeBar.setAnimation(null);
            mPlayPauseReplayView.setAnimation(null);
        }
        // TCL ShenQianfeng End on 2016.08.05
        // TCL BaiYuan Begin on 2016.10.25
        if (null != mToolbar) {
            mToolbar.setAnimation(null);
        }
        // TCL BaiYuan End on 2016.10.25
    }

    @Override
    public void onAnimationStart(Animation animation) {
        // Do nothing.
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        // Do nothing.
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        hide();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (hidden) {
            show();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TCL ShenQianfeng Begin on 2016.08.05
        // Original:
        /*
                 if (super.onTouchEvent(event)) {
            return true;
        }
        if (hidden) {
            show();
            return true;
        }
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            cancelHiding();
            if (mState == State.PLAYING || mState == State.PAUSED) {
                mListener.onPlayPause();
            }
            break;
        case MotionEvent.ACTION_UP:
            maybeStartHiding();
            break;
        }
        return true;
         */
        // Modify To:
        if (super.onTouchEvent(event)) {
            return true;
        }
        
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            if (hidden) {
                show();
                return true;
            } else {
                cancelHiding();
                startHiding();
                return true;
            }
        case MotionEvent.ACTION_UP:
            //maybeStartHiding();
            break;
        }
        return true;
        // TCL ShenQianfeng End on 2016.08.05
    }

    @Override
    protected void updateViews() {
        if (hidden) {
            return;
        }
        super.updateViews();
    }

    // TimeBar listener

    @Override
    public void onScrubbingStart() {
        cancelHiding();
        super.onScrubbingStart();
    }

    @Override
    public void onScrubbingMove(int time) {
        cancelHiding();
        super.onScrubbingMove(time);
    }

    @Override
    public void onScrubbingEnd(int time, int trimStartTime, int trimEndTime) {
        maybeStartHiding();
        super.onScrubbingEnd(time, trimStartTime, trimEndTime);
    }
}
