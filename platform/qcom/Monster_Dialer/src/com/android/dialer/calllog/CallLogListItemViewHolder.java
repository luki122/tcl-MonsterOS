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

import mst.widget.SliderLayout;
import mst.widget.SliderLayout.SwipeListener;
import mst.view.menu.PopupMenu;
import mst.view.menu.PopupMenu.OnMenuItemClickListener;
import mst.view.menu.PopupMenu.OnDismissListener;
import mst.widget.SliderView;
import com.android.contacts.common.mst.DensityUtil;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import mst.provider.CallLog.Calls;
import mst.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v7.widget.CardView;
import mst.widget.recycleview.RecyclerView;
import android.telecom.PhoneAccountHandle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.QuickContactBadge;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.ContactPhotoManager.DefaultImageRequest;
import com.android.contacts.common.dialog.CallSubjectDialog;
import com.android.contacts.common.testing.NeededForTesting;
import com.android.contacts.common.util.UriUtils;
import com.android.dialer.CallDetailActivity;
import com.android.dialer.R;
import com.android.dialer.util.DialerUtils;
import com.android.dialer.util.PhoneNumberUtil;
import com.android.dialer.voicemail.VoicemailPlaybackPresenter;
import com.android.dialer.voicemail.VoicemailPlaybackLayout;

/**
 * This is an object containing references to views contained by the call log list item. This
 * improves performance by reducing the frequency with which we need to find views by IDs.
 *
 * This object also contains UI logic pertaining to the view, to isolate it from the CallLogAdapter.
 */
public final class CallLogListItemViewHolder extends RecyclerView.ViewHolder
implements View.OnClickListener,SliderView.OnSliderButtonLickListener {
	/** The root view of the call log list item */
	public final View rootView;
	/** The quick contact badge for the contact. */
	public QuickContactBadge quickContactView;
	/** The primary action view of the entry. */
	public View primaryActionView;
	/** The details of the phone call. */
	public PhoneCallDetailsViews phoneCallDetailsViews;
	/** The text of the header for a day grouping. */
	//	public final TextView dayGroupHeader;
	/** The view containing the details for the call log row, including the action buttons. */
	public CardView callLogEntryView;
	/** The actionable view which places a call to the number corresponding to the call log row. */
	//	public ImageView primaryActionButtonView;
	public View primaryActionButtonViewParent;
	public CheckBox checkBox;
	public SliderView slider;


	private boolean mIsPrivate=false;

	public boolean ismIsPrivate() {
		return mIsPrivate;
	}

	public void setmIsPrivate(boolean mIsPrivate) {
		this.mIsPrivate = mIsPrivate;
	}

	/** The view containing call log item actions.  Null until the ViewStub is inflated. */
	public View actionsView;
	/** The button views below are assigned only when the action section is expanded. */
	public VoicemailPlaybackLayout voicemailPlaybackView;
	public View callButtonView;
	public View videoCallButtonView;
	public View createNewContactButtonView;
	public View addToExistingContactButtonView;
	public View sendMessageView;
	public View detailsButtonView;
	public View callWithNoteButtonView;

	/**
	 * The row Id for the first call associated with the call log entry.  Used as a key for the
	 * map used to track which call log entries have the action button section expanded.
	 */
	public long rowId;

	/**
	 * The call Ids for the calls represented by the current call log entry.  Used when the user
	 * deletes a call log entry.
	 */
	public long[] callIds;

	/**
	 * The callable phone number for the current call log entry.  Cached here as the call back
	 * intent is set only when the actions ViewStub is inflated.
	 */
	public String number;

	public int position;

	public int slotId;

	/**
	 * The formatted phone number to display.
	 */
	public String displayNumber;

	/**
	 * The phone number presentation for the current call log entry.  Cached here as the call back
	 * intent is set only when the actions ViewStub is inflated.
	 */
	public int numberPresentation;

	/**
	 * The type of the phone number (e.g. main, work, etc).
	 */
	public String numberType;

	/**
	 * The type of call for the current call log entry.  Cached here as the call back
	 * intent is set only when the actions ViewStub is inflated.
	 */
	public int callType;

	/**
	 * The account for the current call log entry.  Cached here as the call back
	 * intent is set only when the actions ViewStub is inflated.
	 */
	public PhoneAccountHandle accountHandle;

	/**
	 * If the call has an associated voicemail message, the URI of the voicemail message for
	 * playback.  Cached here as the voicemail intent is only set when the actions ViewStub is
	 * inflated.
	 */
	public String voicemailUri;

	/**
	 * The name or number associated with the call.  Cached here for use when setting content
	 * descriptions on buttons in the actions ViewStub when it is inflated.
	 */
	public CharSequence nameOrNumber;

	/**
	 * Whether this row is for a business or not.
	 */
	public boolean isBusiness;

	/**
	 * The contact info for the contact displayed in this list item.
	 */
	public ContactInfo info;

	public Drawable typeDrawable;
	public Drawable simDrawable;

	private static final int VOICEMAIL_TRANSCRIPTION_MAX_LINES = 10;
	private static final String TAG = "CallLogListItemViewHolder";

	private final Context mContext;
	private TelecomCallLogCache mTelecomCallLogCache;
	private CallLogListItemHelper mCallLogListItemHelper;
	private VoicemailPlaybackPresenter mVoicemailPlaybackPresenter;

	private int mPhotoSize;

	private View.OnClickListener mExpandCollapseListener;
	private View.OnLongClickListener mLongClickListener;
	private boolean mVoicemailPrimaryActionButtonClicked;

	private CallLogListItemViewHolder(
			Context context,
			View.OnClickListener expandCollapseListener,
			TelecomCallLogCache telecomCallLogCache,
			CallLogListItemHelper callLogListItemHelper,
			VoicemailPlaybackPresenter voicemailPlaybackPresenter,
			View rootView,
			QuickContactBadge quickContactView,
			View primaryActionView,
			PhoneCallDetailsViews phoneCallDetailsViews,
			CardView callLogEntryView,
			//			TextView dayGroupHeader,
			//			ImageView primaryActionButtonView,
			View primaryActionButtonViewParent,
			CheckBox checkBox,
			View.OnLongClickListener mLongClickListener,
			SliderView slider) {
		super(rootView);
		this.rootView = rootView;
		mContext = context;
		if(mContext!=null){
			mExpandCollapseListener = expandCollapseListener;
			mTelecomCallLogCache = telecomCallLogCache;
			mCallLogListItemHelper = callLogListItemHelper;
			mVoicemailPlaybackPresenter = voicemailPlaybackPresenter;


			this.quickContactView = quickContactView;
			this.primaryActionView = primaryActionView;
			this.phoneCallDetailsViews = phoneCallDetailsViews;
			this.callLogEntryView = callLogEntryView;
			//		this.dayGroupHeader = dayGroupHeader;
			//			this.primaryActionButtonView = primaryActionButtonView;
			this.primaryActionButtonViewParent=primaryActionButtonViewParent;
			this.checkBox=checkBox;
			this.mLongClickListener=mLongClickListener;
			this.slider=slider;

			if(slider!=null){
				this.slider.addTextButton(1,"删除");
				this.slider.setOnSliderButtonClickListener(this);
				this.slider.setSwipeListener(new SwipeListener(){
					/**
					 * Called when the main view becomes completely closed.
					 */
					public void onClosed(SliderLayout view){
						//				swipeOpenPosition=-1;
					}

					/**
					 * Called when the main view becomes completely opened.
					 */
					public void onOpened(SliderLayout view){
						//				swipeOpenPosition=position;
					}

					/**
					 * Called when the main view's position changes.
					 * @param slideOffset The new offset of the main view within its range, from 0-1
					 */
					public void onSlide(SliderLayout view, float slideOffset){
						//				Log.d(TAG,"onSlide:"+view+" slideOffset:"+slideOffset);
					}
				});
			}
			//		this.slider.setButtonBackgroundColor(0, Color.parseColor("#ffb3b3b3"));

			//		delete_view=(ViewGroup)slider.findViewById(R.id.delete_view);
			//
			//		delete_view.setOnClickListener(this);



			Resources resources = mContext.getResources();
			mPhotoSize = mContext.getResources().getDimensionPixelSize(R.dimen.contact_photo_size);

			// Set text height to false on the TextViews so they don't have extra padding.
			//			phoneCallDetailsViews.nameView.setElegantTextHeight(false);
			//			phoneCallDetailsViews.callLocationAndDate.setElegantTextHeight(false);

			if(quickContactView!=null) quickContactView.setPrioritizedMimeType(Phone.CONTENT_ITEM_TYPE);

			//        primaryActionButtonView.setOnClickListener(this);
			primaryActionView.setOnClickListener(mExpandCollapseListener);
			Log.d(TAG,"mLongClickListener:"+mLongClickListener);
			primaryActionView.setOnLongClickListener(mLongClickListener);
			primaryActionButtonViewParent.setOnClickListener(this);
		}
	}

	public static CallLogListItemViewHolder create(
			View view,
			Context context,
			View.OnClickListener expandCollapseListener,
			TelecomCallLogCache telecomCallLogCache,
			CallLogListItemHelper callLogListItemHelper,
			VoicemailPlaybackPresenter voicemailPlaybackPresenter,
			View.OnLongClickListener mLongClickListener) {

		//        return new CallLogListItemViewHolder(
		//                context,
		//                expandCollapseListener,
		//                telecomCallLogCache,
		//                callLogListItemHelper,
		//                voicemailPlaybackPresenter,
		//                view,
		//                (QuickContactBadge) view.findViewById(R.id.quick_contact_photo),
		//                view.findViewById(R.id.primary_action_view),
		//                PhoneCallDetailsViews.fromView(view),
		//                (CardView) view.findViewById(R.id.call_log_row),
		//                (TextView) view.findViewById(R.id.call_log_day_group_label),
		//                (ImageView) view.findViewById(R.id.primary_action_button));

		return new CallLogListItemViewHolder(
				context,
				expandCollapseListener,
				telecomCallLogCache,
				callLogListItemHelper,
				voicemailPlaybackPresenter,
				view,
				null,
				view.findViewById(R.id.primary_action_view),
				PhoneCallDetailsViews.fromView(view),
				null,
				//				(TextView) view.findViewById(R.id.call_log_day_group_label),
				//				(ImageView) view.findViewById(R.id.primary_action_button),
				view.findViewById(R.id.item_more),
				(CheckBox)view.findViewById(android.R.id.button1),
				mLongClickListener,
				(SliderView)view.findViewById(R.id.slider_view1));
	}

	/**
	 * Configures the action buttons in the expandable actions ViewStub. The ViewStub is not
	 * inflated during initial binding, so click handlers, tags and accessibility text must be set
	 * here, if necessary.
	 *
	 * @param callLogItem The call log list item view.
	 */
	public void inflateActionViewStub() {
		ViewStub stub = (ViewStub) rootView.findViewById(R.id.call_log_entry_actions_stub);
		if (stub != null) {
			actionsView = (ViewGroup) stub.inflate();

			voicemailPlaybackView = (VoicemailPlaybackLayout) actionsView
					.findViewById(R.id.voicemail_playback_layout);

			callButtonView = actionsView.findViewById(R.id.call_action);
			callButtonView.setOnClickListener(this);

			videoCallButtonView = actionsView.findViewById(R.id.video_call_action);
			videoCallButtonView.setOnClickListener(this);

			createNewContactButtonView = actionsView.findViewById(R.id.create_new_contact_action);
			createNewContactButtonView.setOnClickListener(this);

			addToExistingContactButtonView =
					actionsView.findViewById(R.id.add_to_existing_contact_action);
			addToExistingContactButtonView.setOnClickListener(this);

			sendMessageView = actionsView.findViewById(R.id.send_message_action);
			sendMessageView.setOnClickListener(this);

			detailsButtonView = actionsView.findViewById(R.id.details_action);
			detailsButtonView.setOnClickListener(this);

			callWithNoteButtonView = actionsView.findViewById(R.id.call_with_note_action);
			callWithNoteButtonView.setOnClickListener(this);
		}

		bindActionButtons();
	}

	private void updatePrimaryActionButton(boolean isExpanded) {
		//		Log.d(TAG,"updatePrimaryActionButton,position:"+position);
		//		delete_view.setTag(position);
		if(slider!=null) slider.setTag(position);
		if (!TextUtils.isEmpty(voicemailUri)) {
			// Treat as voicemail list item; show play button if not expanded.
			//			if (!isExpanded) {
			//				primaryActionButtonView.setImageResource(R.drawable.ic_play_arrow_24dp);
			//				primaryActionButtonView.setVisibility(View.VISIBLE);
			//			} else {
			//				primaryActionButtonView.setVisibility(View.GONE);
			//			}
		} else {
			// Treat as normal list item; show call button, if possible.
			boolean canPlaceCallToNumber =
					PhoneNumberUtil.canPlaceCallsTo(number, numberPresentation);

			if (canPlaceCallToNumber) {
				boolean isVoicemailNumber =
						/*mTelecomCallLogCache.isVoicemailNumber(accountHandle, number)*/false;
				if (isVoicemailNumber) {
					// Call to generic voicemail number, in case there are multiple accounts.
					primaryActionView.setTag(
							IntentProvider.getReturnVoicemailCallIntentProvider());
				} else {
//					Log.d(TAG,"primaryActionView.setTag,slotId:"+slotId);
					primaryActionView.setTag(
							IntentProvider.getReturnCallIntentProvider(number,position,checkBox,slotId));
				}

				//				primaryActionButtonView.setContentDescription(TextUtils.expandTemplate(
				//						mContext.getString(R.string.description_call_action),
				//						nameOrNumber));
				//				primaryActionButtonView.setImageResource(R.drawable.ic_call_24dp);
				//				primaryActionButtonView.setVisibility(View.VISIBLE);
			} else {
				primaryActionView.setTag(null);
				//				primaryActionView.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * Binds text titles, click handlers and intents to the voicemail, details and callback action
	 * buttons.
	 */
	private void bindActionButtons() {
		boolean canPlaceCallToNumber = PhoneNumberUtil.canPlaceCallsTo(number, numberPresentation);

		//		if (!TextUtils.isEmpty(voicemailUri) && canPlaceCallToNumber) {
		//			callButtonView.setTag(IntentProvider.getReturnCallIntentProvider(number));
		//			((TextView) callButtonView.findViewById(R.id.call_action_text))
		//			.setText(TextUtils.expandTemplate(
		//					mContext.getString(R.string.call_log_action_call),
		//					nameOrNumber));
		//			callButtonView.setVisibility(View.VISIBLE);
		//		} else {
		//			callButtonView.setVisibility(View.GONE);
		//		}
		//
		//		// If one of the calls had video capabilities, show the video call button.
		//		if (mTelecomCallLogCache.isVideoEnabled() && canPlaceCallToNumber &&
		//				phoneCallDetailsViews.callTypeIcons.isVideoShown()) {
		//			videoCallButtonView.setTag(IntentProvider.getReturnVideoCallIntentProvider(number));
		//			videoCallButtonView.setVisibility(View.VISIBLE);
		//		} else {
		//			videoCallButtonView.setVisibility(View.GONE);
		//		}
		//
		//		// For voicemail calls, show the voicemail playback layout; hide otherwise.
		//		if (callType == Calls.VOICEMAIL_TYPE && mVoicemailPlaybackPresenter != null) {
		//			voicemailPlaybackView.setVisibility(View.VISIBLE);
		//
		//			Uri uri = Uri.parse(voicemailUri);
		//			mVoicemailPlaybackPresenter.setPlaybackView(
		//					voicemailPlaybackView, uri, mVoicemailPrimaryActionButtonClicked);
		//			mVoicemailPrimaryActionButtonClicked = false;
		//
		//			CallLogAsyncTaskUtil.markVoicemailAsRead(mContext, uri);
		//		} else {
		//			voicemailPlaybackView.setVisibility(View.GONE);
		//		}

		//		detailsButtonView.setVisibility(View.VISIBLE);
		detailsButtonView.setTag(
				IntentProvider.getCallDetailIntentProvider(rowId, callIds, null));

		//		if (info != null && UriUtils.isEncodedContactUri(info.lookupUri)) {
		//			createNewContactButtonView.setTag(IntentProvider.getAddContactIntentProvider(
		//					info.lookupUri, info.name, info.number, info.type, true /* isNewContact */));
		//			createNewContactButtonView.setVisibility(View.VISIBLE);
		//
		//			addToExistingContactButtonView.setTag(IntentProvider.getAddContactIntentProvider(
		//					info.lookupUri, info.name, info.number, info.type, false /* isNewContact */));
		//			addToExistingContactButtonView.setVisibility(View.VISIBLE);
		//		} else {
		//			createNewContactButtonView.setVisibility(View.GONE);
		//			addToExistingContactButtonView.setVisibility(View.GONE);
		//		}

		sendMessageView.setTag(IntentProvider.getSendSmsIntentProvider(number));
		Object[] objects=new Object[]{rowId,callIds,number};
		primaryActionButtonViewParent.setTag(objects);
		//		Log.d(TAG,"objects0:"+objects);

		//		mCallLogListItemHelper.setActionContentDescriptions(this);

		//		boolean supportsCallSubject =
		//				mTelecomCallLogCache.doesAccountSupportCallSubject(accountHandle);
		//		boolean isVoicemailNumber =
		//				mTelecomCallLogCache.isVoicemailNumber(accountHandle, number);
		//		callWithNoteButtonView.setVisibility(
		//				supportsCallSubject && !isVoicemailNumber ? View.VISIBLE : View.GONE);
	}

	/**
	 * Show or hide the action views, such as voicemail, details, and add contact.
	 *
	 * If the action views have never been shown yet for this view, inflate the view stub.
	 */
	public void showActions(boolean show) {
		//		expandVoicemailTranscriptionView(show);
		//
		//		if (show) {
		//			// Inflate the view stub if necessary, and wire up the event handlers.
		//			inflateActionViewStub();
		//
		//			actionsView.setVisibility(View.VISIBLE);
		//			actionsView.setAlpha(1.0f);
		//		} else {
		//			// When recycling a view, it is possible the actionsView ViewStub was previously
		//			// inflated so we should hide it in this case.
		//			if (actionsView != null) {
		//				actionsView.setVisibility(View.GONE);
		//			}
		//		}

		updatePrimaryActionButton(show);
	}

	public void expandVoicemailTranscriptionView(boolean isExpanded) {
		if (callType != Calls.VOICEMAIL_TYPE) {
			return;
		}

		//		final TextView view = phoneCallDetailsViews.voicemailTranscriptionView;
		//		if (TextUtils.isEmpty(view.getText())) {
		//			return;
		//		}
		//		view.setMaxLines(isExpanded ? VOICEMAIL_TRANSCRIPTION_MAX_LINES : 1);
		//		view.setSingleLine(!isExpanded);
	}

	public void setPhoto(long photoId, Uri photoUri, Uri contactUri, String displayName,
			boolean isVoicemail, boolean isBusiness) {
		if(quickContactView==null) return;
		quickContactView.assignContactUri(contactUri);
		quickContactView.setOverlay(null);

		int contactType = ContactPhotoManager.TYPE_DEFAULT;
		if (isVoicemail) {
			contactType = ContactPhotoManager.TYPE_VOICEMAIL;
		} else if (isBusiness) {
			contactType = ContactPhotoManager.TYPE_BUSINESS;
		}

		String lookupKey = null;
		if (contactUri != null) {
			lookupKey = UriUtils.getLookupKeyFromUri(contactUri);
		}

		DefaultImageRequest request = new DefaultImageRequest(
				displayName, lookupKey, contactType, true /* isCircular */);

		if (photoId == 0 && photoUri != null) {
			ContactPhotoManager.getInstance(mContext).loadPhoto(quickContactView, photoUri,
					mPhotoSize, false /* darkTheme */, true /* isCircular */, request);
		} else {
			ContactPhotoManager.getInstance(mContext).loadThumbnail(quickContactView, photoId,
					false /* darkTheme */, true /* isCircular */, request);
		}
	}

	@Override
	public void onSliderButtonClick(int id, View view, ViewGroup parent) {
		Log.d(TAG,"onSliderButtonClick,id:"+id+" view:"+view+" parent:"+parent);
		if(this.slider == parent) {
			switch (id) {
			case 1:
				//				Toast.makeText(mContext, "view1 : 1", Toast.LENGTH_SHORT).show();
				this.slider.close(false);
				mExpandCollapseListener.onClick(this.slider);
				break;
			}
		}
	}

	@Override
	public void onClick(View view) {
		Log.d(TAG,"onclick,viewid:"+view.getId());
		if (view.getId() == R.id.primary_action_button && !TextUtils.isEmpty(voicemailUri)) {

			mVoicemailPrimaryActionButtonClicked = true;
			mExpandCollapseListener.onClick(primaryActionView);
		} /*else if(view.getId()==R.id.delete_view){
			mExpandCollapseListener.onClick(delete_view);
		}*/else if (view.getId() == R.id.call_with_note_action) {

			CallSubjectDialog.start(
					(Activity) mContext,
					info.photoId,
					info.photoUri,
					info.lookupUri,
					(String) nameOrNumber /* top line of contact view in call subject dialog */,
					isBusiness,
					number, /* callable number used for ACTION_CALL intent */
					TextUtils.isEmpty(info.name) ? null : displayNumber, /* second line of contact
                                                                           view in dialog. */
							numberType, /* phone number type (e.g. mobile) in second line of contact view */
							accountHandle);
		} else if(view.getId()==R.id.item_more){
			Log.d(TAG,"onclick2");

			//				Intent intent=IntentProvider.getCallDetailIntentProvider(rowId, callIds, null).getIntent(mContext);
			//				if (intent != null) {
			//					DialerUtils.startActivityWithErrorToast(mContext, intent);
			//				}

			Intent intent = new Intent(mContext, CallDetailActivity.class); 
			intent.putExtra(CallDetailActivity.EXTRA_NUMBER, number);
			intent.putExtra(CallDetailActivity.EXTRA_CALL_LOG_IDS, callIds);
			intent.putExtra("mIsPrivate", mIsPrivate);
			mContext.startActivity(intent);


			//			bindActionButtons();
			//			Object[] objects=(Object[]) view.getTag();
			//			Log.d(TAG,"objects:"+objects);
			//			final long rowId=(Long)objects[0];
			//			final long[] callIds=(long[])objects[1];
			//			final String number=(String)objects[2];

			//初始化PopupMenu
			/*PopupMenu popupMenu = new PopupMenu(mContext, view,Gravity.TOP|Gravity.RIGHT);
			//设置popupmenu 中menu的点击事件
			popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					// TODO Auto-generated method stub
					//					Log.d(TAG,"onMenuItemClick,item:"+item+" id:"+item.getItemId()+" R.id.call_log_action_send_message:"+R.id.call_log_action_send_message);
					switch (item.getItemId()) {
					case R.id.call_log_action_details:{
						Log.d(TAG,"click call_log_action_send_message");
						Intent intent=IntentProvider.getCallDetailIntentProvider(rowId, callIds, null).getIntent(mContext);
						if (intent != null) {
							DialerUtils.startActivityWithErrorToast(mContext, intent);
						}
						break;
					}

					case R.id.call_log_action_send_message:{
						Log.d(TAG,"click call_log_action_details");
						Intent intent=IntentProvider.getSendSmsIntentProvider(number).getIntent(mContext);
						if (intent != null) {
							DialerUtils.startActivityWithErrorToast(mContext, intent);
						}
						break;
					}
					default:
						break;
					}
					return false;
				}
			});
			//导入menu布局
			popupMenu.inflate(R.menu.mst_calllog_list_item_more_menu);
			final Menu menu = popupMenu.getMenu();

			boolean showAddToContact=TextUtils.isEmpty(info.name);
			final MenuItem mst_add_to_contact = menu.findItem(R.id.mst_add_to_contact);
			final MenuItem mst_add_to_exist_contact = menu.findItem(R.id.mst_add_to_exist_contact);
			mst_add_to_contact.setVisible(showAddToContact);
			mst_add_to_exist_contact.setVisible(showAddToContact);

			//显示popup menu
			popupMenu.show();*/

			/*if(null == CallLogAdapter.popupWindow || !CallLogAdapter.popupWindow.isShowing()){  
				View contentView = View.inflate(mContext,
						R.layout.mst_popupwindow_for_calllog, null);

				sendMessageView = contentView.findViewById(R.id.call_log_action_send_message);
				sendMessageView.setOnClickListener(this);
				detailsButtonView = contentView.findViewById(R.id.call_log_action_details);
				detailsButtonView.setOnClickListener(this);

				bindActionButtons();
				LinearLayout ll_popup_container = (LinearLayout) contentView
						.findViewById(R.id.ll_popup_container);

				ScaleAnimation sa = new ScaleAnimation( 0.0f,
						1.0f,
						0.0f,
						1.0f,
						Animation.RELATIVE_TO_SELF, 
						1.0f, 
						Animation.RELATIVE_TO_SELF, 
						0.0f);
				sa.setDuration(100);		

				DisplayMetrics dm = new DisplayMetrics();
				((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
				int screenW = dm.widthPixels;// 获取分辨率宽度

				CallLogAdapter.popupWindow = new PopupWindow(contentView, DensityUtil.dip2px(mContext, 150), 
						DensityUtil.dip2px(mContext, 100));
				CallLogAdapter.popupWindow.setBackgroundDrawable(new ColorDrawable(
						Color.TRANSPARENT));

				int[] location = new int[2];
				view.getLocationInWindow(location);
				Log.d(TAG, "location[0]"+location[0]
						+"\nlocation[1]:"+location[1]
								+"\nDensityUtil.dip2px(context, 146):"+DensityUtil.dip2px(mContext, 146)
								+"\nDensityUtil.dip2px(context, 42):"+DensityUtil.dip2px(mContext, 42)
								+"\nscreenW:"+screenW
						);
				CallLogAdapter.popupWindow.showAtLocation(view, Gravity.TOP | Gravity.RIGHT,
						DensityUtil.dip2px(mContext, 20), location[1]+DensityUtil.dip2px(mContext, 20));
				ll_popup_container.startAnimation(sa);
			}else{
				CallLogAdapter.popupWindow.dismiss();
				CallLogAdapter.popupWindow=null;
			}	*/	

		}else{
			final IntentProvider intentProvider = (IntentProvider) view.getTag();
			if (intentProvider != null) {
				final Intent intent = intentProvider.getIntent(mContext);
				// See IntentProvider.getCallDetailIntentProvider() for why this may be null.
				if (intent != null) {
					DialerUtils.startActivityWithErrorToast(mContext, intent);
				}
			}
		}

	}

	@NeededForTesting
	public static CallLogListItemViewHolder createForTest(Context context) {
		Resources resources = context.getResources();
		TelecomCallLogCache telecomCallLogCache = new TelecomCallLogCache(context);
		PhoneCallDetailsHelper phoneCallDetailsHelper = new PhoneCallDetailsHelper(
				context, resources, telecomCallLogCache);

		CallLogListItemViewHolder viewHolder = new CallLogListItemViewHolder(
				context,
				null /* expandCollapseListener */,
				telecomCallLogCache,
				new CallLogListItemHelper(phoneCallDetailsHelper, resources, telecomCallLogCache),
				null /* voicemailPlaybackPresenter */,
				new View(context),
				new QuickContactBadge(context),
				new View(context),
				PhoneCallDetailsViews.createForTest(context),
				new CardView(context),
				//				new TextView(context),
				new View(context),
				new CheckBox(context),
				null,
				null);
		viewHolder.detailsButtonView = new TextView(context);
		viewHolder.actionsView = new View(context);
		viewHolder.voicemailPlaybackView = new VoicemailPlaybackLayout(context);

		return viewHolder;
	}
}
