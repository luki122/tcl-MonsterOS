/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.meetingassistant.R;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-21.
 * the text view can support align distribute
 */
public class AlignTextView extends TextView {
    private float mTextHeight; // single line text height
    private float mTextLineSpaceExtra = 0; // text line space extra
    private int mWidth; // this view's width
    // lines which each line contain a line in this view
    private List<String> mLines = new ArrayList<String>();
    private List<Integer> mTailLines = new ArrayList<Integer>(); // the last line
    private Align mAlign = Align.ALIGN_LEFT; // the last line's default align
    private boolean mFirstCalc = true;  // whether calculated

    private float mLineSpacingMultiplier = 1.0f;
    private float mLineSpacingAdd = 0.0f;

    private int mOriginalHeight = 0;
    private int mOriginalLineCount = 0;
    private int mOriginalPaddingBottom = 0;
    private boolean mSetPaddingFromMe = false;

    // zhe last line's align
    public enum Align {
        ALIGN_LEFT, ALIGN_CENTER, ALIGN_RIGHT,ALIGN_DISTRIBUTE
    }

    public AlignTextView(Context context) {
        super(context);
        setTextIsSelectable(false);
    }

    public AlignTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTextIsSelectable(false);

        mLineSpacingMultiplier = attrs.getAttributeFloatValue("http://schemas.android" + "" +
                ".com/apk/res/android", "lineSpacingMultiplier", 1.0f);

        int[] attributes = new int[]{android.R.attr.lineSpacingExtra};

        TypedArray arr = context.obtainStyledAttributes(attrs, attributes);

        mLineSpacingAdd = arr.getDimensionPixelSize(0, 0);

        mOriginalPaddingBottom = getPaddingBottom();

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AlignTextView);

        int alignStyle = ta.getInt(R.styleable.AlignTextView_align, 0);
        switch (alignStyle) {
            case 1:
                mAlign = Align.ALIGN_CENTER;
                break;
            case 2:
                mAlign = Align.ALIGN_RIGHT;
                break;
            case 3:
                mAlign = Align.ALIGN_DISTRIBUTE;
                break;
            default:
                mAlign = Align.ALIGN_LEFT;
                break;
        }

        ta.recycle();
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (mFirstCalc) {
            mWidth = getMeasuredWidth();
            String text = getText().toString();
            TextPaint paint = getPaint();
            mLines.clear();
            mTailLines.clear();

            String[] items = text.split("\\n");
            for (String item : items) {
                calc(paint, item);
            }

            measureTextViewHeight(text, paint.getTextSize(), getMeasuredWidth() -
                    getPaddingLeft() - getPaddingRight());

            mTextHeight = 1.0f * mOriginalHeight / mOriginalLineCount;

            mTextLineSpaceExtra = mTextHeight * (mLineSpacingMultiplier - 1) + mLineSpacingAdd;

            int heightGap = (int) ((mTextLineSpaceExtra + mTextHeight) * (mLines.size() -
                    mOriginalLineCount));

            mSetPaddingFromMe = true;
            setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(),
                    mOriginalPaddingBottom + heightGap);

            mFirstCalc = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        TextPaint paint = getPaint();
        paint.setColor(getCurrentTextColor());
        paint.drawableState = getDrawableState();
        mWidth = getMeasuredWidth();

        Paint.FontMetrics fm = paint.getFontMetrics();
        float firstHeight = getTextSize() - (fm.bottom - fm.descent + fm.ascent - fm.top);

        int gravity = getGravity();
        if ((gravity & 0x1000) == 0) {
            firstHeight = firstHeight + (mTextHeight - firstHeight) / 2;
        }

        int paddingTop = getPaddingTop();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        // textView's width
        mWidth = mWidth - paddingLeft - paddingRight;
        // loop to draw text
        for (int i = 0; i < mLines.size(); i++) {
            // Y
            float drawY = i * mTextHeight + firstHeight;
            // the current text need to be draw
            String line = mLines.get(i);
            // x
            float drawSpacingX = paddingLeft;
            // gap for all character
            float gap = (mWidth - paint.measureText(line));
            // gap between 2 character
            float interval = gap / (line.length() - 1);

            // draw the last line
            if (mTailLines.contains(i)) {
                interval = 0;
                if (mAlign == Align.ALIGN_CENTER) {
                    drawSpacingX += gap / 2;
                } else if (mAlign == Align.ALIGN_RIGHT) {
                    drawSpacingX += gap;
                }else if(mAlign == Align.ALIGN_DISTRIBUTE){
                  interval = gap / (line.length() - 1);
                }
            }

            for (int j = 0; j < line.length(); j++) {
                float drawX = paint.measureText(line.substring(0, j)) + interval * j;
                canvas.drawText(line.substring(j, j + 1), drawX + drawSpacingX, drawY +
                        paddingTop + mTextLineSpaceExtra * i - fm.bottom/2, paint);
            }
        }
    }

    /**
     * set the last line align
     *
     * @param align align
     */
    public void setAlign(Align align) {
        this.mAlign = align;
        invalidate();
    }

    /**
     * the character number of each line's text
     *
     * @param text text
     */
    private void calc(Paint paint, String text) {
        if (text.length() == 0) {
            mLines.add("\n");
            return;
        }
        int startPosition = 0; // start position
        // one width of a Chinese word
        float oneChineseWidth = paint.measureText("中");
        int ignoreCalcLength = (int) (mWidth / oneChineseWidth); // ignore calc length
        StringBuilder sb = new StringBuilder(text.substring(0, Math.min(ignoreCalcLength + 1,
                text.length())));

        for (int i = ignoreCalcLength + 1; i < text.length(); i++) {
            if (paint.measureText(text.substring(startPosition, i + 1)) > mWidth) {
                startPosition = i;
                mLines.add(sb.toString());

                sb = new StringBuilder();

                if ((text.length() - startPosition) > ignoreCalcLength) {
                    sb.append(text.substring(startPosition, startPosition + ignoreCalcLength));
                } else {
                    mLines.add(text.substring(startPosition));
                    break;
                }

                i = i + ignoreCalcLength - 1;
            } else {
                sb.append(text.charAt(i));
            }
        }
        if (sb.length() > 0) {
            mLines.add(sb.toString());
        }

        mTailLines.add(mLines.size() - 1);
    }


    @Override
    public void setText(CharSequence text, BufferType type) {
        mFirstCalc = true;
        super.setText(text, type);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        if (!mSetPaddingFromMe) {
            mOriginalPaddingBottom = bottom;
        }
        mSetPaddingFromMe = false;
        super.setPadding(left, top, right, bottom);
    }


    /**
     * get the text height
     *
     * @param text        text
     * @param textSize    character size
     * @param deviceWidth the screen width
     */
    private void measureTextViewHeight(String text, float textSize, int deviceWidth) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(deviceWidth, MeasureSpec.EXACTLY);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        textView.measure(widthMeasureSpec, heightMeasureSpec);
        mOriginalLineCount = textView.getLineCount();
        mOriginalHeight = textView.getMeasuredHeight();
    }
}
