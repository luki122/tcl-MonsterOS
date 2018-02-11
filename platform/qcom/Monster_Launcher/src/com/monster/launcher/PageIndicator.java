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
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * lijun change to abstract offsetWindowCenterTo,addMarker,updateMarker,removeMarker see PageIndicatorCube or PageIndicatorCircle
 */
public abstract class PageIndicator extends LinearLayout {
    @SuppressWarnings("unused")
    private static final String TAG = "PageIndicator";
    // Want this to look good? Keep it odd
    protected static final boolean MODULATE_ALPHA_ENABLED = false;

    protected LayoutInflater mLayoutInflater;
    protected int[] mWindowRange = new int[2];
    protected int mMaxWindowSize;

    public ArrayList<PageIndicatorMarker> mMarkers =
            new ArrayList<PageIndicatorMarker>();//liuzuo public>>protected
    protected int mActiveMarkerIndex;

    public void setIsFolderPage(boolean misFolderPage) {
        this.misFolderPage = misFolderPage;
    }

    protected PagedView mPagedView;
    public void setpagedView(PagedView pagedView) {
        this.mPagedView = pagedView;
    }
    //M:liuzuo add addIcon  begin
    protected boolean misFolderPage;
    public static class PageMarkerResources {
        int activeId;
        int inactiveId;
        Bitmap framBitmap;
        public PageMarkerResources() {
            //int color =LauncherAppState.getInstance().getWindowGlobalVaule().getTextColor();
            boolean isBlackText = LauncherAppState.getInstance().getWindowGlobalVaule().isBlackText();
            if(isBlackText){
                activeId = R.drawable.ic_pageindicator_current_black;
                inactiveId = R.drawable.ic_pageindicator_default_black;
            }else{
                activeId = R.drawable.ic_pageindicator_current_white;
                inactiveId = R.drawable.ic_pageindicator_default_white;
            }
        }
        public PageMarkerResources(int aId, int iaId) {
            activeId = aId;
            inactiveId = iaId;
        }
        public PageMarkerResources(Bitmap fb) {
            framBitmap = fb;
        }
    }

    public PageIndicator(Context context) {
        this(context, null);
    }

    public PageIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageIndicator(Context context, AttributeSet attrs, int defStyle) {
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

    abstract protected void offsetWindowCenterTo(int activeIndex, boolean allowAnimations);

    abstract protected void addMarker(int index, PageMarkerResources marker, boolean allowAnimations);

    abstract protected void updateMarker(int index, PageMarkerResources marker) ;

    abstract protected void removeMarker(int index, boolean allowAnimations) ;

    protected void addMarkers(ArrayList<PageMarkerResources> markers, boolean allowAnimations) {
        for (int i = 0; i < markers.size(); ++i) {
            addMarker(Integer.MAX_VALUE, markers.get(i), allowAnimations);
        }
    }

    protected void removeAllMarkers(boolean allowAnimations) {
        while (mMarkers.size() > 0) {
            removeMarker(Integer.MAX_VALUE, allowAnimations);
        }
    }
    protected void setActiveMarker(int index) {
        // Center the active marker
        mActiveMarkerIndex = index;
        offsetWindowCenterTo(index, false);
    }

    protected void clear(){
        removeAllViews();
        mMarkers.clear();
    }

    protected void dumpState(String txt) {
        System.out.println(txt);
        System.out.println("\tmMarkers: " + mMarkers.size());
        for (int i = 0; i < mMarkers.size(); ++i) {
            PageIndicatorMarker m = mMarkers.get(i);
            System.out.println("\t\t(" + i + ") " + m);
        }
        System.out.println("\twindow: [" + mWindowRange[0] + ", " + mWindowRange[1] + "]");
        System.out.println("\tchildren: " + getChildCount());
        for (int i = 0; i < getChildCount(); ++i) {
            PageIndicatorMarker m = (PageIndicatorMarker) getChildAt(i);
            System.out.println("\t\t(" + i + ") " + m);
        }
        System.out.println("\tactive: " + mActiveMarkerIndex);
    }
}
