package com.monster.cloud.activity;

import android.app.AppOpsManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.monster.cloud.R;
import com.monster.cloud.activity.app.AppRecoveryActivity;
import com.monster.cloud.activity.exchange.ShowImportQQDataActivity;
import com.monster.cloud.activity.sms.SmsChooseSyncTypeActivity;
import com.monster.cloud.constants.Constant;
import com.monster.cloud.utils.LogUtil;
import com.monster.cloud.utils.LoginUtil;
import com.monster.cloud.utils.SystemUtil;
import com.monster.market.download.AppDownloadData;
import com.tencent.qqpim.sdk.accesslayer.LoginMgrFactory;
import com.tencent.qqpim.sdk.accesslayer.StatisticsFactory;
import com.tencent.qqpim.sdk.accesslayer.def.IAccountDef;
import com.tencent.qqpim.sdk.accesslayer.def.ISyncDef;
import com.tencent.qqpim.sdk.accesslayer.def.PMessage;
import com.tencent.qqpim.sdk.accesslayer.interfaces.ILoginMgr;
import com.tencent.qqpim.sdk.accesslayer.interfaces.basic.ISyncProcessorObsv;
import com.tencent.qqpim.sdk.accesslayer.interfaces.statistics.IStatisticsUtil;
import com.tencent.qqpim.sdk.apps.contactaccountfilter.ContactAccountFilterAndGetTimeReminder;
import com.tencent.qqpim.sdk.defines.DataSyncResult;
import com.tencent.qqpim.sdk.defines.ISyncMsgDef;
import com.tencent.qqpim.sdk.object.sms.SmsTimeType;
import com.tencent.qqpim.sdk.utils.AccountUtils;
import com.tencent.tauth.Tencent;
import com.tencent.tclsdk.sync.SyncCallLog;
import com.tencent.tclsdk.sync.SyncContact;
import com.tencent.tclsdk.sync.SyncSMS;
import com.tencent.tclsdk.utils.GetCountUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import mst.app.MstActivity;
import mst.app.dialog.AlertDialog;
import mst.app.dialog.ProgressDialog;

/**
 * Created by zouxu on 16-10-28.
 */
public class OneKeyRecoveryActiviy extends MstActivity implements View.OnClickListener {

    private ImageView img_bg;
    private TextView text_no_data;
    private TextView text_connect;
    private RelativeLayout start_recovery_layout;
    private TextView text_start_revovery;
    private RelativeLayout contact_layout;
    private TextView contact_info;
    private RelativeLayout sms_layout;
    private TextView sms_info;
    private RelativeLayout calllog_layout;
    private TextView calllog_info;
    private RelativeLayout app_layout;
    private TextView app_info;

    private CheckBox check_contact;
    private CheckBox check_sms;
    private CheckBox check_calllog;
    private CheckBox check_app;

    private TextView recovery_contact_ing;
    private ProgressBar contact_progress_bar;
    private ImageView contact_next_img;

    private TextView recovery_sms_ing;
    private ProgressBar sms_progress_bar;
    private ImageView sms_next_img;

    private TextView recovery_calllog_ing;
    private ProgressBar calllog_progress_bar;
    private ImageView calllog_next_page;

    private TextView recovery_app_ing;
    private ProgressBar app_progress_bar;
    private ImageView app_nextpage_img;
    private ProgressDialog mGetNumProgressDialog;

    private RelativeLayout data_size_layout;
    private TextView text_size;
    private TextView text_unit;

    private ImageView img_contact;
    private ImageView img_calllog;
    private ImageView img_sms;
    private ImageView img_app;

    private NotificationManagerCompat mNotificationManager;

    public static final int NOTIFICATION_ID = Integer.MAX_VALUE - 100;

    private String need_recovery_size;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SystemUtil.setStatusBarColor(this, R.color.background_fafafa);


        setMstContentView(R.layout.onekey_recovery_activity);
        getToolbar().setTitle(R.string.onekeyrecovery);
        initView();
        CheckIsQQSDKLogin();
        mNotificationManager = NotificationManagerCompat.from(this);
        checkCheckBoxChickAble();
    }

    private void initView() {

        img_contact = (ImageView) findViewById(R.id.img_contact);
        img_calllog = (ImageView) findViewById(R.id.img_calllog);
        img_sms = (ImageView) findViewById(R.id.img_sms);
        img_app = (ImageView) findViewById(R.id.img_app);

        data_size_layout = (RelativeLayout) findViewById(R.id.data_size_layout);
        text_size = (TextView) findViewById(R.id.text_size);
        text_unit = (TextView) findViewById(R.id.text_unit);

        img_bg = (ImageView) findViewById(R.id.img_bg);
        text_no_data = (TextView) findViewById(R.id.text_no_data);
        text_connect = (TextView) findViewById(R.id.text_connect);
        text_connect.setTextColor(Color.argb(255 * 40 / 100, 86, 90, 100));
        text_start_revovery = (TextView) findViewById(R.id.text_start_revovery);
        start_recovery_layout = (RelativeLayout) findViewById(R.id.start_recovery_layout);
        contact_layout = (RelativeLayout) findViewById(R.id.contact_layout);
        contact_info = (TextView) findViewById(R.id.contact_info);
        sms_layout = (RelativeLayout) findViewById(R.id.sms_layout);
        sms_info = (TextView) findViewById(R.id.sms_info);
        calllog_layout = (RelativeLayout) findViewById(R.id.calllog_layout);
        calllog_info = (TextView) findViewById(R.id.calllog_info);
        app_layout = (RelativeLayout) findViewById(R.id.app_layout);
        app_info = (TextView) findViewById(R.id.app_info);
        start_recovery_layout.setAlpha(0.3f);

        contact_layout.setOnClickListener(this);
        calllog_layout.setOnClickListener(this);
        sms_layout.setOnClickListener(this);
        app_layout.setOnClickListener(this);
        start_recovery_layout.setOnClickListener(this);

        check_contact = (CheckBox) findViewById(R.id.check_contact);
        check_sms = (CheckBox) findViewById(R.id.check_sms);
        check_calllog = (CheckBox) findViewById(R.id.check_calllog);
        check_app = (CheckBox) findViewById(R.id.check_app);

        check_contact.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkEnableStartRecovery();
            }
        });
        check_sms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkEnableStartRecovery();
            }
        });
        check_calllog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkEnableStartRecovery();
            }
        });
        check_app.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkEnableStartRecovery();
            }
        });

        recovery_contact_ing = (TextView) findViewById(R.id.recovery_contact_ing);
        contact_progress_bar = (ProgressBar) findViewById(R.id.contact_progress_bar);
        contact_next_img = (ImageView) findViewById(R.id.contact_next_img);

        recovery_sms_ing = (TextView) findViewById(R.id.recovery_sms_ing);
        sms_progress_bar = (ProgressBar) findViewById(R.id.sms_progress_bar);
        sms_next_img = (ImageView) findViewById(R.id.sms_next_img);

        recovery_calllog_ing = (TextView) findViewById(R.id.recovery_calllog_ing);
        calllog_progress_bar = (ProgressBar) findViewById(R.id.calllog_progress_bar);
        calllog_next_page = (ImageView) findViewById(R.id.calllog_next_page);

        recovery_app_ing = (TextView) findViewById(R.id.recovery_app_ing);
        app_progress_bar = (ProgressBar) findViewById(R.id.app_progress_bar);
        app_nextpage_img = (ImageView) findViewById(R.id.app_nextpage_img);
        setEnableStartRecorvery(false);
        contact_next_img.setVisibility(View.GONE);
        calllog_next_page.setVisibility(View.GONE);
    }


    private void updateQQLogin() {//重新授权

        Intent i = new Intent(OneKeyRecoveryActiviy.this, ShowImportQQDataActivity.class);
        i.putExtra("is_should_return", true);
        i.putExtra("title", getString(R.string.onekeyrecovery));
        startActivityForResult(i, SystemUtil.REQUEST_RELOGIN_QQ);
    }


    private void checkEnableStartRecovery() {
        boolean is = false;
        if (check_contact.isChecked()) {
            is = true;
        } else if (check_sms.isChecked() && sms_time_type > 0) {
            is = true;
        } else if (check_calllog.isChecked()) {
            is = true;
        } else if (check_app.isChecked() && (recoveryAppList != null && recoveryAppList.size() > 0)) {
            is = true;
        }
        setEnableStartRecorvery(is);
    }


    @Override
    public void onClick(View view) {

        int id = view.getId();
        switch (id) {
            case R.id.contact_layout:
                check_contact.setChecked(!check_contact.isChecked());
                break;
            case R.id.sms_layout:
                chooseSMS();
                break;
            case R.id.calllog_layout:
                check_calllog.setChecked(!check_calllog.isChecked());
                break;
            case R.id.app_layout:
                if (text_start_revovery.getText().equals(getString(R.string.revocery_cancle))) {
                    openDownloadManager();
                } else if (text_start_revovery.getText().equals(getString(R.string.start_recovery))) {
                    chooseAPP();
                }
                break;
            case R.id.start_recovery_layout:
                if (text_start_revovery.getText().equals(getString(R.string.revocery_cancle))) {
                    showCancleDialog();

                } else if (text_start_revovery.getText().equals(getString(R.string.start_recovery))) {

                    if (!SystemUtil.hasNetwork()) {
                        showNoNetDialog();
                        return;
                    }

                    if(SystemUtil.getConnectingType(this) != Constant.NETWORK_WIFI &&check_app.isChecked() ){
                        showUseNetDialog();
                    } else {
                        checkRecovery();
                        startRecovery();
                    }

                }
                break;
        }

    }

    public void showUseNetDialog(){
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.use_traffic_title)
                .setPositiveButton(com.mst.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkRecovery();
                        startRecovery();
                    }
                }).setNegativeButton(com.mst.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).setMessage(R.string.use_traffic_msg).create();
        alertDialog.show();

    }


    private boolean isCancled = false;

    private void showCancleDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.warning_title)
                .setPositiveButton(R.string.revocery_cancle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isCancled = true;
                        is_shouled_recovery_contact = false;
                        is_shouled_recovery_sms = false;
                        is_should_recovery_calllog = false;
                        is_should_recovery_app = false;
                        finish();

                    }
                }).setNegativeButton(R.string.revocery_goon, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setMessage(R.string.revocery_cancle_warning).create();
        alertDialog.show();

    }


    private boolean is_shouled_recovery_contact = false;
    private boolean is_shouled_recovery_sms = false;
    private boolean is_should_recovery_calllog = false;
    private boolean is_should_recovery_app = false;


    private void checkRecovery() {
        if (check_contact.isChecked()) {

            recovery_contact_ing.setVisibility(View.VISIBLE);
            contact_info.setVisibility(View.GONE);
            contact_progress_bar.setVisibility(View.VISIBLE);
            contact_next_img.setVisibility(View.GONE);
            is_shouled_recovery_contact = true;

            String revocery_waitting = getString(R.string.revocery_waitting);
            int count = localContactNum + cloudContactNum;
            recovery_contact_ing.setText(String.format(revocery_waitting, count));
        } else {
            contact_layout.setVisibility(View.GONE);
        }

        if ((sms_time_type > 0 || phone_num_list != null) && check_sms.isChecked()) {
            recovery_sms_ing.setVisibility(View.VISIBLE);
            sms_info.setVisibility(View.GONE);
            sms_progress_bar.setVisibility(View.VISIBLE);
            sms_next_img.setVisibility(View.GONE);
            is_shouled_recovery_sms = true;

            String revocery_waitting = getString(R.string.revocery_waitting);
            recovery_sms_ing.setText(String.format(revocery_waitting, recovery_sms_count));

        } else {
            sms_layout.setVisibility(View.GONE);
        }

        if (check_calllog.isChecked()) {
            is_should_recovery_calllog = true;

            recovery_calllog_ing.setVisibility(View.VISIBLE);
            calllog_info.setVisibility(View.GONE);
            calllog_progress_bar.setVisibility(View.VISIBLE);
            contact_next_img.setVisibility(View.GONE);

            String revocery_waitting = getString(R.string.revocery_waitting);
            recovery_calllog_ing.setText(String.format(revocery_waitting, cloudCalllogNum));

        } else {
            calllog_layout.setVisibility(View.GONE);
        }

        if (check_app.isChecked()) {
            is_should_recovery_app = true;
        } else {
            app_layout.setVisibility(View.GONE);
        }

    }


    private void startRecovery() {


        if (is_shouled_recovery_contact || is_shouled_recovery_sms || is_should_recovery_calllog) {
            text_start_revovery.setText(R.string.revocery_cancle);
            text_connect.setText(R.string.recoverying);

            check_contact.setVisibility(View.GONE);
            check_calllog.setVisibility(View.GONE);
            check_sms.setVisibility(View.GONE);
            check_app.setVisibility(View.GONE);
            img_contact.setVisibility(View.VISIBLE);
            img_calllog.setVisibility(View.VISIBLE);
            img_sms.setVisibility(View.VISIBLE);
            img_app.setVisibility(View.VISIBLE);

            contact_layout.setEnabled(false);
            calllog_layout.setEnabled(false);
            sms_layout.setEnabled(false);
            //app_layout.setEnabled(false);

        }

        if (is_shouled_recovery_contact) {
            RecoveryCContact();
        } else if (is_should_recovery_calllog) {
            RecoveryCalllog();
        } else if (is_shouled_recovery_sms) {
            RecoverySMS();
        } else if (is_should_recovery_app) {
            RecoveryAPP();

            Intent i = new Intent();
            i.setClass(OneKeyRecoveryActiviy.this, RecoveryFinishActivity.class);
            i.putExtra(RecoveryFinishActivity.OPEN_TYPE, RecoveryFinishActivity.TYPE_ONLY_APP);
            isCancled = true;
            startActivity(i);

            finish();
        }
    }

    public void showNoNetDialog() {

        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.no_network)
                .setPositiveButton(com.mst.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(Settings.ACTION_SETTINGS);
                        startActivity(intent);
                        isCancled = true;
                        is_shouled_recovery_contact = false;
                        is_shouled_recovery_sms = false;
                        is_should_recovery_calllog = false;
                        finish();

                    }
                }).setNegativeButton(com.mst.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isCancled = true;
                        is_shouled_recovery_contact = false;
                        is_shouled_recovery_sms = false;
                        is_should_recovery_calllog = false;
                        finish();
                    }
                }).setMessage(R.string.connect_network).create();
        alertDialog.show();


    }

    private void RecoveryCalllog() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                new SyncCallLog(OneKeyRecoveryActiviy.this, callLogObsv).backup();
            }
        }).start();

    }

    private void resultRecoveryApp(Intent data) {
        if (data == null) {
            return;
        }
        ArrayList<AppDownloadData> tempList;
        tempList = data.getParcelableArrayListExtra(AppRecoveryActivity.RESULT_RECOVERY_APP_LIST_KEY);
        if (tempList != null && tempList.size() > 0) {
            recoveryAppList = new ArrayList<AppDownloadData>();
            recoveryAppList.addAll(tempList);

            check_app.setChecked(true);

            long totalSize = 0;
            for (AppDownloadData downloadData : recoveryAppList) {
                totalSize += downloadData.getShowAppSize();
            }
            String text = getString(R.string.app_recovery_count_and_size,
                    recoveryAppList.size(), SystemUtil.bytes2kb(totalSize));
            app_info.setText(text);
            app_size = totalSize;
            check_app.setChecked(true);
            checkEnableStartRecovery();
        }
    }

    private void RecoveryAPP() {
        LogUtil.i("OneKeyRecoveryActivity", "RecoveryAPP call!");

        if (recoveryAppList != null && recoveryAppList.size() > 0) {
            Intent sIntent = new Intent();
            sIntent.setComponent(new ComponentName("com.monster.market",
                    "com.monster.market.download.AppDownloadService"));
            Bundle startDownloadBundle = new Bundle();
            startDownloadBundle.putInt("download_operation", 109);

            startDownloadBundle.putParcelableArrayList("download_data_list", recoveryAppList);

            sIntent.putExtras(startDownloadBundle);
            startService(sIntent);
        }

    }

    private void openDownloadManager() {
        Intent managerIntent = new Intent("com.monster.market.downloadmanager");
        startActivity(managerIntent);
    }

    private void chooseAPP() {
        Intent i = new Intent();
        i.setClass(this, AppRecoveryActivity.class);
        i.putExtra(AppRecoveryActivity.START_TYPE, AppRecoveryActivity.TYPE_CHOOSE_APP);
        startActivityForResult(i, CHOOSE_APP_REQUEST);
    }

    private void chooseSMS() {
        Intent i = new Intent();
        i.setClass(this, SmsChooseSyncTypeActivity.class);
        i.putExtra("type", 1);
        ////1 一个月 2 三个月 3 半年 4 一年 5全部
        if (sms_time_type > 0) {
            i.putExtra("time_type", sms_time_type);
        } else {
            i.putExtra("time_type", 1);
        }
        startActivityForResult(i, CHOOSE_SMS_REQUEST);
    }

    public void setEnableStartRecorvery(boolean is) {
        start_recovery_layout.setEnabled(is);
        if (is) {
            start_recovery_layout.setAlpha(1.0f);
            data_size_layout.setVisibility(View.VISIBLE);
            text_no_data.setVisibility(View.GONE);
            float count_all = getContactSize() + getSMSSize() + getCalllogSize() + getAPPSize();
//            float count_show = sizeShow(count_all);
//            java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
//
            String size = sizeShow(count_all);
            String unit = getUnit(count_all);

            text_size.setText(size);
            text_unit.setText(unit);
//            text_connect.setText(R.string.comput_time);
            img_bg.setImageResource(R.drawable.data_bg);
            need_recovery_size = size + unit;

        } else {
            start_recovery_layout.setAlpha(0.3f);
            data_size_layout.setVisibility(View.GONE);
            text_no_data.setVisibility(View.VISIBLE);
            text_connect.setText(R.string.connect_sucess);
            img_bg.setImageResource(R.drawable.no_data_bg);
            need_recovery_size = "";
        }
    }

    public String getLeftSize() {
        float count_all = 0.0f;

        if (is_shouled_recovery_contact) {
            count_all += getContactSize();
        }

        if (is_shouled_recovery_sms) {
            count_all += getSMSSize();
        }

        if (is_should_recovery_calllog) {
            count_all += getCalllogSize();
        }

        if (is_should_recovery_app) {
            count_all += getAPPSize();
        }


//        float count_show = sizeShow(count_all);
//        java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
        return sizeShow(count_all) + getUnit(count_all);
    }

    private String sizeShow(float size) {

        BigDecimal fileSize = new BigDecimal(size);

        if (size < 1000) {//kb
            return getNumFormat(size, 3);
        } else if (size >= 1000 && size < 1000 * 1024) {//Mb
            BigDecimal kb = new BigDecimal(1024);
            float returnValue = fileSize.divide(kb, 2, BigDecimal.ROUND_UP)
                    .floatValue();

            return getNumFormat(returnValue, 3);
        } else {
            BigDecimal mb = new BigDecimal(1024 * 1024);//Gb
            float returnValue = fileSize.divide(mb, 2, BigDecimal.ROUND_UP)
                    .floatValue();

            return getNumFormat(returnValue, 3);
        }

    }

    private String getNumFormat(float data, int len) {//len 最大长度 不是小数点位数
        String str = "" + data;
        if (str.length() <= len) {
            String new_str = str;
            if (new_str.endsWith(".0")) {
                new_str = new_str.replace(".0", "");
            } else if (new_str.endsWith(".")) {
                new_str = new_str.replace(".", "");
            }
            return new_str;
        }

        float off_set = 0f;


        if (data < 10) {// KB MB保留2位小数 GB 3位 1000以上的会自动变成0.xx

            if (len == 3) {
                off_set = 0.005f;
            } else {
                off_set = 0.0005f;
            }

        } else if (data >= 10 && data < 100) {//10~100 // KB MB保留1位小数 GB 2位
            if (len == 3) {
                off_set = 0.05f;
            } else {
                off_set = 0.005f;
            }

        } else {//100~1000 // KB MB不保留小数 GB 1位
            if (len == 3) {
                off_set = 0.5f;
            } else {
                off_set = 0.05f;
            }

        }

        float new_data = data + off_set;
        String new_str = "" + new_data;
        if (new_str.contains(".")) {
            new_str = new_str.substring(0, len + 1);
        } else {
            new_str = new_str.substring(0, len);
        }

        if (new_str.endsWith(".0")) {
            new_str = new_str.replace(".0", "");
        } else if (new_str.endsWith(".")) {
            new_str = new_str.replace(".", "");
        }

        return new_str;
    }


    private String getUnit(float size) {
        if (size < 1000) {
            return "KB";
        } else if (size >= 1000 && size < 1000 * 1024) {
            return "MB";
        } else {
            return "GB";
        }
    }

    private float getContactSize() {

        if (!check_contact.isChecked()) {
            return 0.0f;
        }

        return 1.0f * (cloudContactNum + localContactNum);
    }

    private float getSMSSize() {

        if (!check_sms.isChecked()) {
            return 0.0f;
        }

        return recovery_sms_count * 2.0f;
    }

    private float getCalllogSize() {

        if (!check_calllog.isChecked()) {
            return 0.0f;
        }

        return 1.0f * cloudCalllogNum;

    }

    private long app_size = 0l;

    private float getAPPSize() {

        if (!check_app.isChecked()) {
            return 0.0f;
        }

        BigDecimal fileSize = new BigDecimal(app_size);
        BigDecimal kilobyte = new BigDecimal(1024);
        float returnValue = fileSize.divide(kilobyte, 2, BigDecimal.ROUND_UP)
                .floatValue();
        return returnValue;
    }

    public static final int CHOOSE_CONTACT_REQUEST = 100;
    public static final int CHOOSE_SMS_REQUEST = 101;
    public static final int CHOOSE_CALLLOG_REQUEST = 102;
    public static final int CHOOSE_APP_REQUEST = 103;

    private int sms_time_type = 0;//短信恢复时间类型 1个月 3个月 3个月....
    private ArrayList<AppDownloadData> recoveryAppList;

    private int recovery_sms_count = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CHOOSE_CONTACT_REQUEST:
                break;
            case CHOOSE_SMS_REQUEST:
                resultSMS(data);
                break;
            case CHOOSE_CALLLOG_REQUEST:
                break;
            case CHOOSE_APP_REQUEST:
                if (resultCode == RESULT_OK) {
                    resultRecoveryApp(data);
                }
                break;
            case SystemUtil.REQUEST_RELOGIN_QQ://重新授权

                if (resultCode == RESULT_OK) {
                    showProgressDialog();
                    QQSDKLogin();
                } else {
                    finish();
                }
                break;
        }
        checkEnableStartRecovery();

        checkCheckBoxChickAble();
    }


    private void checkCheckBoxChickAble() {
        if (sms_time_type > 0 || phone_num_list != null) {
            check_sms.setClickable(true);
        } else {
            check_sms.setClickable(false);
        }
        if (app_size > 0) {
            check_app.setClickable(true);
        } else {
            check_app.setClickable(false);
        }
    }


    private void RecoveryCContact() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                new SyncContact(OneKeyRecoveryActiviy.this, contactObsv).merge();
            }
        }).start();

    }

    private ArrayList<String> phone_num_list;

    private void resultSMS(Intent data) {

        if (data == null) {
            return;
        }

        check_sms.setChecked(true);

        boolean is_sync_by_time = data.getBooleanExtra("is_sync_by_time", false);

        recovery_sms_count = data.getIntExtra("count", 0);

        if (is_sync_by_time) {
            sms_time_type = data.getIntExtra("time_type", -1);//1 一个月 2 三个月 3 半年 4 一年 5全部
            phone_num_list = null;
            String str_sms_info = "";
            switch (sms_time_type) {
                case 1:
                    str_sms_info = String.format(getString(R.string.last_month_info), recovery_sms_count);
                    break;
                case 2:
                    str_sms_info = String.format(getString(R.string.last_three_month_info), recovery_sms_count);
                    break;
                case 3:
                    str_sms_info = String.format(getString(R.string.last_half_a_year_info), recovery_sms_count);
                    break;
                case 4:
                    str_sms_info = String.format(getString(R.string.last_year_info), recovery_sms_count);
                    break;
                case 5:
                    str_sms_info = String.format(getString(R.string.all_info), recovery_sms_count);
                    break;
            }
            sms_info.setText(str_sms_info);
        } else {
            sms_time_type = 0;
            int contact_count = data.getIntExtra("contact_count", 0);
            sms_info.setText(String.format(getString(R.string.str_select_sms_by_contact_info), contact_count, recovery_sms_count));
            phone_num_list = (ArrayList<String>) data.getStringArrayListExtra("phone_num_list");
        }
    }

    private void RecoverySMS() {//恢复短信

        setDefaultMsmApp();//恢复的时候设置默认短信app
        switch (sms_time_type) {
            case 1:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new SyncSMS(OneKeyRecoveryActiviy.this, smsObsv, SmsTimeType.TIME_ONE_MONTH).backup();
                    }
                }).start();

                break;
            case 2:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new SyncSMS(OneKeyRecoveryActiviy.this, smsObsv, SmsTimeType.TIME_THREE_MONTH).backup();
                    }
                }).start();

                break;
            case 3:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new SyncSMS(OneKeyRecoveryActiviy.this, smsObsv, SmsTimeType.TIME_SIX_MONTH).backup();
                    }
                }).start();

                break;
            case 4:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new SyncSMS(OneKeyRecoveryActiviy.this, smsObsv, SmsTimeType.TIME_ONE_YEAR).backup();
                    }
                }).start();

                break;
            case 5:
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        new SyncSMS(OneKeyRecoveryActiviy.this, smsObsv, SmsTimeType.TIME_ALL).backup();

                    }
                }).start();
                break;
        }
        if (phone_num_list != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    new SyncSMS(OneKeyRecoveryActiviy.this, smsObsv, phone_num_list).backupByPhoneNums();
                }
            }).start();

        }

    }

    public void setDefaultMsmApp() {
//        startActivity(getRequestDefaultSmsAppActivity());
        try {
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService("appops");
            appOpsManager.setMode(15, android.os.Process.myUid(), getPackageName(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    public static Intent getRequestDefaultSmsAppActivity() {
//        final Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
//        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, "com.monster.cloud");
//        return intent;
//    }

    private ISyncProcessorObsv smsObsv = new ISyncProcessorObsv() {

        @Override
        public void onSyncStateChanged(PMessage msg) {
            Message message = mSyncHandler.obtainMessage();
            message.what = 2;
            message.obj = msg;
            mSyncHandler.sendMessage(message);
        }

        @Override
        public void onLoginkeyExpired() {

        }
    };
    private ISyncProcessorObsv contactObsv = new ISyncProcessorObsv() {

        @Override
        public void onSyncStateChanged(PMessage msg) {
            Message message = mSyncHandler.obtainMessage();
            message.what = 1;
            message.obj = msg;
            mSyncHandler.sendMessage(message);
        }

        @Override
        public void onLoginkeyExpired() {

        }
    };

    private ISyncProcessorObsv callLogObsv = new ISyncProcessorObsv() {

        @Override
        public void onSyncStateChanged(PMessage msg) {
            Message message = mSyncHandler.obtainMessage();
            message.what = 3;
            message.obj = msg;
            mSyncHandler.sendMessage(message);
        }

        @Override
        public void onLoginkeyExpired() {

        }
    };

    private Handler mSyncHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            uiProgressChanged((PMessage) msg.obj, msg.what);
        }
    };

    /**
     * UI进度条变化
     *
     * @param msg
     */
    private void uiProgressChanged(PMessage msg, int type) {//1联系人 2短信 3通话记录

        Log.i("zouxu", "uiProgressChanged!!!! msg.msgId=" + msg.msgId);

        switch (msg.msgId) {
            case ISyncMsgDef.ESTATE_SYNC_ALL_BEGIN:
                //同步开始（全部任务）
                break;
            case ISyncMsgDef.ESTATE_SYNC_SCAN_BEGIN:
                //数据库扫描开始
                break;
            case ISyncMsgDef.ESTATE_SYNC_SCAN_FINISHED:
                //数据库扫描结束
                break;
            case ISyncMsgDef.ESTATE_SYNC_PROGRESS_CHANGED:
                //同步进度变化
                int progress = msg.arg1;
                String recovery_ing = getString(R.string.revocery_ing);
                if (type == 1) {//联系人
                    contact_progress_bar.setProgress(progress);
                    recovery_contact_ing.setText(String.format(recovery_ing, progress) + "%");
                } else if (type == 2) {//短信
                    sms_progress_bar.setProgress(progress);
                    recovery_sms_ing.setText(String.format(recovery_ing, progress) + "%");
                } else if (type == 3) {//通话记录
                    calllog_progress_bar.setProgress(progress);
                    recovery_calllog_ing.setText(String.format(recovery_ing, progress) + "%");
                }
                break;
            case ISyncMsgDef.ESTATE_SYNC_DATA_REARRANGEMENT_BEGIN:
                //数据同步完成，数据整理开始
                break;
            case ISyncMsgDef.ESTATE_SYNC_DATA_REARRANGEMENT_FINISHED:
                //数据同步完成，数据整理完成
                break;
            case ISyncMsgDef.ESTATE_SYNC_ALL_FINISHED:
                //同步结束（全部）
//                syncAllFinished(msg);

                if (type == 1) {
                    is_shouled_recovery_contact = false;
//                    contact_layout.setVisibility(View.GONE);
                    startMovingAnim(contact_layout, msg);
                } else if (type == 2) {
                    is_shouled_recovery_sms = false;
//                    sms_layout.setVisibility(View.GONE);
                    startMovingAnim(sms_layout, msg);
                } else if (type == 3) {
                    is_should_recovery_calllog = false;
                    startMovingAnim(calllog_layout, msg);
                }


                if (isAllFinish() && !isCancled) {
                    syncAllFinished(msg);
                    Toast.makeText(OneKeyRecoveryActiviy.this, "同步完成", Toast.LENGTH_SHORT).show();
                    if (!isRunInBackground) {

                        if (is_should_recovery_app) {
                            RecoveryAPP();

                            Intent i = new Intent();
                            i.setClass(OneKeyRecoveryActiviy.this, RecoveryFinishActivity.class);
                            i.putExtra(RecoveryFinishActivity.OPEN_TYPE, RecoveryFinishActivity.TYPE_CONTAINER_APP);
                            isCancled = true;
                            startActivity(i);
                        } else {
                            Intent i = new Intent();
                            i.setClass(OneKeyRecoveryActiviy.this, RecoveryFinishActivity.class);
                            isCancled = true;
                            startActivity(i);
                        }
                    }
                    finish();
                } else {
                    startRecovery();
                }

                showNotif();

                break;
            default:
                break;
        }
    }

    private boolean isAllFinish() {
        return !is_shouled_recovery_contact && !is_shouled_recovery_sms && !is_should_recovery_calllog;
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
//                    Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
//                    showSyncSuccessDialog();
//                    getSMSCount();
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

    public void CheckIsQQSDKLogin() {
        if (LoginUtil.getLoginLabel(this)) {
            getCloudNum(true);
        } else {
            showProgressDialog();
            QQSDKLogin();
        }
    }

    //QQ sdk login
    private ILoginMgr loginMgr;
    private String APPID = "101181845";
    private Tencent tencent;
    private String openId;
    private String accessToken;

    private void QQSDKLogin() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContactAccountFilterAndGetTimeReminder.getContactAccountFilterSync();
                StatisticsFactory.getStatisticsUtil().checkContactAggregationNeeded();
            }
        }).start();
        loginMgr = LoginMgrFactory.getLoginMgr(this, AccountUtils.ACCOUNT_TYPE_QQ_SDK);
        tencent = Tencent.createInstance(APPID, getApplicationContext());
        openId = LoginUtil.getOpenId(this);
        accessToken = LoginUtil.getToken(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                int result = loginMgr.qqSDKlogin(openId, accessToken, APPID);
                if (result == IAccountDef.EM_LOGIN_RES_LOGIN_OK) {
                    LoginUtil.updateLoginLabel(OneKeyRecoveryActiviy.this, true);
                } else if (result == 5001) {
                    LoginUtil.updateLoginLabel(OneKeyRecoveryActiviy.this, false);
                } else if (result == IAccountDef.EM_LOGIN_RES_FAIL) {
                    // TODO 登陆失败
                } else if (result == IAccountDef.EM_LOGIN_RES_NETWORK_FAIL) {
                    // TODO 网络异常
                }
                mQQSDKLoginHandler.sendEmptyMessage(result);
            }
        }).start();

    }

    private Handler mQQSDKLoginHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {//登陆成功
                getCloudNum(false);
            } else {
                mGetNumProgressDialog.dismiss();
                updateQQLogin();
            }
        }
    };


    private int cloudSMSNum = 0;
    private int cloudCalllogNum = 0;
    private int cloudAppNum = 0;

    private void getCloudNum(boolean showDialog) {

        if (showDialog) {
            showProgressDialog();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
//                GetRecordNumProcessor p = new GetRecordNumProcessor(numObs);
//                p.getRecordNumOfContact();
                cloudContactNum = GetCountUtils.getRecordNumOfContact();
                cloudSMSNum = GetCountUtils.getRecordNumOfSMS();
                cloudCalllogNum = GetCountUtils.getRecordNumOfCalllog();
                cloudAppNum = GetCountUtils.getSoftlistCount();
                num_handler.sendEmptyMessage(0);
            }
        }).start();

    }


    public void showProgressDialog() {
        mGetNumProgressDialog = new ProgressDialog(this);
        mGetNumProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mGetNumProgressDialog.setMessage(getString(R.string.sync_ing));
        mGetNumProgressDialog.show();
    }

//    private IGetRecordNumObserver numObs = new IGetRecordNumObserver() {
//        @Override
//        public void getRecordNumFinished(Message message) {
//            num_handler.sendMessage(message);
//        }
//    };

    private int cloudContactNum = 0;
    private int localContactNum = 0;


    private Handler num_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            mGetNumProgressDialog.dismiss();


            IStatisticsUtil mIStatisticsUtil = StatisticsFactory.getStatisticsUtil();
            localContactNum = mIStatisticsUtil.getLocalContactNum(OneKeyRecoveryActiviy.this);


            if (localContactNum == 0 && cloudContactNum == 0 && cloudCalllogNum == 0 && cloudSMSNum == 0 && cloudAppNum == 0) {
                Intent i = new Intent();
                i.setClass(OneKeyRecoveryActiviy.this, CloudNoDataActivity.class);
                startActivity(i);
                finish();
                return;
            }

            String contact_num_info = getString(R.string.recovery_contact_merge);
            contact_info.setText(String.format(contact_num_info, localContactNum, cloudContactNum));

            if (localContactNum == 0 && cloudContactNum == 0) {
                contact_layout.setVisibility(View.GONE);
            }
            String total_count = getString(R.string.str_all_count);

            calllog_info.setText(String.format(total_count, cloudCalllogNum));

            if (cloudCalllogNum == 0) {
                calllog_layout.setVisibility(View.GONE);
            }

            if (cloudSMSNum == 0) {
                sms_layout.setVisibility(View.GONE);
            }

            if (cloudAppNum == 0) {
                app_layout.setVisibility(View.GONE);
            }


        }
    };


    private void startMovingAnim(final View view, final PMessage msg) {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();

        AnimationSet mSet = new AnimationSet(true);

        mSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        TranslateAnimation animation = new TranslateAnimation(0, width, 0, 0);
        AlphaAnimation ala_anim = new AlphaAnimation(1, 0);
        mSet.addAnimation(ala_anim);
        mSet.addAnimation(animation);
        mSet.setDuration(500);


        view.setAnimation(mSet);
        mSet.startNow();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && text_start_revovery.getText().equals(getString(R.string.revocery_cancle))) {
            showCancleDialog();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onNavigationClicked(View view) {
        if (text_start_revovery.getText().equals(getString(R.string.revocery_cancle))) {
            showCancleDialog();
            return;
        }

        finish();
    }

    private boolean isRunInBackground = false;

    @Override
    protected void onPause() {
        isRunInBackground = true;
        showNotif();
        super.onPause();
    }

    @Override
    protected void onResume() {
        isRunInBackground = false;
        mNotificationManager.cancel(NOTIFICATION_ID);
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, mFilter);

        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mReceiver);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = connectivityManager.getActiveNetworkInfo();
                if (info != null && info.isAvailable()) {
//                    String name = info.getTypeName();
//                    Log.d("mark", "当前网络名称：" + name);
                } else {
                    if (text_start_revovery.getText().equals(getString(R.string.revocery_cancle))) {
                        showNoNetDialog();
                    }

                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public void showNotif() {

        if (!isRunInBackground || isCancled || !text_start_revovery.getText().equals(getString(R.string.revocery_cancle))) {
            return;
        }


        if (!isAllFinish()) {
            Intent intent = new Intent(this, OneKeyRecoveryActiviy.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            String str_content = String.format(getString(R.string.str_left), getLeftSize());
            Notification notification = new NotificationCompat.Builder(this)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setContentTitle(getString(R.string.onekeyrecovery))
                    .setContentText(str_content)
                    .setWhen(System.currentTimeMillis())
                    .setOngoing(true)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.cloud_icon)
                    .build();
            mNotificationManager.notify(NOTIFICATION_ID, notification);
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            String str_content = String.format(getString(R.string.str_recovery_size), need_recovery_size);
            Notification notification = new NotificationCompat.Builder(this)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setContentTitle(getString(R.string.onekeyrecovery))
                    .setContentText(str_content)
                    .setWhen(System.currentTimeMillis())
                    .setOngoing(false)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.cloud_icon)
                    .build();
            mNotificationManager.notify(NOTIFICATION_ID, notification);
        }

    }
}
