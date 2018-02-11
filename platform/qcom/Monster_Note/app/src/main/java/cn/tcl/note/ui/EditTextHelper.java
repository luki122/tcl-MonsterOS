/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.ui;

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

import cn.tcl.note.R;
import cn.tcl.note.util.NoteLog;

public class EditTextHelper extends EditText {
    private final String TAG = EditTextHelper.class.getSimpleName();
    private Layout mLayout;
    private int mLineCount = 0;

    private OnEnterAndDelListener mListener;

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

    public void setOnEnterAndDelListener(OnEnterAndDelListener listener) {
        mListener = listener;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new MyInputConnection(super.onCreateInputConnection(outAttrs), true, this);
    }

    //get current selection position
    private int getCurrentSelePos() {
        return getSelectionStart();
    }

    public void setLineSpacing(float mult) {
        float add = getLineSpacingExtra();
        super.setLineSpacing(add, mult);
    }

    public interface OnEnterAndDelListener {
        /**
         * handle enter key event
         *
         * @return true when listener handle,or flase.
         */
        boolean onEnterListener(View view);

        /**
         * handle del key event
         *
         * @return true when listener handle,or flase.
         */
        boolean onDelListener(View view);
    }

    private class MyInputConnection extends InputConnectionWrapper {
        private View mView;

        public MyInputConnection(InputConnection target, boolean mutable, View view) {
            super(target, mutable);
            mView = view;
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            NoteLog.d(TAG, "MyInputConnection keycode=" + event.getKeyCode());

            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_ENTER:
                        NoteLog.d(TAG, "handle enter key");
                        if (mListener != null && mListener.onEnterListener(mView)) {
                            return true;
                        }
                        break;
                    case KeyEvent.KEYCODE_DEL:
                        if (getCurrentSelePos() == 0) {
                            NoteLog.d(TAG, "handle del key");
                            if (mListener.onDelListener(mView)) {
                                return true;
                            }
                        }
                        break;
                }
                return super.sendKeyEvent(event);
            }
            return false;
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            if (mListener != null && beforeLength == 1 && afterLength == 0) {
                //need to do special handle when selection is at start position,
                // or using default handle
                if (getCurrentSelePos() == 0) {
                    NoteLog.d(TAG, "handle del key");
                    return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                }
            }
            return super.deleteSurroundingText(beforeLength, afterLength);
        }

        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            NoteLog.d(TAG, "commitText=" + text);
            if (mListener != null && text.length() > 0 && text.charAt(0) == '\n') {
                NoteLog.d(TAG, "handle enter key");
                return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
            }

            boolean result = super.commitText(text, newCursorPosition);
            //for image and audio edittext,they don't input any text.
            //so when input any text for them,will send a enter event.
            if (getId() != R.id.item_text) {
                NoteLog.d(TAG, "this is not text edit,so send a enter event ");
                sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
            }
            return result;
        }

        @Override
        public boolean finishComposingText() {
//            return super.finishComposingText();
            return true;
        }

        @Override
        public boolean setComposingText(CharSequence text, int newCursorPosition) {
            NoteLog.d(TAG, "setComposingText text=" + text);
            if (getId() != R.id.item_text) {
                commitText(text, newCursorPosition);
                text = "";
            }
            return super.setComposingText(text, newCursorPosition);
        }
    }

    //cursor size
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
