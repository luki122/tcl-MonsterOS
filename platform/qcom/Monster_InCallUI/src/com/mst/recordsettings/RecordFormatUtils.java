package com.mst.recordsettings;

import android.text.format.DateUtils;

public class RecordFormatUtils{
	   static String formatDate (long date) {
	    	if (date == 0) {
	    		return "";
	    	}
	    	
			CharSequence dateText = DateUtils.getRelativeTimeSpanString(
					date, System.currentTimeMillis(),
					DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
			String result = dateText.toString().replaceAll(" ", "");
			result  = replaceDateString(result);
			return result;
		}
	   
	   private static String replaceDateString(String src) {
			if (src == null) {
				return null;
			}
			
			String from[] = {"十一", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
			String to[] = {"11", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"} ;
			for (int i=0; i < from.length; i++) { 
				src = src.replaceAll(from[i], to[i]); 
			}
			
			return src; 
		}
}