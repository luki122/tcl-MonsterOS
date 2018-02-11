/*
 * Copyright (C) 2010 The Android Open Source Project
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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.FloatArrayEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.monster.launcher.util.Thunk;

/**
 * Implements a DropTarget.
 */
public abstract class ButtonDropTarget extends TextView
        implements DropTarget, DragController.DragListener, OnClickListener {

    protected static int DRAG_VIEW_DROP_DURATION = 285;

    protected Launcher mLauncher;
    private int mBottomDragPadding;
    protected SearchDropTargetBar mSearchDropTargetBar;

    /** Whether this drop target is active for the current drag */
    protected boolean mActive;

    /** The paint applied to the drag view on hover */
    protected int mHoverColor = 0;

    protected ColorStateList mOriginalTextColor;
    protected Drawable mDrawable;

    private AnimatorSet mCurrentColorAnim;
    @Thunk
    ColorMatrix mSrcFilter, mDstFilter, mCurrentFilter;
    //M:liuzuo begin
    float mIconLeft;
    float mIconRight;
    //M:liuzuo end
    public ButtonDropTarget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ButtonDropTarget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mBottomDragPadding = getResources().getDimensionPixelSize(R.dimen.drop_target_drag_padding);
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mOriginalTextColor = getTextColors();

        // Remove the text in the Phone UI in landscape
        DeviceProfile grid = ((Launcher) getContext()).getDeviceProfile();
        if (grid.isVerticalBarLayout()) {
            setText("");
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    protected void setDrawable(int resId) {
        // We do not set the drawable in the xml as that inflates two drawables corresponding to
        // drawableLeft and drawableStart.
        mDrawable = getResources().getDrawable(resId);
        int color =LauncherAppState.getInstance().getWindowGlobalVaule().getTextColor();
        setTextColor(color);
        if(color!=-1){
            mDrawable.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        }else{
            mDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }
        if (Utilities.ATLEAST_JB_MR1) {
            setCompoundDrawablesRelativeWithIntrinsicBounds(mDrawable, null, null, null);
        } else {
            setCompoundDrawablesWithIntrinsicBounds(mDrawable, null, null, null);
        }

    }

    public void setLauncher(Launcher launcher) {
        mLauncher = launcher;
    }

    public void setSearchDropTargetBar(SearchDropTargetBar searchDropTargetBar) {
        mSearchDropTargetBar = searchDropTargetBar;
    }

    @Override
    public void onFlingToDelete(DragObject d, PointF vec) { }

    @Override
    public final void onDragEnter(DragObject d) {
        d.dragView.setColor(mHoverColor);
        if (Utilities.ATLEAST_LOLLIPOP) {
            animateTextColor(mHoverColor);
        } else {
            if (mCurrentFilter == null) {
                mCurrentFilter = new ColorMatrix();
            }
            DragView.setColorScale(mHoverColor, mCurrentFilter);
            mDrawable.setColorFilter(new ColorMatrixColorFilter(mCurrentFilter));
            setTextColor(mHoverColor);
        }
//M:liuzuo begin
        float from=getMaxRadius();
        float to =getMinRadius();
        if(from<to){
            initAnimation(from,to,true);
        }else {
            initAnimation(to,from,true);
        }
        changeImage();
//M:liuzuo end
    }


    @Override
    public void onDragOver(DragObject d) {
        // Do nothing
    }

    protected void resetHoverColor() {
        if (Utilities.ATLEAST_LOLLIPOP) {
            animateTextColor(mOriginalTextColor.getDefaultColor());
        } else {
                //mDrawable.setColorFilter(null);
                //setTextColor(mOriginalTextColor);

        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animateTextColor(int targetColor) {
        if (mCurrentColorAnim != null) {
            mCurrentColorAnim.cancel();
        }

        mCurrentColorAnim = new AnimatorSet();
        mCurrentColorAnim.setDuration(DragView.COLOR_CHANGE_DURATION);

        if (mSrcFilter == null) {
            mSrcFilter = new ColorMatrix();
            mDstFilter = new ColorMatrix();
            mCurrentFilter = new ColorMatrix();
        }

        DragView.setColorScale(getTextColor(), mSrcFilter);
        DragView.setColorScale(targetColor, mDstFilter);
        ValueAnimator anim1 = ValueAnimator.ofObject(
                new FloatArrayEvaluator(mCurrentFilter.getArray()),
                mSrcFilter.getArray(), mDstFilter.getArray());
        anim1.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDrawable.setColorFilter(new ColorMatrixColorFilter(mCurrentFilter));
                invalidate();
            }
        });

        mCurrentColorAnim.play(anim1);
        mCurrentColorAnim.play(ObjectAnimator.ofArgb(this, "textColor", targetColor));
      //  mCurrentColorAnim.start();   //liuzuo close the color change of text
    }

    @Override
    public final void onDragExit(DragObject d) {
        if (!d.dragComplete) {
            d.dragView.setColor(0);
            resetHoverColor();
        } else {
            // Restore the hover color
            d.dragView.setColor(mHoverColor);
            return;
        }
//M:liuzuo begin
        float from=getMaxRadius();
        float to =getMinRadius();
        if(from<to){
            initAnimation(to,from,false);
        }else {
            initAnimation(from,to,false);
        }
        //resetImage();
//M:liuzuo end
    }

	@Override
    public final void onDragStart(DragSource source, Object info, int dragAction) {
        mActive = supportsDrop(source, info);
        mDrawable.setColorFilter(null);
        if (mCurrentColorAnim != null) {
            mCurrentColorAnim.cancel();
            mCurrentColorAnim = null;
        }
        //M: liuzuo add
        resetImage();
        setAniAlpha(0);
        int color =LauncherAppState.getInstance().getWindowGlobalVaule().getTextColor();
        setTextColor(color);
        boolean isBlackText = LauncherAppState.getInstance().getWindowGlobalVaule().isBlackText();
        if(isBlackText){
            mDrawable.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        }else{
            mDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }

          //  setTextColor(mOriginalTextColor);

        ((ViewGroup) getParent()).setVisibility(mActive ? View.VISIBLE : View.GONE);
    }

    @Override
    public final boolean acceptDrop(DragObject dragObject) {
        return supportsDrop(dragObject.dragSource, dragObject.dragInfo);
    }

    protected abstract boolean supportsDrop(DragSource source, Object info);

    @Override
    public boolean isDropEnabled() {
        return mActive;
    }

    @Override
    public void onDragEnd() {
        mActive = false;
    }

    /**
     * On drop animate the dropView to the icon.
     */
    @Override
    public void onDrop(final DragObject d) {
        final DragLayer dragLayer = mLauncher.getDragLayer();
        final Rect from = new Rect();
        dragLayer.getViewRectRelativeToSelf(d.dragView, from);

        int width = mDrawable.getIntrinsicWidth();
        int height = mDrawable.getIntrinsicHeight();
        final Rect to = getIconRect(d.dragView.getMeasuredWidth(), d.dragView.getMeasuredHeight(),
                width, height);
        final float scale = (float) to.width() / from.width();
        mSearchDropTargetBar.deferOnDragEnd();

        Runnable onAnimationEndRunnable = new Runnable() {
            @Override
            public void run() {
                completeDrop(d);
                mSearchDropTargetBar.onDragEnd();
                //modify by xiangzx
                exitDragMode();
            }
        };
        dragLayer.animateView(d.dragView, from, to, scale, 1f, 1f, 0.1f, 0.1f,//liuzuo to >> from
                DRAG_VIEW_DROP_DURATION, new DecelerateInterpolator(2),
                new LinearInterpolator(), onAnimationEndRunnable,
                DragLayer.ANIMATION_END_DISAPPEAR, null);
    }

    //add by xiangzx
    protected void exitDragMode(){
        mLauncher.exitSpringLoadedDragModeDelayed(true, 0, null);
    }

    @Override
    public void prepareAccessibilityDrop() { }

    @Thunk abstract void completeDrop(DragObject d);

    @Override
    public void getHitRectRelativeToDragLayer(android.graphics.Rect outRect) {
        super.getHitRect(outRect);
        outRect.bottom += mBottomDragPadding;

        int[] coords = new int[2];
        mLauncher.getDragLayer().getDescendantCoordRelativeToSelf(this, coords);
        outRect.offsetTo(coords[0], coords[1]);
    }

    protected Rect getIconRect(int viewWidth, int viewHeight, int drawableWidth, int drawableHeight) {
        DragLayer dragLayer = mLauncher.getDragLayer();

        // Find the rect to animate to (the view is center aligned)
        Rect to = new Rect();
        dragLayer.getViewRectRelativeToSelf(this, to);

        final int width = drawableWidth;
        final int height = drawableHeight;

        final int left;
        final int right;

        if (Utilities.isRtl(getResources())) {
            //M: liuzuo change rect of  destination begin
           /* right = to.right - getPaddingRight()+getWidth();
            left = right - width;*/
            right= (int) mIconRight +to.left;
            left= (int) mIconLeft +to.left;
        } else {
           /* left = to.left + getPaddingLeft();
            right = left + width;*/
            right= (int) mIconRight +to.left;
            left= (int) mIconLeft +to.left;
            //M: liuzuo change rect of  destination end
        }

        final int top = to.top + (getMeasuredHeight() - height) / 2;
        final int bottom = top +  height;

        to.set(left, top, right, bottom);

        // Center the destination rect about the trash icon
        final int xOffset = (int) -(viewWidth - width) / 2;
        final int yOffset = (int) -(viewHeight - height) / 2;
        to.offset(xOffset, yOffset);

        return to;
    }

    @Override
    public void getLocationInDragLayer(int[] loc) {
        mLauncher.getDragLayer().getLocationInDragLayer(this, loc);
    }

    public void enableAccessibleDrag(boolean enable) {
        setOnClickListener(enable ? this : null);
    }

    protected String getAccessibilityDropConfirmation() {
        return null;
    }

    @Override
    public void onClick(View v) {
        LauncherAppState.getInstance().getAccessibilityDelegate()
            .handleAccessibleDrop(this, null, getAccessibilityDropConfirmation());
    }

    public int getTextColor() {
        return getTextColors().getDefaultColor();
    }

    public static boolean isApplication(ItemInfo info){
        if(info == null)return false;
        return (info instanceof ShortcutInfo)&&(((ShortcutInfo) info).itemType==LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION);
    }

    //add by xiangzx
    public static boolean isShortcut(ItemInfo info){
        if(info == null)return false;
        return (info instanceof ShortcutInfo)&&(((ShortcutInfo) info).itemType==LauncherSettings.BaseLauncherColumns.ITEM_TYPE_SHORTCUT);
    }
    //M:liuzuo begin
    private Paint paint;
    private float radius;

    private float mAniAlphaNum;
    private int mWidth;
    private boolean mIsAnimat;
    private boolean mAnimating;
    private ObjectAnimator mAniRadiu;
    private ObjectAnimator mAniAlpha;
    private void init() {
        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.buttonDropTarget_bg_color));
        paint.setStrokeWidth(3);
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
    }
    private void initAnimation( float from,  float to, final boolean isEnter){
        AnimatorSet animation = LauncherAnimUtils.createAnimatorSet();
        if(Math.abs(from-to)<20){
            if (isEnter){
                from+=20f;
            }else {
                to+=20f;
            }
        }

        Log.d("liuzuo78","from="+from+"  to="+to);
    if(mAnimating&& mAniRadiu !=null) {
        mAniRadiu.cancel();
        mAniAlpha.cancel();
    }
        if(!mAnimating) {
            mAniRadiu = ObjectAnimator.ofFloat(this, "radius", from, to);
            mAniRadiu.setDuration(getResources().getInteger(R.integer.button_drop_target_radius_duration));
            mAniRadiu.setStartDelay(getResources().getInteger(R.integer.button_drop_target_radius_startDelay));
            if(from>to) {
                mAniRadiu.setInterpolator(new DecelerateInterpolator(0.5f));
            }else {
                mAniRadiu.setInterpolator(new DecelerateInterpolator(0.5f));
            }
            mAniRadiu.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mIsAnimat = true;
                    mAnimating = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (isEnter) {
                        mIsAnimat = true;
                    } else {
                        mIsAnimat = false;
                        resetImage();
                    }
                    mAnimating = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mAnimating = false;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animation.play(mAniRadiu);
            if(isEnter){
                mAniAlphaNum=0;
                mAniAlpha = ObjectAnimator.ofFloat(this, "aniAlpha", 0, 127);
                mAniAlpha.setInterpolator(new AccelerateInterpolator(1.5f));
            }else {
                mAniAlphaNum=64;
                mAniAlpha = ObjectAnimator.ofFloat(this, "aniAlpha", 127, 0);
                mAniAlpha.setInterpolator(new AccelerateInterpolator(1f));
                mAniAlpha.addListener(new AnimatorListenerAdapter() {
                });
            }
            mAniAlpha.setDuration(getResources().getInteger(R.integer.button_drop_target_radius_duration));
            mAniAlpha.setStartDelay(getResources().getInteger(R.integer.button_drop_target_radius_startDelay));
            animation.play(mAniAlpha);
            animation.start();
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        Drawable[] drawables = getCompoundDrawables();
        float bodyWidth=0;
        canvas.save();
        if (drawables != null) {
            Drawable drawableLeft = drawables[0];
            if (drawableLeft != null) {
                float textWidth = getPaint().measureText(getText().toString());
                int drawablePadding = getCompoundDrawablePadding();
                int drawableWidth ;
                drawableWidth = drawableLeft.getIntrinsicWidth();
                bodyWidth = textWidth + drawableWidth + drawablePadding;
                mIconLeft =(getWidth() - bodyWidth)/2;
                mIconRight =getWidth()- mIconLeft;
                canvas.translate((getWidth() - bodyWidth) / 2, 0);
            }
        }
        super.onDraw(canvas);
        if(bodyWidth!=0)
        canvas.restore();
        int width=getWidth();
        int height=getHeight();
        if(width!=0 && mWidth!=width){
            mWidth=getWidth();
            radius=getMinRadius();
        }
        if(mIsAnimat){
        paint.setAlpha((int) mAniAlphaNum);
      //  canvas.drawCircle(getWidth()/2, -70, radius, paint);
            RectF rect=getAnimationRect(width,height);

            canvas.drawRoundRect(rect,radius,radius,paint);
        }
    }

    private RectF getAnimationRect(int width,int height) {
        float rectWidth;
        if(mSearchDropTargetBar.getWidth()==width) {
            rectWidth = width/2;
        }else {
            rectWidth=width;
        }
        rectWidth-= rectWidth*(0.2f*(1-(getMaxRadius()-radius)/(getMaxRadius()-getMinRadius())));
        return new RectF((width-rectWidth)/2, -height, (width-rectWidth)/2+rectWidth, height);
    }


    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        invalidate();
        this.radius = radius;
    }
    public float getAniAlpha() {
        return mAniAlphaNum;
    }

    public void setAniAlpha(float aniAlphaNum) {
        mAniAlphaNum = aniAlphaNum;

    }
    private int getMaxRadius(){
        return getHeight();
    }
    private int getMinRadius(){
        return (int) (getHeight()/6);
    }

    public void changeImage() {

    }

    public void resetImage() {

    }
//M:liuzuo end
}
