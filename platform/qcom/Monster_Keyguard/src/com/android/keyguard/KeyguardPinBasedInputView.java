/*
 * Copyright (C) 2014 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.keyguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.android.keyguard.utils.MstMainColorHelper;
import com.android.keyguard.utils.RippleUtils;

/**
 * A Pin based Keyguard input view
 */
public abstract class KeyguardPinBasedInputView extends KeyguardAbsKeyInputView
        implements View.OnKeyListener {

    protected PasswordTextView mPasswordEntry;
    ///TCL Monster: kth mod for sim pin puk inversion 20161117 start
    private Button mOkButton;
    private ImageButton mDeleteButton;
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
    ///TCL Monster: kth mod for sim pin puk inversion 20161117 end

    public KeyguardPinBasedInputView(Context context) {
        this(context, null);
    }

    public KeyguardPinBasedInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void reset() {
        mPasswordEntry.requestFocus();
        super.reset();
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        // send focus to the password field
        return mPasswordEntry.requestFocus(direction, previouslyFocusedRect);
    }

    @Override
    protected void resetState() {
        setPasswordEntryEnabled(true);
        updateMainColor();///TCL Monster: kth mod for sim pin puk inversion 20161117
    }

    @Override
    protected void setPasswordEntryEnabled(boolean enabled) {
        mPasswordEntry.setEnabled(enabled);
        mOkButton.setEnabled(enabled);
    }

    @Override
    protected void setPasswordEntryInputEnabled(boolean enabled) {
        mPasswordEntry.setEnabled(enabled);
        mOkButton.setEnabled(enabled);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.isConfirmKey(keyCode)) {
            performClick(mOkButton);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DEL) {
            performClick(mDeleteButton);
            return true;
        }
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            int number = keyCode - KeyEvent.KEYCODE_0;
            performNumberClick(number);
            return true;
        }
        if (keyCode >= KeyEvent.KEYCODE_NUMPAD_0 && keyCode <= KeyEvent.KEYCODE_NUMPAD_9) {
            int number = keyCode - KeyEvent.KEYCODE_NUMPAD_0;
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
            case PROMPT_REASON_DEVICE_ADMIN:
                return R.string.kg_prompt_reason_device_admin;
            case PROMPT_REASON_USER_REQUEST:
                return R.string.kg_prompt_reason_user_request;
            case PROMPT_REASON_NONE:
                return 0;
            default:
                return R.string.kg_prompt_reason_timeout_pin;
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
        mPasswordEntry.reset(animate, announce);
    }

    @Override
    protected String getPasswordText() {
        return mPasswordEntry.getText();
    }

    @Override
    protected void onFinishInflate() {
        mPasswordEntry = (PasswordTextView) findViewById(getPasswordTextViewId());
        mPasswordEntry.setOnKeyListener(this);

        // Set selected property on so the view can send accessibility events.
        mPasswordEntry.setSelected(true);

        mPasswordEntry.setUserActivityListener(new PasswordTextView.UserActivityListener() {
            @Override
            public void onUserActivity() {
                onUserInput();
            }
        });

        mOkButton = (Button)findViewById(R.id.key_enter);///TCL Monster: kth mod for sim pin puk inversion 20161117
        if (mOkButton != null) {
            mOkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doHapticKeyClick();
                    if (mPasswordEntry.isEnabled()) {
                        verifyPasswordAndUnlock();
                    }
                }
            });
            mOkButton.setOnHoverListener(new LiftToActivateListener(getContext()));
        }

        mDeleteButton = (ImageButton)findViewById(R.id.delete_button);///TCL Monster: kth mod for sim pin puk inversion 20161117
        mDeleteButton.setVisibility(View.VISIBLE);
        mDeleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // check for time-based lockouts
                if (mPasswordEntry.isEnabled()) {
                    mPasswordEntry.deleteLastChar();
                }
                doHapticKeyClick();
            }
        });
        mDeleteButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // check for time-based lockouts
                if (mPasswordEntry.isEnabled()) {
                    resetPasswordText(true /* animate */, true /* announce */);
                }
                doHapticKeyClick();
                return true;
            }
        });
        ///TCL Monster: kth mod for sim pin puk inversion 20161117 start
        mButton0 = (NumPadKey) findViewById(R.id.key0);
        mButton1 = (NumPadKey) findViewById(R.id.key1);
        mButton2 = (NumPadKey) findViewById(R.id.key2);
        mButton3 = (NumPadKey) findViewById(R.id.key3);
        mButton4 = (NumPadKey) findViewById(R.id.key4);
        mButton5 = (NumPadKey) findViewById(R.id.key5);
        mButton6 = (NumPadKey) findViewById(R.id.key6);
        mButton7 = (NumPadKey) findViewById(R.id.key7);
        mButton8 = (NumPadKey) findViewById(R.id.key8);
        mButton9 = (NumPadKey) findViewById(R.id.key9);
        ///TCL Monster: kth mod for sim pin puk inversion 20161117 end
        mPasswordEntry.requestFocus();
        super.onFinishInflate();
        updateMainColor();///TCL Monster: kth mod for sim pin puk inversion 20161117
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            return onKeyDown(keyCode, event);
        }
        return false;
    }

    /// TCL Monster: kth mod click ripple 20161101 start
    // TODO: 16-11-1 the method not called on first start
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        mOkButton.setBackground(RippleUtils.getRippleDrawable(getContext(), mOkButton));
    }
    /// TCL Monster: kth mod click ripple 20161101 end

    ///TCL Monster: kth mod for sim pin puk inversion 20161117 start
    @Override
    public void notifyWallpaperChanged(int RGB, float alpha) {
        mOkButton.setTextColor(RGB);
        mDeleteButton.setColorFilter(RGB);
        mPasswordEntry.setDrawPaintColor(RGB);
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
        mSecurityMessageDisplay.setMsgNomalColor(RGB, alpha);
    }

    private void updateMainColor() {
        KgLockscreenWallpaper mLockscreenWallpaper = new KgLockscreenWallpaper(mContext, getHandler());
        Bitmap bitmap = mLockscreenWallpaper.getBitmap();
        if (mLockscreenWallpaper != null && bitmap != null) {// kth mod for [BUG 3534661] 20161121
            boolean isDark = MstMainColorHelper.isTextColorDark(bitmap, mContext);
            if (isDark) {
                notifyWallpaperChanged(Color.BLACK, .8f);
            } else {
                notifyWallpaperChanged(Color.WHITE, 1f);
            }
        } else {
            notifyWallpaperChanged(Color.BLACK, .8f);// if lockwallpapaer is null, keep main color with BLACK according to default wallpaper is White
        }
    }
    ///TCL Monster: kth mod for sim pin puk inversion 20161117 end
}
