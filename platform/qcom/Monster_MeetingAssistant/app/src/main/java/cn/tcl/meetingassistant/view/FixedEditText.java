/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Layout;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Field;

import cn.tcl.meetingassistant.R;
import cn.tcl.meetingassistant.log.MeetingLog;

/**
 * the EditText for fix lineSpace bug and the cursor's height bug.
 * But you must use the getTextString to get the right content string.
 */
public class FixedEditText extends EditText {

    private final String TAG = FixedEditText.class.getSimpleName();

    private int mLastLineResId;
    private int mCommonLineResId;

    // the flag to control the '\b'
    boolean isBlankAdded = false;

    public FixedEditText(Context context) {
        this(context,null);
    }

    public FixedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FixedEditText);
        mLastLineResId = a.getResourceId(R.styleable.FixedEditText_fixed_text_last_line_cursor,0);
        mCommonLineResId = a.getResourceId(R.styleable.FixedEditText_fixed_text_cursor,0);
        a.recycle();
        init();
        //editable.append('\b');
    }

    public FixedEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    private int lineCount = 0;
    private Layout layout;

    @Override
    public void draw(Canvas canvas) {
        int newLineCount = getLineCount();
        if(lineCount != newLineCount){
            lineCount = newLineCount;
            onSelectionChanged(getSelectionStart(),getSelectionEnd());
        }
        layout = getLayout();
        super.draw(canvas);

    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if(TextUtils.isEmpty(text)){
            isBlankAdded = false;
        }else {
            isBlankAdded = true;
            text = text + "\b";
        }
        super.setText(text, type);
    }

    @Override
    public void dispatchDisplayHint(int hint) {
        super.dispatchDisplayHint(hint);
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        boolean isPrintingStart = false;
        boolean isDeleteLastOne = false;
        int startLength;
        int endLength;

        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int lengthBefore, int lengthAfter) {
            startLength = charSequence.length();
        }

        @Override
        public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
            endLength = text.length();
            if(endLength == 1 && text.toString().equals("\b")){
                isDeleteLastOne = true;
            }else {
                isDeleteLastOne = false;
            }

            if(startLength == 0 && endLength > 0){
                isPrintingStart = true;
            }else {
                isPrintingStart = false;
            }
            MeetingLog.i(TAG,text.toString());
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if(isDeleteLastOne){
                editable.clear();
                isBlankAdded = false;
            }
            if(isPrintingStart && !isBlankAdded) {
                isBlankAdded = true;
                editable.append("\b");
            }
            int startSelection = getSelectionStart();
            float add = getLineSpacingExtra();
            float mul = getLineSpacingMultiplier();
            setLineSpacing(0f, 1f);
            setLineSpacing(add, mul);
            MeetingLog.i(TAG,"text" + editable.toString());
            Selection.setSelection(editable,startSelection);
        }
    };

    void init(){
        this.addTextChangedListener(mTextWatcher);
    }

    public void setCursorDrawable(int drawableId) {
        try {
            Field fCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            fCursorDrawableRes.setAccessible(true);
            Field fEditor = TextView.class.getDeclaredField("mEditor");
            fEditor.setAccessible(true);
            Object editor = fEditor.get(this);
            Class<?> clazz = editor.getClass();
            Field fCursorDrawable = clazz.getDeclaredField("mCursorDrawable");
            fCursorDrawable.setAccessible(true);
            Drawable[] drawables = new Drawable[2];
            drawables[0] = getContext().getResources().getDrawable(drawableId,null);
            drawables[1] = getContext().getResources().getDrawable(drawableId,null);
            fCursorDrawable.set(editor, drawables);
        } catch (Throwable ignored) {
        }
    }

    public String getTextString(){
        Editable editText = getText();
        String s = editText.toString();
        String result;
        if(s.lastIndexOf('\b') == s.length() -1){
            if(s.length() <= 1){
                result =  "";
            }else {
                result =  s.substring(0, s.length() - 1);
            }
        }else {
            result =  s;
        }
        return result;
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        if(getEditableText().length() > 0 && getSelectionStart() == getEditableText().length()){
            setSelection(getEditableText().length() - 1);
        }

        if(getEditableText().length() > 0 && getSelectionEnd() == getEditableText().length()){
            setSelection(getSelectionStart(),getSelectionEnd()-1);
        }

        int stringRightIndex = getEditableText().length();

        if(selStart <= stringRightIndex &&
                getEditableText().subSequence(selStart,stringRightIndex).toString().contains("\n")){
            setCursorDrawableStatus(false);
        }else{
            if(null != layout && layout.getLineForOffset(selStart) < lineCount - 1){
                int line  = layout.getLineForOffset(selStart);
                setCursorDrawableStatus(false);
            }else
                setCursorDrawableStatus(true);
        }
        super.onSelectionChanged(selStart, selEnd);
    }

    @Override
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);
    }

    private void setCursorDrawableStatus(boolean isLastLine){
        if(mLastLineResId == 0 || mCommonLineResId == 0){
            return;
        }
        if(isLastLine){
            setCursorDrawable(mLastLineResId);
        }else{
            setCursorDrawable(mCommonLineResId);
        }
    }

    @Override
    public boolean isSuggestionsEnabled() {
        return false;
    }

}
