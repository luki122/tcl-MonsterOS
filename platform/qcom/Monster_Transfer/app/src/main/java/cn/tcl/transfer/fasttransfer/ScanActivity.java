/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.fasttransfer;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.transfer.sdk.access.ILogicObsv;
import com.tencent.transfer.sdk.access.MessageIdDef;
import com.tencent.transfer.sdk.access.TransferStatusMsg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import beebeesdk.core.BeeBeeEngine;
import beebeesdk.core.HoneyComb;
import cn.tcl.transfer.R;
import cn.tcl.transfer.activity.DisconnectActivity;
import cn.tcl.transfer.util.DialogBuilder;
import cn.tcl.transfer.util.LogUtils;
import cn.tcl.transfer.util.NotificationUtils;
import cn.tcl.transfer.util.Utils;
import mst.app.MstActivity;

public class ScanActivity extends MstActivity implements ILogicObsv {

    private RippleBackground mRippleBackground;
    private ConnectBackground mConnectBackground;
    private Button mCenterButton;
    private TextView mScanText;
    private HoneyComb mHoneyComb;
    private List<String> mBeeList;
    private List<View> mViewList;
    private View mDeviceItemOne;
    private View mDeviceItemTwo;
    private View mDeviceItemThree;
    private View mDeviceItemFour;
    private View mDeviceItemFive;
    private Dialog mDisconnectDialog;
    private String mDevName;
    private int[] mLocation = new int[2];
    private boolean mIsConnecting = false;
    private boolean mIsScanFail = false;
    private static final String TAG="QQTransfer";
    private static final long SCAN_TIME=30*1000L;
    public static final String COMMON_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Transfer/";
    public static final String PIC_PATH= Environment.getExternalStorageDirectory().getAbsolutePath()+"/DCIM/Camera/";
    public static final String VIDEO_PATH= Environment.getExternalStorageDirectory().getAbsolutePath()+"/Movies/";
    public static final String SOFT_PATH= Environment.getExternalStorageDirectory().getAbsolutePath()+"/Transfer/FileTransfer/soft/";
    public static final String MUSIC_PATH= Environment.getExternalStorageDirectory().getAbsolutePath()+"/Music/";
    public static final int[] CONNECT_IMAGE = {R.drawable.connect_1,R.drawable.connect_2,R.drawable.connect_3,R.drawable.connect_4,R.drawable.connect_5};
    private Runnable mCancelScanRunnable = new Runnable() {

        @Override
        public void run() {
            mIsScanFail = true;
            mRippleBackground.stopRippleAnimation();
            mScanText.setVisibility(View.INVISIBLE);
            mHoneyComb.interruptTransferData();
            mCenterButton.setEnabled(true);
            mCenterButton.setText(R.string.text_rescan);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_scan);
        initUI();
        initData();
        File dir = new File(COMMON_PATH);
        if(!dir.exists()) {
            dir.mkdirs();
        }
        BeeBeeEngine.setPicPath(PIC_PATH);
        BeeBeeEngine.setVedioPath(VIDEO_PATH);
        BeeBeeEngine.setSoftWarePath(SOFT_PATH);
        BeeBeeEngine.setMusicPath(MUSIC_PATH);
    }

    @Override
    public void onNavigationClicked(View view) {
        if (mIsConnecting) {
            if (mDisconnectDialog == null) {
                mDisconnectDialog = DialogBuilder.createConfirmDialog(ScanActivity.this, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mDisconnectDialog != null) {
                            mDisconnectDialog.dismiss();
                        }
                        mHoneyComb.reset();
                        Intent intent = new Intent(ScanActivity.this, DisconnectActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        ScanActivity.this.finish();
                    }
                }, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mDisconnectDialog.isShowing()) {
                            mDisconnectDialog.dismiss();
                        }
                    }
                }, getResources().getString(R.string.qq_cancel_confirm_info));
            }
            mDisconnectDialog.dismiss();
            mDisconnectDialog.show();
        } else {
            mHoneyComb.reset();
            finish();
        }
    }
    @Override
    public void onBackPressed() {
        if (mIsConnecting) {
            if (mDisconnectDialog == null) {
                mDisconnectDialog = DialogBuilder.createConfirmDialog(ScanActivity.this, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mDisconnectDialog != null) {
                            mDisconnectDialog.dismiss();
                        }
                        mHoneyComb.reset();
                        Intent intent = new Intent(ScanActivity.this, DisconnectActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        ScanActivity.this.finish();
                    }
                }, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mDisconnectDialog.isShowing()) {
                            mDisconnectDialog.dismiss();
                        }
                    }
                }, getResources().getString(R.string.qq_cancel_confirm_info));
            }
            mDisconnectDialog.dismiss();
            mDisconnectDialog.show();
        } else {
            mHoneyComb.reset();
            finish();
        }
    }

    @Override
    public void notifyMessage(Message message) {
        if (!mIsScanFail) {
            honeyCombHandler.handleMessage(message);
        }
    }

    private void initData() {
        BeeBeeEngine.getInstance(getApplicationContext()).init();
        BeeBeeEngine.getInstance(getApplicationContext()).bindObsv(this);
        mHoneyComb = BeeBeeEngine.getInstance(getApplicationContext()).getHoneyComb();
        Handler handler=new Handler();
        Runnable runnable=new Runnable(){
            @Override
            public void run() {
                mIsScanFail = false;
                mHoneyComb.scanfBee();
            }
        };
        handler.postDelayed(runnable, 500);
        scanhandler.postDelayed(mCancelScanRunnable, SCAN_TIME);
    }

    private void initUI() {
        mRippleBackground = (RippleBackground)findViewById(R.id.content);
        mRippleBackground.startRippleAnimation();
        mConnectBackground = (ConnectBackground)findViewById(R.id.connect);
        mScanText = (TextView)findViewById(R.id.scan_state);
        if (mViewList != null) {
            mViewList.clear();
        }
        ArrayList<View> viewlist = new ArrayList<>();
        mDeviceItemOne = findViewById(R.id.device_area_one);
        viewlist.add(mDeviceItemOne);
        mDeviceItemTwo = findViewById(R.id.device_area_two);
        viewlist.add(mDeviceItemTwo);
        mDeviceItemThree = findViewById(R.id.device_area_three);
        viewlist.add(mDeviceItemThree);
        mDeviceItemFour = findViewById(R.id.device_area_four);
        viewlist.add(mDeviceItemFour);
        mDeviceItemFive = findViewById(R.id.device_area_five);
        viewlist.add(mDeviceItemFive);
        mViewList = viewlist;
        mCenterButton = (Button)findViewById(R.id.centerImage);
        mCenterButton.setText(android.os.Build.MODEL);
        mCenterButton.setEnabled(false);
        mCenterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsScanFail = false;
                mScanText.setVisibility(View.VISIBLE);
                mCenterButton.setText(android.os.Build.MODEL);
                mHoneyComb.scanfBee();
                mCenterButton.setEnabled(false);
                scanhandler.postDelayed(mCancelScanRunnable, SCAN_TIME);
                if (mRippleBackground.isRippleAnimationRunning()) {
                    mRippleBackground.stopRippleAnimation();
                } else {
                    mRippleBackground.startRippleAnimation();
                }
            }
        });
    }
    Handler scanhandler=new Handler();
    Handler honeyCombHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg == null) {
                return;
            }
            LogUtils.d(TAG,"msg.what="+msg.what);
            switch (msg.what) {
                //start transfer
                case MessageIdDef.PROGRESS_CHANGE: {
                    TransferStatusMsg data = (TransferStatusMsg) msg.obj;
                    LogUtils.d(TAG,"data.getStatus="+data.getStatus());
                    switch (data.getStatus()) {
                        case TRANSFER_ALL_BEGIN: {
                            Intent intent = new Intent(ScanActivity.this, TransferActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                            break;
                        }
                    }
                    break;
                }
                //get AP
                case MessageIdDef.DEVICE_LIST_UPDATE: {
                    List<String> ap = (List<String>) msg.obj;

                    ArrayList<String> devList = new ArrayList<>();
                    for (String dev : ap) {
                        if (!devList.contains(dev))
                            devList.add(dev);
                    }
                    mBeeList = devList;
                    if (!mIsConnecting) {
                        setDeviceList();
                    }
                    break;
                }
                //sender ap close
                case MessageIdDef.MSG_HTTP_SENDER_EXIT_REQUEST:
                case MessageIdDef.MSG_HTTP_RECEIVER_EXIT_RESPONE: {
                    mIsConnecting = false;
                    if (mConnectBackground.isRippleAnimationRunning()) {
                        mConnectBackground.stopRippleAnimation();
                    }
                    mHoneyComb.reset();
                    mIsScanFail = true;
                    mCenterButton.setEnabled(true);
                    mCenterButton.setText(R.string.text_rescan);
                    for (int i=0;i<mViewList.size();i++) {
                        View view = mViewList.get(i);
                        ((ImageView)view.findViewById(R.id.device)).setBackgroundResource(R.drawable.phone);
                        view.setVisibility(View.GONE);
                    }
                    break;
                }
                case MessageIdDef.MSG_HTTP_AS_RECEIVER_RESPONE: {
                    Toast.makeText(ScanActivity.this, R.string.notify_connect_success, Toast.LENGTH_SHORT).show();
                    break;
                }
                case MessageIdDef.MSG_HTTP_AS_RECEIVER_RESPONE_FAIL:
                case MessageIdDef.MSG_HTTP_AS_RECEIVER_RESPONE_REJECT:
                case MessageIdDef.CONNECT_AP_FAIL: {
                    mIsConnecting = false;
                    mConnectBackground.stopRippleAnimation();
                    for (int i=0;i<mViewList.size();i++) {
                        View view = mViewList.get(i);
                        ((ImageView)view.findViewById(R.id.device)).setBackgroundResource(R.drawable.phone);
                    }
                    setDeviceList();
                    break;
                }
            }
        }
    };

    private void setDeviceList() {
        if(mBeeList!=null && mBeeList.size()>0) {
            scanhandler.removeCallbacks(mCancelScanRunnable);
            mScanText.setVisibility(View.INVISIBLE);
            for (int i=0;i<mViewList.size();i++) {
                View view = mViewList.get(i);
                TextView devicetext = (TextView)view.findViewById(R.id.device_name);
                TextView clicktext = (TextView)view.findViewById(R.id.click);
                clicktext.setVisibility(View.VISIBLE);
                if (i < mBeeList.size()) {
                    devicetext.setText(mBeeList.get(i));
                    ViewClickListener listener = new ViewClickListener();
                    listener.setPosition(i);
                    view.setVisibility(View.VISIBLE);
                    view.setOnClickListener(listener);
                } else {
                    view.setVisibility(View.GONE);
                }
            }
        } else {
            for (int i=0;i<mViewList.size();i++) {
                View view = mViewList.get(i);
                view.setVisibility(View.GONE);
            }
        }

    }

    private class ViewClickListener implements View.OnClickListener {
        private int i;
        public void setPosition(int i) {
            this.i = i;
        }
        @Override
        public void onClick(View view) {
            if (mIsConnecting) {
                return;
            }
            mHoneyComb.connectBee(mBeeList.get(i));
            int[] location = new int[2];
            mCenterButton.getLocationOnScreen(mLocation);
            TextView clicktext = (TextView)view.findViewById(R.id.click);
            clicktext.setVisibility(View.INVISIBLE);
            ImageView image = (ImageView)view.findViewById(R.id.device);
            image.setBackgroundResource(CONNECT_IMAGE[i]);
            image.getLocationInWindow(location);
            int x = location[0];
            int y = location[1];
            Rect frame = new Rect();
            view.getWindowVisibleDisplayFrame(frame);
            int stateHeight = frame.top;
            LogUtils.d(TAG,"view x:"+x+" view y:"+y);
            mRippleBackground.stopRippleAnimation();
            mScanText.setVisibility(View.INVISIBLE);
            LogUtils.d(TAG,"start x:"+(x+image.getWidth()/2)+" start y:"+(y-stateHeight-image.getHeight()/2));
            mConnectBackground.setStart(ScanActivity.this,x+image.getWidth()/2,y-stateHeight-image.getHeight()/2);
            LogUtils.d(TAG,"end x:"+(mLocation[0]+mCenterButton.getWidth()/2)+" end y:"+(mLocation[1]-stateHeight));
            mConnectBackground.setEnd(ScanActivity.this,mLocation[0]+mCenterButton.getWidth()/2,mLocation[1]-stateHeight);
            mConnectBackground.startRippleAnimation();
            mIsConnecting = true;
            for (int j=0;j<mViewList.size();j++) {
                if (j != i) {
                    View otherdevice = mViewList.get(j);
                    otherdevice.setVisibility(View.INVISIBLE);
                }
            }
        }
    }
}
