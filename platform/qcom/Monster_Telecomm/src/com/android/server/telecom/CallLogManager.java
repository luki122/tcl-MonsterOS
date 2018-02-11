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

package com.android.server.telecom;

import android.annotation.Nullable;
import android.content.Context;
import android.content.Intent;
import android.location.Country;
import android.location.CountryDetector;
import android.location.CountryListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.os.UserHandle;
import android.os.PersistableBundle;
import mst.provider.CallLog.Calls;
import android.telecom.Connection;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.VideoProfile;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;

// TODO: Needed for move to system service: import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.CallerInfo;
import com.mst.interception.InterceptionUtils;

import java.util.Locale;

//[SOLUTION]-Add-BEGIN by TCTNB.(Caixia Chen), 08/12/2016, SOLUTION-2504125
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.text.TextUtils;
//[SOLUTION]-Add-END by TCTNB.(Caixia Chen)

//[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/16/2016, SOLUTION- 2724431 And TASk-2726394
//Porting Framework & Modem----Call related
import android.content.ContentProviderClient;
import android.os.RemoteException;
import android.content.ContentValues;
import android.database.Cursor;
//[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class that provides functionality to write information about calls and their associated
 * caller details to the call log. All logging activity will be performed asynchronously in a
 * background thread to avoid blocking on the main thread.
 */
@VisibleForTesting
public final class CallLogManager extends CallsManagerListenerBase {

    public interface LogCallCompletedListener {
        void onLogCompleted(@Nullable Uri uri);
    }

    /**
     * Parameter object to hold the arguments to add a call in the call log DB.
     */
    private static class AddCallArgs {
        /**
         * @param callerInfo Caller details.
         * @param number The phone number to be logged.
         * @param presentation Number presentation of the phone number to be logged.
         * @param callType The type of call (e.g INCOMING_TYPE). @see
         *     {@link android.provider.CallLog} for the list of values.
         * @param features The features of the call (e.g. FEATURES_VIDEO). @see
         *     {@link android.provider.CallLog} for the list of values.
         * @param creationDate Time when the call was created (milliseconds since epoch).
         * @param durationInMillis Duration of the call (milliseconds).
         * @param dataUsage Data usage in bytes, or null if not applicable.
         * @param logCallCompletedListener optional callback called after the call is logged.
         */
        public AddCallArgs(Context context, CallerInfo callerInfo, String number,
                String postDialDigits, String viaNumber, int presentation, int callType,
                int features, PhoneAccountHandle accountHandle, long creationDate,
                long durationInMillis, Long dataUsage, UserHandle initiatingUser,
                @Nullable LogCallCompletedListener logCallCompletedListener) {
            this.context = context;
            this.callerInfo = callerInfo;
            this.number = number;
            this.postDialDigits = postDialDigits;
            this.viaNumber = viaNumber;
            this.presentation = presentation;
            this.callType = callType;
            this.features = features;
            this.accountHandle = accountHandle;
            this.timestamp = creationDate;
            this.durationInSec = (int)(durationInMillis / 1000);
            this.dataUsage = dataUsage;
            this.initiatingUser = initiatingUser;
            this.logCallCompletedListener = logCallCompletedListener;
        }
        // Since the members are accessed directly, we don't use the
        // mXxxx notation.
        public final Context context;
        public final CallerInfo callerInfo;
        public final String number;
        public final String postDialDigits;
        public final String viaNumber;
        public final int presentation;
        public final int callType;
        public final int features;
        public final PhoneAccountHandle accountHandle;
        public final long timestamp;
        public final int durationInSec;
        public final Long dataUsage;
        public final UserHandle initiatingUser;

        @Nullable
        public final LogCallCompletedListener logCallCompletedListener;
    }

    private static final String TAG = CallLogManager.class.getSimpleName();

    private final Context mContext;
    private final PhoneAccountRegistrar mPhoneAccountRegistrar;
    private final MissedCallNotifier mMissedCallNotifier;
    private static final String ACTION_CALLS_TABLE_ADD_ENTRY =
                "com.android.server.telecom.intent.action.CALLS_ADD_ENTRY";
    private static final String PERMISSION_PROCESS_CALLLOG_INFO =
                "android.permission.PROCESS_CALLLOG_INFO";
    private static final String CALL_TYPE = "callType";
    private static final String CALL_DURATION = "duration";

    //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/16/2016, SOLUTION- 2724431 And TASk-2726394
    //Porting Framework & Modem----Call related
    private static final String SCHEME = "content";
    private static final String AUTHORITY = "com.tct.diagnostics.provider.diagnosticsinfo";
    private static final String TABLE_NAME = "diagnostics";
    private static final Uri CONTENT_URI = new Uri.Builder().scheme(SCHEME).authority(AUTHORITY).path(TABLE_NAME).build();
    //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
    private Object mLock;
    private String mCurrentCountryIso;

    public CallLogManager(Context context, PhoneAccountRegistrar phoneAccountRegistrar,
            MissedCallNotifier missedCallNotifier) {
        mContext = context;
        mPhoneAccountRegistrar = phoneAccountRegistrar;
        mMissedCallNotifier = missedCallNotifier;
        mLock = new Object();
    }

    @Override
    public void onCallStateChanged(Call call, int oldState, int newState) {
        int disconnectCause = call.getDisconnectCause().getCode();
        boolean isNewlyDisconnected =
                newState == CallState.DISCONNECTED || newState == CallState.ABORTED;
        boolean isCallCanceled = isNewlyDisconnected && disconnectCause == DisconnectCause.CANCELED;

        // Log newly disconnected calls only if:
        // 1) It was not in the "choose account" phase when disconnected
        // 2) It is a conference call
        // 3) Call was not explicitly canceled
        if (isNewlyDisconnected &&
                (oldState != CallState.SELECT_PHONE_ACCOUNT &&
                 !call.isConference() &&
                 !isCallCanceled)) {
            int type;
            if (!call.isIncoming()) {
                type = Calls.OUTGOING_TYPE;
            } else if (disconnectCause == DisconnectCause.MISSED) {
                type = Calls.MISSED_TYPE;
            } else {
                type = Calls.INCOMING_TYPE;
            }
            logCall(call, type, true /*showNotificationForMissedCall*/);
            //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/16/2016, SOLUTION- 2724431 And TASk-2726394
            //Porting Framework & Modem----Call related
            recordCall(disconnectCause,type);
            //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
        }
    }

    void logCall(Call call, int type, boolean showNotificationForMissedCall) {
        if (type == Calls.MISSED_TYPE && showNotificationForMissedCall) {
            logCall(call, Calls.MISSED_TYPE,
                    new LogCallCompletedListener() {
                        @Override
                        public void onLogCompleted(@Nullable Uri uri) {
                            mMissedCallNotifier.showMissedCallNotification(call);
                        }
                    });
        } else {
            logCall(call, type, null);
        }
    }

    /**
     * Logs a call to the call log based on the {@link Call} object passed in.
     *
     * @param call The call object being logged
     * @param callLogType The type of call log entry to log this call as. See:
     *     {@link android.provider.CallLog.Calls#INCOMING_TYPE}
     *     {@link android.provider.CallLog.Calls#OUTGOING_TYPE}
     *     {@link android.provider.CallLog.Calls#MISSED_TYPE}
     * @param logCallCompletedListener optional callback called after the call is logged.
     */
    void logCall(Call call, int callLogType,
        @Nullable LogCallCompletedListener logCallCompletedListener) {
        final long creationTime = call.getCreationTimeMillis();
        final long age = call.getAgeMillis();
        //add by lgy
        final long connectedTime = call.getConnectTimeMillis();

        final String logNumber = getLogNumber(call);

        Log.d(TAG, "logNumber set to: %s", Log.pii(logNumber));

        final PhoneAccountHandle emergencyAccountHandle =
                TelephonyUtil.getDefaultEmergencyPhoneAccount().getAccountHandle();

        String formattedViaNumber = PhoneNumberUtils.formatNumber(call.getViaNumber(),
                getCountryIso());
        formattedViaNumber = (formattedViaNumber != null) ?
                formattedViaNumber : call.getViaNumber();

        PhoneAccountHandle accountHandle = call.getTargetPhoneAccount();
        if (emergencyAccountHandle.equals(accountHandle)) {
            accountHandle = null;
        }

        Long callDataUsage = call.getCallDataUsage() == Call.DATA_USAGE_NOT_SET ? null :
                call.getCallDataUsage();

        int callFeatures = getCallFeatures(call.getVideoStateHistory());
        logCall(call.getCallerInfo(), logNumber, call.getPostDialDigits(), formattedViaNumber,
                //modify by lgy for 3445040
//                call.getHandlePresentation(), toPreciseLogType(call, callLogType), callFeatures,
                call.getHandlePresentation(), callLogType, callFeatures,
                accountHandle, age > 0 ? connectedTime :creationTime, age, callDataUsage, call.isEmergencyCall(),
                call.getInitiatingUser(), logCallCompletedListener);
    }

    /**
     * Inserts a call into the call log, based on the parameters passed in.
     *
     * @param callerInfo Caller details.
     * @param number The number the call was made to or from.
     * @param postDialDigits The post-dial digits that were dialed after the number,
     *                       if it was an outgoing call. Otherwise ''.
     * @param presentation
     * @param callType The type of call.
     * @param features The features of the call.
     * @param start The start time of the call, in milliseconds.
     * @param duration The duration of the call, in milliseconds.
     * @param dataUsage The data usage for the call, null if not applicable.
     * @param isEmergency {@code true} if this is an emergency call, {@code false} otherwise.
     * @param logCallCompletedListener optional callback called after the call is logged.
     */
    private void logCall(
            CallerInfo callerInfo,
            String number,
            String postDialDigits,
            String viaNumber,
            int presentation,
            int callType,
            int features,
            PhoneAccountHandle accountHandle,
            long start,
            long duration,
            Long dataUsage,
            boolean isEmergency,
            UserHandle initiatingUser,
            @Nullable LogCallCompletedListener logCallCompletedListener) {

        // On some devices, to avoid accidental redialing of emergency numbers, we *never* log
        // emergency calls to the Call Log.  (This behavior is set on a per-product basis, based
        // on carrier requirements.)
        boolean okToLogEmergencyNumber = false;
        CarrierConfigManager configManager = (CarrierConfigManager) mContext.getSystemService(
                Context.CARRIER_CONFIG_SERVICE);
        PersistableBundle configBundle = configManager.getConfig();
        if (configBundle != null) {
            okToLogEmergencyNumber = configBundle.getBoolean(
                    CarrierConfigManager.KEY_ALLOW_EMERGENCY_NUMBERS_IN_CALL_LOG_BOOL);
        }

        // Don't log emergency numbers if the device doesn't allow it.
        final boolean isOkToLogThisCall = !isEmergency || okToLogEmergencyNumber;

        sendAddCallBroadcast(callType, duration);

        if (isOkToLogThisCall) {
            Log.d(TAG, "Logging Calllog entry: " + callerInfo + ", "
                    + Log.pii(number) + "," + presentation + ", " + callType
                    + ", " + start + ", " + duration);
            AddCallArgs args = new AddCallArgs(mContext, callerInfo, number, postDialDigits,
                    viaNumber, presentation, callType, features, accountHandle, start, duration,
                    dataUsage, initiatingUser, logCallCompletedListener);

            //[SOLUTION]-Add-BEGIN by TCTNB.(Caixia Chen), 08/12/2016, SOLUTION-2504125
            new Thread() {
                @Override
                public void run() {
                    saveCallDurationTime(args);
                }
            }.start();
            //[SOLUTION]-Add-END by TCTNB.(Caixia Chen)
            logCallAsync(args);
        } else {
          Log.d(TAG, "Not adding emergency call to call log.");
        }
    }

    //[SOLUTION]-Add-BEGIN by TCTNB.(Caixia Chen), 08/12/2016, SOLUTION-2504125
    private static final String CALLDURATION_COUNT_FILE_PATH = "/tctpersist/phone/calltimesaver";
    private static final int THREEMINUTESSECONDS = 180;

    private void saveCallDurationTime(AddCallArgs args) {
        // save the time as the following format,for example:1:2:3:4:5 latest:incoming:outgoing:total:date
        long duration = 0;
        duration = args.durationInSec;
        //Log.i(TAG, "duration is " + duration  + ", args.callType = " + args.callType);
        if (duration == 0) {
            //Log.i(TAG, "saveCallDurationTime2 return because duration is 0");
            return;
        }
        FileInputStream in = null;
        FileOutputStream outStream = null;
        try {
            File calltimecount = new File(CALLDURATION_COUNT_FILE_PATH);
            String preTotalTime = "";
            Long preTotalTimelong = 0l;
            String threeMinitesDate = "";
            Long updateTotalTime = 0l;
            long incomingCallDuration = 0L;
            long outgoingCallDuration = 0L;
            if (args.callType == Calls.INCOMING_TYPE) {
                incomingCallDuration = duration;
            } else {
                outgoingCallDuration = duration;
            }
            if (!calltimecount.exists()) {
                calltimecount.createNewFile();
            } else {
                // read origin data from file
                in = new FileInputStream(calltimecount);
                StringBuffer sb = new StringBuffer();
                //int ch = 0;

                byte[] buffer = new byte[1024];
                int len = -1;

                while ((len = in.read(buffer)) != -1) {
                    sb.append(new String(buffer,0,len));
                }

                Log.i(TAG, "saveCallDuration current time = "+ sb.toString());

                final Pattern callTimePattern = Pattern.compile("[0-9:/]*");
                Matcher match = callTimePattern.matcher(sb.toString());
                boolean isCallTimeMatch = match.matches();
                Log.i(TAG, "saveCallDuration is Calltime string = "+ isCallTimeMatch);

                if (!TextUtils.isEmpty(sb.toString()) && isCallTimeMatch) {
                    String[] splites = sb.toString().split(":");
                    incomingCallDuration += Long.parseLong(splites[1]);
                    outgoingCallDuration += Long.parseLong(splites[2]);
                    preTotalTime = splites[3];
                    threeMinitesDate = (splites.length >= 5) ? splites[4] : "";
                } else if (!isCallTimeMatch) {
                    Log.i(TAG, "There may be a mass error");
                }

                int filesize = in.available();
                Log.i(TAG, "saveCallDuration filesize = "+ filesize);
                if (filesize > 1024 * 1024) {
                    Log.i(TAG, "saveCallDuration calltimecount too large, delete then create again");
                    calltimecount.delete();
                    calltimecount.createNewFile();
                    if (!calltimecount.exists()) {
                        Log.i(TAG, "saveCallDuration recreate calltimecount failed");
                        return;
                    }
                }
            }

            if (preTotalTime.equals("")) {
                preTotalTimelong = 0l;
                updateTotalTime = duration;
            } else {
                preTotalTimelong = Long.parseLong(preTotalTime);
                updateTotalTime = preTotalTimelong + duration;
            }

            String writeString = ((Long) duration).toString()
                    + ":"
                    + ((Long) incomingCallDuration).toString()
                    + ":"
                    + ((Long) outgoingCallDuration).toString()
                    + ":"
                    + ((Long) (incomingCallDuration + outgoingCallDuration))
                            .toString();
            //Log.i(TAG, "preTotalTimelong= " + preTotalTimelong + ", updateTotalTime="+updateTotalTime);
            if (preTotalTimelong < THREEMINUTESSECONDS
                    && updateTotalTime < THREEMINUTESSECONDS) {
                //writeString = writeString;
            }
            else if (preTotalTimelong < THREEMINUTESSECONDS
                    && updateTotalTime >= THREEMINUTESSECONDS) {
                Date date = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat ("yyyy/MM/dd");
                writeString = writeString + ":" + formatter.format(date);
            } else {
                writeString = writeString + ":" + threeMinitesDate;
            }

            // write data to file
            outStream = new FileOutputStream(calltimecount);
            outStream.write(writeString.getBytes());
            //Log.i(TAG, "save call time OK , value = " + writeString);
        } catch (Exception ex) {
            Log.i(TAG, "saveCallDuration Exception:"+ ex);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored1) {
                    Log.i(TAG, "Exception Exception:"+ ignored1);
                }
            }
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException ignored2) {
                    Log.i(TAG, "Exception Exception:"+ ignored2);
                }
            }
        }
    }
    //[SOLUTION]-Add-END by TCTNB.(Caixia Chen)

    //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/16/2016, SOLUTION- 2724431 And TASk-2726394
    //Porting Framework & Modem----Call related
    private void recordCall(int disconnectCause,int callType) {
        ContentValues values = new ContentValues();
        values.put("action","ADD");
        boolean isOutGoing = (callType == Calls.OUTGOING_TYPE);
        if (isOutGoing) {
            values.put("PHONE_MO_VOICE",String.valueOf(1));
        } else {
            values.put("PHONE_MT_VOICE",String.valueOf(1));
        }
        switch (disconnectCause) {
            case DisconnectCause.LOCAL:
                values.put("DISCONNECT_LOCAL",String.valueOf(1));
                break;
            case DisconnectCause.REMOTE:
                values.put("DISCONNECT_REMOTE",String.valueOf(1));
                break;
            case DisconnectCause.CANCELED:
                values.put("DISCONNECT_CANCELED",String.valueOf(1));
                break;
            case DisconnectCause.MISSED:
                values.put("DISCONNECT_MISSED",String.valueOf(1));
                break;
            case DisconnectCause.REJECTED:
                values.put("DISCONNECT_REJECTED",String.valueOf(1));
                break;
            case DisconnectCause.BUSY:
                values.put("DISCONNECT_BUSY",String.valueOf(1));
                break;
            case DisconnectCause.RESTRICTED:
                values.put("DISCONNECT_RESTRICTED",String.valueOf(1));
                break;
            case DisconnectCause.ERROR:
                values.put("DISCONNECT_ERROR",String.valueOf(1));
                break;
            case DisconnectCause.UNKNOWN:
                values.put("DISCONNECT_UNKNOWN",String.valueOf(1));
                break;
            case DisconnectCause.OTHER:
                values.put("DISCONNECT_OTHER",String.valueOf(1));
                break;
            default:
                values.put("DISCONNECT_UNKNOWN",String.valueOf(1));
                break;
        }
        ContentProviderClient privider = mContext.getContentResolver().acquireUnstableContentProviderClient(AUTHORITY);
        try {
            if (privider != null) {
                privider.update(CONTENT_URI, values, null, null);
            }
        } catch (IllegalArgumentException e) {
             Log.d("LSH","llegalArgumentException Exception: "+e);
        } catch (RemoteException e) {
             Log.d("LSH","write2DB() RemoteException Exception: "+e);
        } finally {
             if (privider != null) {
                privider.release();
             }
        }
    }
    //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)

    /**
     * Based on the video state of the call, determines the call features applicable for the call.
     *
     * @param videoState The video state.
     * @return The call features.
     */
    private static int getCallFeatures(int videoState) {
        if (VideoProfile.isVideo(videoState)) {
            return Calls.FEATURES_VIDEO;
        }
        return 0;
    }

    /**
     * Retrieve the phone number from the call, and then process it before returning the
     * actual number that is to be logged.
     *
     * @param call The phone connection.
     * @return the phone number to be logged.
     */
    private String getLogNumber(Call call) {
        Uri handle = call.getOriginalHandle();

        if (handle == null) {
            return null;
        }

        String handleString = handle.getSchemeSpecificPart();
        if (!PhoneNumberUtils.isUriNumber(handleString)) {
            handleString = PhoneNumberUtils.stripSeparators(handleString);
        }
        return handleString;
    }

    /**
     * Adds the call defined by the parameters in the provided AddCallArgs to the CallLogProvider
     * using an AsyncTask to avoid blocking the main thread.
     *
     * @param args Prepopulated call details.
     * @return A handle to the AsyncTask that will add the call to the call log asynchronously.
     */
    public AsyncTask<AddCallArgs, Void, Uri[]> logCallAsync(AddCallArgs args) {
        return new LogCallAsyncTask().execute(args);
    }

    /**
     * Helper AsyncTask to access the call logs database asynchronously since database operations
     * can take a long time depending on the system's load. Since it extends AsyncTask, it uses
     * its own thread pool.
     */
    private class LogCallAsyncTask extends AsyncTask<AddCallArgs, Void, Uri[]> {

        private LogCallCompletedListener[] mListeners;

        @Override
        protected Uri[] doInBackground(AddCallArgs... callList) {
            int count = callList.length;
            Uri[] result = new Uri[count];
            mListeners = new LogCallCompletedListener[count];
            for (int i = 0; i < count; i++) {
                AddCallArgs c = callList[i];
                mListeners[i] = c.logCallCompletedListener;
                try {
                    // May block.
                    result[i] = addCall(c);
                    
                    //add by lgy start
                    //handleReject                    
                    boolean isReject = false;
                    ContentValues values = new ContentValues();
                    if (InterceptionUtils.isBlackNumber(mContext, c.number)) {
                        isReject = true;
                        values.put("black_name",
                                InterceptionUtils.getLastBlackName());
                        values.put("reject", 1);
                    }
                    if(isReject) {
                        mContext.getContentResolver().update(result[i],
                                values, null,  null);
                    }
                    
                    if(c.callType == Calls.INCOMING_TYPE && c.durationInSec == 0 && InterceptionUtils.isToAddBlack(mContext, c.number)) {
                        Intent intent = new Intent("com.monster.interception.ACTION_NOTIFY_ADD_BLACK");
                        intent.putExtra("number", c.number);
                        if(c.callerInfo != null && !TextUtils.isEmpty(c.callerInfo.name)) { 
                            intent.putExtra("name", c.callerInfo.name);
                        }
                        mContext.sendBroadcast(intent);
                    }
                    //add by lgy end
                } catch (Exception e) {
                    // This is very rare but may happen in legitimate cases.
                    // E.g. If the phone is encrypted and thus write request fails, it may cause
                    // some kind of Exception (right now it is IllegalArgumentException, but this
                    // might change).
                    //
                    // We don't want to crash the whole process just because of that, so just log
                    // it instead.
                    Log.e(TAG, e, "Exception raised during adding CallLog entry.");
                    result[i] = null;
                }
            }
            return result;
        }

        private Uri addCall(AddCallArgs c) {
            PhoneAccount phoneAccount = mPhoneAccountRegistrar
                    .getPhoneAccountUnchecked(c.accountHandle);
            if (phoneAccount != null &&
                    phoneAccount.hasCapabilities(PhoneAccount.CAPABILITY_MULTI_USER)) {
                if (c.initiatingUser != null &&
                        UserUtil.isManagedProfile(mContext, c.initiatingUser)) {
                    return addCall(c, c.initiatingUser);
                } else {
                    return addCall(c, null);
                }
            } else {
                return addCall(c, c.accountHandle == null ? null : c.accountHandle.getUserHandle());
            }
        }

        /**
         * Insert the call to a specific user or all users except managed profile.
         * @param c context
         * @param userToBeInserted user handle of user that the call going be inserted to. null
         *                         if insert to all users except managed profile.
         */
        private Uri addCall(AddCallArgs c, UserHandle userToBeInserted) {
            return Calls.addCall(c.callerInfo, c.context, c.number, c.postDialDigits, c.viaNumber,
                    c.presentation, c.callType, c.features, c.accountHandle, c.timestamp,
                    c.durationInSec, c.dataUsage, userToBeInserted == null,
                    userToBeInserted);
        }


        @Override
        protected void onPostExecute(Uri[] result) {
            for (int i = 0; i < result.length; i++) {
                Uri uri = result[i];
                /*
                 Performs a simple sanity check to make sure the call was written in the database.
                 Typically there is only one result per call so it is easy to identify which one
                 failed.
                 */
                if (uri == null) {
                    Log.w(TAG, "Failed to write call to the log.");
                }
                if (mListeners[i] != null) {
                    mListeners[i].onLogCompleted(uri);
                }
            }
        }
    }

    private void sendAddCallBroadcast(int callType, long duration) {
        Intent callAddIntent = new Intent(ACTION_CALLS_TABLE_ADD_ENTRY);
        callAddIntent.putExtra(CALL_TYPE, callType);
        callAddIntent.putExtra(CALL_DURATION, duration);
        mContext.sendBroadcast(callAddIntent, PERMISSION_PROCESS_CALLLOG_INFO);
    }

    private String getCountryIsoFromCountry(Country country) {
        if(country == null) {
            // Fallback to Locale if there are issues with CountryDetector
            Log.w(TAG, "Value for country was null. Falling back to Locale.");
            return Locale.getDefault().getCountry();
        }

        return country.getCountryIso();
    }

    /**
     * Get the current country code
     *
     * @return the ISO 3166-1 two letters country code of current country.
     */
    public String getCountryIso() {
        synchronized (mLock) {
            if (mCurrentCountryIso == null) {
                Log.i(TAG, "Country cache is null. Detecting Country and Setting Cache...");
                final CountryDetector countryDetector =
                        (CountryDetector) mContext.getSystemService(Context.COUNTRY_DETECTOR);
                Country country = null;
                if (countryDetector != null) {
                    country = countryDetector.detectCountry();

                    countryDetector.addCountryListener((newCountry) -> {
                        Log.startSession("CLM.oCD");
                        try {
                            synchronized (mLock) {
                                Log.i(TAG, "Country ISO changed. Retrieving new ISO...");
                                mCurrentCountryIso = getCountryIsoFromCountry(newCountry);
                            }
                        } finally {
                            Log.endSession();
                        }
                    }, Looper.getMainLooper());
                }
                mCurrentCountryIso = getCountryIsoFromCountry(country);
            }
            return mCurrentCountryIso;
        }
    }

    private int toPreciseLogType(Call call, int callLogType) {
        final boolean isHighDefAudioCall =
               (call != null) && call.hasProperty(Connection.PROPERTY_HIGH_DEF_AUDIO);
        final boolean isWifiCall =
               (call != null) && call.hasProperty(Connection.PROPERTY_WIFI);
        Log.d(TAG, "callProperties: " + call.getConnectionProperties()
                + "isHighDefAudioCall: " + isHighDefAudioCall
                + "isWifiCall: " + isWifiCall);
        if(!isHighDefAudioCall && !isWifiCall) {
            return callLogType;
        }
        switch (callLogType) {
            case Calls.INCOMING_TYPE :
                if(isWifiCall) {
                    callLogType = Calls.INCOMING_WIFI_TYPE;
                } else {
                    callLogType = TelephonyUtil.INCOMING_IMS_TYPE;
                }
                break;
            case Calls.OUTGOING_TYPE :
                if(isWifiCall) {
                    callLogType = Calls.OUTGOING_WIFI_TYPE;
                } else {
                    callLogType = TelephonyUtil.OUTGOING_IMS_TYPE;
                }
                break;
            case Calls.MISSED_TYPE :
                if(isWifiCall) {
                    callLogType = Calls.MISSED_WIFI_TYPE;
                } else {
                    callLogType = TelephonyUtil.MISSED_IMS_TYPE;
                }
                break;
            default:
                //Normal cs call, no change
        }
        return callLogType;
    }
}
