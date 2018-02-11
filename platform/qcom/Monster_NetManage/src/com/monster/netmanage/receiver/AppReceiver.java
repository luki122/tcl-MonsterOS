package com.monster.netmanage.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * 监听 应用安装与卸载
 * @author zhaolaichao
 *
 */
public class AppReceiver extends BroadcastReceiver {

	public static final String UPDATE_APP_ACTION = "com.monster.netmanage.updateapp.action";
	public static final int PACKAGEADDED = 1;
	public static final int PACKAGEREMOVED = 2;
	private int appTag = 0;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		appTag = 0;
		if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_ADDED)) {
			String packageName = intent.getData().getSchemeSpecificPart();
			appTag = PACKAGEADDED;
            Intent sendIntent = new Intent(UPDATE_APP_ACTION);
            sendIntent.putExtra("UPDATE_APP_NAME", packageName);
            sendIntent.putExtra("UPDATE_APP_TAG", appTag);
            context.sendBroadcast(sendIntent);
		}  else if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_REMOVED)) {
			String packageName = intent.getData().getSchemeSpecificPart();
			appTag = PACKAGEREMOVED;
			Intent sendIntent = new Intent(UPDATE_APP_ACTION);
            sendIntent.putExtra("UPDATE_APP_NAME", packageName);
            sendIntent.putExtra("UPDATE_APP_TAG", appTag);
            context.sendBroadcast(sendIntent);
		} 
	}

}
