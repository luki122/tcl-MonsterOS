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

package com.monster.launcher;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;

import java.util.ArrayList;

public class PageIndicatorCircle extends PageIndicator {
    @SuppressWarnings("unused")
    private static final String TAG = "PageIndicator";

    private int currentIndex,lastIndex;
    private Launcher mLauncher;
    public PageIndicatorCircle(Context context) {
        this(context, null);
    }

    public PageIndicatorCircle(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageIndicatorCircle(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.PageIndicator, defStyle, 0);
        mMaxWindowSize = a.getInteger(R.styleable.PageIndicator_windowSize, 15);
        mWindowRange[0] = 0;
        mWindowRange[1] = 0;
        mLayoutInflater = LayoutInflater.from(context);
        a.recycle();

        // Set the layout transition properties
        LayoutTransition transition = getLayoutTransition();
        transition.setDuration(175);

        if(context instanceof Launcher){
            mLauncher = (Launcher) context;
        }
    }

    protected void enableLayoutTransitions() {
        LayoutTransition transition = getLayoutTransition();
        transition.enableTransitionType(LayoutTransition.APPEARING);
        transition.enableTransitionType(LayoutTransition.DISAPPEARING);
        transition.enableTransitionType(LayoutTransition.CHANGE_APPEARING);
        transition.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
    }

    protected void disableLayoutTransitions() {
        LayoutTransition transition = getLayoutTransition();
        transition.disableTransitionType(LayoutTransition.APPEARING);
        transition.disableTransitionType(LayoutTransition.DISAPPEARING);
        transition.disableTransitionType(LayoutTransition.CHANGE_APPEARING);
        transition.disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
    }

    protected void offsetWindowCenterTo(int activeIndex, boolean allowAnimations) {
        if (mMarkers != null && mMarkers.size() > mMaxWindowSize) {
            allowAnimations = false;
        }
        if (activeIndex < 0) {
            new Throwable().printStackTrace();
        }
        int windowSize = Math.min(mMarkers.size(), mMaxWindowSize);
        int hWindowSize = (int) windowSize / 2;
        float hfWindowSize = windowSize / 2f;
        int windowStart = Math.max(0, activeIndex - hWindowSize);
        int windowEnd = Math.min(mMarkers.size(), windowStart + mMaxWindowSize);
        windowStart = windowEnd - Math.min(mMarkers.size(), windowSize);
        int windowMid = windowStart + (windowEnd - windowStart) / 2;
        boolean windowAtStart = (windowStart == 0);
        boolean windowAtEnd = (windowEnd == mMarkers.size());
        boolean windowMoved = (mWindowRange[0] != windowStart) ||
                (mWindowRange[1] != windowEnd);

        if (!allowAnimations) {
            disableLayoutTransitions();
        }

        // Remove all the previous children that are no longer in the window
        for (int i = getChildCount() - 1; i >= 0; --i) {
            PageIndicatorMarker marker = (PageIndicatorMarker) getChildAt(i);
            int markerIndex = mMarkers.indexOf(marker);
            if (markerIndex < windowStart || markerIndex >= windowEnd) {
                removeView(marker);
            }
        }

        // Add all the new children that belong in the window
        for (int i = 0; i < mMarkers.size(); ++i) {
            PageIndicatorMarker marker = mMarkers.get(i);
            if (windowStart <= i && i < windowEnd) {
                if (indexOfChild(marker) < 0) {
                    addView(marker, i - windowStart);
                }
                if (i == activeIndex) {
                    marker.activate(true);//windowMoved
                } else {
                    marker.inactivate(true);//windowMoved
                }
            } else {
                marker.inactivate(true);
            }

            if (MODULATE_ALPHA_ENABLED) {
                // Update the marker's alpha
                float alpha = 1f;
                if (mMarkers.size() > windowSize) {
                    if ((windowAtStart && i > hWindowSize) ||
                            (windowAtEnd && i < (mMarkers.size() - hWindowSize)) ||
                            (!windowAtStart && !windowAtEnd)) {
                        alpha = 1f - Math.abs((i - windowMid) / hfWindowSize);
                    }
                }
                marker.animate().alpha(alpha).setDuration(500).start();
            }
        }

        if (!allowAnimations&&
        //M:liuzuo add addIcon  begin
        !misFolderPage) {
            //M:liuzuo add addIcon  end
            enableLayoutTransitions();
        }

        mWindowRange[0] = windowStart;
        mWindowRange[1] = windowEnd;
    }

    protected void addMarker(int index, PageMarkerResources marker, boolean allowAnimations){
        index = Math.max(0, Math.min(index, mMarkers.size()));

        PageIndicatorMarker m =
                (PageIndicatorMarker) mLayoutInflater.inflate(R.layout.page_indicator_marker,
                        this, false);
        m.setMarkerDrawables(marker.activeId, marker.inactiveId);

        mMarkers.add(index, m);
        offsetWindowCenterTo(mActiveMarkerIndex, allowAnimations);
    }

    protected void updateMarker(int index, PageMarkerResources marker) {
        if(index >= mMarkers.size())return;
        PageIndicatorMarker m = mMarkers.get(index);
        m.setMarkerDrawables(marker.activeId, marker.inactiveId);
    }

    protected void removeMarker(int index, boolean allowAnimations) {
        if (mMarkers.size() > 0) {
            index = Math.max(0, Math.min(mMarkers.size() - 1, index));
            mMarkers.remove(index);
            offsetWindowCenterTo(mActiveMarkerIndex, false);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        x = x<0?0:x;
        x = x>=getWidth()?(getWidth()-1):x;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            lastIndex = currentIndex = x/(getWidth()/mMarkers.size());
            mPagedView.snapToPage(currentIndex);
            if(mPagedView instanceof Workspace && mPagedView.getCurrentPage() != currentIndex && mLauncher!=null){
                mLauncher.showPageIndicatorDiagital(currentIndex);
            }
        }else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            currentIndex = ( x)/(getWidth()/mMarkers.size());
            if(lastIndex != currentIndex){
                mPagedView.snapToPage(currentIndex);
                if(mPagedView instanceof Workspace && mLauncher!=null){
                    mLauncher.showPageIndicatorDiagital(currentIndex);
                }
                lastIndex = currentIndex;
            }
        }else{
            if(mPagedView instanceof Workspace && mLauncher!=null){
                mLauncher.hidePageIndicatorDiagital();
            }
        }
        return true;
    }
}
