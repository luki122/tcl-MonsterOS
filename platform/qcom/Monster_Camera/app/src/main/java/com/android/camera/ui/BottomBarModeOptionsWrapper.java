/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.camera.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.android.camera.CaptureLayoutHelper;
import com.android.camera.debug.Log;
import com.tct.camera.R;

/**
 * The goal of this class is to ensure mode options is always laid out to
 * the left of or above bottom bar in landscape or portrait respectively.
 * All the other children in this view group can be expected to be laid out
 * the same way as they are in a normal FrameLayout.
 */
public class BottomBarModeOptionsWrapper extends FrameLayout {

    private final static Log.Tag TAG = new Log.Tag("BottomBarWrapper");
    private View mModeOptionsOverlay;
    private BottomBar mBottomBar;
    private CaptureLayoutHelper mCaptureLayoutHelper = null;
    private boolean mIsPhotoContactsIntent = false;
    public BottomBarModeOptionsWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onFinishInflate() {
        mModeOptionsOverlay = findViewById(R.id.mode_options_overlay);
        mBottomBar = (BottomBar)findViewById(R.id.bottom_bar);
    }

    public void setPhotoCaptureIntent(boolean isPhotoContactsIntent) {
        mIsPhotoContactsIntent = isPhotoContactsIntent;
    }

    /**
     * Sets a capture layout helper to query layout rect from.
     */
    public void setCaptureLayoutHelper(CaptureLayoutHelper helper) {
        mCaptureLayoutHelper = helper;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mCaptureLayoutHelper == null) {
            Log.e(TAG, "Capture layout helper needs to be set first.");
            return;
        }
        RectF uncoveredPreviewRect = mCaptureLayoutHelper.getUncoveredPreviewRect();
        RectF bottomBarRect = mCaptureLayoutHelper.getBottomBarRect();

        mModeOptionsOverlay.layout(0, 0, right-left, bottom-top);
        mBottomBar.layout((int) bottomBarRect.left, (int) bottomBarRect.top,
                (int) bottomBarRect.right, (int) bottomBarRect.bottom);
//        RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(mBottomBar.getHeight(),mBottomBar.getHeight());
//        mPeekImage.setLayoutParams(lp);

        if (mCaptureLayoutHelper.shouldOverlayBottomBar()) {
            if (mBottomBar.isBackgroundTransparent()) {
                mBottomBar.setBottomBarColor(Color.TRANSPARENT);
            } else {
                mBottomBar.setBottomBarColor(getResources().getColor(R.color.bottombar_background_overlay));
            }
        } else {
            if (mIsPhotoContactsIntent) {
                mBottomBar.setBottomBarColor(getResources().getColor(R.color.mode_scroll_bar_color_select));
            } else {
                mBottomBar.setBottomBarColor(getResources().getColor(R.color.bottombar_background_default));
            }
        }
    }
}