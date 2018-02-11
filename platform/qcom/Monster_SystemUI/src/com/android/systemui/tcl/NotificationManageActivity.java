package com.android.systemui.tcl;

import android.app.AppGlobals;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.systemui.R;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import mst.app.MstActivity;
import mst.widget.FoldProgressBar;
import mst.widget.MstIndexBar;


public class NotificationManageActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private Context mContext;
    private MstIndexBar mSideBar;
    private ListView mListView;
    private RelativeLayout mForward;
    private TextView mReminder;
    private View mLoadingContainer;
    private FoldProgressBar mProgressBar;

    private View mListContainer;
    public ApplicationsAdapter mApplicationsAdapter;
    private static final Pattern REMOVE_DIACRITICALS_PATTERN
            = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private List<AppEntry> mAppEntrys;
    private PinyinComparator mPinyinComparator;
    private LoadAppEntryTask mLoadAppEntryTask;
    private PackageIntentReceiver mPackageIntentReceiver;
    private NotificationBackend mNotificationBackend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setMstContentView(R.layout.manage_applications_apps);
        getToolbar().setNavigationIcon(com.mst.R.drawable.ic_toolbar_back);
        // 设置标题
        this.setTitle(R.string.notify_manage_title);
        mContext = this;
        mAppEntrys = new ArrayList<>();
        mPinyinComparator = new PinyinComparator();
        mPackageIntentReceiver = new PackageIntentReceiver();
        mNotificationBackend = new NotificationBackend();
        mSideBar = (MstIndexBar) findViewById(R.id.sidrbar);
        mSideBar.deleteLetter(0);
        mSideBar.setOnSelectListener(new MstIndexBar.OnSelectListener() {
            @Override
            public void onSelect(int i, int layer, MstIndexBar.Letter letter) {
                int position = mApplicationsAdapter.getPositionForSection(letter.text.charAt(0));
                if (position != -1) {
                    mListView.setSelection(position);
                }
                mListView.setSelection(position);
            }
        });
        Intent intent = this.getIntent();
        if (true == intent.getBooleanExtra("shownotify", true)) {
            // 设置清理提醒block
            mForward = (RelativeLayout) findViewById(R.id.title);
            mForward.setOnClickListener(this);
            mReminder = (TextView) findViewById(R.id.reminder);
            mReminder.setText(Html.fromHtml(this.getString(R.string.notify_clean_num_format,
                    SpamNotifyHandle.getInstance(this).getNotifyNum())));
        } else {
            this.findViewById(R.id.title).setVisibility(View.GONE);
        }
        mLoadingContainer = findViewById(R.id.loading_container);
        mProgressBar = (FoldProgressBar) findViewById(R.id.progressBar);
        mListContainer = findViewById(R.id.list_container);
        if (mListContainer != null) {
            // Create adapter and list view here
            View emptyView = mListContainer.findViewById(com.android.internal.R.id.empty);
            mListView = (ListView) mListContainer.findViewById(android.R.id.list);
            if (emptyView != null) {
                mListView.setEmptyView(emptyView);
            }
            mListView.setOnItemClickListener(this);
            mListView.setSaveEnabled(true);
            mListView.setItemsCanFocus(true);
            mListView.setTextFilterEnabled(true);
            mApplicationsAdapter = new ApplicationsAdapter();
            mListView.setAdapter(mApplicationsAdapter);
            mListView.setVisibility(View.GONE);
            mListView.getOverlay().clear();
            mListView.setVerticalScrollBarEnabled(false);
            mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (mAppEntrys != null && firstVisibleItem < mAppEntrys.size()) {
                        AppEntry entry = mAppEntrys.get(firstVisibleItem);
                        int index = mSideBar.getIndex(entry.getNormalizedLabel());
                        mSideBar.setFocus(index);
                    }
                }
            });
        }
        mLoadAppEntryTask = new LoadAppEntryTask();
        mLoadAppEntryTask.execute();
        mPackageIntentReceiver.registerReceiver();
    }

    @Override
    public void onClick(View paramView) {
        // TODO Auto-generated method stub
        if (paramView.equals(mForward)) {
            Intent intent = new Intent(this, NotifyInfoActivity.class);
            intent.putExtra("hideManager", true);
            startActivity(intent);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mApplicationsAdapter != null && mApplicationsAdapter.getCount() > position) {
            AppEntry entry = mAppEntrys.get(position);
            Intent intent = new Intent("android.intent.action.SHOW_APP_NOTIFY_SETTING_ACTIVITY");
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, entry.info.packageName);
            intent.putExtra(Settings.EXTRA_APP_UID, entry.info.uid);
            this.startActivityForResult(intent, 0);
        }
    }

    @Override
    public void onNavigationClicked(View view) {
        //在这里处理Toolbar上的返回按钮的点击事件
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPackageIntentReceiver.unregisterReceiver();
    }

    private class LoadAppEntryTask extends AsyncTask {
        final int mAdminRetrieveFlags = PackageManager.GET_UNINSTALLED_PACKAGES |
                PackageManager.GET_DISABLED_COMPONENTS |
                PackageManager.GET_DISABLED_UNTIL_USED_COMPONENTS;
        final int mRetrieveFlags = PackageManager.GET_DISABLED_COMPONENTS |
                PackageManager.GET_DISABLED_UNTIL_USED_COMPONENTS;


        private boolean isDone;
        private List<AppEntry> localEntrys;

        @Override
        protected void onPreExecute() {
            isDone = false;
            localEntrys = new ArrayList<>();
            Utils.setViewShown(mProgressBar, true, true);
            Utils.setViewShown(mLoadingContainer, true, true);
            Utils.setViewShown(mListContainer, false, true);
        }

        @Override
        protected Object doInBackground(Object[] params) {
            final SparseArray<List<ApplicationInfo>> mEntriesMap =
                    new SparseArray<>();
            List<ApplicationInfo> mApplications = new ArrayList<ApplicationInfo>();
            UserManager mUm = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
            final IPackageManager mIpm = AppGlobals.getPackageManager();
            //1获取所有应用
            for (UserInfo user : mUm.getProfiles(UserHandle.myUserId())) {
                try {
                    @SuppressWarnings("unchecked")
                    ParceledListSlice<ApplicationInfo> list =
                            mIpm.getInstalledApplications(
                                    user.isAdmin() ? mAdminRetrieveFlags : mRetrieveFlags,
                                    user.id);
                    mApplications.addAll(list.getList());
                    mEntriesMap.put(user.id, list.getList());
                } catch (RemoteException e) {
                }
            }
            //2过滤应用
            List<AppEntry> entries = new ArrayList<>();
            Iterator<ApplicationInfo> aIt = mApplications.iterator();
            while (aIt.hasNext()) {
                final ApplicationInfo info = aIt.next();
                // Need to trim out any applications that are disabled by
                // something different than the user.
                if (!info.enabled) {
                    if (info.enabledSetting != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {
                        aIt.remove();
                        continue;
                    }
                }
                final AppEntry entry = getEntryLocked(info);
                entries.add(entry);
            }
            Intent launchIntent = new Intent(Intent.ACTION_MAIN, null)
                    .addCategory(Intent.CATEGORY_LAUNCHER);
            //3继续过滤应用
            for (int i = 0; i < mEntriesMap.size(); i++) {
                int userId = mEntriesMap.keyAt(i);
                List<ResolveInfo> intents = mContext.getPackageManager().queryIntentActivitiesAsUser(
                        launchIntent,
                        PackageManager.GET_DISABLED_COMPONENTS
                                | PackageManager.MATCH_DIRECT_BOOT_AWARE
                                | PackageManager.MATCH_DIRECT_BOOT_UNAWARE,
                        userId
                );
                synchronized (mEntriesMap) {
                    final int N = intents.size();
                    for (int j = 0; j < N; j++) {
                        String packageName = intents.get(j).activityInfo.packageName;
                        for (AppEntry entry : entries) {
                            if (entry.info.packageName.equals(packageName)) {
                                entry.hasLauncherEntry = true;
                                break;
                            }
                        }
                    }
                }
            }
            Iterator<AppEntry> eIt = entries.iterator();
            while (eIt.hasNext()) {
                final AppEntry entry = eIt.next();
                if (entry != null && filterApp(entry)) {
                    localEntrys.add(entry);
                }
            }
            //4读取通知开关和应用icon
            Iterator<AppEntry> aeIt = localEntrys.iterator();
            while (aeIt.hasNext()) {
                AppEntry entry = aeIt.next();
                Drawable icon = Utils.getIconDrawable(mContext, entry.info.packageName);
                if (icon == null) {
                    //没有获取到图标，可能是加载期间被卸载了，剔除掉
                    aeIt.remove();
                } else {
                    entry.icon = icon;
                    entry.extraInfo = mNotificationBackend.getNotificationsBanned(entry.info.packageName, entry.info.uid);
                }
                buildIndex(entry);
            }
            // 排序
            Collections.sort(localEntrys, mPinyinComparator);
            return localEntrys;
        }

        @Override
        protected void onPostExecute(Object o) {
            isDone = true;
            Utils.setViewShown(mProgressBar, false, true);
            Utils.setViewShown(mLoadingContainer, false, true);
            Utils.setViewShown(mListContainer, true, true);
            mAppEntrys = (List<AppEntry>) o;
            mApplicationsAdapter.notifyDataSetChanged();
        }

        public boolean isDone() {
            return isDone;
        }

        private boolean filterApp(AppEntry entry) {
            if ((entry.info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                return true;
            } else if ((entry.info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                return true;
            } else if (entry.hasLauncherEntry) {
                return true;
            }
            return false;
        }

        private AppEntry getEntryLocked(ApplicationInfo info) {
            AppEntry entry = new AppEntry(mContext, info);
            return entry;
        }

        /**
         * 为每个应用建立字母索引
         */
        private void buildIndex(AppEntry app) {
            // 为每个应用写上字母标签
            String pinyin = HanziToPinyin.getInstance().transliterate(app.label);
            String sortString = pinyin.substring(0, 1).toUpperCase();

            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                app.normalizedLabel = sortString.toUpperCase();
            } else {
                app.normalizedLabel = "#";
            }
            mSideBar.setEnables(true, mSideBar.getIndex(app.normalizedLabel));
        }
    }

    private class ApplicationsAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mAppEntrys.size();
        }

        @Override
        public Object getItem(int position) {
            return mAppEntrys.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AppViewHolder holder = createOrRecycle(convertView);
            convertView = holder.rootView;

            // Bind the data efficiently with the holder
            AppEntry entry = mAppEntrys.get(position);
            synchronized (entry) {
                holder.entry = entry;
                if (entry.label != null) {
                    holder.appName.setText(entry.label);
                }

                if (entry.icon != null) {
                    holder.appIcon.setImageDrawable(entry.icon);
                }
                updateSummary(holder);
            }
            convertView.setEnabled(isEnabled(position));
            return convertView;
        }

        private void updateSummary(AppViewHolder holder) {
            if (holder.entry.extraInfo != null) {
                boolean banned = (boolean) holder.entry.extraInfo;
                if (banned) {
                    holder.summary.setText(mContext.getString(R.string.enableNotify));
                    holder.summary.setTextColor(mContext.getResources().getColor(R.color.notify_enable));
                } else {
                    holder.summary.setText(mContext.getString(R.string.disableNotify));
                    holder.summary.setTextColor(mContext.getResources().getColor(R.color.notify_disable));
                }
            } else {
                holder.summary.setText(null);
            }
        }


        public int getPositionForSection(int section) {
            // TODO Auto-generated method stub
            for (int i = 0; i < getCount(); i++) {
                String sortStr = mAppEntrys.get(i).getNormalizedLabel();
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }

            return -1;
        }

        public AppViewHolder createOrRecycle(View convertView) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(R.layout.preference_app, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                AppViewHolder holder = new AppViewHolder();
                holder.rootView = convertView;
                holder.appName = (TextView) convertView.findViewById(android.R.id.title);
                holder.appIcon = (ImageView) convertView.findViewById(android.R.id.icon);
                holder.summary = (TextView) convertView.findViewById(android.R.id.summary);
                convertView.setTag(holder);
                return holder;
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                return (AppViewHolder) convertView.getTag();
            }
        }

        public class AppViewHolder {
            public AppEntry entry;
            public View rootView;
            public TextView appName;
            public ImageView appIcon;
            public TextView summary;
        }
    }

    public static class AppEntry {
        public String label;
        public boolean hasLauncherEntry;

        public String getNormalizedLabel() {
            if (normalizedLabel != null) {
                return normalizedLabel;
            }
            normalizedLabel = normalize(label);
            return normalizedLabel;
        }

        // Need to synchronize on 'this' for the following.
        public ApplicationInfo info;
        public Drawable icon;

        public String normalizedLabel;

        // A location where extra info can be placed to be used by custom filters.
        public Object extraInfo;

        AppEntry(Context context, ApplicationInfo info) {
            this.info = info;
            ensureLabel(context);
        }

        public void ensureLabel(Context context) {
            CharSequence label = info.loadLabel(context.getPackageManager());
            this.label = label != null ? label.toString() : info.packageName;
        }


        public String normalize(String str) {
            String tmp = Normalizer.normalize(str, Normalizer.Form.NFD);
            return REMOVE_DIACRITICALS_PATTERN.matcher(tmp)
                    .replaceAll("").toLowerCase();
        }

    }

    public class PinyinComparator implements Comparator<AppEntry> {

        public int compare(AppEntry o1, AppEntry o2) {
            int index;
            if (o1.normalizedLabel.equals("#")) {
                index = 1;
            } else if (o2.normalizedLabel.equals("#")) {
                index = -1;
            } else {
                index = o1.normalizedLabel.compareTo(o2.normalizedLabel);
            }
            return index;
        }

    }

    /**
     * Receives notifications when applications are added/removed.
     */
    private class PackageIntentReceiver extends BroadcastReceiver {
        void registerReceiver() {
            IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
            filter.addDataScheme("package");
            mContext.registerReceiver(this, filter);
            // Register for events related to sdcard installation.
            IntentFilter sdFilter = new IntentFilter();
            sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
            sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
            mContext.registerReceiver(this, sdFilter);
            // Register for events related to user creation/deletion.
            IntentFilter userFilter = new IntentFilter();
            userFilter.addAction(Intent.ACTION_USER_ADDED);
            userFilter.addAction(Intent.ACTION_USER_REMOVED);
            mContext.registerReceiver(this, userFilter);
            IntentFilter inf = new IntentFilter();
            inf.addAction(Utils.ACTION_NOTIFY_BANNED_CHANGE);
            mContext.registerReceiver(this, inf);
        }

        void unregisterReceiver() {
            mContext.unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String actionStr = intent.getAction();
            if (Intent.ACTION_PACKAGE_ADDED.equals(actionStr)) {
                Uri data = intent.getData();
                String pkgName = data.getEncodedSchemeSpecificPart();
                Log.e("kebelzc24", "install pkg -- " + pkgName);
                if (mLoadAppEntryTask.isDone()) {
                    mLoadAppEntryTask = new LoadAppEntryTask();
                    mLoadAppEntryTask.execute();
                }
            } else if (Intent.ACTION_PACKAGE_REMOVED.equals(actionStr)) {
                Uri data = intent.getData();
                String pkgName = data.getEncodedSchemeSpecificPart();
                Log.e("kebelzc24", "delete pkg -- " + pkgName);
                if (mLoadAppEntryTask.isDone) {
                    Iterator<AppEntry> it = mAppEntrys.iterator();
                    while (it.hasNext()) {
                        AppEntry entry = it.next();
                        if (entry.info.packageName.equals(pkgName)) {
                            it.remove();
                            mApplicationsAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }
            } else if (Utils.ACTION_NOTIFY_BANNED_CHANGE.equals(actionStr)) {
                String pkg = intent.getStringExtra("package");
                boolean banned = intent.getBooleanExtra("banned", false);
                int uid = intent.getIntExtra("uid", -1);
                Iterator<AppEntry> it = mAppEntrys.iterator();
                while (it.hasNext()) {
                    AppEntry entry = it.next();
                    if (entry.info.packageName.equals(pkg) && (uid == entry.info.uid)) {
                        entry.extraInfo = banned;
                        mApplicationsAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            } else if (Intent.ACTION_PACKAGE_CHANGED.equals(actionStr)) {
            } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(actionStr) ||
                    Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(actionStr)) {
            } else if (Intent.ACTION_USER_ADDED.equals(actionStr)) {
            } else if (Intent.ACTION_USER_REMOVED.equals(actionStr)) {
            }
        }
    }
}
