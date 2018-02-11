/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import mst.provider.CallLog;
import android.provider.VoicemailContract.Voicemails;
import android.telecom.PhoneAccountHandle;
import android.text.TextUtils;
import android.util.Log;

import com.android.contacts.common.GeoUtil;
import com.android.dialer.PhoneCallDetails;
import com.android.dialer.util.AsyncTaskExecutor;
import com.android.dialer.util.AsyncTaskExecutors;
import com.android.dialer.util.PhoneNumberUtil;
import com.android.dialer.util.TelecomUtil;

import com.google.common.annotations.VisibleForTesting;

public class CallLogAsyncTaskUtil {
	private static String TAG = CallLogAsyncTaskUtil.class.getSimpleName();

	/** The enumeration of {@link AsyncTask} objects used in this class. */
	public enum Tasks {
		DELETE_VOICEMAIL,
		DELETE_CALL,
		MARK_VOICEMAIL_READ,
		GET_CALL_DETAILS,
	}

	private static class CallDetailQuery {
		static final String[] CALL_LOG_PROJECTION = new String[] {
			CallLog.Calls.DATE,
			CallLog.Calls.DURATION,
			CallLog.Calls.NUMBER,
			CallLog.Calls.TYPE,
			CallLog.Calls.COUNTRY_ISO,
			CallLog.Calls.GEOCODED_LOCATION,
			CallLog.Calls.NUMBER_PRESENTATION,
			CallLog.Calls.PHONE_ACCOUNT_COMPONENT_NAME,
			CallLog.Calls.PHONE_ACCOUNT_ID,
			CallLog.Calls.FEATURES,
			CallLog.Calls.DATA_USAGE,
			CallLog.Calls.TRANSCRIPTION
		};

		static final int DATE_COLUMN_INDEX = 0;
		static final int DURATION_COLUMN_INDEX = 1;
		static final int NUMBER_COLUMN_INDEX = 2;
		static final int CALL_TYPE_COLUMN_INDEX = 3;
		static final int COUNTRY_ISO_COLUMN_INDEX = 4;
		static final int GEOCODED_LOCATION_COLUMN_INDEX = 5;
		static final int NUMBER_PRESENTATION_COLUMN_INDEX = 6;
		static final int ACCOUNT_COMPONENT_NAME = 7;
		static final int ACCOUNT_ID = 8;
		static final int FEATURES = 9;
		static final int DATA_USAGE = 10;
		static final int TRANSCRIPTION_COLUMN_INDEX = 11;

		static final String[] CALL_LOG_PROJECTION_MST = new String[] {
			CallLog.Calls.DATE,
			CallLog.Calls.DURATION,
			CallLog.Calls.NUMBER,
			CallLog.Calls.TYPE,
			CallLog.Calls.COUNTRY_ISO,
			CallLog.Calls.GEOCODED_LOCATION,
			CallLog.Calls.NUMBER_PRESENTATION,
			CallLog.Calls.PHONE_ACCOUNT_COMPONENT_NAME,
			CallLog.Calls.PHONE_ACCOUNT_ID,
			CallLog.Calls.FEATURES,
			CallLog.Calls.DATA_USAGE,
			CallLog.Calls.TRANSCRIPTION,
			"display_name",//12
			"lookup_uri",//13
			"raw_contact_id",//14
			"photo_uri",			//15
			"numberlabel",//16
			"starred",//17
		};
	}

	public interface CallLogAsyncTaskListener {
		public void onDeleteCall();
		public void onDeleteVoicemail();
		public void onGetCallDetails(PhoneCallDetails[] details);
	}

	private static AsyncTaskExecutor sAsyncTaskExecutor;

	private static void initTaskExecutor() {
		sAsyncTaskExecutor = AsyncTaskExecutors.createThreadPoolExecutor();
	}

	public static void getCallDetails(
			final Context context,
			final Uri[] callUris,
			final CallLogAsyncTaskListener callLogAsyncTaskListener) {
		if (sAsyncTaskExecutor == null) {
			initTaskExecutor();
		}

		sAsyncTaskExecutor.submit(Tasks.GET_CALL_DETAILS,
				new AsyncTask<Void, Void, PhoneCallDetails[]>() {
					@Override
					public PhoneCallDetails[] doInBackground(Void... params) {
						Log.d(TAG,"liyang18-doInBackground0");
						// TODO: All calls correspond to the same person, so make a single lookup.
						final int numCalls = callUris.length;
						PhoneCallDetails[] details = new PhoneCallDetails[numCalls];
						try {
							for (int index = 0; index < numCalls; ++index) {
								details[index] =
										getPhoneCallDetailsForUri(context, callUris[index]);
							}
							Log.d(TAG,"liyang18-doInBackground1");
							return details;
						} catch (IllegalArgumentException e) {
							// Something went wrong reading in our primary data.
							Log.w(TAG, "Invalid URI starting call details", e);
							return null;
						}
					}

					@Override
					public void onPostExecute(PhoneCallDetails[] phoneCallDetails) {
						if (callLogAsyncTaskListener != null) {
							callLogAsyncTaskListener.onGetCallDetails(phoneCallDetails);
						}
					}
				});
	}

	//add by liyang
	public static void getCallDetailsForMst(
			final Context context,
			final String number,
			final CallLogAsyncTaskListener callLogAsyncTaskListener , final boolean isRejectedDetail) {
		if (sAsyncTaskExecutor == null) {
			initTaskExecutor();
		}

		sAsyncTaskExecutor.submit(Tasks.GET_CALL_DETAILS,
				new AsyncTask<Void, Void, PhoneCallDetails[]>() {
					@Override
					public PhoneCallDetails[] doInBackground(Void... params) {
						Log.d(TAG,"liyang18-getCallDetailsForMst doInBackground0,number:"+number);
						// TODO: All calls correspond to the same person, so make a single lookup.

						String selection = isRejectedDetail ? " calls.reject = 1"  : null;
						Cursor cursor = context.getContentResolver().query(
								Uri.parse("content://call_log/calls/call_detail/"+number.replace(" ", "")), CallDetailQuery.CALL_LOG_PROJECTION_MST, selection, null, "date desc");
						Log.d(TAG,"liyang18-cursor1:"+(cursor==null?"null":cursor.getCount()));
						if (cursor == null || !cursor.moveToFirst()) {
							Log.d(TAG,"liyang18,null");
							return null;
						}
						int total=cursor.getCount();
						PhoneCallDetails[] details = new PhoneCallDetails[total];
						int i=0;
						do{
							final int numberPresentation =
									cursor.getInt(CallDetailQuery.NUMBER_PRESENTATION_COLUMN_INDEX);
							final PhoneAccountHandle accountHandle = PhoneAccountUtils.getAccount(
									cursor.getString(CallDetailQuery.ACCOUNT_COMPONENT_NAME),
									cursor.getString(CallDetailQuery.ACCOUNT_ID));
//							boolean isVoicemail = PhoneNumberUtil.isVoicemailNumber(context, accountHandle, number);
							boolean isVoicemail = false;
							final String countryIso = cursor.getString(CallDetailQuery.COUNTRY_ISO_COLUMN_INDEX);

							//								boolean shouldLookupNumber =
							//										PhoneNumberUtil.canPlaceCallsTo(number, numberPresentation) && !isVoicemail;
							ContactInfo info = ContactInfo.EMPTY;
							PhoneCallDetails detail = new PhoneCallDetails(
									context, number, numberPresentation, info.formattedNumber, isVoicemail);
							if (true) {
								detail.contactUri=(cursor.getString(13)==null?null:Uri.parse(cursor.getString(13)));
								detail.name=cursor.getString(12);
								Log.d(TAG,"liyang18-name:"+detail.name);
								detail.numberType=cursor.getInt(CallDetailQuery.CALL_TYPE_COLUMN_INDEX);
								detail.numberLabel=cursor.getString(16);
								detail.photoUri=(cursor.getString(15)==null?null:Uri.parse(cursor.getString(15)));
								detail.sourceType=0;
								detail.objectId=null;
								detail.starred=cursor.getInt(17);
								detail.lookupUri=detail.contactUri;
								Log.d(TAG,"liyang18-detail.lookupUri:"+detail.lookupUri);
								detail.rawContactId=cursor.getInt(14);
								detail.callTypes = new int[] {
										cursor.getInt(CallDetailQuery.CALL_TYPE_COLUMN_INDEX)};
								detail.date = cursor.getLong(CallDetailQuery.DATE_COLUMN_INDEX);
								detail.duration = cursor.getLong(CallDetailQuery.DURATION_COLUMN_INDEX);
								detail.features = cursor.getInt(CallDetailQuery.FEATURES);
								detail.geocode = cursor.getString(CallDetailQuery.GEOCODED_LOCATION_COLUMN_INDEX);
								detail.transcription = cursor.getString(CallDetailQuery.TRANSCRIPTION_COLUMN_INDEX);
								detail.subscription_id=cursor.getString(CallDetailQuery.ACCOUNT_ID);


								detail.countryIso = !TextUtils.isEmpty(countryIso) ? countryIso
										: GeoUtil.getCurrentCountryIso(context);

								if (!cursor.isNull(CallDetailQuery.DATA_USAGE)) {
									detail.dataUsage = cursor.getLong(CallDetailQuery.DATA_USAGE);
								}
							}
							details[i++]=detail;								
						}while(cursor.moveToNext());
						Log.d(TAG,"liyang18-details:"+details+" length:"+details.length);
						return details;
					}

					@Override
					public void onPostExecute(PhoneCallDetails[] phoneCallDetails) {
						if (callLogAsyncTaskListener != null) {
							callLogAsyncTaskListener.onGetCallDetails(phoneCallDetails);
						}
					}
				});
	}

	/**
	 * Return the phone call details for a given call log URI.
	 */
	private static PhoneCallDetails getPhoneCallDetailsForUri(Context context, Uri callUri) {
		Log.d(TAG,"liyang18-getPhoneCallDetailsForUri,callUri:"+callUri);
		Cursor cursor = context.getContentResolver().query(
				callUri, CallDetailQuery.CALL_LOG_PROJECTION, null, null, null);

		try {
			if (cursor == null || !cursor.moveToFirst()) {
				throw new IllegalArgumentException("Cannot find content: " + callUri);
			}

			// Read call log.
			final String countryIso = cursor.getString(CallDetailQuery.COUNTRY_ISO_COLUMN_INDEX);
			final String number = cursor.getString(CallDetailQuery.NUMBER_COLUMN_INDEX);
			final int numberPresentation =
					cursor.getInt(CallDetailQuery.NUMBER_PRESENTATION_COLUMN_INDEX);

			final PhoneAccountHandle accountHandle = PhoneAccountUtils.getAccount(
					cursor.getString(CallDetailQuery.ACCOUNT_COMPONENT_NAME),
					cursor.getString(CallDetailQuery.ACCOUNT_ID));

			// If this is not a regular number, there is no point in looking it up in the contacts.
			ContactInfoHelper contactInfoHelper =
					new ContactInfoHelper(context, GeoUtil.getCurrentCountryIso(context));
			boolean isVoicemail = PhoneNumberUtil.isVoicemailNumber(context, accountHandle, number);
			boolean shouldLookupNumber =
					PhoneNumberUtil.canPlaceCallsTo(number, numberPresentation) && !isVoicemail;

			ContactInfo info = ContactInfo.EMPTY;
			Log.d(TAG,"liyang18-shouldLookupNumber:"+shouldLookupNumber);
			if (shouldLookupNumber) {
				ContactInfo lookupInfo = contactInfoHelper.lookupNumber(number, countryIso);
				info = lookupInfo != null ? lookupInfo : ContactInfo.EMPTY;
			}

			PhoneCallDetails details = new PhoneCallDetails(
					context, number, numberPresentation, info.formattedNumber, isVoicemail);

			details.accountHandle = accountHandle;
			details.contactUri = info.lookupUri;
			details.name = info.name;
			details.numberType = info.type;
			details.numberLabel = info.label;
			details.photoUri = info.photoUri;
			details.sourceType = info.sourceType;
			details.objectId = info.objectId;
			details.starred=info.starred;
			details.lookupUri=info.lookupUri;
			details.rawContactId=info.rawContactId;

			details.callTypes = new int[] {
					cursor.getInt(CallDetailQuery.CALL_TYPE_COLUMN_INDEX)
			};
			details.date = cursor.getLong(CallDetailQuery.DATE_COLUMN_INDEX);
			details.duration = cursor.getLong(CallDetailQuery.DURATION_COLUMN_INDEX);
			details.features = cursor.getInt(CallDetailQuery.FEATURES);
			details.geocode = cursor.getString(CallDetailQuery.GEOCODED_LOCATION_COLUMN_INDEX);
			details.transcription = cursor.getString(CallDetailQuery.TRANSCRIPTION_COLUMN_INDEX);
			details.subscription_id=cursor.getString(CallDetailQuery.ACCOUNT_ID);


			details.countryIso = !TextUtils.isEmpty(countryIso) ? countryIso
					: GeoUtil.getCurrentCountryIso(context);

			if (!cursor.isNull(CallDetailQuery.DATA_USAGE)) {
				details.dataUsage = cursor.getLong(CallDetailQuery.DATA_USAGE);
			}

			return details;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}


	/**
	 * Delete specified calls from the call log.
	 *
	 * @param context The context.
	 * @param callIds String of the callIds to delete from the call log, delimited by commas (",").
	 * @param callLogAsyncTaskListenerg The listener to invoke after the entries have been deleted.
	 */
	public static void deleteCalls(
			final Context context,
			final String callIds,
			final CallLogAsyncTaskListener callLogAsyncTaskListener) {
		if (sAsyncTaskExecutor == null) {
			initTaskExecutor();
		}

		sAsyncTaskExecutor.submit(Tasks.DELETE_CALL,
				new AsyncTask<Void, Void, Void>() {
			@Override
			public Void doInBackground(Void... params) {
				context.getContentResolver().delete(
						TelecomUtil.getCallLogUri(context),
						CallLog.Calls._ID + " IN (" + callIds + ")", null);
				return null;
			}

			@Override
			public void onPostExecute(Void result) {
				if (callLogAsyncTaskListener != null) {
					callLogAsyncTaskListener.onDeleteCall();
				}
			}
		});

	}
	
	/**
	 * Delete specified calls from the call log.
	 *
	 * @param context The context.
	 * @param callIds String of the callIds to delete from the call log, delimited by commas (",").
	 * @param callLogAsyncTaskListenerg The listener to invoke after the entries have been deleted.
	 */
	public static void deleteCallsByNumber(
			final Context context,
			final String number,
			final CallLogAsyncTaskListener callLogAsyncTaskListener) {
		if (sAsyncTaskExecutor == null) {
			initTaskExecutor();
		}

		sAsyncTaskExecutor.submit(Tasks.DELETE_CALL,
				new AsyncTask<Void, Void, Void>() {
			@Override
			public Void doInBackground(Void... params) {
				context.getContentResolver().delete(
						TelecomUtil.getCallLogUri(context),
						"number='"+number+"'", null);
				return null;
			}

			@Override
			public void onPostExecute(Void result) {
				if (callLogAsyncTaskListener != null) {
					callLogAsyncTaskListener.onDeleteCall();
				}
			}
		});

	}

	public static void markVoicemailAsRead(final Context context, final Uri voicemailUri) {
		if (sAsyncTaskExecutor == null) {
			initTaskExecutor();
		}

		sAsyncTaskExecutor.submit(Tasks.MARK_VOICEMAIL_READ, new AsyncTask<Void, Void, Void>() {
			@Override
			public Void doInBackground(Void... params) {
				ContentValues values = new ContentValues();
				values.put(Voicemails.IS_READ, true);
				context.getContentResolver().update(
						voicemailUri, values, Voicemails.IS_READ + " = 0", null);

				Intent intent = new Intent(context, CallLogNotificationsService.class);
				intent.setAction(CallLogNotificationsService.ACTION_MARK_NEW_VOICEMAILS_AS_OLD);
				context.startService(intent);
				return null;
			}
		});
	}

	public static void deleteVoicemail(
			final Context context,
			final Uri voicemailUri,
			final CallLogAsyncTaskListener callLogAsyncTaskListener) {
		if (sAsyncTaskExecutor == null) {
			initTaskExecutor();
		}

		sAsyncTaskExecutor.submit(Tasks.DELETE_VOICEMAIL,
				new AsyncTask<Void, Void, Void>() {
			@Override
			public Void doInBackground(Void... params) {
				context.getContentResolver().delete(voicemailUri, null, null);
				return null;
			}

			@Override
			public void onPostExecute(Void result) {
				if (callLogAsyncTaskListener != null) {
					callLogAsyncTaskListener.onDeleteVoicemail();
				}
			}
		});
	}

	@VisibleForTesting
	public static void resetForTest() {
		sAsyncTaskExecutor = null;
	}
}
