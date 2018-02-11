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

import com.android.server.telecom.CallIntentProcessor;
import com.android.server.telecom.Log;
import com.android.server.telecom.R;
import com.android.server.telecom.TelephonyUtil;
import com.android.server.telecom.UserUtil;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.UserHandle;
import android.os.UserManager;
import android.telecom.DefaultDialerManager;
import android.telecom.PhoneAccount;
import android.telecom.TelecomManager;
import android.telecom.VideoProfile;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.widget.Toast;
import android.util.TctLog;
//[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/27/2016, SOLUTION-2498534 And TASk-2707501
import android.telephony.TelephonyManager;
import android.telephony.SubscriptionManager;
import android.telecom.PhoneAccountHandle;
import static com.android.internal.telephony.PhoneConstants.SUBSCRIPTION_KEY;
//[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen).
// TODO: Needed for move to system service: import com.android.internal.R;

/**
 * Handles system CALL actions and forwards them to {@link CallIntentProcessor}.
 * Handles all three CALL action types: CALL, CALL_PRIVILEGED, and CALL_EMERGENCY.
 *
 * Pre-L, the only way apps were were allowed to make outgoing emergency calls was the
 * ACTION_CALL_PRIVILEGED action (which requires the system only CALL_PRIVILEGED permission).
 *
 * In L, any app that has the CALL_PRIVILEGED permission can continue to make outgoing emergency
 * calls via ACTION_CALL_PRIVILEGED.
 *
 * In addition, the default dialer (identified via
 * {@link android.telecom.TelecomManager#getDefaultDialerPackage()} will also be granted the
 * ability to make emergency outgoing calls using the CALL action. In order to do this, it must
 * use the {@link TelecomManager#placeCall(Uri, android.os.Bundle)} method to allow its package
 * name to be passed to {@link UserCallIntentProcessor}. Calling startActivity will continue to
 * work on all non-emergency numbers just like it did pre-L.
 */
public class UserCallIntentProcessor {

    private final Context mContext;
    private final UserHandle mUserHandle;
    //public static final String SUB_ID_EXTRA = "com.android.phone.settings.SubscriptionInfoHelper.SubscriptionId"; //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/27/2016, SOLUTION-2498534 And TASk-2707501
    public static final String SUB_ID_EXTRA = SUBSCRIPTION_KEY;
    public UserCallIntentProcessor(Context context, UserHandle userHandle) {
        mContext = context;
        mUserHandle = userHandle;
    }

    /**
     * Processes intents sent to the activity.
     *
     * @param intent The intent.
     */
    public void processIntent(Intent intent, String callingPackageName,
            boolean canCallNonEmergency) {
        // Ensure call intents are not processed on devices that are not capable of calling.
        if (!isVoiceCapable()) {
            return;
        }

        String action = intent.getAction();

        if (Intent.ACTION_CALL.equals(action) ||
                Intent.ACTION_CALL_PRIVILEGED.equals(action) ||
                Intent.ACTION_CALL_EMERGENCY.equals(action)) {
            processOutgoingCallIntent(intent, callingPackageName, canCallNonEmergency);
        }
    }

    private void processOutgoingCallIntent(Intent intent, String callingPackageName,
            boolean canCallNonEmergency) {
        Uri handle = intent.getData();
        String scheme = handle.getScheme();
        String uriString = handle.getSchemeSpecificPart();

        //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/15/2016, SOLUTION-2470079 And TASk-2720014
        TctLog.i("CallActivity","CallActivity.java,handle="+handle);
        TctLog.i("CallActivity","CallActivity.java,uriString="+uriString);
        boolean mboolean = false;
        if(mContext.getResources() != null){
            mboolean = mContext.getApplicationContext().getResources().getBoolean(R.bool.def_Phone_ECT_for_Germany_Telekom);
        }
        TctLog.i("CallActivity","CallActivity.java,mboolean="+mboolean);
        if(mboolean){
            if(uriString.equals("*70#")&&intent != null){
                Uri mUri=Uri.parse("tel:4");
                intent.setData(mUri);
              }
        }
        if(intent != null && intent.getData() != null){
            TctLog.i("CallActivity","CallActivity.java,intent.getData()="+intent.getData().toString());
            TctLog.i("CallActivity","CallActivity.java,intent.getData().getScheme()="+intent.getData().getScheme());
            TctLog.i("CallActivity","CallActivity.java,handle.intent.getData().getSchemeSpecificPart()="+intent.getData().getSchemeSpecificPart());
        }
        //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)

        if (!PhoneAccount.SCHEME_VOICEMAIL.equals(scheme)) {
            handle = Uri.fromParts(PhoneNumberUtils.isUriNumber(uriString) ?
                    PhoneAccount.SCHEME_SIP : PhoneAccount.SCHEME_TEL, uriString, null);
        }

        // Check DISALLOW_OUTGOING_CALLS restriction. Note: We are skipping this check in a managed
        // profile user because this check can always be bypassed by copying and pasting the phone
        // number into the personal dialer.
        if (!UserUtil.isManagedProfile(mContext, mUserHandle)) {
            // Only emergency calls are allowed for users with the DISALLOW_OUTGOING_CALLS
            // restriction.
            if (!TelephonyUtil.shouldProcessAsEmergency(mContext, handle)) {
                final UserManager userManager = (UserManager) mContext.getSystemService(
                        Context.USER_SERVICE);
                if (userManager.hasBaseUserRestriction(UserManager.DISALLOW_OUTGOING_CALLS,
                        mUserHandle)) {
                    showErrorDialogForRestrictedOutgoingCall(mContext,
                            R.string.outgoing_call_not_allowed_user_restriction);
                    Log.w(this, "Rejecting non-emergency phone call due to DISALLOW_OUTGOING_CALLS "
                            + "restriction");
                    return;
                } else if (userManager.hasUserRestriction(UserManager.DISALLOW_OUTGOING_CALLS,
                        mUserHandle)) {
                    RestrictedLockUtils.sendShowAdminSupportDetailsIntent(mContext,
                            EnforcedAdmin.MULTIPLE_ENFORCED_ADMIN);
                    return;
                }
            }
        }
        //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/27/2016, SOLUTION- 2498534 And TASk-2707501
        if (PhoneAccount.SCHEME_VOICEMAIL.equals(scheme)) {
            TelephonyManager telemanager = TelephonyManager.from(mContext);
            final int subId = intent.getIntExtra("SubId",SubscriptionManager.INVALID_SUBSCRIPTION_ID);
            final String number = telemanager.getVoiceMailNumber(subId);
            TctLog.d("CallActivity", "number = "+number+",SubId = "+subId);
            // Check for a voicemail-dialing request.  If the voicemail number is
            // empty, popup dialog to notify user to set voicemail.
            boolean hasVMNumber = false;
            TelecomManager telecomManager = TelecomManager.from(mContext);
            PhoneAccountHandle phoneAccount = telecomManager.getUserSelectedOutgoingPhoneAccount();
            if (phoneAccount == null) {
                hasVMNumber = hasVMNumber();
            }else if (!TextUtils.isEmpty(number)) {
                hasVMNumber = true;
            }
            TctLog.d("CallActivity", "phoneAccount = "+phoneAccount+" ,hasVMNumber = "+hasVMNumber);
            if (!hasVMNumber){
                final Intent errorIntent = new Intent(mContext, ErrorDialogActivity.class);
                errorIntent.putExtra(ErrorDialogActivity.SHOW_MISSING_VOICEMAIL_NO_DIALOG_EXTRA, true);
                errorIntent.putExtra(SUB_ID_EXTRA, subId);
                errorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                mContext.startActivity(errorIntent);
                return;
            }
        }
        //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen).


        if (!canCallNonEmergency && !TelephonyUtil.shouldProcessAsEmergency(mContext, handle)) {
            showErrorDialogForRestrictedOutgoingCall(mContext,
                    R.string.outgoing_call_not_allowed_no_permission);
            Log.w(this, "Rejecting non-emergency phone call because "
                    + android.Manifest.permission.CALL_PHONE + " permission is not granted.");
            return;
        }

        int videoState = intent.getIntExtra(
                TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE,
                VideoProfile.STATE_AUDIO_ONLY);
        Log.d(this, "processOutgoingCallIntent videoState = " + videoState);

        intent.putExtra(CallIntentProcessor.KEY_IS_PRIVILEGED_DIALER,
                isDefaultOrSystemDialer(callingPackageName));

        // Save the user handle of current user before forwarding the intent to primary user.
        intent.putExtra(CallIntentProcessor.KEY_INITIATING_USER, mUserHandle);

        sendBroadcastToReceiver(intent);
    }
    //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/27/2016, SOLUTION- 2498534 And TASk-2707501
    private boolean hasVMNumber() {
        TelephonyManager telemanager = TelephonyManager.from(mContext);
        boolean hasVMNum = false;
        int phoneCount = telemanager.getPhoneCount();
        for (int i = 0; i < phoneCount; i++) {
            try {
                int[] subId = SubscriptionManager.getSubId(i);
                String number = telemanager.getVoiceMailNumber(subId[0]);
                TctLog.d("CallActivity", "hasVMNumber  VMNumber = "+number+" ,subId = "+subId[0]);
                hasVMNum = !TextUtils.isEmpty(number);
            } catch (SecurityException se) {
                TctLog.e("CallActivity", "hasVMNumber: SecurityException, Maybe privilege isn't sufficient.");
             }
            if (hasVMNum) {
                break;
            }
        }
        return hasVMNum;
    }
    //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen).

    private boolean isDefaultOrSystemDialer(String callingPackageName) {
        if (TextUtils.isEmpty(callingPackageName)) {
            return false;
        }

        final String defaultDialer = DefaultDialerManager.getDefaultDialerApplication(mContext,
                mUserHandle.getIdentifier());
        if (TextUtils.equals(defaultDialer, callingPackageName)) {
            return true;
        }

        final TelecomManager telecomManager =
                (TelecomManager) mContext.getSystemService(Context.TELECOM_SERVICE);
        return TextUtils.equals(telecomManager.getSystemDialerPackage(), callingPackageName);
    }

    /**
     * Returns whether the device is voice-capable (e.g. a phone vs a tablet).
     *
     * @return {@code True} if the device is voice-capable.
     */
    private boolean isVoiceCapable() {
        return mContext.getApplicationContext().getResources().getBoolean(
                com.android.internal.R.bool.config_voice_capable);
    }

    /**
     * Trampolines the intent to the broadcast receiver that runs only as the primary user.
     */
    private boolean sendBroadcastToReceiver(Intent intent) {
        intent.putExtra(CallIntentProcessor.KEY_IS_INCOMING_CALL, false);
        intent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        intent.setClass(mContext, PrimaryCallReceiver.class);
        Log.d(this, "Sending broadcast as user to CallReceiver");
        mContext.sendBroadcastAsUser(intent, UserHandle.SYSTEM);
        return true;
    }

    private static void showErrorDialogForRestrictedOutgoingCall(Context context, int stringId) {
        final Intent intent = new Intent(context, ErrorDialogActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ErrorDialogActivity.ERROR_MESSAGE_ID_EXTRA, stringId);
        context.startActivityAsUser(intent, UserHandle.CURRENT);
    }
}
