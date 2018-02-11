/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.Nullable;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.recents.Constants;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.LaunchTaskEvent;
import com.android.systemui.recents.events.ui.ShowApplicationInfoEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;

import static android.app.ActivityManager.StackId.FREEFORM_WORKSPACE_STACK_ID;
import static android.app.ActivityManager.StackId.FULLSCREEN_WORKSPACE_STACK_ID;
import static android.app.ActivityManager.StackId.INVALID_STACK_ID;

/* The task bar view */
public class MstTaskViewHeader extends RelativeLayout {

    private static final float HIGHLIGHT_LIGHTNESS_INCREMENT = 0.075f;
    private static final float OVERLAY_LIGHTNESS_INCREMENT = -0.0625f;
    private static final int OVERLAY_REVEAL_DURATION = 250;
    private static final long FOCUS_INDICATOR_INTERVAL_MS = 30;

    /**
     * A color drawable that draws a slight highlight at the top to help it stand out.
     */
    private class HighlightColorDrawable extends Drawable {

        private Paint mHighlightPaint = new Paint();
        private Paint mBackgroundPaint = new Paint();
        private int mColor;
        private float mDimAlpha;

        public HighlightColorDrawable() {
            mBackgroundPaint.setColor(Color.argb(255, 0, 0, 0));
            mBackgroundPaint.setAntiAlias(true);
            mHighlightPaint.setColor(Color.argb(255, 255, 255, 255));
            mHighlightPaint.setAntiAlias(true);
        }

        public void setColorAndDim(int color, float dimAlpha) {
            if (mColor != color || Float.compare(mDimAlpha, dimAlpha) != 0) {
                mColor = color;
                mDimAlpha = dimAlpha;
                mBackgroundPaint.setColor(color);

                ColorUtils.colorToHSL(color, mTmpHSL);
                // TODO: Consider using the saturation of the color to adjust the lightness as well
                mTmpHSL[2] = Math.min(1f,
                        mTmpHSL[2] + HIGHLIGHT_LIGHTNESS_INCREMENT * (1.0f - dimAlpha));
                mHighlightPaint.setColor(ColorUtils.HSLToColor(mTmpHSL));

                invalidateSelf();
            }
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
            // Do nothing
        }

        @Override
        public void setAlpha(int alpha) {
            // Do nothing
        }

        @Override
        public void draw(Canvas canvas) {
            // Draw the highlight at the top edge (but put the bottom edge just out of view)
            canvas.drawRoundRect(0, 0, mTaskViewRect.width(),
                    2 * Math.max(mHighlightHeight, mCornerRadius),
                    mCornerRadius, mCornerRadius, mHighlightPaint);

            // Draw the background with the rounded corners
            canvas.drawRoundRect(0, mHighlightHeight, mTaskViewRect.width(),
                    getHeight() + mCornerRadius,
                    mCornerRadius, mCornerRadius, mBackgroundPaint);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }

        public int getColor() {
            return mColor;
        }
    }

    Task mTask;

    // Header views
    ImageView mIconView;
    TextView mTitleView;

    // Header drawables
    @ViewDebug.ExportedProperty(category="recents")
    Rect mTaskViewRect = new Rect();
    int mHeaderBarHeight;
    int mHeaderButtonPadding;
    int mCornerRadius;
    int mHighlightHeight;
    @ViewDebug.ExportedProperty(category="recents")
    float mDimAlpha;
    Drawable mLightDismissDrawable;
    Drawable mDarkDismissDrawable;
    Drawable mLightFreeformIcon;
    Drawable mDarkFreeformIcon;
    Drawable mLightFullscreenIcon;
    Drawable mDarkFullscreenIcon;
    Drawable mLightInfoIcon;
    Drawable mDarkInfoIcon;
    int mTaskBarViewLightTextColor;
    int mTaskBarViewDarkTextColor;
    int mDisabledTaskBarBackgroundColor;
    int mMoveTaskTargetStackId = INVALID_STACK_ID;

    // Header background
    private HighlightColorDrawable mBackground;
    private HighlightColorDrawable mOverlayBackground;
    private float[] mTmpHSL = new float[3];

    // Header dim, which is only used when task view hardware layers are not used
    private Paint mDimLayerPaint = new Paint();

    private CountDownTimer mFocusTimerCountDown;

    public MstTaskViewHeader(Context context) {
        this(context, null);
    }

    public MstTaskViewHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MstTaskViewHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MstTaskViewHeader(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setWillNotDraw(false);

        // Load the dismiss resources
        Resources res = context.getResources();
        mLightDismissDrawable = context.getDrawable(R.drawable.recents_dismiss_light);
        mDarkDismissDrawable = context.getDrawable(R.drawable.recents_dismiss_dark);
        mCornerRadius = res.getDimensionPixelSize(R.dimen.recents_task_view_rounded_corners_radius);
        mHighlightHeight = res.getDimensionPixelSize(R.dimen.recents_task_view_highlight);
        mTaskBarViewLightTextColor = context.getColor(R.color.recents_task_bar_light_text_color);
        mTaskBarViewDarkTextColor = context.getColor(R.color.recents_task_bar_dark_text_color);
        mLightFreeformIcon = context.getDrawable(R.drawable.recents_move_task_freeform_light);
        mDarkFreeformIcon = context.getDrawable(R.drawable.recents_move_task_freeform_dark);
        mLightFullscreenIcon = context.getDrawable(R.drawable.recents_move_task_fullscreen_light);
        mDarkFullscreenIcon = context.getDrawable(R.drawable.recents_move_task_fullscreen_dark);
        mLightInfoIcon = context.getDrawable(R.drawable.recents_info_light);
        mDarkInfoIcon = context.getDrawable(R.drawable.recents_info_dark);
        mDisabledTaskBarBackgroundColor =
                context.getColor(R.color.recents_task_bar_disabled_background_color);

        // Configure the background and dim
        mBackground = new HighlightColorDrawable();
        mBackground.setColorAndDim(Color.argb(255, 0, 0, 0), 0f);
        //setBackground(mBackground);
        mOverlayBackground = new HighlightColorDrawable();
        mDimLayerPaint.setColor(Color.argb(255, 0, 0, 0));
        mDimLayerPaint.setAntiAlias(true);
    }

    /**
     * Resets this header along with the TaskView.
     */
    public void reset() {

    }

    @Override
    protected void onFinishInflate() {
        SystemServicesProxy ssp = Recents.getSystemServices();

        // Initialize the icon and description views
        mIconView = (ImageView) findViewById(R.id.icon);
        mTitleView = (TextView) findViewById(R.id.title);

        onConfigurationChanged();
    }

    /**
     * Programmatically sets the layout params for a header bar layout.  This is necessary because
     * we can't get resources based on the current configuration, but instead need to get them
     * based on the device configuration.
     */
    private void updateLayoutParams(View icon, View title, View secondaryButton, View button) {
    	/**Mst: tangjun mod begin*/
    	/*
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, mHeaderBarHeight, Gravity.TOP);
        setLayoutParams(lp);
        lp = new FrameLayout.LayoutParams(mHeaderBarHeight, mHeaderBarHeight, Gravity.START);
        icon.setLayoutParams(lp);
        lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.START | Gravity.CENTER_VERTICAL);
        lp.setMarginStart(mHeaderBarHeight);
        lp.setMarginEnd(mMoveTaskButton != null
                ? 2 * mHeaderBarHeight
                : mHeaderBarHeight);
        title.setLayoutParams(lp);
        if (secondaryButton != null) {
            lp = new FrameLayout.LayoutParams(mHeaderBarHeight, mHeaderBarHeight, Gravity.END);
            lp.setMarginEnd(mHeaderBarHeight);
            secondaryButton.setLayoutParams(lp);
            secondaryButton.setPadding(mHeaderButtonPadding, mHeaderButtonPadding,
                    mHeaderButtonPadding, mHeaderButtonPadding);
        }
        lp = new FrameLayout.LayoutParams(mHeaderBarHeight, mHeaderBarHeight, Gravity.END);
        button.setLayoutParams(lp);
        button.setPadding(mHeaderButtonPadding, mHeaderButtonPadding, mHeaderButtonPadding,
                mHeaderButtonPadding);
        */
    	/**Mst: tangjun mod begin*/
    }

    /**
     * Update the header view when the configuration changes.
     */
    public void onConfigurationChanged() {
        // Update the dimensions of everything in the header. We do this because we need to use
        // resources for the display, and not the current configuration.
        Resources res = getResources();
        int headerBarHeight = TaskStackLayoutAlgorithm.getDimensionForDevice(getContext(),
                R.dimen.recents_task_view_header_height,
                R.dimen.recents_task_view_header_height,
                R.dimen.recents_task_view_header_height,
                R.dimen.recents_task_view_header_height_tablet_land,
                R.dimen.recents_task_view_header_height,
                R.dimen.recents_task_view_header_height_tablet_land);
        int headerButtonPadding = TaskStackLayoutAlgorithm.getDimensionForDevice(getContext(),
                R.dimen.recents_task_view_header_button_padding,
                R.dimen.recents_task_view_header_button_padding,
                R.dimen.recents_task_view_header_button_padding,
                R.dimen.recents_task_view_header_button_padding_tablet_land,
                R.dimen.recents_task_view_header_button_padding,
                R.dimen.recents_task_view_header_button_padding_tablet_land);
        if (headerBarHeight != mHeaderBarHeight || headerButtonPadding != mHeaderButtonPadding) {
            mHeaderBarHeight = headerBarHeight;
            mHeaderButtonPadding = headerButtonPadding;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // Since we update the position of children based on the width of the parent and this view
        // recompute these changes with the new view size
        onTaskViewSizeChanged(mTaskViewRect.width(), mTaskViewRect.height());
    }

    /**
     * Called when the task view frame changes, allowing us to move the contents of the header
     * to match the frame changes.
     */
    public void onTaskViewSizeChanged(int width, int height) {
        mTaskViewRect.set(0, 0, width, height);

        //setLeftTopRightBottom(0, 0, width, getMeasuredHeight());
    }

    @Override
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);

        // Draw the dim layer with the rounded corners
        canvas.drawRoundRect(0, 0, mTaskViewRect.width(), getHeight() + mCornerRadius,
                mCornerRadius, mCornerRadius, mDimLayerPaint);
    }

    /** Only exposed for the workaround for b/27815919. */
    public ImageView getIconView() {
        return mIconView;
    }

    /** Returns the secondary color for a primary color. */
    int getSecondaryColor(int primaryColor, boolean useLightOverlayColor) {
        int overlayColor = useLightOverlayColor ? Color.WHITE : Color.BLACK;
        return Utilities.getColorWithOverlay(primaryColor, overlayColor, 0.8f);
    }

    /**
     * Sets the dim alpha, only used when we are not using hardware layers.
     * (see RecentsConfiguration.useHardwareLayers)
     */
    public void setDimAlpha(float dimAlpha) {
        if (Float.compare(mDimAlpha, dimAlpha) != 0) {
            mDimAlpha = dimAlpha;
            mTitleView.setAlpha(1f - dimAlpha);
            updateBackgroundColor(mBackground.getColor(), dimAlpha);
        }
    }

    /**
     * Updates the background and highlight colors for this header.
     */
    private void updateBackgroundColor(int color, float dimAlpha) {
        if (mTask != null) {
            mBackground.setColorAndDim(color, dimAlpha);
            // TODO: Consider using the saturation of the color to adjust the lightness as well
            ColorUtils.colorToHSL(color, mTmpHSL);
            mTmpHSL[2] = Math.min(1f, mTmpHSL[2] + OVERLAY_LIGHTNESS_INCREMENT * (1.0f - dimAlpha));
            mOverlayBackground.setColorAndDim(ColorUtils.HSLToColor(mTmpHSL), dimAlpha);
            mDimLayerPaint.setAlpha((int) (dimAlpha * 255));
            invalidate();
        }
    }

    /**
     * Binds the bar view to the task.
     */
    public void bindToTask(Task t, boolean touchExplorationEnabled, boolean disabledInSafeMode) {
        mTask = t;

        int primaryColor = disabledInSafeMode
                ? mDisabledTaskBarBackgroundColor
                : t.colorPrimary;
        if (mBackground.getColor() != primaryColor) {
            updateBackgroundColor(primaryColor, mDimAlpha);
        }
        Log.d("171717", "---t.title = " + t.title);
        if (!mTitleView.getText().toString().equals(t.title)) {
            mTitleView.setText(t.title);
        }
        mTitleView.setContentDescription(t.titleDescription);
        /**Mst: tangjun mod begin*/
        /*
        mTitleView.setTextColor(t.useLightOnPrimaryColor ?
                mTaskBarViewLightTextColor : mTaskBarViewDarkTextColor);
         */
        /**Mst: tangjun mod end*/

        // When freeform workspaces are enabled, then update the move-task button depending on the
        // current task
        
        
        // In accessibility, a single click on the focused app info button will show it
        if (touchExplorationEnabled) {
            mIconView.setContentDescription(t.appInfoDescription);
            mIconView.setClickable(true);
        }
    }

    /**
     * Called when the bound task's data has loaded and this view should update to reflect the
     * changes.
     */
    public void onTaskDataLoaded() {
        if (mTask.icon != null) {
            mIconView.setImageDrawable(mTask.icon);
        }
    }

    /** Unbinds the bar view from the task */
    void unbindFromTask(boolean touchExplorationEnabled) {
        mTask = null;
        mIconView.setImageDrawable(null);
        if (touchExplorationEnabled) {
            mIconView.setClickable(false);
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {

        // Don't forward our state to the drawable - we do it manually in onTaskViewFocusChanged.
        // This is to prevent layer trashing when the view is pressed.
        return new int[] {};
    }
}
