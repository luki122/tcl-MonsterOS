package com.monster.cloud.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.monster.cloud.R;
import com.monster.cloud.activity.app.AppBackupActivity;
import com.monster.cloud.activity.contacts.ContactsChooseSyncTypeActivity;
import com.monster.cloud.activity.contacts.SyncContactsActivity;
import com.monster.cloud.activity.exchange.ShowImportQQDataActivity;
import com.monster.cloud.activity.sms.SyncSmsActivity;
import com.monster.cloud.service.SyncService;
import com.monster.cloud.utils.LoginUtil;
import com.monster.cloud.utils.SyncTimeUtil;
import com.monster.cloud.utils.SystemUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.tcl.account.sdkapi.AuthConfig;
import com.tcl.account.sdkapi.QQLoginListener;
import com.tcl.account.sdkapi.SessionAuthorizationType;
import com.tcl.account.sdkapi.SessionStatusCallback;
import com.tcl.account.sdkapi.Token;
import com.tcl.account.sdkapi.UiAccountHelper;
import com.tcl.account.sdkapi.User;
import com.tencent.qqpim.sdk.accesslayer.LoginMgrFactory;
import com.tencent.qqpim.sdk.accesslayer.StatisticsFactory;
import com.tencent.qqpim.sdk.accesslayer.SyncLogMgrFactory;
import com.tencent.qqpim.sdk.accesslayer.def.IAccountDef;
import com.tencent.qqpim.sdk.accesslayer.def.ISyncDef;
import com.tencent.qqpim.sdk.accesslayer.def.PMessage;
import com.tencent.qqpim.sdk.accesslayer.def.SyncProfileResult;
import com.tencent.qqpim.sdk.accesslayer.interfaces.ILoginMgr;
import com.tencent.qqpim.sdk.accesslayer.interfaces.basic.ISyncProcessorObsv;
import com.tencent.qqpim.sdk.apps.contactaccountfilter.ContactAccountFilterAndGetTimeReminder;
import com.tencent.qqpim.sdk.defines.DataSyncResult;
import com.tencent.qqpim.sdk.defines.ISyncMsgDef;
import com.tencent.qqpim.sdk.object.sms.SmsTimeType;
import com.tencent.qqpim.sdk.utils.AccountUtils;
import com.tencent.qqpim.softbox.SoftBoxProtocolModel;
import com.tencent.software.AppInfo;
import com.tencent.tauth.Tencent;
import com.tencent.tclsdk.sync.SyncCallLog;
import com.tencent.tclsdk.sync.SyncContact;
import com.tencent.tclsdk.sync.SyncSMS;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mst.app.MstActivity;
import mst.app.dialog.ProgressDialog;
import mst.widget.MstListView;

/**
 * Created by xiaobin on 16-10-11.
 */
public class MainActivity extends MstActivity implements ActivityCompat.OnRequestPermissionsResultCallback, ISyncProcessorObsv {

    private final static int TYPE_CONTACT = 100;
    private final static int TYPE_MESSAGE = 101;
    private final static int TYPE_RECORD = 102;
    private final static int TYPE_APPLIST = 103;

    private final static int REQUEST_READ_PHONE_STATE = 0;

    //QQ sdk login
    private ILoginMgr loginMgr;
    private String APPID = "101181845";
    private Tencent tencent;

    private String openId = null;
    private String accessToken = null;

    MstListView listView;
    MainPageAdapter adapter;
    ArrayList<ListEntity> dataList = null;
    mst.widget.toolbar.Toolbar toolbar;
    ImageView imgView, animView;

    private UiAccountHelper uiAccountHelper = null;

    private RelativeLayout one_key_recovery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_PHONE_STATE}, 0);
        }

        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_main);

        uiAccountHelper = new UiAccountHelper(this, callback);
        uiAccountHelper.onCreate(savedInstanceState);

        initView();
        toolbar = getToolbar();
        toolbar.setBackgroundColor(Color.parseColor("#05000000"));
        toolbar.inflateMenu(R.menu.toolbar_action_select_all);
        toolbar.setTitle("云服务");

        new Thread(new Runnable() {
            @Override
            public void run() {
                ContactAccountFilterAndGetTimeReminder.getContactAccountFilterSync();
                StatisticsFactory.getStatisticsUtil().checkContactAggregationNeeded();
            }
        }).start();

        loginMgr = LoginMgrFactory.getLoginMgr(this, AccountUtils.ACCOUNT_TYPE_QQ_SDK);
        tencent = Tencent.createInstance(APPID, getApplicationContext());

        checkTCLLogin();
        getIntentData();
    }

    private boolean sync_now = false;

    private void getIntentData() {
        Intent i = getIntent();
        if (i != null) {
            sync_now = i.getBooleanExtra("sync_now", false);
        }
    }


    private boolean checkTCLLogin() {

        User user = UiAccountHelper.getUserInfo(this);
        if (user != null && !TextUtils.isEmpty(user.accountName)) {
            showUserHead(user);

            if(!user.accountName.equals(LoginUtil.getTCLUsrID(this))){
                gotoShowImportQQData();
            } else {
                qqLogin();
            }

            return true;
        } else {
            UiAccountHelper.cleanCache();
            TCLAccountLogin();
            return false;
        }
    }

    private void showUserHead(User user) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        ImageSize mImageSize = new ImageSize(200, 200);
        ImageLoader.getInstance().loadImage(user.headIconUrl, mImageSize, options, new SimpleImageLoadingListener() {

            @Override
            public void onLoadingComplete(String imageUri, View view,
                                          Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);

                if (loadedImage != null) {
                    toolbar.getMenu().getItem(0).setIcon(new BitmapDrawable(getResources(), SystemUtil.createCircleImage(loadedImage, loadedImage.getWidth())));
                }
            }

        });
    }

    private void TCLAccountLogin() {
        AuthConfig authConfig = new AuthConfig(SessionAuthorizationType.AUTH);
        authConfig.setSessionAuthorizationType(SessionAuthorizationType.AUTH);
        Token token = UiAccountHelper.getCurrentToken(getApplicationContext());
//        if (null != token && !token.isInvalid()) {
//            //Toast.makeText(MainActivity.this, "logged!", Toast.LENGTH_LONG).show();
//            return;
//        }

        try {
            uiAccountHelper.requestSSOAuth(authConfig, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        uiAccountHelper.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        NotificationManagerCompat.from(this).cancel(OneKeyRecoveryActiviy.NOTIFICATION_ID);
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiAccountHelper.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiAccountHelper.onDestroy();
        Intent intent = new Intent(MainActivity.this, SyncService.class);
        stopService(intent);
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

    private int should_goto_activity = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        uiAccountHelper.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case SystemUtil.REQUEST_LOGIN_QQ_INIT:
                qqLogin();
                break;
            case SystemUtil.REQUEST_LOGIN_QQ_CONTACT:
                should_goto_activity = SystemUtil.REQUEST_LOGIN_QQ_CONTACT;
                qqLogin();
                break;
            case SystemUtil.REQUEST_LOGIN_QQ_SMS:
                should_goto_activity = SystemUtil.REQUEST_LOGIN_QQ_SMS;
                qqLogin();
                break;
            case SystemUtil.REQUEST_LOGIN_QQ_CALLLOG:
                should_goto_activity = SystemUtil.REQUEST_LOGIN_QQ_CALLLOG;
                qqLogin();
                break;
            case SystemUtil.REQUEST_LOGIN_QQ_APPLIST:
                should_goto_activity = SystemUtil.REQUEST_LOGIN_QQ_APPLIST;
                qqLogin();
                break;
            case SystemUtil.REQUEST_LOGIN_QQ_ONEKEY_RECOVERY:
                should_goto_activity = SystemUtil.REQUEST_LOGIN_QQ_ONEKEY_RECOVERY;
                qqLogin();
                break;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        AnimationDrawable animationDrawable = (AnimationDrawable) animView.getDrawable();
        animationDrawable.start();
    }

    private boolean is_goto_setting = false;

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_select_all:
                //业务逻辑
                if (LoginUtil.isLogInTCLAccount(this)) {
                    gotoSetting();
                } else {
                    TCLAccountLogin();
                    is_goto_setting = true;
                }

                break;
        }
        return true;
    }


    @Override
    public void onSyncStateChanged(PMessage pMessage) {
        Message msg = syncHandler.obtainMessage();
        msg.what = 0;
        msg.obj = pMessage;
        syncHandler.sendMessage(msg);
    }

    @Override
    public void onLoginkeyExpired() {
        // ??? DO NOTHING?
    }


    private SessionStatusCallback callback = new SessionStatusCallback() {
        @Override
        public void onSuccess(Token token) {
            if (is_goto_setting) {
                gotoSetting();
            } else {
                qqLogin();
            }

            User user = UiAccountHelper.getUserInfo(MainActivity.this);
            if (user != null) {
                showUserHead(user);
            }

        }

        @Override
        public void onError(int i) {
            if (!is_goto_setting) {//当点击头像进入tcl账号的时候不finish
                finish();
            }
            is_goto_setting = false;

        }

        @Override
        public void onOAuth(String s) {

        }
    };

    private void gotoSetting() {
        is_goto_setting = false;
        Toast.makeText(this, "gotoSetting!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(MainActivity.this, CloudSettingActivity.class));
    }


    boolean isQQSDKLogin = false;
    boolean isTokenExpired = false;

    private void qqLogin() {
        // get openId and token from SharedPreference first

        boolean enable = uiAccountHelper.qqLoginWithCache(this, new QQLoginListener() {
            @Override
            public void onSuccess(String str_json) {
//                Toast.makeText(MainActivity.this, "onSuccess json:" + json, Toast.LENGTH_LONG).show();

                JSONObject json;

                try {
                    json = new JSONObject(str_json);
                    openId = json.getString("openid");
                    accessToken = json.getString("access_token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (openId == null || accessToken == null) {
                    openId = LoginUtil.getOpenId(MainActivity.this);
                    accessToken = LoginUtil.getToken(MainActivity.this);
                }

                if (openId != null && accessToken != null) {

                    LoginUtil.updateOpenId(MainActivity.this, openId);
                    LoginUtil.updateToken(MainActivity.this, accessToken);
                    LoginUtil.saveTCLUserId(MainActivity.this);

                    qqSDKLogin();
                    //更新token
                    if (isTokenExpired) {
                        updateQQLogin();
                    }
                } else {
                    //qq login first
//            updateQQLogin();
                    gotoShowImportQQData();
                }


            }

            @Override
            public void onError(int errorCode, String errorMessage, String errorDetail) {
                Toast.makeText(MainActivity.this, "onError errorCode:" + errorCode + ",errorMessage:" + errorMessage + ",errorDetail" + errorDetail, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(MainActivity.this, "onCancel", Toast.LENGTH_LONG).show();
            }
        });

        if (!enable) {
            Toast.makeText(MainActivity.this, "QQ Login unEnable", Toast.LENGTH_LONG).show();
        }


    }


    private void gotoShowImportQQData() {
        Intent i = new Intent(this, ShowImportQQDataActivity.class);
        i.putExtra("is_should_return", true);
        i.putExtra("title", getString(R.string.app_name));
        startActivityForResult(i, SystemUtil.REQUEST_LOGIN_QQ_INIT);
    }

    private void updateQQLogin() {
        gotoShowImportQQData();
//        boolean enable = uiAccountHelper.qqLogin(this, new QQLoginListener() {
//            @Override
//            public void onSuccess(String s) {
//                JSONObject json;
//
//                try {
//                    json = new JSONObject(s);
//                    openId = json.getString("openid");
//                    accessToken = json.getString("access_token");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                Log.d("---", "---- openId " + openId + " token " + accessToken);
//                LoginUtil.updateOpenId(MainActivity.this, openId);
//                LoginUtil.updateToken(MainActivity.this, accessToken);
//                isTokenExpired = false;
//                loginHandler.sendEmptyMessage(0);
//            }
//
//            @Override
//            public void onError(int errorCode, String errorMessage, String errorDetail) {
//                Toast.makeText(MainActivity.this,
//                        "onError errorCode:" + errorCode + ",errorMessage:" + errorMessage + ",errorDetail" + errorDetail, Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onCancel() {
//                Toast.makeText(MainActivity.this, "onCancel", Toast.LENGTH_LONG).show();
//            }
//        });
//        if (!enable) {
//            Toast.makeText(MainActivity.this, "QQ Login disabled", Toast.LENGTH_LONG).show();
//        }
    }

    private ProgressDialog mProgressDialog;

    private void showProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(getString(R.string.str_login_ing));
        mProgressDialog.show();

    }

    private void hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    private void qqSDKLogin() {

        showProgressDialog();

        new Thread(new Runnable() {
            @Override
            public void run() {
                int result = loginMgr.qqSDKlogin(openId, accessToken, APPID);
                isQQSDKLogin = false;

                if (result == IAccountDef.EM_LOGIN_RES_LOGIN_OK) {
                    isQQSDKLogin = true;
                    LoginUtil.updateLoginLabel(MainActivity.this, true);
                } else if (result == 5001) {
                    // token expired
//                    isQQSDKLogin = false;
                    LoginUtil.updateLoginLabel(MainActivity.this, false);
                    isTokenExpired = true;
                } else if (result == IAccountDef.EM_LOGIN_RES_FAIL) {
                    // TODO 登陆失败
                } else if (result == IAccountDef.EM_LOGIN_RES_NETWORK_FAIL) {
                    // TODO 网络异常
                }
                SDKLoginHandler.sendEmptyMessage(result);
            }
        }).start();
    }

    private Handler SDKLoginHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            hideProgressDialog();
            switch (msg.what) {
                case IAccountDef.EM_LOGIN_RES_LOGIN_OK:

                    Toast.makeText(MainActivity.this, "sdk login success", Toast.LENGTH_SHORT).show();

                    checkGotoActivity();
                    if (sync_now) {
                        sync_now = false;

                        //执行一键同步
                        if (LoginUtil.getLoginLabel(MainActivity.this)) {

                        } else {
                            qqLogin();
                        }

                        if (!isOnSync) {
                            imgView.setSelected(true);
                            isOnSync = true;

                            for (int i = 0; i < 4; ++i) {
                                dataList.get(i).progress = 0;
                            }
                            adapter.setUpdating(true);
                            adapter.notifyDataSetChanged();
                            handler.sendEmptyMessage(1);
                        } else {
                            isOnStop = true;
                        }

                    }

                    break;
                case 5001:
                    updateQQLogin();
                    break;
                case IAccountDef.EM_LOGIN_RES_FAIL:
                    break;
                case IAccountDef.EM_LOGIN_RES_NETWORK_FAIL:
                    break;
                default:
                    break;
            }
        }
    };


    private void checkGotoActivity() {
        switch (should_goto_activity) {
            case SystemUtil.REQUEST_LOGIN_QQ_CONTACT:
                Intent i_contact = new Intent(MainActivity.this, SyncContactsActivity.class);
                startActivity(i_contact);
                break;
            case SystemUtil.REQUEST_LOGIN_QQ_CALLLOG:
                Intent i_calllog = new Intent(MainActivity.this, SyncRecordActivity.class);
                startActivity(i_calllog);
                break;
            case SystemUtil.REQUEST_LOGIN_QQ_SMS:
                Intent i_sms = new Intent(MainActivity.this, SyncSmsActivity.class);
                startActivity(i_sms);
                break;
            case SystemUtil.REQUEST_LOGIN_QQ_APPLIST:
                Intent i_app = new Intent(MainActivity.this, AppBackupActivity.class);
                startActivity(i_app);
                break;
            case SystemUtil.REQUEST_LOGIN_QQ_ONEKEY_RECOVERY:
                Intent i_recovery = new Intent(MainActivity.this, OneKeyRecoveryActiviy.class);
                startActivity(i_recovery);
                break;
        }
        should_goto_activity = 0;
    }

    private Handler loginHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    qqSDKLogin();
                    break;
                default:
                    break;
            }
        }
    };

    boolean isOnSync = false;
    boolean isOnStop = false;

    private void initView() {

        one_key_recovery = (RelativeLayout) findViewById(R.id.one_key_recovery);
        one_key_recovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

                if (LoginUtil.getLoginLabel(MainActivity.this)) {

                } else {
                    qqLogin();
                }

                if (!isOnSync) {
                    imgView.setSelected(true);
                    isOnSync = true;

                    for (int i = 0; i < 4; ++i) {
                        dataList.get(i).progress = 0;
                    }
                    adapter.setUpdating(true);
                    adapter.notifyDataSetChanged();
                    handler.sendEmptyMessage(1);
                } else {
                    isOnStop = true;
                }
                getLastSyncTime();
            }
        });
        animView = (ImageView) findViewById(R.id.cloud_anim);
    }

    private void initListView() {
        listView = (MstListView) findViewById(R.id.listView);
        dataList = initList();
        adapter = new MainPageAdapter(this, dataList);
        listView.setAdapter(adapter);
        listView.setDivider(null);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                switch (dataList.get(position).tag) {
                    case TYPE_CONTACT:

                        if (isQQSDKLogin) {
                            Intent i_contact = new Intent(MainActivity.this, SyncContactsActivity.class);
                            startActivity(i_contact);

                        } else {
                            gotoImportQQData(SystemUtil.REQUEST_LOGIN_QQ_CONTACT, getString(R.string.app_name));

                        }
                        break;
                    case TYPE_MESSAGE:

                        if (isQQSDKLogin) {
                            Intent i_sms = new Intent(MainActivity.this, SyncSmsActivity.class);
                            startActivity(i_sms);
                        } else {
                            gotoImportQQData(SystemUtil.REQUEST_LOGIN_QQ_SMS, getString(R.string.app_name));

                        }
                        break;
                    case TYPE_RECORD:

                        if (isQQSDKLogin) {
                            Intent i = new Intent(MainActivity.this, SyncRecordActivity.class);
                            startActivity(i);
                        } else {
                            gotoImportQQData(SystemUtil.REQUEST_LOGIN_QQ_CALLLOG, getString(R.string.app_name));

                        }
                        break;
                    case TYPE_APPLIST:

                        if (isQQSDKLogin) {
                            Intent appIntent = new Intent(MainActivity.this, AppBackupActivity.class);
                            startActivity(appIntent);
                        } else {
                            gotoImportQQData(SystemUtil.REQUEST_LOGIN_QQ_APPLIST, getString(R.string.app_name));

                        }
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
            entity.tag = 100 + i;
            entity.progress = 0;
            list.add(entity);
        }
        return list;
    }

    class MainPageAdapter extends BaseAdapter {

        ArrayList<ListEntity> list;

        Context context;
        ImageView imageView;
        TextView name, time;
        TextView label, percent;
        ProgressBar progressBar;

        boolean isUpdating;

        String neverSync;
        String lastSync;
        String neverBackup;
        String lastBackup;

        public MainPageAdapter(Context context, ArrayList<ListEntity> list) {
            this.context = context;
            this.list = list;

            neverSync = context.getResources().getString(R.string.never_sync);
            lastSync = context.getResources().getString(R.string.last_sync);
            neverBackup = context.getResources().getString(R.string.never_backup);
            lastBackup = context.getResources().getString(R.string.last_backup);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.main_page_item, null);
            }

            imageView = (ImageView) convertView.findViewById(R.id.icon);
            name = (TextView) convertView.findViewById(R.id.name);
            percent = (TextView) convertView.findViewById(R.id.percent);
            percent.setText(list.get(position).progress == 0 ? "等待备份" : "正在备份" + list.get(position).progress + "%");
            progressBar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
            progressBar.setProgress(list.get(position).progress);
            label = (TextView) convertView.findViewById(R.id.switch_label);

            time = (TextView) convertView.findViewById(R.id.time);
            long lastSyncTime = 0;
            boolean isAutoOn = false;
            switch (list.get(position).tag) {
                case TYPE_CONTACT:
                    imageView.setImageResource(R.drawable.contact);
                    name.setText(R.string.contact);
                    lastSyncTime = SyncTimeUtil.getContactSyncTime(context);
                    isAutoOn = SyncTimeUtil.getContactSyncLabel(context);
                    break;
                case TYPE_MESSAGE:
                    imageView.setImageResource(R.drawable.message);
                    name.setText(R.string.message);
                    lastSyncTime = SyncTimeUtil.getSmsSyncTime(context);
                    isAutoOn = SyncTimeUtil.getSmsSyncLabel(context);
                    break;
                case TYPE_RECORD:
                    imageView.setImageResource(R.drawable.record);
                    name.setText(R.string.call_log);
                    lastSyncTime = SyncTimeUtil.getRecordSyncTime(context);
                    isAutoOn = SyncTimeUtil.getRecordSyncLabel(context);
                    break;
                case TYPE_APPLIST:
                    imageView.setImageResource(R.drawable.app_list);
                    name.setText(R.string.app_list);
                    lastSyncTime = SyncTimeUtil.getAppListSyncTime(context);
                    isAutoOn = SyncTimeUtil.getAppListSyncLabel(context);
                    break;
            }
            label.setText(isAutoOn ? R.string.auto_sync_on : R.string.auto_sync_off);

            switch (list.get(position).tag) {
                case TYPE_CONTACT:
                    if (lastSyncTime == 0) {
                        time.setText(neverSync);
                    } else {
                        time.setText(lastSync + SyncTimeUtil.setTime(lastSyncTime, context));
                    }
                    break;
                default:
                    if (lastSyncTime == 0) {
                        time.setText(neverBackup);
                    } else {
                        time.setText(lastBackup + SyncTimeUtil.setTime(lastSyncTime, context));
                    }
                    break;
            }

            if (isUpdating) {
                label.setVisibility(View.INVISIBLE);
                time.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                percent.setVisibility(View.VISIBLE);
            } else {
                label.setVisibility(View.VISIBLE);
                time.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                percent.setVisibility(View.GONE);
            }

            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        public void setUpdating(boolean updating) {
            isUpdating = updating;
        }

    }

    class ListEntity {
        int tag;
        int progress;
    }

    public enum Type {
        CTT_TYPE , //contact type
        MSG_TYPE , //message type
        RCD_TYPE   //call log type
    }

    private Handler syncHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    uiProgressChanged((PMessage) msg.obj);
                    break;
                case 1:
                    dataList.get(3).progress = progress;
                    adapter.notifyDataSetChanged();
                    break;
                case 2:
                    SyncTimeUtil.updateListSyncTime(MainActivity.this, System.currentTimeMillis());
                    finishSync();
                    break;
                default:
                    break;
            }
        }
    };

    private void finishSync() {
        animView.setVisibility(View.GONE);
        adapter.setUpdating(false);
        adapter.notifyDataSetChanged();
        imgView.setSelected(false);
        isOnSync = false;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Intent i = new Intent();
                    i.setClass(MainActivity.this, ContactsChooseSyncTypeActivity.class);
                    startActivity(i);
                    break;
                case 1:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            new SyncContact(MainActivity.this, MainActivity.this).sync();
                        }
                    }).start();
                    break;
                case 2:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            new SyncSMS(MainActivity.this, MainActivity.this, SmsTimeType.TIME_ALL).sync();
                        }
                    }).start();
                    break;
                case 3:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            new SyncCallLog(MainActivity.this, MainActivity.this).sync();
                        }
                    }).start();
                    break;
                case 4:
                    //TODO
                    start();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int result = SoftBoxProtocolModel.backupSoft(MainActivity.this);
                            if (result == SoftBoxProtocolModel.RESULT_SUCCESS) {
                                progress = 100;
                                syncHandler.sendEmptyMessage(1);
                                syncHandler.sendEmptyMessage(2);
                            } else if (result == SoftBoxProtocolModel.RESULT_FAIL) {
                                fakeProgressThread.stop();
                                // TODO 异常处理

                            } else if (result == SoftBoxProtocolModel.RESULT_LOGINKEY_EXPIRE) {
                                fakeProgressThread.stop();
                                // TODO 异常处理
                            }
                            Log.d("---", "--- " + result);
                        }
                    }).start();
                    break;
                default:
                    break;
            }
        }
    };

    Type type = Type.CTT_TYPE;

    private void uiProgressChanged(PMessage msg) {
        switch (msg.msgId) {
            case ISyncMsgDef.ESTATE_SYNC_ALL_BEGIN:
                //同步开始（全部任务）
                animView.setVisibility(View.VISIBLE);
                break;
            case ISyncMsgDef.ESTATE_SYNC_SCAN_BEGIN:
                //数据库扫描开始
                break;
            case ISyncMsgDef.ESTATE_SYNC_SCAN_FINISHED:
                //数据库扫描结束
                break;
            case ISyncMsgDef.ESTATE_SYNC_PROGRESS_CHANGED:
                //同步进度变化
                switch (type) {
                    case CTT_TYPE:
                        dataList.get(0).progress = msg.arg1;
                        break;
                    case MSG_TYPE:
                        dataList.get(1).progress = msg.arg1;
                        break;
                    case RCD_TYPE:
                        dataList.get(2).progress = msg.arg1;
                        break;
                    default:
                        break;
                }
                adapter.notifyDataSetChanged();
                break;
            case ISyncMsgDef.ESTATE_SYNC_DATA_REARRANGEMENT_BEGIN:
                //数据同步完成，数据整理开始
                break;
            case ISyncMsgDef.ESTATE_SYNC_DATA_REARRANGEMENT_FINISHED:
                //数据同步完成，数据整理完成
                break;
            case ISyncMsgDef.ESTATE_SYNC_ALL_FINISHED:
                //同步结束（全部）
                switch (type) {
                    case CTT_TYPE:
                        SyncTimeUtil.updateContactSyncTime(MainActivity.this, System.currentTimeMillis());

                        if (!isOnStop) {
                            type = Type.MSG_TYPE;
                            handler.sendEmptyMessage(2);
                        } else {
                            syncAllFinished(msg);
                            finishSync();
                            isOnStop = false;
                            type = Type.CTT_TYPE;
                        }
                        break;
                    case MSG_TYPE:
                        SyncTimeUtil.updateSmsSyncTime(MainActivity.this, System.currentTimeMillis());

                        if (!isOnStop) {
                            type = Type.RCD_TYPE;
                            handler.sendEmptyMessage(3);
                        } else {
                            syncAllFinished(msg);
                            finishSync();
                            isOnStop = false;
                            type = Type.CTT_TYPE;
                        }
                        break;
                    case RCD_TYPE:
                        type = Type.CTT_TYPE;
                        syncAllFinished(msg);
                        SyncTimeUtil.updateRecordSyncTime(MainActivity.this, System.currentTimeMillis());

                        if (!isOnStop) {
                            handler.sendEmptyMessage(4);
                        } else {
                            finishSync();
                            isOnStop = false;
                        }
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    private void syncAllFinished(PMessage msg) {
        // 清理标识，恢复屏幕不长亮。
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        List<DataSyncResult> resultList = null;
        if (null != msg.obj1) {
            resultList = (List<DataSyncResult>) msg.obj1;
        }
        if (null == resultList) {
            return;
        }
        int size = resultList.size();
        DataSyncResult result = null;

        for (int i = 0; i < size; i++) {
            result = resultList.get(i);
            if (null == result) {
                return;
            }
            switch (result.getResult()) {
                case ISyncDef.SYNC_ERR_TYPE_SUCCEED:
                    Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
                    break;
                case ISyncDef.SYNC_ERR_TYPE_RELOGIN:
                    //需要重新登录
                    break;
                case ISyncDef.SYNC_ERR_TYPE_CLIENT_ERR:
                    //客户端错误
                    break;
                case ISyncDef.SYNC_ERR_TYPE_SERVER_ERR:
                    //网络错误
                    break;
                case ISyncDef.SYNC_ERR_TYPE_USER_CANCEL:
                    //用户取消
                    break;
                case ISyncDef.SYNC_ERR_TYPE_FAIL_CONFLICT:
                    //由于其他软件的同步模块正在使用导致的错误
                    break;
                case ISyncDef.SYNC_ERR_TYPE_TIME_OUT:
                    //网络超时错误
                    break;
                default:
                    break;
            }
        }
    }

    int progress = 0;
    Thread fakeProgressThread;

    private void start() {
        fakeProgressThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (progress >= 0 && progress < 99) {
                        progress += 1;
                        syncHandler.sendEmptyMessage(1);
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        fakeProgressThread.start();
    }

    private void getLastSyncTime() {
        final SyncProfileResult ret = SyncLogMgrFactory.getSyncProfile().reqForLastSyncTime();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String last = new Date(((long) ret.lastSyncTime * 1000)).toLocaleString();
//                Log.v("---", "ret = " + ret.result + " last " + last);
                Toast.makeText(MainActivity.this, "ret = " + ret.result + " last " + last, Toast.LENGTH_SHORT).show();
            }
        });
    }

}

