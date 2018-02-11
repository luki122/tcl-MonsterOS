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

package com.android.phone;

import mst.app.dialog.AlertDialog;
import mst.app.dialog.ProgressDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserManager;
import mst.preference.Preference;
import mst.preference.PreferenceActivity;
import mst.preference.PreferenceGroup;
import mst.preference.PreferenceScreen;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.telephony.SubscriptionManager;

import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.OperatorInfo;


/* MODIFIED-BEGIN by bo.chen, 2016-09-27,BUG-3000255*/
import android.telecom.TelecomManager; // Defect - Liyi.Ding - 3273799- DUT can't make PLMN manually during CALL
import mst.app.dialog.AlertDialog.Builder;
import android.content.DialogInterface.OnClickListener;
import mst.preference.PreferenceCategory;
import mst.preference.SwitchPreference;

import java.util.HashMap;
import java.util.List;

import java.util.LinkedList;
import java.util.Map.Entry;
/* MODIFIED-END by bo.chen,BUG-3000255*/

import android.text.BidiFormatter;
import android.text.TextDirectionHeuristics;

/**
 * "Networks" settings UI for the Phone app.
 */
public class NetworkSetting extends PreferenceActivity
        implements DialogInterface.OnCancelListener {

    private static final String LOG_TAG = "phone";
    private static final boolean DBG = true;

    private static final int EVENT_NETWORK_SCAN_COMPLETED = 100;
    private static final int EVENT_NETWORK_SELECTION_DONE = 200;
    private static final int EVENT_AUTO_SELECT_DONE = 300;

    //dialog ids
    private static final int DIALOG_NETWORK_SELECTION = 100;
    private static final int DIALOG_NETWORK_LIST_LOAD = 200;
    private static final int DIALOG_NETWORK_AUTO_SELECT = 300;

    private static final int EVENT_NETWORK_DATA_MANAGER_DONE = 500; // MODIFIED by bo.chen, 2016-08-04,BUG-2670018

    /* MODIFIED-BEGIN by bo.chen, 2016-09-27,BUG-3000255*/
    private static final int DIALOG_CLOSE_AUTO = 1000;
    private static final int DIALOG_TOOPEN_AUTO = 2000;

    private static final int DIALOG_CLOSE_AUTO_DIAL = 3000; // Defect - Liyi.Ding - 3273799- DUT can't make PLMN manually during CALL
    //String keys for preference lookup
    private static final String LIST_NETWORKS_KEY = "list_networks_key";
    private static final String BUTTON_SRCH_NETWRKS_KEY = "button_srch_netwrks_key";
    private static final String BUTTON_AUTO_SELECT_KEY = "button_auto_select_key";

    private static final String BUTTON_AUTO_SELECT_SWITCH_KEY = "button_auto_select_switch_key";
    private static final String SEARCH_NETWORKS_KEY = "search_networks_key";
    private boolean mSwitchFlag = true;



    private static final int SERVICE_STATE_CHANGED = 1;
    /* MODIFIED-END by bo.chen,BUG-3000255*/

    //map of network controls to the network data.
    private HashMap<Preference, OperatorInfo> mNetworkMap;

    int mPhoneId = SubscriptionManager.INVALID_PHONE_INDEX;

    //map of RAT type values to user understandable strings
    private HashMap<String, String> mRatMap;

    protected boolean mIsForeground = false;

    private UserManager mUm;
    private boolean mUnavailable;

    /** message for network selection */
    String mNetworkSelectMsg;

    NetworkSettingDataManager mDataManager = null; // MODIFIED by bo.chen, 2016-08-04,BUG-2670018
    //preference objects
    private PreferenceGroup mNetworkList;
    private Preference mSearchButton;
    private Preference mAutoSelect;

    /* MODIFIED-BEGIN by bo.chen, 2016-09-27,BUG-3000255*/
    Phone mPhone;
    PreferenceGroup netowrkList;
    private SwitchPreference mAutoSelectSwitch;
    private AlertDialog.Builder builder1;



    private Handler mServiceStateHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Log.d(LOG_TAG, "mServiceStateHandler Message: " + msg);
            switch (msg.what) {
                case SERVICE_STATE_CHANGED:
                    onServiceStateChanged(msg);
                    break;
                default:
                    break;
            }
        }
    };

    private void registerForServiceStateChanged() {
        Log.d(LOG_TAG, "registerForServiceStateChanged: mPhone not null:" + (mPhone!=null));
        if (mPhone != null) {
            mPhone.unregisterForServiceStateChanged(mServiceStateHandler);
            mPhone.registerForServiceStateChanged(mServiceStateHandler, SERVICE_STATE_CHANGED, null);
        }
    }

    private void unregisterForServiceStateChanged() {
        if (mPhone != null) {
            mPhone.unregisterForServiceStateChanged(mServiceStateHandler);
        }
        mServiceStateHandler.removeMessages(SERVICE_STATE_CHANGED);
    }

    public void onServiceStateChanged(Message msg) {
        ServiceState serviceState = (ServiceState) ((AsyncResult) msg.obj).result;
        Log.d(LOG_TAG, "onServiceStateChanged: serviceState: " + serviceState);

        boolean changed = false;

        List<OperatorInfo> cloneOpList = new LinkedList<OperatorInfo>();

        for(Entry<Preference, OperatorInfo> entry : mNetworkMap.entrySet()){
            OperatorInfo item = entry.getValue();
            String itemNewState = getNewStateForNetworkChange(item, serviceState);
            OperatorInfo cloneItem;
            if(TextUtils.isEmpty(itemNewState)){
                cloneItem = new OperatorInfo(item.getOperatorAlphaLong(),
                        item.getOperatorAlphaShort(), item.getOperatorNumeric() + "+" + item.getRadioTech(),
                        item.getState().toString().toLowerCase());
            }else{
                cloneItem = new OperatorInfo(item.getOperatorAlphaLong(),
                        item.getOperatorAlphaShort(), item.getOperatorNumeric() + "+" + item.getRadioTech(),
                        itemNewState.toLowerCase());
                changed = true;
            }
            cloneOpList.add(cloneItem);
        }

        Log.d(LOG_TAG, "onServiceStateChanged : changed:" + changed);
        if(changed){
            networksListLoaded(cloneOpList, NetworkQueryService.QUERY_OK);
        }
    }

    private String getNewStateForNetworkChange(OperatorInfo op, ServiceState serviceState){
        Log.d(LOG_TAG, "getNewStateForNetworkChange: [OperatorInfo]:" + op);
        Log.d(LOG_TAG, "getNewStateForNetworkChange: [ServiceState]:"+serviceState);

        if(op.getState().equals(OperatorInfo.State.FORBIDDEN) || op.getState().equals(OperatorInfo.State.UNKNOWN)){
            return null;
        }

        if (serviceState.getVoiceRegState() == ServiceState.STATE_IN_SERVICE) {
            if(op.getState().equals(OperatorInfo.State.AVAILABLE) && equalsOperatorInfo(op, serviceState)){
                Log.d(LOG_TAG, "getNewStateForNetworkChange: available changed to current ");
                return "current";
            }else if(op.getState().equals(OperatorInfo.State.CURRENT) && (!equalsOperatorInfo(op, serviceState)) ){
                Log.d(LOG_TAG, "getNewStateForNetworkChange: current changed to available ");
                return "available";
            }
        } else if (serviceState.getVoiceRegState() != ServiceState.STATE_IN_SERVICE
                && op.getState().equals(OperatorInfo.State.CURRENT)) {
            Log.d(LOG_TAG, "getNewStateForNetworkChange: no service, current changed to available ");
            return "available";
        }
        return null;
    }
    private boolean equalsOperatorInfo(OperatorInfo op, ServiceState serviceState){
        boolean opRATequals = false;  //default is flase
        if(TextUtils.isEmpty(op.getRadioTech())){
            opRATequals = true;   //no need to compare RAT
        }else{
            int opRadioTech = Integer.parseInt(op.getRadioTech());
            // refer: qcril_qmi_nas.c -- qcril_qmi_nas_fill_network_scan_response()
            if(opRadioTech == ServiceState.RIL_RADIO_TECHNOLOGY_EDGE)
                opRadioTech = ServiceState.RIL_RADIO_TECHNOLOGY_GSM;

            Log.d(LOG_TAG, "equalsOperatorInfo -- opRadioTech: " + opRadioTech);
            Log.d(LOG_TAG, "equalsOperatorInfo -- serviceState.getRilVoiceRadioTechnology: " + serviceState.getRilVoiceRadioTechnology());

            if(opRadioTech == serviceState.getRilVoiceRadioTechnology()){
                opRATequals = true;  //[OperatorInfo] RAT equals [ServiceState] RAT
            }
        }

        if(opRATequals
            && op.getOperatorAlphaLong().equals(serviceState.getOperatorAlphaLong())
            && op.getOperatorAlphaShort().equals(serviceState.getOperatorAlphaShort())
            && op.getOperatorNumeric().equals(serviceState.getOperatorNumeric())){
            Log.d(LOG_TAG, "[OperatorInfo] equals [ServiceState]: true");
            return true;
        }

        Log.d(LOG_TAG, "[OperatorInfo] equals [ServiceState]: false");
        return false;
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            AsyncResult ar;
            switch (msg.what) {
                case EVENT_NETWORK_SCAN_COMPLETED:

                    if (getResources().getBoolean( R.bool.feature_phone_show_search_network_switch_on)){
                        mAutoSelectSwitch.setChecked(false);
                   }
                   /* MODIFIED-END by bo.chen,BUG-3000255*/

                    networksListLoaded ((List<OperatorInfo>) msg.obj, msg.arg1);
                    break;

                case EVENT_NETWORK_SELECTION_DONE:
                    if (DBG) log("hideProgressPanel");
                    removeDialog(DIALOG_NETWORK_SELECTION);
                    getPreferenceScreen().setEnabled(true);

                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        if (DBG) log("manual network selection: failed!");

                        /* MODIFIED-BEGIN by bo.chen, 2016-09-27,BUG-3000255*/
                        if (getResources().getBoolean( R.bool.feature_phone_show_search_network_switch_on)&&!isFinishing()){
                            showDialog(DIALOG_TOOPEN_AUTO);
                        } else if (getResources().getBoolean(R.bool.feature_tctfw_AutomaticSearchAfterManualSearchFailed_on)){
                            //If manual network selection failed, try automatic registration.
                            selectNetworkAutomatic();
                            displayNetworkSelectionFailed(ar.exception);
                        } else {
                            displayNetworkSelectionFailed(ar.exception);
                        }
                        /* MODIFIED-END by bo.chen,BUG-3000255*/


                    } else {
                        if (DBG) log("manual network selection: succeeded!");
                        displayNetworkSelectionSucceeded();
                    }

                    break;
                /* MODIFIED-BEGIN by bo.chen, 2016-08-04,BUG-2670018*/
                case EVENT_NETWORK_DATA_MANAGER_DONE:
                    log("EVENT_NETWORK_DATA_MANAGER_DONE: " + msg.arg1);
                    if (msg.arg1 == 1) {
                        TelephonyManager telephonyManager = (TelephonyManager)(NetworkSetting.this.getSystemService(Context.TELEPHONY_SERVICE));
                        log("telephonyManager.getDataState(): " +telephonyManager.getDataState() );
                        if (telephonyManager.getDataState() == TelephonyManager.DATA_DISCONNECTED) {
                            loadNetworksList();
                        } else {
                            displayNetworkQueryForbidden();
                        }

                    /* MODIFIED-BEGIN by bo.chen, 2016-09-27,BUG-3000255*/
                    } else if(msg.arg1 == 0){
                        if (getResources().getBoolean( R.bool.feature_phone_show_search_network_switch_on)){
                            Log.d(LOG_TAG,"Switch Cancle!");
                            //cancle
                            if (mPhone.getServiceState().getIsManualSelection()) {
                                mAutoSelectSwitch.setChecked(false);
                            }else{
                                mAutoSelectSwitch.setChecked(true);
                            }
                        }
                    }
                    if (!getResources().getBoolean( R.bool.feature_phone_show_search_network_switch_on)){
                        mSearchButton.setEnabled(true);
                        /* MODIFIED-END by bo.chen,BUG-3000255*/
                    }
                    break;
                    /* MODIFIED-END by bo.chen,BUG-2670018*/
                case EVENT_AUTO_SELECT_DONE:
                    if (DBG) log("hideProgressPanel");

                    // Always try to dismiss the dialog because activity may
                    // be moved to background after dialog is shown.
                    try {
                        dismissDialog(DIALOG_NETWORK_AUTO_SELECT);
                    } catch (IllegalArgumentException e) {
                        // "auto select" is always trigged in foreground, so "auto select" dialog
                        //  should be shown when "auto select" is trigged. Should NOT get
                        // this exception, and Log it.
                        Log.w(LOG_TAG, "[NetworksList] Fail to dismiss auto select dialog ", e);
                    }
                    getPreferenceScreen().setEnabled(true);

                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {

                        /* MODIFIED-BEGIN by bo.chen, 2016-09-27,BUG-3000255*/
                        if (getResources().getBoolean( R.bool.feature_phone_show_search_network_switch_on)){
                          if(mPhone.getServiceState().getIsManualSelection()){
                              mAutoSelectSwitch.setChecked(false);
                          }else{
                              mAutoSelectSwitch.setChecked(true);
                          }
                        }

                        if (DBG) log("automatic network selection: failed!");
                        displayNetworkSelectionFailed(ar.exception);
                    } else {
                        if (DBG) log("automatic network selection: succeeded!");

                        if (getResources().getBoolean( R.bool.feature_phone_show_search_network_switch_on)){
                            mAutoSelectSwitch.setChecked(true);
                       }
                       /* MODIFIED-END by bo.chen,BUG-3000255*/

                        displayNetworkSelectionSucceeded();
                    }

                    break;
            }

            return;
        }
    };

    /**
     * Service connection code for the NetworkQueryService.
     * Handles the work of binding to a local object so that we can make
     * the appropriate service calls.
     */

    /** Local service interface */
    private INetworkQueryService mNetworkQueryService = null;

    /** Service connection */
    private final ServiceConnection mNetworkQueryServiceConnection = new ServiceConnection() {

        /** Handle the task of binding the local object to the service */
        public void onServiceConnected(ComponentName className, IBinder service) {
            if (DBG) log("connection created, binding local service.");
            mNetworkQueryService = ((NetworkQueryService.LocalBinder) service).getService();
            // as soon as it is bound, run a query.
            /* MODIFIED-BEGIN by bo.chen, 2016-08-04,BUG-2670018*/
            if (getApplicationContext().getResources().getBoolean(
                    R.bool.config_disable_data_manual_plmn) ) {

                    /* MODIFIED-BEGIN by bo.chen, 2016-09-27,BUG-3000255*/
                    if (getResources().getBoolean( R.bool.feature_phone_show_search_network_switch_on)){
                    if(mPhone.getServiceState().getIsManualSelection()){
                        Log.d(LOG_TAG,"now is manual mode");
                        Message onCompleteMsg = mHandler.obtainMessage(EVENT_NETWORK_DATA_MANAGER_DONE);
                        if(mDataManager != null && builder1 !=  null){
                        mDataManager.updateDataState(false, onCompleteMsg,builder1);
                    }
                }
                } else {
                    mSearchButton.setEnabled(false);
                    Message onCompleteMsg = mHandler.obtainMessage(EVENT_NETWORK_DATA_MANAGER_DONE);
                    mDataManager.updateDataState(false, onCompleteMsg);
                }
            } else {
                    if (getResources().getBoolean( R.bool.feature_phone_show_search_network_switch_on)){
                      if (mPhone.getServiceState().getIsManualSelection()){
                        loadNetworksList();
                      }
                    } else {
                        loadNetworksList();
                    }
                    /* MODIFIED-END by bo.chen,BUG-3000255*/

            }
            /* MODIFIED-END by bo.chen,BUG-2670018*/
        }

        /** Handle the task of cleaning up the local binding */
        public void onServiceDisconnected(ComponentName className) {
            if (DBG) log("connection disconnected, cleaning local binding.");
            mNetworkQueryService = null;
        }
    };

    /**
     * This implementation of INetworkQueryServiceCallback is used to receive
     * callback notifications from the network query service.
     */
    private final INetworkQueryServiceCallback mCallback = new INetworkQueryServiceCallback.Stub() {

        /** place the message on the looper queue upon query completion. */
        public void onQueryComplete(List<OperatorInfo> networkInfoArray, int status) {
            if (DBG) log("notifying message loop of query completion.");
            Message msg = mHandler.obtainMessage(EVENT_NETWORK_SCAN_COMPLETED,
                    status, 0, networkInfoArray);
            msg.sendToTarget();
        }
    };

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean handled = false;


        /* MODIFIED-BEGIN by bo.chen, 2016-09-27,BUG-3000255*/
        if (getResources().getBoolean( R.bool.feature_phone_show_search_network_switch_on) && (preference == mAutoSelectSwitch)){
            Log.d(LOG_TAG,"mAutoSelectSwitch.isChecked()" + mAutoSelectSwitch.isChecked() + "isDataDisableRequired" + isDataDisableRequired());
            if(!mAutoSelectSwitch.isChecked()){
                mAutoSelectSwitch.setChecked(false);
                showDialog(DIALOG_CLOSE_AUTO);
            }else{
                selectNetworkAutomatic();
            }
        } else if (preference == mSearchButton) {

// [BUGFIX]-Add-BEGIN by TCTNB.wen.ye,12/01/2015,defect-1002152
/* MODIFIED-END by bo.chen,BUG-3000255*/
            if (getApplicationContext().getResources().getBoolean(
                    R.bool.config_disable_data_manual_plmn)) {
                mSearchButton.setEnabled(false);
                Message onCompleteMsg = mHandler.obtainMessage(EVENT_NETWORK_DATA_MANAGER_DONE);
                mDataManager.updateDataState(false, onCompleteMsg);
            } else {
            loadNetworksList();
            }
            /* MODIFIED-END by bo.chen,BUG-2670018*/
            handled = true;
        } else if (preference == mAutoSelect) {
            selectNetworkAutomatic();
            handled = true;
        } else {
            Preference selectedCarrier = preference;

            String networkStr = selectedCarrier.getTitle().toString();
            if (DBG) log("selected network: " + networkStr);

            Message msg = mHandler.obtainMessage(EVENT_NETWORK_SELECTION_DONE);
            Phone phone = PhoneFactory.getPhone(mPhoneId);
            if (phone != null) {
                phone.selectNetworkManually(mNetworkMap.get(selectedCarrier), true, msg);
                displayNetworkSeletionInProgress(networkStr);
                handled = true;
            } else {
                log("Error selecting network. phone is null.");
            }


        }

        return handled;
    }

    //implemented for DialogInterface.OnCancelListener
    public void onCancel(DialogInterface dialog) {
        // request that the service stop the query with this callback object.
        try {
            mNetworkQueryService.stopNetworkQuery(mCallback);
        } catch (RemoteException e) {
            log("onCancel: exception from stopNetworkQuery " + e);
        }
        finish();
    }

    public String getNormalizedCarrierName(OperatorInfo ni) {
        if (ni != null) {
            return ni.getOperatorAlphaLong() + " (" + ni.getOperatorNumeric() + ")";
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mUm = (UserManager) getSystemService(Context.USER_SERVICE);

        if (mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS)) {
            setContentView(R.layout.telephony_disallowed_preference_screen);
            mUnavailable = true;
            return;
        }

        /* MODIFIED-BEGIN by bo.chen, 2016-09-27,BUG-3000255*/
        if (getResources().getBoolean( R.bool.feature_phone_show_search_network_switch_on)){
            addPreferencesFromResource(R.xml.carrier_select_auto);
        } else {
            addPreferencesFromResource(R.xml.carrier_select);
        }
        mPhone = PhoneUtils.getPhoneFromIntent(getIntent());
        /* MODIFIED-END by bo.chen,BUG-3000255*/


        int subId;
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            subId = intent.getExtras().getInt(GsmUmtsOptions.EXTRA_SUB_ID);
            if (SubscriptionManager.isValidSubscriptionId(subId)) {
                mPhoneId = SubscriptionManager.getPhoneId(subId);
            }
        }


        /* MODIFIED-BEGIN by bo.chen, 2016-09-27,BUG-3000255*/
        if (getResources().getBoolean( R.bool.feature_phone_show_search_network_switch_on)){
            mNetworkList = (PreferenceGroup) getPreferenceScreen().findPreference(SEARCH_NETWORKS_KEY);
        } else {
            mNetworkList = (PreferenceGroup) getPreferenceScreen().findPreference(LIST_NETWORKS_KEY);
        }

        mNetworkMap = new HashMap<Preference, OperatorInfo>();
        mRatMap = new HashMap<String, String>();
        netowrkList = new PreferenceCategory(this);
        initRatMap();
        if (getResources().getBoolean( R.bool.feature_phone_show_search_network_switch_on)){
            builder1 = createDia();
            mAutoSelectSwitch = (SwitchPreference) getPreferenceScreen().findPreference(BUTTON_AUTO_SELECT_SWITCH_KEY);
         } else {
            mSearchButton = getPreferenceScreen().findPreference(BUTTON_SRCH_NETWRKS_KEY);
            mAutoSelect = getPreferenceScreen().findPreference(BUTTON_AUTO_SELECT_KEY);
         }
         /* MODIFIED-END by bo.chen,BUG-3000255*/


        // Start the Network Query service, and bind it.
        // The OS knows to start he service only once and keep the instance around (so
        // long as startService is called) until a stopservice request is made.  Since
        // we want this service to just stay in the background until it is killed, we
        // don't bother stopping it from our end.
        startService (new Intent(this, NetworkQueryService.class));
        bindService (new Intent(this, NetworkQueryService.class).setAction(
                NetworkQueryService.ACTION_LOCAL_BINDER),
                mNetworkQueryServiceConnection, Context.BIND_AUTO_CREATE);
        /* MODIFIED-BEGIN by bo.chen, 2016-09-27,BUG-3000255*/
        log("********isDataDisableRequired*********" + isDataDisableRequired());

        log("********config_disable_data_manual_plmn*********" + getApplicationContext().getResources().getBoolean(
                R.bool.config_disable_data_manual_plmn));
        /* MODIFIED-BEGIN by bo.chen, 2016-08-04,BUG-2670018*/
        if (getApplicationContext().getResources().getBoolean(
                R.bool.config_disable_data_manual_plmn) ) {
            mDataManager = new NetworkSettingDataManager(getApplicationContext());
        }
        /* MODIFIED-END by bo.chen,BUG-2670018*/

        registerForServiceStateChanged();

    }

    @Override
    public void onResume() {
        super.onResume();

        if (getResources().getBoolean( R.bool.feature_phone_show_search_network_switch_on)){
            builder1 = createDia();
            boolean isManualSelection = mPhone.getServiceState().getIsManualSelection();
            Log.d(LOG_TAG,"onResume isManual = " + isManualSelection);
            if (isManualSelection) {
                mAutoSelectSwitch.setChecked(false);
                //loadNetworksList();
            } else if (!mSwitchFlag) {
                mAutoSelectSwitch.setChecked(false);
            } else {
                mAutoSelectSwitch.setChecked(true);
                clearList();
            }
        }
        /* MODIFIED-END by bo.chen,BUG-3000255*/

        mIsForeground = true;
        /* Defect - Liyi.Ding - 3273799- DUT can't make PLMN manually during CALL*/
        TelecomManager telecomManager =  (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
        boolean isInCall=telecomManager.isInCall();
        if(isInCall && !isFinishing() ){
            showDialog(DIALOG_CLOSE_AUTO_DIAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsForeground = false;
    }

    /**
     * Override onDestroy() to unbind the query service, avoiding service
     * leak exceptions.
     */
    @Override
    protected void onDestroy() {
        try {
            /* MODIFIED-BEGIN by bo.chen, 2016-08-04,BUG-2670018*/
            if (mNetworkQueryService != null && mCallback != null)
                log("onDestroy  stopNetworkQuery");
                mNetworkQueryService.stopNetworkQuery(mCallback);
        } catch (RemoteException e) {
            log("onDestroy  RemoteException ");
        }
        try {
        /* MODIFIED-END by bo.chen,BUG-2670018*/
            // used to un-register callback
            mNetworkQueryService.unregisterCallback(mCallback);
        } catch (RemoteException e) {
            log("onDestroy: exception from unregisterCallback " + e);
        }


        /* MODIFIED-BEGIN by bo.chen, 2016-09-27,BUG-3000255*/
        unregisterForServiceStateChanged();

        if (!mUnavailable) {
            // unbind the service.
            unbindService(mNetworkQueryServiceConnection);
        }
        /* MODIFIED-BEGIN by bo.chen, 2016-08-04,BUG-2670018*/

        if (mDataManager != null && builder1 != null) {
            mDataManager.updateDataState(true, null,builder1);
        } else if (mDataManager != null) {
        /* MODIFIED-END by bo.chen,BUG-3000255*/
            mDataManager.updateDataState(true, null);
        }

        /* MODIFIED-END by bo.chen,BUG-2670018*/
        super.onDestroy();
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        /* MODIFIED-BEGIN by bo.chen, 2016-09-27,BUG-3000255*/
        DialogInterface.OnClickListener onNegativeClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAutoSelectSwitch.setChecked(true);
                dialog.dismiss();
            }
        };
        /* MODIFIED-END by bo.chen,BUG-3000255*/


        if ((id == DIALOG_NETWORK_SELECTION) || (id == DIALOG_NETWORK_LIST_LOAD) ||
                (id == DIALOG_NETWORK_AUTO_SELECT)) {
            ProgressDialog dialog = new ProgressDialog(this);
            switch (id) {
                case DIALOG_NETWORK_SELECTION:
                    // It would be more efficient to reuse this dialog by moving
                    // this setMessage() into onPreparedDialog() and NOT use
                    // removeDialog().  However, this is not possible since the
                    // message is rendered only 2 times in the ProgressDialog -
                    // after show() and before onCreate.
                    dialog.setMessage(mNetworkSelectMsg);
                    dialog.setCancelable(false);
                    dialog.setIndeterminate(true);
                    break;
                case DIALOG_NETWORK_AUTO_SELECT:
                    dialog.setMessage(getResources().getString(R.string.register_automatically));
                    dialog.setCancelable(false);
                    dialog.setIndeterminate(true);
                    break;
                case DIALOG_NETWORK_LIST_LOAD:
                default:
                    // reinstate the cancelablity of the dialog.
                    dialog.setMessage(getResources().getString(R.string.load_networks_progress));
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setOnCancelListener(this);
                    break;
            }
            return dialog;

            /* MODIFIED-BEGIN by bo.chen, 2016-09-27,BUG-3000255*/
            }else if(id == DIALOG_CLOSE_AUTO){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getResources().getString(
                        R.string.network_auto_select_turn_off_warning));
                builder.setTitle(getResources().getString(
                        R.string.manual_search_cancel_alert_title));
                builder.setPositiveButton(getText(R.string.dialog_to_turnoff),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //ok
                                if(isDataDisableRequired()){
                                    mDataManager = new NetworkSettingDataManager(getApplicationContext());
                                    Message onCompleteMsg = mHandler.obtainMessage(EVENT_NETWORK_DATA_MANAGER_DONE);
                                    if(builder1 != null){
                                        mDataManager.updateDataState(false, onCompleteMsg,builder1);
                                    }
                                }else{
                                    Message onCompleteMsg = mHandler.obtainMessage(EVENT_NETWORK_DATA_MANAGER_DONE);
                                }
                            }
                        });
                builder.setNegativeButton(getString(android.R.string.cancel), onNegativeClickListener);
                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                return dialog;
            /* Defect - Liyi.Ding - 3273799- DUT can't make PLMN manually during CALL*/
            }else if(id == DIALOG_CLOSE_AUTO_DIAL){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getResources().getString(
                        R.string.dial_search_cancel_alert_message));
                builder.setNegativeButton(getString(android.R.string.ok), new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                         finish();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                return dialog;
            }else if(id == DIALOG_TOOPEN_AUTO){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getResources().getString(
                        R.string.service_not_available_text));
                builder.setTitle(getResources().getString(
                        R.string.service_not_available));
                builder.setPositiveButton(getText(R.string.dialog_to_turnon),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                    //If manual network selection failed, try automatic registration.
                                mAutoSelectSwitch.setChecked(true);
                                    log("********selectNetworkAutomatic*********");
                                    selectNetworkAutomatic();
                            }
                        });
                builder.setNegativeButton(getString(android.R.string.cancel), new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        }
                });
                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                return dialog;
                /* MODIFIED-END by bo.chen,BUG-3000255*/

        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        if ((id == DIALOG_NETWORK_SELECTION) || (id == DIALOG_NETWORK_LIST_LOAD) ||
                (id == DIALOG_NETWORK_AUTO_SELECT)) {
            // when the dialogs come up, we'll need to indicate that
            // we're in a busy state to dissallow further input.
            getPreferenceScreen().setEnabled(false);
        }
    }

    private void displayEmptyNetworkList(boolean flag) {
        mNetworkList.setTitle(flag ? R.string.empty_networks_list : R.string.label_available);
    }

    private void displayNetworkSeletionInProgress(String networkStr) {
        // TODO: use notification manager?
        mNetworkSelectMsg = getResources().getString(R.string.register_on_network, networkStr);

        if (mIsForeground) {
            showDialog(DIALOG_NETWORK_SELECTION);
        }
    }

    private void displayNetworkQueryFailed(int error) {
        String status = getResources().getString(R.string.network_query_error);

        //[Task-3309780] Add begin by TCTNB.jie.qiu, 2016-11-04,[Network] Sometimes UE shows search network error
        //optimize, only display error message when it is in network search activity
        if (!mIsForeground){ return; }
        //[Task-3309780] Add end by TCTNB.jie.qiu

        final PhoneGlobals app = PhoneGlobals.getInstance();
        app.notificationMgr.postTransientNotification(
                NotificationMgr.NETWORK_SELECTION_NOTIFICATION, status);
    }

    /* MODIFIED-BEGIN by bo.chen, 2016-08-04,BUG-2670018*/
    private void displayNetworkQueryForbidden() {
        String status = getResources().getString(R.string.network_query_forbidden);

        final PhoneGlobals app = PhoneGlobals.getInstance();
        app.notificationMgr.postTransientNotification(
                NotificationMgr.NETWORK_SELECTION_NOTIFICATION, status);
    }
    /* MODIFIED-END by bo.chen,BUG-2670018*/
    private void displayNetworkSelectionFailed(Throwable ex) {
        String status;

        //[Task-3309780] Add begin by TCTNB.jie.qiu, 2016-11-03,[Network] Sometimes UE shows search network error
        //optimize, only display error message when it is in network search activity
        if (!mIsForeground){ return; }
        //[Task-3309780] Add end by TCTNB.jie.qiu

        if ((ex != null && ex instanceof CommandException) &&
                ((CommandException)ex).getCommandError()
                  == CommandException.Error.ILLEGAL_SIM_OR_ME)
        {
            status = getResources().getString(R.string.not_allowed);
        } else {
            status = getResources().getString(R.string.connect_later);
        }

        final PhoneGlobals app = PhoneGlobals.getInstance();
        app.notificationMgr.postTransientNotification(
                NotificationMgr.NETWORK_SELECTION_NOTIFICATION, status);

        TelephonyManager tm = (TelephonyManager) app.getSystemService(Context.TELEPHONY_SERVICE);
        Phone phone = PhoneFactory.getPhone(mPhoneId);
        if (phone != null) {
            ServiceState ss = tm.getServiceStateForSubscriber(phone.getSubId());
            if (ss != null) {
                app.notificationMgr.updateNetworkSelection(ss.getState());
            }
        }
    }

    private void displayNetworkSelectionSucceeded() {
        String status = getResources().getString(R.string.registration_done);

        final PhoneGlobals app = PhoneGlobals.getInstance();
        app.notificationMgr.postTransientNotification(
                NotificationMgr.NETWORK_SELECTION_NOTIFICATION, status);

        mHandler.postDelayed(new Runnable() {
            public void run() {
                finish();
            }
        }, 3000);
    }

    private void loadNetworksList() {
        if (DBG) log("load networks list...");

        /* MODIFIED-BEGIN by bo.chen, 2016-09-27,BUG-3000255*/
        mSwitchFlag = false;

        if (mIsForeground) {
            showDialog(DIALOG_NETWORK_LIST_LOAD);
        }

        // delegate query request to the service.
        try {
           if (mNetworkQueryService != null) {
                mNetworkQueryService.startNetworkQuery(mCallback, mPhoneId);
           }
           /* MODIFIED-END by bo.chen,BUG-3000255*/
        } catch (RemoteException e) {
            log("loadNetworksList: exception from startNetworkQuery " + e);
            if (mIsForeground) {
                try {
                    dismissDialog(DIALOG_NETWORK_LIST_LOAD);
                } catch (IllegalArgumentException e1) {
                    // do nothing
                }
            }
        }

        displayEmptyNetworkList(false);
    }

    /**
     * networksListLoaded has been rewritten to take an array of
     * OperatorInfo objects and a status field, instead of an
     * AsyncResult.  Otherwise, the functionality which takes the
     * OperatorInfo array and creates a list of preferences from it,
     * remains unchanged.
     */
    private void networksListLoaded(List<OperatorInfo> result, int status) {
        if (DBG) log("networks list loaded");

        // used to un-register callback
        try {
            mNetworkQueryService.unregisterCallback(mCallback);
        } catch (RemoteException e) {
            log("networksListLoaded: exception from unregisterCallback " + e);
        }

        // update the state of the preferences.
        if (DBG) log("hideProgressPanel");

        // Always try to dismiss the dialog because activity may
        // be moved to background after dialog is shown.
        try {
            dismissDialog(DIALOG_NETWORK_LIST_LOAD);
        } catch (IllegalArgumentException e) {
            // It's not a error in following scenario, we just ignore it.
            // "Load list" dialog will not show, if NetworkQueryService is
            // connected after this activity is moved to background.
            if (DBG) log("Fail to dismiss network load list dialog " + e);
        }

        /* MODIFIED-BEGIN by bo.chen, 2016-08-04,BUG-2670018*/
        if (mDataManager != null) {
            /* MODIFIED-BEGIN by bo.chen, 2016-09-27,BUG-3000255*/
            mDataManager.updateDataState(true, null, builder1);
        }else if (mDataManager != null) {
        /* MODIFIED-END by bo.chen,BUG-3000255*/
            mDataManager.updateDataState(true, null);
        }
        /* MODIFIED-END by bo.chen,BUG-2670018*/
        getPreferenceScreen().setEnabled(true);
        clearList();

        if (status != NetworkQueryService.QUERY_OK) {
            if (DBG) log("error while querying available networks");
            /* MODIFIED-BEGIN by bo.chen, 2016-09-27,BUG-3000255*/
            if (getResources().getBoolean( R.bool.feature_phone_show_search_network_switch_on)){
                selectNetworkAutomatic();
                mAutoSelectSwitch.setChecked(true);
            }
            /* MODIFIED-END by bo.chen,BUG-3000255*/
            displayNetworkQueryFailed(status);
            displayEmptyNetworkList(true);
        } else {
            if (result != null){
                displayEmptyNetworkList(false);

                // create a preference for each item in the list.
                // just use the operator name instead of the mildly
                // confusing mcc/mnc.
                /* MODIFIED-BEGIN by bo.chen, 2016-09-27,BUG-3000255*/
                if (getResources().getBoolean( R.bool.feature_phone_show_search_network_switch_on)){
                    netowrkList.setTitle(R.string.label_available);
                    mNetworkList.addPreference(netowrkList);
                }
                /* MODIFIED-END by bo.chen,BUG-3000255*/
                for (OperatorInfo ni : result) {
                    Preference carrier = new Preference(this, null);
//[SOLUTION]-Add-BEGIN by TCTNB.(JiangLong Pan), 08/05/2016, SOLUTION-2473999
//Antel LTE  instead of "Antel 4G" on manual search

                    String networkTitle = getNetworkTitle(ni);
                    String networkRat = mRatMap.get(ni.getRadioTech());
                    String networkState = ni.getState().toString().toLowerCase();
                   if ("74801".equals(ni.getOperatorNumeric())) {
                       if ("4G".equalsIgnoreCase(networkRat)) {
                           networkTitle = "Antel LTE";
                       }
                   }
//[SOLUTION]-Add-END by TCTNB.(JiangLong Pan)

                    /* MODIFIED-BEGIN by sunyandong, 2016-08-09,BUG-2695862*/
                    //add SDM to controll if operator names should be divided by RAT or not
                    if (getResources().getBoolean( com.android.internal.R.bool.feature_tctfw_RatDivideOperatorName_on) == true
                                 && !TextUtils.isEmpty(ni.getOperatorNumeric())&& !TextUtils.isEmpty(networkRat)) {
                        Log.d("NetworkingSetting","RatDivideOperatorName operatorNumeric ="+ ni.getOperatorNumeric()
                                 + "networkRat = " + networkRat);
                        //Manual Network Selection shows "Yoigo" network twice when camped under "movistar" networ
                        if ("21407".equals(ni.getOperatorNumeric())) {
                            Log.d("NetworkingSetting","networksListLoaded old networkTitle =" + networkTitle+",mPhoneId = "+mPhoneId);
                            String simNumeric = TelephonyManager.getDefault().getSimOperatorNumericForPhone(mPhoneId);
                            if (!TextUtils.isEmpty(simNumeric)&& simNumeric.equals("21404")) {
                                networkTitle = "Movistar "+networkRat;
                            }
                            Log.d("NetworkingSetting","networksListLoaded Yoigo's new networkTitle =" + networkTitle);
                        }
                    }
                    /* MODIFIED-END by sunyandong,BUG-2695862*/

                    /* MODIFIED-BEGIN by yinbimin, 2016-08-11,BUG-2701990*/
                    if (getResources().getBoolean( com.android.internal.R.bool.feature_tctfw_DisableSPNinClaro_on) == true
                         && !TextUtils.isEmpty(ni.getOperatorNumeric())
                         && !TextUtils.isEmpty(networkRat)) {
                         log("DisableSPNinClaro operatorNumeric =" + ni.getOperatorNumeric() + "networkRat = " + networkRat);
                         if ("72405".equals(ni.getOperatorNumeric())) {
                             if ("4G".equalsIgnoreCase(networkRat)) {
                                 networkTitle = "CLARO BR 4G";
                             }
                             else if("3G".equalsIgnoreCase(networkRat)){
                                 networkTitle = "CLARO BR 3G";
                             }
                             else{
                                 networkTitle = "CLARO BR 2G";
                             }
                         }
                    }
                    /* MODIFIED-END by yinbimin,BUG-2701990*/

                    /* MODIFIED-BEGIN by wen.ye, 2016-08-08,BUG-2693043*/
                    if(networkTitle.equals("CMCC")||networkTitle.equals("CHINA MOBILE")||networkTitle.equals("CHINA  MOBILE")){
                        networkTitle=getResources().getString(R.string.network_china_mobile);
                    }else if(networkTitle.equals("UNICOM")||networkTitle.equals("CHN-UNICOM")){
                       networkTitle=getResources().getString(R.string.network_china_unicom);
                    }

                    if(networkState.equals("current")){
                        networkState=getResources().getString(R.string.network_state_current);
                    }else if(networkState.equals("forbidden")){
                        networkState=getResources().getString(R.string.network_state_forbidden);
                    }else if(networkState.equals("available")){
                        networkState=getResources().getString(R.string.network_state_available);
                    }else if(networkState.equals("unknown")){
                        networkState=getResources().getString(R.string.network_state_unknow);
                    }

                     /* MODIFIED-BEGIN by bo.chen, 2016-08-10,BUG-2701766*/
                     if (getResources().getBoolean(R.bool.feature_tctfw_DisplayO2PLUS_on) == true
                             && !TextUtils.isEmpty(ni.getOperatorNumeric())
                             && "26203".equals(ni.getOperatorNumeric())) {
                           String simNumeric = TelephonyManager.getDefault().getSimOperatorNumericForPhone(mPhoneId);
                           if (DBG) log("DisplayO2PLUS operatorNumeric = 26203 , simNumeric =" + simNumeric + ",mPhoneId = "+mPhoneId);
                           if (!TextUtils.isEmpty(simNumeric)
                               && "26207".equals(simNumeric)) {
                               if(!TextUtils.isEmpty(networkRat)){
                                   networkTitle = "o2-de+ " + networkRat;
                               }else{
                                   networkTitle = "o2-de+";
                               }
                           }
                       }
                       /* MODIFIED-END by bo.chen,BUG-2701766*/
                    carrier.setTitle(networkTitle+"("+networkState+")");
                    /* MODIFIED-END by wen.ye,BUG-2693043*/
                    carrier.setPersistent(false);
                    mNetworkList.addPreference(carrier);
                    mNetworkMap.put(carrier, ni);

                    if (DBG) log("  " + ni);
                }
                /* MODIFIED-BEGIN by bo.chen, 2016-09-27,BUG-3000255*/
                mSwitchFlag = false;
            } else {
                displayEmptyNetworkList(true);
            }
        }
    }


    private Builder createDia(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.disconnect_data_confirm_auto);
        return builder;
    }
    private boolean  isDataDisableRequired() {
        boolean isRequired = getApplicationContext().getResources().getBoolean(
                 R.bool.config_disable_data_manual_plmn);
        if ((TelephonyManager.getDefault()
            .getMultiSimConfiguration()
            == TelephonyManager.MultiSimVariants.DSDA) &&
            (SubscriptionManager.getDefaultDataSubscriptionId() != mPhone.getSubId())) {
            isRequired = false;
        }
        return isRequired;
     }
     /* MODIFIED-END by bo.chen,BUG-3000255*/

    /**
     * Returns the title of the network obtained in the manual search.
     *
     * @param OperatorInfo contains the information of the network.
     *
     * @return Long Name if not null/empty, otherwise Short Name if not null/empty,
     * else MCCMNC string.
     */

    private String getNetworkTitle(OperatorInfo ni) {
        String title;

        if (!TextUtils.isEmpty(ni.getOperatorAlphaLong())) {
            title = ni.getOperatorAlphaLong();
        } else if (!TextUtils.isEmpty(ni.getOperatorAlphaShort())) {
            title = ni.getOperatorAlphaShort();
        } else {
            BidiFormatter bidiFormatter = BidiFormatter.getInstance();
            title = bidiFormatter.unicodeWrap(ni.getOperatorNumeric(), TextDirectionHeuristics.LTR);
        }
        if (!ni.getRadioTech().equals(""))
            title += " " + mRatMap.get(ni.getRadioTech());

        if (DBG) log("getNetworkTitle title = " + title);
        return title;
    }

    private void clearList() {
        /* MODIFIED-BEGIN by bo.chen, 2016-09-27,BUG-3000255*/
        mSwitchFlag = true;
        if(mNetworkList != null && netowrkList != null){
            mNetworkList.removePreference(netowrkList);
        }
        /* MODIFIED-END by bo.chen,BUG-3000255*/
        for (Preference p : mNetworkMap.keySet()) {
            mNetworkList.removePreference(p);
        }
        mNetworkMap.clear();
    }

    private void selectNetworkAutomatic() {
        if (DBG) log("select network automatically...");
        if (mIsForeground) {
            showDialog(DIALOG_NETWORK_AUTO_SELECT);
        }

        Message msg = mHandler.obtainMessage(EVENT_AUTO_SELECT_DONE);
        Phone phone = PhoneFactory.getPhone(mPhoneId);
        if (phone != null) {
            phone.setNetworkSelectionModeAutomatic(msg);
        }
    }

    private void initRatMap() {
        mRatMap.put(String.valueOf(ServiceState.RIL_RADIO_TECHNOLOGY_UNKNOWN), "Unknown");
        mRatMap.put(String.valueOf(ServiceState.RIL_RADIO_TECHNOLOGY_GPRS), "2G");
        mRatMap.put(String.valueOf(ServiceState.RIL_RADIO_TECHNOLOGY_EDGE), "2G");
        mRatMap.put(String.valueOf(ServiceState.RIL_RADIO_TECHNOLOGY_UMTS), "3G");
        mRatMap.put(String.valueOf(ServiceState.RIL_RADIO_TECHNOLOGY_IS95A), "2G");
        mRatMap.put(String.valueOf(ServiceState.RIL_RADIO_TECHNOLOGY_IS95B), "2G");
        mRatMap.put(String.valueOf(ServiceState.RIL_RADIO_TECHNOLOGY_1xRTT), "2G");
        mRatMap.put(String.valueOf(ServiceState.RIL_RADIO_TECHNOLOGY_EVDO_0), "3G");
        mRatMap.put(String.valueOf(ServiceState.RIL_RADIO_TECHNOLOGY_EVDO_A), "3G");
        mRatMap.put(String.valueOf(ServiceState.RIL_RADIO_TECHNOLOGY_HSDPA), "3G");
        mRatMap.put(String.valueOf(ServiceState.RIL_RADIO_TECHNOLOGY_HSUPA), "3G");
        mRatMap.put(String.valueOf(ServiceState.RIL_RADIO_TECHNOLOGY_HSPA), "3G");
        mRatMap.put(String.valueOf(ServiceState.RIL_RADIO_TECHNOLOGY_EVDO_B), "3G");
        mRatMap.put(String.valueOf(ServiceState.RIL_RADIO_TECHNOLOGY_EHRPD), "3G");
        mRatMap.put(String.valueOf(ServiceState.RIL_RADIO_TECHNOLOGY_LTE), "4G");
        mRatMap.put(String.valueOf(ServiceState.RIL_RADIO_TECHNOLOGY_HSPAP), "3G");
        mRatMap.put(String.valueOf(ServiceState.RIL_RADIO_TECHNOLOGY_GSM), "2G");
        mRatMap.put(String.valueOf(ServiceState.RIL_RADIO_TECHNOLOGY_TD_SCDMA), "3G");
    }

    private void log(String msg) {
        Log.d(LOG_TAG, "[NetworksList] " + msg);
    }
}
