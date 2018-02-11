/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.mms.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import mst.provider.Telephony;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Checkable;
import android.widget.CheckBox;
import android.widget.QuickContactBadge;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.mms.LogTag;
import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//lichao add begin
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import mst.provider.Telephony.Sms.Conversations;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.*;
import android.telephony.SubscriptionManager;
import mst.provider.Telephony.Sms;
import android.database.sqlite.SqliteWrapper;
import android.telephony.PhoneNumberUtils;
import com.mst.tms.TmsServiceManager;
import com.mst.tms.MarkResult;
import android.text.style.AbsoluteSizeSpan;
//lichao add end
//XiaoYuan SDK add begin
import com.xy.smartsms.iface.IXYConversationListItemHolder;
import com.xy.smartsms.manager.XyPublicInfoItem;
//XiaoYuan SDK add end

/**
 * This class manages the view for given conversation.
 */
//XiaoYuan SDK add ",IXYConversationListItemHolder"
public class ConversationListItem extends LinearLayout implements Contact.UpdateListener,
            Checkable, IXYConversationListItemHolder {
    private static final String TAG = "ConversationListItem";
    private static final boolean DEBUG = false;

    private CheckBox mCheckBox;
    private TextView mSubjectView;//just like summary
    private TextView mFromView;//just like title
    private TextView mUnreadView1;//Unread Counts equals 1
    private TextView mUnreadView2;//Unread Counts more than 1
    private TextView mDateView;
    private View mAttachmentView;
    private ImageView mSimIconView;
    //private View mErrorIndicator;
    //private QuickContactBadge mAvatarView;

    static private Drawable sDefaultContactImage;

    // For posting UI update Runnables from other threads:
    private Handler mHandler = new Handler();

    private Conversation mConversation;
    
    //XiaoYuan SDK add
    private XyPublicInfoItem xyPublicInfoItem;

    public static final StyleSpan STYLE_BOLD = new StyleSpan(Typeface.BOLD);

    private Context mContext;

    public ConversationListItem(Context context) {
        super(context);
        mContext = context;
    }

    public ConversationListItem(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        if (sDefaultContactImage == null) {
            sDefaultContactImage = context.getResources().getDrawable(R.drawable.ic_contact_picture);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mCheckBox = (CheckBox) findViewById(R.id.list_item_check_box);

        mFromView = (TextView) findViewById(R.id.from);
        mSubjectView = (TextView) findViewById(R.id.subject);

        mUnreadView1 = (TextView) findViewById(R.id.unread1);
        mUnreadView2 = (TextView) findViewById(R.id.unread2);

        mDateView = (TextView) findViewById(R.id.date);
        mAttachmentView = findViewById(R.id.attachment);
        //mErrorIndicator = findViewById(R.id.error);
        //mAvatarView = (QuickContactBadge) findViewById(R.id.conv_list_avatar);
        mSimIconView = (ImageView) findViewById(R.id.sim_indicator_icon);
    }

    public Conversation getConversation() {
        return mConversation;
    }

    /**
     * Only used for header binding.
     */
//    public void bind(String title, String explain) {
//        mFromView.setText(title);
//        mSubjectView.setText(explain);
//    }

    private CharSequence formatMessage() {

        ContactList contactList = mConversation.getRecipients();
        if(DEBUG) Log.d(TAG, "formatMessage(), contactList = "+contactList);

        String from = contactList.formatNames("、");
        if(DEBUG) Log.d(TAG, "formatMessage(), from = "+from);

        if (MessageUtils.isWapPushNumber(from)) {
            String[] mAddresses = from.split(":");
            from = mAddresses[mContext.getResources().getInteger(
                    R.integer.wap_push_address_index)];
        }

        /**
         * Add boolean to know that the "from" haven't the Arabic and '+'.
         * Make sure the "from" display normally for RTL.
         */
        Boolean isEnName = false;
        Boolean isLayoutRtl = (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())
                == View.LAYOUT_DIRECTION_RTL);
        if (isLayoutRtl && from != null) {
            if (from.length() >= 1) {
                Pattern pattern = Pattern.compile("[^أ-ي]+");
                Matcher matcher = pattern.matcher(from);
                isEnName = matcher.matches();
                if (from.charAt(0) != '\u202D') {
                    if (isEnName) {
                        from = '\u202D' + from + '\u202C';
                    }
                }
            }
        }

        return from;

        //SpannableStringBuilder buf = new SpannableStringBuilder(from);

        //lichao add for public numbers mark in 2016-10-27 begin
        /*
        boolean isDBG = false;
        if(1 == contactList.size()){
            String subTitle = "";
            String title = contactList.get(0).getName();
            String number = contactList.get(0).getNumber();
            if (isDBG) Log.d(TAG, "\n\n formatMessage title = " + title +", number = " + number);

            String normalizedNumber = PhoneNumberUtils.normalizeNumber(number);
            if (isDBG) Log.d(TAG, " formatMessage normalizedNumber: " + normalizedNumber);

            String note = MessageUtils.getNoteByNameAndNumber(mContext, title, normalizedNumber);
            if (isDBG) Log.d(TAG, " formatMessage note = " + note);

            String location = "";
            MarkResult mark = null;
            String markName = "";
            if (null != TmsServiceManager.getInstance() && true == TmsServiceManager.mIsServiceConnected) {
                location = TmsServiceManager.getInstance().getArea(normalizedNumber);
                mark = TmsServiceManager.getInstance().getMark(MarkResult.USED_FOR_Common, normalizedNumber);
            }
            if (isDBG) Log.d(TAG, "formatMessage, location: " + location);

            if(null != mark){
                if (isDBG) Log.d(TAG, "formatMessage, MarkResult: " + mark.toString());
                markName = mark.getName();
            }else{
                if (isDBG) Log.d(TAG, "formatMessage, MarkResult is null");
            }

            if (title.equals(number)) {
                //The space is limited, only shows one
                if (!TextUtils.isEmpty(note)) {
                    subTitle = note;
                } else if (!TextUtils.isEmpty(markName)) {
                    subTitle = markName;
                } else if (!TextUtils.isEmpty(location)) {
                    subTitle = location;
                }
            } else {
                String formatedNumber = PhoneNumberUtils.formatNumber(number, number,
                        MmsApp.getApplication().getCurrentCountryIso());
                subTitle = formatedNumber;
                //The space is limited, only shows formatedNumber
                //if (!TextUtils.isEmpty(note)) {
                //    subTitle += "  " + note;
                //}
            }
            if(!TextUtils.isEmpty(subTitle)){
                int before = buf.length();
                buf.append("[" + subTitle + "]");
                int end = buf.length();
                //int size = android.R.style.TextAppearance_Small;
                //buf.setSpan(new TextAppearanceSpan(mContext, size), before,
                //        buf.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                buf.setSpan(new AbsoluteSizeSpan(10,true), before, end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                buf.setSpan(new ForegroundColorSpan(Color.GRAY), before, end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            }
        }
        */
        //lichao add for public numbers mark in 2016-10-27 end
        
	    //lichao modify for show UnReadMessageCount(>=2) in red color in 2016-08-09 begin
        /*
        int unreadCount = getUnReadMessageCount();
        Log.v(TAG, " unreadCount: " + unreadCount);

        //if (mConversation.getMessageCount() > 1) {
        if(unreadCount >= 1) {
            int before = buf.length();
            if (isLayoutRtl) {
                if (isEnName) {
                    buf.insert(1, mContext.getResources().getString(R.string.message_count_format,
                            unreadCount) + " ");
                    buf.setSpan(new ForegroundColorSpan(
                            //mContext.getResources().getColor(R.color.message_count_color)),
                            Color.RED),
                            1, buf.length() - before, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                } else {
                    buf.append(" " + unreadCount);
                    buf.setSpan(new ForegroundColorSpan(
                            //mContext.getResources().getColor(R.color.message_count_color)),
                            Color.RED),
                            before, buf.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                }
            } else {
                buf.append(mContext.getResources().getString(R.string.message_count_format,
                        unreadCount));
                buf.setSpan(new ForegroundColorSpan(
                        //mContext.getResources().getColor(R.color.message_count_color)),
                        Color.RED),
                        before, buf.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
           }
        }
        */
	    //lichao modify for show UnReadMessageCount(>=2) in red color in 2016-08-09 end

        /*
        if (mConversation.hasDraft()) {
            if (isLayoutRtl && isEnName) {
                int before = buf.length();
                buf.insert(1,'\u202E'
                        + mContext.getResources().getString(R.string.draft_separator)
                        + '\u202C');
                buf.setSpan(new ForegroundColorSpan(
                        mContext.getResources().getColor(R.drawable.text_color_black)),
                        1, buf.length() - before + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                before = buf.length();
                int size;
                buf.insert(1,mContext.getResources().getString(R.string.has_draft));
                size = android.R.style.TextAppearance_Small;
                buf.setSpan(new TextAppearanceSpan(mContext, size), 1,
                        buf.length() - before + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                buf.setSpan(new ForegroundColorSpan(
                        mContext.getResources().getColor(R.drawable.text_color_red)),
                        1, buf.length() - before + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            } else {
                buf.append(mContext.getResources().getString(R.string.draft_separator));
                int before = buf.length();
                int size;
                buf.append(mContext.getResources().getString(R.string.has_draft));
                size = android.R.style.TextAppearance_Small;
                buf.setSpan(new TextAppearanceSpan(mContext, size, color), before,
                        buf.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                buf.setSpan(new ForegroundColorSpan(
                        mContext.getResources().getColor(R.drawable.text_color_red)),
                        before, buf.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
              }
        }
        */

        // Unread messages are shown in bold
		/*
        if (mConversation.hasUnreadMessages()) {
            buf.setSpan(STYLE_BOLD, 0, buf.length(),
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
		*/
        //return buf;
    }

    /*
    private void updateAvatarView() {
        Drawable avatarDrawable;
        if (mConversation.getRecipients().size() == 1) {
            Contact contact = mConversation.getRecipients().get(0);
            avatarDrawable = contact.getAvatar(mContext, sDefaultContactImage);

            if (contact.existsInDatabase()) {
                mAvatarView.assignContactUri(contact.getUri());
                mAvatarView.setImageDrawable(avatarDrawable);
            } else {
                // identify it is phone number or email address,handle it respectively
                if (Telephony.Mms.isEmailAddress(contact.getNumber())) {
                    mAvatarView.assignContactFromEmail(contact.getNumber(), true);
                } else if (MessageUtils.isWapPushNumber(contact.getNumber())) {
                    mAvatarView.assignContactFromPhone(
                            MessageUtils.getWapPushNumber(contact.getNumber()), true);
                } else {
                    mAvatarView.assignContactFromPhone(contact.getNumber(), true);
                }
            }
        } else {
            // TODO get a multiple recipients asset (or do something else)
            avatarDrawable = sDefaultContactImage;
            mAvatarView.assignContactUri(null);
        }
        mAvatarView.setImageDrawable(avatarDrawable);
        mAvatarView.setVisibility(View.VISIBLE);
    }
    */

    private void updateFromView() {
        Log.v(TAG, " updateFromView() ");
        mFromView.setText(formatMessage());
        //updateAvatarView();
		//XiaoYuan SDK add begin,lichao remove for no use AvatarView
        //bindTextImageView();
		//XiaoYuan SDK add end
    }

    public void onUpdate(Contact updated) {
        Log.v(TAG, " onUpdate() ");
        if (Log.isLoggable(LogTag.CONTACT, Log.DEBUG)) {
            Log.v(TAG, "onUpdate: " + this + " contact: " + updated);
        }
        mHandler.post(new Runnable() {
            public void run() {
                updateFromView();
            }
        });
    }

    public final void bindConversation(Context context, final Conversation conversation) {
        if (DEBUG) Log.v(TAG, "bindConversation()");

        mConversation = conversation;

        //ligy add befor 20160809
        //updateUnReadMessageCount();

        //lichao delete original background set codes in 2016-08-08
        //updateBackground();

        /*
        LayoutParams attachmentLayout = (LayoutParams)mAttachmentView.getLayoutParams();
        boolean hasError = conversation.hasError();
        // When there's an error icon, the attachment icon is left of the error icon.
        // When there is not an error icon, the attachment icon is left of the date text.
        // As far as I know, there's no way to specify that relationship in xml.
        if (hasError) {
            attachmentLayout.addRule(RelativeLayout.LEFT_OF, R.id.error);
        } else {
            attachmentLayout.addRule(RelativeLayout.LEFT_OF, R.id.date);
        }
        */

        int subId = conversation.getSubId();
        int subscription = SubscriptionManager.getSlotId(subId);
        boolean isShowSimIcon = MmsApp.isCreateConversaitonIdBySim
                && MessageUtils.isMsimIccCardActive() && subscription >= 0;
        if (isShowSimIcon) {
            Drawable mSimIndicatorIcon = MessageUtils.getMultiSimIcon(context, subscription);
            mSimIconView.setImageDrawable(mSimIndicatorIcon);
        }

        boolean hasAttachment = conversation.hasAttachment();

        if(isShowSimIcon && hasAttachment){
            MessageUtils.setMargins(context, mSimIconView,11,0,0,0);
            MessageUtils.setMargins(context, mAttachmentView,20,0,0,0);
        } else if(isShowSimIcon && !hasAttachment){
            MessageUtils.setMargins(context, mSimIconView,20,0,0,0);
            MessageUtils.setMargins(context, mAttachmentView,0,0,0,0);
        } else if(!isShowSimIcon && hasAttachment){
            MessageUtils.setMargins(context, mSimIconView,0,0,0,0);
            MessageUtils.setMargins(context, mAttachmentView,20,0,0,0);
        } else{
            MessageUtils.setMargins(context, mSimIconView,0,0,0,0);
            MessageUtils.setMargins(context, mAttachmentView,0,0,0,0);
        }

        mSimIconView.setVisibility(isShowSimIcon ? View.VISIBLE : View.GONE);
        mAttachmentView.setVisibility(hasAttachment ? View.VISIBLE : View.GONE);

        // Date
        mDateView.setText(MessageUtils.formatTimeStampString(context, conversation.getDate()));

        // From.
        mFromView.setText(formatMessage());

        //lichao add in 2016-09-05 begin
        showUnreadMsgCountView();
        //lichao add in 2016-09-05 end

        // Register for updates in changes of any of the contacts in this conversation.
        ContactList contacts = conversation.getRecipients();

        if (Log.isLoggable(LogTag.CONTACT, Log.DEBUG)) {
            Log.v(TAG, "bind: contacts.addListeners " + this);
        }
        Contact.addListener(this);

        // Subject
		//ligy modify Unread Messages color to blue begin
        mSubjectView.setText(formatSubjectContent());
		//ligy modify Unread Messages color to blue end

        mCheckBox.setVisibility(mIsCheckBoxMode ? View.VISIBLE : View.GONE);
        mCheckBox.setChecked(conversation.isChecked());

        /*
        LayoutParams subjectLayout = (LayoutParams)mSubjectView.getLayoutParams();
        // We have to make the subject left of whatever optional items are shown on the right.
        subjectLayout.addRule(RelativeLayout.START_OF, hasAttachment ? R.id.attachment :
            (hasError ? R.id.error : R.id.date));
            */

        // Transmission error indicator.
        //mErrorIndicator.setVisibility(hasError ? VISIBLE : GONE);

        //updateAvatarView();
        //XiaoYuan SDK add begin,lichao remove for no use AvatarView
        //bindTextImageView();
        //XiaoYuan SDK add end
    }
    
	//XiaoYuan SDK add begin,lichao remove for no use AvatarView
    /*
    private void bindTextImageView(){
        if(xyPublicInfoItem == null){
        	xyPublicInfoItem = new XyPublicInfoItem();
        }
        xyPublicInfoItem.bindTextImageView((IXYConversationListItemHolder)this,mFromView,mAvatarView);
    }
    */
	//XiaoYuan SDK add end

    //lichao delete original background set codes in 2016-08-08 begin
	/*
    private void updateBackground() {
        int backgroundId;
        if (mConversation.isChecked()) {
            backgroundId = R.drawable.list_selected_holo_light;
        } else if (mConversation.hasUnreadMessages()) {
            backgroundId = R.drawable.conversation_item_background_unread;
        } else {
            backgroundId = R.drawable.conversation_item_background_read;
        }
        Drawable background = mContext.getResources().getDrawable(backgroundId);
        setBackground(background);
    }
	*/
    //lichao delete original background set codes in 2016-08-08 end

    public final void unbind() {
        if (Log.isLoggable(LogTag.CONTACT, Log.DEBUG)) {
            Log.v(TAG, "unbind: contacts.removeListeners " + this);
        }
        // Unregister contact update callbacks.
        Contact.removeListener(this);
    }

    public void setChecked(boolean checked) {
        mConversation.setIsChecked(checked);
        //lichao delete original background set codes in 2016-08-08
        //updateBackground();
    }

    public boolean isChecked() {
        return mConversation.isChecked();
    }

    public void toggle() {
        mConversation.setIsChecked(!mConversation.isChecked());
    }
    
	//ligy add, lichao modify in 2016-08-09, 2016-09-01 begin
    static final int TYPE_INBOX = 1;

    private int getUnReadMessageCount() {
        if (null == mConversation) {
            return 0;
        }

        if(DEBUG) Log.d(TAG, "mConversation.getThreadId() = " + mConversation.getThreadId());

        if (!mConversation.hasUnreadMessages() || mConversation.getThreadId() <= 0) {
            //Log.v(TAG, " has none UnreadMessages(), return 0");
            return 0;
        }

        final String MAILBOX_URI = "content://mms-sms/mailbox/";
        String inboxUri = MAILBOX_URI + TYPE_INBOX;

        Cursor csr = null;
        int count = 0;
        try {
            csr = mContext.getContentResolver().query(Uri.parse(inboxUri),
                    MessageListAdapter.MAILBOX_PROJECTION,
                    Conversations.THREAD_ID + " = " + mConversation.getThreadId(),
                    null, null);

            while (csr.moveToNext()) {
                if (csr.getInt(MessageListAdapter.COLUMN_SMS_READ) == 0
                        || csr.getInt(MessageListAdapter.COLUMN_MMS_READ) == 0) {
                    count++;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "updateUnReadMessageCount() Exception: \n", e);
        } finally {
            if (csr != null) {
                csr.close();
            }
        }
        //Log.v(TAG, " getUnReadMessageCount: " + count);
        return count;
    }
	//ligy add, lichao modify in 2016-08-09, 2016-09-01 end

    //lichao add for test begin
    private int getCarMessageCount() {
        //Uri CONTENT_CAR_URI = Uri.parse("content://sms/car");
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = SqliteWrapper.query(
                    mContext,
                    mContext.getContentResolver(),
                    Sms.CONTENT_CAR_URI,
                    null, null, null, null);
            if(null != cursor){
                count = cursor.getCount();
            }
        } catch (Exception e) {
            Log.e(TAG, "getCarMessageCount() Exception: \n", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.v(TAG, " getCarMessageCount(), return " + count);
        return count;
    }
    //lichao add for test end

    //lichao add for xiaoyuan SDK in 2016-08-25 begin
	boolean isScrolling = false;
	@Override
	public boolean isScrolling() {
		// TODO Auto-generated method stub
		return isScrolling;
	}

	public void setScrolling(boolean isScrolling) {
		this.isScrolling = isScrolling;
	}

	@Override
	public String getPhoneNumber() {
		if (mConversation.getRecipients().size() == 1) {
            Contact contact = mConversation.getRecipients().get(0);
            String phone = contact.getNumber();
	        return phone;     
        }   
		return "";
	}
    //lichao add for xiaoyuan SDK in 2016-08-25 end
	
    //lichao add in 2016-09-03 begin
    private CharSequence formatSubjectContent(){

        String content = mConversation.getSnippet();

        SpannableStringBuilder buf = new SpannableStringBuilder(content);

        String prefix = null;

        if (mConversation.hasDraft()) {
            prefix = mContext.getResources().getString(R.string.has_draft);

        }
        else if(mConversation.hasError()){
        //else if(true){//for test
            prefix = mContext.getResources().getString(R.string.prefix_send_failure);
        }

        if(null != prefix){
            int length = prefix.length();
            buf.insert(0,prefix + " ");
            //2016-11-02： The bold font is not supported by character library
            buf.setSpan(STYLE_BOLD, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            buf.setSpan(STYLE_BOLD, length-1, length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

            buf.setSpan(new ForegroundColorSpan(
                            mContext.getResources().getColor(R.color.prefix_text_color_red)),
                    0, length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return buf;
    }

    private boolean isEnName(){
        String from = mConversation.getRecipients().formatNames(", ");
        if (MessageUtils.isWapPushNumber(from)) {
            String[] mAddresses = from.split(":");
            from = mAddresses[mContext.getResources().getInteger(
                    R.integer.wap_push_address_index)];
        }

        /**
         * Add boolean to know that the "from" haven't the Arabic and '+'.
         * Make sure the "from" display normally for RTL.
         */
        Boolean isEnName = false;
        Boolean isLayoutRtl = (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())
                == View.LAYOUT_DIRECTION_RTL);
        if (isLayoutRtl && from != null) {
            if (from.length() >= 1) {
                Pattern pattern = Pattern.compile("[^أ-ي]+");
                Matcher matcher = pattern.matcher(from);
                isEnName = matcher.matches();
            }
        }
        return isEnName;
    }
    //lichao add in 2016-09-03 end
    //lichao add in 2016-09-22 begin
    static final int SMALL_BADGE_WIDTH_OR_HIGHT = 6;
    static final int BADGE_WIDTH_OR_HIGHT = 15;
    private void showUnreadMsgCountView(){

        if(null == mConversation){
            Log.v(TAG, "showUnreadMsgCountView(), null == mConversation, return ");
            return;
        }

        int unreadCount = getUnReadMessageCount();
        //int unreadCount = getCarMessageCount();//only for test
        //int unreadCount = mConversation.getMessageCount();//for test
        if(DEBUG) Log.v(TAG, "\n\n showUnreadMsgCountView(), getThreadId()="+ mConversation.getThreadId() +
                ", unreadCount = "+ unreadCount);
        //Log.v(TAG, "showUnreadMsgCountView(), getThreadId(): " + conversation.getThreadId());

        if(unreadCount == 1){
            if(null == mUnreadView1){
                return;
            }
            mUnreadView1.setText("");
            mUnreadView1.setBackground(getUnreadViewBackground(SMALL_BADGE_WIDTH_OR_HIGHT/2));
            mUnreadView1.setVisibility(VISIBLE);
            if(null != mUnreadView2){
                mUnreadView2.setVisibility(GONE);
            }
        }
        else if(unreadCount > 1){
            if(null == mUnreadView2){
                return;
            }
            mUnreadView2.setText(String.valueOf(unreadCount));
            mUnreadView2.setBackground(getUnreadViewBackground(BADGE_WIDTH_OR_HIGHT/2));
            mUnreadView2.setVisibility(VISIBLE);
            if(null != mUnreadView1){
                mUnreadView1.setVisibility(GONE);
            }
        }
        else{
            if(null != mUnreadView1){
                mUnreadView1.setVisibility(GONE);
            }
            if(null != mUnreadView2){
                mUnreadView2.setVisibility(GONE);
            }
        }
    }

    public ShapeDrawable getUnreadViewBackground(int dipRadius) {
        int bgColor_red = mContext.getResources().getColor(R.color.unread_msg_count_bg_color_red);
        int radius = MessageUtils.dip2Px(mContext, dipRadius);
        float[] radiusArray = new float[] { radius, radius, radius, radius, radius, radius, radius, radius };
        RoundRectShape roundRect = new RoundRectShape(radiusArray, null, null);
        ShapeDrawable bgDrawable = new ShapeDrawable(roundRect);
        bgDrawable.getPaint().setColor(bgColor_red);
        bgDrawable.getPaint().setAntiAlias(true);
        return bgDrawable;
    }
    //lichao add in 2016-09-22 end

    private boolean mIsCheckBoxMode = false;
    public void setCheckBoxEnable(boolean flag) {
        mIsCheckBoxMode = flag;
    }
    public boolean getCheckBoxEnable() {
        return mIsCheckBoxMode;
    }
    
}
