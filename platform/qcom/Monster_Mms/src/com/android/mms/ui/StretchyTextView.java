package com.android.mms.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.telephony.PhoneNumberUtils;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.BufferType;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector;
import android.view.View.OnClickListener;
import com.android.mms.R;

/** 
 * tangyisen add
 */  
public class StretchyTextView extends LinearLayout implements View.OnClickListener{

    //type:send and recv
    public static final int TYPE_SEND = 0;
    public static final int TYPE_RECV = 1;

    private CheckOverSizeTextView contentText;
    private TextView operateText;
    private LinearLayout bottomTextLayout;

    private String shrinkup;
    private String spread;
    private int maxLineCount = 15;

    private boolean mIsSend;
    private boolean mOperateExisted;

    private OnDoubleClickListener mOnDoubleClickListener;
    private OnSingleClickListener mOnSingleClickListener;

    private GestureDetector mGestureDetector;
    private MyGestureListener mMyGestureListener = new MyGestureListener();

    private String mCurrClickUrl = null;
    private URLSpan mCurrClickSpan = null;

    public StretchyTextView(Context context) {
        this(context, null);
    }

    public StretchyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.StretchyTextView);
        int type = typedArray.getInt(R.styleable.StretchyTextView_strtype, TYPE_SEND);
        shrinkup = context.getString(R.string.retract);
        spread = context.getString(R.string.message_expand);
        View view = null;
        if (TYPE_SEND == type) {
            mIsSend = true;
            view = inflate(context, R.layout.stretchy_text_send_layout, this);
        } else {
            mIsSend = false;
            view = inflate(context, R.layout.stretchy_text_recv_layout, this);
        }
        contentText = (CheckOverSizeTextView) view.findViewById(R.id.content_textview);
        contentText.setOnOverLineChangedListener(new CheckOverSizeTextView.OnOverSizeChangedListener() {
            @Override
            public void onChanged(boolean isOverSize) {
                // TODO Auto-generated method stub
                if(isOverSize){
                    operateText.setVisibility(View.VISIBLE);
                    operateText.setText(spread);
                } else if (!mOperateExisted) {
                    operateText.setVisibility(View.GONE);
                }
                //contentText.setIsCallChangedListener(false);
            }
        });
        operateText = (TextView) view.findViewById(R.id.bottom_textview);
        bottomTextLayout = (LinearLayout) view.findViewById(R.id.bottom_text_layout);
        setBottomTextGravity(Gravity.LEFT);
        operateText.setOnClickListener(this);
        setOnClickListener(this);
        //setOnLongClickListener(null);
    }

    private boolean touchFlag = false;
    private boolean mIsSpread = false;

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        if (view == operateText) {
            mOperateExisted = true;
            if(contentText.isOverSize() && !mIsSpread) {
                contentText.displayAll();
                operateText.setText(shrinkup);
                mIsSpread = true;
            } else {
                contentText.hide(maxLineCount);
                operateText.setText(spread);
                mIsSpread = false;
            }
        }
    }

    public void reset() {
        mOperateExisted = false;
        contentText.hide(maxLineCount);
        mIsSpread = false;
        touchFlag = false;
        touchFlag2 = false;
        setContent(null);
    }

    public final void setContent(CharSequence charSequence) {
        //operateText.setVisibility(View.GONE);
        contentText.setText(charSequence);
        stripUnderlines(contentText);
    }

   public TextView getContentView() {
       return (TextView)contentText ;
   }

    public void setMaxLineCount(int maxLineCount) {
        this.maxLineCount = maxLineCount;
    }

    public void setContentTextColor(int color){
        this.contentText.setTextColor(color);
    }

    public void setContentTextSize(float size){
        this.contentText.setTextSize(size);
    }

    /** 
     * 设置展开标识的显示位置 
     * @param gravity 
     */
    public void setBottomTextGravity(int gravity){
        bottomTextLayout.setGravity(gravity);
    }

    /*private boolean mCheckEnable = false;

    public void setCheckEnable(boolean flag) {
        mCheckEnable = flag;
    }

    public boolean getCheckEnable() {
        return mCheckEnable;
    }*/

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        //return super.onInterceptTouchEvent(ev);
        /*if (mCheckEnable) {
            if(mOnSingleClickListener != null) {
                mOnSingleClickListener.onSingleClick();
                return true;
            }
            return false;
        }*/
        int action = ev.getAction();
        if (action == MotionEvent.ACTION_UP) {
            if (!touchFlag) {
                touchFlag = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        try{
                            Thread.sleep(500);
                        }catch(Exception e){
                            e.printStackTrace();
                        }finally{
                            if (touchFlag && null != mCurrClickSpan && null != mCurrClickUrl) {
                                /*if(mOnSingleClickListener != null) {
                                    post(new Runnable() {
                                        @Override
                                        public void run() {
                                            // TODO Auto-generated method stub
                                            mOnSingleClickListener.onSingleClick();
                                        }
                                    });
                                }*/
                                post(new Runnable() {
                                    public void run() {
                                        MessageUtils.onMessageSpansClick(getContext(), contentText, mCurrClickUrl, mCurrClickSpan);
                                        mCurrClickUrl = null;
                                        mCurrClickSpan = null;
                                    }
                                });
                            }
                            touchFlag = false;
                        }
                    } 
               }).start();
                //return false;
            }else {
                touchFlag = false;
                if(mOnDoubleClickListener != null) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            mCurrClickUrl = null;
                            mCurrClickSpan = null;
                            mOnDoubleClickListener.onDoubleClick();
                        }
                    });
                }
            }
        }
       // return false;
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        return super.dispatchTouchEvent(ev);
    }

    private boolean touchFlag2 = false;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        /*if (mCheckEnable) {
            if(mOnSingleClickListener != null) {
                mOnSingleClickListener.onSingleClick();
                return true;
            }
            return false;
        }*/
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP) {
            if (!touchFlag2) {
                touchFlag2 = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        try{
                            Thread.sleep(300);
                        }catch(Exception e){
                            e.printStackTrace();
                        }finally{
                            if (touchFlag && null != mCurrClickUrl) {
                                if(mOnSingleClickListener != null) {
                                    post(new Runnable() {
                                        @Override
                                        public void run() {
                                            // TODO Auto-generated method stub
                                            mOnSingleClickListener.onSingleClick();
                                        }
                                    });
                                }
                            }
                            touchFlag2 = false;
                        }
                    } 
               }).start();
            }else {
                touchFlag2 = false;
                if(mOnDoubleClickListener != null) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            //myHandler.rem
                            mOnDoubleClickListener.onDoubleClick();
                        }
                    });
                }
            }
        }
        return super.onTouchEvent(event);
    }

    public interface OnDoubleClickListener {
        void onDoubleClick();
    }

    public interface OnSingleClickListener {
        void onSingleClick();
    }

    public void setOnDoubleClickListener(OnDoubleClickListener listener) {
        mOnDoubleClickListener = listener;
    }

    public void setOnSingleClickListener(OnSingleClickListener listener) {
        mOnSingleClickListener = listener;
    }

    private boolean hasSpan() {
        final URLSpan[] spans = contentText.getUrls();
        if (spans.length == 0) {
            return false;
        }
        return true;
    }

    public void stripUnderlines(TextView textView) {
        if (null != textView && textView.getText() instanceof Spannable) {
            Spannable s = (Spannable)textView.getText();
            URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
            for (URLSpan span : spans) {
                int start = s.getSpanStart(span);
                int end = s.getSpanEnd(span);
                s.removeSpan(span);
                span = new URLSpanNoUnderline(span.getURL());
                s.setSpan(span, start, end, 0);
            }
            //textView.setText(s);
        }
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override  
        public boolean onDoubleTap(MotionEvent event) {
            return true;  
        }

        @Override
        public boolean onSingleTapUp(MotionEvent ev) {
            return true;
        }
    }
    
    private class URLSpanNoUnderline extends URLSpan {
        private boolean isCallSuperClick = false;
        private static final String MAIL_TO_PREFIX = "mailto:";
        public URLSpanNoUnderline(String url) {
            super(url);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setColor(Color.parseColor("#FF01BFBF"));
            ds.setUnderlineText(false);
        }

        @Override
        public void onClick(View view) {
            // TODO Auto-generated method stub
            // super.onClick(widget);
            /*if (mCheckEnable) {
                return;
            }*/
            if (!isCallSuperClick) {
                String url = new String(getURL());
                if (MessageUtils.isWebUrl(url)) {
                    // showUrlOptions(context, url);
                    isCallSuperClick = false;
                }
                else {
                    final String telPrefix = "tel:";
                    if (url.startsWith(telPrefix)) {
                        url = url.substring(telPrefix.length());
                        if (PhoneNumberUtils.isWellFormedSmsAddress(url)) {
                            // showNumberOptions(context, url);
                            isCallSuperClick = false;
                        }
                    }
                    else if (url.startsWith(MAIL_TO_PREFIX)) {
                        // url = url.substring(MAIL_TO_PREFIX.length());
                        // showEmailOptions(context, url);
                        isCallSuperClick = false;
                    }
                    else {
                        // span.onClick(contentText);
                        isCallSuperClick = true;
                    }
                }
                mCurrClickSpan = this;
                mCurrClickUrl = getURL();
            } else {
                super.onClick(view);
            }
        }
    }
}