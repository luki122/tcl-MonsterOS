/* Copyright (C) 2016 Tcl Corporation Limited */

package com.android.phone;

import android.R.integer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.util.TctLog;
import android.widget.Toast;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionManager;
import android.os.SystemProperties;
import android.provider.Telephony;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;

import android.content.ComponentName;
import android.telephony.VoLteServiceState;

import com.android.internal.telephony.PhoneConstants;

import android.text.TextUtils;

import java.util.HashMap;

import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.OperatorInfo;

import android.content.ServiceConnection;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/*    date   |        author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 12/16/2015|     zhujian.shao     |       Task-1175521      Best network_CN*/
/*           |                      |                      |                  */
/*           |                      |                      |                  */
/* ----------|----------------------|----------------------|----------------- */
/* ========================================================================== */
public class BestAccessMobileDataRevicer extends BroadcastReceiver {
    private final String TAG = "BestAccessMobileDataRevicer";
    private static final boolean DBG = true;
    private static final int EVENT_NETWORK_SCAN_COMPLETED = 100;
    private static final int MSG_QUERY_NETWORK_OF_OOS = 200;
    private static final int MSG_QUERY_NETWORK_OF_DATA_CHANGE = 300;
    private static final int DELAY_QUERY_NETWORK = 5000;
    private static final int DELAY_QUERY_NETWORK_2G = 10000;
    private static final int QUERY_READY = -1;
    private static final int QUERY_IS_RUNNING = -2;
    public static final int QUERY_OK = 0;
    public static final int QUERY_EXCEPTION = 1;
    public static final int QUERY_OOS = 1;
    public static final int QUERY_DATA_TYPE = 2;
    //[BUGFIX]-Add by TCTNB.bo.chen,01/25/2016,1461228,bestnetwork
    public static final int DELAY_QUERY_NETWORK_NOT_4G = 18000000;
    private Context mContext;
    private int numPhones = TelephonyManager.getDefault().getPhoneCount();
    private PhoneStateListener[] mMSimPhoneStateListener;
    private HashMap<Integer, Integer> mSubIdPhoneIdMap;
    private HashMap<Integer, String> mPhoneIdNumericMap;
    private int[] mMSimDataServiceState;
    private ServiceState[] mMSimServiceState;
    private int mState = QUERY_READY;
    private boolean isSwitchDds = false;
    private TelephonyManager mPhone;
    private TelephonyManager mTelephonyManager;
    private SubscriptionManager mSubscriptionManager;
    private static BestAccessMobileDataRevicer instance = null;
    private static Object syncLock = new Object();
    private int switchDdsType = -1;
    private int mDataNetType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
    private int mSearchPhoneId = -1;
    private int mPhoneCount = 0;
    private int PHONE_ID1 = PhoneConstants.SUB1;
    private int PHONE_ID2 = PhoneConstants.SUB2;
    private int preDataDefaultPhoneId = -1;

    public static BestAccessMobileDataRevicer getInstance(Context context) {
        if (instance == null) {
            synchronized (syncLock) {
                if (instance == null) {
                    instance = new BestAccessMobileDataRevicer(context);
                }
            }
        }
        return instance;
    }

    private BestAccessMobileDataRevicer(Context context) {
        super();
        mContext = context;
        TctLog.d(TAG, "BestAccessMobileDataRevicer construct mState = "+mState);
        mPhoneIdNumericMap = new HashMap<Integer, String>();
        mMSimDataServiceState = new int[numPhones];
        mMSimServiceState = new ServiceState[numPhones];
        Settings.Global.putInt(context.getContentResolver(),"query_network_state", -1);
        for (int i = 0; i < numPhones; i++) {
            mMSimDataServiceState[i] = TelephonyManager.NETWORK_TYPE_UNKNOWN;
            mPhoneIdNumericMap.put(i,"");
            mMSimServiceState[i] = new ServiceState();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyIntents.ACTION_SUBINFO_RECORD_UPDATED);
        filter.addAction(TelephonyIntents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED);
        filter.addAction("android.net.mobile.Best_NETWORK_SWITCH_DDS");
        context.getApplicationContext().registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        mContext = context;
        String action = intent.getAction();
        int mSubId = intent.getIntExtra("bestNetSubId", 0);
        TctLog.d(TAG, "Intent action:" + action + " , mSubId:" + mSubId);
        if ("android.net.mobile.Best_NETWORK_SWITCH_DDS".equals(action)) {
            TelephonyManager tele = TelephonyManager.from(context);
            mSubscriptionManager = SubscriptionManager.from(context);
            int mDds = mSubscriptionManager.getDefaultDataSubscriptionId();
            if (mSubId > 0 && mDds != mSubId) {
                isSwitchDds = true;
                //Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.DDS_SWITCH_DATA_ON, 3); // MODIFIED by chusheng, 2016-05-03,BUG-2013262
                mSubscriptionManager.setDefaultDataSubId(mSubId);
            }
        } else if (action.equals(TelephonyIntents.ACTION_SUBINFO_RECORD_UPDATED)) {
            if (mTelephonyManager == null) {
                mTelephonyManager = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
            }
            if (mTelephonyManager == null) {
                mSubscriptionManager = SubscriptionManager.from(context);
            }
            for (int i = 0; i < numPhones; i++) {
                mPhoneIdNumericMap.put(i, mTelephonyManager.getSimOperatorNumericForPhone(i));
                TctLog.d(TAG, "SimOperatorNumeric: " + mPhoneIdNumericMap.get(i));
            }
            unregisterPhoneStateListener();
            /* MODIFIED-BEGIN by bo.chen, 2016-06-30,BUG-2419211*/
            if(mSubscriptionManager != null){
                mSearchPhoneId = mSubscriptionManager.getDefaultDataPhoneId();
            } else {
                mSearchPhoneId = -1;
            }
            /* MODIFIED-END by bo.chen,BUG-2419211*/
            if (startSwitchBestAccess(mPhoneIdNumericMap)) {
                registerPhoneStateListener(context);
            }
        }else if (action.equals(TelephonyIntents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED)) {
            int preferredDataSubscription = SubscriptionManager.getDefaultDataSubscriptionId();
            if (isSwitchDds) {
                TctLog.d(TAG, "ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
                isSwitchDds = false;
                if(preDataDefaultPhoneId != -1 && preDataDefaultPhoneId != preferredDataSubscription){
                    //[BUGFIX]-ADD-BEGIN BY yandong.sun 04/26/2016 DEFECT-1996289
                    mSubscriptionManager = SubscriptionManager.from(context);
                    String text = context.getResources().getString(R.string.best_network_switch_success,mSubscriptionManager.getPhoneId(preDataDefaultPhoneId)+1,mSubscriptionManager.getDefaultDataPhoneId()+1);
                    //[BUGFIX]-ADD-END BY yandong.sun
                    //Toast.makeText(context, text, Toast.LENGTH_SHORT).show(); // MODIFIED by sunyandong, 2016-05-06,BUG-2073528
                }
            }
            preDataDefaultPhoneId = preferredDataSubscription;
        }
    }

    private boolean startSwitchBestAccess(HashMap<Integer, String> mHashMap) {
        for (int i = 0; i < numPhones; i++) {
            String mNumeric = mHashMap.get(i);
            if (TextUtils.isEmpty(mNumeric)) {
                return false;
            } else if (!("46000".equals(mNumeric) || "46001".equals(mNumeric) ||
                    "46007".equals(mNumeric) || "46002".equals(mNumeric) ||
                    "46003".equals(mNumeric) || "46009".equals(mNumeric) || "46011"
                        .equals(mNumeric))) {
                return false;
            }
        }
        if (mHashMap.get(0).equals(mHashMap.get(1))) {
            return false;
        }
        TctLog.d(TAG, "startSwitchBestAccess is true");
        return true;
    }

    private void registerPhoneStateListener(Context context) {
        TctLog.d(TAG, "registerPhoneStateListener");
        // telephony
        mPhone = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        mSubIdPhoneIdMap = new HashMap<Integer, Integer>();
        mMSimPhoneStateListener = new PhoneStateListener[numPhones];

        for (int i = 0; i < numPhones; i++) {
            int[] subIdtemp = SubscriptionManager.getSubId(i);
            if (subIdtemp != null) {
                int subId = subIdtemp[0];
                TctLog.d(TAG, "registerPhoneStateListener subId: " + subId);
                TctLog.d(TAG, "registerPhoneStateListener slotId: " + i);
                if (subId > 0) {
                    mSubIdPhoneIdMap.put(subId, i);
                    mMSimPhoneStateListener[i] = getPhoneStateListener(subId, i);
                    mPhone.listen(mMSimPhoneStateListener[i],
                            PhoneStateListener.LISTEN_SERVICE_STATE
                                    | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                                    | PhoneStateListener.LISTEN_DATA_ACTIVITY);
                } else {
                    mMSimPhoneStateListener[i] = null;
                }
            }
        }
    }

    private PhoneStateListener getPhoneStateListener(int subId, int slotId) {
        final int mSlotId = slotId;
        PhoneStateListener mMSimPhoneStateListener = new PhoneStateListener(subId) {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                int phoneId = mSlotId;
                /*TctLog.d(TAG, "onSignalStrengthsChanged received on phoneId :"
                        + getPhoneId(mSubId) + ", slotId=" + mSlotId + ", subId=" + mSubId
                        + "signalStrength=" + signalStrength +
                        ((signalStrength == null) ? "" : (" level=" + signalStrength.getLevel())));*/
            }

            @Override
            public void onServiceStateChanged(ServiceState state) {
                int phoneId = mSlotId;
                TctLog.d(TAG, "onServiceStateChanged received on phoneId : " + phoneId + " ,state = "
                        + state.getState() +" ,type = "+state.getRilDataRadioTechnology() +" ,rilRadioTechnologyToString = "+rilRadioTechnologyToString(state.getRilDataRadioTechnology()));

                int networkType = state.getRilDataRadioTechnology();
                int mDds = SubscriptionManager.getDefaultDataSubscriptionId();
                boolean isBestAccess = Settings.Global.getInt(mContext.getContentResolver(), "is_select_best_network", 0) != 0;
                if (isBestAccess && mDds == mSubId) {
                    if (state.getState() == ServiceState.STATE_IN_SERVICE) {
                        if (mHandler.hasMessages(MSG_QUERY_NETWORK_OF_OOS)) {
                            mHandler.removeMessages(MSG_QUERY_NETWORK_OF_OOS);
                            switchDdsType = -1;
                            TctLog.d(TAG, "removeMessages MSG_QUERY_NETWORK_OF_OOS");
                        }
                    }
                    if (mMSimServiceState[phoneId].getState() == ServiceState.STATE_IN_SERVICE
                            && state.getState() != ServiceState.STATE_IN_SERVICE && mMSimServiceState[1-phoneId].getState() == ServiceState.STATE_IN_SERVICE) {
                        mHandler.sendEmptyMessageDelayed(MSG_QUERY_NETWORK_OF_OOS, DELAY_QUERY_NETWORK);
                        switchDdsType = QUERY_OOS;
                        TctLog.d(TAG, "mMSimServiceState[phoneId].getState() = " + mMSimServiceState[phoneId].getState() +" , state.getState() = "+state.getState());
                    }else if (mMSimServiceState[phoneId].getState() == ServiceState.STATE_IN_SERVICE && mMSimServiceState[1-phoneId].getState() == ServiceState.STATE_IN_SERVICE) {
                        if (state.getDataRegState() == ServiceState.STATE_IN_SERVICE && "4G".equalsIgnoreCase(rilRadioTechnologyToString(networkType))) {
                            if (mHandler.hasMessages(MSG_QUERY_NETWORK_OF_DATA_CHANGE)) {
                                mHandler.removeMessages(MSG_QUERY_NETWORK_OF_DATA_CHANGE);
                                switchDdsType = -1;
                                TctLog.d(TAG, "removeMessages MSG_QUERY_NETWORK_OF_DATA_CHANGE");
                            }
                        }
                       /* MODIFIED-BEGIN by bo.chen, 2016-06-06,BUG-2251068*/
                       if("4G".equalsIgnoreCase(rilRadioTechnologyToString(mMSimDataServiceState[phoneId])) && ("2G".equalsIgnoreCase(rilRadioTechnologyToString(networkType)) || "3G".equalsIgnoreCase(rilRadioTechnologyToString(networkType)) || "Unknown".equalsIgnoreCase(rilRadioTechnologyToString(networkType)))){
                           mHandler.sendEmptyMessageDelayed(MSG_QUERY_NETWORK_OF_DATA_CHANGE, DELAY_QUERY_NETWORK);
                           switchDdsType = QUERY_DATA_TYPE;
                       }
                       if("Unknown".equalsIgnoreCase(rilRadioTechnologyToString(mMSimDataServiceState[phoneId])) && ("2G".equalsIgnoreCase(rilRadioTechnologyToString(networkType)) || "3G".equalsIgnoreCase(rilRadioTechnologyToString(networkType)))){
                       /* MODIFIED-END by bo.chen,BUG-2251068*/
                           mHandler.sendEmptyMessageDelayed(MSG_QUERY_NETWORK_OF_DATA_CHANGE, DELAY_QUERY_NETWORK_2G);
                           switchDdsType = QUERY_DATA_TYPE;
                       }
                    }
                }
                mMSimServiceState[phoneId] = state;
                mMSimDataServiceState[phoneId] = networkType;
            }

            @Override
            public void onDataConnectionStateChanged(int state, int networkType) {
                int phoneId = mSlotId;
                TctLog.d(TAG, "onDataConnectionStateChanged received on phoneId : "
                        + phoneId + " ,subId: " + mSubId + " ,state = " + state + " ,type = " + rilRadioTechnologyToString(networkType));

                boolean isBestAccess = Settings.Global.getInt(mContext.getContentResolver(), "is_select_best_network", 0) != 0;
                /*if (isBestAccess && mSubId == SubscriptionManager.getDefaultDataSubId()) {
                    // mDataState = state;
                     mDataNetType = networkType;
                     if (state == ServiceState.STATE_IN_SERVICE && "4G".equalsIgnoreCase(rilRadioTechnologyToString(networkType))) {
                         if (mHandler.hasMessages(MSG_QUERY_NETWORK_OF_DATA_CHANGE)) {
                             mHandler.removeMessages(MSG_QUERY_NETWORK_OF_DATA_CHANGE);
                             switchDdsType = -1;
                             TctLog.d(TAG, "removeMessages MSG_QUERY_NETWORK_OF_DATA_CHANGE");
                         }
//                         mHandler.sendEmptyMessageDelayed(MSG_QUERY_NETWORK_OF_DATA_CHANGE, DELAY_QUERY_NETWORK);
                     }
                    if("4G".equalsIgnoreCase(rilRadioTechnologyToString(mMSimDataServiceState[phoneId])) && ("2G".equalsIgnoreCase(rilRadioTechnologyToString(networkType)) || "3G".equalsIgnoreCase(rilRadioTechnologyToString(networkType)))){
                        mHandler.sendEmptyMessageDelayed(MSG_QUERY_NETWORK_OF_DATA_CHANGE, DELAY_QUERY_NETWORK);
                        switchDdsType = QUERY_DATA_TYPE;
                    }
                    if("Unknown".equalsIgnoreCase(rilRadioTechnologyToString(mMSimDataServiceState[phoneId])) && "2G".equalsIgnoreCase(rilRadioTechnologyToString(networkType))){
                        mHandler.sendEmptyMessageDelayed(MSG_QUERY_NETWORK_OF_DATA_CHANGE, DELAY_QUERY_NETWORK);
                        switchDdsType = QUERY_DATA_TYPE;
                    }
                }
                mMSimDataServiceState[phoneId] = networkType;*/

            }
        };
        return mMSimPhoneStateListener;
    }

    private void unregisterPhoneStateListener() {
        for (int i = 0; i < mPhoneCount; i++) {
            if (mMSimPhoneStateListener[i] != null) {
                mPhone.listen(mMSimPhoneStateListener[i], PhoneStateListener.LISTEN_NONE);
            }
        }
    }

    private int getPhoneId(int subId) {
        int phoneId;
        phoneId = SubscriptionManager.getPhoneId(subId);
        TctLog.d(TAG, "getPhoneId phoneId: " + phoneId);
        return phoneId;
    }

    private int getDefaultPhoneId() {
        int phoneId;
        int numPhones = TelephonyManager.getDefault().getPhoneCount();
        phoneId = getPhoneId(SubscriptionManager.getDefaultSubscriptionId());
        if (phoneId < 0 || phoneId >= numPhones) {
            phoneId = 0;
        }
        return phoneId;
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;
            switch (msg.what) {
                case EVENT_NETWORK_SCAN_COMPLETED:
                    mState = QUERY_READY;
                    Settings.Global.putInt(mContext.getContentResolver(),"query_network_state", -1);
                    // see if we need to do any work.
                    if (ar == null) {
                        if (DBG)
                            TctLog.d(TAG, "AsyncResult is null.");
                        return;
                    }
                    int exception = (ar.exception == null) ? QUERY_OK : QUERY_EXCEPTION;
                    TctLog.d(TAG, "AsyncResult has exception " + exception);
                    networksListLoaded((ArrayList<OperatorInfo>) ar.result, exception);
                    break;
                case MSG_QUERY_NETWORK_OF_OOS:
                    TctLog.d(TAG, "MSG_QUERY_NETWORK_OF_OOS");
                    //[BUGFIX]-Mod-BEGIN by TSNJ,shu.wang,04/26/2016,Defect-1843014
                    if (mSubscriptionManager != null) {
                        int ddsPhoneId = mSubscriptionManager.getDefaultDataPhoneId();
                        if (ddsPhoneId >=0 && mMSimServiceState[ddsPhoneId].getState() != ServiceState.STATE_IN_SERVICE && mMSimServiceState[1-ddsPhoneId].getState() == ServiceState.STATE_IN_SERVICE) {
                            int[] mSubId = mSubscriptionManager.getSubId(1-ddsPhoneId);
                            if (mSubId[0] > 0) {
                                isSwitchDds = true;
                                //Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.DDS_SWITCH_DATA_ON, 3); // MODIFIED by chusheng, 2016-05-03,BUG-2013262
                                mSubscriptionManager.setDefaultDataSubId(mSubId[0]);
                            }
                        }
                    }
                    //[BUGFIX]-Mod-END by TSNJ,shu.wang
                    break;
                case MSG_QUERY_NETWORK_OF_DATA_CHANGE:
                    TctLog.d(TAG, "MSG_QUERY_NETWORK_OF_DATA_CHANGE");
                    QueryNetThread queryNetThread = new QueryNetThread();
                    new Thread(queryNetThread).start();
                    break;
            }
            return;
        }
    };

    private void loadNetworksList(int mPhoneId) {
        if (DBG)
            TctLog.d(TAG, "load networks list...");

    }

    /**
     * networksListLoaded has been rewritten to take an array of OperatorInfo objects and a status
     * field, instead of an AsyncResult. Otherwise, the functionality which takes the OperatorInfo
     * array and creates a list of preferences from it, remains unchanged.
     */
    private void networksListLoaded(List<OperatorInfo> result, int status) {

        TctLog.d(TAG, "networks list loaded");
        mState = QUERY_READY;
        Settings.Global.putInt(mContext.getContentResolver(),"query_network_state", -1);

        if (status != QUERY_OK) {
            TctLog.d(TAG, "error while querying available networks");
            mHandler.sendEmptyMessageDelayed(MSG_QUERY_NETWORK_OF_DATA_CHANGE, DELAY_QUERY_NETWORK);
        } else {
            if (result != null) {
                mSubscriptionManager = SubscriptionManager.from(mContext);
                int ddsPhoneId = mSubscriptionManager.getDefaultDataPhoneId();
                if (ddsPhoneId != mSearchPhoneId) {
                    /*MODIFIED-BEGIN by bo.chen, 2016-04-14,BUG-1940236*/
                    TctLog.d(TAG, "ddsddsPhoneId = " + ddsPhoneId + "mSearchPhoneId = " + mSearchPhoneId);
                    mSearchPhoneId = -1;
                    return;
                }
                if ("4G".equalsIgnoreCase(rilRadioTechnologyToString(mMSimDataServiceState[ddsPhoneId]))) {
                    TctLog.d(TAG, "rilRadioTechnology = " + rilRadioTechnologyToString(mMSimDataServiceState[ddsPhoneId]));
                    /*MODIFIED-END by bo.chen,BUG-1940236*/
                    mSearchPhoneId = -1;
                    return;
                }
                for (OperatorInfo ni : result) {
                    String networkRat = rilRadioTechnologyToString(Integer.parseInt(ni.getRadioTech()));
                    String networkState = ni.getState().toString().toLowerCase();
                    String operatorNumeric = ni.getOperatorNumeric();
                    TctLog.d(TAG, "networkRat = "+networkRat +" ,networkState = "+networkState +" ,ni.getRadioTech() = "+ni.getRadioTech()+" ,operatorNumeric = "+operatorNumeric);
                    if (networkState.equals("current") && "4G".equalsIgnoreCase(networkRat)) {
                        mSearchPhoneId = -1;
                        return;
                    /*MODIFIED-BEGIN by bo.chen, 2016-04-14,BUG-1940236*/
                    }else if (isSameOperator(operatorNumeric, mPhoneIdNumericMap.get(1-ddsPhoneId)) && "4G".equalsIgnoreCase(networkRat)) {
                        int[] mSubId = mSubscriptionManager.getSubId(1-ddsPhoneId);
                        if (mSubId[0] > 0) {
                            isSwitchDds = true;
                            //Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.DDS_SWITCH_DATA_ON, 3); // MODIFIED by chusheng, 2016-05-03,BUG-2013262
                            mSubscriptionManager.setDefaultDataSubId(mSubId[0]);
                        }
                    }else if (isSameOperator(operatorNumeric, mPhoneIdNumericMap.get(1-ddsPhoneId)) && !("4G".equalsIgnoreCase(networkRat))) {
                    /*MODIFIED-END by bo.chen,BUG-1940236*/
                        //[BUGFIX]-Add by TCTNB.bo.chen,01/25/2016,1461228,bestnetwork
                        mHandler.sendEmptyMessageDelayed(MSG_QUERY_NETWORK_OF_DATA_CHANGE, DELAY_QUERY_NETWORK_NOT_4G);
                    }else {
                        if("46000".equals(mPhoneIdNumericMap.get(ddsPhoneId)) || "46002".equals(mPhoneIdNumericMap.get(ddsPhoneId)) || "46007".equals(mPhoneIdNumericMap.get(ddsPhoneId))){
                            if ("46001".equals(operatorNumeric) && "3G".equalsIgnoreCase(networkRat) || "46003".equals(operatorNumeric) && "3G".equalsIgnoreCase(networkRat)) {
                                int[] mSubId = mSubscriptionManager.getSubId(1-ddsPhoneId);
                                if (mSubId[0] > 0) {
                                    isSwitchDds = true;
                                    //Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.DDS_SWITCH_DATA_ON, 3); // MODIFIED by chusheng, 2016-05-03,BUG-2013262
                                    mSubscriptionManager.setDefaultDataSubId(mSubId[0]);
                                }
                            }
                        }else if ("46001".equals(mPhoneIdNumericMap.get(ddsPhoneId))) {
                            if ("46003".equals(operatorNumeric) && "3G".equalsIgnoreCase(networkRat)) {
                                int[] mSubId = mSubscriptionManager.getSubId(1-ddsPhoneId);
                                if (mSubId[0] > 0) {
                                    isSwitchDds = true;
                                    //Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.DDS_SWITCH_DATA_ON, 3); // MODIFIED by chusheng, 2016-05-03,BUG-2013262
                                    mSubscriptionManager.setDefaultDataSubId(mSubId[0]);
                                }
                            }
                        }
                    }
                }
            /*MODIFIED-BEGIN by bo.chen, 2016-04-14,BUG-1940236*/
            } else {
                TctLog.d(TAG, "result is null");
                /*MODIFIED-END by bo.chen,BUG-1940236*/
            }
        }
    }

    public void startNetworkQuery(Handler mHandler, int phoneId) {
        if (mHandler != null) {
            mState = Settings.Global.getInt(mContext.getContentResolver(),"query_network_state", -1);
            TctLog.d(TAG, "mState = "+mState);
            switch (mState) {
                case QUERY_READY:
                    // TODO: we may want to install a timeout here in case we
                    // do not get a timely response from the RIL.
                    Phone phone = null;
                    try {
                        phone = PhoneFactory.getPhone(phoneId);
                    } catch (IllegalStateException e) {
                        // TODO: handle exception
                        TctLog.d(TAG, "java.lang.IllegalStateException: Default phones haven't been made yet!");
                    }
                    if (phone != null) {
                        phone.getAvailableNetworks(
                                mHandler.obtainMessage(EVENT_NETWORK_SCAN_COMPLETED));
                        mState = QUERY_IS_RUNNING;
                        Settings.Global.putInt(mContext.getContentResolver(),"query_network_state", -2);
                        if (DBG)
                            TctLog.d(TAG, "starting new query");
                    } else {
                        if (DBG) {
                            TctLog.d(TAG, "phone is null");
                        }
                    }
                    break;

                // do nothing if we're currently busy.
                case QUERY_IS_RUNNING:
                    TctLog.d(TAG, "query already in progress");
                    break;
                default:
            }
        }
    }

    public class QueryNetThread implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            mSearchPhoneId = getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId());
            TctLog.d(TAG, "phoneId = "+mSearchPhoneId);
            if (mSearchPhoneId != SubscriptionManager.INVALID_PHONE_INDEX
                    && mSearchPhoneId != SubscriptionManager.DEFAULT_PHONE_INDEX) {
                int otherPhoneId = 1 - mSearchPhoneId;
                int[] otherSubId = SubscriptionManager.getSubId(otherPhoneId);
                String mSubscriptionId = mTelephonyManager.getSubscriberId(otherSubId[0]);
                if (!TextUtils.isEmpty(mSubscriptionId)) {
                     boolean isPolicyLimitbytes2 = Settings.Global.getInt(mContext.getContentResolver(),"policy_limite_bytes_reached"+mSubscriptionId, 0) != 0;
                     TctLog.d(TAG, "isPolicyLimitbytes2 = "+isPolicyLimitbytes2);
                    if (!isPolicyLimitbytes2) {
                        startNetworkQuery(mHandler, mSearchPhoneId);
                    }
                }
            }
        }

    }

    public String rilRadioTechnologyToString(int rt) {
        String rtString;

        switch(rt) {
            case ServiceState.RIL_RADIO_TECHNOLOGY_UNKNOWN:
                rtString = "Unknown";
                break;
            case ServiceState.RIL_RADIO_TECHNOLOGY_GPRS:
                rtString = "2G";
                break;
            case ServiceState.RIL_RADIO_TECHNOLOGY_EDGE:
                rtString = "2G";
                break;
            case ServiceState.RIL_RADIO_TECHNOLOGY_UMTS:
                rtString = "3G";
                break;
            case ServiceState.RIL_RADIO_TECHNOLOGY_IS95A:
                rtString = "2G";
                break;
            case ServiceState.RIL_RADIO_TECHNOLOGY_IS95B:
                rtString = "2G";
                break;
            case ServiceState.RIL_RADIO_TECHNOLOGY_1xRTT:
                rtString = "2G";
                break;
            case ServiceState.RIL_RADIO_TECHNOLOGY_EVDO_0:
                rtString = "3G";
                break;
            case ServiceState.RIL_RADIO_TECHNOLOGY_EVDO_A:
                rtString = "3G";
                break;
            case ServiceState.RIL_RADIO_TECHNOLOGY_HSDPA:
                rtString = "3G";
                break;
            case ServiceState.RIL_RADIO_TECHNOLOGY_HSUPA:
                rtString = "3G";
                break;
            case ServiceState.RIL_RADIO_TECHNOLOGY_HSPA:
                rtString = "3G";
                break;
            case ServiceState.RIL_RADIO_TECHNOLOGY_EVDO_B:
                rtString = "3G";
                break;
            case ServiceState.RIL_RADIO_TECHNOLOGY_EHRPD:
                rtString = "3G";
                break;
            case ServiceState.RIL_RADIO_TECHNOLOGY_LTE:
                rtString = "4G";
                break;
            case ServiceState.RIL_RADIO_TECHNOLOGY_HSPAP:
                rtString = "3G";
                break;
            case ServiceState.RIL_RADIO_TECHNOLOGY_GSM:
                rtString = "2G";
                break;
            case ServiceState.RIL_RADIO_TECHNOLOGY_TD_SCDMA:
                rtString = "3G";
                break;
            case ServiceState.RIL_RADIO_TECHNOLOGY_IWLAN:
                rtString = "IWLAN";
                break;
            case ServiceState.RIL_RADIO_TECHNOLOGY_LTE_CA:
                rtString = "4G";
                break;
            default:
                rtString = "Unexpected";
                TctLog.d(TAG, "Unexpected radioTechnology=" + rt);
                break;
        }
        return rtString;
    }
    /*MODIFIED-BEGIN by bo.chen, 2016-04-14,BUG-1940236*/
    public boolean isSameOperator(String operatorNumeric, String simNumeric){
        boolean isSame = false;
        TctLog.d(TAG, "operatorNumeric = " + operatorNumeric + "simNumeric = " + simNumeric);
        if("46000".equals(operatorNumeric) || "46002".equals(operatorNumeric) || "46007".equals(operatorNumeric) || "46008".equals(operatorNumeric)){
            if("46000".equals(simNumeric) || "46002".equals(simNumeric) || "46007".equals(simNumeric) || "46008".equals(simNumeric)){
                isSame = true;
            }
        } else if("46001".equals(operatorNumeric) || "46009".equals(operatorNumeric)){
            if("46001".equals(simNumeric) || "46009".equals(simNumeric)){
                isSame = true;
            }
        } else if("46003".equals(operatorNumeric) || "46011".equals(operatorNumeric)){
            if("46003".equals(simNumeric) || "46011".equals(simNumeric)){
                isSame = true;
            }
        }
        return isSame;
    }
    /*MODIFIED-END by bo.chen,BUG-1940236*/

}
