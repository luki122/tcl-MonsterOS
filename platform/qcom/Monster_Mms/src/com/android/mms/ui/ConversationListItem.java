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
import android.view.ViewGroup;
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
import android.view.ViewStub;
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
    private static final String TAG = "Mms/ConversationListItem";
    private static final boolean DEBUG = false;

    private View mCheckBoxStubView;
    private CheckBox mCheckBox;
    private TextView mSubjectView;//just like summary
    private TextView mFromView;//just like title
    private View mUnreadStubView;
    private TextView mUnreadView1;//Unread Counts equals 1
//    private TextView mUnreadView2;//Unread Counts more than 1
    private TextView mDateView;
    private View mSimAttachStubView;
    private ImageView mAttachmentView;
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

        mFromView = (TextView) findViewById(R.id.from);
        mSubjectView = (TextView) findViewById(R.id.subject);
        mDateView = (TextView) findViewById(R.id.date);

        //mErrorIndicator = findViewById(R.id.error);
        //mAvatarView = (QuickContactBadge) findViewById(R.id.conv_list_avatar);
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
        if (DEBUG) Log.d(TAG, "formatMessage(), contactList = " + contactList);

        String from = contactList.formatNames("、");
        if (DEBUG) Log.d(TAG, "formatMessage(), from = " + from);

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
    }

    private void updateFromView() {
        if (DEBUG) Log.v(TAG, " updateFromView() ");
        mFromView.setText(formatMessage());
        //updateAvatarView();
        //XiaoYuan SDK add begin,lichao remove for no use AvatarView
        //bindTextImageView();
        //XiaoYuan SDK add end
    }

    //for Contact.UpdateListener()
    public void onUpdate(Contact updated) {
        if (DEBUG) Log.v(TAG, " onUpdate() ");
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
        if (DEBUG) Log.v(TAG, "\n\n bindConversation(), getThreadId()="+ conversation.getThreadId());

        mConversation = conversation;

        updateCheckBox(conversation.isChecked());

        // From.
        mFromView.setText(formatMessage());

        //int unreadCount = getUnreadMessageCount(context);
        //if (DEBUG) Log.v(TAG, "bindConversation, unreadCount = " + unreadCount);
        // Unread red tip
        //updateUnreadView(context, unreadCount);
        boolean hasUnread = mConversation.hasUnreadMessages();
        updateUnreadView(context, hasUnread);

        // Date
        String dateStr = MessageUtils.formatTimeStampStringForItem(context, conversation.getDate());
        //Log.v(TAG, "bindConversation, dateStr = "+ dateStr);
        mDateView.setText(dateStr);

        updateFromViewMaxWidth(context, hasUnread);
        setFromMargins(context);

        // Subject
        CharSequence subject = MessageUtils.formatSubject(mContext, conversation.getSnippet(),
                conversation.hasDraft(), conversation.hasError());
        mSubjectView.setText(subject);

        boolean hasAttachment = conversation.hasAttachment();
        updateAttachIcon(context, hasAttachment);

        int slotId = SubscriptionManager.getSlotId(conversation.getSubId());
        boolean isShowSimIcon = MmsApp.isCreateConversaitonIdBySim
                && MessageUtils.isTwoSimCardEnabled() && slotId >= 0;
        updateSimIcon(context, slotId, isShowSimIcon, hasAttachment);

        //updateSubjectViewMaxWidth(context, isShowSimIcon, hasAttachment);
        setSubjectMargins(context, isShowSimIcon, hasAttachment);

        // Register for updates in changes of any of the contacts in this conversation.
        //ContactList contacts = conversation.getRecipients();
        if (Log.isLoggable(LogTag.CONTACT, Log.DEBUG)) {
            Log.v(TAG, "bind: contacts.addListeners " + this);
        }
        Contact.addListener(this);

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

    public final void unbindConversation(boolean resetCheckBox) {
        if (Log.isLoggable(LogTag.CONTACT, Log.DEBUG)) {
            Log.v(TAG, "unbindConversation: contacts.removeListeners " + this);
        }
        // Unregister contact update callbacks.
        Contact.removeListener(this);

        //lichao add in 2016-12-15 begin
        if(null != mCheckBox && resetCheckBox){
            mCheckBox.setChecked(false);
        }
        //lichao add in 2016-12-15 end
    }

    public void setChecked(boolean checked) {
        mConversation.setIsChecked(checked);
    }

    public boolean isChecked() {
        return mConversation.isChecked();
    }

    public void toggle() {
        mConversation.setIsChecked(!mConversation.isChecked());
    }

    //ligy add, lichao modify in 2016-08-09, 2016-09-01 begin
    static final int TYPE_INBOX = 1;

//    private int getUnreadMessageCount(Context context) {
//        if (null == mConversation) {
//            return 0;
//        }
//        if (!mConversation.hasUnreadMessages() || mConversation.getThreadId() <= 0) {
//            //Log.v(TAG, " has none UnreadMessages(), return 0");
//            return 0;
//        }
//
//        final String MAILBOX_URI = "content://mms-sms/mailbox/";
//        String inboxUri = MAILBOX_URI + TYPE_INBOX;
//        Cursor csr = null;
//        int count = 0;
//        try {
//            csr = context.getContentResolver().query(Uri.parse(inboxUri),
//                    MessageListAdapter.MAILBOX_PROJECTION,
//                    Conversations.THREAD_ID + " = " + mConversation.getThreadId(),
//                    null, null);
//
//            while (csr.moveToNext()) {
//                if (csr.getInt(MessageListAdapter.COLUMN_SMS_READ) == 0
//                        || csr.getInt(MessageListAdapter.COLUMN_MMS_READ) == 0) {
//                    count++;
//                }
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "getUnreadMessageCount() Exception: \n", e);
//        } finally {
//            if (csr != null) {
//                csr.close();
//            }
//        }
//        //Log.v(TAG, " getUnreadMessageCount: " + count);
//        return count;
//    }
    //ligy add, lichao modify in 2016-08-09, 2016-09-01 end

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
    private boolean isEnName() {
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
    static final int BADGE_WIDTH = 15;
    static final int BADGE_MARGIN = 7;
    static final int SMALL_BADGE_WIDTH = 6;
    static final int SMALL_BADGE_MARGIN = 2;

//    private void updateUnreadView(Context context, int unreadCount) {
//        if (unreadCount == 0) {
//            hideUnreadView(context);
//            return;
//        }
//        if (unreadCount == 1) {
//            if (null != mUnreadView2) {
//                mUnreadView2.setVisibility(GONE);
//            }
//            if (null == mUnreadView1) {
//                inflateUnreadStubViewIfNeeded();
//                mUnreadView1 = (TextView) mUnreadStubView.findViewById(R.id.list_item_unread1);
//            }
//            mUnreadView1.setText("");
//            mUnreadView1.setBackground(getUnreadViewBackground(SMALL_BADGE_WIDTH / 2));
//            MessageUtils.setMargins(context, mUnreadView1, SMALL_BADGE_MARGIN, 0, 0, 0);
//            mUnreadView1.setVisibility(VISIBLE);
//            return;
//        }
//        if (unreadCount > 1) {
//            if (null != mUnreadView1) {
//                mUnreadView1.setVisibility(GONE);
//            }
//            if (null == mUnreadView2) {
//                inflateUnreadStubViewIfNeeded();
//                mUnreadView2 = (TextView) mUnreadStubView.findViewById(R.id.list_item_unread2);
//            }
//            if (unreadCount < 100) {
//                mUnreadView2.setText(String.valueOf(unreadCount));
//            } else {
//                mUnreadView2.setText("...");
//            }
//            mUnreadView2.setBackground(getUnreadViewBackground(BADGE_WIDTH / 2));
//            MessageUtils.setMargins(context, mUnreadView2, BADGE_MARGIN, 0, 0, 0);
//            mUnreadView2.setVisibility(VISIBLE);
//            return;
//        }
//    }

    private void updateUnreadView(Context context, boolean hasUnread) {
        if (!hasUnread) {
            hideUnreadView(context);
            return;
        }
        if (hasUnread) {
            if (null == mUnreadView1) {
                inflateUnreadStubViewIfNeeded();
                mUnreadView1 = (TextView) mUnreadStubView.findViewById(R.id.list_item_unread1);
            }
            mUnreadView1.setText("");
            mUnreadView1.setBackground(getUnreadViewBackground(SMALL_BADGE_WIDTH / 2));
//            MessageUtils.setMargins(context, mUnreadView1, SMALL_BADGE_MARGIN, 0, 0, 0);
            mUnreadView1.setVisibility(VISIBLE);
            return;
        }
    }

    private void hideUnreadView(Context context) {
        if (null != mUnreadView1) {
//            MessageUtils.setMargins(context, mUnreadView1, 0, 0, 0, 0);
            mUnreadView1.setVisibility(GONE);
        }
//        if (null != mUnreadView2) {
//            MessageUtils.setMargins(context, mUnreadView2, 0, 0, 0, 0);
//            mUnreadView2.setVisibility(GONE);
//        }
    }

    private void inflateUnreadStubViewIfNeeded() {
        if (null == mUnreadStubView) {
            ViewStub stub = (ViewStub) findViewById(R.id.unread_view_stub);
            mUnreadStubView = stub.inflate();
        }
    }

    public ShapeDrawable getUnreadViewBackground(int dipRadius) {
        int bgColor_red = mContext.getResources().getColor(R.color.unread_msg_count_bg_color_red);
        int radius = MessageUtils.dip2Px(mContext, dipRadius);
        float[] radiusArray = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
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

    //lichao add
    private void updateCheckBox(boolean isChecked) {
        if (!mIsCheckBoxMode) {
            if (null != mCheckBox) {
                mCheckBox.setVisibility(View.GONE);
            }
            return;
        }
        if (null == mCheckBox) {
            inflateCheckBoxStubView();
            mCheckBox = (CheckBox) mCheckBoxStubView.findViewById(R.id.list_item_check_box);
        }
        mCheckBox.setChecked(isChecked);
        mCheckBox.setVisibility(View.VISIBLE);
    }

    private void inflateCheckBoxStubView() {
        if (null == mCheckBoxStubView) {
            ViewStub stub = (ViewStub) findViewById(R.id.checkbox_stub);
            mCheckBoxStubView = stub.inflate();
        }
    }

    //lichao add in 2016-12-12 begin
    private void updateAttachIcon(Context context, boolean hasAttachment) {
        if (!hasAttachment) {
            if (null != mAttachmentView) {
//                MessageUtils.setMargins(context, mAttachmentView,0,0,0,0);
                mAttachmentView.setVisibility(View.GONE);
            }
        } else {
            if (null == mAttachmentView) {
                inflateSimAttachStubView();
                mAttachmentView = (ImageView) mSimAttachStubView.findViewById(R.id.attachment);
            }
//            MessageUtils.setMargins(context, mAttachmentView,20,0,0,0);
            mAttachmentView.setVisibility(View.VISIBLE);
        }
    }

    private void updateSimIcon(Context context, int slotId, boolean isShowSimIcon, boolean hasAttachment) {
        if (!isShowSimIcon) {
            if (null != mSimIconView) {
                MessageUtils.setMargins(context, mSimIconView, 0, 0, 0, 0);
                mSimIconView.setVisibility(View.GONE);
            }
        } else {
            if (null == mSimIconView) {
                inflateSimAttachStubView();
                mSimIconView = (ImageView) mSimAttachStubView.findViewById(R.id.sim_indicator_icon);
            }
            Drawable mSimIndicatorIcon = MessageUtils.getMultiSimIcon(context, slotId);
            mSimIconView.setImageDrawable(mSimIndicatorIcon);
            if (hasAttachment) {
                MessageUtils.setMargins(context, mSimIconView, 11, 0, 0, 0);
            }
//            else {
//                MessageUtils.setMargins(context, mSimIconView,20,0,0,0);
//            }
            mSimIconView.setVisibility(View.VISIBLE);
        }
    }

    private void inflateSimAttachStubView() {
        if (null == mSimAttachStubView) {
            ViewStub stub = (ViewStub) findViewById(R.id.sim_attach_icon_stub);
            mSimAttachStubView = stub.inflate();
        }
    }

    /*
    private void updateSubjectViewMaxWidth(Context context, boolean isShowSimIcon, boolean hasAttachment){
        int right_margins = 0;
        if(hasAttachment){
            right_margins += (20+12);
        }
        if(isShowSimIcon){
            if(hasAttachment){
                right_margins += (11+13);
            }else {
                right_margins += (20+13);
            }
        }
        int left_margins = 0;
        if(mIsCheckBoxMode){
            left_margins = 20 + 20;
        }
        int item_padding = 20*2;
        int subjectMaxWidthDip = mScreenWidthDip - item_padding - right_margins - left_margins;
        mSubjectView.setMaxWidth(MessageUtils.dip2Px(context, subjectMaxWidthDip));
    }
    */

    private void setSubjectMargins(Context context, boolean isShowSimIcon, boolean hasAttachment) {
        int right_margins = 0;
        if (hasAttachment) {
            right_margins += (20 + 12);
        }
        if (isShowSimIcon) {
            if (hasAttachment) {
                right_margins += (11 + 13);
            } else {
                right_margins += (20 + 13);
            }
        }
        int left_margins = 0;
        if (mIsCheckBoxMode) {
            left_margins = 20 + 20;
        }
        MessageUtils.setMargins(context, mSubjectView, left_margins, 0, right_margins, 0);
    }

    private void updateFromViewMaxWidth(Context context, boolean hasUnread){
        int width =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        int height =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        mDateView.measure(width,height);
        int dateViewWidth = mDateView.getMeasuredWidth();//93, 283
        int dateViewWidthDip = MessageUtils.px2dip(context, dateViewWidth);//93->31,283->94

        int date_margins = 20;
        int right_margins = dateViewWidthDip + date_margins;
//        if(unreadCount > 2){
//            right_margins += (BADGE_MARGIN+BADGE_WIDTH+1);
//        } else if(unreadCount == 1){
//            right_margins += (SMALL_BADGE_MARGIN+SMALL_BADGE_WIDTH+1);
//        }
        if(hasUnread){
            right_margins += (SMALL_BADGE_MARGIN+SMALL_BADGE_WIDTH+1);
        }

        int left_margins = 0;
        if(mIsCheckBoxMode){
            left_margins = 20 + 20;
        }
        int item_padding = 20*2;
        int fromMaxWidthDip = mScreenWidthDip - item_padding - right_margins - left_margins;
        //[2016-12-7 14:35: 183, 197, 206], [10:23: 246, 260, 269]
        //Log.v(TAG, "updateFromViewMaxWidth, fromMaxWidthDip = "+ fromMaxWidthDip);
        mFromView.setMaxWidth(MessageUtils.dip2Px(context, fromMaxWidthDip));
    }

    private void setFromMargins(Context context) {
        int left_margins = 0;
        if (mIsCheckBoxMode) {
            left_margins = 20 + 20;
        }
        MessageUtils.setMargins(context, mFromView, left_margins, 0, 0, 0);
    }

    private int mScreenWidthDip = 0;

    public void setScreenWidthDip(int screenWidthDip) {
        mScreenWidthDip = screenWidthDip;
    }
    //lichao add in 2016-12-12 end

}
