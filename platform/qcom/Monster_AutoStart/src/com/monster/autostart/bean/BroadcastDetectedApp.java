package com.monster.autostart.bean;

import java.util.ArrayList;
import java.util.List;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import com.monster.autostart.interfaces.IBaseSolution;
import com.monster.autostart.interfaces.IDetectedBehavior;
import com.monster.autostart.utils.Utilities;

public class BroadcastDetectedApp implements IDetectedBehavior{

	@Override
	public List<AppInfo> detected(String packageName) {
		// TODO Auto-generated method stub
		AppManagerState state = AppManagerState.getInstance();
		
		PackageManager pm = state.getContext().getPackageManager();
		
		PackageInfo packageInfo;
		
		List<IBaseSolution> solution = state
				.getSolution();
		
		List<AppInfo> result = new ArrayList<AppInfo>();
		
		try {
			packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_RECEIVERS);
			ActivityInfo[] receivers = packageInfo.receivers;
			if(receivers == null) return result;
			
			for(IBaseSolution cl : solution){
				if(cl instanceof BroadcastSolution){
					List<AppInfo> list = cl.filter();
					for(int i=0;i<receivers.length;i++){
							ActivityInfo rec = receivers[i];
						for(int j=0;j<list.size();j++){
							AppInfo info = list.get(j);
							String pkg = info.getIntent().getComponent().getPackageName();
							String cls = info.getIntent().getComponent().getClassName();
							if(rec.packageName.equals(pkg) && rec.name.equals(cls)){
								result.add(info);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
}
