/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.activity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.transfer.R;
import cn.tcl.transfer.systemApp.MeetingAssistantSysApp;
import cn.tcl.transfer.systemApp.NoteSysApp;
import cn.tcl.transfer.systemApp.SoundrecorderSysApp;
import cn.tcl.transfer.util.DataManager;
import cn.tcl.transfer.util.LogUtils;
import cn.tcl.transfer.util.PackageDetailInfo;
import mst.widget.toolbar.Toolbar;

public class SelectPackageActivity extends BaseActivity {

    private static final String TAG = "SelectPackageActivity";

    private static final String CHOOSE_SYSTEM = "system";
    private static final String SELECT_LIST = "select_package_list";

    private int mShowMode;
    private List<AppInfo> mSysApps = new ArrayList<>();
    private List<AppInfo> mUserApps = new ArrayList<>();
    private ListView mList;

    private ArrayList<String> mSelectedPackages;

    private PackageAdapter mAdapter;

    private static final int MODE_NULL = 0;
    private static final int MODE_CACULATE_SIZE = 1;
    private static final int MODE_SELECT_PACKAGE = 2;
    private static final int MODE_SEND = 3;

    private boolean isSelectSystemApp = false;
    private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.package_list);
        isSelectSystemApp = getIntent().getBooleanExtra(CHOOSE_SYSTEM, false);
        mSelectedPackages = getIntent().getStringArrayListExtra(SELECT_LIST);
        mToolbar = getToolbar();
        mToolbar.inflateMenu(R.menu.actionbar);
        getAllApplications();

        mList = (ListView)findViewById(R.id.list);
        mAdapter = new PackageAdapter();

        if(mSelectedPackages == null) {
            mSelectedPackages = new ArrayList<String>();
        }
        if(isSelectSystemApp) {
            mAdapter.mData = mSysApps;
        } else {
            mAdapter.mData = mUserApps;
        }
        mList.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        mShowMode = MODE_CACULATE_SIZE;

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                mAdapter.getItem(i).isChecked = !mAdapter.getItem(i).isChecked;
                mAdapter.notifyDataSetChanged();
                updateOptionMenu();
            }
        });

        findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                if(mSelectedPackages == null) {
                    mSelectedPackages = new ArrayList<String>();
                }
                mSelectedPackages.clear();
                for(AppInfo info :mAdapter.mData) {
                    if(info.isChecked) {
                        mSelectedPackages.add(info.packageInfo.packageName);
                    }
                }
                intent.putStringArrayListExtra(SELECT_LIST, mSelectedPackages);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        updateOptionMenu();
    }


    private String getAppName(PackageInfo info) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = getApplicationContext().getPackageManager();
            String applicationName =
                    (String) packageManager.getApplicationLabel(info.applicationInfo);
            if(mSelectedPackages.contains(applicationName)) {
                mSelectedPackages.add(applicationName);
            } else {
                mSelectedPackages.remove(applicationName);
            }
            return applicationName;
        } catch (Exception e) {
            applicationInfo = null;
            Log.e(TAG, "getAppName:", e);
            return  "";
        }
    }

    private Drawable getAppIcon(PackageInfo info) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = getApplicationContext().getPackageManager();
            Drawable applicationIcon;
            if (TextUtils.equals(info.packageName,"com.android.settings")) {
                applicationIcon = getDrawable(R.drawable.tcl_settings);
            } else {
                applicationIcon = info.applicationInfo.loadIcon(packageManager);
            }
            return applicationIcon;
        } catch (Exception e) {
            applicationInfo = null;
            Log.e(TAG, "getAppName:", e);
            return  null;
        }
    }

    private long getPackageDataSize(String packageName) {
        return 0;
    }

    private long loadingAllDataSize() {
        return 0;
    }

    private  void getAllApplications() {
        mSysApps.clear();
        mUserApps.clear();
        PackageManager pckMan = getPackageManager();
        List<PackageInfo> packs = pckMan.getInstalledPackages(0);
        int count = packs.size();
        for (int i = 0; i < count; i++) {
            PackageInfo p = packs.get(i);
            if(TextUtils.equals(p.packageName, getApplicationContext().getPackageName())) {
                continue;
            }
            ApplicationInfo appInfo = p.applicationInfo;
            if(TextUtils.equals(p.packageName, MeetingAssistantSysApp.NAME)
                    || TextUtils.equals(p.packageName, NoteSysApp.NAME)
                    || TextUtils.equals(p.packageName, SoundrecorderSysApp.NAME)
                    || TextUtils.equals(p.packageName, "cn.tcl.weather")
                    ) {
                mSysApps.add(new AppInfo(p, isChecked(appInfo.packageName)));
                continue;
            }

            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0 && (appInfo.flags & ApplicationInfo.FLAG_INSTALLED) > 0) {
                if(DataManager.BACKUP_SYS_APP.contains(p.packageName)) {
                    mSysApps.add(new AppInfo(p, isChecked(appInfo.packageName)));
                }
            } else if((appInfo.flags & ApplicationInfo.FLAG_INSTALLED) > 0) {
                mUserApps.add(new AppInfo(p, isChecked(appInfo.packageName)));
            }
        }
    }

    private class AppInfo {
        public PackageInfo packageInfo;
        public boolean isChecked = true;

        public AppInfo(PackageInfo info, boolean checked) {
            packageInfo = info;
            isChecked = checked;
        }
    }

    private boolean isChecked(String packageName) {
        if(mSelectedPackages == null || mSelectedPackages.size() == 0) {
            return false;
        }
        for(String name: mSelectedPackages) {
            if(TextUtils.equals(packageName, name)) {
                return true;
            }
        }
        return false;
    }

    private class PackageAdapter extends BaseAdapter {

        public  List<AppInfo> mData = new ArrayList<AppInfo>();

        public PackageAdapter() {
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            if(view == null) {
                view = getLayoutInflater().inflate(R.layout.package_item, null);
                TextView title = (TextView)view.findViewById(R.id.title);
                ImageView icon = (ImageView)view.findViewById(R.id.package_icon);
                ImageView selecteIcon = (ImageView)view.findViewById(R.id.selected);

                ViewHolder holder = new ViewHolder();
                holder.title = title;
                holder.icon = icon;
                holder.selecteIcon = selecteIcon;

                title.setText(getAppName(getItem(i).packageInfo));
                icon.setImageDrawable(getAppIcon(getItem(i).packageInfo));

                if(getItem(i).isChecked) {
                    selecteIcon.setImageResource(R.drawable.checked);
                } else {
                    selecteIcon.setImageResource(R.drawable.unchecked);
                }
                view.setTag(holder);
            } else {
                ViewHolder holder = (ViewHolder)view.getTag();
                holder.title.setText(getAppName(getItem(i).packageInfo));
                holder.icon.setImageDrawable(getAppIcon(getItem(i).packageInfo));
                if(getItem(i).isChecked) {
                    holder.selecteIcon.setImageResource(R.drawable.checked);
                } else {
                    holder.selecteIcon.setImageResource(R.drawable.unchecked);
                }
            }
            return view;
        }

        @Override
        public AppInfo getItem(int i) {
            return mData.get(i);
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }
    }


    private class ViewHolder {
        public TextView title;
        public ImageView icon;
        public ImageView selecteIcon;
    }

    private boolean isSelectAll() {
        boolean selectAll = true;
        for(AppInfo info :mAdapter.mData) {
            if(!info.isChecked) {
                selectAll = false;
                return selectAll;
            }
        }
        return selectAll;
    }


    private void selectAll() {
        for(AppInfo info :mAdapter.mData) {
            if(!info.isChecked) {
                info.isChecked = true;
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private void unSelect() {
        for(AppInfo info :mAdapter.mData) {
            if(info.isChecked) {
                info.isChecked = false;
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.select_all:
                if(!isSelectAll()) {
                    selectAll();
                } else {
                    unSelect();
                }
                updateOptionMenu();
                return true;
            default:
                break;
        }
        return super.onMenuItemClick(item);
    }

    @Override
    public void updateOptionMenu() {
        if(!isSelectAll()) {
            getOptionMenu().findItem(R.id.select_all).setTitle(R.string.select_all);
        } else {
            getOptionMenu().findItem(R.id.select_all).setTitle(R.string.select_none);
        }
        super.updateOptionMenu();
    }
}
