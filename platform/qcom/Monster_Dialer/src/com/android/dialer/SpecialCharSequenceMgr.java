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

package com.android.dialer;

import android.app.Activity;
import mst.app.dialog.AlertDialog;
import android.app.DialogFragment;
import android.app.KeyguardManager;
import mst.app.dialog.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Looper;
import android.provider.Settings;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.common.io.MoreCloseables;
import com.android.contacts.common.database.NoNullCursorAsyncQueryHandler;
import com.android.contacts.common.widget.SelectPhoneAccountDialogFragment;
import com.android.contacts.common.widget.SelectPhoneAccountDialogFragment.SelectPhoneAccountListener;
import com.android.dialer.calllog.PhoneAccountUtils;
import com.android.dialer.util.TelecomUtil;
import com.android.internal.telephony.ConfigResourceUtil;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import android.os.SystemProperties;

import android.os.Build;
//Add by guangchao.su for Task  1778217
import android.telephony.SubscriptionManager;
import java.lang.reflect.Method;
//Add by guangchao.su for Defect  1653731
import android.content.pm.PackageManager.NameNotFoundException;
//Add by guangchao.su for Task 1921305
import java.io.File;
import java.io.FileInputStream;

import com.android.internal.telephony.TelephonyProperties; // MODIFIED by guangchao.su, 2016-05-19,BUG-1990874
/**
 * Helper class to listen for some magic character sequences
 * that are handled specially by the dialer.
 *
 * Note the Phone app also handles these sequences too (in a couple of
 * relatively obscure places in the UI), so there's a separate version of
 * this class under apps/Phone.
 *
 * TODO: there's lots of duplicated code between this class and the
 * corresponding class under apps/Phone.  Let's figure out a way to
 * unify these two classes (in the framework? in a common shared library?)
 */
public class SpecialCharSequenceMgr {
    private static final String TAG = "SpecialCharSequenceMgr";
    
    //[BUGFIX]-Add-BEGIN by Yandong.Sun,09/24/2015,Task-527275
    //the device will always register the first ehplmn(it is a special requirement)
    static final String TCT_PROPERTY_ICC_EHPLMN ="gsm.telephony.ehplmn";
    //[BUGFIX]-Add-END by Yandong.Sun

    //[TASK] Add Begin by TCTNB Guoan.chen,11/16/2015, TASK-884051
    //get carrier name from sim
    static String PROPERTY_ICC_SUB0_OPERATOR_ALPHA = "gsm.sim.sub0.operator.alpha";

    static String PROPERTY_ICC_SUB1_OPERATOR_ALPHA = "gsm.sim.sub1.operator.alpha";
    //[TASK] Add End by TCTNB.Guoan.Chen

    //[BUGFIX]-Mod-BEGIN by TCTNB.Xijun.Zhang,05/18/2016,Defect-2151886,
    //Record IMEI by SystemProperties.
    static final String PROPERTY_SLOT0_IMEI = "persist.tct.slot0.imei";
    static final String PROPERTY_SLOT1_IMEI = "persist.tct.slot1.imei";
    //[BUGFIX]-Mod-END by TCTNB.Xijun.Zhang,05/18/2016,Defect-2151886

    private static final String TAG_SELECT_ACCT_FRAGMENT = "tag_select_acct_fragment";

    private static final String SECRET_CODE_ACTION = "android.provider.Telephony.SECRET_CODE";
    private static final String MMI_IMEI_DISPLAY = "*#06#";
    private static final String MMI_REGULATORY_INFO_DISPLAY = "*#07#";
    private static final int IMEI_14_DIGIT = 14;
    //Add by guangchao.su for Task 1921305
    private static final String MMI_CALLDURATION_COUNT_DISPLAY = "###232#";
    private static final String CALLDURATION_COUNT_FILE_PATH = "/tctpersist/phone/calltimesaver";


    private static final String MMI_SWVERSION_DISPLAY = "*#3228#";
    private static final String MMITEST_MODE = "*#2886#";
    //Add BEGIN by guangchao.su for Task 1759079
    private static final String VERSION_INFO_DISPLAY = "*#837837#";
    //Add by guangchao.su for Defect  1653731
    private static final String VERSION_INFO_DISPLAY_CN = "*#0000#";
    //[BUGFIX]-Add-BEGIN by TCTNB.Xijun.Zhang,11/09/2016,Task-3337917
    private static final String NETWORK_SETTINGS_TEST_MODE = "*#789#";
    //[BUGFIX]-Add-END by TCTNB.Xijun.Zhang,11/09/2016,Task-3337917

    //[FEATURE]-Add-BEGIN by TCTNB.93709,23/02/2016,ALM:1652173,Diag Protect
    private static final String DIAGPROTECTOR_MODE = "###2324#";
    public static final String PROPERTY_DIAGPROTECT = "persist.sys.usb.protect";
    public static final String ACTION_USB_PROTECT_CHANGED = "sys.usbprotect.changed";
    //[FEATURE]-Add-END by TCTNB.93709,23/02/2016,ALM:1652173,Diag Protect
    private static boolean isDisplayCNVersion; // MODIFIED by guangchao.su, 2016-06-02,BUG-2222209
    /**
     * Remembers the previous {@link QueryHandler} and cancel the operation when needed, to
     * prevent possible crash.
     *
     * QueryHandler may call {@link ProgressDialog#dismiss()} when the screen is already gone,
     * which will cause the app crash. This variable enables the class to prevent the crash
     * on {@link #cleanup()}.
     *
     * TODO: Remove this and replace it (and {@link #cleanup()}) with better implementation.
     * One complication is that we have SpecialCharSequenceMgr in Phone package too, which has
     * *slightly* different implementation. Note that Phone package doesn't have this problem,
     * so the class on Phone side doesn't have this functionality.
     * Fundamental fix would be to have one shared implementation and resolve this corner case more
     * gracefully.
     */
    private static QueryHandler sPreviousAdnQueryHandler;

    public static class HandleAdnEntryAccountSelectedCallback extends SelectPhoneAccountListener{
        final private TelecomManager mTelecomManager;
        final private QueryHandler mQueryHandler;
        final private SimContactQueryCookie mCookie;

        public HandleAdnEntryAccountSelectedCallback(TelecomManager telecomManager,
                QueryHandler queryHandler, SimContactQueryCookie cookie) {
            mTelecomManager = telecomManager;
            mQueryHandler = queryHandler;
            mCookie = cookie;
        }

        @Override
        public void onPhoneAccountSelected(PhoneAccountHandle selectedAccountHandle,
                boolean setDefault) {
            Uri uri = mTelecomManager.getAdnUriForPhoneAccount(selectedAccountHandle);
            handleAdnQuery(mQueryHandler, mCookie, uri);
            // TODO: Show error dialog if result isn't valid.
        }

    }

    public static class HandleMmiAccountSelectedCallback extends SelectPhoneAccountListener{
        final private Context mContext;
        final private String mInput;
        public HandleMmiAccountSelectedCallback(Context context, String input) {
            mContext = context.getApplicationContext();
            mInput = input;
        }

        @Override
        public void onPhoneAccountSelected(PhoneAccountHandle selectedAccountHandle,
                boolean setDefault) {
            TelecomUtil.handleMmi(mContext, mInput, selectedAccountHandle);
        }
    }

    /** This class is never instantiated. */
    private SpecialCharSequenceMgr() {
    }

    public static boolean handleChars(Context context, String input, EditText textField) {
        //get rid of the separators so that the string gets parsed correctly
        String dialString = PhoneNumberUtils.stripSeparators(input);

        if (handleDeviceIdDisplay(context, dialString)
                || handleRegulatoryInfoDisplay(context, dialString)
                || handlePinEntry(context, dialString)
                // delete by lgy for 3499889
//                || handleAdnEntry(context, dialString, textField)
                || handleMMITestMode(context,dialString)
                || handleSwVersionDisplay(context,dialString)
                || handleSecretCode(context, dialString)
                || handleDiagProtector(context, dialString)//[FEATURE]-Add- by TCTNB.93709,23/02/2016,ALM:1652173,Diag Protect
                //[BUGFIX]-Add by TCTNB.bo.chen,02/18/2016,1644185,
                || handleNetworkModeChange(context,dialString)
                //Add BEGIN by guangchao.su for Task 1759079
                || handleVersionDisplay(context, dialString)
                || handleNetworkSettingsChange(context,dialString)  //[BUGFIX]-Add by TCTNB.Xijun.Zhang,11/09/2016,Task-3337917
                //Add by guangchao.su for Defect  1653731
                //Add by guangchao.su for Task 1921305
//                || handleCallTimeCountDisplay(context, dialString)
                ||handleSwVersionDiaplayForCTCC(context,dialString)) {
            return true;
        }

        return false;
    }
    //Add BEGIN by guangchao.su for Task 1759079
    private static boolean handleVersionDisplay(Context context, String dialString) {
	if (dialString.equals(VERSION_INFO_DISPLAY)) {
            showVersionInfo(context);
            return true;
        }
        return false;
    }

    
    //to do lgy ，there is no software and hardware version
    private static void showVersionInfo(Context context) {
        AlertDialog alert = null;
        String proName = context.getResources()
                .getString(R.string.version_info_dialog_product_name);
        String swVersion = context.getResources().getString(
                R.string.version_info_dialog_software_version);
        String hwVersion = context.getResources().getString(
                R.string.version_info_dialog_hardware_version);
        String m_proName = SystemProperties.get("ro.product.name");
        String m_swVersion = SystemProperties.get("ro.def.software.version");
        String m_hwVersion = SystemProperties.get("ro.def.hardware.version");
        String sw_version = proName + '\n' + m_proName + '\n' + swVersion + '\n' + m_swVersion
                + '\n' + hwVersion + '\n' + m_hwVersion;
        alert = new AlertDialog.Builder(context).setTitle(R.string.version_information)
                .setMessage(sw_version).setPositiveButton(android.R.string.ok,null)
                .setCancelable(false).show();
    }
    //Add END by guangchao.su for Task 1759079

    //Add by guangchao.su for Task 1921305
    static boolean handleCallTimeCountDisplay(Context context, String input) {
        boolean callDuration = true;
        if (callDuration && input.equals(MMI_CALLDURATION_COUNT_DISPLAY)) {
            showCallTimeCountPanel(context);
            return true;
            }
        return false;
        }

  static void showCallTimeCountPanel(Context context) {
        String data = "0";
//        File calltimecount = new File(CALLDURATION_COUNT_FILE_PATH);
        data = getCallDurationTime();

        if (data == null || data.equals("")) {
            data = "0";
        }
        String durationInSec = "0";
        String threeMinitesDate = "";
        String[] time = null;
        if (data.contains("||")) {
            String[] splites = data.toString().split("\\|\\|");
            time = splites[0].split(":");
        } else {
            time = data.split(":");
        }if (time != null && time.length >= 4) {
            durationInSec = time[3];
            threeMinitesDate = (time.length >= 5) ? time[4] : "";
        }

        String show;
        if (!"".equals(threeMinitesDate)) {
            show = "3min: "+threeMinitesDate + '\n' + '\n' +"Total: "+parseDuration(durationInSec);
        } else {
            show = "3min: "+"N/A" + '\n' + '\n' +"Total: "+parseDuration(durationInSec);
        }

        new AlertDialog.Builder(context).setTitle("Call Duration & Date")
                .setMessage(show).setPositiveButton(android.R.string.ok, null)
                .setCancelable(false).show();
    }
    private static String parseDuration(String data) {
        if (data == null || "".equals(data)) {
             return "00:00:00";
         }
         String hourStr, minStr, secStr;
         long time = Long.parseLong(data.trim());
         if (time >= 6000) {
             time = 6000;
         }
         int hour = (int) (time / (60 * 60));
         if (hour < 10) {
             hourStr = "0" + hour;
         } else {
             hourStr = "" + hour;
         }
         int min = (int) ((time % (60 * 60)) / 60);
         if (min < 10) {
             minStr = "0" + min;
         } else {
             minStr = "" + min;
         }
         int sec = (int) (time % 60);
         if (sec < 10) {
             secStr = "0" + sec;
         } else {
             secStr = "" + sec;
         }

         return (hourStr + ":" + minStr + ":" + secStr);
     }

    //Add by guangchao.su for Defect  1653731
    private static boolean handleSwVersionDiaplayForCTCC(Context context, String dialString) {
        /* MODIFIED-BEGIN by guangchao.su, 2016-06-02,BUG-2222209*/
//        isDisplayCNVersion = context.getResources().getBoolean(com.android.internal.R.bool.def_version_info_display_cn_on);
    	isDisplayCNVersion = true;
        if(isDisplayCNVersion){
           if (dialString.equals(VERSION_INFO_DISPLAY_CN)) {
               showVersionInfoForCTCC(context);
               return true ;
           }
        }
        /* MODIFIED-END by guangchao.su,BUG-2222209*/

	return false ;
    }

    /* MODIFIED-BEGIN by guangchao.su, 2016-05-17,BUG-2125842*/
    public static String getOSVersion() {
        String osVersion = "Android " + Build.VERSION.RELEASE;
        return osVersion;
    }
    public static String getTotalMemory() {
         int SIZE_16 = 16;
         int SIZE_32 = 32;
         int SIZE_64 = 64;
         long KB = 1024;
         long MB = KB * 1024;
         long GB = MB * 1024;
         long internalTotalBytes=0;
         long totalspace=0;
         final File path = new File("/data");
         long totalBytes=path.getTotalSpace();
         long totalGBytes=totalBytes / GB;
         if (totalGBytes < SIZE_16) {
             totalspace =SIZE_16;
         } else if (totalGBytes >SIZE_16&& totalGBytes <SIZE_32) {
            totalspace =SIZE_32;
         }else{
            totalspace =SIZE_64;
         }
         return totalspace + "G";
    }

    public static String getMeid(Context context) {
        String meid = "null";
        String phone_meid = Settings.System.getString(context.getContentResolver(),"phone_meid");
        String phone_meid_for_gsm = Settings.System.getString(context.getContentResolver(),"phone_meid_for_gsm");
        if(phone_meid!=null) {
          return phone_meid;
        } else if(phone_meid_for_gsm!=null){
          return phone_meid_for_gsm;
        }
        return null;
    }

    private static void showVersionInfoForCTCC (Context context) {
    	AlertDialog alert = null;
        String proName = context.getResources()
                .getString(R.string.version_info_dialog_product_name);
        String swVersion = context.getResources().getString(
                R.string.version_info_dialog_software_version);
        String hwVersion = context.getResources().getString(
                R.string.version_info_dialog_hardware_version);
        String m_proName = SystemProperties.get("ro.product.model");
        /* MODIFIED-BEGIN by guangchao.su, 2016-05-17,BUG-2125842*/
        String imei_Code = "IMEI1";
        String imei_Code1 = "IMEI2";
        String sid_Code = "SID";
        String meid_Code = "MEID";
        String nid_Code = "NID";
        String base_Code = "BaseID";
        String uim_Code = "UIM ID";
        String memroy_Code = context.getResources()
                .getString(R.string.version_info_dialog_memroy);
        String osversion_Code = context.getResources()
                .getString(R.string.version_info_dialog_osversion);
        String prl_Code = context.getResources()
                .getString(R.string.version_info_dialog_version);

        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        /* MODIFIED-BEGIN by guangchao.su, 2016-05-19,BUG-1990874*/
//        String imei_name1 = SystemProperties.get(PROPERTY_SLOT1_IMEI, "");
//        String imei_name  =  SystemProperties.get(PROPERTY_SLOT0_IMEI, "");
      String imei_name1 = telephonyManager.getImei(1);
      String imei_name  =  telephonyManager.getImei(0);
        /* MODIFIED-END by guangchao.su,BUG-1990874*/
        String sid_name = "";
//        String meid_name = getMeid(context);
        int[] subId=SubscriptionManager.getSubId(0);
        int type;
        if(subId==null||subId.length==0) {
           type=TelephonyManager.PHONE_TYPE_GSM;
        }else{
           type = telephonyManager.getCurrentPhoneType(subId[0]);
        }
        String meid_name = "";
        if (type == TelephonyManager.PHONE_TYPE_CDMA) {
        	meid_name =   telephonyManager.getDeviceId(0);
        }
        
        String nid_name = "";
        String base_name = "";
        String memroy_name = getTotalMemory();
        String osversion_memroy = getOSVersion();
        String prl_memroy = "";
        String uim_name = "";
        /* MODIFIED-END by guangchao.su,BUG-2125842*/

        String m_swVersion = null;
        try {
            /* MODIFIED-BEGIN by guangchao.su, 2016-05-12,BUG-2125842*/
            m_swVersion = SystemProperties.get("ro.def.ct.software.version");
        } catch (Exception e) {
        /* MODIFIED-END by guangchao.su,BUG-2125842*/
            e.printStackTrace();
        }
        String m_hwVersion = SystemProperties.get("ro.def.hardware.version");
        String sw_version = proName + '\n' + m_proName + '\n' + swVersion + '\n' + m_swVersion
                /* MODIFIED-BEGIN by guangchao.su, 2016-05-17,BUG-2125842*/
                + '\n' + hwVersion + '\n' + m_hwVersion + '\n'  + prl_Code + '\n'  +prl_memroy+ '\n'
                + osversion_Code + '\n'  +osversion_memroy + '\n' + memroy_Code + '\n' +memroy_name + '\n'
                + sid_Code + '\n'  +sid_name + '\n' + nid_Code + '\n'  +nid_name + '\n'
                + meid_Code + '\n'  +meid_name + '\n' + base_Code + '\n' +base_name + '\n'
                + imei_Code + '\n'  +imei_name + '\n'  +imei_Code1 + '\n'  +imei_name1 + '\n'
                + uim_Code + '\n'  +uim_name;
                /* MODIFIED-END by guangchao.su,BUG-2125842*/
        alert = new AlertDialog.Builder(context).setTitle(R.string.version_information)
                .setMessage(sw_version).setPositiveButton(android.R.string.ok,null)
                .setCancelable(false).show();
    }

    //[FEATURE]-Add-BEGIN by TCTNB.93709,23/02/2016,ALM:1652173,Diag Protect
    //to do lgy, there is no persist.sys.usb.protect prop
    private static boolean handleDiagProtector(Context context, String dialString) {
        if (dialString.equals(DIAGPROTECTOR_MODE)) { // 0=disable, 1=enable
          Log.i(TAG, "handleDiagProtector");
          Intent intent = new Intent(ACTION_USB_PROTECT_CHANGED);
          String strDiagProtect = SystemProperties.get(PROPERTY_DIAGPROTECT, "1");
          Log.i(TAG, "93391 property_diagprotect=" + strDiagProtect);
          if(strDiagProtect.equals("1")) {
              intent.putExtra("usbprotect", "0");
          } else {
              intent.putExtra("usbprotect", "1");
          }
          //packages/apps/Settings/src/com/android/settings/DiagProtectorReceiver.java
          context.sendBroadcast(intent);

          String show = new String();
          if(strDiagProtect.equals("1")) {
             show = context.getResources().getString(R.string.diagprotect_off);
          } else {
             show = context.getResources().getString(R.string.diagprotect_on);
          }
          AlertDialog alert = new AlertDialog.Builder(context)
                .setTitle(R.string.diagprotect_title)
                .setMessage(show)
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .show();

          return true;
        }
        return false;
    }
    //[FEATURE]-Add-END by TCTNB.93709,23/02/2016,ALM:1652173,Diag Protect

    /**
     * Cleanup everything around this class. Must be run inside the main thread.
     *
     * This should be called when the screen becomes background.
     */
    public static void cleanup() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Log.wtf(TAG, "cleanup() is called outside the main thread");
            return;
        }

        if (sPreviousAdnQueryHandler != null) {
            sPreviousAdnQueryHandler.cancel();
            sPreviousAdnQueryHandler = null;
        }
    }

    /**
     * Handles secret codes to launch arbitrary activities in the form of *#*#<code>#*#*.
     * If a secret code is encountered an Intent is started with the android_secret_code://<code>
     * URI.
     *
     * @param context the context to use
     * @param input the text to check for a secret code in
     * @return true if a secret code was encountered
     */
    static boolean handleSecretCode(Context context, String input) {
        // Secret codes are in the form *#*#<code>#*#*
        int len = input.length();
        if (len > 8 && input.startsWith("*#*#") && input.endsWith("#*#*")) {
            final Intent intent = new Intent(SECRET_CODE_ACTION,
                    Uri.parse("android_secret_code://" + input.substring(4, len - 4)));
            context.sendBroadcast(intent);
            return true;
        }

        return false;
    }

    //[BUGFIX]-Add-BEGIN by TCTNB.bo.chen,02/18/2016,1644185,
    //secret codes to set network mode list customization
    private static boolean handleNetworkModeChange(Context context, String dialString) {
        if ("*#789#*".equals(dialString)) {
            Log.wtf("*#789#*","networkModechange");
            /*String networkModeFlag = SystemProperties.get("feature.netowrkmode.change","false");
            TctDialerLog.i("bo.chen",networkModeFlag);
            if (networkModeFlag.equals("false")) {
                networkModeFlag = "true";
            } else {
                networkModeFlag = "false";
            }
            SystemProperties.set("feature.netowrkmode.change",networkModeFlag);
            TctDialerLog.i("bo.chen",networkModeFlag);*/
            if (Settings.System.getInt(context.getContentResolver(), "networkmode_flag", 0) == 0) {
                Settings.System.putInt(context.getContentResolver(), "networkmode_flag", 1);
            } else {
                Settings.System.putInt(context.getContentResolver(), "networkmode_flag", 0);
            }
            return true;
        }
        return false;
    }
    //[BUGFIX]-Add-END by TCTNB.bo.chen
    /**
     * Handle ADN requests by filling in the SIM contact number into the requested
     * EditText.
     *
     * This code works alongside the Asynchronous query handler {@link QueryHandler}
     * and query cancel handler implemented in {@link SimContactQueryCookie}.
     */
    static boolean handleAdnEntry(Context context, String input, EditText textField) {
        /* ADN entries are of the form "N(N)(N)#" */
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager == null
                || telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_GSM) {
            return false;
        }

        // if the phone is keyguard-restricted, then just ignore this
        // input.  We want to make sure that sim card contacts are NOT
        // exposed unless the phone is unlocked, and this code can be
        // accessed from the emergency dialer.
        KeyguardManager keyguardManager =
                (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager.inKeyguardRestrictedInputMode()) {
            return false;
        }

        int len = input.length();
        if ((len > 1) && (len < 5) && (input.endsWith("#"))) {
            try {
                // get the ordinal number of the sim contact
                final int index = Integer.parseInt(input.substring(0, len-1));

                // The original code that navigated to a SIM Contacts list view did not
                // highlight the requested contact correctly, a requirement for PTCRB
                // certification.  This behaviour is consistent with the UI paradigm
                // for touch-enabled lists, so it does not make sense to try to work
                // around it.  Instead we fill in the the requested phone number into
                // the dialer text field.

                // create the async query handler
                final QueryHandler handler = new QueryHandler (context.getContentResolver());

                // create the cookie object
                final SimContactQueryCookie sc = new SimContactQueryCookie(index - 1, handler,
                        ADN_QUERY_TOKEN);

                // setup the cookie fields
                sc.contactNum = index - 1;
                sc.setTextField(textField);

                // create the progress dialog
                sc.progressDialog = new ProgressDialog(context);
                sc.progressDialog.setTitle(R.string.simContacts_title);
                sc.progressDialog.setMessage(context.getText(R.string.simContacts_emptyLoading));
                sc.progressDialog.setIndeterminate(true);
                sc.progressDialog.setCancelable(true);
                sc.progressDialog.setOnCancelListener(sc);
                sc.progressDialog.setCanceledOnTouchOutside(false);
                sc.progressDialog.getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

                final TelecomManager telecomManager =
                        (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                List<PhoneAccountHandle> subscriptionAccountHandles =
                        PhoneAccountUtils.getSubscriptionPhoneAccounts(context);

                boolean hasUserSelectedDefault = subscriptionAccountHandles.contains(
                        telecomManager.getDefaultOutgoingPhoneAccount(PhoneAccount.SCHEME_TEL));

                if (subscriptionAccountHandles.size() == 1 || hasUserSelectedDefault) {
                    Uri uri = telecomManager.getAdnUriForPhoneAccount(null);
                    handleAdnQuery(handler, sc, uri);
                } else if (subscriptionAccountHandles.size() > 1){
                    SelectPhoneAccountListener callback =
                            new HandleAdnEntryAccountSelectedCallback(telecomManager, handler, sc);

                    DialogFragment dialogFragment = SelectPhoneAccountDialogFragment.newInstance(R.string.select_phone_account_for_calls,
                            false,
                            subscriptionAccountHandles, callback);
                    dialogFragment.show(((Activity) context).getFragmentManager(),
                            TAG_SELECT_ACCT_FRAGMENT);
                } else {
                    return false;
                }

                return true;
            } catch (NumberFormatException ex) {
                // Ignore
            }
        }
        return false;
    }

    private static void handleAdnQuery(QueryHandler handler, SimContactQueryCookie cookie,
            Uri uri) {
        if (handler == null || cookie == null || uri == null) {
            Log.w(TAG, "queryAdn parameters incorrect");
            return;
        }

        // display the progress dialog
        cookie.progressDialog.show();

        // run the query.
        handler.startQuery(ADN_QUERY_TOKEN, cookie, uri, new String[]{ADN_PHONE_NUMBER_COLUMN_NAME},
                null, null, null);

        if (sPreviousAdnQueryHandler != null) {
            // It is harmless to call cancel() even after the handler's gone.
            sPreviousAdnQueryHandler.cancel();
        }
        sPreviousAdnQueryHandler = handler;
    }

    static boolean handlePinEntry(final Context context, final String input) {
        if ((input.startsWith("**04") || input.startsWith("**05")) && input.endsWith("#")) {
            final TelecomManager telecomManager =
                    (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            List<PhoneAccountHandle> subscriptionAccountHandles =
                    PhoneAccountUtils.getSubscriptionPhoneAccounts(context);
            boolean hasUserSelectedDefault = subscriptionAccountHandles.contains(
                    telecomManager.getDefaultOutgoingPhoneAccount(PhoneAccount.SCHEME_TEL));

            if (subscriptionAccountHandles.size() == 1 || hasUserSelectedDefault) {
                // Don't bring up the dialog for single-SIM or if the default outgoing account is
                // a subscription account.
                return TelecomUtil.handleMmi(context, input, null);
            } else if (subscriptionAccountHandles.size() > 1){
                SelectPhoneAccountListener listener =
                        new HandleMmiAccountSelectedCallback(context, input);

                DialogFragment dialogFragment = SelectPhoneAccountDialogFragment.newInstance(
                        subscriptionAccountHandles, listener);
                dialogFragment.show(((Activity) context).getFragmentManager(),
                        TAG_SELECT_ACCT_FRAGMENT);
            }
            return true;
        }
        return false;
    }

    // TODO: Use TelephonyCapabilities.getDeviceIdLabel() to get the device id label instead of a
    // hard-coded string.
    //Change by guangchao.su for Task  1778217
    static boolean handleDeviceIdDisplay(Context context, String input) {
        /*TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        ConfigResourceUtil mConfigResUtil = new ConfigResourceUtil();

        if (telephonyManager != null && input.equals(MMI_IMEI_DISPLAY)) {
            int labelResId = (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) ?
                    R.string.imei : R.string.meid;

            List<String> deviceIds = new ArrayList<String>();
            for (int slot = 0; slot < telephonyManager.getPhoneCount(); slot++) {
                String deviceId = telephonyManager.getDeviceId(slot);

                boolean enable14DigitImei = false;
                try {
                    enable14DigitImei = mConfigResUtil.getBooleanValue(context,
                            "config_enable_display_14digit_imei");
                } catch(RuntimeException ex) {
                    //do Nothing
                    Log.e(TAG, "Config for 14 digit IMEI not found: " + ex);
                }
                if (enable14DigitImei && deviceId != null
                        && deviceId.length() > IMEI_14_DIGIT) {
                    deviceId = deviceId.substring(0, IMEI_14_DIGIT);
                }

                if (!TextUtils.isEmpty(deviceId)) {
                    deviceIds.add("IMEI"+(slot+1)+":  "+deviceId);
                }
            }

            AlertDialog alert = new AlertDialog.Builder(context)
                    .setTitle(labelResId)
                    .setItems(deviceIds.toArray(new String[deviceIds.size()]), null)
                    .setPositiveButton(android.R.string.ok, null)
                    .setCancelable(false)
                    .show();
            return true;
        }*/
        if (input.equals(MMI_IMEI_DISPLAY)) {
            showDeviceIdPanel(context);
            return true;
        }
        return false;
    }

    //Add by guangchao.su for Task  1778217
    private static void showDeviceIdPanel (Context context) {
        String info = "";
        String cdmaIMEI="";
        //Add by guangchao.su for Defect 1941738
        String cdmaMEID ="";
        int labelId = R.string.imei;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int phonecount = telephonyManager.getPhoneCount();
        StringBuilder[] deviceId = new StringBuilder[phonecount];
        for (int i=0;i<phonecount;i++) {
    	    deviceId[i] = null;
        }

        if (telephonyManager.isMultiSimEnabled()) {
            int[] type = new int[phonecount];
            String[] ids = new String[phonecount];
            boolean multimode = false;
            for (int i=0; i<phonecount; i++) {
                int[] subId=SubscriptionManager.getSubId(i);
                /* MODIFIED-BEGIN by guangchao.su, 2016-05-11,BUG-1990874*/
                if(subId==null||subId.length==0) {
                   type[i]=TelephonyManager.PHONE_TYPE_GSM;
                }else{
                   type[i] = telephonyManager.getCurrentPhoneType(subId[0]);
                }
                /* MODIFIED-END by guangchao.su,BUG-1990874*/
                ids[i] = telephonyManager.getDeviceId(i);
                if (type[i] == TelephonyManager.PHONE_TYPE_CDMA) {
                    // C+G mode
                    multimode = true;
                    cdmaMEID = ids[i]; // MODIFIED by guangchao.su, 2016-04-20,BUG-1941738
                    cdmaIMEI=telephonyManager.getImei(i);
                }
            }
            for (int i=0; i<phonecount; i++) {
                deviceId[i] = new StringBuilder(50);
                deviceId[i].append(ids[i] == null ? "" : ids[i]);
                if(multimode){
//Add by guangchao.su for Defect 1941738
                    String   prefix = (type[i] == TelephonyManager.PHONE_TYPE_GSM)? "IMEI1:" : "MEID:";
                    deviceId[i].insert(0, prefix);
                }

                if (i != telephonyManager.getPhoneCount()-1) {
                    deviceId[i].append("\n");
                }
            }

            for(int i=1; i<phonecount; i++){
                deviceId[0].append(deviceId[i].toString());
            }
            /* MODIFIED-BEGIN by guangchao.su, 2016-05-11,BUG-1990874*/
            if(multimode&&!TextUtils.isEmpty(cdmaIMEI)){
//                String IMEI_CODE2 = SystemProperties.get(PROPERTY_SLOT1_IMEI, "");
//                String IMEI_CODE1 = SystemProperties.get(PROPERTY_SLOT0_IMEI, "");
                deviceId[0]=new StringBuilder("MEID:"+cdmaMEID+"\n"+"IMEI1:"
                        +cdmaIMEI+"\n"+"IMEI2:"+ids[1]);
            }
        } else {
            cdmaMEID = telephonyManager.getDeviceId(); 
            cdmaIMEI = telephonyManager.getImei();
        	if(telephonyManager.getCurrentPhoneType() == TelephonyManager.PHONE_TYPE_CDMA && !TextUtils.isEmpty(cdmaIMEI)) {
        	       deviceId[0]=new StringBuilder("MEID:"+cdmaMEID+"\n"+"IMEI:"
                           +cdmaIMEI);
        	} else {
                deviceId[0]=new StringBuilder(telephonyManager.getDeviceId());	
        	}
        }
        AlertDialog alert = null;
        info = deviceId[0].toString();
        alert=new AlertDialog.Builder(context).setTitle(labelId)
                .setMessage(info)
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .show();

    }

    //Add by guangchao.su for Task  1778217
    private static String getMeidDec(String input){
        if(input == null || "null".equals(input)){
            return "";
        }
        String ret = null;
        ret = transformSerial(input, 16, 10, 8, 10, 8);
        return ret;	}

    private static String transformSerial(String n, int srcBase, int dstBase, int p1Width, int p1Padding, int p2Padding){
        String p1 = lPad(Long.toString(Long.parseLong(n.substring(0,p1Width),srcBase),dstBase), p1Padding, "0");
        String p2 = lPad(Long.toString(Long.parseLong(n.substring(p1Width),srcBase),dstBase), p2Padding, "0");
        String c = p1+p2;
        return c.toUpperCase();
        }

    private static String lPad(String s, int len, String p){
        if(s.length() >= len){
            return s;
        }
        return lPad(p + s, len, p);
    }

    private static boolean handleRegulatoryInfoDisplay(Context context, String input) {
        if (input.equals(MMI_REGULATORY_INFO_DISPLAY)) {
            Log.d(TAG, "handleRegulatoryInfoDisplay() sending intent to settings app");
            Intent showRegInfoIntent = new Intent(Settings.ACTION_SHOW_REGULATORY_INFO);
            try {
                context.startActivity(showRegInfoIntent);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "startActivity() failed: " + e);
            }
            return true;
        }
        return false;
    }

    /*******
     * This code is used to handle SIM Contact queries
     *******/
    private static final String ADN_PHONE_NUMBER_COLUMN_NAME = "number";
    private static final String ADN_NAME_COLUMN_NAME = "name";
    private static final int ADN_QUERY_TOKEN = -1;

    /**
     * Cookie object that contains everything we need to communicate to the
     * handler's onQuery Complete, as well as what we need in order to cancel
     * the query (if requested).
     *
     * Note, access to the textField field is going to be synchronized, because
     * the user can request a cancel at any time through the UI.
     */
    private static class SimContactQueryCookie implements DialogInterface.OnCancelListener{
        public ProgressDialog progressDialog;
        public int contactNum;

        // Used to identify the query request.
        private int mToken;
        private QueryHandler mHandler;

        // The text field we're going to update
        private EditText textField;

        public SimContactQueryCookie(int number, QueryHandler handler, int token) {
            contactNum = number;
            mHandler = handler;
            mToken = token;
        }

        /**
         * Synchronized getter for the EditText.
         */
        public synchronized EditText getTextField() {
            return textField;
        }

        /**
         * Synchronized setter for the EditText.
         */
        public synchronized void setTextField(EditText text) {
            textField = text;
        }

        /**
         * Cancel the ADN query by stopping the operation and signaling
         * the cookie that a cancel request is made.
         */
        public synchronized void onCancel(DialogInterface dialog) {
            // close the progress dialog
            if (progressDialog != null) {
                progressDialog.dismiss();
            }

            // setting the textfield to null ensures that the UI does NOT get
            // updated.
            textField = null;

            // Cancel the operation if possible.
            mHandler.cancelOperation(mToken);
        }
    }

    /**
     * Asynchronous query handler that services requests to look up ADNs
     *
     * Queries originate from {@link #handleAdnEntry}.
     */
    private static class QueryHandler extends NoNullCursorAsyncQueryHandler {

        private boolean mCanceled;

        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        /**
         * Override basic onQueryComplete to fill in the textfield when
         * we're handed the ADN cursor.
         */
        @Override
        protected void onNotNullableQueryComplete(int token, Object cookie, Cursor c) {
            try {
                sPreviousAdnQueryHandler = null;
                if (mCanceled) {
                    return;
                }

                SimContactQueryCookie sc = (SimContactQueryCookie) cookie;

                // close the progress dialog.
                sc.progressDialog.dismiss();

                // get the EditText to update or see if the request was cancelled.
                EditText text = sc.getTextField();
                Context context = sc.progressDialog.getContext(); // MODIFIED by guangchao.su, 2016-06-17,BUG-2360730

                // if the TextView is valid, and the cursor is valid and positionable on the
                // Nth number, then we update the text field and display a toast indicating the
                // caller name.
                if ((c != null) && (text != null) && (c.moveToPosition(sc.contactNum))) {
                    String name = c.getString(c.getColumnIndexOrThrow(ADN_NAME_COLUMN_NAME));
                    String number =
                            c.getString(c.getColumnIndexOrThrow(ADN_PHONE_NUMBER_COLUMN_NAME));

                    /* MODIFIED-BEGIN by guangchao.su, 2016-06-17,BUG-2360730*/
                    if(number != null){
                        text.getText().replace(0, 0, number);
                     // display the name as a toast
                        name = context.getString(R.string.menu_callNumber, name);
                        Toast.makeText(context, name, Toast.LENGTH_SHORT)
                            .show();
                    }
                    /* MODIFIED-END by guangchao.su,BUG-2360730*/
                }
            } finally {
                MoreCloseables.closeQuietly(c);
            }
        }

        public void cancel() {
            mCanceled = true;
            // Ask AsyncQueryHandler to cancel the whole request. This will fail when the query is
            // already started.
            cancelOperation(ADN_QUERY_TOKEN);
        }
    }

    static boolean handleSwVersionDisplay(Context context, String input) {
        if (input.equals(MMI_SWVERSION_DISPLAY)) {
            showSwVersionPanel(context);
            return true;
        }
        return false;
    }

    static void showSwVersionPanel(Context context) {
        String boot_ver = SystemProperties.get("ro.tct.boot.ver");
        String sys_ver = SystemProperties.get("ro.tct.sys.ver");
        String recovery_ver = SystemProperties.get("ro.tct.reco.ver");
        String mod_ver = SystemProperties.get("ro.tct.non.ver");
        String kernelconfig  = SystemProperties.get("ro.tct.kernelconfig","");
        
        String x_file_name  = SystemProperties.get("ro.tct.sml.ver","");
        //Change by guangchao.su for Task 1785204
//Change by guangchao.su for Defect 1778721
        String sw_version =  boot_ver+'\n'+
                        sys_ver+'\n'+
                        recovery_ver+'\n'+
                        mod_ver /*+ ("".equals(x_file_name) ? "" : ('\n' + x_file_name))*/ + '\n' + kernelconfig;
        String custpack_ver = SystemProperties.get("ro.tct.cust.ver");
        if (custpack_ver != null && !custpack_ver.contains("????????")) {
            sw_version +='\n'+custpack_ver;
        }
        AlertDialog alert = new AlertDialog.Builder(context)
                                 .setTitle("Image Mapping")
                                 .setMessage(sw_version)
                                 .setPositiveButton(android.R.string.ok, null)
                                 .setCancelable(false)
                                .show();
    }

    private static boolean handleMMITestMode(Context context, String dialString) {
        if (dialString.equals(MMITEST_MODE)) {
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.setClassName("com.android.mmi", "com.android.mmi.MMITest");
                try {
                    context.startActivity(i);
                } catch(Exception e) {
                    Log.i("SpecialCharSequenceMgr", "handleMMITestMode Exception = " + e);
                }
            return true;
        }
        return false;
    }
    
    // Task824155-qinglin.fan@tcl.com-begin
    //to do lgy , the file is empty
    private static String getCallDurationTime(){
             try {
                 File calltimecount = new File(CALLDURATION_COUNT_FILE_PATH);
                 if (calltimecount.exists()) {
                   FileInputStream in = new FileInputStream(calltimecount);
                   StringBuffer sb = new StringBuffer();
                   int ch = 0;
                   while ((ch = in.read()) != -1) {
                     sb.append((char) ch);
                   }
                   in.close();
                   return sb.toString();
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
         return "";
    }
  // Task824155-qinglin.fan@tcl.com-end
    
    //[BUGFIX]-Add-BEGIN by TCTNB.Xijun.Zhang,11/09/2016,Task-3337917
    //secret codes to set network mode list customization
    private static boolean handleNetworkSettingsChange(Context context, String dialString) {
        if (NETWORK_SETTINGS_TEST_MODE.equals(dialString)) {
            if (Settings.System.getInt(context.getContentResolver(), "network_settings_test_mode_flag", 0) == 0) {
                Settings.System.putInt(context.getContentResolver(), "network_settings_test_mode_flag", 1);
            } else {
                Settings.System.putInt(context.getContentResolver(), "network_settings_test_mode_flag", 0);
            }
            return true;
        }
        return false;
    }
    //[BUGFIX]-Add-END by TCTNB.Xijun.Zhang,11/09/2016,Task-3337917
}
