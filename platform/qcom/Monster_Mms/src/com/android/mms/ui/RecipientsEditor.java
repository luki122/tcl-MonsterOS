/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mms.ui;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import mst.provider.Telephony.Mms;
import android.telephony.PhoneNumberUtils;
import android.text.Annotation;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;
import android.util.AttributeSet;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import com.android.ex.chips.DropdownChipLayouter;
import com.android.ex.chips.RecipientAlternatesAdapter;
import com.android.ex.chips.RecipientEditTextView;
import com.android.ex.chips.RecipientEntry;
import com.android.ex.chips.DropdownChipLayouter.AdapterType;
import com.android.ex.chips.recipientchip.DrawableRecipientChip;
import com.android.ex.chips.recipientchip.DrawableRecipientChip;
import com.android.ex.chips.recipientchip.ReplacementDrawableSpan;
import com.android.mms.MmsConfig;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.ui.StretchyTextView.OnDoubleClickListener;
import com.android.mms.ui.StretchyTextView.OnSingleClickListener;

import android.support.annotation.Nullable;
import android.view.GestureDetector;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Provide UI for editing the recipients of multi-media messages.
 */
public class RecipientsEditor extends RecipientEditTextView {
    private static final char SBC_CHAR_START = 65281;
    private static final char SBC_CHAR_END = 65373;

    private int mLongPressedPosition = -1;
    private final RecipientsEditorTokenizer mTokenizer;
    private char mLastSeparator = ',';
    private Runnable mOnSelectChipRunnable;
    private final AddressValidator mInternalValidator;
    private Context mContext;

    //tangyisen begin
    //private ListPopupWindow mAlternatesPopupReflect;
    //private ListPopupWindow mAddressPopupReflect;
    //private int mUnselectedChipBackgroundColorReflect;
    //private DrawableRecipientChip mSelectedChipReflect;
    private GestureDetector mGestureDetectorReflect;
    //private String mCopyAddressReflect;
    private Method putOffsetInRangeFun;
    private Method findChipFun;
    private Method removeChipFun;
    private Field mCursorDrawableResField;
    private Method alreadyHasChipFun;
    //private Drawable mChipBackgroundReflect;
    //mSelectedChip,mGestureDetector,mCopyAddress,putOffsetInRange,findChip,removeChip
    //tangyisen end

    /** A noop validator that does not munge invalid texts and claims any address is valid */
    private class AddressValidator implements Validator {
        public CharSequence fixText(CharSequence invalidText) {
            return invalidText;
        }

        public boolean isValid(CharSequence text) {
            return true;
        }
    }

    public RecipientsEditor(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mTokenizer = new RecipientsEditorTokenizer();
        setTokenizer(mTokenizer);

        mInternalValidator = new AddressValidator();
        super.setValidator(mInternalValidator);

        // For the focus to move to the message body when soft Next is pressed
        setImeOptions(EditorInfo.IME_ACTION_NEXT);

        setThreshold(1);    // pop-up the list after a single char is typed

        /*
         * The point of this TextWatcher is that when the user chooses
         * an address completion from the AutoCompleteTextView menu, it
         * is marked up with Annotation objects to tie it back to the
         * address book entry that it came from.  If the user then goes
         * back and edits that part of the text, it no longer corresponds
         * to that address book entry and needs to have the Annotations
         * claiming that it does removed.
         */
        //tangyisen begin
        int screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        try {
            /*Field field1 = RecipientEditTextView.class.getDeclaredField("mAlternatesPopup");
            field1.setAccessible(true);
            mAlternatesPopupReflect = (ListPopupWindow)field1.get(this);
            if (mAlternatesPopupReflect != null) {
                mAlternatesPopupReflect.setWidth(screenWidth);
            }
            Field field2 = RecipientEditTextView.class.getDeclaredField("mAddressPopup");
            field2.setAccessible(true);
            mAddressPopupReflect = (ListPopupWindow)field2.get(this);
            if (mAddressPopupReflect != null) {
                mAddressPopupReflect.setWidth(screenWidth);
            }*/
            /*Field field = RecipientEditTextView.class.getDeclaredField("mUnselectedChipBackgroundColor");
            field.setAccessible(true);
            field.set(this, mContext.getResources().getColor(R.color.recipients_edit_hightlight));*/

            /*field = RecipientEditTextView.class.getDeclaredField("mSelectedChip");
            field.setAccessible(true);
            mSelectedChipReflect = (DrawableRecipientChip)field.get(this);*/

            Field field = RecipientEditTextView.class.getDeclaredField("mGestureDetector");
            field.setAccessible(true);
            mGestureDetectorReflect = (GestureDetector)field.get(this);

            mCursorDrawableResField = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableResField.setAccessible(true);

            /*field = RecipientEditTextView.class.getDeclaredField("mChipBackground");
            field.setAccessible(true);
            field.set(this, mContext.getResources().getDrawable(R.drawable.recipients_editor_bg));*/

            /*field = RecipientEditTextView.class.getDeclaredField("mCopyAddress");
            field.setAccessible(true);
            mCopyAddressReflect = (String)field.get(this);*/

            putOffsetInRangeFun = RecipientEditTextView.class.getDeclaredMethod("putOffsetInRange", float.class,float.class);
            putOffsetInRangeFun.setAccessible(true);

            findChipFun = RecipientEditTextView.class.getDeclaredMethod("findChip", int.class);
            findChipFun.setAccessible(true);

            removeChipFun = RecipientEditTextView.class.getDeclaredMethod("removeChip", DrawableRecipientChip.class);
            removeChipFun.setAccessible(true);

            alreadyHasChipFun = RecipientEditTextView.class.getDeclaredMethod("alreadyHasChip", int.class, int.class);
            alreadyHasChipFun.setAccessible(true);

        }catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        //tangyisen end
        addTextChangedListener(new TextWatcher() {
            private Annotation[] mAffected;

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                    int count, int after) {
                mAffected = ((Spanned) s).getSpans(start, start + count,
                        Annotation.class);
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                    int before, int after) {
                // inserting a character
                if (before == 0 && after == 1 &&
                        start >= 0 && start < s.length()) {
                    char c = s.charAt(start);
                    if (c == ',' || c == ';') {
                        // Remember the delimiter the user typed to end this recipient. We'll
                        // need it shortly in terminateToken().
                        mLastSeparator = c;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mAffected != null) {
                    for (Annotation a : mAffected) {
                        s.removeSpan(a);
                    }
                }
                mAffected = null;
                //begin tangyisen
             //   try {
                   /* if (s != null && s.length() > 30 && (boolean)alreadyHasChipFun.invoke(this, 0, s.length() - 1)) {
                        setCursorDrawableRes(R.drawable.recipients_cursor);
                    } else {
                        setCursorDrawableRes(R.drawable.recipients_chips_cursor);
                    }*/
               /*  }catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }*/
                //end tangyisen
            }
        });

        setDropDownBackgroundResource(R.drawable.recipients_editor_dropdown_bg);
        setDropdownChipLayouter(new MyDropdownChipLayouter(LayoutInflater.from(context), context));
    }

    //begin tangyisen
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
    //end tangyisen
    

    //begin tangyisen
    @Override
    public boolean onEditorAction(TextView view, int action, KeyEvent keyEvent) {
        //boolean rtn = super.onEditorAction(view, action, keyEvent);
        if (action == EditorInfo.IME_ACTION_DONE) {
            Log.d("RecipientEditTextView", "RecipientsEditor, onEditorAction(), IME_ACTION_DONE");
            if(mOnEditorOperateListener != null) {
                mOnEditorOperateListener.onEditorDoneClick(getText().toString());
                setText(null);
            }
        } /*else if (action == EditorInfo.IME)*/
        return true;
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

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        boolean rtn = super.onKeyDown(keyCode, event);
        if(!rtn && keyCode == KeyEvent.KEYCODE_DEL) {
            if(mOnEditorOperateListener != null) {
                mOnEditorOperateListener.onEditorDelClick(getText().toString());
            }
            return true;
        }
        return rtn;
    }
    //end tangyisen
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

    //begin tangyisen
    @Override
    public void onFocusChanged(boolean hasFocus, int direction, Rect previous) {
        //super.onFocusChanged(hasFocus, direction, previous);
        if(hasFocus) {
            showInputMethod();
            super.onFocusChanged(hasFocus, direction, previous);
            //showInputMethod();
        } else if(mOnEditorOperateListener != null) {
            mOnEditorOperateListener.onEditorDoneClick(getText().toString());
        }
        setText(null);
    }

    /*private void createMstMoreChip() {
        if (mNoChips) {
            createMoreChipPlainText();
            return;
        }

        if (!mShouldShrink) {
            return;
        }
        Field field = null;
        Field field2 = null;
        Method method = null;
        
        try {
            Class<?> moreImageSpan = Class.forName("com.android.ex.chips.RecipientEditTextView.MoreImageSpan");

            ReplacementDrawableSpan[] tempMore = getText().getSpans(0, getText().length(), moreImageSpan);

            if (tempMore.length > 0) {
                getText().removeSpan(tempMore[0]);
            }

            method = RecipientsEditor.class.getDeclaredMethod("getSortedRecipients ");
            method.setAccessible(true);
            DrawableRecipientChip[] recipients = (DrawableRecipientChip[])method.invoke(this);

            if (recipients == null || recipients.length <= 2RecipientsEditTExtView.CHIP_LIMIT) {
                // mMoreChip = null;
                field = RecipientEditTextView.class.getDeclaredField("mMoreChip");
                field.setAccessible(true);
                field.set(this, null);
                return;
            }
            Spannable spannable = getText();
            int numRecipients = recipients.length;
            int overage = numRecipients - 2;//RecipientsEditTExtView.CHIP_LIMIT;
            // MoreImageSpan moreSpan = createMoreSpan(overage);
            method = RecipientsEditor.class.getDeclaredMethod("createMoreSpan", int.class);
            method.setAccessible(true);
            Object moreSpan = method.invoke(this, overage);

            field = RecipientEditTextView.class.getDeclaredField("mRemovedSpans");
            field.setAccessible(true);
            ArrayList<DrawableRecipientChip> mRemovedSpans = (ArrayList<DrawableRecipientChip>)field.get(this);
            ArrayList<DrawableRecipientChip> mRemovedSpans2 =  new ArrayList<DrawableRecipientChip>();
            mRemovedSpans.set(this,mRemovedSpans2);
            int totalReplaceStart = 0;
            int totalReplaceEnd = 0;
            Editable text = getText();

            field = RecipientEditTextView.class.getDeclaredField("mTemporaryRecipients");
            field.setAccessible(true);
            ArrayList<DrawableRecipientChip> mTemporaryRecipients = (ArrayList<DrawableRecipientChip>)field.get(this);

            for (int i = numRecipients - overage; i < recipients.length; i++) {
                // mRemovedSpans.add(recipients[i]);
                if (i == numRecipients - overage) {
                    totalReplaceStart = spannable.getSpanStart(recipients[i]);
                }
                if (i == recipients.length - 1) {
                    totalReplaceEnd = spannable.getSpanEnd(recipients[i]);
                }
                if (mTemporaryRecipients == null || !mTemporaryRecipients.contains(recipients[i])) {
                    int spanStart = spannable.getSpanStart(recipients[i]);
                    int spanEnd = spannable.getSpanEnd(recipients[i]);
                    recipients[i].setOriginalText(text.toString().substring(spanStart, spanEnd));
                }
                spannable.removeSpan(recipients[i]);
            }
            if (totalReplaceEnd < text.length()) {
                totalReplaceEnd = text.length();
            }
            int end = Math.max(totalReplaceStart, totalReplaceEnd);
            int start = Math.min(totalReplaceStart, totalReplaceEnd);
            SpannableString chipText = new SpannableString(text.subSequence(start, end));
            chipText.setSpan(moreSpan, 0, chipText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.replace(start, end, chipText);
            // mMoreChip = moreSpan;
            field = RecipientEditTextView.class.getDeclaredField("mMoreChip");
            field.setAccessible(true);
            field.set(this, moreSpan);

            field = RecipientEditTextView.class.getDeclaredField("mMaxLines");
            field.setAccessible(true);
            if (!isPhoneQuery() && getLineCount() > field.getInt(this)) {
                setMaxLines(getLineCount());
            }
        }catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }*/
    //end tangyisen

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        super.onItemClick(parent, view, position, id);

        //RecipientEntry entry = ((RecipientAlternatesAdapter) parent.getAdapter()).getRecipientEntry(position);
        //begin tangyisen add for select from dropdown list
        if(null != mOnEditorOperateListener) {
            String origin = getText().toString();
            String number = new String(origin.substring(0, origin.length() - 2));
            mOnEditorOperateListener.onEditorDropDownSelected(number);
            setText(null);
        }
        //end tangyisen
        
        if (mOnSelectChipRunnable != null) {
            mOnSelectChipRunnable.run();
        }
    }

    public void setOnSelectChipRunnable(Runnable onSelectChipRunnable) {
        mOnSelectChipRunnable = onSelectChipRunnable;
    }

    //tangyisen begin
    @Override
    public void onLongPress(MotionEvent event){
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
        return mTokenizer.getNumbers().size();
    }

    public List<String> getNumbers() {
        return mTokenizer.getNumbers();
    }

    public String getExsitNumbers(){
        return mTokenizer.getNumbersString();
    }

    public ContactList constructContactsFromInput(boolean blocking) {
        List<String> numbers = mTokenizer.getNumbers();
        ContactList list = new ContactList();
        for (String number : numbers) {
            Contact contact = Contact.get(number, blocking);
            contact.setNumber(number);
            list.add(contact);
        }
        return list;
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
        for (String number : mTokenizer.getNumbers()) {
            if (isValidAddress(number, isMms)) {
                validNum++;
            } else {
                invalidNum++;
            }
        }
        int count = mTokenizer.getNumbers().size();
        if (validNum == count) {
            return MessageUtils.ALL_RECIPIENTS_VALID;
        } else if (invalidNum == count) {
            return MessageUtils.ALL_RECIPIENTS_INVALID;
        }
        return invalidNum;

    }

    public boolean hasInvalidRecipient(boolean isMms) {
        for (String number : mTokenizer.getNumbers()) {
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

    public String formatInvalidNumbers(boolean isMms) {
        StringBuilder sb = new StringBuilder();
        for (String number : mTokenizer.getNumbers()) {
            if (!isValidAddress(number, isMms)) {
                if (sb.length() != 0) {
                    sb.append(", ");
                }
                sb.append(number);
            }
        }
        return sb.toString();
    }

    //begin tangyisen add tmp
    public int getValidRecipientsCount(boolean isMms, ContactList list) {
        int validNum = 0;
        int invalidNum = 0;
        for (String number : list.getNumbers()) {
            if (isValidAddress(number, isMms)) {
                validNum++;
            } else {
                invalidNum++;
            }
        }
        int count = list.size();
        if (validNum == count) {
            return MessageUtils.ALL_RECIPIENTS_VALID;
        } else if (invalidNum == count) {
            return MessageUtils.ALL_RECIPIENTS_INVALID;
        }
        return invalidNum;
    }

    public boolean hasInvalidRecipient(boolean isMms, ContactList list) {
        for (String number : list.getNumbers()) {
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

    public String formatInvalidNumbers(boolean isMms, ContactList list) {
        StringBuilder sb = new StringBuilder();
        for (String number : list.getNumbers()) {
            if (!isValidAddress(number, isMms)) {
                if (sb.length() != 0) {
                    sb.append(", ");
                }
                sb.append(number);
            }
        }
        return sb.toString();
    }

    public boolean containsEmail(ContactList list) {
        if (TextUtils.indexOf(getText(), '@') == -1)
            return false;

        List<String> numbers = list.getNumbersList();
        for (String number : numbers) {
            if (Mms.isEmailAddress(number))
                return true;
        }
        return false;
    }
    //end tangyisen add tmp

    public boolean containsEmail() {
        if (TextUtils.indexOf(getText(), '@') == -1)
            return false;

        List<String> numbers = mTokenizer.getNumbers();
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
    private CharSequence mRecipientsList;
    //end tangyisen
    public void populate(ContactList list) {
        // Very tricky bug. In the recipient editor, we always leave a trailing
        // comma to make it easy for users to add additional recipients. When a
        // user types (or chooses from the dropdown) a new contact Mms has never
        // seen before, the contact gets the correct trailing comma. But when the
        // contact gets added to the mms's contacts table, contacts sends out an
        // onUpdate to CMA. CMA would recompute the recipients and since the
        // recipient editor was still visible, call mRecipientsEditor.populate(recipients).
        // This would replace the recipient that had a comma with a recipient
        // without a comma. When a user manually added a new comma to add another
        // recipient, this would eliminate the span inside the text. The span contains the
        // number part of "Fred Flinstone <123-1231>". Hence, the whole
        // "Fred Flinstone <123-1231>" would be considered the number of
        // the first recipient and get entered into the canonical_addresses table.
        // The fix for this particular problem is very easy. All recipients have commas.
        // TODO: However, the root problem remains. If a user enters the recipients editor
        // and deletes chars into an address chosen from the suggestions, it'll cause
        // the number annotation to get deleted and the whole address (name + number) will
        // be used as the number.
        if (list.size() == 0) {
            // The base class RecipientEditTextView will ignore empty text. That's why we need
            // this special case.
            setText(null);
        } else {
            // Clear the recipient when add contact again
            setText("");
            for (Contact c : list) {
                // Calling setText to set the recipients won't create chips,
                // but calling append() will.

                // Need to judge  whether contactToToken(c) return valid data,if it is not,
                // do not append it so that the comma can not be displayed.
                CharSequence charSequence = contactToToken(c);
                if (charSequence != null && charSequence.length() > 0) {
                    append( charSequence+ ", ");
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /*final int action = ev.getAction();
        final int x = (int) ev.getX();
        final int y = (int) ev.getY();

        if (action == MotionEvent.ACTION_DOWN) {
            mLongPressedPosition = pointToPosition(x, y);
        }

        return super.onTouchEvent(ev);*/
        showInputMethod();
        if (!isFocused()) {
            // Ignore any chip taps until this view is focused.
            boolean rtn = super.onTouchEvent(event);
            //showInputMethod();
            return rtn;
        }
        //boolean handled = super.onTouchEvent(event);
        int action = event.getAction();
        mGestureDetectorReflect.onTouchEvent(event);
        if (action == MotionEvent.ACTION_UP) {
            float x = event.getX();
            float y = event.getY();
            //int offset = putOffsetInRange(x, y);
            //DrawableRecipientChip currentChip = findChip(offset);
            try {
                int offset = (int)putOffsetInRangeFun.invoke(this, x, y);
                DrawableRecipientChip currentChip = (DrawableRecipientChip)findChipFun.invoke(this, offset);
                if (currentChip != null) {
                    removeChipFun.invoke(this, currentChip);
                    //handled = true;
                } else {
                    showInputMethod();
                }
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 
        }
        return true;
        //return super.onTouchEvent(event);
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

    public void setCursorDrawableRes(int res) {
        try{
            mCursorDrawableResField.setInt(this, res);
        }catch(IllegalAccessException e){
            e.printStackTrace();
        }
    }
    //tangyisen end

    @Override
    protected ContextMenuInfo getContextMenuInfo() {
        //tangyisen delete when long press show nothing
        /*if ((mLongPressedPosition >= 0)) {
            Spanned text = getText();
            if (mLongPressedPosition <= text.length()) {
                int start = mTokenizer.findTokenStart(text, mLongPressedPosition);
                int end = mTokenizer.findTokenEnd(text, start);

                if (end != start) {
                    String number = getNumberAt(getText(), start, end, getContext());
                    Contact c = Contact.get(number, false);
                    return new RecipientContextMenuInfo(c);
                }
            }
        }*/
        return null;
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

    private class RecipientsEditorTokenizer
            implements MultiAutoCompleteTextView.Tokenizer {

        @Override
        public int findTokenStart(CharSequence text, int cursor) {
            int i = cursor;
            char c;

            // If we're sitting at a delimiter, back up so we find the previous token
            if (i > 0 && ((c = text.charAt(i - 1)) == ',' || c == ';')) {
                --i;
            }
            // Now back up until the start or until we find the separator of the previous token
            while (i > 0 && (c = text.charAt(i - 1)) != ',' && c != ';') {
                i--;
            }
            while (i < cursor && text.charAt(i) == ' ') {
                i++;
            }
            //filter Full width space
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
                if ((c = text.charAt(i)) == ',' || c == ';') {
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
            if (i > 0 && ((c = text.charAt(i - 1)) == ',' || c == ';')) {
                return text;
            } else {
                // Use the same delimiter the user just typed.
                // This lets them have a mixture of commas and semicolons in their list.
                String separator = mLastSeparator + " ";
                if (text instanceof Spanned) {
                    SpannableString sp = new SpannableString(text + separator);
                    TextUtils.copySpansFrom((Spanned) text, 0, text.length(),
                                            Object.class, sp, 0);
                    return sp;
                } else {
                    return text + separator;
                }
            }
        }

        public List<String> getNumbers() {
            Spanned sp = RecipientsEditor.this.getText();
            int len = sp.length();
            List<String> list = new ArrayList<String>();

            int start = 0;
            int i = 0;
            while (i < len + 1) {
                char c;
                if ((i == len) || ((c = sp.charAt(i)) == ',') || (c == ';')) {
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
            Spanned sp = RecipientsEditor.this.getText();
            int len = sp.length();
            StringBuilder sb = new StringBuilder();
            int start = 0;
            int i = 0;
            while (i < len + 1) {
                char c;
                if ((i == len) || ((c = sp.charAt(i)) == ',') || (c == ';')) {
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

    static class RecipientContextMenuInfo implements ContextMenuInfo {
        final Contact recipient;

        RecipientContextMenuInfo(Contact r) {
            recipient = r;
        }
    }
}
