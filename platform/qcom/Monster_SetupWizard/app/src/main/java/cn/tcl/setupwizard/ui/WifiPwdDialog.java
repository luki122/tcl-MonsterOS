/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.setupwizard.ui;

/* MODIFIED-BEGIN by xinlei.sheng, 2016-10-13,BUG-2669930*/
import mst.app.dialog.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import cn.tcl.setupwizard.R;

public class WifiPwdDialog extends AlertDialog implements View.OnClickListener, TextWatcher {

    private Context mContext;
    private View mView;
    private String mSsid;
    private TextView mSsidText;
    private EditText mPasswordText;
    private ImageView mDisplayOrHidePassword;
    private boolean mIsDisplayPassword;
    private final DialogInterface.OnClickListener mListener;

    public WifiPwdDialog(Context context, String ssid, DialogInterface.OnClickListener listener) {
        super(context);
        mListener = listener;
        mContext = context;
        mSsid = ssid;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mIsDisplayPassword = false;
        mView = getLayoutInflater().inflate(R.layout.wifi_pwd_dialog, null);
        mSsidText = (TextView) mView.findViewById(R.id.ssid);
        mSsidText.setText(mSsid);
        mPasswordText = (EditText) mView.findViewById(R.id.password);
        mDisplayOrHidePassword = (ImageView) mView.findViewById(R.id.displayOrHide_password);

        if (savedInstanceState != null) {
            String ssid = savedInstanceState.getString("ssid");
            if (ssid != null) {
                mSsidText.setText(ssid);
            }
        }

        setView(mView);

        //setTitle("TP-Link-dd332");
        setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getString(R.string.cancel_title), mListener);
        setButton(DialogInterface.BUTTON_POSITIVE, getContext().getString(R.string.connect_title), mListener);

        mPasswordText.addTextChangedListener(this);
        mDisplayOrHidePassword.setOnClickListener(this);

        super.onCreate(savedInstanceState);

        validate();
    }


    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.displayOrHide_password) {
            if (!mIsDisplayPassword) {
                //Show the password
                mIsDisplayPassword = true;
                mDisplayOrHidePassword.setColorFilter(Color.BLACK);
                mPasswordText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                Selection.setSelection(mPasswordText.getText(), mPasswordText.getText().length());
            } else {
                //Hide the password
                mIsDisplayPassword = false;
                mDisplayOrHidePassword.setColorFilter(Color.GRAY);
                mPasswordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                Selection.setSelection(mPasswordText.getText(), mPasswordText.getText().length());
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        validate();
    }

    private void validate() {
        if (mPasswordText != null && mPasswordText.length() >= 8) {
            getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
        } else {
            getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        }
    }

    public void setHint(String hint) {
        mPasswordText.setHint(hint);
    }

    public void setSsid(String ssid) {
        if (mSsidText != null) {
            mSsidText.setText(ssid);
        }
    }

    public String getSsid() {
        if (mSsidText != null) {
            return mSsidText.getText().toString();
        } else {
            return null;
        }
    }

    public String getPassword() {
        if (mPasswordText != null && mPasswordText.length() > 0) {
            return mPasswordText.getText().toString();
        } else {
            return null;
        }
    }

    public void setPassword(String password) {
        if (mPasswordText != null) {
            mPasswordText.setText(password);
            /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
        }
    }
}
