/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.setupwizard.ui;

import android.app.Dialog; // MODIFIED by xinlei.sheng, 2016-09-12,BUG-2669930
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface; // MODIFIED by xinlei.sheng, 2016-10-13,BUG-2669930
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.os.Parcelable;
import android.os.Bundle;
import android.os.Handler;
/* MODIFIED-BEGIN by xinlei.sheng, 2016-11-04,BUG-2669930*/
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
/* MODIFIED-END by xinlei.sheng,BUG-2669930*/
import android.text.TextUtils;
import android.view.View;
/* MODIFIED-BEGIN by xinlei.sheng, 2016-09-21,BUG-2669930*/
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
/* MODIFIED-END by xinlei.sheng,BUG-2669930*/

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
/* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
import java.util.HashMap;

import cn.tcl.setupwizard.R;
import cn.tcl.setupwizard.adapter.CustomerWifiInfo;
import cn.tcl.setupwizard.adapter.WifiListAdapter;
import cn.tcl.setupwizard.utils.LogUtils;
import cn.tcl.setupwizard.utils.SystemBarHelper;
import cn.tcl.setupwizard.utils.WifiConnector;
import cn.tcl.setupwizard.utils.WifiSearcher;
import cn.tcl.setupwizard.utils.WifiUtils;
/* MODIFIED-BEGIN by xinlei.sheng, 2016-10-13,BUG-2669930*/

public class WifiSetActivity extends BaseActivity
/* MODIFIED-END by xinlei.sheng,BUG-2669930*/
        implements View.OnClickListener, DialogInterface.OnClickListener {
        /* MODIFIED-END by xinlei.sheng,BUG-2669930*/

    public final static String TAG = "WifiSetActivity";
    private RecyclerView mListView;
    private ArrayList<CustomerWifiInfo> mWifiInfos;
    private WifiListAdapter mAdapter;
    private WifiUtils mWifiUtils;
    private WifiPwdDialog mWifiPwdDialog;
    private Context mContext;
    /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
    private String mSelectSsid, mPreSsid;

    private boolean mIsRefreshWifi;

    private static final int MSG_WIFI_SEARCH_SUCCESS = 10;
    private static final int MSG_WIFI_SEARCH_FAILED = 11;
    private static final int MSG_WIFI_CONNECTED_SUCCESS = 12;
    private static final int MSG_WIFI_CONNECTED_FAILED = 13;
    private static final int MSG_WIFI_REFRESH = 14;

    private static final int WIFI_PASSWORD_DIALOG = 1; // MODIFIED by xinlei.sheng, 2016-10-13,BUG-2669930

    private WifiSearcher mWifiSearcher;
    private WifiConnector mWifiConnector;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_WIFI_SEARCH_SUCCESS:
                /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
                    LogUtils.i(TAG, "load wifi list finish");
                    mListView.setVisibility(View.VISIBLE); // MODIFIED by xinlei.sheng, 2016-09-30,BUG-2669930
                    findViewById(R.id.wifi_load_progress).setVisibility(View.GONE);
                    mWifiInfos.clear();
                    mWifiInfos.addAll(mWifiUtils.getWifiInfos().values());
                    sortWifiList();
                    /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
                    mAdapter.setSelectIndex(0);
                    mAdapter.notifyDataSetChanged();
                    mListView.scrollToPosition(mAdapter.getSelectIndex()); // MODIFIED by xinlei.sheng, 2016-11-04,BUG-2669930
                    break;

                case MSG_WIFI_SEARCH_FAILED:
                    mListView.setVisibility(View.VISIBLE); // MODIFIED by xinlei.sheng, 2016-09-30,BUG-2669930
                    findViewById(R.id.wifi_load_progress).setVisibility(View.GONE);
                    break;

                case MSG_WIFI_CONNECTED_SUCCESS:
                    LogUtils.i(TAG, "Connected successfully: " + msg.obj);
                    if (mWifiSearcher.isSearching()) {
                        mWifiSearcher.stopSearch();
                    }
                    mWifiSearcher.search();
                    break;

                case MSG_WIFI_CONNECTED_FAILED:
                    LogUtils.i(TAG, "Connected failed: " + msg.obj);
                    //if (mWifiSearcher.isSearching()) {
                    //    mWifiSearcher.stopSearch();
                    //}
                    //mWifiSearcher.search();
                    if (!WifiUtils.isWifiConnected(mContext)) {
                        updateWifiInfoState(mSelectSsid, 1);
                        mAdapter.notifyDataSetChanged();
                        CustomerWifiInfo selectWifiInfo = getCustomerWifiInfo(mSelectSsid);
                        if (selectWifiInfo != null && !isDestroyed()) { // MODIFIED by xinlei.sheng, 2016-09-30,BUG-2669930
                            if (WifiUtils.getSecurity(selectWifiInfo.getSecurity()) > 0) {
                                /* MODIFIED-BEGIN by xinlei.sheng, 2016-10-13,BUG-2669930*/
                                mWifiPwdDialog.setSsid(mSelectSsid);
                                mWifiPwdDialog.setPassword("");
                                mWifiPwdDialog.setHint(getString(R.string.wifi_dialog_hint2));
                                showDialog(WIFI_PASSWORD_DIALOG);
                                /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
                            }
                        }
                    }
                    break;

                case MSG_WIFI_REFRESH:
                    /*if (!mWifiConnector.isConnecting() && !mWifiSearcher.isSearching()) {
                        mWifiInfos.clear();
                        mWifiInfos.addAll(mWifiUtils.getWifiInfos().values());
                        mAdapter.notifyDataSetChanged();
                    }*/
                    break;

                default:
                    break;
            }
        }
    };

    private Runnable mRefreshWifiRunnable = new Runnable() {
        @Override
        public void run() {
            while (mIsRefreshWifi) {
                LogUtils.i(TAG, "refresh wifi thread running---->");
                mWifiUtils.startScan();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    LogUtils.i("mRefreshWifiRunnable", e.toString());
                }
            }
        }
    };

    WifiSearcher.SearchWifiListener mSearchWifiListener = new WifiSearcher.SearchWifiListener() {
        @Override
        public void onSearchWifiFailed(WifiSearcher.ErrorType errorType) {
            LogUtils.i(TAG, "onSearchWifiFailed");
            mHandler.sendEmptyMessage(MSG_WIFI_SEARCH_FAILED);
        }

        @Override
        public void onSearchWifiSuccess(HashMap<String, CustomerWifiInfo> customerWifiInfos) {
            LogUtils.i(TAG, "onSearchWifiSuccess");
            mListView.setVisibility(View.VISIBLE); // MODIFIED by xinlei.sheng, 2016-09-30,BUG-2669930
            findViewById(R.id.wifi_load_progress).setVisibility(View.GONE);
            mWifiInfos.clear();
            mWifiInfos.addAll(customerWifiInfos.values());
            sortWifiList();
            mAdapter.notifyDataSetChanged();
            setConnectedWifiToTop(); // MODIFIED by xinlei.sheng, 2016-11-17,BUG-3356295
            String connectedSsid = mWifiUtils.getConnectedSsid();
            if (!TextUtils.isEmpty(connectedSsid)) {
                mSelectSsid = connectedSsid;
            }

            /*if (!mIsRefreshWifi) {
                LogUtils.i(TAG, "begin refresh wifi thread ---->");
                mIsRefreshWifi = true;
                new Thread(mRefreshWifiRunnable).start();
            }*/
        }
    };

    /* MODIFIED-BEGIN by xinlei.sheng, 2016-11-17,BUG-3356295*/
    private void setConnectedWifiToTop() {
        for(int i = 0 ; i < mWifiInfos.size() ; i ++){
            CustomerWifiInfo topInfo = mWifiInfos.get(i);
            if(topInfo.getState() != 3){
                continue;
            }
            if(i == 0){
                return;
            }
            topInfo = mWifiInfos.remove(i);
            mWifiInfos.add(0,topInfo);
            mAdapter.notifyDataSetChanged();
            sortSingleWifiInfo(mWifiInfos.get(1),1);
        }
    }

    WifiConnector.WifiConnectListener mWifiConnectListener = new WifiConnector.WifiConnectListener() {
        @Override
        public void OnWifiConnectCompleted(boolean isConnected) {
            if (isConnected) {
//                mHandler.sendEmptyMessageDelayed(MSG_WIFI_CONNECTED_SUCCESS, 200);
/* MODIFIED-END by xinlei.sheng,BUG-3356295*/
            } else {
                mHandler.sendEmptyMessageDelayed(MSG_WIFI_CONNECTED_FAILED, 200);
                /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_set);
        registerWifiReceiver();
        /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
        mContext = this;

        mWifiSearcher = new WifiSearcher(this, mSearchWifiListener);
        mWifiConnector = new WifiConnector(this, mWifiConnectListener);

        mWifiUtils = new WifiUtils(this);
        mWifiInfos = new ArrayList<>();
        if (!mWifiUtils.isWifiEnabled()) {
            mWifiUtils.openWifi();
        }

        mWifiInfos.addAll(mWifiUtils.getWifiInfos().values());
        sortWifiList(); // MODIFIED by xinlei.sheng, 2016-10-14,BUG-2669930
        mAdapter = new WifiListAdapter(this, mWifiInfos);
        /* MODIFIED-BEGIN by xinlei.sheng, 2016-11-04,BUG-2669930*/
        mListView = (RecyclerView) findViewById(R.id.wifi_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mListView.setLayoutManager(layoutManager);
        mListView.setAdapter(mAdapter);
        mListView.scrollToPosition(mAdapter.getSelectIndex());
        /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
        /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-30,BUG-2669930*/
        findViewById(R.id.header_back).setOnClickListener(this);
        findViewById(R.id.wifi_goto_advance).setOnClickListener(this);
        findViewById(R.id.wifi_btn_continue).setOnClickListener(this);
        findViewById(R.id.wifi_btn_skip).setOnClickListener(this);

        /* MODIFIED-BEGIN by xinlei.sheng, 2016-10-14,BUG-2669930*/
        if (mWifiInfos.size() < 1) {
            mListView.setVisibility(View.GONE);
            findViewById(R.id.wifi_load_progress).setVisibility(View.VISIBLE);
        }
        /* MODIFIED-END by xinlei.sheng,BUG-2669930*/

        //mWifiSearcher.search(); // MODIFIED by xinlei.sheng, 2016-10-14,BUG-2669930
        /* MODIFIED-END by xinlei.sheng,BUG-2669930*/

        /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-21,BUG-2669930*/
        ImageView imageView = (ImageView) findViewById(R.id.background_wifi);
        Glide.with(this).load(R.drawable.gif_wifi)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(new GlideDrawableImageViewTarget(imageView, 1));
                /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
    }

    @Override
    /* MODIFIED-BEGIN by xinlei.sheng, 2016-11-04,BUG-2669930*/
    protected void onResume() {
        super.onResume();
        mIsRefreshWifi = true;
        new Thread(mRefreshWifiRunnable).start();
    }

    @Override
    /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
    protected void onStop() {
        super.onStop();
        mIsRefreshWifi = false;
    }

    @Override
    protected void onDestroy() {
        LogUtils.i(TAG, "onDestroy------>");
        super.onDestroy();
        unRegisterWifiReceiver();
        if (mWifiSearcher.isSearching()) {
            mWifiSearcher.stopSearch();
        }
        if (mWifiConnector.isConnecting()) {
            mWifiConnector.stopConnect();
        }
    }

    @Override
    public void onSetupFinished() {
        if (!this.isDestroyed()) {
            this.finish();
        }
        /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
    }

    @Override
    /* MODIFIED-BEGIN by xinlei.sheng, 2016-10-13,BUG-2669930*/
    protected Dialog onCreateDialog(int id, Bundle bundle) {
        if (id == WIFI_PASSWORD_DIALOG) {
            mWifiPwdDialog = new WifiPwdDialog(mContext, bundle.getString("ssid"), this);
            SystemBarHelper.hideSystemBars(mWifiPwdDialog);
            return  mWifiPwdDialog;
        }
        return null;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            String ssid = mWifiPwdDialog.getSsid();
            String pwd = mWifiPwdDialog.getPassword();
            if (!TextUtils.isEmpty(ssid) && !TextUtils.isEmpty(pwd)) {
                if (mWifiConnector.isConnecting()) {
                    mWifiConnector.stopConnect();
                } else {
                    mWifiConnector.disconnect();
                }
                if (mSelectSsid != null) {
                    mPreSsid = mSelectSsid;
                }
                mSelectSsid = ssid;
                mWifiConnector.connect(ssid, pwd, 3);
                updateWifiInfoState(mPreSsid, 1);
                updateWifiInfoState(mSelectSsid, 2);
                mAdapter.notifyDataSetChanged();
            }
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {

        }
    }

    @Override
    /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId) {
            case R.id.header_back:
                finish();
                Intent simIntent = new Intent(this, StartActivity.class); // MODIFIED by xinlei.sheng, 2016-10-19,BUG-2669930
                simIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(simIntent);
                break;
            case R.id.wifi_btn_continue:
            case R.id.wifi_btn_skip:
                startActivity(new Intent(this, SimSetActivity.class)); // MODIFIED by xinlei.sheng, 2016-09-30,BUG-2669930

                break;
            case R.id.wifi_goto_advance:
                LogUtils.i(TAG, "goto wifi advance");
                Intent intent = new Intent();
                ComponentName cn = new ComponentName("com.android.settings",
                        "com.android.settings.Settings$AdvancedWifiSettingsActivity");
                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    public void onItemClick(CustomerWifiInfo wifiInfo) { // MODIFIED by xinlei.sheng, 2016-11-04,BUG-2669930
        /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
        if (wifiInfo != null && !wifiInfo.isConnected()) {
            LogUtils.i(TAG, "onTtemClick: ssid: " + wifiInfo.getSsid());
            LogUtils.i(TAG, "onTtemClick: securityType: " + wifiInfo.getSecurity());

            if (WifiUtils.getSecurity(wifiInfo.getSecurity()) <= 0) {
                String ssid = wifiInfo.getSsid();
                if (mWifiConnector.isConnecting()) {
                    mWifiConnector.stopConnect();
                } else {
                    mWifiConnector.disconnect();
                }
                if (mSelectSsid != null) {
                    mPreSsid = mSelectSsid;
                }
                mSelectSsid = ssid;
                mWifiConnector.connect(ssid, null, 1);
                updateWifiInfoState(mPreSsid, 1);
                updateWifiInfoState(mSelectSsid, 2);
                mAdapter.notifyDataSetChanged();
            } else {
                /* MODIFIED-BEGIN by xinlei.sheng, 2016-10-13,BUG-2669930*/
                if (mWifiPwdDialog != null) {
                    mWifiPwdDialog.setHint(getString(R.string.wifi_dialog_hint));
                    mWifiPwdDialog.setPassword("");
                    mWifiPwdDialog.setSsid(wifiInfo.getSsid());
                    SystemBarHelper.hideSystemBars(mWifiPwdDialog);
                }
                Bundle bundle = new Bundle();
                bundle.putString("ssid", wifiInfo.getSsid());
                showDialog(WIFI_PASSWORD_DIALOG, bundle);
                /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
            }
        } else {
            LogUtils.i(TAG, "wifiInfo is null");
        }
    }

    private void registerWifiReceiver() {
        IntentFilter mWifiFilter = new IntentFilter();
        mWifiFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION); // MODIFIED by xinlei.sheng, 2016-10-14,BUG-2669930
        mWifiFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mWifiFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mWifiConnectReceiver, mWifiFilter);
    }

    private void unRegisterWifiReceiver() {
        unregisterReceiver(mWifiConnectReceiver);
    }

    /* MODIFIED-BEGIN by xinlei.sheng, 2016-11-04,BUG-2669930*/
    private void sortWifiList(ArrayList<CustomerWifiInfo> wifiInfo) {
        Collections.sort(wifiInfo, new Comparator<CustomerWifiInfo>() {
        /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
            @Override
            public int compare(CustomerWifiInfo lhs, CustomerWifiInfo rhs) {
                return (rhs.getLevel() - lhs.getLevel());
            }
        });

        /*try { // MODIFIED by xinlei.sheng, 2016-09-12,BUG-2669930
            CustomerWifiInfo connectedInfo = null;
            for (int i = 0; i < mWifiInfos.size(); i++) {
                if (mWifiInfos.get(i).isConnected()) {
                    connectedInfo = mWifiInfos.get(i);
                    mWifiInfos.remove(i);
                    break;
                }
            }
            if (connectedInfo != null) {
                mWifiInfos.add(0, connectedInfo);
            }
        } catch (IndexOutOfBoundsException e) {
            LogUtils.i(TAG, "sortWifiList: " + e.toString());
        *//* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*//*
        }*/
    }
    /* MODIFIED-BEGIN by xinlei.sheng, 2016-11-04,BUG-2669930*/
    private void sortWifiList() {
        sortWifiList(mWifiInfos);
    }
    /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
    private void updateWifiInfoState(String ssid, int state) {
        for(CustomerWifiInfo wifiInfo : mWifiInfos) {
            if (TextUtils.equals(wifiInfo.getSsid(), ssid)) {
                wifiInfo.setState(state);
                break;
            }
        }
    }

    private CustomerWifiInfo getCustomerWifiInfo(String ssid) {
        for(CustomerWifiInfo wifiInfo : mWifiInfos) {
            if (TextUtils.equals(wifiInfo.getSsid(), ssid)) {
                return wifiInfo;
            }
        }
        return null;
        /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
    }

    private BroadcastReceiver mWifiConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int message = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                switch (message) {
                    case WifiManager.WIFI_STATE_DISABLED:
                        LogUtils.i(TAG, "WIFI status disabled");
                        mWifiInfos.clear();
                        mAdapter.notifyDataSetChanged();
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                        LogUtils.i(TAG, "WIFI status disabling");
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        LogUtils.i(TAG, "WIFI status enabled");
                        mWifiUtils.startScan(); // MODIFIED by xinlei.sheng, 2016-10-14,BUG-2669930
                        break;
                    case WifiManager.WIFI_STATE_ENABLING:
                        LogUtils.i(TAG, "WIFI status enabling");
                        break;
                    case WifiManager.WIFI_STATE_UNKNOWN:
                        LogUtils.i(TAG, "WIFI status unknown");
                        break;
                    default:
                        break;
                }
            } else if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                Parcelable parcelableExtra = intent
                        .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (null != parcelableExtra) {
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    switch (networkInfo.getState()) {
                        case CONNECTED:
                            LogUtils.i(TAG, "network connected");
                            /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
                            String connectedSsid = mWifiUtils.getConnectedSsid();
                            if (connectedSsid != null) {
                                if (mSelectSsid != null) {
                                    mPreSsid = mSelectSsid;
                                }
                                mSelectSsid = connectedSsid;
                                updateWifiInfoState(mPreSsid, 1);
                                updateWifiInfoState(mSelectSsid, 3);
                                mAdapter.notifyDataSetChanged();
                                setConnectedWifiToTop(); // MODIFIED by xinlei.sheng, 2016-11-17,BUG-3356295
                            }
                            findViewById(R.id.wifi_btn_skip).setVisibility(View.GONE);
                            findViewById(R.id.wifi_btn_continue).setVisibility(View.VISIBLE);
                            /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
                            break;
                        case CONNECTING:
                            LogUtils.i(TAG, "network is connecting");
                            break;
                        case DISCONNECTED:
                            LogUtils.i(TAG, "network disconnected");
                            break;
                        case DISCONNECTING:
                            LogUtils.i(TAG, "network is disconnecting");
                            break;
                        case SUSPENDED:
                            break;
                        case UNKNOWN:
                            break;
                        default:
                            break;
                    }
                }
            /* MODIFIED-BEGIN by xinlei.sheng, 2016-10-14,BUG-2669930*/
            } else if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                LogUtils.i(TAG, "received SCAN_RESULTS_AVAILABLE_ACTION");
                mListView.setVisibility(View.VISIBLE);
                findViewById(R.id.wifi_load_progress).setVisibility(View.GONE);
                handleNewWifi(); // MODIFIED by xinlei.sheng, 2016-11-04,BUG-2669930
                String connectedSsid = mWifiUtils.getConnectedSsid();
                if (!TextUtils.isEmpty(connectedSsid)) {
                    mSelectSsid = connectedSsid;
                }
                /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
            }
        }
    };

    /* MODIFIED-BEGIN by xinlei.sheng, 2016-11-04,BUG-2669930*/
    private void handleNewWifi() {
        HashMap<String,CustomerWifiInfo> newWifi= mWifiUtils.getWifiInfos();
        sortWifiList(new ArrayList<CustomerWifiInfo>(newWifi.values()));
        printWifiinfo(new ArrayList<CustomerWifiInfo>(newWifi.values()));
        printWifiinfo(mWifiInfos);
        LogUtils.d(TAG,"===============================================");
        //del
        CustomerWifiInfo tempInfo; // MODIFIED by xinlei.sheng, 2016-11-17,BUG-3356295
        for(int i=mWifiInfos.size()-1;i>=0;i--){
            String ssid=mWifiInfos.get(i).getSsid();
            if(!newWifi.containsKey(ssid)){
                mWifiInfos.remove(i);
                LogUtils.d(TAG,"remove "+ssid+" "+i);
                mAdapter.notifyItemRemoved(i);
            }
        }
        //move
        CustomerWifiInfo watchDog=new CustomerWifiInfo();
        watchDog.setSsid("dog");
        watchDog.setLevel(-1);
        mWifiInfos.add(watchDog);
        for(int i=0;i<mWifiInfos.size()-1;i++){
            /* MODIFIED-BEGIN by xinlei.sheng, 2016-11-17,BUG-3356295*/
            tempInfo = mWifiInfos.get(i);
            int newLevel = newWifi.get(tempInfo.getSsid()).getLevel();
            if(newLevel != tempInfo.getLevel()) {
                tempInfo.setLevel(newLevel);
                mAdapter.notifyItemChanged(i);
                LogUtils.d(TAG,"set new "+mWifiInfos.get(i).getSsid()+" level="+newLevel);
                if(hasWifiInfoConnected(tempInfo)){
                    continue;
                }
                //find a fix pisition
                sortSingleWifiInfo(tempInfo,i);
//                int newPosi = i;
//                if(i+1<mWifiInfos.size()&& newLevel<mWifiInfos.get(i+1).getLevel()){
//                    for(int j=i+1;j<mWifiInfos.size();j++){
//                         tempInfo = mWifiInfos.get(j);
//                        if(hasWifiInfoConnected(tempInfo)){
//                            continue;
//                        }
//                        int temp = tempInfo.getLevel();
//                        if (newLevel >= temp) {
//                            newPosi = j;
//                            break;
//                        }
//                    }
//                }else if(i-1>=0&&newLevel>mWifiInfos.get(i-1).getLevel()){
//                    for(int j=i-1;j>=0;j--){
//                         tempInfo = mWifiInfos.get(j);
//                        if(hasWifiInfoConnected(tempInfo)){
//                            continue;
//                        }
//                        int temp = tempInfo.getLevel();
//                        if (newLevel <= temp) {
//                            newPosi = j+1;
//                            break;
//                        }else if(j==0){
//                            newPosi=j;
//                        }
//                    }
//                }
//
//                if (newPosi != i) {
////                    mAdapter.notifyItemMoved(i, newPosi);
//                    tempInfo = mWifiInfos.get(i);
//                    mWifiInfos.add(newPosi, tempInfo);
//                    mAdapter.notifyItemInserted(newPosi);
//                    if(newPosi<i){
//                        mWifiInfos.remove(i+1);
//                        mAdapter.notifyItemRemoved(i+1);
//                    }else{
//                        mWifiInfos.remove(i);
//                        mAdapter.notifyItemRemoved(i);
//                    }
//                    LogUtils.d(TAG, "move " + tempInfo.getSsid() + " to " + newPosi);
//                }
/* MODIFIED-END by xinlei.sheng,BUG-3356295*/

            }
        }
        mWifiInfos.remove(mWifiInfos.size()-1);
        /*printWifiinfo(mWifiInfos);
        for(int i=0;i<mWifiInfos.size()-1;i++){
            if(mWifiInfos.get(i).getLevel()<mWifiInfos.get(i+1).getLevel()){
                LogUtils.e(TAG,"sort error");
            }
        }
        LogUtils.d(TAG,"&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");*/

        //add
        ArrayList<String> allssid=new ArrayList<>();
        for(int i=0;i<mWifiInfos.size();i++){
            allssid.add(mWifiInfos.get(i).getSsid());
        }
        for(String newSsid : newWifi.keySet()){
            if(!allssid.contains(newSsid)){
                int leval=newWifi.get(newSsid).getLevel();
                for(int i=0;i<mWifiInfos.size();i++){
                    /* MODIFIED-BEGIN by xinlei.sheng, 2016-11-17,BUG-3356295*/
                    tempInfo = mWifiInfos.get(i);
                    if(hasWifiInfoConnected(tempInfo)){
                        continue;
                    }
                    if(leval>=tempInfo.getLevel()){
                    /* MODIFIED-END by xinlei.sheng,BUG-3356295*/
                        mWifiInfos.add(i,newWifi.get(newSsid));
                        LogUtils.d(TAG,"add "+newSsid+" to "+i);
                        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mListView.getLayoutManager();
                        int firstItem = linearLayoutManager.findFirstVisibleItemPosition();
                        LogUtils.d(TAG,"item ="+firstItem);
                        mAdapter.notifyItemInserted(i);
                        if(firstItem==0&&i==0){
                            mListView.scrollToPosition(0);
                        }

                        break;
                    }
                }
            }
        }
        LogUtils.d(TAG,"===============================================");
        LogUtils.d(TAG,"after fix");
        printWifiinfo(mWifiInfos);
    }

    /* MODIFIED-BEGIN by xinlei.sheng, 2016-11-17,BUG-3356295*/
    private boolean hasWifiInfoConnected(CustomerWifiInfo tempInfo) {
        if(tempInfo.getState() == 3){
            return true;
        }
        return false;
    }

    private void sortSingleWifiInfo(CustomerWifiInfo wifiInfo , int posi){
        int newPosi = posi;
        int level = wifiInfo.getLevel();
        CustomerWifiInfo tempInfo;
        if(2<mWifiInfos.size()&& level<mWifiInfos.get(posi+1).getLevel()){
            for(int j=posi+1;j<mWifiInfos.size();j++){
                tempInfo = mWifiInfos.get(j);
                if(hasWifiInfoConnected(tempInfo)){
                    continue;
                }
                int temp = tempInfo.getLevel();
                if (level >= temp) {
                    newPosi = j;
                    break;
                }
            }
        }else if(posi-1 >= 0 && level > mWifiInfos.get(posi-1).getLevel()){
            for(int j= posi-1;j>=0;j--){
                tempInfo = mWifiInfos.get(j);
                if(hasWifiInfoConnected(tempInfo)){
                    continue;
                }
                int temp = tempInfo.getLevel();
                if (level <= temp) {
                    newPosi = j+1;
                    break;
                }else if(j==0){
                    newPosi=j;
                }
            }
        }

        if (newPosi != posi) {
//                    mAdapter.notifyItemMoved(i, newPosi);
            tempInfo = mWifiInfos.get(posi);
            mWifiInfos.add(newPosi, tempInfo);
            mAdapter.notifyItemInserted(newPosi);
            if(newPosi<posi){
                mWifiInfos.remove(posi+1);
                mAdapter.notifyItemRemoved(posi+1);
            }else{
                mWifiInfos.remove(posi);
                mAdapter.notifyItemRemoved(posi);
            }
            LogUtils.d(TAG, "move " + tempInfo.getSsid() + " to " + newPosi);
        }
    }
    /* MODIFIED-END by xinlei.sheng,BUG-3356295*/

    private void printWifiinfo(ArrayList<CustomerWifiInfo> wifiInfo){
        LogUtils.d(TAG,"print wifi start");
        int i=0;
        for(CustomerWifiInfo wifi : wifiInfo){
            LogUtils.d(TAG," "+i+" ="+wifi);
            i++;
        }
        LogUtils.d(TAG,"print wifi end");
    }
    /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
}
