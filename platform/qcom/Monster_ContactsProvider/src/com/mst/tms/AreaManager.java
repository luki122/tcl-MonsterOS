package com.mst.tms;
import com.android.providers.contacts.ContactsProvidersApplication;
import com.android.providers.contacts.R;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;

public class AreaManager {
	
	public static final String TAG = "AreaManager";
	
	public static String getArea(String number) {
		// get location of the input phone number 
		// 获取用户输入的号码的归属地
		String location = TmsServiceManager.getInstance().getArea(number);
		Log.d(TAG, " number = " + number + ", area = " + location);
		if(PhoneNumberUtils.isEmergencyNumber(number) && TextUtils.isEmpty(location)) {
		    if(ContactsProvidersApplication.getInstance() != null) {
		        location = ContactsProvidersApplication.getInstance().getString(R.string.emergency_number);
		    } else {
		        location = "紧急呼叫号码";
		    }
		}
		return location;
	}
}
