/*
 * Copyright (C) 2008 The Android Open Source Project
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


package com.monster.launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Scroller;

/**
 * Class for dragging dock view to left or right.
 * @author xiangzhenxiong
 * 2016/05/26
 */
public class DockDragController {


    private int mActivePointerId = -1;
    private VelocityTracker mVelocityTracker;
    /** X coordinate of the down event. */
    private int mStartX;
    /** Y coordinate of the down event. */
    private int mStartY;

    boolean mIsInHotSeat;

    private static final float START_SCROLL_THRESHOLD = 0.2f;
    private static final float MOVING_THRESHOLD = 0.06f;
    private static final int MIN_FLING_VELOCITY = 2000;
    private int mMaximumVelocity = 20000;
    private int mVelocityUnit = 1000;
    public static final int AUTOSCROLL_DURATION = 200;
    private int velocityX;

    private int mWindowWidth;
    private int mHalfOfSearchWidth;
    private Scroller mHotSeatScroller;
    private Scroller mSearchViewScroller;
    private int mDistance;
    private Launcher mLauncher;

    private int mTmpPoint[] = new int[2];
    private Rect mDragLayerRect = new Rect();

    protected final int NORMAL = 0, LEFT = 1,  RIGHT = 2;
    private   int  mDirection = NORMAL;
    private boolean mEnableRightScroll = false;

    public static final String TAG = "DockDragController";
    /**
     * @param launcher The application's context.
     */
    public DockDragController(Launcher launcher) {
        mLauncher = launcher;
        mVelocityTracker = VelocityTracker.obtain();
    }


    private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    /**
     * Clamps the position to the drag layer bounds.
     */
    private int[] getClampedDragLayerPos(float x, float y) {
        mLauncher.getDragLayer().getLocalVisibleRect(mDragLayerRect);
        mTmpPoint[0] = (int) Math.max(mDragLayerRect.left, Math.min(x, mDragLayerRect.right - 1));
        mTmpPoint[1] = (int) Math.max(mDragLayerRect.top, Math.min(y, mDragLayerRect.bottom - 1));
        return mTmpPoint;
    }


    public boolean responseWorkspace(){
        if(mDirection == LEFT) {
            autoScroll(RIGHT, AUTOSCROLL_DURATION);
            return true;
        }
        return false;
    }
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mHotSeatScroller = mLauncher.getHotseat().getScroller();
        mSearchViewScroller = mLauncher.getAppSearchView().getScroller();

        if(mLauncher.getWorkspace().getState() != Workspace.State.NORMAL){
            return false;
        }
        mWindowWidth = mLauncher.getHotseat().getWidth();
        mHalfOfSearchWidth = mLauncher.getAppSearchView().getWidth()/2;
        // Update the velocity tracker
        acquireVelocityTrackerAndAddMovement(ev);

        final int action = ev.getAction();
        final int[] dragLayerPos = getClampedDragLayerPos(ev.getX(), ev.getY());
        final int eventX = dragLayerPos[0];
        final int eventY = dragLayerPos[1];

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                int mEndX = eventX;
                if(mIsInHotSeat){
                    if(Math.abs(mStartX - mEndX) > mWindowWidth * MOVING_THRESHOLD) {
                        if(mStartX < mEndX && !mEnableRightScroll){
                           return false;
                        }
                        if(mStartX > mEndX){
                            mLauncher.prepareAllAppsForAnimate();
                        }
                        if(!mHotSeatScroller.isFinished()){
                            mHotSeatScroller.abortAnimation();
                        }

                        if(!mSearchViewScroller.isFinished()){
                            mSearchViewScroller.abortAnimation();
                        }
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:
                mIsInHotSeat = false;
                // Remember location of down touch
                mActivePointerId = ev.getPointerId(0);
                mStartX = eventX;
                mStartY = eventY;
                Rect hitRect = new Rect();
                mLauncher.getDragLayer().getDescendantRectRelativeToSelf(mLauncher.getHotseat(), hitRect);
                if( hitRect.contains(mStartX,mStartY) && mLauncher.getOpenFolder() == null){
                    mIsInHotSeat = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                 mIsInHotSeat = false;
                 releaseVelocityTracker();
                 break;
            case MotionEvent.ACTION_CANCEL:
                 mIsInHotSeat = false;
                 releaseVelocityTracker();
                 break;
        }
        return false;
    }


    private void autoScroll(int direction, int duration){

            int temp = 0;
            switch (mDirection){
                case NORMAL:
                    temp = 0;
                    break;

                case LEFT:
                    temp = mWindowWidth;
                    break;

                case RIGHT:
                    temp = -mWindowWidth;
                    break;
            }
                //scroll right
                if (direction == RIGHT) {
                    startScroll(temp, 0, -1 * mWindowWidth, 0 ,duration);
                    if( mDirection == LEFT ){
                        mDirection = NORMAL;
                    }else if( mDirection == NORMAL ) {
                        mDirection = RIGHT;
                    }
                }
                //scroll left
                else {

                    startScroll(temp, 0, mWindowWidth, 0 ,duration);
                    if( mDirection == RIGHT ){
                        mDirection = NORMAL;
                    }else if( mDirection == NORMAL ) {
                        mDirection = LEFT;
                        mLauncher.showAppsView(true /* animated */, false /* resetListToTop */,
                                true /* updatePredictedApps */, false /* focusSearchBar */);
                    }
                }
    }

    public void responseActionUpEvent(float finalDistance, final int duration) {
        int temp = 0;
        switch (mDirection){
            case NORMAL:
                temp = mDistance;
                break;

            case LEFT:
                temp = mWindowWidth + mDistance;
                break;

            case RIGHT:
                temp = -mWindowWidth + mDistance;
                break;
        }
        if (Math.abs(mDistance) >= mWindowWidth * START_SCROLL_THRESHOLD || velocityX > 3000) {
            //scroll right
            if (mDistance < 0) {
                startScroll(temp, 0, -1 * (int)finalDistance, 0, duration);
                if( mDirection == LEFT ){
                    mDirection = NORMAL;
                    mLauncher.showWorkspace(true);
                }else if( mDirection == NORMAL ) {
                    mDirection = RIGHT;
                }
            }
            //scroll left
            else {
                startScroll(temp, 0, (int)finalDistance ,0 ,duration);
                if( mDirection == RIGHT ){
                    mDirection = NORMAL;
                }else if( mDirection == NORMAL ) {
                    mDirection = LEFT;
                    mLauncher.getWorkspace().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mLauncher.showAppsView(false /* animated */, false /* resetListToTop */,
                                    true /* updatePredictedApps */, false /* focusSearchBar */);
                            mLauncher.enterAllAppsAnimate(duration/2, true, new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    mLauncher.getAppSearchView().setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                    }, duration/2);

                }
            }

        } else{
			/*
			  return to original position
			 */
            if (mDistance < 0) {
                startScroll(temp, 0, -1 * mDistance, 0, duration);
            } else {
                startScroll(temp, 0, -1 * mDistance ,0 ,duration);
            }

            }
    }

    private void startScroll(final int startX, int startY,final int dx, int dy, int duration){
        if(dx != -mWindowWidth) {
            float appsViewFromAlpha = mLauncher.mAppsView.getAlpha();
            float appsViewToAlpha = dx > 0 ? 1 : 0;
            ObjectAnimator appsViewAlphaAnima = ObjectAnimator.ofFloat(mLauncher.mAppsView, "alpha",
                    appsViewFromAlpha, appsViewToAlpha).setDuration(duration / 3);
            appsViewAlphaAnima.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (startX + dx == 0) {
                        mLauncher.rollBackToWorkspace();
                    }
                }
            });
            appsViewAlphaAnima.start();

            //mLauncher.getAppSearchView().setVisibility(AppSearchView.VISIBLE);
            mSearchViewScroller.startScroll(startX/2, startY, dx>0 ? dx/2+mHalfOfSearchWidth:dx/2, dy, duration);
            mLauncher.getAppSearchView().invalidate();
        }
        mHotSeatScroller.startScroll(startX, startY, dx, dy, duration*2);
        mLauncher.getHotseat().invalidate();
    }

    private void responseLeftScroll(int distance){
        mLauncher.mAppsView.setAlpha(distance*1.0f/mWindowWidth);
        mLauncher.getHotseat().scrollTo(distance , 0);
        mLauncher.getAppSearchView().setVisibility(AppSearchView.VISIBLE);
        mLauncher.getAppSearchView().scrollTo(distance/2, 0);
        mLauncher.setupTransparentSystemBarsForLollipop(true);
    }

    private void responseRightScroll(int distance){
            mLauncher.getHotseat().scrollTo(distance, 0);
            mLauncher.getAppSearchView().scrollTo(distance/2, 0);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        // Update the velocity tracker
        acquireVelocityTrackerAndAddMovement(ev);

        final int action = ev.getAction();
        final int[] dragLayerPos = getClampedDragLayerPos(ev.getX(), ev.getY());
        final int dragLayerX = dragLayerPos[0];

        switch (action) {
        case MotionEvent.ACTION_DOWN:
             break;
        case MotionEvent.ACTION_MOVE:
            if( !mIsInHotSeat ){
                return true;
            }
            int tempDistance = mStartX - dragLayerX - (int)(mWindowWidth * MOVING_THRESHOLD);
            //judge opposite-sign
            if((tempDistance^mDistance)>>>31 == 0){
                mDistance = tempDistance;
            }else{
                return true;
            }
            int temp = 0;
            switch (mDirection){
                case NORMAL:
                    temp = mDistance;
                    break;
                case LEFT:
                    temp = mWindowWidth + mDistance;
                    break;
                case RIGHT:
                    temp = -mWindowWidth + mDistance;
                    break;
            }
            if( mDistance < 0 ){
                if( mDirection != RIGHT ){
                    responseRightScroll(temp);
                }
            }
            else{
                if( mDirection != LEFT ){
                    responseLeftScroll(temp);
                }
            }
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            if(!mIsInHotSeat){
                return true;
            }
            mVelocityTracker.computeCurrentVelocity(mVelocityUnit, mMaximumVelocity);
            velocityX = (int) mVelocityTracker.getXVelocity(mActivePointerId);
            velocityX = Math.abs(velocityX);
            velocityX = Math.max(MIN_FLING_VELOCITY, velocityX);
            manualActionUp(false);
            break;
        }
        return true;
    }

    public void manualActionUp(boolean manual){
        int finalDistance = mWindowWidth - Math.abs(mDistance);
        int duration = 200;
        if(!manual) {
            duration = 2 * Math.round(1000 * finalDistance / velocityX);
            duration = duration <= 200 ? 200 : duration;
            duration = duration >= 400 ? 400 : duration;
        }
        responseActionUpEvent(finalDistance, duration);
        releaseVelocityTracker();
        mIsInHotSeat = false;
    }

}
