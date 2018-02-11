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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class PageIndicatorMarker extends FrameLayout {
    @SuppressWarnings("unused")
    private static final String TAG = "PageIndicator";

    private static final int MARKER_FADE_DURATION = 175;
    public boolean isCube = false;//lijun add for pageIndicatorCube
    public int markerId;

    private ImageView mActiveMarker;
    private ImageView mInactiveMarker;
    private boolean mIsActive = false;
    public PageIndicatorMarker(Context context) {
        this(context, null);
    }

    public PageIndicatorMarker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageIndicatorMarker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onFinishInflate() {
        mActiveMarker = (ImageView) findViewById(R.id.active);
        mInactiveMarker = (ImageView) findViewById(R.id.inactive);
    }

    void setMarkerDrawables(int activeResId, int inactiveResId) {
        Resources r = getResources();
        mActiveMarker.setImageDrawable(r.getDrawable(activeResId));
        mInactiveMarker.setImageDrawable(r.getDrawable(inactiveResId));
    }

    public void updateBackgroud() {
        int targetColor;
        int bgColor;
        int outlineColor;
        boolean isBlackText = LauncherAppState.getInstance().getWindowGlobalVaule().isBlackText();
        targetColor = getResources().getColor(R.color.celllayout_frame_nail_background_color_target);
        if (isBlackText) {
            bgColor = getResources().getColor(R.color.celllayout_frame_nail_background_color_dark);
            outlineColor = getResources().getColor(R.color.celllayout_frame_nail_background_color_normal_outline_dark);
        } else {
            bgColor = getResources().getColor(R.color.celllayout_frame_nail_background_color_light);
            outlineColor = getResources().getColor(R.color.celllayout_frame_nail_background_color_normal_outline_light);
        }
        bgColor = getResources().getColor(R.color.celllayout_frame_nail_background_color_translate);
        GradientDrawable gdActive = new GradientDrawable();
        gdActive.setColor(bgColor);
        gdActive.setCornerRadius(3);
        gdActive.setStroke(3, targetColor);
        mActiveMarker.setBackground(gdActive);

        GradientDrawable gdInActive = new GradientDrawable();
        gdInActive.setColor(bgColor);
        gdInActive.setCornerRadius(3);
        gdInActive.setStroke(2, outlineColor);
        mInactiveMarker.setBackground(gdInActive);
    }

    /**
     * lijun add just for cubeindicator
     * 用于缩略图指示器
     */
    void setMarkerDrawables(Bitmap b) {
        mActiveMarker.setImageBitmap(b);
        mInactiveMarker.setImageBitmap(b);
        updateBackgroud();//lijun add
    }

    void activate(boolean immediate) {
        if (immediate) {
            mActiveMarker.animate().cancel();
            mActiveMarker.setAlpha(1f);
            mActiveMarker.setScaleX(1f);
            mActiveMarker.setScaleY(1f);
            mInactiveMarker.animate().cancel();
            mInactiveMarker.setAlpha(0f);
        } else {
            mActiveMarker.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(MARKER_FADE_DURATION).start();
            mInactiveMarker.animate()
                    .alpha(0f)
                    .setDuration(MARKER_FADE_DURATION).start();
        }
        mIsActive = true;
    }

    void inactivate(boolean immediate) {
        if (immediate) {
            mInactiveMarker.animate().cancel();
            mInactiveMarker.setAlpha(1f);
            mActiveMarker.animate().cancel();
            mActiveMarker.setAlpha(0f);
            mActiveMarker.setScaleX(isCube ?1f:0.5f);
            mActiveMarker.setScaleY(isCube ?1f:0.5f);
        } else {
            mInactiveMarker.animate().alpha(1f)
                    .setDuration(MARKER_FADE_DURATION).start();
            mActiveMarker.animate()
                    .alpha(0f)
                    .scaleX(isCube ?1f:0.5f)
                    .scaleY(isCube ?1f:0.5f)
                    .setDuration(MARKER_FADE_DURATION).start();
        }
        mIsActive = false;
    }

    boolean isActive() {
        return mIsActive;
    }

    //lijun add for pageIndicatorCube
    public int getCenterX(){
        return (this.getLeft() + this.getRight())/2;
    }
}
