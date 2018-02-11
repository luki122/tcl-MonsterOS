package com.mst.t9search;

//add by liyang 2016-10-25


import org.codeaurora.internal.IExtTelephony;
import mst.provider.ContactsContract.Groups;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ServiceManager;
import android.os.RemoteException;
import com.mediatek.contacts.simcontact.SubInfoUtils;
import com.android.contacts.common.interactions.TouchPointManager;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import java.util.ArrayList;
import java.util.HashMap;
import com.android.contacts.common.mst.DensityUtil;
import com.android.contacts.common.CallUtil;
import android.R.integer;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import mst.provider.CallLog.Calls;
import mst.provider.ContactsContract.CommonDataKinds.Phone;
import mst.provider.ContactsContract.QuickContact;
import mst.provider.ContactsContract;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.ContactPhotoManager.DefaultImageRequest;
import com.android.contacts.common.GeoUtil;
import com.android.contacts.common.format.TextHighlighter;
import com.android.contacts.common.list.ContactListItemView;
import com.android.contacts.common.list.DirectoryPartition;
import com.android.contacts.common.list.PhoneNumberListAdapter;
import com.android.contacts.common.mst.DialerSearchHelperForMst;
import com.android.contacts.common.mst.DialerSearchHelperForMst.DialerSearchResultColumnsForMst;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.telephony.PhoneNumberUtils;
import android.text.BidiFormatter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.contacts.common.R;
import com.mst.t9search.Contacts;
import com.mst.t9search.ViewUtil;
import com.mst.t9search.Contacts.SearchByType;
import com.mst.t9search.ContactsHelper.CallLogQuery;

public class MstSearchContactsAdapter extends BaseAdapter {
	private Context mContext;
	private int mTextViewResourceId;
	private List<Contacts> mContacts;
	
	private String mFormattedQueryString;
	private String mCountryIso;
	private boolean isMultiSimEnabled;
	public final static int SHORTCUT_INVALID = -1;
	public final static int SHORTCUT_CREATE_NEW_CONTACT = 0;
	public final static int SHORTCUT_ADD_TO_EXISTING_CONTACT = 1;
	public final static int SHORTCUT_SEND_SMS_MESSAGE = 2;


	public final static int SHORTCUT_COUNT = 3;
	private static final String TAG = "MstSearchContactsAdapter";

	private final boolean[] mShortcutEnabled = new boolean[SHORTCUT_COUNT];

	private final BidiFormatter mBidiFormatter = BidiFormatter.getInstance();
	/**
	 * Drawable representing an incoming answered call.
	 */
	public Drawable incoming;

	/**
	 * Drawable respresenting an outgoing call.
	 */
	public Drawable outgoing;

	/**
	 * Drawable representing an incoming missed call.
	 */
	public Drawable missed;

	public void setContacts(List<Contacts> mContacts) {
		this.mContacts.clear();
		if(mContacts!=null&&mContacts.size()>0) this.mContacts.addAll(mContacts);
	}

//	private int mst_search_item_padding_left;
	private static final int PROVISIONED = 1;
	private static final int NOT_PROVISIONED = 0;
	private static final int INVALID_STATE = -1;
	private static final int CARD_NOT_PRESENT = -2;
	public static int slot0Status;//卡槽1状态
	public static int slot1Status;//卡槽2状态
	public boolean reQueryisMultiSimEnabled(){
//		Log.d(TAG,"reQueryisMultiSimEnabled");
		slot0Status=getSlotProvisionStatus(0);
		slot1Status=getSlotProvisionStatus(1);
		Log.d(TAG,"slot0Status:"+slot0Status+" slot1Status:"+slot1Status);
		if(slot0Status==1&&slot1Status==1) isMultiSimEnabled=true;
		else isMultiSimEnabled=false;
		Log.d(TAG,"isMultiSimEnabled:"+isMultiSimEnabled);
		return isMultiSimEnabled;
	}
	private static int getSlotProvisionStatus(int slot) {
		int provisionStatus = -1;
		try {
			//get current provision state of the SIM.
			IExtTelephony extTelephony =
					IExtTelephony.Stub.asInterface(ServiceManager.getService("extphone"));
			provisionStatus =  extTelephony.getCurrentUiccCardProvisioningStatus(slot);
		} catch (RemoteException ex) {
			provisionStatus = INVALID_STATE;
			Log.e(TAG,"Failed to get slotId: "+ slot +" Exception: " + ex);
		} catch (NullPointerException ex) {
			provisionStatus = INVALID_STATE;
			Log.e(TAG,"Failed to get slotId: "+ slot +" Exception: " + ex);
		}
		return provisionStatus;
	}	

	
	public MstSearchContactsAdapter(Context context, /*List<Contacts> contacts,*/boolean isForDialer) {
		mContext = context;
//		mContacts = contacts;
		this.mContacts=new ArrayList<Contacts>();
		this.isForDialer=isForDialer;
//		mst_search_item_padding_left=context.getResources().getDimensionPixelOffset(R.dimen.mst_search_item_padding_left);
		if(isForDialer){
			mCountryIso = GeoUtil.getCurrentCountryIso(context);
			incoming =context.getDrawable(R.drawable.mst_in_call_icon);
			outgoing = context.getDrawable(R.drawable.mst_out_call_icon);
			missed = context.getDrawable(R.drawable.mst_in_call_missed_icon);
			isMultiSimEnabled=reQueryisMultiSimEnabled();

			setShortcutEnabled(SHORTCUT_SEND_SMS_MESSAGE,true);
			setShortcutEnabled(SHORTCUT_CREATE_NEW_CONTACT,true);
			setShortcutEnabled(SHORTCUT_ADD_TO_EXISTING_CONTACT,true);
		}
	}
	
	private OnClickGroupListener onClickGroupListener;
	public interface OnClickGroupListener{
		public void click(long groupId);
	}
	
	public void setOnClickGroupListener(OnClickGroupListener onClickGroupListener){
		this.onClickGroupListener=onClickGroupListener;
	}

	//返回 代表某一个样式 的 数值  
	@Override
	public int getItemViewType(int position) {
//		Log.d(TAG,"getItemViewType:"+position);	
		//		if(position==0||position==contactsCount+contactsHeaderCount||position==contactsCount+callLogCount+contactsHeaderCount+callLogHeaderCount) return ITEM_HEADER;
		if(isForDialer&&(position>=mContacts.size()||mContacts.size()==0)) return ITEM_FOOTER;
		if(mContacts.get(position).getContactId()==-1) return ITEM_HEADER;
		if(!isForDialer&&mContacts.get(position).getSearchByType()==SearchByType.SearchByName) return ITEM_CONTENT_SINGLE;

		return ITEM_CONTENT_DOUBLE;
	}
	public String getFormattedQueryString() {
		return mFormattedQueryString;
	}

	public String getQueryString() {
		return mFormattedQueryString;
	}

	public void setQueryString(String queryString) {
		if(queryString.length()>22) {
			mFormattedQueryString=queryString;
			return;
		}
		mFormattedQueryString = PhoneNumberUtils.formatNumber(
				PhoneNumberUtils.normalizeNumber(queryString), mCountryIso);
	}

	private static final int ITEM_HEADER=1;
	private static final int ITEM_FOOTER=2;
	private static final int ITEM_CONTENT_SINGLE=3;
	private static final int ITEM_CONTENT_DOUBLE=4;
	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 5;
	}


	@Override
	public View getView(int position, final View convertView, final ViewGroup parent) {
		int type = getItemViewType(position);

		View view = null;
		ViewHolder viewHolder;
		ViewHolder1 viewHolder0;
		ViewHolder1 viewHolder1;
		Contacts contact=null;
		StringBuilder secondStringBuilder=new StringBuilder();
		switch (type) {
		case ITEM_HEADER:
			contact=getItem(position);
			if (convertView == null) {
				view = View.inflate(mContext, R.layout.mst_contacts_search_header, null);
				if(!isForDialer){
					((View)view.findViewById(R.id.mst_contacts_search_header_textview)).setBackgroundColor(mContext.getResources().getColor(R.color.contact_main_background));
				}
				viewHolder1 = new ViewHolder1();
				viewHolder1.name=(TextView)view.findViewById(R.id.mst_contacts_search_header_textview);
				view.setTag(viewHolder1);
			}else{
				view = convertView;
				viewHolder1 = (ViewHolder1) view.getTag();
			}
			viewHolder1.name.setText(contact.getName());
			break;

		case ITEM_CONTENT_DOUBLE:{
			contact=getItem(position);
			final String lookup=contact.getLookup();
			if (convertView == null) {
				view = View.inflate(mContext, R.layout.mst_call_log_list_item_slider, null);
//				view.setPadding(mst_search_item_padding_left, 0, 0, 0);
				viewHolder = new ViewHolder();
				viewHolder.name=(TextView)view.findViewById(R.id.name);
				viewHolder.callType=(ImageView)view.findViewById(R.id.call_type_icon);
				viewHolder.simIcon=(ImageView)view.findViewById(R.id.sim_icon);
				viewHolder.number=(TextView)view.findViewById(R.id.number);
				viewHolder.itemMore=view.findViewById(R.id.item_more);
				viewHolder.primaryView=view.findViewById(R.id.primary_action_view);
				view.setTag(viewHolder);
			}else{
				view = convertView;
				viewHolder = (ViewHolder) view.getTag();
			}

			final String number =contact.getPhoneNumber();
			String formatNumber = number==null?null:PhoneNumberUtils.formatNumber(number, mCountryIso);
			if (formatNumber == null) {
				formatNumber = number;
			}
			final String displayName = contact.getName();
			final int contactType = contact.getContactType();
			if(contactType==ContactsHelper.TYPE_CONTACTS||contactType==ContactsHelper.TYPE_FREQUENT){//联系人
				final Uri contactUri = contact.getContactUri();
				viewHolder.simIcon.setVisibility(View.GONE);
				viewHolder.callType.setVisibility(View.GONE);
				viewHolder.primaryView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if(isForDialer){
							Intent intent= getCallIntent(number);
							startActivityWithErrorToast(mContext, intent);
						}else{
							if (contactUri != null) {
							    hideInputMethod();
								QuickContact.showQuickContact(mContext, parent, contactUri,
										QuickContact.MODE_LARGE, null);
							}
						}
					}
				});

				if(isForDialer){
					viewHolder.itemMore.setVisibility(View.VISIBLE);
					viewHolder.itemMore.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							if (contactUri != null) {
							    hideInputMethod();
								QuickContact.showQuickContact(mContext, parent, contactUri,
										QuickContact.MODE_LARGE, null);
							}							
						}
					});
				}else{
					viewHolder.itemMore.setVisibility(View.GONE);
					viewHolder.itemMore.setOnClickListener(null);
				}
			}else if(contactType==ContactsHelper.TYPE_CALLLOG){
				int callType= contact.getCallType();
				int simIcon=contact.getSimIcon();
				String location=contact.getLocation();
				long date=contact.getDate();				
				String dateString=(String) DateUtils.getRelativeTimeSpanString(date,
						System.currentTimeMillis(),
						DateUtils.MINUTE_IN_MILLIS,
						DateUtils.FORMAT_ABBREV_RELATIVE);
				final Drawable drawable = getCallTypeDrawable(callType);
				viewHolder.callType.setBackground(drawable);
				if(!TextUtils.isEmpty(location)){
					secondStringBuilder.append("  "+location);
				}
				secondStringBuilder.append("  "+dateString);
//				viewHolder.number.setText(secondStringBuilder.toString());

				if(isMultiSimEnabled){
					int slotid=SubscriptionManager.getSlotId(simIcon);
					if (slotid == 1) {
						viewHolder.simIcon.setBackground(mContext.getDrawable(R.drawable.mst_sim2_icon));
						viewHolder.simIcon.setVisibility(View.VISIBLE);
					} else if (slotid == 0) {
						viewHolder.simIcon.setBackground(mContext.getDrawable(R.drawable.mst_sim1_icon));
						viewHolder.simIcon.setVisibility(View.VISIBLE);
					}else{
						viewHolder.simIcon.setVisibility(View.GONE);
					}
				}else{
					viewHolder.simIcon.setVisibility(View.GONE);
				}
				viewHolder.callType.setVisibility(View.VISIBLE);
				viewHolder.itemMore.setVisibility(View.VISIBLE);
				viewHolder.primaryView.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent intent= getCallIntent(number);
						startActivityWithErrorToast(mContext, intent);
					}
				});
				viewHolder.itemMore.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent intent = new Intent("mst.calldetail");
						intent.putExtra("EXTRA_NUMBER", number);
						mContext.startActivity(intent);
					}
				});
			}else if(contactType==ContactsHelper.TYPE_USEFULNUMBER){
				viewHolder.simIcon.setVisibility(View.GONE);
				viewHolder.callType.setVisibility(View.GONE);
				viewHolder.itemMore.setVisibility(View.GONE);
				viewHolder.primaryView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent intent= getCallIntent(number);
						startActivityWithErrorToast(mContext, intent);
					}
				});			
			}

			switch (contact.getSearchByType()) {
			case SearchByNull:
				//			ViewUtil.showTextNormal(viewHolder.mNameTv, contact.getName());
				//			ViewUtil.showTextNormal(viewHolder.mPhoneNumberTv,
				//					contact.getPhoneNumber());
				break;
			case SearchByPhoneNumber:
				ViewUtil.showTextNormal(viewHolder.name, displayName);
				ViewUtil.showTextHighlight(viewHolder.number, contactType==ContactsHelper.TYPE_CALLLOG?secondStringBuilder.toString():number, contact.getMatchKeywords().toString());
				break;
			case SearchByName:
				ViewUtil.showTextHighlight(viewHolder.name, displayName,
						contact.getMatchKeywords().toString());
				ViewUtil.showTextNormal(viewHolder.number,
						contactType==ContactsHelper.TYPE_CALLLOG?secondStringBuilder.toString():number);
				break;
			default:
				break;
			}

			break;
		}

		case ITEM_CONTENT_SINGLE:{
			contact=getItem(position);
			final String lookup=contact.getLookup();
			if (convertView == null) {
				view = View.inflate(mContext, com.mst.R.layout.list_item_1_line,null);
//				view.setPadding(mst_search_item_padding_left, 0, 0, 0);
				viewHolder0 = new ViewHolder1();
				viewHolder0.name=(TextView)view.findViewById(android.R.id.text1);				
				view.setTag(viewHolder0);
			}else{
				view = convertView;
				viewHolder0 = (ViewHolder1) view.getTag();
			}

			final String displayName = contact.getName();
			final int contactType = contact.getContactType();
			if(contactType==ContactsHelper.TYPE_CONTACTS||contactType==ContactsHelper.TYPE_FREQUENT){//联系人
				final Uri contactUri = contact.getContactUri();
				viewHolder0.name.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (contactUri != null) {
						    hideInputMethod();
							QuickContact.showQuickContact(mContext, parent, contactUri,
									QuickContact.MODE_LARGE, null);
						}
					}
				});
			}else if(contactType==ContactsHelper.TYPE_GROUP){
				final long groupId=contact.getContactId();
				viewHolder0.name.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if(onClickGroupListener!=null) onClickGroupListener.click(groupId);
					}
				});
			}

			if(contact.getMatchKeywords()!=null){
				ViewUtil.showTextHighlight(viewHolder0.name, displayName,
						contact.getMatchKeywords().toString());
			}else{
				ViewUtil.showTextNormal(viewHolder0.name, contact.getName());
			}

			break;
		}

		case ITEM_FOOTER:
			final int shortcutType = getShortcutTypeFromPosition(position);
//			Log.d(TAG,"position:"+position+" shortcutType:"+shortcutType);
			if (shortcutType >= 0) {
				if (convertView != null) {
					assignShortcutToViewForMst(convertView, shortcutType);	
					return convertView;
				} else {
					View v = View.inflate(mContext, R.layout.mst_dialpad_shortcut_view, null);
					assignShortcutToViewForMst(v, shortcutType);
					return v;
				}
			}
			break;

		default:
			break;
		}

		return view;
	}




	/**
	 * @return The number of enabled shortcuts. Ranges from 0 to a maximum of SHORTCUT_COUNT
	 */
	public int getShortcutCount() {
		int count = 0;
		if(!isForDialer) return count;
		for (int i = 0; i < mShortcutEnabled.length; i++) {
			if (mShortcutEnabled[i]) count++;
		}
		return count;
	}

	private boolean isForDialer=true;
	public void disableAllShortcuts() {
		for (int i = 0; i < mShortcutEnabled.length; i++) {
			mShortcutEnabled[i] = false;
		}
	}
	/**
	 * @param position The position of the item
	 * @return The enabled shortcut type matching the given position if the item is a
	 * shortcut, -1 otherwise
	 */
	public int getShortcutTypeFromPosition(int position) {
		int shortcutCount = position - mContacts.size();
//		Log.d(TAG,"position:"+position+" shortcutCount:"+shortcutCount);
		if (shortcutCount >= 0) {
			// Iterate through the array of shortcuts, looking only for shortcuts where
			// mShortcutEnabled[i] is true
			for (int i = 0; shortcutCount >= 0 && i < mShortcutEnabled.length; i++) {
				if (mShortcutEnabled[i]) {
					shortcutCount--;
					if (shortcutCount < 0) return i;
				}
			}
//			throw new IllegalArgumentException("Invalid position - greater than cursor count "
//					+ " but not a shortcut.");
		}
		return SHORTCUT_INVALID;
	}

	private void assignShortcutToViewForMst(View v, int shortcutType) {
//		Log.d(TAG,"assignShortcutToViewForMst,v:"+v);
		final CharSequence text;
		final int drawableId;
		final Resources resources = mContext.getResources();
		//		final String number = getFormattedQueryString();
		switch (shortcutType) {
		//		case SHORTCUT_DIRECT_CALL:
		//			text = resources.getString(
		//					R.string.search_shortcut_call_number,
		//					mBidiFormatter.unicodeWrap(number, TextDirectionHeuristics.LTR));
		//			drawableId = R.drawable.ic_search_phone;
		//			break;
		case SHORTCUT_CREATE_NEW_CONTACT:
			text = resources.getString(R.string.search_shortcut_create_new_contact);
			drawableId = R.drawable.mst_call_detail_add_to_contact_icon;
			break;
		case SHORTCUT_ADD_TO_EXISTING_CONTACT:
			text = resources.getString(R.string.search_shortcut_add_to_contact);
			drawableId = R.drawable.mst_call_detail_add_to_exist_contact_icon;
			break;
		case SHORTCUT_SEND_SMS_MESSAGE:
			text = resources.getString(R.string.mst_search_shortcut_send_sms_message);
			drawableId = R.drawable.mst_send_sms_icon;
			break;
			//		case SHORTCUT_MAKE_VIDEO_CALL:
			//			text = resources.getString(R.string.search_shortcut_make_video_call);
			//			drawableId = R.drawable.ic_videocam;
			//			break;
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
//		Log.d(TAG,"setShortcutEnabled,shortcutType:"+shortcutType+" visible:"+visible);
		final boolean changed = mShortcutEnabled[shortcutType] != visible;
		mShortcutEnabled[shortcutType] = visible;
		return changed;
	}

	public Intent getCallIntent(
		String number) {
		final Intent intent = CallUtil.getCallIntent(number);
		intent.putExtra("slot", -1);
		return intent;
	}
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
	public static void startActivityWithErrorToast(Context context, Intent intent) {
		try {
			if ((Intent.ACTION_CALL.equals(intent.getAction())
					&& context instanceof Activity)) {
				// All dialer-initiated calls should pass the touch point to the InCallUI
				Point touchPoint = TouchPointManager.getInstance().getPoint();
				if (touchPoint.x != 0 || touchPoint.y != 0) {
					Bundle extras = new Bundle();
					extras.putParcelable(TouchPointManager.TOUCH_POINT, touchPoint);
					intent.putExtra(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS, extras);
				}
				final TelecomManager tm =
						(TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
				tm.placeCall(intent.getData(), intent.getExtras());
			} else {
				context.startActivity(intent);
			}
		} catch (ActivityNotFoundException e) {
			//            Toast.makeText(context, msgId, Toast.LENGTH_SHORT).show();
		}
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
		//		public TextView header;
		//		public TextView accountLabel;
	}

	protected class ViewHolder1 {
		public TextView name;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mContacts.size()+getShortcutCount();
	}

	@Override
	public Contacts getItem(int position) {
		// TODO Auto-generated method stub
		return mContacts.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	//add by lgy for 3412328
	private Activity mActivity ;
	public void setActivity(Activity a) {
	    mActivity = a;
	}
	
	private void hideInputMethod() {
        if(mActivity == null||mActivity.getCurrentFocus()==null) {
            return;
        }
        final InputMethodManager imm = (InputMethodManager) mContext.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(), 0);
        }
    }
}
