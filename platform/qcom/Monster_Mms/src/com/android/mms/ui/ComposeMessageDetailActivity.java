/*
 * Copyright (c) 2014, The Linux Foundation. All rights reserved.
 * Not a Contribution.
 *
 * Copyright (C) 2012 The Android Open Source Project.
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

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
//import android.app.AlertDialog;
//import android.app.ProgressDialog;
import mst.app.dialog.AlertDialog;
import mst.app.dialog.ProgressDialog;
import mst.app.MstActivity;
import mst.widget.toolbar.Toolbar;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import mst.provider.Telephony.Mms;
import mst.provider.Telephony.Sms;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.method.HideReturnsTransformationMethod;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.view.GestureDetector;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.mms.data.Contact;
import com.android.mms.LogTag;
import com.android.mms.R;
import com.android.mms.transaction.MessageSender;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.transaction.SmsMessageSender;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.MessageUtils;
import com.google.android.mms.MmsException;

public class ComposeMessageDetailActivity extends MstActivity{

    private static final String TAG = "MessageDetailActivity";
    private String mMsgBodyText;
    private CheckOverSizeTextView mMsgBodyTextView;
    private GestureDetector mGestureDetector;
    private DoubleGestureListener mDoubleGestureListener = new DoubleGestureListener();
    private ScrollView mScrollView;
    private boolean mScrollViewFinish = true;
    private float mDownX;
    private float mDownY;
    private final static float DITANCE = 10;
    private int mTouchSlop;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.message_mst_detail_content);
        //mGestureDetector = new GestureDetector(mDoubleGestureListener);
        final ViewConfiguration configuration = ViewConfiguration.get(this);
        mTouchSlop = configuration.getScaledTouchSlop();
        handleIntent();
        initUi();
    }

    
    /*@Override
    public boolean onTouchEvent(MotionEvent event){   
        mGestureDetector.onTouchEvent(event);
        //return true;
        return super.onTouchEvent(event);  
    } */

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initUi() {
        //mScaleDetector = new ScaleGestureDetector(this, new MyScaleListener());
        mScrollView = (ScrollView)findViewById(R.id.message_mst_detail);
        /*mScrollView.setOnTouchListener(new View.OnTouchListener() {  
            public boolean onTouch(View v, MotionEvent event) {  
                // ... Respond to touch events         
                mGestureDetector.onTouchEvent(event);  
                return super.onTouchEvent(event);  
            }  
        });*/
        mScrollView.setOnTouchListener(new View.OnTouchListener() {
            private boolean mIsMove;
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                // TODO Auto-generated method stub
                //mGestureDetector.onTouchEvent(arg1);
                boolean isOverSize = mMsgBodyTextView != null ? mMsgBodyTextView.getIsEvenOverSize() : false;
                int action = arg1.getAction();
                if(action == MotionEvent.ACTION_DOWN) {
                    mDownX = arg1.getX();
                    mDownY = arg1.getY();
                }
                /*if(action == MotionEvent.ACTION_MOVE) {
                    mIsMove = true;
                }*/
                if(action == MotionEvent.ACTION_UP) {
                    float distanceX = Math.abs(arg1.getX() - mDownX);
                    float distanceY = Math.abs(arg1.getY() - mDownY);
                    if(!isOverSize || (isOverSize &&/* !mIsMove*/(distanceX < mTouchSlop && distanceY < mTouchSlop))) {
                        ComposeMessageDetailActivity.this.finish();
                        return true;
                    }
                    mIsMove = false;
                }
                return false;
            }
        });
        mMsgBodyTextView = (CheckOverSizeTextView)findViewById(R.id.message_mst_detail_body);
        mMsgBodyTextView.setOnOverLineChangedListener(new CheckOverSizeTextView.OnOverSizeChangedListener() {
            @Override
            public void onChanged(boolean isOverSize) {
                // TODO Auto-generated method stub
                if(isOverSize){
                    //mMsgBodyTextView.setLines(Integer.MAX_VALUE);
                    mMsgBodyTextView.displayAll();
                }
                //contentText.setIsCallChangedListener(false);
            }
        });
        /*mMsgBodyTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                // TODO Auto-generated method stub
                //mGestureDetector.onTouchEvent(arg1);
                int action = arg1.getAction();
                if(!mScrollViewFinish && action == MotionEvent.ACTION_UP) {
                    ComposeMessageDetailActivity.this.finish();
                    return true;
                }
                return false;
            }
        });*/
        if(!TextUtils.isEmpty(mMsgBodyText)) {
            mMsgBodyTextView.setText(mMsgBodyText);
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        mMsgBodyText = intent.getStringExtra("msgBody");

        // Cancel failed notification. if need
        MessageUtils.cancelFailedToDeliverNotification(intent, this);
        MessageUtils.cancelFailedDownloadNotification(intent, this);

        if (TextUtils.isEmpty(mMsgBodyText)) {
            Log.e(TAG, "There's no sms uri!");
            finish();
        }
    }

    private class DoubleGestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override  
        public boolean onDoubleTap(MotionEvent event) {
            ComposeMessageDetailActivity.this.finish();
            return true;  
        }

        @Override  
        public boolean onSingleTapUp(MotionEvent event) {
            ComposeMessageDetailActivity.this.finish();
            return true;  
        }
    }
}
