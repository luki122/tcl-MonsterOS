package com.android.mms.ui;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//import mst.provider.Telephony.Mms;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.Telephony.Mms;
import android.telephony.PhoneNumberUtils;
import android.text.Annotation;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.QwertyKeyListener;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;
import android.util.AttributeSet;
import android.view.GestureDetector.OnGestureListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.android.ex.chips.BaseRecipientAdapter;
import com.android.ex.chips.DropdownChipLayouter;
import com.android.mms.MmsConfig;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;

import android.support.annotation.Nullable;
import android.view.GestureDetector;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.support.annotation.NonNull;
import com.android.ex.chips.RecipientEntry;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
/*tangyisen add*/

public class MstRecipientsEditor extends MultiAutoCompleteTextView implements OnEditorActionListener,OnItemClickListener {

    private static final char SBC_CHAR_START = 65281;
    private static final char SBC_CHAR_END = 65373;

    private static final char CHAR_DIVIDER = '、';
    private static final String STRING_DIVIDER ="、";

    private int mLongPressedPosition = -1;
    private final MstRecipientsEditorTokenizer mTokenizer;
    private char mLastSeparator = ',';
    private Runnable mOnSelectChipRunnable;
    private final MstAddressValidator mInternalValidator;
    private Context mContext;
    protected DropdownChipLayouter mDropdownChipLayouter;

    private boolean mIsCanDelete = false;
    private int mPreCursorPos = -1;
    /*private String mPreText;*/

    private ContactList mContactList = new ContactList();

    /** A noop validator that does not munge invalid texts and claims any address is valid */
    private class MstAddressValidator implements Validator {
        public CharSequence fixText(CharSequence invalidText) {
            return invalidText;
        }

        public boolean isValid(CharSequence text) {
            return true;
        }
    }
    /*public MstRecipientEditor(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }*/
    private boolean isOperDeleteDot = false;

    public MstRecipientsEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        mContext = context;
        mTokenizer = new MstRecipientsEditorTokenizer();
        setTokenizer(mTokenizer);
        mInternalValidator = new MstAddressValidator();
        super.setValidator(mInternalValidator);

        // For the focus to move to the message body when soft Next is pressed
        setImeOptions(EditorInfo.IME_ACTION_NEXT);

        setThreshold(1);    // pop-up the list after a single char is typed
        setOnItemClickListener(this);
        setOnEditorActionListener(this);

        addTextChangedListener(new TextWatcher() {
            CharSequence pre;
            //String willBeDeleteString;
            boolean deleteIsDot;
            int preCur = -1;

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                    int count, int after) {
                if (s instanceof SpannableString) {
                    pre = (CharSequence)(new SpannableString(s));
                } else if (s instanceof SpannableStringBuilder) {
                    pre = (CharSequence)(new SpannableStringBuilder(s));
                } else {
                    pre = (CharSequence)(new String(s.toString()));
                }
                //willBeDeleteString = s.toString().substring(start, start + count);
                if (s.length() == 1) {
                    deleteIsDot = false;
                } else {
                    deleteIsDot = MstRecipientsEditor.STRING_DIVIDER.equals(s.toString().substring(start, start + count));
                }
                if (mIsCanDelete) {
                    isOperDeleteDot = (count > 0);
                }
                preCur = getSelectionStart();
                /*if(mIsCanDelete && after > 0 && getSelectionStart() == s.length() - 1) {
                    setSelection(s.length());
                }*/
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                    int before, int after) {
                /*if((!mIsCanDelete && start != 0 && getSelectionStart() != s.length())
                    || (mIsCanDelete  && start != 0 && getSelectionStart() != s.length() - 1)) {
                    setText(pre);
                    addAllDotSpan(getText());
                    setSelection(mPreCursorPos);
                }*/
                if(TextUtils.isEmpty(s) || s.length() == 0) {
                    return;
                }
                if (deleteIsDot) {
                    if(!isOperDeleteDot) {
                        //mIsCanDelete = true;
                        isOperDeleteDot = true;
                        setText(pre);
                        setSelection(preCur);
                    }
                } else if (isOperDeleteDot) {
                    if(!mIsCanDelete) {//mean start == 0
                        ArrayList<Integer> pos = queryString(s.toString().substring(0, preCur + 1));
                        BackgroundColorSpan highlightSpan =
                            new BackgroundColorSpan(mContext.getResources().getColor(
                                R.color.recipients_hightlight_color_bg));
                        OtherSpan dotHighlightSpan =
                            new OtherSpan(mContext.getResources().getColor(
                                    R.color.recipients_hightlight_dot_hightlight));
                        OtherSpan textHighlightSpan =
                            new OtherSpan(mContext.getResources().getColor(
                                    R.color.recipients_hightlight_text_color));
                        int prePos = (pos.size() - 2) < 0 ? 0 : pos.get(pos.size() - 2) + 1;
                        if (s instanceof Spannable) {
                            Spannable span = (Spannable)s;
                            span.setSpan(highlightSpan, prePos, pos.get(pos.size() - 1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            span.setSpan(textHighlightSpan, prePos, pos.get(pos.size() - 1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            span.setSpan(dotHighlightSpan, pos.get(pos.size() - 1), pos.get(pos.size() - 1) + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        mIsCanDelete = true;
                    } else {
                        mIsCanDelete = false;
                        isOperDeleteDot = false;
                        ArrayList<Integer> pos = queryString(s.toString().substring(0, getSelectionStart() + 1));
                        int prePosition = pos.size() - 2;
                        //prePosition < 0 means delete the first one
                        if(prePosition < 0) {
                            //setText(null);
                            mContactList.remove(0);
                            String text = getText().toString();
                            /*String subString = text.substring(0, pos.get(pos.size() - 1) + 1);
                            setText(text.replace(subString, ""));*/
                            setText(text.substring(pos.get(pos.size() - 1) + 1));
                            addAllDotSpan(getText());
                            text = getText().toString();
                            if(!TextUtils.isEmpty(text)) {
                                setSelection(text.length());
                            }
                            mPreCursorPos = text.length();
                        } else {
                            mContactList.remove(pos.size() - 1);
                            String text = getText().toString();
                            /*String subString = text.substring(pos.get(prePosition) + 1, pos.get(pos.size() - 1) + 1);
                            setText(text.replace(subString, ""));*/
                            String subString1 = TextUtils.substring(text, 0, pos.get(prePosition) + 1);
                            String subString2 = TextUtils.substring(text, pos.get(pos.size() - 1) + 1, text.length());
                            setText(subString1 + subString2);
                            addAllDotSpan(getText());
                            setSelection(pos.get(prePosition) + 1);
                            mPreCursorPos = pos.get(prePosition) + 1;
                        }
                        if(mOnEditorOperateListener != null) {
                            mOnEditorOperateListener.onEditorDelClick(getText().toString());
                        }
                    }
                } else {
                    if (mIsCanDelete  && start != 0 && getSelectionStart() == s.length() - 1) {
                        mIsCanDelete = false;
                        setText(pre + String.valueOf(s.charAt(s.length() - 2)));
                        setSelection(s.length());
                        removeHightlightSpan();
                        addAllDotSpan(getText());
                    } else if(start != 0 && s.toString().lastIndexOf(STRING_DIVIDER) > getSelectionStart()) {
                        mIsCanDelete = false;
                        setText(pre);
                        removeHightlightSpan();
                        addAllDotSpan(getText());
                        //tangyisen add avoid indexover
                        if (mPreCursorPos > 0 && (mPreCursorPos < s.toString().length() + 1)) {
                            setSelection(mPreCursorPos);
                        } else {
                            setSelection(s.length());
                        }
                    } else if(mIsCanDelete) {
                        /*mIsCanDelete = false;
                        mPreCursorPos = getSelectionStart();
                        if(!TextUtils.isEmpty(mPreText)) {
                            String text = s.toString();
                            String show = mPreText + text.substring(mPreText.length());
                            setText(show);
                            mPreText = getText().toString();
                            mPreCursorPos = mPreText.length();
                            setSelection(mPreText.length());
                            //setText(mPreText);
                            //setSelection(mPreCursorPos);
                        }*/
                        mIsCanDelete = false;
                        removeHightlightSpan();
                        /*setSelection(mPreCursorPos + 1);*/
                        int cursorPos = mPreCursorPos + 1;
                        if (cursorPos > 0 && (cursorPos < s.toString().length() + 1)) {
                            setSelection(cursorPos);
                        } else {
                            setSelection(s.length());
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //addAllDotSpan(getText());
            }
        });

        setDropDownBackgroundResource(R.drawable.recipients_editor_dropdown_bg);
        setDropdownChipLayouter(new MyDropdownChipLayouter(LayoutInflater.from(context), context));
    }

    /*public static String stringFilter(String str)throws PatternSyntaxException{ 
        String regEx = "[/\\:*?<>|\"\n\t]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("");
        }*/

    public void setDropdownChipLayouter(DropdownChipLayouter dropdownChipLayouter) {
        mDropdownChipLayouter = dropdownChipLayouter;
        //mDropdownChipLayouter.setDeleteListener(this);
        //mDropdownChipLayouter.setPermissionRequestDismissedListener(this);
    }

    /*public MstRecipientEditor(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }*/

    @Override
    public boolean onEditorAction(TextView view, int action, KeyEvent keyEvent) {
        //boolean rtn = super.onEditorAction(view, action, keyEvent);
        if (action == EditorInfo.IME_ACTION_DONE) {
            dismissDropDown();
            if(mOnEditorOperateListener != null) {
                mOnEditorOperateListener.onEditorDoneClick(getText().toString());
                ensureAddContact();
                return true;
            }
        }
        return false;
    }

    @Override
    public InputConnection onCreateInputConnection(@NonNull EditorInfo outAttrs) {
        InputConnection connection = super.onCreateInputConnection(outAttrs);
        int imeActions = outAttrs.imeOptions&EditorInfo.IME_MASK_ACTION;
        if ((imeActions&EditorInfo.IME_ACTION_DONE) != 0) {
            // clear the existing action
            outAttrs.imeOptions ^= imeActions;
            // set the DONE action
            outAttrs.imeOptions |= EditorInfo.IME_ACTION_DONE;
        }
        if ((outAttrs.imeOptions&EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
            outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
        }

        outAttrs.actionId = EditorInfo.IME_ACTION_DONE;

        // Custom action labels are discouraged in L; a checkmark icon is shown in place of the
        // custom text in this case.
        outAttrs.actionLabel = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? null :
            getContext().getString(R.string.action_label);
        return connection;
    }

    private OnEditorOperateListener mOnEditorOperateListener;
    public interface OnEditorOperateListener {
        void onEditorDoneClick(String text);
        void onEditorDelClick(String text);
        void onEditorDropDownSelected(String text);
    }
    public void setOnEditorOperateListener(OnEditorOperateListener listener) {
        mOnEditorOperateListener = listener;
    }

    private boolean needDependOnDividerChar = false;
    @Override
    public void setText(CharSequence text, boolean filter) {
        if (needDependOnDividerChar) {
            super.setText(text, filter);
        } else {
            setText(text);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        requestFocus();
        showInputMethod();
        int action = event.getAction();
        boolean canMoveCursor = false;
        boolean rtn = true;
        ensureAddContact();
        /*if(action == MotionEvent.ACTION_MOVE) {
            return true;
        }*/
        if(action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_MOVE) {
            final int offset = getOffsetForPosition(event.getX(), event.getY());
            int selectPos = -1;
            String text = getText().toString();
            ArrayList<Integer> posList = queryString(text);
            //int[] posInts = posList.toArray(new int[1]);
            /*for(int pos : posList) {
                if(offset == pos + 1) {
                    canMoveCursor = true;
                    break;
                }
            }*/
            int start = 0;
            int end = 0;
            for(int i= 0; i < posList.size(); i ++) {
                if(i == 0) {
                    start = 0;
                }else{
                    start = posList.get(i - 1);
                }
                end = posList.get(i);
                if(offset > start && offset < end) {
                    canMoveCursor = true;
                    selectPos = end + 1;
                    break;
                }
            }
            if(offset == text.length()) {
                canMoveCursor = true;
                selectPos = text.length();
            }
            if(canMoveCursor && !posList.isEmpty()) {
                //rtn = super.onTouchEvent(event);
                removeHightlightSpan();
                /*SpannableStringBuilder styled = new SpannableStringBuilder(getText().toString());
                ForegroundColorSpan highlightSpan =
                        new ForegroundColorSpan(mContext.getResources().getColor(
                                R.color.recipients_hightlight_color_bg));
                BackgroundColorSpan highlightSpan =
                    new BackgroundColorSpan(mContext.getResources().getColor(
                        R.color.recipients_hightlight_color_bg));
                styled.setSpan(highlightSpan, start + 1, end + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                setText(styled);*/
                BackgroundColorSpan highlightSpan =
                    new BackgroundColorSpan(mContext.getResources().getColor(
                        R.color.recipients_hightlight_color_bg));
                OtherSpan dotHighlightSpan =
                    new OtherSpan(mContext.getResources().getColor(
                            R.color.recipients_hightlight_dot_hightlight));
                OtherSpan textHighlightSpan =
                    new OtherSpan(mContext.getResources().getColor(
                            R.color.recipients_hightlight_text_color));
                if (getText() instanceof Spannable) {
                    if(start != 0) {
                        start ++;
                    }
                    Spannable s = (Spannable)getText();
                    s.setSpan(highlightSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    s.setSpan(textHighlightSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    s.setSpan(dotHighlightSpan, end, end + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                //setSelection(selectPos);
                setSelection(selectPos - 1);
                mPreCursorPos = selectPos;
                mIsCanDelete = true;
            }
        }
        /*if(canMoveCursor) {
            rtn = super.onTouchEvent(event);
        }*/
        /*if(mIsCanDelete && mPreCursorPos != getSelectionStart()) {
            mIsCanDelete = false;
            mPreCursorPos = getSelectionStart();
            if(!TextUtils.isEmpty(mPreText)) {
                setText(mPreText);
                setSelection(mPreCursorPos);
                removeHightlightSpan();
            }
        }
        mPreCursorPos = getSelectionStart();*/
        return true;
    }

    private void removeHightlightSpan() {
        if (getText() instanceof Spannable) {
            Spannable s = (Spannable)getText();
            OtherSpan[] spans1 = s.getSpans(0, s.length(), OtherSpan.class);
            for (OtherSpan span : spans1) {
                s.removeSpan(span);
            }
            BackgroundColorSpan[] spans2 = s.getSpans(0, s.length(), BackgroundColorSpan.class);
            for (BackgroundColorSpan span : spans2) {
                s.removeSpan(span);
            }
        }
    }

    private class DotSpan extends ForegroundColorSpan {
        public DotSpan(int color) {
            super(color);
        }

        public DotSpan(Parcel src) {
            super(src);
        }
    }

    private class OtherSpan extends ForegroundColorSpan {
        public OtherSpan(int color) {
            super(color);
        }

        public OtherSpan(Parcel src) {
            super(src);
        }
    }

    private void addDotSpan(CharSequence text, int start) {
        DotSpan dotHighlightSpan =
            new DotSpan(mContext.getResources().getColor(
                    R.color.recipients_hightlight_text_color));
        if (text instanceof Spannable) {
            Spannable s = (Spannable)text;
            s.setSpan(dotHighlightSpan, start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void addAllDotSpan(CharSequence text) {
        /*DotSpan dotHighlightSpan =
            new DotSpan(mContext.getResources().getColor(
                    R.color.recipients_hightlight_text_color));*/
        ArrayList<Integer> posList = queryString(text.toString());
        if (text instanceof Spannable) {
            Spannable s = (Spannable)text;
            DotSpan[] spans = s.getSpans(0, s.length(), DotSpan.class);
            for (DotSpan span : spans) {
                s.removeSpan(span);
            }
            for(int pos : posList) {
                DotSpan dotHighlightSpan =
                    new DotSpan(mContext.getResources().getColor(
                            R.color.recipients_hightlight_text_color));
                s.setSpan(dotHighlightSpan, pos, pos + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_ENTER) {
            dismissDropDown();
            if(mOnEditorOperateListener != null) {
                mOnEditorOperateListener.onEditorDoneClick(getText().toString());
                ensureAddContact();
                return true;
            }
        } else if(keyCode == KeyEvent.KEYCODE_DEL) {
          //boolean rtn = super.onKeyDown(keyCode, event);
            int curPos = getSelectionStart();
            if(mIsCanDelete) {
                curPos ++;
            }
            if(curPos == 0) {
                return super.onKeyDown(keyCode, event);
            }
            //mPreCursorPos = curPos;
            String sub = getText().toString().substring(0, curPos);
            ArrayList<Integer> pos = queryString(sub);
            if (pos == null || pos.isEmpty()) {
                return super.onKeyDown(keyCode, event);
            }
            if(!mIsCanDelete && pos.get(pos.size() - 1) + 1 != curPos) {
                mIsCanDelete = false;
                return super.onKeyDown(keyCode, event);
            } else {
                if(!mIsCanDelete) {
                    //mPreText = getText().toString();
                    //SpannableStringBuilder styled = new SpannableStringBuilder(getText().toString());
                    /*ForegroundColorSpan highlightSpan =
                            new ForegroundColorSpan(mContext.getResources().getColor(
                                    R.color.recipients_hightlight_color_bg));*/
                    BackgroundColorSpan highlightSpan =
                        new BackgroundColorSpan(mContext.getResources().getColor(
                            R.color.recipients_hightlight_color_bg));
                    OtherSpan dotHighlightSpan =
                        new OtherSpan(mContext.getResources().getColor(
                                R.color.recipients_hightlight_dot_hightlight));
                    OtherSpan textHighlightSpan =
                        new OtherSpan(mContext.getResources().getColor(
                                R.color.recipients_hightlight_text_color));
                    int prePos = (pos.size() - 2) < 0 ? 0 : pos.get(pos.size() - 2) + 1;
                    if (getText() instanceof Spannable) {
                        Spannable s = (Spannable)getText();
                        s.setSpan(highlightSpan, prePos, pos.get(pos.size() - 1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        s.setSpan(textHighlightSpan, prePos, pos.get(pos.size() - 1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        s.setSpan(dotHighlightSpan, pos.get(pos.size() - 1), pos.get(pos.size() - 1) + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    /*styled.setSpan(highlightSpan,
                        prePos, pos.get(pos.size() - 1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);*/
                    //setText(styled);
                    //setSelection(curPos);
                    setSelection(curPos - 1);
                    mIsCanDelete = true;
                    mPreCursorPos = curPos;
                } else {
                    //mPreText = null;
                    mIsCanDelete = false;
                    int prePosition = pos.size() - 2;
                    //prePosition < 0 means delete the first one
                    if(prePosition < 0) {
                        //setText(null);
                        mContactList.remove(0);
                        String text = getText().toString();
                        /*String subString = text.substring(0, pos.get(pos.size() - 1) + 1);
                        setText(text.replace(subString, ""));*/
                        setText(text.substring(pos.get(pos.size() - 1) + 1));
                        addAllDotSpan(getText());
                        text = getText().toString();
                        if(!TextUtils.isEmpty(text)) {
                            setSelection(text.length());
                        }
                        mPreCursorPos = text.length();
                    } else {
                        mContactList.remove(pos.size() - 1);
                        String text = getText().toString();
                        /*String subString = text.substring(pos.get(prePosition) + 1, pos.get(pos.size() - 1) + 1);
                        setText(text.replace(subString, ""));*/
                        String subString1 = TextUtils.substring(text, 0, pos.get(prePosition) + 1);
                        String subString2 = TextUtils.substring(text, pos.get(pos.size() - 1) + 1, text.length());
                        setText(subString1 + subString2);
                        addAllDotSpan(getText());
                        setSelection(pos.get(prePosition) + 1);
                        mPreCursorPos = pos.get(prePosition) + 1;
                    }
                }
            }
            if(mOnEditorOperateListener != null) {
                mOnEditorOperateListener.onEditorDelClick(getText().toString());
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
        return false;
    }

    private void queryString(String str, String subString, int start, int end) {
        
    }

    @Override
    public boolean isSuggestionsEnabled() {
        return false;
    }

    private ArrayList<Integer> queryString(String str/*, int start, int end*/) {
        char[] value = str.toCharArray();
        ArrayList<Integer> posList = new ArrayList<>();
        for(int i = 0; i < value.length; i ++) {
            if(value[i] == CHAR_DIVIDER) {
                posList.add(i);
            }
        }
        return posList;
    }

    @Override
    public void onFocusChanged(boolean hasFocus, int direction, Rect previous) {
        super.onFocusChanged(hasFocus, direction, previous);
        /*if(mOnEditorOperateListener != null) {
            mOnEditorOperateListener.onEditorDoneClick(getText().toString());
        }*/
       // setText(null);
        if(hasFocus) {
            showInputMethod();
        } else {
            ensureAddContact();
        }
    }

    //private RecipientEntry mRecipientEntry = null;
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //super.onItemClick(parent, view, position, id);
        if(position < 0) {
            return;
        }
        final RecipientEntry mRecipientEntry = getAdapter().getItem(position);
        if(TextUtils.isEmpty(mRecipientEntry.getDestination())) {
            Toast.makeText(mContext, R.string.toast_contact_no_number, Toast.LENGTH_SHORT).show();
            return;
        }
        Contact contact = findDuplicatePhone(mRecipientEntry.getDestination());
        if(contact == null) {
            ContactList list = ContactList.blockingGetByUris(buildPhoneUris(mRecipientEntry.getDataId()));
            /*ContactList newList = new ContactList();
            for (String number : mContactList.getNumbers()) {
                Contact contact = Contact.get(number, false);
                contact.setNumber(number);
                newList.add(contact);
            }
            mContactList = newList;
            if(list.removeAll(mContactList)) {
                Toast.makeText(mContext, R.string.remove_duplicate_contact, Toast.LENGTH_SHORT).show();
            }*/
            mContactList.addAll(list);
        } else {
            Toast.makeText(mContext, R.string.remove_duplicate_contact, Toast.LENGTH_SHORT).show();
        }
        setDisplayText();
    }

    private Contact findDuplicatePhone(String phone) {
        if(TextUtils.isEmpty(phone)) {
            return null;
        }
        for(Contact tmp : mContactList) {
            if (tmp.getNumber().equals(phone)) {
                return tmp;
            }
        }
        return null;
    }

    private void setDisplayText() {
        if(mContactList == null || mContactList.isEmpty()) {
            setText(null);
            return;
        }
        StringBuilder builder = new StringBuilder();
        for(Contact c : mContactList) {
            if(!TextUtils.isEmpty(c.getName())) {
                builder.append(c.getName());
                builder.append(STRING_DIVIDER);
                
            } else if(!TextUtils.isEmpty(c.getNumber())) {
                builder.append(c.getNumber());
                builder.append(STRING_DIVIDER);
            }
        }
        setText(builder.toString());
        addAllDotSpan(getText());
        setSelection(getText().length());
    }

    @Override
    protected void replaceText(CharSequence text) {
        /*clearComposingText();
        int end = getSelectionEnd();
        int start = mTokenizer.findTokenStart(getText(), end);

        Editable editable = getText();
        String original = TextUtils.substring(editable, start, end);

        //QwertyKeyListener.markAsReplaced(editable, start, end, original);
        //editable.replace(start, end, mTokenizer.terminateToken(text));
        if (null != mRecipientEntry) {
            editable.replace(start, end, getDisplayFromRecipientEntry(mRecipientEntry));
            mRecipientEntry = null;
        }
        editable.replace(start, end, getDisplayString(text.toString()));*/
        //super.replaceText(text);
    }

    /*public CharSequence getDisplayFromRecipientEntry(RecipientEntry entry) {
        final String displayName = entry.getDisplayName();
        final String emailAddress = entry.getDestination();
        if (TextUtils.isEmpty(displayName) || TextUtils.equals(displayName, emailAddress)) {
             return emailAddress;
        } else {
            return displayName;
        }
    }
    public CharSequence getDisplayString(String text) {
        //see Rfc822Token,BaseRecipientAdapter
        int addressStart = text.indexOf("<");
        if(addressStart <  0) {
            return text + STRING_DIVIDER;
        } else {
            return TextUtils.substring(text, 0, addressStart - 1) + STRING_DIVIDER;
        }
    }*/

    private Uri[] buildPhoneUris(final long id) {
        Uri[] newUris = new Uri[1];
        newUris[0] = ContentUris.withAppendedId(Phone.CONTENT_URI, id);
        return newUris;
    }

    @Override
    public BaseRecipientAdapter getAdapter() {
        return (BaseRecipientAdapter)super.getAdapter();
    }

    @Override
    public void append(CharSequence text, int start, int end) {
        // We don't care about watching text changes while appending.
        super.append(text, start, end);
    }

    @Override
    public boolean enoughToFilter() {
        if (!super.enoughToFilter()) {
            return false;
        }
        // If the user is in the middle of editing an existing recipient, don't offer the
        // auto-complete menu. Without this, when the user selects an auto-complete menu item,
        // it will get added to the list of recipients so we end up with the old before-editing
        // recipient and the new post-editing recipient. As a precedent, gmail does not show
        // the auto-complete menu when editing an existing recipient.
        int end = getSelectionEnd();
        int len = getText().length();

        return end == len;

    }

    public int getRecipientCount() {
        return mContactList.size();//mTokenizer.getNumbers().size();
    }

    public List<String> getNumbers() {
        return mContactList.getNumbersList();//mTokenizer.getNumbers();
    }

    public String getExsitNumbers(){
        //todo change
        return mTokenizer.getNumbersString();
    }

    public ContactList constructContactsFromInput(boolean blocking) {
        /*List<String> numbers = mTokenizer.getNumbers();
        ContactList list = new ContactList();
        for (String number : numbers) {
            Contact contact = Contact.get(number, blocking);
            contact.setNumber(number);
            list.add(contact);
        }
        return list;*/
        ensureAddContact();
        ContactList newList = new ContactList();
        for (String number : mContactList.getNumbers()) {
            Contact contact = Contact.get(number, blocking);
            contact.setNumber(number);
            newList.add(contact);
        }
        mContactList = newList;
        return mContactList;
    }

    private void ensureAddContact() {
        String text = getText().toString();
        int last = text.lastIndexOf(STRING_DIVIDER);
        if(text.lastIndexOf(STRING_DIVIDER) != text.length() -1) {
            Contact contact = findDuplicatePhone(text.substring(last + 1));
            if (contact == null) {
                contact = Contact.get(text.substring(last + 1), true);
                mContactList.add(contact);
            } else {
                Toast.makeText(mContext, R.string.remove_duplicate_contact, Toast.LENGTH_SHORT).show();
            }
            setDisplayText();
            /*append(STRING_DIVIDER);
            addDotSpan(getText(), getText().toString().length() - 1);*/
        }
    }

    public void addContact() {
        ensureAddContact();
    }

    private boolean isValidAddress(String number, boolean isMms) {
        if (isMms) {
            return MessageUtils.isValidMmsAddress(number);
        } else {
            if (hasInvalidCharacter(number)) {
                return false;
            }

            // TODO: PhoneNumberUtils.isWellFormedSmsAddress() only check if the number is a valid
            // GSM SMS address. If the address contains a dialable char, it considers it a well
            // formed SMS addr. CDMA doesn't work that way and has a different parser for SMS
            // address (see CdmaSmsAddress.parse(String address)). We should definitely fix this!!!
            return PhoneNumberUtils.isWellFormedSmsAddress(number)
                    || Mms.isEmailAddress(number);
        }
    }

    /**
     * Return true if the number contains invalid character.
     */
    private boolean hasInvalidCharacter(String number) {
        char[] charNumber = number.trim().toCharArray();
        int count = charNumber.length;
        if (mContext.getResources().getBoolean(R.bool.config_filter_char_address)) {
            for (int i = 0; i < count; i++) {
                // Allow first character is + character
                if (i == 0 && charNumber[i] == '+') {
                    continue;
                }
                if (!isValidCharacter(charNumber[i])) {
                    return true;
                }
            }
        } else {
            for (int i = 0; i < count; i++) {
                if (isSBCCharacter(charNumber, i)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
    * Return true if the charNumber belongs full-width characters
    */
    private boolean isSBCCharacter(char[] charNumber, int i) {
        return charNumber[i] >= SBC_CHAR_START && charNumber[i] <= SBC_CHAR_END;
    }

    private boolean isValidCharacter(char c) {
        return (c >= '0' && c <= '9') || c == '-' || c == '(' || c == ')' || c == ' ';
    }

    public int getValidRecipientsCount(boolean isMms) {
        int validNum = 0;
        int invalidNum = 0;
        for (String number : mContactList.getNumbers()) {//mTokenizer.getNumbers()) {
            if (isValidAddress(number, isMms)) {
                validNum++;
            } else {
                invalidNum++;
            }
        }
        int count = mContactList.size();//mTokenizer.getNumbers().size();
        if (validNum == count) {
            return MessageUtils.ALL_RECIPIENTS_VALID;
        } else if (invalidNum == count) {
            return MessageUtils.ALL_RECIPIENTS_INVALID;
        }
        return invalidNum;

    }

    public boolean hasInvalidRecipient(boolean isMms) {
        /*for (String number : mTokenizer.getNumbers()) {
            if (!isValidAddress(number, isMms)) {
                if (MmsConfig.getEmailGateway() == null) {
                    return true;
                } else if (!MessageUtils.isAlias(number)) {
                    return true;
                }
            }
        }*/
        for (String number : mContactList.getNumbers()) {
            if (!isValidAddress(number, isMms)) {
                if (MmsConfig.getEmailGateway() == null) {
                    return true;
                } else if (!MessageUtils.isAlias(number)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean RecipientCheckValid(String number, boolean isMms) {
        if (!isValidAddress(number, isMms)) {
            if (MmsConfig.getEmailGateway() == null) {
                return true;
            } else if (!MessageUtils.isAlias(number)) {
                return true;
            }
        }
        return false;
    }

    public String formatInvalidNumbers(boolean isMms) {
        StringBuilder sb = new StringBuilder();
        for (String number : mContactList.getNumbers()) {//mTokenizer.getNumbers()) {
            if (!isValidAddress(number, isMms)) {
                if (sb.length() != 0) {
                    sb.append(", ");
                }
                sb.append(number);
            }
        }
        return sb.toString();
    }

    public boolean containsEmail() {
        if (TextUtils.indexOf(getText(), '@') == -1)
            return false;

        List<String> numbers = mContactList.getNumbersList();//mTokenizer.getNumbers();
        for (String number : numbers) {
            if (Mms.isEmailAddress(number))
                return true;
        }
        return false;
    }

    public static CharSequence contactToToken(Contact c) {
        SpannableString s = new SpannableString(c.getNameAndNumber());
        int len = s.length();

        if (len == 0) {
            return s;
        }

        s.setSpan(new Annotation("number", c.getNumber()), 0, len,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return s;
    }

    //begin tangyisen
    //private CharSequence mRecipientsList;
    //end tangyisen
    public void populate(ContactList list) {
        if (list.size() == 0) {
            // The base class RecipientEditTextView will ignore empty text. That's why we need
            // this special case.
            setText(null);
        } else {
            // Clear the recipient when add contact again
            setText("");
            mContactList.clear();
            mContactList.addAll(list);
            for (Contact c : list) {
                //CharSequence charSequence = contactToToken(c);
                CharSequence charSequence = c.getName();
                if (charSequence != null && charSequence.length() > 0) {
                    append( charSequence+ STRING_DIVIDER);
                    addDotSpan(getText(), getText().toString().length() - 1);
                }
            }
        }
    }

    private int pointToPosition(int x, int y) {
        // Check layout before getExtendedPaddingTop().
        // mLayout is used in getExtendedPaddingTop().
        Layout layout = getLayout();
        if (layout == null) {
            return -1;
        }

        x -= getCompoundPaddingLeft();
        y -= getExtendedPaddingTop();


        x += getScrollX();
        y += getScrollY();

        int line = layout.getLineForVertical(y);
        int off = layout.getOffsetForHorizontal(line, x);

        return off;
    }


    //tangyisen begin
    private void showInputMethod() {
        //View focusView = this.getCurrentFocus();
        final InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            //imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            imm.showSoftInput(this, 0);
        }
    }

    private OnDropDownListener mOnDropDownListener;
    public interface OnDropDownListener {
        void onDropDownShow();
        void onDropDownDismiss();
    }
    public void setOnDropDownListener(OnDropDownListener listener) {
        mOnDropDownListener = listener;
    }
    @Override
    public void showDropDown() {
        super.showDropDown();
        if(mOnDropDownListener != null) {
            mOnDropDownListener.onDropDownShow();
        }
    }

    @Override
    public void dismissDropDown() {
        super.dismissDropDown();
        if(mOnDropDownListener != null) {
            mOnDropDownListener.onDropDownDismiss();
        }
    }

    @Override
    public <T extends ListAdapter & Filterable> void setAdapter(@NonNull T adapter) {
        super.setAdapter(adapter);
        BaseRecipientAdapter baseAdapter = (BaseRecipientAdapter) adapter;
        baseAdapter.registerUpdateObserver(new BaseRecipientAdapter.EntriesUpdatedObserver() {
            @Override
            public void onChanged(List<RecipientEntry> entries) {
                // Scroll the chips field to the top of the screen so
                // that the user can see as many results as possible.
                /*if (entries != null && entries.size() > 0) {
                    scrollBottomIntoView();
                    // Here the current suggestion count is still the old one since we update
                    // the count at the bottom of this function.
                    if (mCurrentSuggestionCount == 0) {
                        // Announce the new number of possible choices for accessibility.
                        announceForAccessibilityCompat(getContext().getString(
                                R.string.accessbility_suggestion_dropdown_opened));
                    }
                }

                // Set the dropdown height to be the remaining height from the anchor to the bottom.
                mDropdownAnchor.getLocationInWindow(mCoords);
                getWindowVisibleDisplayFrame(mRect);
                setDropDownHeight(mRect.bottom - mCoords[1] - mDropdownAnchor.getHeight() -
                    getDropDownVerticalOffset());

                mCurrentSuggestionCount = entries == null ? 0 : entries.size();*/
            }
        });
        baseAdapter.setDropdownChipLayouter(mDropdownChipLayouter);
    }

    private static String getNumberAt(Spanned sp, int start, int end, Context context) {
        String number = getFieldAt("number", sp, start, end, context);
        number = PhoneNumberUtils.replaceUnicodeDigits(number);
        if (!TextUtils.isEmpty(number)) {
            if (!Mms.isPhoneNumber(number)) {
                number = number.replaceAll(" ", "");
            }
            int pos = number.indexOf('<');
            if (pos >= 0 && pos < number.indexOf('>')) {
                // The number looks like an Rfc882 address, i.e. <fred flinstone> 891-7823
                Rfc822Token[] tokens = Rfc822Tokenizer.tokenize(number);
                if (tokens.length == 0) {
                    return number;
                }
                return tokens[0].getAddress();
            }
        }
        return number;
    }

    private static int getSpanLength(Spanned sp, int start, int end, Context context) {
        // TODO: there's a situation where the span can lose its annotations:
        //   - add an auto-complete contact
        //   - add another auto-complete contact
        //   - delete that second contact and keep deleting into the first
        //   - we lose the annotation and can no longer get the span.
        // Need to fix this case because it breaks auto-complete contacts with commas in the name.
        Annotation[] a = sp.getSpans(start, end, Annotation.class);
        if (a.length > 0) {
            return sp.getSpanEnd(a[0]);
        }
        return 0;
    }

    private static String getFieldAt(String field, Spanned sp, int start, int end,
            Context context) {
        Annotation[] a = sp.getSpans(start, end, Annotation.class);
        String fieldValue = getAnnotation(a, field);
        if (TextUtils.isEmpty(fieldValue)) {
            fieldValue = TextUtils.substring(sp, start, end);
        }
        return fieldValue;
    }

    private static String getAnnotation(Annotation[] a, String key) {
        for (int i = 0; i < a.length; i++) {
            if (a[i].getKey().equals(key)) {
                return a[i].getValue();
            }
        }
        return "";
    }
    private class MstRecipientsEditorTokenizer implements MultiAutoCompleteTextView.Tokenizer {

        @Override
        public int findTokenStart(CharSequence text, int cursor) {
            int i = cursor;
            char c;

            // If we're sitting at a delimiter, back up so we find the previous token
            if (isFocused() && i > 0 && ((c = text.charAt(i - 1)) == CHAR_DIVIDER)) {
                return i;
            }
            if (i > 0 && ((c = text.charAt(i - 1)) == ',' || c == ';' || c == CHAR_DIVIDER)) {
                --i;
            }
            // Now back up until the start or until we find the separator of the previous token
            while (i > 0 && (c = text.charAt(i - 1)) != ',' && c != ';' && c != CHAR_DIVIDER) {
                i--;
            }
            /*if (i > 0 && ((c = text.charAt(i - 1)) == CHAR_DIVIDER)) {
                return i;
            }

            while (i > 0 && (c = text.charAt(i - 1)) != ',' && c != ';' && c != CHAR_DIVIDER) {
                i--;
            }*/

            while (i < cursor && text.charAt(i) == ' ') {
                i++;
            }
            // filter Full width space
            while (i < cursor && text.charAt(i) == '\u3000') {
                i++;
            }

            return i;
        }

        @Override
        public int findTokenEnd(CharSequence text, int cursor) {
            int i = cursor;
            int len = text.length();
            char c;

            while (i < len) {
                if ((c = text.charAt(i)) == ',' || c == ';' || c == CHAR_DIVIDER) {
                    return i;
                } else {
                    i++;
                }
            }

            return len;
        }

        @Override
        public CharSequence terminateToken(CharSequence text) {
            int i = text.length();

            while (i > 0 && text.charAt(i - 1) == ' ') {
                i--;
            }

            char c;
            if (i > 0 && ((c = text.charAt(i - 1)) == ',' || c == ';' || c == CHAR_DIVIDER)) {
                return text;
            } else {
                // Use the same delimiter the user just typed.
                // This lets them have a mixture of commas and semicolons in their list.
                String separator = mLastSeparator + " ";
                if (text instanceof Spanned) {
                    SpannableString sp = new SpannableString(text + separator);
                    TextUtils.copySpansFrom((Spanned)text, 0, text.length(), Object.class, sp, 0);
                    return sp;
                } else {
                    return text + separator;
                }
            }
        }

        public List<String> getNumbers() {
            Spanned sp = MstRecipientsEditor.this.getText();
            int len = sp.length();
            List<String> list = new ArrayList<String>();

            int start = 0;
            int i = 0;
            while (i < len + 1) {
                char c;
                if ((i == len) || ((c = sp.charAt(i)) == ',') || (c == ';') || (c == CHAR_DIVIDER)) {
                    if (i > start) {
                        list.add(getNumberAt(sp, start, i, getContext()));
                        // calculate the recipients total length. This is so if
                        // the name contains
                        // commas or semis, we'll skip over the whole name to
                        // the next
                        // recipient, rather than parsing this single name into
                        // multiple
                        // recipients.
                        int spanLen = getSpanLength(sp, start, i, getContext());
                        if (spanLen > i) {
                            i = spanLen;
                        }
                    }

                    i++;

                    while ((i < len) && (sp.charAt(i) == ' ')) {
                        i++;
                    }

                    start = i;
                } else {
                    i++;
                }
            }

            return list;
        }

        public String getNumbersString() {
            Spanned sp = MstRecipientsEditor.this.getText();
            int len = sp.length();
            StringBuilder sb = new StringBuilder();
            int start = 0;
            int i = 0;
            while (i < len + 1) {
                char c;
                if ((i == len) || ((c = sp.charAt(i)) == ',') || (c == ';')  || (c == CHAR_DIVIDER)) {
                    if (i > start) {
                        sb.append("'" + getNumberAt(sp, start, i, mContext) + "',");
                        // calculate the recipients total length. This is so if
                        // the name contains
                        // commas or semis, we'll skip over the whole name to
                        // the next
                        // recipient, rather than parsing this single name into
                        // multiple
                        // recipients.
                        int spanLen = getSpanLength(sp, start, i, mContext);
                        if (spanLen > i) {
                            i = spanLen;
                        }
                    }
                    i++;
                    while ((i < len) && (sp.charAt(i) == ' ')) {
                        i++;
                    }
                    start = i;
                } else {
                    i++;
                }
            }

            return (sb.length() != 0) ? (sb.deleteCharAt(sb.length() - 1).toString()) : null;
        }
    }

    private class MyDropdownChipLayouter extends DropdownChipLayouter{

        private int mAutocompleteDividerMarginStartReflect;
        public MyDropdownChipLayouter(LayoutInflater inflater, Context context) {
            super(inflater, context);
            try {
                Field field = DropdownChipLayouter.class.getDeclaredField("mAutocompleteDividerMarginStart");
                field.setAccessible(true);
                mAutocompleteDividerMarginStartReflect = (int)field.get(this);
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 
        }

        @Override
        protected int getItemLayoutResId(AdapterType type) {
            //return R.layout.mms_chips_recipient_dropdown_item;
            return R.layout.recipients_editor_popupwindow;
        }

        public View bindView(View convertView, ViewGroup parent, RecipientEntry entry, int position,
            AdapterType type, String constraint, StateListDrawable deleteDrawable) {
            // Default to show all the information
            CharSequence[] styledResults =
                getStyledResults(constraint, entry.getDisplayName(), entry.getDestination());
            CharSequence displayName = styledResults[0];
            CharSequence destination = styledResults[1];
            boolean showImage = true;
            CharSequence destinationType = getDestinationType(entry);

            final View itemView = reuseOrInflateView(convertView, parent, type);

            final ViewHolder viewHolder = new ViewHolder(itemView);

            if (TextUtils.isEmpty(displayName) || TextUtils.equals(displayName, destination)) {
                displayName = destination;
                // We only show the destination for secondary entries, so clear it only for the
                // first level.
                if (entry.isFirstLevel()) {
                    destination = null;
                }
            }

            // For BASE_RECIPIENT set all top dividers except for the first one to be GONE.
            if (viewHolder.topDivider != null) {
                viewHolder.topDivider.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
                MarginLayoutParamsCompat.setMarginStart((MarginLayoutParams)viewHolder.topDivider.getLayoutParams(),
                    mAutocompleteDividerMarginStartReflect);
            }
            if (viewHolder.bottomDivider != null) {
                MarginLayoutParamsCompat.setMarginStart((MarginLayoutParams)viewHolder.bottomDivider.getLayoutParams(),
                    mAutocompleteDividerMarginStartReflect);
            }
            // Bind the information to the view
            bindTextToView(displayName, viewHolder.displayNameView);
            bindTextToView(destination, viewHolder.destinationView);
            bindTextToView(destinationType, viewHolder.destinationTypeView);
            bindIconToView(showImage, entry, viewHolder.imageView, type);
            bindDrawableToDeleteView(deleteDrawable, entry.getDisplayName(), viewHolder.deleteView);

            return itemView;
        }

        @Override
        protected CharSequence[] getStyledResults(@Nullable String constraint, String... results) {
            if (isAllWhitespace(constraint)) {
                return results;
            }

            CharSequence[] styledResults = new CharSequence[results.length];
            boolean foundMatch = false;
            for (int i = 0; i < results.length; i++) {
                String result = results[i];
                if (result == null) {
                    continue;
                }

                if (!foundMatch) {
                    int index = result.toLowerCase().indexOf(constraint.toLowerCase());
                    if (index != -1) {
                        SpannableStringBuilder styled = SpannableStringBuilder.valueOf(result);
                        ForegroundColorSpan highlightSpan =
                                new ForegroundColorSpan(mContext.getResources().getColor(
                                        R.color.recipients_edit_hightlight));
                        styled.setSpan(highlightSpan,
                                index, index + constraint.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        styledResults[i] = styled;
                        foundMatch = true;
                        continue;
                    }
                }
                styledResults[i] = result;
            }
            return styledResults;
        }
    }

    private static boolean isAllWhitespace(@Nullable String string) {
        if (TextUtils.isEmpty(string)) {
            return true;
        }

        for (int i = 0; i < string.length(); ++i) {
            if (!Character.isWhitespace(string.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    private Layout mLayout;
    private int mLineCount = 0;
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
