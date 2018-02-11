/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.activity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.tct.backupmanager.IBackupManagerServiceCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import cn.tcl.transfer.IReceiveCallback;
import cn.tcl.transfer.R;
import cn.tcl.transfer.fasttransfer.ReceiveBackground;
import cn.tcl.transfer.receiver.IReceiveInfo;
import cn.tcl.transfer.receiver.ReceiveBackupDataService;
import cn.tcl.transfer.systemApp.CalendarSysApp;
import cn.tcl.transfer.systemApp.ContactsSysApp;
import cn.tcl.transfer.systemApp.DialerSysApp;
import cn.tcl.transfer.systemApp.MmsSysApp;
import cn.tcl.transfer.util.DialogBuilder;
import cn.tcl.transfer.util.FilePathUtils;
import cn.tcl.transfer.util.LogUtils;
import cn.tcl.transfer.util.NotificationUtils;
import cn.tcl.transfer.util.Utils;

public class ReceivingActivity extends BaseActivity {

    private static final String TAG = "ReceivingActivity";

    List<PackageInfo> mSysApps = new ArrayList<>();
    List<PackageInfo> mUserApps = new ArrayList<>();

    ArrayList<String> mSelectSysApps = new ArrayList<>();
    ArrayList<String> mSelectUserApps = new ArrayList<>();

    public static List<RecvItem> mAllDataList = new ArrayList<RecvItem>();

    TextView mTimeLeftView;
    private ScrollView mScrollView;

    private ReceiveBackground mStatusIcon;
    private GearView mRestoreIcon;

    private static RecvItem mSysRecvItem;
    private static RecvItem mAppRecvItem;
    private static RecvItem mImageRecvItem;
    private static RecvItem mVideoRecvItem;
    private static RecvItem mAudioRecvItem;
    private static RecvItem mDocRecvItem;

    private static final int RECV_SUCESS = 10001;
    private static final int OPERATE_CANCEL = 10002;
    private static final int OPERATE_ERROR = 10003;

    private static final int CONNECT_ERROR = 10004;

    private static final int RESTORE_PROGRESS = 20000;
    private static final int RESTORE_SUCCESS = 20001;

    private static final int UPDATE_RECV_TIME = 30000;
    private static final int ITEMHEIGHT = 64;

    private static boolean isRestoring = false;

    private final int DB_INSERT_SPEED = 150;
    private volatile float mRecvSpeed = Utils.TRANSFER_SPEED;
    private final long RESTORE_SPEED = 20 * 1024 * 1024;

    public static boolean hasLauncher = false;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RECV_SUCESS:
                    try {
                        mHandler.removeMessages(UPDATE_RECV_TIME);
                        mStatusIcon.stopRippleAnimation();
                        mStatusIcon.setVisibility(View.GONE);
                        mTimeLeftView.setText(R.string.text_recovery_status);
                        updateRestoreLeftTimeView();
                        if (mRemoteService != null) {
                            isRestoring = true;
                            mSysRecvItem.updateView();
                            mAppRecvItem.updateView();
                            mImageRecvItem.updateView();
                            mVideoRecvItem.updateView();
                            mAudioRecvItem.updateView();
                            mDocRecvItem.updateView();
                            mRemoteService.beginRestore(mRestoreCallback);
                        }
                        findViewById(R.id.cancel).setVisibility(View.GONE);
                        mRestoreIcon.setVisibility(View.VISIBLE);
                        Toast.makeText(ReceivingActivity.this, R.string.text_restore, Toast.LENGTH_SHORT).show();
                    } catch (RemoteException e) {
                        Log.e(TAG, "beginRestore:", e);
                    }

                    break;
                case RESTORE_PROGRESS:
                    updateRestoreLeftTimeView();
                    break;
                case RESTORE_SUCCESS:
                    mHandler.removeMessages(RESTORE_PROGRESS);
                    Intent intent = new Intent(ReceivingActivity.this, CompleteActivity.class);
                    intent.putExtra(Utils.IS_SEND, false);
                    startActivity(intent);
                    finish();
                    break;
                case OPERATE_CANCEL:
                    finish();
                    break;
                case CONNECT_ERROR:
                    Intent intent1 = new Intent();
                    intent1.setClass(getApplicationContext(), DisconnectActivity.class);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent1);
                    finish();
                    break;
                case UPDATE_RECV_TIME:
                    updateRecvLeftTimeView();
                    mHandler.sendEmptyMessageDelayed(UPDATE_RECV_TIME, 10 * 1000);
                    break;
            }
        }
    };

    private long getLeftSize() {
        long leftSize = 0;
        for (RecvItem item : mAllDataList) {
            if(item.recvStatus != RecvItem.RECV_OK) {
                leftSize += item.totalSize - item.recvSize;
            }
        }
        return leftSize;
    }

    private void updateRecvLeftTimeView() {

        long leftSize = getLeftSize();
        long currentTime = System.currentTimeMillis();
        if(ReceiveBackupDataService.mTotalReceiveSize != 0) {
            mRecvSpeed = ReceiveBackupDataService.mTotalReceiveSize * 1000 / (currentTime - ReceiveBackupDataService.startRecvTime);
        }
        long leftSendTime = (long)(leftSize/ mRecvSpeed);
        long time = leftSendTime;
        long minutes = 0;
        if(leftSize > 0) {
            minutes = time/60;
            if(time%60 > 0) {
                minutes += 1;
            }
        }
        LogUtils.d(TAG, "updateRecvLeftTimeView" + "  mRecvSpeed=" + mRecvSpeed + "  leftSendTime:" + leftSendTime );
        mTimeLeftView.setText(getString(R.string.remaining_time,  minutes +  " " +  getString(R.string.text_min)));
    }

    private void updateRestoreLeftTimeView() {
        long sysTime = getRestoreDataLeftTime();
        long sqlTime = getRestoreSqlLeftTime();
        long time = Math.max(sysTime, sqlTime)/1000;

        long minutes = 0;
        if(time > 0) {
            minutes = time/60;
            if(time%60 > 0) {
                minutes += 1;
            }
        }
        LogUtils.d(TAG, "updateRestoreLeftTimeView" + "  sysTime=" + sysTime + "  installTime:" + time );
        mTimeLeftView.setText(getString(R.string.text_recovery_status,  minutes + " " +  getString(R.string.text_min)));
        mHandler.sendEmptyMessageDelayed(RESTORE_PROGRESS, 10 * 1000);
    }

    private IBackupManagerServiceCallback mRestoreCallback = new IBackupManagerServiceCallback.Stub() {

        @Override
        public void onStart() throws RemoteException {
            LogUtils.d(TAG, "onStart");
        }

        @Override
        public void onComplete() throws RemoteException {
            LogUtils.d(TAG, "onComplete");
            mHandler.sendEmptyMessage(RESTORE_SUCCESS);
            String[] pkgs = getResources().getStringArray(R.array.backup_system_app_pkg);
            for(String pkgName : pkgs) {
                if(hasLauncher && TextUtils.equals(pkgName, "com.monster.launcher")) {
                    break;
                }
                stop(getApplicationContext(), pkgName);
            }
            FilePathUtils.delFolder(Utils.RECEIVED_PATH);
        }

        @Override
        public void onUpdate(String test) throws RemoteException {
            LogUtils.d(TAG, "onUpdate: " + test);
            if(test.toLowerCase().startsWith("apk end:")) {
                String apkPath = test.substring(test.indexOf(":") + 1);
                FilePathUtils.delAllFile(apkPath);
            } else if(test.toLowerCase().startsWith("end:")) {
                String pkg = test.substring(test.indexOf(":") + 1);
                String sysDataPath = Utils.APP_APK_RECEIVED_PATH + "/" + pkg + ".tar";
                FilePathUtils.delAllFile(sysDataPath);
            }
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



    private IReceiveInfo mRemoteService;
    ServiceConnection mConn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mRemoteService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            try {
                mRemoteService = IReceiveInfo.Stub.asInterface(service);
                mRemoteService.registerCallback(mCallBack);
                mRemoteService.canSendNow();
                LogUtils.i(TAG, "onServiceConnected");
            } catch (Exception e) {
                Log.e(TAG, "onServiceConnected Exception:", e);
            }
        }
    };

    private IReceiveCallback.Stub  mCallBack = new IReceiveCallback.Stub() {

        @Override
        public void onConnected() throws RemoteException {
        }

        @Override
        public void onStart(int type) {
            mAllDataList.get(type).recvStatus = RecvItem.RECVING;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAllDataList.get(type).updateSendStatus(RecvItem.RECVING);
                }
            });
        }

        @Override
        public void onProgress(int type, long size, long speed) {
            mAllDataList.get(type).recvSize = size;
            mAllDataList.get(type).recvStatus = RecvItem.RECVING;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAllDataList.get(type).updateView();
                }
            });
        }

        @Override
        public void onFileBeginRecv(int type, String fileName) {
            mAllDataList.get(type).currentSendInfo = fileName;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAllDataList.get(type).updateSendFileName(fileName);
                }
            });
        }

        @Override
        public void onComplete(int type) {
            mAllDataList.get(type).recvStatus = RecvItem.RECV_OK;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAllDataList.get(type).updateView();
                    moveScrollToDest(type);
                }
            });
        }

        @Override
        public void onError(int type, String reason) {
            mHandler.sendEmptyMessage(CONNECT_ERROR);
        }

        @Override
        public void onAllComplete() throws RemoteException {
            mHandler.sendEmptyMessage(RECV_SUCESS);
        }

        @Override
        public void onCancel() throws RemoteException {

        }

        @Override
        public void onReceiveDataInfo(String info) throws RemoteException {

        }

        @Override
        public void onReceiveDataSize(String info) throws RemoteException {
            try {
                JSONObject json = new JSONObject(info);
                long sysSize = Long.parseLong((String)json.get(Utils.CATEGORY_SYS_SIZE));
                long appSize = Long.parseLong((String)json.get(Utils.CATEGORY_APP_SIZE));
                long imageSize = Long.parseLong((String)json.get(Utils.CATEGORY_IMAGE_SIZE));
                long videoSize = Long.parseLong((String)json.get(Utils.CATEGORY_VIDEO_SIZE));
                long audioSize = Long.parseLong((String)json.get(Utils.CATEGORY_AUDIO_SIZE));
                long docSize = Long.parseLong((String)json.get(Utils.CATEGORY_DOCUMENT_SIZE));

                mAllDataList.get(Utils.CATEGORY_SYS).totalSize = sysSize;
                mAllDataList.get(Utils.CATEGORY_APP).totalSize = appSize;
                mAllDataList.get(Utils.CATEGORY_IMAGE).totalSize = imageSize;
                mAllDataList.get(Utils.CATEGORY_VIDEO).totalSize = videoSize;
                mAllDataList.get(Utils.CATEGORY_AUDIO).totalSize = audioSize;
                mAllDataList.get(Utils.CATEGORY_DOCUMENT).totalSize = docSize;

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mAllDataList.get(Utils.CATEGORY_SYS).updateView();
                        mAllDataList.get(Utils.CATEGORY_APP).updateView();
                        mAllDataList.get(Utils.CATEGORY_IMAGE).updateView();
                        mAllDataList.get(Utils.CATEGORY_VIDEO).updateView();
                        mAllDataList.get(Utils.CATEGORY_AUDIO).updateView();
                        mAllDataList.get(Utils.CATEGORY_DOCUMENT).updateView();
                    }
                });
            } catch (JSONException e) {
                LogUtils.e(TAG, "onReceiveDataSize", e);
            } catch (Exception e) {
                LogUtils.e(TAG, "onReceiveDataSize", e);
            }
        }
    };

    private void moveScrollToDest(int type) {
        float density = getResources().getDisplayMetrics().density;
        int scrollY = (int)(density * ITEMHEIGHT * (type + 1));
        mScrollView.scrollTo(0 , scrollY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.recving_layout);
        Toast.makeText(this, R.string.notify_receiving, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, ReceiveBackupDataService.class);
        bindService(intent, mConn, Context.BIND_AUTO_CREATE);
        mTimeLeftView = (TextView)findViewById(R.id.secondary_text);
        mStatusIcon = (ReceiveBackground)findViewById(R.id.status_icon);
        mStatusIcon.post(new Runnable() {
            @Override
            public void run() {
                mStatusIcon.init(ReceivingActivity.this);
                mStatusIcon.startRippleAnimation();
            }
        });
        mRestoreIcon = (GearView)findViewById(R.id.restore_icon);
        mRestoreIcon.setVisibility(View.GONE);

        getAllApplications();

        initView();
        mHandler.sendEmptyMessage(UPDATE_RECV_TIME);
    }

    private void initView() {

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isRestoring) {
                    showCancelDialog();
                } else {
                    view.setEnabled(false);
                }
            }
        });
        mScrollView = (ScrollView)findViewById(R.id.receive_scrollview);
        try {
            if (getIntent() == null || !getIntent().hasExtra("data_size")) {
                return;
            }
            String info = getIntent().getStringExtra("data_size");
            LogUtils.d(TAG, "onCreate :" + info);
            JSONObject json = new JSONObject(info);
            long sysSize = Long.parseLong((String)json.get(Utils.CATEGORY_SYS_SIZE));
            long appSize = Long.parseLong((String)json.get(Utils.CATEGORY_APP_SIZE));
            long imageSize = Long.parseLong((String)json.get(Utils.CATEGORY_IMAGE_SIZE));
            long videoSize = Long.parseLong((String)json.get(Utils.CATEGORY_VIDEO_SIZE));
            long audioSize = Long.parseLong((String)json.get(Utils.CATEGORY_AUDIO_SIZE));
            long docSize = Long.parseLong((String)json.get(Utils.CATEGORY_DOCUMENT_SIZE));

            mAllDataList.get(Utils.CATEGORY_SYS).totalSize = sysSize;
            mAllDataList.get(Utils.CATEGORY_APP).totalSize = appSize;
            mAllDataList.get(Utils.CATEGORY_IMAGE).totalSize = imageSize;
            mAllDataList.get(Utils.CATEGORY_VIDEO).totalSize = videoSize;
            mAllDataList.get(Utils.CATEGORY_AUDIO).totalSize = audioSize;
            mAllDataList.get(Utils.CATEGORY_DOCUMENT).totalSize = docSize;

            mSysRecvItem.initView(findViewById(R.id.sys_view));
            mAppRecvItem.initView(findViewById(R.id.app_view));
            mImageRecvItem.initView(findViewById(R.id.image_view));
            mVideoRecvItem.initView(findViewById(R.id.video_view));
            mAudioRecvItem.initView(findViewById(R.id.audio_view));
            mDocRecvItem.initView(findViewById(R.id.doc_view));

        } catch (JSONException e) {
            LogUtils.e(TAG, "onCreate", e);
        } catch (Exception e) {
            LogUtils.e(TAG, "onCreate", e);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mRemoteService.unregisterCallback(mCallBack);
        } catch (RemoteException e) {
            LogUtils.e(TAG, "onDestroy", e);
        }
        unbindService(mConn);
    }

    private void cancelSending() {
        try {
            mRemoteService.cancelReceive();
        } catch (RemoteException e) {
            LogUtils.e(TAG, "cancelSending", e);
        }
    }

    private void showCancelDialog() {

        DialogBuilder.createConfirmDialog(this, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                cancelSending();
                Intent intent = new Intent(ReceivingActivity.this, DisconnectActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                ReceivingActivity.this.finish();
            }
        }, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }, getString(R.string.cancel_confirm_info)).show();
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
            if(TextUtils.equals(p.packageName, getApplicationContext().getPackageName())) {
                continue;
            }
            ApplicationInfo appInfo = p.applicationInfo;
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
                mSysApps.add(p);
                mSelectSysApps.add(p.packageName);
            } else if((appInfo.flags & ApplicationInfo.FLAG_INSTALLED) > 0) {
                mUserApps.add(p);
                mSelectUserApps.add(p.packageName);
            }
        }
    }

    private static class ViewHolder {
        public TextView title;
        public TextView summary;
        public TextView info;
        public ImageView categoryIcon;
        public ImageView expandIcon;
        public ProgressBar progressBar;
        View paddingView;
    }

    public static class RecvItem {

        public static final int RECV_NO = 0;
        public static final int RECVING = 1;
        public static final int RECV_OK = 2;

        public long totalSize;
        public long recvSize;
        public String title;
        public int recvStatus;
        public int icon;
        private Context mContext;

        public String currentSendInfo;
        public ViewHolder viewHolder = new ViewHolder();
        public View mItemview = null;

        public String getSummary() {

            switch (recvStatus) {
                case RecvItem.RECV_NO:
                case RecvItem.RECV_OK:
                    return mContext.getString(R.string.total_size, Utils.convertFileSize(totalSize));
                case RecvItem.RECVING:
                    return "";
                default:
                    return "";
            }
        }

        public String getInfo() {

            switch (recvStatus) {
                case RecvItem.RECV_NO:
                case RecvItem.RECV_OK:
                    return mContext.getString(R.string.total_size, Utils.convertFileSize(totalSize));
                case RecvItem.RECVING:
                    return currentSendInfo;
                default:
                    return "";
            }
        }

        public String getRecvSize() {
            if(recvStatus == RecvItem.RECVING) {
                return mContext.getString(R.string.recv_size, Utils.convertFileSize(recvSize));
            }
            return "";
        }

        public RecvItem(String title1, int ic, Context context) {
            title = title1;
            icon = ic;
            recvStatus = RECV_NO;
            mContext = context;
        }

        public void initView(View view) {
            mItemview = view;
            TextView titleView = (TextView)view.findViewById(R.id.title);
            TextView summary = (TextView)view.findViewById(R.id.summary);
            TextView sendInfo = (TextView)view.findViewById(R.id.send_info);
            ImageView categoryIcon = (ImageView)view.findViewById(R.id.category_icon);
            ImageView expandIcon = (ImageView)view.findViewById(R.id.expand);
            ProgressBar progressBar = (ProgressBar)view.findViewById(R.id.send_progress);
            View paddingView = view.findViewById(R.id.sending_top);

            viewHolder.title = titleView;
            viewHolder.summary = summary;
            viewHolder.info = sendInfo;
            viewHolder.categoryIcon = categoryIcon;
            viewHolder.expandIcon = expandIcon;
            viewHolder.progressBar = progressBar;
            viewHolder.paddingView = paddingView;

            viewHolder.categoryIcon.setImageResource(icon);
            updateView();
        }

        public void updateView() {

            if(isRestoring) {
                viewHolder.paddingView.setVisibility(View.VISIBLE);
                viewHolder.progressBar.setVisibility(View.GONE);
                viewHolder.expandIcon.setVisibility(View.VISIBLE);

                viewHolder.title.setText(title);
                viewHolder.info.setText(R.string.recv_successful);
                viewHolder.summary.setText("");
                viewHolder.categoryIcon.setImageResource(icon);
                return;
            }

            if(totalSize == 0) {
                if (mItemview != null) {
                    mItemview.setVisibility(View.GONE);
                }
                return;
            }
            updateSendStatus(recvStatus);
            viewHolder.title.setText(title);
            viewHolder.info.setText(getInfo());
            viewHolder.summary.setText(getRecvSize());
            viewHolder.progressBar.setMax(100);
            if(totalSize != 0) {
                viewHolder.progressBar.setProgress((int) (recvSize * 100 / totalSize));
            }
        }

        public void updateSendFileName(String name) {
            currentSendInfo = name;
            viewHolder.info.setText(getInfo());
        }

        public void updateSendStatus(int status) {
            recvStatus = status;
            switch (recvStatus) {
                case RecvItem.RECV_NO:
                    viewHolder.paddingView.setVisibility(View.VISIBLE);
                    viewHolder.progressBar.setVisibility(View.GONE);
                    viewHolder.expandIcon.setVisibility(View.GONE);
                    break;
                case RecvItem.RECVING:
                    viewHolder.paddingView.setVisibility(View.GONE);
                    viewHolder.progressBar.setVisibility(View.VISIBLE);
                    viewHolder.expandIcon.setVisibility(View.GONE);
                    break;
                case RecvItem.RECV_OK:
                    viewHolder.paddingView.setVisibility(View.VISIBLE);
                    viewHolder.progressBar.setVisibility(View.GONE);
                    viewHolder.expandIcon.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    }
    @Override
    public void onNavigationClicked(View view) {
        moveTaskToBack(true);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        return;
    }

    private void stop(Context context, final String pkgName) {
        if(context == null || TextUtils.isEmpty(pkgName)) {
            return;
        }
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        Method m = null;
        try {
            Class c = Class.forName("android.app.ActivityManager");
            m = c.getMethod("forceStopPackage", Class.forName("java.lang.String") );
            m.invoke(am, pkgName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initDataList(Context context) {
        mAllDataList.clear();
        isRestoring = false;
        String[] categories = context.getResources().getStringArray(R.array.receiver_content_item);

        mSysRecvItem = new RecvItem(categories[Utils.CATEGORY_SYS], R.drawable.system_icon, context);
        mAppRecvItem = new RecvItem(categories[Utils.CATEGORY_APP], R.drawable.app_icon, context);
        mImageRecvItem = new RecvItem(categories[Utils.CATEGORY_IMAGE], R.drawable.image_icon, context);
        mVideoRecvItem = new RecvItem(categories[Utils.CATEGORY_VIDEO], R.drawable.video_icon, context);
        mAudioRecvItem = new RecvItem(categories[Utils.CATEGORY_AUDIO], R.drawable.music_icon, context);
        mDocRecvItem = new RecvItem(categories[Utils.CATEGORY_DOCUMENT], R.drawable.doc_icon, context);

        mAllDataList.add(mSysRecvItem);
        mAllDataList.add(mAppRecvItem);
        mAllDataList.add(mImageRecvItem);
        mAllDataList.add(mVideoRecvItem);
        mAllDataList.add(mAudioRecvItem);
        mAllDataList.add(mDocRecvItem);
    }

    private long getRestoreDataLeftTime() {
        long dataSize = FilePathUtils.caculateFolderSize(Utils.SYS_DATA_RECEIVED_PATH);
        long otherDataSize = FilePathUtils.caculateFolderSize(Utils.SYS_OTHER_DATA_RECEIVED_PATH);
        long leftSize = dataSize + otherDataSize;

        long spendTime = System.currentTimeMillis() - ReceiveBackupDataService.mStartRestoreTime;
        long restoreSize = ReceiveBackupDataService.mTotalSize - leftSize;

        long speed = RESTORE_SPEED;
        if(restoreSize > 0) {
            speed = restoreSize * 1000/spendTime;
        }
        long leftTime = leftSize * 1000/speed;
        LogUtils.v(TAG, "getRestoreDataLeftTime leftTime:" + leftTime + "   speed:"+ speed);
        return leftTime;
    }

    private long getRestoreSqlLeftTime() {

        long contactTime = calculateContactTime();
        long calendarTime = calculateCalendarTime();
        long smstTime = calculateSmsTime();
        long mmsTime = calculateMmsTime();
        long calllogTime = calculateCalllogTime();

        LogUtils.v(TAG, "contactTime:" + contactTime
                + "    calendarTime:" + calendarTime
                + "    smstTime:" + smstTime
                + "    mmsTime:" + mmsTime
                + "    calllogTime:" + calllogTime);

        return Math.max(Math.max(Math.max(contactTime, calendarTime), Math.max(smstTime, mmsTime)), calllogTime);
    }

    private long calculateContactTime() {
        int totalCount = ContactsSysApp.recvCount;
        int insertCount = 0;
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        try {
            insertCount = cursor.getCount() - ContactsSysApp.localCount;
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }
        if(insertCount >= totalCount || totalCount == 0 || insertCount < 0) {
            return 0;
        }
        if(ContactsSysApp.startTime == 0) {
            return 0;
        }
        long insertSpeed = DB_INSERT_SPEED;
        if(insertCount > 0) {
            insertSpeed = (System.currentTimeMillis() - ContactsSysApp.startTime) / insertCount;
        }
        int contactCount = totalCount - insertCount;
        long leftTime = contactCount * insertSpeed;

        LogUtils.v(TAG, "calculateContactTime leftTime:" + leftTime  + "   count:" +  contactCount +   "  insertSpeed:" + insertSpeed);
        return leftTime;
    }

    private long calculateCalendarTime() {
        int totalCount = CalendarSysApp.recvCount;
        int insertCount = CalendarSysApp.inertCount;

        if(insertCount == totalCount || totalCount == 0) {
            return 0;
        }
        if(CalendarSysApp.startTime == 0) {
            return 0;
        }
        long insertSpeed = DB_INSERT_SPEED;
        if(insertCount > 0) {
            insertSpeed = (System.currentTimeMillis() - CalendarSysApp.startTime) / insertCount;
        }

        int contactCount = totalCount - insertCount;
        long leftTime = contactCount * insertSpeed;
        LogUtils.v(TAG, "calculateCalendarTime leftTime:" + leftTime + "  insertSpeed:" + insertSpeed);
        return leftTime;
    }

    private long calculateSmsTime() {
        int totalCount = MmsSysApp.smsRecvCount;
        int insertCount = MmsSysApp.smsInertCount;

        if(insertCount == totalCount || totalCount == 0) {
            return 0;
        }
        if(MmsSysApp.smsStartTime == 0) {
            return 0;
        }
        long insertSpeed = DB_INSERT_SPEED;
        if(insertCount > 0) {
            insertSpeed = (System.currentTimeMillis() - MmsSysApp.smsStartTime) / insertCount;
        }

        int contactCount = totalCount - insertCount;
        long leftTime = contactCount * insertSpeed;
        LogUtils.v(TAG, "calculateSmsTime leftTime:" + leftTime + "   count:" +  contactCount +  "  insertSpeed:" + insertSpeed);
        return leftTime;
    }

    private long calculateMmsTime() {
        int totalCount = MmsSysApp.mmsRecvCount;
        int insertCount = MmsSysApp.mmsInertCount;

        if(insertCount == totalCount || totalCount == 0) {
            return 0;
        }
        if(MmsSysApp.mmsStartTime == 0) {
            return 0;
        }
        long insertSpeed = DB_INSERT_SPEED;
        if(insertCount > 0) {
            insertSpeed = (System.currentTimeMillis() - MmsSysApp.mmsStartTime) / insertCount;
        }
        int contactCount = totalCount - insertCount;
        long leftTime = contactCount * insertSpeed;
        LogUtils.v(TAG, "calculateMmsTime leftTime:" + leftTime  + "   count:" +  contactCount +   "  insertSpeed:" + insertSpeed);
        return leftTime;
    }

    private long calculateCalllogTime() {
        int totalCount = DialerSysApp.recvCount;
        int insertCount = DialerSysApp.inertCount;

        if(insertCount == totalCount || totalCount == 0) {
            return 0;
        }
        if(DialerSysApp.startTime == 0) {
            return 0;
        }
        long insertSpeed = DB_INSERT_SPEED;
        if(insertCount > 0) {
            insertSpeed = (System.currentTimeMillis() - DialerSysApp.startTime) / insertCount;
        }
        int contactCount = totalCount - insertCount;
        long leftTime = contactCount * insertSpeed;
        LogUtils.v(TAG, "calculateCalllogTime leftTime:" + leftTime  + "   count:" +  contactCount +   "  insertSpeed:" + insertSpeed);
        return leftTime;
    }

}
