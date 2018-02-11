package com.android.camera.widget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;

import com.android.camera.ui.Lockable;
import com.android.camera.ui.ManualItem;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.android.camera.util.LockUtils;
import com.tct.camera.R;

public class FloatingActionsMenu extends ViewGroup implements Lockable {
    private static final int ANIMATION_DURATION = 300;
    private static final float COLLAPSED_PLUS_ROTATION = 180f;
    private static final float EXPANDED_PLUS_ROTATION = 0f;

    private int mAddButtonPlusColor;
    private int mAddButtonColorNormal;
    private int mAddButtonColorPressed;

    private int mButtonSpacing;

    public boolean mExpanded = true;
    private boolean mLayoutDone = false;

    private boolean isPortrait = true;
    private AnimatorSet mExpandAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
    private AnimatorSet mCollapseAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
    public AddFloatingActionButton mAddButton;
    private LinearLayout mAddButtonLayout;
    private RotatingDrawable mRotatingDrawable;
    private int mIconId = R.drawable.ic_manual_settings;
    private ManualItem mItemISO;
    private ManualItem mItemS;
    private ManualItem mItemWb;
    private ManualItem mItemF;

    private ValueAnimator.AnimatorUpdateListener animatorUpdateListener;
    private boolean mIsFirstUseManual = false;

    public void toggleForTutorial() {
        if(mIsFirstUseManual){
            if(mMenuExpandChangeListener != null && mExpanded){
                mMenuExpandChangeListener.onManualMenuExpandChanged(mExpanded);
            }
        }
    }

    public void manualAddButtonClick() {
        if(mAddButton != null){
            mAddButton.performClick();
        }
    }

    public interface ManualMenuExpandChangeListener {
        public void onManualMenuExpandChanged(boolean expand);
        public void onManualMenuClick();
    }

    private ManualMenuExpandChangeListener mMenuExpandChangeListener;
    public FloatingActionsMenu(Context context) {
        this(context, null);
    }

    public FloatingActionsMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMultiLock= LockUtils.getInstance().generateMultiLock(LockUtils.LockType.MULTILOCK);
        init(context, attrs);
    }

    public FloatingActionsMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mMultiLock= LockUtils.getInstance().generateMultiLock(LockUtils.LockType.MULTILOCK);
        init(context, attrs);
    }

    public void setMenuExpandChangeListener(ManualMenuExpandChangeListener listener, boolean isFirstUseManual) {
        mMenuExpandChangeListener = listener;
        mIsFirstUseManual = isFirstUseManual;
    }

    private void init(Context context, AttributeSet attributeSet) {
        mAddButtonPlusColor = getColor(android.R.color.white);
        mAddButtonColorNormal = getColor(android.R.color.holo_blue_dark);
        mAddButtonColorPressed = getColor(android.R.color.holo_blue_light);
        mButtonSpacing = (int) (getResources().getDimension(R.dimen.fab_actions_spacing)
                - getResources().getDimension(R.dimen.fab_shadow_radius) - getResources()
                .getDimension(R.dimen.fab_shadow_offset));

        if (attributeSet != null) {
            TypedArray attr = context.obtainStyledAttributes(attributeSet,
                    R.styleable.FloatingActionsMenu, 0, 0);
            if (attr != null) {
                try {
                    mAddButtonPlusColor = attr.getColor(
                            R.styleable.FloatingActionsMenu_addButtonPlusIconColor,
                            getColor(android.R.color.white));
                    mAddButtonColorNormal = attr.getColor(
                            R.styleable.FloatingActionsMenu_addButtonColorNormal,
                            getColor(android.R.color.holo_blue_dark));
                    mAddButtonColorPressed = attr.getColor(
                            R.styleable.FloatingActionsMenu_addButtonColorPressed,
                            getColor(android.R.color.holo_blue_light));
                } finally {
                    attr.recycle();
                }
            }
        }

        createAddButton(context);
    }

    private static class RotatingDrawable extends LayerDrawable {
        public RotatingDrawable(Drawable drawable) {
            super(new Drawable[] {
                drawable
            });
        }

        private float mRotation = COLLAPSED_PLUS_ROTATION;

        @SuppressWarnings("UnusedDeclaration")
        public float getRotation() {
            return mRotation;
        }

        @SuppressWarnings("UnusedDeclaration")
        public void setRotation(float rotation) {
            mRotation = rotation;
            invalidateSelf();
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.save();
            canvas.rotate(mRotation, getBounds().centerX(), getBounds().centerY());
            super.draw(canvas);
            canvas.restore();
        }
    }

    private void createAddButton(Context context) {
        mAddButton = new AddFloatingActionButton(context) {
            @Override
            void updateBackground() {
                mPlusColor = mAddButtonPlusColor;
                mColorNormal = mAddButtonColorNormal;
                mColorPressed = mAddButtonColorPressed;
                super.updateBackground();
            }

            @Override
            Drawable getIconDrawable() {
                final RotatingDrawable rotatingDrawable = new RotatingDrawable(getResources()
                        .getDrawable(mIconId));
                mRotatingDrawable = rotatingDrawable;

                final OvershootInterpolator interpolator = new OvershootInterpolator();
                final ObjectAnimator collapseAnimator;
                final ObjectAnimator expandAnimator;
                if (mMenuOnTop) {
                    collapseAnimator = ObjectAnimator.ofFloat(rotatingDrawable,
                            "rotation", EXPANDED_PLUS_ROTATION, COLLAPSED_PLUS_ROTATION);
                    expandAnimator = ObjectAnimator.ofFloat(rotatingDrawable,
                            "rotation", COLLAPSED_PLUS_ROTATION, EXPANDED_PLUS_ROTATION);
                } else {
                    collapseAnimator = ObjectAnimator.ofFloat(rotatingDrawable,
                            "rotation", COLLAPSED_PLUS_ROTATION, EXPANDED_PLUS_ROTATION);
                    expandAnimator = ObjectAnimator.ofFloat(rotatingDrawable,
                            "rotation", EXPANDED_PLUS_ROTATION, COLLAPSED_PLUS_ROTATION);
                }


                collapseAnimator.setInterpolator(interpolator);
                expandAnimator.setInterpolator(interpolator);

                mExpandAnimation.play(expandAnimator);
                mCollapseAnimation.play(collapseAnimator);

                return rotatingDrawable;
            }
        };

        // mAddButton.setId(R.id.fab_expand_menu_button);
        mAddButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int manualItemWidth = getDimension(R.dimen.manual_item_height);
        int addButtonWidth = getDimension(R.dimen.fab_icon_size);
        lp.setMarginEnd(manualItemWidth/2 - addButtonWidth/2);
        mAddButton.setLayoutParams(lp);

        mAddButtonLayout = new LinearLayout(context);
        mAddButtonLayout.setGravity(Gravity.END);
        mAddButtonLayout.addView(mAddButton);

        addView(mAddButtonLayout, super.generateDefaultLayoutParams());
    }

    int getDimension(@DimenRes
    int id) {
        return (int) getResources().getDimension(id);
    }

    private int getColor(@ColorRes
    int id) {
        return getResources().getColor(id);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    private boolean mMenuOnTop = false;
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childrenTotalHeight = 0;
        int childrenCount=this.getChildCount();
        for (int i = childrenCount - 1; i >= 0; i--) {
            childrenTotalHeight += getChildAt(i).getMeasuredHeight();
        }
        mButtonSpacing = getDimension(R.dimen.fab_icon_margin);
        int menuSpacing = (getMeasuredHeight() - childrenTotalHeight - mButtonSpacing * 3) / 2;
        if (menuSpacing <= 0) {
            mButtonSpacing = (getMeasuredHeight() - childrenTotalHeight) / 4;
            menuSpacing = mButtonSpacing / 2;
        }
        int addButtonHeight = mAddButton.getMeasuredHeight();

        int addButtonY = 0;

        int bottomX = r - l;
        int bottomY = addButtonY + addButtonHeight;
        if (mMenuOnTop) {
            mAddButtonLayout.layout(0, 0, bottomX, addButtonHeight);
        } else {
            bottomY = 0;
            addButtonY = getMeasuredHeight() - addButtonHeight;
            mAddButtonLayout.layout(0, addButtonY, bottomX, getMeasuredHeight());
        }

        bottomY += menuSpacing;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            final View child = getChildAt(i);

            if (child == mAddButtonLayout)
                continue;

            int childX = 0;
            int childY = bottomY;
            child.layout(childX, childY, bottomX, childY + child.getMeasuredHeight());
            float collapsedTranslation = addButtonY - childY;
            float expandedTranslation = 0f;

            child.setTranslationY(mExpanded ? expandedTranslation : collapsedTranslation);
            child.setAlpha(mExpanded ? 1f : 0f);

            LayoutParams params = (LayoutParams) child.getLayoutParams();
            params.mCollapseY.setFloatValues(expandedTranslation, collapsedTranslation);
            params.mExpandY.setFloatValues(collapsedTranslation, expandedTranslation);
            params.setAnimationsTarget(child);

            bottomY = childY + mButtonSpacing + child.getMeasuredHeight();
        }
        mLayoutDone = true;
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(super.generateDefaultLayoutParams());
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(super.generateLayoutParams(attrs));
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(super.generateLayoutParams(p));
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return super.checkLayoutParams(p);
    }

    private static Interpolator sExpandInterpolator = new OvershootInterpolator();
    private static Interpolator sCollapseInterpolator = new DecelerateInterpolator(3f);
    private static Interpolator sAlphaExpandInterpolator = new DecelerateInterpolator();

    private class LayoutParams extends ViewGroup.LayoutParams {

        private ObjectAnimator mExpandY = new ObjectAnimator();
        private ObjectAnimator mExpandX = new ObjectAnimator();
        private ObjectAnimator mExpandAlpha = new ObjectAnimator();
        private ObjectAnimator mCollapseY = new ObjectAnimator();
        private ObjectAnimator mCollapseX = new ObjectAnimator();
        private ObjectAnimator mCollapseAlpha = new ObjectAnimator();

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
        bringChildToFront(mAddButtonLayout);
        mItemISO = (ManualItem) findViewById(R.id.item_iso);
        mItemS = (ManualItem) findViewById(R.id.item_s);
        mItemWb = (ManualItem) findViewById(R.id.item_wb);
        mItemF = (ManualItem) findViewById(R.id.item_f);
    }

    public void collapse() {
        if (mExpanded && mLayoutDone) {
            mExpanded = false;
            mItemISO.resetView();
            mItemS.resetView();
            mItemWb.resetView();
            mItemF.resetView();
            mCollapseAnimation.start();
            mExpandAnimation.cancel();
//            setIcon(R.drawable.ic_manualsettings_hide);
        }
    }

    public void setIcon(int icon){
        this.mIconId = icon;
        mAddButton.updateBackground();
    }


    public void toggle() {
        if (mExpanded) {
            collapse();
            if(mIsFirstUseManual && mMenuExpandChangeListener != null){
                mMenuExpandChangeListener.onManualMenuClick();
            }
        } else {
//            setIcon(R.drawable.ic_manualsettings_show);
            this.post(new Runnable() {
                @Override
                public void run() {
                    expand();
                }
            });
        }
    }

    public void expand() {
        if (!mExpanded) {
            mExpanded = true;
            mCollapseAnimation.cancel();
            mExpandAnimation.start();
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.mExpanded = mExpanded;

        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            mExpanded = savedState.mExpanded;

            if (mRotatingDrawable != null) {
                mRotatingDrawable.setRotation(mExpanded ? EXPANDED_PLUS_ROTATION
                        : COLLAPSED_PLUS_ROTATION);
            }

            super.onRestoreInstanceState(savedState.getSuperState());
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    public static class SavedState extends BaseSavedState {
        public boolean mExpanded;

        public SavedState(Parcelable parcel) {
            super(parcel);
        }

        private SavedState(Parcel in) {
            super(in);
            mExpanded = in.readInt() == 1;
        }

        @Override
        public void writeToParcel(@NonNull
        Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mExpanded ? 1 : 0);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(isLocked()){
            return true;
        }
        return super.onTouchEvent(event);
    }

    private LockUtils.Lock mMultiLock;
    @Override
    public void lockSelf() {
        mMultiLock.aquireLock(this.hashCode());
    }


    @Override
    public int lock() {
        return mMultiLock.aquireLock();
    }

    @Override
    public boolean unlockWithToken(int token) {
        return mMultiLock.unlockWithToken(token);
    }

    @Override
    public void unLockSelf() {
        mMultiLock.unlockWithToken(this.hashCode());
    }

    @Override
    public boolean isLocked() {
        return mMultiLock.isLocked();
    }
/* MODIFIED-BEGIN by wenhua.tu, 2016-08-11,BUG-2710178*/

    public boolean isExpanded() {
        return mExpanded;
    }
    /* MODIFIED-END by wenhua.tu,BUG-2710178*/
}
