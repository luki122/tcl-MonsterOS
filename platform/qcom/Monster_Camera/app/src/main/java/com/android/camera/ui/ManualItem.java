package com.android.camera.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.camera.AnimationManager;
import com.android.camera.ManualUI;
import com.android.camera.test.TestUtils; // MODIFIED by wenhua.tu, 2016-08-11,BUG-2710178
import com.tct.camera.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ManualItem extends LinearLayout implements View.OnClickListener, Rotatable, CustomSeekBar.EnableStateChangedCallback {
    private static final String TAG = "ManualItem";

    public static final int MANUAL_SETTING_ISO = 0;
    public static final int MANUAL_SETTING_EXPOSURE = 1;
    public static final int MANUAL_SETTING_WHITE_BALANCE = 2;
    public static final int MANUAL_SETTING_FOCUS_POS = 3;

    private final int SCALE_FACTOR = 10;
    private final String INFINITE_CONSTANT = "\u221e";

    private int mManualSettingType;
    private ManualStateChangeListener mListener;

    private ImageView mSettingName;
    private TextView mSettingValue;
    private LinearLayout mRoot;
    //private TextView mTextAuto;
    private LinearLayout mAutoLayout;
    private ImageView mAutoIndicator;
    private SeekBar mSeekBar;
    private CustomSeekBar mSeekBarLayout;
    private LinearLayout mProgressView;
    private LayoutInflater mInflater;
    private RotateLayout mAutoRotate;
    private RotateLayout mItemRotate;
    private final Handler mHandler = new MainHandler();
    private static final int HIDE_MANUAL_PROGRESS = 0;
    private static final int UPDATE_MANUAL_SETTING = 1;
    private static final int HIDE_MANUAL_PROGRESS_DELAY = 3000;
    private static final int UPDATE_MANUAL_SETTING_DELAY = 100;
    private ArrayList<Integer> mISOValues;
    private ArrayList<String> mExposureTimeDouble;
    private ArrayList<String> mExposureTimeTitle;
    private int mMinFocusPos;
    private int mMaxFocusPos;
    private ArrayList<String> mWBValues;
    private ArrayList<String> mWBTitles;
    private String mCurWBState;
    private boolean mAuto;
    private int mProgress;
    private int mIndex;
    private int mMin;
    private int mMax;
    public ManualItem(Context context) {
        super(context);
        init(context);
    }

    public ManualItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ManualItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context);
    }
    @Override
    public void setOrientation(int degree, boolean animation) {
        mAutoRotate.setRotation(-degree);
        mItemRotate.setRotation(-degree);
    }
    @Override
    public void onEnableStateChanged(boolean enable) {
        if (enable && mAuto) {
            mAuto = false;
            updateUI();
            if (mListener != null) {
                mListener.onManualSettingChanged(mManualSettingType, mProgress, mIndex, mAuto);
            }
        }
    }
    private void init(Context context) {
        mInflater = LayoutInflater.from(context);
        View v = mInflater.inflate(R.layout.manual_item, this);
        mSettingName = (ImageView) v.findViewById(R.id.item_title);
        mSettingValue = (TextView) v.findViewById(R.id.description_item);
        mRoot = (LinearLayout) v.findViewById(R.id.item_root);
        // mTextAuto = (TextView) v.findViewById(R.id.auto);
        mAutoLayout = (LinearLayout) v.findViewById(R.id.auto_layout);
        mAutoIndicator = (ImageView) v.findViewById(R.id.auto);
        mAutoRotate = (RotateLayout) v.findViewById(R.id.rotateauto);
        mItemRotate = (RotateLayout) v.findViewById(R.id.manul_mode_rotatelayout);
        mSeekBar = (SeekBar) v.findViewById(R.id.customseekbar);
        mSeekBarLayout = (CustomSeekBar) v.findViewById(R.id.seekbar);
        mSeekBarLayout.setEnableOnTouch(true, this);
        mProgressView = (LinearLayout) v.findViewById(R.id.manual_progress_view);
        mRoot.setOnClickListener(this);
        // mTextAuto.setOnClickListener(this);
        mAutoLayout.setOnClickListener(this);
    }

    public void initType(ManualStateChangeListener l, ArrayList<Integer> values, String curState) {
        mISOValues = new ArrayList<>();
        mISOValues.addAll(values);
        initType(l, MANUAL_SETTING_ISO, curState);
    }

    public void initType(ManualStateChangeListener l, int settingType,ArrayList<String> values, ArrayList<String> titleList, String curState) {
        if (settingType == MANUAL_SETTING_EXPOSURE) {
            mExposureTimeDouble = new ArrayList<>();
            mExposureTimeTitle = new ArrayList<>();
            mExposureTimeDouble.addAll(values);
            mExposureTimeTitle.addAll(titleList);
        } else if (settingType == MANUAL_SETTING_WHITE_BALANCE) {
            mWBTitles = new ArrayList<>();
            mWBValues = new ArrayList<>();
            mWBValues.addAll(values);
            mWBTitles.addAll(titleList);
        }
        initType(l, settingType, curState);
    }

    public void initType(ManualStateChangeListener l, int min, int max, String curState) {
        mMinFocusPos = min;
        mMaxFocusPos = max;
        initType(l, MANUAL_SETTING_FOCUS_POS, curState);
    }

    public void initType(ManualStateChangeListener l, int settingType, String curState) {
        mManualSettingType = settingType;
        mProgress = 0;
        mAuto = true;
        mIndex = 0;
        if (curState != null) {
            try{
                JSONObject job=new JSONObject(curState);
                mAuto = (boolean) job.get(ManualUI.SETTING_AUTO);
                mProgress = (int) job.get(ManualUI.SETTING_PROGRESS);
                mIndex = (int) job.get(ManualUI.SETTING_INDEX);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "initType " + mManualSettingType + ", " + mAuto + ", " + mIndex + ", " + mProgress + ", " + curState);
        switch (mManualSettingType) {
            case MANUAL_SETTING_ISO:
                mSettingName.setImageResource(R.drawable.ic_manual_setting_iso);
                mSettingName.setImageLevel(0);
                mMin = 0;
                mMax = (mISOValues.size()-1) * SCALE_FACTOR;
                break;
            case MANUAL_SETTING_EXPOSURE:
                mSettingName.setImageResource(R.drawable.ic_manual_setting_shutter);
                mSettingName.setImageLevel(0);
                mMin = 0;
                mMax = (mExposureTimeDouble.size()-1) * SCALE_FACTOR;
                break;
            case MANUAL_SETTING_WHITE_BALANCE:
                mSettingName.setImageResource(R.drawable.ic_manual_setting_wb);
                mSettingName.setImageLevel(0);
                mMin = 0;
                mMax = (mWBValues.size()-1) * SCALE_FACTOR;
                break;
            case MANUAL_SETTING_FOCUS_POS:
                mSettingName.setImageResource(R.drawable.ic_manual_setting_focus);
                mSettingName.setImageLevel(0);
                mMin = mMinFocusPos + 1;
                mMax = mMaxFocusPos;
                break;
        }
        mSeekBar.setMax(mMax - mMin);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHandler.removeMessages(HIDE_MANUAL_PROGRESS);
                mHandler.sendEmptyMessageDelayed(HIDE_MANUAL_PROGRESS, HIDE_MANUAL_PROGRESS_DELAY);
                mHandler.removeMessages(UPDATE_MANUAL_SETTING);
                mHandler.sendEmptyMessage(UPDATE_MANUAL_SETTING);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeMessages(HIDE_MANUAL_PROGRESS);
                cancelAnimateHide();
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                mProgress = progress;
                setSettingValue();
                mHandler.removeMessages(UPDATE_MANUAL_SETTING);
                mHandler.sendEmptyMessageDelayed(UPDATE_MANUAL_SETTING, UPDATE_MANUAL_SETTING_DELAY);
            }
        });
        mSeekBar.setProgress(mProgress);
        mListener = l;
        mListener.onManualSettingChanged(mManualSettingType, mProgress, mIndex, mAuto);
        updateUI();
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.item_root) {
            if (mProgressView.getVisibility() == View.VISIBLE) {
                mSettingName.setImageLevel(0);
                mSettingValue.setTextColor(getResources().getColor(R.color.manual_item_normal));
                animateHide();
                mHandler.removeMessages(HIDE_MANUAL_PROGRESS);
            } else {
                mSettingName.setImageLevel(1);
                mSettingValue.setTextColor(getResources().getColor(R.color.manual_item_selected));
                animateShow();
                mHandler.removeMessages(HIDE_MANUAL_PROGRESS);
                mHandler.sendEmptyMessageDelayed(HIDE_MANUAL_PROGRESS, HIDE_MANUAL_PROGRESS_DELAY);
            }
            if (mListener != null) {
                mListener.onVisibilityChanged(mManualSettingType);
            }
        } else if (id == R.id.auto_layout) {
            mHandler.removeMessages(HIDE_MANUAL_PROGRESS);
            mHandler.sendEmptyMessageDelayed(HIDE_MANUAL_PROGRESS, HIDE_MANUAL_PROGRESS_DELAY);
            mAuto = !mAuto;
            updateUI();
            if (mListener != null) {
                mListener.onManualSettingChanged(mManualSettingType, mProgress, mIndex, mAuto);
            }
        }
    }
    private void setSettingValue() {
        String settingValue = null;
        mIndex = mProgress / SCALE_FACTOR;
        switch (mManualSettingType) {
            case MANUAL_SETTING_ISO:
                settingValue = mISOValues.get(mIndex)+"";
                break;
            case MANUAL_SETTING_EXPOSURE:
                settingValue = mExposureTimeTitle.get(mIndex);
                break;
            case MANUAL_SETTING_WHITE_BALANCE:
                settingValue = mWBTitles.get(mIndex);
                break;
            case MANUAL_SETTING_FOCUS_POS:
                mIndex = mProgress + mMin;
                if (mProgress == (mMax-mMin)) {
                    settingValue = INFINITE_CONSTANT;
                } else {
                    settingValue = mProgress+mMin+"";
                }
                break;
        }
        if (settingValue != null) {
            mSettingValue.setText(settingValue);
        }
    }

    private void updateUI() {
        mSeekBar.setEnabled(!mAuto);
        if (mAuto) {
            //mTextAuto.setBackgroundResource(R.drawable.manual_gridview_circle_bg_pressed);
            mAutoIndicator.setImageResource(R.drawable.ic_manual_auto_normal);
            mSettingValue.setText(R.string.pref_camera_iso_s_f_entry_auto);
            return;
        }
        //mTextAuto.setBackgroundResource(R.drawable.manual_gridview_circle_bg);
        mAutoIndicator.setImageResource(R.drawable.ic_manual_auto_active);
        setSettingValue();
    }

    private ValueAnimator mHideSeekbarAnimator;
    private ValueAnimator mShowSeekbarAnmator;

    private void animateHide(){
        if(mProgressView.getVisibility()==View.INVISIBLE){
            return;
        }
        if(mHideSeekbarAnimator ==null|| mShowSeekbarAnmator ==null){
            mShowSeekbarAnmator = AnimationManager.buildShowingAnimator(mProgressView);
            mHideSeekbarAnimator = AnimationManager.buildHidingAnimator(mProgressView);
        }
        if(mShowSeekbarAnmator.isRunning()){
            mShowSeekbarAnmator.cancel();
        }
        if(!mHideSeekbarAnimator.isRunning()){
            mHideSeekbarAnimator.start();
        }
    }

    private void cancelAnimateHide(){
        if(mHideSeekbarAnimator != null && mHideSeekbarAnimator.isRunning()) {
            mHideSeekbarAnimator.cancel();
            mProgressView.setAlpha(1.0f);
            if (mListener != null) {
                mListener.onVisibilityChanged(mManualSettingType);
            }
        }
    }

    private void animateShow(){
        if(mProgressView.getVisibility()==View.VISIBLE){
            return;
        }
        if(mHideSeekbarAnimator ==null|| mShowSeekbarAnmator ==null){
            mShowSeekbarAnmator = AnimationManager.buildShowingAnimator(mProgressView);
            mHideSeekbarAnimator = AnimationManager.buildHidingAnimator(mProgressView);
        }
        if(mHideSeekbarAnimator.isRunning()){
            mHideSeekbarAnimator.cancel();
        }

        if(!mShowSeekbarAnmator.isRunning()){
            mShowSeekbarAnmator.start();
        }
    }

    public void resetView() {
        mHandler.removeMessages(HIDE_MANUAL_PROGRESS);
        animateHide();
        mSettingName.setImageLevel(0);
        mSettingValue.setTextColor(getResources().getColor(R.color.manual_item_normal));
    }

    public interface ManualStateChangeListener {
        public void onManualSettingChanged(int settingType, int progressValue, int index, boolean auto);

        public void onVisibilityChanged(int settingType);
    }

    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            if (message.what == HIDE_MANUAL_PROGRESS) {
                resetView();
            } else if (message.what == UPDATE_MANUAL_SETTING) {
                if (mListener != null) {
                    mListener.onManualSettingChanged(mManualSettingType, mProgress, mIndex, mAuto);
                }
            }
        }
    }
/* MODIFIED-BEGIN by wenhua.tu, 2016-08-11,BUG-2710178*/

    public ArrayList getValues() {
        if (TestUtils.IS_TEST) {
            switch (mManualSettingType) {
                case MANUAL_SETTING_ISO:
                    return mISOValues;

                case MANUAL_SETTING_EXPOSURE:
                    return mExposureTimeDouble;

                case MANUAL_SETTING_WHITE_BALANCE:
                    return mWBValues;

                case MANUAL_SETTING_FOCUS_POS:
                    ArrayList list = new ArrayList<>();
                    list.add(mMin);
                    list.add(mMax);
                    list.add(INFINITE_CONSTANT);
                    return list;
            }
        }

        return null;
    }

    public ArrayList getTitles() {
        if (TestUtils.IS_TEST) {
            switch (mManualSettingType) {
                case MANUAL_SETTING_ISO:
                    return mISOValues;

                case MANUAL_SETTING_EXPOSURE:
                    return mExposureTimeTitle;

                case MANUAL_SETTING_WHITE_BALANCE:
                    return mWBTitles;

                case MANUAL_SETTING_FOCUS_POS:
                    ArrayList list = new ArrayList<>();
                    list.add(mMin);
                    list.add(mMax);
                    list.add(INFINITE_CONSTANT);
                    return list;
            }
        }

        return null;
    }

    public int getScaleFactor() {
        if (TestUtils.IS_TEST) {
            return SCALE_FACTOR;
        }
        return -1;
    }
    /* MODIFIED-END by wenhua.tu,BUG-2710178*/
}
