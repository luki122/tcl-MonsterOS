/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.dialer.calllog;

import android.net.Uri;
import android.telephony.TelephonyManager;
import android.telephony.SubscriptionManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import mst.provider.CallLog.Calls;
import android.telephony.SubscriptionManager;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.common.CallUtil;
import com.android.dialer.DialerApplication;
import com.android.dialer.PhoneCallDetails;
import com.android.dialer.R;
import com.android.dialer.util.DialerUtils;
import com.google.common.collect.Lists;
import com.mst.record.PhoneCallRecord;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mst.app.dialog.AlertDialog;
/**
 * Adapter for a ListView containing history items from the details of a call.
 */
public class CallDetailHistoryAdapter extends BaseAdapter {
	/** The top element is a blank header, which is hidden under the rest of the UI. */
	private static final int VIEW_TYPE_HEADER = 0;
	/** Each history item shows the detail of a call. */
	private static final int VIEW_TYPE_HISTORY_ITEM = 1;
	private static final String TAG = "CallDetailHistoryAdapter";

	private final Context mContext;
	private final LayoutInflater mLayoutInflater;
	private final CallTypeHelper mCallTypeHelper;
	private final PhoneCallDetails[] mPhoneCallDetails;

	/**
	 * List of items to be concatenated together for duration strings.
	 */
	private ArrayList<CharSequence> mDurationItems = Lists.newArrayList();

	public CallDetailHistoryAdapter(Context context, LayoutInflater layoutInflater,
			CallTypeHelper callTypeHelper, PhoneCallDetails[] phoneCallDetails) {
		mContext = context;
		mLayoutInflater = layoutInflater;
		mCallTypeHelper = callTypeHelper;
		mPhoneCallDetails = phoneCallDetails;
		incoming =context.getDrawable(R.drawable.mst_in_call_icon);
		outgoing = context.getDrawable(R.drawable.mst_out_call_icon);
		missed = context.getDrawable(R.drawable.mst_in_call_missed_icon);
	}

	@Override
	public boolean isEnabled(int position) {
		// None of history will be clickable.
		//        return false;
		final PhoneCallDetails details = mPhoneCallDetails[position];       
		return details.getPhoneRecords() != null;
	}

	@Override
	public int getCount() {
		return mPhoneCallDetails.length/* + 1*/;
	}

	@Override
	public Object getItem(int position) {
		//        if (position == 0) {
		//            return null;
		//        }
		return mPhoneCallDetails[position/* - 1*/];
	}

	@Override
	public long getItemId(int position) {
		//        if (position == 0) {
		//            return -1;
		//        }
		return position/* - 1*/;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public int getItemViewType(int position) {
		//        if (position == 0) {
		//            return VIEW_TYPE_HEADER;
		//        }
		return VIEW_TYPE_HISTORY_ITEM;
	}

	// date类型转换为String类型
	// formatType格式为yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
	// data Date类型的时间
	public String dateToString(Date data, String formatType) {
		return new SimpleDateFormat(formatType).format(data);
	}

	// string类型转换为date类型
	// strTime要转换的string类型的时间，formatType要转换的格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日
	// HH时mm分ss秒，
	// strTime的时间格式必须要与formatType的时间格式相同
	public Date stringToDate(String strTime, String formatType)
			throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat(formatType);
		Date date = null;
		date = formatter.parse(strTime);
		return date;
	}

	// long转换为Date类型
	// currentTime要转换的long类型的时间
	// formatType要转换的时间格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
	public Date longToDate(long currentTime, String formatType)
			throws ParseException {
		Date dateOld = new Date(currentTime); // 根据long类型的毫秒数生命一个date类型的时间
		String sDateTime = dateToString(dateOld, formatType); // 把date类型的时间转换为string
		Date date = stringToDate(sDateTime, formatType); // 把String类型转换为Date类型
		return date;
	}

	// long类型转换为String类型
	// currentTime要转换的long类型的时间
	// formatType要转换的string类型的时间格式
	public String longToString(long currentTime, String formatType) {
		try{
			Date date = longToDate(currentTime, formatType); // long类型转成Date类型
			int year=date.getYear();

			long now=System.currentTimeMillis();
			Date nowDate=longToDate(now, formatType);
			int nowYear=nowDate.getYear();
			if(year==nowYear) formatType="MM月dd日 HH:mm";

			String strTime = dateToString(date, formatType); // date类型转成String
			return strTime;
		}catch(Exception e){
			Log.d(TAG,"e:"+e);
			return "时间未知";
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		/* if (position == 0) {
            final View header = convertView == null
                    ? mLayoutInflater.inflate(R.layout.call_detail_history_header, parent, false)
                    : convertView;
            return header;
        }*/

		// Make sure we have a valid convertView to start with
		final View result  = convertView == null
				? mLayoutInflater.inflate(R.layout.call_detail_history_item, parent, false)
						: convertView;

				PhoneCallDetails details = mPhoneCallDetails[position/* - 1*/];
				//        CallTypeIconsView callTypeIconView =
				//                (CallTypeIconsView) result.findViewById(R.id.call_type_icon);
				//        TextView callTypeTextView = (TextView) result.findViewById(R.id.call_type_text);
				TextView dateView = (TextView) result.findViewById(R.id.date);
				TextView durationView = (TextView) result.findViewById(R.id.duration);
				
				ImageView recordView=(ImageView)result.findViewById(R.id.callrecord_icon);
	            //add by lgy for record 
                if(details.getPhoneRecords() != null) {
                    recordView.setVisibility(View.VISIBLE);
                } else {
                    recordView.setVisibility(View.GONE);
                }
				
				int callType = details.callTypes[0];
				boolean isVideoCall = (details.features & Calls.FEATURES_VIDEO) == Calls.FEATURES_VIDEO
						&& CallUtil.isVideoEnabled(mContext);

				//        callTypeIconView.clear();
				//        callTypeIconView.add(callType);
				//        callTypeIconView.setShowVideo(isVideoCall);
				//        callTypeTextView.setText(mCallTypeHelper.getCallTypeText(callType, isVideoCall));
				// Set the date.
				//				CharSequence dateValue = DateUtils.formatDateRange(mContext, details.date, details.date,
				//						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE |
				//                DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR);
				//        int index0=dateValue.toString().indexOf("日");
				//        int index1=dateValue.toString().indexOf(" ");
				//        String dataString=dateValue.toString().substring(0,index0+1)+"    "+dateValue.toString().substring(index1+1);

				String mDate=longToString(details.date, "yyyy年MM月dd日 HH:mm");
				dateView.setText(mDate);
				// Set the duration
				if (Calls.VOICEMAIL_TYPE == callType || CallTypeHelper.isMissedCallType(callType)) {
					durationView.setVisibility(View.GONE);
				} else {
					durationView.setVisibility(View.VISIBLE);
					durationView.setText(formatDurationAndDataUsage(details.duration, details.dataUsage));
				}

				ImageView simView=(ImageView)result.findViewById(R.id.sim_icon);
				if(DialerApplication.isMultiSimEnabled&&details.subscription_id!=null){
					int slotid=SubscriptionManager.getSlotId(Integer.parseInt(details.subscription_id));
					Log.d(TAG,"slotid:"+slotid+" subscription_id:"+details.subscription_id);		
					if (slotid == 1) {
						simView.setBackground(mContext.getDrawable(R.drawable.mst_sim2_icon));
						simView.setVisibility(View.VISIBLE);
					} else if (slotid == 0) {
						simView.setBackground(mContext.getDrawable(R.drawable.mst_sim1_icon));
						simView.setVisibility(View.VISIBLE);
					}else{
						simView.setVisibility(View.GONE);
					}
				}else{
					simView.setVisibility(View.GONE);
				}

				ImageView callTypeView=(ImageView)result.findViewById(R.id.call_type_icon);
				final Drawable drawable = getCallTypeDrawable(callType);
				callTypeView.setBackground(drawable);
				
				return result;
	}

	/** Call log type for incoming calls. */
	public static final int INCOMING_TYPE = 1;
	/** Call log type for outgoing calls. */
	public static final int OUTGOING_TYPE = 2;
	/** Call log type for missed calls. */
	public static final int MISSED_TYPE = 3;
	/** Call log type for voicemails. */
	public static final int VOICEMAIL_TYPE = 4;
	public final Drawable incoming;

	/**
	 * Drawable respresenting an outgoing call.
	 */
	public final Drawable outgoing;

	/**
	 * Drawable representing an incoming missed call.
	 */
	public final Drawable missed;
	public Drawable getCallTypeDrawable(int callType) {
		switch (callType) {
		case Calls.INCOMING_TYPE:
			return incoming;
		case Calls.OUTGOING_TYPE:
			return outgoing;
		case Calls.MISSED_TYPE:
			return missed;
		default:
			// It is possible for users to end up with calls with unknown call types in their
			// call history, possibly due to 3rd party call log implementations (e.g. to
			// distinguish between rejected and missed calls). Instead of crashing, just
			// assume that all unknown call types are missed calls.
			return missed;
		}
	}

	private CharSequence formatDuration(long elapsedSeconds) {
		long minutes = 0;
		long seconds = 0;

		if (elapsedSeconds >= 60) {
			minutes = elapsedSeconds / 60;
			elapsedSeconds -= minutes * 60;
			seconds = elapsedSeconds;
			return mContext.getString(R.string.callDetailsDurationFormat, minutes, seconds);
		} else {
			seconds = elapsedSeconds;
			return mContext.getString(R.string.callDetailsShortDurationFormat, seconds);
		}
	}

	/**
	 * Formats a string containing the call duration and the data usage (if specified).
	 *
	 * @param elapsedSeconds Total elapsed seconds.
	 * @param dataUsage Data usage in bytes, or null if not specified.
	 * @return String containing call duration and data usage.
	 */
	private CharSequence formatDurationAndDataUsage(long elapsedSeconds, Long dataUsage) {
		CharSequence duration = formatDuration(elapsedSeconds);

		if (dataUsage != null) {
			mDurationItems.clear();
			mDurationItems.add(duration);
			mDurationItems.add(Formatter.formatShortFileSize(mContext, dataUsage));

			return DialerUtils.join(mContext.getResources(), mDurationItems);
		} else {
			return duration;
		}
	}

	//add by lgy for record
	public void playRecord(int position){
		final PhoneCallDetails details = mPhoneCallDetails[position];       
		playRecord(details.getPhoneRecords());
	}

	private void playRecord(final List<PhoneCallRecord> records) {
		if (null == records) {
			return;
		}

		final int size = records.size();

		if (size == 1) {
			playRecord(records.get(0));
			return;
		}

		CharSequence[] items = new CharSequence[size];
		for (int i = 0; i < items.length; i++) {
			items[i] = new File(records.get(i).getPath()).getName().substring(0, 13) + ".amr";
		}
		new AlertDialog.Builder(mContext)
		.setTitle(R.string.record_settings_label)
		.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				playRecord(records.get(which));
			}
		})
		.show();
	}

	private void playRecord(PhoneCallRecord record) {
		if (null == record) {
			return;
		}

		Uri data = Uri.fromFile(new File(record.getPath()));  
		Intent intent = new Intent(Intent.ACTION_VIEW);  
		//        intent.setClassName("com.android.music", "com.android.music.AudioPreview");
		intent.setDataAndType(data, record.getMimeType());                
		try {
			mContext.startActivity(intent);
		} catch(Exception e) {
			e.printStackTrace();
			Toast.makeText(
					mContext,
					mContext.getResources().getString(
							R.string.no_music_activity), Toast.LENGTH_SHORT)
							.show();
		}

	}
	
}
