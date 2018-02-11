package com.android.dialer.list;

//import mst.widget.SliderView;
//import mst.widget.SliderLayout;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import java.util.ArrayList;
import java.util.HashMap;
import com.android.contacts.common.mst.DensityUtil;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import mst.provider.CallLog.Calls;
import mst.provider.ContactsContract.CommonDataKinds.Phone;
import mst.provider.ContactsContract.Contacts;
import mst.provider.ContactsContract.QuickContact;
import android.provider.MstContactsContract.DialerSearch;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.BidiFormatter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.ContactPhotoManager.DefaultImageRequest;
import com.android.contacts.common.GeoUtil;
import com.android.contacts.common.format.TextHighlighter;
import com.android.contacts.common.list.ContactListItemView;
import com.android.contacts.common.list.PhoneNumberListAdapter;
import com.android.dialer.CallDetailActivity;
import com.android.dialer.PhoneCallDetails;
import com.android.dialer.R;
import com.android.dialer.calllog.CallLogAdapter;
import com.android.dialer.calllog.CallTypeIconsView;
import com.android.dialer.calllog.ContactInfoHelper;
import com.android.dialer.calllog.IntentProvider;
import com.android.dialer.calllog.PhoneAccountUtils;
import com.android.dialer.calllog.PhoneNumberDisplayUtil;
import com.android.dialer.calllog.TelecomCallLogCache;
import com.android.dialer.util.DialerUtils;
import com.android.dialer.util.IntentUtil;
import com.android.dialer.util.PhoneNumberUtil;

import com.mediatek.dialer.util.DialerFeatureOptions;
import com.mediatek.dialer.util.DialerSearchUtils;
import com.android.contacts.common.mst.DialerSearchHelperForMst;
import com.android.contacts.common.mst.DialerSearchHelperForMst.DialerSearchResultColumnsForMst;

/**
 * {@link PhoneNumberListAdapter} with the following added shortcuts, that are displayed as list
 * items:
 * 1) Directly calling the phone number query
 * 2) Adding the phone number query to a contact
 *
 * These shortcuts can be enabled or disabled to toggle whether or not they show up in the
 * list.
 */
public class DialerPhoneNumberListAdapter extends PhoneNumberListAdapter {

	private String mFormattedQueryString;
	private String mCountryIso;
	private boolean isMultiSim;
	public final static int SHORTCUT_INVALID = -1;
	public final static int SHORTCUT_DIRECT_CALL = 0;
	public final static int SHORTCUT_CREATE_NEW_CONTACT = 1;
	public final static int SHORTCUT_ADD_TO_EXISTING_CONTACT = 2;
	public final static int SHORTCUT_SEND_SMS_MESSAGE = 3;
	public final static int SHORTCUT_MAKE_VIDEO_CALL = 4;

	public final static int SHORTCUT_COUNT = 5;

	private final boolean[] mShortcutEnabled = new boolean[SHORTCUT_COUNT];

	private final BidiFormatter mBidiFormatter = BidiFormatter.getInstance();
	/**
	 * Drawable representing an incoming answered call.
	 */
	public final Drawable incoming;

	/**
	 * Drawable respresenting an outgoing call.
	 */
	public final Drawable outgoing;

	/**
	 * Drawable representing an incoming missed call.
	 */
	public final Drawable missed;
	private final String callLogString;
	private final String usefulNumbersString;
	private final String contactsListString;
	public DialerPhoneNumberListAdapter(Context context) {
		super(context);

		mCountryIso = GeoUtil.getCurrentCountryIso(context);

		/// M: [MTK Dialer Search] @{
		mPhoneNumberUtils = new TelecomCallLogCache(context);
		if (DialerFeatureOptions.isDialerSearchEnabled()) {
			//			initResources(context);
		}

		incoming =context.getDrawable(R.drawable.mst_in_call_icon);
		outgoing = context.getDrawable(R.drawable.mst_out_call_icon);
		missed = context.getDrawable(R.drawable.mst_in_call_missed_icon);
		isMultiSim=isMsimIccCardActive();
		callLogString=context.getString(R.string.mst_call_logs);
		usefulNumbersString=context.getString(R.string.mst_useful_number);
		contactsListString=context.getString(R.string.contactsList);
		/// @}
	}

	public boolean isMsimIccCardActive() {
		if (isMultiSimEnabledMms()) {
			if (isIccCardActivated(0) && isIccCardActivated(1)) {
				return true;
			}
		}
		return false;
	}
	public boolean isIccCardActivated(int subscription) {
		TelephonyManager tm = TelephonyManager.getDefault();
		//		log("isIccCardActivated subscription " + tm.getSimState(subscription));
		return (tm.getSimState(subscription) != TelephonyManager.SIM_STATE_ABSENT)
				&& (tm.getSimState(subscription) != TelephonyManager.SIM_STATE_UNKNOWN);
	}
	public boolean isMultiSimEnabledMms() {
		return TelephonyManager.getDefault().isMultiSimEnabled();
	}

	@Override
	public int getCount() {
		return super.getCount() + getShortcutCount();
	}

	/**
	 * @return The number of enabled shortcuts. Ranges from 0 to a maximum of SHORTCUT_COUNT
	 */
	public int getShortcutCount() {
		int count = 0;
		for (int i = 0; i < mShortcutEnabled.length; i++) {
			if (mShortcutEnabled[i]) count++;
		}
		return count;
	}

	public void disableAllShortcuts() {
		for (int i = 0; i < mShortcutEnabled.length; i++) {
			mShortcutEnabled[i] = false;
		}
	}

	@Override
	public int getItemViewType(int position) {
		final int shortcut = getShortcutTypeFromPosition(position);
		if (shortcut >= 0) {
			// shortcutPos should always range from 1 to SHORTCUT_COUNT
			return super.getViewTypeCount() + shortcut;
		} else {
			return super.getItemViewType(position);
		}
	}

	@Override
	public int getViewTypeCount() {
		// Number of item view types in the super implementation + 2 for the 2 new shortcuts
		return super.getViewTypeCount() + SHORTCUT_COUNT;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int shortcutType = getShortcutTypeFromPosition(position);
		Log.d(TAG, "getView,position:"+position+" shortcutType:"+shortcutType);
		if (shortcutType >= 0) {
			if (convertView != null) {
				//				assignShortcutToView((ContactListItemView) convertView, shortcutType);
				assignShortcutToViewForMst(convertView, shortcutType);	
				return convertView;
			} else {
				//				final ContactListItemView v = new ContactListItemView(getContext(), null);
				//				v.setShowLeftPhoto(true);	
				//				assignShortcutToView(v, shortcutType);
				View v = View.inflate(getContext(), R.layout.mst_dialpad_shortcut_view, null);
				assignShortcutToViewForMst(v, shortcutType);				
				return v;
			}
		} else {
			return super.getView(position, convertView, parent);
		}
	}

	/**
	 * @param position The position of the item
	 * @return The enabled shortcut type matching the given position if the item is a
	 * shortcut, -1 otherwise
	 */
	public int getShortcutTypeFromPosition(int position) {
		int shortcutCount = position - super.getCount();
		if (shortcutCount >= 0) {
			// Iterate through the array of shortcuts, looking only for shortcuts where
			// mShortcutEnabled[i] is true
			for (int i = 0; shortcutCount >= 0 && i < mShortcutEnabled.length; i++) {
				if (mShortcutEnabled[i]) {
					shortcutCount--;
					if (shortcutCount < 0) return i;
				}
			}
			throw new IllegalArgumentException("Invalid position - greater than cursor count "
					+ " but not a shortcut.");
		}
		return SHORTCUT_INVALID;
	}

	@Override
	public boolean isEmpty() {
		return getShortcutCount() == 0 && super.isEmpty();
	}

	@Override
	public boolean isEnabled(int position) {
		final int shortcutType = getShortcutTypeFromPosition(position);
		if (shortcutType >= 0) {
			return true;
		} else {
			return super.isEnabled(position);
		}
	}



	private void assignShortcutToView(ContactListItemView v, int shortcutType) {
		Log.d(TAG,"assignShortcutToView");
		final CharSequence text;
		final int drawableId;
		final Resources resources = getContext().getResources();
		final String number = getFormattedQueryString();
		switch (shortcutType) {
		case SHORTCUT_DIRECT_CALL:
			text = resources.getString(
					R.string.search_shortcut_call_number,
					mBidiFormatter.unicodeWrap(number, TextDirectionHeuristics.LTR));
			drawableId = R.drawable.ic_search_phone;
			break;
		case SHORTCUT_CREATE_NEW_CONTACT:
			text = resources.getString(R.string.search_shortcut_create_new_contact);
			drawableId = R.drawable.mst_call_detail_add_to_contact_icon;
			break;
		case SHORTCUT_ADD_TO_EXISTING_CONTACT:
			text = resources.getString(R.string.search_shortcut_add_to_contact);
			drawableId = R.drawable.mst_call_detail_add_to_exist_contact_icon;
			break;
		case SHORTCUT_SEND_SMS_MESSAGE:
			text = resources.getString(R.string.search_shortcut_send_sms_message);
			drawableId = R.drawable.mst_send_sms_icon;
			break;
		case SHORTCUT_MAKE_VIDEO_CALL:
			text = resources.getString(R.string.search_shortcut_make_video_call);
			drawableId = R.drawable.ic_videocam;
			break;
		default:
			throw new IllegalArgumentException("Invalid shortcut type");
		}
		v.setDrawableResource(drawableId);
		v.setDisplayName(text);
		//		v.setNameTextViewTextColor(Color.BLACK);
		v.setPhotoPosition(super.getPhotoPosition());
		v.setAdjustSelectionBoundsEnabled(false);
	}

	private void assignShortcutToViewForMst(View v, int shortcutType) {
		Log.d(TAG,"assignShortcutToView");
		final CharSequence text;
		final int drawableId;
		final Resources resources = getContext().getResources();
		final String number = getFormattedQueryString();
		switch (shortcutType) {
		case SHORTCUT_DIRECT_CALL:
			text = resources.getString(
					R.string.search_shortcut_call_number,
					mBidiFormatter.unicodeWrap(number, TextDirectionHeuristics.LTR));
			drawableId = R.drawable.ic_search_phone;
			break;
		case SHORTCUT_CREATE_NEW_CONTACT:
			text = resources.getString(R.string.search_shortcut_create_new_contact);
			drawableId = R.drawable.mst_call_detail_add_to_contact_icon;
			break;
		case SHORTCUT_ADD_TO_EXISTING_CONTACT:
			text = resources.getString(R.string.search_shortcut_add_to_contact);
			drawableId = R.drawable.mst_call_detail_add_to_exist_contact_icon;
			break;
		case SHORTCUT_SEND_SMS_MESSAGE:
			text = resources.getString(R.string.search_shortcut_send_sms_message);
			drawableId = R.drawable.mst_send_sms_icon;
			break;
		case SHORTCUT_MAKE_VIDEO_CALL:
			text = resources.getString(R.string.search_shortcut_make_video_call);
			drawableId = R.drawable.ic_videocam;
			break;
		default:
			throw new IllegalArgumentException("Invalid shortcut type");
		}
		ImageView imageView=(ImageView)v.findViewById(R.id.mst_shortcut_image);
		TextView textView=(TextView)v.findViewById(R.id.mst_shortcut_title);	

		imageView.setBackgroundResource(drawableId);
		textView.setText(text);
	}

	/**
	 * @return True if the shortcut state (disabled vs enabled) was changed by this operation
	 */
	public boolean setShortcutEnabled(int shortcutType, boolean visible) {
		Log.d(TAG,"setShortcutEnabled,shortcutType:"+shortcutType+" visible:"+visible);
		final boolean changed = mShortcutEnabled[shortcutType] != visible;
		mShortcutEnabled[shortcutType] = visible;
		return changed;
	}

	public String getFormattedQueryString() {
		return mFormattedQueryString;
	}

	@Override
	public void setQueryString(String queryString) {
		mFormattedQueryString = PhoneNumberUtils.formatNumber(
				PhoneNumberUtils.normalizeNumber(queryString), mCountryIso);
		super.setQueryString(queryString);
	}

	/// M: [MTK Dialer Search] @{
	private final String TAG = "DialerPhoneNumberListAdapter";

	private final int VIEW_TYPE_UNKNOWN = -1;
	private final int VIEW_TYPE_CONTACT = 0;
	private final int VIEW_TYPE_CALL_LOG = 1;

	private final int NUMBER_TYPE_NORMAL = 0;
	private final int NUMBER_TYPE_UNKNOWN = 1;
	private final int NUMBER_TYPE_VOICEMAIL = 2;
	private final int NUMBER_TYPE_PRIVATE = 3;
	private final int NUMBER_TYPE_PAYPHONE = 4;
	private final int NUMBER_TYPE_EMERGENCY = 5;

	private final int DS_MATCHED_DATA_INIT_POS    = 3;
	private final int DS_MATCHED_DATA_DIVIDER     = 3;

	public final int NAME_LOOKUP_ID_INDEX        = 0;
	public final int CONTACT_ID_INDEX            = 1;
	public final int DATA_ID_INDEX               = 2;
	public final int CALL_LOG_DATE_INDEX         = 3;
	public final int CALL_LOG_ID_INDEX           = 4;
	public final int CALL_TYPE_INDEX             = 5;
	public final int CALL_GEOCODED_LOCATION_INDEX = 6;
	public final int PHONE_ACCOUNT_ID_INDEX                = 7;
	public final int PHONE_ACCOUNT_COMPONENT_NAME_INDEX     = 8;
	public final int PRESENTATION_INDEX          = 9;
	public final int INDICATE_PHONE_SIM_INDEX    = 10;
	public final int CONTACT_STARRED_INDEX       = 11;
	public final int PHOTO_ID_INDEX              = 12;
	public final int SEARCH_PHONE_TYPE_INDEX     = 13;
	public final int SEARCH_PHONE_LABEL_INDEX    = 14;
	public final int NAME_INDEX                  = 15;
	public final int SEARCH_PHONE_NUMBER_INDEX   = 16;
	public final int CONTACT_NAME_LOOKUP_INDEX   = 17;
	public final int IS_SDN_CONTACT              = 18;
	public final int DS_MATCHED_DATA_OFFSETS     = 19;
	public final int DS_MATCHED_NAME_OFFSETS     = 20;

	private ContactPhotoManager mContactPhotoManager;
	private final TelecomCallLogCache mPhoneNumberUtils;
	private PhoneNumberDisplayUtil mPhoneNumberHelper;

	private String mUnknownNumber;
	private String mPrivateNumber;
	private String mPayphoneNumber;

	private String mEmergency;

	private String mVoiceMail;

	private HashMap<Integer, Drawable> mCallTypeDrawables = new HashMap<Integer, Drawable>();

	private TextHighlighter mTextHighlighter;

	/**
	 * M: bind view for mediatek's search UI.
	 * @see com.android.contacts.common.list.PhoneNumberListAdapter
	 * #bindView(android.view.View, int, android.database.Cursor, int)
	 */
	@Override
	protected void bindView(View itemView, int partition, Cursor cursor, int position) {
		//		if (!DialerFeatureOptions.isDialerSearchEnabled()) {
		//			super.bindView(itemView, partition, cursor, position);
		//			return;
		//		}

		final int viewType = getViewType(cursor);
		Log.d(TAG,"bindview position:"+position+" viewType:"+viewType);
		switch (viewType) {
		case VIEW_TYPE_CONTACT:
			//			bindContactView(itemView, getContext(), cursor);
			bindContactViewForMst(itemView,getContext(),cursor,position);
			break;
		case VIEW_TYPE_CALL_LOG:
			bindCallLogView(itemView, getContext(), cursor,position);
			break;
		default:
			break;
		}
	}

	/**
	 * M: create item view for this feature
	 * @see com.android.contacts.common.list.PhoneNumberListAdapter
	 * #newView(android.content.Context, int, android.database.Cursor, int, android.view.ViewGroup)
	 */
	@Override
	protected View newView(Context context, int partition, Cursor cursor, int position,
			ViewGroup parent) {
		//		if (!DialerFeatureOptions.isDialerSearchEnabled()) {
		//			Log.d(TAG,"newview1");
		//			return super.newView(context, partition, cursor, position, parent);
		//		}

		//		final int viewType = getViewType(cursor);
		//		Log.d(TAG,"viewType:"+viewType);
		Log.d(TAG,"newView");
		View view = View.inflate(context, R.layout.mst_t9_search_result_list_item, null);
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.header=(TextView)view.findViewById(R.id.call_log_day_group_label);
		viewHolder.name=(TextView)view.findViewById(R.id.name);
		viewHolder.callType=(ImageView)view.findViewById(R.id.call_type_icon);
		viewHolder.simIcon=(ImageView)view.findViewById(R.id.sim_icon);
		viewHolder.number=(TextView)view.findViewById(R.id.number);
		//		viewHolder.location=(TextView)view.findViewById(R.id.call_location);
		//		viewHolder.date=(TextView)view.findViewById(R.id.call_date);
		viewHolder.itemMore=view.findViewById(R.id.item_more);
		//		viewHolder.count=(TextView)view.findViewById(R.id.call_type_count);
		//		viewHolder.call_type_icons=view.findViewById(R.id.call_type_icons);
		//		((SliderView)view.findViewById(R.id.slider_view1)).setLockDrag(true);
		viewHolder.primaryView=view.findViewById(R.id.primary_action_view);
		CheckBox checkBox=(CheckBox)view.findViewById(android.R.id.button1);
		checkBox.setVisibility(View.GONE);
		view.setTag(viewHolder);
		return view;


		/*switch (viewType) {
		case VIEW_TYPE_CONTACT:{
			Log.d(TAG,"newview2");
			View view = View.inflate(context, R.layout.mtk_dialer_search_item_view, null);
			TypedArray a = context.obtainStyledAttributes(null, R.styleable.ContactListItemView);

			view.setPadding(a.getDimensionPixelOffset(
					R.styleable.ContactListItemView_list_item_padding_left, 0),
					a.getDimensionPixelOffset(
							R.styleable.ContactListItemView_list_item_padding_top, 0),
							a.getDimensionPixelOffset(
									R.styleable.ContactListItemView_list_item_padding_right, 0),
									a.getDimensionPixelOffset(
											R.styleable.ContactListItemView_list_item_padding_bottom, 0));

			ViewHolder viewHolder = new ViewHolder();

//			viewHolder.quickContactBadge = (QuickContactBadge) view
//					.findViewById(R.id.quick_contact_photo);
			viewHolder.name = (TextView) view.findViewById(R.id.name);
			viewHolder.labelAndNumber = (TextView) view.findViewById(R.id.labelAndNumber);
			viewHolder.callInfo = (View) view.findViewById(R.id.call_info);
			viewHolder.callType = (ImageView) view.findViewById(R.id.callType);
			viewHolder.address = (TextView) view.findViewById(R.id.address);
			viewHolder.date = (TextView) view.findViewById(R.id.date);

			viewHolder.accountLabel = (TextView) view.findViewById(R.id.call_account_label);

			view.setTag(viewHolder);
			return view;
		}

		case VIEW_TYPE_CALL_LOG:{
			Log.d(TAG,"newview3");
			View view = View.inflate(context, R.layout.mtk_dialer_search_item_view, null);
			View header=view.findViewById(R.id.call_log_day_group_label);
			header.setVisibility(View.GONE);
			ViewHolder viewHolder = new ViewHolder();			
			viewHolder.name = (TextView) view.findViewById(R.id.name);			
			//			viewHolder.callType = (ImageView) view.findViewById(R.id.callType);			
			viewHolder.date = (TextView) view.findViewById(R.id.call_location);
			viewHolder.accountLabel = (TextView) view.findViewById(R.id.call_account_label);
			view.setTag(viewHolder);
			return view;
		}
		}

		return null;*/

	}

	/**
	 * M: init UI resources
	 * @param context
	 */
	private void initResources(Context context) {
		mContactPhotoManager = ContactPhotoManager.getInstance(context);
		mPhoneNumberHelper = new PhoneNumberDisplayUtil();

		//        mEmergency = context.getResources().getString(R.string.emergencycall);
		mEmergency = "Emergency";
		mVoiceMail = context.getResources().getString(R.string.voicemail);
		mPrivateNumber = context.getResources().getString(R.string.private_num);
		mPayphoneNumber = context.getResources().getString(R.string.payphone);
		mUnknownNumber = context.getResources().getString(R.string.unknown);

		// 1. incoming 2. outgoing 3. missed 4.voicemail
		// Align drawables of result items in dialer search to AOSP style.
		CallTypeIconsView.Resources resources = new CallTypeIconsView.Resources(context);
		mCallTypeDrawables.put(Calls.INCOMING_TYPE, resources.incoming);
		mCallTypeDrawables.put(Calls.OUTGOING_TYPE, resources.outgoing);
		mCallTypeDrawables.put(Calls.MISSED_TYPE, resources.missed);
		mCallTypeDrawables.put(Calls.VOICEMAIL_TYPE, resources.voicemail);
	}

	/**
	 * M: calculate view's type from cursor
	 * @param cursor
	 * @return type number
	 */
	private int getViewType(Cursor cursor) {
		int retval = VIEW_TYPE_UNKNOWN;
		//        final int contactId = cursor.getInt(CONTACT_ID_INDEX);
		//        final int callLogId = cursor.getInt(CALL_LOG_ID_INDEX);
		//
		//        Log.d(TAG, "getViewType: contactId: " + contactId + " ,callLogId: " + callLogId);
		//
		//        if (contactId > 0) {
		//            retval = VIEW_TYPE_CONTACT;
		//        } else if (callLogId > 0) {
		//            retval = VIEW_TYPE_CALL_LOG;
		//        }
		//		final String value6=cursor.getString(6);
		final String value7=cursor.getString(7);
		final String value8=cursor.getString(8);
		final String value9=cursor.getString(9);
		Log.d(TAG,"value7:"+value7+" value8:"+value8+" value9:"+value9);

		if(TextUtils.isEmpty(value7)&&TextUtils.isEmpty(value8)&&TextUtils.isEmpty(value9)){
			retval = VIEW_TYPE_CALL_LOG;
		}else{
			retval = VIEW_TYPE_CONTACT;
		}

		Log.d(TAG," value7:"+value7+" value8:"+value8);

		return retval;
	}

	/**
	 * M: bind contact view from cursor data
	 * @param view
	 * @param context
	 * @param cursor
	 */
	private void bindContactView(View view, Context context, Cursor cursor) {

		//add by lgy start
		//		final int  columnCount= cursor.getColumnCount();
		//		if(columnCount == 11) {
		//			bindContactViewForMst(view, context, cursor);
		//			return;
		//		}
		//add by lgy end

		/*final ViewHolder viewHolder = (ViewHolder) view.getTag();

		viewHolder.labelAndNumber.setVisibility(View.VISIBLE);
		viewHolder.callInfo.setVisibility(View.GONE);
		viewHolder.accountLabel.setVisibility(View.GONE);

		final String number = cursor.getString(SEARCH_PHONE_NUMBER_INDEX);
		String formatNumber = numberLeftToRight(PhoneNumberUtils.formatNumber(number, mCountryIso));
		if (formatNumber == null) {
			formatNumber = number;
		}

		final int presentation = cursor.getInt(PRESENTATION_INDEX);
		final PhoneAccountHandle accountHandle = PhoneAccountUtils.getAccount(
				cursor.getString(PHONE_ACCOUNT_COMPONENT_NAME_INDEX),
				cursor.getString(PHONE_ACCOUNT_ID_INDEX));

		final int numberType = getNumberType(accountHandle, number, presentation);

		final int labelType = cursor.getInt(SEARCH_PHONE_TYPE_INDEX);
		CharSequence label = cursor.getString(SEARCH_PHONE_LABEL_INDEX);
		int subId = cursor.getInt(INDICATE_PHONE_SIM_INDEX);
		// Get type label only if it will not be "Custom" because of an empty label.
		// So IMS contacts search item don't show lable as "Custom".
		//        if (!(labelType == Phone.TYPE_CUSTOM && TextUtils.isEmpty(label))) {
		//            /// M: for plug-in @{
		//            ICallerInfoExt callerInfoExt = (ICallerInfoExt)
		//                    MPlugin.createInstance(ICallerInfoExt.class.getName(), context);
		//            if (callerInfoExt != null) {
		//                label = callerInfoExt.getTypeLabel(context, labelType, label, null, subId);
		//            }
		//            /// @}
		//        }
		final CharSequence displayName = cursor.getString(NAME_INDEX);

		Uri contactUri = getContactUri(cursor);
		Log.d(TAG, "bindContactView111, contactUri: " + contactUri);

//		long photoId = cursor.getLong(PHOTO_ID_INDEX);
//
//		if (numberType == NUMBER_TYPE_VOICEMAIL || numberType == NUMBER_TYPE_EMERGENCY) {
//			photoId = 0;
//			viewHolder.quickContactBadge.assignContactUri(null);
//		} else {
//			viewHolder.quickContactBadge.assignContactUri(contactUri);
//		}
//		viewHolder.quickContactBadge.setOverlay(null);
//
//		if (photoId > 0) {
//			mContactPhotoManager.loadThumbnail(viewHolder.quickContactBadge, photoId, false, true,
//					null);
//		} else {
//			String identifier = cursor.getString(CONTACT_NAME_LOOKUP_INDEX);
//			DefaultImageRequest request = new DefaultImageRequest((String) displayName, identifier,
//					true);
//			//            if (subId > 0) {
//			//                request.subId = subId;
//			//                request.photoId = cursor.getInt(IS_SDN_CONTACT);
//			//            }
//			mContactPhotoManager.loadThumbnail(viewHolder.quickContactBadge, photoId, false, true,
//					request);
//		}

		if (isSpecialNumber(numberType)) {
			if (numberType == NUMBER_TYPE_VOICEMAIL || numberType == NUMBER_TYPE_EMERGENCY) {
				if (numberType == NUMBER_TYPE_VOICEMAIL) {
					viewHolder.name.setText(mVoiceMail);
				} else {
					viewHolder.name.setText(mEmergency);
				}

				viewHolder.labelAndNumber.setVisibility(View.VISIBLE);
				String highlight = getNumberHighlight(cursor);
				if (!TextUtils.isEmpty(highlight)) {
					SpannableStringBuilder style = highlightHyphen(highlight, formatNumber, number);
					viewHolder.labelAndNumber.setText(style);
				} else {
					viewHolder.labelAndNumber.setText(formatNumber);
				}
			} else {
				final String convert = specialNumberToString(numberType);
				viewHolder.name.setText(convert);
			}
		} else {
			// empty name ?
			if (!TextUtils.isEmpty(displayName)) {
				// highlight name
				String highlight = getNameHighlight(cursor);

				if (!TextUtils.isEmpty(highlight)) {
					SpannableStringBuilder style = highlightString(highlight, displayName);
					viewHolder.name.setText(style);
					if (isRegularSearch(cursor)) {
						viewHolder.name.setText(highlightName(highlight, displayName));
					}
				} else {
					viewHolder.name.setText(displayName);
				}
				// highlight number
				if (!TextUtils.isEmpty(formatNumber)) {
					highlight = getNumberHighlight(cursor);
					if (!TextUtils.isEmpty(highlight)) {
						SpannableStringBuilder style = highlightHyphen(highlight, formatNumber,
								number);
						setLabelAndNumber(viewHolder.labelAndNumber, label, style);
					} else {
						setLabelAndNumber(viewHolder.labelAndNumber, label,
								new SpannableStringBuilder(formatNumber));
					}
				} else {
					viewHolder.labelAndNumber.setVisibility(View.GONE);
				}
			} else {
				viewHolder.labelAndNumber.setVisibility(View.GONE);

				// highlight number and set number to name text view
				if (!TextUtils.isEmpty(formatNumber)) {
					final String highlight = getNumberHighlight(cursor);
					if (!TextUtils.isEmpty(highlight)) {
						SpannableStringBuilder style = highlightHyphen(highlight, formatNumber,
								number);
						viewHolder.name.setText(style);
					} else {
						viewHolder.name.setText(formatNumber);
					}
				} else {
					viewHolder.name.setVisibility(View.GONE);
				}
			}
		}*/

	}

	public static final String TYPE = "type";

	/** Call log type for incoming calls. */
	public static final int INCOMING_TYPE = 1;
	/** Call log type for outgoing calls. */
	public static final int OUTGOING_TYPE = 2;
	/** Call log type for missed calls. */
	public static final int MISSED_TYPE = 3;
	/** Call log type for voicemails. */
	public static final int VOICEMAIL_TYPE = 4;

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

	public View sendMessageView;
	public View detailsButtonView;
	/**
	 * M: Bind call log view by cursor data
	 * @param view
	 * @param context
	 * @param cursor
	 */
	private void bindCallLogView(View view, final Context context, Cursor cursor,int position) {
		Log.d(TAG,"bindCallLogView");
		final ViewHolder viewHolder = (ViewHolder) view.getTag();
		if(true){
			final String name=cursor.getString(1);
			String date=cursor.getString(2);
			final String location=cursor.getString(6);
			final int simId=cursor.getInt(5);
			final int callType=cursor.getInt(4);
			date=(String) DateUtils.getRelativeTimeSpanString(Long.parseLong(date),
					System.currentTimeMillis(),
					DateUtils.MINUTE_IN_MILLIS,
					DateUtils.FORMAT_ABBREV_RELATIVE);

			String highlight = cursor.getString(10);
			if (!TextUtils.isEmpty(highlight)) {				
				SpannableStringBuilder style = new SpannableStringBuilder(name);
				int start=name.indexOf(highlight);
				int end=start+highlight.length();
				Log.d(TAG,"start:"+start+" end:"+end);
				if(start<0) start=0;
				style.setSpan(new ForegroundColorSpan(Color.parseColor("#19A8AE")), start, end,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				viewHolder.name.setText(style);
			}else{
				viewHolder.name.setText(name);
			}
			//			viewHolder.date.setText(date);
			//			viewHolder.location.setText(location);

			final Drawable drawable = getCallTypeDrawable(callType);
			viewHolder.callType.setBackground(drawable);

			if(isMultiSim){
				int slotid=SubscriptionManager.getSlotId(simId);
				if (slotid == 1) {
					viewHolder.simIcon.setBackground(context.getDrawable(R.drawable.mst_sim2_icon));
					viewHolder.simIcon.setVisibility(View.VISIBLE);
				} else if (slotid == 0) {
					viewHolder.simIcon.setBackground(context.getDrawable(R.drawable.mst_sim1_icon));
					viewHolder.simIcon.setVisibility(View.VISIBLE);
				}else{
					viewHolder.simIcon.setVisibility(View.GONE);
				}
			}else{
				viewHolder.simIcon.setVisibility(View.GONE);
			}
			//			viewHolder.header.setVisibility(View.GONE);

			if(cursor.moveToPrevious()){
				final String value7=cursor.getString(7);
				final String value8=cursor.getString(8);
				final String value9=cursor.getString(9);
				if(TextUtils.isEmpty(value7)&&TextUtils.isEmpty(value8)&&TextUtils.isEmpty(value9)){
					viewHolder.header.setVisibility(View.GONE);
				}else{
					viewHolder.header.setText(callLogString);
					viewHolder.header.setVisibility(View.VISIBLE);
				}
				cursor.moveToNext();
			}else{
				Log.d(TAG,"move false");
				viewHolder.header.setText(callLogString);
				viewHolder.header.setVisibility(View.VISIBLE);
			}			


			viewHolder.number.setVisibility(View.GONE);
			//			viewHolder.date.setVisibility(View.VISIBLE);
			//			viewHolder.location.setVisibility(TextUtils.isEmpty(location)?View.GONE:View.VISIBLE);
			//			viewHolder.call_type_icons.setVisibility(View.GONE);
			viewHolder.callType.setVisibility(View.VISIBLE);
			viewHolder.itemMore.setVisibility(View.VISIBLE);
			viewHolder.primaryView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Log.d(TAG,"name:"+name);
					// TODO Auto-generated method stub
					final PhoneAccountHandle accountHandle=null;
					Intent intent= IntentUtil.getCallIntent(name, accountHandle);
					DialerUtils.startActivityWithErrorToast(context, intent);
				}
			});
			viewHolder.itemMore.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View view) {
					Log.d(TAG,"onclick2,"+name);
					Intent intent = new Intent(context, CallDetailActivity.class); 
					intent.putExtra(CallDetailActivity.EXTRA_NUMBER, name);
					context.startActivity(intent);
				}
			});
			return;
		}


		/*viewHolder.callInfo.setVisibility(View.VISIBLE);
		viewHolder.labelAndNumber.setVisibility(View.GONE);

		final String number = cursor.getString(SEARCH_PHONE_NUMBER_INDEX);
		String formattedNumber = numberLeftToRight(PhoneNumberUtils.formatNumber(number,
				mCountryIso));
		if (TextUtils.isEmpty(formattedNumber)) {
			formattedNumber = number;
		}

		final int presentation = cursor.getInt(PRESENTATION_INDEX);
		final PhoneAccountHandle accountHandle = PhoneAccountUtils.getAccount(
				cursor.getString(PHONE_ACCOUNT_COMPONENT_NAME_INDEX),
				cursor.getString(PHONE_ACCOUNT_ID_INDEX));

		final int numberType = getNumberType(accountHandle, number, presentation);

		final int type = cursor.getInt(CALL_TYPE_INDEX);
		final long date = cursor.getLong(CALL_LOG_DATE_INDEX);
		final int indicate = cursor.getInt(INDICATE_PHONE_SIM_INDEX);
		String geocode = cursor.getString(CALL_GEOCODED_LOCATION_INDEX);

		// create a temp contact uri for quick contact view.
		Uri contactUri = null;
		if (!TextUtils.isEmpty(number)) {
			contactUri = ContactInfoHelper.createTemporaryContactUri(number);
		}

		int contactType = ContactPhotoManager.TYPE_DEFAULT;
		if (numberType == NUMBER_TYPE_VOICEMAIL) {
			contactType = ContactPhotoManager.TYPE_VOICEMAIL;
			contactUri = null;
		}

//		viewHolder.quickContactBadge.assignContactUri(contactUri);
//		viewHolder.quickContactBadge.setOverlay(null);

		/// M: [ALPS01963857] keep call log and smart search's avatar in same color @{
		boolean isVoiceNumber = mPhoneNumberUtils.isVoicemailNumber(accountHandle, number);
		String nameForDefaultImage = mPhoneNumberHelper.getDisplayNumber(context, number,
				presentation, number,isVoiceNumber).toString();
		/// @}

		String identifier = cursor.getString(CONTACT_NAME_LOOKUP_INDEX);
		DefaultImageRequest request = new DefaultImageRequest(nameForDefaultImage, identifier,
				contactType, true);
//		mContactPhotoManager.loadThumbnail(viewHolder.quickContactBadge, 0, false, true, request);

		viewHolder.address.setText(geocode);

		if (isSpecialNumber(numberType)) {
			if (numberType == NUMBER_TYPE_VOICEMAIL || numberType == NUMBER_TYPE_EMERGENCY) {
				if (numberType == NUMBER_TYPE_VOICEMAIL) {
					viewHolder.name.setText(mVoiceMail);
				} else {
					viewHolder.name.setText(mEmergency);
				}

				String highlight = getNumberHighlight(cursor);
				if (!TextUtils.isEmpty(highlight)) {
					SpannableStringBuilder style = highlightHyphen(highlight, formattedNumber,
							number);
					viewHolder.address.setText(style);
				} else {
					viewHolder.address.setText(formattedNumber);
				}
			} else {
				final String convert = specialNumberToString(numberType);
				viewHolder.name.setText(convert);
			}
		} else {
			if (!TextUtils.isEmpty(formattedNumber)) {
				String highlight = getNumberHighlight(cursor);
				if (!TextUtils.isEmpty(highlight)) {
					SpannableStringBuilder style = highlightHyphen(highlight, formattedNumber,
							number);
					viewHolder.name.setText(style);
				} else {
					viewHolder.name.setText(formattedNumber);
				}
			}
		}

		java.text.DateFormat dateFormat = DateFormat.getTimeFormat(context);
		String dateString = dateFormat.format(date);
		viewHolder.date.setText(dateString);

		viewHolder.callType.setImageDrawable(mCallTypeDrawables.get(type));

		final String accountLabel = PhoneAccountUtils.getAccountLabel(context, accountHandle);

		if (!TextUtils.isEmpty(accountLabel)) {
			viewHolder.accountLabel.setText(accountLabel);
			/// M: [ALPS02038899] set visible in case of gone
			viewHolder.accountLabel.setVisibility(View.VISIBLE);
			// Set text color for the corresponding account.
			int color = PhoneAccountUtils.getAccountColor(context, accountHandle);
			if (color == PhoneAccount.NO_HIGHLIGHT_COLOR) {
				int defaultColor = R.color.dialtacts_secondary_text_color;
				viewHolder.accountLabel.setTextColor(context.getResources().getColor(defaultColor));
			} else {
				viewHolder.accountLabel.setTextColor(color);
			}
		} else {
			viewHolder.accountLabel.setVisibility(View.GONE);
		}*/

	}

	private int getNumberType(PhoneAccountHandle accountHandle, CharSequence number,
			int presentation) {
		int type = NUMBER_TYPE_NORMAL;
		if (presentation == Calls.PRESENTATION_UNKNOWN) {
			type = NUMBER_TYPE_UNKNOWN;
		} else if (presentation == Calls.PRESENTATION_RESTRICTED) {
			type = NUMBER_TYPE_PRIVATE;
		} else if (presentation == Calls.PRESENTATION_PAYPHONE) {
			type = NUMBER_TYPE_PAYPHONE;
		} else if (mPhoneNumberUtils.isVoicemailNumber(accountHandle, number)) {
			type = NUMBER_TYPE_VOICEMAIL;
		}
		if (PhoneNumberUtil.isLegacyUnknownNumbers(number)) {
			type = NUMBER_TYPE_UNKNOWN;
		}
		return type;
	}

	private Uri getContactUri(Cursor cursor) {
		final String lookup = cursor.getString(CONTACT_NAME_LOOKUP_INDEX);
		final int contactId = cursor.getInt(CONTACT_ID_INDEX);
		return Contacts.getLookupUri(contactId, lookup);
	}

	private boolean isSpecialNumber(int type) {
		return type != NUMBER_TYPE_NORMAL;
	}

	/**
	 * M: highlight search result string
	 * @param highlight
	 * @param target
	 * @return
	 */
	private SpannableStringBuilder highlightString(String highlight, CharSequence target) {
		SpannableStringBuilder style = new SpannableStringBuilder(target);
		int length = highlight.length();
		final int styleLength = style.length();
		int start = -1;
		int end = -1;
		for (int i = DS_MATCHED_DATA_INIT_POS; i + 1 < length; i += DS_MATCHED_DATA_DIVIDER) {
			start = (int) highlight.charAt(i);
			end = (int) highlight.charAt(i + 1) + 1;
			/// M: If highlight area is invalid, just skip it.
			if (start > styleLength || end > styleLength || start > end) {
				Log.d(TAG, "highlightString, start: " + start + " ,end: " + end
						+ " ,styleLength: " + styleLength);
				break;
			}
			style.setSpan(new StyleSpan(Typeface.BOLD), start, end,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return style;
	}

	/**
	 * M: highlight searched result name
	 * @param highlight
	 * @param target
	 * @return
	 */
	private CharSequence highlightName(String highlight, CharSequence target) {
		String highlightedPrefix = getUpperCaseQueryString();
		if (highlightedPrefix != null) {
			mTextHighlighter = new TextHighlighter(Typeface.BOLD);
			target =  mTextHighlighter.applyPrefixHighlight(target, highlightedPrefix);
		}
		return target;
	}

	/**
	 * M: highlight search result hyphen
	 * @param highlight
	 * @param target
	 * @param origin
	 * @return
	 */
	private SpannableStringBuilder highlightHyphen(String highlight, String target, String origin) {
		if (target == null) {
			Log.w(TAG, "highlightHyphen target is null");
			return null;
		}
		SpannableStringBuilder style = new SpannableStringBuilder(target);
		ArrayList<Integer> numberHighlightOffset = DialerSearchUtils
				.adjustHighlitePositionForHyphen(target, highlight
						.substring(DS_MATCHED_DATA_INIT_POS), origin);
		if (numberHighlightOffset != null && numberHighlightOffset.size() > 1) {
			style.setSpan(new StyleSpan(Typeface.BOLD), numberHighlightOffset.get(0),
					numberHighlightOffset.get(1) + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return style;
	}

	private String getNameHighlight(Cursor cursor) {
		final int index = cursor.getColumnIndex(DialerSearch.MATCHED_NAME_OFFSET);
		return index != -1 ? cursor.getString(index) : null;
	}

	private boolean isRegularSearch(Cursor cursor) {
		final int index = cursor.getColumnIndex(DialerSearch.MATCHED_DATA_OFFSET);
		String regularSearch = (index != -1 ? cursor.getString(index) : null);
		Log.d(TAG, "" + regularSearch);

		return Boolean.valueOf(regularSearch);
	}

	private String getNumberHighlight(Cursor cursor) {
		final int index = cursor.getColumnIndex(DialerSearch.MATCHED_DATA_OFFSET);
		return index != -1 ? cursor.getString(index) : null;
	}

	/**
	 * M: set label and number to view
	 * @param view
	 * @param label
	 * @param number
	 */
	private void setLabelAndNumber(TextView view, CharSequence label,
			SpannableStringBuilder number) {
		if (PhoneNumberUtils.isUriNumber(number.toString())) {
			view.setText(number);
			return;
		}
		if (TextUtils.isEmpty(label)) {
			view.setText(number);
		} else if (TextUtils.isEmpty(number)) {
			view.setText(label);
		} else {
			number.insert(0, label + " ");
			view.setText(number);
		}
	}

	private String specialNumberToString(int type) {
		switch (type) {
		case NUMBER_TYPE_UNKNOWN:
			return mUnknownNumber;
		case NUMBER_TYPE_PRIVATE:
			return mPrivateNumber;
		case NUMBER_TYPE_PAYPHONE:
			return mPayphoneNumber;
		default:
			break;
		}
		return null;
	}

	protected class ViewHolder {
		//		public QuickContactBadge quickContactBadge;
		public TextView name;
		//		public TextView labelAndNumber;
		//		public View callInfo;
		public ImageView callType;
		public ImageView simIcon;
		//		public TextView address;
		public TextView location;
		public TextView date;
		public TextView number;
		public View itemMore;
		//		public SliderLayout ;
		public View primaryView;
		public TextView count;
		//		public View call_type_icons;
		public TextView header;
		//		public TextView accountLabel;
	}

	/**
	 * M: Fix ALPS01398152, Support RTL display for Arabic/Hebrew/Urdu
	 * @param origin
	 * @return
	 */
	private String numberLeftToRight(String origin) {
		return TextUtils.isEmpty(origin) ? origin : '\u202D' + origin + '\u202C';
	}
	/// @}

	/**
	 * M: [Suggested Account] get PhoneAccountHandle via position.
	 */
	public PhoneAccountHandle getSuggestPhoneAccountHandle(int position) {
		final Cursor cursor = (Cursor)getItem(position);
		PhoneAccountHandle phoneAccountHandle = null;
		if (cursor != null) {
			phoneAccountHandle = PhoneAccountUtils.getAccount(
					cursor.getString(PHONE_ACCOUNT_COMPONENT_NAME_INDEX),
					cursor.getString(PHONE_ACCOUNT_ID_INDEX));

			long id = cursor.getLong(DATA_ID_INDEX);

			/// M: Design change for data_id(DATA_ID_INDEX).
			/// For phone number stored as contact, its data_id will be bigger than 0;
			/// For phone number not stored as contact, its data_id will be always 0.
			/// We should filter out these whose phone number stored as contacts.
			if (id > 0) {
				phoneAccountHandle = null;
			}
			return phoneAccountHandle;
		} else {
			Log.w(TAG, "Cursor was null in getPhoneAccountHandle(), return null");
			return null;
		}
	}


	//add by lgy start
	private void bindContactViewForMst(View view, final Context context, Cursor cursor,int position) {
		final ViewHolder viewHolder = (ViewHolder) view.getTag();		
		if(viewHolder==null) return;	

		final String number = cursor.getString(DialerSearchResultColumnsForMst.PHONE_NUMBER_INDEX);
		String formatNumber = PhoneNumberUtils.formatNumber(number, mCountryIso);
		Log.d(TAG,"bindContactViewForMst,formatNumber:"+formatNumber);
		if (formatNumber == null) {
			formatNumber = number;
		}

		//        final int presentation = cursor.getInt(PRESENTATION_INDEX);
		//        final PhoneAccountHandle accountHandle = PhoneAccountUtils.getAccount(
		//                cursor.getString(PHONE_ACCOUNT_COMPONENT_NAME_INDEX),
		//                cursor.getString(PHONE_ACCOUNT_ID_INDEX));
		//
		//        final int numberType = getNumberType(accountHandle, number, presentation);
		//
		//        final int labelType = cursor.getInt(SEARCH_PHONE_TYPE_INDEX);
		//        CharSequence label = cursor.getString(SEARCH_PHONE_LABEL_INDEX);
		//        int subId = cursor.getInt(INDICATE_PHONE_SIM_INDEX);
		final CharSequence displayName = cursor.getString(DialerSearchResultColumnsForMst.NAME_INDEX);

		final String lookup = cursor.getString(DialerSearchResultColumnsForMst.LOOKUP_KEY_INDEX);
		final long contactId = cursor.getLong(DialerSearchResultColumnsForMst.CONTACT_ID_INDEX);
		final Uri contactUri = Contacts.getLookupUri(contactId, lookup);
		Log.d(TAG, "bindContactViewForMst, contactUri: " + contactUri);

		//		long photoId = cursor.getLong(DialerSearchResultColumnsForMst.PHOTO_ID_INDEX);

		//        if (numberType == NUMBER_TYPE_VOICEMAIL || numberType == NUMBER_TYPE_EMERGENCY) {
		//            photoId = 0;
		//            viewHolder.quickContactBadge.assignContactUri(null);
		//        } else {
		//				viewHolder.quickContactBadge.assignContactUri(contactUri);
		//        }
		//		viewHolder.quickContactBadge.setOverlay(null);

		//		if (photoId > 0) {
		//			mContactPhotoManager.loadThumbnail(viewHolder.quickContactBadge, photoId, false, true,
		//					null);
		//		} else {
		//			String identifier = cursor.getString(DialerSearchResultColumnsForMst.LOOKUP_KEY_INDEX);
		//			DefaultImageRequest request = new DefaultImageRequest((String) displayName, identifier,
		//					true);
		//			//            if (subId > 0) {
		//			//                request.subId = subId;
		//			//                request.photoId = cursor.getInt(IS_SDN_CONTACT);
		//			//            }
		//			mContactPhotoManager.loadThumbnail(viewHolder.quickContactBadge, photoId, false, true,
		//					request);
		//		}

		//        if (isSpecialNumber(numberType)) {
		//            if (numberType == NUMBER_TYPE_VOICEMAIL || numberType == NUMBER_TYPE_EMERGENCY) {
		//                if (numberType == NUMBER_TYPE_VOICEMAIL) {
		//                    viewHolder.name.setText(mVoiceMail);
		//                } else {
		//                    viewHolder.name.setText(mEmergency);
		//                }
		//
		//                viewHolder.labelAndNumber.setVisibility(View.VISIBLE);
		//                String highlight = getNumberHighlight(cursor);
		//                if (!TextUtils.isEmpty(highlight)) {
		//                    SpannableStringBuilder style = highlightHyphen(highlight, formatNumber, number);
		//                    viewHolder.labelAndNumber.setText(style);
		//               } else {
		//                   viewHolder.labelAndNumber.setText(formatNumber);
		//                }
		//            } else {
		//                final String convert = specialNumberToString(numberType);
		//                viewHolder.name.setText(convert);
		//            }
		//        } else {
		//            // empty name ?
		//            if (!TextUtils.isEmpty(displayName)) {
		//                // highlight name
		//                String highlight = getNameHighlight(cursor);
		//                if (!TextUtils.isEmpty(highlight)) {
		//                    SpannableStringBuilder style = highlightString(highlight, displayName);
		//                    viewHolder.name.setText(style);
		//                    if (isRegularSearch(cursor)) {
		//                        viewHolder.name.setText(highlightName(highlight, displayName));
		//                    }
		//                } else {
		//                    viewHolder.name.setText(displayName);
		//                }
		//                // highlight number
		//                if (!TextUtils.isEmpty(formatNumber)) {
		//                    highlight = getNumberHighlight(cursor);
		//                    if (!TextUtils.isEmpty(highlight)) {
		//                        SpannableStringBuilder style = highlightHyphen(highlight, formatNumber,
		//                                number);
		//                        setLabelAndNumber(viewHolder.labelAndNumber, label, style);
		//                    } else {
		//                        setLabelAndNumber(viewHolder.labelAndNumber, label,
		//                                new SpannableStringBuilder(formatNumber));
		//                    }
		//                } else {
		//                    viewHolder.labelAndNumber.setVisibility(View.GONE);
		//                }
		//            } else {
		//                viewHolder.labelAndNumber.setVisibility(View.GONE);
		//
		//                // highlight number and set number to name text view
		//                if (!TextUtils.isEmpty(formatNumber)) {
		//                    final String highlight = getNumberHighlight(cursor);
		//                    if (!TextUtils.isEmpty(highlight)) {
		//                        SpannableStringBuilder style = highlightHyphen(highlight, formatNumber,
		//                                number);
		//                        viewHolder.name.setText(style);
		//                    } else {
		//                        viewHolder.name.setText(formatNumber);
		//                    }
		//                } else {
		//                    viewHolder.name.setVisibility(View.GONE);
		//                }
		//            }
		//        }

		if (!TextUtils.isEmpty(formatNumber)) {
			DialerSearchHelperForMst.highlightNumber(viewHolder.number, formatNumber, cursor);
		} else {
			viewHolder.number.setText(null);
		}

		//		if(TextUtils.equals(formatNumber, getQueryString())){
		//			Log.d(TAG,"match number");
		//			setShortcutEnabled(DialerPhoneNumberListAdapter.SHORTCUT_CREATE_NEW_CONTACT,false);
		//	    	setShortcutEnabled(DialerPhoneNumberListAdapter.SHORTCUT_ADD_TO_EXISTING_CONTACT,false);
		//		}
		DialerSearchHelperForMst.highlightName(viewHolder.name, cursor);
		viewHolder.simIcon.setVisibility(View.GONE);
		viewHolder.number.setVisibility(View.VISIBLE);
//		viewHolder.date.setVisibility(View.GONE);
		viewHolder.itemMore.setVisibility(View.VISIBLE);
		viewHolder.callType.setVisibility(View.GONE);
//		viewHolder.location.setVisibility(View.GONE);
		//		viewHolder.call_type_icons.setVisibility(View.GONE);
		//		viewHolder.primaryView.setOnClickListener(null);
		//		viewHolder.itemMore.setOnClickListener(null);

		if(contactId<=-10000){
			viewHolder.itemMore.setVisibility(View.GONE);
			if(cursor.moveToPrevious()){
				long preContactId=cursor.getLong(DialerSearchResultColumnsForMst.CONTACT_ID_INDEX);
				//				for(int i=0;i<cursor.getColumnCount();i++){
				//					Log.d(TAG,"i:"+i+" ,"+cursor.getString(i));
				//				}
				Log.d(TAG,"preContactId:"+preContactId+" contactId:"+contactId);
				if(preContactId>=0) {
					viewHolder.header.setText(usefulNumbersString);
					viewHolder.header.setVisibility(View.VISIBLE);
				}else{
					viewHolder.header.setVisibility(View.GONE);
				}
				cursor.moveToNext();
			}else{
				Log.d(TAG,"move false");
				viewHolder.header.setText(usefulNumbersString);
				viewHolder.header.setVisibility(View.VISIBLE);
			}			
		}else{
			viewHolder.itemMore.setVisibility(View.VISIBLE);
			if(position==0){
				Log.d(TAG,"0--");
				viewHolder.header.setText(contactsListString);
				viewHolder.header.setVisibility(View.VISIBLE);
			}else{
				Log.d(TAG,"1--");
				viewHolder.header.setVisibility(View.GONE);
			}
		}

		viewHolder.itemMore.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				Log.d(TAG,"onclick3");
				if(contactId>0){
					if (contactUri != null) {
						QuickContact.showQuickContact(context, viewHolder.itemMore, contactUri,
								QuickContact.MODE_LARGE, null);
					}
				}
			}
		});

		viewHolder.primaryView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d(TAG, "[onViewContactAction]contactUri = " + contactUri);
				final PhoneAccountHandle accountHandle=null;
				Intent intent= IntentUtil.getCallIntent(number, accountHandle);
				DialerUtils.startActivityWithErrorToast(context, intent);
			}
		});

	}
	//add by lgy end

}
