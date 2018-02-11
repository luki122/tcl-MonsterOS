/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.gallery3d.ui;

import android.content.Context;
import android.view.ViewConfiguration;

import com.android.gallery3d.common.OverScroller;
import com.android.gallery3d.common.Utils;

public class ScrollerHelper {
    private OverScroller mScroller;
    private int mOverflingDistance;
    private boolean mOverflingEnabled;
    
    // TCL ShenQianfeng Begin on 2016.10.29
    private boolean mVerticalOnly = true;
    // TCL ShenQianfeng End on 2016.10.29
    
    public ScrollerHelper(Context context) {
        mScroller = new OverScroller(context);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mOverflingDistance = configuration.getScaledOverflingDistance();
    }
    
    // TCL ShenQianfeng Begin on 2016.10.29
    public void setVerticalOnly(boolean verticalOnly) {
        mVerticalOnly = verticalOnly;
    }
    // TCL ShenQianfeng End on 2016.10.29

    public void setOverfling(boolean enabled) {
        mOverflingEnabled = enabled;
    }

    /**
     * Call this when you want to know the new location. The position will be
     * updated and can be obtained by getPosition(). Returns true if  the
     * animation is not yet finished.
     */
    public boolean advanceAnimation(long currentTimeMillis) {
        return mScroller.computeScrollOffset();
    }

    public boolean isFinished() {
        return mScroller.isFinished();
    }

    public void forceFinished() {
        mScroller.forceFinished(true);
    }

    public int getPosition() {
        // TCL ShenQianfeng Begin on 2016.10.29
        // Original:
        // return mScroller.getCurrX();
        // Modify To:
        if(mVerticalOnly) {
            return mScroller.getCurrY();
        } else {
            return mScroller.getCurrX();
        }
        // TCL ShenQianfeng End on 2016.10.29
    }

    public float getCurrVelocity() {
        return mScroller.getCurrVelocity();
    }

    public void setPosition(int position) {
        
        // TCL ShenQianfeng Begin on 2016.10.29
        // Original:
        /*
          mScroller.startScroll(
                    position, 0,    // startX, startY
                    0, 0, 0);       // dx, dy, duration
         */
        // Modify To:
        if(mVerticalOnly) {
            mScroller.startScroll(
                    0, position,    // startX, startY
                    0, 0, 0);       // dx, dy, duration
        } else {
            mScroller.startScroll(
                    position, 0,    // startX, startY
                    0, 0, 0);       // dx, dy, duration
        }
        // TCL ShenQianfeng End on 2016.10.29

        // This forces the scroller to reach the final position.
        mScroller.abortAnimation();
    }
    
    // TCL ShenQianfeng Begin on 2016.10.29
    public void fling(int velocity, int min, int max) {
        if(mVerticalOnly) {
            int currY = getPosition();
            mScroller.fling(
                    0, currY,      // startX, startY
                    0, velocity,    // velocityX, velocityY
                    0, 0, // minX, maxX
                    min, max,          // minY, maxY
                    0, mOverflingEnabled ? mOverflingDistance : 0);
        } else {
            int currX = getPosition();
            mScroller.fling(
                    currX, 0,      // startX, startY
                    velocity, 0,   // velocityX, velocityY
                    min, max,      // minX, maxX
                    0, 0,          // minY, maxY
                    mOverflingEnabled ? mOverflingDistance : 0, 0);
        }
    }
    // TCL ShenQianfeng End on 2016.10.29
    
    /*
    public void fling(int velocity, int min, int max) {
        int currX = getPosition();
        mScroller.fling(
                currX, 0,      // startX, startY
                velocity, 0,   // velocityX, velocityY
                min, max,      // minX, maxX
                0, 0,          // minY, maxY
                mOverflingEnabled ? mOverflingDistance : 0, 0);
    }
    */

    // Returns the distance that over the scroll limit.
    public int startScroll(int distance, int min, int max) {
        // TCL ShenQianfeng Begin on 2016.10.29
        // Original:
        /*
         int currPosition = mScroller.getCurrX();
        int finalPosition = mScroller.isFinished() ? currPosition :
                mScroller.getFinalX();
        int newPosition = Utils.clamp(finalPosition + distance, min, max);
        if (newPosition != currPosition) {
            mScroller.startScroll(
                currPosition, 0,                    // startX, startY
                newPosition - currPosition, 0, 0);  // dx, dy, duration
        }
        return finalPosition + distance - newPosition;
         */
        // Modify To:
        if(mVerticalOnly) {
            int currPosition = mScroller.getCurrY();
            int finalPosition = mScroller.isFinished() ? currPosition : mScroller.getFinalY();
            int newPosition = Utils.clamp(finalPosition + distance, min, max);
            if (newPosition != currPosition) {
                mScroller.startScroll(
                    0, currPosition,                     // startX, startY
                    0, newPosition - currPosition, 0);  // dx, dy, duration
            }
            return finalPosition + distance - newPosition;
        } else {
            int currPosition = mScroller.getCurrX();
            int finalPosition = mScroller.isFinished() ? currPosition : mScroller.getFinalX();
            int newPosition = Utils.clamp(finalPosition + distance, min, max);
            if (newPosition != currPosition) {
                mScroller.startScroll(
                    currPosition, 0,                    // startX, startY
                    newPosition - currPosition, 0, 0);  // dx, dy, duration
            }
            return finalPosition + distance - newPosition;
        }
        // TCL ShenQianfeng End on 2016.10.29
        
    }
}
