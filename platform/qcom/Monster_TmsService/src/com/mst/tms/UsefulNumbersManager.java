package com.mst.tms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.usefulnumber.UsefulNumberManager;
import android.text.TextUtils;
import android.util.Log;

public class UsefulNumbersManager {
	public static final String TAG = "UsefulNumbersManager";

	private static UsefulNumberManager mUsefulNumberManager;

	public static synchronized  UsefulNumberManager  getInstance() {
		if(mUsefulNumberManager == null) {
			mUsefulNumberManager = ManagerCreatorC.getManager(UsefulNumberManager.class);
		}
		return mUsefulNumberManager;
	}

	public static List<UsefulNumberResult> getUsefulNumber(String number) {
		Log.d(TAG,"getUsefulNumber");
		// get all useful numbers
		HashMap<String, String> dataMap = getInstance()
				.getAllYellowNumbers();
		Log.d(TAG,"dataMap:"+dataMap+" size:"+(dataMap!=null?dataMap.size():"null"));
		Set<Entry<String, String>> dataSet = dataMap.entrySet();

		List<UsefulNumberResult> result=new ArrayList<UsefulNumberResult>();
		boolean mstQueryAll=TextUtils.equals("mstQueryAll", number);
		Log.d(TAG,"mstQueryAll:"+mstQueryAll);
		for (Entry<String, String> entity : dataSet) {
			String key=entity.getKey();
			String value=entity.getValue();
			if(mstQueryAll){
				result.add(new UsefulNumberResult(key, value));
			}else{
				Log.d(TAG,"key:"+key+" value:"+value+" number:"+number+" index:"+key.indexOf(number));
				if(key.indexOf(number)>=0){
					result.add(new UsefulNumberResult(key, value));
				}
			}
		}

		Log.d(TAG,"result size:"+result.size());
		return result;
	}
}
