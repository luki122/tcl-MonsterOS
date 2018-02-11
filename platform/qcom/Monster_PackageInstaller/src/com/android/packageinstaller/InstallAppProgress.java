/*
**
** Copyright 2007, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/
package com.android.packageinstaller;

import static android.content.pm.PackageInstaller.SessionParams.UID_UNKNOWN;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.android.packageinstaller.UninstallAppProgress.PackageDeleteObserver;
import com.android.packageinstaller.adplugin.ScannerAdPlugin;
import com.android.packageinstaller.adplugin.ScannerUtils;
import com.android.packageinstaller.adplugin.ShortcutSqlite;
import com.android.packageinstaller.permission.utils.IoUtils;
import com.android.packageinstaller.permission.utils.PermUtils;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import tmsdk.common.module.qscanner.QScanConstants;
import tmsdk.common.module.qscanner.QScanResultEntity;

/**
 * This activity corresponds to a download progress screen that is displayed 
 * when the user tries
 * to install an application bundled as an apk file. The result of the application install
 * is indicated in the result code that gets set to the corresponding installation status
 * codes defined in PackageManager. If the package being installed already exists,
 * the existing package is replaced with the new one.
 */
public class InstallAppProgress extends Activity implements View.OnClickListener, OnCancelListener,OnCheckedChangeListener {
    private final String TAG="InstallAppProgress";
    public static final String SP_INSTALL_DETECTION = "install_detection";
    private static final String BROADCAST_ACTION =
            "com.android.packageinstaller.ACTION_INSTALL_COMMIT";
    private static final String BROADCAST_SENDER_PERMISSION =
            "android.permission.INSTALL_PACKAGES";
    private ApplicationInfo mAppInfo;
    private Uri mPackageURI;
    private ProgressBar mProgressBar;
    private View mOkPanel;
    private TextView mStatusTextView;
    private TextView mExplanationTextView;
    private Button mDoneButton;
    private Button mLaunchButton;
    private final int INSTALL_COMPLETE = 1;
    private Intent mLaunchIntent;
    private static final int DLG_OUT_OF_SPACE = 1;
    private CharSequence mLabel;
    private HandlerThread mInstallThread;
    private Handler mInstallHandler;
    
    /*********************** START *******************************/
    private ScannerAdPlugin mScannerAdPlugin ;
    private List<QScanResultEntity> mQScanResultEntitys ;
    private FrameLayout top_divider ;
    private LinearLayout llyt_optimization ,llty_app_complete,app_yh_complete,app_snippet;
    private TextView tv_advertisement,tv_permission,tv_quick,tv_suspension_window;
    private CheckBox ck_advertisement,ck_permissions,ck_quick,ck_suspension_window ;
    private Button cxsm_button,yh_button,jx_button,xz_button ;
    private RelativeLayout rlyt_advertisement,rlyt_permission,rlyt_quick,rlyt_suspension_window,rlyt_bg ;
    private ImageView img_app_icon ;
    private TextView txt_app_name,txt_app_sm ;
    /*********************** END *******************************/

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INSTALL_COMPLETE:
    
                    if (getIntent().getBooleanExtra(Intent.EXTRA_RETURN_RESULT, false)) {
                        Intent result = new Intent();
                        result.putExtra(Intent.EXTRA_INSTALL_RESULT, msg.arg1);
                        setResult(msg.arg1 == PackageInstaller.STATUS_SUCCESS
                                ? Activity.RESULT_OK : Activity.RESULT_FIRST_USER,
                                        result);
                        clearCachedApkIfNeededAndFinish();
                        return;
                    }
                    // Update the status text
                    mProgressBar.setVisibility(View.GONE);
                    top_divider.setVisibility(View.VISIBLE) ;
                    // Show the ok button
                    int centerTextLabel;
                    int centerExplanationLabel = -1;
                    if (msg.arg1 == PackageInstaller.STATUS_SUCCESS) {
                        mLaunchButton.setVisibility(View.VISIBLE);
//                        ((ImageView)findViewById(R.id.center_icon))
//                                .setImageDrawable(getDrawable(R.drawable.ic_done_92));
                        centerTextLabel = R.string.install_done;
                        // Enable or disable launch button
                        mLaunchIntent = getPackageManager().getLaunchIntentForPackage(
                                mAppInfo.packageName);
                        boolean enabled = false;
                        if(mLaunchIntent != null) {
                            List<ResolveInfo> list = getPackageManager().
                                    queryIntentActivities(mLaunchIntent, 0);
                            if (list != null && list.size() > 0) {
                                enabled = true;
                            }
                        }
                        if (enabled) {
                            mLaunchButton.setOnClickListener(InstallAppProgress.this);
                        } else {
                            mLaunchButton.setEnabled(false);
                        }
                    } else if (msg.arg1 == PackageInstaller.STATUS_FAILURE_STORAGE){
                        showDialogInner(DLG_OUT_OF_SPACE);
                        return;
                    } else {
                        // Generic error handling for all other error codes.
//                        ((ImageView)findViewById(R.id.center_icon))
//                                .setImageDrawable(getDrawable(R.drawable.ic_report_problem_92));
//                        centerExplanationLabel = getExplanationFromErrorCode(msg.arg1);
//                        centerTextLabel = R.string.install_failed;
//                        mLaunchButton.setVisibility(View.GONE);
                    	 centerExplanationLabel = getExplanationFromErrorCode(msg.arg1);
                         centerTextLabel = R.string.install_failed;
//                         mLaunchButton.setVisibility(View.INVISIBLE);
                         txt_app_sm.setText(R.string.lbl_yjyh_install004) ;
                         txt_app_sm.setTextColor(Color.parseColor("#8A534F")) ;
                         mOkPanel.setVisibility(View.VISIBLE);
                         mStatusTextView.setVisibility(View.GONE) ;
                 		llyt_optimization.setVisibility(View.GONE) ;

                 		llty_app_complete.setVisibility(View.VISIBLE) ;
                 		llty_app_complete.setBackgroundColor(Color.parseColor("#fafafa")) ;
                 		rlyt_bg.setBackgroundColor(Color.parseColor("#fafafa")) ;
                 		mDoneButton.setVisibility(View.VISIBLE) ;
                 		mLaunchButton.setVisibility(View.GONE) ;
                 		app_snippet.setVisibility(View.GONE) ;
                 		 mProgressBar.setVisibility(View.GONE);
                 		 top_divider.setVisibility(View.GONE) ;
                 		 mDoneButton.setOnClickListener(InstallAppProgress.this);
                         return ;
                    }
//                    if (centerExplanationLabel != -1) {
//                        mExplanationTextView.setText(centerExplanationLabel);
//                        findViewById(R.id.center_view).setVisibility(View.GONE);
//                        ((TextView)findViewById(R.id.explanation_status)).setText(centerTextLabel);
//                        findViewById(R.id.explanation_view).setVisibility(View.VISIBLE);
//                    } else {
//                        ((TextView)findViewById(R.id.center_text)).setText(centerTextLabel);
//                        findViewById(R.id.center_view).setVisibility(View.VISIBLE);
//                        findViewById(R.id.explanation_view).setVisibility(View.GONE);
//                    }
//                    mDoneButton.setOnClickListener(InstallAppProgress.this);
//                    mOkPanel.setVisibility(View.VISIBLE);
                    
                   mStatusTextView.setText(centerTextLabel);
                    if (centerExplanationLabel != -1) {
                        mExplanationTextView.setText(centerExplanationLabel);
                        mExplanationTextView.setVisibility(View.VISIBLE);
                    } else {
                        mExplanationTextView.setVisibility(View.GONE);
                    }
                    mDoneButton.setOnClickListener(InstallAppProgress.this);

                    initScanner();
                    searchAlertWindow() ;
                    searchInstallShortcut();

                    break;
                default:
                    break;
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int statusCode = intent.getIntExtra(
                    PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
            if (statusCode == PackageInstaller.STATUS_PENDING_USER_ACTION) {
                context.startActivity((Intent)intent.getParcelableExtra(Intent.EXTRA_INTENT));
            } else {
                onPackageInstalled(statusCode);
            }
        }
    };

    private int getExplanationFromErrorCode(int errCode) {
        Log.d(TAG, "Installation error code: " + errCode);
        switch (errCode) {
            case PackageInstaller.STATUS_FAILURE_BLOCKED:
                return R.string.install_failed_blocked;
            case PackageInstaller.STATUS_FAILURE_CONFLICT:
                return R.string.install_failed_conflict;
            case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE:
                return R.string.install_failed_incompatible;
            case PackageInstaller.STATUS_FAILURE_INVALID:
                return R.string.install_failed_invalid_apk;
            default:
                return -1;
        }
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mScannerAdPlugin = new ScannerAdPlugin(mAdPluginHandler) ;
        Intent intent = getIntent();
        mAppInfo = intent.getParcelableExtra(PackageUtil.INTENT_ATTR_APPLICATION_INFO);
        mPackageURI = intent.getData();
      
        final String scheme = mPackageURI.getScheme();
        if (scheme != null && !"file".equals(scheme) && !"package".equals(scheme)) {
            throw new IllegalArgumentException("unexpected scheme " + scheme);
        }

        mInstallThread = new HandlerThread("InstallThread");
        mInstallThread.start();
        mInstallHandler = new Handler(mInstallThread.getLooper());

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_ACTION);
        registerReceiver(
                mBroadcastReceiver, intentFilter, BROADCAST_SENDER_PERMISSION, null /*scheduler*/);

        initView();
    }

    @Override
    public void onBackPressed() {
        clearCachedApkIfNeededAndFinish();
    }

    @SuppressWarnings("deprecation")
    @Override
    public Dialog onCreateDialog(int id, Bundle bundle) {
        switch (id) {
        case DLG_OUT_OF_SPACE:
            String dlgText = getString(R.string.out_of_space_dlg_text, mLabel);
            return new mst.app.dialog.AlertDialog.Builder(this)
                    .setTitle(R.string.out_of_space_dlg_title)
                    .setMessage(dlgText)
                    .setPositiveButton(R.string.manage_applications, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //launch manage applications
                            Intent intent = new Intent("android.intent.action.MANAGE_PACKAGE_STORAGE");
                            startActivity(intent);
                            clearCachedApkIfNeededAndFinish();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "Canceling installation");
                            clearCachedApkIfNeededAndFinish();
                        }
                    })
                    .setOnCancelListener(this)
                    .create();
        }
       return null;
   }

    @SuppressWarnings("deprecation")
    private void showDialogInner(int id) {
        removeDialog(id);
        showDialog(id);
    }

    void onPackageInstalled(int statusCode) {
        Message msg = mHandler.obtainMessage(INSTALL_COMPLETE);
        msg.arg1 = statusCode;
        mHandler.sendMessage(msg);
    }

    int getInstallFlags(String packageName) {
        PackageManager pm = getPackageManager();
        try {
            PackageInfo pi =
                    pm.getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            if (pi != null) {
                return PackageManager.INSTALL_REPLACE_EXISTING;
            }
        } catch (NameNotFoundException e) {
        }
        return 0;
    }

    private void doPackageStage(PackageManager pm, PackageInstaller.SessionParams params) {
        final PackageInstaller packageInstaller = pm.getPackageInstaller();
        PackageInstaller.Session session = null;
        try {
            final String packageLocation = mPackageURI.getPath();
            final File file = new File(packageLocation);
            final int sessionId = packageInstaller.createSession(params);
            final byte[] buffer = new byte[65536];

            session = packageInstaller.openSession(sessionId);

            final InputStream in = new FileInputStream(file);
            final long sizeBytes = file.length();
            final OutputStream out = session.openWrite("PackageInstaller", 0, sizeBytes);
            try {
                int c;
                while ((c = in.read(buffer)) != -1) {
                    out.write(buffer, 0, c);
                    if (sizeBytes > 0) {
                        final float fraction = ((float) c / (float) sizeBytes);
                        session.addProgress(fraction);
                    }
                }
                session.fsync(out);
            } finally {
                IoUtils.closeQuietly(in);
                IoUtils.closeQuietly(out);
            }

            // Create a PendingIntent and use it to generate the IntentSender
            Intent broadcastIntent = new Intent(BROADCAST_ACTION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    InstallAppProgress.this /*context*/,
                    sessionId,
                    broadcastIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            session.commit(pendingIntent.getIntentSender());
        } catch (IOException e) {
            onPackageInstalled(PackageInstaller.STATUS_FAILURE);
        } finally {
            IoUtils.closeQuietly(session);
        }
    }

    void initView() {
        setContentView(R.layout.op_progress);
        initScannerView() ;
        
        final PackageUtil.AppSnippet as;
        final PackageManager pm = getPackageManager();
        final int installFlags = getInstallFlags(mAppInfo.packageName);

        if((installFlags & PackageManager.INSTALL_REPLACE_EXISTING )!= 0) {
            Log.w(TAG, "Replacing package:" + mAppInfo.packageName);
        }
        if ("package".equals(mPackageURI.getScheme())) {
            as = new PackageUtil.AppSnippet(pm.getApplicationLabel(mAppInfo),
                    pm.getApplicationIcon(mAppInfo));
        } else {
            final File sourceFile = new File(mPackageURI.getPath());
            as = PackageUtil.getAppSnippet(this, mAppInfo, sourceFile);
        }
        mLabel = as.label;
        PackageUtil.initSnippetForNewApp(this, as, R.id.app_snippet);
        mStatusTextView = (TextView)findViewById(R.id.center_text);
        mExplanationTextView = (TextView) findViewById(R.id.explanation);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setIndeterminate(true);
        // Hide button till progress is being displayed
        mOkPanel = findViewById(R.id.buttons_panel);
        mDoneButton = (Button)findViewById(R.id.done_button);
        mLaunchButton = (Button)findViewById(R.id.launch_button);
        mOkPanel.setVisibility(View.INVISIBLE);
        
        img_app_icon.setImageDrawable(as.icon);
        txt_app_name.setText(as.label) ;

        if ("package".equals(mPackageURI.getScheme())) {
            try {
                pm.installExistingPackage(mAppInfo.packageName);
                onPackageInstalled(PackageInstaller.STATUS_SUCCESS);
            } catch (PackageManager.NameNotFoundException e) {
                onPackageInstalled(PackageInstaller.STATUS_FAILURE_INVALID);
            }
        } else {
            final PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                    PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            params.referrerUri = getIntent().getParcelableExtra(Intent.EXTRA_REFERRER);
            params.originatingUri = getIntent().getParcelableExtra(Intent.EXTRA_ORIGINATING_URI);
            params.originatingUid = getIntent().getIntExtra(Intent.EXTRA_ORIGINATING_UID,
                    UID_UNKNOWN);

            mInstallHandler.post(new Runnable() {
                @Override
                public void run() {
                    doPackageStage(pm, params);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        mInstallThread.getLooper().quitSafely();
    }

    public void onClick(View v) {
        if(v == mDoneButton) {
            if (mAppInfo.packageName != null) {
                Log.i(TAG, "Finished installing "+mAppInfo.packageName);
            }
            clearCachedApkIfNeededAndFinish();
        } else if(v == mLaunchButton) {
            try {
                startActivity(mLaunchIntent);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "Could not start activity", e);
            }
            clearCachedApkIfNeededAndFinish();
        }else if(v == yh_button){
        	yjyh() ;
//      	  Intent intent = new Intent(InstallAppProgress.this,UninstallerActivity.class)  ;
//            intent.setData(Uri.parse(mAppInfo.packageName)) ;
//            intent.putExtra(Intent.EXTRA_USER,  new UserHandle(InstallAppProgress.this.getUserId())) ;
//            intent.putExtra(Intent.EXTRA_UNINSTALL_ALL_USERS, false) ;
//           // intent.putExtra(PackageInstaller.EXTRA_CALLBACK, null) ;
//            InstallAppProgress.this.startActivity(intent) ;
      }else if(v == cxsm_button){
      	Toast.makeText(InstallAppProgress.this, "重新扫描", Toast.LENGTH_LONG).show();
      }else if(v == jx_button){
    	  txt_app_sm.setText(R.string.lbl_yjyh_install001) ;
			mLaunchButton.setVisibility(View.VISIBLE) ;
			yh_button.setVisibility(View.VISIBLE) ;
			jx_button.setVisibility(View.GONE);
			xz_button.setVisibility(View.GONE);
      }else if(v == xz_button){
    	  IPackageManager packageManager =
	                IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
	        PackageDeleteObserver observer = new PackageDeleteObserver();
	        try {
	            packageManager.deletePackageAsUser(mAppInfo.packageName, observer,
	            		android.os.Process.myUserHandle().getIdentifier(),
	            		PackageManager.DELETE_ALL_USERS );
	        } catch (RemoteException e) {
	            // Shouldn't happen.
	            Log.e("--------------", "Failed to talk to package manager");
	        }
      }
    }
    
    
    /**************************************/
    class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
        public void packageDeleted(String packageName, int returnCode) {
        	mDeletePackageHandler.sendMessage(Message.obtain()) ;
        }
    }
    
    private Handler mDeletePackageHandler = new Handler() {
        public void handleMessage(Message msg) {
        	Toast.makeText(InstallAppProgress.this, getString(R.string.uninstall_done), Toast.LENGTH_LONG).show();
        	InstallAppProgress.this.finish();
        }
    } ;
    /***************************************/

    public void onCancel(DialogInterface dialog) {
        clearCachedApkIfNeededAndFinish();
    }

    private void clearCachedApkIfNeededAndFinish() {
        // If we are installing from a content:// the apk is copied in the cache
        // dir and passed in here. As we aren't started for a result because our
        // caller needs to be able to forward the result, here we make sure the
        // staging file in the cache dir is removed.
        if ("file".equals(mPackageURI.getScheme()) && mPackageURI.getPath() != null
                && mPackageURI.getPath().startsWith(getCacheDir().toString())) {
            File file = new File(mPackageURI.getPath());
            file.delete();
        }
        finish();
    }

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		   ck_advertisement.setOnCheckedChangeListener(this) ;
	       // ck_permissions.setOnCheckedChangeListener(this) ;
	        ck_quick.setOnCheckedChangeListener(this) ;
	        ck_suspension_window.setOnCheckedChangeListener(this) ;

			if(!ck_advertisement.isChecked() /*&& !ck_permissions.isChecked()*/ && !ck_quick.isChecked() && !ck_suspension_window.isChecked()){
				yh_button.setVisibility(View.GONE) ;
				//cxsm_button.setVisibility(View.GONE) ;
				mDoneButton.setVisibility(View.VISIBLE) ;
				mLaunchButton.setVisibility(View.VISIBLE) ;
			}else{
				yh_button.setVisibility(View.VISIBLE) ;
				//cxsm_button.setVisibility(View.VISIBLE) ;
				mDoneButton.setVisibility(View.GONE) ;
				mLaunchButton.setVisibility(View.VISIBLE) ;
			}
	}
	
 /**################################# START ##########################################**/
    
    private Handler mAdPluginHandler = new Handler() {
	        public void handleMessage(Message msg) {
	                  switch(msg.what){
	                  case ScannerAdPlugin.MSG_SCANNER_START:
	                	  int centerTextLabel = R.string.lbl_canner_adplugin;
	                	  mStatusTextView.setText(centerTextLabel);
	                	  mOkPanel.setVisibility(View.INVISIBLE);
	                	  
	                	  break;
	                  case ScannerAdPlugin.MSG_SCANNER_CANNEL:
	                	  break;
	                  case ScannerAdPlugin.MSG_SCANNER_END:
	                	  centerTextLabel = R.string.lbl_jb;
	                	  mStatusTextView.setText(centerTextLabel);
	                	  
	                	  centerTextLabel = R.string.install_done;
	                	  mStatusTextView.setText(centerTextLabel);
	                	  mOkPanel.setVisibility(View.VISIBLE);
	                	  
	                	  mQScanResultEntitys = (List<QScanResultEntity>)msg.obj ;
	                	  setData();
	                	  //Toast.makeText(InstallAppProgress.this, "扫描： " + mQScanResultEntitys.size(), Toast.LENGTH_LONG).show() ;
	                	  break;
	                  case ScannerAdPlugin.MSG_SCANNER_ERROR:
	                	  break;
	                  case ScannerAdPlugin.MSG_SCANNER_NO_FOUND:
	                	  break;
	                  case ScannerAdPlugin.MSG_SCANNER_PAUSE:
	                	  break;
	                  }	
	        }
     } ;
    
    private void initScanner(){
    	
    	mScannerAdPlugin.startScanner( mAppInfo.packageName) ;
    }
    
    private void searchAlertWindow(){
    	boolean flag = PermUtils.isPermission(InstallAppProgress.this, mAppInfo.packageName, "android.permission.SYSTEM_ALERT_WINDOW") ;
    	if(flag){
    		txt_app_sm.setText(R.string.lbl_yjyh_install001) ;
    		rlyt_suspension_window.setVisibility(View.VISIBLE) ;
    		ck_suspension_window.setChecked(true) ;
    		
    		rlyt_suspension_window.postDelayed(new Runnable() {
				@Override
				public void run() {
					// 设置询问权限
		    		mstUpdatePermissionStatusToDb(InstallAppProgress.this, mAppInfo.packageName, Manifest.permission.SYSTEM_ALERT_WINDOW, 0);
				}
			}, 1000) ;
    		
    	}else{
    		rlyt_suspension_window.setVisibility(View.GONE) ;
    		ck_suspension_window.setChecked(false) ;
      	}
    }
    
    @SuppressLint("InlinedApi")
	private void searchInstallShortcut(){
    	boolean flag = PermUtils.isPermission(InstallAppProgress.this, mAppInfo.packageName, "com.android.launcher.permission.INSTALL_SHORTCUT") ;
    	if(flag){
    		txt_app_sm.setText(R.string.lbl_yjyh_install001) ;
    		rlyt_quick.setVisibility(View.VISIBLE) ;
    		ck_quick.setChecked(true) ;
    		
    		rlyt_quick.postDelayed(new Runnable() {
				@Override
				public void run() {
					// 设置询问权限
		    		mstUpdatePermissionStatusToDb(InstallAppProgress.this, mAppInfo.packageName, Manifest.permission.INSTALL_SHORTCUT, 0);
				}
			}, 1000) ;
    		
    	}else{
    		rlyt_quick.setVisibility(View.GONE) ;
    		ck_quick.setChecked(false) ;
    	}
    }
    

	private void initScannerView(){
		  top_divider = (FrameLayout)findViewById(R.id.top_divider) ;
		llty_app_complete = (LinearLayout)findViewById(R.id.llty_app_complete) ;
		app_snippet = (LinearLayout)findViewById(R.id.app_snippet) ;
		app_yh_complete = (LinearLayout)findViewById(R.id.app_yh_complete) ;
		rlyt_bg = (RelativeLayout)findViewById(R.id.rlyt_bg) ;
		
		img_app_icon = (ImageView)findViewById(R.id.img_app_icon) ; 
		 txt_app_name = (TextView)findViewById(R.id.txt_app_name) ;
		 txt_app_sm  = (TextView)findViewById(R.id.txt_app_sm) ;
		
		llyt_optimization = (LinearLayout)findViewById(R.id.app_optimization) ;

    	tv_advertisement = (TextView)findViewById(R.id.tv_advertisement) ;
    	tv_permission = (TextView)findViewById(R.id.tv_permission) ;
    	tv_quick = (TextView)findViewById(R.id.tv_quick) ;
    	tv_suspension_window  = (TextView)findViewById(R.id.tv_suspension_window) ;
        ck_advertisement = (CheckBox)findViewById(R.id.ck_advertisement) ;
        ck_permissions = (CheckBox)findViewById(R.id.ck_permissions) ;
        ck_quick = (CheckBox)findViewById(R.id.ck_quick) ;
        ck_suspension_window  = (CheckBox)findViewById(R.id.ck_suspension_window) ;
        yh_button = (Button)findViewById(R.id.yh_button) ;
        cxsm_button = (Button)findViewById(R.id.cxsm_button) ;
        
        jx_button = (Button)findViewById(R.id.jx_button) ;
        xz_button = (Button)findViewById(R.id.xz_button) ;
        
        rlyt_advertisement = (RelativeLayout)findViewById(R.id.rlyt_advertisement) ;
        rlyt_permission = (RelativeLayout)findViewById(R.id.rlyt_permission) ;
        rlyt_quick = (RelativeLayout)findViewById(R.id.rlyt_quick) ;
        rlyt_suspension_window = (RelativeLayout)findViewById(R.id.rlyt_suspension_window) ;
        
        yh_button.setOnClickListener(this) ;
        cxsm_button.setOnClickListener(this) ;
        ck_advertisement.setOnCheckedChangeListener(this) ;
       // ck_permissions.setOnCheckedChangeListener(this) ;
        ck_quick.setOnCheckedChangeListener(this) ;
        ck_suspension_window.setOnCheckedChangeListener(this) ;
        
        jx_button.setOnClickListener(this);
        xz_button.setOnClickListener(this);
        
        ck_permissions.setChecked(false) ;
        rlyt_permission.setVisibility(View.GONE) ;
    }
	
	private void setData(){
		mStatusTextView.setVisibility(View.GONE) ;
		llyt_optimization.setVisibility(View.VISIBLE) ;

		llty_app_complete.setVisibility(View.VISIBLE) ;
		mDoneButton.setVisibility(View.GONE) ;
		mLaunchButton.setVisibility(View.VISIBLE) ;
		app_snippet.setVisibility(View.GONE) ;
		yh_button.setVisibility(View.VISIBLE) ;
//		cxsm_button.setVisibility(View.VISIBLE) ;
		 mProgressBar.setVisibility(View.GONE);
		 top_divider.setVisibility(View.GONE) ;
		if(mQScanResultEntitys != null && mQScanResultEntitys.size() > 0 ){
			QScanResultEntity entity = mQScanResultEntitys.get(0) ;
			if(entity.plugins != null && entity.plugins.size() > 0){
				txt_app_sm.setText(R.string.lbl_yjyh_install001) ;
				rlyt_advertisement.setVisibility(View.VISIBLE) ;
				ck_advertisement.setChecked(true) ;
				tv_advertisement.setText(String.format(getString(R.string.lbl_advertisement), entity.plugins.size())) ;
			}else{
				rlyt_advertisement.setVisibility(View.GONE) ;
				ck_advertisement.setChecked(false) ;
			}
			
			// 安装按钮是否打开
			int flag = Settings.Secure.getInt(InstallAppProgress.this.getContentResolver(), SP_INSTALL_DETECTION, 0);
			if(isRisk() && flag == 1){
				txt_app_sm.setText(R.string.lbl_yjyh_install005) ;
				txt_app_sm.setTextColor(Color.parseColor("#c54b4a"));
				mLaunchButton.setVisibility(View.GONE) ;
				yh_button.setVisibility(View.GONE) ;
				jx_button.setVisibility(View.VISIBLE);
				xz_button.setVisibility(View.VISIBLE);
				
			}
		}else{
			rlyt_advertisement.setVisibility(View.GONE) ;
			ck_advertisement.setChecked(false) ;
		}
	}
	
	/**
	 * 判断是否存在病毒风险
	 * @return
	 */
	private boolean isRisk(){
		if(mQScanResultEntitys != null && mQScanResultEntitys.size() > 0 ){
			for(QScanResultEntity entity:mQScanResultEntitys){
				if(!(entity.type == QScanConstants.TYPE_OK)){
					return true ;
				}
			}
		}
		return false ;
	}
	
	/***
	 * 一键优化
	 */
	@SuppressLint("InlinedApi")
	private void yjyh(){
		/**
		 * 拦截广告插件
		 */
		if(ck_advertisement.isChecked() && mQScanResultEntitys != null && mQScanResultEntitys.size() > 0 ){
//			Toast.makeText(this, "拦截广告", Toast.LENGTH_LONG).show() ;
			QScanResultEntity entity = mQScanResultEntitys.get(0) ;
			ScannerUtils.addOrUpdateAdInfo(InstallAppProgress.this, entity) ;
		}
		
		/**
		 * 禁止悬浮窗
		 */
		if(ck_suspension_window.isChecked()){
//			setCanDrawOverlay(false);
			mstUpdatePermissionStatusToDb(InstallAppProgress.this, mAppInfo.packageName, Manifest.permission.SYSTEM_ALERT_WINDOW, -1);
		}
		
		/***
		 * 禁止自动创建快捷方式
		 */
		if(ck_quick.isChecked()){
//			ShortcutSqlite db = new ShortcutSqlite(InstallAppProgress.this) ;
//			db.save(mAppInfo.packageName, mLabel + "") ;
			mstUpdatePermissionStatusToDb(InstallAppProgress.this, mAppInfo.packageName, Manifest.permission.INSTALL_SHORTCUT, -1);
		}
		
		app_snippet.setVisibility(View.GONE) ;
		llyt_optimization.setVisibility(View.GONE) ;
		app_yh_complete.setVisibility(View.VISIBLE) ;
		llty_app_complete.setVisibility(View.VISIBLE) ;
		yh_button.setVisibility(View.GONE) ;
//		cxsm_button.setVisibility(View.GONE) ;
		mDoneButton.setVisibility(View.VISIBLE) ;
		mLaunchButton.setVisibility(View.VISIBLE) ;
		
//		Toast.makeText(InstallAppProgress.this, getString(R.string.lbl_yjyh_install_message), Toast.LENGTH_LONG).show();
		txt_app_sm.setText(R.string.lbl_yjyh_install003) ;
	}
    
    private static PackageInfo getPackageInfo(Activity activity, String packageName) {
        try {
            return activity.getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
    
    private AppOpsManager mAppOpsManager;
    protected PackageInfo mPackageInfo;
    /**
     * 设置是否禁止显示悬浮窗
     * @param newState true 可以显示 ，false 禁止显示
     */
    private void setCanDrawOverlay(boolean newState) {
    	try{
    	 mAppOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
    	 mPackageInfo = getPackageInfo(this, mAppInfo.packageName);
    	 
        mAppOpsManager.setMode(AppOpsManager.OP_SYSTEM_ALERT_WINDOW,
                mPackageInfo.applicationInfo.uid, mAppInfo.packageName, newState
                ? AppOpsManager.MODE_ALLOWED : AppOpsManager.MODE_ERRORED);
        
        
//        mAppOpsManager.setMode(AppOpsManager.OP_TOAST_WINDOW,
//                mPackageInfo.applicationInfo.uid, mAppInfo.packageName, newState
//                ? AppOpsManager.MODE_ALLOWED : AppOpsManager.MODE_ERRORED);
        
    	}catch(Exception e){
    		e.printStackTrace() ;
    	}
    }
    
    /**
     * 修改应用权限
     * 
     * @param context
     * @param packageName 包名
     * @param permGroup 权限名：快捷方式为 Manifest.permission.INSTALL_SHORTCUT
     * @param status 权限状态：1为允许， 0为询问，-1为拦截
     */
    public static void mstUpdatePermissionStatusToDb(Context context, String packageName, String permGroup, int status) {
    	try {
    		//Log.e("-----------------------------", "packageName = "  + packageName + "    permGroup = " + permGroup + "   status = " + status);
    		Uri GN_PERM_URI = Uri.parse("content://com.monster.settings.PermissionProvider/permissions");
    		ContentValues cv = new ContentValues();
    		cv.put("status", status);
    		int i = context.getContentResolver().update(GN_PERM_URI,cv, " packagename = ? and permissiongroup =?",
    				new String[] {packageName, permGroup});
    		//Log.e("-----------------------------", "i = " + i);
    	} catch (Exception e){
    		//Log.e("-----------------------------", e.toString() );
    		e.printStackTrace();
    	}
    }
    
    /***############################### END ###################################***/
}
