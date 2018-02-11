/*
 * Copyright (C) 2006 The Android Open Source Project
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
/******************************************************************************/
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/*    date   |        author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 11/04/2016|     Dandan.Fang      |     TASK3318984      |When click "always */
/*           |                      |                      |forward" notification, */
/*           |                      |                      |ue should go to " */
/*           |                      |                      |"call forwarding  */
/*           |                      |                      |settings" UI.     */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/
package com.android.phone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import mst.preference.PreferenceManager;
import android.provider.ContactsContract.PhoneLookup;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;
//[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/29/2016, SOLUTION- 2500198 And TASk-2781344
import android.os.Handler;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import com.android.internal.telephony.TelephonyIntents;
//[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyCapabilities;
import com.android.phone.settings.VoicemailSettingsActivity;
import com.android.phone.vvm.omtp.sync.VoicemailStatusQueryHelper;
import com.android.phone.settings.VoicemailNotificationSettingsUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.codeaurora.internal.IExtTelephony;

/**
 * NotificationManager-related utility code for the Phone app.
 *
 * This is a singleton object which acts as the interface to the
 * framework's NotificationManager, and is used to display status bar
 * icons and control other status bar-related behavior.
 *
 * @see PhoneGlobals.notificationMgr
 */
public class NotificationMgr {
    private static final String LOG_TAG = NotificationMgr.class.getSimpleName();
    private static final boolean DBG =
            (PhoneGlobals.DBG_LEVEL >= 1) && (SystemProperties.getInt("ro.debuggable", 0) == 1);
    // Do not check in with VDBG = true, since that may write PII to the system log.
    private static final boolean VDBG = false;

    // notification types
    static final int MMI_NOTIFICATION = 1;
    static final int NETWORK_SELECTION_NOTIFICATION = 2;
    static final int VOICEMAIL_NOTIFICATION = 3;
    static final int CALL_FORWARD_NOTIFICATION = 4;
    static final int DATA_DISCONNECTED_ROAMING_NOTIFICATION = 5;
    static final int SELECTED_OPERATOR_FAIL_NOTIFICATION = 6;

    static final int NOTIFICATION_ID_OFFSET = 50;
    static final int LTE_ONLT_NOTIFICATION = 9;

    /** The singleton NotificationMgr instance. */
    private static NotificationMgr sInstance;

    private PhoneGlobals mApp;
    private Phone mPhone;

    private Context mContext;
    private NotificationManager mNotificationManager;
    private final ComponentName mNotificationComponent;
    private StatusBarManager mStatusBarManager;
    private UserManager mUserManager;
    private Toast mToast;
    private SubscriptionManager mSubscriptionManager;
    private TelecomManager mTelecomManager;
    private TelephonyManager mTelephonyManager;

    // used to track the notification of selected network unavailable
    private boolean mSelectedUnavailableNotify = false;
    //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/29/2016, SOLUTION- 2500198 And TASk-2781344
    private Handler mHandler = new Handler();
    private int updateTime = 0;
    private int maxUpdateTimes = 3;
    private int mDelayTime =1000;
    //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)

    // used to track whether the message waiting indicator is visible, per subscription id.
    private ArrayMap<Integer, Boolean> mMwiVisible = new ArrayMap<Integer, Boolean>();

    //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/31/2016, SOLUTION- 2499549 And TASk-2781362
    //[Call setting]SIM 2 call forwarding icon not disappear when pull out SIM 2
    private ArrayMap<Integer, Integer> mCfiVisible = new ArrayMap<Integer, Integer>();
    //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
    //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/29/2016, SOLUTION- 2500198 And TASk-2781344
    private class SimStateChangeReceiver extends BroadcastReceiver {
        // Broadcast Action: The sim card state has changed.
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
                if (null != action && action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
                    int state = mTelephonyManager.getSimState();
                    switch (state) {
                        case TelephonyManager.SIM_STATE_READY:
                            updateTime = 0;
                            break;
                        default:
                            break;
                     }
                }
         }
    }
    //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)

//[SOLUTION]-Add-BEGIN by TCTNB.(JiangLong Pan), 08/12/2016, SOLUTION-2504537
//[GPR][RU][NB PreTest][Roaming]Data roaming warning about mobile data closed should not disappear until mobile data restored
    private boolean mRuRomingNotification;
//[SOLUTION]-Add-END by TCTNB.(JiangLong Pan)
    private IExtTelephony mExtTelephony = IExtTelephony.Stub.
            asInterface(ServiceManager.getService("extphone"));

    /**
     * Private constructor (this is a singleton).
     * @see #init(PhoneGlobals)
     */
    private NotificationMgr(PhoneGlobals app) {
        mApp = app;
        mContext = app;
        mNotificationManager =
                (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
        mStatusBarManager =
                (StatusBarManager) app.getSystemService(Context.STATUS_BAR_SERVICE);
        mUserManager = (UserManager) app.getSystemService(Context.USER_SERVICE);
        mPhone = app.mCM.getDefaultPhone();
        mSubscriptionManager = SubscriptionManager.from(mContext);
        mTelecomManager = TelecomManager.from(mContext);
        mTelephonyManager = (TelephonyManager) app.getSystemService(Context.TELEPHONY_SERVICE);

        //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/29/2016, SOLUTION- 2500198 And TASk-2781344
        IntentFilter intentFilter =new IntentFilter(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        SimStateChangeReceiver mSimStateChangeReceiver = new SimStateChangeReceiver();
        mContext.registerReceiver(mSimStateChangeReceiver, intentFilter);
        //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
        final String notificationComponent = mContext.getString(
                R.string.config_customVoicemailComponent);

        mNotificationComponent = notificationComponent != null
                ? ComponentName.unflattenFromString(notificationComponent) : null;

//[SOLUTION]-Add-BEGIN by TCTNB.(JiangLong Pan), 08/12/2016, SOLUTION-2504537
//[GPR][RU][NB PreTest][Roaming]Data roaming warning about mobile data closed should not disappear until mobile data restored
        mRuRomingNotification = mContext.getResources().getBoolean(R.bool.feature_phone_russia_show_roaming_notification);
//[SOLUTION]-Add-END by TCTNB.(JiangLong Pan)
    }

    /**
     * Initialize the singleton NotificationMgr instance.
     *
     * This is only done once, at startup, from PhoneApp.onCreate().
     * From then on, the NotificationMgr instance is available via the
     * PhoneApp's public "notificationMgr" field, which is why there's no
     * getInstance() method here.
     */
    /* package */ static NotificationMgr init(PhoneGlobals app) {
        synchronized (NotificationMgr.class) {
            if (sInstance == null) {
                sInstance = new NotificationMgr(app);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            return sInstance;
        }
    }

    /** The projection to use when querying the phones table */
    static final String[] PHONES_PROJECTION = new String[] {
        PhoneLookup.NUMBER,
        PhoneLookup.DISPLAY_NAME,
        PhoneLookup._ID
    };

    /**
     * Re-creates the message waiting indicator (voicemail) notification if it is showing.  Used to
     * refresh the voicemail intent on the indicator when the user changes it via the voicemail
     * settings screen.  The voicemail notification sound is suppressed.
     *
     * @param subId The subscription Id.
     */
    /* package */ void refreshMwi(int subId) {
        // In a single-sim device, subId can be -1 which means "no sub id".  In this case we will
        // reference the single subid stored in the mMwiVisible map.
        if (subId == SubscriptionInfoHelper.NO_SUB_ID) {
            if (mMwiVisible.keySet().size() == 1) {
                Set<Integer> keySet = mMwiVisible.keySet();
                Iterator<Integer> keyIt = keySet.iterator();
                if (!keyIt.hasNext()) {
                    return;
                }
                subId = keyIt.next();
            }
        }
        if (mMwiVisible.containsKey(subId)) {
            boolean mwiVisible = mMwiVisible.get(subId);
            if (mwiVisible) {
                updateMwi(subId, mwiVisible, false /* enableNotificationSound */);
            }
        }
    }

    /**
     * Updates the message waiting indicator (voicemail) notification.
     *
     * @param visible true if there are messages waiting
     */
    /* package */ void updateMwi(int subId, boolean visible) {
        updateMwi(subId, visible, true /* enableNotificationSound */);
    }

    /**
     * Updates the message waiting indicator (voicemail) notification.
     *
     * @param subId the subId to update.
     * @param visible true if there are messages waiting
     * @param enableNotificationSound {@code true} if the notification sound should be played.
     */
    void updateMwi(int subId, boolean visible, boolean enableNotificationSound) {
        if (!PhoneGlobals.sVoiceCapable) {
            // Do not show the message waiting indicator on devices which are not voice capable.
            // These events *should* be blocked at the telephony layer for such devices.
            Log.w(LOG_TAG, "Called updateMwi() on non-voice-capable device! Ignoring...");
            return;
        }
        int [] mwiIcon = {R.drawable.stat_notify_voicemail_sub1,
                R.drawable.stat_notify_voicemail_sub2};
        Phone phone = PhoneGlobals.getPhone(subId);
        if (phone == null) {
            Log.w(LOG_TAG, "updateMwi: phone is null, returning...");
            return;
        }
        int phoneId = phone.getPhoneId();
        int notificationId = getNotificationId(VOICEMAIL_NOTIFICATION, phoneId);

        if (visible && phone != null) {
            VoicemailStatusQueryHelper queryHelper = new VoicemailStatusQueryHelper(mContext);
            PhoneAccountHandle phoneAccount = PhoneUtils.makePstnPhoneAccountHandle(phone);
            if (queryHelper.isNotificationsChannelActive(phoneAccount)) {
                Log.v(LOG_TAG, "Notifications channel active for visual voicemail, hiding mwi.");
                visible = false;
            }
        }

        Log.i(LOG_TAG, "updateMwi(): subId " + subId + " update to " + visible);
        mMwiVisible.put(subId, visible);

        if (visible) {
            if (phone == null) {
                Log.w(LOG_TAG, "Found null phone for: " + subId);
                return;
            }

            SubscriptionInfo subInfo = mSubscriptionManager.getActiveSubscriptionInfo(subId);
            if (subInfo == null) {
                Log.w(LOG_TAG, "Found null subscription info for: " + subId);
                return;
            }

            int resId = android.R.drawable.stat_notify_voicemail;
            if (mTelephonyManager.getPhoneCount() > 1) {
                resId = mwiIcon[phoneId];
            }

            // This Notification can get a lot fancier once we have more
            // information about the current voicemail messages.
            // (For example, the current voicemail system can't tell
            // us the caller-id or timestamp of a message, or tell us the
            // message count.)

            // But for now, the UI is ultra-simple: if the MWI indication
            // is supposed to be visible, just show a single generic
            // notification.

            String notificationTitle = mContext.getString(R.string.notification_voicemail_title);
            String vmNumber = phone.getVoiceMailNumber();
            if (DBG) log("- got vm number: '" + vmNumber + "'");

            // The voicemail number may be null because:
            //   (1) This phone has no voicemail number.
            //   (2) This phone has a voicemail number, but the SIM isn't ready yet. This may
            //       happen when the device first boots if we get a MWI notification when we
            //       register on the network before the SIM has loaded. In this case, the
            //       SubscriptionListener in CallNotifier will update this once the SIM is loaded.
            if ((vmNumber == null) && !phone.getIccRecordsLoaded()) {
                if (DBG) log("- Null vm number: SIM records not loaded (yet)...");
                return;
            }

            Integer vmCount = null;

            if (TelephonyCapabilities.supportsVoiceMessageCount(phone)) {
                vmCount = phone.getVoiceMessageCount();
                String titleFormat = mContext.getString(R.string.notification_voicemail_title_count);
                notificationTitle = String.format(titleFormat, vmCount);
            }

            // This pathway only applies to PSTN accounts; only SIMS have subscription ids.
            PhoneAccountHandle phoneAccountHandle = PhoneUtils.makePstnPhoneAccountHandle(phone);

            Intent intent;
            String notificationText;
            boolean isSettingsIntent = TextUtils.isEmpty(vmNumber);

            if (isSettingsIntent) {
                notificationText = mContext.getString(
                        R.string.notification_voicemail_no_vm_number);

                // If the voicemail number if unknown, instead of calling voicemail, take the user
                // to the voicemail settings.
                notificationText = mContext.getString(
                        R.string.notification_voicemail_no_vm_number);
                intent = new Intent(VoicemailSettingsActivity.ACTION_ADD_VOICEMAIL);
                intent.putExtra(SubscriptionInfoHelper.SUB_ID_EXTRA, subId);
                intent.setClass(mContext, VoicemailSettingsActivity.class);
            } else {
                if (mTelephonyManager.getPhoneCount() > 1) {
                    notificationText = subInfo.getDisplayName().toString();
                } else {
                    notificationText = String.format(
                            mContext.getString(R.string.notification_voicemail_text_format),
                            PhoneNumberUtils.formatNumber(vmNumber));
                }
                intent = new Intent(
                        Intent.ACTION_CALL, Uri.fromParts(PhoneAccount.SCHEME_VOICEMAIL, "",
                                null));
                intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle);
                //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/31/2016, SOLUTION- 2499755 And TASk-2781359
                //[Call]From the notification bar can not dial voicemail when inserting SIM 2.
                intent.putExtra("SubId", subId);
                //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen).
            }

            PendingIntent pendingIntent =
                    PendingIntent.getActivity(mContext, subId /* requestCode */, intent, 0);
            Uri ringtoneUri = null;

            if (enableNotificationSound) {
                ringtoneUri = VoicemailNotificationSettingsUtil.getRingtoneUri(phone);
            }

            Resources res = mContext.getResources();
            PersistableBundle carrierConfig = PhoneGlobals.getInstance().getCarrierConfigForSubId(
                    phone.getSubId());

            //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/31/2016, SOLUTION- 2500171 And TASk-2781356
            //[Call]"New voicemail" will disappear when click it in the notification bar
            boolean isOngoing = carrierConfig.getBoolean(CarrierConfigManager.KEY_VOICEMAIL_NOTIFICATION_PERSISTENT_BOOL);
            log("KEY_VOICEMAIL_NOTIFICATION_PERSISTENT_BOOL = " + isOngoing);
            //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)

            Notification.Builder builder = new Notification.Builder(mContext);
            builder.setSmallIcon(resId)
                    .setWhen(System.currentTimeMillis())
                    .setColor(subInfo.getIconTint())
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationText)
                    .setContentIntent(pendingIntent)
                    .setSound(ringtoneUri)
                    .setColor(res.getColor(R.color.dialer_theme_color));
                    //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/31/2016, SOLUTION- 2500171 And TASk-2781356
                    //[Call]"New voicemail" will disappear when click it in the notification bar
                    //.setOngoing(carrierConfig.getBoolean(
                    //        CarrierConfigManager.KEY_VOICEMAIL_NOTIFICATION_PERSISTENT_BOOL));
                    //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
            boolean voicemailNotiCleardisable = mContext.getResources().getBoolean(R.bool.def_voicemailNotiClear_disable);

            if (VoicemailNotificationSettingsUtil.isVibrationEnabled(phone)) {
                builder.setDefaults(Notification.DEFAULT_VIBRATE);
            }

            final Notification notification = builder.build();

            //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/31/2016, SOLUTION- 2500171 And TASk-2781356
            //[Call]"New voicemail" will disappear when click it in the notification bar
            if (!TextUtils.isEmpty(vmNumber) && !voicemailNotiCleardisable) {
                notification.flags = Notification.FLAG_AUTO_CANCEL;
            }
            //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)

            List<UserInfo> users = mUserManager.getUsers(true);
            for (int i = 0; i < users.size(); i++) {
                final UserInfo user = users.get(i);
                final UserHandle userHandle = user.getUserHandle();
                if (!mUserManager.hasUserRestriction(
                        UserManager.DISALLOW_OUTGOING_CALLS, userHandle)
                        && !user.isManagedProfile()) {
                    if (!sendNotificationCustomComponent(vmCount, vmNumber, pendingIntent,
                            isSettingsIntent)) {
                        mNotificationManager.notifyAsUser(
                                Integer.toString(subId) /* tag */,
                                notificationId,
                                notification,
                                userHandle);
                    }
                }
            }
        } else {
            if (!sendNotificationCustomComponent(0, null, null, false)) {
                //[SOLUTION]-Mod-BEGIN by TCTNB.(Caixia Chen), 08/29/2016, SOLUTION-2500141
                for(int i = 0;i < 2; i++) {
                    notificationId = getNotificationId(VOICEMAIL_NOTIFICATION, i);
                    mNotificationManager.cancelAsUser(
                            Integer.toString(subId) /* tag */,
                            notificationId,
                            UserHandle.ALL);
                }
                //[SOLUTION]-Mod-END by TCTNB.(Caixia Chen)
            }
        }
    }

    /**
     * Sends a broadcast with the voicemail notification information to a custom component to
     * handle. This method is also used to indicate to the custom component when to clear the
     * notification. A pending intent can be passed to the custom component to indicate an action to
     * be taken as it would by a notification produced in this class.
     * @param count The number of pending voicemail messages to indicate on the notification. A
     *              Value of 0 is passed here to indicate that the notification should be cleared.
     * @param number The voicemail phone number if specified.
     * @param pendingIntent The intent that should be passed as the action to be taken.
     * @param isSettingsIntent {@code true} to indicate the pending intent is to launch settings.
     *                         otherwise, {@code false} to indicate the intent launches voicemail.
     * @return {@code true} if a custom component was notified of the notification.
     */
    private boolean sendNotificationCustomComponent(Integer count, String number,
            PendingIntent pendingIntent, boolean isSettingsIntent) {
        if (mNotificationComponent != null) {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            intent.setComponent(mNotificationComponent);
            intent.setAction(TelephonyManager.ACTION_SHOW_VOICEMAIL_NOTIFICATION);

            if (count != null) {
                intent.putExtra(TelephonyManager.EXTRA_NOTIFICATION_COUNT, count);
            }

            // Additional information about the voicemail notification beyond the count is only
            // present when the count not specified or greater than 0. The value of 0 represents
            // clearing the notification, which does not require additional information.
            if (count == null || count > 0) {
                if (!TextUtils.isEmpty(number)) {
                    intent.putExtra(TelephonyManager.EXTRA_VOICEMAIL_NUMBER, number);
                }

                if (pendingIntent != null) {
                    intent.putExtra(isSettingsIntent
                            ? TelephonyManager.EXTRA_LAUNCH_VOICEMAIL_SETTINGS_INTENT
                            : TelephonyManager.EXTRA_CALL_VOICEMAIL_INTENT,
                            pendingIntent);
                }
            }

            mContext.sendBroadcast(intent);
            return true;
        }

        return false;
    }

    /**
     * Updates the message call forwarding indicator notification.
     *
     * @param visible true if there are messages waiting
     */
    /* package */ void updateCfi(int subId, boolean visible) {
        if (DBG) log("updateCfi(): " + visible);
        Phone phone = PhoneGlobals.getPhone(subId);
        if (phone == null) {
            Log.w(LOG_TAG, "updateCfi: phone is null, returning...");
            return;
        }
        int phoneId = phone.getPhoneId();
        int [] callfwdIcon = {R.drawable.stat_sys_phone_call_forward_sub1,
                R.drawable.stat_sys_phone_call_forward_sub2};
        int notificationId = getNotificationId(CALL_FORWARD_NOTIFICATION, phoneId);

        if (visible) {
            // If Unconditional Call Forwarding (forward all calls) for VOICE
            // is enabled, just show a notification.  We'll default to expanded
            // view for now, so the there is less confusion about the icon.  If
            // it is deemed too weird to have CF indications as expanded views,
            // then we'll flip the flag back.

            // TODO: We may want to take a look to see if the notification can
            // display the target to forward calls to.  This will require some
            // effort though, since there are multiple layers of messages that
            // will need to propagate that information.

            SubscriptionInfo subInfo = mSubscriptionManager.getActiveSubscriptionInfo(subId);
            if (subInfo == null) {
                Log.w(LOG_TAG, "Found null subscription info for: " + subId);
                return;
            }

            //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/31/2016, SOLUTION- 2499549 And TASk-2781362
            mCfiVisible.put(subId, notificationId);
            Log.i("cfi_test", "show cfi notification: subId " + subId + ", notificationId = " + notificationId);
            //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)

            String notificationTitle;
            int resId = R.drawable.stat_sys_phone_call_forward;
            if (mTelephonyManager.getPhoneCount() > 1) {
                resId = callfwdIcon[phoneId];
                notificationTitle = subInfo.getDisplayName().toString();
            } else {
                notificationTitle = mContext.getString(R.string.labelCF);
            }

            Notification.Builder builder = new Notification.Builder(mContext)
                    .setSmallIcon(resId)
                    .setColor(subInfo.getIconTint())
                    .setContentTitle(notificationTitle)
                    .setContentText(mContext.getString(R.string.sum_cfu_enabled_indicator))
                    .setShowWhen(false)
                    .setOngoing(true);

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //[BUGFIX]-Add-BEGIN by TCTNB.Dandan.Fang,11/04/2016,TASK3318984,
            //[Call forwarding]When click "always forward" notification, ue should go to "call forwarding settings" UI.
            //intent.setClassName("com.android.phone", "com.android.phone.CallFeaturesSetting");
            intent.setClassName("com.android.phone", "com.android.phone.GsmUmtsCallForwardOptions");
            //[BUGFIX]-Add-END by TCTNB.Dandan.Fang
            SubscriptionInfoHelper.addExtrasToIntent(
                    intent, mSubscriptionManager.getActiveSubscriptionInfo(subId));
            //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/29/2016, SOLUTION- 2500198 And TASk-2781344
            //PendingIntent contentIntent = PendingIntent.getActivity(mContext, subId /* requestCode */, intent, 0);
            PendingIntent contentIntent = PendingIntent.getActivity(mContext, subId /* requestCode */, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
            List<UserInfo> users = mUserManager.getUsers(true);
            for (int i = 0; i < users.size(); i++) {
                final UserInfo user = users.get(i);
                if (user.isManagedProfile()) {
                    continue;
                }
                UserHandle userHandle = user.getUserHandle();
                builder.setContentIntent(user.isAdmin() ? contentIntent : null);
                mNotificationManager.notifyAsUser(
                        Integer.toString(subId) /* tag */,
                        notificationId,
                        builder.build(),
                        userHandle);
            }
           //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/29/2016, SOLUTION- 2500198 And TASk-2781344
           if (updateTime < maxUpdateTimes) {
               final int mSubId = subId;
               final boolean mVisible = visible;
               mHandler.postDelayed(new Runnable() {
                   @Override
                   public void run() {
                       updateCfi(mSubId,mVisible);
                       updateTime++;
                   }
               }, mDelayTime);
           }
        //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
        } else {
            //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/31/2016, SOLUTION- 2499549 And TASk-2781362
            //[Call setting]SIM 2 call forwarding icon not disappear when pull out SIM 2
            if (!mSubscriptionManager.isActiveSubId(subId) && mCfiVisible.containsKey(subId)) {
                Log.i("cfi_test", "cancel cfi notification pull out sim subId " + subId + ", notificationId = " + mCfiVisible.get(subId)
                        + ", id2 = " + notificationId);
                mNotificationManager.cancelAsUser(
                        Integer.toString(subId) /* tag */,
                        mCfiVisible.get(subId),
                        UserHandle.ALL);
                mCfiVisible.remove(subId);
            } else {
                Log.i("cfi_test", "cancel cfi notification subId " + subId + ", notificationId = " + notificationId
                        + ", cfi noti_id = " + mCfiVisible.get(subId) + ", cfi size = " + mCfiVisible.size());
                mNotificationManager.cancelAsUser(
                        Integer.toString(subId) /* tag */,
                        notificationId,
                        UserHandle.ALL);
                mCfiVisible.remove(subId);
            }
            //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
        }
    }

    /**
     * Shows the "data disconnected due to roaming" notification, which
     * appears when you lose data connectivity because you're roaming and
     * you have the "data roaming" feature turned off.
     */
    /* package */ void showDataDisconnectedRoaming() {
        if (DBG) log("showDataDisconnectedRoaming()...");

        // "Mobile network settings" screen / dialog
        Intent intent = new Intent(mContext, com.android.phone.MobileNetworkSettings.class);
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

        final CharSequence contentText = mContext.getText(R.string.roaming_reenable_message);

        final Notification.Builder builder = new Notification.Builder(mContext)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle(mContext.getText(R.string.roaming))
                .setColor(mContext.getResources().getColor(R.color.dialer_theme_color))
                .setContentText(contentText);

        List<UserInfo> users = mUserManager.getUsers(true);
        for (int i = 0; i < users.size(); i++) {
            final UserInfo user = users.get(i);
            if (user.isManagedProfile()) {
                continue;
            }
            UserHandle userHandle = user.getUserHandle();
            builder.setContentIntent(user.isAdmin() ? contentIntent : null);
            final Notification notif =
                    new Notification.BigTextStyle(builder).bigText(contentText).build();

//[SOLUTION]-Add-BEGIN by TCTNB.(JiangLong Pan), 08/12/2016, SOLUTION-2504537
//[GPR][RU][NB PreTest][Roaming]Data roaming warning about mobile data closed should not disappear until mobile data restored
            if (mRuRomingNotification) {
                notif.flags = Notification.FLAG_ONGOING_EVENT;
            }
//[SOLUTION]-Add-END by TCTNB.(JiangLong Pan)

            mNotificationManager.notifyAsUser(
                    null /* tag */, DATA_DISCONNECTED_ROAMING_NOTIFICATION, notif, userHandle);
        }
    }

    /**
     * Turns off the "data disconnected due to roaming" notification.
     */
    /* package */ void hideDataDisconnectedRoaming() {
        if (DBG) log("hideDataDisconnectedRoaming()...");
        mNotificationManager.cancel(DATA_DISCONNECTED_ROAMING_NOTIFICATION);
    }

    /**
     * Display the network selection "no service" notification
     * @param operator is the numeric operator number
     */
    private void showNetworkSelection(String operator) {
        if (DBG) log("showNetworkSelection(" + operator + ")...");

        Notification.Builder builder = new Notification.Builder(mContext)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setShowWhen(false)
                .setOngoing(true);
        /* MODIFIED-BEGIN by bo.chen, 2016-09-27,BUG-3000255*/
        if (mContext.getResources().getBoolean( R.bool.feature_phone_show_search_network_switch_on)) {
            builder
            .setContentTitle(mContext.getString(R.string.network_register_fail_title))
            .setContentText(
                   mContext.getString(R.string.network_register_fail_text));
        } else {
            builder
            .setContentTitle(mContext.getString(R.string.notification_network_selection_title))
            .setContentText(
                    mContext.getString(R.string.notification_network_selection_text, operator));
        }
        /* MODIFIED-END by bo.chen,BUG-3000255*/
        // create the target network operators settings intent
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        // Use NetworkSetting to handle the selection intent
        intent.setComponent(new ComponentName(
                mContext.getString(R.string.network_operator_settings_package),
                mContext.getString(R.string.network_operator_settings_class)));
        intent.putExtra(GsmUmtsOptions.EXTRA_SUB_ID, mPhone.getSubId());
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

        List<UserInfo> users = mUserManager.getUsers(true);
        for (int i = 0; i < users.size(); i++) {
            final UserInfo user = users.get(i);
            if (user.isManagedProfile()) {
                continue;
            }
            UserHandle userHandle = user.getUserHandle();
            builder.setContentIntent(user.isAdmin() ? contentIntent : null);
            mNotificationManager.notifyAsUser(
                    null /* tag */,
                    SELECTED_OPERATOR_FAIL_NOTIFICATION,
                    builder.build(),
                    userHandle);
        }
    }

    /**
     * Turn off the network selection "no service" notification
     */
    private void cancelNetworkSelection() {
        if (DBG) log("cancelNetworkSelection()...");
        mNotificationManager.cancelAsUser(
                null /* tag */, SELECTED_OPERATOR_FAIL_NOTIFICATION, UserHandle.ALL);
    }

    /**
     * Update notification about no service of user selected operator
     *
     * @param serviceState Phone service state
     */
    void updateNetworkSelection(int serviceState) {
        if (TelephonyCapabilities.supportsNetworkSelection(mPhone)) {
            int subId = mPhone.getSubId();
            int slotId = mPhone.getPhoneId();
            int provisionStatus;
            final int PROVISIONED = 1;
            final int INVALID_STATE = -1;
            if (SubscriptionManager.isValidSubscriptionId(subId)) {
                // get the shared preference of network_selection.
                // empty is auto mode, otherwise it is the operator alpha name
                // in case there is no operator name, check the operator numeric
                SharedPreferences sp =
                        PreferenceManager.getDefaultSharedPreferences(mContext);
                String networkSelection =
                        sp.getString(Phone.NETWORK_SELECTION_NAME_KEY + subId, "");
                if (TextUtils.isEmpty(networkSelection)) {
                    networkSelection =
                            sp.getString(Phone.NETWORK_SELECTION_KEY + subId, "");
                }

                if (DBG) log("updateNetworkSelection()..." + "state = " +
                        serviceState + " new network " + networkSelection);

                try {
                    //get current provision state of the SIM.
                    provisionStatus = mExtTelephony.getCurrentUiccCardProvisioningStatus(slotId);
                } catch (RemoteException ex) {
                    provisionStatus = INVALID_STATE;
                    if (DBG) log("Failed to get status for slotId: "+ slotId +" Exception: " + ex);
                } catch (NullPointerException ex) {
                    provisionStatus = INVALID_STATE;
                    if (DBG) log("Failed to get status for slotId: "+ slotId +" Exception: " + ex);
                }

                if (serviceState == ServiceState.STATE_OUT_OF_SERVICE
                        && !TextUtils.isEmpty(networkSelection)
                        && provisionStatus == PROVISIONED) {
                    showNetworkSelection(networkSelection);
                    mSelectedUnavailableNotify = true;
                } else {
                    if (mSelectedUnavailableNotify) {
                        cancelNetworkSelection();
                        mSelectedUnavailableNotify = false;
                    }
                }
            } else {
                if (DBG) log("updateNetworkSelection()..." + "state = " +
                        serviceState + " not updating network due to invalid subId " + subId);
            }
        }
    }

    /* package */ void postTransientNotification(int notifyId, CharSequence msg) {
        if (mToast != null) {
            mToast.cancel();
        }

        mToast = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        mToast.show();
    }

    private int getNotificationId(int notificationId, int slotId) {
        return notificationId + (slotId * NOTIFICATION_ID_OFFSET);
    }

    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }

    //[FEATURE]-Add-BEGIN by TCTNB.Fuqiang.Song,08/04/2016,2670355,
    //For Russian Beeline,notify end user when set preferred network mode to Lte  On-
    //ly.The Notification should display when reboot or hotplug if Lte only is the
    //preferred networkmode. Refer to 528518.

    /**
     * Updates the message LTE ONLY indicator notification.
     *
     * @param visible true if there are messages waiting
     */
    void updateLteOnlyIcon(boolean visible) {
        if (visible) {
            Notification notification;

            final CharSequence contentText = mContext.getText(R.string.lteonly_notification_message);

            final Notification.Builder builder = new Notification.Builder(mContext);
            builder.setSmallIcon(R.drawable.cs_service_not_available);
            builder.setContentTitle(mContext.getText(R.string.lteonly_notification_title));
            builder.setContentText(contentText);

            Intent intent;
            intent = new Intent("intent.notification.disabledlteonly");

            builder.setContentIntent(PendingIntent.getBroadcast(mContext, 0, intent, 0));

            notification = new Notification.BigTextStyle(builder).bigText(contentText)
                    .build();

            notification.flags |= Notification.FLAG_ONGOING_EVENT;  // also implies FLAG_NO_CLEAR
            notification.priority = Notification.PRIORITY_HIGH;

            mNotificationManager.notify(LTE_ONLT_NOTIFICATION, notification);
        } else {
            mNotificationManager.cancel(LTE_ONLT_NOTIFICATION);
        }
    }
    //[FEATURE]-Add-END by TCTNB.Fuqiang.Song

}
