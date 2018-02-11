package com.monster.cloud.activity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.monster.cloud.ICallBack;
import com.monster.cloud.ProgressConnection;
import com.monster.cloud.R;
import com.monster.cloud.activity.app.AppBackupActivity;
import com.monster.cloud.activity.contacts.SyncContactsActivity;
import com.monster.cloud.activity.exchange.ShowImportQQDataActivity;
import com.monster.cloud.activity.sms.SyncSmsActivity;
import com.monster.cloud.adpater.MainPageAdapter.ListEntity;
import com.monster.cloud.adpater.MainPageAdapter.MainPageListAdapter;
import com.monster.cloud.constants.Constant;
import com.monster.cloud.preferences.FilePreferences;
import com.monster.cloud.preferences.Preferences;
import com.monster.cloud.service.SyncService;
import com.monster.cloud.sync.BaseSyncTask;
import com.monster.cloud.sync.SyncHelper;
import com.monster.cloud.utils.LoginUtil;
import com.monster.cloud.utils.SyncTimeUtil;
import com.monster.cloud.utils.SystemUtil;
import com.tcl.account.sdkapi.AuthConfig;
import com.tcl.account.sdkapi.QQLoginListener;
import com.tcl.account.sdkapi.SessionAuthorizationType;
import com.tcl.account.sdkapi.SessionStatusCallback;
import com.tcl.account.sdkapi.Token;
import com.tcl.account.sdkapi.UiAccountHelper;
import com.tcl.account.sdkapi.User;
import com.tencent.qqpim.sdk.accesslayer.StatisticsFactory;
import com.tencent.qqpim.sdk.apps.contactaccountfilter.ContactAccountFilterAndGetTimeReminder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import mst.app.dialog.AlertDialog;

/**
 * Created by xiaobin on 16-10-11.
 */
public class MainActivity extends BaseActivity implements ActivityCompat.OnRequestPermissionsResultCallback,
                                                         SharedPreferences.OnSharedPreferenceChangeListener {

    private final static int REQUEST_READ_PHONE_STATE = 0;
    private final static String TAG = "MainActivity";

    //QQ sdk login
    private String APPID = "101181845";

    private String openId = null;
    private String accessToken = null;

    private ListView listView;
    private MainPageListAdapter adapter;
    private ArrayList<ListEntity> dataList = null;
    private mst.widget.toolbar.Toolbar toolbar;
    private ImageView imgView, animView;

    private FilePreferences preferences;
    private SharedPreferences sharedPreferences;
    private UiAccountHelper uiAccountHelper = null;

    private RelativeLayout onKeyRecovery;
    private TextView textHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        startService(new Intent(this, SyncService.class));
        bindService();
        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_PHONE_STATE}, 0);
        }

        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_main);
        getToolbar().setNavigationIcon(null);
        toolbar = getToolbar();
        toolbar.inflateMenu(R.menu.toolbar_action_select_all);
        toolbar.setTitle("云服务");

        SystemUtil.setStatusBarColor(this,R.color.background_fafafa);
        uiAccountHelper = new UiAccountHelper(this, callback);
        uiAccountHelper.onCreate(savedInstanceState);

        initView();

        new Thread(new Runnable() {
            @Override
            public void run() {
                ContactAccountFilterAndGetTimeReminder.getContactAccountFilterSync();
                StatisticsFactory.getStatisticsUtil().checkContactAggregationNeeded();
            }
        }).start();

        checkTCLLogin();

        //listen to the sharedPreference
        preferences = (FilePreferences) Preferences.Factory.getInstance(this, Constant.FILE_TYPE);
        sharedPreferences = preferences.getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        bindService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiAccountHelper.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        NotificationManagerCompat.from(this).cancel(OneKeyRecoveryActiviy.NOTIFICATION_ID);
        if (mService != null && isOnSync) {
            try {
                mService.notifyServiceOnStart();
            } catch (RemoteException e) {

            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiAccountHelper.onPause();
        if (mService != null && isOnSync) {
            try {
                mService.notifyServiceOnStop();
            } catch (RemoteException e) {

            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//        unbindService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService();
        uiAccountHelper.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onNavigationClicked(View view) {
        finish();
    }

    @Override
    public void onBackPressed() {
        if (isOnSync) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(getString(R.string.cancel_synchronize))
                    .setMessage(getString(R.string.commit_cancel_synchronize))
                    .setPositiveButton(R.string.cancel_synchronize, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                mService.stopSynchronize();
//                                        syncHandler.sendEmptyMessage(2);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton(R.string.continue_synchronize, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // DO NOTHING
                        }
                    }).show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // DO NOTHING
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        uiAccountHelper.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        AnimationDrawable animationDrawable = (AnimationDrawable) animView.getDrawable();
        animationDrawable.start();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_select_all:
                //业务逻辑

                if(!SystemUtil.checkNoNetWork(MainActivity.this)){
                    return true;
                }

                gotoSetting();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    }

    @Override
    public void initViews() {

    }

    @Override
    public void initData() {

    }

    @Override
    public void networkNotAvailable() {
        if (isOnSync) {
            SystemUtil.checkNoNetWork(this);
            //TODO
            try {
                mService.stopSynchronize();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private SessionStatusCallback callback = new SessionStatusCallback() {
        @Override
        public void onSuccess(Token token) {
            qqLogin();
        }

        @Override
        public void onError(int i) {
            // TODO: 16-12-20  
            finish();
        }

        @Override
        public void onOAuth(String s) {

        }
    };

    private boolean checkTCLLogin() {
        User user = UiAccountHelper.getUserInfo(this);
        if (user != null && !TextUtils.isEmpty(user.accountName)) {
            qqLogin();
            return true;
        } else {
            UiAccountHelper.cleanCache();
            TCLAccountLogin();
            return false;
        }
    }

    private void TCLAccountLogin() {
        AuthConfig authConfig = new AuthConfig(SessionAuthorizationType.AUTH);
        authConfig.setSessionAuthorizationType(SessionAuthorizationType.AUTH);
        try {
            uiAccountHelper.requestSSOAuth(authConfig, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void gotoSetting() {
        startActivityForResult(new Intent(MainActivity.this, CloudSettingActivity.class),1);
    }

    boolean isQQSDKLogin = false;

    private void qqLogin() {
        // get openId and token from SharedPreference first
        boolean enable = uiAccountHelper.qqLoginWithCache(this, new QQLoginListener() {
            @Override
            public void onSuccess(String str_json) {
                JSONObject json;
                try {
                    json = new JSONObject(str_json);
                    openId = json.getString("openid");
                    accessToken = json.getString("access_token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                
                User user = UiAccountHelper.getUserInfo(MainActivity.this);
                if (openId != null && accessToken != null && user.accountName.equals(LoginUtil.getTCLUsrID(MainActivity.this))) {
                    LoginUtil.updateOpenId(MainActivity.this, openId);
                    LoginUtil.updateToken(MainActivity.this, accessToken);
                    LoginUtil.saveTCLUserId(MainActivity.this);
                } else {
                    //qq login first
                    gotoShowImportQQData();
                }
            }

            @Override
            public void onError(int errorCode, String errorMessage, String errorDetail) {
                Log.e(TAG, "onError errorCode:" + errorCode + ",errorMessage:" + errorMessage + ",errorDetail" + errorDetail);
            }

            @Override
            public void onCancel() {
                Log.e(TAG, "login onCancel");
            }
        });

        if (!enable) {
            Log.e(TAG, "QQ Login unEnable");
        }

    }

    // TODO: 16-12-20 不知道用到哪里 
    private void gotoShowImportQQData() {
        Intent i = new Intent(this, ShowImportQQDataActivity.class);
        i.putExtra("is_should_return", true);
        i.putExtra("title", getString(R.string.app_name));
        startActivityForResult(i, SystemUtil.REQUEST_LOGIN_QQ_INIT);
    }

    boolean isOnSync = false;

    private void initView() {
        animView = (ImageView) findViewById(R.id.cloud_anim);
        textHint = (TextView) findViewById(R.id.texthint);
        onKeyRecovery = (RelativeLayout) findViewById(R.id.one_key_recovery);
        onKeyRecovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!SystemUtil.checkNoNetWork(MainActivity.this)){
                    return;
                }

                if (isQQSDKLogin) {
                    Intent i = new Intent(MainActivity.this, OneKeyRecoveryActiviy.class);
                    startActivity(i);
                } else {
                    gotoImportQQData(SystemUtil.REQUEST_LOGIN_QQ_ONEKEY_RECOVERY, getString(R.string.app_name));
                }
            }
        });

        initListView();
        imgView = (ImageView) findViewById(R.id.btn_update);
        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!SystemUtil.checkNoNetWork(MainActivity.this)){
                    return;
                }

                if (LoginUtil.getLoginLabel(MainActivity.this)) {

                } else {
                    qqLogin();
                }

                if (!isOnSync) {
                    if (mService == null) {
                        return;
                    }


                    for (int i = 0; i < 4; ++i) {
                        dataList.get(i).progress = 0;
                    }

                    if (SyncHelper.isAllSyncEnable(MainActivity.this)) {
                        imgView.setSelected(true);
                        isOnSync = true;
                        startAllSynchronize();
                    } else {
                        //TODO 弹出提示框:是否打开所有开关
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.cozy_hint_title)
                                .setMessage(R.string.one_step_open_content)
                                .setPositiveButton(R.string.one_step_open, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //TODO 1.打开所有开关 2.然后一键同步
                                        SyncTimeUtil.setContactSyncLabel(MainActivity.this, true);
                                        SyncTimeUtil.setSmsSyncLabel(MainActivity.this, true);
                                        SyncTimeUtil.setRecordSyncLabel(MainActivity.this, true);
                                        SyncTimeUtil.setAppListSyncLabel(MainActivity.this, true);
                                        imgView.setSelected(true);
                                        isOnSync = true;
                                        startAllSynchronize();
                                    }
                                })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //DO NOTHING
                                    }
                                }).show();
                    }
                } else {
                    //stop synchronize
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(getString(R.string.cancel_synchronize))
                            .setMessage(getString(R.string.commit_cancel_synchronize))
                            .setPositiveButton(R.string.cancel_synchronize, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        mService.stopSynchronize();
//                                        syncHandler.sendEmptyMessage(2);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .setNegativeButton(R.string.continue_synchronize, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // DO NOTHING
                                }
                            }).show();

                }

            }
        });
    }

    private void initListView() {
        listView = (ListView) findViewById(R.id.listView);
        dataList = initList();
        adapter = new MainPageListAdapter(this, dataList);
        listView.setAdapter(adapter);
        listView.setDivider(null);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                if(!SystemUtil.checkNoNetWork(MainActivity.this)){
                    return;
                }

                switch (dataList.get(position).tag) {
                    case BaseSyncTask.TASK_TYPE_SYNC_CONTACT:
                        Intent i_contact = new Intent(MainActivity.this, SyncContactsActivity.class);
                        startActivity(i_contact);
                        break;
                    case BaseSyncTask.TASK_TYPE_SYNC_SMS:
                        Intent i_sms = new Intent(MainActivity.this, SyncSmsActivity.class);
                        startActivity(i_sms);
                        break;
                    case BaseSyncTask.TASK_TYPE_SYNC_CALLLOG:
                        Intent i = new Intent(MainActivity.this, SyncRecordActivity.class);
                        startActivity(i);
                        break;
                    case BaseSyncTask.TASK_TYPE_SYNC_SOFT:
                        Intent appIntent = new Intent(MainActivity.this, AppBackupActivity.class);
                        startActivity(appIntent);
                        break;
                }
            }
        });
    }

    private void gotoImportQQData(int request, String title) {

        Intent i = new Intent(this, ShowImportQQDataActivity.class);
        i.putExtra("is_should_return", true);
        i.putExtra("title", title);
        startActivityForResult(i, request);

    }

    private ArrayList<ListEntity> initList() {
        ArrayList<ListEntity> list = new ArrayList<>();
        for (int i = 0; i < 4; ++i) {
            ListEntity entity = new ListEntity();
            entity.tag = i + 123;
            entity.progress = 0;
            list.add(entity);
        }
        return list;
    }

    private Handler syncHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    adapter.notifyDataSetChanged();
                    break;
                case 1:
                    updateSyncTime(msg.arg1);
                    break;
                case 2:
                    finishSync();
                    break;
                default:
                    break;
            }
        }
    };

    private void finishSync() {
        animView.setVisibility(View.GONE);
        textHint.setVisibility(View.VISIBLE);
        onKeyRecovery.setVisibility(View.VISIBLE);
        listView.setEnabled(true);
        adapter.setUpdating(false);
        adapter.notifyDataSetChanged();
        imgView.setSelected(false);
        isOnSync = false;
    }

    private ProgressConnection mService;

    private ICallBack.Stub aidlCallback = new ICallBack.Stub() {
        @Override
        public void updateProgress(int taskType, int progress) throws RemoteException {
            // TODO update main page listview progress bar
            // TODO 效率优化
            Log.d("BaseSyncTask", "list update progress: " + progress + "task type: " + taskType);
            int current = 0;
            for (int i = 0; i < dataList.size(); ++i) {
                if (dataList.get(i).tag == taskType) {
                    current = i;
                    break;
                }
            }
            dataList.get(current).progress = progress;
            // notify handler to update UI
            syncHandler.sendEmptyMessage(0);
        }

        @Override
        public void notifyCurrentSyncFinished(int currentType) throws RemoteException {
            Message msg = syncHandler.obtainMessage();
            msg.what = 1;
            msg.arg1 =currentType;
            syncHandler.sendMessage(msg);
            //TODO 当进度条100%时 listview.smoothScrollToPosition(i)
            //TODO 当前listview最上一行对应adapter的position应满足 position >= size - 4 && size >= 1 否则向上移动
        }

        @Override
        public void notifyAllSyncFinished() throws RemoteException {
            //adapter中的不显示项清空
            adapter.deleteHiddenPositions();
            // 列表恢复原来样式
            syncHandler.sendEmptyMessage(2);
        }
    };

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ProgressConnection.Stub.asInterface(service);
            try {
                Log.e("BaseSyncTask", "client registerCallback...");
                mService.registerCallback(aidlCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                Log.e("BaseSyncTask", "client unregisterCallback...");
                mService.unregisterCallback(aidlCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mService = null;
        }
    };

    public void bindService() {
        Intent bindIntend = new Intent(this, SyncService.class);
        bindService(bindIntend, connection, BIND_AUTO_CREATE);
    }

    public void unbindService() {
        unbindService(connection);
    }

    private void updateSyncTime(int type) {
        switch (type) {
            case BaseSyncTask.TASK_TYPE_SYNC_CONTACT:
                SyncTimeUtil.updateContactSyncTime(this, System.currentTimeMillis());
                break;
            case BaseSyncTask.TASK_TYPE_SYNC_SMS:
                SyncTimeUtil.updateSmsSyncTime(MainActivity.this, System.currentTimeMillis());
                break;
            case BaseSyncTask.TASK_TYPE_SYNC_CALLLOG:
                SyncTimeUtil.updateRecordSyncTime(MainActivity.this, System.currentTimeMillis());
                break;
            case BaseSyncTask.TASK_TYPE_SYNC_SOFT:
                SyncTimeUtil.updateListSyncTime(MainActivity.this, System.currentTimeMillis());
                break;
        }
    }

    private void startAllSynchronize() {
        if (mService != null) {
            adapter.updateLabels();
            adapter.setUpdating(true);
            adapter.notifyDataSetChanged();

            try {
                mService.startSynchronize(-1, true);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            //1. 隐藏QQ助手恢复本机的条目 2. to disable list item onClick 3.显示动画
            animView.setVisibility(View.VISIBLE);
            textHint.setVisibility(View.GONE);
            onKeyRecovery.setVisibility(View.GONE);
            listView.setEnabled(false);
        }
    }
}

