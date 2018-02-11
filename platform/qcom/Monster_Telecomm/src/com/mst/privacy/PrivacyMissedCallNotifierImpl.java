/*
 * Copyright 2014, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mst.privacy;

import static android.Manifest.permission.READ_PHONE_STATE;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;

import com.android.server.telecom.Call;
import com.android.server.telecom.CallState;
import com.android.server.telecom.CallerInfoAsyncQueryFactory;
import com.android.server.telecom.CallsManager;
import com.android.server.telecom.CallsManagerListenerBase;
import com.android.server.telecom.Constants;
import com.android.server.telecom.ContactsAsyncHelper;
import com.android.server.telecom.Log;
import com.android.server.telecom.MissedCallNotifier;
import com.android.server.telecom.PhoneAccountRegistrar;
import com.android.server.telecom.R;
import com.android.server.telecom.Runnable;
import com.android.server.telecom.TelecomBroadcastIntentProcessor;
import com.android.server.telecom.TelecomSystem;
import com.android.server.telecom.TelephonyUtil;
import com.android.server.telecom.components.TelecomBroadcastReceiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.UserHandle;
import android.provider.CallLog.Calls;
import android.telecom.DefaultDialerManager;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccount;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.BidiFormatter;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;

import com.android.internal.telephony.CallerInfo;

import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

//[SOLUTION]-Add-BEGIN by TCTNB.(Caixia Chen), 08/23/2016, SOLUTION-2504488
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
//[SOLUTION]-Add-END by TCTNB.(Caixia Chen)

import android.telephony.SubscriptionManager;

// TODO: Needed for move to system service: import com.android.internal.R;

/**
 * Creates a notification for calls that the user missed (neither answered nor
 * rejected).
 * 
 * TODO: Make TelephonyManager.clearMissedCalls call into this class.
 * 
 * TODO: Reduce dependencies in this implementation; remove the need to create a
 * new Call simply to look up caller metadata, and if possible, make it
 * unnecessary to get a direct reference to the CallsManager. Try to make this
 * class simply handle the UI and Android-framework entanglements of missed call
 * notification.
 */
public class PrivacyMissedCallNotifierImpl extends CallsManagerListenerBase
        implements MissedCallNotifier {

    public interface PrivacyMissedCallNotifierImplFactory {
        MissedCallNotifier makeMissedCallNotifierImpl(Context context,
                PhoneAccountRegistrar phoneAccountRegistrar);
    }

    public interface NotificationBuilderFactory {
        Notification.Builder getBuilder(Context context);
    }

    private static class DefaultNotificationBuilderFactory implements
            NotificationBuilderFactory {
        public DefaultNotificationBuilderFactory() {
        }

        @Override
        public Notification.Builder getBuilder(Context context) {
            return new Notification.Builder(context);
        }
    }

    private static final String[] CALL_LOG_PROJECTION = new String[] {
            Calls._ID, Calls.NUMBER, Calls.NUMBER_PRESENTATION, Calls.DATE,
            Calls.DURATION, Calls.TYPE, Calls.PHONE_ACCOUNT_ID // add by lgy for
                                                               // 3429114
    };

    private static final int CALL_LOG_COLUMN_ID = 0;
    private static final int CALL_LOG_COLUMN_NUMBER = 1;
    private static final int CALL_LOG_COLUMN_NUMBER_PRESENTATION = 2;
    private static final int CALL_LOG_COLUMN_DATE = 3;
    private static final int CALL_LOG_COLUMN_DURATION = 4;
    private static final int CALL_LOG_COLUMN_TYPE = 5;
    private static final int CALL_LOG_COLUMN_PHONE_ACCOUNT_ID = 6; // add by lgy
                                                                   // for
                                                                   // 3429114

    static final int NOTIFICATION_BASE = 17000 + 3 + 100;
    static final int HANGUP_PRIVATE_RINGING_CALL_NOTIFICATION = NOTIFICATION_BASE + 1;
    private static final int MISSED_CALL_NOTIFICATION_ID = NOTIFICATION_BASE + 2;

    private final Context mContext;
    private final PhoneAccountRegistrar mPhoneAccountRegistrar;
    private final NotificationManager mNotificationManager;
    // [SOLUTION]-Add-BEGIN by TCTNB.(Caixia Chen), 08/23/2016, SOLUTION-2504488
    private String mLastFdnMissedCallName = "mLastFdnMissedCallName";
    private String mLastFdnMissedCallNumber = "mLastFdnMissedCallNumber";
    private String isStartThePhone = "isStartThePhone";
    private SharedPreferences msharedPreferences;
    private SharedPreferences.Editor mDitor;
    // [SOLUTION]-Add-END by TCTNB.(Caixia Chen)
    private final NotificationBuilderFactory mNotificationBuilderFactory;
    private final ComponentName mNotificationComponent;
    private UserHandle mCurrentUserHandle;

    // Used to track the number of missed calls.
    private ConcurrentMap<UserHandle, AtomicInteger> mMissedCallCounts;

    public PrivacyMissedCallNotifierImpl(Context context,
            PhoneAccountRegistrar phoneAccountRegistrar) {
        this(context, phoneAccountRegistrar,
                new DefaultNotificationBuilderFactory());
    }

    public PrivacyMissedCallNotifierImpl(Context context,
            PhoneAccountRegistrar phoneAccountRegistrar,
            NotificationBuilderFactory notificationBuilderFactory) {
        mContext = context;
        mPhoneAccountRegistrar = phoneAccountRegistrar;
        mNotificationManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        final String notificationComponent = context
                .getString(R.string.notification_component);

        mNotificationBuilderFactory = notificationBuilderFactory;
        mNotificationComponent = notificationComponent != null ? ComponentName
                .unflattenFromString(notificationComponent) : null;
        mMissedCallCounts = new ConcurrentHashMap<>();
        // [SOLUTION]-Add-BEGIN by TCTNB.(Caixia Chen), 08/23/2016,
        // SOLUTION-2504488
        msharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        mDitor = msharedPreferences.edit();
        // [SOLUTION]-Add-END by TCTNB.(Caixia Chen)
    }

    /**
     * Clears missed call notification and marks the call log's missed calls as
     * read.
     */
    @Override
    public void clearMissedCalls(UserHandle userHandle) {
        // If the default dialer is showing the missed call notification then it
        // will modify the
        // call log and we don't have to do anything here.
        markMissedCallsAsRead(userHandle);

        cancelMissedCallNotification(userHandle);
    }

    private void markMissedCallsAsRead(final UserHandle userHandle) {
        AsyncTask.execute(new Runnable("MCNI.mMCAR") {
            @Override
            public void loggedRun() {
                // Clear the list of new missed calls from the call log.
                ContentValues values = new ContentValues();
                values.put(Calls.NEW, 0);
                values.put(Calls.IS_READ, 1);
                StringBuilder where = new StringBuilder();
                where.append(Calls.NEW);
                where.append(" = 1 AND ");
                where.append(Calls.TYPE);
                where.append(" = ?");
                where.append(" AND privacy_id = "
                        + PrivacyUtils.getCurrentAccountId());
                try {
                    Uri callsUri = ContentProvider.maybeAddUserId(
                            Calls.CONTENT_URI, userHandle.getIdentifier());
                    mContext.getContentResolver()
                            .update(callsUri,
                                    values,
                                    where.toString(),
                                    new String[] { Integer
                                            .toString(Calls.MISSED_TYPE) });
                } catch (IllegalArgumentException e) {
                    Log.w(this, "ContactsProvider update command failed", e);
                }
            }
        }.prepare());
    }

    /**
     * Create a system notification for the missed call.
     * 
     * @param call
     *            The missed call.
     */
    @Override
    public void showMissedCallNotification(Call call) {
        final PhoneAccountHandle phoneAccountHandle = call
                .getTargetPhoneAccount();
        final PhoneAccount phoneAccount = mPhoneAccountRegistrar
                .getPhoneAccountUnchecked(phoneAccountHandle);
        UserHandle userHandle;
        if (phoneAccount != null
                && phoneAccount
                        .hasCapabilities(PhoneAccount.CAPABILITY_MULTI_USER)) {
            userHandle = mCurrentUserHandle;
        } else {
            userHandle = phoneAccountHandle.getUserHandle();
        }
        showMissedCallNotification(call, userHandle);
    }

    private void showMissedCallNotification(Call call, UserHandle userHandle) {
        mMissedCallCounts.putIfAbsent(userHandle, new AtomicInteger(0));
        int missCallCounts = mMissedCallCounts.get(userHandle)
                .incrementAndGet();

        final int titleResId;
        final String expandedText; // The text in the notification's line 1 and
                                   // 2.

        // Display the first line of the notification:
        // 1 missed call: <caller name || handle>
        // More than 1 missed call: <number of calls> + "missed calls"
        if (missCallCounts == 1) {
            expandedText = getNameForCall(call);

            CallerInfo ci = call.getCallerInfo();
            if (ci != null && ci.userType == CallerInfo.USER_TYPE_WORK) {
                titleResId = R.string.notification_missedWorkCallTitle;
            } else {
                titleResId = R.string.notification_missedCallTitle;
            }
        } else {
            titleResId = R.string.notification_missedCallsTitle;
            expandedText = mContext.getString(
                    R.string.notification_missedCallsMsg, missCallCounts);
        }

        // Create a public viewable version of the notification, suitable for
        // display when sensitive
        // notification content is hidden.
        // We use user's context here to make sure notification is badged if it
        // is a managed user.
        Context contextForUser = getContextForUser(userHandle);
        Notification.Builder publicBuilder = mNotificationBuilderFactory
                .getBuilder(contextForUser);
        publicBuilder
                .setSmallIcon(android.R.drawable.stat_notify_missed_call)
                .setColor(mContext.getResources().getColor(R.color.theme_color))
                .setWhen(call.getCreationTimeMillis())
                // Show "Phone" for notification title.
                .setContentTitle(
                        mContext.getText(R.string.userCallActivityLabel))
                // Notification details shows that there are missed call(s), but
                // does not reveal
                // the missed caller information.
                .setContentText(mContext.getText(titleResId))
                .setContentIntent(createCallLogPendingIntent(userHandle))
                .setAutoCancel(true)
                .setDeleteIntent(
                        createClearMissedCallsPendingIntent(userHandle));

        // Create the notification suitable for display when sensitive
        // information is showing.
        Notification.Builder builder = mNotificationBuilderFactory
                .getBuilder(contextForUser);
        builder.setSmallIcon(android.R.drawable.stat_notify_missed_call)
                .setColor(mContext.getResources().getColor(R.color.theme_color))
                .setWhen(call.getCreationTimeMillis())
                .setContentTitle(mContext.getText(titleResId))
                .setContentText(expandedText)
                .setContentIntent(createCallLogPendingIntent(userHandle))
                .setAutoCancel(true)
                .setDeleteIntent(
                        createClearMissedCallsPendingIntent(userHandle))
                // Include a public version of the notification to be shown when
                // the missed call
                // notification is shown on the user's lock screen and they have
                // chosen to hide
                // sensitive notification information.
                .setPublicVersion(publicBuilder.build());

        // add by lgy for notification time
        builder.setShowWhen(true);

        Uri handleUri = call.getHandle();
        String handle = handleUri == null ? null : handleUri
                .getSchemeSpecificPart();

        // Add additional actions when there is only 1 missed call, like
        // call-back and SMS.
        if (missCallCounts == 1) {
            Log.d(this, "Add actions with number %s.", Log.piiHandle(handle));

            if (!TextUtils.isEmpty(handle)
                    && !TextUtils.equals(handle,
                            mContext.getString(R.string.handle_restricted))) {
                // modify by lgy for 3429114 start
                String subId = getSubIdFromCall(call);
                builder.addAction(
                        R.drawable.ic_phone_24dp,
                        mContext.getString(R.string.notification_missedCall_call_back),
                        createCallBackPendingIntent(handleUri, userHandle,
                                subId));
                // modify by lgy for 3429114 end

                if (canRespondViaSms(call)) {
                    builder.addAction(
                            R.drawable.ic_message_24dp,
                            mContext.getString(R.string.notification_missedCall_message),
                            createSendSmsFromNotificationPendingIntent(
                                    handleUri, userHandle));
                }
            }

            Bitmap photoIcon = call.getPhotoIcon();
            if (photoIcon != null) {
                builder.setLargeIcon(photoIcon);
            } else {
                Drawable photo = call.getPhoto();
                if (photo != null && photo instanceof BitmapDrawable) {
                    builder.setLargeIcon(((BitmapDrawable) photo).getBitmap());
                }
            }
        } else {
            Log.d(this, "Suppress actions. handle: %s, missedCalls: %d.",
                    Log.piiHandle(handle), missCallCounts);
        }

        Notification notification = builder.build();
        configureLedOnNotification(notification);

        Log.i(this, "Adding missed call notification for %s.", call);
        long token = Binder.clearCallingIdentity();
        try {
            mNotificationManager.notifyAsUser(null /* tag */,
                    MISSED_CALL_NOTIFICATION_ID, notification, userHandle);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /** Cancels the "missed call" notification. */
    private void cancelMissedCallNotification(UserHandle userHandle) {
        // Reset the number of missed calls to 0.
        mMissedCallCounts.putIfAbsent(userHandle, new AtomicInteger(0));
        mMissedCallCounts.get(userHandle).set(0);
        // [SOLUTION]-Add-BEGIN by TCTNB.(Caixia Chen), 08/23/2016,
        // SOLUTION-2504488
        mDitor.remove(mLastFdnMissedCallName);
        mDitor.remove(mLastFdnMissedCallNumber);
        mDitor.commit();
        // [SOLUTION]-Add-END by TCTNB.(Caixia Chen)

        long token = Binder.clearCallingIdentity();
        try {
            mNotificationManager.cancelAsUser(null,
                    MISSED_CALL_NOTIFICATION_ID, userHandle);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /**
     * Returns the name to use in the missed call notification.
     */
    private String getNameForCall(Call call) {
        String handle = call.getHandle() == null ? null : call.getHandle()
                .getSchemeSpecificPart();
        String name = call.getName();

        if (!TextUtils.isEmpty(handle)) {
            String formattedNumber = PhoneNumberUtils.formatNumber(handle,
                    getCurrentCountryIso(mContext));

            // The formatted number will be null if there was a problem
            // formatting it, but we can
            // default to using the unformatted number instead (e.g. a SIP URI
            // may not be able to
            // be formatted.
            if (!TextUtils.isEmpty(formattedNumber)) {
                handle = formattedNumber;
            }
        }

        // [SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/24/2016, SOLUTION-
        // 2487056 And TASk-2781418
        // Missed Voicemail number just display as number
        if (!TextUtils.isEmpty(handle)
                && PhoneNumberUtils.isVoiceMailNumber(handle)) {
            return mContext.getText(
                    com.android.internal.R.string.defaultVoiceMailAlphaTag)
                    .toString();
        }
        // [SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
        if (!TextUtils.isEmpty(name) && TextUtils.isGraphic(name)) {
            // [SOLUTION]-Add-BEGIN by TCTNB.(Caixia Chen), 08/09/2016,
            // SOLUTION-2496951
            if (isArabicLang()) {
                name = '\u202a' + name + '\u202c';
            }
            // [SOLUTION]-Add-END by TCTNB.(Caixia Chen)
            return name;
        } else if (!TextUtils.isEmpty(handle)) {
            // A handle should always be displayed LTR using {@link
            // BidiFormatter} regardless of the
            // content of the rest of the notification.
            // TODO: Does this apply to SIP addresses?
            BidiFormatter bidiFormatter = BidiFormatter.getInstance();
            return bidiFormatter.unicodeWrap(handle,
                    TextDirectionHeuristics.LTR);
        } else {
            // Use "unknown" if the call is unidentifiable.
            return mContext.getString(R.string.unknown);
        }
    }

    // [SOLUTION]-Add-BEGIN by TCTNB.(Caixia Chen), 08/09/2016, SOLUTION-2496951
    public static boolean isArabicLang() {
        String langCode = Locale.getDefault().getLanguage();
        if (langCode.equals("ar") || langCode.equals("fa")
                || langCode.equals("iw")) {
            return true;
        } else {
            return false;
        }
    }

    // [SOLUTION]-Add-END by TCTNB.(Caixia Chen)

    /**
     * @return The ISO 3166-1 two letters country code of the country the user
     *         is in based on the network location. If the network location does
     *         not exist, fall back to the locale setting.
     */
    private String getCurrentCountryIso(Context context) {
        // Without framework function calls, this seems to be the most accurate
        // location service
        // we can rely on.
        final TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String countryIso = telephonyManager.getNetworkCountryIso()
                .toUpperCase();

        if (countryIso == null) {
            countryIso = Locale.getDefault().getCountry();
            Log.w(this,
                    "No CountryDetector; falling back to countryIso based on locale: "
                            + countryIso);
        }
        return countryIso;
    }

    /**
     * Creates a new pending intent that sends the user to the call log.
     * 
     * @return The pending intent.
     */
    private PendingIntent createCallLogPendingIntent(UserHandle userHandle) {
        // Intent intent = new Intent(Intent.ACTION_VIEW, null);
        // intent.setType(Calls.CONTENT_TYPE);
        Intent intent = new Intent(
                "com.aurora.privacymanage.GOTO_CALL_PRIVACY_MODULE");

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(mContext);
        taskStackBuilder.addNextIntent(intent);

        return taskStackBuilder.getPendingIntent(0, 0, null, userHandle);
    }

    /**
     * Creates an intent to be invoked when the missed call notification is
     * cleared.
     */
    private PendingIntent createClearMissedCallsPendingIntent(
            UserHandle userHandle) {
        return createPrivacyTelecomPendingIntent(
                PrivacyBroadcastReceiver.ACTION_CLEAR_MISSED_CALLS, null,
                userHandle);
    }

    private PendingIntent createClearHangupPrivateRingCallIntent(
            UserHandle userHandle) {
        return createPrivacyTelecomPendingIntent(
                PrivacyBroadcastReceiver.ACTION_CLEAR_HANGUP_PRIVATE_RINGING_CALLS,
                null, userHandle);
    }

    /**
     * Creates an intent to be invoked when the user opts to "call back" from
     * the missed call notification.
     * 
     * @param handle
     *            The handle to call back.
     */
    // modify by lgy for 3429114 start
    private PendingIntent createCallBackPendingIntent(Uri handle,
            UserHandle userHandle, String subId) {
        return createTelecomPendingIntent(
                TelecomBroadcastIntentProcessor.ACTION_CALL_BACK_FROM_NOTIFICATION,
                handle, userHandle, subId);
    }

    // modify by lgy for 3429114 end

    /**
     * Creates an intent to be invoked when the user opts to "send sms" from the
     * missed call notification.
     */
    private PendingIntent createSendSmsFromNotificationPendingIntent(
            Uri handle, UserHandle userHandle) {
        return createTelecomPendingIntent(
                TelecomBroadcastIntentProcessor.ACTION_SEND_SMS_FROM_NOTIFICATION,
                Uri.fromParts(Constants.SCHEME_SMSTO,
                        handle.getSchemeSpecificPart(), null), userHandle);
    }

    /**
     * Creates generic pending intent from the specified parameters to be
     * received by {@link TelecomBroadcastIntentProcessor}.
     * 
     * @param action
     *            The intent action.
     * @param data
     *            The intent data.
     */
    private PendingIntent createTelecomPendingIntent(String action, Uri data,
            UserHandle userHandle) {
        Intent intent = new Intent(action, data, mContext,
                TelecomBroadcastReceiver.class);
        intent.putExtra(TelecomBroadcastIntentProcessor.EXTRA_USERHANDLE,
                userHandle);
        return PendingIntent.getBroadcast(mContext, 0, intent, 0);
    }

    private PendingIntent createPrivacyTelecomPendingIntent(String action,
            Uri data, UserHandle userHandle) {
        Intent intent = new Intent(action, data, mContext,
                PrivacyBroadcastReceiver.class);
        intent.putExtra(TelecomBroadcastIntentProcessor.EXTRA_USERHANDLE,
                userHandle);
        return PendingIntent.getBroadcast(mContext, 0, intent, 0);
    }

    /**
     * Configures a notification to emit the blinky notification light.
     */
    private void configureLedOnNotification(Notification notification) {
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        // [FEATURE]-Fix-BEGIN by TCTNB.(Chuanjun Chen), 10/15/2016,
        // FEATURE-2640603 And TASk-3003215
        notification.defaults |= Notification.DEFAULT_LIGHTS
                | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
        // [FEATURE]-Add-END by TCTNB.(Chuanjun Chen)
    }

    private boolean canRespondViaSms(Call call) {
        // Only allow respond-via-sms for "tel:" calls.
        return call.getHandle() != null
                && PhoneAccount.SCHEME_TEL.equals(call.getHandle().getScheme());
    }

    /**
     * Adds the missed call notification on startup if there are unread missed
     * calls.
     */
    @Override
    public void reloadFromDatabase(final TelecomSystem.SyncRoot lock,
            final CallsManager callsManager,
            final ContactsAsyncHelper contactsAsyncHelper,
            final CallerInfoAsyncQueryFactory callerInfoAsyncQueryFactory,
            final UserHandle userHandle) {
        Log.d(this, "reloadFromDatabase()...");

        // instantiate query handler
        AsyncQueryHandler queryHandler = new AsyncQueryHandler(
                mContext.getContentResolver()) {
            @Override
            protected void onQueryComplete(int token, Object cookie,
                    Cursor cursor) {
                Log.d(PrivacyMissedCallNotifierImpl.this,
                        "onQueryComplete()...");
                if (cursor != null) {
                    try {
                        mMissedCallCounts.remove(userHandle);
                        while (cursor.moveToNext()) {
                            // Get data about the missed call from the cursor
                            final String handleString = cursor
                                    .getString(CALL_LOG_COLUMN_NUMBER);
                            final int presentation = cursor
                                    .getInt(CALL_LOG_COLUMN_NUMBER_PRESENTATION);
                            final long date = cursor
                                    .getLong(CALL_LOG_COLUMN_DATE);

                            final Uri handle;
                            if (presentation != Calls.PRESENTATION_ALLOWED
                                    || TextUtils.isEmpty(handleString)) {
                                handle = null;
                            } else {
                                handle = Uri
                                        .fromParts(
                                                PhoneNumberUtils
                                                        .isUriNumber(handleString) ? PhoneAccount.SCHEME_SIP
                                                        : PhoneAccount.SCHEME_TEL,
                                                handleString, null);
                            }

                            synchronized (lock) {

                                // Convert the data to a call object
                                Call call = new Call(Call.CALL_ID_UNKNOWN,
                                        mContext, callsManager, lock, null,
                                        contactsAsyncHelper,
                                        callerInfoAsyncQueryFactory, null,
                                        null, null, null,
                                        Call.CALL_DIRECTION_INCOMING, false,
                                        false);
                                call.setDisconnectCause(new DisconnectCause(
                                        DisconnectCause.MISSED));
                                call.setState(CallState.DISCONNECTED,
                                        "throw away call");
                                call.setCreationTimeMillis(date);

                                // add by lgy for 3429114 start
                                call.setTargetPhoneAccount(getPhoneAccountHandle(cursor
                                        .getString(CALL_LOG_COLUMN_PHONE_ACCOUNT_ID)));
                                // add by lgy for 3429114 end

                                // Listen for the update to the caller
                                // information before posting
                                // the notification so that we have the contact
                                // info and photo.
                                call.addListener(new Call.ListenerBase() {
                                    @Override
                                    public void onCallerInfoChanged(Call call) {
                                        call.removeListener(this); // No longer
                                                                   // need to
                                                                   // listen to
                                                                   // call
                                        // changes after the contact info
                                        // is retrieved.
                                        showMissedCallNotification(call,
                                                userHandle);
                                    }
                                });
                                // Set the handle here because that is what
                                // triggers the contact
                                // info query.
                                // [SOLUTION]-Add-BEGIN by TCTNB.(Caixia Chen),
                                // 08/23/2016, SOLUTION-2504488
                                mDitor.putBoolean(isStartThePhone, true)
                                        .commit();
                                // [SOLUTION]-Add-END by TCTNB.(Caixia Chen)
                                call.setHandle(handle, presentation);
                            }
                        }
                    } finally {
                        cursor.close();
                    }
                }
            }
        };

        // setup query spec, look for all Missed calls that are new.
        StringBuilder where = new StringBuilder("(type=");
        where.append(Calls.MISSED_TYPE);
        where.append(" OR type=");
        where.append(TelephonyUtil.MISSED_IMS_TYPE);
        where.append(" OR type=");
        where.append(Calls.MISSED_WIFI_TYPE + ")");
        where.append(" AND new=1");
        where.append(" AND is_read=0");
        where.append(" AND privacy_id = " + PrivacyUtils.getCurrentAccountId());

        Uri callsUri = ContentProvider.maybeAddUserId(Calls.CONTENT_URI,
                userHandle.getIdentifier());
        // start the query
        queryHandler.startQuery(0, null, callsUri, CALL_LOG_PROJECTION,
                where.toString(), null, Calls.DEFAULT_SORT_ORDER);
    }

    @Override
    public void setCurrentUserHandle(UserHandle currentUserHandle) {
        mCurrentUserHandle = currentUserHandle;
    }

    private Context getContextForUser(UserHandle user) {
        try {
            return mContext.createPackageContextAsUser(
                    mContext.getPackageName(), 0, user);
        } catch (NameNotFoundException e) {
            // Default to mContext, not finding the package system is running as
            // is unlikely.
            return mContext;
        }
    }

    // add by lgy for 3429114 start
    private String getSubIdFromCall(Call call) {
        Log.w(this, "getSubIdFromCall subId call = " + call);
        String subId = null;
        if (call.getTargetPhoneAccount() != null) {
            subId = call.getTargetPhoneAccount().getId();
            Log.w(this, "getSubIdFromCall subId = " + subId);
        }
        return subId;
    }

    private PendingIntent createTelecomPendingIntent(String action, Uri data,
            UserHandle userHandle, String subId) {
        Intent intent = new Intent(action, data, mContext,
                TelecomBroadcastReceiver.class);
        intent.putExtra(TelecomBroadcastIntentProcessor.EXTRA_USERHANDLE,
                userHandle);
        if (!TextUtils.isEmpty(subId)) {
            try {
                intent.putExtra("slot",
                        SubscriptionManager.getSlotId(Integer.valueOf(subId)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return PendingIntent.getBroadcast(mContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PhoneAccountHandle getPhoneAccountHandle(String subId) {
        if (TextUtils.isEmpty(subId)) {
            return null;
        }
        TelecomManager telecomManager = TelecomManager.from(mContext);
        List<PhoneAccountHandle> accounts = telecomManager
                .getCallCapablePhoneAccounts();
        for (PhoneAccountHandle account : accounts) {
            if (subId.equals(account.getId())) {
                return account;
            }
        }
        return null;
    }

    // add by lgy for 3429114 end

    void notifyHangupPrivateRingingCallFake(Call call, long privateId) {
        notifyHangupPrivateRingingCallFakeInternal(getUserHandleFromCall(call),
                privateId);
    }

    public void notifyHangupPrivateRingingCallFake(long privateId) {
        notifyHangupPrivateRingingCallFakeInternal(mCurrentUserHandle,
                privateId);
    }

    private void notifyHangupPrivateRingingCallFakeInternal(
            UserHandle userHandle, long privateId) {
        Log.w(this, "notifyHangupPrivateRingingCallFake()");
        final Notification.Builder builder = new Notification.Builder(mContext);
        Drawable largeIcon = mContext.getResources().getDrawable(
                R.drawable.pirvate_noti_fake);
        builder.setLargeIcon(((BitmapDrawable) largeIcon).getBitmap());
        builder.setUsesChronometer(false);
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(true);
        builder.setDeleteIntent(createClearHangupPrivateRingCallIntent(userHandle));
        builder.setContentIntent(createClearHangupPrivateRingCallIntent(userHandle));
        String expandedText;
        expandedText = "";
        builder.setContentTitle(PrivacyDataUtils
                .getPrivateRingNotificationText(privateId));
        builder.setContentText(expandedText);
        Notification notification = builder.build();
        notification.icon = R.drawable.stat_notify_missed_call;
        mNotificationManager.notify(HANGUP_PRIVATE_RINGING_CALL_NOTIFICATION,
                notification);
    }

    void cancelHangupPrivateRingingCallNotification() {
        Log.w(this, "cancelHangupPrivateRingingCallNotification()");
        mNotificationManager.cancel(HANGUP_PRIVATE_RINGING_CALL_NOTIFICATION);
    }

    void cancelAllNotification() {
        cancelMissedCallNotification(mCurrentUserHandle);
        updateNotificationsWhenGoToPrivateMode();
    }

    // 切换到隐私空间时候发送未读的未接来电通知
    protected void updateNotificationsWhenGoToPrivateMode() {
        Log.d(this, "updateNotificationsAtStartup()...");

        if (PrivacyUtils.getCurrentAccountId() <= 0) {
            return;
        }

        TelecomSystem.getInstance().getCallsManager()
                .reloadPrivacyMissedCallsOfUser(mCurrentUserHandle);

    }

    private UserHandle getUserHandleFromCall(Call call) {
        final PhoneAccountHandle phoneAccountHandle = call
                .getTargetPhoneAccount();
        final PhoneAccount phoneAccount = mPhoneAccountRegistrar
                .getPhoneAccountUnchecked(phoneAccountHandle);
        UserHandle userHandle;
        if (phoneAccount != null
                && phoneAccount
                        .hasCapabilities(PhoneAccount.CAPABILITY_MULTI_USER)) {
            userHandle = mCurrentUserHandle;
        } else {
            userHandle = phoneAccountHandle.getUserHandle();
        }
        return userHandle;
    }
}
