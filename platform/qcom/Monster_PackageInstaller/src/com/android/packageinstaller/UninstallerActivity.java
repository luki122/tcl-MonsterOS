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

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;
import tmsdk.fg.creator.ManagerCreatorF;
import tmsdk.fg.module.deepclean.DeepcleanManager;
import tmsdk.fg.module.deepclean.RubbishEntity;
import tmsdk.fg.module.deepclean.RubbishType;
import tmsdk.fg.module.deepclean.ScanProcessListener;
import tmsdk.fg.module.deepclean.UpdateRubbishDataCallback;

/*
 * This activity presents UI to uninstall an application. Usually launched with intent
 * Intent.ACTION_UNINSTALL_PKG_COMMAND and attribute 
 * com.android.packageinstaller.PackageName set to the application package name
 */
public class UninstallerActivity extends Activity implements UpdateRubbishDataCallback{
    private static final String TAG = "UninstallerActivity";
    
    /** 我是来找垃圾的 */
	private DeepcleanManager mDeepcleanManager;
    /********************************/

    public static class UninstallAlertDialogFragment extends DialogFragment implements
            DialogInterface.OnClickListener {
    	
    	View view = null ;
    	TextView txt_uninstall_text ;
    	CheckBox ck_uninstall_clear;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final PackageManager pm = getActivity().getPackageManager();
            final DialogInfo dialogInfo = ((UninstallerActivity) getActivity()).mDialogInfo;
            final CharSequence appLabel = dialogInfo.appInfo.loadLabel(pm);

//            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
//            StringBuilder messageBuilder = new StringBuilder();
            
            /*********************/
            view = LayoutInflater.from(getActivity()).inflate(R.layout.uninstall_dialog_layout,null);
            txt_uninstall_text = (TextView)view.findViewById(R.id.txt_uninstall_text) ;
            ck_uninstall_clear = (CheckBox)view.findViewById(R.id.ck_uninstall_clear) ;
            /*******************/
            
            mst.app.dialog.AlertDialog.Builder dialogBuilder = new mst.app.dialog.AlertDialog.Builder(getActivity());
            dialogBuilder.setView(view);
            StringBuilder messageBuilder = new StringBuilder();

            // If the Activity label differs from the App label, then make sure the user
            // knows the Activity belongs to the App being uninstalled.
            if (dialogInfo.activityInfo != null) {
                final CharSequence activityLabel = dialogInfo.activityInfo.loadLabel(pm);
                if (!activityLabel.equals(appLabel)) {
                    messageBuilder.append(
                            getString(R.string.uninstall_activity_text, activityLabel));
                    messageBuilder.append(" ").append(appLabel).append(".\n\n");
                }
            }

            final boolean isUpdate =
                    ((dialogInfo.appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
            UserManager userManager = UserManager.get(getActivity());
            if (isUpdate) {
                if (isSingleUser(userManager)) {
                    messageBuilder.append(getString(R.string.uninstall_update_text));
                } else {
                    messageBuilder.append(getString(R.string.uninstall_update_text_multiuser));
                }
            } else {
                if (dialogInfo.allUsers && !isSingleUser(userManager)) {
                    messageBuilder.append(getString(R.string.uninstall_application_text_all_users));
                } else if (!dialogInfo.user.equals(android.os.Process.myUserHandle())) {
                    UserInfo userInfo = userManager.getUserInfo(dialogInfo.user.getIdentifier());
                    messageBuilder.append(
                            getString(R.string.uninstall_application_text_user, userInfo.name));
                } else {
                    messageBuilder.append(getString(R.string.uninstall_application_text));
                }
            }

//            dialogBuilder.setTitle(appLabel);
//            dialogBuilder.setIcon(dialogInfo.appInfo.loadIcon(pm));
//            dialogBuilder.setPositiveButton(android.R.string.ok, this);
//            dialogBuilder.setNegativeButton(android.R.string.cancel, this);
//            dialogBuilder.setMessage(messageBuilder.toString());
            
            dialogBuilder.setTitle(appLabel);
            dialogBuilder.setIcon(dialogInfo.appInfo.loadIcon(pm));
            dialogBuilder.setPositiveButton(android.R.string.ok, this);
            dialogBuilder.setNegativeButton(android.R.string.cancel, this);
           // dialogBuilder.setMessage(messageBuilder.toString());
            txt_uninstall_text.setText(messageBuilder.toString());
            return dialogBuilder.create();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == Dialog.BUTTON_POSITIVE) {
                ((UninstallerActivity) getActivity()).startUninstallProgress(ck_uninstall_clear.isChecked());
            } else {
                ((UninstallerActivity) getActivity()).dispatchAborted();
            }
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            if (isAdded()) {
                getActivity().finish();
            }
        }

        /**
         * Returns whether there is only one user on this device, not including
         * the system-only user.
         */
        private boolean isSingleUser(UserManager userManager) {
            final int userCount = userManager.getUserCount();
            return userCount == 1
                    || (UserManager.isSplitSystemUser() && userCount == 2);
        }
    }

    public static class AppNotFoundDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new mst.app.dialog.AlertDialog.Builder(getActivity())
                    .setTitle(R.string.app_not_found_dlg_title)
                    .setMessage(R.string.app_not_found_dlg_text)
                    .setNeutralButton(android.R.string.ok, null)
                    .create();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            if (isAdded()) {
                ((UninstallerActivity) getActivity()).dispatchAborted();
                getActivity().setResult(Activity.RESULT_FIRST_USER);
                getActivity().finish();
            }
        }
    }

    static class DialogInfo {
        ApplicationInfo appInfo;
        ActivityInfo activityInfo;
        boolean allUsers;
        UserHandle user;
        IBinder callback;
    }

    private String mPackageName;
    private DialogInfo mDialogInfo;

    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(Color.TRANSPARENT);
//			 window.setNavigationBarColor(Color.TRANSPARENT);
		}
        
        /**********************2016/8/10 19:46  start ***************************/

        
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
		mDeepcleanManager.insertUninstallPkg(mPackageName);
		mDeepcleanManager.updateRubbishData(this) ;
		
        /*************************end*********************************/
        
        
        // Get intent information.
        // We expect an intent with URI of the form package://<packageName>#<className>
        // className is optional; if specified, it is the activity the user chose to uninstall
        final Intent intent = getIntent();
        final Uri packageUri = intent.getData();
        if (packageUri == null) {
            Log.e(TAG, "No package URI in intent");
            showAppNotFound();
            return;
        }
        mPackageName = packageUri.getEncodedSchemeSpecificPart();
        if (mPackageName == null) {
            Log.e(TAG, "Invalid package name in URI: " + packageUri);
            showAppNotFound();
            return;
        }

        final IPackageManager pm = IPackageManager.Stub.asInterface(
                ServiceManager.getService("package"));

        mDialogInfo = new DialogInfo();

        mDialogInfo.user = intent.getParcelableExtra(Intent.EXTRA_USER);
        if (mDialogInfo.user == null) {
            mDialogInfo.user = android.os.Process.myUserHandle();
        }

        mDialogInfo.allUsers = intent.getBooleanExtra(Intent.EXTRA_UNINSTALL_ALL_USERS, false);
        mDialogInfo.callback = intent.getIBinderExtra(PackageInstaller.EXTRA_CALLBACK);

        try {
            mDialogInfo.appInfo = pm.getApplicationInfo(mPackageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES, mDialogInfo.user.getIdentifier());
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to get packageName. Package manager is dead?");
        }

        if (mDialogInfo.appInfo == null) {
            Log.e(TAG, "Invalid packageName: " + mPackageName);
            showAppNotFound();
            return;
        }

        // The class name may have been specified (e.g. when deleting an app from all apps)
        final String className = packageUri.getFragment();
        if (className != null) {
            try {
                mDialogInfo.activityInfo = pm.getActivityInfo(
                        new ComponentName(mPackageName, className), 0,
                        mDialogInfo.user.getIdentifier());
            } catch (RemoteException e) {
                Log.e(TAG, "Unable to get className. Package manager is dead?");
                // Continue as the ActivityInfo isn't critical.
            }
        }

        showConfirmationDialog();
    }

    private void showConfirmationDialog() {
        showDialogFragment(new UninstallAlertDialogFragment());
    }

    private void showAppNotFound() {
        showDialogFragment(new AppNotFoundDialogFragment());
    }

    private void showDialogFragment(DialogFragment fragment) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        fragment.show(ft, "dialog");
    }

    void startUninstallProgress(boolean flag) {
        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        newIntent.putExtra(Intent.EXTRA_USER, mDialogInfo.user);
        newIntent.putExtra(Intent.EXTRA_UNINSTALL_ALL_USERS, mDialogInfo.allUsers);
        newIntent.putExtra(PackageInstaller.EXTRA_CALLBACK, mDialogInfo.callback);
        newIntent.putExtra(PackageUtil.INTENT_ATTR_APPLICATION_INFO, mDialogInfo.appInfo);
        if (getIntent().getBooleanExtra(Intent.EXTRA_RETURN_RESULT, false)) {
            newIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        }
        newIntent.putExtra("CLEAN_FILE", flag) ;
        newIntent.setClass(this, UninstallAppProgress.class);
        startActivity(newIntent);
    }

    void dispatchAborted() {
        if (mDialogInfo != null && mDialogInfo.callback != null) {
            final IPackageDeleteObserver2 observer = IPackageDeleteObserver2.Stub.asInterface(
                    mDialogInfo.callback);
            try {
                observer.onPackageDeleted(mPackageName,
                        PackageManager.DELETE_FAILED_ABORTED, "Cancelled by user");
            } catch (RemoteException ignored) {
            }
        }
    }
    
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
}
