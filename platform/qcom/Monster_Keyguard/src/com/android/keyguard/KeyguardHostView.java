/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.keyguard;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.os.PowerManager;
import android.os.SystemClock;
import android.service.trust.TrustAgentService;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;

import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardSecurityContainer.SecurityCallback;
import com.android.keyguard.KeyguardSecurityModel.SecurityMode;

import java.io.File;

/**
 * Base class for keyguard view.  {@link #reset} is where you should
 * reset the state of your view.  Use the {@link KeyguardViewCallback} via
 * {@link #getCallback()} to send information back (such as poking the wake lock,
 * or finishing the keyguard).
 *
 * Handles intercepting of media keys that still work when the keyguard is
 * showing.
 */
public class KeyguardHostView extends FrameLayout implements SecurityCallback {

    public interface OnDismissAction {
        /**
         * @return true if the dismiss should be deferred
         */
        boolean onDismiss();
    }

    private AudioManager mAudioManager;
    private TelephonyManager mTelephonyManager = null;
    protected ViewMediatorCallback mViewMediatorCallback;
    protected LockPatternUtils mLockPatternUtils;
    private OnDismissAction mDismissAction;
    private Runnable mCancelAction;

    private final KeyguardUpdateMonitorCallback mUpdateCallback =
            new KeyguardUpdateMonitorCallback() {

        @Override
        public void onUserSwitchComplete(int userId) {
            getSecurityContainer().showPrimarySecurityScreen(false /* turning off */);
        }

        @Override
        public void onTrustGrantedWithFlags(int flags, int userId) {
            if (userId != KeyguardUpdateMonitor.getCurrentUser()) return;
            if (!isAttachedToWindow()) return;
            boolean bouncerVisible = isVisibleToUser();
            boolean initiatedByUser =
                    (flags & TrustAgentService.FLAG_GRANT_TRUST_INITIATED_BY_USER) != 0;
            boolean dismissKeyguard =
                    (flags & TrustAgentService.FLAG_GRANT_TRUST_DISMISS_KEYGUARD) != 0;

            if (initiatedByUser || dismissKeyguard) {
                if (mViewMediatorCallback.isScreenOn() && (bouncerVisible || dismissKeyguard)) {
                    if (!bouncerVisible) {
                        // The trust agent dismissed the keyguard without the user proving
                        // that they are present (by swiping up to show the bouncer). That's fine if
                        // the user proved presence via some other way to the trust agent.
                        Log.i(TAG, "TrustAgent dismissed Keyguard.");
                    }
                    dismiss(false /* authenticated */);
                } else {
                    mViewMediatorCallback.playTrustedSound();
                }
            }
        }
    };

    // Whether the volume keys should be handled by keyguard. If true, then
    // they will be handled here for specific media types such as music, otherwise
    // the audio service will bring up the volume dialog.
    private static final boolean KEYGUARD_MANAGES_VOLUME = false;
    public static final boolean DEBUG = KeyguardConstants.DEBUG;
    private static final String TAG = "KeyguardViewBase";

    private KeyguardSecurityContainer mSecurityContainer;

    public KeyguardHostView(Context context) {
        this(context, null);
    }

    public KeyguardHostView(Context context, AttributeSet attrs) {
        super(context, attrs);
        KeyguardUpdateMonitor.getInstance(context).registerCallback(mUpdateCallback);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mViewMediatorCallback != null) {
            mViewMediatorCallback.keyguardDoneDrawing();
        }
    }

    /**
     * Sets an action to run when keyguard finishes.
     *
     * @param action
     */
    public void setOnDismissAction(OnDismissAction action, Runnable cancelAction) {
        if (mCancelAction != null) {
            mCancelAction.run();
            mCancelAction = null;
        }
        mDismissAction = action;
        mCancelAction = cancelAction;
    }

    public void cancelDismissAction() {
        setOnDismissAction(null, null);
    }

    @Override
    protected void onFinishInflate() {
        mSecurityContainer =
                (KeyguardSecurityContainer) findViewById(R.id.keyguard_security_container);
        mLockPatternUtils = new LockPatternUtils(mContext);
        mSecurityContainer.setLockPatternUtils(mLockPatternUtils);
        mSecurityContainer.setSecurityCallback(this);
        mSecurityContainer.showPrimarySecurityScreen(false);
        // mSecurityContainer.updateSecurityViews(false /* not bouncing */);
    }
    
    /**Mst: tangjun add begin*/
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
    	// TODO Auto-generated method stub
    	super.onWindowFocusChanged(hasWindowFocus);
    	//setTranslationY(getHeight()); // kth modify for bug issue #2 #11 Bouncer display abnormal 20160806
    }
    /**Mst: tangjun add end*/

    /**
     * Called when the view needs to be shown.
     */
    public void showPrimarySecurityScreen() {
        if (DEBUG) Log.d(TAG, "show()");
        mSecurityContainer.showPrimarySecurityScreen(false);
    }

    /**
     * Show a string explaining why the security view needs to be solved.
     *
     * @param reason a flag indicating which string should be shown, see
     *               {@link KeyguardSecurityView#PROMPT_REASON_NONE},
     *               {@link KeyguardSecurityView#PROMPT_REASON_RESTART} and
     *               {@link KeyguardSecurityView#PROMPT_REASON_TIMEOUT}.
     */
    public void showPromptReason(int reason) {
        mSecurityContainer.showPromptReason(reason);
    }

    public void showMessage(String message, int color) {
        mSecurityContainer.showMessage(message, color);
    }

    /**
     *  Dismisses the keyguard by going to the next screen or making it gone.
     *
     *  @return True if the keyguard is done.
     */
    public boolean dismiss() {
        return dismiss(false);
    }

    public boolean handleBackKey() {
        if (mSecurityContainer.getCurrentSecuritySelection() != SecurityMode.None) {
            mSecurityContainer.dismiss(false);
            return true;
        }
        return false;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            event.getText().add(mSecurityContainer.getCurrentSecurityModeContentDescription());
            return true;
        } else {
            return super.dispatchPopulateAccessibilityEvent(event);
        }
    }

    protected KeyguardSecurityContainer getSecurityContainer() {
        return mSecurityContainer;
    }

    @Override
    public boolean dismiss(boolean authenticated) {
        return mSecurityContainer.showNextSecurityScreenOrFinish(authenticated);
    }

    /**
     * Authentication has happened and it's time to dismiss keyguard. This function
     * should clean up and inform KeyguardViewMediator.
     *
     * @param strongAuth whether the user has authenticated with strong authentication like
     *                   pattern, password or PIN but not by trust agents or fingerprint
     */
    @Override
    public void finish(boolean strongAuth) {
        // If there's a pending runnable because the user interacted with a widget
        // and we're leaving keyguard, then run it.
        boolean deferKeyguardDone = false;
        if (mDismissAction != null) {
            deferKeyguardDone = mDismissAction.onDismiss();
            mDismissAction = null;
            mCancelAction = null;
        }
        if (mViewMediatorCallback != null) {
            if (deferKeyguardDone) {
                mViewMediatorCallback.keyguardDonePending(strongAuth);
            } else {
                mViewMediatorCallback.keyguardDone(strongAuth);
            }
        }
    }

    @Override
    public void reset() {
        mViewMediatorCallback.resetKeyguard();
    }

    @Override
    public void onSecurityModeChanged(SecurityMode securityMode, boolean needsInput) {
        if (mViewMediatorCallback != null) {
            mViewMediatorCallback.setNeedsInput(needsInput);
        }
    }

    public void userActivity() {
        if (mViewMediatorCallback != null) {
            mViewMediatorCallback.userActivity();
        }
    }

    /**
     * Called when the Keyguard is not actively shown anymore on the screen.
     */
    public void onPause() {
        if (DEBUG) Log.d(TAG, String.format("screen off, instance %s at %s",
                Integer.toHexString(hashCode()), SystemClock.uptimeMillis()));
        mSecurityContainer.showPrimarySecurityScreen(true);
        mSecurityContainer.onPause();
        clearFocus();
    }

    /**
     * Called when the Keyguard is actively shown on the screen.
     */
    public void onResume() {
        if (DEBUG) Log.d(TAG, "screen on, instance " + Integer.toHexString(hashCode()));
        mSecurityContainer.onResume(KeyguardSecurityView.SCREEN_ON);
        requestFocus();
    }

    /**
     * Starts the animation when the Keyguard gets shown.
     */
    public void startAppearAnimation() {
        /// TCL Monster: kth mod for bouncer appear anim 20161112 start
        setTranslationY(0);
        mSecurityContainer.startAppearAnimation();
    	/**Mst: tangjun mod for Keyguard show Animation beign*/
/*    	float curTranslationY = this.getTranslationY();
    	Log.e("111111", "--KeyguardHostView startAppearAnimation curTranslationY = " + curTranslationY);
    	ObjectAnimator animator = ObjectAnimator.ofFloat(this, "TranslationY", curTranslationY, 0).setDuration(300);
    	AnimatorSet animSet = new AnimatorSet();
    	animSet.play(animator);
    	animSet.start();*/
        /**Mst: tangjun mod for Keyguard show Animation end*/
        /// TCL Monster: kth mod for bouncer appear anim 20161112 end
    }

    public void startDisappearAnimation(Runnable finishRunnable) {
        if (!mSecurityContainer.startDisappearAnimation(finishRunnable) && finishRunnable != null) {
            finishRunnable.run();
        }
    }

    /**
     * Called before this view is being removed.
     */
    public void cleanUp() {
    	/**Mst: tangjun add for init KeyguardHostView TranslationY begin*/
    	setTranslationY(getHeight());
    	/**Mst: tangjun add for init KeyguardHostView TranslationY end*/
        getSecurityContainer().onPause();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (interceptMediaKey(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * Allows the media keys to work when the keyguard is showing.
     * The media keys should be of no interest to the actual keyguard view(s),
     * so intercepting them here should not be of any harm.
     * @param event The key event
     * @return whether the event was consumed as a media key.
     */
    public boolean interceptMediaKey(KeyEvent event) {
        final int keyCode = event.getKeyCode();
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    /* Suppress PLAY/PAUSE toggle when phone is ringing or
                     * in-call to avoid music playback */
                    if (mTelephonyManager == null) {
                        mTelephonyManager = (TelephonyManager) getContext().getSystemService(
                                Context.TELEPHONY_SERVICE);
                    }
                    if (mTelephonyManager != null &&
                            mTelephonyManager.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
                        return true;  // suppress key event
                    }
                case KeyEvent.KEYCODE_MUTE:
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_STOP:
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                case KeyEvent.KEYCODE_MEDIA_REWIND:
                case KeyEvent.KEYCODE_MEDIA_RECORD:
                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                case KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK: {
                    handleMediaKeyEvent(event);
                    return true;
                }

                case KeyEvent.KEYCODE_VOLUME_UP:
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                case KeyEvent.KEYCODE_VOLUME_MUTE: {
                    if (KEYGUARD_MANAGES_VOLUME) {
                        synchronized (this) {
                            if (mAudioManager == null) {
                                mAudioManager = (AudioManager) getContext().getSystemService(
                                        Context.AUDIO_SERVICE);
                            }
                        }
                        // Volume buttons should only function for music (local or remote).
                        // TODO: Actually handle MUTE.
                        mAudioManager.adjustSuggestedStreamVolume(
                                keyCode == KeyEvent.KEYCODE_VOLUME_UP
                                        ? AudioManager.ADJUST_RAISE
                                        : AudioManager.ADJUST_LOWER /* direction */,
                                AudioManager.STREAM_MUSIC /* stream */, 0 /* flags */);
                        // Don't execute default volume behavior
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_MUTE:
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                case KeyEvent.KEYCODE_MEDIA_STOP:
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                case KeyEvent.KEYCODE_MEDIA_REWIND:
                case KeyEvent.KEYCODE_MEDIA_RECORD:
                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                case KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK: {
                    handleMediaKeyEvent(event);
                    return true;
                }
            }
        }
        return false;
    }

    private void handleMediaKeyEvent(KeyEvent keyEvent) {
        synchronized (this) {
            if (mAudioManager == null) {
                mAudioManager = (AudioManager) getContext().getSystemService(
                        Context.AUDIO_SERVICE);
            }
        }
        mAudioManager.dispatchMediaKeyEvent(keyEvent);
    }

    @Override
    public void dispatchSystemUiVisibilityChanged(int visibility) {
        super.dispatchSystemUiVisibilityChanged(visibility);

        if (!(mContext instanceof Activity)) {
            setSystemUiVisibility(STATUS_BAR_DISABLE_BACK);
        }
    }

    /**
     * In general, we enable unlocking the insecure keyguard with the menu key. However, there are
     * some cases where we wish to disable it, notably when the menu button placement or technology
     * is prone to false positives.
     *
     * @return true if the menu key should be enabled
     */
    private static final String ENABLE_MENU_KEY_FILE = "/data/local/enable_menu_key";
    public boolean shouldEnableMenuKey() {
        final Resources res = getResources();
        final boolean configDisabled = res.getBoolean(R.bool.config_disableMenuKeyInLockScreen);
        final boolean isTestHarness = ActivityManager.isRunningInTestHarness();
        final boolean fileOverride = (new File(ENABLE_MENU_KEY_FILE)).exists();
        return !configDisabled || isTestHarness || fileOverride;
    }

    public void setViewMediatorCallback(ViewMediatorCallback viewMediatorCallback) {
        mViewMediatorCallback = viewMediatorCallback;
        // Update ViewMediator with the current input method requirements
        mViewMediatorCallback.setNeedsInput(mSecurityContainer.needsInput());
    }

    public void setLockPatternUtils(LockPatternUtils utils) {
        mLockPatternUtils = utils;
        mSecurityContainer.setLockPatternUtils(utils);
    }

    public SecurityMode getSecurityMode() {
        return mSecurityContainer.getSecurityMode();
    }

    public SecurityMode getCurrentSecurityMode() {
        return mSecurityContainer.getCurrentSecurityMode();
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        return super.onInterceptTouchEvent(event);
    }


    public void setKeyguardViewInitTranslationY() {
    	 setTranslationY(getHeight());
    }
    /**Mst: tangjun add end*/

    ///TCL Monster: kth add for keyguard inversion 20161022 start @{
    public void onWallpaperChanged(int RGB, float alpha) {
        mSecurityContainer.notifyWallpaperChanged(RGB, alpha);
    }
    ///TCL Monster: kth add for keyguard inversion 20161022 end @}

    ///TCL Monster: kth add for handle back event 20161114 start @{
    public void handleBackPressed() {
        mSecurityContainer.startCancelDownAnimation();
    }
    ///TCL Monster: kth add for handle back event 20161114 end @}

    ///TCL Monster: kth add for double tap to go sleep 20161122 start
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        handleDoubleTap(event);
        return super.onTouchEvent(event);
    }

    private int count = 0;
    private long firstClick = 0;
    private long lastClick = 0;

    // TODO: 16-11-22 use GestureDetector#OnDoubleTapListener instead
    private void handleDoubleTap(MotionEvent event) {
        if (!getContext().getResources().getBoolean(R.bool.config_enable_double_tap_sleep)) {
            return;
        }
        int timeout = getContext().getResources().getInteger(R.integer.config_double_tap_sleep_timeout);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (firstClick != 0 && System.currentTimeMillis() - firstClick > timeout) {
                count = 0;
            }
            count++;
            if (count == 1) {
                firstClick = System.currentTimeMillis();
            } else if (count == 2) {
                lastClick = System.currentTimeMillis();
                if (lastClick - firstClick < timeout) {
                    if (DEBUG) Log.d(TAG, "handleDoubleTap and go to sleep");
                    PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
                    pm.goToSleep(SystemClock.uptimeMillis());
                }
                clearTapState();
            }
        }
    }

    private void clearTapState() {
        count = 0;
        firstClick = 0;
        lastClick = 0;
    }
    ///TCL Monster: kth add for double tap to go sleep 20161122 end
}
