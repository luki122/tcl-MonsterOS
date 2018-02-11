package com.android.contacts.activities;

//add by liyang
import com.android.contacts.common.model.account.AccountWithDataSet;
import com.android.contacts.mst.MstBusinessCardResults;
import android.accounts.Account;
import com.android.vcard.VCardEntry;
import com.mediatek.contacts.simcontact.SubInfoUtils;
import com.android.vcard.VCardEntryCommitter;
import com.android.vcard.VCardEntryConstructor;
import com.android.vcard.VCardEntryHandler;
import com.android.vcard.VCardInterpreter;
import com.android.vcard.VCardParser;
import com.android.vcard.VCardParser_V21;
import com.android.vcard.VCardParser_V30;
import com.android.vcard.exception.VCardException;
import com.android.vcard.exception.VCardNestedException;
import com.android.vcard.exception.VCardNotSupportedException;
import com.android.vcard.exception.VCardVersionException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.android.contacts.ContactSaveService;
import com.android.contacts.R;
import com.android.contacts.mst.AddressBean;
import com.android.contacts.mst.MstVcfUtils;
import com.intsig.openapilib.OpenApi;
import com.intsig.openapilib.OpenApiParams;
import com.mediatek.contacts.ExtensionManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import mst.provider.ContactsContract;
import mst.provider.ContactsContract.Groups;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MstBusinessCardScanActivity extends Activity{

	private Button button;
	private TextView textView;
	OpenApi openApi = OpenApi.instance("SAU6QXVYNdXJHL6ateEtBy4T");

	OpenApiParams params = new OpenApiParams() {
		{
			this.setRecognizeLanguage("");
			this.setReturnCropImage(true);
			this.setTrimeEnhanceOption(true);
			this.setSaveCard(false);
		}
	};

	public void createBusinessCardGroup(){
		ContentValues values = new ContentValues();
		values.put(Groups.ACCOUNT_TYPE, "Phone");
		values.put(Groups.ACCOUNT_NAME, "Local Phone Account");
		values.put(Groups.TITLE, getString(R.string.mst_business_card));
		values.put(Groups._ID,0);
		values.put(Groups.GROUP_IS_READ_ONLY,1);

		// Create the new group
		final Uri groupUri = getContentResolver().insert(Groups.CONTENT_URI, values);

		// If there's no URI, then the insertion failed. Abort early because group members can't be
		// added if the group doesn't exist
		if (groupUri == null) {
			Log.e(TAG, "Couldn't create group with label " + "名片");
			addGroupSuccess=false;
			return;
		}
	}
	private int mSubId = SubInfoUtils.getInvalidSubId();
	public class ContactSaveCompletedReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			Bundle bundle=intent.getExtras();
			if(bundle==null) return;
			String lookupUriString=bundle.getString("lookupUri");
			Uri uri=Uri.parse(lookupUriString);
			List<String> pathSegments = uri.getPathSegments();
			int segmentCount = pathSegments.size();
			 if (segmentCount < 3) {
				 return;
			 }
			 String lookupKey = pathSegments.get(2);
			Log.d(TAG,"onReceive,action:"+action+" lookupUriString:"+lookupUriString+" intent:"+intent+" bundle:"+bundle);
			final ContentResolver resolver=getContentResolver();
			Cursor c = resolver.query(uri, new String[]{"_id","name_raw_contact_id","index_in_sim"}, null, null, null);
			if (c == null) {
				return;
			}
			long rawContactId=0;
			int indexInSim=0;
			try {
				if (c.moveToFirst()) {
					//                    long contactId = c.getLong(0);
					rawContactId=c.getLong(1);
					indexInSim=c.getInt(2);
				}
			} finally {
				c.close();
			}

			if(TextUtils.equals(action, BROADCASTACTION_STRING)){
				Log.d(TAG,"add1 to business group&&attach photo");
				Intent saveIntent = ContactSaveService.createGroupUpdateIntentForIcc(MstBusinessCardScanActivity.this,
						0, null, new long[]{rawContactId},
						null, 
						MstBusinessCardScanActivity.this.getClass(),
						"0",
						getString(R.string.mst_business_card), mSubId, new int[]{indexInSim},
						null,
						new AccountWithDataSet("Phone", "Local Phone Account", null));
				Log.d(TAG,"saveIntent:"+saveIntent);
				MstBusinessCardScanActivity.this.startService(saveIntent);				
			}
			
			//处理名片图片			
			renameFile(photoPath, lookupKey+".jpg");
		}

	}

	/** *//**文件重命名 
	 * @param path 文件目录 
	 * @param oldname  原来的文件名 
	 * @param newname 新文件名 
	 */ 
	public void renameFile(String path,String newname){ 
		String oldname=path.substring(path.lastIndexOf("/")+1);
		Log.d(TAG,"path:"+path+" oldname:"+oldname+" newname:"+newname);
		if(!oldname.equals(newname)){//新的文件名和以前文件名不同时,才有必要进行重命名 
			File oldfile=new File(path); 
			File newfile=new File(path.substring(0,path.lastIndexOf("/")+1)+newname); 
			Log.d(TAG,"oldfile:"+oldfile+" newfile:"+newfile+" path:"+newfile.getPath());
			if(!oldfile.exists()){
				return;//重命名文件不存在
			}
			if(newfile.exists())//若在该目录下已经有一个文件和新文件名相同，则不允许重命名 
				Log.d(TAG,newname+"已经存在！"); 
			else{ 
				oldfile.renameTo(newfile); 
			} 
		}else{
			Log.d(TAG,"新文件名和旧文件名相同...");
		}
	}

	private static final int REQUEST_CODE_RECOGNIZE = 0x1001;
	protected static final String TAG = "MstBusinessCardScanActivity";
	private boolean addGroupSuccess=true;
	private MstBusinessCardResults mstBusinessCardResults;
	public static final String BROADCASTACTION_STRING="com.android.contacts.mst.MstBusinessCardResults";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_scan);
		textView=(TextView)findViewById(R.id.text1);
		button=(Button)findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Cursor cursor=null;
				try{
					final ContentResolver resolver = getContentResolver();
					cursor=resolver.query(Groups.CONTENT_URI, null, "_id=0", null,null);
					Log.d(TAG,"cursor:"+(cursor==null?"null":cursor.getCount()));					
					if(cursor==null||cursor.getCount()==0){
						createBusinessCardGroup();
					}else{
						cursor.moveToFirst();
						int delete=cursor.getInt(cursor.getColumnIndex(Groups.DELETED));
						String title=cursor.getString(cursor.getColumnIndex(Groups.TITLE));
						if(delete==1){
							ContentValues values = new ContentValues();
							values.put(Groups.DELETED,0);
							values.put(Groups.ACCOUNT_TYPE, "Phone");
							values.put(Groups.ACCOUNT_NAME, "Local Phone Account");
							values.put(Groups.TITLE, getString(R.string.mst_business_card));
							values.put(Groups.GROUP_IS_READ_ONLY,1);
							int rows=resolver.update(Groups.CONTENT_URI, values, "_id=0",null);
							if(rows==0){
								addGroupSuccess=false;
							}
						}
					}
				}catch(Exception e){
					Log.d(TAG,"e:"+e);
					addGroupSuccess=false;
				}finally{
					cursor.close();
					cursor=null;
				}

				if(addGroupSuccess) testRecognizeCapture();
			}
		});
		mstBusinessCardResults=new MstBusinessCardResults(MstBusinessCardScanActivity.this);
		myReceiver=new ContactSaveCompletedReceiver();
		registerReceiver(myReceiver,new IntentFilter(BROADCASTACTION_STRING));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try{
			unregisterReceiver(myReceiver);
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	private ContactSaveCompletedReceiver myReceiver;
	public void testRecognizeCapture() {
		if(openApi.isCamCardInstalled(this)){
			if ( openApi.isExistAppSupportOpenApi(this) ){
				openApi.recognizeCardByCapture(this, REQUEST_CODE_RECOGNIZE, params);
			}else{
				Toast.makeText(this, "No app support openapi", Toast.LENGTH_LONG).show();
				Log.d(TAG,"camcard download link:"+openApi.getDownloadLink());
			}
		}else{
			Toast.makeText(this, "No CamCard", Toast.LENGTH_LONG).show();
			Log.d(TAG,"camcard download link:"+openApi.getDownloadLink());
		}
	}

	public void testRecognizeImage(String path) {
		if ( openApi.isExistAppSupportOpenApi(this) ){
			openApi.recognizeCardByImage(this, path, REQUEST_CODE_RECOGNIZE, params);
		}	else {
			Toast.makeText(this, "No app support openapi", Toast.LENGTH_LONG).show();
			Log.d(TAG,"camcard download link:"+openApi.getDownloadLink());
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_CODE_RECOGNIZE) {
				showResult(data.getStringExtra(OpenApi.EXTRA_KEY_VCF),
						data.getStringExtra(OpenApi.EXTRA_KEY_IMAGE));
			}
		} else {
			int errorCode=data.getIntExtra(openApi.ERROR_CODE, 200);
			String errorMessage=data.getStringExtra(openApi.ERROR_MESSAGE);
			Log.d(TAG,"ddebug error " + errorCode+","+errorMessage);
			Toast.makeText(this, "Recognize canceled/failed. + ErrorCode " + errorCode + " ErrorMsg " + errorMessage,
					Toast.LENGTH_LONG).show();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}



	private String photoPath;
	private void showResult(String vcf, String path) {
		//		Intent intent = new Intent(this, MstBusinessCardShowResultActivity.class);
		//		intent.putExtra("result_vcf", vcf);
		//		intent.putExtra("result_trimed_image", path);
		//		startActivity(intent);
		textView.setText(vcf);
		photoPath=path;


		//		List<AddressBean> addressBeans = MstVcfUtils.importVCFFileContact(vcf);  
		//		Log.d(TAG,addressBeans.size()+"");  
		//		for (AddressBean addressBean : addressBeans) {  
		//			Log.d(TAG,"tureName : " + addressBean.getTrueName());  
		//			Log.d(TAG,"mobile : " + addressBean.getMobile());  
		//			Log.d(TAG,"workMobile : " + addressBean.getWorkMobile());  
		//			Log.d(TAG,"Email : " + addressBean.getEmail());  
		//			Log.d(TAG,"--------------------------------");  
		//		}  

		final VCardEntryConstructor constructor = new VCardEntryConstructor(0,
				new Account("Phone","Local Phone Account"), null);
		constructor.addEntryHandler(mstBusinessCardResults);
		InputStream is = new ByteArrayInputStream(vcf.getBytes());
		Log.d(TAG,"vcf:"+vcf+" is:"+is+" path:"+path);
		boolean successful = false;
		try {
			//            if (uri != null) {
			//                Log.i(TAG, "start importing one vCard (Uri: " + uri + ")");
			//                is = mResolver.openInputStream(uri);
			//            } else if (request.data != null){
			//                Log.i(TAG, "start importing one vCard (byte[])");
			//                is = new ByteArrayInputStream(request.data);
			//            }

			if (is != null) {
				successful = readOneVCard(is, 0, null, constructor,
						possibleVCardVersions);
			}
		} catch (Exception e) {
			successful = false;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
					// ignore
				}
			}
		}		
	}


	final static int VCARD_VERSION_V21 = 1;
	final static int VCARD_VERSION_V30 = 2;    
	int[] possibleVCardVersions = new int[] {
			VCARD_VERSION_V21,
			VCARD_VERSION_V30
	};

	private VCardParser mVCardParser;
	private boolean readOneVCard(InputStream is, int vcardType, String charset,
			final VCardInterpreter interpreter,
			final int[] possibleVCardVersions) {
		boolean successful = false;
		final int length = possibleVCardVersions.length;
		for (int i = 0; i < length; i++) {
			final int vcardVersion = possibleVCardVersions[i];
			try {
				if (i > 0 && (interpreter instanceof VCardEntryConstructor)) {
					// Let the object clean up internal temporary objects,
					((VCardEntryConstructor) interpreter).clear();
				}

				// We need synchronized block here,
				// since we need to handle mCanceled and mVCardParser at once.
				// In the worst case, a user may call cancel() just before creating
				// mVCardParser.
				synchronized (this) {
					mVCardParser = (vcardVersion == VCARD_VERSION_V30 ?
							new VCardParser_V30(vcardType) :
								new VCardParser_V21(vcardType));
					//                    if (isCancelled()) {
					//                        Log.i(TAG, "ImportProcessor already recieves cancel request, so " +
					//                                "send cancel request to vCard parser too.");
					//                        mVCardParser.cancel();
					//                    }
				}
				Log.d(TAG,"mVCardParser:"+mVCardParser);
				mVCardParser.parse(is, interpreter);

				successful = true;
				break;
			} catch (IOException e) {
				Log.e(TAG, "IOException was emitted: " + e.getMessage());
			} catch (VCardNestedException e) {
				// This exception should not be thrown here. We should instead handle it
				// in the preprocessing session in ImportVCardActivity, as we don't try
				// to detect the type of given vCard here.
				//
				// TODO: Handle this case appropriately, which should mean we have to have
				// code trying to auto-detect the type of given vCard twice (both in
				// ImportVCardActivity and ImportVCardService).
				Log.e(TAG, "Nested Exception is found.");
			} catch (VCardNotSupportedException e) {
				Log.e(TAG, e.toString());
			} catch (VCardVersionException e) {
				if (i == length - 1) {
					Log.e(TAG, "Appropriate version for this vCard is not found.");
				} else {
					// We'll try the other (v30) version.
				}
			} catch (VCardException e) {
				Log.e(TAG, e.toString());
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
					}
				}
			}
		}

		return successful;
	}
}
