/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.monster.interception.util;

import android.content.Context;
import android.telephony.SubscriptionManager;
import android.util.Log;

;

public class SimUtils {

	private static final String TAG = "SimUtils";

	public static boolean isShowDoubleButton(Context context) {

		SubscriptionManager sm = SubscriptionManager.from(context);
		return sm.getActiveSubscriptionInfoCount() > 1;
	}
	
	public static int getSubIdBySlot(Context context, int slot) {
		SubscriptionManager sm = SubscriptionManager.from(context);
	      int[] subId = SubscriptionManager.getSubId(slot);
          if (subId != null) {
        	  return subId[0];
          }
		return  SubscriptionManager.INVALID_SUBSCRIPTION_ID;
	}
	
	public static int getSlotbyId(Context context , int subid) {
		return SubscriptionManager.getSlotId(subid);
	}
}
