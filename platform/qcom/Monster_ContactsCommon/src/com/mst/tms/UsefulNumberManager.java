package com.mst.tms;
import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

public class UsefulNumberManager {
	
	public static final String TAG = "UsefulNumberManager";
	
	public static HashMap<String,String> getUsefulNumber(String number) {
		// 获取常用号码
		ArrayList<UsefulNumberResult> numbers = TmsServiceManager.getInstance().getUsefulNumber(number);		 
		HashMap<String,String> result=new HashMap<String, String>();
		if(numbers==null) return result;
		for(UsefulNumberResult item:numbers){
			String key=item.getNumber();
			String value=item.getName();
			result.put(key,value);
		}
		return result;
	}
}
