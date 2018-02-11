package com.android.phone;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import mst.preference.Preference;
import mst.preference.PreferenceScreen;
import android.telephony.CarrierConfigManager;
import android.util.Log;
import android.view.MenuItem;

import com.android.internal.telephony.Phone;

import java.util.ArrayList;

//[SOLUTION]-Add-BEGIN by TCTNB.(JiangLong Pan), 08/12/2016, SOLUTION-2475185
//Call waiting status
//Call waiting status is missing
import com.android.internal.telephony.Phone;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import mst.app.dialog.ProgressDialog;
import mst.app.dialog.AlertDialog;
import static com.android.internal.telephony.CommandsInterface.SERVICE_CLASS_DATA;
import static com.android.internal.telephony.CommandsInterface.SERVICE_CLASS_DATA_ASYNC;
import static com.android.internal.telephony.CommandsInterface.SERVICE_CLASS_DATA_SYNC;
import static com.android.internal.telephony.CommandsInterface.SERVICE_CLASS_FAX;
import static com.android.internal.telephony.CommandsInterface.SERVICE_CLASS_MAX;
import static com.android.internal.telephony.CommandsInterface.SERVICE_CLASS_PACKET;
import static com.android.internal.telephony.CommandsInterface.SERVICE_CLASS_PAD;
import static com.android.internal.telephony.CommandsInterface.SERVICE_CLASS_SMS;
import static com.android.internal.telephony.CommandsInterface.SERVICE_CLASS_VOICE;
//[SOLUTION]-Add-END by TCTNB.(JiangLong Pan)

public class GsmUmtsAdditionalCallOptions extends TimeConsumingPreferenceActivity {
    private static final String LOG_TAG = "GsmUmtsAdditionalCallOptions";
    private final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 2);

    private static final String BUTTON_CLIR_KEY  = "button_clir_key";
    private static final String BUTTON_CW_KEY    = "button_cw_key";

    private CLIRListPreference mCLIRButton;
    private CallWaitingCheckBoxPreference mCWButton;

    private final ArrayList<Preference> mPreferences = new ArrayList<Preference>();
    private int mInitIndex = 0;
    private Phone mPhone;
    private SubscriptionInfoHelper mSubscriptionInfoHelper;

//[SOLUTION]-Add-BEGIN by TCTNB.(JiangLong Pan), 08/12/2016, SOLUTION-2475185
//Call waiting status
    //Call waiting status is missing
    private static final String BUTTON_CW_STATUS = "query_cw_status";
    private Preference queryCallWaitingScreen;
    ProgressDialog dialog;
    private MyHandler myHandler = new MyHandler();

      private class MyHandler extends Handler {
          static final int MESSAGE_GET_CALL_WAITING = 0;
          @Override
          public void handleMessage(Message msg) {
              switch (msg.what) {
                  case MESSAGE_GET_CALL_WAITING:
                      handleGetCallWaitingResponse(msg);
                      break;
                  default:
                      break;
              }
          }

          private void handleGetCallWaitingResponse(Message msg) {
              if (dialog != null) dialog.dismiss();//[BUGFIX]Mod-by TCTNB.Yuanming.Li,01/02/2014,PR-579795
              AsyncResult ar = (AsyncResult) msg.obj;
              if (ar.exception != null) {
                  if (DBG) {
                      Log.d(LOG_TAG, "handleGetCallWaitingResponse: ar.exception=" + ar.exception);
                  }
              } else if (ar.userObj instanceof Throwable) {
                  if (DBG) {
                      Log.d(LOG_TAG, "handleGetCallWaitingResponse: ar.userObj=" + ar.userObj);
                  }
              } else {
                  if (DBG) {
                      Log.d(LOG_TAG, "handleGetCallWaitingResponse: CW state successfully queried.");
                  }
                  int[] cwArray = (int[])ar.result;
                  boolean isTelecel = getResources().getBoolean(R.bool.feature_callwaitingstring_forTelcel_on);
                  try {
                      StringBuilder sb = new StringBuilder();
                      if(mCWButton != null){
                          if(mCWButton.isChecked()){
                              sb.append(getText(com.android.internal.R.string.serviceEnabledFor));
                          }else{
                              if (isTelecel ){
                                  sb.append(getText(R.string.serviceDisabledForTelecel));
                              } else {
                                  sb.append(getText(com.android.internal.R.string.serviceDisabled));
                              }
                          }
                      }
                      if(isTelecel){
                          sb.append("\n");
                          sb.append(serviceClassToCFString(SERVICE_CLASS_VOICE));
                      }else {
                          for (int classMask = 1; classMask <= SERVICE_CLASS_MAX; classMask <<= 1) {
                              if ((classMask & cwArray[1]) != 0) {
                                  sb.append("\n");
                                  sb.append(serviceClassToCFString(classMask & cwArray[1]));
                              }
                          }
                      }

                      if (GsmUmtsAdditionalCallOptions.this != null && !GsmUmtsAdditionalCallOptions.this.isFinishing()) {
                          new AlertDialog.Builder(GsmUmtsAdditionalCallOptions.this)
                              .setTitle(getString(com.android.internal.R.string.CwMmi))
                              .setMessage(sb.toString()).setPositiveButton(android.R.string.ok, null)
                              .setCancelable(true).show();
                      }
                  } catch (ArrayIndexOutOfBoundsException e) {
                      Log.e(LOG_TAG, "handleGetCallWaitingResponse: improper result: err ="
                              + e.getMessage());
                  }
              }
          }
      }

      private CharSequence serviceClassToCFString (int serviceClass) {
          switch (serviceClass) {
              case SERVICE_CLASS_VOICE:
                  return getText(com.android.internal.R.string.serviceClassVoice);
              case SERVICE_CLASS_DATA:
                  return getText(com.android.internal.R.string.serviceClassData);
              case SERVICE_CLASS_FAX:
                  return getText(com.android.internal.R.string.serviceClassFAX);
              case SERVICE_CLASS_SMS:
                  return getText(com.android.internal.R.string.serviceClassSMS);
              case SERVICE_CLASS_DATA_SYNC:
                  return getText(com.android.internal.R.string.serviceClassDataSync);
              case SERVICE_CLASS_DATA_ASYNC:
                  return getText(com.android.internal.R.string.serviceClassDataAsync);
              case SERVICE_CLASS_PACKET:
                  return getText(com.android.internal.R.string.serviceClassPacket);
              case SERVICE_CLASS_PAD:
                  return getText(com.android.internal.R.string.serviceClassPAD);
              default:
                  return null;
          }
      }

      @Override
      public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
          if(queryCallWaitingScreen != null && preference == queryCallWaitingScreen){
              queryCWStatus();
          }
          return false;
      }

      private void queryCWStatus() {
          mPhone.getCallWaiting(myHandler.obtainMessage(MyHandler.MESSAGE_GET_CALL_WAITING,
                      MyHandler.MESSAGE_GET_CALL_WAITING, MyHandler.MESSAGE_GET_CALL_WAITING));
          dialog = new ProgressDialog(this);
          dialog.setTitle(getString(com.android.internal.R.string.CwMmi));
          dialog.setIndeterminate(true);
          dialog.setCancelable(false);
          dialog.setMessage(getText(R.string.reading_settings));
          dialog.show();
      }

      @Override
      protected void onDestroy() {
          if (dialog != null && dialog.isShowing()) {
              dialog.dismiss();
          }
          super.onDestroy();
      }
//[SOLUTION]-Add-END by TCTNB.(JiangLong Pan)

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.gsm_umts_additional_options);

        mSubscriptionInfoHelper = new SubscriptionInfoHelper(this, getIntent());
        mSubscriptionInfoHelper.setActionBarTitle(
                getActionBar(), getResources(), R.string.additional_gsm_call_settings_with_label);
        mPhone = mSubscriptionInfoHelper.getPhone();

        PreferenceScreen prefSet = getPreferenceScreen();
        mCLIRButton = (CLIRListPreference) prefSet.findPreference(BUTTON_CLIR_KEY);
        mCWButton = (CallWaitingCheckBoxPreference) prefSet.findPreference(BUTTON_CW_KEY);

//[SOLUTION]-Add-BEGIN by TCTNB.(JiangLong Pan), 08/12/2016, SOLUTION-2475185
//Call waiting status
        //Call waiting status is missing
        queryCallWaitingScreen = prefSet.findPreference(BUTTON_CW_STATUS);
        boolean isShowCWStatus = this.getResources().getBoolean(R.bool.def_feature_call_waiting_status);
        if (!isShowCWStatus) {
            if (queryCallWaitingScreen != null) {
                prefSet.removePreference(queryCallWaitingScreen);
                queryCallWaitingScreen = null;
            }
        }
//[SOLUTION]-Add-END by TCTNB.(JiangLong Pan)

        mPreferences.add(mCLIRButton);
        mPreferences.add(mCWButton);

        if (icicle == null) {
            if (DBG) Log.d(LOG_TAG, "start to init ");
            if (isUtEnabledToDisableClir()) {
                mCLIRButton.setSummary(R.string.sum_default_caller_id);
                mCWButton.init(this, false, mPhone);
            } else {
                mCLIRButton.init(this, false, mPhone);
            }
        } else {
            if (DBG) Log.d(LOG_TAG, "restore stored states");
            mInitIndex = mPreferences.size();
            if (isUtEnabledToDisableClir()) {
                mCLIRButton.setSummary(R.string.sum_default_caller_id);
                mCWButton.init(this, true, mPhone);
            } else {
                mCLIRButton.init(this, true, mPhone);
                mCWButton.init(this, true, mPhone);
                int[] clirArray = icicle.getIntArray(mCLIRButton.getKey());
                if (clirArray != null) {
                    if (DBG) Log.d(LOG_TAG, "onCreate:  clirArray[0]="
                            + clirArray[0] + ", clirArray[1]=" + clirArray[1]);
                    mCLIRButton.handleGetCLIRResult(clirArray);
                } else {
                    mCLIRButton.init(this, false, mPhone);
                }
            }
        }

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // android.R.id.home will be triggered in onOptionsItemSelected()
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        
    	   if(mCLIRButton != null) { 
  			 prefSet.removePreference(mCLIRButton);
  	   }
    }

    private boolean isUtEnabledToDisableClir() {
        boolean skipClir = false;
        CarrierConfigManager configManager = (CarrierConfigManager)
            getSystemService(Context.CARRIER_CONFIG_SERVICE);
        PersistableBundle pb = configManager.getConfigForSubId(mPhone.getSubId());
        if (pb != null) {
            skipClir = pb.getBoolean("config_disable_clir_over_ut");
        }
        return mPhone.isUtEnabled() && skipClir;
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mCLIRButton.clirArray != null) {
            outState.putIntArray(mCLIRButton.getKey(), mCLIRButton.clirArray);
        }
    }

    @Override
    public void onFinished(Preference preference, boolean reading) {
        if (mInitIndex < mPreferences.size()-1 && !isFinishing()) {
            mInitIndex++;
            Preference pref = mPreferences.get(mInitIndex);
            if (pref instanceof CallWaitingCheckBoxPreference) {
                ((CallWaitingCheckBoxPreference) pref).init(this, false, mPhone);
            }
        }
        super.onFinished(preference, reading);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {  // See ActionBar#setDisplayHomeAsUpEnabled()
            //[DEFECT]-ADD-BEGIN by TCTNB chuanjun.chen,09/12/2016,defect-2896261.
            //CallFeaturesSetting.goUpToTopLevelSetting(this, mSubscriptionInfoHelper);
            onBackPressed();
            //[DEFECT]-ADD-END by TCTNB chuanjun.chen.
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
