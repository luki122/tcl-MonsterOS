package com.monster.netmanage.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by shengbx on 2/2/16.
 */
public class DataFlowUtil {

    public static long getDayOfMonthBegin(int paramInt) {
        Calendar localCalendar = Calendar.getInstance();
        localCalendar.set(Calendar.DAY_OF_MONTH, paramInt);
        localCalendar.set(Calendar.HOUR_OF_DAY, 0);
        localCalendar.set(Calendar.MINUTE, 0);
        localCalendar.set(Calendar.SECOND, 0);
        return localCalendar.getTimeInMillis();
    }

    public static long getDayOfMonthEnd(int paramInt) {
        Calendar localCalendar = Calendar.getInstance();
        localCalendar.set(Calendar.DAY_OF_MONTH, paramInt);
        localCalendar.set(Calendar.HOUR_OF_DAY, 23);
        localCalendar.set(Calendar.MINUTE, 59);
        localCalendar.set(Calendar.SECOND, 59);
        return localCalendar.getTimeInMillis();
    }

    public static long getMonthBegin() {
        Calendar localCalendar = Calendar.getInstance();
        localCalendar.set(Calendar.DAY_OF_MONTH, 1);
        localCalendar.set(Calendar.HOUR_OF_DAY, 0);
        localCalendar.set(Calendar.MINUTE, 0);
        localCalendar.set(Calendar.SECOND, 0);
        return localCalendar.getTimeInMillis();
    }

    public static long getMonthEnd() {
        Calendar localCalendar = Calendar.getInstance();
        localCalendar.set(Calendar.DAY_OF_MONTH, localCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        localCalendar.set(Calendar.HOUR_OF_DAY, 23);
        localCalendar.set(Calendar.MINUTE, 59);
        localCalendar.set(Calendar.SECOND, 59);
        return localCalendar.getTimeInMillis();
    }

//    public static long getTodayBegin() {
//        Time time = new Time();
//        time.setToNow();
//        time.second = 0;
//        time.minute = 0;
//        time.hour = 0;
//        return time.toMillis(true);
//    }
//
//    public static long getTodayEnd() {
//        Time time = new Time();
//        time.setToNow();
//        time.hour = 23;
//        time.second = 59;
//        time.minute = 59;
//        return time.toMillis(true);
//    }

    /**
     * @param slotIndex 卡槽
     * @return
     */
    public static String getIccId(Context context, int slotIndex) {
        SubscriptionInfo info = SubscriptionManager.from(context).getActiveSubscriptionInfoForSimSlotIndex(slotIndex);
        return info.getIccId();
    }


//    public static String getSubscriberIdFromTele(Context paramContext, int slotId) {
//        if (!DataFlowUtil.isIccCardActivated(slotId)) {
//            return null;
//        }
//        int[] subId = SubscriptionManager.getSubId(slotId);
//        if (subId.length > 0) {
//            int l = subId[0];
//            if (l == -1) {
//                SystemClock.sleep(200);
//                l = SubscriptionManager.getSubId(slotId)[0];
//            }
//            return TelephonyManager.from(paramContext).getSubscriberId(l);
//        } else {
//            throw new RuntimeException("Error slotId");
//        }
////        for (String str = TelephonyManager.from(paramContext).getSubscriberId(l); ; str = "")
////            return str;
//    }

//    public static boolean isIccCardActivated(int subscription) {
//        TelephonyManager tm = TelephonyManager.getDefault();
////        log("isIccCardActivated subscription: "+ subscription + " SimState: "+ tm.getSimState(subscription));
//        int state = tm.getSimState(subscription);
//        return (state != TelephonyManager.SIM_STATE_ABSENT)
//                && (state != TelephonyManager.SIM_STATE_UNKNOWN)
//                && (state != TelephonyManager.SIM_STATE_NOT_READY);
//    }

    public static List<ApplicationInfo> getNetApps(Context context, List netAppList) {

//        ArrayList netAppList = new ArrayList();
        if(context == null){
            return null;
        }
        List<String> nameList = new ArrayList<String>();
        PackageInfo systemInfo = null;
        PackageManager manager = context.getPackageManager();
//        List<ApplicationInfo> launcherAppsList = getLauncherApps(context);
        List<ApplicationInfo> launcherAppsList = manager.getInstalledApplications(PackageManager.MATCH_ALL);

        for (ApplicationInfo info : launcherAppsList) {
            if (isNetworkApp(context, info.packageName)) {
//                    if (info.uid == 1000) {
//                        if (systemInfo == null) {
//                            systemInfo = packageInfo;
//                        }
//                        continue;
//                    }
                if (0 != (info.flags & ApplicationInfo.FLAG_SYSTEM)) {
                    continue;
                }

                if (!nameList.contains(info.packageName)) {
                    nameList.add(info.packageName);
                    netAppList.add(info);
                } else {
                    String s = info.loadLabel(manager).toString();
                    Log.d("oscar", "--------" + info.packageName + "----sssss&&uid:" + s + "," + info.uid);
                }
                Log.d("oscar", "1--------" + info.packageName + "----sssss&&uid:" + info.uid);
            }
        }
//        netAppList.add(systemInfo);

        return netAppList;
    }

    public static boolean isNetworkApp(Context context, String packageName) {
        return context.getPackageManager().checkPermission("android.permission.INTERNET", packageName) == 0;
    }

//    public static long getStartTime(Context context, int sim) {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        int cycleDay = sharedPreferences.getInt(DataFlowSettingFragment.PLAN_START_PREFERENCE + DataFlowSettingFragment.SEPARATOR + sim, 1);
//        return computeLastCycleBoundary(System.currentTimeMillis(), cycleDay);
//
//    }

//    public static long computeLastCycleBoundary(long currentTime, int cycleDay) {
//
//        final Time now = new Time();
//        now.set(currentTime);
//
//        // first, find cycle boundary for current month
//        final Time cycle = new Time(now);
//        cycle.hour = cycle.minute = cycle.second = 0;
//        snapToCycleDay(cycle, cycleDay);
//
//        if (Time.compare(cycle, now) >= 0) {
//            // cycle boundary is beyond now, use last cycle boundary; start by
//            // pushing ourselves squarely into last month.
//            final Time lastMonth = new Time(now);
//            lastMonth.hour = lastMonth.minute = lastMonth.second = 0;
//            lastMonth.monthDay = 1;
//            lastMonth.month -= 1;
//            lastMonth.normalize(true);
//
//            cycle.set(lastMonth);
//            snapToCycleDay(cycle, cycleDay);
//        }
//
//        Log.d(TAG, "computeLastCycleBoundary" + cycle.toString());
//        return cycle.toMillis(true);
//    }

    /**
     * Snap to the cycle day for the current month given; when cycle day doesn't
     * exist, it snaps to last second of current month.
     */
//    public static void snapToCycleDay(Time time, int cycleDay) {
//        if (cycleDay > time.getActualMaximum(MONTH_DAY)) {
//            // cycle day isn't valid this month; snap to last second of month
//            time.month += 1;
//            time.monthDay = 1;
//            time.second = -1;
//        } else {
//            time.monthDay = cycleDay;
//        }
//        time.normalize(true);
//    }


    /* MODIFIED-BEGIN by junyong.sun-nb, 2016-03-30, BUG-1839353 */
    public static List<ResolveInfo> getLauncherApps(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN", null);
        intent.addCategory("android.intent.category.LAUNCHER");
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, 0);
        Log.d("oscar", "Launcher app size：" + resolveInfos.size());
        return resolveInfos;
        /* MODIFIED-END by junyong.sun-nb,BUG-1839353 */
    }

//    public static String getNumberBySubID(int subID) {
//        TelephonyManager tm = TelephonyManager.getDefault();
//
////        log("isIccCardActivated subscription: "+ subscription + " SimState: "+ tm.getSimState(subscription));
//        return tm.getLine1NumberForSubscriber(subID);
//    }

    private static final String TEST_SUBSCRIBER_PROP = "test.subscriberid";

//    public static String getActiveDataSubscriberId(Context context) {
//        final TelephonyManager tele = TelephonyManager.from(context);
//        int subId = SubscriptionManager.getDefaultDataSubId();
//        final String actualSubscriberId = tele.getSubscriberId(subId);
//        android.util.Log.d("tstest", "subId = " + subId + "--actualSubscriberId = " + actualSubscriberId);
//        return SystemProperties.get(TEST_SUBSCRIBER_PROP, actualSubscriberId);
//    }


    private static final long SIM_IN_SUB0 = 0;
    private static final long SIM_IN_SUB1 = 1;

//    public static String getOperatorName(Context context,int index) {
//        TelephonyManager manager = TelephonyManager.getDefault();
//        int subId = SubscriptionManager.getSubId(index - 1)[0];
//        String operatorName = manager.getSimOperatorNameForSubscription(subId);
//        if (TextUtils.isEmpty(operatorName)) {
//            operatorName = manager.getNetworkOperatorName(subId);
//            if (!TextUtils.isEmpty(operatorName)) {
//                String simCarrierNameString = getSimCarrierName(index - 1);
//                if (!TextUtils.isEmpty(simCarrierNameString)) {
//                    operatorName = simCarrierNameString;
//                }
//            }
//        }
//
//        if (!TextUtils.isEmpty(operatorName)) {
//            if (operatorName.equals("CMCC") || operatorName.equals("CHINA MOBILE"))
//                operatorName = context.getString(R.string.China_Mobile);
//            else if (operatorName.equals("UNICOM") || operatorName.equals("CHN-UNICOM")) {
//                operatorName = context.getString(R.string.China_Unicom);
//            }
//        }
//
//        return operatorName;
//    }

//    private static String getSimCarrierName(long slotId) {
//        String simCarierName = "";
//
//        if (SIM_IN_SUB0 == slotId) {
//            simCarierName = SystemProperties.get(TelephonyProperties.PROPERTY_ICC_SUB0_OPERATOR_ALPHA);
//        } else if (SIM_IN_SUB1 == slotId) {
//            simCarierName = SystemProperties.get(TelephonyProperties.PROPERTY_ICC_SUB1_OPERATOR_ALPHA);
//        }
//        return simCarierName;
//    }

//    /* MODIFIED-BEGIN by junyong.sun-nb, 2016-03-30, BUG-1839353 */
//    public static List<List<AppEntry>> getSystemApps(Context context, List<List<AppEntry>> list) {
//
//        if(context == null){
//            return null;
//        }
//        PackageManager manager = context.getPackageManager();
//
//        List<ResolveInfo> installedApplications = getLauncherApps(context);
//
//        ArrayMap<Integer, List<AppEntry>> map = new ArrayMap<>();
//
//        for (ResolveInfo info :
//                installedApplications) {
//            if (!isNetworkApp(context, info.activityInfo.applicationInfo.packageName)) {
//                continue;
//            }
//            if (0 == (info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)) {
//                continue;
//            }
//            int uid = info.activityInfo.applicationInfo.uid;
//            AppEntry entry = new AppEntry();
//            entry.uid = uid;
//            entry.icon = info.loadIcon(manager);
//            entry.title = info.loadLabel(manager).toString();
//            List<AppEntry> appEntries = map.get(uid);
//            if (appEntries != null) {
//                appEntries.add(entry);
//            } else {
//                appEntries = new ArrayList<>();
//                appEntries.add(entry);
//                map.put(uid, appEntries);
//            }
//        }
//
//        list.addAll(map.values());
//        /* MODIFIED-END by junyong.sun-nb,BUG-1839353 */
//        return list;
//    }

//    public static long getMonthUsed(Context context, int sim) {
//        INetworkStatsService statsService = INetworkStatsService.Stub.asInterface(
//                ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
//        try {
//            Log.d(TAG, "forceUpdate - start");
//            statsService.forceUpdate();
//            Log.d(TAG, "forceUpdate - end");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        INetworkStatsSession statsSession = null;
//        try {
//            statsSession = statsService.openSession();
//        } catch (RemoteException e) {
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        } catch (Exception e) {
//            Log.d(TAG, "Error init");
//            e.printStackTrace();
//            return -1;
//        }
//
//        NetworkTemplate mobileTemplate;
//        mobileTemplate = buildTemplateMobileAll(DataFlowUtil.getSubscriberIdFromTele(context, sim - 1));
//        int fields = FIELD_RX_BYTES | FIELD_TX_BYTES;
//        NetworkStatsHistory mobileNetwork = null;
//        try {
//            mobileNetwork = statsSession.getHistoryForNetwork(mobileTemplate, fields);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//
//        NetworkStatsHistory.Entry entry;
//
//        final long now = System.currentTimeMillis();
//        long totalStart = DataFlowUtil.getStartTime(context, sim);
//        final long totalEnd = now;
//
//        //本月使用
//        entry = mobileNetwork.getValues(totalStart, totalEnd, now, null);
//        return (entry != null ? entry.rxBytes + entry.txBytes : 0);
//
//    }

//    public static long getMonthUsedWithSetting(Context context, int sim) {
//        long monthUsed = getMonthUsed(context, sim);
//
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        long datausage = Long.valueOf(sharedPreferences.getString(DATA_USAGE_PREFERENCE + SEPARATOR + sim, "0"));
//        long timestamp = sharedPreferences.getLong(PlanSettingFragment.TIME_STAMP + SEPARATOR + sim, -1);
//
//        if(timestamp > getStartTime(context, sim)){ //返回相减结果
//            return monthUsed - datausage;
//        }
//        return monthUsed;
//    }

//    public static void recordZeorDataEntry(Context context,int sim,long start,long size){
//
//        INetworkStatsService statsService = INetworkStatsService.Stub.asInterface(
//                ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
//
//        INetworkStatsSession statsSession = null;
//        try {
//            statsSession = statsService.openSession();
////            statsService.openSessionForUsageStats(null);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        NetworkTemplate mobileTemplate = buildTemplateMobileAll(DataFlowUtil.getSubscriberIdFromTele(context, sim - 1));
//        int fields = FIELD_RX_BYTES | FIELD_TX_BYTES;
//        NetworkStatsHistory mobileNetwork = null;
//        try {
//            mobileNetwork = statsSession.getHistoryForNetwork(mobileTemplate, fields);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
////        NetworkStatsHistory.Entry entry = new  NetworkStats.Entry();
//
//        mobileNetwork.recordData(start,System.currentTimeMillis(),size,0);
//    }


//    public static String formatDataFlowSize(Context context, long sizeBytes) {
//        Resources res = context.getResources();
//        float result = sizeBytes;
//        int suffix = R.string.byteShort;
//        long mult = 1;
//        if (result > 900) {
//            suffix = R.string.kilobyteShort;
//            result = result / 1024;
//        }
//        if (result > 900) {
//            suffix = R.string.megabyteShort;
//            result = result / 1024;
//        }
//        if (result > 900) {
//            suffix = R.string.gigabyteShort;
//            result = result / 1024;
//        }
//        if (result > 900) {
//            suffix = R.string.terabyteShort;
//            result = result / 1024;
//        }
//        if (result > 900) {
//            suffix = R.string.petabyteShort;
//            result = result / 1024;
//        }
//        // Note we calculate the rounded long by ourselves, but still let String.format()
//        // compute the rounded value. String.format("%f", 0.1) might not return "0.1" due to
//        // floating point errors.
//        DecimalFormat format = new DecimalFormat("#0.##");
//
//        final String roundedString = format.format(result);
//
//        // Note this might overflow if result >= Long.MAX_VALUE / 100, but that's like 80PB so
//        // it's okay (for now)...
//
//        final String units = res.getString(suffix);
//
//        return roundedString + " " + units;// MODIFIED by junyong.sun-nb, 2016-03-30, BUG-1839353
//    }

//    public static boolean isBandwidthControlEnabled() {
//        final INetworkManagementService netManager = INetworkManagementService.Stub
//                .asInterface(ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
//        try {
//            return netManager.isBandwidthControlEnabled();
//        } catch (RemoteException e) {
//            return false;
//        }
//    }
//
//    public static boolean isSameDay(long time1,long time2){
//        Time t1 = new Time();
//        t1.set(time1);
//
//        Time t2 = new Time();
//        t2.set(time2);
//
//        return t1.year == t2.year && t1.yearDay == t2.yearDay;
//    }
//
//    public static boolean isSameMonth(long time1,long time2){
//        Time t1 = new Time();
//        t1.set(time1);
//
//        Time t2 = new Time();
//        t2.set(time2);
//
//        return t1.year == t2.year && t1.yearDay == t2.yearDay;
//    }

}
