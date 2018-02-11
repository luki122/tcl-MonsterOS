package com.android.contacts.common.mst;

import java.util.ArrayList;

import android.R.integer;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.net.Uri;
import android.net.Uri.Builder;
import mst.provider.ContactsContract;
import mst.provider.ContactsContract.CommonDataKinds.Callable;
import mst.provider.ContactsContract.CommonDataKinds.Phone;
import mst.provider.ContactsContract.DialerSearch;
import mst.provider.ContactsContract.RawContacts;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.contacts.common.list.ContactListAdapter.ContactQuery;
import com.android.contacts.common.preference.ContactsPreferences;
import com.google.common.base.Preconditions;

/**
 * M: [MTK Dialer Search] Data query helper class
 */
public class DialerSearchHelperForMst {
	private static final String TAG = "ContactsDialerSearchHelperForMst";

	public interface DialerSearchResultColumnsForMst {
		public int CONTACT_ID_INDEX            = 1;
		public int LOOKUP_KEY_INDEX            = 2;
		public int INDICATE_PHONE_SIM_INDEX    = 3;
		public int INDEX_IN_SIM_INDEX          = 4;        
		public int PHOTO_ID_INDEX              = 5;
		public int NAME_INDEX                  = 6;
		public int PHONE_NUMBER_INDEX          = 7;
		public int DATA_HIGHLIGHT_INDEX        = 8;
		public int PINYIN_INDEX                = 9;
		public int PINYIN_HIGHLIGHT_INDEX      = 10;
		//        public int IS_PRIVACY_INDEX            = 11;
		//        public int PHOTO_URI_INDEX            = 12;
	}

	public static void highlightNumber(TextView tv, String formatNumber,
			Cursor cursor) {
		highlightNumber(tv, formatNumber,
				cursor,mSpanColorBg);
	}
	public static void highlightNumber(TextView tv, String formatNumber,
			Cursor cursor,int color) {
		Log.d(TAG,"highlightNumber,formatNumber:"+formatNumber);
		if(tv==null||formatNumber==null||cursor==null) return;
		for(int i=0;i<formatNumber.length();i++) Log.d(TAG,"i:"+i+"-"+formatNumber.charAt(i)+";");
		if (!TextUtils.isEmpty(formatNumber)) {
			String numberHighlight = cursor
					.getString(DialerSearchResultColumnsForMst.DATA_HIGHLIGHT_INDEX);

			if (null != numberHighlight && numberHighlight.length() == 2) {
				try {
					SpannableStringBuilder numberStyle = new SpannableStringBuilder(
							formatNumber);
					int start = numberHighlight.charAt(0);
					int end = numberHighlight.charAt(1);
					Log.d(TAG,"hightlightNumber,start:"+start+" end:"+end);
					// add by lgy start
//					if(!formatNumber.contains("@")){//liyang add
//						start++;
//						end++;
//					}

					// add by lgy end

					char[] numChars = formatNumber.toCharArray();
					for (int s = 0; s <= start; ++s) {
						if (' ' == numChars[s] || '-' == numChars[s]) {
							++start;
							++end;
						}
					}

					for (int e = start + 1; e <= end && e < numChars.length; ++e) {
						if (' ' == numChars[e] || '-' == numChars[e]) {
							++end;
						}
					}

					if (end > formatNumber.length()) {
						end = formatNumber.length();
					}

					Log.d(TAG,"start:"+start+" end:"+end);

					numberStyle.setSpan(new ForegroundColorSpan(color),
							start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

					tv.setText(numberStyle);
					setVisibility(tv, View.VISIBLE);
					return;
				} catch (ArrayIndexOutOfBoundsException ae) {
					ae.printStackTrace();
				} catch (IndexOutOfBoundsException ie) {
					ie.printStackTrace();
				}
			}

			tv.setText(formatNumber);
			setVisibility(tv, View.VISIBLE);
		}
	}

	public static void highlightName(TextView tv, Cursor cursor){
		highlightName(tv, cursor,mSpanColorBg);
	}
	public static void highlightName(TextView tv, Cursor cursor,int color) {	
		String name0 = cursor
				.getString(DialerSearchResultColumnsForMst.NAME_INDEX);
		String name = name0.toUpperCase();
		String pinyin = cursor
				.getString(DialerSearchResultColumnsForMst.PINYIN_INDEX);


		Log.d(TAG,"highlightName,name:"+name0+" pinyin:"+pinyin+" tv:"+tv);
		if (TextUtils.isEmpty(name0)) {
			String number = cursor
					.getString(DialerSearchResultColumnsForMst.PHONE_NUMBER_INDEX);
			if (!TextUtils.isEmpty(number)) {
				tv.setText(number);
				setVisibility(tv, View.VISIBLE);
				return;
			}

			tv.setText(null);
			return;
		}


		if (!TextUtils.isEmpty(pinyin)) {
			String pinyinHighlight = cursor
					.getString(DialerSearchResultColumnsForMst.PINYIN_HIGHLIGHT_INDEX);
//			Log.d(TAG,"column:"+cursor.getColumnName(DialerSearchResultColumnsForMst.PINYIN_HIGHLIGHT_INDEX)+" pinyinHighlight:"+pinyinHighlight);
			if (pinyinHighlight != null) {
				SpannableStringBuilder namestyle = new SpannableStringBuilder(
						name0);
				SpannableStringBuilder pinyinstyle = new SpannableStringBuilder(
						pinyin);
				if (!name0.equals(pinyin)) {
					Log.d(TAG,"pinyinHighlight1");
					ArrayList<Integer> nameToken = new ArrayList<Integer>();
					int nameIndex = 0;
					int pinyinIndex = 0;
					int pinyinLength = pinyin.length();
					for (int i = 0; i < pinyinLength; i++) {
						char c = pinyin.charAt(i);
						Log.d(TAG,"c:"+c+"nameIndex:"+nameIndex+" pinyinIndex:"+pinyinIndex);
						if (0 == i && c != name.charAt(nameIndex)) {
							Log.d(TAG,"c1");
							pinyinIndex++;
							continue;
						} else if (i > 0 && (nameIndex + 1) < name.length()
								&& c != name.charAt(nameIndex + 1)
								&& ('a' <= c && c <= 'z')) {
							Log.d(TAG,"c2:"+name.charAt(nameIndex + 1));
							pinyinIndex++;
							continue;
						} else if (i > 0 && (nameIndex + 1) == name.length()) {
							if (pinyinIndex < pinyinLength
									&& ('A' <= c && c <= 'Z')) {
								Log.d(TAG,"c3");
								nameToken.add(pinyinIndex);
								pinyinIndex++;

								//add by liyang
								if(pinyinLength==pinyinIndex){
									Log.d(TAG,"c3.1");
									nameToken.add(pinyinIndex);
								}
								continue;
							}

							pinyinIndex++;
							if (pinyinIndex == pinyinLength) {
								Log.d(TAG,"c4");
								nameToken.add(pinyinIndex);
								break;
							}
						} else {
							if (pinyinIndex > 0) {
								Log.d(TAG,"c5");
								nameToken.add(pinyinIndex);
							}
							Log.d(TAG,"c6");
							nameIndex++;
							pinyinIndex++;

							//add by liyang
							if((nameIndex + 1) == name.length()&&pinyinLength==pinyinIndex){
								Log.d(TAG,"c7");
								if('A' <= c && c <= 'Z'){
									Log.d(TAG,"c8");
									nameToken.add(pinyinIndex);
								}
							}
						}					

					}



					if (nameToken.size() <= 0) {
						Log.d(TAG,"c7");
						tv.setText(name0);
						setVisibility(tv, View.VISIBLE);
						return;
					}

					if (pinyin.charAt(pinyin.length() - 1) == name.charAt(name
							.length() - 1)) {
						Log.d(TAG,"c8");
						nameToken.add(nameToken.get(nameToken.size() - 1) + 1);
					}

					for (int i : nameToken) {
						Log.d(TAG, "nameToken = " + i);
					}

					for (int i = 0, len = pinyinHighlight.length(); i < len; i = i + 2) {
						int start = (int) pinyinHighlight.charAt(i);
						int end = (int) pinyinHighlight.charAt(i + 1);
						int startIndex = 0;
						int endIndex = 0;
						Log.d(TAG, "start = " + start + "  end = " + end);
						if (start == 0 && end == 0) {
							break;
						}

						for (int temp = 0; temp < nameToken.size(); temp++) {
							if (start == 0) {
								startIndex = 0;
							} else if (start == nameToken.get(temp)) {
								startIndex = temp + 1;
							}

							if (end > nameToken.get(nameToken.size() - 1)) {
								endIndex = nameToken.size();
							} else if (end == nameToken.get(temp)) {
								endIndex = temp + 1;
							} else if (end > nameToken.get(temp)
									&& end < nameToken.get(temp + 1)) {
								endIndex = temp + 2;
							} else if (end <= nameToken.get(0)) {
								endIndex = 1;
							}
							Log.d(TAG, "startIndex = " + startIndex +
									"  endIndex = " + endIndex);
						}

						//add by liyang 处理空格
						ArrayList<Integer> spaceToken = new ArrayList<Integer>();
						for(int j=0;j<name.length();j++){
							char c=name.charAt(j);
							if(c==' '){
								Log.d(TAG,"space");
								spaceToken.add(j);
							}
						}
						for(int k:spaceToken){
							if(k<startIndex){
								Log.d(TAG,"k:"+k+" space1");
								startIndex++;
							}
							if(k<endIndex){
								Log.d(TAG,"k:"+k+" space2");
								endIndex++;
							}
						}
						Log.d(TAG, "startIndex1 = " + startIndex +
								"  endIndex1 = " + endIndex);
						
						try {
							namestyle.setSpan(new ForegroundColorSpan(
									color), startIndex, endIndex,
									Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					tv.setText(namestyle);
					setVisibility(tv, View.VISIBLE);
					return;
				}

				for (int i = 0, len = pinyinHighlight.length(); i < len; i = i + 2) {
					int start = (int) pinyinHighlight.charAt(i);
					int end = (int) pinyinHighlight.charAt(i + 1);
					pinyinstyle.setSpan(new ForegroundColorSpan(color),
							start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}

				tv.setText(pinyinstyle);
				setVisibility(tv, View.VISIBLE);
				return;
			}
		}

		tv.setText(name0);
		setVisibility(tv, View.VISIBLE);
	}

	public static void highlightNameForContactList(TextView tv, Cursor cursor,boolean isAllDigit) {		
		String name = cursor
				.getString(DialerSearchResultColumnsForMst.NAME_INDEX);
		String pinyin = cursor
				.getString(DialerSearchResultColumnsForMst.PINYIN_INDEX);


		Log.d(TAG,"highlightNameForContactList,name:"+name+" pinyin:"+pinyin+" tv:"+tv+" isAllDigit:"+isAllDigit);
		if (TextUtils.isEmpty(name)) {
			String number = cursor
					.getString(DialerSearchResultColumnsForMst.PHONE_NUMBER_INDEX);
			if (!TextUtils.isEmpty(number)) {
				tv.setText(number);
				setVisibility(tv, View.VISIBLE);
				return;
			}

			tv.setText(null);
			return;
		}

		//		if(isAllDigit){//add by liyang
		//			tv.setText(name);
		//			setVisibility(tv, View.VISIBLE);
		//			return;
		//		}

		if (!TextUtils.isEmpty(pinyin)) {
			Log.d(TAG,"pinyinHighlight0");
			String pinyinHighlight = cursor
					.getString(DialerSearchResultColumnsForMst.PINYIN_HIGHLIGHT_INDEX);
			if (pinyinHighlight != null) {
				Log.d(TAG,"pinyinHighlight-1");
				SpannableStringBuilder namestyle = new SpannableStringBuilder(
						name);
				SpannableStringBuilder pinyinstyle = new SpannableStringBuilder(
						pinyin);
				if (!name.equals(pinyin)) {
					Log.d(TAG,"pinyinHighlight1");
					ArrayList<Integer> nameToken = new ArrayList<Integer>();
					int nameIndex = 0;
					int pinyinIndex = 0;
					int pinyinLength = pinyin.length();
					for (int i = 0; i < pinyinLength; i++) {
						char c = pinyin.charAt(i);
						if (0 == i && c != name.charAt(nameIndex)) {
							pinyinIndex++;
							Log.d(TAG,"pinyinHighlight2,pinyinIndex:"+pinyinIndex);
							continue;
						} else if (i > 0 && (nameIndex + 1) < name.length()
								&& c != name.charAt(nameIndex + 1)
								&& ('a' <= c && c <= 'z')) {
							pinyinIndex++;
							Log.d(TAG,"pinyinHighlight3,pinyinIndex:"+pinyinIndex);
							continue;
						} else if (i > 0 && (nameIndex + 1) == name.length()) {
							Log.d(TAG,"pinyinHighlight4,pinyinIndex:"+pinyinIndex);
							if (pinyinIndex < pinyinLength
									&& ('A' <= c && c <= 'Z')) {
								nameToken.add(pinyinIndex);
								pinyinIndex++;
								Log.d(TAG,"pinyinHighlight5,pinyinIndex:"+pinyinIndex);
								continue;
							}

							pinyinIndex++;
							Log.d(TAG,"pinyinHighlight6,pinyinIndex:"+pinyinIndex);
							if (pinyinIndex == pinyinLength) {
								Log.d(TAG,"pinyinHighlight7,pinyinIndex:"+pinyinIndex);
								nameToken.add(pinyinIndex);
								break;
							}
						} else {
							if (pinyinIndex > 0) {
								Log.d(TAG,"pinyinHighlight8,pinyinIndex:"+pinyinIndex);
								nameToken.add(pinyinIndex);
							}
							nameIndex++;
							pinyinIndex++;
						}
					}
					Log.d(TAG,"pinyinHighlight9,pinyinIndex:"+pinyinIndex);
					if (nameToken.size() <= 0) {
						Log.d(TAG,"pinyinHighlight10,pinyinIndex:"+pinyinIndex);
						tv.setText(name);
						setVisibility(tv, View.VISIBLE);
						return;
					}

					if (pinyin.charAt(pinyin.length() - 1) == name.charAt(name
							.length() - 1)) {
						Log.d(TAG,"pinyinHighlight11,pinyinIndex:"+pinyinIndex);
						nameToken.add(nameToken.get(nameToken.size() - 1) + 1);
					}

					// for (int i : nameToken) {
					// Log.d(TAG, " = " + i);
					// }

					for(int i:nameToken){
						Log.d(TAG,"i:"+i);
					}
					for (int i = 0, len = pinyinHighlight.length(); i < len; i = i + 2) {
						int start = (int) pinyinHighlight.charAt(i);
						int end = (int) pinyinHighlight.charAt(i + 1);
						Log.d(TAG,"pinyinHighlight,i:"+i+" start:"+start+" end:"+end);
						int startIndex = 0;
						int endIndex = 0;
						// Log.d(TAG, "start = " + start + "  end = " + end);
						if (start == 0 && end == 0) {
							break;
						}

						for (int temp = 0; temp < nameToken.size(); temp++) {
							if (start == 0) {
								startIndex = 0;
							} else if (start == nameToken.get(temp)) {
								startIndex = temp + 1;
								Log.d(TAG,"pinyinHighlight15,startIndex:"+startIndex);
							}

							if (end > nameToken.get(nameToken.size() - 1)) {
								endIndex = nameToken.size();
								Log.d(TAG,"pinyinHighlight16,endIndex:"+endIndex);

							} else if (end == nameToken.get(temp)) {
								endIndex = temp + 1;
								Log.d(TAG,"pinyinHighlight17,endIndex:"+endIndex);
							} else if (end > nameToken.get(temp)
									&& end < nameToken.get(temp + 1)) {
								endIndex = temp + 2;
								Log.d(TAG,"pinyinHighlight18,endIndex:"+endIndex);
							} else if (end <= nameToken.get(0)) {
								endIndex = 1;
								Log.d(TAG,"pinyinHighlight19,endIndex:"+endIndex);
							}
							// Log.d(TAG, "startIndex = " + startIndex +
							// "  endIndex = " + endIndex);
						}
						Log.d(TAG,"pinyinHighlight: startIndex:"+startIndex+" endIndex:"+endIndex);
						try {
							namestyle.setSpan(new ForegroundColorSpan(
									mSpanColorBg), startIndex, endIndex,
									Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					tv.setText(namestyle);
					setVisibility(tv, View.VISIBLE);
					return;
				}

				Log.d(TAG,"pinyinHighlight12");
				for (int i = 0, len = pinyinHighlight.length(); i < len; i = i + 2) {
					int start = (int) pinyinHighlight.charAt(i);
					int end = (int) pinyinHighlight.charAt(i + 1);
					pinyinstyle.setSpan(new ForegroundColorSpan(mSpanColorBg),
							start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}

				tv.setText(pinyinstyle);
				setVisibility(tv, View.VISIBLE);
				return;
			}
		}

		Log.d(TAG,"pinyinHighlight13");
		tv.setText(name);
		setVisibility(tv, View.VISIBLE);
	}

	private static int mSpanColorBg=Color.parseColor("#19A8AE");

	private static void setVisibility(View view, int visibility) {
		if (view.getVisibility() != visibility) {
			view.setVisibility(visibility);
		}
	}

}
