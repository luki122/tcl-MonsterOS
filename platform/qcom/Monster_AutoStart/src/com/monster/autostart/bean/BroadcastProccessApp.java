package com.monster.autostart.bean;

import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.monster.autostart.db.MulwareProvider;
import com.monster.autostart.interfaces.IProccessBehavior;
import com.monster.autostart.utils.DeferredHandler;
import com.monster.autostart.utils.Utilities;

public class BroadcastProccessApp implements IProccessBehavior {

	Context sContext;
	PackageManager pm;

	DeferredHandler mHandler = new DeferredHandler();
	
	public BroadcastProccessApp(Context c) {
		// TODO Auto-generated constructor stub
		sContext = c;
		pm = sContext.getPackageManager();
	}

	@Override
	public void proccess( List<AppInfo> l) {
		// TODO Auto-generated method stub

		AppManagerState state = AppManagerState.getInstance();

		MulwareProvider provider = state.getAppProvider();
		l = provider.query(null, null, null);
		
		Log.e(Utilities.TAG, "_CLS_:" + "ProccessBroadcastFilterApp" + ";"
				+ "_FUNCTION_:" + "ListOfDisable.size=" + l.size());
		try {

			for (int i = 0; i < l.size(); i++) {
				AppInfo info = l.get(i);
				ComponentName cp = info.getIntent().getComponent();
				int status = info.getStatus();
				switch (status) {
				case Utilities.COMPONENT_AUTO_START_DISABLE:
                    Utilities.getInstance().setComponentEnabledSetting(pm, cp, PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
					break;
				case Utilities.COMPONENT_AUTO_START_ENABLE:
					Utilities.getInstance().setComponentEnabledSetting(pm, cp, PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
					break;

				default:
					break;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(Utilities.TAG, "_CLS_:" + "ProccessBroadcastFilterApp" + ";"
					+ "_FUNCTION_:"
					+ "proccessFilterAppication catch exception=" + e);
		}

	}

}
