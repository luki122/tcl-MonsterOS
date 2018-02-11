package com.android.camera.widget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.DimenRes;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import com.android.camera.test.TestUtils; // MODIFIED by wenhua.tu, 2016-08-11,BUG-2710178
import com.android.camera.ui.RotateImageView;
import com.tct.camera.R;

public class SoundAction extends ViewGroup {
    private static final int ANIMATION_DURATION = 300;
    private AnimatorSet mExpandAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
    private AnimatorSet mCollapseAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);

    private int mButtonSpacing;
    private boolean mExpanded;
    private boolean isPortrait = false;
    private RotateImageView mAddButton;

    public SoundAction(Context context, AttributeSet attrs) {
        super(context, attrs);
        mButtonSpacing = getDimension(R.dimen.sound_icon_spacing);
        createAddButton(context);
    }

    private void createAddButton(Context context) {
        mAddButton = new RotateImageView(context);
        mAddButton.setImageResource(R.drawable.ic_kid_show);
        ViewGroup.LayoutParams ps = new ViewGroup.LayoutParams(getDimension(R.dimen.sound_icon_width), getDimension(R.dimen.sound_icon_height));
        mAddButton.setLayoutParams(ps);

        mAddButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });

        addView(mAddButton, ps);
    }

    int getDimension(@DimenRes int id) {
        return (int) getResources().getDimension(id);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int width = 0;
        int height = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);

            height = Math.max(height, child.getMeasuredHeight());
            width += child.getMeasuredWidth();
        }

        width += mButtonSpacing * (getChildCount() - 1);
        width = width * 12 / 10;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int addButtonWidth = mAddButton.getMeasuredWidth();
        int addButtonHeight = mAddButton.getMeasuredHeight();
        int marginRight = getDimension(R.dimen.sound_icon_margin_right);
        int addButtonX = r + marginRight;
        int addButtonY = b - t - addButtonHeight;
        int bottomX = addButtonX - addButtonWidth;
        int bottomY = addButtonY + addButtonHeight;
        mAddButton.layout(addButtonX - addButtonWidth, addButtonY, addButtonX, bottomY);
        bottomX -= mButtonSpacing;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            final View child = getChildAt(i);

            if (child == mAddButton)
                continue;

            int childX = bottomX;
            child.layout(childX - child.getMeasuredWidth(), addButtonY, childX, bottomY);
            float collapsedTranslation = addButtonX - childX;
            float expandedTranslation = 0f;

            child.setTranslationX(mExpanded ? expandedTranslation : collapsedTranslation);
            child.setAlpha(mExpanded ? 1f : 0f);

            LayoutParams params = (LayoutParams) child.getLayoutParams();
            params.mCollapseX.setFloatValues(expandedTranslation, collapsedTranslation);
            params.mExpandX.setFloatValues(collapsedTranslation, expandedTranslation);
            params.setAnimationsTarget(child);

            bottomX = childX - mButtonSpacing - child.getMeasuredHeight();
        }

    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.RIGHT;
        return new LayoutParams(super.generateLayoutParams(params));
    }

    private class LayoutParams extends ViewGroup.LayoutParams {

        private ObjectAnimator mExpandY = new ObjectAnimator();
        private ObjectAnimator mExpandX = new ObjectAnimator();
        private ObjectAnimator mExpandAlpha = new ObjectAnimator();
        private ObjectAnimator mCollapseY = new ObjectAnimator();
        private ObjectAnimator mCollapseX = new ObjectAnimator();
        private ObjectAnimator mCollapseAlpha = new ObjectAnimator();

        private Interpolator sExpandInterpolator = new OvershootInterpolator();
        private Interpolator sCollapseInterpolator = new DecelerateInterpolator(3f);
        private Interpolator sAlphaExpandInterpolator = new DecelerateInterpolator();

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);

            mExpandY.setInterpolator(sExpandInterpolator);
            mExpandX.setInterpolator(sExpandInterpolator);
            mExpandAlpha.setInterpolator(sAlphaExpandInterpolator);
            mCollapseY.setInterpolator(sCollapseInterpolator);
            mCollapseX.setInterpolator(sCollapseInterpolator);
            mCollapseAlpha.setInterpolator(sCollapseInterpolator);

            mCollapseAlpha.setProperty(View.ALPHA);
            mCollapseAlpha.setFloatValues(1f, 0f);

            mExpandAlpha.setProperty(View.ALPHA);
            mExpandAlpha.setFloatValues(0f, 1f);

            mCollapseY.setProperty(View.TRANSLATION_Y);
            mCollapseX.setProperty(View.TRANSLATION_X);
            mExpandY.setProperty(View.TRANSLATION_Y);
            mExpandX.setProperty(View.TRANSLATION_X);
            mExpandAnimation.play(mExpandAlpha);
            if (isPortrait)
                mExpandAnimation.play(mExpandY);
            else
                mExpandAnimation.play(mExpandX);

            mCollapseAnimation.play(mCollapseAlpha);
            if (isPortrait)
                mCollapseAnimation.play(mCollapseY);
            else
                mCollapseAnimation.play(mCollapseX);
        }

        public void setAnimationsTarget(View view) {
            mCollapseAlpha.setTarget(view);
            mCollapseY.setTarget(view);
            mCollapseX.setTarget(view);
            mExpandAlpha.setTarget(view);
            mExpandY.setTarget(view);
            mExpandX.setTarget(view);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        bringChildToFront(mAddButton);
    }

    public void collapse() {
        if (mExpanded) {
            mExpanded = false;
            mAddButton.setImageResource(R.drawable.ic_kid_show);
            mCollapseAnimation.start();
            mExpandAnimation.cancel();
        }
    }


    private void toggle() {
        if (mExpanded) {
            collapse();
        } else {
            this.post(new Runnable() {
                @Override
                public void run() {
                    expand();
                }
            });
        }
    }

    private void expand() {
        if (!mExpanded) {
            mExpanded = true;
            mAddButton.setImageResource(R.drawable.ic_kid_close);
            mCollapseAnimation.cancel();
            mExpandAnimation.start();
        }
    }

    /* MODIFIED-BEGIN by wenhua.tu, 2016-08-11,BUG-2710178*/
    public boolean isExpand() {
        if (TestUtils.IS_TEST) {
            return mExpanded;
        }

        return false;
    }
    /* MODIFIED-END by wenhua.tu,BUG-2710178*/
}
