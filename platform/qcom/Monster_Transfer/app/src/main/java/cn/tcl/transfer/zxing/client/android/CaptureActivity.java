/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.transfer.zxing.client.android;



import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Map;

import cn.tcl.transfer.ICallback;
import cn.tcl.transfer.R;
import cn.tcl.transfer.activity.CategoryListActivity;
import cn.tcl.transfer.activity.OldNoteActivity;
import cn.tcl.transfer.entity.CommEntity;
import cn.tcl.transfer.operator.wifi.APAdmin;
import cn.tcl.transfer.operator.wifi.WIFIAdmin;
import cn.tcl.transfer.send.ISendInfo;
import cn.tcl.transfer.send.SendBackupDataService;
import cn.tcl.transfer.util.ApBean;
import cn.tcl.transfer.util.CodeUtil;
import cn.tcl.transfer.util.DialogBuilder;
import cn.tcl.transfer.util.LogUtils;
import cn.tcl.transfer.util.QRCodeInfo;
import cn.tcl.transfer.zxing.client.android.camera.CameraManager;
import mst.app.MstActivity;

/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the barcode correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 */
public final class CaptureActivity extends MstActivity implements SurfaceHolder.Callback {

    private static final String TAG = CaptureActivity.class.getSimpleName();
    private static final String TAG_HOT = "HOT";

    private static final String SCAN_RESULT = "scan_result";
    private static final String SEND_MESSAGE = "send hello";

    private static final String[] ZXING_URLS = { "http://zxing.appspot.com/scan", "zxing://scan/" };
    private static final long CONNECT_DELAY = 1500L;

    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private Result savedResultToShow;
    private ViewfinderView viewfinderView;
    private Result lastResult;
    private boolean hasSurface;
    private IntentSource source;
    private Collection<BarcodeFormat> decodeFormats;
    private Map<DecodeHintType,?> decodeHints;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;
    private AmbientLightManager ambientLightManager;
    private String mSsid;

    ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setMstContentView(R.layout.capture);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
        ambientLightManager = new AmbientLightManager(this);

        mWm = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        mAp = new APAdmin(this);

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
            mScanHandler.sendEmptyMessage(CONNECT_SUCCESS_DIALOG);
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
            Log.e(TAG, "onError failed");
            mScanHandler.sendEmptyMessage(CONNECT_FAILED_DIALOG);
        }

        @Override
        public void onAllComplete() throws RemoteException {
        }

        @Override
        public void onCancel() throws RemoteException {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
        // want to open the camera driver and measure the screen sysDataSize if we're going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the wrong sysDataSize and partially
        // off screen.
        cameraManager = new CameraManager(getApplication());

        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        viewfinderView.setCameraManager(cameraManager);

        handler = null;
        lastResult = null;


        beepManager.updatePrefs();
        ambientLightManager.start(cameraManager);

        inactivityTimer.onResume();

        Intent intent = getIntent();

        source = IntentSource.NONE;
        decodeFormats = null;
        characterSet = null;

        if (intent != null) {

            String action = intent.getAction();
            String dataString = intent.getDataString();

            if (Intents.Scan.ACTION.equals(action)) {

                // Scan the formats the intent requested, and return the result to the calling activity.
                source = IntentSource.NATIVE_APP_INTENT;
                decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);
                decodeHints = DecodeHintManager.parseDecodeHints(intent);

                if (intent.hasExtra(Intents.Scan.WIDTH) && intent.hasExtra(Intents.Scan.HEIGHT)) {
                    int width = intent.getIntExtra(Intents.Scan.WIDTH, 0);
                    int height = intent.getIntExtra(Intents.Scan.HEIGHT, 0);
                    if (width > 0 && height > 0) {
                        cameraManager.setManualFramingRect(width, height);
                    }
                }

                if (intent.hasExtra(Intents.Scan.CAMERA_ID)) {
                    int cameraId = intent.getIntExtra(Intents.Scan.CAMERA_ID, -1);
                    if (cameraId >= 0) {
                        cameraManager.setManualCameraId(cameraId);
                    }
                }

                String customPromptMessage = intent.getStringExtra(Intents.Scan.PROMPT_MESSAGE);

            } else if (dataString != null &&
                    dataString.contains("http://www.google") &&
                    dataString.contains("/m/products/scan")) {

                // Scan only products and send the result to mobile Product Search.
                source = IntentSource.PRODUCT_SEARCH_LINK;
                decodeFormats = DecodeFormatManager.PRODUCT_FORMATS;

            } else if (isZXingURL(dataString)) {

                // Scan formats requested in query string (all formats if none specified).
                // If a return URL is specified, send the results there. Otherwise, handle it ourselves.
                source = IntentSource.ZXING_LINK;
                Uri inputUri = Uri.parse(dataString);
                decodeFormats = DecodeFormatManager.parseDecodeFormats(inputUri);
                // Allow a sub-set of the hints to be specified by the caller.
                decodeHints = DecodeHintManager.parseDecodeHints(inputUri);

            }

            characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);

        }

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
        }
    }

    private static boolean isZXingURL(String dataString) {
        if (dataString == null) {
            return false;
        }
        for (String url : ZXING_URLS) {
            if (dataString.startsWith(url)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        ambientLightManager.stop();
        beepManager.close();
        cameraManager.closeDriver();
        if (!hasSurface) {
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
        unbindService(mConn);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (source == IntentSource.NATIVE_APP_INTENT) {
                    setResult(RESULT_CANCELED);
                    finish();
                    return true;
                }
                if ((source == IntentSource.NONE || source == IntentSource.ZXING_LINK) && lastResult != null) {
                    restartPreviewAfterDelay(0L);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_FOCUS:
            case KeyEvent.KEYCODE_CAMERA:
                // Handle these events so they don't launch the Camera app
                return true;
            // Use volume up/down to turn on light
//            case KeyEvent.KEYCODE_VOLUME_DOWN:
//                cameraManager.setTorch(false);
//                return true;
//            case KeyEvent.KEYCODE_VOLUME_UP:
//                cameraManager.setTorch(true);
//                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onNavigationClicked(View view) {
        this.finish();
    }
    private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
        // Bitmap isn't used yet -- will be used soon
        if (handler == null) {
            savedResultToShow = result;
        } else {
            if (result != null) {
                savedResultToShow = result;
            }
            if (savedResultToShow != null) {
                Message message = Message.obtain(handler, R.id.decode_succeeded, savedResultToShow);
                handler.sendMessage(message);
            }
            savedResultToShow = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * A valid barcode has been found, so give an indication of success and show the results.
     *
     * @param rawResult The contents of the barcode.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param barcode   A greyscale bitmap of the camera data which was decoded.
     */
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        inactivityTimer.onActivity();
        lastResult = rawResult;
        if (rawResult != null && !TextUtils.isEmpty(rawResult.getText())) {
            analysisCode(rawResult.getText());
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            LogUtils.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, decodeFormats, decodeHints, characterSet, cameraManager);
            }
            decodeOrStoreSavedBitmap(null, null);
        } catch (IOException ioe) {
            Log.e(TAG,"initializing camera IOException:",ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.e(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage(getString(R.string.msg_camera_framework_bug));
        builder.setPositiveButton(R.string.text_confirm, new FinishListener(this));
        builder.setOnCancelListener(new FinishListener(this));
        builder.show();
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
        resetStatusView();
    }

    private void resetStatusView() {
        viewfinderView.setVisibility(View.VISIBLE);
        lastResult = null;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }


    private Socket mSocket;
    private APAdmin mAp;
    private WifiManager mWm;
    private ObjectInputStream mOis;
    private ObjectOutputStream mOos;
    private Dialog mConnectingDialog;
    private Dialog mRescanDialog;
    private static final int CONNECT_FAILED_DIALOG = 1202;
    private static final int CONNECT_SUCCESS_DIALOG = 1203;
    private static final int CONNECT_START_DIALOG = 1204;
    private static final int CONNECT_RESCAN_DIALOG = 1205;
    private static final int SCAN_FAILED_DIALOG = 1206;
    private Handler mScanHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what) {
                case SCAN_FAILED_DIALOG:
                    if (mConnectingDialog != null && mConnectingDialog.isShowing()) {
                        mConnectingDialog.dismiss();
                    }
                    mScanHandler.sendEmptyMessage(CONNECT_RESCAN_DIALOG);
                    Toast.makeText(CaptureActivity.this, R.string.scan_failed, Toast.LENGTH_SHORT).show();
                    break;
                case CONNECT_FAILED_DIALOG:
                    if (mConnectingDialog != null && mConnectingDialog.isShowing()) {
                        mConnectingDialog.dismiss();
                    }
                    mScanHandler.sendEmptyMessage(CONNECT_RESCAN_DIALOG);
                    Toast.makeText(CaptureActivity.this, R.string.connect_failed, Toast.LENGTH_SHORT).show();
                    break;
                case CONNECT_SUCCESS_DIALOG:
                    if (mConnectingDialog != null) {
                        mConnectingDialog.dismiss();
                    }

                    Intent intent = new Intent(CaptureActivity.this, CategoryListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    Toast.makeText(CaptureActivity.this, R.string.connect_success, Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case CONNECT_START_DIALOG:
                    if (mConnectingDialog == null) {
                        mConnectingDialog = DialogBuilder.createProgressDialog(CaptureActivity.this, getResources().getString(R.string.text_loading));
                    }
                    if (!mConnectingDialog.isShowing() && !isFinishing()) {
                        mConnectingDialog.show();
                    }
                    break;
                case CONNECT_RESCAN_DIALOG:
                    if (mRescanDialog == null) {
                        mRescanDialog = DialogBuilder.createConfirmDialog(CaptureActivity.this, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                restartPreviewAfterDelay(0L);
                                if (mRescanDialog != null && mRescanDialog.isShowing()) {
                                    mRescanDialog.dismiss();
                                }
                            }
                        });
                    }
                    if (!mRescanDialog.isShowing() && !isFinishing()) {
                        mRescanDialog.show();
                    }
                    break;
            }
        }
    };

    private void analysisCode(String code) {
        QRCodeInfo info = CodeUtil.getInfo(code);
        Log.d(TAG,"scan result info:"+info);
        if (info != null && !TextUtils.isEmpty(info.getSsid())) {
            mSsid = info.getSsid();
            connectAP(mSsid);
        } else {
            mScanHandler.sendEmptyMessage(SCAN_FAILED_DIALOG);
        }
    }
    private void connectAP(String ssid) {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled()) {
            wifi.setWifiEnabled(true);
            Handler handler=new Handler();
            Runnable runnable=new Runnable(){
                @Override
                public void run() {
                    connectAP(mSsid);
                }
            };
            handler.postDelayed(runnable, CONNECT_DELAY);
        } else {
            mScanHandler.sendEmptyMessage(CONNECT_START_DIALOG);
            Boolean flag = mAp.connectToAP(this, ssid);
            LogUtils.d(TAG, "connectToAP flag:" + flag);

//                startRequestConnect();
            mScanHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (flag) {
                        try {
                            mRemoteService.connect();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG, "connectAP");
                    } else {
                        Log.d(TAG, "connectAP failed");
                        mScanHandler.sendEmptyMessage(CONNECT_FAILED_DIALOG);
                    }
                }
            }, 10 * 1000);
        }
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
}

