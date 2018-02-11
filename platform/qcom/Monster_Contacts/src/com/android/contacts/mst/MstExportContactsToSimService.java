package com.android.contacts.mst;

//add by liyang
import java.util.Arrays;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.EncodeException;
import android.os.ServiceManager;
import com.android.contacts.activities.MstContactImportExportActivity;
import com.android.contacts.R;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import mst.provider.ContactsContract;
import mst.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

public class MstExportContactsToSimService extends Service {


	public static boolean isCancel=false;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		isCancel=false;
		Log.d(TAG,"inCreate");
	}

	private int subId=-1;
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG,"onStartCommand,isRunning:"+isRunning);
		isCancel=false;
		if(isRunning) {
			Toast.makeText(this,"请等待上次导出完成再操作", Toast.LENGTH_LONG).show();
			return START_STICKY;
		}


		//		Toast.makeText(this, "正在后台导出所选联系人", Toast.LENGTH_LONG).show();
		Bundle bundle=null;
		if(intent!=null){
			bundle=intent.getExtras();
		}
		if(bundle!=null){
			subId=bundle.getInt("subId");
			long[] dataIds=bundle.getLongArray("dataIds");
			Log.d(TAG,"subId:"+subId+" dataIds:"+Arrays.toString(dataIds));
			doExportToSim(dataIds);
		}
		return super.onStartCommand(intent, flags,startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG,"onDestroy");
	}
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {


			case CONTACTS_EXPORTED:
				Log.d(TAG,"case CONTACTS_EXPORTED");
				int result = (Integer)msg.obj;
				String resultString=null;
				if (result == SUCCESS) {
					resultString="导出联系人成功";
				} else if (result == NO_CONTACTS) {
					resultString="未选择导出的联系人";
				}

				NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
				manager.cancel(1111);
				Notification notification = new Notification(R.drawable.ic_launcher_contacts, resultString, System.currentTimeMillis());
				notification.setLatestEventInfo(MstExportContactsToSimService.this, resultString,resultString, null); 
				manager.notify(1111, notification);
				isRunning=false;
				MstExportContactsToSimService.this.stopSelf();
				break;

			case CONTACTS_EXPORTE_FAIL:
				if(index==0) {
					sendBroadcast(new Intent("CONTACTS_EXPORT_FULL"));
				}else{
					Intent intent=new Intent("CONTACTS_EXPORT_PART_FULL");
					intent.putExtra("index", index);
					intent.putExtra("totalContacts", totalContacts);
					sendBroadcast(intent);
				}
				isRunning=false;
				MstExportContactsToSimService.this.stopSelf();
				break;

			default:
				break;
			}
		}
	};
	protected static final int SUCCESS = 1;
	private static final int FAILURE = 0;
	private static final int NO_CONTACTS = 2;
	protected static final String TAG = "MstExportContactsToSimService";
	private int mResult = SUCCESS;
	protected boolean mIsForeground = false;
	private boolean mSimContactsLoaded = false;
	private static final int CONTACTS_EXPORTED = 1;
	private static final int CONTACTS_EXPORTE_FAIL = 2;

	protected Uri resolveIntent(int subId) {
		if (subId != -1) {
			return Uri.parse("content://icc/adn/subId/" + subId);
		} else {
			return Uri.parse("content://icc/adn");
		}
	}
	private boolean isRunning;
	private Cursor contactsCursor;
	private int totalContacts=0;
	private void doExportToSim(final long[] dataIds) {

		Log.d(TAG,"doExportToSim");		
		if(dataIds==null||dataIds.length==0) return;
		new Thread(new Runnable() {
			public void run() {
				index=0;
				mResult = SUCCESS;
				contactsCursor = getContactsContentCursor(dataIds);
				isRunning=true;
				totalContacts=(contactsCursor==null?0:contactsCursor.getCount());
				Log.d(TAG,"doExportToSim,totalContacts:"+totalContacts);
				if (totalContacts < 1) {
					// If there are no contacts in Phone book display it to user.
					mResult = NO_CONTACTS;
				} else {
					//We need to load SIM Records atleast once before exporting to SIM.
					if (!mSimContactsLoaded) {
						getContentResolver().query(resolveIntent(subId), null, null,null, null);
						mSimContactsLoaded = true;
					}
					Intent intent0=new Intent("CONTACTS_EXPORT_DOING");
					intent0.putExtra("index", index);
					intent0.putExtra("totalContacts", totalContacts);
					sendBroadcast(intent0);

					for (int i = 0; !isCancel&&contactsCursor.moveToNext(); i++) {
						boolean success=populateContactDataFromCursor(contactsCursor);
						if(success) {
							index++;	
							Intent intent=new Intent("CONTACTS_EXPORT_DOING");
							intent.putExtra("index", index);
							intent.putExtra("totalContacts", totalContacts);
							sendBroadcast(intent);
						}

						else {
							if(index==0) {
								sendBroadcast(new Intent("CONTACTS_EXPORT_FULL"));
							}else{
								Intent intent=new Intent("CONTACTS_EXPORT_PART_FULL");
								intent.putExtra("index", index);
								intent.putExtra("totalContacts", totalContacts);
								sendBroadcast(intent);
							}
							break;
						}
					}
				}
				contactsCursor.close();

				//				if(index==totalContacts){
				//					Message message = Message.obtain(mHandler, CONTACTS_EXPORTED,(Integer)mResult);
				//					mHandler.sendMessage(message);
				//				}

				isRunning=false;


				if(isCancel){
					Intent intent=new Intent("USER_CANCEL_EXPORT");
					intent.putExtra("index", index);
					intent.putExtra("totalContacts", totalContacts);
					sendBroadcast(intent);
				}
				MstExportContactsToSimService.this.stopSelf();
			}
		}).start();
	}

	private Cursor getContactsContentCursor(long[] dataIds) {
		Uri phoneBookContentUri = Phone.CONTENT_URI;
		StringBuilder sb=new StringBuilder("(");
		for(long l:dataIds){
			sb.append(l+",");
		}
		sb.setLength(sb.length()-1);
		sb.append(")");
		Log.d(TAG,"getContactsContentCursor,sb:"+sb.toString());
		String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER +
				"='1' AND (account_type is NULL OR account_type !=?) AND _id IN "+sb.toString();
		String[] selectionArg = new String[] {"SIM"};

		return getContentResolver().query(phoneBookContentUri, null, selection,
				selectionArg, null);
	}


	int index=0;
	private boolean populateContactDataFromCursor(final Cursor dataCursor) {		
		Log.d(TAG,"populateContactDataFromCursor,index:"+index);
		Uri uri = resolveIntent(subId);
		if (uri == null) {
			Log.d(TAG," populateContactDataFromCursor: uri is null, return ");
			return false;
		}
		Uri contactUri;
		int nameIdx = dataCursor
				.getColumnIndex(ContactsContract.Data.DISPLAY_NAME);
		int phoneIdx = dataCursor
				.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

		final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
				.getService(Context.TELEPHONY_SERVICE));

		// Extract the name.
		String tempName = dataCursor.getString(nameIdx);
		String name=null;
		if (!TextUtils.isEmpty(tempName)) {
			name = cutLongNameByte(tempName,13);
		}
		// Extract the phone number.
		String rawNumber = dataCursor.getString(phoneIdx);
		String number = PhoneNumberUtils.normalizeNumber(rawNumber);

		ContentValues values = new ContentValues();
		values.put("tag", name);
		values.put("number", number);
		Log.d(TAG, "name : " + name + " number : " + number+" uri:"+uri);
		contactUri = getContentResolver().insert(uri, values);
		Log.d(TAG,"index:"+index+" contactUri:"+contactUri);
		if (contactUri == null) {
			Log.e("ExportContactsToSim", "Failed to export contact to SIM for " +
					"name : " + name + " number : " + number);
		}
		return contactUri!=null;
	}


	//用于对姓名自动截断
	private int SpecEncodingArrLen = 44;
	private short[][] specialEncoding = {
			{0x0040, 0x0000}, //@
			{0x00A3, 0x0001}, 
			{0x0024, 0x0002},
			{0x00A5, 0x0003},
			{0x00E8, 0x0004},
			{0x00E9, 0x0005},
			{0x00F9, 0x0006},
			{0x00EC, 0x0007},
			{0x00F2, 0x0008},
			{0x00C7, 0x0009},
			{0x0020, 0x000A},
			{0x00D8, 0x000B},
			{0x00F8, 0x000C},
			{0x0020, 0x000D},
			{0x00C5, 0x000E},
			{0x00E5, 0x000F},
			{0x0394, 0x0010},
			{0x005F, 0x0011},
			{0x03A6, 0x0012},
			{0x0393, 0x0013},
			{0x039B, 0x0014},
			{0x03A9, 0x0015},
			{0x03A0, 0x0016},
			{0x03A8, 0x0017},
			{0x03A3, 0x0018},
			{0x0398, 0x0019},
			{0x039E, 0x001A},
			{0x00C6, 0x001C},
			{0x00E6, 0x001D},
			{0x00DF, 0x001E},
			{0x00C9, 0x001F},
			{0x00A4, 0x0024},
			{0x00A1, 0x0040},
			{0x00C4, 0x005B},
			{0x00D6, 0x005C},
			{0x00D1, 0x005D},
			{0x00DC, 0x005E},
			{0x00A7, 0x005F},
			{0x00BF, 0x0060},
			{0x00E4, 0x007B},
			{0x00F6, 0x007C},
			{0x00F1, 0x007D},
			{0x00FC, 0x007E},
			{0x00E0, 0x007F},
	};


	public int encodeUCS2_0x81(char[] src, char[] des, int maxLen)
	{
		int i, j, len;
		int base = 0xFF000000;
		short[] tmpAlphaId = new short[40*4+4+1];
		char[] temp = new char[5];

		len = src.length;
		for (i=0,j=0; i<len; i+=4, j++) {
			temp[0] = src[i];
			temp[1] = src[i + 1];
			temp[2] = src[i + 2];
			temp[3] = src[i + 3];
			temp[4] = 0;
			tmpAlphaId[j] = (short) rild_sms_hexCharToDecInt(temp, 4);            
		}
		tmpAlphaId[j] = '\0';
		len = j;

		if (len <= 3)   // at least 3 characters
			return 0;
		if ( ((len+3)*2+1) > maxLen) // the destinaiton buffer is not enough(include '\0')
			return 0;
		for(i=0; i<len; i++) {
			int needSpecialEncoding = 0;
			if((tmpAlphaId[i] & 0x8000) > 0) return 0;
			for(int k=0; k<SpecEncodingArrLen; k++){
				if(tmpAlphaId[i] == specialEncoding[k][0]){
					tmpAlphaId[i] = specialEncoding[k][1];
					needSpecialEncoding = 1;
					break;
				}
			}
			if(needSpecialEncoding != 1){
				if(tmpAlphaId[i] < 128){
					if(tmpAlphaId[i] == 0x0020 ||
							tmpAlphaId[i] == 0x005E ||
							tmpAlphaId[i] == 0x007B ||
							tmpAlphaId[i] == 0x007D ||
							tmpAlphaId[i] == 0x005B ||
							tmpAlphaId[i] == 0x007E ||
							tmpAlphaId[i] == 0x005D ||
							tmpAlphaId[i] == 0x005C ||
							tmpAlphaId[i] == 0x007C )
						return 0;
					else       
					{   
						if(tmpAlphaId[i] == 0x0060){    
							if(base == 0xFF000000){
								base = 0;
								tmpAlphaId[i] = 0x00E0;
							}else{
								return 0;
							}
						}
						continue;
					}
				}
				if(base == 0xFF000000){
					base = tmpAlphaId[i] & 0x7f80;
				}
				tmpAlphaId[i] ^= base;
				if ( tmpAlphaId[i] >= 128)
					break;
				tmpAlphaId[i] |= 0x80;
			}

		}

		if (i != len)
			return 0;

		int realLen = 0;
		for (i=0; i<len; i++) {
			if((tmpAlphaId[i] & 0xFF00) != 0x1B00){
				// do nothing
			}
			else{
				realLen++;
			}
		}
		realLen += len;

		return realLen;
	}


	//进行UCS编码
	public String encodeATUCS(String input) {
		byte[] textPart;
		StringBuilder output;

		output = new StringBuilder();
		if(input.length() > 40)
		{
			input = input.substring(0, 40);
		}

		for (int i = 0; i < input.length(); i++) {
			String hexInt = Integer.toHexString(input.charAt(i));
			for (int j = 0; j < (4 - hexInt.length()); j++)
				output.append("0");
			output.append(hexInt);
		}

		return output.toString();
	}


	public int rild_sms_hexCharToDecInt(char[] hex, int length)
	{
		int i = 0;
		int value, digit;

		for (i = 0, value = 0; i < length && hex[i] != '\0'; i++)
		{
			if (hex[i]>='0' && hex[i]<='9')
			{
				digit = hex[i] - '0';
			}
			else if ( hex[i]>='A' && hex[i] <= 'F')
			{
				digit = hex[i] - 'A' + 10;
			}
			else if ( hex[i]>='a' && hex[i] <= 'f')
			{
				digit = hex[i] - 'a' + 10;
			}
			else
			{
				return -1;
			}
			value = value*16 + digit;
		}

		return value;
	}

	public int countTheLength(char[] line, int maxlen)
	{
		char[] alphaId = new char[40*4+4+1];
		char[] temp = new char[5];
		int tmp, i, j;
		int nameLimited = maxlen;

		//pack Alpha Id
		int len = line.length;
		if ((len%4) != 0) {
			//LOGE("The alphaId should encode using Hexdecimal: %s", line);
		}else if(len > (40*4)) {
			//LOGE("The alphaId shouldn't longer than RIL_MAX_PHB_NAME_LEN");
		}

		for (i=0,j=0; i<len; i+=4, j++) {
			temp[0] = line[i];
			temp[1] = line[i + 1];
			temp[2] = line[i + 2];
			temp[3] = line[i + 3];
			temp[4] = 0;
			tmp = rild_sms_hexCharToDecInt(temp, 4);

			if (tmp >= 128) {
				break;
			}
			alphaId[j] = (char)tmp;
			//alphaId[ril_max_phb_name_len] = '\0';
		}
		alphaId[j] = '\0';


		if (i != len) {
			len /= 4;

			if (encodeUCS2_0x81(line, alphaId, alphaId.length) > 0) {  //try UCS2_0x81 coding
				return (nameLimited - 3);
			}
			else {
				// UCS2 coding
				return (nameLimited - 2) / 2;
			}
		}       
		return nameLimited;
	}

	//返回根据SIM卡能保存的最大姓名长度，使用UCS2编码裁剪过后的姓名，也就是我们需要使用的自动截断后的姓名
	//simTag  传入的姓名
	//dstSlotId  卡槽ID (0或者1)
	//iTel   ITelepony的接口，用于获取对应SIM卡能保存的姓名的最大长度
	private String cutLongNameByte(String simTag, int nameLimit) {
		int len = simTag.length();
		try {
			//7 bit string
			byte[] iraResult = com.android.internal.telephony.GsmAlphabet.stringToGsm7BitPacked(simTag);
			if (iraResult.length > nameLimit && simTag.length() > 0){
				simTag = simTag.substring(0,simTag.length()-1);
				simTag = cutLongNameByte(simTag, nameLimit);
			}
		} catch (EncodeException e) {
			String temp = encodeATUCS(simTag);
			int length = countTheLength(temp.toCharArray(), nameLimit);                                
			if (len > length) {
				simTag = simTag.substring(0, length);
			}
		}
		return simTag;
	}
}