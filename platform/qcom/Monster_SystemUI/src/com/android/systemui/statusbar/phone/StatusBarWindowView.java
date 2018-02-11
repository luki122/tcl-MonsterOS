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

package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.ColorInt;
import android.annotation.DrawableRes;
import android.annotation.LayoutRes;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.session.MediaSessionLegacyHelper;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.InputQueue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.widget.FrameLayout;

import com.android.internal.view.FloatingActionMode;
import com.android.internal.widget.FloatingToolbar;
import com.android.systemui.R;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.statusbar.BaseStatusBar;
import com.android.systemui.statusbar.DragDownHelper;
import com.android.systemui.statusbar.StatusBarState;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;

import com.android.keyguard.KeyguardSecurityModel.SecurityMode; // //kth add ignore event when keyguard security set None 20160825

public class StatusBarWindowView extends FrameLayout {
    public static final String TAG = "StatusBarWindowView";
    public static final boolean DEBUG = BaseStatusBar.DEBUG;

    private DragDownHelper mDragDownHelper;
    private NotificationStackScrollLayout mStackScrollLayout;
    private NotificationPanelView mNotificationPanel;
    private View mBrightnessMirror;

    private int mRightInset = 0;

    private PhoneStatusBar mService;
    private final Paint mTransparentSrcPaint = new Paint();
    private FalsingManager mFalsingManager;

    // Implements the floating action mode for TextView's Cut/Copy/Past menu. Normally provided by
    // DecorView, but since this is a special window we have to roll our own.
    private View mFloatingActionModeOriginatingView;
    private ActionMode mFloatingActionMode;
    private FloatingToolbar mFloatingToolbar;
    private ViewTreeObserver.OnPreDrawListener mFloatingToolbarPreDrawListener;

    public StatusBarWindowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMotionEventSplittingEnabled(false);
        mTransparentSrcPaint.setColor(0);
        mTransparentSrcPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        mFalsingManager = FalsingManager.getInstance(context);
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        if (getFitsSystemWindows()) {
            boolean paddingChanged = insets.left != getPaddingLeft()
                    || insets.top != getPaddingTop()
                    || insets.bottom != getPaddingBottom();

            // Super-special right inset handling, because scrims and backdrop need to ignore it.
            if (insets.right != mRightInset) {
                mRightInset = insets.right;
                applyMargins();
            }
            // Drop top inset, apply left inset and pass through bottom inset.
            if (paddingChanged) {
                setPadding(insets.left, 0, 0, 0);
            }
            insets.left = 0;
            insets.top = 0;
            insets.right = 0;
        } else {
            if (mRightInset != 0) {
                mRightInset = 0;
                applyMargins();
            }
            boolean changed = getPaddingLeft() != 0
                    || getPaddingRight() != 0
                    || getPaddingTop() != 0
                    || getPaddingBottom() != 0;
            if (changed) {
                setPadding(0, 0, 0, 0);
            }
            insets.top = 0;
        }
        return false;
    }

    private void applyMargins() {
        final int N = getChildCount();
        for (int i = 0; i < N; i++) {
            View child = getChildAt(i);
            if (child.getLayoutParams() instanceof LayoutParams) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (!lp.ignoreRightInset && lp.rightMargin != mRightInset) {
                    lp.rightMargin = mRightInset;
                    child.requestLayout();
                }
            }
        }
    }

    @Override
    public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected FrameLayout.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mStackScrollLayout = (NotificationStackScrollLayout) findViewById(
                R.id.notification_stack_scroller);
        mNotificationPanel = (NotificationPanelView) findViewById(R.id.notification_panel);
        mBrightnessMirror = findViewById(R.id.brightness_mirror);
    }

    public void setService(PhoneStatusBar service) {
        mService = service;
        mDragDownHelper = new DragDownHelper(getContext(), this, mStackScrollLayout, mService);
    }

    @Override
    protected void onAttachedToWindow () {
        super.onAttachedToWindow();

        // We need to ensure that our window doesn't suffer from overdraw which would normally
        // occur if our window is translucent. Since we are drawing the whole window anyway with
        // the scrim, we don't need the window to be cleared in the beginning.
        if (mService.isScrimSrcModeEnabled()) {
            IBinder windowToken = getWindowToken();
            WindowManager.LayoutParams lp = (WindowManager.LayoutParams) getLayoutParams();
            lp.token = windowToken;
            setLayoutParams(lp);
            WindowManagerGlobal.getInstance().changeCanvasOpacity(windowToken, true);
            setWillNotDraw(false);
        } else {
            setWillNotDraw(!DEBUG);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                if (!down) {
                    mService.onBackPressed();
                }
                return true;
            case KeyEvent.KEYCODE_MENU:
                if (!down) {
                    return mService.onMenuPressed();
                }
            case KeyEvent.KEYCODE_SPACE:
                if (!down) {
                    return mService.onSpacePressed();
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (mService.isDozing()) {
                    MediaSessionLegacyHelper.getHelper(mContext).sendVolumeKeyEvent(event, true);
                    return true;
                }
                break;
        }
        if (mService.interceptMediaKey(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mFalsingManager.onTouchEvent(ev, getWidth(), getHeight());
        if (mBrightnessMirror != null && mBrightnessMirror.getVisibility() == VISIBLE) {
            // Disallow new pointers while the brightness mirror is visible. This is so that you
            // can't touch anything other than the brightness slider while the mirror is showing
            // and the rest of the panel is transparent.
            if (ev.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
                return false;
            }
        }
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            mStackScrollLayout.closeControlsIfOutsideTouch(ev);
        }

        return super.dispatchTouchEvent(ev);
    }
	    /**Mst: tangjun add begin*/
    private float x = 0;
    private float y = 0;
    private float downY = 0;
    private VelocityTracker mVelocityTracker_Mst;
    private float mMinVelocityPxPerSecond;
    private static final float MIN_VELOCITY_DP_PER_SECOND = 250;
    private boolean isHideKeyguardBouncer;
    private StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private Handler mHandler = new Handler();
    
    public interface AnimFinishListener {
    	void playFinishLister();
    }
    private AnimFinishListener mAnimFinishListener = new AnimFinishListener() {
		
		@Override
		public void playFinishLister() {
			// TODO Auto-generated method stub
			//mService.onBackPressed();// kth mod because of bouncer showing conditions 20160826
			mService.getStatusBarKeyguardViewManager().reset();
		}
	};
    /** 
     * 
     * @param event ��VelocityTracker���MotionEvent 
     * 
     * @see android.view.VelocityTracker#obtain() 
     * @see android.view.VelocityTracker#addMovement(MotionEvent) 
     */
    private void acquireVelocityTracker(final MotionEvent event) { 
        if(null == mVelocityTracker_Mst) { 
        	mVelocityTracker_Mst = VelocityTracker.obtain();
        } 
        mVelocityTracker_Mst.addMovement(event); 
    } 
   
    /** 
	* �ͷ�VelocityTracker 
	* 
	* @see android.view.VelocityTracker#clear() 
	* @see android.view.VelocityTracker#recycle() 
	*/
    private void releaseVelocityTracker() { 
        if(null != mVelocityTracker_Mst) { 
        	mVelocityTracker_Mst.clear(); 
        	mVelocityTracker_Mst.recycle(); 
        	mVelocityTracker_Mst = null; 
        } 
    }
    
    private void handleKeyguardBouncerHide(MotionEvent event){
        Log.d(TAG, "handleKeyguardBouncerHide: event=" + event.getAction());
        final int action = event.getAction();
    	final int pagingTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    	acquireVelocityTracker(event); 
    	final VelocityTracker verTracker = mVelocityTracker_Mst; 
    	switch (action) { 
    	case MotionEvent.ACTION_DOWN:
            Log.d(TAG, "handleKeyguardBouncerHide: getKeyguardViewTranslationY=" + mStatusBarKeyguardViewManager.getBouncer().getKeyguardViewTranslationY());
            if(mStatusBarKeyguardViewManager.getBouncer().getKeyguardViewTranslationY() == 0) {
        		isHideKeyguardBouncer = true;
        		boolean a  = mNotificationPanel.onTouchEvent(event);
                Log.d(TAG, "handleKeyguardBouncerHide: a=" + a);
            }
    		x = event.getRawX();
    		y = event.getRawY();
    		downY = y;
    		break; 

    	case MotionEvent.ACTION_MOVE: 
    		if(downY > 500 || !isHideKeyguardBouncer) {
    			return;
    		}
    		if(event.getRawY() - y > pagingTouchSlop) {
    			mNotificationPanel.onTouchEvent(event);
    		}
    		break; 

    	case MotionEvent.ACTION_UP:
    	case MotionEvent.ACTION_CANCEL:
    		if(downY > 500 || !isHideKeyguardBouncer) {
    			return;
    		}
    		isHideKeyguardBouncer = false;
    		verTracker.computeCurrentVelocity(1000); 
    		final float velocityX = verTracker.getXVelocity(); 
    		final float velocityY = verTracker.getYVelocity(); 
    		boolean expand = false;
           
            //if(Math.abs(event.getRawY() - y) > pagingTouchSlop) {/// kth mod keyguard bug 20160826
            expand = event.getRawY() - y >  mStatusBarKeyguardViewManager.getBouncer().getKeyguardViewInitTranslationY() / 2 || 
                    (velocityY > mMinVelocityPxPerSecond && event.getRawY() - y >  mStatusBarKeyguardViewManager.getBouncer().getKeyguardViewInitTranslationY() / 4) ;
            mNotificationPanel.fling(velocityY, expand, mNotificationPanel.isFalseTouch(event.getX(), event.getY()), mAnimFinishListener);
            releaseVelocityTracker();
            break; 

    	default: 
    		break; 
    	} 
    }

    public void setStatusBarKeyguardViewManager(StatusBarKeyguardViewManager statusBarKeyguardViewManager) {
        mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
    }
    /**Mst: tangjun add end*/

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercept = false;
        /**Mst: tangjun add begin*/
        boolean isSecurityMode = mStatusBarKeyguardViewManager.getBouncer().isKeyguardSecurity();//kth mod ignore event when keyguard security set None 20160825
//      if(mService.getBarState()!=StatusBarState.SHADE && isSecurityMode){
        if (mStatusBarKeyguardViewManager.isBouncerShowing()) {
//                handleKeyguardBouncerHide(ev);
//            intercept = true;
        }
        /**Mst: tangjun add end*/
        if (mNotificationPanel.isFullyExpanded()
                && mStackScrollLayout.getVisibility() == View.VISIBLE
                && mService.getBarState() == StatusBarState.KEYGUARD
                && !mService.isBouncerShowing()) {
            //intercept = mDragDownHelper.onInterceptTouchEvent(ev);//delete by chenhl
            // wake up on a touch down event, if dozing
            if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
                mService.wakeUpIfDozing(ev.getEventTime(), ev);
            }
        }
        if (!intercept) {
            super.onInterceptTouchEvent(ev);
        }
        if (intercept) {
            MotionEvent cancellation = MotionEvent.obtain(ev);
            cancellation.setAction(MotionEvent.ACTION_CANCEL);
            mStackScrollLayout.onInterceptTouchEvent(cancellation);
            mNotificationPanel.onInterceptTouchEvent(cancellation);
            cancellation.recycle();
        }
        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean handled = false;
        if (mService.getBarState() == StatusBarState.KEYGUARD) {
            //handled = mDragDownHelper.onTouchEvent(ev);//delete by chenhl
        }
        if (!handled) {
            handled = super.onTouchEvent(ev);
        }
        final int action = ev.getAction();
        if (!handled && (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)) {
            mService.setInteracting(StatusBarManager.WINDOW_STATUS_BAR, false);
        }
//        handleKeyguardBouncerHide(ev);
        return handled;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mService.isScrimSrcModeEnabled()) {
            // We need to ensure that our window is always drawn fully even when we have paddings,
            // since we simulate it to be opaque.
            int paddedBottom = getHeight() - getPaddingBottom();
            int paddedRight = getWidth() - getPaddingRight();
            if (getPaddingTop() != 0) {
                canvas.drawRect(0, 0, getWidth(), getPaddingTop(), mTransparentSrcPaint);
            }
            if (getPaddingBottom() != 0) {
                canvas.drawRect(0, paddedBottom, getWidth(), getHeight(), mTransparentSrcPaint);
            }
            if (getPaddingLeft() != 0) {
                canvas.drawRect(0, getPaddingTop(), getPaddingLeft(), paddedBottom,
                        mTransparentSrcPaint);
            }
            if (getPaddingRight() != 0) {
                canvas.drawRect(paddedRight, getPaddingTop(), getWidth(), paddedBottom,
                        mTransparentSrcPaint);
            }
        }
        if (DEBUG) {
            Paint pt = new Paint();
            pt.setColor(0x80FFFF00);
            pt.setStrokeWidth(12.0f);
            pt.setStyle(Paint.Style.STROKE);
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), pt);
        }
    }

    public void cancelExpandHelper() {
        if (mStackScrollLayout != null) {
            mStackScrollLayout.cancelExpandHelper();
        }
    }

    public class LayoutParams extends FrameLayout.LayoutParams {

        public boolean ignoreRightInset;

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.StatusBarWindowView_Layout);
            ignoreRightInset = a.getBoolean(
                    R.styleable.StatusBarWindowView_Layout_ignoreRightInset, false);
            a.recycle();
        }
    }

    @Override
    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback,
            int type) {
        if (type == ActionMode.TYPE_FLOATING) {
            return startActionMode(originalView, callback, type);
        }
        return super.startActionModeForChild(originalView, callback, type);
    }

    private ActionMode createFloatingActionMode(
            View originatingView, ActionMode.Callback2 callback) {
        if (mFloatingActionMode != null) {
            mFloatingActionMode.finish();
        }
        cleanupFloatingActionModeViews();
        final FloatingActionMode mode =
                new FloatingActionMode(mContext, callback, originatingView);
        mFloatingActionModeOriginatingView = originatingView;
        mFloatingToolbarPreDrawListener =
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        mode.updateViewLocationInWindow();
                        return true;
                    }
                };
        return mode;
    }

    private void setHandledFloatingActionMode(ActionMode mode) {
        mFloatingActionMode = mode;
        mFloatingToolbar = new FloatingToolbar(mContext, mFakeWindow);
        ((FloatingActionMode) mFloatingActionMode).setFloatingToolbar(mFloatingToolbar);
        mFloatingActionMode.invalidate();  // Will show the floating toolbar if necessary.
        mFloatingActionModeOriginatingView.getViewTreeObserver()
                .addOnPreDrawListener(mFloatingToolbarPreDrawListener);
    }

    private void cleanupFloatingActionModeViews() {
        if (mFloatingToolbar != null) {
            mFloatingToolbar.dismiss();
            mFloatingToolbar = null;
        }
        if (mFloatingActionModeOriginatingView != null) {
            if (mFloatingToolbarPreDrawListener != null) {
                mFloatingActionModeOriginatingView.getViewTreeObserver()
                        .removeOnPreDrawListener(mFloatingToolbarPreDrawListener);
                mFloatingToolbarPreDrawListener = null;
            }
            mFloatingActionModeOriginatingView = null;
        }
    }

    private ActionMode startActionMode(
            View originatingView, ActionMode.Callback callback, int type) {
        ActionMode.Callback2 wrappedCallback = new ActionModeCallback2Wrapper(callback);
        ActionMode mode = createFloatingActionMode(originatingView, wrappedCallback);
        if (mode != null && wrappedCallback.onCreateActionMode(mode, mode.getMenu())) {
            setHandledFloatingActionMode(mode);
        } else {
            mode = null;
        }
        return mode;
    }

    private class ActionModeCallback2Wrapper extends ActionMode.Callback2 {
        private final ActionMode.Callback mWrapped;

        public ActionModeCallback2Wrapper(ActionMode.Callback wrapped) {
            mWrapped = wrapped;
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return mWrapped.onCreateActionMode(mode, menu);
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            requestFitSystemWindows();
            return mWrapped.onPrepareActionMode(mode, menu);
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return mWrapped.onActionItemClicked(mode, item);
        }

        public void onDestroyActionMode(ActionMode mode) {
            mWrapped.onDestroyActionMode(mode);
            if (mode == mFloatingActionMode) {
                cleanupFloatingActionModeViews();
                mFloatingActionMode = null;
            }
            requestFitSystemWindows();
        }

        @Override
        public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
            if (mWrapped instanceof ActionMode.Callback2) {
                ((ActionMode.Callback2) mWrapped).onGetContentRect(mode, view, outRect);
            } else {
                super.onGetContentRect(mode, view, outRect);
            }
        }
    }

    /**
     * Minimal window to satisfy FloatingToolbar.
     */
    private Window mFakeWindow = new Window(mContext) {
        @Override
        public void takeSurface(SurfaceHolder.Callback2 callback) {
        }

        @Override
        public void takeInputQueue(InputQueue.Callback callback) {
        }

        @Override
        public boolean isFloating() {
            return false;
        }

        @Override
        public void alwaysReadCloseOnTouchAttr() {
        }

        @Override
        public void setContentView(@LayoutRes int layoutResID) {
        }

        @Override
        public void setContentView(View view) {
        }

        @Override
        public void setContentView(View view, ViewGroup.LayoutParams params) {
        }

        @Override
        public void addContentView(View view, ViewGroup.LayoutParams params) {
        }

        @Override
        public void clearContentView() {
        }

        @Override
        public View getCurrentFocus() {
            return null;
        }

        @Override
        public LayoutInflater getLayoutInflater() {
            return null;
        }

        @Override
        public void setTitle(CharSequence title) {
        }

        @Override
        public void setTitleColor(@ColorInt int textColor) {
        }

        @Override
        public void openPanel(int featureId, KeyEvent event) {
        }

        @Override
        public void closePanel(int featureId) {
        }

        @Override
        public void togglePanel(int featureId, KeyEvent event) {
        }

        @Override
        public void invalidatePanelMenu(int featureId) {
        }

        @Override
        public boolean performPanelShortcut(int featureId, int keyCode, KeyEvent event, int flags) {
            return false;
        }

        @Override
        public boolean performPanelIdentifierAction(int featureId, int id, int flags) {
            return false;
        }

        @Override
        public void closeAllPanels() {
        }

        @Override
        public boolean performContextMenuIdentifierAction(int id, int flags) {
            return false;
        }

        @Override
        public void onConfigurationChanged(Configuration newConfig) {
        }

        @Override
        public void setBackgroundDrawable(Drawable drawable) {
        }

        @Override
        public void setFeatureDrawableResource(int featureId, @DrawableRes int resId) {
        }

        @Override
        public void setFeatureDrawableUri(int featureId, Uri uri) {
        }

        @Override
        public void setFeatureDrawable(int featureId, Drawable drawable) {
        }

        @Override
        public void setFeatureDrawableAlpha(int featureId, int alpha) {
        }

        @Override
        public void setFeatureInt(int featureId, int value) {
        }

        @Override
        public void takeKeyEvents(boolean get) {
        }

        @Override
        public boolean superDispatchKeyEvent(KeyEvent event) {
            return false;
        }

        @Override
        public boolean superDispatchKeyShortcutEvent(KeyEvent event) {
            return false;
        }

        @Override
        public boolean superDispatchTouchEvent(MotionEvent event) {
            return false;
        }

        @Override
        public boolean superDispatchTrackballEvent(MotionEvent event) {
            return false;
        }

        @Override
        public boolean superDispatchGenericMotionEvent(MotionEvent event) {
            return false;
        }

        @Override
        public View getDecorView() {
            return StatusBarWindowView.this;
        }

        @Override
        public View peekDecorView() {
            return null;
        }

        @Override
        public Bundle saveHierarchyState() {
            return null;
        }

        @Override
        public void restoreHierarchyState(Bundle savedInstanceState) {
        }

        @Override
        protected void onActive() {
        }

        @Override
        public void setChildDrawable(int featureId, Drawable drawable) {
        }

        @Override
        public void setChildInt(int featureId, int value) {
        }

        @Override
        public boolean isShortcutKey(int keyCode, KeyEvent event) {
            return false;
        }

        @Override
        public void setVolumeControlStream(int streamType) {
        }

        @Override
        public int getVolumeControlStream() {
            return 0;
        }

        @Override
        public int getStatusBarColor() {
            return 0;
        }

        @Override
        public void setStatusBarColor(@ColorInt int color) {
        }

        @Override
        public int getNavigationBarColor() {
            return 0;
        }

        @Override
        public void setNavigationBarColor(@ColorInt int color) {
        }

        @Override
        public void setDecorCaptionShade(int decorCaptionShade) {
        }

        @Override
        public void setResizingCaptionDrawable(Drawable drawable) {
        }

        @Override
        public void onMultiWindowModeChanged() {
        }

        @Override
        public void reportActivityRelaunched() {
        }
    };

}

