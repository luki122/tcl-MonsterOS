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

package com.android.camera;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
/* MODIFIED-BEGIN by sichao.hu, 2016-03-22, BUG-1027573 */
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
/* MODIFIED-END by sichao.hu,BUG-1027573 */
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.android.camera.debug.Log;
import com.android.camera.ui.RotateImageView;
import com.android.camera.ui.TouchCoordinate;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.Gusterpolator;
import com.android.external.plantform.ExtBuild; //MODIFIED by peixin, 2016-04-11,BUG-1929434

/**
 * A button designed to be used for the on-screen shutter button.
 * It's currently an {@code ImageView} that can call a delegate when the
 * pressed state changes.
 */
public class ShutterButton extends RotateImageView {
    private static final Log.Tag TAG = new Log.Tag("ShutterButton");
    public static final float ALPHA_WHEN_ENABLED = 1f;
    public static final float ALPHA_WHEN_DISABLED = 0.2f;


    private static final float ROTATE_DEGREE_TARGET=45f;
    private static final int TRANSITION_DURATION =250;// MODIFIED by sichao.hu, 2016-03-22, BUG-1027573
    private static final float BASE_ALPHA=0.5f;

    private static final int LONGPRESS_THRESHOLD=300;

    private boolean mTouchEnabled = true;
    private TouchCoordinate mTouchCoordinate;

    private final Context mContext;
    /**
     * A callback to be invoked when a ShutterButton's pressed state changes.
     */
    public interface OnShutterButtonListener {
        /**
         * Called when a ShutterButton has been pressed.
         *
         * @param pressed The ShutterButton that was pressed.
         */
        void onShutterButtonFocus(boolean pressed);
        void onShutterCoordinate(TouchCoordinate coord);
        void onShutterButtonClick();
        void onShutterButtonLongClick();
    }

    private List<OnShutterButtonListener> mListeners
        = new ArrayList<OnShutterButtonListener>();
    private boolean mOldPressed;
    private boolean mIsContructed=false;

    public ShutterButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setScaleType(ScaleType.MATRIX);
//        setOnLongClickListener(mLongClick);
        mIsContructed=true;

    }

    /**
     * Add an {@link OnShutterButtonListener} to a set of listeners.
     */
    private static Object LISTENER_SYNC_LOCK=new Object();
    public void addOnShutterButtonListener(OnShutterButtonListener listener) {
        synchronized (LISTENER_SYNC_LOCK) {
            if (!mListeners.contains(listener)) {
                mListeners.add(listener);
            }
        }
    }

    /**
     * Remove an {@link OnShutterButtonListener} from a set of listeners.
     */
    public void removeOnShutterButtonListener(OnShutterButtonListener listener) {
        synchronized (LISTENER_SYNC_LOCK) {
            if (mListeners.contains(listener)) {
                mListeners.remove(listener);
            }
        }
    }

    private Matrix mScaleMatrix;
    private Matrix mRotateMatrix;
    private Matrix mCombineMatrix;
    private AnimatorSet mAnimatorSet;

    /* MODIFIED-BEGIN by sichao.hu, 2016-03-22, BUG-1027573 */
    private Drawable mPendingDrawable;
    @Override
    public void setImageDrawable(final Drawable drawable) {
        if (this.getDrawable()==null || !mIsContructed || this.getWidth() == 0
                || CameraUtil.isBatterySaverEnabled(mContext)){
            resetShutter(drawable);
            return;
        }

        mPendingAlpha=0;
        mPendingDrawable=drawable;
//        playShutterIconTransferAnimation(drawable);
        playShutterIconFadeInOutAnimation(drawable);

    }

    public void setImageDrawableWithoutRotation(final Drawable drawable){
        if(mFadeComboAnimator!=null&&mFadeComboAnimator.isRunning()){
            mFadeComboAnimator.cancel();
        }
        resetShutter(drawable);
    }


    private ValueAnimator mFadeInAnimator;
    private ValueAnimator mFadeOutAnimator;
    private AnimatorSet mFadeComboAnimator;
    private int mCurrentAlpha =255;//Indicates the alpha value for fade-in animation
    private int mPendingAlpha =0;//Indicates the alpha value for fade-out animation
    private void initializeFadeAnimation(){
        ValueAnimator.AnimatorUpdateListener fadeInAnimatorUpdateListener=new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mCurrentAlpha =(int)mFadeInAnimator.getAnimatedValue();

            }
        };
        ValueAnimator.AnimatorUpdateListener fadeOutAnimatorUpdateListener=new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mPendingAlpha =(int)valueAnimator.getAnimatedValue();
                invalidate();
            }
        };
        mFadeInAnimator=ValueAnimator.ofInt(255,0);
        mFadeInAnimator.setDuration(TRANSITION_DURATION);
        mFadeInAnimator.addUpdateListener(fadeInAnimatorUpdateListener);

        mFadeOutAnimator=ValueAnimator.ofInt(0, 255);
        mFadeOutAnimator.setDuration(TRANSITION_DURATION);
        mFadeOutAnimator.addUpdateListener(fadeOutAnimatorUpdateListener);

        mFadeComboAnimator=new AnimatorSet();
        mFadeComboAnimator.playTogether(mFadeInAnimator, mFadeOutAnimator);
        mFadeComboAnimator.setDuration(TRANSITION_DURATION);
    }

    private void playShutterIconFadeInOutAnimation(final Drawable drawable){
        if(mFadeComboAnimator==null){
            initializeFadeAnimation();
        }
        if(mFadeComboAnimator.isRunning()){
            mFadeComboAnimator.cancel();
        }
        mFadeComboAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                /* MODIFIED-BEGIN by bin.zhang2-nb, 2016-04-29,BUG-2004227*/
                if (ExtBuild.device() == ExtBuild.MTK_MT6755) {
                    // on mtk 6755, shutter button does not show when switch mode.
                    // root cuase is drawable alpha value is not 255,when fade in/out animation end.
                    // so update to 255,before onDraw().
                    if (drawable != null) {
                        drawable.setAlpha(255);
                    }
                }
                resetShutter(drawable);
                animator.removeListener(this);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                if (ExtBuild.device() == ExtBuild.MTK_MT6755) {
                    // on mtk 6755, shutter button does not show when switch mode.
                    // root cuase is drawable alpha value is not 255,when fade in/out animation end.
                    // so update to 255,before onDraw().
                    if (drawable != null) {
                        drawable.setAlpha(255);
                    }
                }
                /* MODIFIED-END by bin.zhang2-nb,BUG-2004227*/
                resetShutter(drawable);
                animator.removeListener(this);
                /* MODIFIED-END by sichao.hu,BUG-1027573 */
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        /* MODIFIED-BEGIN by sichao.hu, 2016-03-22, BUG-1027573 */
        mFadeComboAnimator.start();
    }

    private void resetShutter(Drawable drawable){
        super.setImageDrawable(drawable);

        setEnabled(isEnabled());//reset alpha value in TwoStateImageView
        mPendingDrawable=null;
        mPendingAlpha=0;
        mCurrentAlpha=255;
        invalidate();
    }

    private boolean mTouchConsumed=false;
    private boolean mTouchDown=false;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            mTouchConsumed=false;
            mTouchDown=true;
        }else if(event.getAction()==MotionEvent.ACTION_CANCEL||event.getAction()==MotionEvent.ACTION_UP){
            mTouchDown=false;
            if (event.getAction()==MotionEvent.ACTION_CANCEL) {
                setPressed(false);
            }
            drawableStateChanged();
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean isPressed() {
        if(!mTouchDown){
            return false;
        }
        return super.isPressed();
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        if(mCurrentDrawable!=null){
//            mCurrentDrawable.setAlpha(mCurrentAlpha);
//            mCurrentDrawable.draw(canvas);
//        }
        if(mPendingDrawable!=null){
            if(this.getDrawable()!=null) {
                this.getDrawable().setAlpha(mCurrentAlpha);
            }
            mPendingDrawable.setAlpha(mPendingAlpha);
            canvas.save();
            canvas.rotate(-mCurrentDegree);
            mPendingDrawable.setBounds(canvas.getClipBounds());
            mPendingDrawable.draw(canvas);
            canvas.restore();
        }
        super.onDraw(canvas);
    }

    private boolean mNeedSuperDraw=true;
    @Override
    protected boolean needSuperDrawable() {
        return true;
        /* MODIFIED-END by sichao.hu,BUG-1027573 */
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent m) {
        if (mTouchEnabled) {
            if (m.getActionMasked() == MotionEvent.ACTION_UP) {
                mTouchCoordinate = new TouchCoordinate(m.getX(), m.getY(), this.getMeasuredWidth(),
                        this.getMeasuredHeight());
            }
            return super.dispatchTouchEvent(m);
        } else {
            return false;
        }
    }

    public void enableTouch(boolean enable) {
        mTouchEnabled = enable;
    }


    public Runnable mLongPressRunnable=new Runnable() {
        @Override
        public void run() {
            if(!isPressed()){
                return;
            }
            synchronized (LISTENER_SYNC_LOCK){
                mTouchConsumed=true;
                for (OnShutterButtonListener listener : mListeners) {
                    listener.onShutterCoordinate(mTouchCoordinate);
                    mTouchCoordinate = null;
                    listener.onShutterButtonLongClick();
                }
            }
        }
    };

    /**
     * Hook into the drawable state changing to get changes to isPressed -- the
     * onPressed listener doesn't always get called when the pressed state
     * changes.
     */
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        final boolean pressed = isPressed();
        Log.w(TAG, "drawable state changed isPressed ? " + pressed);
        if (pressed != mOldPressed) {
            if (!pressed) {
                // When pressing the physical camera button the sequence of
                // events is:
                //    focus pressed, optional camera pressed, focus released.
                // We want to emulate this sequence of events with the shutter
                // button. When clicking using a trackball button, the view
                // system changes the drawable state before posting click
                // notification, so the sequence of events is:
                //    pressed(true), optional click, pressed(false)
                // When clicking using touch events, the view system changes the
                // drawable state after posting click notification, so the
                // sequence of events is:
                //    pressed(true), pressed(false), optional click
                // Since we're emulating the physical camera button, we want to
                // have the same order of events. So we want the optional click
                // callback to be delivered before the pressed(false) callback.
                //
                // To do this, we delay the posting of the pressed(false) event
                // slightly by pushing it on the event queue. This moves it
                // after the optional click notification, so our client always
                // sees events in this sequence:
                //     pressed(true), optional click, pressed(false)
                post(new Runnable() {
                    @Override
                    public void run() {
                        callShutterButtonFocus(pressed);
                    }
                });
            } else {
                callShutterButtonFocus(pressed);
            }
            mOldPressed = pressed;
        }
    }

    private void callShutterButtonFocus(boolean pressed) {
        this.removeCallbacks(mLongPressRunnable);
        for (OnShutterButtonListener listener : mListeners) {
            listener.onShutterButtonFocus(pressed);
        }
        if(pressed){
            this.postDelayed(mLongPressRunnable,LONGPRESS_THRESHOLD);
        }
    }

    @Override
    public boolean performClick() {
        boolean result = super.performClick();
        if (getVisibility() == View.VISIBLE&&!mTouchConsumed) {
            synchronized (LISTENER_SYNC_LOCK) {
                for (OnShutterButtonListener listener : mListeners) {
                    listener.onShutterCoordinate(mTouchCoordinate);
                    mTouchCoordinate = null;
                    listener.onShutterButtonClick();
                }
            }
        }
        return result;
    }


    private OnLongClickListener mLongClick=new OnLongClickListener(){

        @Override
        public boolean onLongClick(View v) {
            for (OnShutterButtonListener listener : mListeners) {
                listener.onShutterCoordinate(mTouchCoordinate);
                mTouchCoordinate = null;
                listener.onShutterButtonLongClick();
            }
            return false;
        }

    };
}
