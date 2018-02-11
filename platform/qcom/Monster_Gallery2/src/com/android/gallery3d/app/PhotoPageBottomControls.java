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

import android.R.integer;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.android.gallery3d.R;
import com.android.gallery3d.util.LogUtil;
import com.coremedia.iso.boxes.ContainerBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import mst.widget.toolbar.Toolbar;

public class PhotoPageBottomControls implements OnClickListener {
    
    private static final String TAG = "PhotoPageBottomControls";
    
    public interface Delegate {
        // TCL ShenQianfeng Begin on 2016.09.20
        public boolean canDisplayBottomControlWhenZoomingIn(int control);
        // TCL ShenQianfeng End on 2016.09.20
        public boolean canDisplayBottomControls();
        public boolean canDisplayBottomControl(int control);
        public void onBottomControlClicked(int control);
        public void refreshBottomControlsWhenReady(boolean shouldAnimate);
    }

    private static final int TYPE_FADE_IN = 1;
    private static final int TYPE_FADE_OUT = 2;

    private Delegate mDelegate;
    private ViewGroup mParentLayout;
    private RelativeLayout mContainer;

    private ArrayList<View> mControls = new ArrayList<View>();

    private Animation mContainerAnimIn;//new AlphaAnimation(0f, 1f);
    private Animation mContainerAnimOut;// = new AlphaAnimation(1f, 0f);
    
    //private static final int CONTAINER_ANIM_DURATION_MS = 250;
    
    // TCL ShenQianfeng Begin on 2016.10.18
    // Annotated Below:
    /*
    private static final int CONTROL_ANIM_DURATION_MS = 20;
    private static Animation getControlAnimForVisibility(boolean visible) {
        Animation anim = visible ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);
        anim.setDuration(CONTROL_ANIM_DURATION_MS);
        return anim;
    }
    */
    // TCL ShenQianfeng End on 2016.10.18

    // TCL ShenQianfeng Begin on 2016.09.20
    private int mNavigationBarHeight;
    private int mOriginalPaddingBottom;
    private boolean mIsSettingZoomingAlpha;
    
    private HideAnimationListener mHideAnimationListener;

    public boolean isSettingZoomingAlpha() {
        return mIsSettingZoomingAlpha;
    }

    public void setIsSettingZoomingAlpha(boolean isSetting) {
        mIsSettingZoomingAlpha = isSetting;
    }
    
    public class HideAnimationListener implements AnimationListener {
        private View mView;

        public HideAnimationListener(View view) {
            mView = view;
        }

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mView.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }
    
    // TCL ShenQianfeng End on 2016.09.20

    public PhotoPageBottomControls(Delegate delegate, Context context, RelativeLayout layout, int navigationBarHeight) {
        mDelegate = delegate;
        mParentLayout = layout;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContainer = (RelativeLayout) inflater.inflate(R.layout.photopage_bottom_controls, mParentLayout, false);
        mNavigationBarHeight = navigationBarHeight;
        mOriginalPaddingBottom = mContainer.getPaddingBottom();
        // TCL ShenQianfeng Begin on 2016.10.18
        if(mNavigationBarHeight != 0) {
            mContainer.setPadding(mContainer.getPaddingLeft(), 
                                                             mContainer.getPaddingTop(), 
                                                             mContainer.getPaddingRight(), 
                                                             mOriginalPaddingBottom + mNavigationBarHeight);
        }
        // TCL ShenQianfeng End on 2016.10.18
        mParentLayout.addView(mContainer);
        // TCL BaiYuan Begin on 2016.10.18
        mContainer.setOnClickListener(this);
        // TCL BaiYuan End on 2016.10.18
        for (int i = mContainer.getChildCount() - 1; i >= 0; i--) {
            View child = mContainer.getChildAt(i);
            child.setOnClickListener(this);
            mControls.add(child);
        }

        mContainerAnimIn = AnimationUtils.loadAnimation(context, R.anim.float_up_in);
        mContainerAnimOut = AnimationUtils.loadAnimation(context, R.anim.float_down_out);
        //mContainerAnimIn.setDuration(CONTAINER_ANIM_DURATION_MS);
        //mContainerAnimOut.setDuration(CONTAINER_ANIM_DURATION_MS);

        mDelegate.refreshBottomControlsWhenReady(false);
    }
    
    /*
    public void updatePadding(boolean hasNavigationBarHeight) {
        if(mNavigationBarHeight == 0) return;
        int currentPaddingBottom = mContainer.getPaddingBottom();
        if(hasNavigationBarHeight) {
            if (currentPaddingBottom != mOriginalPaddingBottom + mNavigationBarHeight) {
                mContainer.setPadding(mContainer.getPaddingLeft(), 
                        mContainer.getPaddingTop(), 
                        mContainer.getPaddingRight(), 
                        mOriginalPaddingBottom + mNavigationBarHeight);
            }
        } else {
            if(currentPaddingBottom != mOriginalPaddingBottom) {
                mContainer.setPadding(mContainer.getPaddingLeft(), 
                        mContainer.getPaddingTop(), 
                        mContainer.getPaddingRight(), 
                        mOriginalPaddingBottom);
            }
        }
    }
    */
    
    public void updatePadding(Rect newWindowInsets) {
        int currentPaddingBottom = mContainer.getPaddingBottom();
        int paddingBottom = newWindowInsets.bottom == 0 ? mOriginalPaddingBottom : (newWindowInsets.bottom + mOriginalPaddingBottom);
        if(currentPaddingBottom != paddingBottom) {
                mContainer.setPadding(mContainer.getPaddingLeft(), 
                        mContainer.getPaddingTop(), 
                        mContainer.getPaddingRight(), 
                        paddingBottom);
        }
    }
    
    public void initControlStatusWhenZoomingInStart() {
        for( int i = 0; i < mControls.size(); i++) {
            View control = mControls.get(i);
            boolean enabled = mDelegate.canDisplayBottomControlWhenZoomingIn(control.getId());
            control.setEnabled(enabled);
            //LogUtil.d(TAG, " initControlStatusWhenZoomingInStart : i :" + i + " enabled:" + enabled);
        }
    }

    private void hide() {
        //LogUtil.d(TAG, "PhotoPageBottomControls::hide");
        mContainer.clearAnimation();
        mContainerAnimOut.reset();
        if(mHideAnimationListener == null) {
            mHideAnimationListener = new HideAnimationListener(mContainer);
        }
        mContainerAnimOut.setAnimationListener(mHideAnimationListener);
        mContainer.startAnimation(mContainerAnimOut);
    }

    private void show() {
        mContainer.clearAnimation();
        mContainerAnimIn.reset();
        mContainer.setVisibility(View.VISIBLE);
        mContainer.startAnimation(mContainerAnimIn);
        //LogUtil.d(TAG, "PhotoPageBottomControls::show");
    }

    // TCL ShenQianfeng Begin on 2016.09.20
    public void setAlpha(float alpha, boolean fadeIn) {
        //long time = System.currentTimeMillis();
        //LogUtil.d(TAG, "setAlpha: alpha:" + alpha + " fadeIn:" + fadeIn);
        if ((fadeIn && alpha == 0.0f) || ( ! fadeIn && alpha == 1.0f)) {
            mIsSettingZoomingAlpha = true;
        } else {
            mIsSettingZoomingAlpha = false;
        }
        if (fadeIn) {
            mContainer.setVisibility(View.VISIBLE);
            for(int i=0; i<mControls.size(); i++) {
                View view = mControls.get(i);
                boolean canDisplay = mDelegate.canDisplayBottomControlWhenZoomingIn(view.getId());
                view.setEnabled(canDisplay);
                //LogUtil.d(TAG, "setAlpha: canDisplay :" + canDisplay + " setEnabled i " + i);
            }
        }
        mContainer.setAlpha(alpha);
        if (! fadeIn && alpha == 0.0f) {
            mContainer.setVisibility(View.GONE);
        }
        //LogUtil.d(TAG, "PhotoPageBottomControls::setAlpha use ------>" + (System.currentTimeMillis() - time));
    }

    private boolean isContainerVisible() {
        return mContainer.getVisibility() == View.VISIBLE;
    }
    
    private boolean isControlEnabled(View control) {
        return control.isEnabled();
    }
    
    // TCL ShenQianfeng End on 2016.09.20
    
    public void refreshImmediately() {
        boolean visible = mDelegate.canDisplayBottomControls();
        boolean containerVisibilityChanged = (visible != isContainerVisible());
        if (containerVisibilityChanged && ! mIsSettingZoomingAlpha) {
            if (visible) {
                //LogUtil.i2(TAG, "refresh ----> show()");
                mContainer.setVisibility(View.VISIBLE);
            } else {
                //LogUtil.i2(TAG, "refresh ----> hide()");
                mContainer.setVisibility(View.GONE);
            }
        }
        for (final View control : mControls) {
            boolean prevVisibility = isControlEnabled(control);
            final boolean curVisibility = mDelegate.canDisplayBottomControl(control.getId());
            if (prevVisibility != curVisibility) {
                control.setEnabled(curVisibility);
                //LogUtil.i(TAG, "refresh ----> enabled:" + curVisibility);
            }
        }
    }

    public void refresh() {
        boolean visible = mDelegate.canDisplayBottomControls();
        //LogUtil.i2(TAG, "refresh canDisplayBottomControls visible: " + visible + " mIsSettingZoomingAlpha:" + mIsSettingZoomingAlpha);
        boolean containerVisibilityChanged = (visible != isContainerVisible());
        if (containerVisibilityChanged && ! mIsSettingZoomingAlpha) {
            if (visible) {
                //LogUtil.i2(TAG, "refresh ----> show()");
                show();
            } else {
                //LogUtil.i2(TAG, "refresh ----> hide()");
                hide();
            }
        }
        for (final View control : mControls) {
            boolean prevVisibility = isControlEnabled(control);
            final boolean curVisibility = mDelegate.canDisplayBottomControl(control.getId());
            if (prevVisibility != curVisibility) {
                control.setEnabled(curVisibility);
                //LogUtil.i(TAG, "refresh ----> enabled:" + curVisibility);
            }
        }
        // Force a layout change
        // mContainer.requestLayout(); // Kick framework to draw the control.
    }

    public void cleanup() {
        mParentLayout.removeView(mContainer);
        mControls.clear();
    }

    @Override
    public void onClick(View view) {
        /*
        Boolean controlVisible = mControlsVisible.get(view);
        if (mContainerVisible && controlVisible != null
                && controlVisible.booleanValue()) {
            mDelegate.onBottomControlClicked(view.getId());
        }
        */
        mDelegate.onBottomControlClicked(view.getId());
    }
}
