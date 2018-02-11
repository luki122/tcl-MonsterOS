/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;

import mst.app.dialog.AlertDialog;
import mst.app.dialog.AlertDialog.Builder;

import java.io.UnsupportedEncodingException;

import cn.tcl.filemanager.R;
import cn.tcl.filemanager.activity.SafeBoxSettingsActivity;
import cn.tcl.filemanager.utils.CommonUtils;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.ToastHelper;

public class AlertDialogFragment extends DialogFragment implements
        OnClickListener {
    public static final String TAG = "AlertDialogFragment";

    private static final String TITLE = "title";
    private static final String CANCELABLE = "cancelable";
    private static final String ICON = "icon";
    private static final String MESSAGE = "message";
    private static final String LAYOUT = "layout";
    private static final String NEGATIVE_TITLE = "negativeTitle";
    private static final String POSITIVE_TITLE = "positiveTitle";

    public static final int INVIND_RES_ID = -1;

    protected OnClickListener mDoneListener;
    protected OnDismissListener mDismissListener;
    protected OnClickListener mOnCancelListener; // MODIFIED by zibin.wang, 2016-05-06,BUG-2019352
    protected ToastHelper mToastHelper;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putAll(getArguments());
        super.onSaveInstanceState(outState);
    }

    public static class AlertDialogFragmentBuilder {
        protected final Bundle mBundle = new Bundle();

        /**
         * This method creates AlertDialogFragment with parameter of mBundle.
         *
         * @return AlertDialogFragment
         */
        public AlertDialogFragment create() {
            AlertDialogFragment f = new AlertDialogFragment();
            f.setArguments(mBundle);
            return f;
        }

        /**
         * This method sets TITLE for AlertDialogFragmentBuilder, which responds
         * to title of dialog.
         *
         * @param resId resource id of title
         * @return AlertDialogFragmentBuilder
         */
        public AlertDialogFragmentBuilder setTitle(int resId) {
            mBundle.putInt(TITLE, resId);
            return this;
        }

        /**
         * This method sets LAYOUT for AlertDialogFragmentBuilder, which
         * responds to layout of dialog.
         *
         * @param resId resource id of layout
         * @return AlertDialogFragmentBuilder
         */
        public AlertDialogFragmentBuilder setLayout(int resId) {
            mBundle.putInt(LAYOUT, resId);
            return this;
        }

        /**
         * This method sets CANCELABLE for AlertDialogFragmentBuilder (default
         * value is true), which responds to weather dialog can be canceled.
         *
         * @param cancelable true for can be canceled, and false for can not be
         *                   canceled // MODIFIED by haifeng.tang, 2016-04-21,BUG-1940832
         * @return AlertDialogFragmentBuilder
         */
        public AlertDialogFragmentBuilder setCancelable(boolean cancelable) {
            mBundle.putBoolean(CANCELABLE, cancelable);
            return this;
        }

        /**
         * This method sets ICON for AlertDialogFragmentBuilder.
         *
         * @param resId resource id of icon
         * @return AlertDialogFragmentBuilder
         */
        public AlertDialogFragmentBuilder setIcon(int resId) {
            mBundle.putInt(ICON, resId);
            return this;
        }

        /**
         * This method sets MESSAGE for AlertDialogFragmentBuilder, which is a
         * string.
         *
         * @param resId resource id of message
         * @return AlertDialogFragmentBuilder
         */
        public AlertDialogFragmentBuilder setMessage(int resId) {
            mBundle.putInt(MESSAGE, resId);
            return this;
        }

        /**
         * This method sets NEGATIVE_TITLE for AlertDialogFragmentBuilder, which
         * responds to title of negative button.
         *
         * @param resId resource id of title
         * @return AlertDialogFragmentBuilder
         */
        public AlertDialogFragmentBuilder setCancelTitle(int resId) {
            mBundle.putInt(NEGATIVE_TITLE, resId);
            return this;
        }

        /**
         * This method sets POSITIVE_TITLE for AlertDialogFragmentBuilder, which
         * responds to title of positive button.
         *
         * @param resId resource id of title
         * @return AlertDialogFragmentBuilder
         */
        public AlertDialogFragmentBuilder setDoneTitle(int resId) {
            mBundle.putInt(POSITIVE_TITLE, resId);
            return this;
        }
    }

    /**
     * This method sets doneListenser for AlertDialogFragment
     *
     * @param listener doneListenser for AlertDialogFragment, which will
     *                 response to press done button // MODIFIED by haifeng.tang, 2016-04-21,BUG-1940832
     */
    public void setOnDoneListener(OnClickListener listener) {
        mDoneListener = listener;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mDoneListener != null) {
            mDoneListener.onClick(dialog, which);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = createAlertDialogBuilder(savedInstanceState);
        return builder.create();
    }

    /**
     * This method gets a instance of AlertDialog.Builder
     *
     * @param savedInstanceState information for AlertDialog.Builder
     * @return
     */
    protected Builder createAlertDialogBuilder(Bundle savedInstanceState) {
        Bundle args = null;
        if (savedInstanceState == null) {
            args = getArguments();
        } else {
            args = savedInstanceState;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (args != null) {
            int title = args.getInt(TITLE, INVIND_RES_ID);
            if (title != INVIND_RES_ID) {
                builder.setTitle(title);
            }

            int icon = args.getInt(ICON, INVIND_RES_ID);
            if (icon != INVIND_RES_ID) {
                builder.setIcon(icon);
            }

            int message = args.getInt(MESSAGE, INVIND_RES_ID);
            int layout = args.getInt(LAYOUT, INVIND_RES_ID);
            if (layout != INVIND_RES_ID) {
                View view = getActivity().getLayoutInflater().inflate(layout,
                        null);
                builder.setView(view);
            } else if (message != INVIND_RES_ID) {
                builder.setMessage(message);
            }

            int cancel = args.getInt(NEGATIVE_TITLE, INVIND_RES_ID);

            if (cancel != INVIND_RES_ID) {
                builder.setNegativeButton(cancel, mOnCancelListener); // MODIFIED by zibin.wang, 2016-05-06,BUG-2019352
            }

            int done = args.getInt(POSITIVE_TITLE, INVIND_RES_ID);
            if (done != INVIND_RES_ID) {
                builder.setPositiveButton(done, this);
            }

            mToastHelper = new ToastHelper(getActivity());
            boolean cancelable = args.getBoolean(CANCELABLE, true);
            builder.setCancelable(cancelable);
        }
        return builder;
    }

    /**
     * This method sets dismissListener for AlertDialogFragment, which will
     * response to dismissDialog
     *
     * @param listener OnDismissListener for AlertDialogFragment
     */
    public void setDismissListener(OnDismissListener listener) {
        mDismissListener = listener;
    }

    /* MODIFIED-BEGIN by zibin.wang, 2016-05-06,BUG-2019352*/
    public void setOnCancelListener(OnClickListener listener) {
        mOnCancelListener = listener;
    }
    /* MODIFIED-END by zibin.wang,BUG-2019352*/

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (mDismissListener != null) {
            mDismissListener.onDismiss(dialog);
        }
        super.onDismiss(dialog);
    }

    public static class EditDialogFragmentBuilder extends
            AlertDialogFragmentBuilder {
        @Override
        public EditTextDialogFragment create() {
            EditTextDialogFragment f = new EditTextDialogFragment();
            f.setArguments(mBundle);
            return f;
        }

        /**
         * This method sets default string and default selection for
         * EditTextDialogFragment.
         * // MODIFIED-BEGIN by zibin.wang, 2016-05-06, BUG-2019352
         * <p/>
         * // MODIFIED-BEGIN by haifeng.tang, 2016-04-21, BUG-1940832
         * // MODIFIED-END by zibin.wang, BUG-2019352
         *
         * @param defaultString    default string to show on EditTextDialogFragment
         * @param defaultSelection resource id for default selection
         * @return EditDialogFragmentBuilder
         */
        public EditDialogFragmentBuilder setDefault(String defaultString,
                                                    int defaultSelection, boolean hint) {
                                                    /* MODIFIED-END by haifeng.tang,BUG-1940832*/
            mBundle.putString(EditTextDialogFragment.DEFAULT_STRING,
                    defaultString);
            mBundle.putInt(EditTextDialogFragment.DEFAULT_SELCTION,
                    defaultSelection);
            mBundle.putBoolean(EditTextDialogFragment.DEFAULT_HINT, hint);
            return this;
        }
    }

    public static class EditTextDialogFragment extends AlertDialogFragment {
        public static final String TAG = "EditTextDialogFragment";
        public static final String DEFAULT_STRING = "defaultString";
        public static final String DEFAULT_SELCTION = "defaultSelection";
        public static final String DEFAULT_HINT = "defaultHint";
        private EditText mEditText;
        private TextView mRenameLimit;
        private EditTextDoneListener mEditTextDoneListener;
        boolean mHasToasted = false;
        private String mFragmentTag = null;
        private int mFileNameMaxLength = FileInfo.FILENAME_MAX_LENGTH;
        /* MODIFIED-BEGIN by songlin.qi, 2016-06-05,BUG-2241761*/
        private boolean invalidChecking = true;
        private boolean tooLongInputPrompting = true;
        private FileInfo mFileInfo;
        /* MODIFIED-END by songlin.qi,BUG-2241761*/

        public interface EditTextDoneListener {
            /**
             * This method is used to overwrite by its implement
             *
             * @param text text on EditText when done button is pressed
             */
            void onClick(String text);
        }

        public void setFileNameMaxLength(int length) {
            mFileNameMaxLength = length;
        }

        /* MODIFIED-BEGIN by songlin.qi, 2016-06-05,BUG-2241761*/
        public void setInvalidChecking(boolean value) {
            invalidChecking = value;
        }

        public void setTooLongInputPrompting(boolean value) {
            tooLongInputPrompting = value;
        }

        public void setEditFile(FileInfo fileInfo) {
            mFileInfo = fileInfo;
        }

        /* MODIFIED-END by songlin.qi,BUG-2241761*/
        @Override
        public void onSaveInstanceState(Bundle outState) {
            getArguments().putString(DEFAULT_STRING,
                    mEditText.getText().toString());
            getArguments().putInt(DEFAULT_SELCTION,
                    mEditText.getSelectionStart());
            super.onSaveInstanceState(outState);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            this.setOnDoneListener(this);
            AlertDialog.Builder builder = createAlertDialogBuilder(savedInstanceState);
            builder.setCancelable(false); // MODIFIED by zibin.wang, 2016-05-06,BUG-2019352
            Bundle args = null;
            if (savedInstanceState == null) {
                args = getArguments();
            } else {
                args = savedInstanceState;
            }
            if (args != null) {
                String defaultString = args.getString(DEFAULT_STRING, "");
                int selection = args.getInt(DEFAULT_SELCTION, 0);
                /* MODIFIED-BEGIN by haifeng.tang, 2016-04-21,BUG-1940832*/
                boolean hint = args.getBoolean(DEFAULT_HINT, false);
                View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_edit_text, null);
                builder.setView(view);
                mEditText = (EditText) view.findViewById(R.id.edit_text);
                mRenameLimit = (TextView) view.findViewById(R.id.rename_limit);
                if (hint) {
                    mEditText.setHint(defaultString);
                    mRenameLimit.setVisibility(View.GONE); // MODIFIED by songlin.qi, 2016-06-06,BUG-2264011
                } else {
                    mEditText.setText(defaultString);
                    if (selection >= defaultString.length()) {
                        selection = defaultString.length();
                    }
                    if (selection < 0) {
                        selection = 0;
                    }
                    if (selection >= getResources().getInteger(R.integer.name_max_length)) {
                        selection = getResources().getInteger(R.integer.name_max_length);
                    }
                    mEditText.setSelection(selection);
                    String str = mEditText.getText().toString();
                    int length = str.length();
                    //Rename or create file selected character rule of edit
                    if (mFileInfo != null && !mFileInfo.isDirectory()
                            && length > 0 && str.matches(".*[.].*") && !str.substring(length - 1, length).equals(".")) {
                        int i = str.lastIndexOf(".");
                        if (i > 0) {
                            mEditText.setSelection(0, i);
                        } else {
                            mEditText.selectAll();
                        }
                    } else {
                        mEditText.selectAll();
                    }
                    mFragmentTag = this.getTag();
                    /* MODIFIED-BEGIN by songlin.qi, 2016-06-06,BUG-2264011*/
                    if (defaultString != null && mFileNameMaxLength == 20 && defaultString.length() >= 20) {
                        mRenameLimit.setVisibility(View.VISIBLE);
                    } else {
                        mRenameLimit.setVisibility(View.GONE);
                        /* MODIFIED-END by songlin.qi,BUG-2264011*/
                    }
                }
            }
            return builder.create(); // MODIFIED by zibin.wang, 2016-05-06,BUG-2019352
            /* MODIFIED-END by haifeng.tang,BUG-1940832*/
        }

        @Override
        public void onResume() {
            super.onResume();
            /* MODIFIED-BEGIN by songlin.qi, 2016-06-14,BUG-2269190*/
            AlertDialog dialog = ((AlertDialog) getDialog());
            if (dialog != null) {
                if (mEditText != null) {
                    final Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (button != null) {
                            button.setEnabled(true);
                    }
                }
                dialog.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                setTextChangedCallback(mEditText, dialog);
                CommonUtils.setDialogTitleInCenter(dialog);
            }
        }

        /**
         * The method is used to set filter to EditText which is used for user
         * entering filename. This filter will ensure that the inputed filename
         * wouldn't be too long. If so, the inputed info would be rejected.
         *
         * @param edit      The EditText for filter to be registered. // MODIFIED by haifeng.tang, 2016-04-21,BUG-1940832
         * @param maxLength limitation of length for input text
         */
        private void setEditTextFilter(final EditText edit, final int maxLength) {
            InputFilter filter = new InputFilter.LengthFilter(mFileNameMaxLength) {
                private static final int VIBRATOR_TIME = 100;

                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                    /* MODIFIED-BEGIN by haifeng.tang, 2016-04-21,BUG-1940832*/
                    boolean isTooLong = false;
                    CharSequence result = null;
                    try {
                        /* MODIFIED-BEGIN by songlin.qi, 2016-06-08,BUG-2274533*/
                        int length;
                        if (mFragmentTag != null && mFragmentTag.equals(SafeBoxSettingsActivity.SAFE_RENAME_DIALOG_TAG)) {
                            // if for changing safe box name, count the String length
                            length = source.toString().length() + dest.toString().length();
                        } else {
                            length = source.toString().getBytes("UTF-8").length + dest.toString().getBytes("UTF-8").length;
                        }
                        /* MODIFIED-END by songlin.qi,BUG-2274533*/
                        if (length <= mFileNameMaxLength) {
                            int keep = mFileNameMaxLength - (dest.length() - (dend - dstart));
                            if (keep <= 0) {
                                isTooLong = true;
                                result = "";
                            } else if (keep >= end - start) {
                                // return null; // keep original
                            } else {
                                isTooLong = true;
                                boolean needsub = true;
                                keep += start;
                                if (Character.isHighSurrogate(source.charAt(keep - 1))) {
                                    --keep;
                                    if (keep == start) {
                                        result = "";
                                        needsub = false;
                                    }
                                }
                                if (needsub) {
                                    result = source.subSequence(start, keep);
                                    boolean hasComposing = false;
                                    Object composingObj = null;
                                    if (source instanceof Spanned) {
                                        Spanned text = (Spanned) source;
                                        Object[] sps = text.getSpans(0, text.length(),
                                                Object.class);
                                        if (sps != null) {
                                            for (int i = sps.length - 1; i >= 0; i--) {
                                                Object o = sps[i];
                                                if ((text.getSpanFlags(o) & Spanned.SPAN_COMPOSING) != 0) {
                                                    hasComposing = true;
                                                    composingObj = o;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    if (hasComposing) {
                                        SpannableString ss = new SpannableString(result);
                                        ss.setSpan(composingObj, 0, ss.length(),
                                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                                        | Spanned.SPAN_COMPOSING);
                                        result = ss;
                                    }
                                }
                            }
                        } else {
                            // ADD START FOR PR1052226 BY HONGBIN.CHEN 20150724
                            result = "";
                            isTooLong = true;
                            // ADD END FOR PR1052226 BY HONGBIN.CHEN 20150724
                        }
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                    }

                    if (isTooLong) {
                        Vibrator vibrator = (Vibrator) getActivity()
                                .getSystemService(Context.VIBRATOR_SERVICE);
                        boolean hasVibrator = vibrator.hasVibrator();
                        if (hasVibrator) {
                            vibrator.vibrate(new long[]{VIBRATOR_TIME,
                                    VIBRATOR_TIME}, INVIND_RES_ID);
                        }
                        /* MODIFIED-BEGIN by songlin.qi, 2016-06-05,BUG-2241761*/
                        if (tooLongInputPrompting) {
                            // PR930526 modify show toast to user when the input
                            // file name is too long by fengke at 2015.02.13 start
                            try {
                                mToastHelper.showToast(R.string.file_name_too_long);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            /* MODIFIED-END by songlin.qi,BUG-2241761*/
                        }
                    }

//                    if (source != null && source.length() > 0 && !mHasToasted
//                            && dstart == 0) {
//                        if (source.charAt(0) == '.') {
//                            mToastHelper.showToast(R.string.create_hidden_file);
//                            mHasToasted = true;
//                        }
//                    }
                    return result;

                }
            };
            edit.setFilters(new InputFilter[]{filter});
            /* MODIFIED-END by haifeng.tang,BUG-1940832*/
        }


        /**
         * This method register callback and set filter to Edit, in order to
         * make sure that user input is legal. The input can't be illegal
         * filename and can't be too long.
         *
         * @param editText EditText, which user type on
         *                 // MODIFIED-BEGIN by haifeng.tang, 2016-04-21, BUG-1940832 // MODIFIED by zibin.wang, 2016-05-06,BUG-2019352
         * @param dialog   dialog, which EditText associated with
         */
        protected void setTextChangedCallback(final EditText editText, final AlertDialog dialog) {
            setEditTextFilter(editText, FileInfo.FILENAME_MAX_LENGTH);//add for PR919043 by yane.wang@jrdcom.com 20150202
            /* MODIFIED-END by haifeng.tang,BUG-1940832*/
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              /* MODIFIED-BEGIN by haifeng.tang, 2016-04-21,BUG-1940832*/
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    if (mFragmentTag != null && mFragmentTag.equals(SafeBoxSettingsActivity.SAFE_RENAME_DIALOG_TAG)) {
                        int length = editText.getText().length();
                        if (mFileNameMaxLength == 20 && length >= 20) { // MODIFIED by songlin.qi, 2016-06-08,BUG-2274533
                            mRenameLimit.setVisibility(View.VISIBLE);
                        } else {
                            /* MODIFIED-BEGIN by songlin.qi, 2016-06-05,BUG-2241761*/
                            mRenameLimit.setVisibility(View.GONE);
                        }
                    }
                    /* MODIFIED-BEGIN by songlin.qi, 2016-06-14,BUG-2269190*/
                    Button button = ((AlertDialog) getDialog())
                            .getButton(DialogInterface.BUTTON_POSITIVE);
                    int txt_pos = 0;
                    String input = s.toString().trim().replace("\n", "");
                    Log.e(TAG,"AlertDialogFragment OnTextChanged input :" + input + " cursor pos " + start + " count " + count + " before " + before);

                    if (!invalidChecking) {
                        if (button != null) {
                            button.setEnabled(true);
                        }
                        /* MODIFIED-END by songlin.qi,BUG-2269190*/
                        return;
                    }
                    /* MODIFIED-END by songlin.qi,BUG-2241761*/


                    // int textLength = 0;
                    //try {
                    //textLength = s.toString().getBytes("UTF-8").length;
//                        if (textLength >= FileInfo.FILENAME_MAX_LENGTH) {
//                            mToastHelper.showToast(R.string.file_name_too_long);
//                        }
                    //} catch (UnsupportedEncodingException e) {
                    // e.printStackTrace();
                    // }
                    /* MODIFIED-END by haifeng.tang,BUG-1940832*/
                    //add for PR848342 by yane.wang@jrdcom.com 20141124 end
                    if (TextUtils.isEmpty(input) || input.matches(".*[/\\\\:*?\"<>|].*") || input.charAt(0) == '.') {
                        String inputText = "";
                        try {
                            inputText = s.toString();
                        } catch (IndexOutOfBoundsException ex) {
                            ex.printStackTrace();
                        }
                        if (input.length() > 0 && input.matches(".*[/\\\\:*?\"<>|].*")) {
                            mToastHelper.showToast(R.string.invalid_char_prompt);
                            inputText = inputText.replace("/", "");
                            inputText = inputText.replace("\\", "");
                            inputText = inputText.replace(":", "");
                            inputText = inputText.replace("*", "");
                            inputText = inputText.replace("?", "");
                            inputText = inputText.replace("\"", "");
                            inputText = inputText.replace("<", "");
                            inputText = inputText.replace(">", "");
                            inputText = inputText.replace("|", "");
                            editText.setText(inputText);
                            Log.e(TAG,"OnTextChanged0 inputText " + inputText + " length " + inputText.length());
                            txt_pos = start + count - 1;
                            if (txt_pos < 0) txt_pos = 0;
                            mEditText.setSelection(txt_pos);
                        }
                        if (input.length() > 0 && input.charAt(0) == '.') {
                            mToastHelper.showToast(R.string.invalid_char_prompt);
                            inputText = inputText.replace(".", "");
                            editText.setText(inputText);
                            Log.e(TAG,"OnTextChanged1 inputText " + inputText + " length " + inputText.length());
                            txt_pos = start + count - 1;
                            if (txt_pos < 0) txt_pos = 0;
                            mEditText.setSelection(txt_pos);
                        }
//                        /* MODIFIED-BEGIN by songlin.qi, 2016-06-14,BUG-2269190*/
//                        if (button != null) {
//                            button.setEnabled(false);
//                            button.setAlpha(0.5f);
//                        }
                    } else {
                        if (input.trim().equalsIgnoreCase("..") || input.trim().equalsIgnoreCase(".")) {
                            if (button != null) {
                                button.setEnabled(false);
                                button.setAlpha(0.5f);
                                /* MODIFIED-END by songlin.qi,BUG-2269190*/
                            }
                        }
                        //add for PR894096 by yane.wang@jrdcom.com 20150107 begin
//						else if (s.toString().equals("")
//								|| s.toString().trim().length() == 0) {
//                            Button botton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
//                            if (botton != null) {
//                                botton.setEnabled(false);
//                            }
//                        }
//                      //add for PR894096 by yane.wang@jrdcom.com 20150107 end
//                      //add for PR900537 by yane.wang@jrdcom.com 20150113 begin
//                        else if (textLength > FileInfo.FILENAME_MAX_LENGTH) {
//                            Button botton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
//                            if (botton != null) {
//                                botton.setEnabled(false);
//                            }
//                        }
//                      //add for PR900537 by yane.wang@jrdcom.com 20150113 end
                        else {
                            /* MODIFIED-BEGIN by songlin.qi, 2016-06-14,BUG-2269190*/
                            if (button != null) {
                                button.setEnabled(true);
                                button.setAlpha(1f);
                                /* MODIFIED-END by songlin.qi,BUG-2269190*/
                            }
                        }
                    }
                }
            });
        }

        /**
         * This method gets EditText's content on EditTextDialogFragment
         *
         * @return content of EditText
         */
        public String getText() {
            if (mEditText != null) {
                return mEditText.getText().toString().trim();
            }
            return null;
        }

        /**
         * This method sets EditTextDoneListener for EditTextDialogFragment
         *
         * @param listener EditTextDoneListener, which will response press done
         *                 button // MODIFIED by haifeng.tang, 2016-04-21,BUG-1940832
         */
        public void setOnEditTextDoneListener(EditTextDoneListener listener) {
            mEditTextDoneListener = listener;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (mEditTextDoneListener != null) {
                mEditTextDoneListener.onClick(getText());
            }
        }
    }


    //    public static class ChoiceDialogFragmentBuilder extends // MODIFIED by haifeng.tang, 2016-04-21,BUG-1940832
//            AlertDialogFragmentBuilder {
//        @Override
//        public ChoiceDialogFragment create() {
//            ChoiceDialogFragment f = new ChoiceDialogFragment();
//            f.setArguments(mBundle);
//            return f;
//        }
//
//        /**
//         * This method sets default choice and array for ChoiceDialogFragment.
//         *
//         * @param arrayId resource id for array
//         * @param defaultChoice resource id for default choice
//         * @return ChoiceDialogFragmentBuilder
//         */
//        public ChoiceDialogFragmentBuilder setDefault(int arrayId,
//                int defaultChoice) {
//            mBundle.putInt(ChoiceDialogFragment.DEFAULT_CHOICE, defaultChoice);
//            mBundle.putInt(ChoiceDialogFragment.ARRAY_ID, arrayId);
//            return this;
//        }
//    }
//
//    public static class ChoiceDialogFragment extends AlertDialogFragment {
//        public static final String TAG = "ChoiceDialogFragment";
//        public static final String DEFAULT_CHOICE = "defaultChoice";
//        public static final String ARRAY_ID = "arrayId";
//        public static final String ITEM_LISTENER = "itemlistener";
//        private int mArrayId;
//        private int mDefaultChoice;
//        private OnClickListener mItemLinster = null;
//
//        /**
//         * This method sets clickListener for ChoiceDialogFragment
//         *
//         * @param listener onClickListener, which will response press cancel
//         *            button
//         */
//        public void setItemClickListener(OnClickListener listener) {
//            mItemLinster = listener;
//        }
//
//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            AlertDialog.Builder builder = createAlertDialogBuilder(savedInstanceState);
//
//            Bundle args = null;
//            if (savedInstanceState == null) {
//                args = getArguments();
//            } else {
//                args = savedInstanceState;
//            }
//            if (args != null) {
//                mDefaultChoice = args.getInt(DEFAULT_CHOICE);
//                mArrayId = args.getInt(ARRAY_ID);
//            }
//            builder.setSingleChoiceItems(mArrayId, mDefaultChoice, this);
//            return builder.create();
//        }
//
//        @Override
//        public void onClick(DialogInterface dialog, int which) {
//            if (mItemLinster != null) {
//                mItemLinster.onClick(dialog, which);
//            }
//        }
//    }
 /* MODIFIED-BEGIN by songlin.qi, 2016-06-05,BUG-2241761*/
    // MODIFIED by zibin.wang, 2016-05-06,BUG-2019352
}
/* MODIFIED-END by songlin.qi,BUG-2241761*/
