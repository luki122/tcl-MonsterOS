/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.setupwizard.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.lang.reflect.Method;

import cn.tcl.setupwizard.R;

public class SimUtils {

    public static final String TAG = "SimUtils";

    /**
     * whether support multi SIM cards.
     * @param context
     * @return
     */
    public static boolean isMultiSimEnabled(Context context) {
        boolean isMultiSim = false;
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Class<?> tmClass = tm.getClass();
            Method isMultiSimEnabled = tmClass.getDeclaredMethod("isMultiSimEnabled");
            isMultiSimEnabled.setAccessible(true);
            isMultiSim = (Boolean) isMultiSimEnabled.invoke(tm);
        } catch (Exception e) {
            LogUtils.e(TAG, "isMultiSimEnabled: " + e.toString());
        }
        return isMultiSim;
    }

    /**
     * get the sim count
     * @param context
     * @return
     */
    public static int getSimCount(Context context) {
        try {
            String platform = SystemPropertiesUtil.get("ro.mediatek.platform");
            if (platform.startsWith("MT")) {
                boolean isDualsim = SystemPropertiesUtil.getBoolean("dualsim.ui.support", false);
                if (isDualsim) {
                    LogUtils.e(TAG, "It's MTK platform  dual sim card ");
                    return 2;
                }
            }

            final TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            Class<?> tmClass = tm.getClass();
            Method getSimCountMethod = tmClass.getDeclaredMethod("getSimCount");
            getSimCountMethod.setAccessible(true);
            Integer mNumObject = (Integer) getSimCountMethod.invoke(tm);
            int mNumSlots = mNumObject.intValue();
            LogUtils.e(TAG, "card count =  " + mNumSlots);
            return mNumSlots;
        } catch (Exception e) {
            LogUtils.i(TAG, "getSimCount: " + e.toString());
            return 1;
        }
    }

    /**
     * whether default SIM is enabled.
     * @param context
     * @return
     */
    public static boolean isSimCardEnabled(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = tm.getSimState();
        if (simState == TelephonyManager.SIM_STATE_READY) {
            return true;
        }

        return false;
    }

    /**
     * whether a specified SIM is enabled by slotId.
     * @param context
     * @param slotId
     * @return
     */
    public static boolean isSimCardEnabledBySlotId(Context context, int slotId) {
        if (slotId < 0) {
            return false;
        }
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Class<?> tmClass = tm.getClass();
            Method getSimStateMethod = tmClass.getDeclaredMethod("getSimState", int.class);
            getSimStateMethod.setAccessible(true);
            int simState1 = (Integer) getSimStateMethod.invoke(tm, slotId);
            if (simState1 == TelephonyManager.SIM_STATE_READY) {
                return true;
            }
        } catch (Exception e) {
            LogUtils.i(TAG, "isSimCardExistBySlotId: " + e.toString());
        }

        return false;
    }

    /**
     * whether the mobile data of default SIM is enabled.
     * @param context
     * @return
     */
    public static boolean getDataEnabled(Context context) {
        boolean isDataEnabled = false;
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Class<?> tmClass = tm.getClass();
            Method getDataEnabledMethod = tmClass.getDeclaredMethod("getDataEnabled");
            getDataEnabledMethod.setAccessible(true);
            isDataEnabled = (Boolean) getDataEnabledMethod.invoke(tm);
        } catch (Exception e) {
            LogUtils.i(TAG, "getDataEnabled: " + e.toString());
        }
        return isDataEnabled;
    }

    /**
     * whether the mobile data of a specified SIM is enabled.
     * @param context
     * @param slotId
     * @return
     */
    public static boolean getDataEnabledBySlotId(Context context, int slotId) {
        boolean isDataEnabled = false;
        int subId = getSubId(context, slotId);
        try {
            if (subId >= 0) {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                Class<?> tmClass = tm.getClass();
                Method getDataEnabledMethod = tmClass.getDeclaredMethod("getDataEnabled", int.class);
                getDataEnabledMethod.setAccessible(true);
                isDataEnabled = (Boolean) getDataEnabledMethod.invoke(tm, subId);
            }
        } catch (Exception e) {
            LogUtils.i(TAG, "getDataEnabledBySlotId: " + e.toString());
        }
        return isDataEnabled;
    }

    /**
     * set the default mobile data enabled or not.
     * @param context
     * @param enable
     */
    public static void setDataEnabled(Context context, boolean enable) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Class<?> tmClass = tm.getClass();
            Method setDataEnabledMethod = tmClass.getDeclaredMethod("setDataEnabled", boolean.class);
            setDataEnabledMethod.setAccessible(true);
            setDataEnabledMethod.invoke(tm, enable);
        } catch (Exception e) {
            LogUtils.i(TAG, "setDataEnabled: " + e.toString());
        }
    }

    /**
     * set a specified mobile data enabled or not.
     * @param context
     * @param slotId
     * @param enable
     */
    public static void setDataEnabledBySlotId(Context context, int slotId, boolean enable) {
        int subId = getSubId(context, slotId);
        try {
            if (subId >= 0) {

                /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
                if(enable) {
                    setDefaultDataSubId(context, subId);
                }
                /* MODIFIED-END by xinlei.sheng,BUG-2669930*/

                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                Class<?> tmClass = tm.getClass();
                Method setDataEnabledMethod = tmClass.getDeclaredMethod("setDataEnabled", int.class, boolean.class);
                setDataEnabledMethod.setAccessible(true);
                setDataEnabledMethod.invoke(tm, subId, enable);
            } else {
                LogUtils.i(TAG, "setDataEnabledBySlotId: subId < 0");
            }
        } catch (Exception e) {
            LogUtils.i(TAG, "setDataEnabledBySlotId: " + e.toString());
        }
    }

    /**
     * set default data subId
     * @param subId
     */
    /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
    public static void setDefaultDataSubId(Context context, int subId) {
        try {
            if (subId >= 0) {
                Class<?> subMClass = Class.forName("android.telephony.SubscriptionManager");
                Method fromMethod = subMClass.getDeclaredMethod("from", Context.class);
                fromMethod.setAccessible(true);
                Object sm = fromMethod.invoke(null, context);
                if (sm != null) {
                    Method setDefaultDataMethod = subMClass.getDeclaredMethod("setDefaultDataSubId", int.class);
                    setDefaultDataMethod.setAccessible(true);
                    setDefaultDataMethod.invoke(sm, subId);
                } else {
                    LogUtils.i(TAG, "SubscriptionManager instance is null!");
                }
            } else {
                LogUtils.i(TAG, "setDefaultDataSubId: subId < 0");
            }
        } catch (Exception e) {
            LogUtils.i(TAG, "setDefaultDataSubId: " + e.toString());
            e.printStackTrace();
            /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
        }
    }

    /**
     * return the network type of default SIM.
     * @param context
     * @return
     */
    public static int getNetworkType(Context context) {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = tm.getNetworkType();
        return networkType;
    }

    /**
     * return the network type of a specified SIM by slotId.
     * @param context
     * @param slotId
     * @return
     */
    public static int getNetworkTypeBySlotId(Context context, int slotId) {
        int networkType = 0;
        int subId = getSubId(context, slotId);

        try {
            if (subId >= 0) {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                Class<?> tmClass = tm.getClass();
                Method operatorMethod = tmClass.getDeclaredMethod("getNetworkType", int.class);
                operatorMethod.setAccessible(true);
                networkType = (Integer) operatorMethod.invoke(tm, subId);
                LogUtils.i(TAG, "getNetworkTypeBySlotId: networkType = " + networkType);
            }
        } catch (Exception e) {
            LogUtils.i(TAG, "getNetworkTypeBySlotId: " + e.toString()); // MODIFIED by xinlei.sheng, 2016-09-12,BUG-2669930
        }

        return networkType;
    }

    /*
     * get the operator name of a SIM by MCC+MNC.
     * @param operator
     * @return
     */
    private static String getSimOperator(String operator, Context context) {
        String name;
        if (operator.startsWith("46000") || operator.startsWith("46002")||operator.startsWith("46007")) {
            name = context.getString(R.string.china_mobile);
        } else if (operator.startsWith("46001")||operator.startsWith("46006")) {
            name = context.getString(R.string.china_unicom);
        } else if (operator.startsWith("46003")||operator.startsWith("46005")||operator.startsWith("46011")) {
            name = context.getString(R.string.china_telecom);
        } else {
            name = "unknown";
        }
        return name;
    }

    /**
     * get the operator name of the default SIM.
     * @return
     */
    public static String getSimOperatorName(Context context) {
        if (!isSimCardEnabled(context)) {
            return "sim is disabled";
        }

        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String operatorName = tm.getSimOperatorName();
        String operator = tm.getSimOperator();
        if (!TextUtils.isEmpty(operatorName)) {
            LogUtils.i(TAG, "getSimOperatorName: operatorName = " + operatorName);
            return operatorName;
        }

        if (!TextUtils.isEmpty(operator)) {
            LogUtils.i(TAG, "getSimOperatorName: operator = " + operator);
            return getSimOperator(operator, context);
        }

        String IMSI = tm.getSubscriberId();
        if (!TextUtils.isEmpty(IMSI)) {
            LogUtils.i(TAG, "getSimOperatorName: IMSI = " + IMSI);
            return getSimOperator(IMSI, context);
        }

        return "unknown";
    }

    /**
     * get the operator name of a specified SIM by slotId.
     * @param context
     * @param slotId
     * @return
     */
    public static String getSimOperatorNameBySlotId(Context context, int slotId) {
        if (!isSimCardEnabledBySlotId(context, slotId)) {
            LogUtils.e(TAG, "getSimOperatorNameBySlotId: sim card is disabled");
            return "sim is disabled";
        }

        String operator = "";
        int subId = getSubId(context, slotId);

        try {
            if (subId >= 0) {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                Class<?> tmClass = tm.getClass();
                Method operatorMethod = tmClass.getDeclaredMethod("getSimOperator", int.class);
                operatorMethod.setAccessible(true);
                operator = (String) operatorMethod.invoke(tm, subId);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "getSimOperatorNameBySlotId: " + e.toString());
        }

        if (!TextUtils.isEmpty(operator)) {
            LogUtils.i(TAG, "getSimOperatorNameBySlotId: operator = " + operator);
            return getSimOperator(operator, context);
        } else {
            return "unknown";
        }
    }

    private static boolean isIccExist(Context context, int slotId) {
        // boolean isIccExist = false;
        // final TelephonyManager mTelephonyManager =
        // (TelephonyManager)
        // context.getSystemService(Context.TELEPHONY_SERVICE);
        // Class<?> tmClass = mTelephonyManager.getClass();
        // try {
        // Method checkIccMethod = tmClass.getDeclaredMethod("hasIccCard",
        // int.class);
        // checkIccMethod.setAccessible(true);
        // isIccExist = (Boolean) checkIccMethod.invoke(mTelephonyManager,
        // slotId);
        // } catch (Exception e) {
        // e.printStackTrace();
        // try {
        // Method checkIccMethod = tmClass.getDeclaredMethod("hasIccCard",
        // long.class);
        // checkIccMethod.setAccessible(true);
        // isIccExist = (Boolean) checkIccMethod.invoke(mTelephonyManager,
        // slotId);
        // } catch (Exception e2) {
        // e2.printStackTrace();
        // }
        // }
        //
        // return isIccExist;
        return true;
    }

    /*
     * get the subId of a specified SIM by slotId.
     *
     * @param context
     * @param slotId
     * @return
     */
    private static int getSubId(Context context, int slotId) {
        int subId = -1;
        try {
            if (isIccExist(context, slotId)) {
                Class<?> subMClass = Class.forName("android.telephony.SubscriptionManager");
                Method getSubIdMethod = subMClass.getDeclaredMethod("getSubId", int.class);
                getSubIdMethod.setAccessible(true);
                int[] subIds = (int[]) getSubIdMethod.invoke(null, slotId);
                if (subIds != null) {
                    LogUtils.i(TAG, "getSubId: subIds[0] = " + subIds[0]);
                    return subIds[0];
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "getSubId: " + e.toString());
        }
        LogUtils.e(TAG, "getSubId: subId = " + subId);
        return subId;
    }

    /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
    public static int getDataDefaultSim(int sim1NetworkType, int sim2NetworkType) {
        if (sim1NetworkType >= sim2NetworkType) {
            return 0;
        } else {
            return 1;
        }
    }

    private static boolean isApplicationOnIcc(int phoneId, AppType appType) {
        LogUtils.i(TAG, "isApplicationOnIcc: phoneId = " + phoneId + ", appType = " + appType);
        boolean isUSIM = false;
        try {
            Class<?> uiccClass = Class.forName("com.android.internal.telephony.uicc.UiccController");
            Method getInstanceMethod = uiccClass.getDeclaredMethod("getInstance");
            getInstanceMethod.setAccessible(true);
            Object uiccController = getInstanceMethod.invoke(null);
            if (uiccController != null) {
                LogUtils.i(TAG, "isApplicationOnIcc: uiccController = " + uiccController);
                Class<?> uiccCardClass = Class.forName("com.android.internal.telephony.uicc.UiccCard");
                Method getUiccCardMethod = uiccCardClass.getMethod("getUiccCard", int.class);
                getUiccCardMethod.setAccessible(true);
                Object uiccCard = getUiccCardMethod.invoke(uiccController, phoneId);
                if (uiccCard != null) {
                    Method isApplicationONIccMethod = uiccCardClass.getMethod("isApplicationOnIcc", AppType.class);
                    isApplicationONIccMethod.setAccessible(true);
                    isUSIM = (Boolean)isApplicationONIccMethod.invoke(uiccCard, appType);
                    LogUtils.i(TAG, "isApplicationOnIcc: isUSIM = " + isUSIM);
                } else {
                    LogUtils.e(TAG, "isApplicationOnIcc: UiccCard instance is null");
                }
            } else {
                LogUtils.e(TAG, "isApplicationOnIcc: UiccController instance is null");
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "isApplicationOnIcc: occur exception, " + e.getMessage());
            e.printStackTrace();
        } finally {
            return isUSIM;
        }
    }

    public static int getDefaultSimId() {
        if (isApplicationOnIcc(0, AppType.APPTYPE_SIM) && isApplicationOnIcc(1,AppType.APPTYPE_USIM)) {
            return 1;
        } else {
            return 0;
        }
    }

    public enum AppType{
        APPTYPE_UNKNOWN,
        APPTYPE_SIM,
        APPTYPE_USIM,
        APPTYPE_RUIM,
        APPTYPE_CSIM,
        APPTYPE_ISIM
    }
    /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
}
