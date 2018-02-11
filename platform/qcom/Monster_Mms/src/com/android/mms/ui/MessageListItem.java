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

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import android.app.AlertDialog;
import mst.app.dialog.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SqliteWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Browser;
import android.provider.Settings;
import android.provider.ContactsContract.Profile;
import mst.provider.Telephony.Sms;
import mst.provider.Telephony.Mms;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.LineHeightSpan;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.mms.LogTag;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.WorkingMessage;
import com.android.mms.model.LayoutModel;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.transaction.Transaction;
import com.android.mms.transaction.TransactionBundle;
import com.android.mms.transaction.TransactionService;
import com.android.mms.ui.WwwContextMenuActivity;
import com.android.mms.util.DownloadManager;
import com.android.mms.util.ItemLoadedCallback;
import com.android.mms.util.ThumbnailManager.ImageLoaded;
import com.google.android.mms.ContentType;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.NotificationInd;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPersister;

//lichao add begin
import android.graphics.Color;
import android.text.Layout;
import mst.widget.MstListView;
//import com.android.mms.util.FolderTextView;
//lichao add end
//XiaoYuan SDK start
import com.xy.smartsms.iface.IXYSmartSmsHolder;
import com.xy.smartsms.iface.IXYSmartSmsListItemHolder;
import com.xy.smartsms.manager.XYBubbleListItem;
//XiaoYuan SDK end
//begin tangyisen
import mst.widget.FoldProgressBar;
//end tangyisen

/**
 * This class provides view of a message in the messages list.
 */
//XiaoYuan SDK add ",IXYSmartSmsListItemHolder"
public class MessageListItem extends LinearLayout implements
        SlideViewInterface, OnClickListener, Checkable, IXYSmartSmsListItemHolder  {
    public static final String EXTRA_URLS = "com.android.mms.ExtraUrls";

    private static final String TAG = "MessageListItem";
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_BODY_TEXT = false;//lichao add
    private static final boolean DEBUG_DONT_LOAD_IMAGES = false;
    // The message is from Browser
    private static final String BROWSER_ADDRESS = "Browser Information";
    private static final String CANCEL_URI = "canceluri";
    // transparent background
    private static final int ALPHA_TRANSPARENT = 0;
    private static final int KILOBYTE = 1024;

    static final int MSG_LIST_EDIT    = 1;
    static final int MSG_LIST_PLAY    = 2;
    static final int MSG_LIST_DETAILS = 3;
    public static final int MSG_LIST_SHOW_EDIT = 4;
    static final int MSG_LIST_RESEND = 5;

    static final int SHOW_MMS_VIEW_NONE = -1;
    static final int SHOW_MMS_VIEW_IMAGE = 0;
    static final int SHOW_MMS_VIEW_AUDIO = 1;

    private View mMmsView;
    private CircleImageView mImageView;
    private View mMmsViewImage;
    private View mMmsViewAudio;
    private View mMmsAudioShow;
    private View mMmsViewAudioImg;
    private View mMmsViewAudioText;
    private ImageView mSendFailed;
    private ImageButton mSlideShowButton;

    private StretchyTextView mBodyTextView;
    private View mMmsTextDivider;

    private ImageView mDownloadButton;
    private ComposeMessageSendRemind mDownloading;
    private LinearLayout mMmsLayout;
    private CheckBox mCheckBox;
    private Handler mHandler;
    private MessageItem mMessageItem;
    private TextView mDateView;
    public View mMessageBlock;
    private Presenter mPresenter;
    private int mPosition;      // for debugging
    private ImageLoadedCallback mImageLoadedCallback;
    private boolean mMultiRecipients;
    private int mManageMode;

    private View mMessageSendRemind;
    private StretchyTextView spreadTextView ;
    private boolean mIsSendItem;
    private ViewGroup mRichItemGroupLayout;
    private ImageView mSwitchRichBubbleIcon = null;

    private boolean mIsCheckBoxMode = false;

    public MessageListItem(Context context) {
        super(context);
    }

    public MessageListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if(DEBUG) Log.d(TAG, "onFinishInflate()...");

        mBodyTextView = (StretchyTextView) findViewById(R.id.text_view_buttom);
        setBodyTextViewClickListener();

        mMmsTextDivider = findViewById(R.id.mms_text_divider);
        mDateView = (TextView) findViewById(R.id.date_view);
        mSendFailed = (ImageView) findViewById(R.id.send_failed);
        if(null != mSendFailed) {
            mSendFailed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    if(!mIsCheckBoxMode) {
                        if(null != mMessageSendRemind) {
                            sendMessage(mMessageItem, MSG_LIST_RESEND);
                        }
                    }
                }
            });
        }
        mMessageBlock = findViewById(R.id.message_block);
        mMmsLayout = (LinearLayout) findViewById(R.id.mms_layout_view_parent);

        mCheckBox = (CheckBox) findViewById(R.id.list_item_check_box);
        mMessageSendRemind = findViewById(R.id.message_list_item_send_remind_stub);
        /*mRichItemGroupLayout = (ViewGroup)findViewById(R.id.layout_duoqu_rich_item_group);
        MessageUtils.hideView(mRichItemGroupLayout);*/
        mSwitchRichBubbleIcon = (ImageView)findViewById(R.id.img_id_switch_rich);
        MessageUtils.hideView(mSwitchRichBubbleIcon);
    }

    public void markAsSelected(boolean selected) {
        if (mCheckBox != null) {
            mCheckBox.setChecked(selected);
        }
    }

    private void setBodyTextViewClickListener() {
        mBodyTextView.setOnDoubleClickListener(new StretchyTextView.OnDoubleClickListener() {
            @Override
            public void onDoubleClick() {
                // TODO Auto-generated method stub
                if (!mIsCheckBoxMode) {
                    MessageUtils.showMstSmsMessageContent(getContext(), mBodyTextView.getContentView().getText().toString());
                }
            }
        });
        mBodyTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View arg0) {
                // TODO Auto-generated method stub
                sendMessage(mMessageItem, MSG_LIST_SHOW_EDIT);
                return true;
            }
        });
        mBodyTextView.getContentView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View arg0) {
                // TODO Auto-generated method stub
                sendMessage(mMessageItem, MSG_LIST_SHOW_EDIT);
                return true;
            }
        });
    }

    public void bindMessageItem(MessageItem msgItem, boolean convHasMultiRecipients, int position) {
        if (DEBUG) {
            Log.v(TAG, "bind for item: " + position + " old: " +
                   (mMessageItem != null ? mMessageItem.toString() : "NULL" ) +
                    " new " + msgItem.toString());
        }
        //XiaoYuan SDK modify "boolean sameItem" to "mSameItem" begin
        mSameItem = mMessageItem != null && mMessageItem.mMsgId == msgItem.mMsgId;
        //XiaoYuan SDK modify end
        mMessageItem = msgItem;

        mPosition = position;
        mMultiRecipients = convHasMultiRecipients;

        setLongClickable(false);
        setClickable(false);

        switch (msgItem.mMessageType) {
            case PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND:
                bindNotifInd();
                break;
            default:
                initBubbleModelForItem(msgItem);
                if (DEBUG) {
                    Log.v(TAG, "bindMessageItem, msgItem.mHighlight = " + msgItem.mHighlight);
                }
                if(msgItem.getIsRichBubbleItem()){
                    bindRichBubbleView();
                }else{
                    bindCommonMessage(mSameItem);
                }
                //showDateView(mSameItem);
                //XiaoYuan SDK modify end
                break;
        }
        //showDateView(mSameItem);
    }

    public void unbindMessageItem() {
        // Clear all references to the message item, which can contain attachments and other
        // memory-intensive objects
        if (mImageView != null) {
            mImageView.setOnClickListener(null);
        }
        if (mSlideShowButton != null) {
            mSlideShowButton.setTag(null);
        }
        if (mMmsViewAudio != null) {
            mMmsViewAudio.setOnClickListener(null);
        }
        // leave the presenter in case it's needed when rebound to a different MessageItem.
        if (mPresenter != null) {
            mPresenter.cancelBackgroundLoading();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        unbindMessageItem();
        super.onDetachedFromWindow();
    }

    public MessageItem getMessageItem() {
        return mMessageItem;
    }

    public void setMsgListItemHandler(Handler handler) {
        mHandler = handler;
    }

    private int getFormatSize(int size) {
        return (size + KILOBYTE - 1) / KILOBYTE;
    }

    private void bindNotifInd() {
        showMmsView(false, SHOW_MMS_VIEW_NONE);
        if (mMessageItem.mMessageSize == 0
            && TextUtils.isEmpty(mMessageItem.mExpireOnTimestamp)) {
            mMessageItem.setOnPduLoaded(new MessageItem.PduLoadedCallback() {
                public void onPduLoaded(MessageItem messageItem) {
                    if (DEBUG) {
                        Log.v(TAG, "PduLoadedCallback in MessageListItem for item: "
                                + mPosition + " " + (mMessageItem == null ? "NULL"
                                        : mMessageItem.toString())
                                + " passed in item: " + (messageItem == null ? "NULL"
                                        : messageItem.toString()));
                    }
                    if (messageItem != null
                            && mMessageItem != null
                            && messageItem.getMessageId() == mMessageItem.getMessageId()
                            && (mMessageItem.mMessageSize != 0 || !TextUtils
                                    .isEmpty(mMessageItem.mExpireOnTimestamp))) {
                        bindNotifInd();
                    }
                }
            });
        } else {
            String msgSizeText = mContext.getString(R.string.message_size_label)
                    + String.valueOf(getFormatSize(mMessageItem.mMessageSize))
                    + mContext.getString(R.string.kilobyte);

            CharSequence formattedMessage = formatMessage(mMessageItem, null,
                    mMessageItem.mSubId, mMessageItem.mSubject,
                    mMessageItem.mHighlight, mMessageItem.mTextContentType);
            MessageUtils.hideView(mMmsTextDivider);
            if (null != mBodyTextView) {
                mBodyTextView.setVisibility(View.VISIBLE);
                mBodyTextView.setContent(buildTimestampLine(msgSizeText + "\n"
                    + mMessageItem.mExpireOnTimestamp));
            }
        }

        switch (mMessageItem.getMmsDownloadStatus()) {
            case DownloadManager.STATE_PRE_DOWNLOADING:
            case DownloadManager.STATE_DOWNLOADING:
                showDownloadingAttachment();
                break;
            case DownloadManager.STATE_UNKNOWN:
            case DownloadManager.STATE_UNSTARTED:
                DownloadManager downloadManager = DownloadManager.getInstance();
                boolean autoDownload = downloadManager.isAuto();
                boolean dataSuspended = (MmsApp.getApplication().getTelephonyManager()
                        .getDataState() == TelephonyManager.DATA_SUSPENDED);

                // If we're going to automatically start downloading the mms attachment, then
                // don't bother showing the download button for an instant before the actual
                // download begins. Instead, show downloading as taking place.
                if (autoDownload && !dataSuspended) {
                    showDownloadingAttachment();
                    break;
                }
            case DownloadManager.STATE_TRANSIENT_FAILURE:
            case DownloadManager.STATE_PERMANENT_FAILURE:
            default:
                inflateDownloadControls();
                MessageUtils.hideView(mDownloading);
                MessageUtils.showView(mDownloadButton);
                if(null != mDownloadButton) {
                    mDownloadButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                NotificationInd nInd = (NotificationInd) PduPersister.getPduPersister(
                                        mContext).load(mMessageItem.mMessageUri);
                                Log.d(TAG, "Download notify Uri = " + mMessageItem.mMessageUri);
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                builder.setTitle(R.string.download);
                                builder.setCancelable(true);
                                // Judge notification weather is expired
                                if (nInd.getExpiry() < System.currentTimeMillis() / 1000L) {
                                    // builder.setIcon(R.drawable.ic_dialog_alert_holo_light);
                                    builder.setMessage(mContext
                                            .getString(R.string.service_message_not_found));
                                    builder.show();
                                    SqliteWrapper.delete(mContext, mContext.getContentResolver(),
                                            mMessageItem.mMessageUri, null, null);
                                    return;
                                }
                                // Judge whether memory is full
                                else if (MessageUtils.isMmsMemoryFull()) {
                                    builder.setMessage(mContext.getString(R.string.sms_full_body));
                                    builder.show();
                                    return;
                                }
                                // Judge whether message size is too large
                                else if ((int) nInd.getMessageSize() >
                                          MmsConfig.getMaxMessageSize()) {
                                    builder.setMessage(mContext.getString(R.string.mms_too_large));
                                    builder.show();
                                    return;
                                }
                                // If mobile data is turned off, inform user start data and try again.
                                /*else if (MessageUtils.isMobileDataDisabled(mContext)) {
                                    builder.setMessage(mContext.getString(R.string.inform_data_off));
                                    builder.show();
                                    return;
                                }*/
                            } catch (MmsException e) {
                                Log.e(TAG, e.getMessage(), e);
                                return;
                            }
                            MessageUtils.hideView(mDownloadButton);
                            MessageUtils.showView(mDownloading);
                            Intent intent = new Intent(mContext, TransactionService.class);
                            intent.putExtra(TransactionBundle.URI, mMessageItem.mMessageUri.toString());
                            intent.putExtra(TransactionBundle.TRANSACTION_TYPE,
                                    Transaction.RETRIEVE_TRANSACTION);
                            intent.putExtra("sub_id", mMessageItem.mSubId);
                            //begin tangyisen add threadid depend on
                            intent.putExtra("thread_id", mMessageItem.mThreadId);
                            //end tangyisen

                            mContext.startService(intent);

                            DownloadManager.getInstance().markState(
                                     mMessageItem.mMessageUri, DownloadManager.STATE_PRE_DOWNLOADING);
                        }
                    });
                }
                break;
        }
        MessageUtils.hideView(mSendFailed);
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

    private void showDownloadingAttachment() {
        inflateDownloadControls();
        MessageUtils.showView(mDownloading);
        MessageUtils.hideView(mDownloadButton);
    }

    public TextView getBodyTextView() {
        return mBodyTextView.getContentView();
    }

    private void bindCommonMessage(final boolean sameItem) {
        MessageUtils.hideView(mDownloading);
        MessageUtils.hideView(mDownloadButton);
        MessageUtils.hideView(mRichItemGroupLayout);
        //mBodyTextView.getContentView().setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        boolean haveLoadedPdu = mMessageItem.isSms() || mMessageItem.mSlideshow != null;
        // Here we're avoiding reseting the avatar to the empty avatar when we're rebinding
        // to the same item. This happens when there's a DB change which causes the message item
        // cache in the MessageListAdapter to get cleared. When an mms MessageItem is newly
        // created, it has no info in it except the message id. The info is eventually loaded
        // and bindCommonMessage is called again (see onPduLoaded below). When we haven't loaded
        // the pdu, we don't want to call updateAvatarView because it
        // will set the avatar to the generic avatar then when this method is called again
        // from onPduLoaded, it will reset to the real avatar. This test is to avoid that flash.
        if (!sameItem || haveLoadedPdu) {
            boolean isSelf = Sms.isOutgoingFolder(mMessageItem.mBoxId);
            String addr = isSelf ? null : mMessageItem.mAddress;
            //After pdu loaded, update the text view according to the slide-layout setting.
        }

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
            if (formattedMessage.length() == 0) {
                MessageUtils.hideView(mBodyTextView);
                MessageUtils.hideView(mMmsTextDivider);
            } else {
                MessageUtils.showView(mBodyTextView);
                mBodyTextView.setContent(formattedMessage);
                if (mMessageItem.isMms()) {
                    MessageUtils.showView(mMmsTextDivider);
                } else {
                    MessageUtils.hideView(mMmsTextDivider);
                }
            }
        }
        // Debugging code to put the URI of the image attachment in the body of the list item.
        if (DEBUG_BODY_TEXT) {
            String debugText = null;
            if (mMessageItem.mSlideshow == null) {
                debugText = "NULL slideshow";
            } else {
                SlideModel slide = mMessageItem.mSlideshow.get(0);
                if (slide == null) {
                    debugText = "NULL first slide";
                } else if (!slide.hasImage()) {
                    debugText = "Not an image";
                } else {
                    debugText = slide.getImage().getUri().toString();
                }
            }
            mBodyTextView.setVisibility(View.VISIBLE);
            mBodyTextView.setContent(mPosition + ": " + debugText);
            if (mMessageItem.isMms()) {
                MessageUtils.showView(mMmsTextDivider);
            } else {
                MessageUtils.hideView(mMmsTextDivider);
            }
        }

        // If we're in the process of sending a message (i.e. pending), then we show a "SENDING..."
        // string in place of the timestamp.
        if (!sameItem || haveLoadedPdu) {
            if (mMessageSendRemind != null) {
                if (mMessageItem.isSending()) {
                    mMessageSendRemind.setVisibility(View.VISIBLE);
                }else if(mMessageSendRemind.getVisibility() == View.VISIBLE) {
                    mMessageSendRemind.setVisibility(View.GONE);
                }
            }
        }
        if (mMessageItem.isSms()) {
            showMmsView(false, SHOW_MMS_VIEW_NONE);
            mMessageItem.setOnPduLoaded(null);
        } else {
            if (DEBUG) {
                Log.v(TAG, "bindCommonMessage for item: " + mPosition + " " +
                        mMessageItem.toString() +
                        " mMessageItem.mAttachmentType: " + mMessageItem.mAttachmentType +
                        " sameItem: " + sameItem);
            }
            if (mMessageItem.mAttachmentType != WorkingMessage.TEXT) {
                //now maybe just know is or not text,don't know audio or image
                if (!sameItem) {
                    setImage(null, null);
                }
                setOnClickListener(mMessageItem);
                drawPlaybackButton(mMessageItem);
            } else {
                showMmsView(false, SHOW_MMS_VIEW_NONE);
            }
            if (mMessageItem.mSlideshow == null) {
                final int mCurrentAttachmentType = mMessageItem.mAttachmentType;
                mMessageItem.setOnPduLoaded(new MessageItem.PduLoadedCallback() {
                    public void onPduLoaded(MessageItem messageItem) {
                        if (DEBUG) {
                            Log.v(TAG, "PduLoadedCallback in MessageListItem for item: " + mPosition +
                                    " " + (mMessageItem == null ? "NULL" : mMessageItem.toString()) +
                                    " passed in item: " +
                                    (messageItem == null ? "NULL" : messageItem.toString()));
                        }
                        if (messageItem != null && mMessageItem != null &&
                                messageItem.getMessageId() == mMessageItem.getMessageId()) {
                            mMessageItem.setCachedFormattedMessage(null);
                            bindCommonMessage(
                                    mCurrentAttachmentType == messageItem.mAttachmentType);
                        }
                    }
                });
            } else {
                if (mPresenter == null) {
                    mPresenter = PresenterFactory.getPresenter(
                            "MmsThumbnailPresenter", mContext,
                            this, mMessageItem.mSlideshow);
                } else {
                    mPresenter.setModel(mMessageItem.mSlideshow);
                    mPresenter.setView(this);
                }
                if (mImageLoadedCallback == null) {
                    mImageLoadedCallback = new ImageLoadedCallback(this);
                } else {
                    mImageLoadedCallback.reset(this);
                }
                mPresenter.present(mImageLoadedCallback);
            }
        }
        drawRightStatusIndicator(mMessageItem);
        //tangyisen check need?
        requestLayout();
    }

    public void showDateView(final boolean show){
        if(show) {
            String times_line = buildTimestampLine(mMessageItem.mTimestamp);
            MessageUtils.showView(mDateView);
            mDateView.setText(times_line);
        } else {
            MessageUtils.hideView(mDateView);
        }
    }

    static private class ImageLoadedCallback implements ItemLoadedCallback<ImageLoaded> {
        private long mMessageId;
        private final MessageListItem mListItem;

        public ImageLoadedCallback(MessageListItem listItem) {
            mListItem = listItem;
            mMessageId = listItem.getMessageItem().getMessageId();
        }

        public void reset(MessageListItem listItem) {
            mMessageId = listItem.getMessageItem().getMessageId();
        }

        public void onItemLoaded(ImageLoaded imageLoaded, Throwable exception) {
            if (DEBUG_DONT_LOAD_IMAGES) {
                return;
            }
            // Make sure we're still pointing to the same message. The list item could have
            // been recycled.
            MessageItem msgItem = mListItem.mMessageItem;
            if (msgItem != null && msgItem.getMessageId() == mMessageId) {
                if (imageLoaded.mIsVideo) {
                    mListItem.setVideoThumbnail(null, imageLoaded.mBitmap);
                } else {
                    mListItem.setImage(null, imageLoaded.mBitmap);
                }
            }
        }
    }

    @Override
    public void startAudio() {
        // TODO Auto-generated method stub
    }

    @Override
    public void startVideo() {
        // TODO Auto-generated method stub
    }

    @Override
    public void setAudio(Uri audio, String name, Map<String, ?> extras) {
        // TODO Auto-generated method stub
        showMmsView(true, SHOW_MMS_VIEW_AUDIO);
    }

    @Override
    public void setImage(String name, Bitmap bitmap) {
        showMmsView(true, SHOW_MMS_VIEW_IMAGE);
        try {
            mImageView.setType(CircleImageView.IMAGE_TYEP);
            mImageView.setSrc(bitmap);
            mImageView.setVisibility(VISIBLE);
        } catch (java.lang.OutOfMemoryError e) {
            Log.e(TAG, "setImage: out of memory: ", e);
        }
    }

    private void showMmsView(boolean visible, int type) {
        if (mMmsView == null) {
            if(!visible) {
                return;
            }
            mMmsView = findViewById(R.id.mms_view);
            // if mMmsView is still null here, that mean the mms section hasn't been inflated
            if (visible && mMmsView == null) {
                //inflate the mms view_stub
                View mmsStub = findViewById(R.id.mms_layout_view_stub);
                mmsStub.setVisibility(View.VISIBLE);
                mMmsView = findViewById(R.id.mms_view);
            }
        }
        if (mMmsView != null) {
            if (mMmsViewImage == null) {
                mMmsViewImage = findViewById(R.id.mms_view_image);
            }
            if (mMmsViewImage != null) {
                if (mImageView == null) {
                    mImageView = (CircleImageView) findViewById(R.id.image_view);
                    mImageView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View arg0) {
                            // TODO Auto-generated method stub
                            sendMessage(mMessageItem, MSG_LIST_SHOW_EDIT);
                            return true;
                        }
                    });
                }
                if (mSlideShowButton == null) {
                    mSlideShowButton = (ImageButton) findViewById(R.id.play_slideshow_button);
                }
            }
            if (mMmsViewAudio == null) {
                mMmsViewAudio = findViewById(R.id.mms_view_audio);
                mMmsViewAudio.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View arg0) {
                        // TODO Auto-generated method stub
                        sendMessage(mMessageItem, MSG_LIST_SHOW_EDIT);
                        return true;
                    }
                });
            }
            if (mMmsViewAudio != null) {
                if(mMmsViewAudioImg == null) {
                    mMmsViewAudioImg = findViewById(R.id.mms_view_audio_img);
                }
                if(mMmsViewAudioImg == null) {
                    mMmsViewAudioText = findViewById(R.id.mms_view_audio_text);
                }
            }
            if (visible) {
                if(type == SHOW_MMS_VIEW_AUDIO) {
                    MessageUtils.showView(mMmsView);
                    MessageUtils.hideView(mMmsViewImage);
                    MessageUtils.hideView(mImageView);
                    MessageUtils.showView(mMmsViewAudio);
                    MessageUtils.showView(mMmsViewAudioImg);
                    MessageUtils.showView(mMmsViewAudioText);
                } else if(type == SHOW_MMS_VIEW_IMAGE){
                    MessageUtils.showView(mMmsView);
                    MessageUtils.showView(mMmsViewImage);
                    MessageUtils.showView(mImageView);
                    MessageUtils.hideView(mMmsViewAudio);
                    MessageUtils.hideView(mMmsViewAudioImg);
                    MessageUtils.hideView(mMmsViewAudioText);
                } else {
                    MessageUtils.hideView(mMmsView);
                    MessageUtils.hideView(mImageView);
                    MessageUtils.hideView(mMmsViewImage);
                    MessageUtils.hideView(mMmsViewAudio);
                    MessageUtils.hideView(mMmsViewAudioImg);
                    MessageUtils.hideView(mMmsViewAudioText);
                }
            } else {
                MessageUtils.hideView(mMmsView);
                MessageUtils.hideView(mImageView);
                MessageUtils.hideView(mMmsViewImage);
                MessageUtils.hideView(mMmsViewAudio);
                MessageUtils.hideView(mMmsViewAudioImg);
                MessageUtils.hideView(mMmsViewAudioText);
            }
        }
    }

    private void inflateDownloadControls() {
        if (mDownloadButton == null) {
            findViewById(R.id.mms_downloading_view_stub).setVisibility(VISIBLE);
            mDownloadButton = (ImageView) findViewById(R.id.img_begin_downloading);
            mDownloading = (ComposeMessageSendRemind) findViewById(R.id.prog_downloading);
        }
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
        //for search result
        if (highlight != null) {
            Matcher m = highlight.matcher(buf.toString());
            while (m.find()) {
                int hightlight_color = mContext.getResources().getColor(R.color.search_hightlight_color);
                buf.setSpan(new ForegroundColorSpan(hightlight_color), m.start(), m.end(), 0);
            }
        }
        return buf;
    }

    private boolean isSimCardMessage() {
        //never can be true here, copy a MessageListItem.java to SimMessageListItem.java
        //return mContext instanceof ManageSimMessages;
        return false;
    }

    public void setManageSelectMode(int manageMode) {
        mManageMode = manageMode;
    }

    private void drawPlaybackButton(MessageItem msgItem) {
        switch (msgItem.mAttachmentType) {
            case WorkingMessage.SLIDESHOW:
            case WorkingMessage.AUDIO:
            case WorkingMessage.VIDEO:
                // Show the 'Play' button and bind message info on it.
                mSlideShowButton.setTag(msgItem);
                // Set call-back for the 'Play' button.
                mSlideShowButton.setOnClickListener(this);
                setLongClickable(false);
                break;
            default:
                mSlideShowButton.setVisibility(View.GONE);
                break;
        }
    }

    // OnClick Listener for the playback button
    @Override
    public void onClick(View v) {
        sendMessage(mMessageItem, MSG_LIST_PLAY);
    }

    private void sendMessage(MessageItem messageItem, int message) {
        if (mHandler != null) {
            Message msg = Message.obtain(mHandler, message);
            msg.obj = messageItem;
            msg.sendToTarget(); // See ComposeMessageActivity.mMessageListItemHandler.handleMessage
        }
    }

    private void sendMessage(MessageListItem messageListItem, int message) {
        if (mHandler != null) {
            Message msg = Message.obtain(mHandler, message);
            msg.obj = messageListItem;
            msg.sendToTarget(); // See ComposeMessageActivity.mMessageListItemHandler.handleMessage
        }
    }

    //same like setCheckBoxVisibility, setCheckBoxEnable
    public void setIsCheckBoxMode(boolean isCheckBoxMode) {
        mIsCheckBoxMode = isCheckBoxMode;
        if (mCheckBox != null) {
            mCheckBox.setVisibility(isCheckBoxMode ? View.VISIBLE: View.GONE);
        }
        //lichao add in 2016-11-11
        if(mXYBubbleListItem != null){
            mXYBubbleListItem.setIsCheckBoxMode(isCheckBoxMode);
        }
    }

    public boolean getIsCheckBoxMode() {
        return mIsCheckBoxMode;
    }

    public void setIsSendItem(boolean flag) {
        mIsSendItem = flag;
    }
    
    public boolean getIsSendItem() {
        return mIsSendItem;
    }

    public void resetItem() {
        if(null != mBodyTextView){
            mBodyTextView.reset();
        }
        showMmsView(false, SHOW_MMS_VIEW_NONE);
    }

    public void onMessageListItemClick() {
    }

    private void setOnClickListener(final MessageItem msgItem) {
        switch(msgItem.mAttachmentType) {
            case WorkingMessage.VCARD:
            case WorkingMessage.IMAGE:
            case WorkingMessage.VIDEO:
                mImageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendMessage(msgItem, MSG_LIST_PLAY);
                    }
                });
                break;
            case WorkingMessage.AUDIO:
                mMmsViewAudio.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendMessage(msgItem, MSG_LIST_PLAY);
                    }
                });
            default:
                mImageView.setOnClickListener(null);
                break;
            }
    }

    private void drawRightStatusIndicator(MessageItem msgItem) {
        if ((msgItem.isOutgoingMessage() && msgItem.isFailedMessage()) ||
                msgItem.mDeliveryStatus == MessageItem.DeliveryStatus.FAILED) {
            MessageUtils.showView(mSendFailed);
        } else {
            MessageUtils.hideView(mSendFailed);
        }
    }

    @Override
    public void setImageRegionFit(String fit) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setImageVisibility(boolean visible) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setText(String name, String text) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setTextVisibility(boolean visible) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setVideo(String name, Uri uri) {
    }

    @Override
    public void setVideoThumbnail(String name, Bitmap bitmap) {
        showMmsView(true, SHOW_MMS_VIEW_IMAGE);
        try {
            mImageView.setType(CircleImageView.VIDEO_TYEP);
            mImageView.setSrc(bitmap);
            if(bitmap == null) {
                MessageUtils.hideView(mSlideShowButton);
            } else {
                MessageUtils.showView(mSlideShowButton);
            }
            MessageUtils.showView(mImageView);
        } catch (java.lang.OutOfMemoryError e) {
            Log.e(TAG, "setVideo: out of memory: ", e);
        }
    }

    @Override
    public void setVideoVisibility(boolean visible) {
        // TODO Auto-generated method stub
    }

    @Override
    public void stopAudio() {
        // TODO Auto-generated method stub
    }

    @Override
    public void stopVideo() {
        // TODO Auto-generated method stub
    }

    @Override
    public void reset() {
    }

    @Override
    public void setVisibility(boolean visible) {
        // TODO Auto-generated method stub
    }

    @Override
    public void pauseAudio() {
        // TODO Auto-generated method stub

    }

    @Override
    public void pauseVideo() {
        // TODO Auto-generated method stub

    }

    @Override
    public void seekAudio(int seekTo) {
        // TODO Auto-generated method stub

    }

    @Override
    public void seekVideo(int seekTo) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setVcard(Uri lookupUri, String name) {
        showMmsView(true, SHOW_MMS_VIEW_NONE);
        try {
            //mImageView.setSrc(BiR.drawable.ic_attach_vcard);
            //mImageView.setImageResource(R.drawable.ic_attach_vcard);
            mImageView.setVisibility(VISIBLE);
        } catch (java.lang.OutOfMemoryError e) {
            // shouldn't be here.
            Log.e(TAG, "setVcard: out of memory: ", e);
        }
    }

    @Override
    public boolean isChecked() {
        //return mIsCheck;
        return mMessageItem.isChecked();
    }

    @Override
    public void setChecked(boolean arg0) {
        mMessageItem.setIsChecked(arg0);
    }

    @Override
    public void toggle() {
    }

    public void setBodyTextSize(float size) {
        if (mBodyTextView != null
                && mBodyTextView.getVisibility() == View.VISIBLE) {
            mBodyTextView.getContentView().setTextSize(size);
        }
    }

    /*<begin hewengao/xiaoyuan 20150824  impl IXYSmartSmsHolder */
    private boolean mSameItem =false;
    private IXYSmartSmsHolder mSmartSmsHolder =null;
    private XYBubbleListItem mXYBubbleListItem =null;
    @Override
    public void showDefaultListItem() {
        bindCommonMessage(mSameItem);
    }

    @Override
    public View getListItemView() {
        return MessageListItem.this;
    }

    @Override
    public IXYSmartSmsHolder getXySmartSmsHolder() {
        if(mSmartSmsHolder == null || mContext != null && mContext instanceof IXYSmartSmsHolder){
            mSmartSmsHolder= (IXYSmartSmsHolder)mContext;
        }
        return mSmartSmsHolder;
    }

    @Override
    public int getShowBubbleMode() {
        //lichao modify in 2016-11-12 for MessageListItem is the reusable view,
        //we should get unique data from the unique mMessageItem
//        if (mIsSendItem || null != mMessageItem.mHighlight) {
//            return XYBubbleListItem.DUOQU_SMARTSMS_SHOW_DEFAULT_PRIMITIVE;
//        }
//        if(mMessageItem.isSms() && getXySmartSmsHolder() != null && getXySmartSmsHolder().isNotifyComposeMessage()){
//            return XYBubbleListItem.DUOQU_SMARTSMS_SHOW_BUBBLE_RICH;
//        }
//        return XYBubbleListItem.DUOQU_SMARTSMS_SHOW_DEFAULT_PRIMITIVE;
        return mMessageItem.getBubbleModelForItem();
    }

    //boolean mIsRichBubbleMode = false;
    //int mBubbleModel = XYBubbleListItem.DUOQU_SMARTSMS_SHOW_DEFAULT_PRIMITIVE;

    //private void setRichBubbleMode(MessageItem msgItem){
    private void initBubbleModelForItem(MessageItem msgItem){
        int bubbleModelForItem = XYBubbleListItem.DUOQU_SMARTSMS_SHOW_DEFAULT_PRIMITIVE;
        boolean isRichBubbleItem = false;
        //boolean isSimpleByUser = mMessageItem.getHideRichBubbleByUser();
        if(/*!isSimpleByUser &&*/ mMessageItem.isSms()
                && getXySmartSmsHolder() != null
                && getXySmartSmsHolder().isNotifyComposeMessage()
                && msgItem.mBoxId == Sms.MESSAGE_TYPE_INBOX //!mIsSendItem
                && null == msgItem.mHighlight){
            bubbleModelForItem = XYBubbleListItem.DUOQU_SMARTSMS_SHOW_BUBBLE_RICH;
            isRichBubbleItem = true;
        }
        mMessageItem.setBubbleModelForItem(bubbleModelForItem);
        mMessageItem.setIsRichBubbleItem(isRichBubbleItem);
    }

    //private boolean isRichBubbleMode(){
    //    return mIsRichBubbleMode;
    //}
    
    private void bindRichBubbleView(){
        if(DEBUG) Log.d(TAG, "bindRichBubbleView()...");
        if(null == mRichItemGroupLayout) {
            findViewById(R.id.layout_duoqu_rich_item_group_stub).setVisibility(View.VISIBLE);
            mRichItemGroupLayout = (ViewGroup)findViewById(R.id.layout_duoqu_rich_item_group);
            MessageUtils.showView(mRichItemGroupLayout);
        }
        if(mXYBubbleListItem == null){
            mXYBubbleListItem = new XYBubbleListItem(getXySmartSmsHolder(), this);
        }
        if(mXYBubbleListItem != null){
            mXYBubbleListItem.bindBubbleView(mMessageItem);
            //lichao add for editmode on xiaoyuan rich bubble begin
            if (null != mHandler) {
                mXYBubbleListItem.setBubbleListItemHandler(mHandler);
            } else {
                Log.e(TAG, "bindRichBubbleView(), mHandler is null");
            }
            //lichao add for editmode on xiaoyuan rich bubble end
        }
    }
    /* hewengao/xiaoyuan 20150824  end>*/

    //lichao add for xiaoyuan SDK in 2016-08-25 begin
    public MstListView getListView() {
        return mSmartSmsHolder.getListView();
    }
    //lichao add for xiaoyuan SDK in 2016-08-25 end
}
