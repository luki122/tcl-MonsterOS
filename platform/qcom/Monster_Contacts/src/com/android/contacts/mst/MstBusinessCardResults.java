//add by liyang


package com.android.contacts.mst;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import mst.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.android.contacts.R;
import com.android.contacts.activities.PeopleActivity;
import com.android.vcard.VCardEntry;
import com.android.vcard.VCardEntry.PhoneData;
import com.android.vcard.VCardEntry.OrganizationData;
import com.android.vcard.VCardEntry.NoteData;
import com.android.vcard.VCardEntry.EmailData;
import com.android.vcard.VCardEntry.ImData;
import com.android.vcard.VCardEntry.PhotoData;
import com.android.vcard.VCardEntry.WebsiteData;
import com.android.vcard.VCardEntry.PostalData
;
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

public class MstBusinessCardResults  implements VCardEntryHandler{

	private static final String TAG = "MstBusinessCardResults";
	private Context mContext;
	public MstBusinessCardResults(Context context){
		mContext=context;
	}
	
	
	@Override
	public void onEntryCreated(VCardEntry entry) {
		Log.d(TAG,"onEntryCreated,entry:"+entry+" name:"+entry.getDisplayName());
		//		if(entry==null) return;

		List<PhoneData> phoneDatas=entry.getPhoneList();
		List<OrganizationData> organizationDatas=entry.getOrganizationList();
		List<NoteData> noteDatas=entry.getNotes();
		List<EmailData> emailDatas=entry.getEmailList();
		List<PostalData> postalDatas=entry.getPostalList();
		List<ImData> imDatas=entry.getImList();
//		Log.d(TAG,"phoneDatas:"+phoneDatas+"\norganizationDatas:"+organizationDatas+"\nnoteDatas:"+noteDatas
//				+"\nemailDatas:"+emailDatas+"\npostalDatas:"+postalDatas+"\nimDatas:"+imDatas);
		
		String NAME=entry.getDisplayName();
		String PHONE=(phoneDatas!=null&&phoneDatas.size()>0?phoneDatas.get(0).getNumber():null);
		int PHONE_TYPE=(phoneDatas!=null&&phoneDatas.size()>0?phoneDatas.get(0).getType():NO_PHONE_TYPE);
		//			boolean PHONE_ISPRIMARY,
		String SECONDARY_PHONE=(phoneDatas!=null&&phoneDatas.size()>1?phoneDatas.get(1).getNumber():null);	
		int SECONDARY_PHONE_TYPE=(phoneDatas!=null&&phoneDatas.size()>1?phoneDatas.get(1).getType():NO_PHONE_TYPE);
		//			boolean SECONDARY_PHONE_ISPRIMARY,
		String TERTIARY_PHONE=(phoneDatas!=null&&phoneDatas.size()>2?phoneDatas.get(2).getNumber():null);
		int TERTIARY_PHONE_TYPE=(phoneDatas!=null&&phoneDatas.size()>2?phoneDatas.get(2).getType():NO_PHONE_TYPE);
		//			boolean TERTIARY_SECONDARY_PHONE_ISPRIMARY,

		String COMPANY=(organizationDatas!=null&&organizationDatas.size()>0?organizationDatas.get(0).getOrganizationName():null);

		String JOB_TITLE=(organizationDatas!=null&&organizationDatas.size()>0?organizationDatas.get(0).getTitle():null);

		String NOTES=(noteDatas!=null&&noteDatas.size()>0?noteDatas.get(0).getNote():null);
		String EMAIL=(emailDatas!=null&&emailDatas.size()>0?emailDatas.get(0).getAddress():null);
		int EMAIL_TYPE=(emailDatas!=null&&emailDatas.size()>0?emailDatas.get(0).getType():NO_PHONE_TYPE);

		//			boolean EMAIL_ISPRIMARY,
		String SECONDARY_EMAIL=(emailDatas!=null&&emailDatas.size()>1?emailDatas.get(1).getAddress():null);
		int SECONDARY_EMAIL_TYPE=(emailDatas!=null&&emailDatas.size()>1?emailDatas.get(1).getType():NO_PHONE_TYPE);
		//			boolean SECONDARY_EMAIL_ISPRIMARY,
		String TERTIARY_EMAIL=(emailDatas!=null&&emailDatas.size()>2?emailDatas.get(2).getAddress():null);
		int TERTIARY_EMAIL_TYPE=(emailDatas!=null&&emailDatas.size()>2?emailDatas.get(2).getType():NO_PHONE_TYPE);
		//			boolean TERTIARY_EMAIL_ISPRIMARY,
		String POSTAL=(postalDatas!=null&&postalDatas.size()>0?postalDatas.get(0).getFormattedAddress(0):null);
		int POSTAL_TYPE=(postalDatas!=null&&postalDatas.size()>0?postalDatas.get(0).getType():NO_PHONE_TYPE);
		//			boolean POSTAL_ISPRIMARY,

		String IM_HANDLE=(imDatas!=null&&imDatas.size()>0?imDatas.get(0).getAddress():null);
		int IM_PROTOCOL=(imDatas!=null&&imDatas.size()>0?imDatas.get(0).getProtocol():NO_PHONE_TYPE);

		
		Log.d(TAG,"getNewContactIntentForVcf,name:"+NAME
				+"\nPHONE:"+PHONE+" PHONE_TYPE:"+PHONE_TYPE
				+"\nSECONDARY_PHONE:"+SECONDARY_PHONE+" SECONDARY_PHONE_TYPE:"+SECONDARY_PHONE_TYPE
				+"\nTERTIARY_PHONE:"+TERTIARY_PHONE+" TERTIARY_PHONE_TYPE:"+TERTIARY_PHONE_TYPE
				+"\nCOMPANY:"+COMPANY
				+"\nJOB_TITLE:"+JOB_TITLE
				+"\nNOTES:"+NOTES
				+"\nEMAIL:"+EMAIL+" EMAIL_TYPE:"+EMAIL_TYPE
				+"\nSECONDARY_EMAIL:"+SECONDARY_EMAIL+" SECONDARY_EMAIL_TYPE:"+SECONDARY_EMAIL_TYPE
				+"\nTERTIARY_EMAIL:"+TERTIARY_EMAIL+" TERTIARY_EMAIL_TYPE:"+TERTIARY_EMAIL_TYPE
				+"\nPOSTAL:"+POSTAL+" POSTAL_TYPE:"+POSTAL_TYPE
				+"\nIM_HANDLE:"+IM_HANDLE+" IM_PROTOCOL:"+IM_PROTOCOL);
		
		if(TextUtils.isEmpty(NAME)
				&&TextUtils.isEmpty(PHONE)
				&&TextUtils.isEmpty(SECONDARY_PHONE)
				&&TextUtils.isEmpty(TERTIARY_PHONE)
				&&TextUtils.isEmpty(COMPANY)
				&&TextUtils.isEmpty(JOB_TITLE)
				&&TextUtils.isEmpty(NOTES)
				&&TextUtils.isEmpty(EMAIL)
				&&TextUtils.isEmpty(SECONDARY_EMAIL)
				&&TextUtils.isEmpty(TERTIARY_EMAIL)
				&&TextUtils.isEmpty(POSTAL)
				&&TextUtils.isEmpty(IM_HANDLE)){
			Toast.makeText(mContext, mContext.getString(R.string.mst_business_card_scan_fail), Toast.LENGTH_LONG).show();
			return;
		}
		Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
		intent.setClassName("com.android.contacts","com.android.contacts.activities.MstContactEditorActivity"); 
		intent.putExtra("isForBusinessCard", true);

		if (NAME != null) {
			intent.putExtra(ContactsContract.Intents.Insert.NAME, NAME);
		}


		if (PHONE != null) {
			intent.putExtra(ContactsContract.Intents.Insert.PHONE, PHONE);
		}        
		if (PHONE_TYPE != NO_PHONE_TYPE) {
			intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, PHONE_TYPE);
		}
		//		if (PHONE_ISPRIMARY) {
		//			intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, PHONE_TYPE);
		//		}		

		if (SECONDARY_PHONE != null) {
			intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, SECONDARY_PHONE);
		}        
		if (SECONDARY_PHONE_TYPE != NO_PHONE_TYPE) {
			intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE, SECONDARY_PHONE_TYPE);
		}

		if (TERTIARY_PHONE != null) {
			intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE, TERTIARY_PHONE);
		}        
		if (TERTIARY_PHONE_TYPE != NO_PHONE_TYPE) {
			intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE_TYPE, TERTIARY_PHONE_TYPE);
		}


		if (COMPANY != null) {
			intent.putExtra(ContactsContract.Intents.Insert.COMPANY, COMPANY);
		}


		if (JOB_TITLE != null) {
			intent.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, JOB_TITLE);
		}


		if (NOTES != null) {
			intent.putExtra(ContactsContract.Intents.Insert.NOTES, NOTES);
		}


		if (EMAIL != null) {
			intent.putExtra(ContactsContract.Intents.Insert.EMAIL, EMAIL);
		}
		if (EMAIL_TYPE != NO_PHONE_TYPE) {
			intent.putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, EMAIL_TYPE);
		}

		if (SECONDARY_EMAIL != null) {
			intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL, SECONDARY_EMAIL);
		}
		if (SECONDARY_EMAIL_TYPE != NO_PHONE_TYPE) {
			intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL_TYPE, SECONDARY_EMAIL_TYPE);
		}

		if (TERTIARY_EMAIL != null) {
			intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_EMAIL, TERTIARY_EMAIL);
		}
		if (TERTIARY_EMAIL_TYPE != NO_PHONE_TYPE) {
			intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_EMAIL_TYPE, TERTIARY_EMAIL_TYPE);
		}


		if (POSTAL != null) {
			intent.putExtra(ContactsContract.Intents.Insert.POSTAL, POSTAL);
		}
		if (POSTAL_TYPE != NO_PHONE_TYPE) {
			intent.putExtra(ContactsContract.Intents.Insert.POSTAL_TYPE, POSTAL_TYPE);
		}


		if (IM_HANDLE != null) {
			intent.putExtra(ContactsContract.Intents.Insert.IM_HANDLE, IM_HANDLE);
		}
		if (IM_PROTOCOL != NO_PHONE_TYPE) {
			intent.putExtra(ContactsContract.Intents.Insert.IM_PROTOCOL, IM_PROTOCOL);
		}
		
//		Intent intent=getNewContactIntentForVcf(
//				entry.getDisplayName(), 
//				(phoneDatas.size()>0?phoneDatas.get(0).getNumber():null), 
//				(phoneDatas.size()>0?phoneDatas.get(0).getType():NO_PHONE_TYPE),
//				//				(phoneDatas.size()>0?phoneDatas.get(0).isPrimary():false), 
//				(phoneDatas.size()>1?phoneDatas.get(1).getNumber():null), 
//				(phoneDatas.size()>1?phoneDatas.get(1).getType():NO_PHONE_TYPE),
//				//				(phoneDatas.size()>1?phoneDatas.get(1).isPrimary():false), 
//				(phoneDatas.size()>2?phoneDatas.get(2).getNumber():null), 
//				(phoneDatas.size()>2?phoneDatas.get(2).getType():NO_PHONE_TYPE),
//				//				(phoneDatas.size()>2?phoneDatas.get(2).isPrimary():false), 
//				(organizationDatas.size()>0?organizationDatas.get(0).getOrganizationName():null), 
//				(organizationDatas.size()>0?organizationDatas.get(0).getTitle():null), 
//				(noteDatas.size()>0?noteDatas.get(0).getNote():null), 
//				(emailDatas.size()>0?emailDatas.get(0).getAddress():null), 
//				(emailDatas.size()>0?emailDatas.get(0).getType():NO_PHONE_TYPE),
//				//				(emailDatas.size()>0?emailDatas.get(0).isPrimary():false), 
//				(emailDatas.size()>1?emailDatas.get(1).getAddress():null), 
//				(emailDatas.size()>1?emailDatas.get(1).getType():NO_PHONE_TYPE),
//				//				(emailDatas.size()>1?emailDatas.get(1).isPrimary():false), 
//				(emailDatas.size()>2?emailDatas.get(2).getAddress():null), 
//				(emailDatas.size()>2?emailDatas.get(2).getType():NO_PHONE_TYPE),
//				//				(emailDatas.size()>2?emailDatas.get(2).isPrimary():false), 
//				(postalDatas.size()>0?postalDatas.get(0).getFormattedAddress(0):null), 
//				(postalDatas.size()>0?postalDatas.get(0).getType():NO_PHONE_TYPE),
//				//				(postalDatas.size()>0?postalDatas.get(0).isPrimary():false),
//				(imDatas.size()>0?imDatas.get(0).getAddress():null), 
//				(imDatas.size()>0?imDatas.get(0).getProtocol():NO_PHONE_TYPE)
//				//				(imDatas.size()>0?imDatas.get(0).isPrimary():false)		
//				);
		intent.putExtra("fromMstBusiness", true);
		mContext.startActivity(intent);
	}


	private final int NO_PHONE_TYPE = -1;
	public Intent getNewContactIntentForVcf(
			String NAME, 
			String PHONE, 
			int PHONE_TYPE,
			//			boolean PHONE_ISPRIMARY,
			String SECONDARY_PHONE, 			
			int SECONDARY_PHONE_TYPE,
			//			boolean SECONDARY_PHONE_ISPRIMARY,
			String TERTIARY_PHONE,
			int TERTIARY_PHONE_TYPE,
			//			boolean TERTIARY_SECONDARY_PHONE_ISPRIMARY,
			String COMPANY,
			String JOB_TITLE,
			String NOTES,
			String EMAIL,
			int EMAIL_TYPE,
			//			boolean EMAIL_ISPRIMARY,
			String SECONDARY_EMAIL,
			int SECONDARY_EMAIL_TYPE,
			//			boolean SECONDARY_EMAIL_ISPRIMARY,
			String TERTIARY_EMAIL,
			int TERTIARY_EMAIL_TYPE,
			//			boolean TERTIARY_EMAIL_ISPRIMARY,
			String POSTAL,			
			int POSTAL_TYPE,
			//			boolean POSTAL_ISPRIMARY,
			String IM_HANDLE,
			int IM_PROTOCOL
			//			boolean IM_ISPRIMARY
			) {

		Log.d(TAG,"getNewContactIntentForVcf,name:"+NAME+"\nPHONE:"+PHONE+" PHONE_TYPE:"+PHONE_TYPE
				+"\nSECONDARY_PHONE:"+SECONDARY_PHONE+" SECONDARY_PHONE_TYPE:"+SECONDARY_PHONE_TYPE
				+"\nTERTIARY_PHONE:"+TERTIARY_PHONE+" TERTIARY_PHONE_TYPE:"+TERTIARY_PHONE_TYPE
				+"\nCOMPANY:"+COMPANY
				+"\nJOB_TITLE:"+JOB_TITLE
				+"\nNOTES:"+NOTES
				+"\nEMAIL:"+EMAIL+" EMAIL_TYPE:"+EMAIL_TYPE
				+"\nSECONDARY_EMAIL:"+SECONDARY_EMAIL+" SECONDARY_EMAIL_TYPE:"+SECONDARY_EMAIL_TYPE
				+"\nTERTIARY_EMAIL:"+TERTIARY_EMAIL+" TERTIARY_EMAIL_TYPE:"+TERTIARY_EMAIL_TYPE
				+"\nPOSTAL:"+POSTAL+" POSTAL_TYPE:"+POSTAL_TYPE
				+"\nIM_HANDLE:"+IM_HANDLE+" IM_PROTOCOL:"+IM_PROTOCOL);
		Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);	

		if (NAME != null) {
			intent.putExtra(ContactsContract.Intents.Insert.NAME, NAME);
		}


		if (PHONE != null) {
			intent.putExtra(ContactsContract.Intents.Insert.PHONE, PHONE);
		}        
		if (PHONE_TYPE != NO_PHONE_TYPE) {
			intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, PHONE_TYPE);
		}
		//		if (PHONE_ISPRIMARY) {
		//			intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, PHONE_TYPE);
		//		}		

		if (SECONDARY_PHONE != null) {
			intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, SECONDARY_PHONE);
		}        
		if (SECONDARY_PHONE_TYPE != NO_PHONE_TYPE) {
			intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE, SECONDARY_PHONE_TYPE);
		}

		if (TERTIARY_PHONE != null) {
			intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE, TERTIARY_PHONE);
		}        
		if (TERTIARY_PHONE_TYPE != NO_PHONE_TYPE) {
			intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE_TYPE, TERTIARY_PHONE_TYPE);
		}


		if (COMPANY != null) {
			intent.putExtra(ContactsContract.Intents.Insert.COMPANY, COMPANY);
		}


		if (JOB_TITLE != null) {
			intent.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, JOB_TITLE);
		}


		if (NOTES != null) {
			intent.putExtra(ContactsContract.Intents.Insert.NOTES, NOTES);
		}


		if (EMAIL != null) {
			intent.putExtra(ContactsContract.Intents.Insert.EMAIL, EMAIL);
		}
		if (EMAIL_TYPE != NO_PHONE_TYPE) {
			intent.putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, EMAIL_TYPE);
		}

		if (SECONDARY_EMAIL != null) {
			intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL, SECONDARY_EMAIL);
		}
		if (SECONDARY_EMAIL_TYPE != NO_PHONE_TYPE) {
			intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL_TYPE, SECONDARY_EMAIL_TYPE);
		}

		if (TERTIARY_EMAIL != null) {
			intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_EMAIL, TERTIARY_EMAIL);
		}
		if (TERTIARY_EMAIL_TYPE != NO_PHONE_TYPE) {
			intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_EMAIL_TYPE, TERTIARY_EMAIL_TYPE);
		}


		if (POSTAL != null) {
			intent.putExtra(ContactsContract.Intents.Insert.POSTAL, POSTAL);
		}
		if (POSTAL_TYPE != NO_PHONE_TYPE) {
			intent.putExtra(ContactsContract.Intents.Insert.POSTAL_TYPE, POSTAL_TYPE);
		}


		if (IM_HANDLE != null) {
			intent.putExtra(ContactsContract.Intents.Insert.IM_HANDLE, IM_HANDLE);
		}
		if (IM_PROTOCOL != NO_PHONE_TYPE) {
			intent.putExtra(ContactsContract.Intents.Insert.IM_PROTOCOL, IM_PROTOCOL);
		}
		Log.d(TAG,"intent:"+intent);
        intent.putExtra("fromMstBusiness", true);
		return intent;
	}

	@Override
	public void onEnd() {
		// do nothing
	}
	@Override
	public void onStart() {
		// do nothing
	}
}
