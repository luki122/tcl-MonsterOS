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

package com.android.camera;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.android.camera.debug.Log;
import com.android.camera.test.TestUtils; // MODIFIED by wenhua.tu, 2016-08-11,BUG-2710178
import com.android.camera.ui.RotateImageView;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.android.camera.util.Gusterpolator;
import com.tct.camera.R;

/*
 * A toggle button that supports two or more states with images rendererd on top
 * for each state.
 * The button is initialized in an XML layout file with an array reference of
 * image ids (e.g. imageIds="@array/camera_flashmode_icons").
 * Each image in the referenced array represents a single integer state.
 * Every time the user touches the button it gets set to next state in line,
 * with the corresponding image drawn onto the face of the button.
 * State wraps back to 0 on user touch when button is already at n-1 state.
 */
public class MultiToggleImageButton extends RotateImageView{
    /*
     * Listener interface for button state changes.
     */
    public interface OnStateChangeListener {
        /*
         * @param view the MultiToggleImageButton that received the touch event
         * @param state the new state the button is in
         */
        public abstract void stateChanged(View view, int state);
    }
    /*
     * Listener interface for disabled button click.
     */
    public interface OnUnhandledClickListener {
        /*
         * @param view the MultiToggleImageButton that received the touch event
         * @param state the new state the button is in
         */
        public abstract void unhandledClick();
    }

    public interface OnTouchListener {
        public void onTouchDown();
        public void onTouchUp();
    }

    public static final int ANIM_DIRECTION_VERTICAL = 0;
    public static final int ANIM_DIRECTION_HORIZONTAL = 1;

    private static final int ANIM_DURATION_MS = 250;
    private static final int UNSET = -1;

    private OnStateChangeListener mOnStateChangeListener;
    private OnUnhandledClickListener mOnUnhandledClickListener;
    private int mState = UNSET;
    private int[] mImageIds;
    private int[] mDescIds;
    private int mLevel;
    private boolean mClickEnabled = true;
    private int mParentSize;
    private int mAnimDirection;
    private Matrix mMatrix = new Matrix();
    private ValueAnimator mAnimator;
    private final boolean isOptimizeSwitchCamera = CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_ENABLE_OPTIMIZE_SWITCH, false);
    private OnTouchListener mOnTouchListener;

    public MultiToggleImageButton(Context context) {
        super(context);
        init();
    }

    public MultiToggleImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        parseAttributes(context, attrs);
        setState(UNSET);
    }

    public MultiToggleImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
        parseAttributes(context, attrs);
        setState(UNSET);
    }

    /*
     * Set the state change listener.
     *
     * @param onStateChangeListener the listener to set
     */
    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        mOnStateChangeListener = onStateChangeListener;
    }

    public void setOnUnhandledClickListener(OnUnhandledClickListener onUnhandledClickListener) {
        mOnUnhandledClickListener = onUnhandledClickListener;
    }
    /*
     * Get the current button state.
     *
     */
    public int getState() {
        return mState;
    }

    /* MODIFIED-BEGIN by wenhua.tu, 2016-08-11,BUG-2710178*/
    public int[] getImageIds() {
        if (TestUtils.IS_TEST) {
            return mImageIds;
        }
        return null;
    }
    /* MODIFIED-END by wenhua.tu,BUG-2710178*/

    /*
     * Set the current button state, thus causing the state change listener to
     * get called.
     *
     * @param state the desired state
     */
    public void setState(int state) {
        setState(state, true);
    }

    /*
     * Set the current button state.
     *
     * @param state the desired state
     * @param callListener should the state change listener be called?
     */
    public void setState(final int state, final boolean callListener) {
        setState(state, callListener, true);
    }
    public void setState(final int state, final boolean callListener, final boolean animate) {
        setStateAnimatedInternal(state, callListener, animate);
    }
    Log.Tag TAG =new Log.Tag("MultiTogButton");
    /**
     * Set the current button state via an animated transition.
     *
     * @param state
     * @param callListener
     */
    private void setStateAnimatedInternal(final int state, final boolean callListener, final boolean animate) {
        if (state == UNSET) {
            return;
        }
        if (!animate && mAnimator != null && mAnimator.isRunning()) {
            mAnimator.end();
        }
        if (mState == state || mState == UNSET || !animate) {
            setStateInternal(state, callListener);
            return;
        }

        if (mImageIds == null) {
            return;
        }
        if (isOptimizeSwitchCamera)
            if (callListener && mOnStateChangeListener != null) {
                if(mWaitForTouchDown){
                    if(mOnTouchListener!=null){
                        mOnTouchListener.onTouchUp();
                    }
                    mWaitForTouchDown=false;
                }
                mOnStateChangeListener.stateChanged(MultiToggleImageButton.this, state);
            }

        new AsyncTask<Integer, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Integer... params) {
                return combine(params[0], params[1]);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap == null) {
                    // If combine bitmap is null, check whether the images are set via
                    // overrideImageIds() again.
                    if (mImageIds != null && state < mImageIds.length) {
                        setStateInternal(state, callListener);
                    }
                } else {
                    setImageBitmap(bitmap);

                    Log.w(TAG,"Bitmap width is "+bitmap.getWidth()+" bitmap height is "+bitmap.getHeight());
                    int offset;

//                    if (mAnimDirection == ANIM_DIRECTION_VERTICAL) {
//                        mMatrix.setTranslate(0.0f, (Float) animation.getAnimatedValue());
//                    } else if (mAnimDirection == ANIM_DIRECTION_HORIZONTAL) {
//                        mMatrix.setTranslate((Float) animation.getAnimatedValue(), 0.0f);
//                    }

                    int rotation=(-mOrientation);
                    if (mAnimDirection == ANIM_DIRECTION_VERTICAL) {
                        offset = (mParentSize+getHeight())/2;
                        mAnimator.setFloatValues(-offset, 0.0f);
                    } else if (mAnimDirection == ANIM_DIRECTION_HORIZONTAL) {
                        offset = (mParentSize+getWidth())/2;
                        if((rotation+360)%360!=270){
                            mAnimator.setFloatValues(0.0f,-offset);
                        }else{
                            mAnimator.setFloatValues(-offset,0.0f);
                        }
                    } else {
                        return;
                    }

                    Log.w(TAG,"offset is "+offset);

//                    mAnimator.setFloatValues(-offset, 0.0f);
                    AnimatorSet s = new AnimatorSet();
                    s.play(mAnimator);
                    s.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            mNeedSuperDraw = false;
                            setClickEnabled(false);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            setStateInternal(state, callListener); // remain internal state change after animation end , to make sure the image resource wouldn't change during combining task running
                            setClickEnabled(true);
                            mNeedSuperDraw=true;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            if(mWaitForTouchDown){
                                if(mOnTouchListener!=null){
                                    mOnTouchListener.onTouchUp();
                                }
                            }
                            super.onAnimationCancel(animation);
                            mNeedSuperDraw=true;
                        }
                    });
                    s.start();
                }
            }
        }.execute(mState, state);
        if (isOptimizeSwitchCamera) {
            mState = state;
        }
    }

    /**
     * Enable or disable click reactions for this button
     * without affecting visual state.
     * For most cases you'll want to use {@link #setEnabled(boolean)}.
     * @param enabled True if click enabled, false otherwise.
     */
    public void setClickEnabled(boolean enabled) {
        mClickEnabled = enabled;
    }

    private void setStateInternal(int state, boolean callListener) {
        mState = state;
        if (mImageIds != null) {
            setImageByState(mState);
        }

        if (mDescIds != null) {
            String oldContentDescription = String.valueOf(getContentDescription());
            String newContentDescription = getResources().getString(mDescIds[mState]);
            if (oldContentDescription != null && !oldContentDescription.isEmpty()
                    && !oldContentDescription.equals(newContentDescription)) {
                setContentDescription(newContentDescription);
                String announceChange = getResources().getString(
                    R.string.button_change_announcement, newContentDescription);
                announceForAccessibility(announceChange);
            }
        }
        super.setImageLevel(mLevel);
        if (!isOptimizeSwitchCamera) {
            mState = state;
            if (callListener && mOnStateChangeListener != null) {
                if(mWaitForTouchDown){
                    if(mOnTouchListener!=null){
                        mOnTouchListener.onTouchUp();
                    }
                    mWaitForTouchDown=false;
                }
                mOnStateChangeListener.stateChanged(MultiToggleImageButton.this, getState());
            }
        }
    }

    private void nextState() {
        int state = mState + 1;
        if (state >= mImageIds.length) {
            state = 0;
        }
        setState(state);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isLocked()) {
            return super.onTouchEvent(event);
        }
        if (isEnabled()/* || getId() != R.id.flash_toggle_button*/) {
            final int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
//                    if(mOnTouchListener!=null){
//                        mOnTouchListener.onTouchDown();
//                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
//                    if (mOnTouchListener != null) {
//                        mOnTouchListener.onTouchUp();
//                    }
                    break;
            }
            return super.onTouchEvent(event);
        } else {
            final int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_UP:
                    if (mOnUnhandledClickListener != null) {
                        mOnUnhandledClickListener.unhandledClick();
                    }
                    break;
            }
            return true;
        }
    }

    public void setOnTouchListener(OnTouchListener listener){
        mOnTouchListener =listener;
    }


    private boolean mWaitForTouchDown=false;

    protected void init() {
        this.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickEnabled) {
                    if(mOnTouchListener!=null) {
                        mOnTouchListener.onTouchDown();
                    }
                    mWaitForTouchDown=true;
                    nextState();
                }
            }
        });
        setScaleType(ImageView.ScaleType.MATRIX);

        mAnimator = ValueAnimator.ofFloat(0.0f, 0.0f);
        mAnimator.setDuration(ANIM_DURATION_MS);
        mAnimator.setInterpolator(Gusterpolator.INSTANCE);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mMatrix.reset();
                if (mAnimDirection == ANIM_DIRECTION_VERTICAL) {
                    mMatrix.setTranslate(0.0f, (Float) animation.getAnimatedValue());
                } else if (mAnimDirection == ANIM_DIRECTION_HORIZONTAL) {
                    mMatrix.setTranslate((Float) animation.getAnimatedValue(), 0.0f);
                }

                setImageMatrix(mMatrix);
                invalidate();
            }
        });
    }

    private boolean mNeedSuperDraw=true;
    @Override
    protected boolean needSuperDrawable() {
        return mNeedSuperDraw;
    }

    private void parseAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
            attrs,
            R.styleable.MultiToggleImageButton,
            0, 0);
        int imageIds = a.getResourceId(R.styleable.MultiToggleImageButton_imageIds, 0);
        if (imageIds > 0) {
            overrideImageIds(imageIds);
        }
        int descIds = a.getResourceId(R.styleable.MultiToggleImageButton_contentDescriptionIds, 0);
        if (descIds > 0) {
            overrideContentDescriptions(descIds);
        }
        a.recycle();
    }

    /**
     * Override the image ids of this button.
     */
    public void overrideImageIds(int resId) {
        TypedArray ids = null;
        try {
            ids = getResources().obtainTypedArray(resId);
            int[] tmpImageIds = new int[ids.length()];
            for (int i = 0; i < ids.length(); i++) {
                tmpImageIds[i] = ids.getResourceId(i, 0);
            }
            mImageIds = tmpImageIds;
        } finally {
            if (ids != null) {
                ids.recycle();
            }
        }

        if (mState >= 0 && mState < mImageIds.length) {
            setImageByState(mState);
        }
    }

    /**
     * Override the content descriptions of this button.
     */
    public void overrideContentDescriptions(int resId) {
        TypedArray ids = null;
        try {
            ids = getResources().obtainTypedArray(resId);
            mDescIds = new int[ids.length()];
            for (int i = 0; i < ids.length(); i++) {
                mDescIds[i] = ids.getResourceId(i, 0);
            }
        } finally {
            if (ids != null) {
                ids.recycle();
            }
        }
    }

    /**
     * Set size info (either width or height, as necessary) of the view containing
     * this button. Used for offset calculations during animation.
     * @param s The size.
     */
    public void setParentSize(int s) {
        mParentSize = s;
    }

    /**
     * Set the animation direction.
     * @param d Either ANIM_DIRECTION_VERTICAL or ANIM_DIRECTION_HORIZONTAL.
     */
    public void setAnimDirection(int d) {
        mAnimDirection = d;
    }

    @Override
    public void setImageLevel(int level) {
        super.setImageLevel(level);
        mLevel = level;
    }

    private void setImageByState(int state) {
        if (mImageIds != null && state >= 0 && state < mImageIds.length) {
            setImageResource(mImageIds[state]);
        }
        super.setImageLevel(mLevel);
    }


    private int mOrientation=0;
    @Override
    public void setOrientation(int degree, boolean animation) {
        super.setOrientation(degree, animation);
        if(degree%180==0){
            setAnimDirection(ANIM_DIRECTION_VERTICAL);
        }else{
            setAnimDirection(ANIM_DIRECTION_HORIZONTAL);
        }
        mOrientation=degree;
    }

    private Bitmap combine(int oldState, int newState) {
        // in some cases, a new set of image Ids are set via overrideImageIds()
        // and oldState overruns the array.
        // check here for that.
        if (oldState >= mImageIds.length) {
            return null;
        }

        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) {
            return null;
        }

        int[] enabledState = new int[] {android.R.attr.state_enabled};

        // new state
        Drawable newDrawable = getResources().getDrawable(mImageIds[newState]).mutate();
        newDrawable.setState(enabledState);

        // old state
        Drawable oldDrawable = getResources().getDrawable(mImageIds[oldState]).mutate();
        oldDrawable.setState(enabledState);

        // combine 'em
        Bitmap bitmap = null;

        int bitmapHeight = (height*2) + ((mParentSize - height)/2);
        int oldBitmapOffset = height + ((mParentSize - height)/2);

        bitmap = Bitmap.createBitmap(width, bitmapHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        int newLeft = (width - newDrawable.getIntrinsicWidth())/2;
        int newTop = (height - newDrawable.getIntrinsicHeight())/2;
        newDrawable.setBounds(newLeft, newTop,
                newLeft + newDrawable.getIntrinsicWidth(), newTop + newDrawable.getIntrinsicHeight());

        int oldLeft = (width-oldDrawable.getIntrinsicWidth())/2;
        int oldTop = (height - oldDrawable.getIntrinsicHeight())/2;
        oldDrawable.setBounds(oldLeft, oldBitmapOffset + oldTop,
                oldLeft + oldDrawable.getIntrinsicWidth(), oldBitmapOffset + oldTop + oldDrawable.getIntrinsicHeight());

        newDrawable.draw(canvas);
        oldDrawable.draw(canvas);

        if (mAnimDirection == ANIM_DIRECTION_VERTICAL) {

        } else {
            int rotation = -mOrientation;
            Log.w(TAG, "rotate orientation is" + (-mOrientation));
            Matrix matrix=new Matrix();
            matrix.setRotate(rotation,0,0);

            Bitmap bitmapToRecycle=bitmap;

            bitmap=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);

            bitmapToRecycle.recycle();
        }

        return bitmap;
    }

}
