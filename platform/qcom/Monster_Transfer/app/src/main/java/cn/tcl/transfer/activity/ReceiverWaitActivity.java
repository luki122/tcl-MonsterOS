/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.activity;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import cn.tcl.transfer.IReceiveCallback;
import cn.tcl.transfer.R;
import cn.tcl.transfer.entity.CommEntity;
import cn.tcl.transfer.operator.wifi.APAdmin;
import cn.tcl.transfer.receiver.IReceiveInfo;
import cn.tcl.transfer.receiver.ReceiveBackupDataService;
import cn.tcl.transfer.send.ISendInfo;
import cn.tcl.transfer.util.CodeUtil;
import cn.tcl.transfer.util.DialogBuilder;
import cn.tcl.transfer.util.FileBean;
import cn.tcl.transfer.util.LogUtils;
import cn.tcl.transfer.util.NotificationUtils;
import cn.tcl.transfer.util.Utils;
import mst.app.MstActivity;

public class ReceiverWaitActivity extends MstActivity {

    private Button mBtn_cancel;
    private TextView mWaitText;
    private APAdmin apadmin;
    private String mSsid;
    private static final String SSID = "ssid";
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private ServerSocket socketConnection;
    private Socket mSocket;
    private boolean mConnectStatus = true;
    private Dialog mDisconnectDialog;
    private Timer timer = null;
    private TimerTask task = null;
    private Message msg = null;
    private static boolean flag = false;
    private static final String TAG = "WAIT";
    private static final int ANIM = 1;
    private static final long WAIT_TIME=15*60*1000L;


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mDisconnectHandler.removeCallbacks(mDisconnectScanRunnable);
            Intent intent = new Intent(ReceiverWaitActivity.this, ReceivingActivity.class);
            intent.putExtra("data_size", (String)msg.obj);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    };
    Handler mDisconnectHandler=new Handler();
    private Runnable mDisconnectScanRunnable = new Runnable() {

        @Override
        public void run() {
            if (apadmin == null) {
                apadmin = new APAdmin(ReceiverWaitActivity.this);
            }
            apadmin.closeWifiAp();
            Intent intent = new Intent();
            intent.setClass(ReceiverWaitActivity.this,DisconnectActivity.class);
            ReceiverWaitActivity.this.startActivity(intent);
            ReceiverWaitActivity.this.finish();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            mSsid = intent.getStringExtra(SSID);
        }
        Toast.makeText(this, R.string.notify_connect_success, Toast.LENGTH_SHORT).show();
        setMstContentView(R.layout.activity_receiver_wait);
        mWaitText = (TextView)findViewById(R.id.point_text);
        mBtn_cancel = (Button)findViewById(R.id.cancel);
        mBtn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDisconnectDialog == null) {
                    mDisconnectDialog = DialogBuilder.createConfirmDialog(ReceiverWaitActivity.this, new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (apadmin == null) {
                                apadmin =new APAdmin(ReceiverWaitActivity.this);
                            }
                            apadmin.closeWifiAp();
                            Intent intent = new Intent();
                            intent.setClass(ReceiverWaitActivity.this,DisconnectActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            ReceiverWaitActivity.this.startActivity(intent);
                            if (mDisconnectDialog != null) {
                                mDisconnectDialog.dismiss();
                            }
                            ReceiverWaitActivity.this.finish();
                        }
                    }, new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (mDisconnectDialog.isShowing()) {
                                mDisconnectDialog.dismiss();
                            }
                        }
                    }, getResources().getString(R.string.text_cancel_confirm));
                }
                mDisconnectDialog.dismiss();
                mDisconnectDialog.show();
            }
        });

        Intent intent1 = new Intent(this, ReceiveBackupDataService.class);
        bindService(intent1, mConn, Context.BIND_AUTO_CREATE);

        mDisconnectHandler.postDelayed(mDisconnectScanRunnable, WAIT_TIME);
    }

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
                System.out.println("onServiceConnected");
            } catch (Exception e) {
                LogUtils.e(TAG, "onServiceConnected", e);
            }
            System.out.println("bind success! " + mRemoteService.toString());
        }
    };

    private IReceiveCallback.Stub  mCallBack = new IReceiveCallback.Stub() {

        @Override
        public void onConnected() throws RemoteException {
        }

        @Override
        public void onStart(int type) {

        }

        @Override
        public void onProgress(int type, long size, long speed) {
        }

        @Override
        public void onFileBeginRecv(int type, String fileName) {

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

        @Override
        public void onReceiveDataInfo(String info) throws RemoteException {

        }

        @Override
        public void onReceiveDataSize(String info) throws RemoteException {
            Message msg = mHandler.obtainMessage(1);
            msg.obj = info;
            mHandler.sendMessage(msg);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        startWait();
        LogUtils.d(TAG,"this enter flag is resume"+flag);
    }

    @Override
    protected void onStop() {
        super.onStop();
        flag = false;
        try{
            if (task != null) {
                task.cancel();
                task = null;
            }
            if (timer != null) {
                timer.cancel();
                timer.purge();
                timer = null;
            }
            mHandler.removeMessages(msg.what);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConn);
        mConn = null;
        mRemoteService = null;
        mCallBack = null;
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

    @Override
    public void onBackPressed() {
        if (mDisconnectDialog == null) {
            mDisconnectDialog = DialogBuilder.createConfirmDialog(ReceiverWaitActivity.this, new DialogInterface.OnClickListener(){

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (apadmin == null) {
                        apadmin =new APAdmin(ReceiverWaitActivity.this);
                    }
                    apadmin.closeWifiAp();
                    Intent intent = new Intent();
                    intent.setClass(ReceiverWaitActivity.this,DisconnectActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    ReceiverWaitActivity.this.startActivity(intent);
                    if (mDisconnectDialog != null) {
                        mDisconnectDialog.dismiss();
                    }
                    ReceiverWaitActivity.this.finish();
                }
            }, new DialogInterface.OnClickListener(){

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (mDisconnectDialog.isShowing()) {
                        mDisconnectDialog.dismiss();
                    }
                }
            }, getResources().getString(R.string.text_cancel_confirm));
        }
        mDisconnectDialog.dismiss();
        mDisconnectDialog.show();
    }
    private int mTextId = R.string.text_wait_for_send;
    private Handler mWaitHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ANIM:
                    if (mTextId == R.string.text_wait_for_send) {
                        mTextId = R.string.text_wait_for_send_one;
                        mWaitText.setText(".");
                    } else if (mTextId == R.string.text_wait_for_send_one) {
                        mTextId = R.string.text_wait_for_send_two;
                        mWaitText.setText("..");
                    } else if (mTextId == R.string.text_wait_for_send_two) {
                        mTextId = R.string.text_wait_for_send;
                        mWaitText.setText("");
                    }
                    break;
                default:
                    break;
            }
        }
    };
    public void startWait() {
        if (null == timer) {
            if (null == task) {
                task = new TimerTask() {

                    @Override
                    public void run() {
                        if (null == msg) {
                            msg = new Message();
                        } else {
                            msg = Message.obtain();
                        }
                        msg.what = ANIM;
                        mWaitHandler.sendMessage(msg);
                    }

                };
            }

            timer = new Timer(true);
            timer.schedule(task, 1000, 1000);
        }
    }
    @Override
    public void onNavigationClicked(View view) {
        if (mDisconnectDialog == null) {
            mDisconnectDialog = DialogBuilder.createConfirmDialog(ReceiverWaitActivity.this, new DialogInterface.OnClickListener(){

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (apadmin == null) {
                        apadmin =new APAdmin(ReceiverWaitActivity.this);
                    }
                    apadmin.closeWifiAp();
                    Intent intent = new Intent();
                    intent.setClass(ReceiverWaitActivity.this,DisconnectActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    ReceiverWaitActivity.this.startActivity(intent);
                    if (mDisconnectDialog != null) {
                        mDisconnectDialog.dismiss();
                    }
                    ReceiverWaitActivity.this.finish();
                }
            }, new DialogInterface.OnClickListener(){

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (mDisconnectDialog.isShowing()) {
                        mDisconnectDialog.dismiss();
                    }
                }
            }, getResources().getString(R.string.text_cancel_confirm));
        }
        mDisconnectDialog.dismiss();
        mDisconnectDialog.show();
    }
}
