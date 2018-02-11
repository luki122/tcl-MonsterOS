package com.mst.dialersearch;

import com.mst.tms.UsefulNumberResult;
import com.mst.tms.TmsServiceManager;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

import com.android.providers.contacts.HanziToPinyin;
import com.android.providers.contacts.ContactsDatabaseHelper.Tables;
import com.android.providers.contacts.HanziToPinyin.Token;
import com.mediatek.providers.contacts.CallLogProviderEx;

import net.sourceforge.pinyin4j.ChineseToPinyinHelper;

//import com.android.providers.contacts.ContactsDatabaseHelper.AccountsColumns;
//import com.android.providers.contacts.ContactsDatabaseHelper.DataColumns;
//import com.android.providers.contacts.ContactsDatabaseHelper.RawContactsColumns;
//import com.android.providers.contacts.ContactsDatabaseHelper.Tables;
//import com.android.providers.contacts.ContactsDatabaseHelper.Views;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.CallLog.Calls;
import com.android.providers.mst.ContactsContract.CommonDataKinds.GroupMembership;
import com.android.providers.mst.ContactsContract.CommonDataKinds.Phone;
import com.android.providers.mst.ContactsContract.Contacts;
import com.android.providers.mst.ContactsContract.Data;
import com.android.providers.mst.ContactsContract.RawContacts;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;

public class DialerSearchHelperForMst {
	private static final String TAG = "DialerSearchHelperForMstInProvider";
	private static final int MAX_NUMBER_INDEX_START = 16;
	public static final String MST_DIALER_SEARCH_TABLE = "mst_dialer_search";
	public static final String MST_DIALER_SEARCH_VIEW = "view_mst_dialer_search";

	//	private static final String TEMP_DIALER_SEARCH_VIEW_TABLE = "mst_dialer_search_temp";	 

	private static final String TEMP_DIALER_SEARCH_VIEW_TABLE = "view_mst_dialer_search";	//modify by liyang ??
	private int groupCount=0;
	public interface AccountsColumns extends BaseColumns {
		String CONCRETE_ID = Tables.ACCOUNTS + "." + BaseColumns._ID;

		String ACCOUNT_NAME = RawContacts.ACCOUNT_NAME;
		String ACCOUNT_TYPE = RawContacts.ACCOUNT_TYPE;
		String DATA_SET = RawContacts.DATA_SET;

		String CONCRETE_ACCOUNT_NAME = Tables.ACCOUNTS + "." + ACCOUNT_NAME;
		String CONCRETE_ACCOUNT_TYPE = Tables.ACCOUNTS + "." + ACCOUNT_TYPE;
		String CONCRETE_DATA_SET = Tables.ACCOUNTS + "." + DATA_SET;
	}

	public interface DataColumns {
		public static final String MIMETYPE_ID = "mimetype_id";
	}

	public interface RawContactsColumns {
		public static final String ACCOUNT_ID = "account_id";
		public static final String CONCRETE_ACCOUNT_ID = Tables.RAW_CONTACTS + "." + ACCOUNT_ID;

	}

	public interface Tables {
		public static final String CONTACTS = "contacts";
		public static final String DELETED_CONTACTS = "deleted_contacts";
		public static final String RAW_CONTACTS = "raw_contacts";
		public static final String STREAM_ITEMS = "stream_items";
		public static final String STREAM_ITEM_PHOTOS = "stream_item_photos";
		public static final String PHOTO_FILES = "photo_files";
		public static final String PACKAGES = "packages";
		public static final String MIMETYPES = "mimetypes";
		public static final String PHONE_LOOKUP = "phone_lookup";
		public static final String NAME_LOOKUP = "name_lookup";
		public static final String AGGREGATION_EXCEPTIONS = "agg_exceptions";
		public static final String SETTINGS = "settings";
		public static final String DATA = "data";
		public static final String GROUPS = "groups";
		public static final String PRESENCE = "presence";
		public static final String AGGREGATED_PRESENCE = "agg_presence";
		public static final String NICKNAME_LOOKUP = "nickname_lookup";
		public static final String CALLS = "calls";
		public static final String STATUS_UPDATES = "status_updates";
		public static final String PROPERTIES = "properties";
		public static final String ACCOUNTS = "accounts";
		public static final String VISIBLE_CONTACTS = "visible_contacts";
		public static final String DIRECTORIES = "directories";
		public static final String DEFAULT_DIRECTORY = "default_directory";
		public static final String SEARCH_INDEX = "search_index";
		public static final String VOICEMAIL_STATUS = "voicemail_status";
		public static final String PRE_AUTHORIZED_URIS = "pre_authorized_uris";
		/// M: dialer search & conference calls table @{
		public static final String DIALER_SEARCH = "dialer_search";
		public static final String MST_DIALER_SEARCH = "mst_dialer_search";
		public static final String CONFERENCE_CALLS = "conference_calls";
		// @}
	}

	public interface Views {
		public static final String DATA = "view_data";
		public static final String RAW_CONTACTS = "view_raw_contacts";
		public static final String CONTACTS = "view_contacts";
		public static final String ENTITIES = "view_entities";
		public static final String RAW_ENTITIES = "view_raw_entities";
		public static final String GROUPS = "view_groups";
		public static final String DATA_USAGE_STAT = "view_data_usage_stat";
		public static final String STREAM_ITEMS = "view_stream_items";

		/// M: dialer search view @{
		public static final String DIALER_SEARCH_VIEW = "view_dialer_search";
		public static final String MST_DIALER_SEARCH_VIEW = "view_mst_dialer_search";
		/// @}
	}

	public class SearchResultCache {
		private static final String SPLITER = ",";
		public final String mSearchKey;
		private StringBuilder mRawContactIds;
		private int mIdCount = 0;

		public SearchResultCache(String searchKey) {
			mSearchKey = searchKey;
			mRawContactIds = new StringBuilder();
		}

		public void addId(String rawContactId) {
			mRawContactIds.append(rawContactId).append(SPLITER);
			++mIdCount;
		}

		public boolean isEmpty() {
			return mIdCount == 0;
		}

		public String getRawContactIds() {
			int len = mRawContactIds.length();
			if (len > 0 && mRawContactIds.substring(len - 1, len).equals(SPLITER)) {
				return mRawContactIds.subSequence(0, len - 1).toString();
			}
			return mRawContactIds.toString();
		}
	}

	public interface DialerSearchColumns {
		String _ID = BaseColumns._ID;
		String RAW_CONTACT_ID = "raw_contact_id";
		String QUAN_PINYIN = "quan_pinyin";
		String JIAN_PINYIN = "jian_pinyin";
		String QUAN_T9 = "quan_t9";
		String JIAN_T9 = "jian_t9";
		String MATCH_MAP = "match_map_quan";
		String MATCH_MAP_JIAN = "match_map_jian";
		String POLYPHONIC = "polyphonic";
		String SORT_KEY = "sort_key";
		String QUAN_PINYIN_HIGHLIGHT = "quan_pinyin_highlight";
		String MATCH_MAP_HIGHLIGHT = "match_map_quan_highlight";

		String[] COLUMN_NAMES = new String[]{
				RAW_CONTACT_ID,
				QUAN_PINYIN,
				JIAN_PINYIN,
				QUAN_T9,
				JIAN_T9,
				MATCH_MAP,
				MATCH_MAP_JIAN,
				POLYPHONIC,
				SORT_KEY,
				QUAN_PINYIN_HIGHLIGHT,
				MATCH_MAP_HIGHLIGHT,
		};

		int RCI_INDEX = 0;
		int QUAN_PINYIN_INDEX = 1;
		int JIAN_PINYIN_INDEX = 2;
		int QUAN_T9_INDEX = 3;
		int JIAN_T9_INDEX = 4;
		int MATCH_MAP_INDEX = 5;
		int MATCH_MAP_JIAN_INDEX = 6;
		int POLYPHONIC_INDEX = 7;
		int SORT_KEY_INDEX = 8;
		int QUAN_PINYIN_HIGHLIGHT_INDEX = 9;
		int MATCH_MAP_HIGHLIGHT_INDEX = 10;
	}

	public interface DialerSearchViewColumns {
		String _ID = BaseColumns._ID;
		String RAW_CONTACT_ID = "vds_raw_contact_id";
		String CONTACT_ID = "vds_contact_id";
		String LOOKUP_KEY = "vds_lookup";
		String NAME = "vds_phone_name";
		String PHONE_NUMBER = "vds_phone_number";
		String PHOTO_ID = "vds_photo_id";
		String INDICATE_PHONE_SIM = "vds_indicate_phone_sim";
		String INDEX_IN_SIM = "vds_index_in_sim";
		String QUAN_PINYIN = "vds_quan_pinyin";
		String JIAN_PINYIN = "vds_jian_pinyin";

		String TIMES_CONTACTED = "vds_times_contacted";
		String QUAN_T9 = "vds_quan_t9";
		String JIAN_T9 = "vds_jian_t9";
		String MATCH_MAP = "vds_match_map_quan";
		String MATCH_MAP_JIAN = "vds_match_map_jian";
		String POLYPHONIC = "vds_polyphonic";
		String SORT_KEY = "vds_sort_key";
		String HAS_PHONE_NUMBER = "vds_has_phone_number";
		String MIME_TYPE = "vds_mimetype_id";
		String ACCOUNT_NAME = "vds_account_name";
		String ACCOUNT_TYPE = "vds_account_type";
		String IN_VISIBLE_GROUP = "vds_in_visible_group";
		String QUAN_PINYIN_HIGHLIGHT = "vds_quan_pinyin_highlight";
		String MATCH_MAP_HIGHLIGHT = "vds_match_map_quan_highlight";
		//		String AUTO_RECORD = "vds_auto_record";
		//		String IS_PRIVACY = "vds_is_privacy";

		String[] COLUMN_NAMES = new String[]{
				RAW_CONTACT_ID,
				CONTACT_ID,
				LOOKUP_KEY,
				NAME,
				PHONE_NUMBER,
				PHOTO_ID,
				INDICATE_PHONE_SIM,
				INDEX_IN_SIM,
				QUAN_PINYIN,
				JIAN_PINYIN,

				TIMES_CONTACTED,
				QUAN_T9,
				JIAN_T9,
				MATCH_MAP,
				MATCH_MAP_JIAN,
				POLYPHONIC,
				SORT_KEY,
				HAS_PHONE_NUMBER,
				MIME_TYPE,
				ACCOUNT_NAME,
				ACCOUNT_TYPE,
				IN_VISIBLE_GROUP,
				QUAN_PINYIN_HIGHLIGHT,
				MATCH_MAP_HIGHLIGHT
				//				 AUTO_RECORD
				//				 IS_PRIVACY,
		};

		int RCI_INDEX = 0;
		int CONTACT_ID_INDEX = 1;
		int LOOKUP_KEY_INDEX = 2;
		int NAME_INDEX = 3;
		int PHONE_NUMBER_INDEX = 4;
		int PHOTO_ID_INDEX = 5;
		int INDICATE_PHONE_SIM_INDEX = 6;
		int INDEX_IN_SIM_INDEX = 7;
		int QUAN_PINYIN_INDEX = 8;
		int JIAN_PINYIN_INDEX = 9;

		int TIMES_CONTACTED_INDEX = 10;
		int QUAN_T9_INDEX = 11;
		int JIAN_T9_INDEX = 12;
		int MATCH_MAP_QUAN_INDEX = 13;
		int MATCH_MAP_JIAN_INDEX = 14;
		int POLYPHONIC_INDEX = 15;
		int SORT_KEY_INDEX = 16;
		int HAS_PHONE_NUMBER_INDEX = 17;
		int MIME_TYPE_INDEX = 18;
		int QUAN_PINYIN_HIGHLIGHT_INDEX = 22;
		int MATCH_MAP_HIGHLIGHT_INDEX = 23;

		//		int IS_PRIVACY_INDEX = 25;
	}

	private class DialerSearchViewRow implements DialerSearchViewColumns {
		String mRawContactId;
		String mContactId;
		String mLookupKey;
		String mName;
		String mIndicatePhoneSim;
		String mIndexInSim;
		String mPhotoId;
		String mPhoneNumber;
		private String mDataHighlight;
		int mTimesContacted;
		int mPolyphonic;

		String mQuanPinyin;
		String mSortKey;
		String mJianPinyin;
		private String mPinyinHighlight;
		String mQuanT9;
		String mJianT9;
		String mMatchMapQuan;
		String mMatchMapJian;
		int mQuanT9Len;
		int mJianT9Len;

		private String[] mQuanPinyinMulti;
		private String[] mJianPinyinMulti;
		private String[] mJianT9Multi;
		private String[] mQuanT9Multi;
		private String[] mMatchMapQuanMulti;
		private String[] mMatchMapJianMulti;
		private String[] mSortKeyMulti;
		private int mMultiIndex;

		String mMatchMapQuanHighlight;
		private String[] mMatchMapQuanMultiHighlight;
		String mQuanPinyinHighlight;
		private String[] mQuanPinyinMultiHighlight;

		private String mPrivacyId;

		private boolean isNameMatch=true;
		public void setNameMatch(boolean isNameMatch) {
			this.isNameMatch = isNameMatch;
		}

		public void read(Cursor cursor, String searchKey) {
			mRawContactId = cursor.getString(RCI_INDEX);
			mContactId = cursor.getString(CONTACT_ID_INDEX);
			mLookupKey = cursor.getString(LOOKUP_KEY_INDEX);
			mName = cursor.getString(NAME_INDEX);
			mIndicatePhoneSim = cursor.getString(INDICATE_PHONE_SIM_INDEX);
			mIndexInSim = cursor.getString(INDEX_IN_SIM_INDEX);
			mPhotoId = cursor.getString(PHOTO_ID_INDEX);

			int mimeType=cursor.getInt(MIME_TYPE_INDEX);
			Log.d(TAG,"mName:"+mName+" mimeType:"+mimeType+" cursor.getString(PHONE_NUMBER_INDEX):"+cursor.getString(PHONE_NUMBER_INDEX));
			if(mimeType==5||mimeType==1||(mimeType<=-10000)){
				mPhoneNumber = cursor.getString(PHONE_NUMBER_INDEX);
			}else{
				mPhoneNumber="";
			}
			if(mimeType<0&&mimeType>-10000) groupCount++;
			/*mPhoneNumber = DialerSearchUtils.stripSpecialCharInNumberForDialerSearch(
					cursor.getString(PHONE_NUMBER_INDEX));
			 */
			mDataHighlight = getDataHighlight(mPhoneNumber, searchKey);
			mTimesContacted = cursor.getInt(TIMES_CONTACTED_INDEX);
			mPolyphonic = cursor.getInt(POLYPHONIC_INDEX);

			//			mPrivacyId = String.valueOf(cursor.getLong(IS_PRIVACY_INDEX));

			if (0 == mPolyphonic) {
				mQuanPinyin = cursor.getString(QUAN_PINYIN_INDEX);

				mQuanPinyinHighlight = cursor.getString(QUAN_PINYIN_HIGHLIGHT_INDEX);
				mMatchMapQuanHighlight = cursor.getString(MATCH_MAP_HIGHLIGHT_INDEX);

				mJianPinyin = cursor.getString(JIAN_PINYIN_INDEX);
				mQuanT9 = cursor.getString(QUAN_T9_INDEX);
				mJianT9 = cursor.getString(JIAN_T9_INDEX);
				mMatchMapQuan = cursor.getString(MATCH_MAP_QUAN_INDEX);
				mMatchMapJian = cursor.getString(MATCH_MAP_JIAN_INDEX);
				mJianT9Len = mJianT9.length();
				mQuanT9Len = mQuanT9.length();
				mSortKey = cursor.getString(SORT_KEY_INDEX);
			} else {
				mQuanPinyinMulti = cursor.getString(QUAN_PINYIN_INDEX).split(POLYPHONIC_SEPARATOR_STR);

				mQuanPinyinMultiHighlight = cursor.getString(QUAN_PINYIN_HIGHLIGHT_INDEX).split(POLYPHONIC_SEPARATOR_STR);
				mMatchMapQuanMultiHighlight = cursor.getString(MATCH_MAP_HIGHLIGHT_INDEX).split(POLYPHONIC_SEPARATOR_STR);

				mJianPinyinMulti = cursor.getString(JIAN_PINYIN_INDEX).split(POLYPHONIC_SEPARATOR_STR);
				mQuanT9Multi = cursor.getString(QUAN_T9_INDEX).split(POLYPHONIC_SEPARATOR_STR);
				mJianT9Multi = cursor.getString(JIAN_T9_INDEX).split(POLYPHONIC_SEPARATOR_STR);
				mMatchMapQuanMulti = cursor.getString(MATCH_MAP_QUAN_INDEX).split(POLYPHONIC_SEPARATOR_STR);
				mMatchMapJianMulti = cursor.getString(MATCH_MAP_JIAN_INDEX).split(POLYPHONIC_SEPARATOR_STR);
				mSortKeyMulti = cursor.getString(SORT_KEY_INDEX).split(POLYPHONIC_SEPARATOR_STR);

				pickInMulti(0);
			}
		}

		private void pickInMulti(int multiIndex) {
			mQuanPinyin = mQuanPinyinMulti[multiIndex];
			mJianPinyin = mJianPinyinMulti[multiIndex];
			mQuanT9 = mQuanT9Multi[multiIndex];
			mJianT9 = mJianT9Multi[multiIndex];
			mMatchMapQuan = mMatchMapQuanMulti[multiIndex];
			mMatchMapJian = mMatchMapJianMulti[multiIndex];
			mSortKey = mSortKeyMulti[multiIndex];
			mJianT9Len = mJianT9.length();
			mQuanT9Len = mQuanT9.length();			
			mPinyinHighlight = null;

			mQuanPinyinHighlight = mQuanPinyinMultiHighlight[multiIndex];
			mMatchMapQuanHighlight = mMatchMapQuanMultiHighlight[multiIndex];
		}

		public boolean next() {
			if (mPolyphonic == 0 || mMultiIndex >= mQuanPinyinMulti.length - 1) {
				return false;
			}

			++mMultiIndex;
			pickInMulti(mMultiIndex);

			return true;
		}

		// is [)
		private String getDataHighlight(String number, String searchKey) {
			if (null != number) {
				int index = indexOf(number, searchKey);
				if (-1 != index) {
					return new String(new char[]{(char)index, (char)(index + searchKey.length())});	
				}
			}

			return null;
		}

		public void setPinyinHighlight(char[] nameHighlight) {
			mPinyinHighlight = String.valueOf(nameHighlight);
		}

		public int getDataMatchIndex() {
			if (null == mDataHighlight) {
				return -1;
			}

			return mDataHighlight.charAt(0);
		}

		private Object[] objs=null;
		public Object[] getDialerSearchResultRow() {
			if(objs==null){
				objs = new Object[DialerSearchResultColumns.COLUMN_NAMES.length];
				objs[DialerSearchResultColumns.CONTACT_ID_INDEX] = mContactId;
				objs[DialerSearchResultColumns.LOOKUP_KEY_INDEX] = mLookupKey;
				objs[DialerSearchResultColumns.INDICATE_PHONE_SIM_INDEX] = mIndicatePhoneSim;
				objs[DialerSearchResultColumns.INDEX_IN_SIM_INDEX] = mIndexInSim;
				objs[DialerSearchResultColumns.PHOTO_ID_INDEX] = mPhotoId;
				objs[DialerSearchResultColumns.NAME_INDEX] = mName;				
				objs[DialerSearchResultColumns.PHONE_NUMBER_INDEX] = mPhoneNumber;
				objs[DialerSearchResultColumns.DATA_HIGHLIGHT_INDEX] = mDataHighlight;
				objs[DialerSearchResultColumns.PINYIN_INDEX] = mQuanPinyinHighlight;//mQuanPinyin;
				objs[DialerSearchResultColumns.PINYIN_HIGHLIGHT_INDEX] = mPinyinHighlight;
				objs[DialerSearchResultColumns.IS_NAME_MATCH_INDEX]=isNameMatch;
				Log.d(TAG, "getDialerSearchResultRow:"+mName+","+mPhoneNumber+" mContactId:"+mContactId+" mPhoneNumber:"+mPhoneNumber
						+" isNameMatch:"+isNameMatch);
				//			objs[DialerSearchResultColumns.IS_PRIVACY_INDEX] = mPrivacyId;
			}

			return objs;
		}
	}

	public interface DialerSearchResultColumns {
		String _ID = BaseColumns._ID;
		String CONTACT_ID = "contact_id";
		String LOOKUP_KEY = "lookup_key";

		String INDICATE_PHONE_SIM = "indicate_phone_sim";
		String INDEX_IN_SIM = "index_in_sim";
		String PHOTO_ID = "photo_id";
		String NAME = "name";

		String PHONE_NUMBER = "phone_number";
		String DATA_HIGH_LIGHT = "data_highlight_offset";

		String QUAN_PINYIN = "quan_pinyin";
		String PINYIN_HIGH_LIGHT = "pinyin_highlight_offset";
		String IS_NAME_MATCH="is_name_match";

		//		String IS_PRIVACY = "is_privacy";

		public String[] COLUMN_NAMES = {
				_ID,
				CONTACT_ID,
				LOOKUP_KEY,
				INDICATE_PHONE_SIM,//3
				INDEX_IN_SIM,
				PHOTO_ID,
				NAME,//6
				PHONE_NUMBER,
				DATA_HIGH_LIGHT,
				QUAN_PINYIN,//9
				PINYIN_HIGH_LIGHT,
				IS_NAME_MATCH
				//			IS_PRIVACY,
		};

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
		public int IS_NAME_MATCH_INDEX      = 11;
		//        public int IS_PRIVACY_INDEX            = 11;
	}

	private static DialerSearchHelperForMst mDialerSearchHelperForMst = new DialerSearchHelperForMst();
	private Map<String, SearchResultCache> mSearchRetCache = new HashMap<String, SearchResultCache>();
	private static Context context;
	private DialerSearchHelperForMst() {
	}

	public static DialerSearchHelperForMst getInstance() {
		return mDialerSearchHelperForMst;
	}

	public static DialerSearchHelperForMst getInstance(Context mContext) {
		context=mContext;
		return mDialerSearchHelperForMst;
	}

	private void clearCache() {
		mSearchRetCache.clear();
	}

	private void clearCache(String searchKey) {
		if (TextUtils.isEmpty(searchKey)) {
			mSearchRetCache.clear();
			return;
		}

		Iterator<Entry<String, SearchResultCache>> iterator = mSearchRetCache.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, SearchResultCache> entry = iterator.next();
			if (!searchKey.startsWith(entry.getKey())) {
				iterator.remove();
			}
		}
	}

	private void writeCache(SearchResultCache searchRetCache) {
		mSearchRetCache.put(searchRetCache.mSearchKey, searchRetCache);
	}

	private SearchResultCache getCache(String searchKey) {
		if (!TextUtils.isEmpty(searchKey)) {
			SearchResultCache cache = mSearchRetCache.get(searchKey);
			if (null != cache) {
				return cache;
			}

			if (searchKey.length() > 1) {
				return mSearchRetCache.get(searchKey.substring(0, searchKey.length() - 1));
			}
		}

		return null;
	}

	public void init(SQLiteDatabase db) {
		Log.i(TAG, "DialerSearchHelperForMstV2  init");
		clearCache();

		//		db.execSQL("DROP TABLE IF EXISTS " + TEMP_DIALER_SEARCH_VIEW_TABLE);
		//		db.execSQL("CREATE TEMP TABLE  " + TEMP_DIALER_SEARCH_VIEW_TABLE 
		//				+ " AS SELECT * FROM " + MST_DIALER_SEARCH_VIEW);
	}

	public Cursor query(SQLiteDatabase db, Uri uri, String selection) {
		String originSearchKey = "";
		String uriStr=URLDecoder.decode(uri.toString()).toLowerCase();
		Log.d(TAG,"query,uri:"+uri+" uriStr:"+uriStr);
		if (uriStr.indexOf("mst_dialer_search/") > 0) {
			originSearchKey = uriStr.substring(uriStr.lastIndexOf("/")+1);
		} else {
			return null;
		}

		String searchKey=null;
		if(!TextUtils.isEmpty(originSearchKey)){
			ArrayList<Token> tokens = HanziToPinyin.getInstance().getTokens(originSearchKey);
			StringBuilder sb=new StringBuilder();
			for (int i = 0; i<tokens.size(); i++) {                	
				final Token token = tokens.get(i);
				sb.append(token.target);                    
			}
			searchKey=sb.toString().toLowerCase();
		}
		Log.d(TAG,"originSearchKey:"+originSearchKey+" searchKey:"+searchKey);
		if(searchKey==null) return null;

		boolean isAllDigits = true;
		for (char c : searchKey.toCharArray()) {
			if (('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z')) {
				isAllDigits = false;
				break;
			}
		}

		Log.d(TAG,"query,isAllDigits:"+isAllDigits);
		return queryForDial(db, searchKey, selection, uri,isAllDigits,originSearchKey);
		//		if (isAllDigit) {
		//			return queryDigit(db, searchKey, selection);
		//		} else {
		//			return queryAbc(db, searchKey, selection);
		//		}	
	}

	/*public Cursor queryMutil(SQLiteDatabase db, Uri uri, String selection) {
		String searchKey = "";
		String uriStr = uri.toString();
		if (uriStr.indexOf("aurora_multi_search/") > 0) {
			searchKey = uriStr.substring(uriStr.lastIndexOf("/")+1);
		} else {
			return null;
		}

		boolean hasHanzi = hasHanZi(searchKey);
		boolean isAllDigit = hasHanzi ? false : true;

		if (isAllDigit) {
			for (char c : searchKey.toCharArray()) {
				if (9 < c && c < 0) {
					isAllDigit = false;
					searchKey = searchKey.toLowerCase();
					break;
				}
			}
		}

		StringBuffer sb = new StringBuffer();
		if (!isAllDigit) {
			for (char c : searchKey.toCharArray()) {
				if (hasHanZi(String.valueOf(c))) {
					String str = ChineseToPinyinHelper.translateMulti(String.valueOf(c), true)[0][0];
					sb.append(str);
				} else {
					sb.append(c);
				}
			}

			if (sb != null) {
				searchKey = sb.toString();
			}
		}

		if (isAllDigit) {
			return queryDigit(db, searchKey, selection);
		} else {
			return queryContactId(db, searchKey, selection);
		}
	}*/

	private void addToListSort(List<DialerSearchViewRow> list, DialerSearchViewRow sdr) {
		/*if (sdr.mTimesContacted > 0) {
			for (int i = 0, size = list.size() - 1; i <= size; ++i) {
				if (sdr.mTimesContacted > list.get(i).mTimesContacted) {
					list.add(i, sdr);
					return;
				}
			}
		}*/

		list.add(sdr);
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

	/*protected Cursor queryDigit(SQLiteDatabase db, String searchKey, String selec) {
		final int KEY_LEN = searchKey.length();
		Long startTime = System.currentTimeMillis();

		//		String selection = "(" + DialerSearchViewColumns.JIAN_T9 + " LIKE '%" + searchKey.charAt(0) + "%' OR " +
		//				DialerSearchViewColumns.PHONE_NUMBER + " LIKE '%" + searchKey + "%')";
		String selection = "(" 
				+"(" + DialerSearchViewColumns.JIAN_T9 + " LIKE '%" + searchKey.charAt(0) + "%' AND "+DialerSearchViewColumns.MIME_TYPE+"=7)"
				+" OR (" +DialerSearchViewColumns.PHONE_NUMBER + " LIKE '%" + searchKey + "%' AND "+DialerSearchViewColumns.MIME_TYPE+">0))";

		//		boolean skip = false;
		//		if (KEY_LEN > 1) {   		
		//			SearchResultCache searchRetCache = getCache(searchKey);
		//			skip = (null != searchRetCache && searchRetCache.isEmpty()); 
		//			if (!skip) {
		//				if (null != searchRetCache) {
		//					selection = selection + " AND (" + 
		//							DialerSearchViewColumns.RAW_CONTACT_ID + 
		//							" IN (" + searchRetCache.getRawContactIds() + "))";	
		//				}
		//			}
		//		}


		if (selec != null) {
			selection = selection + " AND " + selec;
		}
		Cursor queryCursor = db.query(TEMP_DIALER_SEARCH_VIEW_TABLE, DialerSearchViewColumns.COLUMN_NAMES, 
				selection,
				null, 
				null, 
				null,
				DialerSearchViewColumns.SORT_KEY+","+DialerSearchViewColumns.MIME_TYPE);	

		Log.d(TAG,"selection:"+selection+" queryCursor:"+(queryCursor==null?"null":queryCursor.getCount()));
		List<DialerSearchViewRow> matchedList =dealDigits(queryCursor,searchKey);

		MatrixCursor resultCursor =new MatrixCursor(DialerSearchResultColumns.COLUMN_NAMES);
		if (null != matchedList && matchedList.size() > 0) {
			//过滤联系人相同项
			filterSameContacts(matchedList, resultCursor);			
		}

		if (null != queryCursor) {
			queryCursor.close();
			queryCursor = null;
		}
		matchedList = null;
		Log.d(TAG,"resultCursor.count1:"+(resultCursor==null?"null":resultCursor.getCount()));	


		//查询通话记录（只查非联系人的通话记录）
		Cursor callLogCursor=db.query("calls", 
				CallLogQuery._PROJECTION, 
				"deleted = 0 AND NOT (type = 4) AND number LIKE '%"+searchKey+"%' AND name IS NULL", 
				null, 
				"number",
				null, 
				"_id desc");		
		Log.d(TAG,"calllogcursor:"+(callLogCursor==null?"null":callLogCursor.getCount()));




		//		if (KEY_LEN > 1) {
		//			SearchResultCache searchRetCache = new SearchResultCache(searchKey);
		//			if (null != matchedList) {
		//				for (DialerSearchViewRow sdr : matchedList) {
		//					searchRetCache.addId(sdr.mRawContactId);
		//				}
		//			}
		//			writeCache(searchRetCache);
		//		}

		//		if (null != matchedList && matchedList.size() > 0) {
		//			//			resultCursor = new MatrixCursor(DialerSearchResultColumns.COLUMN_NAMES);
		//			int size = matchedList.size();
		//
		//			Iterator<DialerSearchViewRow> iterator = matchedList.iterator();
		//			int count = 0;
		//			while(iterator.hasNext() && count < size) {
		//				DialerSearchViewRow sdr = iterator.next();
		//				resultCursor.addRow(sdr.getDialerSearchResultRow());
		//				count++;
		//			}
		//			matchedList = null;
		//		}


		if(callLogCursor!=null&&callLogCursor.getCount()>0&&callLogCursor.moveToFirst()){
			do{
				Object[] raw = new Object[DialerSearchResultColumns.COLUMN_NAMES.length];

				String number=callLogCursor.getString(CallLogQuery.NUMBER);
				//				String area = calllogLines.getString(CallLogQuery.CALLS_JOIN_DATA_VIEW_AREA);
				long date = callLogCursor.getLong(CallLogQuery.DATE);
				long duration = callLogCursor.getLong(CallLogQuery.DURATION);
				int type = callLogCursor.getInt(CallLogQuery.CALL_TYPE);
				int simId = callLogCursor.getInt(CallLogQuery.ACCOUNT_ID);
				int callid=callLogCursor.getInt(CallLogQuery.ID);
				String location=callLogCursor.getString(CallLogQuery.GEOCODED_LOCATION);
				//				int callscount=callLogCursor.getInt(CallLogQuery.GN_CALLS_JOIN_DATA_VIEW_CALLS_COUNT);
				//				String callids=callLogCursor.getString(CallLogQuery.GN_CALLS_JOIN_DATA_VIEW_CALLS_COUNT_IDS);

				raw[0]=callid;
				raw[1]=number;
				raw[2]=date;
				raw[3]=duration;
				raw[4]=type;
				raw[5]=simId;
				raw[6]=location;
				raw[7]="";
				raw[8]="";
				raw[9]="";
				raw[10]=searchKey;

				resultCursor.addRow(raw);						

			}while(callLogCursor.moveToNext());
		}
		if (null != callLogCursor) {
			callLogCursor.close();
			callLogCursor = null;
		}   
		Log.d(TAG,"resultCursor.count2:"+(resultCursor==null?"null":resultCursor.getCount()));


		//查询常用号码（黄页）
		String selection1 = "(" + DialerSearchViewColumns.JIAN_T9 + " LIKE '%" + searchKey.charAt(0) + "%' OR " +
				DialerSearchViewColumns.PHONE_NUMBER + " LIKE '%" + searchKey + "%')  AND "
				+ DialerSearchViewColumns.MIME_TYPE + " <-9999";
		Cursor usefulNumberCursor = db.query(TEMP_DIALER_SEARCH_VIEW_TABLE, 
				DialerSearchViewColumns.COLUMN_NAMES, 
				selection1,
				null, 
				null, 
				null, 
				DialerSearchViewColumns.SORT_KEY);
		Log.d(TAG,"usefulNumberCursor:"+(usefulNumberCursor==null?"null":usefulNumberCursor.getCount()));
		List<DialerSearchViewRow> matchedList1 =dealDigits(usefulNumberCursor,searchKey);
		if (null != usefulNumberCursor) {
			usefulNumberCursor.close();
			usefulNumberCursor = null;
		}     
		if (null != matchedList1 && matchedList1.size() > 0) {
			//			resultCursor = new MatrixCursor(DialerSearchResultColumns.COLUMN_NAMES);
			int size = matchedList1.size();

			Iterator<DialerSearchViewRow> iterator = matchedList1.iterator();
			int count = 0;
			while(iterator.hasNext() && count < size) {
				DialerSearchViewRow sdr = iterator.next();
				resultCursor.addRow(sdr.getDialerSearchResultRow());
				count++;
			}			
		}	
		matchedList1 = null;
		Log.d(TAG,"resultCursor.count3:"+(resultCursor==null?"null":resultCursor.getCount()));

		return resultCursor;
	}*/

	protected Cursor queryForDial(SQLiteDatabase db, String key, String selec,Uri uri,boolean isAllDigits,String originSearchKey) {
		long startTime=System.currentTimeMillis();
		Log.d(TAG,"queryForDial0");
		String searchKey=key;
		//		if(!isAllDigits){}
		final int KEY_LEN = key.length();


		String s=isAllDigits?DialerSearchViewColumns.JIAN_T9:DialerSearchViewColumns.JIAN_PINYIN;
		String selection = "(" 
				+"(" +s + " LIKE '%" + searchKey.charAt(0) + "%' AND "+DialerSearchViewColumns.MIME_TYPE+"=5)"
				+" OR (" +DialerSearchViewColumns.PHONE_NUMBER + " LIKE '%" + searchKey + "%' AND "+DialerSearchViewColumns.MIME_TYPE+"=5))";

		//		boolean skip = false;
		//		if (KEY_LEN > 1) {   		
		//			SearchResultCache searchRetCache = getCache(searchKey);
		//			skip = (null != searchRetCache && searchRetCache.isEmpty()); 
		//			if (!skip) {
		//				if (null != searchRetCache) {
		//					selection = selection + " AND (" + 
		//							DialerSearchViewColumns.RAW_CONTACT_ID + 
		//							" IN (" + searchRetCache.getRawContactIds() + "))";	
		//				}
		//			}
		//		}

		if (selec != null) {
			selection = selection + " AND " + selec;
		}
		Cursor queryCursor = db.query(TEMP_DIALER_SEARCH_VIEW_TABLE, 
				DialerSearchViewColumns.COLUMN_NAMES, 
				selection,
				null, 
				null, 
				null,
				DialerSearchViewColumns.SORT_KEY+","+DialerSearchViewColumns.PHONE_NUMBER);	
		Log.d(TAG,"queryForDial1");
		Log.d(TAG,"selection:"+selection+" queryCursor:"+(queryCursor==null?"null":queryCursor.getCount()));
		List<DialerSearchViewRow> matchedList=null;
		if(isAllDigits){
			matchedList =dealDigits(queryCursor,searchKey);
		}else{
			matchedList =dealAbc(queryCursor,searchKey,originSearchKey);
		}
		Log.d(TAG,"queryForDial2");
		MatrixCursor resultCursor =new MatrixCursor(DialerSearchResultColumns.COLUMN_NAMES);
		if (null != matchedList && matchedList.size() > 0) {
			//过滤联系人相同项
			filterSameContacts(matchedList, resultCursor,searchKey,true);			
		}
		if (null != queryCursor) {
			queryCursor.close();
			queryCursor = null;
		}
		matchedList = null;
		Log.d(TAG,"resultCursor.count1:"+(resultCursor==null?"null":resultCursor.getCount()));	

		Log.d(TAG,"queryForDial3");
		if(isAllDigits){
			//查询通话记录（只查非联系人的通话记录）
			Cursor callLogCursor=db.query("calls", 
					CallLogQuery._PROJECTION, 
					"deleted = 0 AND NOT (type = 4) AND number LIKE '%"+searchKey+"%' AND name IS NULL", 
					null, 
					"number",
					null, 
					"_id desc");		
			Log.d(TAG,"calllogcursor:"+(callLogCursor==null?"null":callLogCursor.getCount()));




			//		if (KEY_LEN > 1) {
			//			SearchResultCache searchRetCache = new SearchResultCache(searchKey);
			//			if (null != matchedList) {
			//				for (DialerSearchViewRow sdr : matchedList) {
			//					searchRetCache.addId(sdr.mRawContactId);
			//				}
			//			}
			//			writeCache(searchRetCache);
			//		}

			//		if (null != matchedList && matchedList.size() > 0) {
			//			//			resultCursor = new MatrixCursor(DialerSearchResultColumns.COLUMN_NAMES);
			//			int size = matchedList.size();
			//
			//			Iterator<DialerSearchViewRow> iterator = matchedList.iterator();
			//			int count = 0;
			//			while(iterator.hasNext() && count < size) {
			//				DialerSearchViewRow sdr = iterator.next();
			//				resultCursor.addRow(sdr.getDialerSearchResultRow());
			//				count++;
			//			}
			//			matchedList = null;
			//		}


			if(callLogCursor!=null&&callLogCursor.getCount()>0&&callLogCursor.moveToFirst()){
				do{
					Object[] raw = new Object[DialerSearchResultColumns.COLUMN_NAMES.length];

					String number=callLogCursor.getString(CallLogQuery.NUMBER);
					//				String area = calllogLines.getString(CallLogQuery.CALLS_JOIN_DATA_VIEW_AREA);
					long date = callLogCursor.getLong(CallLogQuery.DATE);
					long duration = callLogCursor.getLong(CallLogQuery.DURATION);
					int type = callLogCursor.getInt(CallLogQuery.CALL_TYPE);
					int simId = callLogCursor.getInt(CallLogQuery.ACCOUNT_ID);
					int callid=callLogCursor.getInt(CallLogQuery.ID);
					String location=callLogCursor.getString(CallLogQuery.GEOCODED_LOCATION);
					//				int callscount=callLogCursor.getInt(CallLogQuery.GN_CALLS_JOIN_DATA_VIEW_CALLS_COUNT);
					//				String callids=callLogCursor.getString(CallLogQuery.GN_CALLS_JOIN_DATA_VIEW_CALLS_COUNT_IDS);

					raw[0]=callid;
					raw[1]=number;
					raw[2]=date;
					raw[3]=duration;
					raw[4]=type;
					raw[5]=simId;
					raw[6]=location;
					raw[7]="";
					raw[8]="";
					raw[9]="";
					raw[10]=searchKey;

					resultCursor.addRow(raw);						

				}while(callLogCursor.moveToNext());
			}
			if (null != callLogCursor) {
				callLogCursor.close();
				callLogCursor = null;
			}   
			Log.d(TAG,"resultCursor.count2:"+(resultCursor==null?"null":resultCursor.getCount()));
			Log.d(TAG,"queryForDial4");
		}


		//查询常用号码（黄页）

		String selection1 = "(" + s + " LIKE '%" + searchKey.charAt(0) + "%' OR " +
				DialerSearchViewColumns.PHONE_NUMBER + " LIKE '%" + searchKey + "%')  AND "
				+ DialerSearchViewColumns.MIME_TYPE + " <-9999";
		Cursor usefulNumberCursor = db.query(TEMP_DIALER_SEARCH_VIEW_TABLE, 
				DialerSearchViewColumns.COLUMN_NAMES, 
				selection1,
				null, 
				null, 
				null, 
				DialerSearchViewColumns.SORT_KEY);
		Log.d(TAG,"usefulNumberCursor:"+(usefulNumberCursor==null?"null":usefulNumberCursor.getCount()));
		Log.d(TAG,"queryForDial5");
		List<DialerSearchViewRow> matchedList1 =null;		
		if(isAllDigits){
			matchedList1 =dealDigits(usefulNumberCursor,searchKey);
		}else{
			matchedList1 =dealAbc(usefulNumberCursor,searchKey,originSearchKey);
		}
		if (null != usefulNumberCursor) {
			usefulNumberCursor.close();
			usefulNumberCursor = null;
		}     
		Log.d(TAG,"queryForDial6");
		if (null != matchedList1 && matchedList1.size() > 0) {
			//			resultCursor = new MatrixCursor(DialerSearchResultColumns.COLUMN_NAMES);
			int size = matchedList1.size();
			Iterator<DialerSearchViewRow> iterator = matchedList1.iterator();
			int count = 0;
			while(iterator.hasNext() && count < size) {
				DialerSearchViewRow sdr = iterator.next();
				resultCursor.addRow(sdr.getDialerSearchResultRow());
				count++;
			}			
		}	
		matchedList1 = null;
		Log.d(TAG,"resultCursor.count3:"+(resultCursor==null?"null":resultCursor.getCount()));
		Log.d(TAG,"queryForDial7");
		return resultCursor;
	}

	//************
	public void createDialerSearchTable(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + MST_DIALER_SEARCH_TABLE + ";");
		db.execSQL("CREATE TABLE " + MST_DIALER_SEARCH_TABLE + " ("
				+ DialerSearchColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ DialerSearchColumns.RAW_CONTACT_ID + " INTEGER REFERENCES raw_contacts(_id) NOT NULL,"
				+ DialerSearchColumns.QUAN_PINYIN + " VARCHAR DEFAULT NULL,"
				+ DialerSearchColumns.JIAN_PINYIN + " VARCHAR DEFAULT NULL,"
				+ DialerSearchColumns.QUAN_T9 + " VARCHAR DEFAULT NULL,"
				+ DialerSearchColumns.JIAN_T9 + " VARCHAR DEFAULT NULL,"
				+ DialerSearchColumns.MATCH_MAP + " VARCHAR DEFAULT NULL,"
				+ DialerSearchColumns.MATCH_MAP_JIAN + " VARCHAR DEFAULT NULL,"
				+ DialerSearchColumns.POLYPHONIC + " INTEGER DEFAULT 0,"
				+ DialerSearchColumns.SORT_KEY + " VARCHAR DEFAULT NULL,"

	            + DialerSearchColumns.QUAN_PINYIN_HIGHLIGHT + " VARCHAR DEFAULT NULL," // add by wangth for highlight
	            + DialerSearchColumns.MATCH_MAP_HIGHLIGHT + " VARCHAR DEFAULT NULL"
	            + ");");

		db.execSQL("CREATE INDEX MST_DIALER_search_raw_contact_id_index ON "
				+ MST_DIALER_SEARCH_TABLE + " ("
				+ DialerSearchColumns.RAW_CONTACT_ID + ");");
		createUsefulNumberTable(db);
	}

	public static final String MST_USEFUL_NUMBER_TABLE="mst_useful_number";
	private void createUsefulNumberTable(SQLiteDatabase db){
		Log.d(TAG,"createUsefulNumberTable");
		db.execSQL("DROP TABLE IF EXISTS " + MST_USEFUL_NUMBER_TABLE + ";");
		db.execSQL("CREATE TABLE " 
				+ MST_USEFUL_NUMBER_TABLE 
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+" contact_id INTEGER DEFAULT 0,"
				+" name VARCHAR DEFAULT NULL,"
				+" number VARCHAR DEFAULT NULL);");
	}

	public void createDialerSearchView(SQLiteDatabase db) {
		Log.d(TAG,"createDialerSearchView");
		db.execSQL("DROP VIEW IF EXISTS " + MST_DIALER_SEARCH_VIEW + ";");
		String GDS_DETAIL_TABLE = "contacts_detail";
		String GDS_NUMBER_TABLE = "number_data";
		String RAW_CONTACT_ID = "raw_contact_id";

		String VIEW_SELECT = "SELECT " + 
				MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.RAW_CONTACT_ID +
				" AS " + DialerSearchViewColumns.RAW_CONTACT_ID + "," +
				GDS_DETAIL_TABLE + "." + RawContacts.CONTACT_ID + 
				" AS " + DialerSearchViewColumns.CONTACT_ID + "," +
				GDS_DETAIL_TABLE + "." + Contacts.LOOKUP_KEY + 
				" AS " + DialerSearchViewColumns.LOOKUP_KEY + "," +
				GDS_DETAIL_TABLE + "." + RawContacts.DISPLAY_NAME_PRIMARY + 
				" AS " + DialerSearchViewColumns.NAME + "," +
				GDS_NUMBER_TABLE + "." + DialerSearchViewColumns.PHONE_NUMBER +
				" AS " + DialerSearchViewColumns.PHONE_NUMBER + "," +
				GDS_DETAIL_TABLE + "." + Contacts.PHOTO_ID + 
				" AS " + DialerSearchViewColumns.PHOTO_ID + "," +
				GDS_DETAIL_TABLE + "." + RawContacts.INDICATE_PHONE_SIM + 
				" AS " + DialerSearchViewColumns.INDICATE_PHONE_SIM + "," +
				GDS_DETAIL_TABLE + "." + RawContacts.INDEX_IN_SIM + 
				" AS " + DialerSearchViewColumns.INDEX_IN_SIM + "," +
				MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.QUAN_PINYIN + 
				" AS " + DialerSearchViewColumns.QUAN_PINYIN + "," +
				MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.JIAN_PINYIN + 
				" AS " + DialerSearchViewColumns.JIAN_PINYIN + "," +

		GDS_DETAIL_TABLE + "." + Contacts.TIMES_CONTACTED + 
		" AS " + DialerSearchViewColumns.TIMES_CONTACTED + "," +
		MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.JIAN_T9 + 
		" AS " + DialerSearchViewColumns.JIAN_T9 + "," +
		MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.QUAN_T9 + 
		" AS " + DialerSearchViewColumns.QUAN_T9 + "," +
		MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.MATCH_MAP + 
		" AS " + DialerSearchViewColumns.MATCH_MAP + "," +
		MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.MATCH_MAP_JIAN + 
		" AS " + DialerSearchViewColumns.MATCH_MAP_JIAN + "," +
		MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.POLYPHONIC + 
		" AS " + DialerSearchViewColumns.POLYPHONIC +  "," +
		MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.SORT_KEY + 
		" AS " + DialerSearchViewColumns.SORT_KEY + "," +
		GDS_DETAIL_TABLE + "." + Contacts.HAS_PHONE_NUMBER + 
		" AS " + DialerSearchViewColumns.HAS_PHONE_NUMBER + "," +
		GDS_NUMBER_TABLE + "." + DataColumns.MIMETYPE_ID + 
		" AS " + DialerSearchViewColumns.MIME_TYPE + "," +
		GDS_DETAIL_TABLE + "." + RawContacts.ACCOUNT_NAME +
		" AS " + DialerSearchViewColumns.ACCOUNT_NAME + "," +
		GDS_DETAIL_TABLE + "." + RawContacts.ACCOUNT_TYPE +
		" AS " + DialerSearchViewColumns.ACCOUNT_TYPE + "," +
		GDS_DETAIL_TABLE + "." + Contacts.IN_VISIBLE_GROUP + 
		" AS " + DialerSearchViewColumns.IN_VISIBLE_GROUP +


		"," + MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.QUAN_PINYIN_HIGHLIGHT + 
		" AS " + DialerSearchViewColumns.QUAN_PINYIN_HIGHLIGHT + "," +
		MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.MATCH_MAP_HIGHLIGHT + 
		" AS " + DialerSearchViewColumns.MATCH_MAP_HIGHLIGHT +

		//        // add for auto_record
		//        "," + GDS_NUMBER_TABLE + "." + "auto_record" +
		//        " AS " + DialerSearchViewColumns.AUTO_RECORD +
		//        
		//        // add for privacy
		//        "," + GDS_NUMBER_TABLE + "." + "is_privacy" +
		//        " AS " + DialerSearchViewColumns.IS_PRIVACY +

		" FROM " +
		MST_DIALER_SEARCH_TABLE + " LEFT JOIN " +
		" (SELECT " +
		Tables.CONTACTS + "." + Contacts._ID + " AS " + RawContacts.CONTACT_ID + "," +
		Tables.CONTACTS + "." + Contacts.LOOKUP_KEY + " AS " + Contacts.LOOKUP_KEY + "," +
		Tables.CONTACTS + "." + Contacts.PHOTO_ID + " AS " + Contacts.PHOTO_ID + "," +
		Tables.CONTACTS + "." + Contacts.TIMES_CONTACTED + " AS " + Contacts.TIMES_CONTACTED + "," +
		Tables.CONTACTS + "." + Contacts.HAS_PHONE_NUMBER + " AS " + Contacts.HAS_PHONE_NUMBER + "," +
		Tables.RAW_CONTACTS + "." + RawContacts._ID + " AS " + RAW_CONTACT_ID + "," +
		Tables.RAW_CONTACTS + "." + RawContacts.DISPLAY_NAME_PRIMARY  + " AS " + RawContacts.DISPLAY_NAME_PRIMARY + "," + 
		Tables.RAW_CONTACTS + "." + RawContacts.INDICATE_PHONE_SIM + " AS " + RawContacts.INDICATE_PHONE_SIM + "," +
		Tables.RAW_CONTACTS + "." + RawContacts.INDEX_IN_SIM + " AS " + RawContacts.INDEX_IN_SIM + "," + 
		AccountsColumns.CONCRETE_ACCOUNT_NAME + " AS " + RawContacts.ACCOUNT_NAME + "," +
		AccountsColumns.CONCRETE_ACCOUNT_TYPE + " AS " + RawContacts.ACCOUNT_TYPE + "," +
		Views.CONTACTS + "." + Contacts.IN_VISIBLE_GROUP + " AS " + Contacts.IN_VISIBLE_GROUP +

		" FROM " +
		Tables.CONTACTS + " LEFT JOIN " + Tables.RAW_CONTACTS + " LEFT JOIN " + Tables.ACCOUNTS + " LEFT JOIN " + Views.CONTACTS +
		" WHERE " +
		//Tables.RAW_CONTACTS + "." + RawContacts._ID + "=" + Tables.CONTACTS + "." + "name_raw_contact_id" + " AND " +
		//Tables.CONTACTS + "." + Contacts.HAS_PHONE_NUMBER + " = 1 ) AS " + GDS_DETAIL_TABLE + 
		Tables.RAW_CONTACTS + "." + RawContacts.CONTACT_ID + "=" + Tables.CONTACTS + "." + Contacts._ID + 
		" AND " + RawContactsColumns.CONCRETE_ACCOUNT_ID + "=" + AccountsColumns.CONCRETE_ID + 
		" AND " + Tables.CONTACTS + "." + Contacts._ID + "=" + Views.CONTACTS + "." + "_id" + 
		" ) AS " + GDS_DETAIL_TABLE + 
		" LEFT JOIN " +

		" (SELECT " + RAW_CONTACT_ID + "," + Data.DATA1 + " AS " + DialerSearchViewColumns.PHONE_NUMBER +
		"," + DataColumns.MIMETYPE_ID + " AS " + DataColumns.MIMETYPE_ID +
		//		 "," + "auto_record" + ",is_privacy" +
		" FROM " +
		Tables.DATA +
		" WHERE " +
		DataColumns.MIMETYPE_ID + " in ('5', '7','1') ) AS " + GDS_NUMBER_TABLE +

		" WHERE " +
		GDS_DETAIL_TABLE + "." + RAW_CONTACT_ID + "=" + MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.RAW_CONTACT_ID + " AND " +
		GDS_NUMBER_TABLE + "." + RAW_CONTACT_ID + "=" + MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.RAW_CONTACT_ID 

		//用于匹配群组名称 add by liyang
		+" union "+
		"SELECT " + 
		MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.RAW_CONTACT_ID +
		" AS " + DialerSearchViewColumns.RAW_CONTACT_ID + "," +
		"mst_dialer_search.raw_contact_id" + 
		" AS " + DialerSearchViewColumns.CONTACT_ID + "," +
		"mst_dialer_search.raw_contact_id" + 
		" AS " + DialerSearchViewColumns.LOOKUP_KEY + "," +
		"vds_groups.vds_phone_name" + 
		" AS " + DialerSearchViewColumns.NAME + "," +
		"vds_groups.vds_phone_number" +
		" AS " + DialerSearchViewColumns.PHONE_NUMBER + "," +
		"mst_dialer_search.raw_contact_id" + 
		" AS " + DialerSearchViewColumns.PHOTO_ID + "," +
		"mst_dialer_search.raw_contact_id" + 
		" AS " + DialerSearchViewColumns.INDICATE_PHONE_SIM + "," +
		"mst_dialer_search.raw_contact_id" + 
		" AS " + DialerSearchViewColumns.INDEX_IN_SIM + "," +
		MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.QUAN_PINYIN + 
		" AS " + DialerSearchViewColumns.QUAN_PINYIN + "," +
		MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.JIAN_PINYIN + 
		" AS " + DialerSearchViewColumns.JIAN_PINYIN + "," +
		"mst_dialer_search.raw_contact_id" + 
		" AS " + DialerSearchViewColumns.TIMES_CONTACTED + "," +
		MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.JIAN_T9 + 
		" AS " + DialerSearchViewColumns.JIAN_T9 + "," +
		MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.QUAN_T9 + 
		" AS " + DialerSearchViewColumns.QUAN_T9 + "," +
		MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.MATCH_MAP + 
		" AS " + DialerSearchViewColumns.MATCH_MAP + "," +
		MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.MATCH_MAP_JIAN + 
		" AS " + DialerSearchViewColumns.MATCH_MAP_JIAN + "," +
		MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.POLYPHONIC + 
		" AS " + DialerSearchViewColumns.POLYPHONIC +  "," +
		MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.SORT_KEY + 
		" AS " + DialerSearchViewColumns.SORT_KEY + "," +
		"mst_dialer_search.raw_contact_id" + 
		" AS " + DialerSearchViewColumns.HAS_PHONE_NUMBER + "," +
		"mst_dialer_search.raw_contact_id" + 
		" AS " + DialerSearchViewColumns.MIME_TYPE + "," +
		"mst_dialer_search.raw_contact_id" +
		" AS " + DialerSearchViewColumns.ACCOUNT_NAME + "," +
		"mst_dialer_search.raw_contact_id" +
		" AS " + DialerSearchViewColumns.ACCOUNT_TYPE + "," +
		"mst_dialer_search.raw_contact_id" + 
		" AS " + DialerSearchViewColumns.IN_VISIBLE_GROUP +
		"," + MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.QUAN_PINYIN_HIGHLIGHT + 
		" AS " + DialerSearchViewColumns.QUAN_PINYIN_HIGHLIGHT + "," +
		MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.MATCH_MAP_HIGHLIGHT + 
		" AS " + DialerSearchViewColumns.MATCH_MAP_HIGHLIGHT +
		" FROM " +
		MST_DIALER_SEARCH_TABLE + " LEFT JOIN " +
		"(SELECT _id,title AS vds_phone_name,title AS vds_phone_number FROM groups) AS vds_groups where mst_dialer_search.raw_contact_id<=0  AND mst_dialer_search.raw_contact_id=-vds_groups._id"

      //用于匹配常用号码  add by liyang
      +" union "+
      "SELECT " + 
      MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.RAW_CONTACT_ID +
      " AS " + DialerSearchViewColumns.RAW_CONTACT_ID + "," +
      "mst_dialer_search.raw_contact_id" + 
      " AS " + DialerSearchViewColumns.CONTACT_ID + "," +
      "mst_dialer_search.raw_contact_id" + 
      " AS " + DialerSearchViewColumns.LOOKUP_KEY + "," +
      "mst_useful_number.name" + 
      " AS " + DialerSearchViewColumns.NAME + "," +
      "mst_useful_number.number" +
      " AS " + DialerSearchViewColumns.PHONE_NUMBER + "," +
      "mst_dialer_search.raw_contact_id" + 
      " AS " + DialerSearchViewColumns.PHOTO_ID + "," +
      "mst_dialer_search.raw_contact_id" + 
      " AS " + DialerSearchViewColumns.INDICATE_PHONE_SIM + "," +
      "mst_dialer_search.raw_contact_id" + 
      " AS " + DialerSearchViewColumns.INDEX_IN_SIM + "," +
      MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.QUAN_PINYIN + 
      " AS " + DialerSearchViewColumns.QUAN_PINYIN + "," +
      MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.JIAN_PINYIN + 
      " AS " + DialerSearchViewColumns.JIAN_PINYIN + "," +
      "mst_dialer_search.raw_contact_id" + 
      " AS " + DialerSearchViewColumns.TIMES_CONTACTED + "," +
      MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.JIAN_T9 + 
      " AS " + DialerSearchViewColumns.JIAN_T9 + "," +
      MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.QUAN_T9 + 
      " AS " + DialerSearchViewColumns.QUAN_T9 + "," +
      MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.MATCH_MAP + 
      " AS " + DialerSearchViewColumns.MATCH_MAP + "," +
      MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.MATCH_MAP_JIAN + 
      " AS " + DialerSearchViewColumns.MATCH_MAP_JIAN + "," +
      MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.POLYPHONIC + 
      " AS " + DialerSearchViewColumns.POLYPHONIC +  "," +
      MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.SORT_KEY + 
      " AS " + DialerSearchViewColumns.SORT_KEY + "," +
      "mst_dialer_search.raw_contact_id" + 
      " AS " + DialerSearchViewColumns.HAS_PHONE_NUMBER + "," +
      "mst_dialer_search.raw_contact_id" + 
      " AS " + DialerSearchViewColumns.MIME_TYPE + "," +
      "mst_dialer_search.raw_contact_id" +
      " AS " + DialerSearchViewColumns.ACCOUNT_NAME + "," +
      "mst_dialer_search.raw_contact_id" +
      " AS " + DialerSearchViewColumns.ACCOUNT_TYPE + "," +
      "mst_dialer_search.raw_contact_id" + 
      " AS " + DialerSearchViewColumns.IN_VISIBLE_GROUP +
      "," + MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.QUAN_PINYIN_HIGHLIGHT + 
      " AS " + DialerSearchViewColumns.QUAN_PINYIN_HIGHLIGHT + "," +
      MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.MATCH_MAP_HIGHLIGHT + 
      " AS " + DialerSearchViewColumns.MATCH_MAP_HIGHLIGHT +
      " FROM " +
      MST_DIALER_SEARCH_TABLE + " LEFT JOIN " +
      "mst_useful_number where mst_dialer_search.raw_contact_id<-9999  AND mst_dialer_search.raw_contact_id=mst_useful_number.contact_id"

		/*+

		" ORDER BY " + MST_DIALER_SEARCH_TABLE + "." + DialerSearchColumns.SORT_KEY*/;

		Log.d(TAG,"createDialerSearchView,VIEW_SELECT:"+VIEW_SELECT);
		db.execSQL("CREATE VIEW " + MST_DIALER_SEARCH_VIEW + " AS " + VIEW_SELECT);
	}

	private SQLiteStatement mDialerSearchNameUpdate;
	private void updateNameForDialerSearch(SQLiteDatabase db, long rawContactId,
			String displayNamePrimary) {

		if (null == displayNamePrimary) {
			return;
		}

		if (mDialerSearchNameUpdate == null) {
			mDialerSearchNameUpdate = db.compileStatement("UPDATE "
					+ MST_DIALER_SEARCH_TABLE + " SET "
					+ DialerSearchColumns.QUAN_PINYIN + "=?,"
					+ DialerSearchColumns.JIAN_PINYIN + "=?,"
					+ DialerSearchColumns.QUAN_T9 + "=?,"
					+ DialerSearchColumns.JIAN_T9 + "=?,"
					+ DialerSearchColumns.MATCH_MAP + "=?,"
					+ DialerSearchColumns.MATCH_MAP_JIAN + "=?,"					
					+ DialerSearchColumns.POLYPHONIC + "=?,"
					+ DialerSearchColumns.SORT_KEY + "=?,"
					+ DialerSearchColumns.QUAN_PINYIN_HIGHLIGHT + "=?,"
					+ DialerSearchColumns.MATCH_MAP_HIGHLIGHT + "=?"
					+ " WHERE " + DialerSearchColumns.RAW_CONTACT_ID + "=? "
					);
		}

		bindToSqliteStatement(mDialerSearchNameUpdate, rawContactId, displayNamePrimary);
		mDialerSearchNameUpdate.execute();
	}

	private SQLiteStatement mUseNumberInsert;	
	public void insertUserNumber(SQLiteDatabase db, long contactId,
			String name,String number) {
		Log.d(TAG,"insertNameForDialerSearch");
		if (null == db||TextUtils.isEmpty(number)||TextUtils.isEmpty(name)) {
			return;
		}

		if (mUseNumberInsert == null) {
			mUseNumberInsert = db.compileStatement(
					"INSERT INTO " + MST_USEFUL_NUMBER_TABLE + "(contact_id,name,number)" +
					" VALUES (?,?,?)");
		}

		mUseNumberInsert.bindLong(1, contactId);
		mUseNumberInsert.bindString(2, name);
		mUseNumberInsert.bindString(3, number);

		try {
			mUseNumberInsert.executeInsert();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void test(SQLiteDatabase db) {
		Log.d(TAG,"test");
		if (null == db) {
			return;
		}

		SQLiteStatement testInsertStatement = db.compileStatement(
				"INSERT INTO calls (number) VALUES (?)");
		testInsertStatement.bindString(1, "000000000");

		SQLiteStatement testDeleteStatement = db.compileStatement(
				"DELETE FROM calls where number=?");
		testDeleteStatement.bindString(1, "000000000");
		try {
			testInsertStatement.executeInsert();
			testDeleteStatement.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private SQLiteStatement mDialerSearchNewRecordInsert;
	private void insertNameForDialerSearch(SQLiteDatabase db, long rawContactId,
			String displayNamePrimary) {
		Log.d(TAG,"insertNameForDialerSearch,rawcontactId:"+rawContactId+" displayNamePrimary:"+displayNamePrimary);
		if (null == displayNamePrimary) {
			return;
		}

		if (mDialerSearchNewRecordInsert == null) {
			//    	    if (!db.isOpen()) {
			//                try {
			//                    db = ContactsProvider2.mGnContactsHelper.getWritableDatabase();
			//                } catch (Exception e) {
			//                    e.printStackTrace();
			//                }
			//                
			//                Log.d(TAG, "1:try open  db.isOpen() = " + db.isOpen());
			//            }

			mDialerSearchNewRecordInsert = db.compileStatement(
					"INSERT INTO " + MST_DIALER_SEARCH_TABLE + "(" +
							DialerSearchColumns.QUAN_PINYIN + "," +
							DialerSearchColumns.JIAN_PINYIN + "," +
							DialerSearchColumns.QUAN_T9 + "," +
							DialerSearchColumns.JIAN_T9 + "," +
							DialerSearchColumns.MATCH_MAP + "," +
							DialerSearchColumns.MATCH_MAP_JIAN + "," +
							DialerSearchColumns.POLYPHONIC + "," +
							DialerSearchColumns.SORT_KEY + "," +
							DialerSearchColumns.QUAN_PINYIN_HIGHLIGHT + "," +
							DialerSearchColumns.MATCH_MAP_HIGHLIGHT + "," +
							DialerSearchColumns.RAW_CONTACT_ID +
							")" +
					" VALUES (?,?,?,?,?,?,?,?,?,?,?)");
		}

		//Do not insert name now, update it later for both name and alternative name.
		bindToSqliteStatement(mDialerSearchNewRecordInsert, rawContactId, displayNamePrimary);

		Log.d(TAG, "db.isOpen() = " + db.isOpen());
		try {
			mDialerSearchNewRecordInsert.executeInsert();
		} catch (Exception e) {
			e.printStackTrace();
			//    	    if (!db.isOpen()) {
			//                try {
			//                    db = ContactsProvider2.mGnContactsHelper.getWritableDatabase();
			//                    Log.d(TAG, "2:try open  db.isOpen() = " + db.isOpen());
			//                    if (db.isOpen()) {
			//                        mDialerSearchNewRecordInsert.executeInsert();
			//                    }
			//                } catch (Exception ex) {
			//                    ex.printStackTrace();
			//                }
			//            }
		}
	}

	public final char POLYPHONIC_SEPARATOR = ChineseToPinyinHelper.POLYPHONIC_SEPARATOR;
	public final String POLYPHONIC_SEPARATOR_STR = ChineseToPinyinHelper.POLYPHONIC_SEPARATOR_STR;

	private void bindToSqliteStatement(SQLiteStatement sqliteStatement,
			long rawContactId, String displayNamePrimary) {
		Log.d(TAG,"bindToSqliteStatement,rawcontactId:"+rawContactId+" displayNameprimary:"+displayNamePrimary);
		String[][] pinyinArrays = ChineseToPinyinHelper.translateMulti(displayNamePrimary, true);
		String[][] pinyinArraysHighlight = ChineseToPinyinHelper.translateMulti(displayNamePrimary.toUpperCase(), false);

		if (pinyinArrays.length == 1) {
			String[] pinyinArray = pinyinArrays[0];
			String quanPinyin = getQuanPinyin(pinyinArray);
			sqliteStatement.bindString(1, quanPinyin);
			sqliteStatement.bindString(2, getJianPinyin(pinyinArray));
			sqliteStatement.bindString(3, getQuanT9(quanPinyin));
			sqliteStatement.bindString(4, getJianT9(pinyinArray));
			Log.d(TAG,"quanpin1:"+quanPinyin+" jian1:"+ getJianPinyin(pinyinArray));
			/*
            sqliteStatement.bindString(5, getMatchMapQuan(pinyinArray));
            sqliteStatement.bindString(6, getMatchMapJian(pinyinArray));
            sqliteStatement.bindLong(7, 0);
            sqliteStatement.bindString(8, quanPinyin.toLowerCase());
			 */
			sqliteStatement.bindString(5, getMatchMapQuan(pinyinArray));
			sqliteStatement.bindString(6, getMatchMapJianHighlight(pinyinArraysHighlight[0]));
			sqliteStatement.bindLong(7, 0);			
			sqliteStatement.bindString(8, quanPinyin.toLowerCase());
			Log.d(TAG,"quanPinyin.toLowerCase():"+quanPinyin.toLowerCase());
			sqliteStatement.bindString(9, getQuanPinyin(pinyinArraysHighlight[0]));
			sqliteStatement.bindString(10, getMatchMapQuanHighlight(pinyinArraysHighlight[0]));

		} else {
			StringBuilder quanPinyin = new StringBuilder();
			StringBuilder jianPinyin = new StringBuilder();
			StringBuilder quanT9 = new StringBuilder();
			StringBuilder jianT9 = new StringBuilder();
			StringBuilder matchMapQuan = new StringBuilder();
			StringBuilder matchMapJian = new StringBuilder();
			StringBuilder sortKey = new StringBuilder();

			StringBuilder quanPinyinHighlight = new StringBuilder();
			StringBuilder matchMapQuanHighlight = new StringBuilder();

			for (String[] pinyinArray : pinyinArrays) {
				String pinyin = getQuanPinyin(pinyinArray);

				quanPinyin.append(pinyin).append(POLYPHONIC_SEPARATOR);				
				jianPinyin.append(getJianPinyin(pinyinArray)).append(POLYPHONIC_SEPARATOR);
				quanT9.append(getQuanT9(pinyin)).append(POLYPHONIC_SEPARATOR);
				jianT9.append(getJianT9(pinyinArray)).append(POLYPHONIC_SEPARATOR);
				matchMapQuan.append(getMatchMapQuan(pinyinArray)).append(POLYPHONIC_SEPARATOR);
				/*
				matchMapJian.append(getMatchMapJian(pinyinArray)).append(POLYPHONIC_SEPARATOR);
				 */
				sortKey.append(pinyin.toLowerCase()).append(POLYPHONIC_SEPARATOR);
			}

			for (String[] pinyinArrayHighlight : pinyinArraysHighlight) {
				String pinyin = getQuanPinyin(pinyinArrayHighlight);
				quanPinyinHighlight.append(pinyin).append(POLYPHONIC_SEPARATOR);
				matchMapQuanHighlight.append(getMatchMapQuanHighlight(pinyinArrayHighlight)).append(POLYPHONIC_SEPARATOR);
				matchMapJian.append(getMatchMapJianHighlight(pinyinArrayHighlight)).append(POLYPHONIC_SEPARATOR);
			}

			quanPinyin.setLength(quanPinyin.length()-1);
			jianPinyin.setLength(jianPinyin.length()-1);
			quanT9.setLength(quanT9.length()-1);
			jianT9.setLength(jianT9.length()-1);
			matchMapQuan.setLength(matchMapQuan.length()-1);
			matchMapJian.setLength(matchMapJian.length()-1);
			sortKey.setLength(sortKey.length()-1);

			quanPinyinHighlight.setLength(quanPinyinHighlight.length()-1);
			matchMapQuanHighlight.setLength(matchMapQuanHighlight.length()-1);

			sqliteStatement.bindString(1, quanPinyin.toString());
			sqliteStatement.bindString(2, jianPinyin.toString());
			Log.d(TAG,"quanpin2:"+quanPinyin.toString()+" jian2:"+ jianPinyin.toString());
			sqliteStatement.bindString(3, quanT9.toString());
			sqliteStatement.bindString(4, jianT9.toString());
			sqliteStatement.bindString(5, matchMapQuan.toString());
			sqliteStatement.bindString(6, matchMapJian.toString());
			sqliteStatement.bindLong(7, 1);
			sqliteStatement.bindString(8, sortKey.toString());
			sqliteStatement.bindString(9, quanPinyinHighlight.toString());
			sqliteStatement.bindString(10, matchMapQuanHighlight.toString());
		}

		sqliteStatement.bindLong(11, rawContactId);
	}

	public void updateOrInsertNameDialerSearch(SQLiteDatabase db, long rawContactId,
			String displayNamePrimary) {/*
		Log.d(TAG,"rawContactId,rawcontactId:"+rawContactId+" displayNamePrimary:"+displayNamePrimary);
		boolean recordExisted = false; 
		Cursor c = db.query(Tables.MST_DIALER_SEARCH, new String[]{BaseColumns._ID},
				DialerSearchColumns.RAW_CONTACT_ID + "=" + rawContactId,
				null, null, null, BaseColumns._ID + " LIMIT 1");
		if (null != c) {
			recordExisted = (c.getCount() > 0);
			c.close();				
		}

		if (recordExisted) {
			updateNameForDialerSearch(db, rawContactId, displayNamePrimary);
		} else {
			insertNameForDialerSearch(db, rawContactId, displayNamePrimary);
		}
	*/}

	private SQLiteStatement mDialerSearchDelete;
	public void deleteNameForDialerSearch(SQLiteDatabase db, long rawContactId) {

		if (mDialerSearchDelete == null) {
			mDialerSearchDelete = db.compileStatement(
					"DELETE FROM " + MST_DIALER_SEARCH_TABLE + 
					" WHERE " + DialerSearchColumns.RAW_CONTACT_ID + "=?");
		}
		mDialerSearchDelete.bindLong(1, rawContactId);
		mDialerSearchDelete.execute();

		updateDialerSearchDataForDelete(db, rawContactId);
	}

	public void updateDialerSearchDataForDelete(SQLiteDatabase db, long rawContactId) {
		Cursor c = db.rawQuery(
				"SELECT _id,number,data_id FROM calls WHERE raw_contact_id = "
						+ rawContactId + " GROUP BY data_id;", null);
		if (c != null) {
			Log.d(TAG,"[updateDialerSearchDataForDelete]calls count:" + c.getCount());
			while (c.moveToNext()) {
				long callId = c.getLong(c.getColumnIndex("_id"));
				String number = c.getString(c.getColumnIndex("number"));
				long dataId = c.getLong(c.getColumnIndex("data_id"));
				Log.d(TAG,"[updateDialerSearchDataForDelete]callId:" + callId
						+ "|number:" + number + "|dataId:" + dataId);
				//                String UseStrict = mUseStrictPhoneNumberComparation ? "1" : "0";
				String UseStrict = "0";
				Cursor dataCursor = null;
				//                if (PhoneNumberUtils.isUriNumber(number)) {
				//                    // M: fix CR:ALPS01763175,substitute mimetype values for mimetype_id to
				//                    // ensure mimetype_id change query still right.
				//                    dataCursor = db.rawQuery(
				//                                    "SELECT _id,raw_contact_id,contact_id FROM view_data "
				//                                            + " WHERE data1 =?" + " AND (mimetype= '" + SipAddress.CONTENT_ITEM_TYPE + "'"
				//                                            +" OR mimetype= '" + ImsCall.CONTENT_ITEM_TYPE + "')"
				//                                            + " AND raw_contact_id !=? LIMIT 1",
				//                                    new String[] {
				//                                            number, String.valueOf(rawContactId)
				//                                    });
				//                    // M: fix CR:ALPS01763175,substitute mimetype values for mimetype_id to
				//                    // ensure mimetype_id change query still right.
				//                } else {
				dataCursor = db.rawQuery(
						"SELECT _id,raw_contact_id,contact_id FROM view_data "
								+ " WHERE PHONE_NUMBERS_EQUAL(data1, '" + number + "' , "
								+ UseStrict + " )"
								+ " AND mimetype= '" + Phone.CONTENT_ITEM_TYPE + "'" + " AND raw_contact_id !=? LIMIT 1",
								new String[] {
										String.valueOf(rawContactId)
								});
				//                }
				if (dataCursor != null && dataCursor.moveToFirst()) {
					long newDataId = dataCursor.getLong(dataCursor.getColumnIndex("_id"));
					long newRawId = dataCursor.getLong(dataCursor.getColumnIndex("raw_contact_id"));
					Log.d(TAG,"[updateDialerSearchDataForDelete]newDataId:" + newDataId
							+ "|newRawId:" + newRawId);
					db.execSQL("UPDATE calls SET data_id=?, raw_contact_id=? "
							+ " WHERE data_id=?", new String[] {
									String.valueOf(newDataId),
									String.valueOf(newRawId),
									String.valueOf(dataId)
							});
					//                    db.execSQL("UPDATE dialer_search SET call_log_id=? "
					//                                + " WHERE data_id=?", new String[] {
					//                                String.valueOf(callId),
					//                                String.valueOf(newDataId)
					//                    });
				} else {
					Log.d(TAG,"[updateDialerSearchDataForDelete]update call log null.");
					db.execSQL("UPDATE calls SET data_id=null, raw_contact_id=null "
							+ "WHERE data_id=?", new String[] {
									String.valueOf(dataId)
							});
					//                    db.execSQL("UPDATE dialer_search "
					//                            + "SET data_id=-call_log_id, "
					//                            + " raw_contact_id=-call_log_id, "
					//                            + " normalized_name=?, "
					//                            + " normalized_name_alternative=? "
					//                            + " WHERE data_id =?",
					//                            new String[] {
					//                                    number, number, String.valueOf(dataId)
					//                            });

				}
				if (dataCursor != null) {
					dataCursor.close();
				}
			}
			c.close();
		}
		//        String delStr = "DELETE FROM dialer_search WHERE raw_contact_id=" + rawContactId;
		//        Log.d(TAG,"[updateDialerSearchDataForDelete]delStr:" + delStr);
		//        db.execSQL(delStr);
	}

	//*********************
	private static final char[][] T9_ARRAY = {
		{'0', '+'},
		{'1'},
		{'2', 'a', 'b', 'c', 'A', 'B', 'C'},
		{'3', 'd', 'e', 'f', 'D', 'E', 'F'},
		{'4', 'g', 'h', 'i', 'G', 'H', 'I'},
		{'5', 'j', 'k', 'l', 'J', 'K', 'L'},
		{'6', 'm', 'n', 'o', 'M', 'N', 'O'},
		{'7', 'p', 'q', 'r', 's', 'P', 'Q', 'R', 'S'},
		{'8', 't', 'u', 'v', 'T', 'U', 'V'},
		{'9', 'w', 'x', 'y', 'z', 'W', 'X', 'Y', 'Z'},
	};

	private static final int[][] RUSSIAN_ARRAY = {
		{1040,1072}, {1041,1073}, {1042,1074}, {1043,1075},
		{1044,1076}, {1045,1077}, {1046,1078}, {1047,1079},
		{1048,1080}, {1049,1081}, {1050,1082}, {1051,1083},
		{1052,1084}, {1053,1085}, {1054,1086}, {1055,1087},
		{1056,1088}, {1057,1089}, {1058,1090}, {1059,1091},
		{1060,1092}, {1061,1093}, {1062,1094}, {1063,1095},
		{1064,1096}, {1065,1097}, {1066,1098}, {1067,1099},
		{1068,1100}, {1069,1101}, {1070,1102}, {1071,1103},
	};

	private static final HashMap<Character, Character> DIALER_KEY_MAP = new HashMap<Character, Character>();
	static {
		for (int v = 0; v <= 9; ++v) {
			char value = (char)(v + '0');
			for (int j = 0, len = T9_ARRAY[v].length; j < len; ++j) {    			
				DIALER_KEY_MAP.put(T9_ARRAY[v][j], value);
			}
		}

		DIALER_KEY_MAP.put('*', '*');
		DIALER_KEY_MAP.put('+', '+');


		ChineseToPinyinHelper.setLegalCharactSet(DIALER_KEY_MAP.keySet());
	}

	public String getQuanPinyin(String[] pinyinArray) {
		if (null != pinyinArray) {
			StringBuilder sb = new StringBuilder();
			for (String str : pinyinArray) {
				sb.append(str);
			}
			return sb.toString();
		}
		return "";
	}

	private String getJianPinyin(String[] pinyinArray) {
		StringBuilder sb = new StringBuilder();
		for (String p : pinyinArray) {
			sb.append(p.charAt(0));
		}
		return sb.toString().toLowerCase();
	}

	private String getJianT9(String[] pinyinArray) {
		StringBuilder sb = new StringBuilder();
		for (String p : pinyinArray) {
			Character cValue = DIALER_KEY_MAP.get(p.charAt(0));
			if (null != cValue) {
				sb.append(cValue);	
			}
		}
		return sb.toString();
	}


	private String getQuanT9(String pinyin) {
		StringBuilder sb = new StringBuilder();
		char[] charArray = pinyin.toCharArray(); 
		for (char c : charArray) {
			Character cValue = DIALER_KEY_MAP.get(c);
			if (null != cValue) {
				sb.append(cValue);	
			}
		}
		return sb.toString();
	}

	private String getMatchMapQuan(String[] pinyinArray) {

		StringBuilder sb = new StringBuilder();
		int index = 0;
		for (String p : pinyinArray) {
			sb.append((char)index);
			index = index + p.length();
			sb.append((char)index);
		}
		return sb.toString();
	}

	private String getMatchMapJian(String[] pinyinArray) {
		StringBuilder sb = new StringBuilder();
		int index = 0;
		for (String p : pinyinArray) {
			sb.append((char)index);
			sb.append((char)(index+1));
			index = index + p.length();
		}
		return sb.toString();
	}

	private String getMatchMapQuanHighlight(String[] pinyinArray) {
		StringBuilder sb = new StringBuilder();
		int index = 0;
		for (String p : pinyinArray) {
			boolean isFuHao = firstIsFuHao(p);
			if (isFuHao) {
				index = index + p.length();
				continue;
			}

			sb.append((char)index);
			index = index + p.length();
			sb.append((char)index);
		}
		return sb.toString();
	}

	private String getMatchMapJianHighlight(String[] pinyinArray) {
		StringBuilder sb = new StringBuilder();
		int index = 0;
		for (String p : pinyinArray) {
			boolean isFuHao = firstIsFuHao(p);
			if (isFuHao) {
				index = index + p.length();
				continue;
			}

			sb.append((char)index);
			sb.append((char)(index+1));
			index = index + p.length();
		}
		return sb.toString();
	}

	private static final String SEARCH_BEGIN_STRING="mst_querystring_for_contact_search_begin";
	//联系人列表搜索 add by liyang
	public Cursor queryContacts(SQLiteDatabase db, Uri uri, String selec) {
		Log.d(TAG,"queryAbcForContacts,uri:"+uri+" selec:"+selec);



		groupCount=0;
		String uriStr=URLDecoder.decode(uri.toString()).toLowerCase();
		String originSearchKey=null;
		String searchKey=null;
		Log.d(TAG,"uriStr:"+uriStr);
		if (uriStr.indexOf("mst_contacts_search/") > 0) {
			originSearchKey = uriStr.substring(uriStr.lastIndexOf("/")+1);
		} else {
			return null;
		}	
		if(!TextUtils.isEmpty(originSearchKey)){
			ArrayList<Token> tokens = HanziToPinyin.getInstance().getTokens(originSearchKey);
			StringBuilder sb=new StringBuilder();
			for (int i = 0; i<tokens.size(); i++) {                	
				final Token token = tokens.get(i);
				sb.append(token.target);                    
			}
			searchKey=sb.toString().toLowerCase();
		}
		Log.d(TAG,"originSearchKey:"+originSearchKey+" searchKey:"+searchKey);

		//		StringBuilder hanziSearchKey=new StringBuilder();
		//		for (char c : originSearchKey.toCharArray()) {
		//			if (!('a' <= c && c <= 'z') &&!('A' <= c && c <= 'Z')&&('0' <= c && c <= '9')) {
		//				hanziSearchKey.append(c);
		//				break;
		//			}
		//		}

		int KEY_LEN = searchKey.length();		
		if(KEY_LEN<=0) return null;

		Cursor queryCursorAll = null;
		Cursor queryCursorCommonly = null;
		MatrixCursor resultCursor=null;
		List<DialerSearchViewRow> matchedList=new ArrayList<DialerSearchViewRow>();//常用联系人
		List<DialerSearchViewRow> allMatchedList=new ArrayList<DialerSearchViewRow>();	//所有联系人

		//当搜索词为空时，显示５个最常用的联系人列表
		String sortKeyCommonly=DialerSearchViewColumns.TIMES_CONTACTED +" desc";
		if(TextUtils.equals(SEARCH_BEGIN_STRING, searchKey)){
			Log.d(TAG,SEARCH_BEGIN_STRING);			

			String selectionCommonly=DialerSearchViewColumns.TIMES_CONTACTED+">0";
			queryCursorCommonly = db.query(TEMP_DIALER_SEARCH_VIEW_TABLE, 
					DialerSearchViewColumns.COLUMN_NAMES, 
					selectionCommonly,
					null, 
					"vds_contact_id", 
					null,
					sortKeyCommonly,
					"5");	
			Log.d(TAG,"queryCursorCommonly:"+queryCursorCommonly+" count:"+(queryCursorCommonly==null?0:queryCursorCommonly.getCount()));

			if (queryCursorCommonly != null && queryCursorCommonly.moveToFirst()) {
				List<DialerSearchViewRow> fullMatchedList = new LinkedList<DialerSearchViewRow>();
				do{
					DialerSearchViewRow sdr = new DialerSearchViewRow();
					sdr.read(queryCursorCommonly, "");
					addToListSort(fullMatchedList, sdr);  
				}while(queryCursorCommonly.moveToNext());
				Log.d(TAG,"fullMatchedList size:"+fullMatchedList.size());

				final int commonlyCount=(queryCursorCommonly==null?0:queryCursorCommonly.getCount());
				resultCursor = new MatrixCursor(DialerSearchResultColumns.COLUMN_NAMES){
					@Override
					public Bundle getExtras() {
						Bundle bundle=new Bundle();
						bundle.putInt("commonlyCount",commonlyCount);
						bundle.putInt("isQueryCommon", 1);
						return bundle;
					}
				};

				for (int i = 0; i < fullMatchedList.size(); i++) {
					resultCursor.addRow(fullMatchedList.get(i).getDialerSearchResultRow());
				}			

			}	
			Log.d(TAG,"resultCursor0:"+resultCursor+" count:"+(resultCursor==null?0:resultCursor.getCount()));
			if (null != queryCursorCommonly) {
				queryCursorCommonly.close();
				queryCursorCommonly = null;
			} 
			return resultCursor;
		}

		String selection=null;
		String selectionCommonly=null;

		//联系人搜索，关键词不为空时，先显示常用的匹配的联系人，再显示所有联系人（需排除最常用联系人）,最后显示群组
		selection = "((" + DialerSearchViewColumns.JIAN_PINYIN + " LIKE '%" + searchKey.charAt(0) + "%' AND "+DialerSearchViewColumns.MIME_TYPE+"=7)"
				+" OR (" +DialerSearchViewColumns.PHONE_NUMBER + " LIKE '%" + searchKey + "%' AND ("+DialerSearchViewColumns.MIME_TYPE+"=5 OR "+DialerSearchViewColumns.MIME_TYPE+"=1))"
				+" OR (" + DialerSearchViewColumns.JIAN_PINYIN + " LIKE '%" + searchKey.charAt(0) + "%' AND "+DialerSearchViewColumns.MIME_TYPE+"<=0 AND "+DialerSearchViewColumns.MIME_TYPE+">-10000))"
				;
		if(!TextUtils.isEmpty(selec)) selection=selection+selec;
		selectionCommonly=selection+" AND "+DialerSearchViewColumns.TIMES_CONTACTED+">0";
		Log.d(TAG,"queryAbc1,selection:"+selection+" selectionCommonly:"+selectionCommonly);

		queryCursorCommonly = db.query(TEMP_DIALER_SEARCH_VIEW_TABLE, 
				DialerSearchViewColumns.COLUMN_NAMES,
				selectionCommonly,
				null, 
				null, 
				null, 
				sortKeyCommonly,
				"5");	
		Log.d(TAG,"queryCursorCommonly2:"+queryCursorCommonly+" count:"+(queryCursorCommonly==null?0:queryCursorCommonly.getCount()));
		StringBuilder sb=null;
		if(queryCursorCommonly!=null&&queryCursorCommonly.getCount()>0&&queryCursorCommonly.moveToFirst()){
			sb=new StringBuilder(" AND "+DialerSearchViewColumns.CONTACT_ID+" NOT IN(");
			int count1=0;
			do{
				sb.append(queryCursorCommonly.getInt(DialerSearchViewColumns.CONTACT_ID_INDEX)+",");
			}while(queryCursorCommonly.moveToNext());
			sb.setLength(sb.length()-1);
			sb.append(")");
		}

		//如果在常用联系人中显示了，其它联系人中则不显示
		if(sb!=null) selection+=sb.toString();
		Log.d(TAG,"selection2:"+selection+" sb:"+sb);
		queryCursorAll = db.query(TEMP_DIALER_SEARCH_VIEW_TABLE, 
				DialerSearchViewColumns.COLUMN_NAMES, 
				selection,
				null,
				null, 
				null, 
				DialerSearchViewColumns.SORT_KEY+","+DialerSearchViewColumns.PHONE_NUMBER);	
		Log.d(TAG,"queryCursorAll:"+queryCursorAll+" count:"+(queryCursorAll==null?0:queryCursorAll.getCount()));


		/*if(queryCursorCommonly!=null&&queryCursorCommonly.getCount()>0){
			matchedList =dealAbc(queryCursorCommonly,searchKey,false);
			Log.d(TAG,"matchedList count:"+(matchedList==null?0:matchedList.size()));
			List<DialerSearchViewRow> matchedListAfterFilter=new ArrayList<DialerSearchViewRow>();
			for (int i = 0; i < matchedList.size(); i++) {
				Object contactIdObj = matchedList.get(i).getDialerSearchResultRow()[1];//id
				String name=(String)matchedList.get(i).getDialerSearchResultRow()[6];
				//				Object contactIdObj7 = matchedList.get(i).getDialerSearchResultRow()[7];//phonenumber or email
				boolean merge = false;
				if(TextUtils.isEmpty(name)){
					for (int j = 0; j < i; j++) {
						Object mergeContactIdObj = matchedList.get(j).getDialerSearchResultRow()[1];
						if (null != contactIdObj && null != mergeContactIdObj 
								&& contactIdObj.equals(mergeContactIdObj)
								) {
							merge = true;
							break;
						}
					}
				}
				if (!merge) {
					matchedListAfterFilter.add(matchedList.get(i));
				}
			}
			Log.d(TAG,"matchedListafterfilter count:"+(matchedListAfterFilter==null?0:matchedListAfterFilter.size()));
			matchedList=new ArrayList<DialerSearchViewRow>();
			for(int i=0;i<matchedListAfterFilter.size()&&i<5;i++){
				matchedList.add(matchedListAfterFilter.get(i));
			}
			Log.d(TAG,"matchedListResult count:"+(matchedList==null?0:matchedList.size()));
		}*/

		if(queryCursorCommonly!=null&&queryCursorCommonly.getCount()>0){
			matchedList =dealAbc(queryCursorCommonly,searchKey,originSearchKey);
			Log.d(TAG,"matchedList count:"+(matchedList==null?0:matchedList.size()));
		}

		if(queryCursorAll!=null&&queryCursorAll.getCount()>0){
			allMatchedList =dealAbc(queryCursorAll,searchKey,originSearchKey);
			Log.d(TAG,"allMatchedList count:"+(allMatchedList==null?0:allMatchedList.size()));
		}



		final int commonlyCount=(matchedList==null?0:matchedList.size());

		matchedList.addAll(allMatchedList);
		Log.d(TAG,"matchedListAddAll count:"+(matchedList==null?0:matchedList.size()));

		if (null != matchedList && matchedList.size() > 0) {
			resultCursor = new MatrixCursor(DialerSearchResultColumns.COLUMN_NAMES){
				@Override
				public Bundle getExtras() {
					Bundle bundle=new Bundle();
					bundle.putInt("commonlyCount",commonlyCount);
					bundle.putInt("groupCount", groupCount);
					return bundle;
				}
			};
			//过滤联系人相同项
			filterSameContacts(matchedList, resultCursor,null,false);			
		}

		//release
		matchedList = null;
		allMatchedList=null;	
		if (null != queryCursorAll) {
			queryCursorAll.close();
			queryCursorAll = null;
		} 
		if (null != queryCursorCommonly) {
			queryCursorCommonly.close();
			queryCursorCommonly = null;
		} 

		Log.d(TAG,"resultCursor count:"+(resultCursor==null?0:resultCursor.getCount()));
		if(resultCursor==null){
			resultCursor = new MatrixCursor(DialerSearchResultColumns.COLUMN_NAMES);
		}
		return resultCursor;
	}


	private static final String QUERY_ABC_PREFIX = "mstcontactqueryfordialerprefix";
	//	private static final String QUERY_CONTACTLIST_PREFIX = "mstquerycontactlistprefix";
	/*protected Cursor queryAbc(SQLiteDatabase db, String searchKey, String selec) {
		int KEY_LEN = searchKey.length();
		//		boolean dialSearchOnly = false;
		//		boolean single = false;
		//		if (searchKey.startsWith(QUERY_ABC_PREFIX)) {
		//			single = true;
		//			searchKey = searchKey.replaceAll(QUERY_ABC_PREFIX, "");
		//			KEY_LEN = searchKey.length();
		//		} else {
		//			dialSearchOnly = true;
		//		}

		Cursor queryCursor = null;

		//			String selection = "(" + DialerSearchViewColumns.JIAN_PINYIN + " LIKE '%" + searchKey.charAt(0) + "%')";
		String selection = "((" + DialerSearchViewColumns.JIAN_PINYIN + " LIKE '%" + searchKey.charAt(0) + "%' AND "+DialerSearchViewColumns.MIME_TYPE+"=7)"
				+" OR (" +DialerSearchViewColumns.PHONE_NUMBER + " LIKE '%" + searchKey + "%' AND "+DialerSearchViewColumns.MIME_TYPE+"=5))";
		boolean skip = false;
		if (KEY_LEN > 1) {
			SearchResultCache searchRetCache = getCache(searchKey);
			skip = (null != searchRetCache && searchRetCache.isEmpty()); 
			if (!skip) {
				if (null != searchRetCache) {
					selection = selection + " AND (" + 
							DialerSearchViewColumns.RAW_CONTACT_ID + 
							" IN (" + searchRetCache.getRawContactIds() + "))";	
				}
			}
		}

		//			if (dialSearchOnly) {
		//				selection = selection +  " AND (" + DialerSearchViewColumns.MIME_TYPE + 
		//						" = 5)"; 
		//			}
		//
		//			if (null != selec && !(selec.replaceAll(" ", "").isEmpty())) {
		//				selection = selection + " AND " + selec;
		//			}

		queryCursor = db.query(TEMP_DIALER_SEARCH_VIEW_TABLE, 
				DialerSearchViewColumns.COLUMN_NAMES, 
				selection,
				null, null, null,
				DialerSearchViewColumns.SORT_KEY);	


		List<DialerSearchViewRow> matchedList =dealAbc(queryCursor,searchKey);
		Log.d(TAG,"matchedList count:"+(matchedList==null?0:matchedList.size()));

		MatrixCursor resultCursor = new MatrixCursor(DialerSearchResultColumns.COLUMN_NAMES);
		if (null != matchedList && matchedList.size() > 0) {			
			//过滤联系人相同项
			filterSameContacts(matchedList, resultCursor);	
		}
		if (null != queryCursor) {
			queryCursor.close();
			queryCursor = null;
		}
		matchedList = null;

		//		if (KEY_LEN > 1) {
		//			SearchResultCache searchRetCache = new SearchResultCache(searchKey);
		//			if (null != matchedList) {
		//				for (DialerSearchViewRow sdr : matchedList) {
		//					searchRetCache.addId(sdr.mRawContactId);
		//				}	
		//			}
		//			writeCache(searchRetCache);
		//		}

		return resultCursor;
	}*/

	//过滤重复联系人
	private void filterSameContacts(List<DialerSearchViewRow> matchedList,MatrixCursor resultCursor,String searchKey,boolean isForDial){
		Log.d(TAG,"filterSameContacts");
		List<DialerSearchViewRow> groupMatchedList=new ArrayList<DialerSearchHelperForMst.DialerSearchViewRow>();
		for (int i = 0; i < matchedList.size(); i++) {
			boolean merge = false;
			Object contactIdObj7 = matchedList.get(i).getDialerSearchResultRow()[7];//phonenumber or email
			Object contactIdObj = matchedList.get(i).getDialerSearchResultRow()[1];//id
			boolean isNameMatch=Boolean.parseBoolean(matchedList.get(i).getDialerSearchResultRow()[11].toString());

			if(contactIdObj!=null&&Integer.parseInt(contactIdObj.toString())<=0){
				Log.d(TAG,"groupMatchedList add");
				groupMatchedList.add(matchedList.get(i));
				continue;
			}

			if(i>0){
				//				if(contactIdObj7!=null&&!TextUtils.isEmpty(contactIdObj7.toString())){
				//					Log.d(TAG,"contactIdObj7 not empty");
				//					merge=false;
				//				}else{
				//					Object mergeContactIdObj = matchedList.get(i-1).getDialerSearchResultRow()[1];
				//					Log.d(TAG,"contactIdObj:"+contactIdObj+" mergeContactIdObj:"+mergeContactIdObj
				//							+" contactIdObj7:"+contactIdObj7);
				//					if (null != contactIdObj && null != mergeContactIdObj&& contactIdObj.equals(mergeContactIdObj)){
				//						Log.d(TAG,"merge true");
				//						merge = true;
				//					}
				//				}

				Object mergeContactIdObj = matchedList.get(i-1).getDialerSearchResultRow()[1];
				//				Object mergeContactIdObj7 = matchedList.get(i-1).getDialerSearchResultRow()[7];
				Log.d(TAG,"contactIdObj:"+contactIdObj+" mergeContactIdObj:"+mergeContactIdObj
						+" contactIdObj7:"+contactIdObj7/*+" mergeContactIdObj7:"+mergeContactIdObj7*/+" isNameMatch:"+isNameMatch);
				//				if(TextUtils.isEmpty(contactIdObj7.toString())){
				//					//					if(isForDial) merge=true;
				//					//					else merge=false;
				//					merge=true;
				//				}
				//				else if (null != contactIdObj && null != mergeContactIdObj&& TextUtils.equals(contactIdObj.toString(),mergeContactIdObj.toString())
				//						&&null != mergeContactIdObj && null != mergeContactIdObj7&& TextUtils.equals(contactIdObj7.toString(),mergeContactIdObj7.toString())){
				//					Log.d(TAG,"merge true");
				//					merge = true;
				//				}

				if(isForDial){
					if (null != contactIdObj && null != mergeContactIdObj&& TextUtils.equals(contactIdObj.toString(),mergeContactIdObj.toString())
							&&TextUtils.isEmpty(contactIdObj7.toString())){
						Log.d(TAG,"merge true");
						merge = true;
					}
				}else{
					if (null != contactIdObj && null != mergeContactIdObj&&isNameMatch&& TextUtils.equals(contactIdObj.toString(),mergeContactIdObj.toString())){
						Log.d(TAG,"merge true");
						merge = true;
					}
				}
			}

			Log.d(TAG,"merge0:"+merge);
			if (merge) {
				continue;
			}

			if(searchKey!=null&&contactIdObj7!=null&&TextUtils.equals(contactIdObj7.toString(), searchKey)){
				Bundle bundle=new Bundle();
				bundle.putBoolean("isNumberMatch",true);
				resultCursor.setExtras(bundle);
				Log.d(TAG,"bundle:"+bundle);
			}
			resultCursor.addRow(matchedList.get(i).getDialerSearchResultRow());
		}

		Log.d(TAG,"groupMatchedList size:"+(groupMatchedList==null?0:groupMatchedList.size()));
		for (int i = 0; i < groupMatchedList.size(); i++) {
			resultCursor.addRow(groupMatchedList.get(i).getDialerSearchResultRow());
		}
	}



	private final int indexOf(String sourceStr, String targetStr) {
		char[] source = sourceStr.toCharArray();
		int sourceCount = sourceStr.length();
		char[] target = targetStr.toCharArray();
		int targetCount = targetStr.length();

		if (0 >= sourceCount) {
			return (targetCount == 0 ? sourceCount : -1);
		}

		if (targetCount == 0) {
			return 0;
		}

		char first = target[0];
		int max = sourceCount - targetCount;
		int specialOffset = 0;
		for (int i = 0; i <= max; i++) {
			/* Look for first character. */
			if (source[i] != first) {
				do {
					if (' ' == source[i] || '-' == source[i]) {
						specialOffset++;
					}
				} while (++i <= max && source[i] != first);
			}

			/* Found first character, now look at the rest of v2 */
			if (i <= max) {
				int j = i + 1;
				int end = j + targetCount - 1;
				for (int k = 1; j < end; j++, k++) {
					while ((' ' == source[j] || '-' == source[j]) && j < end) {
						j++;
						end++;
					}

					if (source[j] != target[k]) {
						break;
					}
				}

				if (j == end) {
					/* Found whole string. */
					return i - specialOffset;
				}
			}
		}
		return -1;
	}	

	protected Cursor queryContactId(SQLiteDatabase db, String searchKey, String selec) {
		if (searchKey == null) {
			return null;
		}

		String selection = "(" + DialerSearchViewColumns.QUAN_PINYIN + " LIKE '%" + searchKey + "%') AND "
				+ DialerSearchViewColumns.MIME_TYPE + "=5";
		String project[] = new String[] {
				DialerSearchViewColumns.CONTACT_ID,
				DialerSearchViewColumns.NAME,
				DialerSearchViewColumns.QUAN_PINYIN,
				DialerSearchViewColumns.PHONE_NUMBER,
		};

		Cursor cursor = db.query(MST_DIALER_SEARCH_VIEW, project, selection,
				null, DialerSearchViewColumns.CONTACT_ID, null, DialerSearchViewColumns.SORT_KEY);

		MatrixCursor matCursor = new MatrixCursor(project);
		ArrayList<Object[]> sortObjList = new ArrayList<Object[]>();
		String quanpin = null;
		int index = 0;
		int preEqualCount = 0;
		int prePartEqualCount = 0;
		char indexP;

		try {
			if (cursor != null && cursor.moveToFirst()) {
				do {
					boolean add = true;
					quanpin = cursor.getString(2);
					index = quanpin.indexOf(searchKey);

					String subStr = quanpin.substring(index + searchKey.length());
					if (subStr != null && subStr.length() > 0) {
						indexP = subStr.charAt(0);
						if (indexP > 'a' && indexP < 'z') {
							add = false;
						}
					}

					if (add && index >= 0) {
						//Log.e("wangth", "index = " + index + "  quanp = " + quanpin + "  searchKey = " + searchKey);
						String subStrPre = quanpin.substring(0, index);

						Object[] obj = new Object[project.length];
						for (int jj = 0; jj < project.length; jj++) {
							obj[jj] = cursor.getString(jj);
						}

						if (searchKey.equals(quanpin)) {
							sortObjList.add(preEqualCount, obj);
							preEqualCount++;
						} else if (index == 0) {
							sortObjList.add(preEqualCount + prePartEqualCount, obj);
							prePartEqualCount++;
						} else {
							sortObjList.add(obj);
						}
					}
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		for (Object obj[] : sortObjList) {
			matCursor.addRow(obj);
		}
		//
		return matCursor;
	}


	private List<DialerSearchViewRow> dealDigits(Cursor queryCursor,String searchKey){
		List<DialerSearchViewRow> matchedList = null;
		int onlyNumberMatchedStart = 0;
		MatrixCursor resultCursor = null;
		final int KEY_LEN = searchKey.length();
		if(queryCursor == null || !queryCursor.moveToFirst()) return null;
		List<DialerSearchViewRow> fullMatchedList = new LinkedList<DialerSearchViewRow>();
		List<DialerSearchViewRow> firstJianMatchedList = new LinkedList<DialerSearchViewRow>();
		List<DialerSearchViewRow> firstPartMatchedList = new LinkedList<DialerSearchViewRow>();
		List<DialerSearchViewRow> partMatchedList = new LinkedList<DialerSearchViewRow>();        	
		List<DialerSearchViewRow> numberMatchedList = new LinkedList<DialerSearchViewRow>();
		int[] numberMatchedListMark = new int[MAX_NUMBER_INDEX_START];

		do {
			Log.d(TAG,"queryDigit4");
			try {
				DialerSearchViewRow sdr = new DialerSearchViewRow();
				sdr.read(queryCursor, searchKey);
				int index = -1;
				do {
					String JIAN = sdr.mJianT9;
					String QUAN = sdr.mQuanT9;

					boolean isJianMatch = false;

					if (sdr.mJianT9Len >= KEY_LEN) {
						index = JIAN.indexOf(searchKey);
						Log.d(TAG,"mJianT9Len:"+sdr.mJianT9Len+" Key_len:"+KEY_LEN+" JIAN:"+JIAN+" searchKey:"+searchKey+" index:"+index);
						isJianMatch = (-1 != index);
					}
					Log.d(TAG,"queryDigit5,isJianMatch:"+isJianMatch);
					if (isJianMatch) {
						char[] pinyinHighlight = Arrays.copyOfRange(sdr.mMatchMapJian.toCharArray(),
								index * 2, (index + searchKey.length())*2);
						sdr.setPinyinHighlight(pinyinHighlight);    				
					} else {
						index = QUAN.indexOf(searchKey);							
					}

					Log.d(TAG,"index:"+index+" isJianMatch:"+isJianMatch);
					char[] matchMapQuan = sdr.mMatchMapQuan.toCharArray();
					char[] matchMapQuanHighlight = sdr.mMatchMapQuanHighlight.toCharArray(); // add for highlight

					if (0 == index) {
						if (!isJianMatch) {
							Log.d(TAG,"queryDigit6");
							//	    					sdr.setPinyinHighlight(new char[]{0, (char)(KEY_LEN)}); // modify for wangth highlight 20140227
							sdr.setPinyinHighlight(new char[]{matchMapQuanHighlight[0], (char)(matchMapQuanHighlight[0] + KEY_LEN)});
						}

						if (KEY_LEN == sdr.mQuanT9Len) {
							addToListSort(fullMatchedList, sdr);    					
						} else {
							if (isJianMatch) {
								addToListSort(firstJianMatchedList, sdr);    						
							} else {
								addToListSort(firstPartMatchedList, sdr);
							}
						}
					} else if (-1 != index) {
						boolean isMatch = isJianMatch;
						if (!isMatch) {
							Log.d(TAG,"queryDigit7");
							for (int i = 0, len = matchMapQuan.length; i < len; i+=2) {
								if (matchMapQuan[i] == index) {
									//	    							char[] matchOffset = new char[]{matchMapQuan[i], (char)(matchMapQuan[i] + KEY_LEN)}; // modify for wangth highlight 20140227
									char[] matchOffset = new char[]{matchMapQuanHighlight[i], (char)(matchMapQuanHighlight[i] + KEY_LEN)};
									sdr.setPinyinHighlight(matchOffset);    							
									isMatch = true;
									break;
								}
							}    					
						}
						Log.d(TAG,"queryDigit7,isMatch:"+isMatch);
						if (!isMatch) {
							index = -1;
						} else {
							addToListSort(partMatchedList, sdr);
						}
					}

					if (-1 == index) {
						Log.d(TAG,"queryDigit8");
						String[] sqlits = new String[matchMapQuan.length/2];
						for (int i = 0; i < sqlits.length; ++i) {
							sqlits[i] = QUAN.substring(matchMapQuan[i*2], matchMapQuan[i*2 + 1]);
						}

						for (int i = 0, srcLen = sdr.mQuanT9Len; i < sqlits.length; ++i) {
							String tmpSearchKey = new String(searchKey);
							if (tmpSearchKey.charAt(0) != sqlits[i].charAt(0)) {
								if (i > 0) {
									srcLen -= sqlits[i - 1].length();
									if (srcLen < KEY_LEN) {
										break;
									}	
								}
								continue;
							}

							char keyFirshChar;
							String curSqlit;
							int matchIndex = 0;
							char[] nameHighlight = new char[(matchMapQuan.length - i)*2];
							for (int j = i; j < sqlits.length; ++j) {
								curSqlit = sqlits[j];
								keyFirshChar = tmpSearchKey.charAt(0);
								if (curSqlit.charAt(0) == keyFirshChar) {
									//	    							nameHighlight[matchIndex++] = matchMapQuan[j*2];
									nameHighlight[matchIndex++] = matchMapQuanHighlight[j*2]; // modify for wangth highlight 20140227

									if (tmpSearchKey.startsWith(curSqlit)) {
										String afterSub = tmpSearchKey.substring(curSqlit.length());
										if (tmpSearchKey.length() > 1 && afterSub.length() > 0 && 
												j + 1 < sqlits.length) {
											char nextSqlitFirst = sqlits[j + 1].charAt(0);
											if (nextSqlitFirst == tmpSearchKey.charAt(1) &&
													nextSqlitFirst != afterSub.charAt(0)) {
												//        										nameHighlight[matchIndex++] = (char)(matchMapQuan[j*2] + 1);
												nameHighlight[matchIndex++] = (char)(matchMapQuanHighlight[j*2] + 1); // modify for wangth highlight 20140227
												tmpSearchKey = tmpSearchKey.substring(1);
												afterSub = null;
											}
										}
										if (null != afterSub) {
											//        									nameHighlight[matchIndex++] = matchMapQuan[j*2+1];
											nameHighlight[matchIndex++] = matchMapQuanHighlight[j*2+1]; // modify for wangth highlight 20140227
											tmpSearchKey = afterSub;
										}
									} else if (curSqlit.startsWith(tmpSearchKey)) {
										//	        							nameHighlight[matchIndex++] = (char)(matchMapQuan[j*2] + tmpSearchKey.length());
										nameHighlight[matchIndex++] = (char)(matchMapQuanHighlight[j*2] + tmpSearchKey.length()); // modify for wangth highlight 20140227
										index = nameHighlight[0];
										break;
									} else {
										//	        							nameHighlight[matchIndex++] = (char)(matchMapQuan[j*2] + 1);
										nameHighlight[matchIndex++] = (char)(matchMapQuanHighlight[j*2] + 1); // modify for wangth highlight 20140227
										tmpSearchKey = tmpSearchKey.substring(1);
									}

									if (tmpSearchKey.length() == 0) {
										index = nameHighlight[0];
										break;
									}
								} else {
									index = -1;
									break;
								}
							}
							Log.d(TAG,"queryDigit9");
							if (-1 != index) {
								sdr.setPinyinHighlight(nameHighlight);
								if (nameHighlight[0] == 0) {
									addToListSort(firstPartMatchedList, sdr);
								} else {
									addToListSort(partMatchedList, sdr);
								}
								break;
							}
						}
					}

					Log.d(TAG,"match email or phonenumber");
					if (-1 == index) {//姓名没匹配到，就去匹配电话号码或email
						index=sdr.mPhoneNumber.indexOf(searchKey);
						if(index>=0){
							Log.d(TAG,"match");
							numberMatchedList.add(sdr);
							break;
						}
					}
				} while (-1 == index && sdr.next());

				/*Log.d(TAG,"queryDigit10");
					if (-1 == index) {
						index = sdr.getDataMatchIndex();   //NumberMatched if index > -1
						if (-1 != index) {
							if (index >= MAX_NUMBER_INDEX_START) {
								index = MAX_NUMBER_INDEX_START - 1;
							}

							int size = numberMatchedList.size();
							if (sdr.mTimesContacted < 1) {
								int location = numberMatchedListMark[index];
								if (location != size) {
									numberMatchedList.add(location, sdr);
								} else {
									numberMatchedList.add(sdr);
								}
							} else {
								boolean isAdded = false;
								for (int i = 0; i < size; ++i) {
									if (sdr.mTimesContacted > numberMatchedList.get(i).mTimesContacted) {
										numberMatchedList.add(i, sdr);
										isAdded = true;                						
										break;
									}
								}
								if (!isAdded) {

									numberMatchedList.add(sdr);
								}
								index = 0;
							}	
							Log.d(TAG,"queryDigit11");
							for (int i = index; i < MAX_NUMBER_INDEX_START; ++i) {
								++numberMatchedListMark[i];
							}
						}
					 }*/
			} catch (Exception e) {
				e.printStackTrace();
			}
		} while(queryCursor.moveToNext());

		matchedList = new LinkedList<DialerSearchViewRow>();
		if (fullMatchedList.size() > 0) matchedList.addAll(fullMatchedList);	
		if(firstJianMatchedList.size()>0) matchedList.addAll(firstJianMatchedList);
		if(firstPartMatchedList.size()>0) matchedList.addAll(firstPartMatchedList);
		if(partMatchedList.size()>0) matchedList.addAll(partMatchedList);
		if(numberMatchedList.size()>0) matchedList.addAll(numberMatchedList);
		Log.d(TAG,"dealabc,fullMatchedList:"+fullMatchedList.size()+" firstJianMatchedList:"+firstJianMatchedList.size()
				+" firstPartMatchedList:"+firstPartMatchedList.size()+" partMatchedList:"+partMatchedList.size()+" numberMatchedList:"+numberMatchedList.size());

		//		if (KEY_LEN > 1) {
		//			SearchResultCache searchRetCache = new SearchResultCache(searchKey);
		//			if (null != matchedList) {
		//				for (DialerSearchViewRow sdr : matchedList) {
		//					searchRetCache.addId(sdr.mRawContactId);
		//				}
		//			}
		//			writeCache(searchRetCache);
		//		}

		return matchedList;
	}


	private List<DialerSearchViewRow> dealAbc(Cursor queryCursor,String searchKey,String originalSearchKey){

		List<DialerSearchViewRow> matchedList = null;
		int KEY_LEN = searchKey.length();
		if(queryCursor == null || !queryCursor.moveToFirst()) return null;		
		List<DialerSearchViewRow> fullMatchedList = new LinkedList<DialerSearchViewRow>();
		List<DialerSearchViewRow> firstJianMatchedList = new LinkedList<DialerSearchViewRow>();
		List<DialerSearchViewRow> firstPartMatchedList = new LinkedList<DialerSearchViewRow>();
		List<DialerSearchViewRow> partMatchedList = new LinkedList<DialerSearchViewRow>();        	
		List<DialerSearchViewRow> numberMatchedList = new LinkedList<DialerSearchViewRow>();
		List<DialerSearchViewRow> hanziMatchedList = new LinkedList<DialerSearchViewRow>();

		do {
			try {
				DialerSearchViewRow sdr = new DialerSearchViewRow();
				Log.d(TAG,"dealAbc:"+queryCursor.getString(3));
				sdr.read(queryCursor, searchKey);
				int index = -1;
				do {
					String JIAN = sdr.mJianPinyin;
					String QUAN = sdr.mSortKey;
					String NAME=sdr.mName;

					boolean isJianMatch = false;
					if (sdr.mJianT9Len >= KEY_LEN) {
						index = JIAN.indexOf(searchKey);
						isJianMatch = (-1 != index);
					}

					if (isJianMatch) {
						Log.d(TAG,"liyang0");
						char[] pinyinHighlight = Arrays.copyOfRange(sdr.mMatchMapJian.toCharArray(),
								index * 2, (index + searchKey.length())*2);
						sdr.setPinyinHighlight(pinyinHighlight);    				
					} else {
						Log.d(TAG,"liyang1");
						index = QUAN.indexOf(searchKey);
					}

					char[] matchMapQuan = sdr.mMatchMapQuan.toCharArray();
					char[] matchMapQuanHighlight = sdr.mMatchMapQuanHighlight.toCharArray(); // add for highlight

					if (0 == index) {
						Log.d(TAG,"liyang2");
						if (!isJianMatch) {
							//	    					sdr.setPinyinHighlight(new char[]{0, (char)(KEY_LEN)}); // modify for wangth highlight 20140227
							sdr.setPinyinHighlight(new char[]{matchMapQuanHighlight[0], (char)(matchMapQuanHighlight[0] + KEY_LEN)});
						}

						if(NAME.indexOf(originalSearchKey)>=0){
							addToListSort(hanziMatchedList, sdr); 
							break;
						}else{
							if (KEY_LEN == sdr.mQuanT9Len) {							
								addToListSort(fullMatchedList, sdr);   
								break;
							} else {
								if (isJianMatch) {
									Log.d(TAG,"liyang2.1");
									addToListSort(firstJianMatchedList, sdr);    	
									break;
								} else {
									Log.d(TAG,"liyang2.2");
									addToListSort(firstPartMatchedList, sdr);
									break;
								}
							}
						}
					} else if (-1 != index) {
						Log.d(TAG,"liyang3");
						boolean isMatch = isJianMatch;
						if (!isMatch) {
							Log.d(TAG,"liyang4");
							for (int i = 0, len = matchMapQuan.length; i < len; i+=2) {
								if (matchMapQuan[i] == index) {
									//	    							char[] matchOffset = new char[]{matchMapQuan[i], (char)(matchMapQuan[i] + KEY_LEN)}; // modify for wangth highlight 20140227
									char[] matchOffset = new char[]{matchMapQuanHighlight[i], (char)(matchMapQuanHighlight[i] + KEY_LEN)};
									sdr.setPinyinHighlight(matchOffset);    							
									isMatch = true;
									break;
								}
							}
						}
						if (!isMatch) {
							index = -1;
						} else {
							Log.d(TAG,"liyang5");
							if(NAME.indexOf(originalSearchKey)>=0){
								addToListSort(hanziMatchedList, sdr); 
								break;
							}else{
								addToListSort(partMatchedList, sdr);
							}
							break;
						}
					}

					if (-1 == index) {//简拼和全拼都没有匹配到
						String[] sqlits = new String[matchMapQuan.length/2];
						for (int i = 0; i < sqlits.length; ++i) {
							sqlits[i] = QUAN.substring(matchMapQuan[i*2], matchMapQuan[i*2 + 1]);
						}

						for (int i = 0, srcLen = sdr.mQuanT9Len; i < sqlits.length; ++i) {
							if (sqlits[i].isEmpty()) {
								continue;
							}

							String tmpSearchKey = new String(searchKey);
							if (tmpSearchKey.charAt(0) != sqlits[i].charAt(0)) {
								if (i > 0) {
									srcLen -= sqlits[i - 1].length();
									if (srcLen < KEY_LEN) {
										break;
									}	
								}
								continue;
							}

							char keyFirshChar;
							String curSqlit;
							int matchIndex = 0;
							char[] nameHighlight = new char[(matchMapQuan.length - i)*2];
							for (int j = i; j < sqlits.length; ++j) {
								curSqlit = sqlits[j];
								keyFirshChar = tmpSearchKey.charAt(0);
								if (curSqlit.charAt(0) == keyFirshChar) {
									//	    							nameHighlight[matchIndex++] = matchMapQuan[j*2];
									nameHighlight[matchIndex++] = matchMapQuanHighlight[j*2]; // modify for wangth highlight 20140227

									if (tmpSearchKey.startsWith(curSqlit)) {
										tmpSearchKey = tmpSearchKey.substring(curSqlit.length());
										//	        							nameHighlight[matchIndex++] = matchMapQuan[j*2+1];
										nameHighlight[matchIndex++] = matchMapQuanHighlight[j*2+1];
									} else if (curSqlit.startsWith(tmpSearchKey)) {
										//	        							nameHighlight[matchIndex++] = (char)(matchMapQuan[j*2] + tmpSearchKey.length());
										nameHighlight[matchIndex++] = (char)(matchMapQuanHighlight[j*2] + tmpSearchKey.length()); // modify for wangth highlight 20140227
										index = nameHighlight[0];
										break;
									} else {
										//	        							nameHighlight[matchIndex++] = (char)(matchMapQuan[j*2] + 1); // modify for wangth highlight 20140227
										nameHighlight[matchIndex++] = (char)(matchMapQuanHighlight[j*2] + 1);
										tmpSearchKey = tmpSearchKey.substring(1);
									}

									if (tmpSearchKey.length() == 0) {
										index = nameHighlight[0];
										break;
									}
								} else {
									index = -1;
									break;
								}
							}

							if (-1 != index) {
								sdr.setPinyinHighlight(nameHighlight);

								if(NAME.indexOf(originalSearchKey)>=0){
									addToListSort(hanziMatchedList, sdr); 
									break;
								}else{
									if (nameHighlight[0] == 0) {
										addToListSort(firstPartMatchedList, sdr);
									} else {
										addToListSort(partMatchedList, sdr);
									}
									break;
								}
							}
						}							
					}

					Log.d(TAG,"match email or phonenumber");
					if (-1 == index) {//姓名没匹配到，就去匹配电话号码或email
						index=sdr.mPhoneNumber.indexOf(searchKey);
						if(index>=0){
							Log.d(TAG,"match");

							sdr.setNameMatch(false);
							numberMatchedList.add(sdr);
							break;
						}
					}

				} while (-1 == index && sdr.next());

			} catch (Exception e) {
				e.printStackTrace();
			}
		} while(queryCursor.moveToNext());

		matchedList = new LinkedList<DialerSearchViewRow>();
		if (hanziMatchedList.size() > 0) matchedList.addAll(hanziMatchedList);	
		if (fullMatchedList.size() > 0) matchedList.addAll(fullMatchedList);	
		if(firstJianMatchedList.size()>0) matchedList.addAll(firstJianMatchedList);
		if(firstPartMatchedList.size()>0) matchedList.addAll(firstPartMatchedList);
		if(partMatchedList.size()>0) matchedList.addAll(partMatchedList);
		if(numberMatchedList.size()>0) matchedList.addAll(numberMatchedList);
		Log.d(TAG,"dealabc1,hanziMatchedList:"+hanziMatchedList.size()+" fullMatchedList:"+fullMatchedList.size()+" firstJianMatchedList:"+firstJianMatchedList.size()
				+" firstPartMatchedList:"+firstPartMatchedList.size()+" partMatchedList:"+partMatchedList.size()+" numberMatchedList:"+numberMatchedList.size());

		//		if (KEY_LEN > 1) {
		//			SearchResultCache searchRetCache = new SearchResultCache(searchKey);
		//			if (null != matchedList) {
		//				for (DialerSearchViewRow sdr : matchedList) {
		//					searchRetCache.addId(sdr.mRawContactId);
		//				}	
		//			}
		//			writeCache(searchRetCache);
		//		}

		return matchedList;
	}

	private static boolean firstIsFuHao(String tag) {
		if (tag == null) {
			return false;    
		}

		Pattern p;
		Matcher m;
		boolean result = false;

		p = Pattern.compile("\\p{Punct}");
		m = p.matcher(tag);
		if (m.find()) {
			result = true;
		}
		return result;
	}

	private static boolean hasHanZi(String tag) {
		if (tag == null) {
			return false;    
		}

		Pattern p;
		Matcher m;
		boolean result = false;

		p = Pattern.compile("[\u4e00-\u9fa5]");
		m = p.matcher(tag);
		if (m.find()) {
			result = true;
		}
		return result;
	}
}
