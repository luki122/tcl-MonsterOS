package com.mst.utils;

import android.content.Context;
import android.util.Log;
import android.telephony.SubscriptionManager;

import java.util.HashMap; 

import com.android.incallui.InCallApp;
  
public class SubUtils {  
    private static final String TAG = "SubUtils";
    
    private static HashMap<Integer,Integer> mSubIdSlotIdPairs = new HashMap<Integer,Integer>();

    public static boolean isDoubleCardInsert() {
    	return SubscriptionManager.from(InCallApp.getInstance()).getActiveSubscriptionInfoCount() == 2;
    }

    public static int getSubIdbySlot(Context ctx, int slot) {  
    	int subid[] =  SubscriptionManager.getSubId(slot);
    	if(subid != null) {
    		return subid[0];    		
    	}
    	return SubscriptionManager.INVALID_SUBSCRIPTION_ID;
    }
    
    public static int getSlotBySubId(int subId) {
//     	SubscriptionManager mSubscriptionManager = SubscriptionManager.from(ctx); 
//     	int slot = mSubscriptionManager.getSlotId(subId);
//     	if(slot > -1) {
//     		mSubIdSlotIdPairs.put(subId , slot);
//     	} else if(mSubIdSlotIdPairs.get(subId) != null){
//     		slot = mSubIdSlotIdPairs.get(subId);
//     	}     	
//		return slot;
    	return SubscriptionManager.getSlotId(subId);
    }

    public static boolean isValidPhoneId(int slot) {
    	return SubscriptionManager.isValidPhoneId(slot);
    	
    }
    

}  