/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import cn.tcl.transfer.IReceiveCallback;
import cn.tcl.transfer.R;
import cn.tcl.transfer.operator.wifi.APAdmin;
import cn.tcl.transfer.receiver.IReceiveInfo;
import cn.tcl.transfer.receiver.ReceiveBackupDataService;
import cn.tcl.transfer.util.CodeUtil;
import cn.tcl.transfer.util.LogUtils;
import cn.tcl.transfer.util.qrimage.QRImage;
import mst.app.MstActivity;

public class CreateHotspotActivity extends MstActivity {
    private static final String SSID = "Transfer_CN_";
    private static final String EXTRA_SSID = "ssid";
    private static final int ENTER_WAIT = 1;
    private static final int CREATE_CODE = 1;
    private static final String TAG = "Hotspot";
    private static final String SEND_MESSAGE = "recv hello";
    private static final String WIFI_AP_STATE_CHANGED = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    private static final String WIFI_STATE = "wifi_state";
    private static final int WIFI_AP_STATE_DISABLED = 11;
    private APAdmin mApm;
    private String mSsid;
    private ImageView mImage;

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver(){
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WIFI_AP_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(WIFI_STATE, 0);
                if (state == WIFI_AP_STATE_DISABLED) {
                    CreateHotPostTask createTask = new CreateHotPostTask(CreateHotspotActivity.this);
                    createTask.execute();
                    try {
                        unregisterReceiver(wifiReceiver);
                    } catch (IllegalArgumentException e) {
                        if (e.getMessage().contains("Receiver not registered")) {
                            // Ignore this exception. This is exactly what is desired
                        } else {
                            // unexpected
                            Log.e(TAG,"unregisterReceiver exception");
                        }
                    }
                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_create_hotspot);
        mImage = (ImageView)findViewById(R.id.center_image);
        mApm = new APAdmin(this);
        mSsid = createName(SSID);
        if (!Settings.System.canWrite(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, CREATE_CODE);
        } else {
            if (!mApm.isWifiApEnabled()) {
                CreateHotPostTask createTask = new CreateHotPostTask(this);
                createTask.execute();
            } else {
                mApm.closeWifiAp();
                registerReceiver(wifiReceiver,new IntentFilter(WIFI_AP_STATE_CHANGED));
            }
        }
        mBackkeyFlag = false;
        Intent recv = new Intent(this, ReceiveBackupDataService.class);
        startService(recv);

        Intent intent1 = new Intent(this, ReceiveBackupDataService.class);
        bindService(intent1, mConn, Context.BIND_AUTO_CREATE);
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
            mHandler.sendEmptyMessage(ENTER_WAIT);
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
        }
    };

    public String createName(String header) {
        String strRes = "";
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            strRes = strRes + Math.abs(random.nextInt()) % 10;
        }
        return header + strRes;
    }

    class CreateHotPostTask extends AsyncTask<Void,Integer,String> {
        private Context context;
        CreateHotPostTask(Context context) {
            this.context = context;
        }


        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(Void... params) {
            if (mApm != null) {
                return mApm.startAP(mSsid);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                String code = CodeUtil.createInfo(result);
                if (TextUtils.isEmpty(code)) {
                    Toast.makeText(CreateHotspotActivity.this, R.string.text_qrcode_fail, Toast.LENGTH_SHORT).show();
                } else {
                    mImage.setImageBitmap(QRImage.createQRImage(code));
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_CODE) {
            if (Settings.System.canWrite(this)) {
                CreateHotPostTask createTask = new CreateHotPostTask(this);
                createTask.execute();
            } else {
                Toast.makeText(CreateHotspotActivity.this, R.string.text_setting_permission_not_granted, Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onNavigationClicked(View view) {
        mBackkeyFlag =true;
        if (mApm != null) {
            mApm.closeWifiAp();
        }
        this.finish();
    }

    private boolean mFlag = false;
    private boolean mDeclineflag = false;
    private boolean mHotspotclose =false;
    private boolean mDeclineRestart  = false;
    private int mHotspotcount = 0;
    private ServerSocket mSocketConnection;
    private Socket mSocket;
    private ObjectInputStream mIn;
    private ObjectOutputStream mOut;
    private String mPhonetype;
    private boolean mBackkeyFlag = false;

    private void enterWaitActivity() {
        mBackkeyFlag =true;
        Intent intent = new Intent();
        intent.setClass(CreateHotspotActivity.this,ReceiverWaitActivity.class);
        intent.putExtra(EXTRA_SSID, mSsid);
        CreateHotspotActivity.this.startActivity(intent);
        CreateHotspotActivity.this.finish();
    }

//    public void startRequestResponse() {
//        new Thread() {
//            public void run() {
//                try {
//                    if(!mDeclineRestart){
//                        Thread.sleep(5000);
//                    }
//                    if (!mBackkeyFlag) {
//                        ServerResponse();
//                    }
//                } catch (Exception e) {
//                    Log.e(TAG, "startRequestResponse: ", e);
//                }
//            }
//        }.start();
//    }

    @Override
    protected void onStop() {
        LogUtils.d(TAG, "onStop()");
        try {
            mFlag = false;
            mHotspotclose = false;
            if (mSocketConnection != null) {
                mSocketConnection.close();
            }
            if (mSocket != null && !mSocket.isClosed()) {
                mSocket.shutdownInput();
                mSocket.shutdownOutput();
                mSocket.close();

            }

        } catch (Exception e) {
            Log.e(TAG, "onStop: ", e);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        unbindService(mConn);
        try {
            unregisterReceiver(wifiReceiver);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException", e);
            if (e.getMessage().contains("Receiver not registered")) {
                // Ignore this exception. This is exactly what is desired
            } else {
                // unexpected
                Log.e(TAG,"unregisterReceiver exception");
            }
        }
        super.onDestroy();
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
        super.onBackPressed();
        if (mApm != null) {
            mApm.closeWifiAp();
        }
        mBackkeyFlag =true;
    }
    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.d(TAG, "socketConnection is close---1111");
        LogUtils.d(TAG,"this enter flag is resume"+mFlag);
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ENTER_WAIT:
                    enterWaitActivity();
                    break;
                default:
                    break;
            }
        }
    };

}
