package com.android.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.camera.CameraActivity;
import com.android.camera.debug.Log;
import com.tct.camera.R;

import java.util.ArrayList;

/**
 * Created by mec on 8/23/16.
 */
public class SelectTextViewLayout extends LinearLayout implements SeekBar.OnSeekBarChangeListener {
    private static final Log.Tag TAG = new Log.Tag("SelectTextViewLayout");

    private int mLength;
    private CharSequence[] mEntries;
    private LinearLayout mSelectLayout;
    private SeekBar mSeekBar;
    private float mSection;
    private int mProgressMax;
    private int mIndex = 0;
    private ArrayList<Integer> mTextViewWidth = new ArrayList<Integer>();
    private CameraActivity mActivity;
    private final int PROGRESS_MAX = 10000;

    public interface onTextViewChangeListener {
        void onSelectTextViewChanged(int index);
    }

    private onTextViewChangeListener mOnTextViewChangeListener;

    public SelectTextViewLayout(Context context) {
        this(context, null);
    }

    public SelectTextViewLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        if (context instanceof CameraActivity)
            mActivity = (CameraActivity) context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSelectLayout = (LinearLayout) findViewById(R.id.select_preference_text);
        mSeekBar = (SeekBar) findViewById(R.id.select_preference_seekbar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setMax(PROGRESS_MAX);
        mProgressMax = mSeekBar.getMax();
    }

    public void notifyDataChanged() {
        if (mSelectLayout != null && mSelectLayout.getChildCount() == 0) {
            mLength = getEntries().length;
            for (int i = 0; i < mLength; i++) {
                TextView item = (TextView) LayoutInflater.from(mActivity).inflate(R.layout.select_preference_item,
                        mSelectLayout, false);
                item.setText(getEntries()[i]);
                if (i == 0) {
                    item.setGravity(Gravity.LEFT);
                } else if (i == mLength - 1) {
                    item.setGravity(Gravity.RIGHT);
                } else {
                    item.setGravity(Gravity.CENTER);
                }
                mSelectLayout.addView(item);
            }
            mSection = mProgressMax * 1.0f / (mLength - 1);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int measureHeight = MeasureSpec.getSize(heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (measureWidth == 0 || measureHeight == 0) {
            return;
        }
        int childCount = mSelectLayout.getChildCount();
        int left = getTextViewMarginLeft();
        for (int i = 0; i < childCount; i++) {
            View child = mSelectLayout.getChildAt(i);
            if (i > 0) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                lp.leftMargin = left;
                child.setLayoutParams(lp);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        setTextColorFormIndex(getIndex());
        setProgressFormIndex(getIndex());
    }

    private int getTextViewMarginLeft() {
        return (int) Math.floor((mSelectLayout.getMeasuredWidth() - getTextViewWidthLength()) / (float) (mLength - 1));
    }

    private int getSelectLayoutRealWidth() {
        return getTextViewWidthLength() + getTextViewMarginLeft() * (mLength - 1);
    }

    private int getCertainTextViewWidth(int index) {
        if (mTextViewWidth.size() > index) {
            return mTextViewWidth.get(index);
        } else {
            int childCount = mSelectLayout.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = mSelectLayout.getChildAt(i);
                int width = child.getMeasuredWidth();
                mTextViewWidth.add(width);
            }
            return mTextViewWidth.get(index);
        }
    }

    private int getTextViewWidthLength() {
        int totalViewWidthBeforeCurrent = 0;
        for (int i = 0; i < mLength; i++) {
            totalViewWidthBeforeCurrent += getCertainTextViewWidth(i);
        }
        return totalViewWidthBeforeCurrent;
    }

    private float getCertainTextViewCenter(int index) {
        return mSelectLayout.getChildAt(index).getLeft() + getCertainTextViewWidth(index) / 2.0f;
    }

    private int getProgressAccordingToIndex(int index) {
        float certainCenter = getCertainTextViewCenter(index);
        return Math.round(1.0f * (certainCenter / getSelectLayoutRealWidth()) * (mProgressMax));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int index;
        index = getIndexFormSelectedTextView(seekBar.getProgress());
        setProgressFormIndex(index);
        if (index != mIndex) {
            mIndex = index;
            setTextColorFormIndex(index);
            mOnTextViewChangeListener.onSelectTextViewChanged(index);
        }
    }

    public int getIndexFormSelectedTextView(int progress) {
        float start = mSection / 2;
        int index = 0;
        if (progress <= start) {
            index = 0;
        } else if (progress > (mLength - 2) * mSection + start) {
            index = mLength - 1;
        } else {
            for (int i = 0; i < mLength - 2; i++) {
                if (progress > (start + i * mSection) && progress <= (start + (i + 1) * mSection)) {
                    index = i + 1;
                }
            }
        }
        return index;
    }

    public void setTextColorFormIndex(int index) {
        for (int i = 0; i < mLength; i++) {
            TextView item = (TextView) mSelectLayout.getChildAt(i);
            if (item != null) {
                if (i == index) {
                    item.setTextColor(getContext().getResources().getColor(R.color.mode_name_text_color_selected));
                } else {
                    item.setTextColor(getContext().getResources().getColor(R.color.mode_name_text_color_unselected));
                }
            }
        }
    }

    public void setProgressFormIndex(int index) {
        if (index == 0) {
            mSeekBar.setProgress(0);
        } else if (index == mLength - 1) {
            mSeekBar.setProgress(mProgressMax);
        } else {
            int indexProgress = getProgressAccordingToIndex(index);
            mSeekBar.setProgress(indexProgress);
        }
    }

    public void setTextViewChangeListener(onTextViewChangeListener l) {
        mOnTextViewChangeListener = l;
    }

    public void setIndex(int index) {
        mIndex = index;
    }

    public int getIndex() {
        return mIndex;
    }

    public CharSequence[] getEntries() {
        return mEntries;
    }

    public void setEntries(CharSequence[] timerEntries) {
        mEntries = timerEntries;
        notifyDataChanged();
    }
}