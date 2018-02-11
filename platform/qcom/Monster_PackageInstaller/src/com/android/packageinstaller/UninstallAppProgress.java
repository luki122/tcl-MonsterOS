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

import android.app.Activity;
import android.app.admin.IDevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import com.android.packageinstaller.adplugin.ShortcutSqlite;

import tmsdk.fg.creator.ManagerCreatorF;
import tmsdk.fg.module.deepclean.DeepcleanManager;
import tmsdk.fg.module.deepclean.RubbishEntity;
import tmsdk.fg.module.deepclean.RubbishEntityManager;
import tmsdk.fg.module.deepclean.RubbishType;
import tmsdk.fg.module.deepclean.ScanProcessListener;
import tmsdk.fg.module.deepclean.UpdateRubbishDataCallback;
import tmsdk.fg.module.deepclean.rubbish.SoftRubModel;

/**
 * This activity corresponds to a download progress screen that is displayed 
 * when an application is uninstalled. The result of the application uninstall
 * is indicated in the result code that gets set to 0 or 1. The application gets launched
 * by an intent with the intent's class name explicitly set to UninstallAppProgress and expects
 * the application object of the application to uninstall.
 */
public class UninstallAppProgress extends Activity implements OnClickListener ,UpdateRubbishDataCallback{
    private final String TAG="UninstallAppProgress";

    private ApplicationInfo mAppInfo;
    private boolean mAllUsers;
    private UserHandle mUser;
    private IBinder mCallback;

    private Button mOkButton;
    private Button mDeviceManagerButton;
    private Button mUsersButton;
    private volatile int mResultCode = -1;

    /**
     * If initView was called. We delay this call to not have to call it at all if the uninstall is
     * quick
     */
    private boolean mIsViewInitialized;

    /** Amount of time to wait until we show the UI */
    private static final int QUICK_INSTALL_DELAY_MILLIS = 50;

    private static final int UNINSTALL_COMPLETE = 1;
    private static final int UNINSTALL_IS_SLOW = 2;
    
    /********************************/
    private boolean mIsCleanFile = false ;
    /** 我是来找垃圾的 */
	private DeepcleanManager mDeepcleanManager;
    /********************************/

    private boolean isProfileOfOrSame(UserManager userManager, int userId, int profileId) {
        if (userId == profileId) {
            return true;
        }
        UserInfo parentUser = userManager.getProfileParent(profileId);
        return parentUser != null && parentUser.id == userId;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (isFinishing() || isDestroyed()) {
                return;
            }

            switch (msg.what) {
                case UNINSTALL_IS_SLOW:
                    initView();
                    break;
                case UNINSTALL_COMPLETE:
                    mHandler.removeMessages(UNINSTALL_IS_SLOW);

                    if (msg.arg1 != PackageManager.DELETE_SUCCEEDED) {
                        initView();
                    }

                    mResultCode = msg.arg1;
                    final String packageName = (String) msg.obj;

                    if (mCallback != null) {
                        final IPackageDeleteObserver2 observer = IPackageDeleteObserver2.Stub
                                .asInterface(mCallback);
                        try {
                            observer.onPackageDeleted(mAppInfo.packageName, mResultCode,
                                    packageName);
                        } catch (RemoteException ignored) {
                        }
                        finish();
                        return;
                    }

                    if (getIntent().getBooleanExtra(Intent.EXTRA_RETURN_RESULT, false)) {
                        Intent result = new Intent();
                        result.putExtra(Intent.EXTRA_INSTALL_RESULT, mResultCode);
                        setResult(mResultCode == PackageManager.DELETE_SUCCEEDED
                                ? Activity.RESULT_OK : Activity.RESULT_FIRST_USER,
                                        result);
                        finish();
                        return;
                    }

                    // Update the status text
                    final String statusText;
                    switch (msg.arg1) {
                        case PackageManager.DELETE_SUCCEEDED:
                            statusText = getString(R.string.uninstall_done);
                            // Show a Toast and finish the activity
                            Context ctx = getBaseContext();
                            Toast.makeText(ctx, statusText, Toast.LENGTH_LONG).show();
                            //ShortcutSqlite sqlite = new ShortcutSqlite(UninstallAppProgress.this) ;
                            
                            //sqlite.delete(mAppInfo.packageName, "");
                            /************************垃圾扫描清理 start****************************/
                            if(mIsCleanFile){
//                            	Toast.makeText(ctx, "正在清理残留文件", Toast.LENGTH_LONG).show();
                            	cleanCanceFile();
                            }else{
//                            	Toast.makeText(ctx, "没有清理残留文件", Toast.LENGTH_LONG).show();
//                            	setResultAndFinish(mResultCode);
                            }
                            /****************************end**********************************/
                            
                            setResultAndFinish(mResultCode);
                            return;
                        case PackageManager.DELETE_FAILED_DEVICE_POLICY_MANAGER: {
                            UserManager userManager =
                                    (UserManager) getSystemService(Context.USER_SERVICE);
                            IDevicePolicyManager dpm = IDevicePolicyManager.Stub.asInterface(
                                    ServiceManager.getService(Context.DEVICE_POLICY_SERVICE));
                            // Find out if the package is an active admin for some non-current user.
                            int myUserId = UserHandle.myUserId();
                            UserInfo otherBlockingUser = null;
                            for (UserInfo user : userManager.getUsers()) {
                                // We only catch the case when the user in question is neither the
                                // current user nor its profile.
                                if (isProfileOfOrSame(userManager, myUserId, user.id)) continue;

                                try {
                                    if (dpm.packageHasActiveAdmins(packageName, user.id)) {
                                        otherBlockingUser = user;
                                        break;
                                    }
                                } catch (RemoteException e) {
                                    Log.e(TAG, "Failed to talk to package manager", e);
                                }
                            }
                            if (otherBlockingUser == null) {
                                Log.d(TAG, "Uninstall failed because " + packageName
                                        + " is a device admin");
                                mDeviceManagerButton.setVisibility(View.VISIBLE);
                                statusText = getString(
                                        R.string.uninstall_failed_device_policy_manager);
                            } else {
                                Log.d(TAG, "Uninstall failed because " + packageName
                                        + " is a device admin of user " + otherBlockingUser);
                                mDeviceManagerButton.setVisibility(View.GONE);
                                statusText = String.format(
                                        getString(R.string.uninstall_failed_device_policy_manager_of_user),
                                        otherBlockingUser.name);
                            }
                            break;
                        }
                        case PackageManager.DELETE_FAILED_OWNER_BLOCKED: {
                            UserManager userManager =
                                    (UserManager) getSystemService(Context.USER_SERVICE);
                            IPackageManager packageManager = IPackageManager.Stub.asInterface(
                                    ServiceManager.getService("package"));
                            List<UserInfo> users = userManager.getUsers();
                            int blockingUserId = UserHandle.USER_NULL;
                            for (int i = 0; i < users.size(); ++i) {
                                final UserInfo user = users.get(i);
                                try {
                                    if (packageManager.getBlockUninstallForUser(packageName,
                                            user.id)) {
                                        blockingUserId = user.id;
                                        break;
                                    }
                                } catch (RemoteException e) {
                                    // Shouldn't happen.
                                    Log.e(TAG, "Failed to talk to package manager", e);
                                }
                            }
                            int myUserId = UserHandle.myUserId();
                            if (isProfileOfOrSame(userManager, myUserId, blockingUserId)) {
                                mDeviceManagerButton.setVisibility(View.VISIBLE);
                            } else {
                                mDeviceManagerButton.setVisibility(View.GONE);
                                mUsersButton.setVisibility(View.VISIBLE);
                            }
                            // TODO: b/25442806
                            if (blockingUserId == UserHandle.USER_SYSTEM) {
                                statusText = getString(R.string.uninstall_blocked_device_owner);
                            } else if (blockingUserId == UserHandle.USER_NULL) {
                                Log.d(TAG, "Uninstall failed for " + packageName + " with code "
                                        + msg.arg1 + " no blocking user");
                                statusText = getString(R.string.uninstall_failed);
                            } else {
                                statusText = mAllUsers
                                        ? getString(R.string.uninstall_all_blocked_profile_owner) :
                                        getString(R.string.uninstall_blocked_profile_owner);
                            }
                            break;
                        }
                        default:
                            Log.d(TAG, "Uninstall failed for " + packageName + " with code "
                                    + msg.arg1);
                            statusText = getString(R.string.uninstall_failed);
                            break;
                    }
                    findViewById(R.id.progress_view).setVisibility(View.GONE);
                    findViewById(R.id.status_view).setVisibility(View.VISIBLE);
                    ((TextView)findViewById(R.id.status_text)).setText(statusText);
                    findViewById(R.id.ok_panel).setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Intent intent = getIntent();
        mAppInfo = intent.getParcelableExtra(PackageUtil.INTENT_ATTR_APPLICATION_INFO);
        mCallback = intent.getIBinderExtra(PackageInstaller.EXTRA_CALLBACK);
        
        /**********************2016/8/10 19:46  start ***************************/
        
        mIsCleanFile = intent.getBooleanExtra("CLEAN_FILE", false) ;
        
        mDeepcleanManager = ManagerCreatorF.getManager(DeepcleanManager.class);
		mDeepcleanManager.appendWhitePath("/tencent/mobileqq");
		mDeepcleanManager.appendWhitePath("/tencent/MicroMsg");
		ScanProcessListener listener = initProcessListener();// 扫描进程
		if (mDeepcleanManager.init(listener)) {
			//Toast.makeText(this, "初始化垃圾成功", Toast.LENGTH_LONG).show();
		} else {
			//Toast.makeText(this, "初始化垃圾失败", Toast.LENGTH_LONG).show();
		}
		mDeepcleanManager.insertUninstallPkg("com.tencent.mm");
		mDeepcleanManager.updateRubbishData(this) ;
		
        /*************************end*********************************/

        // This currently does not support going through a onDestroy->onCreate cycle. Hence if that
        // happened, just fail the operation for mysterious reasons.
        if (icicle != null) {
            mResultCode = PackageManager.DELETE_FAILED_INTERNAL_ERROR;

            if (mCallback != null) {
                final IPackageDeleteObserver2 observer = IPackageDeleteObserver2.Stub
                        .asInterface(mCallback);
                try {
                    observer.onPackageDeleted(mAppInfo.packageName, mResultCode, null);
                } catch (RemoteException ignored) {
                }
                finish();
            } else {
                setResultAndFinish(mResultCode);
            }

            return;
        }

        mAllUsers = intent.getBooleanExtra(Intent.EXTRA_UNINSTALL_ALL_USERS, false);
        if (mAllUsers && !UserManager.get(this).isAdminUser()) {
            throw new SecurityException("Only admin user can request uninstall for all users");
        }
        mUser = intent.getParcelableExtra(Intent.EXTRA_USER);
        if (mUser == null) {
            mUser = android.os.Process.myUserHandle();
        } else {
            UserManager userManager = (UserManager) getSystemService(Context.USER_SERVICE);
            List<UserHandle> profiles = userManager.getUserProfiles();
            if (!profiles.contains(mUser)) {
                throw new SecurityException("User " + android.os.Process.myUserHandle() + " can't "
                        + "request uninstall for user " + mUser);
            }
        }

        PackageDeleteObserver observer = new PackageDeleteObserver();

        // Make window transparent until initView is called. In many cases we can avoid showing the
        // UI at all as the app is uninstalled very quickly. If we show the UI and instantly remove
        // it, it just looks like a flicker.
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        getPackageManager().deletePackageAsUser(mAppInfo.packageName, observer,
                mAllUsers ? PackageManager.DELETE_ALL_USERS : 0, mUser.getIdentifier());

        mHandler.sendMessageDelayed(mHandler.obtainMessage(UNINSTALL_IS_SLOW),
                QUICK_INSTALL_DELAY_MILLIS);
    }
    
    class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
        public void packageDeleted(String packageName, int returnCode) {
            Message msg = mHandler.obtainMessage(UNINSTALL_COMPLETE);
            msg.arg1 = returnCode;
            msg.obj = packageName;
            mHandler.sendMessage(msg);
        }
    }
    
    void setResultAndFinish(int retCode) {
        setResult(retCode);
        finish();
    }
    
    public void initView() {
        if (mIsViewInitialized) {
            return;
        }
        mIsViewInitialized = true;

        // We set the window background to translucent in constructor, revert this
        TypedValue attribute = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.windowBackground, attribute, true);
        if (attribute.type >= TypedValue.TYPE_FIRST_COLOR_INT &&
                attribute.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            getWindow().setBackgroundDrawable(new ColorDrawable(attribute.data));
        } else {
            getWindow().setBackgroundDrawable(getResources().getDrawable(attribute.resourceId,
                    getTheme()));
        }

        getTheme().resolveAttribute(android.R.attr.navigationBarColor, attribute, true);
        getWindow().setNavigationBarColor(attribute.data);

        getTheme().resolveAttribute(android.R.attr.statusBarColor, attribute, true);
        getWindow().setStatusBarColor(attribute.data);

        boolean isUpdate = ((mAppInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
        setTitle(isUpdate ? R.string.uninstall_update_title : R.string.uninstall_application_title);

        setContentView(R.layout.uninstall_progress);
        // Initialize views
        View snippetView = findViewById(R.id.app_snippet);
        PackageUtil.initSnippetForInstalledApp(this, mAppInfo, snippetView);
        mDeviceManagerButton = (Button) findViewById(R.id.device_manager_button);
        mUsersButton = (Button) findViewById(R.id.users_button);
        mDeviceManagerButton.setVisibility(View.GONE);
        mDeviceManagerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClassName("com.android.settings",
                        "com.android.settings.Settings$DeviceAdminSettingsActivity");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        mUsersButton.setVisibility(View.GONE);
        mUsersButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_USER_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        // Hide button till progress is being displayed
        mOkButton = (Button) findViewById(R.id.ok_button);
        mOkButton.setOnClickListener(this);
    }

    public void onClick(View v) {
        if(v == mOkButton) {
            Log.i(TAG, "Finished uninstalling pkg: " + mAppInfo.packageName);
            setResultAndFinish(mResultCode);
        }
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent ev) {
        if (ev.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (mResultCode == -1) {
                // Ignore back key when installation is in progress
                return true;
            } else {
                // If installation is done, just set the result code
                setResult(mResultCode);
            }
        }
        return super.dispatchKeyEvent(ev);
    }
    
    
  /*************************2016/8/10 19:58 start**************************/
    
    private void cleanCanceFile(){
    /*	int flags = RubbishType.SCAN_FLAG_ALL;
		if(mDeepcleanManager.startScan(flags)) {
			//Toast.makeText(this,"清扫垃圾success", Toast.LENGTH_LONG).show() ;
		}else{
			//Toast.makeText(this,"清扫垃圾error", Toast.LENGTH_LONG).show() ;
		}*/
    	
    	new Thread() {
			public void run() {
				SoftRubModel _SoftRubModel = mDeepcleanManager
						.scanSoftRubbish(mAppInfo.packageName);
				if (null != _SoftRubModel) {
					mDeepcleanManager.cleanSoftRubModelRubbish(_SoftRubModel);
					Log.e("SoftRubModel", "appName ::" + _SoftRubModel.mAppName);
					Log.e("SoftRubModel", "mRubbishFileSize ::" + _SoftRubModel.mRubbishFileSize);
				} else {
					Log.e("SoftRubModel", "Rubbish not found!!!");
				}

			}
		}.start();
    }
    
    /****************************end************************************/

    /****************************start*************************************/
    // 任务进程开启
    	private ScanProcessListener initProcessListener() {

    		return new ScanProcessListener() {
    			@Override
    			public void onScanStarted( ) {
    				
    			}
    	
    			@Override
    			public void onScanProcessChange(int nowPercent, String scanPath){ 
    				Log.e(TAG,"onScanProcessChange : "+nowPercent+ " %"   + "    scanPath = " + scanPath);
    			}
    			
    			public void onRubbishFound(RubbishEntity aRubbish){
    				
    			} 
    			
    			@Override
    			public void onScanFinished() { 
    				Log.i(TAG,"onScanFinished : "  );
    			 /*List<RubbishEntity> _Rubbishes = mDeepcleanManager.getmRubbishEntityManager().getRubbishes();
    				
    				for(RubbishEntity aRubbish:_Rubbishes){
    					Log.e("****", "" +mAppInfo.packageName + "       " + aRubbish.getPackageName()) ;
    					Log.e("-*-----------------------------------", "description = " + aRubbish.description + " , " + aRubbish.getAppName() + " , " + aRubbish.getPackageName() + " " ) ;
    					for(String path:aRubbish.getRubbishKey()){
    						Log.e("----------------------------------", "path = " + path) ;
    					}
    					if((!TextUtils.isEmpty(aRubbish.getPackageName()))
   							&& (mAppInfo.packageName.equals(aRubbish.getPackageName())  ||
   									mAppInfo.packageName.startsWith(aRubbish.getPackageName()) ||
   									aRubbish.getPackageName().startsWith(mAppInfo.packageName))){
    						aRubbish.setStatus(RubbishType.MODEL_TYPE_SELECTED);
    						Log.i("----------------------------------", "找到了。。。。。。。。。。。。。。。。。 ") ;
    						Log.e("----------------------------------", "找到了。。。。。。。。。。。。。。。。。 ") ;
    						Log.e("----------------------------------", "找到了。。。。。。。。。。。。。。。。。 ") ;
    						Log.e("----------------------------------", "找到了。。。。。。。。。。。。。。。。。 ") ;
    						Log.e("----------------------------------", "找到了。。。。。。。。。。。。。。。。。 ") ;
    						
    					}else{
    						aRubbish.setStatus(RubbishType.MODEL_TYPE_UNSELECTED);
    					}
    				}
    				mDeepcleanManager.startClean();*/
    				
    				
    				RubbishEntityManager _rubbishManager = mDeepcleanManager.getmRubbishEntityManager();
    				List<RubbishEntity> _rubbish =  _rubbishManager.getRubbishes();
    				for(RubbishEntity _aRubbish :_rubbish ){
    					Log.e("onClick"," "+_aRubbish.getPackageName());
    					if ((!TextUtils.isEmpty(_aRubbish.getPackageName()))
    							&& (mAppInfo.packageName.equals(_aRubbish.getPackageName())  ||
       									mAppInfo.packageName.startsWith(_aRubbish.getPackageName()) ||
       									_aRubbish.getPackageName().startsWith(mAppInfo.packageName))) {
    						// 将当前垃圾，设定为选择清除。 在后面的清理过程中，该垃圾会被删除。
    						_aRubbish.setStatus(RubbishType.MODEL_TYPE_DELETED );
    					}
    				}
    				/**
    				 * 注意：开始清理全部垃圾,只清理model.getStatus() == RubbishType.MODEL_TYPE_SELECTED 的垃圾model
    				 * 如果需要自行清理每一项的垃圾，可分别对每一项结果中的files自己实现delete，注意软件垃圾的files中保存的是目录。 
    				 */
    				mDeepcleanManager.startClean();
    				
    			}

     
    			@Override
    			public void onScanCanceled( ) {
    				Log.i(TAG, "onScanCanceled") ;
    			}
    	
      			public void onCleanStart( ) {
      				Log.i(TAG, "onScanCanceled") ;
    			}
      			
    			@Override
    			public void onCleanProcessChange(  long currenCleanSize, int nowPercent) {
    				Log.i(TAG, "onScanCanceled") ;
    			}
     
    			@Override
    			public void onCleanCancel( ) {
    				Log.i(TAG, "onScanCanceled") ;
//    				Message msg = Message.obtain() ;
//    				msg.what = CLEAN_CANCEL ;
//    				mUIHandler.sendMessage(msg) ;
    			}

    			/***
    			 * 完全清理完毕
    			 */
    			public void onCleanFinish() {
    				Log.i(TAG, "onScanCanceled") ;
//    				Message msg = Message.obtain() ;
//    				msg.what = CLEAN_FINISH;
//    				mUIHandler.sendMessage(msg) ;
    			}

    			@Override
    			public void onScanError(int error) {
    				Log.i(TAG, "onScanCanceled") ;
//    				Message msg = Message.obtain() ;
//    				msg.what = SCAN_ERROR ;
//    				mUIHandler.sendMessage(msg) ;
    			}

    			@Override
    			public void onCleanError(int error) {
    				Log.i(TAG, "onScanCanceled") ;
//    				Message msg = Message.obtain() ;
//    				msg.what = CLEAN_ERROR ;
//    				mUIHandler.sendMessage(msg) ;
    			}
    			
    		};
    	}
    	
   	@Override
   	public void updateFinished() {
   	}
   	
   	private static final int CLEAN_CANCEL = 1001 ;
   	private static final int CLEAN_FINISH = 1002 ;
   	private static final int CLEAN_ERROR = 1003 ;
   	private static final int SCAN_ERROR = 1004 ;
   	
   	/** 主线程的handler，你懂的	 */
   	private Handler mUIHandler = new Handler() {

   		@Override
   		public void handleMessage(Message msg) {
   			switch (msg.what) {
   			case CLEAN_CANCEL:
   				Toast.makeText(UninstallAppProgress.this, "CLEAN_CANCEL", Toast.LENGTH_LONG).show();
   				setResultAndFinish(mResultCode);
   				break;
   			case CLEAN_FINISH:
   				Toast.makeText(UninstallAppProgress.this, "CLEAN_FINISH", Toast.LENGTH_LONG).show();
   				setResultAndFinish(mResultCode);
   				break;
   			case CLEAN_ERROR:
   				Toast.makeText(UninstallAppProgress.this, "CLEAN_ERROR", Toast.LENGTH_LONG).show();
   				setResultAndFinish(mResultCode);
   				break;
   			case SCAN_ERROR:
   				Toast.makeText(UninstallAppProgress.this, "SCAN_ERROR", Toast.LENGTH_LONG).show();
   				setResultAndFinish(mResultCode);
   				break;

   			}
   		}
   	
   	};
   	
   	/****************************end**************************************/
}
