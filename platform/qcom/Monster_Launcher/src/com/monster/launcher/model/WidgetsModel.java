
package com.monster.launcher.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ResolveInfo;

import com.monster.launcher.AppInfo;
import com.monster.launcher.Log;

import com.monster.launcher.AppFilter;
import com.monster.launcher.IconCache;
import com.monster.launcher.InvariantDeviceProfile;
import com.monster.launcher.ItemInfo;
import com.monster.launcher.LauncherAppState;
import com.monster.launcher.LauncherAppWidgetProviderInfo;
import com.monster.launcher.Utilities;
import com.monster.launcher.compat.AlphabeticIndexCompat;
import com.monster.launcher.compat.AppWidgetManagerCompat;
import com.monster.launcher.compat.UserHandleCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Widgets data model that is used by the adapters of the widget views and controllers.
 *
 * <p> The widgets and shortcuts are organized using package name as its index.
 */
public class WidgetsModel {

    private static final String TAG = "WidgetsModel";
    private static final boolean DEBUG = false;

    /* List of packages that is tracked by this model. */
    private ArrayList<PackageItemInfo> mPackageItemInfos = new ArrayList<>();

    /* Map of widgets and shortcuts that are tracked per package. */
    private HashMap<PackageItemInfo, ArrayList<Object>> mWidgetsList = new HashMap<>();

    private ArrayList<Object> mRawList;

    private final AppWidgetManagerCompat mAppWidgetMgr;
    private final WidgetsAndShortcutNameComparator mWidgetAndShortcutNameComparator;
//    private final Comparator<ItemInfo> mAppNameComparator;
    private final Comparator<ItemInfo> mAppNameComparator;
    private final IconCache mIconCache;
    private final AppFilter mAppFilter;
    private AlphabeticIndexCompat mIndexer;
    //add by lijun start
    private Context mContext;
    private AppNameComparator lableComparator;
    private boolean isSpeakEnglish;
    private HashMap<CharSequence, String> mCachedSectionNames = new HashMap<>();
    //add by lijun end

    public WidgetsModel(Context context,  IconCache iconCache, AppFilter appFilter) {
        mAppWidgetMgr = AppWidgetManagerCompat.getInstance(context);
        mWidgetAndShortcutNameComparator = new WidgetsAndShortcutNameComparator(context);
        mAppNameComparator = (new AppNameComparator(context)).getAppInfoComparator();
        mIconCache = iconCache;
        mAppFilter = appFilter;
        mIndexer = new AlphabeticIndexCompat(context);
        mContext = context;//add by lijun
        lableComparator = new AppNameComparator(context);//add by lijun
    }

    @SuppressWarnings("unchecked")
    private WidgetsModel(WidgetsModel model) {
        mAppWidgetMgr = model.mAppWidgetMgr;
        mPackageItemInfos = (ArrayList<PackageItemInfo>) model.mPackageItemInfos.clone();
        mWidgetsList = (HashMap<PackageItemInfo, ArrayList<Object>>) model.mWidgetsList.clone();
        mRawList = (ArrayList<Object>) model.mRawList.clone();
        mWidgetAndShortcutNameComparator = model.mWidgetAndShortcutNameComparator;
        mAppNameComparator = model.mAppNameComparator;
        mIconCache = model.mIconCache;
        mAppFilter = model.mAppFilter;
    }

    // Access methods that may be deleted if the private fields are made package-private.
    public int getPackageSize() {
        if (mPackageItemInfos == null) {
            return 0;
        }
        return mPackageItemInfos.size();
    }

    // Access methods that may be deleted if the private fields are made package-private.
    public PackageItemInfo getPackageItemInfo(int pos) {
        if (pos >= mPackageItemInfos.size() || pos < 0) {
            return null;
        }
        return mPackageItemInfos.get(pos);
    }

    public List<Object> getSortedWidgets(int pos) {
        return mWidgetsList.get(mPackageItemInfos.get(pos));
    }

    public ArrayList<Object> getRawList() {
        return mRawList;
    }

    public void setWidgetsAndShortcuts(ArrayList<Object> rawWidgetsShortcuts) {
        Utilities.assertWorkerThread();
        mRawList = rawWidgetsShortcuts;
        if (DEBUG) {
            Log.d(TAG, "addWidgetsAndShortcuts, widgetsShortcuts#=" + rawWidgetsShortcuts.size());
        }

        // Temporary list for {@link PackageItemInfos} to avoid having to go through
        // {@link mPackageItemInfos} to locate the key to be used for {@link #mWidgetsList}
        HashMap<String, PackageItemInfo> tmpPackageItemInfos = new HashMap<>();

        // clear the lists.
        mWidgetsList.clear();
        mPackageItemInfos.clear();
        mWidgetAndShortcutNameComparator.reset();

        InvariantDeviceProfile idp = LauncherAppState.getInstance().getInvariantDeviceProfile();

        // add and update.
        for (Object o: rawWidgetsShortcuts) {
            String packageName = "";
            UserHandleCompat userHandle = null;
            ComponentName componentName = null;
            if (o instanceof LauncherAppWidgetProviderInfo) {
                LauncherAppWidgetProviderInfo widgetInfo = (LauncherAppWidgetProviderInfo) o;

                // Ensure that all widgets we show can be added on a workspace of this size
                int minSpanX = Math.min(widgetInfo.spanX, widgetInfo.minSpanX);
                int minSpanY = Math.min(widgetInfo.spanY, widgetInfo.minSpanY);
                if (minSpanX <= (int) idp.numColumns &&
                    minSpanY <= (int) idp.numRows) {
                    componentName = widgetInfo.provider;
                    packageName = widgetInfo.provider.getPackageName();
                    userHandle = mAppWidgetMgr.getUser(widgetInfo);
                } else {
                    if (DEBUG) {
                        Log.d(TAG, String.format(
                                "Widget %s : (%d X %d) can't fit on this device",
                                widgetInfo.provider, minSpanX, minSpanY));
                    }
                    continue;
                }
            } else if (o instanceof ResolveInfo) {
                ResolveInfo resolveInfo = (ResolveInfo) o;
                componentName = new ComponentName(resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name);
                packageName = resolveInfo.activityInfo.packageName;
                userHandle = UserHandleCompat.myUserHandle();
            }

            if (componentName == null || userHandle == null) {
                Log.e(TAG, String.format("Widget cannot be set for %s.", o.getClass().toString()));
                continue;
            }
            if (mAppFilter != null && !mAppFilter.shouldShowApp(componentName)) {
                if (DEBUG) {
                    Log.d(TAG, String.format("%s is filtered and not added to the widget tray.",
                        packageName));
                }
                continue;
            }

            PackageItemInfo pInfo = tmpPackageItemInfos.get(packageName);
            ArrayList<Object> widgetsShortcutsList = mWidgetsList.get(pInfo);
            if (widgetsShortcutsList != null) {
                widgetsShortcutsList.add(o);
            } else {
                widgetsShortcutsList = new ArrayList<>();
                widgetsShortcutsList.add(o);
                pInfo = new PackageItemInfo(packageName);
                mIconCache.getTitleAndIconForApp(packageName, userHandle,
                        true /* userLowResIcon */, pInfo);
                pInfo.titleSectionName = mIndexer.computeSectionName(pInfo.title);
                mWidgetsList.put(pInfo, widgetsShortcutsList);
                tmpPackageItemInfos.put(packageName,  pInfo);
                mPackageItemInfos.add(pInfo);
            }
        }

        // sort.
//        Collections.sort(mPackageItemInfos, mAppNameComparator);
        //lijun add start
        sortBySystemOrThirdPart();
        //lijun add end
        for (PackageItemInfo p: mPackageItemInfos) {
            Collections.sort(mWidgetsList.get(p), mWidgetAndShortcutNameComparator);
        }
    }

    private void sortBySystemOrThirdPart(){
        if(mPackageItemInfos == null || mPackageItemInfos.size()<=0)return;
        ArrayList<PackageItemInfo> systemList = new ArrayList<PackageItemInfo>();
        ArrayList<PackageItemInfo> thirdPartList = new ArrayList<PackageItemInfo>();
        boolean isSystemApp = true;
        for (Iterator it = mPackageItemInfos.iterator(); it.hasNext(); ) {
            PackageItemInfo pkgInfo = (PackageItemInfo) it.next();
            isSystemApp = Utilities.isSystemApp(pkgInfo.packageName,mContext);
            if (isSystemApp) {
                systemList.add(pkgInfo);
            }else {
                thirdPartList.add(pkgInfo);
            }
        }
        sortByLable(systemList);
        sortByLable(thirdPartList);
        mPackageItemInfos.clear();
        mPackageItemInfos.addAll(systemList);
        mPackageItemInfos.addAll(thirdPartList);
    }

    private void sortByLable(ArrayList<PackageItemInfo> infoList){
        Locale curLocale = mContext.getResources().getConfiguration().locale;
        isSpeakEnglish = "en".equals(curLocale.getLanguage());
        TreeMap<String, ArrayList<PackageItemInfo>> sectionMap = null;
        boolean localeRequiresSectionSorting = curLocale.equals(Locale.SIMPLIFIED_CHINESE);
        if (localeRequiresSectionSorting || isSpeakEnglish) {
            sectionMap = new TreeMap<>(lableComparator.getSectionNameComparator());

            for (PackageItemInfo info : infoList) {
                if (info.mSearchRegExp == null) {
                    info.mSearchRegExp = Utilities.getRegExpBySpChinese(info.title);
                }
                String sectionName = getAndUpdateCachedSectionName(info.title);
                ArrayList<PackageItemInfo> sectionApps = sectionMap.get(sectionName);
                if (sectionApps == null) {
                    sectionApps = new ArrayList<>();
                    sectionMap.put(sectionName, sectionApps);
                }
                sectionApps.add(info);
            }
            List<PackageItemInfo> allApps = new ArrayList<>(infoList.size());
            for (Map.Entry<String, ArrayList<PackageItemInfo>> entry : sectionMap.entrySet()) {
                allApps.addAll(entry.getValue());
            }
            infoList.clear();
            infoList.addAll(allApps);
        }
    }

    private String getAndUpdateCachedSectionName(CharSequence title) {
        String sectionName = mCachedSectionNames.get(title);
        if (sectionName == null) {
            String newTitle = null;       //add by xiangzx
            if (isSpeakEnglish) {
                if(title != null && title.length() > 0) {
                    String firstChar = ""+title.charAt(0);
                    ArrayList<String> pinyin = Utilities.getFullPinYinList(firstChar);
                    if(pinyin != null && pinyin.size() >0){
                        newTitle = ""+pinyin.get(0).charAt(0);
                    }
                }
            }
            sectionName = mIndexer.computeSectionName(newTitle != null ? newTitle : title);
            if("…".equals(sectionName)){
                sectionName = "#";
            }
            mCachedSectionNames.put(title, sectionName);
        }
        return sectionName;
    }

    /**
     * Create a snapshot of the widgets model.
     * <p>
     * Usage case: view binding without being modified from package updates.
     */
    @Override
    public WidgetsModel clone(){
        return new WidgetsModel(this);
    }
}