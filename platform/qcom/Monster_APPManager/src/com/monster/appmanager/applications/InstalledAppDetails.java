/**
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.monster.appmanager.applications;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatteryStatsHelper;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.monster.appmanager.DataUsageSummary;
import com.monster.appmanager.DataUsageSummary.AppItem;
import com.monster.appmanager.R;
import com.monster.appmanager.SettingsActivity;
import com.monster.appmanager.Utils;
import com.monster.appmanager.applications.AppStateOverlayBridge.OverlayState;
import com.monster.appmanager.applications.PermissionsSummaryHelper.PermissionsResultCallback;
import com.monster.appmanager.fuelgauge.BatteryEntry;
import com.monster.appmanager.fuelgauge.PowerUsageDetail;
import com.monster.appmanager.net.ChartData;
import com.monster.appmanager.net.ChartDataLoader;
import com.monster.appmanager.notification.AppNotificationSettings;
import com.monster.appmanager.notification.NotificationBackend;
import com.monster.appmanager.notification.NotificationBackend.AppRow;
import com.monster.appmanager.viewhelp.ButtonPreference;
import com.monster.permission.ui.ManagePermissionsInfoActivity;
import com.monster.permission.ui.MstPermission;
import com.monster.permission.ui.MstPermission.MstAppGroup;
import com.monster.permission.ui.MstPermission.MstPermEntry;
import com.monster.permission.ui.MstPermission.PermOpSelectItem;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.icu.text.ListFormatter;
import android.icu.util.Output;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryStats;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import mst.view.menu.PopupMenu;

/**
 * Activity to display application information from Settings. This activity presents
 * extended information associated with a package like code, data, total size, permissions
 * used by the application and also the set of default launchable activities.
 * For system applications, an option to clear user data is displayed only if data size is > 0.
 * System applications that do not want clear user data do not have this option.
 * For non-system applications, there is no option to clear data. Instead there is an option to
 * uninstall the application.
 */
public class InstalledAppDetails extends AppInfoBase
        implements View.OnClickListener, OnPreferenceClickListener, OnPreferenceChangeListener, MstPermission.OnPermChangeListener {

    private static final String LOG_TAG = "InstalledAppDetails";
    public static final String EXTRA_KEY_FROM_APP_DETAIL = "from_appdetail";

    // Menu identifiers
    public static final int UNINSTALL_ALL_USERS_MENU = 1;
    public static final int UNINSTALL_UPDATES = 2;

    // Result code identifiers
    public static final int REQUEST_UNINSTALL = 0;
    private static final int SUB_INFO_FRAGMENT = 1;

    private static final int LOADER_CHART_DATA = 2;

    private static final int DLG_FORCE_STOP = DLG_BASE + 1;
    private static final int DLG_DISABLE = DLG_BASE + 2;
    private static final int DLG_SPECIAL_DISABLE = DLG_BASE + 3;
    private static final int DLG_FACTORY_RESET = DLG_BASE + 4;

    private static final String KEY_HEADER = "header_view";
    private static final String KEY_NOTIFICATION = "notification_settings";
    private static final String KEY_STORAGE = "storage_settings";
    private static final String KEY_PERMISSION = "permission_settings";
    private static final String KEY_DATA = "data_settings";
    private static final String KEY_LAUNCH = "preferred_settings";
    private static final String KEY_BATTERY = "battery";
    private static final String KEY_MEMORY = "memory";
    
    private static final String KEY_CONTROL_BUTTON_PANNEL = "control_button_panel";

    private final HashSet<String> mHomePackages = new HashSet<String>();

    private boolean mInitialized;
    private boolean mShowUninstalled;
    private LayoutPreference mHeader;
    private Button mUninstallButton;
    private boolean mUpdatedSysApp = false;
    private Button mForceStopButton;
    private Preference mNotificationPreference;
    private Preference mStoragePreference;
    private Preference mPermissionsPreference;
    private Preference mLaunchPreference;
    private Preference mDataPreference;
    private Preference mMemoryPreference;

    private boolean mDisableAfterUninstall;
    // Used for updating notification preference.
    private final NotificationBackend mBackend = new NotificationBackend();

    private ChartData mChartData;
    private INetworkStatsSession mStatsSession;

    private Preference mBatteryPreference;

    private BatteryStatsHelper mBatteryHelper;
    private BatterySipper mSipper;

    protected ProcStatsData mStatsManager;
    protected ProcStatsPackageEntry mStats;

    private boolean handleDisableable(Button button) {
        boolean disableable = false;
        // Try to prevent the user from bricking their phone
        // by not allowing disabling of apps signed with the
        // system cert and any launcher app in the system.
        if (mHomePackages.contains(mAppEntry.info.packageName)
                || Utils.isSystemPackage(mPm, mPackageInfo)) {
            // Disable button for core system applications.
            button.setText(R.string.disable_text);
        } else if (mAppEntry.info.enabled && !isDisabledUntilUsed()) {
            button.setText(R.string.disable_text);
            disableable = true;
        } else {
            button.setText(R.string.enable_text);
            disableable = true;
        }

        return disableable;
    }

    private boolean isDisabledUntilUsed() {
        return mAppEntry.info.enabledSetting
                == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED;
    }

    private void initUninstallButtons() {
        final boolean isBundled = (mAppEntry.info.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        boolean enabled = true;
        if (isBundled) {
            enabled = handleDisableable(mUninstallButton);
        } else {
            if ((mPackageInfo.applicationInfo.flags & ApplicationInfo.FLAG_INSTALLED) == 0
                    && mUserManager.getUsers().size() >= 2) {
                // When we have multiple users, there is a separate menu
                // to uninstall for all users.
                enabled = false;
            }
            mUninstallButton.setText(R.string.uninstall_text);
        }
        // If this is a device admin, it can't be uninstalled or disabled.
        // We do this here so the text of the button is still set correctly.
        if (mDpm.packageHasActiveAdmins(mPackageInfo.packageName)) {
            enabled = false;
        }

        if (isProfileOrDeviceOwner(mPackageInfo.packageName)) {
            enabled = false;
        }

        // Home apps need special handling.  Bundled ones we don't risk downgrading
        // because that can interfere with home-key resolution.  Furthermore, we
        // can't allow uninstallation of the only home app, and we don't want to
        // allow uninstallation of an explicitly preferred one -- the user can go
        // to Home settings and pick a different one, after which we'll permit
        // uninstallation of the now-not-default one.
        if (enabled && mHomePackages.contains(mPackageInfo.packageName)) {
            if (isBundled) {
                enabled = false;
            } else {
                ArrayList<ResolveInfo> homeActivities = new ArrayList<ResolveInfo>();
                ComponentName currentDefaultHome  = mPm.getHomeActivities(homeActivities);
                if (currentDefaultHome == null) {
                    // No preferred default, so permit uninstall only when
                    // there is more than one candidate
                    enabled = (mHomePackages.size() > 1);
                } else {
                    // There is an explicit default home app -- forbid uninstall of
                    // that one, but permit it for installed-but-inactive ones.
                    enabled = !mPackageInfo.packageName.equals(currentDefaultHome.getPackageName());
                }
            }
        }

        if (mAppControlRestricted) {
            enabled = false;
        }

        mUninstallButton.setEnabled(enabled);
        if (enabled) {
            // Register listener
            mUninstallButton.setOnClickListener(this);
        }
    }

    /** Returns if the supplied package is device owner or profile owner of at least one user */
    private boolean isProfileOrDeviceOwner(String packageName) {
        List<UserInfo> userInfos = mUserManager.getUsers();
        DevicePolicyManager dpm = (DevicePolicyManager)
                getContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (packageName.equals(dpm.getDeviceOwner())) {
            return true;
        }
        for (UserInfo userInfo : userInfos) {
            ComponentName cn = dpm.getProfileOwnerAsUser(userInfo.id);
            if (cn != null && cn.getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setHasOptionsMenu(true);
        addPreferencesFromResource(R.xml.installed_app_details);

//        if (Utils.isBandwidthControlEnabled()) {
//            INetworkStatsService statsService = INetworkStatsService.Stub.asInterface(
//                    ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
//            try {
//                mStatsSession = statsService.openSession();
//            } catch (RemoteException e) {
//                throw new RuntimeException(e);
//            }
//        } else {
//            removePreference(KEY_DATA);
//        }
        mBatteryHelper = new BatteryStatsHelper(getActivity(), true);
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.APPLICATIONS_INSTALLED_APP_DETAILS;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mFinishing) {
            return;
        }
        mState.requestSize(mPackageName, mUserId);
//        AppItem app = new AppItem(mAppEntry.info.uid);
//        app.addUid(mAppEntry.info.uid);
//        if (mStatsSession != null) {
//            getLoaderManager().restartLoader(LOADER_CHART_DATA,
//                    ChartDataLoader.buildArgs(getTemplate(getContext()), app),
//                    mDataCallbacks);
//        }
        new BatteryUpdater().execute();
        new MemoryUpdater().execute();
//        new UpdateUiAsync().execute();
        refreshAuth();
    }

    @Override
    public void onPause() {
        getLoaderManager().destroyLoader(LOADER_CHART_DATA);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        TrafficStats.closeQuietly(mStatsSession);
        super.onDestroy();
        unRegDataconnectReceiver();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mFinishing) {
            return;
        }
        handleHeader();
        initView();

//        mNotificationPreference = findPreference(KEY_NOTIFICATION);
//        mNotificationPreference.setIcon(R.drawable.ic_settings_notifications_alpha);
//        mNotificationPreference.setOnPreferenceClickListener(this);
//        mStoragePreference = findPreference(KEY_STORAGE);
//        mStoragePreference.setIcon(R.drawable.ic_settings_storage_alpha);
//        mStoragePreference.setOnPreferenceClickListener(this);
//        mPermissionsPreference = findPreference(KEY_PERMISSION);
//        mPermissionsPreference.setIcon(R.drawable.ic_settings_applications_alpha);
//        mPermissionsPreference.setOnPreferenceClickListener(this);
//        mDataPreference = findPreference(KEY_DATA);
//        mDataPreference.setIcon(R.drawable.ic_settings_cell_standby);
//        if (mDataPreference != null) {
//            mDataPreference.setOnPreferenceClickListener(this);
//        }
//        mBatteryPreference = findPreference(KEY_BATTERY);
//        mBatteryPreference.setIcon(R.drawable.ic_settings_battery_alpha);
//        mBatteryPreference.setEnabled(false);
//        mBatteryPreference.setOnPreferenceClickListener(this);
//        mMemoryPreference = findPreference(KEY_MEMORY);
//        mMemoryPreference.setIcon(R.drawable.ic_settings_data_usage_alpha);
//        mMemoryPreference.setOnPreferenceClickListener(this);
//
//        mLaunchPreference = findPreference(KEY_LAUNCH);
//        mLaunchPreference.setIcon(R.drawable.ic_settings_applications_alpha);
//        if (mAppEntry != null && mAppEntry.info != null) {
//            if ((mAppEntry.info.flags&ApplicationInfo.FLAG_INSTALLED) == 0 ||
//                    !mAppEntry.info.enabled) {
//                mLaunchPreference.setEnabled(false);
//            } else {
//                mLaunchPreference.setOnPreferenceClickListener(this);
//            }
//        } else {
//            mLaunchPreference.setEnabled(false);
//        }
        
//        PreferenceCategory category = (PreferenceCategory)findPreference("original_category");
//        category.removeAll();
        removePreference("original_category");
    }

    private void handleHeader() {
        mHeader = (LayoutPreference) findPreference(KEY_HEADER);

        // Get Control button panel
//        View btnPanel = mHeader.findViewById(R.id.control_buttons_panel);
//        mForceStopButton = (Button) btnPanel.findViewById(R.id.right_button);
//        mForceStopButton.setText(R.string.force_stop);
//        mUninstallButton = (Button) btnPanel.findViewById(R.id.left_button);
//        mForceStopButton.setEnabled(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, UNINSTALL_UPDATES, 0, R.string.app_factory_reset)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, UNINSTALL_ALL_USERS_MENU, 1, R.string.uninstall_all_users_text)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (mFinishing) {
            return;
        }
        boolean showIt = true;
        if (mUpdatedSysApp) {
            showIt = false;
        } else if (mAppEntry == null) {
            showIt = false;
        } else if ((mAppEntry.info.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
            showIt = false;
        } else if (mPackageInfo == null || mDpm.packageHasActiveAdmins(mPackageInfo.packageName)) {
            showIt = false;
        } else if (UserHandle.myUserId() != 0) {
            showIt = false;
        } else if (mUserManager.getUsers().size() < 2) {
            showIt = false;
        }
        menu.findItem(UNINSTALL_ALL_USERS_MENU).setVisible(showIt);
        mUpdatedSysApp = (mAppEntry.info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
        menu.findItem(UNINSTALL_UPDATES).setVisible(mUpdatedSysApp && !mAppControlRestricted);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case UNINSTALL_ALL_USERS_MENU:
                uninstallPkg(mAppEntry.info.packageName, true, false);
                return true;
            case UNINSTALL_UPDATES:
                showDialogInner(DLG_FACTORY_RESET, 0);
                return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_UNINSTALL) {
            if (mDisableAfterUninstall) {
                mDisableAfterUninstall = false;
                try {
                    ApplicationInfo ainfo = getActivity().getPackageManager().getApplicationInfo(
                            mAppEntry.info.packageName, PackageManager.GET_UNINSTALLED_PACKAGES
                            | PackageManager.GET_DISABLED_COMPONENTS);
                    if ((ainfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
                        new DisableChanger(this, mAppEntry.info,
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER)
                                .execute((Object)null);
                    }
                } catch (NameNotFoundException e) {
                }
            }
            if (!refreshUi()) {
                setIntentAndFinish(true, true);
            }
        }
    }

    // Utility method to set application label and icon.
    private void setAppLabelAndIcon(PackageInfo pkgInfo) {
        final View appSnippet = mHeader.findViewById(R.id.app_snippet);
        mState.ensureIcon(mAppEntry);
        setupAppSnippet(appSnippet, mAppEntry.label, mAppEntry.icon,
                pkgInfo != null ? pkgInfo.versionName : null);
    }

    private boolean signaturesMatch(String pkg1, String pkg2) {
        if (pkg1 != null && pkg2 != null) {
            try {
                final int match = mPm.checkSignatures(pkg1, pkg2);
                if (match >= PackageManager.SIGNATURE_MATCH) {
                    return true;
                }
            } catch (Exception e) {
                // e.g. named alternate package not found during lookup;
                // this is an expected case sometimes
            }
        }
        return false;
    }

    @Override
    protected boolean refreshUi() {
        retrieveAppEntry();
        if (mAppEntry == null) {
            return false; // onCreate must have failed, make sure to exit
        }

        if (mPackageInfo == null) {
            return false; // onCreate must have failed, make sure to exit
        }

        // Get list of "home" apps and trace through any meta-data references
        List<ResolveInfo> homeActivities = new ArrayList<ResolveInfo>();
        mPm.getHomeActivities(homeActivities);
        mHomePackages.clear();
        for (int i = 0; i< homeActivities.size(); i++) {
            ResolveInfo ri = homeActivities.get(i);
            final String activityPkg = ri.activityInfo.packageName;
            mHomePackages.add(activityPkg);

            // Also make sure to include anything proxying for the home app
            final Bundle metadata = ri.activityInfo.metaData;
            if (metadata != null) {
                final String metaPkg = metadata.getString(ActivityManager.META_HOME_ALTERNATE);
                if (signaturesMatch(metaPkg, activityPkg)) {
                    mHomePackages.add(metaPkg);
                }
            }
        }

        checkForceStop();
        setAppLabelAndIcon(mPackageInfo);
        initUninstallButtons();

        // Update the preference summaries.
        Activity context = getActivity();
//        mStoragePreference.setSummary(AppStorageSettings.getSummary(mAppEntry, context));
//        mLaunchPreference.setSummary(Utils.getLaunchByDeafaultSummary(mAppEntry, mUsbManager,
//                mPm, context));
//        mNotificationPreference.setSummary(getNotificationSummary(mAppEntry, context,
//                mBackend));
//        if (mDataPreference != null) {
//            mDataPreference.setSummary(getDataSummary());
//        }

        updateBattery();
        
        //storage
        try {
        	refreshSizeInfo();
        	refreshButtons();
        	updateOverlayWindowPermission();
        	updateDefaultAppOption();
        	updateAutostartSwitch();
        	updateShortcutSwitch();
        	updateNotifcationSwitch();
        	initItemShow();
		} catch (Exception e) {
			return false;
		}

        if (!mInitialized) {
            // First time init: are we displaying an uninstalled app?
            mInitialized = true;
            mShowUninstalled = (mAppEntry.info.flags&ApplicationInfo.FLAG_INSTALLED) == 0;
        } else {
            // All other times: if the app no longer exists then we want
            // to go away.
            try {
                ApplicationInfo ainfo = context.getPackageManager().getApplicationInfo(
                        mAppEntry.info.packageName, PackageManager.GET_UNINSTALLED_PACKAGES
                        | PackageManager.GET_DISABLED_COMPONENTS);
                if (!mShowUninstalled) {
                    // If we did not start out with the app uninstalled, then
                    // it transitioning to the uninstalled state for the current
                    // user means we should go away as well.
                    return (ainfo.flags&ApplicationInfo.FLAG_INSTALLED) != 0;
                }
            } catch (NameNotFoundException e) {
                return false;
            }
        }
        
        return true;
    }

    private void updateBattery() {
        if (mSipper != null) {
//            mBatteryPreference.setEnabled(true);
            int dischargeAmount = mBatteryHelper.getStats().getDischargeAmount(
                    BatteryStats.STATS_SINCE_CHARGED);
            final int percentOfMax = (int) ((mSipper.totalPowerMah)
                    / mBatteryHelper.getTotalPower() * dischargeAmount + .5f);
//            mBatteryPreference.setSummary(getString(R.string.battery_summary, percentOfMax));
//            
            batteryUsageStr = getString(R.string.battery_summary, percentOfMax);
        } else {
//            mBatteryPreference.setEnabled(false);
//            mBatteryPreference.setSummary(getString(R.string.no_battery_summary));
        }
        
        updateResourceUsage();
    }

    private CharSequence getDataSummary() {
        if (mChartData != null) {
            long totalBytes = mChartData.detail.getTotalBytes();
            if (totalBytes == 0) {
                return getString(R.string.no_data_usage);
            }
            Context context = getActivity();
            return getString(R.string.data_summary_format,
                    Formatter.formatFileSize(context, totalBytes),
                    DateUtils.formatDateTime(context, mChartData.detail.getStart(),
                            DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH));
        }
        return getString(R.string.computing_size);
    }

    @Override
    protected AlertDialog createDialog(int id, int errorCode) {
        switch (id) {
            case DLG_DISABLE:
                return new AlertDialog.Builder(getActivity())
                        .setMessage(getActivity().getText(R.string.app_disable_dlg_text))
                        .setPositiveButton(R.string.app_disable_dlg_positive,
                                new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Disable the app
                                new DisableChanger(InstalledAppDetails.this, mAppEntry.info,
                                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER)
                                .execute((Object)null);
                            }
                        })
                        .setNegativeButton(R.string.dlg_cancel, null)
                        .create();
            case DLG_SPECIAL_DISABLE:
                return new AlertDialog.Builder(getActivity())
                        .setMessage(getActivity().getText(R.string.app_special_disable_dlg_text))
                        .setPositiveButton(R.string.app_disable_dlg_positive,
                                new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Clear user data here
                                uninstallPkg(mAppEntry.info.packageName,
                                        false, true);
                            }
                        })
                        .setNegativeButton(R.string.dlg_cancel, null)
                        .create();
            case DLG_FORCE_STOP:
                return new AlertDialog.Builder(getActivity())
                        .setTitle(getActivity().getText(R.string.force_stop_dlg_title))
                        .setMessage(getActivity().getText(R.string.force_stop_dlg_text))
                        .setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Force stop
                                forceStopPackage(mAppEntry.info.packageName);
                            }
                        })
                        .setNegativeButton(R.string.dlg_cancel, null)
                        .create();
            case DLG_FACTORY_RESET:
                return new AlertDialog.Builder(getActivity())
                        .setTitle(getActivity().getText(R.string.app_factory_reset_dlg_title))
                        .setMessage(getActivity().getText(R.string.app_factory_reset_dlg_text))
                        .setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Clear user data here
                                uninstallPkg(mAppEntry.info.packageName,
                                        false, false);
                            }
                        })
                        .setNegativeButton(R.string.dlg_cancel, null)
                        .create();
        }
        return null;
    }

    private void uninstallPkg(String packageName, boolean allUsers, boolean andDisable) {
         // Create new intent to launch Uninstaller activity
        Uri packageURI = Uri.parse("package:"+packageName);
        Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageURI);
        uninstallIntent.putExtra(Intent.EXTRA_UNINSTALL_ALL_USERS, allUsers);
        startActivityForResult(uninstallIntent, REQUEST_UNINSTALL);
        mDisableAfterUninstall = andDisable;
    }

    private void forceStopPackage(String pkgName) {
        ActivityManager am = (ActivityManager)getActivity().getSystemService(
                Context.ACTIVITY_SERVICE);
        am.forceStopPackage(pkgName);
        int userId = UserHandle.getUserId(mAppEntry.info.uid);
        mState.invalidatePackage(pkgName, userId);
        ApplicationsState.AppEntry newEnt = mState.getEntry(pkgName, userId);
        if (newEnt != null) {
            mAppEntry = newEnt;
        }
        checkForceStop();
    }

    private void updateForceStopButton(boolean enabled) {
        if (mAppControlRestricted) {
            mForceStopButton.setEnabled(false);
        } else {
            mForceStopButton.setEnabled(enabled);
            mForceStopButton.setOnClickListener(InstalledAppDetails.this);
        }
    }

    private void checkForceStop() {
        if (mDpm.packageHasActiveAdmins(mPackageInfo.packageName)) {
            // User can't force stop device admin.
            updateForceStopButton(false);
        } else if ((mAppEntry.info.flags&ApplicationInfo.FLAG_STOPPED) == 0) {
            // If the app isn't explicitly stopped, then always show the
            // force stop button.
            updateForceStopButton(true);
        } else {
            Intent intent = new Intent(Intent.ACTION_QUERY_PACKAGE_RESTART,
                    Uri.fromParts("package", mAppEntry.info.packageName, null));
            intent.putExtra(Intent.EXTRA_PACKAGES, new String[] { mAppEntry.info.packageName });
            intent.putExtra(Intent.EXTRA_UID, mAppEntry.info.uid);
            intent.putExtra(Intent.EXTRA_USER_HANDLE, UserHandle.getUserId(mAppEntry.info.uid));
            getActivity().sendOrderedBroadcastAsUser(intent, UserHandle.CURRENT, null,
                    mCheckKillProcessesReceiver, null, Activity.RESULT_CANCELED, null, null);
        }
    }

    private void startManagePermissionsActivity() {
        // start new activity to manage app permissions
    	Intent intent = new Intent(ManagePermissionsInfoActivity.MANAGE_APP_PERMISSIONS);
    	intent.setComponent(new ComponentName(getContext(), ManagePermissionsInfoActivity.class));
        intent.putExtra(Intent.EXTRA_PACKAGE_NAME, mAppEntry.info.packageName);
        intent.putExtra(AppInfoWithHeader.EXTRA_HIDE_INFO_BUTTON, true);
        try {
            startActivity(intent); 
        } catch (ActivityNotFoundException e) {
            Log.w(LOG_TAG, "No app can handle android.intent.action.MANAGE_APP_PERMISSIONS");
        }
    }

    private void startAppInfoFragment(Class<?> fragment, CharSequence title) {
        // start new fragment to display extended information
        Bundle args = new Bundle();
        args.putString(ARG_PACKAGE_NAME, mAppEntry.info.packageName);
        args.putInt(ARG_PACKAGE_UID, mAppEntry.info.uid);
        args.putBoolean(AppInfoWithHeader.EXTRA_HIDE_INFO_BUTTON, true);

        SettingsActivity sa = (SettingsActivity) getActivity();
        sa.startPreferencePanel(fragment.getName(), args, -1, title, this, SUB_INFO_FRAGMENT);
    }

    /*
     * Method implementing functionality of buttons clicked
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    public void onClick(View v) {
        if (mAppEntry == null) {
            setIntentAndFinish(true, true);
            return;
        }
        String packageName = mAppEntry.info.packageName;
        if(v == mUninstallButton) {
            if ((mAppEntry.info.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                if (mAppEntry.info.enabled && !isDisabledUntilUsed()) {
                    if (mUpdatedSysApp) {
//                        showDialogInner(DLG_SPECIAL_DISABLE, 0);
                    	new mst.app.dialog.AlertDialog.Builder(getActivity())
                    	.setTitle(getActivity().getText(R.string.app_disable_dlg_positive))
                    	.setMessage(getActivity().getText(R.string.app_special_disable_dlg_text))
                    	.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
                    		public void onClick(DialogInterface dialog, int which) {
                    			// Clear user data here
                    			uninstallPkg(mAppEntry.info.packageName,
                    					false, true);
                    		}
                    	})
                    	.setNegativeButton(R.string.dlg_cancel, null)
                    	.show();
                    } else {
//                        showDialogInner(DLG_DISABLE, 0);
                    	new mst.app.dialog.AlertDialog.Builder(getActivity())
                    	.setTitle(getActivity().getText(R.string.app_disable_dlg_positive))
                    	.setMessage(getActivity().getText(R.string.app_disable_dlg_text))
                    	.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
                    		public void onClick(DialogInterface dialog, int which) {
                                // Disable the app
                                new DisableChanger(InstalledAppDetails.this, mAppEntry.info,
                                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER)
                                .execute((Object)null);
                    		}
                    	})
                    	.setNegativeButton(R.string.dlg_cancel, null)
                    	.show();
                    }
                } else {
                    new DisableChanger(this, mAppEntry.info,
                            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
                                    .execute((Object) null);
                }
            } else if ((mAppEntry.info.flags & ApplicationInfo.FLAG_INSTALLED) == 0) {
                uninstallPkg(packageName, true, false);
            } else {
                uninstallPkg(packageName, false, false);
            }
        } else if (v == mForceStopButton) {
//            showDialogInner(DLG_FORCE_STOP, 0);
            //forceStopPackage(mAppInfo.packageName);
        	new mst.app.dialog.AlertDialog.Builder(getActivity())
        	.setTitle(getActivity().getText(R.string.force_stop_dlg_title))
        	.setMessage(getActivity().getText(R.string.force_stop_dlg_text))
        	.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int which) {
        			// Force stop
        			forceStopPackage(mAppEntry.info.packageName);
        		}
        	})
        	.setNegativeButton(R.string.dlg_cancel, null)
        	.show();
        } else if (v == mClearCacheButton) {
        	new mst.app.dialog.AlertDialog.Builder(getActivity())
                .setTitle(getActivity().getText(R.string.clear_cache_dlg_title))
                .setMessage(getActivity().getText(R.string.clear_cache_dlg_text))
                .setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int which) {
                		// Lazy initialization of observer
                		if (mClearCacheObserver == null) {
                			mClearCacheObserver = new ClearCacheObserver();
                		}
                		mPm.deleteApplicationCacheFiles(mPackageName, mClearCacheObserver);
                	}
                })
                .setNegativeButton(R.string.dlg_cancel, null)
                .show();
        } else if (v == mClearDataButton) {
            if (false && mAppEntry.info.manageSpaceActivityName != null) {
                if (!Utils.isMonkeyRunning()) {
                    Intent intent = new Intent(Intent.ACTION_DEFAULT);
                    intent.setClassName(mAppEntry.info.packageName,
                            mAppEntry.info.manageSpaceActivityName);
                    startActivityForResult(intent, REQUEST_MANAGE_SPACE);
                }
            } else {
                new mst.app.dialog.AlertDialog.Builder(getActivity())
                .setTitle(getActivity().getText(R.string.clear_data_dlg_title))
                .setMessage(getActivity().getText(R.string.clear_data_dlg_text))
                .setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int which) {
                		// Clear user data here
                		initiateClearUserData();
                	}
                })
                .setNegativeButton(R.string.dlg_cancel, null)
                .show();
            }
        } else if(v == mBtnDefaultApp) {
        	confirmToClearPreferredActivity();
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mStoragePreference) {
            startAppInfoFragment(AppStorageSettings.class, mStoragePreference.getTitle());
        } else if (preference == mNotificationPreference) {
            startAppInfoFragment(AppNotificationSettings.class,
                    getString(R.string.app_notifications_title));
        } else if (preference == mPermissionsPreference) {
            startManagePermissionsActivity();
        } else if (preference == mLaunchPreference) {
            startAppInfoFragment(AppLaunchSettings.class, mLaunchPreference.getTitle());
        } else if (preference == mMemoryPreference) {
            ProcessStatsBase.launchMemoryDetail((SettingsActivity) getActivity(),
                    mStatsManager.getMemInfo(), mStats);
        } else if (preference == mDataPreference) {
            Bundle args = new Bundle();
            args.putString(DataUsageSummary.EXTRA_SHOW_APP_IMMEDIATE_PKG,
                    mAppEntry.info.packageName);

            SettingsActivity sa = (SettingsActivity) getActivity();
            sa.startPreferencePanel(DataUsageSummary.class.getName(), args, -1,
                    getString(R.string.app_data_usage), this, SUB_INFO_FRAGMENT);
        } else if (preference == mBatteryPreference) {
            BatteryEntry entry = new BatteryEntry(getActivity(), null, mUserManager, mSipper);
            PowerUsageDetail.startBatteryDetailPage((SettingsActivity) getActivity(),
                    mBatteryHelper, BatteryStats.STATS_SINCE_CHARGED, entry, true);
        } else if(preference == mAuthUsagePreference && mAuthTotalCount > 0) {
        	Intent intent = new Intent(getContext(), ManagePermissionsInfoActivity.class);
        	intent.setAction(ManagePermissionsInfoActivity.MANAGE_APP_PERMISSIONS);
        	intent.putExtra(Intent.EXTRA_PACKAGE_NAME, mAppEntry.info.packageName);
        	intent.putExtra(EXTRA_KEY_FROM_APP_DETAIL, true);
        	startActivity(intent);
        } else if(preference == mNotificationUsage) {
        	Intent intent = new Intent("android.intent.action.SHOW_APP_NOTIFY_SETTING_ACTIVITY");
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, mAppEntry.info.packageName);
            intent.putExtra(Settings.EXTRA_APP_UID, mAppEntry.info.uid);
            startActivity(intent);
        } else if(preference == mPreferenceShortcut) {
        	permOpSelectItem.showPermOpSelectDialog(getContext());
        } else {
            return false;
        }
        return true;
    }

    public static void setupAppSnippet(View appSnippet, CharSequence label, Drawable icon,
            CharSequence versionName) {
        LayoutInflater.from(appSnippet.getContext()).inflate(R.layout.widget_text_views,
                (ViewGroup) appSnippet.findViewById(android.R.id.widget_frame));

        ImageView iconView = (ImageView) appSnippet.findViewById(android.R.id.icon);
        iconView.setImageDrawable(icon);
        // Set application name.
        TextView labelView = (TextView) appSnippet.findViewById(android.R.id.title);
        labelView.setText(label);
        // Version number of application
        TextView appVersion = (TextView) appSnippet.findViewById(R.id.widget_text1);

        if (!TextUtils.isEmpty(versionName)) {
            appVersion.setSelected(true);
            appVersion.setVisibility(View.VISIBLE);
            appVersion.setText(appSnippet.getContext().getString(R.string.version_text,
                    String.valueOf(versionName)));
        } else {
            appVersion.setVisibility(View.INVISIBLE);
        }
    }

    private static NetworkTemplate getTemplate(Context context) {
        if (DataUsageSummary.hasReadyMobileRadio(context)) {
            return NetworkTemplate.buildTemplateMobileWildcard();
        }
        if (DataUsageSummary.hasWifiRadio(context)) {
            return NetworkTemplate.buildTemplateWifiWildcard();
        }
        return NetworkTemplate.buildTemplateEthernet();
    }

    public static CharSequence getNotificationSummary(AppEntry appEntry, Context context) {
        return getNotificationSummary(appEntry, context, new NotificationBackend());
    }

    public static CharSequence getNotificationSummary(AppEntry appEntry, Context context,
            NotificationBackend backend) {
        AppRow appRow = backend.loadAppRow(context.getPackageManager(), appEntry.info);
        return getNotificationSummary(appRow, context);
    }

    public static CharSequence getNotificationSummary(AppRow appRow, Context context) {
        if (appRow.banned) {
            return context.getString(R.string.notifications_disabled);
        }
        ArrayList<CharSequence> notifSummary = new ArrayList<>();
        if (appRow.priority) {
            notifSummary.add(context.getString(R.string.notifications_priority));
        }
        if (appRow.sensitive) {
            notifSummary.add(context.getString(R.string.notifications_sensitive));
        }
        if (!appRow.peekable) {
            notifSummary.add(context.getString(R.string.notifications_no_peeking));
        }
        switch (notifSummary.size()) {
            case 3:
                return context.getString(R.string.notifications_three_items,
                        notifSummary.get(0), notifSummary.get(1), notifSummary.get(2));
            case 2:
                return context.getString(R.string.notifications_two_items,
                        notifSummary.get(0), notifSummary.get(1));
            case 1:
                return notifSummary.get(0);
            default:
                return context.getString(R.string.notifications_enabled);
        }
    }

    private class MemoryUpdater extends AsyncTask<Void, Void, ProcStatsPackageEntry> {

        @Override
        protected ProcStatsPackageEntry doInBackground(Void... params) {
            if (getActivity() == null) {
                return null;
            }
            if (mPackageInfo == null) {
                return null;
            }
            if (mStatsManager == null) {
                mStatsManager = new ProcStatsData(getActivity(), false);
                mStatsManager.setDuration(ProcessStatsBase.sDurations[0]);
            }
            mStatsManager.refreshStats(true);
            for (ProcStatsPackageEntry pkgEntry : mStatsManager.getEntries()) {
                for (ProcStatsEntry entry : pkgEntry.mEntries) {
                    if (entry.mUid == mPackageInfo.applicationInfo.uid) {
                        pkgEntry.updateMetrics();
                        return pkgEntry;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(ProcStatsPackageEntry entry) {
            if (getActivity() == null) {
                return;
            }
            if (entry != null) {
                mStats = entry;
//                mMemoryPreference.setEnabled(true);
                double amount = Math.max(entry.mRunWeight, entry.mBgWeight)
                        * mStatsManager.getMemInfo().weightToRam;
//                mMemoryPreference.setSummary(getString(R.string.memory_use_summary,
//                        Formatter.formatShortFileSize(getContext(), (long) amount)));
                
                memUsageStr = getString(R.string.memory_use_summary,
                        Formatter.formatShortFileSize(getContext(), (long) amount));
            } else {
//                mMemoryPreference.setEnabled(false);
//                mMemoryPreference.setSummary(getString(R.string.no_memory_use_summary));
                
//                memUsageStr = getString(R.string.no_memory_use_summary);
            }
            
            updateResourceUsage();
        }
    }

    private class BatteryUpdater extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            mBatteryHelper.create((Bundle) null);
            mBatteryHelper.refreshStats(BatteryStats.STATS_SINCE_CHARGED,
                    mUserManager.getUserProfiles());
            List<BatterySipper> usageList = mBatteryHelper.getUsageList();
            final int N = usageList.size();
            for (int i = 0; i < N; i++) {
                BatterySipper sipper = usageList.get(i);
                if (sipper.getUid() == mPackageInfo.applicationInfo.uid) {
                    mSipper = sipper;
                    break;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (getActivity() == null) {
                return;
            }
            refreshUi();
        }
    }

    private static class DisableChanger extends AsyncTask<Object, Object, Object> {
        final PackageManager mPm;
        final WeakReference<InstalledAppDetails> mActivity;
        final ApplicationInfo mInfo;
        final int mState;

        DisableChanger(InstalledAppDetails activity, ApplicationInfo info, int state) {
            mPm = activity.mPm;
            mActivity = new WeakReference<InstalledAppDetails>(activity);
            mInfo = info;
            mState = state;
        }

        @Override
        protected Object doInBackground(Object... params) {
            mPm.setApplicationEnabledSetting(mInfo.packageName, mState, 0);
            return null;
        }
    }

    private final LoaderCallbacks<ChartData> mDataCallbacks = new LoaderCallbacks<ChartData>() {

        @Override
        public Loader<ChartData> onCreateLoader(int id, Bundle args) {
            return new ChartDataLoader(getActivity(), mStatsSession, args);
        }

        @Override
        public void onLoadFinished(Loader<ChartData> loader, ChartData data) {
            mChartData = data;
            mDataPreference.setSummary(getDataSummary());
//            if(mDataPreference != null) {
//            	mDataTotalPreference.setSummary(getTotaDatalConnect());
//            }
//            if(mWifiDataTotalPreference != null){
//            	mWifiDataTotalPreference.setSummary(getWifiDataConnect());
//            }
        }

        @Override
        public void onLoaderReset(Loader<ChartData> loader) {
            // Leave last result.
        }
    };

    private final BroadcastReceiver mCheckKillProcessesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateForceStopButton(getResultCode() != Activity.RESULT_CANCELED);
        }
    };

    private final PermissionsResultCallback mPermissionCallback
            = new PermissionsResultCallback() {
        @Override
        public void onPermissionSummaryResult(int standardGrantedPermissionCount,
                int requestedPermissionCount, int additionalGrantedPermissionCount,
                List<CharSequence> grantedGroupLabels) {
            if (getActivity() == null) {
                return;
            }
            final Resources res = getResources();
            CharSequence summary = null;

            if (requestedPermissionCount == 0) {
                summary = res.getString(
                        R.string.runtime_permissions_summary_no_permissions_requested);
                mPermissionsPreference.setOnPreferenceClickListener(null);
                mPermissionsPreference.setEnabled(false);
            } else {
                final ArrayList<CharSequence> list = new ArrayList<>(grantedGroupLabels);
                if (additionalGrantedPermissionCount > 0) {
                    // N additional permissions.
                    list.add(res.getQuantityString(
                            R.plurals.runtime_permissions_additional_count,
                            additionalGrantedPermissionCount, additionalGrantedPermissionCount));
                }
                if (list.size() == 0) {
                    summary = res.getString(
                            R.string.runtime_permissions_summary_no_permissions_granted);
                } else {
                    summary = ListFormatter.getInstance().format(list);
                }
                mPermissionsPreference.setOnPreferenceClickListener(InstalledAppDetails.this);
                mPermissionsPreference.setEnabled(true);
            }
            mPermissionsPreference.setSummary(summary);
        }
    };
    
    //////////////////////////////////////////////////
    private boolean mIsInitItemShow = false;
    
    private void initView() {
    	handleControlButtonPannel();
    	initStorageCategory();
    	initNotification();
    	initDataConnect();
    	initResourceUsage();
    	initAuth();
    	initOthers();
    	
    	initItemShow();
    }
    
    private void initItemShow() {
        if (!mIsInitItemShow && mAppEntry != null && mAppEntry.info != null) {
        	mIsInitItemShow = true;
        	if ((mAppEntry.info.flags & ApplicationInfo.FLAG_SYSTEM) == 1 || Utils.isSystemPackage(mPm, mPackageInfo)) {
        		removePreference("notification_category");
        		removePreference("auth_category");
        		mOthersCategory.removePreference(mSwitchOverlayWindow);
        		mOthersCategory.removePreference(mPreferenceShortcut);
        	}
        }
    }
    
    ///////////////////////////////////////////////////
    // control button
    private void handleControlButtonPannel() {
//        CommonPreference preference = (CommonPreference)findPreference(KEY_CONTROL_BUTTON_PANNEL);
//        View btnPanel = preference.findViewById(R.id.control_buttons_panel);
//        mForceStopButton = (Button) btnPanel.findViewById(R.id.left_button);
//        mUninstallButton = (Button) btnPanel.findViewById(R.id.right_button);
    	
    	ViewGroup pinnedFooter = (ViewGroup)getView().findViewById(R.id.pinned_footer);
    	View buttonContainer = getActivity().getLayoutInflater().inflate(R.layout.two_button_panel, pinnedFooter);
        mForceStopButton = (Button)buttonContainer.findViewById(R.id.button1);
        mUninstallButton = (Button)buttonContainer.findViewById(R.id.button2);
        
        mForceStopButton.setText(R.string.force_stop);
        mForceStopButton.setEnabled(false);
    }
    
    ///////////////////////////////////////////////
    // storage
    private static final String KEY_STORAGE_CATEGORY = "storage_category";
    private static final String KEY_APP_SIZE = "app_size";
    private static final String KEY_DATA_SIZE = "data_size";
    private static final String KEY_CACHE_SIZE = "cache_size";
    
    private PreferenceCategory category;
    private Preference mAppSize;
    private ButtonPreference mDataSize;
    private ButtonPreference mCacheSize;
    private Button mClearDataButton;
    private Button mClearCacheButton;
    private CharSequence mInvalidSizeStr;
    private CharSequence mComputingStr;
    private boolean mCanClearData = true;
    private boolean mHaveSizes = false;
    private long mLastCodeSize = -1;
    private long mLastDataSize = -1;
    private long mLastExternalCodeSize = -1;
    private long mLastExternalDataSize = -1;
    private long mLastCacheSize = -1;
    private long mLastTotalSize = -1;
    private static final int SIZE_INVALID = -1;
    private static final int MSG_CLEAR_USER_DATA = 1;
    private static final int MSG_CLEAR_CACHE = 3;
    private static final int OP_SUCCESSFUL = 1;
    private static final int OP_FAILED = 2;
    public static final int REQUEST_MANAGE_SPACE = 2;
    private ClearCacheObserver mClearCacheObserver;
    private ClearUserDataObserver mClearDataObserver;
    
    private void initStorageCategory() {
        category = (PreferenceCategory) findPreference(KEY_STORAGE_CATEGORY);
        mAppSize =  findPreference(KEY_APP_SIZE);
        mDataSize =  (ButtonPreference)findPreference(KEY_DATA_SIZE);
        mCacheSize = (ButtonPreference)findPreference(KEY_CACHE_SIZE);
//        setPreferenceTextColor((CommonPreference)mAppSize);
        
		mClearDataButton = mDataSize.getButton();
		mClearDataButton.setText(R.string.clear_cache_btn_text);
        mClearCacheButton = mCacheSize.getButton();
        mClearCacheButton.setText(R.string.clear_cache_btn_text);
        mClearDataButton.setOnClickListener(this);
        mClearCacheButton.setOnClickListener(this);
        
        mComputingStr = getActivity().getText(R.string.computing_size);
        mInvalidSizeStr = getActivity().getText(R.string.invalid_size_value);
    }
    
    private void refreshSizeInfo() {
        if (mAppEntry.size == ApplicationsState.SIZE_INVALID
                || mAppEntry.size == ApplicationsState.SIZE_UNKNOWN) {
            mLastCodeSize = mLastDataSize = mLastCacheSize = mLastTotalSize = -1;
            if (!mHaveSizes) {
                mAppSize.setSummary(mComputingStr);
                mDataSize.setSummary(mComputingStr);
                mCacheSize.setSummary(mComputingStr);
                category .setSummary(mComputingStr);
            }
            mClearDataButton.setEnabled(false);
            mClearCacheButton.setEnabled(false);

        } else {
            mHaveSizes = true;
            long codeSize = mAppEntry.codeSize;
            long dataSize = mAppEntry.dataSize;
            if (Environment.isExternalStorageEmulated()) {
                codeSize += mAppEntry.externalCodeSize;
                dataSize +=  mAppEntry.externalDataSize;
            } else {
                if (mLastExternalCodeSize != mAppEntry.externalCodeSize) {
                    mLastExternalCodeSize = mAppEntry.externalCodeSize;
//                    mExternalCodeSize.setSummary(getSizeStr(mAppEntry.externalCodeSize));
                }
                if (mLastExternalDataSize !=  mAppEntry.externalDataSize) {
                    mLastExternalDataSize =  mAppEntry.externalDataSize;
//                    mExternalDataSize.setSummary(getSizeStr( mAppEntry.externalDataSize));
                }
            }
            if (mLastCodeSize != codeSize) {
                mLastCodeSize = codeSize;
                mAppSize.setSummary(getSizeStr(codeSize));
            }
            if (mLastDataSize != dataSize) {
                mLastDataSize = dataSize;
                mDataSize.setSummary(getSizeStr(dataSize));
            }
            long cacheSize = mAppEntry.cacheSize + mAppEntry.externalCacheSize;
            if (mLastCacheSize != cacheSize) {
                mLastCacheSize = cacheSize;
                mCacheSize.setSummary(getSizeStr(cacheSize));
            }
            if (mLastTotalSize != mAppEntry.size) {
                mLastTotalSize = mAppEntry.size;
                category .setSummary(getSizeStr(mAppEntry.size));
            }

            if ((mAppEntry.dataSize+ mAppEntry.externalDataSize) <= 0 || !mCanClearData) {
                mClearDataButton.setEnabled(false);
            } else {
                mClearDataButton.setEnabled(true);
                mClearDataButton.setOnClickListener(this);
            }
            if (cacheSize <= 0) {
                mClearCacheButton.setEnabled(false);
            } else {
                mClearCacheButton.setEnabled(true);
                mClearCacheButton.setOnClickListener(this);
            }
        }
        if (mAppControlRestricted) {
            mClearCacheButton.setEnabled(false);
            mClearDataButton.setEnabled(false);
        }
    }
    
    private void initDataButtons() {
        // If the app doesn't have its own space management UI
        // And it's a system app that doesn't allow clearing user data or is an active admin
        // Then disable the Clear Data button.
        if (mAppEntry.info.manageSpaceActivityName == null
                && ((mAppEntry.info.flags&(ApplicationInfo.FLAG_SYSTEM
                        | ApplicationInfo.FLAG_ALLOW_CLEAR_USER_DATA))
                        == ApplicationInfo.FLAG_SYSTEM
                        || mDpm.packageHasActiveAdmins(mPackageName))) {
            mClearDataButton.setText(R.string.clear_user_data_text);
            mClearDataButton.setEnabled(false);
            mCanClearData = false;
        } else {
            if (mAppEntry.info.manageSpaceActivityName != null) {
                mClearDataButton.setText(R.string.manage_space_text);
            } else {
                mClearDataButton.setText(R.string.clear_user_data_text);
            }
            mClearDataButton.setOnClickListener(this);
        }

        if (mAppControlRestricted) {
            mClearDataButton.setEnabled(false);
        }
    }
    
    private String getSizeStr(long size) {
        if (size == SIZE_INVALID) {
            return mInvalidSizeStr.toString();
        }
//        return Formatter.formatFileSize(getActivity(), size);
        return Utils.formatFileSize(getActivity(), size);
    }
    
    private void refreshButtons() {
        initDataButtons();
    }
    
    /*
     * Private method to initiate clearing user data when the user clicks the clear data
     * button for a system package
     */
    private void initiateClearUserData() {
        mClearDataButton.setEnabled(false);
        // Invoke uninstall or clear user data based on sysPackage
        String packageName = mAppEntry.info.packageName;
        Log.i(TAG, "Clearing user data for package : " + packageName);
        if (mClearDataObserver == null) {
            mClearDataObserver = new ClearUserDataObserver();
        }
        ActivityManager am = (ActivityManager)
                getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        boolean res = am.clearApplicationUserData(packageName, mClearDataObserver);
        if (!res) {
            // Clearing data failed for some obscure reason. Just log error for now
            Log.i(TAG, "Couldnt clear application user data for package:"+packageName);
//            showDialogInner(DLG_CANNOT_CLEAR_DATA, 0);
        } else {
            mClearDataButton.setText(R.string.recompute_size);
        }
    }
    
    class ClearCacheObserver extends IPackageDataObserver.Stub {
        public void onRemoveCompleted(final String packageName, final boolean succeeded) {
            final Message msg = mHandler.obtainMessage(MSG_CLEAR_CACHE);
            msg.arg1 = succeeded ? OP_SUCCESSFUL : OP_FAILED;
            mHandler.sendMessage(msg);
        }
    }

    class ClearUserDataObserver extends IPackageDataObserver.Stub {
       public void onRemoveCompleted(final String packageName, final boolean succeeded) {
           final Message msg = mHandler.obtainMessage(MSG_CLEAR_USER_DATA);
           msg.arg1 = succeeded ? OP_SUCCESSFUL : OP_FAILED;
           mHandler.sendMessage(msg);
        }
    }
    
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (getView() == null) {
                return;
            }
            switch (msg.what) {
                case MSG_CLEAR_USER_DATA:
                    processClearMsg(msg);
                    break;
                case MSG_CLEAR_CACHE:
                    // Refresh size info
                    mState.requestSize(mPackageName, mUserId);
                    break;
            }
        }
    };
    
    /*
     * Private method to handle clear message notification from observer when
     * the async operation from PackageManager is complete
     */
    private void processClearMsg(Message msg) {
        int result = msg.arg1;
        String packageName = mAppEntry.info.packageName;
        mClearDataButton.setText(R.string.clear_user_data_text);
        if (result == OP_SUCCESSFUL) {
            Log.i(TAG, "Cleared user data for package : "+packageName);
            mState.requestSize(mPackageName, mUserId);
        } else {
            mClearDataButton.setEnabled(true);
        }
    }
    
    ///////////////////////////////////////////////
    // notification
    private static final String NOTIFICATION_CONTENT_URI = "content://com.android.systemui.tcl.WdjNotificationProvider/notify_count";
    private SwitchPreference mSwitchNotifcation;
    private CommonPreference mNotificationUsage; 
    
    private void initNotification() {
        PreferenceCategory category = (PreferenceCategory) findPreference("notification_category");
        mNotificationUsage = (CommonPreference)category.findPreference("notification_brief");
        TextView item1 = (TextView)mNotificationUsage.findViewById(R.id.notification_item1);
        TextView item2 = (TextView)mNotificationUsage.findViewById(R.id.notification_item2);
        mNotificationUsage.setOnPreferenceClickListener(this);
        
        long installTime = System.currentTimeMillis();
        if(mPackageInfo != null) {
        	installTime = mPackageInfo.firstInstallTime;
        }
        long[] notificationCount = getTotalNotification();
        if(notificationCount == null) {
        	notificationCount = new long[2];
        	notificationCount[0] = 0;
        	notificationCount[1] = 0;
        }
        String usage = getString(R.string.notification_item1,
                    notificationCount[0],
                    DateUtils.formatDateTime(getContext(), installTime,
                            DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH));
        item1.setText(usage);
        
        usage = getString(R.string.notification_item2, notificationCount[1]);
        item2.setText(usage);
        
        mSwitchNotifcation = (SwitchPreference)category.findPreference("notification_receive");
        mSwitchNotifcation.setOnPreferenceChangeListener(this);
        updateNotifcationSwitch();
    }
    
    private void updateNotifcationSwitch() {
    	if(mAppEntry != null && mAppEntry.info != null) {
    		AppRow appRow = mBackend.loadAppRow(getPackageManager(), mAppEntry.info);
    		mSwitchNotifcation.setChecked(!appRow.banned);
    	}
    }
    
    private long[] getTotalNotification() {
    	return mst.utils.SystemUiHelper.getNotifyCount(getContext().getApplicationContext(), mPackageName);
    }
    
    private long getNonImportantNotification() {
    	long result = 0;
    	try {
    		Cursor cursor = getContentResolver().query(
    				Uri.parse(NOTIFICATION_CONTENT_URI), null, null, null,  null);
    		if(cursor!=null && cursor.getCount() > 0){
    			cursor.moveToFirst();
    			result = cursor.getLong(cursor.getColumnIndex("count"));
    		}
    		if(cursor != null) {
    			cursor.close();
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return result;
    }
    
    ///////////////////////////////////////////////
    private Preference mDataTotalPreference;
    private Preference mWifiDataTotalPreference;
    private SwitchPreference mSwitchDataConnect;
    private INetworkManagementService mNetworkService;
    
    // data connect
    private void initDataConnect() {
        PreferenceCategory category = (PreferenceCategory) findPreference("data_category");
        mDataTotalPreference = category.findPreference("data_total");
        mWifiDataTotalPreference = category.findPreference("data_wifi_total");
        mSwitchDataConnect = (SwitchPreference)category.findPreference("data_switch_toggle");
        mSwitchDataConnect.setOnPreferenceChangeListener(this);
        
        mDataTotalPreference.setSummary(getTotaDatalConnect());
        mWifiDataTotalPreference.setSummary(getWifiDataConnect());
        
        mNetworkService = INetworkManagementService.Stub.asInterface(
        		ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
        regDataConnectReceiver();
        
        initDataConnectEnabled();
        updateDataConnectSwitch();
    }
    
    private void initDataConnectEnabled() {
    	boolean hasPermission = false;
    	int flag = mPm.checkPermission(Manifest.permission.INTERNET, mPackageName);
    	if(flag == PackageManager.PERMISSION_GRANTED) {
    		hasPermission = true;
    	}
    	mSwitchDataConnect.setEnabled(hasPermission);
    }
    
    private void updateDataConnectSwitch() {
    	if(mSwitchDataConnect.isEnabled()) {
    		mSwitchDataConnect.setChecked(!isDataConnectDisabled());
    	}
    }
    
    private String getTotaDatalConnect() {
    	long totalBytes = 0;
    	
//        if (mChartData != null) {
//            totalBytes = mChartData.detail.getTotalBytes();
//        }
        return Formatter.formatFileSize(getContext(), totalBytes);
    }
    
    private String getWifiDataConnect() {
    	long totalBytes = 0;
        return Formatter.formatFileSize(getContext(), totalBytes);
    }
    
    private void setDataConnectDisableAsync(final boolean isReject) {
//    	new Thread(new Runnable() {
//			@Override
//			public void run() {
//				setDataConnectDisable(isReject);
//			}
//		}).start();
    	
    	Intent intent = new Intent("com.monster.netmanage.action.updatedata_state");
    	intent.putExtra("CHAGE_STATE_UID", mPackageInfo.applicationInfo.uid);
    	intent.putExtra("CHAGE_STATE", !isReject);
    	getActivity().sendBroadcast(intent);
    }
    
    private void setDataConnectDisable(final boolean isReject) {
    	if(mNetworkService != null) {
    		try {
    			//传入true代表要禁止其联网。                  
				mNetworkService.setUidDataRules(mPackageInfo.applicationInfo.uid, isReject);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
    		
			try {
				Context otherAppContext = getActivity().createPackageContext("com.monster.netmanage", Context.CONTEXT_IGNORE_SECURITY);
				SharedPreferences sharedPreferences = otherAppContext.getSharedPreferences("com.monster.netmanage_preferences",Context.MODE_WORLD_READABLE|Context.MODE_MULTI_PROCESS);
				
				String typeData = sharedPreferences.getString("type_data", "");
				boolean isDataDisabled = false;
				boolean isNoneItem = true;
				if(!TextUtils.isEmpty(typeData)) {
					isNoneItem = false;
					String matchStr = mPackageInfo.applicationInfo.uid+"";
					String matchTypeData = typeData;
					if(typeData.contains(",")) {
						matchTypeData = "," + typeData + ",";
						matchStr = "," + matchStr + ",";
					}
					isDataDisabled = matchTypeData.contains(matchStr); 
				}
				
				String outStr = null;
				if(isReject && !isDataDisabled) {
					// 禁用
					if(isNoneItem) {
						outStr = "" + mPackageInfo.applicationInfo.uid;
					} else {
						outStr = typeData + "," + mPackageInfo.applicationInfo.uid;
					}
					sharedPreferences.edit().putString("type_data", typeData).commit();
				} else if(!isReject && isDataDisabled) {
					// 启用
					String[] items = typeData.split(",");
					if(items != null && items.length > 0) {
						StringBuilder sb = new StringBuilder();
						String matchStr = mPackageInfo.applicationInfo.uid+"";
						String itemStr;
						for (int i = 0; i < items.length; i++) {
							itemStr = items[i];
							if(!TextUtils.isEmpty(itemStr) && !itemStr.equals(matchStr)) {
								sb.append(itemStr);
								if((i + 1) < items.length) {
									sb.append(",");
								}
							}
						}
						outStr = sb.toString();
					} else {
						outStr = mPackageInfo.applicationInfo.uid+"";
					}
				}
				
				if(outStr != null) {
					boolean result = sharedPreferences.edit().putString("type_data", outStr).commit();
				}
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
    	}
    }
    
    private boolean isDataConnectDisabled() {
    	boolean result = false;
    	try {
    		Context otherAppContext = getActivity().createPackageContext("com.monster.netmanage", Context.CONTEXT_IGNORE_SECURITY);
    		SharedPreferences sharedPreferences = otherAppContext.getSharedPreferences("com.monster.netmanage_preferences",Context.MODE_WORLD_READABLE|Context.MODE_MULTI_PROCESS);
    		String typeData = sharedPreferences.getString("type_data", "");
    		if(!TextUtils.isEmpty(typeData)) {
    			String matchStr = mPackageInfo.applicationInfo.uid+"";
    			if(typeData.contains(",")) {
    				typeData = "," + typeData + ",";
    				matchStr = "," + matchStr + ",";
    			}
    			result = typeData.contains(matchStr); 
    		}
    	} catch (NameNotFoundException e) {
    		e.printStackTrace();
    	}
    	
    	return result;
    }
    
    private void regDataConnectReceiver() {
    	IntentFilter filter = new IntentFilter();
    	filter.addAction("com.monster.netmanage.action.send.updatedata");
    	getActivity().registerReceiver(mReceiver, filter);
    	
    	Intent intent = new Intent("com.monster.netmanage.action.updatedata");
    	intent.putExtra("MOBILE_POLICY_UID", mPackageInfo.applicationInfo.uid);
    	getActivity().sendBroadcast(intent);
    }
    
    private void unRegDataconnectReceiver() {
    	try {
    		getActivity().unregisterReceiver(mReceiver);
		} catch (Exception e) {
		}
    }
    
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(("com.monster.netmanage.action.send.updatedata").equals(action)) {
				String mobileData = intent.getStringExtra("MOBILE_DATA");
				String wifiData = intent.getStringExtra("WIFI_DATA");
				
				if(!TextUtils.isEmpty(mobileData) && !"null".equals(mobileData)) {
					mDataTotalPreference.setSummary(mobileData);
				}
				if(!TextUtils.isEmpty(wifiData) && !"null".equals(wifiData)) {
					mWifiDataTotalPreference.setSummary(wifiData);
				}
			}
		}
	};
    
    ///////////////////////////////////////////////
    // resource usage
    private static final String BOOT_START_PERMISSION = "android.permission.RECEIVE_BOOT_COMPLETED";
    private static final String AUTOSTART_CONTENT_URI = "content://com.monster.autostart.db/AutoStartApps";
    private CommonPreference mResourceUsagePreference;
    private SwitchPreference mSwitchAutostart;
    private String batteryUsageStr;
    private String memUsageStr;
    private TextView batteryUsageTv;
    private TextView memUsageTv;
    
    private void initResourceUsage() {
        PreferenceCategory category = (PreferenceCategory) findPreference("resource_category");
        mResourceUsagePreference = (CommonPreference)category.findPreference("resource_usage");
        mSwitchAutostart = (SwitchPreference)category.findPreference("switch_autostart");
        mSwitchAutostart.setOnPreferenceChangeListener(this);
//        batteryUsageStr = getString(R.string.no_battery_summary);
//        memUsageStr = getString(R.string.no_memory_use_summary);
        
        batteryUsageStr = getString(R.string.battery_summary, 0);
        memUsageStr = getString(R.string.memory_use_summary,
        		Formatter.formatShortFileSize(getContext(), 0));
        
        batteryUsageTv = (TextView)mResourceUsagePreference.findViewById(R.id.notification_item1);
        memUsageTv = (TextView)mResourceUsagePreference.findViewById(R.id.notification_item2);
        mResourceUsagePreference.findViewById(R.id.pref_image_detail).setVisibility(View.GONE);
    }
    
    private void updateResourceUsage() {
//         mResourceUsagePreference.setTitle(batteryUsageStr + ";\n" + memUsageStr + ";");
    	batteryUsageTv.setText(batteryUsageStr);
    	memUsageTv.setText(memUsageStr);
    }
    
    private void updateAutostartSwitch() {
    	boolean hasPermission = false;
//    	int flag = mPm.checkPermission(BOOT_START_PERMISSION, mPackageName);
//    	if(flag == PackageManager.PERMISSION_GRANTED) {
//    		hasPermission = true;
//    	}
    	
    	hasPermission = isAutostartEnable();
    	mSwitchAutostart.setEnabled(hasPermission);
    	if(hasPermission) {
    		mSwitchAutostart.setChecked(isAutostartChecked());
    	}
    }
    
    private boolean isAutostartEnable() {
    	boolean isEnable = false;
    	try {
    		Cursor cursor = getContentResolver().query(
    				Uri.parse(AUTOSTART_CONTENT_URI), new String[] {"intent,status"}, "intent LIKE '%component=" + mPackageName + "/%;end'", null,  null);
    		if(cursor!=null && cursor.getCount() > 0){
    			isEnable = true;
    		}
    		if(cursor != null) {
    			cursor.close();
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return isEnable;
    }
    
    private boolean isAutostartChecked() {
    	boolean isEnable = false;
    	try {
    		Cursor cursor = getContentResolver().query(
    				Uri.parse(AUTOSTART_CONTENT_URI), new String[] {"intent,status"}, "intent LIKE '%component=" + mPackageName + "/%;end' and status=1", null,  null);
    		if(cursor!=null && cursor.getCount() > 0){
    			isEnable = true;
    		}
    		if(cursor != null) {
    			cursor.close();
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return isEnable;
    }
    
    private void setAutostartChecked(boolean enable) {
    	int result = enable ? 1 : 0;
		ContentValues contentValues = new ContentValues();
		contentValues.put("status", result);
		try {
			getContentResolver().update(Uri.parse(AUTOSTART_CONTENT_URI), contentValues,  "intent LIKE '%component=" + mPackageName + "/%;end'", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    ///////////////////////////////////////////////
    // Auth
    private CommonPreference mAuthUsagePreference;
    private TextView mAuthCountTv;
    private TextView mAuthEnabledCountTv;
    private int mAuthTotalCount;
    private MstPermission mMstPermission;
    
    private void initAuth() {
        mMstPermission = new MstPermission(getContext());
        mMstPermission.setmListener(this);
        PreferenceCategory category = (PreferenceCategory) findPreference("auth_category");
        mAuthUsagePreference = (CommonPreference)category.findPreference("auth_usage");
        mAuthCountTv = (TextView)mAuthUsagePreference.findViewById(R.id.notification_item1);
        mAuthEnabledCountTv = (TextView)mAuthUsagePreference.findViewById(R.id.notification_item2);
        mAuthUsagePreference.setOnPreferenceClickListener(this);
        setAuthText(0, 0);
        
//        getAppPermission();
//        updateAuth();
        
//        ButtonPreference mAuthOptimize;
//        mAuthOptimize =  (ButtonPreference)category.findPreference("auth_optimize");
//        mAuthOptimize.getButton().setText(R.string.auth_optimize);
//        mAuthOptimize.getButton().setOnClickListener(this);
    }
    
    private void updateAuth() {
    	if(mMstPermission == null) {
    		return;
    	}
    	
    	MstAppGroup appGroup =  mMstPermission.getAppGroupMap().get(mPackageName);
    	if(appGroup != null) {
    		mAuthTotalCount = appGroup.size();
    		setAuthText(mAuthTotalCount, appGroup.getGrantedCount());
    	}
    }
    
    private void setAuthText(int totalCount, int enabledCount) {
    	String usage = getString(R.string.auth_usage, totalCount);
    	mAuthCountTv.setText(usage);

    	usage = getString(R.string.auth_enabled,  enabledCount);
    	mAuthEnabledCountTv.setText(usage);
    }
    
    private void refreshAuth() {
    	List<String> packageNameList = new ArrayList<>();
    	packageNameList.add(mPackageName);
        mMstPermission.refreshAsync(packageNameList, null);
    }
    
	@Override
	public void onPermRefreshComplete() {
		if(getActivity() != null) {
			updateAuth();
		}
	}

	@Override
	public void onPermRefreshError() {
	}
    
    ///////////////////////////////////////////////
    // others
    private SwitchPreference mSwitchOverlayWindow;
    private AppOpsManager mAppOpsManager;
    private AppStateOverlayBridge mOverlayBridge;
    private OverlayState mOverlayState;
    private PreferenceCategory mOthersCategory; 
    private ButtonPreference mPreferenceShortcut;
    private ButtonPreference mPreferenceDefaultApp;
    private Button mBtnDefaultApp;
//    private Button mBtnShortcut;
//    private AppPermissions mAppPermissions;
    //private AppPermissionGroup mShortcutPermissionGroup;
//    private String[] permissionTypes;
    private MstPermEntry permEntry;
    private MstPermEntry overlayPermEntry;
    private PermOpSelectItem permOpSelectItem;
	private TextView shortcutText;
    
    private void initOthers() {
    	// overlay window
        mOverlayBridge = new AppStateOverlayBridge(getActivity(), mState, null);
        mAppOpsManager = (AppOpsManager) getActivity().getSystemService(Context.APP_OPS_SERVICE);
        mOthersCategory = (PreferenceCategory) findPreference("others_category");
        mSwitchOverlayWindow = (SwitchPreference)mOthersCategory.findPreference("switch_overlay_window");
        mSwitchOverlayWindow.setOnPreferenceChangeListener(this);
        
        // shortcut
//		permissionTypes = getContext().getResources().getStringArray(R.array.permition_types);
//        mPreferenceShortcut =  (ButtonPreference)mOthersCategory.findPreference("btn_shortcut");
//        View arrowImg = mPreferenceShortcut.findViewById(R.id.option_arrow);
//        arrowImg.setVisibility(View.VISIBLE);
//        mBtnShortcut = mPreferenceShortcut.getButton();
//        mBtnShortcut.setEnabled(false);
//        mBtnShortcut.setText(R.string.app_detail_btn_shortcut_ask);
//        mBtnShortcut.setOnClickListener(this);
//        mBtnShortcut.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				PopupMenu popupMenu = new PopupMenu(getActivity(), v, Gravity.RIGHT);
//				popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//					@Override
//					public boolean onMenuItemClick(MenuItem item) {
//						if(permEntry != null) {
//							int id = item.getItemId();
//							int mode = MstPermission.UNKNOWN_MODE;
//							if(id == R.id.option_ask) {
//								mode = MstPermission.ASK_MODE;
//							} else if(id == R.id.option_allow) {
//								mode = MstPermission.ALLOW_MODE;
//							} else if(id == R.id.option_disallow) {
//								mode = MstPermission.DISABLE_MODE;
//							}
//							permEntry.setStatus(mode);
//							mBtnShortcut.setText(item.getTitle());
//							updateMode(permEntry);
//						}
//						return true;
//					}
//				});
//				popupMenu.inflate(R.menu.shortcut_options);
//				popupMenu.show();
//			}
//		});
        
        // default app
        mPreferenceDefaultApp =  (ButtonPreference)mOthersCategory.findPreference("btn_default_app");
        mBtnDefaultApp = mPreferenceDefaultApp.getButton();
        mBtnDefaultApp.setOnClickListener(this);        
        
    	permEntry = MstPermission.getPermissionEntry(getContext(), mPackageName, MstPermission.INSTALL_SHORTCUT_GROUP);
    	
    	// shortcut
        mPreferenceShortcut =  (ButtonPreference)mOthersCategory.findPreference("btn_shortcut");
        mPreferenceShortcut.setOnPreferenceClickListener(this);
        shortcutText = (TextView)mPreferenceShortcut.findViewById(R.id.widget_text3);
        if(permEntry != null) {
        	permOpSelectItem = new PermOpSelectItem(getContext());
        	permOpSelectItem.setPermEntry(permEntry);
        	permOpSelectItem.setListener(new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(shortcutText != null) {
						shortcutText.setText(permOpSelectItem.getCurrenModeLabel());
					}
				}
			});
        } else {
        	shortcutText.setText(R.string.app_detail_btn_shortcut_disallow);
        	mPreferenceShortcut.findViewById(android.R.id.title).setEnabled(false);
        }
    }
    
    private void updateShortcutSwitch() {
//    	boolean hasPermission = false;
//    	int flag = mPm.checkPermission(MstPermission.INSTALL_SHORTCUT_GROUP, mPackageName);
//    	if(flag == PackageManager.PERMISSION_GRANTED) {
//    		hasPermission = true;
//    	}
//    	mBtnShortcut.setEnabled(hasPermission);
    	
//    	mBtnShortcut.setEnabled(permEntry != null);
//    	if(permEntry != null) {
//			mBtnShortcut.setText(getCurrenModeLabel());
//    	}
    	
    	permEntry = MstPermission.getPermissionEntry(getContext(), mPackageName, MstPermission.INSTALL_SHORTCUT_GROUP);
    	mPreferenceShortcut.setEnabled(permEntry != null);
    	if(permEntry != null) {
    		permOpSelectItem.setPermEntry(permEntry);
    		shortcutText.setText(permOpSelectItem.getCurrenModeLabel());
    	}
    }
    
//    private String getCurrenModeLabel() {
//		String label = null;
//		int mode = permEntry.getStatus();
//		if(mode == MstPermission.ALLOW_MODE) {
//			label = permissionTypes[0];
//		} else if(mode == MstPermission.ASK_MODE) {
//			label = permissionTypes[1];
//		} else {
//			label = permissionTypes[2];
//		}
//		
//		return label;
//	}
    
	private void updateMode(MstPermEntry permEntry) {
		MstPermission.mstUpdatePermissionStatusToDb(getContext(), permEntry);
	}
    
    private void updateOverlayWindowPermission() {
//    	mOverlayState = mOverlayBridge.getOverlayInfo(mPackageName,
//    			mPackageInfo.applicationInfo.uid);
//        boolean isAllowed = mOverlayState.isPermissible();
//        mSwitchOverlayWindow.setChecked(isAllowed);
//        // you cannot ask a user to grant you a permission you did not have!
//        mSwitchOverlayWindow.setEnabled(mOverlayState.permissionDeclared);
        
    	overlayPermEntry = MstPermission.getPermissionEntry(getContext(), mPackageName, MstPermission.SYSTEM_ALERT_WINDOW_GROUP);
    	mSwitchOverlayWindow.setEnabled(overlayPermEntry != null);
    	if(overlayPermEntry != null) {
    		mSwitchOverlayWindow.setChecked(overlayPermEntry.getStatus() == 1);
    	}
    }
    
    private void setCanDrawOverlay(boolean newState) {
//        mAppOpsManager.setMode(AppOpsManager.OP_SYSTEM_ALERT_WINDOW,
//                mPackageInfo.applicationInfo.uid, mPackageName, newState
//                ? AppOpsManager.MODE_ALLOWED : AppOpsManager.MODE_ERRORED);
    	
    	if(overlayPermEntry != null) {
    		overlayPermEntry.setStatus(newState ? 1 : -1);
    		updateMode(overlayPermEntry);
    	}
    }
    
    private void updateDefaultAppOption() {
    	mBtnDefaultApp.setEnabled(false);
    	if (mAppEntry != null && mAppEntry.info != null) {
    		if ((mAppEntry.info.flags&ApplicationInfo.FLAG_INSTALLED) == 0 ||
    				!mAppEntry.info.enabled) {
    		} else if(Utils.hasPreferredActivities(getPackageManager(), mAppEntry.info.packageName)){
    			mPreferenceDefaultApp.setEnabled(true);
    			mBtnDefaultApp.setEnabled(true);
    		}
    	}
    }
    
    private void confirmToClearPreferredActivity() {
    	new mst.app.dialog.AlertDialog.Builder(getActivity())
        	.setMessage(getActivity().getText(R.string.app_defail_title_clear_preferred_activity))
        	.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int which) {
        			getPackageManager().clearPackagePreferredActivities(mAppEntry.info.packageName);
        			updateDefaultAppOption();
        		}
        	})
        	.setNegativeButton(R.string.dlg_cancel, null)
        	.show();
    }
    	
//	private void getAppPermission() {
//		if(mAppPermissions != null) {
//			return;
//		}
//        PackageInfo packageInfo = AppPermissionsFragmentSelect.getPackageInfo(getActivity(), mAppEntry.info.packageName);
//		mAppPermissions = new AppPermissions(getActivity(), packageInfo, null, true, new Runnable() {
//            @Override
//            public void run() {
//                getActivity().finish();
//            }
//        });
//		//mShortcutPermissionGroup = mAppPermissions.getPermissionGroup(Manifest.permission_group.INSTALL_SHORTCUT);
//	}
//	
//	private void updatePermission() {
//		if(mAppPermissions == null) {
//			getAppPermission();
//		} else {
//			mAppPermissions.refresh();
//		}
//	}
//	
//	private class UpdateUiAsync extends AsyncTask<Void, Void, Void> {
//        @Override
//        protected Void doInBackground(Void... params) {
//        	updatePermission();
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void result) {
//            if (getActivity() == null) {
//                return;
//            }
//            
//        	updateAuth();
//        	updateShortcutSwitch();
//        }
//    }

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mSwitchOverlayWindow) {
//			if (mOverlayState != null && (Boolean) newValue != mOverlayState.isPermissible()) {
//                setCanDrawOverlay(!mOverlayState.isPermissible());
	        	final boolean enabled = (Boolean) newValue;
                setCanDrawOverlay(enabled);
                updateOverlayWindowPermission();
//            }
            return true;
        } else if(preference == mSwitchAutostart) {
        	final boolean enabled = (Boolean) newValue;
        	setAutostartChecked(enabled);
        	return true;
        } else if(preference == mSwitchNotifcation) {
        	final boolean banned = (Boolean) newValue;
        	if (banned) {
        		MetricsLogger.action(getActivity(), MetricsLogger.ACTION_BAN_APP_NOTES, mPackageName);
        	}
        	final boolean success =  mBackend.setNotificationsBanned(mAppEntry.info.packageName, mAppEntry.info.uid, !banned);
        	return success;
        } else if(preference == mSwitchDataConnect) {
        	final boolean banned = (Boolean) newValue;
        	setDataConnectDisableAsync(!banned);
        	return true;
        }
		return false;
	}
	
	@Override
	public void onPackageSizeChanged(String packageName) {
        if (packageName.equals(mAppEntry.info.packageName)) {
            refreshSizeInfo();
        }
	}

}

