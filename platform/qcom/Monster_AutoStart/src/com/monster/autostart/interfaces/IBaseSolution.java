package com.monster.autostart.interfaces;

import java.util.List;

import com.monster.autostart.bean.AppInfo;
import com.monster.autostart.bean.BroadcastDetectedApp;
import com.monster.autostart.bean.BroadcastFilterApp;
import com.monster.autostart.bean.BroadcastProccessApp;

public abstract class IBaseSolution {

	BroadcastFilterApp mFilterApp; // filter
	BroadcastProccessApp mProccessApp; //process
	/**M:Hazel add for detected app behavior when installing*/
	BroadcastDetectedApp mDetected; //detected
	
	public List<AppInfo> filter(){
		return mFilterApp.filter();
	}
	
	public void proccess(List<AppInfo> l){
		mProccessApp.proccess(l);
	}
	
	public List<AppInfo> deteced(String name){
		return mDetected.detected(name);
	}
	
	public void setFilter(BroadcastFilterApp filter){
		this.mFilterApp = filter;
	}
	
	public void setProccess(BroadcastProccessApp proccess){
		this.mProccessApp = proccess;
	}
	
	public void setDetected(BroadcastDetectedApp d){
		this.mDetected = d;
	}
}
