package com.monster.appmanager.viewhelp;

import com.monster.appmanager.MainActivity;
import com.monster.appmanager.R;
import com.monster.appmanager.Settings.ManagePermissionsActivity;
import com.monster.appmanager.Utils;
import com.monster.appmanager.Settings.DataUsageSummaryActivity;
import com.monster.appmanager.Settings.ManageDefaultAppsActivity;
import com.monster.appmanager.Settings.NotificationSettingsActivity;
import com.monster.appmanager.Settings.StorageUseActivity;
import com.monster.appmanager.applications.ManageApplications;
import com.monster.appmanager.virusscan.AdScanningActivity;
import com.monster.appmanager.virusscan.ScannerActivity;
import com.monster.appmanager.virusscan.VirusScanMain;
import com.monster.permission.ui.ManagePermissionsInfoActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 主界面功能列表
 * 
 * @author luolaigang
 * 
 */
@SuppressLint("NewApi")
public class FunctionsHelp implements OnClickListener {
	public static final String TAG = "FunctionsHelp";
	public static final int REQUEST_CODE = 10000;
	public static final int REQUEST_CODE_ONE_KEY_SCAN = 10001;
	private int[] functionIds = { R.id.function1, R.id.function2,
			R.id.function3, R.id.function4, R.id.function5, R.id.function6,
			R.id.function7, };
	private int[] functionTitless = { R.string.authority_management, R.string.advertising_interception,
			R.string.self_starting, R.string.network_management, R.string.space_management, R.string.default_software,
			R.string.notification_management, };
	private int[] functionIcons = { R.drawable.authority_management, R.drawable.intercept,
			R.drawable.self_starting, R.drawable.network_management, R.drawable.space_management, R.drawable.default_software,
			R.drawable.notification_management, };
	
	private TextView[] functionItems = new TextView[functionIds.length];
	private boolean disableClick = false;

	public void initViews(LinearLayout functionGroup, Context context) {
		LinearLayout linearLayout = (LinearLayout)functionGroup.getChildAt(0);
		functionItems[0] = (TextView)linearLayout.getChildAt(0);
		functionItems[1] = (TextView)linearLayout.getChildAt(1);
		functionItems[2] = (TextView)linearLayout.getChildAt(2);
		functionItems[3] = (TextView)linearLayout.getChildAt(3);
		linearLayout = (LinearLayout)functionGroup.getChildAt(1);
		functionItems[4] = (TextView)linearLayout.getChildAt(0);
		functionItems[5] = (TextView)linearLayout.getChildAt(1);
		functionItems[6] = (TextView)linearLayout.getChildAt(2);
		
		for(int i=0; i<functionItems.length; i++){
			functionItems[i].setOnClickListener(this);
			functionItems[i].setId(functionIds[i]);
			
			((TextView)functionItems[i]).setText(functionTitless[i]);
			((TextView)functionItems[i]).setTextSize(10f);
			Drawable drawable = MainActivity.mainActivity.getResources().getDrawable(functionIcons[i], MainActivity.mainActivity.getTheme());
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			((TextView)functionItems[i]).setCompoundDrawables(null, drawable, null, null);
		}
	}

	//是否打开
	private boolean isOpen = false;
	public void openOrCloseGridView(){
		isOpen = !isOpen;
	}
	@Override
	public void onClick(View view) {
		if(isDisableClick()) {
			return;
		}
		Intent intent = new Intent();
		switch (view.getId()) {
		case R.id.function1:
			intent.setAction(ManagePermissionsInfoActivity.MANAGE_PERMISSIONS);
			intent.setClass(view.getContext(), ManagePermissionsInfoActivity.class);
			//intent.setClass(view.getContext(), ManagePermissionsActivity.class);
			break;
		case R.id.function2:
			SharedPreferences mySharedPreferences= view.getContext().getSharedPreferences(ScannerActivity.TAG, Activity.MODE_PRIVATE);
			boolean hasScan = mySharedPreferences.getBoolean(ScannerActivity.HAS_SCAN, false);
			if(hasScan){//已扫描
				intent.setClass(view.getContext(), VirusScanMain.class);
			}else{
//				intent.setClass(view.getContext(), ScannerActivity.class);
				intent.setClass(view.getContext(), AdScanningActivity.class);
				MainActivity.mainActivity.startActivityForResult(intent, REQUEST_CODE);
				//intent.putExtra(ScannerActivity.FROM_MAIN_ACTIVITY, true);
				return;
			}
			break;
		case R.id.function3:
			intent.setClassName("com.monster.autostart", "com.monster.autostart.activity.AutoStartMangerActivity");
			break;
		case R.id.function4:
			//intent.setClass(view.getContext(), DataUsageSummaryActivity.class);
			intent.setClassName("com.monster.netmanage", "com.monster.netmanage.activity.DataRangeActivity");
			break;
		case R.id.function5:
			Context context = view.getContext();
			VolumeInfo mVolume = context.getSystemService(StorageManager.class).findVolumeById(VolumeInfo.ID_PRIVATE_INTERNAL);
			Bundle args = new Bundle();
            args.putString(ManageApplications.EXTRA_CLASSNAME,
                    StorageUseActivity.class.getName());
            args.putString(ManageApplications.EXTRA_VOLUME_UUID, mVolume.getFsUuid());
            args.putString(ManageApplications.EXTRA_VOLUME_NAME, mVolume.getDescription());
            intent = Utils.onBuildStartFragmentIntent(view.getContext(), ManageApplications.class.getName(), args, null, R.string.apps_storage, null,
                    false);
			break;
		case R.id.function6:
			intent.setClass(view.getContext(), ManageDefaultAppsActivity.class);
			break;
		case R.id.function7:
			intent.setAction("android.intent.action.SHOW_NOTIFY_MANAGE_ACTIVITY");
			break;
		}		
		try{
			view.getContext().startActivity(intent);
		}catch(Exception e){
			e.printStackTrace();
			Toast.makeText(view.getContext(), R.string.coding, Toast.LENGTH_SHORT).show();
		}
	
	}
	
	public boolean isDisableClick() {
		return disableClick;
	}
	public void setDisableClick(boolean disableClick) {
		this.disableClick = disableClick;
	}
}
