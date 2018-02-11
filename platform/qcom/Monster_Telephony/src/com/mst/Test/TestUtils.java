package com.mst.test;

import android.content.SharedPreferences;
import android.content.Context;
import com.android.phone.PhoneGlobals;

public class TestUtils {
	public static boolean isInTestMode() {
		SharedPreferences sP = null;
		sP = PhoneGlobals.getInstance().getSharedPreferences(
				"com.android.phone_preferences", Context.MODE_PRIVATE);
		// return sP != null && sP.getBoolean("simulate_switch", false);
		return true;
	}
}