package com.android.mms.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.method.LinkMovementMethod;
import android.text.method.Touch;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.style.ClickableSpan;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import com.android.mms.R;



/**
 * 固定行数展开收缩控件
 * Created by lichao on 16-8-27.
 */
public class FolderTextView extends TextView{

    private static final String TAG = "FolderTextView";

    private static final String ELLIPSIS="...";
    private static final String FOLD_TEXT = "收起";
    private static final String UNFOLD_TEXT = "更多";
    private static final int DEFAULT_FOLD_LINE = 8;

    /**
     * 收缩状态
     */
    private boolean isFold = false;

    /**
     * 绘制，防止重复进行绘制
     */
    private boolean isDrawed = false;
    /**
     * 内部绘制
     */
    private boolean isInner = false;

    /**
     * 折叠行数
     */
    private int foldLine;

    /**
     * 全文本
     */
    private String fullText;
    private float mSpacingMult = 1.0f;
    private float mSpacingAdd = 0.0f;

    boolean dontConsumeNonUrlClicks = true;
    boolean linkHit;

    private String retract_str;
    private String spread_str;

    public FolderTextView(Context context) {
        this(context, null);
    }

    public FolderTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FolderTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(R.styleable.FolderTextView);
        foldLine = a.getInt(R.styleable.FolderTextView_foldline, DEFAULT_FOLD_LINE);
        a.recycle();

        retract_str = context.getString(R.string.retract);
        spread_str = context.getString(R.string.spread);
    }

    /**
     * 不更新全文本下，进行展开和收缩操作
     * @param text
     */
    private void setUpdateText(CharSequence text){
        isInner = true;
        setText(text);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if(TextUtils.isEmpty(fullText) || !isInner){
            isDrawed = false;
            fullText = String.valueOf(text);
        }
        super.setText(text, type);
    }

    @Override
    public void setLineSpacing(float add, float mult) {
        mSpacingAdd = add;
        mSpacingMult = mult;
        super.setLineSpacing(add, mult);
    }

    public int getFoldLine() {
        return foldLine;
    }

    public void setFoldLine(int foldLine) {
        this.foldLine = foldLine;
    }

    private Layout makeTextLayout(String text) {
        return new StaticLayout(text, getPaint(), getWidth() - getPaddingLeft() - getPaddingRight(),
                Layout.Alignment.ALIGN_NORMAL, mSpacingMult, mSpacingAdd, false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(!isDrawed){
            resetText();
        }
        super.onDraw(canvas);
        isDrawed = true;
        isInner = false;
    }

    private void resetText() {

        Layout layout_text = makeTextLayout(fullText);
        if(layout_text.getLineCount() <= getFoldLine()){
            setUpdateText(fullText);
            return;
        }

        String spanText = fullText;

        SpannableString spanStr;

        //收缩状态
        if(isFold){
            spanStr = createUnFoldSpan(spanText);
        }else{ //展开状态
            spanStr = createFoldSpan(spanText);
        }

        setUpdateText(spanStr);

        /*
        //if use LinkMovementMethod will lead no response on TextView click
        //when you call setMovementMethod or setKeyListener, TextView will "fixes" it's settings:
        //setFocusable(true), setClickable(true), setLongClickable(true)
         */
        //setMovementMethod(LinkMovementMethod.getInstance());

        //if use LocalLinkMovementMethod will lead slide change to long press event
        setMovementMethod(LocalLinkMovementMethod.getInstance());
    }

    /**
     * 创建展开状态下的Span
     * @param text 源文本
     * @return
     */
    private SpannableString createUnFoldSpan(String text) {
        //retract_str = FOLD_TEXT;
        String destStr = text + retract_str;
        int start = destStr.length() - retract_str.length();
        int end = destStr.length();

        SpannableString spanStr = new SpannableString(destStr);
        spanStr.setSpan(clickSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spanStr;
    }

    /**
     * 创建收缩状态下的Span
     * @param text
     * @return
     */
    private SpannableString createFoldSpan(String text) {
        //spread_str = UNFOLD_TEXT;
        String destStr = tailorText(text);
        int start = destStr.length() - spread_str.length();
        int end = destStr.length();

        SpannableString spanStr = new SpannableString(destStr);
        spanStr.setSpan(clickSpan,start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spanStr;
    }

    /**
     * 裁剪文本至固定行数
     * @param text 源文本
     * @return
     */
    private String tailorText(String text){
        //spread_str = UNFOLD_TEXT;
        String destStr = text + ELLIPSIS + spread_str;
        Layout layout_destStr = makeTextLayout(destStr);

        //如果行数大于固定行数
        if(layout_destStr.getLineCount() > getFoldLine()){
            int index = layout_destStr.getLineEnd(getFoldLine());
            if(text.length() < index){
                index = text.length();
            }
            String subText = text.substring(0, index-1); //从最后一位逐渐试错至固定行数
            return tailorText(subText);
        }else{
            return destStr;
        }
    }

    ClickableSpan clickSpan = new ClickableSpan() {
        @Override
        public void onClick(View widget) {
            isFold = !isFold;
            isDrawed = false;
            invalidate();
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(ds.linkColor);
        }
    };

    @Override
    public boolean hasFocusable() {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        linkHit = false;
        boolean res = super.onTouchEvent(event);

        if (dontConsumeNonUrlClicks){
            return linkHit;
        }else{
            return res;
        }
    }

    public static class LocalLinkMovementMethod extends LinkMovementMethod{

        static LocalLinkMovementMethod sInstance;

        private long lastClickTime;

        private static final long CLICK_DELAY = 500l;

        public static LocalLinkMovementMethod getInstance() {
            if (sInstance == null)
                sInstance = new LocalLinkMovementMethod();

            return sInstance;
        }

        @Override
        public boolean onTouchEvent(TextView widget,
                                    Spannable buffer, MotionEvent event) {
            int action = event.getAction();

            if (action == MotionEvent.ACTION_UP ||
                    action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                //Log.d(TAG, "onTouchEvent() x = "+x);
                //Log.d(TAG, "onTouchEvent() y = "+y);

                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int Offset_x = layout.getOffsetForHorizontal(line, x);

                //Log.d(TAG, "onTouchEvent() line = "+line);
                //Log.d(TAG, "onTouchEvent() Offset_x = "+Offset_x);

                //Log.d(TAG, "onTouchEvent() widget.getScrollX() = "+widget.getScrollX());
                //Log.d(TAG, "onTouchEvent() widget.getScrollY() = "+widget.getScrollY());

                ClickableSpan[] link = buffer.getSpans(Offset_x, Offset_x, ClickableSpan.class);

                if (link.length != 0) {
                    if (action == MotionEvent.ACTION_UP) {
                        //Log.d(TAG, "onTouchEvent() 111");
                        if(System.currentTimeMillis() - lastClickTime < CLICK_DELAY){
                            link[0].onClick(widget);
                        }
                    } else if (action == MotionEvent.ACTION_DOWN) {
                        //Log.d(TAG, "onTouchEvent() 222");
                        Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));
                        lastClickTime = System.currentTimeMillis();
                    }

                    if (widget instanceof FolderTextView){
                        //Log.d(TAG, "onTouchEvent() 333");
                        ((FolderTextView) widget).linkHit = true;
                    }
                    //Log.d(TAG, "onTouchEvent() 444");
                    return true;
                }else {
                    Selection.removeSelection(buffer);
                    Touch.onTouchEvent(widget, buffer, event);
                    //Log.d(TAG, "onTouchEvent() 666");
                    return false;
                }
            }
            //Log.d(TAG, "onTouchEvent() 777");
            return Touch.onTouchEvent(widget, buffer, event);
        }
    }
}
