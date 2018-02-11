package com.android.mms.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.text.TextUtils;

public class CheckOverSizeTextView extends TextView {

    protected boolean isOverSize;
    private boolean mIsCallChangedListener = true;
    private OnOverSizeChangedListener mOnOverSizeChangedListener;
    private boolean isEvenOverSize = false;

    public CheckOverSizeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // TODO Auto-generated constructor stub
    }

    public CheckOverSizeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public CheckOverSizeTextView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mIsCallChangedListener && mOnOverSizeChangedListener != null) {
            mOnOverSizeChangedListener.onChanged(checkOverLine());
        }
    }

    public boolean checkOverLine() {
        int maxLine = getMaxLines();
        if(getLineCount() < maxLine){
            isOverSize = false;
            return false;
        } else if (getLineCount() > maxLine){
            isOverSize = true;
            return true;
        }
        //Field field = getClass().getSuperclass().getDeclaredField("mLayout");
        //field.setAccessible(true);
        Layout mLayout = getLayout();//(Layout) field.get(this);
        if (mLayout == null) {
            return false;
        }
        isOverSize = mLayout.getEllipsisCount(maxLine - 1) > 0 ? true : false;
        /*int start = mLayout.getEllipsisStart(1);
        CharSequence m= mLayout.getText();
        String str = m.toString();
        String rexgString = "、";
        Pattern pattern = Pattern.compile(rexgString);  
        Matcher matcher = pattern.matcher(str);
        int oo = 0;
        while(matcher.find()){
            oo++;
        }*/
        return isOverSize;
    }

    public boolean isOverSize() {
        return isOverSize;
    }

    public int getTextMatchCount(String rexgString) {
        Layout mLayout = getLayout();
        if (null == mLayout) {
            return 0;
        }
        CharSequence text= mLayout.getText();
        String str = text.toString();
        //String rexgString = "、";
        Pattern pattern = Pattern.compile(rexgString);  
        Matcher matcher = pattern.matcher(str);
        int rtn = 0;
        while(matcher.find()){
            rtn++;
        }
        return rtn;
    }

    public void displayAll() {
        setMaxLines(Integer.MAX_VALUE);
        setEllipsize(null);
        isEvenOverSize = true;
    }

    public boolean getIsEvenOverSize() {
        return isEvenOverSize;
    }

    public void hide(int maxlines) {
        setEllipsize(TextUtils.TruncateAt.END);
        setMaxLines(maxlines);
    }

    public void setOnOverLineChangedListener(OnOverSizeChangedListener changedListener) {
        this.mOnOverSizeChangedListener = changedListener;
    }

    public void setIsCallChangedListener(boolean isCall) {
        this.mIsCallChangedListener = isCall;
    }

    public interface OnOverSizeChangedListener {
        /**
         * <span style="color:purple">when invalide,the method will be called
         * and tell you whether the content text is over size
         * 
         * @param isOverLine
         *            whether content text is over size
         */
        public void onChanged(boolean isOverSize);
    };
}
