package com.android.providers.contacts;

import java.util.Locale;

import com.mst.tms.TmsServiceManager;


import android.app.Application;
import android.os.SystemProperties;

public class ContactsProvidersApplication extends Application {

	public static final boolean sIsMstPrivacySupport = true;
	private static ContactsProvidersApplication mInstance;
	public static ContactsProvidersApplication getInstance() {
		return mInstance;
	}
	
	public ContactsProvidersApplication() {
	    mInstance = this;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
        TmsServiceManager.getInstance(this).bindService();
	}
	
	public static boolean isCts() {
//		return !isChinaSetting();
		return false;
	}
	
	private static boolean isChinaSetting() {  
        String language = getLanguageEnv();  
  
        if (language != null  
                && (language.trim().equals("zh-CN") || language.trim().equals("zh-TW")))  
            return true;  
        else  
            return false;  
    }  
	
	private static  String getLanguageEnv() {  
	       Locale l = Locale.getDefault();  
	       String language = l.getLanguage();  
	       String country = l.getCountry().toLowerCase();  
	       if ("zh".equals(language)) {  
	           if ("cn".equals(country)) {  
	               language = "zh-CN";  
	           } else if ("tw".equals(country)) {  
	               language = "zh-TW";  
	           }  
	       } else if ("pt".equals(language)) {  
	           if ("br".equals(country)) {  
	               language = "pt-BR";  
	           } else if ("pt".equals(country)) {  
	               language = "pt-PT";  
	           }  
	       }  
	       return language;  
	   }  
}
