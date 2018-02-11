package com.android.gallery3d.filtershow;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

import com.android.gallery3d.R;

public class TopBarManager {
    
    private Button mCancelButton;
    private Button mSaveButton;
    
    private Button mResetButton;
    
    private ImageButton mApplyButton;
    private ImageButton mCancelApplyButton;
    
    public static final int MODE_CANCEL_SAVE = 0;
    public static final int MODE_CANCEL_APPLY = 1;
    
    private int mCurrentMode;
    
    public TopBarManager(View root) {
        
        mCancelButton = (Button)root.findViewById(R.id.cancel_btn);
        mSaveButton = (Button)root.findViewById(R.id.save_btn);
        
        mApplyButton = (ImageButton)root.findViewById(R.id.apply_btn);
        mCancelApplyButton = (ImageButton)root.findViewById(R.id.cancel_apply_btn);
        
        mResetButton = (Button)root.findViewById(R.id.reset_btn);
    }
    
    public void setOnClickListener(OnClickListener listener) {
        mCancelButton.setOnClickListener(listener);
        mSaveButton.setOnClickListener(listener);
        
        mApplyButton.setOnClickListener(listener);
        mCancelApplyButton.setOnClickListener(listener);
        
        mResetButton.setOnClickListener(listener);
    }
    
    private void showCancelSaveButtons(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        mCancelButton.setVisibility(visibility);
        mSaveButton.setVisibility(visibility);
    }
    
    private void showCancelApplyButtons(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        mCancelApplyButton.setVisibility(visibility);
        mApplyButton.setVisibility(visibility);
    }
    
    public void showResetButton(boolean show) {
        if(mCurrentMode == MODE_CANCEL_SAVE) show = false;
        int visibility = show ? View.VISIBLE : View.GONE;
        mResetButton.setVisibility(visibility);
    }
    
    public void switchMode(int mode) {
        mCurrentMode = mode;
        switch(mode) {
        case MODE_CANCEL_SAVE:
            showCancelSaveButtons(true);
            showCancelApplyButtons(false);
            showResetButton(false);
            break;
        case MODE_CANCEL_APPLY:
            showCancelSaveButtons(false);
            showCancelApplyButtons(true);
            showResetButton(false);
            break;
        }
    }
    
    public int getMode() {
        return mCurrentMode;
    }
    
    public void enableResetButton(boolean enabled) {
        mResetButton.setEnabled(enabled);
    }
    
    public void enableSaveButton(boolean enabled) {
        mSaveButton.setEnabled(enabled);
    }
    
    public void enableApplyButton(boolean enabled) {
        mApplyButton.setEnabled(enabled);
    }
}
