/*
 * Copyright (C) 2007 The Android Open Source Project
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

package mst.app.dialog;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * <p>A dialog showing a progress indicator and an optional text message or view.
 * Only a text message or a view can be used at the same time.</p>
 * <p>The dialog can be made cancelable on back key press.</p>
 * <p>The progress range is 0..10000.</p>
 */
public class WrapDialog extends Dialog{
    private static String TAG = "WrapDialog";

    private View mTopPanel;
    private ImageView mTitleIcon;
    private TextView mTitleView;

    private View mBottomPanel;
    private Button mButton1;
    private Button mButton2;
    private Button mButton3;

    private int mPaddingLeft = 0;
    private int mPaddingTop = 0;
    private int mPaddingRight = 0;
    private int mPaddingBottom = 0;

    private View mPanel;

    private FrameLayout mCustomPanel;

    private Context mContext;

    private ArrayList<OnClickListener> mListeners;

    public WrapDialog(Context context) {
        super(context,resolveDialogTheme(context,0));
        init();
    }

    public WrapDialog(Context context, int theme) {
        super(context, resolveDialogTheme(context, theme));
        init();
    }

    static int resolveDialogTheme(Context context, int themeResId) {
        return themeResId == 0 ? com.mst.internal.R.style.Mst_Material_WrapDialog : themeResId;
    }

    private void init(){
//        mPaddingLeft = mContext.getResources().getDimensionPixelOffset(com.mst.internal.R.dimen.wrapdialog_padding_left);
//        mPaddingTop = mContext.getResources().getDimensionPixelOffset(com.mst.internal.R.dimen.wrapdialog_padding_top);
//        mPaddingRight = mContext.getResources().getDimensionPixelOffset(com.mst.internal.R.dimen.wrapdialog_padding_right);
//        mPaddingBottom = mContext.getResources().getDimensionPixelOffset(com.mst.internal.R.dimen.wrapdialog_padding_bottom);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        mPanel = inflater.inflate(com.mst.internal.R.layout.wrap_dialog_material,null);
        mTopPanel = mPanel.findViewById(com.android.internal.R.id.topPanel);
        mTopPanel.setVisibility(View.GONE);
        mTitleIcon = (ImageView) mPanel.findViewById(android.R.id.icon);
        mTitleIcon.setVisibility(View.GONE);
        mTitleView = (TextView) mPanel.findViewById(com.android.internal.R.id.alertTitle);

        mBottomPanel = mPanel.findViewById(com.android.internal.R.id.buttonPanel);
        mBottomPanel.setVisibility(View.GONE);
        mButton1 = (Button) mPanel.findViewById(android.R.id.button1);
        mButton2 = (Button) mPanel.findViewById(android.R.id.button2);
        mButton3 = (Button) mPanel.findViewById(android.R.id.button3);
        mButton1.setVisibility(View.GONE);
        mButton2.setVisibility(View.GONE);
        mButton3.setVisibility(View.GONE);

        mCustomPanel = (FrameLayout) mPanel.findViewById(android.R.id.content);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mPanel);
    }

    private ArrayList getListeners(){
        if(mListeners == null){
            mListeners = new ArrayList<>();
        }
        return mListeners;
    }

    private void addListener(OnClickListener listener){
        ArrayList array = getListeners();
        if(!array.contains(listener)){
            array.add(listener);
        }
    }

    public Button getButton(int whichButton) {
        return null;
    }

    public void setCustomView(int layout){
        View view = LayoutInflater.from(getContext()).inflate(layout, mCustomPanel,false);
        mCustomPanel.addView(view);
    }

    public void setCustomView(View view){
        mCustomPanel.addView(view);
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mListeners != null){
                int whichButton = 0;
                if(v == mButton1){
                    whichButton = BUTTON_POSITIVE;
                }else if(v == mButton2){
                    whichButton = BUTTON_NEGATIVE;
                }else if(v == mButton3){
                    whichButton = BUTTON_NEUTRAL;
                }
                for(OnClickListener listener : mListeners){
                    listener.onClick(WrapDialog.this,whichButton);
                }
            }
        }
    };

    public void setButton(int whichButton, CharSequence text, Message msg) {
        setButton(whichButton,text);
    }

    public void setButton(int whichButton, CharSequence text){
        Button button = null;
        switch (whichButton){
            case BUTTON_POSITIVE:
                button = mButton1;
                break;
            case BUTTON_NEGATIVE:
                button = mButton2;
                break;
            case BUTTON_NEUTRAL:
                button = mButton3;
                break;
        }
        button.setVisibility(View.VISIBLE);
        button.setText(text);
        button.setOnClickListener(mListener);
        mBottomPanel.setVisibility(View.VISIBLE);
    }

    public void setButton(int whichButton, CharSequence text, OnClickListener listener) {
        setButton(whichButton,text);
        addListener(listener);
    }

    public void setButton(CharSequence text, Message msg) {
        setButton(BUTTON_POSITIVE,text,msg);
    }

    public void setButton(CharSequence text, OnClickListener listener) {
        setButton(BUTTON_POSITIVE,text,listener);
    }

    public void setButton2(CharSequence text, Message msg) {
        setButton(BUTTON_NEGATIVE,text,msg);
    }

    public void setButton2(CharSequence text, OnClickListener listener) {
        setButton(BUTTON_NEGATIVE,text,listener);
    }

    public void setButton3(CharSequence text, Message msg) {
        setButton(BUTTON_NEUTRAL,text,msg);
    }

    public void setButton3(CharSequence text, OnClickListener listener) {
        setButton(BUTTON_NEUTRAL,text,listener);
    }

    public void setIcon(int resId) {
        mTitleIcon.setImageResource(resId);
        mTitleIcon.setVisibility(View.VISIBLE);
        mTopPanel.setVisibility(View.VISIBLE);
    }

    public void setIcon(Drawable icon) {
        mTitleIcon.setImageDrawable(icon);
        mTitleIcon.setVisibility(View.VISIBLE);
        mTopPanel.setVisibility(View.VISIBLE);
    }

    public void setTitle(CharSequence title) {
        mTitleView.setText(title);
        mTopPanel.setVisibility(View.VISIBLE);
    }
}
