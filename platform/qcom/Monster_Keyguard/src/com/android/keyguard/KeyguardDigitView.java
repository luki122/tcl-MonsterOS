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

package com.android.keyguard;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.RenderNode;
import android.view.RenderNodeAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.TextView;
import android.widget.Button;

import com.android.internal.widget.LockPatternChecker;
import com.android.keyguard.utils.RippleUtils;
import com.android.keyguard.view.ShowDigitView;
import com.android.settingslib.animation.AppearAnimationUtils;
import com.android.settingslib.animation.DisappearAnimationUtils;
import com.android.keyguard.R;

import android.widget.Button;

/**
 * Displays a PIN pad for unlocking.
 */
public class KeyguardDigitView extends KeyguardAbsKeyInputView
	implements View.OnKeyListener {

	private final AppearAnimationUtils mAppearAnimationUtils;
	private final DisappearAnimationUtils mDisappearAnimationUtils;
	private ViewGroup mContainer;
	private ViewGroup mRow0;
	private ViewGroup mRow1;
	private ViewGroup mRow2;
    private ViewGroup mRow3;
    private int mDisappearYTranslation;
    private View[][] mViews;
    
    protected TextView mPasswordEntry;
    private Button mDeleteButton; // kth mod for digitview layout 20160902
    private NumPadKey mButton0;
    private NumPadKey mButton1;
    private NumPadKey mButton2;
    private NumPadKey mButton3;
    private NumPadKey mButton4;
    private NumPadKey mButton5;
    private NumPadKey mButton6;
    private NumPadKey mButton7;
    private NumPadKey mButton8;
    private NumPadKey mButton9;
    
    private ShowDigitView mShowDigitView;
    private View mMsgView;
    private View mNumPad;
    
    private boolean mDigitDismissing;
    private Interpolator mAppearInterpolator;

    public KeyguardDigitView(Context context) {
        this(context, null);
    }

    public KeyguardDigitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAppearAnimationUtils = new AppearAnimationUtils(context);
        mDisappearAnimationUtils = new DisappearAnimationUtils(context,
                125, 0.6f /* translationScale */,
                0.45f /* delayScale */, AnimationUtils.loadInterpolator(
                        mContext, android.R.interpolator.fast_out_linear_in));
        mDisappearYTranslation = getResources().getDimensionPixelSize(
                R.dimen.disappear_y_translation);
        mAppearInterpolator = AnimationUtils.loadInterpolator(context,
                android.R.interpolator.linear_out_slow_in);
    }
    
    @Override
    public void reset() {
        mPasswordEntry.requestFocus();
        mDigitDismissing = false;
        super.reset();
    }
    
    @Override
    protected void resetState() {
        setPasswordEntryEnabled(true);
        mSecurityMessageDisplay.setMessage(R.string.kg_password_instructions, true);
        mShowDigitView.onTextChange(0);
    }
    
    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        // send focus to the password field
        return mPasswordEntry.requestFocus(direction, previouslyFocusedRect);
    }
    
    @Override
    protected void setPasswordEntryEnabled(boolean enabled) {
        mPasswordEntry.setEnabled(enabled);
    }
    
    @Override
    protected void setPasswordEntryInputEnabled(boolean enabled) {
        mPasswordEntry.setEnabled(enabled);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_DEL) {
            performClick(mDeleteButton);
            return true;
        }
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            int number = keyCode - KeyEvent.KEYCODE_0 ;
            performNumberClick(number);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    protected int getPromtReasonStringRes(int reason) {
        switch (reason) {
            case PROMPT_REASON_RESTART:
                return R.string.kg_prompt_reason_restart_pin;
            case PROMPT_REASON_TIMEOUT:
                return R.string.kg_prompt_reason_timeout_pin;
            //TCL Monster: kth mod for prompt reason for digit bouncer 20161202 start @{
            case PROMPT_REASON_NONE:
                return 0;
            default:
                return R.string.kg_prompt_reason_timeout_pin;
            //end @}
        }
    }

    private void performClick(View view) {
        view.performClick();
    }

    private void performNumberClick(int number) {
        switch (number) {
            case 0:
                performClick(mButton0);
                break;
            case 1:
                performClick(mButton1);
                break;
            case 2:
                performClick(mButton2);
                break;
            case 3:
                performClick(mButton3);
                break;
            case 4:
                performClick(mButton4);
                break;
            case 5:
                performClick(mButton5);
                break;
            case 6:
                performClick(mButton6);
                break;
            case 7:
                performClick(mButton7);
                break;
            case 8:
                performClick(mButton8);
                break;
            case 9:
                performClick(mButton9);
                break;
        }
    }
    
    @Override
    protected void resetPasswordText(boolean animate, boolean announce) {
    	mPasswordEntry.setText("");
    }

    @Override
    protected String getPasswordText() {
        return mPasswordEntry.getText().toString();
    }

    @Override
    protected int getPasswordTextViewId() {
        return R.id.digitEntry;
    }
    
    /// kth mod for digitview layout 20160902 start @{
    private int getDeleteOrCancelStringRes(CharSequence s) {
        if(mPasswordEntry.getText().toString() != null && s.length() ==0) {
            return R.string.kg_bouncer_control_cancel;
        }else {
            return R.string.delete_button_str;
        }
    }
    /// end @}

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        
        mShowDigitView = (ShowDigitView) findViewById(R.id.show_digit_view);
        
        mPasswordEntry = (TextView) findViewById(getPasswordTextViewId());
        mPasswordEntry.setOnKeyListener(this);

        // Set selected property on so the view can send accessibility events.
        mPasswordEntry.setSelected(true);

        mDeleteButton = (Button) findViewById(R.id.delete_button);// kth mod for digitview layout 20160902
        mDeleteButton.setVisibility(View.VISIBLE);
        mDeleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // check for time-based lockouts
                /// kth mod for digitview layout 20160902 start @{
                if (mDeleteButton.getText().toString().equals(getContext().getString(R.string.kg_bouncer_control_cancel))) {
//                    mCallback.reset();/// kth mod 20161112
                    startCancelDownAnimation();
                    return;
                }///end @}
                if (mPasswordEntry.isEnabled()) {
                    CharSequence str = mPasswordEntry.getText();
                    if (str.length() > 0) {
                        mPasswordEntry.setText(str.subSequence(0, str.length() - 1));
                    }
                }
                doHapticKeyClick();
            }
        });
        mDeleteButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // check for time-based lockouts
                if (mPasswordEntry.isEnabled()) {
                    resetPasswordText(true /* animate */, false);
                }
                doHapticKeyClick();
                return true;
            }
        });
        
        //mPasswordEntry.setKeyListener(DigitsKeyListener.getInstance());
        mPasswordEntry.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        mPasswordEntry.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mDeleteButton.setText(getDeleteOrCancelStringRes(s));// kth mod for digitview layout 20160902
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                if (mShowDigitView != null) {
                    mShowDigitView.onTextChange(s.length());
                }
//                if (mCallback != null) {
//                    mCallback.userActivity();
//                }
                if (s.length() >= 4) {
                    verifyPasswordAndUnlock();
                }
            }
        });
        mMsgView = findViewById(R.id.keyguard_message_area);
        //mNumPad = findViewById(R.id.num_key);
        mButton0 = (NumPadKey)findViewById(R.id.key0);
        mButton1 = (NumPadKey)findViewById(R.id.key1);
        mButton2 = (NumPadKey)findViewById(R.id.key2);
        mButton3 = (NumPadKey)findViewById(R.id.key3);
        mButton4 = (NumPadKey)findViewById(R.id.key4);
        mButton5 = (NumPadKey)findViewById(R.id.key5);
        mButton6 = (NumPadKey)findViewById(R.id.key6);
        mButton7 = (NumPadKey)findViewById(R.id.key7);
        mButton8 = (NumPadKey)findViewById(R.id.key8);
        mButton9 = (NumPadKey)findViewById(R.id.key9);

        mPasswordEntry.requestFocus();

        mContainer = (ViewGroup) findViewById(R.id.container);
        mRow0 = (ViewGroup) findViewById(R.id.row0);
        mRow1 = (ViewGroup) findViewById(R.id.row1);
        mRow2 = (ViewGroup) findViewById(R.id.row2);
        mRow3 = (ViewGroup) findViewById(R.id.row3);
        /// TCL Monster: kth mod for appear animation 20161112 start
        mViews = new View[][]{
                new View[]{
                        mRow0, null, null
                },
                new View[]{
                        findViewById(R.id.key1), findViewById(R.id.key2),
                        findViewById(R.id.key3)
                },
                new View[]{
                        findViewById(R.id.key4), findViewById(R.id.key5),
                        findViewById(R.id.key6)
                },
                new View[]{
                        findViewById(R.id.key7), findViewById(R.id.key8),
                        findViewById(R.id.key9)
                },
                new View[]{
                        emcBtn, findViewById(R.id.key0), mDeleteButton
                },
        };
        /// TCL Monster: kth mod for appear animation 20161112 end
    }
    
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            onKeyDown(keyCode, event);
            return true;
        }
        return false;
    }

    @Override
    public void showUsabilityHint() {
    }

    @Override
    public int getWrongPasswordStringId() {
        return R.string.kg_wrong_password;
    }
    
    /*
    @Override
    protected void verifyPasswordAndUnlock() {
        mPasswordEntry.setEnabled(false);
        String entry = mPasswordEntry.getText().toString();
        if (mLockPatternUtils.checkPassword(entry)) {
            startAnim(false);
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCallback.reportSuccessfulUnlockAttempt();
                    mPasswordEntry.setEnabled(true);
                    mCallback.dismiss(true);
                }
            }, 250);
        } else if (entry.length() > MINIMUM_PASSWORD_LENGTH_BEFORE_REPORT) {
            // to avoid accidental lockout, only count attempts that are long enough to be a
            // real password. This may require some tweaking.
            mCallback.reportFailedUnlockAttempt();
            if (mCallback.getFailedAttempts() > 0
                    && 0 == (mCallback.getFailedAttempts() % LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT)) {
                long deadline = mLockPatternUtils.setLockoutAttemptDeadline();
                handleAttemptLockout(deadline);
            }
            mSecurityMessageDisplay.setMessage(getWrongPasswordStringId(), true);
            if (mShowDigitView != null) {
                mShowDigitView.onTextChange(5);
                Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
                mShowDigitView.startAnimation(anim);
            }
        }
        postDelayed(mClearTextRunnable, 600);
    }
    */
    
    @Override
    protected void verifyPasswordAndUnlock() {
        if (mDigitDismissing) return; // already verified but haven't been dismissed; don't do it again.

        final String entry = getPasswordText();
        setPasswordEntryInputEnabled(false);
        if (mPendingLockCheck != null) {
            mPendingLockCheck.cancel(false);
        }

        if (entry.length() <= MINIMUM_PASSWORD_LENGTH_BEFORE_REPORT) {
            // to avoid accidental lockout, only count attempts that are long enough to be a
            // real password. This may require some tweaking.
            setPasswordEntryInputEnabled(true);
            onPasswordChecked(false /* matched */, 0, false /* not valid - too short */);
            return;
        }

        mPendingLockCheck = LockPatternChecker.checkPassword(
                mLockPatternUtils,
                entry,
                KeyguardUpdateMonitor.getCurrentUser(),
                new LockPatternChecker.OnCheckCallback() {
                    @Override
                    public void onChecked(boolean matched, int timeoutMs) {
                        setPasswordEntryInputEnabled(true);
                        mPendingLockCheck = null;
                        onPasswordChecked(matched, timeoutMs, true /* isValidPassword */);
                    }
                });
    }

    private boolean isAttemptLockout;
    private void onPasswordChecked(boolean matched, int timeoutMs, boolean isValidPassword) {
        isAttemptLockout = false;
        setPasswordEntryInputEnabled(false);// TCL monster:kth add for fix digitview bug when click continuously during amimate and 600ms delay 20160906
        if (matched) {
            mDigitDismissing = true;
            mCallback.reportUnlockAttempt(KeyguardUpdateMonitor.getCurrentUser(), true, 0);
            mCallback.dismiss(true);
        } else {
            if (isValidPassword) {
                mCallback.reportUnlockAttempt(KeyguardUpdateMonitor.getCurrentUser(), false, timeoutMs);
                if (timeoutMs > 0) {
                    long deadline = mLockPatternUtils.setLockoutAttemptDeadline(
                            KeyguardUpdateMonitor.getCurrentUser(), timeoutMs);
                    isAttemptLockout = true;
                    handleAttemptLockout(deadline);
                }
            }
            if (timeoutMs == 0) {
                //String msg = getString(getWrongPasswordStringId());
                mSecurityMessageDisplay.setMessage(getWrongPasswordStringId(), true);
            }
            
            if (mShowDigitView != null) {
                mShowDigitView.onTextChange(5);
                Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
                mShowDigitView.startAnimation(anim);
            }
        }
        postDelayed(mClearTextRunnable, 600);
    }
    
    private Runnable mClearTextRunnable = new Runnable() {

        @Override
        public void run() {
            if(!isAttemptLockout) {
                setPasswordEntryInputEnabled(true);
            }
            resetPasswordText(true /* animate */, false);
            mSecurityMessageDisplay.setMessage(R.string.kg_password_instructions, true);
        }
    };

    /*@Override
    public void onPause() {
        super.onPause();
        if (mShowDigitView != null) {
            mShowDigitView.onTextChange(0);
        }
    }*/

    @Override
    public void startAppearAnimation() {
        enableClipping(false);
        /// TCL Monster: kth mod bouncer anim 20161114 start
        ObjectAnimator animatorTrans = ObjectAnimator.ofFloat(this, "TranslationY", mAppearAnimationUtils.getStartTranslation(), 0).setDuration(200);
        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f).setDuration(200);
        AnimatorSet animSet = new AnimatorSet();
        animSet.setInterpolator(mAppearInterpolator);
        animSet.playTogether(animatorTrans, animatorAlpha);
        animSet.start();
        enableClipping(true);
/*        setAlpha(1f);
        setTranslationY(mAppearAnimationUtils.getStartTranslation());
        AppearAnimationUtils.startTranslationYAnimation(this, 0 *//* delay *//*, 300 *//* duration *//*,
                0, mAppearAnimationUtils.getInterpolator());
        ///TCL Monster: kth remove mViews animation 20161114 start @{
        mAppearAnimationUtils.startAnimation2d(mViews,
                new Runnable() {
                    @Override
                    public void run() {
                        enableClipping(true);
                    }
                });*/
        enableClipping(true);
        ///end @}
    }

    @Override
    public boolean startDisappearAnimation(final Runnable finishRunnable) {
        enableClipping(false);
        setTranslationY(0);
        ///TCL Monster: kth mod for [BUG 3501362] 20161119 start
//        AppearAnimationUtils.startTranslationYAnimation(this, 0 /* delay */, 180 /* duration */,
//                mDisappearYTranslation, mDisappearAnimationUtils.getInterpolator());
//        mDisappearAnimationUtils.startAnimation2d(mViews,
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        enableClipping(true);
//                        if (finishRunnable != null) {
//                            finishRunnable.run();
//                        }
//                    }
//                });

        ObjectAnimator animatorTrans = ObjectAnimator.ofFloat(this, "TranslationY", 0, -200).setDuration(100);
        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(this, "alpha", getAlpha(), 0f).setDuration(100);
        AnimatorSet animSet = new AnimatorSet();
        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                enableClipping(true);
                if (finishRunnable != null) {
                    finishRunnable.run();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animSet.playTogether(animatorTrans, animatorAlpha);
        animSet.start();
        ///TCL Monster: kth mod for [BUG 3501362] 20161119 end
        return true;
    }

    /// TCL Monster: kth add for keyguard inversion 20161022 start @{
    @Override
    public void notifyWallpaperChanged(int RGB, float alpha) {
        mDeleteButton.setTextColor(RGB);
        emcBtn.setTextColor(RGB);
        mButton0.setTextColor(RGB);
        mButton1.setTextColor(RGB);
        mButton2.setTextColor(RGB);
        mButton3.setTextColor(RGB);
        mButton4.setTextColor(RGB);
        mButton5.setTextColor(RGB);
        mButton6.setTextColor(RGB);
        mButton7.setTextColor(RGB);
        mButton8.setTextColor(RGB);
        mButton9.setTextColor(RGB);
        setAlpha(alpha);
        mShowDigitView.setDotColor(RGB);
        mSecurityMessageDisplay.setMsgNomalColor(RGB, alpha);
    }
    /// end @}

    private void enableClipping(boolean enable) {
        mContainer.setClipToPadding(enable);
        mContainer.setClipChildren(enable);
        mRow1.setClipToPadding(enable);
        mRow2.setClipToPadding(enable);
        mRow3.setClipToPadding(enable);
        setClipChildren(enable);
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    /// TCL Monster: kth mod click ripple 20161101 start
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        mDeleteButton.setBackground(RippleUtils.getRippleDrawable(getContext(), mDeleteButton));
    }
    /// TCL Monster: kth mod click ripple 20161101 end

    private boolean isAnimating = false;
    @Override
    public void startCancelDownAnimation() {
        if (isAnimating) {
            return;
        }
        ObjectAnimator animatorTrans = ObjectAnimator.ofFloat(this, "TranslationY", 0, 200).setDuration(300);
        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(this, "alpha", getAlpha(), 0f).setDuration(300);
        AnimatorSet animSet = new AnimatorSet();
        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mCallback.reset();
                isAnimating = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animSet.playTogether(animatorTrans, animatorAlpha);
        animSet.start();
    }
}
