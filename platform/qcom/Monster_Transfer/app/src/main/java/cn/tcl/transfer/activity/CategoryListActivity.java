/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.tct.backupmanager.IBackupManagerService;
import com.tct.backupmanager.IBackupManagerServiceCallback;

import cn.tcl.transfer.File_Exchange;
import cn.tcl.transfer.ICallback;
import cn.tcl.transfer.R;
import cn.tcl.transfer.send.ISendInfo;
import cn.tcl.transfer.send.SendBackupDataService;
import cn.tcl.transfer.systemApp.MeetingAssistantSysApp;
import cn.tcl.transfer.systemApp.NoteSysApp;
import cn.tcl.transfer.systemApp.SoundrecorderSysApp;
import cn.tcl.transfer.util.CalculateSizeTask;
import cn.tcl.transfer.util.DataManager;
import cn.tcl.transfer.util.DialogBuilder;
import cn.tcl.transfer.util.FilePathUtils;
import cn.tcl.transfer.util.LogUtils;
import cn.tcl.transfer.util.NotificationUtils;
import cn.tcl.transfer.util.PackageDetailInfo;
import cn.tcl.transfer.util.Utils;

public class CategoryListActivity extends BaseActivity {

    private static final String TAG = "CategoryListActivity";

    private static final String CHOOSE_SYSTEM = "system";
    private static final String SELECT_LIST = "select_package_list";

    private int mShowMode;
    private List<PackageInfo> mSysApps = new ArrayList<>();
    private List<PackageInfo> mUserApps = new ArrayList<>();

    HashSet<String> mSelectPackage = new HashSet<>();

    private ArrayList<String> mSelectSysApps = new ArrayList<>();
    private ArrayList<String> mSelectUserApps = new ArrayList<>();

    private TextView mSelectSizeView;
    private TextView mSizeUnitView;
    private TextView mLeftTimeView;

    private CategoryItem mSysItem;
    private CategoryItem mAppItem;
    private CategoryItem mImageItem;
    private CategoryItem mVideoItem;
    private CategoryItem mAudioItem;
    private CategoryItem mDocItem;


    private Object mLockObject = new Object();
    private List<CategoryItem> mShowList = new ArrayList<CategoryItem>();

    private static final int MODE_NULL = 0;
    private static final int MODE_CACULATE_SIZE = 1;
    private static final int MODE_SELECT_PACKAGE = 2;
    private static final int MODE_SEND = 3;

    public static final int REQUEST_SELECT_SYS_PACKAGE = 0;
    public static final int REQUEST_SELECT_APP_PACKAGE = 1;

    private static final long WAIT_TIME=15*60*1000L;

    private IBackupManagerService mBackup = null;

    CalculateSizeTask.CalculateSizeCallback mCalculateSizeCallback;
    private boolean isCaculateSize = true;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setMstContentView(R.layout.category_list);
            Toast.makeText(this, R.string.notify_connect_success, Toast.LENGTH_SHORT).show();
            getAllApplications();
            mSelectSizeView = (TextView)findViewById(R.id.first_text);
            mSizeUnitView = (TextView)findViewById(R.id.size_unit);
            mLeftTimeView = (TextView)findViewById(R.id.secondary_text);

            initView();

            mShowMode = MODE_CACULATE_SIZE;
            findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendSelect();
                }
            });
            enableView(false);
            findViewById(R.id.send).setEnabled(false);

            mCalculateSizeCallback = new CalculateSizeTask.CalculateSizeCallback() {
                @Override
                public void onUpdate(final String pkgName, long size, long externalSize, long apkSize) {
                    LogUtils.d(TAG, Formatter.formatFileSize(getApplicationContext(), size) + ":" + pkgName);
                    if(DataManager.mSizeInfo.containsKey(pkgName)) {
                        DataManager.mSizeInfo.get(pkgName).sysDataSize = size;
                        DataManager.mSizeInfo.get(pkgName).externalDataSize = externalSize;
                        DataManager.mSizeInfo.get(pkgName).apkSize = apkSize;
                    }
                    synchronized (mLockObject) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mSelectPackage.remove(pkgName);
                                String sizeString = Formatter.formatFileSize(getApplicationContext(), getSelectSize());
                                String[] tmp = sizeString.split(" ");
                                mSelectSizeView.setText(tmp[0]);
                                mSizeUnitView.setText(tmp[1]);
                                if(mSelectPackage.size() == 0) {
                                    isCaculateSize = false;
                                    updateSizeView(getSelectSize());
                                    enableView(true);
                                    refreshView();
                                    findViewById(R.id.send).setEnabled(true);
                                }
                            }
                        });
                    }
                }
            };
            new CalculateSizeTask(this, DataManager.mSizeInfo, mCalculateSizeCallback).execute();
            mDisconnectHandler.postDelayed(mDisconnectScanRunnable, WAIT_TIME);

            Intent intent = new Intent(this, SendBackupDataService.class);
            bindService(intent, mConn, Context.BIND_AUTO_CREATE);
    }

    private ISendInfo mRemoteService;
    ServiceConnection mConn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mRemoteService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            try {
                mRemoteService = ISendInfo.Stub.asInterface(service);
                mRemoteService.registerCallback(mCallBack);
                LogUtils.i(TAG, "onServiceConnected");
            } catch (Exception e) {
                Log.e(TAG, "onServiceConnected:", e);
            }
        }
    };

    private ICallback.Stub  mCallBack = new ICallback.Stub() {
        @Override
        public void onStart(int type) {
        }

        @Override
        public void onProgress(int type, long size, long speed) {
        }

        @Override
        public void onFileBeginSend(int type, String fileName) {
        }

        @Override
        public void onComplete(int type) {
        }

        @Override
        public void onError(int type, String reason) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Intent intent1 = new Intent();
                    intent1.setClass(getApplicationContext(), DisconnectActivity.class);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent1);
                    finish();
                }
            });
        }

        @Override
        public void onAllComplete() throws RemoteException {
        }

        @Override
        public void onCancel() throws RemoteException {
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConn);
        mDisconnectHandler.removeCallbacks(mDisconnectScanRunnable);
    }

    @Override
    public void finish() {
        super.finish();
        try {
            mRemoteService.unregisterCallback(mCallBack);
        } catch (Exception e) {
            LogUtils.e(TAG, "mHandler", e);
        }
    }

    private void initView() {
        final String[] categories = getResources().getStringArray(R.array.receiver_content_item);

        mSysItem = new CategoryItem(Utils.CATEGORY_SYS, categories[Utils.CATEGORY_SYS]);
        mAppItem = new CategoryItem(Utils.CATEGORY_APP, categories[Utils.CATEGORY_APP]);
        mImageItem = new CategoryItem(Utils.CATEGORY_IMAGE, categories[Utils.CATEGORY_IMAGE]);
        mVideoItem = new CategoryItem(Utils.CATEGORY_VIDEO, categories[Utils.CATEGORY_VIDEO]);
        mAudioItem = new CategoryItem(Utils.CATEGORY_AUDIO, categories[Utils.CATEGORY_AUDIO]);
        mDocItem = new CategoryItem(Utils.CATEGORY_DOCUMENT, categories[Utils.CATEGORY_DOCUMENT]);

        mShowList.add(mSysItem);
        mShowList.add(mAppItem);
        mShowList.add(mImageItem);
        mShowList.add(mVideoItem);
        mShowList.add(mAudioItem);
        mShowList.add(mDocItem);

        mSysItem.initView(findViewById(R.id.sys_view));
        mAppItem.initView(findViewById(R.id.app_view));
        mImageItem.initView(findViewById(R.id.image_view));
        mVideoItem.initView(findViewById(R.id.video_view));
        mAudioItem.initView(findViewById(R.id.audio_view));
        mDocItem.initView(findViewById(R.id.doc_view));
    }


    private void refreshView() {

        for(CategoryItem categoryItem: mShowList) {
            categoryItem.updateView();
        }
    }


    private void enableView(boolean isEnable) {
        for(CategoryItem categoryItem: mShowList) {
            categoryItem.enableView(isEnable);
        }
        if(!isEnable) {
            ((Button)findViewById(R.id.send)).setAlpha(0.3f);
            ((LockableScrollView)findViewById(R.id.scroll)).setScrollingEnabled(false);
        } else {
            ((Button)findViewById(R.id.send)).setAlpha(1.0f);
            ((LockableScrollView)findViewById(R.id.scroll)).setScrollingEnabled(true);
        }
        findViewById(R.id.send).setEnabled(isEnable);
    }

    private long getSelectSize() {
        long size = 0;
        for(int i = 0; i < 6;i++) {
            if(mShowList.get(i).isChecked) {
                size += mShowList.get(i).getSize();
            }
        }
        return size;
    }

    private IBackupManagerServiceCallback mBackupCallback = new IBackupManagerServiceCallback.Stub() {

        @Override
        public void onStart() throws RemoteException {
            LogUtils.d(TAG, "onStart");
        }

        @Override
        public void onComplete() throws RemoteException {
            LogUtils.d(TAG, "onComplete");
            if (mBackup != null) {
                mBackup.unregisterCallback(mBackupCallback);
            }
        }

        @Override
        public void onUpdate(String test) throws RemoteException {
              LogUtils.d(TAG, "onUpdate: " + test);
        }

        @Override
        public void onProgress(int progress) throws RemoteException {
            LogUtils.d(TAG, "onProgress: " + progress);
        }

        @Override
        public void onError(String error) throws RemoteException {
            LogUtils.d(TAG, "error: " + error);
        }
    };


    Handler mDisconnectHandler=new Handler();
    private Runnable mDisconnectScanRunnable = new Runnable() {
        @Override
        public void run() {
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            if (wifiManager.isWifiEnabled()) {
                wifiManager.disconnect();
            }
            Intent intent = new Intent();
            intent.setClass(CategoryListActivity.this,DisconnectActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            CategoryListActivity.this.startActivity(intent);
            CategoryListActivity.this.finish();
        }
    };

    private void sendSelect() {
        mDisconnectHandler.removeCallbacks(mDisconnectScanRunnable);
        Intent intent1 = new Intent(CategoryListActivity.this,
                SendingActivity.class);

        int selectItem = 0;
        for(int i = 0; i < 6; i++)
        if(mShowList.get(i).isChecked) {
            selectItem = selectItem | (1<<i);
        }
        DataManager.selectItem = selectItem;
        DataManager.mSelectSysApps = mSelectSysApps;
        DataManager.mSelectUserApps = mSelectUserApps;
        startActivity(intent1);

        Intent intent = new Intent(CategoryListActivity.this, SendBackupDataService.class);
        intent.putExtra("needbackup", true);
        startService(intent);

        CategoryListActivity.this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_SELECT_SYS_PACKAGE && resultCode == RESULT_OK) {
            ArrayList<String> packages = data.getStringArrayListExtra(SELECT_LIST);
            mSelectSysApps.clear();
            mSelectSysApps.addAll(packages);
            if(packages == null || packages.size() == 0) {
                mShowList.get(Utils.CATEGORY_SYS).isChecked = false;
            } else {
                mShowList.get(Utils.CATEGORY_SYS).isChecked = true;
            }
            refreshView();
            if(packages != null) {
                for(String pkg: packages) {
                    LogUtils.d(TAG, "select: " + pkg);
                }
            }
            updateSizeView(getSelectSize());
        }
        else if(requestCode == REQUEST_SELECT_APP_PACKAGE && resultCode == RESULT_OK) {
            ArrayList<String> packages = data.getStringArrayListExtra(SELECT_LIST);
            mSelectUserApps.clear();
            mSelectUserApps.addAll(packages);
            if(packages == null || packages.size() == 0) {
                mShowList.get(Utils.CATEGORY_APP).isChecked = false;
            } else {
                mShowList.get(Utils.CATEGORY_APP).isChecked = true;
            }
            refreshView();
            if(packages != null) {
                for(String pkg: packages) {
                    LogUtils.d(TAG, "select: " + pkg);
                }
            }
            updateSizeView(getSelectSize());
        }
    }

    private void updateSizeView(long size) {

        long totalSize = size;
        String sizeString = Formatter.formatFileSize(getApplicationContext(), totalSize);
        String[] tmp = sizeString.split(" ");
        mSelectSizeView.setText(tmp[0]);
        mSizeUnitView.setText(tmp[1]);
        long compressTime = totalSize/(1024 * 1024 * 30);
        long sendTime = totalSize/(1024 * 1024 * 3);

        long time = compressTime + sendTime;
        int minutes = 0;
        if(time < 60 && totalSize > 0) {
            minutes = 1;
        } else {
            minutes = (int)time/60;
        }
        mLeftTimeView.setText(getString(R.string.remaining_time,  minutes + getString(R.string.text_min)));
        if(size == 0) {
            ((Button)findViewById(R.id.send)).setAlpha(0.3f);
            findViewById(R.id.send).setEnabled(false);
        } else {
            ((Button)findViewById(R.id.send)).setAlpha(1.0f);
            findViewById(R.id.send).setEnabled(true);
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
        mSelectSysApps.clear();
        mSelectUserApps.clear();

        PackageManager pckMan = getPackageManager();
        List<PackageInfo> packs = pckMan.getInstalledPackages(0);
        int count = packs.size();
        for (int i = 0; i < count; i++) {
            PackageInfo p = packs.get(i);
            ApplicationInfo appInfo = p.applicationInfo;
            if(TextUtils.equals(p.packageName, getApplicationContext().getPackageName())) {
                continue;
            }
            if(TextUtils.equals(p.packageName, MeetingAssistantSysApp.NAME)
                    || TextUtils.equals(p.packageName, NoteSysApp.NAME)
                    || TextUtils.equals(p.packageName, SoundrecorderSysApp.NAME)
                    || TextUtils.equals(p.packageName, "cn.tcl.weather")
                    ) {
                mSysApps.add(p);
                mSelectSysApps.add(p.packageName);
                DataManager.mSizeInfo.put(p.packageName, new PackageDetailInfo(p));
                mSelectPackage.add(p.packageName);
                LogUtils.d(TAG, ": [" + p.packageName + "]" );
                continue;
            }

            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
                if(DataManager.BACKUP_SYS_APP.contains(p.packageName)) {
                    mSysApps.add(p);
                    mSelectSysApps.add(p.packageName);
                    DataManager.mSizeInfo.put(p.packageName, new PackageDetailInfo(p));
                    mSelectPackage.add(p.packageName);
                    LogUtils.d(TAG, ": [" + p.packageName + "]" );
                }
            } else if((appInfo.flags & ApplicationInfo.FLAG_INSTALLED) > 0) {
                mUserApps.add(p);
                mSelectUserApps.add(p.packageName);
                DataManager.mSizeInfo.put(p.packageName, new PackageDetailInfo(p));
                mSelectPackage.add(p.packageName);
            }
        }
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };


    private class ViewHolder {
        public TextView title;
        public TextView summary;
        public ImageView selecteIcon;
        public View expandIcon;
        public View expandArea;
        public View selectArea;
    }

    private class CategoryItem {
        private int type = 0;

        public int size;
        public String title;
        public int status;
        public int icon;
        public int count;
        public ViewHolder viewHolder;
        public View mItemview = null;

        boolean isChecked = true;

        public CategoryItem(String title1, int ic) {
            title = title1;
            icon = ic;
        }

        public CategoryItem(int categoryType, String title1) {
            type = categoryType;
            title = title1;
        }

        public void initView(View view) {
            mItemview = view;
            TextView title = (TextView)view.findViewById(R.id.title);
            TextView summary = (TextView)view.findViewById(R.id.summary);
            View expandArea = view.findViewById(R.id.expand_area);
            ImageView selecteIcon = (ImageView)view.findViewById(R.id.selected);
            View expandIcon = view.findViewById(R.id.expand);
            View selectArea = view.findViewById(R.id.select_area);

            viewHolder = new ViewHolder();
            viewHolder.title = title;
            viewHolder.summary = summary;
            viewHolder.expandArea = expandArea;
            viewHolder.selecteIcon = selecteIcon;
            viewHolder.expandIcon = expandIcon;
            viewHolder.selectArea = selectArea;
            updateView();
        }

        public void enableView(boolean isEnable) {
            if(mItemview != null) {
                mItemview.setEnabled(isEnable);
                if(!isEnable) {
                    viewHolder.title.setAlpha(0.3f);
                    viewHolder.summary.setAlpha(0.3f);

                    viewHolder.selecteIcon.setImageResource(R.drawable.checked_disable);
                } else {
                    viewHolder.title.setAlpha(1.0f);
                    viewHolder.summary.setAlpha(1.0f);

                    viewHolder.selecteIcon.setImageResource(R.drawable.checked);
                    viewHolder.selectArea.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            isChecked = !isChecked;
                            if(type == 0) {
                                if(!isChecked) {
                                    mSelectSysApps.clear();
                                } else {
                                    mSelectSysApps.clear();
                                    for(PackageInfo packageInfo:mSysApps) {
                                        mSelectSysApps.add(packageInfo.packageName);
                                    }
                                }
                            } else if(type == 1) {
                                if(!isChecked) {
                                    mSelectUserApps.clear();
                                } else {
                                    mSelectUserApps.clear();
                                    for(PackageInfo packageInfo:mUserApps) {
                                        mSelectUserApps.add(packageInfo.packageName);
                                    }
                                }
                            }
                            updateSizeView(getSelectSize());
                            refreshView();
                        }
                    });
                }
            }
        }

        public void updateView() {
            viewHolder.title.setText(title);
            viewHolder.summary.setText(getSummary());
            if(isChecked) {
                viewHolder.selecteIcon.setImageResource(R.drawable.checked);
            } else {
                viewHolder.selecteIcon.setImageResource(R.drawable.unchecked);
            }
            if(type > Utils.CATEGORY_APP) {
                viewHolder.expandIcon.setVisibility(View.GONE);
            } else {
                viewHolder.expandArea.setVisibility(View.VISIBLE);
                if(isCaculateSize) {
                    return;
                }
                viewHolder.expandArea.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(CategoryListActivity.this, SelectPackageActivity.class);
                        boolean isSys = (type == 0);
                        intent.putExtra(CHOOSE_SYSTEM, isSys);
                        if(isSys) {
                            intent.putStringArrayListExtra(SELECT_LIST, mSelectSysApps);
                            startActivityForResult(intent, CategoryListActivity.REQUEST_SELECT_SYS_PACKAGE);
                        } else {
                            intent.putStringArrayListExtra(SELECT_LIST, mSelectUserApps);
                            startActivityForResult(intent, CategoryListActivity.REQUEST_SELECT_APP_PACKAGE);
                        }
                    }
                });
            }
        }

        public String getSummary() {
            if(isCaculateSize) {
                return getString(R.string.caculate_size_info);
            }
            String tmp = getString(R.string.item_info);
            switch (type) {
                case Utils.CATEGORY_SYS:
                    return String.format(tmp, Utils.convertFileSize(getSize()), mSelectSysApps.size());
                case Utils.CATEGORY_APP:
                    return String.format(tmp, Utils.convertFileSize(getSize()), mSelectUserApps.size());
                case Utils.CATEGORY_IMAGE:
                    return String.format(tmp, Utils.convertFileSize(getSize()),
                            FilePathUtils.getFileCount(File_Exchange.TYPE_IMAGE, getApplicationContext()));
                case Utils.CATEGORY_VIDEO:
                    return String.format(tmp, Utils.convertFileSize(getSize()),
                            FilePathUtils.getFileCount(File_Exchange.TYPE_VIDEO, getApplicationContext()));
                case Utils.CATEGORY_AUDIO:
                    return String.format(tmp, Utils.convertFileSize(getSize()),
                            FilePathUtils.getFileCount(File_Exchange.TYPE_AUDIO, getApplicationContext()));
                case Utils.CATEGORY_DOCUMENT:
                    return String.format(tmp, Utils.convertFileSize(getSize()),
                            FilePathUtils.getFileCount(File_Exchange.TYPE_DOCUMENT, getApplicationContext()));
                default:
                    break;

            }
            return "";
        }

        public long getSize() {
            switch (type) {
                case Utils.CATEGORY_SYS:
                    long size = 0;
                    for (String packageName: mSelectSysApps) {
                        if(DataManager.mSizeInfo.containsKey(packageName)) {
                            size += DataManager.mSizeInfo.get(packageName).sysDataSize + DataManager.mSizeInfo.get(packageName).externalDataSize;
                        } else {
                            Log.e(TAG, packageName + " is not exist!");
                        }
                    }
                    return size;
                case Utils.CATEGORY_APP:
                    long size1 = 0;
                    for (String packageName: mSelectUserApps) {
                        if(DataManager.mSizeInfo.containsKey(packageName)) {
                            size1 += DataManager.mSizeInfo.get(packageName).sysDataSize
                                    + DataManager.mSizeInfo.get(packageName).externalDataSize
                                    + DataManager.mSizeInfo.get(packageName).apkSize;
                        } else {
                            Log.e(TAG, packageName + " is not exist!");
                        }
                    }
                    return size1;
                case Utils.CATEGORY_IMAGE:
                    return FilePathUtils.getFileSize(File_Exchange.TYPE_IMAGE, getApplicationContext());
                case Utils.CATEGORY_VIDEO:
                    return FilePathUtils.getFileSize(File_Exchange.TYPE_VIDEO, getApplicationContext());
                case Utils.CATEGORY_AUDIO:
                    return FilePathUtils.getFileSize(File_Exchange.TYPE_AUDIO, getApplicationContext());
                case Utils.CATEGORY_DOCUMENT:
                    return FilePathUtils.getFileSize(File_Exchange.TYPE_DOCUMENT, getApplicationContext());
                default:
                    break;
            }
            return 0;
        }
    }

    @Override
    public void onNavigationClicked(View view) {
        //moveTaskToBack(true);
        showCancelDialog();
    }

    @Override
    public void onBackPressed() {
        //moveTaskToBack(true);
        showCancelDialog();
    }

    private void showCancelDialog() {

        DialogBuilder.createConfirmDialog(this, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(CategoryListActivity.this, DisconnectActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                try {
                    mRemoteService.cancelSend();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                CategoryListActivity.this.finish();
            }
        }, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }, getString(R.string.cancel_confirm_info)).show();
    }
}
