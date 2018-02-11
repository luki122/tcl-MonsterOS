package com.monster.autostart.bean;

import java.util.ArrayList;
import java.util.List;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;
import com.monster.autostart.R;

import com.monster.autostart.interfaces.IFilterBehavior;
import com.monster.autostart.utils.Utilities;

public class BroadcastFilterApp implements IFilterBehavior {

	Context sContext;

	List<AppInfo> sList = new ArrayList<AppInfo>();

	
	
	public BroadcastFilterApp(Context c) {
		sContext = c;
	}

	@Override
	public List<AppInfo> filter() {
		// TODO Auto-generated method stub
		Log.e(Utilities.TAG, "_CLS_:" + "FilterBroadcastApp" + ";"
				+ "_FUNCTION_:" + "getFilterApplication");

		Resources res = sContext.getResources();
		String[] actions = res.getStringArray(R.array.filter_broadcast_content);
		
		/**M:Hazel add for process sBgApplist that which item will set enable in according to the white list begin at 2016-11-6*/
		String[] sArrayWhiteList  = res.getStringArray(R.array.white_list_content);
		/**M:Hazel add for process sBgApplist that which item will set enable in according to the white list end at 2016-11-6*/
		
		List<ResolveInfo> resolveInfoList = new ArrayList<ResolveInfo>();
		List<AppInfo> AppInfoList = new ArrayList<AppInfo>();

		PackageManager packageManager = sContext.getPackageManager();

		for (int i=0;i<actions.length;i++) {
			String action = actions[i];
			Intent intent = new Intent(action);
			 
			if(action.contains("PACKAGE")){
				Uri uriInfo = Uri.parse("package://");
				intent.setData(uriInfo);
			}

			
			try {
				resolveInfoList = Utilities.getInstance().queryBroadcastReceivers(sContext, intent);
				AppInfoList = Utilities.getInstance().getAppsList(resolveInfoList,sArrayWhiteList,
						packageManager);
				Log.e(Utilities.TAG, "action="+action+";"+"list.size="+AppInfoList.size());
				//print(AppInfoList);
				sList.addAll(AppInfoList);
				
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

//		AppInfoList = Utilities.getInstance().getAppsList(resolveInfoList,
//				packageManager);

		return sList;
	}

	void print(List<AppInfo> list) {
		for (AppInfo info : list) {
			Log.e("sunset", "list.getSize=" + list.size()
					+ ";" + "info.title=" + info.title +";");
		}
	}

}
