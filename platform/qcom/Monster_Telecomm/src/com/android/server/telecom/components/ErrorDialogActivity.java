/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.server.telecom.components;

import com.android.server.telecom.Log;
import com.android.server.telecom.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
//[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/27/2016, SOLUTION- 2498534 And TASk-2707501
import android.telecom.TelecomManager;
import android.telephony.SubscriptionManager;
import com.android.server.telecom.AccountSelectDialogActivity;
import com.android.server.telecom.Log;
import com.android.server.telecom.R;
import android.telecom.PhoneAccountHandle;
import java.util.List;
import java.util.ArrayList;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionInfo;
import static com.android.internal.telephony.PhoneConstants.SUBSCRIPTION_KEY;
import android.util.TctLog;
//[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen).
// TODO: Needed for move to system service: import com.android.internal.R;

/**
 * Used to display an error dialog from within the Telecom service when an outgoing call fails
 */
public class ErrorDialogActivity extends Activity {
    private static final String TAG = ErrorDialogActivity.class.getSimpleName();

    public static final String SHOW_MISSING_VOICEMAIL_NO_DIALOG_EXTRA = "show_missing_voicemail";
    public static final String ERROR_MESSAGE_ID_EXTRA = "error_message_id";
    public static final String ERROR_MESSAGE_STRING_EXTRA = "error_message_string";

    /**
     * Intent action to bring up Voicemail Provider settings.
     */
    public static final String ACTION_ADD_VOICEMAIL =
            "com.android.phone.CallFeaturesSetting.ADD_VOICEMAIL";
    //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/27/2016, SOLUTION- 2498534 And TASk-2707501
    //public static final String SUB_ID_EXTRA = "com.android.phone.settings.SubscriptionInfoHelper.SubscriptionId";
    public static final String SUB_ID_EXTRA = SUBSCRIPTION_KEY;
    private String mSubscriptionLabel;
    public static final String SUB_LABEL_EXTRA ="com.android.phone.settings.SubscriptionInfoHelper.SubscriptionLabel";
    private int mSubId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
    private static final String RESTART_ACTIVITY_EXTRA ="DialerActivity";
    private static final boolean  SHOW_RESTART_ACTIVITY = true;
    private static final String START_FROM_INCALLACTIVITY = "start_from_incallactivity";
    private static boolean startFromIncallactivity = false;
    //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final boolean showVoicemailDialog = getIntent().getBooleanExtra(
                SHOW_MISSING_VOICEMAIL_NO_DIALOG_EXTRA, false);
        //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/27/2016, SOLUTION- 2498534 And TASk-2707501
        mSubId = getIntent().getIntExtra(SUB_ID_EXTRA, SubscriptionManager.INVALID_SUBSCRIPTION_ID);
        startFromIncallactivity = getIntent().getBooleanExtra(START_FROM_INCALLACTIVITY,false);
        mSubscriptionLabel = getIntent().getStringExtra(SUB_LABEL_EXTRA);
        //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
        if (showVoicemailDialog) {
            showMissingVoicemailErrorDialog();
        }  else if (getIntent().getCharSequenceExtra(ERROR_MESSAGE_STRING_EXTRA) != null) {
            final CharSequence error = getIntent().getCharSequenceExtra(
                    ERROR_MESSAGE_STRING_EXTRA);
            showGenericErrorDialog(error);
        } else {
            final int error = getIntent().getIntExtra(ERROR_MESSAGE_ID_EXTRA, -1);
            if (error == -1) {
                Log.w(TAG, "ErrorDialogActivity called with no error type extra.");
                finish();
            } else {
                showGenericErrorDialog(error);
            }
        }
    }

    private void showGenericErrorDialog(CharSequence msg) {
        final DialogInterface.OnClickListener clickListener;
        final DialogInterface.OnCancelListener cancelListener;

        clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };

        cancelListener = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        };

        final AlertDialog errorDialog = new AlertDialog.Builder(this)
                .setMessage(msg).setPositiveButton(android.R.string.ok, clickListener)
                        .setOnCancelListener(cancelListener).create();

        errorDialog.show();
    }

    private void showGenericErrorDialog(int resid) {
        final CharSequence msg = getResources().getText(resid);
        showGenericErrorDialog(msg);
    }

    private void showMissingVoicemailErrorDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.no_vm_number)
                .setMessage(R.string.no_vm_number_msg)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }})
                .setNegativeButton(R.string.add_vm_number_str,
                        new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    addVoiceMailNumberPanel(dialog);
                                }})
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }}).show();
    }


    private void addVoiceMailNumberPanel(DialogInterface dialog) {
        if (dialog != null) {
            dialog.dismiss();
        }

        // Navigate to the Voicemail setting in the Call Settings activity.
        //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/27/2016, SOLUTION- 2498534 And TASk-2707501
        TelecomManager telecomManager = TelecomManager.from(this);
        PhoneAccountHandle phoneAccount = telecomManager.getUserSelectedOutgoingPhoneAccount();
        final SubscriptionManager subscriptionManager = SubscriptionManager.from(this);
        final List<SubscriptionInfo> subInfoList =
              subscriptionManager.getActiveSubscriptionInfoList();
        TctLog.d(TAG, "ErrorDialogActivity addVoiceMailNumberPanel phoneAccount: "+phoneAccount);
        TctLog.d(TAG, "ErrorDialogActivity addVoiceMailNumberPanel subInfoList: "+subInfoList);
        if (phoneAccount == null || subInfoList ==null) {
            if(subInfoList!=null && (subInfoList.size()==1 || startFromIncallactivity)){
                Intent intent = null;
                if(getResources().getBoolean(R.bool.def_directly_to_voicenum_setup_on)){
                    TctLog.d(TAG, "1WXHdef_directly_to_voicenum_setup_on = true");
                    intent = new Intent(ACTION_ADD_VOICEMAIL);
                    intent.putExtra("longPressed",true);
                } else {
                    TctLog.d(TAG, "2WXHdef_directly_to_voicenum_setup_on = false");
                    intent = new Intent(TelecomManager.ACTION_SHOW_CALL_SETTINGS);
                }
                int subId = subInfoList.get(0).getSubscriptionId();
                if (startFromIncallactivity) {
                    subId = mSubId;
                }
                SubscriptionInfo mSubscriptionInfo = subscriptionManager.getActiveSubscriptionInfo(subId);
                if (mSubscriptionInfo != null) {
                    intent.putExtra(SUB_LABEL_EXTRA, mSubscriptionInfo.getDisplayName().toString());
                }
                intent.putExtra(SUBSCRIPTION_KEY, subId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(RESTART_ACTIVITY_EXTRA, SHOW_RESTART_ACTIVITY);
                startActivity(intent);
            }else{
                Intent intent = new Intent(this, AccountSelectDialogActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } else {
            TctLog.d(TAG, "ErrorDialogActivity addVoiceMailNumberPanel else ");
            Intent intent = null;
            if(getResources().getBoolean(R.bool.def_directly_to_voicenum_setup_on)){
                TctLog.d(TAG, "2WXHdef_directly_to_voicenum_setup_on = true");
                intent = new Intent(ACTION_ADD_VOICEMAIL);
                intent.putExtra("longPressed",true);
            } else {
                TctLog.d(TAG, "2WXHdef_directly_to_voicenum_setup_on = false");
                intent = new Intent(TelecomManager.ACTION_SHOW_CALL_SETTINGS);
            }
            int subId = mSubId;
            try {
                subId = Integer.parseInt(phoneAccount.getId());
            } catch (NumberFormatException e){
                return;
            }
            final SubscriptionInfo mSubscriptionInfo = subscriptionManager.getActiveSubscriptionInfo(subId);
            if (mSubscriptionInfo != null) {
                intent.putExtra(SUB_LABEL_EXTRA, mSubscriptionInfo.getDisplayName().toString());
            }
            intent.putExtra(SUBSCRIPTION_KEY, subId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(RESTART_ACTIVITY_EXTRA, SHOW_RESTART_ACTIVITY);
            startActivity(intent);
        }
        //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        // Don't show the return to previous task animation to avoid showing a black screen.
        // Just dismiss the dialog and undim the previous activity immediately.
        overridePendingTransition(0, 0);
    }
}
