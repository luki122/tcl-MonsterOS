package com.monster.netmanage.receiver;

import tmsdk.common.TMSBootReceiver;

import com.monster.netmanage.service.AppTaskService;

import android.content.Context;
import android.content.Intent;

/**
 * 开机事件监听
 * @author boyliang
 */
public final class NetManagerBootReceiver extends TMSBootReceiver {

	@Override
	public void doOnRecv(final Context context, Intent intent) {
		super.doOnRecv(context, intent);
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			//监听用户打开应用操作
			Intent serviceIntent = new Intent(context, AppTaskService.class);
			context.startService(serviceIntent);
		}
	}
	
}
