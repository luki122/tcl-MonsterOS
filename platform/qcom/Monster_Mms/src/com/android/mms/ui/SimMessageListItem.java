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
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SqliteWrapper;
import android.graphics.Bitmap;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import mst.provider.Telephony.Sms;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.LineHeightSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.WorkingMessage;
import com.android.mms.model.LayoutModel;
import com.android.mms.model.SlideModel;
import com.android.mms.transaction.Transaction;
import com.android.mms.transaction.TransactionBundle;
import com.android.mms.transaction.TransactionService;
import com.android.mms.util.DownloadManager;
import com.android.mms.util.ItemLoadedCallback;
import com.android.mms.util.ThumbnailManager.ImageLoaded;
import com.google.android.mms.ContentType;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.NotificationInd;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPersister;
import mst.app.dialog.AlertDialog;
import mst.widget.MstListView;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides view of a message in the SIM messages list.
 */
//SlideViewInterface, OnClickListener,
public class SimMessageListItem extends LinearLayout implements Checkable  {
    public static final String EXTRA_URLS = "com.android.mms.ExtraUrls";

    private static final String TAG = "SimMessageListItem";
    private static final boolean DEBUG = false;

    private boolean mIsCheck = false;

    private TextView mSimMessageAddress;
    private TextView mBodyTextView;
    private CheckBox mCheckBox;
    private MessageItem mMessageItem;
    private TextView mDateView;
    private boolean mMultiRecipients;

    public SimMessageListItem(Context context) {
        super(context);
    }

    public SimMessageListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mBodyTextView = (TextView) findViewById(R.id.sim_msg_body);
        mDateView = (TextView) findViewById(R.id.sim_msg_date);
        mSimMessageAddress = (TextView) findViewById(R.id.sim_msg_address);
        mCheckBox = (CheckBox) findViewById(R.id.sim_msg_check_box);
    }

    public void bindSimMessage(MessageItem msgItem, boolean convHasMultiRecipients, int position) {
        if (DEBUG) {
            Log.v(TAG, "bind for item: " + position + " old: " +
                   (mMessageItem != null ? mMessageItem.toString() : "NULL" ) +
                    " new " + msgItem.toString());
        }
        boolean sameItem = mMessageItem != null && mMessageItem.mMsgId == msgItem.mMsgId;
        mMessageItem = msgItem;

        mMultiRecipients = convHasMultiRecipients;

        setLongClickable(false);
        setClickable(false);    // let the list view handle clicks on the item normally. When
                                // clickable is true, clicks bypass the listview and go straight
                                // to this listitem. We always want the listview to handle the
                                // clicks first.

        bindCommonMessage(sameItem);

        if (mMessageItem.isOutgoingMessage()
                || mMessageItem.mBoxId == Sms.MESSAGE_TYPE_SENT
                || mMessageItem.isCdmaInboxMessage()) {
            mDateView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public MessageItem getMessageItem() {
        return mMessageItem;
    }

    private String buildTimestampLine(String timestamp) {
        if (!mMultiRecipients || mMessageItem.isMe() || TextUtils.isEmpty(mMessageItem.mContact)) {
            // Never show "Me" for messages I sent.
            return timestamp;
        }
        // This is a group conversation, show the sender's name on the same line as the timestamp.
        return mContext.getString(R.string.message_timestamp_format, mMessageItem.mContact,
                timestamp);
    }

    public TextView getBodyTextView() {
        return mBodyTextView;
    }

    private void bindCommonMessage(final boolean sameItem) {

        mCheckBox.setVisibility(mIsCheckBoxMode ? View.VISIBLE : View.GONE);
        mCheckBox.setChecked(mIsCheck);

        mBodyTextView.setVisibility(View.VISIBLE);

        // Since the message text should be concatenated with the sender's
        // address(or name), I have to display it here instead of
        // displaying it by the Presenter.
        mBodyTextView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

        boolean haveLoadedPdu = mMessageItem.isSms() || mMessageItem.mSlideshow != null;


        // Add SIM sms address above body.
        if (isSimCardMessage()) {
            mSimMessageAddress.setVisibility(VISIBLE);
            SpannableStringBuilder buf = new SpannableStringBuilder();
            buf.append(Contact.get(mMessageItem.mAddress, true).getName());
            mSimMessageAddress.setText(buf);
        }

        // Get and/or lazily set the formatted message from/on the
        // MessageItem.  Because the MessageItem instances come from a
        // cache (currently of size ~50), the hit rate on avoiding the
        // expensive formatMessage() call is very high.
        CharSequence formattedMessage = mMessageItem.getCachedFormattedMessage();
        if (formattedMessage == null) {
            formattedMessage = formatMessage(mMessageItem,
                                             mMessageItem.mBody,
                                             mMessageItem.mSubId,
                                             mMessageItem.mSubject,
                                             mMessageItem.mHighlight,
                                             mMessageItem.mTextContentType);
            mMessageItem.setCachedFormattedMessage(formattedMessage);
        }
        if (!sameItem || haveLoadedPdu) {
            mBodyTextView.setText(formattedMessage);
        }

        // If we're in the process of sending a message (i.e. pending), then we show a "SENDING..."
        // string in place of the timestamp.
        if (!sameItem || haveLoadedPdu) {
            String times_line = buildTimestampLine(mMessageItem.mTimestamp);

            if (isSimCardMessage()) {
                times_line = MessageUtils.formatTimeStampStringForItem(mContext, mMessageItem.mDate);
            }
            if (!TextUtils.isEmpty(times_line)) {
                mDateView.setVisibility(View.VISIBLE);
                mDateView.setText(times_line);
            }
            //lichao modify in 2016-08-22 end
        }

        requestLayout();
    }

    private CharSequence formatMessage(MessageItem msgItem, String body,
                                       int subId, String subject, Pattern highlight,
                                       String contentType) {
        SpannableStringBuilder buf = new SpannableStringBuilder();

        boolean hasSubject = !TextUtils.isEmpty(subject);
        if (hasSubject) {
            buf.append(mContext.getResources().getString(R.string.inline_subject, subject));
        }

        if (!TextUtils.isEmpty(body)) {
            // Converts html to spannable if ContentType is "text/html".
            if (contentType != null && ContentType.TEXT_HTML.equals(contentType)) {
                buf.append("\n");
                buf.append(Html.fromHtml(body));
            } else {
                if (hasSubject) {
                    buf.append(" - ");
                }
                buf.append(body);
            }
        }

        return buf;
    }

    private boolean isSimCardMessage() {
        return mContext instanceof SimMessageList;
    }

    @Override
    public boolean isChecked() {
        return mIsCheck;
    }

    @Override
    public void setChecked(boolean arg0) {
        mIsCheck = arg0;
    }

    @Override
    public void toggle() {
    }

    private boolean mIsCheckBoxMode = false;
    public void setCheckBoxEnable(boolean flag) {
        mIsCheckBoxMode = flag;
    }
    public boolean getCheckBoxEnable() {
        return mIsCheckBoxMode;
    }
}
