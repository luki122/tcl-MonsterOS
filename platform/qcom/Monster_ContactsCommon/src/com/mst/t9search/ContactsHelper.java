package com.mst.t9search;


//add by liyang


import mst.provider.ContactsContract.CommonDataKinds.Phone;
import mst.provider.ContactsContract.Groups;
import mst.provider.CallLog.Calls;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.android.contacts.common.R;
import com.android.contacts.common.mst.HanziToPinyin;
import com.android.contacts.common.mst.HanziToPinyin.Token;
import com.android.contacts.common.mst.HanziToPinyin;
import com.android.contacts.common.mst.HanziToPinyin.Token;
import android.R.integer;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Handler;
import android.provider.ContactsContract;
import mst.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.util.Log;
import com.mst.t9search.Contacts;
import com.mst.t9search.Contacts.SearchByType;
import com.t9search.util.*;
import com.t9search.model.*;

public class ContactsHelper {
	private static final String TAG="ContactsHelper";
	private Context mContext;
	private static ContactsHelper mInstance = null;
	private List<Contacts> mBaseContacts = null;	//The basic data used for the search
	private List<Contacts> mSearchContacts=null;	//The search results from the basic data
	/*save the first input string which search no result.
		mFirstNoSearchResultInput.size<=0, means that the first input string which search no result not appear.
		mFirstNoSearchResultInput.size>0, means that the first input string which search no result has appeared, 
		it's  mFirstNoSearchResultInput.toString(). 
		We can reduce the number of search basic data by the first input string which search no result.
	 */
	private StringBuffer  mFirstNoSearchResultInput=null;
	private AsyncTask<Object, Object, List<Contacts>> mLoadTask = null;
	private OnContactsLoad mOnContactsLoad = null;
	private OnContactsChanged mOnContactsChanged=null;
	private ContentObserver mContentObserver;
	private boolean mContactsChanged = true;
	private Handler mContactsHandler=new Handler();

	public void setmOnContactsChanged(OnContactsChanged mOnContactsChanged) {
		this.mOnContactsChanged = mOnContactsChanged;
	}

	public List<Contacts> getmBaseContacts() {
		return mBaseContacts;
	}

	public interface OnContactsLoad {
		void onContactsLoadSuccess();

		void onContactsLoadFailed();
	}

	public interface OnContactsChanged{
		void onContactsChanged();
	}

	public ContactsHelper() {
		initContactsHelper();		
	}

	public static ContactsHelper getInstance() {
		//		Log.d(TAG,"instance:"+mInstance);
		if (null == mInstance) {
			mInstance = new ContactsHelper();
		}

		return mInstance;
	}

	public void destroy(){
		Log.d(TAG,"destroy");
		if(/*null!=mInstance*/true){
			unregisterContentObserver();
			mInstance=null;//the system will free other memory. 
		}
	}

	public List<Contacts> getBaseContacts() {
		return mBaseContacts;
	}

	// public void setBaseContacts(List<Contacts> baseContacts) {
	// mBaseContacts = baseContacts;
	// }

	public List<Contacts> getSearchContacts() {
		return mSearchContacts;
	}

	//	public void setSearchContacts(List<Contacts> searchContacts) {
	//		mSearchContacts = searchContacts;
	//	}

	public OnContactsLoad getOnContactsLoad() {
		return mOnContactsLoad;
	}

	public void setOnContactsLoad(OnContactsLoad onContactsLoad) {
		mOnContactsLoad = onContactsLoad;
	}

	String allContactsString=null;
	String otherContactsString=null;
	public void setContext(Context context){
		mContext=context;
		String frequentString=mContext.getString(R.string.mst_commonly_contacts_header);
		String callLogString=mContext.getString(R.string.mst_call_logs);
		String usefulNumbersString=mContext.getString(R.string.mst_useful_number);
		String contactsListString=mContext.getString(R.string.contactsList);
		String groupString=mContext.getString(R.string.mst_group_header);
		allContactsString=mContext.getString(R.string.mst_all_contacts_header);
		otherContactsString=mContext.getString(R.string.mst_other_contacts_header);

		headerContacts0 = new Contacts(frequentString, null,0,0,null,0,null,-1,0);
		headerContacts1 = new Contacts(contactsListString, null,0,0,null,0,null,-1,1);
		headerContacts2 = new Contacts(callLogString, null,0,0,null,0,null,-1,2);
		headerContacts3 = new Contacts(usefulNumbersString, null,0,0,null,0,null,-1,3);
		headerContacts4 = new Contacts(groupString, null,0,0,null,0,null,-1,4);

		registerContentObserver();
	}


	private boolean isContactsChanged() {
		return mContactsChanged;
	}

	private void setContactsChanged(boolean contactsChanged) {
		mContactsChanged = contactsChanged;
	}

	/**
	 * Provides an function to start load contacts
	 * 
	 * @return start load success return true, otherwise return false
	 */
	private boolean isForDialer;
	private boolean isForDialpadT9;	

	public boolean isForDialpadT9() {
		return isForDialpadT9;
	}

	public void setForDialpadT9(boolean isForDialpadT9) {
		this.isForDialpadT9 = isForDialpadT9;
	}

	private boolean isLoadingContacts=false;
	private boolean isStartedLoadingContacts=false;

	public boolean isLoadingContacts() {
		return isLoadingContacts;
	}

	public void setLoadingContacts(boolean isLoadingContacts) {
		this.isLoadingContacts = isLoadingContacts;
	}

	public boolean isStartedLoadingContacts() {
		return isStartedLoadingContacts;
	}

	public boolean startLoadContactsForChoiceSearch(List<Contacts> contacts) {
		initContactsHelper();
		this.mBaseContacts=contacts;		
		return true;
	}
	/**
	 * Provides an function to start load contacts
	 * 
	 * @return start load success return true, otherwise return false
	 */
	public boolean startLoadContacts(boolean isForDialer) {
		Log.d(TAG,"startLoadContacts,"+isForDialer);
		this.isForDialer = isForDialer;
		if (true == isLoading()) {
			return false;
		}

		if(false==isContactsChanged()){
			return false;
		}

		isLoadingContacts=true;
		isStartedLoadingContacts=true;
		initContactsHelper();

		mLoadTask = new AsyncTask<Object, Object, List<Contacts>>() {

			@Override
			protected List<Contacts> doInBackground(Object... params) {
				return loadContacts(mContext);
			}

			@Override
			protected void onPostExecute(List<Contacts> result) {
				parseContacts(result);
				super.onPostExecute(result);
				setContactsChanged(false);
				mLoadTask = null;

				isLoadingContacts=false;
				if(null!=mOnContactsChanged){
					Log.i(TAG,"mOnContactsChanged mContactsChanged="+mContactsChanged);
					mOnContactsChanged.onContactsChanged();
				}
			}
		}.execute();

		return true;
	}



	private static final String SEARCH_BEGIN_STRING = "mst_querystring_for_contact_search_begin";
	private String originKey=null;
	public void query(String searchKey){
		Log.d(TAG,"query:"+searchKey);
		originKey=searchKey;
		if(isForDialer) {
			if(isForDialpadT9) t9Search(searchKey);//拨号盘
			else t9SearchByPinyin(PinyinUtil.getFullSpell(searchKey));		//通话记录首页搜索
		}		
		else {
			if(TextUtils.equals(SEARCH_BEGIN_STRING, searchKey)) t9SearchShowFrequentContacts();
			else {
				t9SearchContacts(PinyinUtil.getFullSpell(searchKey));//联系人搜索
			}
		}
	}

	public void queryForChoiceSearch(String searchKey){
		Log.d(TAG,"query:"+searchKey);
		originKey=searchKey;
		t9SearchByPinyin(PinyinUtil.getFullSpell(searchKey));
	}


	/**
	 * @description search base data according to string parameter
	 * @param keyword (valid characters include:'0'~'9','*','#')
	 * @return void
	 *
	 * 
	 */
	public void t9Search(String keyword){
		if(null==keyword){//add all base data to search
			if(null!=mSearchContacts){
				mSearchContacts.clear();
			}else{
				mSearchContacts=new ArrayList<Contacts>();
			}

			for(Contacts contacts:mBaseContacts){
				contacts.setSearchByType(SearchByType.SearchByNull);
				contacts.clearMatchKeywords();
			}

			//			mSearchContacts.addAll(mBaseContacts);
			mFirstNoSearchResultInput.delete(0, mFirstNoSearchResultInput.length());
			Log.i(TAG,"null==search,mFirstNoSearchResultInput.length()="+mFirstNoSearchResultInput.length());
			return;
		}

		if(mFirstNoSearchResultInput.length()>0){
			if(keyword.contains(mFirstNoSearchResultInput.toString())){
				Log.i(TAG,"no need  to search,null!=search,mFirstNoSearchResultInput.length()="+mFirstNoSearchResultInput.length()+"["+mFirstNoSearchResultInput.toString()+"]"+";searchlen="+keyword.length()+"["+keyword+"]");
				return;
			}else{
				Log.i(TAG,"delete  mFirstNoSearchResultInput, null!=search,mFirstNoSearchResultInput.length()="+mFirstNoSearchResultInput.length()+"["+mFirstNoSearchResultInput.toString()+"]"+";searchlen="+keyword.length()+"["+keyword+"]");
				mFirstNoSearchResultInput.delete(0, mFirstNoSearchResultInput.length());
			}
		}

		if(null!=mSearchContacts){
			mSearchContacts.clear();	
		}else{
			mSearchContacts=new ArrayList<Contacts>();
		}

		int contactsCount=mBaseContacts.size();

		/**
		 * search process:
		 * 1:Search by name
		 *  (1)Search by name pinyin characters(org name->name pinyin characters)	('0'~'9','*','#')
		 *  (2)Search by org name		('0'~'9','*','#')
		 * 2:Search by phone number		('0'~'9','*','#')
		 */
		long preContactType=-1;
		long preContactId=-1;
		long preContactId1=-1;

		for(int i=0; i<contactsCount; i++){
			PinyinSearchUnit pinyinSearchUnit=mBaseContacts.get(i).getNamePinyinSearchUnits();
			long contactType=mBaseContacts.get(i).getContactType();
			long contactId=mBaseContacts.get(i).getContactId();

			if(true==T9Util.match(pinyinSearchUnit, keyword)){//search by name;
				//				if(preContactId==-1) {
				//					Log.d(TAG,"preContactId==-1");
				//					if(contactId>0) {mSearchContacts.add(headerContacts1);Log.d(TAG,"0:"+i);}
				//					else if(contactId==0) {mSearchContacts.add(headerContacts2);Log.d(TAG,"1:"+i);}
				//					else {mSearchContacts.add(headerContacts3);Log.d(TAG,"2:"+i);}
				//				}else{
				//					if(preContactId>0&&contactId==0) {mSearchContacts.add(headerContacts2);Log.d(TAG,"3:"+i);}
				//					else if(preContactId>=0&&contactId<-9999) {mSearchContacts.add(headerContacts3);Log.d(TAG,"4:"+i);}
				//				}
				//				preContactId=contactId;

				if(preContactType!=contactType) {
					if(contactType==0) mSearchContacts.add(headerContacts0);
					else if(contactType==1) mSearchContacts.add(headerContacts1);
					else if(contactType==2) mSearchContacts.add(headerContacts2);
					else if(contactType==3) mSearchContacts.add(headerContacts3);
					else if(contactType==4) mSearchContacts.add(headerContacts4);
				}
				preContactType=contactType;
				mBaseContacts.get(i).setSearchByType(SearchByType.SearchByName);
				mBaseContacts.get(i).setMatchKeywords(pinyinSearchUnit.getMatchKeyword().toString());
				mBaseContacts.get(i).setMatchStartIndex(mBaseContacts.get(i).getName().indexOf(pinyinSearchUnit.getMatchKeyword().toString()));
				mBaseContacts.get(i).setMatchLength(mBaseContacts.get(i).getMatchKeywords().length());
				if(contactType==2) mSearchContacts.add(mBaseContacts.get(i));
				else if(preContactId!=contactId){
					mSearchContacts.add(mBaseContacts.get(i));
				}
				preContactId=contactId;

				continue;
			}else{
				if(mBaseContacts.get(i).getPhoneNumber()!=null&&mBaseContacts.get(i).getPhoneNumber().contains(keyword)){	//search by phone number
					if(preContactType!=contactType) {
						if(contactType==0) mSearchContacts.add(headerContacts0);
						else if(contactType==1) mSearchContacts.add(headerContacts1);
						else if(contactType==2) mSearchContacts.add(headerContacts2);
						else if(contactType==3) mSearchContacts.add(headerContacts3);
						else if(contactType==4) mSearchContacts.add(headerContacts4);
					}
					preContactType=contactType;

					mBaseContacts.get(i).setSearchByType(SearchByType.SearchByPhoneNumber);
					mBaseContacts.get(i).setMatchKeywords(keyword);
					mBaseContacts.get(i).setMatchStartIndex(mBaseContacts.get(i).getPhoneNumber().indexOf(keyword));
					mBaseContacts.get(i).setMatchLength(keyword.length());
					if(contactType!=1){
						mSearchContacts.add(mBaseContacts.get(i));
					}else{
						if(preContactId1!=contactId){
							mSearchContacts.add(mBaseContacts.get(i));
						}
					}
					preContactId1=contactId;	

					//Log.i(TAG, "["+mBaseContacts.get(i).getPhoneNumber()+"]"+"["+mBaseContacts.get(i).getMatchKeywords().toString()+"]"+"["+mBaseContacts.get(i).getMatchStartIndex()+"]"+"["+mBaseContacts.get(i).getMatchLength()+"]");
					continue;
				}		
			}
		}

		if(mSearchContacts.size()<=0){
			if(mFirstNoSearchResultInput.length()<=0){
				mFirstNoSearchResultInput.append(keyword);
				Log.i(TAG,"no search result,null!=search,mFirstNoSearchResultInput.length()="+mFirstNoSearchResultInput.length()+"["+mFirstNoSearchResultInput.toString()+"]"+";searchlen="+keyword.length()+"["+keyword+"]");
			}else{

			}
		}else{
			Collections.sort(mSearchContacts, Contacts.mSearchComparator);
		}

	}

	/**
	 * @description search base data according to string parameter
	 * @param keyword (valid characters include:'0'~'9','*','#')
	 * @return void
	 *
	 * 
	 */
	public void t9SearchByPinyin(String keyword){
		if(null==keyword){//add all base data to search
			if(null!=mSearchContacts){
				mSearchContacts.clear();
			}else{
				mSearchContacts=new ArrayList<Contacts>();
			}

			for(Contacts contacts:mBaseContacts){
				contacts.setSearchByType(SearchByType.SearchByNull);
				contacts.clearMatchKeywords();
			}

			//						mSearchContacts.addAll(mBaseContacts);
			mFirstNoSearchResultInput.delete(0, mFirstNoSearchResultInput.length());
			Log.i(TAG,"null==search,mFirstNoSearchResultInput.length()="+mFirstNoSearchResultInput.length());
			return;
		}

		if(mFirstNoSearchResultInput.length()>0){
			if(keyword.contains(mFirstNoSearchResultInput.toString())){
				Log.i(TAG,"no need  to search,null!=search,mFirstNoSearchResultInput.length()="+mFirstNoSearchResultInput.length()+"["+mFirstNoSearchResultInput.toString()+"]"+";searchlen="+keyword.length()+"["+keyword+"]");
				return;
			}else{
				Log.i(TAG,"delete  mFirstNoSearchResultInput, null!=search,mFirstNoSearchResultInput.length()="+mFirstNoSearchResultInput.length()+"["+mFirstNoSearchResultInput.toString()+"]"+";searchlen="+keyword.length()+"["+keyword+"]");
				mFirstNoSearchResultInput.delete(0, mFirstNoSearchResultInput.length());
			}
		}

		if(null!=mSearchContacts){
			mSearchContacts.clear();	
		}else{
			mSearchContacts=new ArrayList<Contacts>();
		}

		int contactsCount=mBaseContacts.size();

		/**
		 * search process:
		 * 1:Search by name
		 *  (1)Search by name pinyin characters(org name->name pinyin characters)	('0'~'9','*','#')
		 *  (2)Search by org name		('0'~'9','*','#')
		 * 2:Search by phone number		('0'~'9','*','#')
		 */
		long preContactType=-1;
		long preContactId=-1;
		long preContactId1=-1;

		for(int i=0; i<contactsCount; i++){
			PinyinSearchUnit pinyinSearchUnit=mBaseContacts.get(i).getNamePinyinSearchUnits();
			long contactType=mBaseContacts.get(i).getContactType();
			long contactId=mBaseContacts.get(i).getContactId();

			if(true==T9Util.matchByPinyin(pinyinSearchUnit, keyword)){//search by name;
				if(preContactType!=contactType) {
					if(contactType==0) mSearchContacts.add(headerContacts0);
					else if(contactType==1) mSearchContacts.add(headerContacts1);
					else if(contactType==2) mSearchContacts.add(headerContacts2);
					else if(contactType==3) mSearchContacts.add(headerContacts3);
					else if(contactType==4) mSearchContacts.add(headerContacts4);
				}
				preContactType=contactType;

				mBaseContacts.get(i).setSearchByType(SearchByType.SearchByName);
				mBaseContacts.get(i).setMatchKeywords(pinyinSearchUnit.getMatchKeyword().toString());
				mBaseContacts.get(i).setMatchStartIndex(mBaseContacts.get(i).getName().indexOf(pinyinSearchUnit.getMatchKeyword().toString()));
				mBaseContacts.get(i).setMatchLength(mBaseContacts.get(i).getMatchKeywords().length());
				if(contactType==2) mSearchContacts.add(mBaseContacts.get(i));
				else if(preContactId!=contactId){
					mSearchContacts.add(mBaseContacts.get(i));
				}
				preContactId1=contactId;	
				continue;
			}else{
				if(mBaseContacts.get(i).getPhoneNumber() !=null &&mBaseContacts.get(i).getPhoneNumber().contains(originKey)){	//search by phone number
					if(preContactType!=contactType) {
						if(contactType==0) mSearchContacts.add(headerContacts0);
						else if(contactType==1) mSearchContacts.add(headerContacts1);
						else if(contactType==2) mSearchContacts.add(headerContacts2);
						else if(contactType==3) mSearchContacts.add(headerContacts3);
						else if(contactType==4) mSearchContacts.add(headerContacts4);
					}
					preContactType=contactType;

					mBaseContacts.get(i).setSearchByType(SearchByType.SearchByPhoneNumber);
					mBaseContacts.get(i).setMatchKeywords(originKey);
					mBaseContacts.get(i).setMatchStartIndex(mBaseContacts.get(i).getPhoneNumber().indexOf(originKey));
					mBaseContacts.get(i).setMatchLength(originKey.length());
					if(contactType!=1){
						mSearchContacts.add(mBaseContacts.get(i));
					}else{
						if(preContactId1!=contactId){
							mSearchContacts.add(mBaseContacts.get(i));
						}
					}
					preContactId1=contactId;	

					//Log.i(TAG, "["+mBaseContacts.get(i).getPhoneNumber()+"]"+"["+mBaseContacts.get(i).getMatchKeywords().toString()+"]"+"["+mBaseContacts.get(i).getMatchStartIndex()+"]"+"["+mBaseContacts.get(i).getMatchLength()+"]");
					continue;
				}		
			}
		}

		if(mSearchContacts.size()<=0){
			if(mFirstNoSearchResultInput.length()<=0){
				mFirstNoSearchResultInput.append(keyword);
				Log.i(TAG,"no search result,null!=search,mFirstNoSearchResultInput.length()="+mFirstNoSearchResultInput.length()+"["+mFirstNoSearchResultInput.toString()+"]"+";searchlen="+keyword.length()+"["+keyword+"]");
			}else{

			}
		}else{
			Collections.sort(mSearchContacts, Contacts.mSearchComparator);
		}

	}

	public void t9SearchShowFrequentContacts(){
		if(null!=mSearchContacts){
			mSearchContacts.clear();	
		}else{
			mSearchContacts=new ArrayList<Contacts>();
		}

		boolean isAdded=false;
		int contactsCount=mBaseContacts.size();
		long preContactId=-1;		
		for(int i=0; i<contactsCount; i++){
			long contactType=mBaseContacts.get(i).getContactType();			
			if(contactType!=TYPE_FREQUENT) break;
			if(!isAdded){
				mSearchContacts.add(headerContacts0);
				isAdded=true;
			}

			long contactId=mBaseContacts.get(i).getContactId();

			if(preContactId!=contactId){
				mBaseContacts.get(i).setSearchByType(SearchByType.SearchByName);
				mBaseContacts.get(i).clearMatchKeywords();
				mSearchContacts.add(mBaseContacts.get(i));
			}
			preContactId=contactId;
		}
		Log.d(TAG,"mSearchContacts1:"+(mSearchContacts==null?"null":mSearchContacts.size()));
	}
	/**
	 * @description search base data according to string parameter
	 * @param keyword (valid characters include:'0'~'9','*','#')
	 * @return void
	 *
	 * 
	 */
	public void t9SearchContacts(String keyword){
		if(null==keyword){//add all base data to search
			if(null!=mSearchContacts){
				mSearchContacts.clear();
			}else{
				mSearchContacts=new ArrayList<Contacts>();
			}

			for(Contacts contacts:mBaseContacts){
				contacts.setSearchByType(SearchByType.SearchByNull);
				contacts.clearMatchKeywords();
			}

			//						mSearchContacts.addAll(mBaseContacts);
			mFirstNoSearchResultInput.delete(0, mFirstNoSearchResultInput.length());
			Log.i(TAG,"null==search,mFirstNoSearchResultInput.length()="+mFirstNoSearchResultInput.length());
			return;
		}

		if(mFirstNoSearchResultInput.length()>0){
			if(keyword.contains(mFirstNoSearchResultInput.toString())){
				Log.i(TAG,"no need  to search,null!=search,mFirstNoSearchResultInput.length()="+mFirstNoSearchResultInput.length()+"["+mFirstNoSearchResultInput.toString()+"]"+";searchlen="+keyword.length()+"["+keyword+"]");
				return;
			}else{
				Log.i(TAG,"delete  mFirstNoSearchResultInput, null!=search,mFirstNoSearchResultInput.length()="+mFirstNoSearchResultInput.length()+"["+mFirstNoSearchResultInput.toString()+"]"+";searchlen="+keyword.length()+"["+keyword+"]");
				mFirstNoSearchResultInput.delete(0, mFirstNoSearchResultInput.length());
			}
		}

		if(null!=mSearchContacts){
			mSearchContacts.clear();	
		}else{
			mSearchContacts=new ArrayList<Contacts>();
		}

		int contactsCount=mBaseContacts.size();

		/**
		 * search process:
		 * 1:Search by name
		 *  (1)Search by name pinyin characters(org name->name pinyin characters)	('0'~'9','*','#')
		 *  (2)Search by org name		('0'~'9','*','#')
		 * 2:Search by phone number		('0'~'9','*','#')
		 */
		long preContactType=-1;
		long preContactId=-1;
		long preContactId1=-1;

		boolean hasFrequent=false;
		for(int i=0; i<contactsCount; i++){
			PinyinSearchUnit pinyinSearchUnit=mBaseContacts.get(i).getNamePinyinSearchUnits();
			long contactType=mBaseContacts.get(i).getContactType();
			long contactId=mBaseContacts.get(i).getContactId();

			if(true==T9Util.matchByPinyin(pinyinSearchUnit, keyword)){//search by name;
				if(preContactType!=contactType) {
					if(contactType==0) {
						mSearchContacts.add(headerContacts0);
						hasFrequent=true;
					}
					else if(contactType==1) {
						mSearchContacts.add(headerContacts1);
						if(hasFrequent) headerContacts1.setName(otherContactsString);
						else headerContacts1.setName(allContactsString);
					}
					else if(contactType==4) mSearchContacts.add(headerContacts4);
				}
				preContactType=contactType;	

				mBaseContacts.get(i).setSearchByType(SearchByType.SearchByName);
				mBaseContacts.get(i).setMatchKeywords(pinyinSearchUnit.getMatchKeyword().toString());
				mBaseContacts.get(i).setMatchStartIndex(mBaseContacts.get(i).getName().indexOf(pinyinSearchUnit.getMatchKeyword().toString()));
				mBaseContacts.get(i).setMatchLength(mBaseContacts.get(i).getMatchKeywords().length());
				//				Log.d(TAG,"preContact:"+preContactId+" contactId:"+contactId);
				if(preContactId!=contactId){
					mSearchContacts.add(mBaseContacts.get(i));
				}
				preContactId=contactId;

				continue;
			}else{
				//				Log.d(TAG,"PhoneNumber():"+mBaseContacts.get(i).getPhoneNumber()+" key:"+originKey);
				if(mBaseContacts.get(i).getPhoneNumber() != null && mBaseContacts.get(i).getPhoneNumber().contains(originKey)){	//search by phone number
					if(preContactType!=contactType) {
						if(contactType==0) {
							mSearchContacts.add(headerContacts0);
							hasFrequent=true;
						}
						else if(contactType==1) {
							mSearchContacts.add(headerContacts1);
							if(hasFrequent) headerContacts1.setName(otherContactsString);
							else headerContacts1.setName(allContactsString);
						}
						else if(contactType==4) mSearchContacts.add(headerContacts4);
					}
					preContactType=contactType;

					mBaseContacts.get(i).setSearchByType(SearchByType.SearchByPhoneNumber);
					mBaseContacts.get(i).setMatchKeywords(originKey);
					mBaseContacts.get(i).setMatchStartIndex(mBaseContacts.get(i).getPhoneNumber().indexOf(originKey));
					mBaseContacts.get(i).setMatchLength(originKey.length());

					if(preContactId1!=contactId){
						mSearchContacts.add(mBaseContacts.get(i));
					}
					preContactId1=contactId;	

					//Log.i(TAG, "["+mBaseContacts.get(i).getPhoneNumber()+"]"+"["+mBaseContacts.get(i).getMatchKeywords().toString()+"]"+"["+mBaseContacts.get(i).getMatchStartIndex()+"]"+"["+mBaseContacts.get(i).getMatchLength()+"]");
					continue;
				}		
			}
		}

		if(mSearchContacts.size()<=0){
			if(mFirstNoSearchResultInput.length()<=0){
				mFirstNoSearchResultInput.append(keyword);
				Log.i(TAG,"no search result,null!=search,mFirstNoSearchResultInput.length()="+mFirstNoSearchResultInput.length()+"["+mFirstNoSearchResultInput.toString()+"]"+";searchlen="+keyword.length()+"["+keyword+"]");
			}else{

			}
		}else{
			Collections.sort(mSearchContacts, Contacts.mSearchComparator);
		}

		//		for(Contacts contact:mSearchContacts){
		//			Log.d(TAG,"contact:"+contact.getName()+" ;"+contact.getPhoneNumber());
		//		}
	}


	private void initContactsHelper(){
		//		mContext=T9SearchApplication.getContextObject();
		setContactsChanged(true);
		if (null == mBaseContacts) {
			mBaseContacts = new ArrayList<Contacts>();
		} else {
			mBaseContacts.clear();
		}

		if(null==mSearchContacts){
			mSearchContacts=new ArrayList<Contacts>();
		}else{
			mSearchContacts.clear();
		}

		if(null==mFirstNoSearchResultInput){
			mFirstNoSearchResultInput=new StringBuffer();
		}else{
			mFirstNoSearchResultInput.delete(0, mFirstNoSearchResultInput.length());
		}
	}
	private Contacts headerContacts0;
	private Contacts headerContacts1;
	private Contacts headerContacts2;
	private Contacts headerContacts3;
	private Contacts headerContacts4;

	private Runnable mRunnable=new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			startLoadContacts(isForDialer);
		}
	};
	private void registerContentObserver(){
		Log.d(TAG,"registerContentObserver");
		if(null==mContentObserver){
			mContentObserver=new ContentObserver(mContactsHandler) {

				@Override
				public void onChange(boolean selfChange) {
					Log.d(TAG,"Phone.CONTENT_URI onChange");
					// TODO Auto-generated method stub
					setContactsChanged(true);

					mContactsHandler.removeCallbacks(mRunnable);
					//modify by lgy for 3379830
					mContactsHandler.postDelayed(mRunnable, 300);			

					super.onChange(selfChange);
				}

			};
		}

		if(null!=mContext){
			Log.d(TAG,"registerContentObserver1");
			mContext.getContentResolver().registerContentObserver(Phone.CONTENT_URI, true,
					mContentObserver);
			if(/*isForDialer*/true){
				mContext.getContentResolver().registerContentObserver(Calls.CONTENT_URI, true,
						mContentObserver);
			}
			mContext.getContentResolver().registerContentObserver(Groups.CONTENT_URI, true,
					mContentObserver);
			mContext.getContentResolver().registerContentObserver(Groups.CONTENT_URI, true,
					mContentObserver);
		}
	}

	private void unregisterContentObserver(){
		if(null!=mContentObserver){
			if(null!=mContext){
				try{
					mContext.getContentResolver().unregisterContentObserver(mContentObserver);
				}catch(Exception e){

				}
			}
		}
	}

	private boolean isLoading() {
		return (mLoadTask != null && mLoadTask.getStatus() == Status.RUNNING);
	}

	public static final int TYPE_FREQUENT=0;
	public static final int TYPE_CONTACTS=1;
	public static final int TYPE_CALLLOG=2;
	public static final int TYPE_USEFULNUMBER=3;
	public static final int TYPE_GROUP=4;


	private List<Contacts> loadContacts(Context context) {
		isSearchContactsLoaded=false;
		Log.d(TAG," spend loadContacts begin ");
		long time=System.currentTimeMillis();
		List<Contacts> contacts = new ArrayList<Contacts>();
		Contacts cs = null;
		Cursor  frequentCursor=null;
		Cursor  frequentCursorData=null;
		Cursor cursor = null;
		Cursor callLogCursor = null;
		Cursor usefulCursor=null;
		Cursor groupCursor=null;
		String sortKey="contact_id ASC,raw_contact_id ASC";
		StringBuilder frequentStringBuilder=null;
		Uri uri=isForDialer?Phone.CONTENT_URI:Data.CONTENT_URI;
		String mimeTypeSelection="(mimetype='vnd.android.cursor.item/phone_v2' " +
				"or mimetype='vnd.android.cursor.item/email_v2' " +
				"or mimetype='vnd.android.cursor.item/postal-address_v2' " +
				"or mimetype='vnd.android.cursor.item/note')";
		try {					
			if(!isForDialer){//查询5个最常用联系人
				frequentCursor = context.getContentResolver().query(
						mst.provider.ContactsContract.Contacts.CONTENT_URI, 
						null,
						"times_contacted>0", 
						null, 
						"times_contacted desc limit 5");
				int frequentCount=frequentCursor==null?0:frequentCursor.getCount();
				Log.d(TAG,"frequentCount:"+frequentCount);

				if(frequentCount>0){
					StringBuilder sb=new StringBuilder("contact_id IN(");
					frequentStringBuilder=new StringBuilder("contact_id NOT IN(");
					String temp=null;
					while (frequentCursor.moveToNext()) {
						temp=frequentCursor.getLong(frequentCursor.getColumnIndex("_id"))+",";
						sb.append(temp);
						frequentStringBuilder.append(temp);
					}
					sb.setLength(sb.length()-1);
					sb.append(")");
					Log.d(TAG,"sb:"+sb);

					frequentStringBuilder.setLength(frequentStringBuilder.length()-1);
					frequentStringBuilder.append(")");
					Log.d(TAG,"frequentStringBuilder:"+frequentStringBuilder);

					frequentCursorData = context.getContentResolver().query(
							uri, 
							null,
							isForDialer?sb.toString():sb.toString()+" and "+mimeTypeSelection,
									null, 
									//modify by lgy for 3445598
									//							sortKey);
							"times_contacted desc");
					Log.d(TAG,"frequentDataCount:"+(frequentCursorData==null?"null":frequentCursorData.getCount()));
					while (frequentCursorData.moveToNext()) {
						String displayName = frequentCursorData.getString(frequentCursorData.getColumnIndex(Phone.DISPLAY_NAME));
						String phoneNumber = frequentCursorData.getString(frequentCursorData.getColumnIndex(Phone.NUMBER));
						int callType= 0;
						int simIcon=0;
						String location=null;
						long date=0;
						String lookup=frequentCursorData.getString(frequentCursorData.getColumnIndex(Phone.LOOKUP_KEY));
						long contactId= frequentCursorData.getLong(frequentCursorData.getColumnIndex(Phone.CONTACT_ID));
						//						Log.d(TAG,"Phone.CONTACT_ID:"+Phone.CONTACT_ID);

						cs = new Contacts(displayName, phoneNumber,callType,simIcon,location,date,lookup,contactId,TYPE_FREQUENT);
						PinyinUtil.parse(cs.getNamePinyinSearchUnits());

						contacts.add(cs);
					}
				}
			}

			//查询所有联系人
			String contactSelection=null;
			if(frequentStringBuilder==null){
				if(isForDialer) contactSelection=null;
				else contactSelection=mimeTypeSelection;
			}else{
				if(isForDialer) contactSelection=frequentStringBuilder.toString();
				else contactSelection=frequentStringBuilder.toString()+" and "+mimeTypeSelection;
			}
			Log.d(TAG,"contactSelection:"+contactSelection);
			cursor = context.getContentResolver().query(
					uri, 
					null,
					contactSelection,
					null, 
					sortKey);

			int contactsCount=cursor==null?0:cursor.getCount();
			Log.d(TAG,"contactsCount:"+contactsCount);
			while (cursor.moveToNext()) {
				String displayName = cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME));
				String phoneNumber = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
				int callType= 0;
				int simIcon=0;
				String location=null;
				long date=0;
				String lookup=cursor.getString(cursor.getColumnIndex(Phone.LOOKUP_KEY));
				long contactId= cursor.getLong(cursor.getColumnIndex(Phone.CONTACT_ID));

				cs = new Contacts(displayName, phoneNumber,callType,simIcon,location,date,lookup,contactId,TYPE_CONTACTS);
				PinyinUtil.parse(cs.getNamePinyinSearchUnits());

				contacts.add(cs);
			}

			if(isForDialer){
				//查询通话记录（只查非联系人的通话记录）			
				callLogCursor = context.getContentResolver().query(
						Uri.parse("content://call_log/calls/call_distinct_search/"),
						null, 
						null,
						null, 
						null);
				int callLogCount=callLogCursor==null?0:callLogCursor.getCount();
				Log.d(TAG,"callLogCount:"+callLogCount);
				while (callLogCursor.moveToNext()) {
					String displayName = callLogCursor
							.getString(CallLogQuery.NUMBER);
					String phoneNumber = callLogCursor
							.getString(CallLogQuery.NUMBER);

					int callType= callLogCursor.getInt(CallLogQuery.CALL_TYPE);
					int simIcon=callLogCursor.getInt(CallLogQuery.ACCOUNT_ID);
					String location=callLogCursor.getString(CallLogQuery.GEOCODED_LOCATION);
					long date=callLogCursor.getLong(CallLogQuery.DATE);
					String lookup=null;
					long contactId= 0;

					cs = new Contacts(displayName, phoneNumber,callType,simIcon,location,date,lookup,contactId,TYPE_CALLLOG);
					PinyinUtil.parse(cs.getNamePinyinSearchUnits());				
					contacts.add(cs);
				}

				//查询常用号码
				usefulCursor = context.getContentResolver().query(
						Uri.parse("content://call_log/calls/useful_number/"),
						null, 
						null,
						null, 
						null);
				int usefulNumberCount=usefulCursor==null?0:usefulCursor.getCount();
				Log.d(TAG,"usefulNumberCount:"+usefulNumberCount);

				while (usefulCursor.moveToNext()) {
					String displayName = usefulCursor
							.getString(2);
					String phoneNumber = usefulCursor
							.getString(3);
					int callType= 0;
					int simIcon=0;
					String location=null;
					long date=0;
					String lookup=null;
					long contactId= usefulCursor.getLong(1);

					cs = new Contacts(displayName, phoneNumber,callType,simIcon,location,date,lookup,contactId,TYPE_USEFULNUMBER);
					PinyinUtil.parse(cs.getNamePinyinSearchUnits());

					contacts.add(cs);
				}
			}else{
				//查询群组信息
				groupCursor = context.getContentResolver().query(
						Groups.CONTENT_URI,
						null, 
						"deleted=0",
						null, 
						null);
				int groupCount=groupCursor==null?0:groupCursor.getCount();
				Log.d(TAG,"groupCount:"+groupCount);

				while (groupCursor.moveToNext()) {
					String displayName = groupCursor.getString(groupCursor.getColumnIndex("title"));
					String phoneNumber = "";
					int callType= 0;
					int simIcon=0;
					String location=null;
					long date=0;
					String lookup=null;
					long contactId= groupCursor.getLong(groupCursor.getColumnIndex("_id"));
					//					Log.d(TAG,"displayName:"+displayName+" group contactId:"+contactId);

					cs = new Contacts(displayName, phoneNumber,callType,simIcon,location,date,lookup,contactId,TYPE_GROUP);
					PinyinUtil.parse(cs.getNamePinyinSearchUnits());

					contacts.add(cs);
				}
			}

		} catch (Exception e) {
			Log.d(TAG,"e:"+e);
		} finally {
			if(frequentCursor!=null){
				frequentCursor.close();
				frequentCursor=null;
			}
			if(cursor!=null){
				cursor.close();
				cursor = null;
			}
			if(callLogCursor!=null){
				callLogCursor.close();
				callLogCursor=null;
			}
			if(usefulCursor!=null){
				usefulCursor.close();
				usefulCursor=null;
			}
			if(groupCursor!=null){
				groupCursor.close();
				groupCursor=null;
			}
			if(frequentCursorData!=null){
				frequentCursorData.close();
				frequentCursorData=null;
			}
		}

		Log.d(TAG,"loadContacts end,spend:"+(System.currentTimeMillis()-time));
		return contacts;
	}

	public static boolean isSearchContactsLoaded=false;
	private void parseContacts(List<Contacts> contacts) {
		if (null == contacts || contacts.size() < 1) {
			if (null != mOnContactsLoad) {
				mOnContactsLoad.onContactsLoadFailed();
			}
			return;
		}

		for (Contacts contact : contacts) {
			if (!mBaseContacts.contains(contact)) {
				mBaseContacts.add(contact);
			}
		}

		if (null != mOnContactsLoad) {
			t9Search(null);
			isSearchContactsLoaded=true;
			mOnContactsLoad.onContactsLoadSuccess();
		}

		return;
	}

	public static final class CallLogQuery {
		public static final String[] _PROJECTION = new String[] {
			Calls._ID,                          // 0
			Calls.NUMBER,                       // 1
			Calls.DATE,                         // 2
			Calls.DURATION,                     // 3
			Calls.TYPE,                         // 4
			Calls.COUNTRY_ISO,                  // 5
			Calls.VOICEMAIL_URI,                // 6
			Calls.GEOCODED_LOCATION,            // 7
			Calls.CACHED_NAME,                  // 8
			Calls.CACHED_NUMBER_TYPE,           // 9
			Calls.CACHED_NUMBER_LABEL,          // 10
			Calls.CACHED_LOOKUP_URI,            // 11
			Calls.CACHED_MATCHED_NUMBER,        // 12
			Calls.CACHED_NORMALIZED_NUMBER,     // 13
			Calls.CACHED_PHOTO_ID,              // 14
			Calls.CACHED_FORMATTED_NUMBER,      // 15
			Calls.IS_READ,                      // 16
			Calls.NUMBER_PRESENTATION,          // 17
			Calls.PHONE_ACCOUNT_COMPONENT_NAME, // 18
			Calls.PHONE_ACCOUNT_ID,             // 19
			Calls.FEATURES,                     // 20
			Calls.DATA_USAGE,                   // 21
			Calls.TRANSCRIPTION,                // 22
			Calls.CACHED_PHOTO_URI              // 23
		};

		public static final int ID = 0;
		public static final int NUMBER = 1;
		public static final int DATE = 2;
		public static final int DURATION = 3;
		public static final int CALL_TYPE = 4;
		public static final int COUNTRY_ISO = 5;
		public static final int VOICEMAIL_URI = 6;
		public static final int GEOCODED_LOCATION = 7;
		public static final int CACHED_NAME = 8;
		public static final int CACHED_NUMBER_TYPE = 9;
		public static final int CACHED_NUMBER_LABEL = 10;
		public static final int CACHED_LOOKUP_URI = 11;
		public static final int CACHED_MATCHED_NUMBER = 12;
		public static final int CACHED_NORMALIZED_NUMBER = 13;
		public static final int CACHED_PHOTO_ID = 14;
		public static final int CACHED_FORMATTED_NUMBER = 15;
		public static final int IS_READ = 16;
		public static final int NUMBER_PRESENTATION = 17;
		public static final int ACCOUNT_COMPONENT_NAME = 18;
		public static final int ACCOUNT_ID = 19;
		public static final int FEATURES = 20;
		public static final int DATA_USAGE = 21;
		public static final int TRANSCRIPTION = 22;
		public static final int CACHED_PHOTO_URI = 23;
	}
}
