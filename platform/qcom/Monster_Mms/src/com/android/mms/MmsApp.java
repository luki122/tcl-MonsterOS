/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.mms;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.drm.DrmManagerClient;
import android.location.Country;
import android.location.CountryDetector;
import android.location.CountryListener;
import android.content.pm.PackageManager;
import android.content.ComponentName;
import android.net.Uri;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.mms.data.Contact;
import com.android.mms.data.Conversation;
import com.android.mms.layout.LayoutManager;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.transaction.MmsNoConfirmationSendActivity;
import com.android.mms.transaction.MmsSystemEventReceiver;
import com.android.mms.transaction.SmsReceiver;
import com.android.mms.transaction.SmsReceiverService;
import com.android.mms.ui.MessageUtils;
import com.android.mms.util.DownloadManager;
import com.android.mms.util.DraftCache;
import com.android.mms.util.PduLoaderManager;
import com.android.mms.util.RateController;
import com.android.mms.util.ThumbnailManager;
//lichao add for tencent service begin
import com.mst.tms.TmsServiceManager;
//lichao add for tencent service end
//XiaoYuan SDK start
import cn.com.xy.sms.sdk.ui.popu.util.XySdkUtil;
import com.xy.smartsms.manager.XySdkAction;
//XiaoYuan SDK end

public class MmsApp extends Application {
    public static final String LOG_TAG = LogTag.TAG;
    
    /**XiaoYuan SDK start********************************************************************/
    public static final String DUOQU_SDK_CHANNEL ="XHpWJNFQTCLOS";//rahtBH7wTCL
    //private static final String DUOQU_SDK_CHANNEL_SECRETKEY="MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAJrN7Y2Y5wvlKrztDhY2N80OMBjDU9SdGOD617kdnIhCkUwZ79r+1A/ygiuQ2Ryk9Cr+UpdTvHaz9ccseWlno5CJqeiPyj56aypQ3QbvWmWvo4PzFj/+8Envo/5CXmyZ22ezfgDJlCrBOlrP6l92ji3W476+frfNVupqmtqG0EfvAgMBAAECgYACSfMuOcmIwn2sR3EC4RBYJtKNOy4dIamBghP4d7idxyYw0t1aBpSKG9LS5BRhuQqnLBV6iVFrC+QhasXsygyAC56Ql910tKyOqyX1jbeqrSnX8TqjT3dXCC/YH1B4mESvRI7jka/qGJEYMbjrXXQAhQcLinXbFIKzHaGEl2Ju4QJBAO1nYaKHGNmbRKsMWb226BFbkzOtfxr8QVWMnViOJiQazsAxClq3Warl02JDInaW6+edGkBngN4yT2bN+7FJU/8CQQCm7jG+fNqQXOJKgPhPK3HGTO6dcIliEG87i0a3dfJAk4sl+9KdfxYth1ENuOp5ZW6TMEzpgtx8A642nxtb/kwRAkAhH6OHwcG92uQh2X9L8RFAGr7XHwX1Be03un7ZtDuuHe9q8Wy4a4yfQ6HSu/s7AKO5lTnscoSQfASIG4VanxiRAkA1nehIYN3q5Iqil44qD7A5m1hBJXZbEyBHJdMO9klbSIi3KI1bnQhIk2ALYkudtmCv0iHCFxAunRgF31DwWVuhAkEA6Pkq1c573YcKqY9Dd4P2PtjQMXNAirSoihnMeYL7u2bCJvXDhof2yil5bpaQx9pUHmbLrkO9NlwQZKcDTizVUw==";
    private static final String DUOQU_SDK_CHANNEL_SECRETKEY= "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAN0W8Iz5eU25EsE0krVnjbdKAcWCstvtXrAaj4RKoXJnTRSgWg512tSNiRJLPfb45Hai4txNJQMpCMDGzV0zasXua4S3QDIdt5j36N08tnHuXyY7tZGGxFiYVApMVkLLod8oacpeG2scwEglG6P2182Iz9v5tzOhOFqQZcelYk0VAgMBAAECgYB0YDvU6QiqGc4+du72oACfx0vxbF+7llHuPrzmtOYQs0GGpvVggTbkBXBueb6vPsn7jLf/oHfoFvRtjPfSpFsBxIBZ27fX46Qt7MCKeHXWkyEWjklSGCw8iDxhfAAMUfkWEhfNIbyqJ70uo1Gg2Dz5piTre7POcTsU89BZXSWzbQJBAPLZiE148Xg0AhC5C2lCpEI00vytGqh9C+hhQFrh6U3Wgfzho1h6vpIMzXgs+AHcVto3U9nzTbyTQTqcGuclRhsCQQDpD8LAhDUQ9/mnak1oJLIruAfAEbclDpN8p9E0mFxqQmzTrws75+ajJoug7kFCfsrwPLkCMrTTXXacD9LUwqyPAkByOwaVQn16yCPsj84hThqLleNkvVwbwu4V+aDW3wrp9SuhstUt6la3xlPj4msqHOWxsXK8w2heenmlQwaYkXzfAkEAmVtZlzxnfsbtbBAy5zheVQ4/a0886BFzUy9KJgWrqfATlCc2iTDLPsf6UOb14j90YihSxPOAsSRrC74NyRpgWQJABhjfsZY/RGjLMjC5OGj/AZ/sW9k/evlz/vmxLPge4LAiVgRWqGduES4bPtyaT28s8fz0yh6rwXqsGam++fUwog==";
    /**XiaoYuan SDK end********************************************************************/

    private SearchRecentSuggestions mRecentSuggestions;
    private TelephonyManager mTelephonyManager;
    private CountryDetector mCountryDetector;
    private CountryListener mCountryListener;
    private String mCountryIso;
    private static MmsApp sMmsApp = null;
    private PduLoaderManager mPduLoaderManager;
    private ThumbnailManager mThumbnailManager;
    private DrmManagerClient mDrmManagerClient;
    
    //add by lgy
    public final static boolean isCreateConversaitonIdBySim = true;

    //lichao add for tencent service begin
    public final static boolean isUseTencetService = true;
	//lichao add for tencent service end

    @Override
    public void onCreate() {
        super.onCreate();

        if (Log.isLoggable(LogTag.STRICT_MODE_TAG, Log.DEBUG)) {
            // Log tag for enabling/disabling StrictMode violation log. This will dump a stack
            // in the log that shows the StrictMode violator.
            // To enable: adb shell setprop log.tag.Mms:strictmode DEBUG
            StrictMode.setThreadPolicy(
                    new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
        }

        sMmsApp = this;

        // Load the default preference values
        if (getResources().getBoolean(R.bool.def_custom_preferences_settings)) {
            PreferenceManager.setDefaultValues(this, R.xml.custom_preferences,
                    false);
        } else {
            PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        }

        // Figure out the country *before* loading contacts and formatting numbers
        mCountryDetector = (CountryDetector) getSystemService(Context.COUNTRY_DETECTOR);
        mCountryListener = new CountryListener() {
            @Override
            public synchronized void onCountryDetected(Country country) {
                mCountryIso = country.getCountryIso();
            }
        };
        mCountryDetector.addCountryListener(mCountryListener, getMainLooper());

        //lichao add "final" for tencent service
        final Context context = getApplicationContext();
        mPduLoaderManager = new PduLoaderManager(context);
        mThumbnailManager = new ThumbnailManager(context);

        MmsConfig.init(this);
        Contact.init(this);
        DraftCache.init(this);
        Conversation.init(this);
        DownloadManager.init(this);
        RateController.init(this);
        LayoutManager.init(this);
        MessagingNotification.init(this);

        activePendingMessages();
		
        /**XiaoYuan SDK start********************************************************************/
        XySdkUtil.init(context, DUOQU_SDK_CHANNEL, DUOQU_SDK_CHANNEL_SECRETKEY, new XySdkAction());
        /**XiaoYuan SDK end********************************************************************/
		
        registerMobileDataObserver();

        int enablePlugger = getResources().getBoolean(R.bool.enablePlugger) ?
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                : PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
        PackageManager pm_plugger = getPackageManager();
        pm_plugger.setComponentEnabledSetting(new ComponentName(this,
                MmsNoConfirmationSendActivity.class), enablePlugger,
                PackageManager.DONT_KILL_APP);

        //lichao add for tencent service in 2016-08-18 begin
        if (isUseTencetService) {
            if(null == TmsServiceManager.getInstance()){
                TmsServiceManager.getInstance(context);
            }
            if(null != TmsServiceManager.getInstance()
                    && false == TmsServiceManager.mIsServiceConnected ){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TmsServiceManager.getInstance().bindService();
                    }
                }).start();
            }
        }
        //lichao add for tencent service in 2016-08-18 end
    }

    private void registerMobileDataObserver() {
        int phoneCount = TelephonyManager.getDefault().getPhoneCount();
        final int MOBILE_DATA_ON = 1;

        for( int i =0; i < phoneCount; i++) {
            Uri uri = Settings.Global.getUriFor(Settings.Global.MOBILE_DATA + i);
            getContentResolver().registerContentObserver(uri, false, new ContentObserver(null) {
                @Override
                public void onChange(boolean selfChange, Uri uri) {
                     Log.d(LOG_TAG, "MobileData UI changed = " + uri);
                     String uriLastSegment = uri.getLastPathSegment();
                     int uriLength = uriLastSegment.length();
                     int phoneId = Character.getNumericValue(uriLastSegment.charAt(uriLength - 1));

                     try {
                         int value = Settings.Global.getInt(getContentResolver(),
                                 Settings.Global.MOBILE_DATA + phoneId);
                         Log.d(LOG_TAG, "value = " + value);
                         if (value == MOBILE_DATA_ON) {
                             if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                                 Log.d(LOG_TAG,
                                         "MobileData turned ON, trigger pending mms processing");
                             }
                             activePendingMessages();
                         }
                     } catch (SettingNotFoundException e) {
                         Log.e(LOG_TAG, "Exception in getting MobileData UI value. e = " + e);
                     }
                }
            });
        }
    }

    /**
     * Try to process all pending messages(which were interrupted by user, OOM, Mms crashing,
     * etc...) when Mms app is (re)launched.
     */
    private void activePendingMessages() {
        // For Mms: try to process all pending transactions if possible
        MmsSystemEventReceiver.wakeUpService(this);

        // For Sms: retry to send smses in outbox and queued box
        sendBroadcast(new Intent(SmsReceiverService.ACTION_SEND_INACTIVE_MESSAGE,
                null,
                this,
                SmsReceiver.class));
    }

    synchronized public static MmsApp getApplication() {
        return sMmsApp;
    }

    @Override
    public void onTerminate() {
        mCountryDetector.removeCountryListener(mCountryListener);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        mPduLoaderManager.onLowMemory();
        mThumbnailManager.onLowMemory();
    }

    public PduLoaderManager getPduLoaderManager() {
        return mPduLoaderManager;
    }

    public ThumbnailManager getThumbnailManager() {
        return mThumbnailManager;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        LayoutManager.getInstance().onConfigurationChanged(newConfig);
    }

    /**
     * @return Returns the TelephonyManager.
     */
    public TelephonyManager getTelephonyManager() {
        if (mTelephonyManager == null) {
            mTelephonyManager = (TelephonyManager)getApplicationContext()
                    .getSystemService(Context.TELEPHONY_SERVICE);
        }
        return mTelephonyManager;
    }

    /**
     * Returns the content provider wrapper that allows access to recent searches.
     * @return Returns the content provider wrapper that allows access to recent searches.
     */
    public SearchRecentSuggestions getRecentSuggestions() {
        /*
        if (mRecentSuggestions == null) {
            mRecentSuggestions = new SearchRecentSuggestions(this,
                    SuggestionsProvider.AUTHORITY, SuggestionsProvider.MODE);
        }
        */
        return mRecentSuggestions;
    }

    // This function CAN return null.
    public String getCurrentCountryIso() {
        if (mCountryIso == null) {
            Country country = mCountryDetector.detectCountry();
            if (country != null) {
                mCountryIso = country.getCountryIso();
            }
        }
        return mCountryIso;
    }

    public DrmManagerClient getDrmManagerClient() {
        if (mDrmManagerClient == null) {
            mDrmManagerClient = new DrmManagerClient(getApplicationContext());
        }
        return mDrmManagerClient;
    }

}
