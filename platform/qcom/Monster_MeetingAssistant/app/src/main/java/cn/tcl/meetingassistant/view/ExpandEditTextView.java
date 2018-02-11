/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import cn.tcl.meetingassistant.log.MeetingLog;

/**
 * Created on 2016/8/28 0028.
 * The EditText you can expand or collapse
 */
public class ExpandEditTextView extends FixedEditText {

    private final String TAG = ExpandEditTextView.class.getSimpleName();

    private int mMinHeight  = 0 ;
    private int mMaxHeight;
    private boolean isAnimating = false;
    public boolean isExpand = false;
    private OnExpandAbleListener mOnExpandAbleListener;
    private OnExpandStatusChangedListener mOnExpandStatusChangedListener;
    private OnAnimatingListener mOnAnimatingListener;
    private boolean mSetExpand = false;

    public ExpandEditTextView(Context context) {
        super(context);
    }

    public ExpandEditTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        addTextChangeListener();
    }

    public ExpandEditTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        addTextChangeListener();
    }

    private void addTextChangeListener(){
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    boolean hasFirstMeature = true;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        if(!mSetExpand){
            getHeights();
            MeetingLog.i(TAG,"onMeasure mMinHeight " + mMinHeight);
            getLayoutParams().height = mMinHeight;
            mSetExpand = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        boolean lastStatus = isExpand;
        getHeights();
        int height = getHeight();
        MeetingLog.i(TAG,"onDraw height is " + height);
        MeetingLog.i(TAG,"onDraw mMinHeight is " + mMinHeight);
        if(getHeight() > mMinHeight){
            isExpand = true;
        }else {
            isExpand = false;
        }
        //MeetingLog.i(TAG,"is Expand " + isExpand);
        if(mOnExpandStatusChangedListener != null && lastStatus != isExpand){
            MeetingLog.i(TAG,"Expand status changes");
            mOnExpandStatusChangedListener.onExpandStatusChanged(isExpand);
        }

        if(mMaxHeight > mMinHeight && null != mOnExpandAbleListener){
            mOnExpandAbleListener.onExpandAble(true);
        }else if(mMaxHeight <= mMinHeight && null != mOnExpandAbleListener){
            mOnExpandAbleListener.onExpandAble(false);
        }
    }

    /**
     * expand
     * @param listener ths listener will be called at the moment of this view be expanded
     */
    public void expand(@Nullable final OnAnimatorEndListener listener){
        if(isAnimating){
            return;
        }
        isAnimating = true;
        ValueAnimator animation = ValueAnimator.ofFloat(0f,1f);

        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                MeetingLog.i(TAG,"expand onAnimationUpdate start");
                float value = (float) animation.getAnimatedValue();
                int deltaHeight =(int) (mMinHeight + value * (mMaxHeight - mMinHeight)) - getLayoutParams().height;
                getLayoutParams().height = (int) (mMinHeight + value * (mMaxHeight - mMinHeight));
                MeetingLog.i(TAG,"expand onAnimationUpdate height " + getLayoutParams().height);
                if(mOnAnimatingListener != null){
                    mOnAnimatingListener.onAnimating(deltaHeight);
                }
                requestLayout();
                MeetingLog.i(TAG,"expand onAnimationUpdate end");
            }
        });
        animation.setInterpolator(new LinearInterpolator());
        animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                MeetingLog.i(TAG,"expand onAnimationEnd start");
                isAnimating = false;
                if(null != listener)
                    listener.onEnd();
                setMaxLines(1000000);
                getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                MeetingLog.i(TAG,"expand onAnimationEnd end");
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animation.start();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        Log.i(TAG,"set Text min " + mMinHeight);
    }

    @Override
    public boolean callOnClick() {
        return super.callOnClick();
    }


    /**
     * collapse
     * @param listener ths listener will be called at the moment of this view be collapsed
     */
    public void collapse(@Nullable final OnAnimatorEndListener listener){
            if(isAnimating){
                return;
            }
            isAnimating = true;
            ValueAnimator animation = ValueAnimator.ofFloat(0f,1f);

            animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    MeetingLog.i(TAG,"collapse onAnimationUpdate start");
                    float value = (float) animation.getAnimatedValue();
                    int deltaHeight =(int) (mMinHeight + value * (mMaxHeight - mMinHeight)) - getLayoutParams().height;
                    getLayoutParams().height = (int) (mMaxHeight - value * (mMaxHeight - mMinHeight));
                    MeetingLog.i(TAG,"collapse onAnimationUpdate height " + getLayoutParams().height);
                    requestLayout();
                    if(mOnAnimatingListener != null){
                        mOnAnimatingListener.onAnimating(deltaHeight);
                    }
                    postInvalidate();
                    MeetingLog.i(TAG,"collapse onAnimationUpdate end");
                }

            });
            animation.setInterpolator(new LinearInterpolator());
            animation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    setSelection(0);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    MeetingLog.i(TAG,"collapse onAnimationEnd start");
                    isAnimating = false;
                    if(null != listener)
                        listener.onEnd();
                    postInvalidate();
                    MeetingLog.i(TAG,"collapse onAnimationEnd end");
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            animation.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_MOVE && !isExpand){
            return false;
        }
        return super.onTouchEvent(event);
    }


    public void setCollapse(){
        setMaxLines(getMinLines());
        mSetExpand = false;
    }

    public void setExpand(){
        setMaxLines(1000000000);
        mSetExpand = true;
    }

    public void setOnExpandAbleListener(OnExpandAbleListener listener){
        mOnExpandAbleListener = listener;
    }

    public void setOnExpandStatusChangedListener(OnExpandStatusChangedListener listener){
        mOnExpandStatusChangedListener = listener;
    }

    public void setmOnAnimatingListener(OnAnimatingListener mOnAnimatingListener) {
        this.mOnAnimatingListener = mOnAnimatingListener;
    }

    interface OnAnimatorEndListener{
        void onEnd();
    }

    interface OnExpandAbleListener{
        void onExpandAble(boolean expandable);
    }

    interface OnExpandStatusChangedListener{
        void onExpandStatusChanged(boolean isExpand);
    }

    interface OnAnimatingListener{
        void onAnimating(int height);
    }

    private void getHeights(){
        MeetingLog.i(TAG,"getHeights start minLines is " + getMinLines());
        MeetingLog.i(TAG,"getHeights text " + getTextString());
        if(getLineCount() <= getMinLines()){
            mMinHeight = getMeasuredHeight();
            mMaxHeight = getMeasuredHeight();
        }else {
            mMinHeight = (int) (getPaddingTop() + getLayout().getLineBottom(getMinLines() -1) - getLineSpacingExtra());
            mMaxHeight = getLayout().getLineBottom(getLineCount() -1 ) + getPaddingTop() + getPaddingBottom();
        }
        MeetingLog.i(TAG,"getHeights mMinHeight is " + mMinHeight);
        MeetingLog.i(TAG,"getHeights mMaxHeight is " + mMaxHeight);
        MeetingLog.i(TAG,"getHeights end");
    }
}
