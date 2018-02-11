package cn.tcl.music.util;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

public class EditTextLimitTextWatcher implements TextWatcher {
    private final static String TAG = EditTextLimitTextWatcher.class.getSimpleName();

    private final int mMaxLength;
    private String mToastText;
    private Context mContext;
    private Toast mToast;
    private EditText mEditText;
    private int mCharCount;

    public EditTextLimitTextWatcher(Context context, EditText editText, int maxLength,
                                    String toastText) {
        this.mContext = context;
        this.mEditText = editText;
        this.mMaxLength = maxLength;
        this.mToastText = toastText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mCharCount = before + count;
        if (mCharCount > mMaxLength) {
            mEditText.setSelection(mEditText.length());
        }
        try {
            mCharCount = mEditText.getText().toString().getBytes("GBK").length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mCharCount > mMaxLength) {
            CharSequence subSequence = null;
            for (int i = 0; i < s.length(); i++) {
                subSequence = s.subSequence(0, i);
                try {
                    if (subSequence.toString().getBytes("GBK").length == mCharCount) {
                        mEditText.setText(subSequence.toString());
                        break;
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            if (!TextUtils.isEmpty(mToastText)) {
                ToastUtil.showToast(mContext,mToastText);
            }
            String androidVersion = android.os.Build.VERSION.RELEASE;
            if (androidVersion.charAt(0) >= '4') {
                mEditText.setText(subSequence.toString());
            }
        }
    }
}
