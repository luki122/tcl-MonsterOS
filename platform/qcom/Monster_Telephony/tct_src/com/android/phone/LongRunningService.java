package com.android.phone;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.TelephonyIntents;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import mst.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TctLog;
import com.android.internal.telephony.IccCardConstants;//[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/31/2016, SOLUTION- 2499549 And TASk-2781362
public class LongRunningService extends IntentService{

    public LongRunningService(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }
    //[BUGFIX]-ADD-BEGIN By TCTNB.Haiping.Le 2016.02.03,For Defect-1532625
    //Add Non-Argument constructor to avoid FC
    public LongRunningService() {
        super("LongRunningService");
    }
    //[BUGFIX]-ADD-END By TCTNB.Haiping.Le
    private static final String TAG = "LongRunningService";
    private static final String MCC_FOR_NL= "204";
    SharedPreferences mDefaultDataRoamingPrefereces;
    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO Auto-generated method stub
        String action = intent.getAction();
        mDefaultDataRoamingPrefereces = PreferenceManager.getDefaultSharedPreferences(this);
        if (TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(action)){
            //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/31/2016, SOLUTION- 2499549 And TASk-2781362
            String stateExtra = intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);
            if (IccCardConstants.INTENT_VALUE_ICC_ABSENT.equals(stateExtra)) {
                ActivityCollector.finishAllActivity();
            }
            //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
            final Phone mPhone = PhoneUtils.getPhoneFromIntent(intent);
            int subId = PhoneUtils.getSubIdFromIntent(intent);
            Phone mphone = PhoneUtils.getPhoneFromSubId(subId);
            String preNumber = PhoneNumberUtils.extractNetworkPortion(mphone.getVoiceMailNumber());
            String vmNumber = getResources().getString(R.string.def_phone_voice_mail_number);
            boolean isGetVoiceMailFromPhone = getResources().getBoolean(R.bool.def_phone_is_getVm_fromPhone);
            boolean isSimVoiceMailHightPritory = getResources().getBoolean(R.bool.def_Phone_isFirstReadSimVoiceMailNumber);
            boolean isVoiceMailNumberForOM = getResources().getBoolean(R.bool.def_Phone_isVoiceMailNumberForOpenMarket);
            //[BUGFIX]-Mod-BEGIN by NJTS-wei.li,for Defect-947381,11/18/2015
            TelephonyManager tm = (TelephonyManager)getSystemService(this.TELEPHONY_SERVICE);
            String mccmncnumber = tm.getSubscriberId(subId);
            //[BUGFIX]-Mod-End by NJTS-wei.li,for Defect-947381,11/18/2015
            String telproperty =  SystemProperties.get("ro.ssv.enabled", "false"); //[BUGFIX]-Add-BEGIN by NJTS-wei.huang,for SSV Idol4s,10/10/2015

            setAlwaysAskForCallAndMms(this); //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/17/2016, SOLUTION- 2473878 And TASk-2753627
            TctLog.i(TAG, " subId:"+subId);
            TctLog.i(TAG, " preNumber:"+preNumber + "   vmNumber:"+vmNumber);
            TctLog.i(TAG, " isVoiceMailNumberForOM:"+isVoiceMailNumberForOM);
            TctLog.i(TAG, " mccmncnumber:"+mccmncnumber);

            //[FEATURE]-ADD-BEGIN by TSNJ,zhujian.shao,11/06/2015,FR-861447(porting from PR1014434)
            TctLog.i(TAG, " isSimVoiceMailHightPritory:"+isSimVoiceMailHightPritory+" ,isGetVoiceMailFromPhone:"+isGetVoiceMailFromPhone);
            if (isSimVoiceMailHightPritory
                    || TextUtils.isEmpty(mccmncnumber)) {
                if (isGetVoiceMailFromPhone
                        && TextUtils.isEmpty(preNumber)) {
                    TctLog.i("Phone.BootCellBroadcastReceiver", "isGetVoiceMailFromPhone DSDS:" + isGetVoiceMailFromPhone);
                    if (!TextUtils.isEmpty(vmNumber) && !vmNumber.equals("0")) {
                        TctLog.i("Phone.BootCellBroadcastReceiver", "vmNumber DSDS:" + vmNumber);
                        mphone.setVoiceMailNumber(mphone.getVoiceMailAlphaTag(),vmNumber, null);//[BUGFIX]-Add by TCTNJ.zhujian.shao,1/16/2016,defect-1442133
                    }
                }
//                return;//[BUGFIX]-Modified-BEGIN by TCTNJ.zhujian.shao 2016-03-11,defect1749835 
            }
          //[FEATURE]-ADD-END by TSNJ,zhujian.shao,11/06/2015,FR-861447(porting from PR1014434)

            //[BUGFIX]-Add-BEGIN by NJTS-wei.huang,for SSV Idol4s,10/10/2015
            if(telproperty.equals("true") && !TextUtils.isEmpty(mccmncnumber)){
                vmNumber = getResources().getString(R.string.def_phone_ssv_voicemailNumber);
            }
            //[BUGFIX]-Add-END by NJTS-wei.huang,for SSV Idol4s,10/10/2015
            if(isVoiceMailNumberForOM && !TextUtils.isEmpty(mccmncnumber) && MCC_FOR_NL.equals(mccmncnumber.substring(0, 3))){
                vmNumber = "1233";
            }

            if (isVoiceMailNumberForOM && !TextUtils.isEmpty(vmNumber) && !vmNumber.equals("0")) {
                TctLog.i(TAG, "setVoiceMailNumber  vmNumber:" + vmNumber);
                mphone.setVoiceMailNumber(mphone.getVoiceMailAlphaTag(),vmNumber, null);//[BUGFIX]-Add by TCTNJ.zhujian.shao,1/16/2016,defect-1442133
            }

            return;
            //[BUGFIX]-Add-BEGIN by NJTS-wei.huang,for SSV Idol4s,10/10/2015

        }
    }
    //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/17/2016, SOLUTION- 2473878 And TASk-2753627
    //There is no variable in perso to set phone to always ask which SIM will be use in SMS and voice calls
    private void setAlwaysAskForCallAndMms(Context context) {
         android.util.Log.i("BootCellBroadcastReceiver","--longruningservice----setAlwaysAskForCallAndMms-");
        int i = 0;
        boolean setDefaultValue = false;
        boolean enableDSDS = false;
        boolean alreadysetAlwaysAsk = false;
        setDefaultValue = context.getResources().getBoolean(R.bool.def_setting_always_ask);
        enableDSDS = TelephonyManager.getDefault().isMultiSimEnabled();
        alreadysetAlwaysAsk = mDefaultDataRoamingPrefereces.getBoolean("setAlwaysAsk", false);
         android.util.Log.i("BootCellBroadcastReceiver","--longruningservice----setDefaultValue ="+setDefaultValue);
        if (!setDefaultValue || !enableDSDS || alreadysetAlwaysAsk){
            Log.i("BootCellBroadcastReceiver","return from setAlwaysAskForCallAndMms:setDefaultValue="+setDefaultValue+" enableDSDS="+enableDSDS);
            return;
        }
        TelephonyManager tm = (TelephonyManager)context.getSystemService(context.TELEPHONY_SERVICE);
        for (i = 0 ; i < tm.getSimCount(); i++ ) {
            int state = tm.getSimState(i);
            Log.i("BootCellBroadcastReceiver","setAlwaysAskForCallAndMms:sim"+i+" ,"+"state="+state);
            if (state != TelephonyManager.SIM_STATE_READY) {
                break;
            }
        }
        if (i > 1) {
            PhoneFactory.setPromptEnabled(true);
            PhoneFactory.setSMSPromptEnabled(true);
            Log.i("BootCellBroadcastReceiver","setAlwaysAskForCallAndMms-set-ok");
            mDefaultDataRoamingPrefereces.edit().putBoolean("setAlwaysAsk", true);
        }
    }
    //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)

}
