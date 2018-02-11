/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.mms.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Field;

import com.android.mms.R;
//tangyisen add

public class EditTextHelper extends EditText {
    private final String TAG = EditTextHelper.class.getSimpleName();
    private Layout mLayout;
    private int mLineCount = 0;


    public EditTextHelper(Context context) {
        super(context);
    }

    public EditTextHelper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextHelper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EditTextHelper(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean isSuggestionsEnabled() {
        return false;
    }

    //因为目前貌似是光标如果在最后一行，是正常大小，在其他行，会变大。
    //所以我们解决的大致思路是：重写两个光标的drawable，判断光标当前行是否是最后一行，如果是，就调用较小的drawable；如果不是，就调用较大的drawable。
    //因为设置光标Drawable没有公开，所以需要用反射。
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
            drawables[0] = getContext().getResources().getDrawable(drawableId, null);
            drawables[1] = getContext().getResources().getDrawable(drawableId, null);
            fCursorDrawable.set(editor, drawables);
        } catch (Throwable ignored) {
        }
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        if (getEditableText().length() > 0 && getSelectionStart() == getEditableText().length()) {
            setSelection(getEditableText().length());
        }

        if (getEditableText().length() > 0 && getSelectionEnd() == getEditableText().length()) {
            setSelection(getSelectionStart(), getSelectionEnd());
        }

        int stringRightIndex = getEditableText().length();

        if (selStart <= stringRightIndex &&
                getEditableText().subSequence(selStart, stringRightIndex).toString().contains("\n")) {
            setCursorDrawableStatus(false);
        } else {
            if (null != mLayout && mLayout.getLineForOffset(selStart) < mLineCount - 1) {
                int line = mLayout.getLineForOffset(selStart);
                setCursorDrawableStatus(false);
            } else
                setCursorDrawableStatus(true);
        }
        super.onSelectionChanged(selStart, selEnd);
    }


    private void setCursorDrawableStatus(boolean isLastLine) {
        if (isLastLine) {
            setCursorDrawable(R.drawable.cursor_last_line_drawable);
        } else {
            setCursorDrawable(R.drawable.cursor_drawable);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        int newLineCount = getLineCount();
        if (mLineCount != newLineCount) {
            mLineCount = newLineCount;
            onSelectionChanged(getSelectionStart(), getSelectionEnd());
        }
        mLayout = getLayout();
        super.draw(canvas);
    }
}
