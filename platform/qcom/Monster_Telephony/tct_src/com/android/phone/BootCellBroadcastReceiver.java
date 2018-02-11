package com.android.phone;

/******************************************************************************/
/*                                                               Date:05/2014 */
/*                                PRESENTATION                                */
/*                                                                            */
/*       Copyright 2014 TCL Communication Technology Holdings Limited.        */
/*                                                                            */
/* This material is company confidential, cannot be reproduced in any form    */
/* without the written permission of TCL Communication Technology Holdings    */
/* Limited.                                                                   */
/*                                                                            */
/* -------------------------------------------------------------------------- */
/*  Author :  xueyong.zhang                                                   */
/*  Email  :  xueyong.zhang@jrdcom.com                                        */
/*  Role   :                                                                  */
/*  Reference documents :                                                     */
/* -------------------------------------------------------------------------- */
/*  Comments :                                                                */
/*  File     :                                                                */
/*  Labels   :                                                                */
/* -------------------------------------------------------------------------- */
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/*    date   |        author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 09/22/2015|      wei.li          |    Task-527312       |Need to add def_- */
/*           |                      |                      |is_first_read_si- */
/*           |                      |                      |m_voice_mail_num- */
/*           |                      |                      |ber for russia    */
/* ----------|----------------------|----------------------|----------------- */
/* 09/25/2015|       wei.li         |   Task-528519        |[ALWE] Need a CLID */
/*           |                      |                      |variable to force  */
/*           |                      |                      |Voicemail number - */
/*           |                      |                      |for NL             */
/* ----------|----------------------|----------------------|----------------- */
/* 08/04/2016|     Fuqiang.Song     |       2670355        |For Russian Beel- */
/*           |                      |                      |ine, Notify user  */
/*           |                      |                      |when preferred n- */
/*           |                      |                      |etwork mode is L- */
/*           |                      |                      |te Only.          */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import mst.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.util.TctLog;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyIntents;
import android.telephony.TelephonyManager;
import android.os.SystemProperties; //[BUGFIX]-Add-BEGIN by NJTS-wei.huang,for SSV Idol4s,10/10/2015
import com.android.internal.telephony.PhoneFactory;
import com.android.phone.BestAccessMobileDataRevicer; // MODIFIED by bo.chen, 2016-09-07,BUG-2852672
import android.os.UserHandle;
import com.android.internal.telephony.IccCardConstants;//[FEATURE]-ADD by TCTNB.(Huan_Liu),01/29/2016,Defect:1245549 ,
import android.provider.Settings.SettingNotFoundException;
import com.android.internal.telephony.PhoneConstants;

import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.RILConstants;


import android.database.ContentObserver;
import android.net.Uri;
import android.provider.Settings;
import android.telephony.SubscriptionManager;


public class BootCellBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "Phone.BootCellBroadcastReceiver";
    private static final String MCC_FOR_NL = "204";//[TASK]-Add by TSNJ.wei.li,09/23/2015,528519

    //[BUGFIX]-Add-BEGIN by NJTS-wei.huang,for SSV Idol4s,10/10/2015
    private static final String DATA_ROAMING_ENBELED_KEY="data_roaming_enabled_by_default_key";
    private static final int MODE_DISABLE_ROAMING=0;
    private static final int MODE_ALL_NETWORK_ROAMING=1;
    private static final int MODE_NATIONAL_ROAMING =2;
    //[BUGFIX]-Add-END by NJTS-wei.huang,for SSV Idol4s,10/10/2015
    SharedPreferences mDefaultDataRoamingPrefereces; //Merged by rain.zhang for Idol4 Task528478, 2015/10/28

    ContentObserver observer = null;
    boolean lteOnlyDialog = false;

    @Override
    public void onReceive(Context context,Intent intent){
        //[BUGFIX]-Add-BEGIN by TCTNJ.zhujian.shao,1/12/2016,defect-1312286
        // Return if not a primary user
        if (UserHandle.myUserId() != UserHandle.USER_OWNER) {
            TctLog.i(TAG, " now is not user owner ,myUserId = "+ UserHandle.myUserId());
            return;
        }
        //[BUGFIX]-Add-END by TCTNJ.zhujian.shao,1/12/2016,defect-1312286
        String action = intent.getAction();
        mDefaultDataRoamingPrefereces = PreferenceManager.getDefaultSharedPreferences(context); //Merged by rain.zhang for Idol4 Task528478, 2015/10/28
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)){
          //[BUGFIX]-Add-BEGIN by TCTNJ.zhujian.shao,1/13/2016,defect-1394251

            Log.d(TAG, " ACTION_BOOT_COMPLETED");

            //When boot completes, display Lte only notifycation if preferred network mode is LTE only,
            // otherwise do not display.
            lteOnlyDialog = context.getResources().getBoolean(R.bool.feature_phone_lteOnlyDialogForBeeline_on);
            if (lteOnlyDialog == true) {
                updateLteOnlyNotification();
            }





          //[BUGFIX]-Add-END by TCTNJ.zhujian.shao,1/13/2016,defect-1394251
            boolean isSimVoiceMailHightPritory = context.getResources().getBoolean(R.bool.def_Phone_isFirstReadSimVoiceMailNumber);
            TctLog.i(TAG, " isSimVoiceMailHightPritory:"+isSimVoiceMailHightPritory);
            if(isSimVoiceMailHightPritory){
                return;
            }
            String vmNumber = context.getResources().getString(R.string.def_phone_voice_mail_number);
            String preNumber = PhoneNumberUtils.extractNetworkPortion(PhoneGlobals.getPhone().getVoiceMailNumber());
            TctLog.i(TAG, " isSimVoiceMailHightPritory:"+isSimVoiceMailHightPritory+" vmNumber:"+vmNumber+" preNumber:"+preNumber);
            if (!TextUtils.isEmpty(vmNumber) && !vmNumber.equals("0")) {
                PhoneGlobals.getPhone().setVoiceMailNumber(PhoneGlobals.getPhone().getVoiceMailAlphaTag(),
                        vmNumber, null);
            }
            /* MODIFIED-BEGIN by bo.chen, 2016-09-07,BUG-2852672*/
            boolean isTurnOnBestNetwork = context.getResources().getBoolean(com.android.internal.R.bool.def_tctfw_isTurnOnBestNetworkAcess);
            if (isTurnOnBestNetwork) {
                TctLog.i(TAG, " new BestAccessMobileDataRevicer");
                BestAccessMobileDataRevicer mAccessMobileDataRevicer = BestAccessMobileDataRevicer.getInstance(context);
            }
        //[TASK]-Add-BEGIN by TSNJ.wei.li,09/25/2015,528519
        } else if (TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(action)) {
            TctLog.i(TAG, " ACTION_SIM_STATE_CHANGED");
            boolean isTurnOnBestNetwork = context.getResources().getBoolean(com.android.internal.R.bool.def_tctfw_isTurnOnBestNetworkAcess);
            if (isTurnOnBestNetwork) {
                TctLog.i(TAG, " new BestAccessMobileDataRevicer sim state change");
                BestAccessMobileDataRevicer mAccessMobileDataRevicer = BestAccessMobileDataRevicer.getInstance(context);
            }
            /* MODIFIED-END by bo.chen,BUG-2852672*/
            //[BUGFIX]-ADD-BEGIN By TCTNB.Haiping.LE, 2016-02-01 Defect 1532625
            TctLog.i(TAG, "start LongRunningService");
            intent.setClass(context, LongRunningService.class);
            context.startService(intent);

            //When Hotplug USIM or While USIM is present, observe perferred network mode,
            //display Lte only notifycation if preferred network mode is LTE only, otherwise do not display.
            lteOnlyDialog = context.getResources().getBoolean(R.bool.feature_phone_lteOnlyDialogForBeeline_on);

            if (lteOnlyDialog == true) {

                String simState = IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
                simState = intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);

                if (null == observer )
                    observer = new PreferNetworkObserver();

                if (IccCardConstants.INTENT_VALUE_ICC_IMSI.equals(simState)) {
                    int subId = intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY, SubscriptionManager.MAX_SUBSCRIPTION_ID_VALUE);
                    Uri uri = Settings.Global.getUriFor(Settings.Global.PREFERRED_NETWORK_MODE + subId);
                    Log.d(TAG,"ICC is IMSI State,register for uri "+ uri);
                    context.getContentResolver().registerContentObserver(uri, false, observer);
                }

                if (IccCardConstants.INTENT_VALUE_ICC_ABSENT.equals(simState)) {
                    int subId = intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY, SubscriptionManager.MAX_SUBSCRIPTION_ID_VALUE);
                    Uri uri = Settings.Global.getUriFor(Settings.Global.PREFERRED_NETWORK_MODE + subId);
                    Log.d(TAG,"ICC is absent,unregister for uri "+ uri);
                    context.getContentResolver().unregisterContentObserver(observer);
                }

            }

        } else if("com.jrdcom.ssv.action.LOAD_RESOURCES".equals(action)) {
            String newMccMnc=intent.getStringExtra("current_mccmnc");
            setDefaultDataRoamingByOperator(context,newMccMnc);
            //[BUGFIX]-Add-END by NJTS-wei.huang,for SSV Idol4s,10/10/2015
        }
        //[TASK]-Add-END by TSNJ.wei.li,09/25/2015,528519

   }

    //Merged by rain.zhang for Idol4 Task528478, 2015/10/28 END
    //[BUGFIX]-DELETE-END By TCTNB.Haiping.LE
    //[BUGFIX]-Add-BEGIN by NJTS-wei.huang,for SSV Idol4s,10/10/2015
    private String getVoicemailNumberForSSV(Context context,String mccmncNumber,String operator){
        String vmNumber = null;
        vmNumber = context.getResources().getString(R.string.def_phone_ssv_voicemailNumber);
        return vmNumber;
    }

    private void setDefaultDataRoamingByOperator(Context context,String newMccMnc){
        boolean isDataroamingOn=context.getResources().getBoolean(R.bool.def_phone_ssv_DataRoamingOn);
        Phone phone=PhoneGlobals.getPhone();
        if (isDataroamingOn) {
            phone.setDataRoamingEnabled(true);
            TctLog.i("BootCellBroadcastReceiver","ssv:enable data roaming by default for mccmnc:"+newMccMnc);
            android.provider.Settings.Secure.putInt(
                    phone.getContext().getContentResolver(),
                    android.provider.Settings.Secure.DATA_ROAMING, MODE_ALL_NETWORK_ROAMING);
                        /*mDefaultDataRoamingPrefereces.edit()
                                . putBoolean(DATA_ROAMING_ENBELED_KEY+mccmnc,true)
                                 .commit();*/
        }else{
            TctLog.i("BootCellBroadcastReceiver","ssv:disable data roaming by default for mccmnc: "+newMccMnc);
            phone.setDataRoamingEnabled(false);
            android.provider.Settings.Secure.putInt(
                    phone.getContext().getContentResolver(),
                    android.provider.Settings.Secure.DATA_ROAMING, MODE_DISABLE_ROAMING);
                        /*mDefaultDataRoamingPrefereces.edit()
                                . putBoolean(DATA_ROAMING_ENBELED_KEY+mccmnc,false)
                                 .commit();*/
        }
    }
    //[BUGFIX]-Add-END by NJTS-wei.huang,for SSV Idol4s,10/10/2015

    /**Update LTE only notification when power on,hot pulg-in  ICC.
            1. When power on with or hot plug in the last ICCs, if one of the preferred network mode is LTE only,
                display LTE only notification .otherwise do not display.
             2. When power on with  or hot plug in new ICC, disappear notification after setting the  preferred network mode;
    **/
    private void updateLteOnlyNotification () {
        PhoneGlobals app = PhoneGlobals.getInstance();
        for (int phoneId = 0; phoneId < TelephonyManager.getDefault().getPhoneCount(); phoneId++) {
            int phoneIdNetworkType = RILConstants.PREFERRED_NETWORK_MODE;
            Phone phone = PhoneFactory.getPhone(phoneId);
            int phoneSubId = phone.getSubId();
            boolean isSubActive = SubscriptionController.getInstance().isActiveSubId(phoneSubId);
            if (isSubActive) {
                try {
                    phoneIdNetworkType =
                    TelephonyManager.getIntAtIndex(phone.getContext().getContentResolver(),
                                                    Settings.Global.PREFERRED_NETWORK_MODE , phoneId);
                } catch (SettingNotFoundException snfe) {
                    Log.d(TAG, "Settings Exception Reading Valuefor phoneID " + phoneId);
                }

                int networkType = phoneIdNetworkType;
                Log.d(TAG, "phoneId = " + phoneId + " phoneIdNetworkType = " + phoneIdNetworkType + ",SubId = "+phoneSubId);

                networkType = Settings.Global.getInt(phone.getContext().getContentResolver(),
                                                    Settings.Global.PREFERRED_NETWORK_MODE + phoneSubId,
                                                     phoneIdNetworkType);

                Log.d(TAG, "phoneSubId = " + phoneSubId +" networkType = " + networkType);
                if (networkType == Phone.NT_MODE_LTE_ONLY) {
                    app.notificationMgr.updateLteOnlyIcon(true);
                    break;
                }
            }
            if (phoneId == TelephonyManager.getDefault().getPhoneCount()-1)
                app.notificationMgr.updateLteOnlyIcon(false);
        }

    }

     /**When Hotplug in or power on with new ICC,observer its network mode,update LTE only
             notification when the preferred network mode has been set **/
    private class PreferNetworkObserver extends ContentObserver {
        private String TAG = "PreferNetworkObserver";
        private boolean DBG = true;
        private int mPhoneCount = TelephonyManager.getDefault().getPhoneCount();
        final PhoneGlobals app = PhoneGlobals.getInstance();
        PreferNetworkObserver() {
            super(null);
        }
        public synchronized void  onChange(boolean selfChange, Uri uri) {
            if (uri != null) {
                String authority = uri.getAuthority();
                String uriLastSegment = uri.getLastPathSegment();
                int phoneId = 0;
                int subId = 0;
                if (authority.equals("settings")) {
                    if (mPhoneCount > 1) {
                        String[] lastSegmentParts = uriLastSegment.split("\\d");
                        int uriLength = uriLastSegment.length();
                        int keyLength = lastSegmentParts[0].length();
                        subId = Integer.parseInt(uriLastSegment.substring(keyLength, uriLength));
                        uriLastSegment = uriLastSegment.substring(0, keyLength);
                        if (DBG)
                            Log.d(TAG, "MultiSim onChange(): subId = " + subId);
                        if (DBG)
                            Log.d(TAG, "onChange():uri=" + uri.toString() + " authority=" + authority
                                    + " path=" + uri.getPath() + " segments=" + uri.getPathSegments()
                                    + " uriLastSegment=" + uriLastSegment);
                        switch (uriLastSegment) {
                            case Settings.Global.PREFERRED_NETWORK_MODE:
                                updateLteOnlyNotification ();
                                break;

                            default:
                                Log.e(TAG, "Received unsupported uri");
                        }
                    } else {
                        Log.d(TAG,"Single SIM");
                    }
                } else {
                    Log.d(TAG,"Authority is "+authority);
                }
            }else {
                Log.d(TAG,"URI is null.");
            }
        }
    }

}


