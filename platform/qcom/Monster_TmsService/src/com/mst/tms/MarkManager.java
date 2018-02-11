package com.mst.tms;

import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.numbermarker.NumMarkerManager;
import tmsdk.common.module.numbermarker.NumQueryRet;
import android.text.TextUtils;
import android.util.Log;

public class MarkManager {
	
	public static final String TAG = "MarkManager";
	
	private static NumMarkerManager mNumMarkerManager;
	
	public static synchronized  NumMarkerManager  getInstance() {
		if(mNumMarkerManager == null) {
			mNumMarkerManager = ManagerCreatorC.getManager(NumMarkerManager.class);
		}
		return mNumMarkerManager;
	}	
	
	
	public static final int TYPE_CHECK_COMM = 16; //通用
	public static final int TYPE_CHECK_CALLING = 17; //主叫
	public static final int TYPE_CHECK_CALLED = 18; //被叫
	
	private static NumQueryRet getMarkInternal(final int type, String number) {
		if(TextUtils.isEmpty(number)) {
			return null;
		}
		final String numberF = number;
		// 本地查
		Log.v(TAG, "localFetchNumberInfo--inputNumber:[" + numberF + "]");
		NumQueryRet item = getInstance().localFetchNumberInfo(numberF);
		
		//放弃云查，改为定时或者不定时更新数据库
		return item;
	}
	
    public static MarkResult getMark(int type, String number) {
    	NumQueryRet item = getMarkInternal(type, number);
    	if(item != null) {
    		MarkResult result = new MarkResult(item.property, item.number, item.name, item.tagType, item.tagCount, item.warning, item.usedFor, item.location, item.eOperator);
    		return result;
    	}
    	return null;    	    
    }
	
}
