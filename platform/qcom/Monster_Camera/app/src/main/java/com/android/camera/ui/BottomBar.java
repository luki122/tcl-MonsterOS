/*
 * Copyright (C) 2013 The Android Open Source Project
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

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.android.camera.CaptureLayoutHelper;
import com.android.camera.ShutterButton;
import com.android.camera.debug.Log;
import com.android.camera.util.ApiHelper;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.android.camera.util.Gusterpolator;
import com.tct.camera.R;

import java.util.HashMap;
import java.util.Map;

/**
 * BottomBar swaps its width and height on rotation. In addition, it also
 * changes gravity and layout orientation based on the new orientation.
 * Specifically, in landscape it aligns to the right side of its parent and lays
 * out its children vertically, whereas in portrait, it stays at the bottom of
 * the parent and has a horizontal layout orientation.
 */
public class BottomBar extends FrameLayout {

    private static final Log.Tag TAG = new Log.Tag("BottomBar");

    private static final int CIRCLE_ANIM_DURATION_MS = 500;
    private static final int DRAWABLE_MAX_LEVEL = 10000;
    private static final int MODE_CAPTURE = 0;
    private static final int MODE_INTENT = 1;
    private static final int MODE_INTENT_REVIEW = 2;
    private static final int MODE_CANCEL = 3;
    private final boolean bVdfModeSwitchOn;

    private int mMode;

    private final int mBackgroundAlphaOverlay;
    private final int mBackgroundAlphaDefault;
    private boolean mOverLayBottomBar;

    private FrameLayout mCaptureLayout;
    private FrameLayout mCancelLayout;
    private TopRightWeightedLayout mIntentReviewLayout;

    private ShutterButton mShutterButton;
    private ShutterButton mContactsIntentShutterButton;
    private RotatableButton mRotatableButton;
    private PeekImageView mPeekThumb;
    private Button mContactsIntentPeekThumb;
    private View mModeStrip;
    private View mModeStripIndicator;
    private RotatableButton mCancelButton;
    private RotatableButton mCaptureButton;
    private RotatableButton mPauseRecord;
    private LockRotatableButton mSwitchButton;
    private View mSegmentRemoveButton;
    private View mRemixButton;
    private View mMockBar;

    private int mBackgroundColor;
    private int mBackgroundPressedColor;
    private int mBackgroundAlpha = 0xff;
    private final int mVideoButtonColor;

    private boolean mDrawCircle;
    private final float mCircleRadius;
    private CaptureLayoutHelper mCaptureLayoutHelper = null;

    private final Drawable.ConstantState[] mShutterButtonBackgroundConstantStates;
    // a reference to the shutter background's first contained drawable
    // if it's an animated circle drawable (for video mode)
    private AnimatedCircleDrawable mAnimatedCircleDrawable;
    // a reference to the shutter background's first contained drawable
    // if it's a color drawable (for all other modes)

    private BottomMockViewAnimationDrawable mMockViewAnimationDrawable;
    private ColorDrawable mColorDrawable;

    private RectF mRect = new RectF();

    private boolean mIsBackgroundTransparent;

    public BottomBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCircleRadius = getResources()
                .getDimensionPixelSize(R.dimen.video_capture_circle_diameter) / 2;
        mBackgroundAlphaOverlay = getResources()
                .getInteger(R.integer.bottom_bar_background_alpha_overlay);
        mBackgroundAlphaDefault = getResources()
                .getInteger(R.integer.bottom_bar_background_alpha);
        mVideoButtonColor=context.getResources().getColor(R.color.video_mode_color);

        // preload all the drawable BGs
        TypedArray ar = context.getResources()
                .obtainTypedArray(R.array.shutter_button_backgrounds);
        int len = ar.length();
        mShutterButtonBackgroundConstantStates = new Drawable.ConstantState[len];
        for (int i = 0; i < len; i++) {
            int drawableId = ar.getResourceId(i, -1);
            mShutterButtonBackgroundConstantStates[i] =
                    context.getResources().getDrawable(drawableId).getConstantState();
        }
        ar.recycle();
        bVdfModeSwitchOn = CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_FIX_MODE_SWITCHING, false);
    }

    private void setPaintColor(int alpha, int color) {
        if (mAnimatedCircleDrawable != null) {
            mAnimatedCircleDrawable.setColor(color);
            mAnimatedCircleDrawable.setAlpha(alpha);
        } else if (mColorDrawable != null) {
            mColorDrawable.setColor(color);
            mColorDrawable.setAlpha(alpha);
        }

        if (mIntentReviewLayout != null) {
            ColorDrawable intentBackground = (ColorDrawable) mIntentReviewLayout
                    .getBackground();
            intentBackground.setColor(color);
            intentBackground.setAlpha(alpha);
        }
    }

    private void refreshPaintColor() {
        setPaintColor(mBackgroundAlpha, mBackgroundColor);
    }

    private void setCancelBackgroundColor(int alpha, int color) {
        LayerDrawable layerDrawable = (LayerDrawable) mCancelButton.getBackground();
        Drawable d = layerDrawable.getDrawable(0);
        if (d instanceof AnimatedCircleDrawable) {
            AnimatedCircleDrawable animatedCircleDrawable = (AnimatedCircleDrawable) d;
            animatedCircleDrawable.setColor(color);
            animatedCircleDrawable.setAlpha(alpha);
        } else if (d instanceof ColorDrawable) {
            ColorDrawable colorDrawable = (ColorDrawable) d;
            if (!ApiHelper.isLOrHigher()) {
                colorDrawable.setColor(color);
            }
            colorDrawable.setAlpha(alpha);
        }
    }

    private void setCaptureButtonUp() {
        setPaintColor(mBackgroundAlpha, mBackgroundColor);
    }

    private void setCaptureButtonDown() {
        if (!ApiHelper.isLOrHigher()) {
            setPaintColor(mBackgroundAlpha, mBackgroundPressedColor);
        }
    }

    private void setCancelButtonUp() {
        setCancelBackgroundColor(mBackgroundAlpha, mBackgroundColor);
    }

    private void setCancelButtonDown() {
        setCancelBackgroundColor(mBackgroundAlpha, mBackgroundPressedColor);
    }

    @Override
    public void onFinishInflate() {
        mCaptureLayout =
                (FrameLayout) findViewById(R.id.bottombar_capture);
        mCancelLayout =
                (FrameLayout) findViewById(R.id.bottombar_cancel);
        mCancelLayout.setVisibility(View.GONE);

        mModeStrip=findViewById(R.id.mode_strip_view);

        if (!bVdfModeSwitchOn){
            mModeStripIndicator=findViewById(R.id.mode_scroll_indicator);
        }

        mIntentReviewLayout =
                (TopRightWeightedLayout) findViewById(R.id.bottombar_intent_review);
        mRotatableButton =(RotatableButton)findViewById(R.id.video_shutter_button);

        mPeekThumb=(PeekImageView)findViewById(R.id.peek_thumb);
        mContactsIntentPeekThumb=(Button)findViewById(R.id.contacts_intent_peek_thumb);
        mCaptureButton = (RotatableButton)findViewById(R.id.video_snap_button);
        mCaptureButton.setVisibility(View.GONE);
        mPauseRecord = (RotatableButton)findViewById(R.id.pause_record);
        mPauseRecord.setVisibility(GONE);
        mSegmentRemoveButton=findViewById(R.id.button_segement_remove);

        mMockBar=findViewById(R.id.bottombar_mock);
        mRemixButton=findViewById(R.id.button_remix);

        mShutterButton =
                (ShutterButton) findViewById(R.id.shutter_button);
        mShutterButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_DOWN == event.getActionMasked()) {
                    setCaptureButtonDown();
                } else if (MotionEvent.ACTION_UP == event.getActionMasked() ||
                        MotionEvent.ACTION_CANCEL == event.getActionMasked()) {
                    setCaptureButtonUp();
                } else if (MotionEvent.ACTION_MOVE == event.getActionMasked()) {
                    mRect.set(0, 0, getWidth(), getHeight());
                    if (!mRect.contains(event.getX(), event.getY())) {
                        setCaptureButtonUp();
                    }
                }
                return false;
            }
        });

        mContactsIntentShutterButton =
                (ShutterButton) findViewById(R.id.contacts_intent_shutter_button);
        mContactsIntentShutterButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_DOWN == event.getActionMasked()) {
                    setCaptureButtonDown();
                } else if (MotionEvent.ACTION_UP == event.getActionMasked() ||
                        MotionEvent.ACTION_CANCEL == event.getActionMasked()) {
                    setCaptureButtonUp();
                } else if (MotionEvent.ACTION_MOVE == event.getActionMasked()) {
                    mRect.set(0, 0, getWidth(), getHeight());
                    if (!mRect.contains(event.getX(), event.getY())) {
                        setCaptureButtonUp();
                    }
                }
                return false;
            }
        });
        mCancelButton =
                (RotatableButton) findViewById(R.id.shutter_cancel_button);
        mCancelButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_DOWN == event.getActionMasked()) {
                    setCancelButtonDown();
                } else if (MotionEvent.ACTION_UP == event.getActionMasked() ||
                        MotionEvent.ACTION_CANCEL == event.getActionMasked()) {
                    setCancelButtonUp();
                } else if (MotionEvent.ACTION_MOVE == event.getActionMasked()) {
                    mRect.set(0, 0, getWidth(), getHeight());
                    if (!mRect.contains(event.getX(), event.getY())) {
                        setCancelButtonUp();
                    }
                }
                return false;
            }
        });

        extendTouchAreaToMatchParent(R.id.done_button);

        mSwitchButton = (LockRotatableButton)findViewById(R.id.camera_toggle_button_botto_bottom);
    }

    private void extendTouchAreaToMatchParent(int id) {
        final View button = findViewById(id);
        final View parent = (View) button.getParent();

        parent.post(new Runnable() {
            @Override
            public void run() {
                Rect parentRect = new Rect();
                parent.getHitRect(parentRect);
                Rect buttonRect = new Rect();
                button.getHitRect(buttonRect);

                int widthDiff = parentRect.width() - buttonRect.width();
                int heightDiff = parentRect.height() - buttonRect.height();

                buttonRect.left -= widthDiff / 2;
                buttonRect.right += widthDiff / 2;
                buttonRect.top -= heightDiff / 2;
                buttonRect.bottom += heightDiff / 2;

                parent.setTouchDelegate(new TouchDelegate(buttonRect, button));
            }
        });
    }

    /**
     * Perform a transition from the bottom bar options layout to the bottom bar
     * capture layout.
     */
    public void transitionToCapture() {
        mCaptureLayout.setVisibility(View.VISIBLE);
        mCancelLayout.setVisibility(View.GONE);
        mIntentReviewLayout.setVisibility(View.GONE);

        mMode = MODE_CAPTURE;
    }

    /**
     * Perform a transition from the bottom bar options layout to the bottom bar
     * capture layout.
     */
    public void transitionToCancel() {
        mCaptureLayout.setVisibility(View.GONE);
        mIntentReviewLayout.setVisibility(View.GONE);
        mCancelLayout.setVisibility(View.VISIBLE);

        mMode = MODE_CANCEL;
    }

    /**
     * Perform a transition to the global intent layout. The current layout
     * state of the bottom bar is irrelevant.
     */
    public void transitionToIntentCaptureLayout() {
        mIntentReviewLayout.setVisibility(View.GONE);
        mCaptureLayout.setVisibility(View.VISIBLE);
        mCancelLayout.setVisibility(View.GONE);

        mMode = MODE_INTENT;
    }

    /**
     * Perform a transition to the global intent review layout. The current
     * layout state of the bottom bar is irrelevant.
     */
    public void transitionToIntentReviewLayout() {
        mCaptureLayout.setVisibility(View.GONE);
        mIntentReviewLayout.setVisibility(View.VISIBLE);
        mCancelLayout.setVisibility(View.GONE);

        mMode = MODE_INTENT_REVIEW;
    }

    /**
     * @return whether UI is in intent review mode
     */
    public boolean isInIntentReview() {
        return mMode == MODE_INTENT_REVIEW;
    }

    private void setButtonImageLevels(int level) {
        ((ImageButton) findViewById(R.id.cancel_button)).setImageLevel(level);
        ((ImageButton) findViewById(R.id.done_button)).setImageLevel(level);
        ((ImageButton) findViewById(R.id.retake_button)).setImageLevel(level);
    }

    private void setOverlayBottomBar(boolean overlay) {
        mOverLayBottomBar = overlay;
        if (overlay) {
            setBackgroundAlpha(mBackgroundAlphaOverlay);
            setButtonImageLevels(1);
        } else {
            setBackgroundAlpha(mBackgroundAlphaDefault);
            setButtonImageLevels(0);
        }
    }

    /**
     * Sets a capture layout helper to query layout rect from.
     */
    public void setCaptureLayoutHelper(CaptureLayoutHelper helper) {
        mCaptureLayoutHelper = helper;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int measureHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (measureWidth == 0 || measureHeight == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec); // MODIFIED by xuan.zhou, 2016-04-26,BUG-1996414
            return;
        }

        if (mCaptureLayoutHelper == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            Log.e(TAG, "Capture layout helper needs to be set first.");
        } else {
            setShutterButtonLayoutParams();
            RectF bottomBarRect = mCaptureLayoutHelper.getBottomBarRect();
            super.onMeasure(MeasureSpec.makeMeasureSpec(
                    (int) bottomBarRect.width(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec((int) bottomBarRect.height(), MeasureSpec.EXACTLY)
                    );
            boolean shouldOverlayBottomBar = mCaptureLayoutHelper.shouldOverlayBottomBar();
            setOverlayBottomBar(shouldOverlayBottomBar);
        }
    }

    // prevent touches on bottom bar (not its children)
    // from triggering a touch event on preview area
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    public void setBottomBarColor(int color){

        mCachedColor=color;
        if(mOverridenColor!=null){
            super.setBackgroundColor(mOverridenColor);
        }else{
            super.setBackgroundColor(color);
        }
    }


    private Integer mOverridenColor=null;
    private int mCachedColor=Color.TRANSPARENT;
    public void overrideBottomBarColor(Integer color){
        mOverridenColor=color;

        if(mOverridenColor==null){
            super.setBackgroundColor(mCachedColor);
            return;
        }
        super.setBackgroundColor(color);
    }

    @Override
    public void setBackgroundColor(int color) {
        mCachedColor=color;
        if(mMockBar!=null){
            mMockBar.setBackgroundColor(color);
        }
        setBackgroundColorInternal(color);
    }

    /**
     * This method wouldn't affect mock view color ,hence the video stop to full animation (vice versa) would change if use call this method
     * @param color
     */
    private void setBackgroundColorInternal(int color){
        mBackgroundColor = color;

        setPaintColor(mBackgroundAlpha, mBackgroundColor);
        setCancelBackgroundColor(mBackgroundAlpha, mBackgroundColor);
    }

    private void setBackgroundPressedColor(int color) {
        if (ApiHelper.isLOrHigher()) {
            // not supported (setting a color on a RippleDrawable is hard =[ )
        } else {
            mBackgroundPressedColor = color;
        }
    }

    private LayerDrawable applyCircleDrawableToShutterBackground(LayerDrawable shutterBackground) {
        // the background for video has a circle_item drawable placeholder
        // that gets replaced by an AnimatedCircleDrawable for the cool
        // shrink-down-to-a-circle effect
        // all other modes need not do this replace
        Drawable d = shutterBackground.findDrawableByLayerId(R.id.circle_item);
        if (d != null) {
            Drawable animatedCircleDrawable =
                    new AnimatedCircleDrawable((int) mCircleRadius);
            animatedCircleDrawable.setLevel(DRAWABLE_MAX_LEVEL);
//            shutterBackground
//                    .setDrawableByLayerId(R.id.circle_item, animatedCircleDrawable);
            mAnimatedCircleDrawable=(AnimatedCircleDrawable)animatedCircleDrawable;
        }

        return shutterBackground;
    }

    private LayerDrawable newDrawableFromConstantState(Drawable.ConstantState constantState) {
        return (LayerDrawable) constantState.newDrawable(getContext().getResources());
    }

    private void setupShutterBackgroundForModeIndex(int index) {
        LayerDrawable shutterBackground = applyCircleDrawableToShutterBackground(
                newDrawableFromConstantState(mShutterButtonBackgroundConstantStates[index]));
        mShutterButton.setBackground(shutterBackground);
        mCancelButton.setBackground(applyCircleDrawableToShutterBackground(
                newDrawableFromConstantState(mShutterButtonBackgroundConstantStates[index])));

        Drawable d = shutterBackground.getDrawable(0);
        mAnimatedCircleDrawable = null;
        mColorDrawable = null;
        if (d instanceof AnimatedCircleDrawable) {
            mAnimatedCircleDrawable = (AnimatedCircleDrawable) d;
        } else if (d instanceof ColorDrawable) {
            mColorDrawable = (ColorDrawable) d;
        }

        int colorId = CameraUtil.getCameraThemeColorId(index, getContext());
        int pressedColor = getContext().getResources().getColor(colorId);
        setBackgroundPressedColor(pressedColor);
        refreshPaintColor();
    }


    private LayerDrawable applyBottomMockAnimationToMockView(LayerDrawable mockBackgorund,int targetColor) {
        //replace the background of mockView to be an instance of BottomMockViewAnimationDrawable
        Drawable d = mockBackgorund.findDrawableByLayerId(R.id.mock_circle);
        if (d != null&&!(d instanceof  BottomMockViewAnimationDrawable)) {
            Point shutterCenter=new Point();
            int x=mShutterButton.getLeft()+mShutterButton.getMeasuredWidth()/2;
            int y=mShutterButton.getTop()+mShutterButton.getMeasuredHeight()/2;
            shutterCenter.set(x,y);
            Drawable mockAnimationDrawable =
                    new BottomMockViewAnimationDrawable(shutterCenter,targetColor,mShutterButton.getWidth());
            mockAnimationDrawable.setLevel(DRAWABLE_MAX_LEVEL);
            mockBackgorund.setDrawableByLayerId(R.id.mock_circle,mockAnimationDrawable);
            mMockViewAnimationDrawable=(BottomMockViewAnimationDrawable)mockAnimationDrawable;
        }

        return mockBackgorund;
    }

    private void setUpMockViewBackground(int targetColor){

        mMockBar.setBackground(
                applyBottomMockAnimationToMockView(
                        (LayerDrawable) this.getResources().
                                getDrawable(R.drawable.bottom_mock_background, null).
                                getConstantState().newDrawable(),
                        targetColor));
    }

    public void setColorsForModeIndex(int index) {
        setupShutterBackgroundForModeIndex(index);
    }

    public void setBackgroundAlpha(int alpha) {
        mBackgroundAlpha = alpha;
        setPaintColor(mBackgroundAlpha, mBackgroundColor);
        setCancelBackgroundColor(mBackgroundAlpha, mBackgroundColor);
    }

    /**
     * Sets the shutter button enabled if true, disabled if false.
     * <p>
     * Disabled means that the shutter button is not clickable and is greyed
     * out.
     */
    public void setShutterButtonEnabled(final boolean enabled) {
        mShutterButton.post(new Runnable() {
            @Override
            public void run() {
                mShutterButton.setEnabled(enabled);
                setShutterButtonImportantToA11y(enabled);
            }
        });
    }

    /* MODIFIED-BEGIN by sichao.hu, 2016-03-22, BUG-1027573 */
    public void setShutterbuttonEnabledWithoutAppearenceChanged(final boolean enabled){
        mShutterButton.post(new Runnable() {
            @Override
            public void run() {
                mShutterButton.enableFilter(false);
                mShutterButton.setEnabled(enabled);
                setShutterButtonImportantToA11y(enabled);
                mShutterButton.enableFilter(true);
            }
        });
    }
    /* MODIFIED-END by sichao.hu,BUG-1027573 */

    public void setShutterButtonPress(final boolean press) {
        mShutterButton.post(new Runnable() {
            @Override
            public void run() {
                mShutterButton.setPressed(press);
            }
        });
    }
    public void setShutterButtonLongClickable(final boolean longClickable) {
        mShutterButton.post(new Runnable() {
            @Override
            public void run() {
                mShutterButton.setLongClickable(longClickable);
            }
        });
    }
    /**
     * Sets whether shutter button should be included in a11y announcement and
     * navigation
     */
    public void setShutterButtonImportantToA11y(boolean important) {
        if (important) {
            mShutterButton.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
        } else {
            mShutterButton.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
    }

    /**
     * Returns whether the capture button is enabled.
     */
    public boolean isShutterButtonEnabled() {
        return mShutterButton.isEnabled();
    }

    private TransitionDrawable crossfadeDrawable(Drawable from, Drawable to) {
        Drawable[] arrayDrawable = new Drawable[2];
        arrayDrawable[0] = from;
        arrayDrawable[1] = to;
        TransitionDrawable transitionDrawable = new TransitionDrawable(arrayDrawable);
        transitionDrawable.setCrossFadeEnabled(true);
        return transitionDrawable;
    }

    /**
     * Sets the shutter button's icon resource. By default, all drawables
     * instances loaded from the same resource share a common state; if you
     * modify the state of one instance, all the other instances will receive
     * the same modification. In order to modify properties of this icon
     * drawable without affecting other drawables, here we use a mutable
     * drawable which is guaranteed to not share states with other drawables.
     */
    private int mResId=-1;
    public void setShutterButtonIcon(int resId) {
        if(mResId==resId){
            return;
        }
        mResId=resId;
        Drawable iconDrawable = getResources().getDrawable(resId);
        if (iconDrawable != null) {
            iconDrawable = iconDrawable.mutate();
        }
        mShutterButton.setImageDrawable(iconDrawable);
    }

    public void setShutterButtonIcon(int resId, boolean animation) {
        if(mResId==resId){
            return;
        }
        mResId=resId;
        Drawable iconDrawable = getResources().getDrawable(resId);
        if (iconDrawable != null) {
            iconDrawable = iconDrawable.mutate();
        }
        if (animation) {
            mShutterButton.setImageDrawable(iconDrawable);
        } else {
            mShutterButton.setImageDrawableWithoutRotation(iconDrawable);
        }
    }

    public void setShutterButtonLayoutParams() {
        RelativeLayout.LayoutParams shutterButtonParams = (RelativeLayout.LayoutParams) mShutterButton.getLayoutParams();
        if (mCaptureLayoutHelper.isBottomBarHeightReset()) {
            shutterButtonParams.topMargin = (int) getResources().getDimension(R.dimen.video_capture_shutter_margin_top);
        } else {
            shutterButtonParams.topMargin = (int) getResources().getDimension(R.dimen.bottom_bar_shutter_margin_top);
        }
        mShutterButton.setLayoutParams(shutterButtonParams);
    }

    /**
     * Animates bar to a single stop button
     */
    public void animateToVideoStop(int resId) {
//        if (mOverLayBottomBar && mAnimatedCircleDrawable != null) {
//            mAnimatedCircleDrawable.animateToSmallRadius();
//            mDrawCircle = true;
//        }

        mModeStrip.setVisibility(GONE);
        if(!bVdfModeSwitchOn && mModeStripIndicator != null){
            mModeStripIndicator.setVisibility(GONE);
        }
        mMockBar.setVisibility(VISIBLE);
        overrideBottomBarColor(Color.TRANSPARENT);
        setUpMockViewBackground(mCachedColor);

//        if(mMockViewAnimationDrawable!=null){
//            mMockViewAnimationDrawable.animateToCircle(this.getContext().getResources().getColor(R.color.video_mode_color)
//            ,this.getMeasuredWidth(),this.getMeasuredHeight());
//        }

        TransitionDrawable transitionDrawable = crossfadeDrawable(
                mShutterButton.getDrawable(),
                getResources().getDrawable(resId));
        mShutterButton.setImageDrawableWithoutRotation(transitionDrawable);
        transitionDrawable.startTransition(CIRCLE_ANIM_DURATION_MS);
        animateHidePeek();
        animateHideVideoShutter();
    }

    /**
     * Animates bar to full width / length with video capture icon
     */

    public static interface BottomBarSizeListener{
        public void onFullSizeReached();
    }

    public void animateToFullSize(int resId,final BottomBarSizeListener listener) {
//        if (mDrawCircle && mAnimatedCircleDrawable != null) {
//            mAnimatedCircleDrawable.animateToFullSize();
//            mDrawCircle = false;
//        }

        overrideBottomBarColor(Color.TRANSPARENT);

        TransitionDrawable transitionDrawable = crossfadeDrawable(
                mShutterButton.getDrawable(),
                getResources().getDrawable(resId));
        if(mMockViewAnimationDrawable!=null){
            Log.v(TAG,"animate to full size");
            mMockViewAnimationDrawable.animateToFullSize(new BottomMockViewAnimationDrawable.OnAnimationAsyncListener() {
                @Override
                public void onAnimationFinish() {
                    Log.v(TAG, "fullsize animation end");// MODIFIED by sichao.hu, 2016-03-22, BUG-1027573
                    if(listener!=null) {
                        listener.onFullSizeReached();
                    }
                    overrideBottomBarColor(null);
                    mModeStrip.setVisibility(VISIBLE);
                    if(!bVdfModeSwitchOn && mModeStripIndicator != null){
                        mModeStripIndicator.setVisibility(VISIBLE);
                    }

                    mMockBar.setVisibility(GONE);
                }
            });

        }

        mShutterButton.setImageDrawableWithoutRotation(transitionDrawable);
        transitionDrawable.startTransition(CIRCLE_ANIM_DURATION_MS);

    }

    private ValueAnimator[] mHideContactsIntentShutterButtonAnimator = new ValueAnimator[1];
    private ValueAnimator[] mShowContactsIntentShutterButtonAnmator = new ValueAnimator[1];
    private ValueAnimator[] mHideShutterButtonAnimator = new ValueAnimator[1];
    private ValueAnimator[] mShowShutterButtonAnmator = new ValueAnimator[1];
    private ValueAnimator[] mHidePeekAnimator = new ValueAnimator[1];
    private ValueAnimator[] mShowPeekAnmator = new ValueAnimator[1];
    private ValueAnimator[] mHideContactsIntentPeekAnimator = new ValueAnimator[1];
    private ValueAnimator[] mShowContactsIntentPeekAnmator = new ValueAnimator[1];
    private ValueAnimator[] mHideVideoShutterAnimator = new ValueAnimator[1];
    private ValueAnimator[] mShowVideoShutterAnimator = new ValueAnimator[1];
    private ValueAnimator[] mHideVideoCaptureAnimator = new ValueAnimator[1];
    private ValueAnimator[] mShowVideoCaptureAnimator = new ValueAnimator[1];
    private ValueAnimator[] mHideSegmentRemove = new ValueAnimator[1];
    private ValueAnimator[] mShowSegmentRemove = new ValueAnimator[1];
    private ValueAnimator[] mHideRemix = new ValueAnimator[1];
    private ValueAnimator[] mShowRemix = new ValueAnimator[1];
    private ValueAnimator[] mHideSwitchButtonAnimator = new ValueAnimator[1];
    private ValueAnimator[] mShowSwitchButtonAnimator = new ValueAnimator[1];
    private ValueAnimator[] mHidePauseButtonAnimator = new ValueAnimator[1];
    private ValueAnimator[] mShowPauseButtonAnimator = new ValueAnimator[1];

    private ValueAnimator buildShowingAnimator(final View viewToShow){
        ValueAnimator animator =ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.setDuration(CIRCLE_ANIM_DURATION_MS);
        animator.setInterpolator(Gusterpolator.INSTANCE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (Float) animation.getAnimatedValue();
                viewToShow.setAlpha(alpha);
                if (viewToShow.getVisibility() == View.GONE) {
                    viewToShow.setVisibility(View.VISIBLE);
                }
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        return animator;
    }

    private ValueAnimator buildHidingAnimator(final View viewToHide) {
        ValueAnimator animator = ValueAnimator.ofFloat(1.0f, 0.0f);
        animator.setDuration(CIRCLE_ANIM_DURATION_MS);
        animator.setInterpolator(Gusterpolator.INSTANCE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (Float) animation.getAnimatedValue();
                viewToHide.setAlpha(alpha);
                if (alpha == 0.0f) {
                    viewToHide.setVisibility(View.GONE);
                    viewToHide.setAlpha(1.0f);
                }
            }
        });
        return animator;
    }


    private void animateHide(ValueAnimator[] showingAnimator,ValueAnimator[] hidingAnimator,View viewToHide){
        Log.w(TAG, "animateHide " + viewToHide);
        if(showingAnimator ==null|| hidingAnimator ==null) {
            return;
        }
        if(viewToHide.getVisibility()==View.GONE && (showingAnimator[0] == null || !showingAnimator[0].isRunning())){
            return;
        }
        if(hidingAnimator[0] == null || showingAnimator[0] == null){
            showingAnimator[0]=buildShowingAnimator(viewToHide);
            hidingAnimator[0]=buildHidingAnimator(viewToHide);
        }
        if(showingAnimator[0].isRunning()){
            showingAnimator[0].cancel();
        }
        if(!hidingAnimator[0].isRunning()){
            hidingAnimator[0].start();
        }
        lockView(viewToHide);
    }

    private void lockView(View viewToLock){
        if(viewToLock instanceof Lockable){
            if(mAnimationViewLockMap.containsKey(viewToLock)){
                int prevLock=mAnimationViewLockMap.get(viewToLock);
                ((Lockable)viewToLock).unlockWithToken(prevLock);
            }
            int lock=((Lockable) viewToLock).lock();
            mAnimationViewLockMap.put(viewToLock, lock);
        }
    }

    private void unlockView(View viewToUnlock){
        if(viewToUnlock instanceof Lockable){
            if(mAnimationViewLockMap.containsKey(viewToUnlock)){
                int prevLock=mAnimationViewLockMap.get(viewToUnlock);
                ((Lockable)viewToUnlock).unlockWithToken(prevLock);
            }
        }

    }

    private void hideWithoutAnimation(ValueAnimator[] showingAnimator,ValueAnimator[] hidingAnimator,View viewToHide){
        Log.w(TAG, "animateHide " + viewToHide);
        if(showingAnimator ==null|| hidingAnimator ==null) {
            viewToHide.setVisibility(GONE);
            return;
        }
        if(hidingAnimator[0] == null || showingAnimator[0] == null){
            viewToHide.setVisibility(GONE);
            return;
        }
        if(showingAnimator[0].isRunning()){
            showingAnimator[0].cancel();
        }
        if(hidingAnimator[0].isRunning()){
            hidingAnimator[0].end();
        }
        viewToHide.setVisibility(GONE);
    }

    private Map<View ,Integer> mAnimationViewLockMap=new HashMap<>();

    private void showWithoutAnimation(ValueAnimator[] showingAnimator,ValueAnimator[] hidingAnimator,View viewToShow){
        if(showingAnimator==null||hidingAnimator==null){
            viewToShow.setVisibility(VISIBLE);
            return;
        }
        if(hidingAnimator[0] == null || showingAnimator[0] == null){
            viewToShow.setAlpha(1.0f);
            viewToShow.setVisibility(VISIBLE);
            return;
        }
        if(showingAnimator[0].isRunning()){
            showingAnimator[0].end();
        }
        if(hidingAnimator[0].isRunning()){
            hidingAnimator[0].cancel();
        }
        viewToShow.setVisibility(VISIBLE);
        unlockView(viewToShow);
    }

    private void animateShow(ValueAnimator[] showingAnimator,ValueAnimator[] hidingAnimator,View viewToShow){
        Log.w(TAG, "animateShow " + viewToShow);
        if(showingAnimator ==null|| hidingAnimator ==null) {
            return;
        }
        if(viewToShow.getVisibility()==View.VISIBLE && (hidingAnimator[0] == null || !hidingAnimator[0].isRunning())){
            return;
        }
        if(hidingAnimator[0] ==null|| showingAnimator[0] ==null){
            showingAnimator[0]=buildShowingAnimator(viewToShow);
            hidingAnimator[0]=buildHidingAnimator(viewToShow);
        }
        if(hidingAnimator[0].isRunning()){
            hidingAnimator[0].cancel();
        }

        if(!showingAnimator[0].isRunning()){
            showingAnimator[0].start();
        }
        unlockView(viewToShow);
    }

    public void showContactsIntentShutterButton() {
        showWithoutAnimation(mShowContactsIntentShutterButtonAnmator, mHideContactsIntentShutterButtonAnimator, mContactsIntentShutterButton);
    }

    public void hideShutterButton(){
        hideWithoutAnimation(mShowShutterButtonAnmator, mHideShutterButtonAnimator, mShutterButton);
    }

    public void showPeek(){
        showWithoutAnimation(mShowPeekAnmator, mHidePeekAnimator, mPeekThumb);
    }


    public void hidePeek(){
        hideWithoutAnimation(mShowPeekAnmator, mHidePeekAnimator, mPeekThumb);
    }

    public void showContactsIntentPeek() {
        showWithoutAnimation(mShowContactsIntentPeekAnmator, mHideContactsIntentPeekAnimator, mContactsIntentPeekThumb);
    }


    public void hideContactsIntentPeek() {
        hideWithoutAnimation(mShowContactsIntentPeekAnmator, mHideContactsIntentPeekAnimator, mContactsIntentPeekThumb);
    }

    public void showVideoShutter(){
        showWithoutAnimation(mShowVideoShutterAnimator, mHideVideoShutterAnimator, mRotatableButton);
    }

    public void hideVideoShutter(){
        hideWithoutAnimation(mShowVideoShutterAnimator, mHideVideoShutterAnimator, mRotatableButton);
    }

    public void showSegmentRemove(){
        showWithoutAnimation(mShowSegmentRemove, mHideSegmentRemove, mSegmentRemoveButton);
    }

    public void hideSegementRemove(){
        hideWithoutAnimation(mShowSegmentRemove, mHideSegmentRemove, mSegmentRemoveButton);
    }

    public void showRemix(){
        showWithoutAnimation(mShowRemix, mHideRemix, mRemixButton);
    }

    public void hideRemix(){
        hideWithoutAnimation(mShowRemix, mHideRemix, mRemixButton);
    }

    public void showVideoCapture(){
        showWithoutAnimation(mShowVideoCaptureAnimator, mHideVideoCaptureAnimator, mCaptureButton);
    }

    public void hideVideoCapture(){
        hideWithoutAnimation(mShowVideoCaptureAnimator, mHideVideoCaptureAnimator, mCaptureButton);
    }

    public void animateHidePeek(){
        animateHide(mShowPeekAnmator, mHidePeekAnimator, mPeekThumb);
    }

    public void animateShowPeek(){
        animateShow(mShowPeekAnmator, mHidePeekAnimator, mPeekThumb);
    }

    public void animateHideVideoShutter(){
       animateHide(mShowVideoShutterAnimator, mHideVideoShutterAnimator, mRotatableButton);
    }

    public void animateShowVideoShutter(){
        animateShow(mShowVideoShutterAnimator, mHideVideoShutterAnimator, mRotatableButton);
    }

    public void animateHideSegementRemove(){
        animateHide(mShowSegmentRemove, mHideSegmentRemove, mSegmentRemoveButton);
    }

    public void animateShowSegmentRemove(){
        animateShow(mShowSegmentRemove, mHideSegmentRemove, mSegmentRemoveButton);
    }

    public void animateHideRemix(){
        animateHide(mShowRemix, mHideRemix, mRemixButton);
    }

    public void animateShowRemix(){
        animateShow(mShowRemix, mHideRemix, mRemixButton);
    }

    public void animateHideVideoCapture() {
       animateHide(mShowVideoCaptureAnimator, mHideVideoCaptureAnimator, mCaptureButton);
    }

    public void animateShowVideoCapture() {
        animateShow(mShowVideoCaptureAnimator, mHideVideoCaptureAnimator, mCaptureButton);
    }

    public void animateHidePauseRecord() {
        animateHide(mShowPauseButtonAnimator, mHidePauseButtonAnimator, mPauseRecord);
    }

    public void animateShowPauseRecord() {
        animateShow(mShowPauseButtonAnimator, mHidePauseButtonAnimator, mPauseRecord);
    }

    public void animateHideSwitchButton() {
        animateHide(mShowSwitchButtonAnimator, mHideSwitchButtonAnimator, mSwitchButton);
    }

    public void animateShowSwitchButton() {
        animateShow(mShowSwitchButtonAnimator, mHideSwitchButtonAnimator, mSwitchButton);
    }

    public void showSwitchButton() {
        showWithoutAnimation(mShowSwitchButtonAnimator, mHideSwitchButtonAnimator, mSwitchButton);
    }

    public boolean isBackgroundTransparent() {
        return mIsBackgroundTransparent;
    }

    public void setIsBackgroundTransparent(boolean isBackgroundTransparent) {
        mIsBackgroundTransparent = isBackgroundTransparent;
    }

    public void hideSwitchButton() {
        if(mSwitchButton!=null){
            if(mSwitchButton.getVisibility()!=View.GONE) {
                if(Thread.currentThread()== Looper.getMainLooper().getThread()) {
                    mSwitchButton.setVisibility(View.GONE);
                }else{
                    mSwitchButton.post(new Runnable() {
                        @Override
                        public void run() {
                            mSwitchButton.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }
    }

    public void setSwitchBtnEnabled(boolean enabled) {
        if (mSwitchButton == null) {
            return;
        }
        mSwitchButton.setEnabled(enabled);
    }

    /**
     * A callback executed in the state listener of a button.
     *
     * Used by a module to set specific behavior when a button's
     * state changes.
     */
    public interface SwitchButtonCallback {
        public void onToggleStateChanged(int state);
    }
}