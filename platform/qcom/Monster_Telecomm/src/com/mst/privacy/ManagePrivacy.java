/*
 * Copyright (c) 2013 The Linux Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of The Linux Foundation, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mst.privacy;

import mst.app.dialog.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.android.server.telecom.CallerInfoAsyncQueryFactory;
import com.android.server.telecom.CallsManager;
import com.android.server.telecom.ContactsAsyncHelper;
import com.android.server.telecom.PhoneAccountRegistrar;
import com.android.server.telecom.TelecomSystem;
import com.monster.privacymanage.entity.AidlAccountData;

import android.database.ContentObserver;
import android.database.Cursor;
import android.os.*;
import android.net.Uri;
import android.provider.Telephony;

public class ManagePrivacy {
    private static final String LOG_TAG = "ManagePrivacy";

    // Key used to read and write the saved network selection numeric value
    private static final String BIND_SERVICE = "com.aurora.privacymanage.";
    private static final String ACTION_SWITCH = "com.monster.privacymanage.SWITCH_ACCOUNT";
    private static final String ACTION_DELETE = "com.monster.privacymanage.DELETE_ACCOUNT";

    /** The singleton ManagedRoaming instance. */
    private static ManagePrivacy sInstance;

    private Context mContext;


    public PrivacyMissedCallNotifierImpl notificationMgr;

    private long mCurrentPrivateId = 0;

    public static ManagePrivacy init(Context context) {
        synchronized (ManagePrivacy.class) {
            if (sInstance == null) {
                sInstance = new ManagePrivacy(context);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = "
                        + sInstance);
            }
            return sInstance;
        }
    }
    
    public static ManagePrivacy getInstance() {
        return sInstance;
    }
    
    public Context getContext() {
        return mContext;
    }

    private ManagePrivacy(Context context) {
        mContext = context;
        mCurrentPrivateId = PrivacyUtils.getCurrentAccountId();
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SWITCH);
        filter.addAction(ACTION_DELETE);
        mContext.registerReceiver(mManagePrivacyReceiver, filter);
        
        PrivacyUtils.bindService(mContext);
        

        
        
        mHandler = new Handler();
        mHandler.post(new Runnable() {
            public void run() {
                notificationMgr =  TelecomSystem.getInstance().getCallsManager().mPrivacyMissedCallNotifier;
            }
        });
        mHandler.postDelayed(new Runnable() {
            public void run() {
                notificationMgr.updateNotificationsWhenGoToPrivateMode();
            }
        }, 3000);
    }

    private BroadcastReceiver mManagePrivacyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getExtras() != null) {
                String action = intent.getAction();
                AidlAccountData account = intent.getParcelableExtra("account");
                Log.i(LOG_TAG,
                        "PrivacyAccountChangeReceiver  " + "onReceive action: "
                                + action + "  account id: "
                                + account.getAccountId() + "  path: "
                                + account.getHomePath());

                if (action != null
                        && action
                                .equals(ACTION_SWITCH)) {
                    PrivacyUtils.mCurrentAccountId = account.getAccountId();
                    PrivacyUtils.mCurrentAccountHomePath = account
                            .getHomePath();

                    if (PrivacyUtils.mCurrentAccountId > 0) {
                        PrivacyUtils.mIsPrivacyMode = true;
                    } else {
                        PrivacyUtils.mIsPrivacyMode = false;
                    }
                    OnchangeWhenSwitch();

                } else if (action != null
                        && action
                                .equals(ACTION_DELETE)) {
                    boolean delete = intent.getBooleanExtra("delete", false);
                    PrivacyUtils.mIsPrivacyMode = false;
//                    mHandler.postDelayed(new Runnable() {
//                        public void run() {
//                            PhoneGlobals.getInstance().notificationMgr
//                                    .cancelMissedCallNotification();
//                            PhoneGlobals.getInstance().notificationMgr
//                                    .updateNotificationsAtStartup();
//                        }
//                    }, 1000);
                }

//                PrivacyUtils.killPrivacyActivity();
            }

        }
    };

    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
   

    private void OnchangeWhenSwitch() {
        notificationMgr.cancelAllNotification();
    }

    private Handler mHandler;

 


}
