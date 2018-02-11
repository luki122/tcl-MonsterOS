package com.android.mms.ui;

import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.telephony.PhoneNumberUtils;

import cn.com.xy.sms.sdk.util.StringUtils;

import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.MmsApp;
import com.android.mms.ui.SearchActivity.TextViewSnippet;
import com.android.mms.R;

import java.util.List;

import com.android.mms.data.SearchListItemData;

public class MstSearchAdapter extends BaseAdapter {

    private static final String TAG = "Mms/MstSearchAdapter";
    private static final boolean DEBUG = true;

    private Context mContext;
    private List<SearchListItemData> mItemDatas;

    private String mSearchString = "";

    /*
    public static final int DATA_TYPE_UNKNOWN = -1;
    public static final int DATA_TYPE_HEADER = 0;
    public static final int DATA_TYPE_THREAD = 1;
    public static final int DATA_TYPE_MESSAGE = 2;
     */
    private static final int VIEW_TYPE_TOTAL_COUNT = 3;
    public static final int VIEW_TYPE_INVALID = -1;
    public static final int VIEW_TYPE_HEADER = 0;
    public static final int VIEW_TYPE_THREAD = 1;
    public static final int VIEW_TYPE_MESSAGE = 2;

    public static final String HIGH_LIGHT_TEXT = "highlight";
    public static final String HIGH_LIGHT_PLACE = "highlight_place";

    int mPreItemType = VIEW_TYPE_INVALID;
    private static final int POSITION_INVALID = -1;
    int mFirstThreadPosition = POSITION_INVALID;
    int mFirstMessagePosition = POSITION_INVALID;

    public MstSearchAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_TOTAL_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if(null == mItemDatas){
            return -1;
        }
        return mItemDatas.get(position).getDataType();
    }

    @Override
    public int getCount() {
        if(null == mItemDatas){
            return 0;
        }
        return mItemDatas.size();
    }

    @Override
    public SearchListItemData getItem(int position) {
        if(null == mItemDatas){
            return null;
        }
        return mItemDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setItemDatas(List<SearchListItemData> itemDatas) {
        this.mItemDatas = itemDatas;
    }

    //Recycling use
    public class ViewHolderHeader {
        TextView categorytitle;

        public ViewHolderHeader(View view) {
            categorytitle = (TextView) (view.findViewById(R.id.category_title));
        }
    }

    //Recycling use
    public class ViewHolder {
        TextViewSnippet title;
        TextViewSnippet subTitle;
        TextView date;
        ImageView simicon;

        public ViewHolder(View view) {
            title = (TextViewSnippet) (view.findViewById(R.id.search_item_title));
            subTitle = (TextViewSnippet) (view.findViewById(R.id.search_item_subtitle));
            date = (TextView) (view.findViewById(R.id.search_item_date));
            simicon = (ImageView) (view.findViewById(R.id.search_sim_indicator_icon));
        }
    }

    @Override
    public View getView(int position, final View convertView, final ViewGroup parent) {
        final int type = getItemViewType(position);
        SearchListItemData itemData = getItem(position);
        View view = null;
        ViewHolderHeader viewHolderHeader;
        ViewHolder viewHolder;
        String highlight_title = "";
        String highlight_subTitle = "";
        long threadId = -1;
        switch (type) {
            case VIEW_TYPE_HEADER:
                if (convertView == null) {
                    view = View.inflate(mContext, R.layout.search_item_header, null);
                    viewHolderHeader = new ViewHolderHeader(view);
                    view.setTag(viewHolderHeader);
                } else {
                    view = convertView;
                    viewHolderHeader = (ViewHolderHeader) view.getTag();
                }
                //viewHolderHeader.categorytitle.setVisibility(View.VISIBLE);
                viewHolderHeader.categorytitle.setText(itemData.getTitleValue());
                break;
            case VIEW_TYPE_THREAD:
            case VIEW_TYPE_MESSAGE:
                if (convertView == null) {
                    view = View.inflate(mContext, R.layout.search_item_mst, null);
                    viewHolder = new ViewHolder(view);
                    view.setTag(viewHolder);
                } else {
                    view = convertView;
                    viewHolder = (ViewHolder) view.getTag();
                }
                if (VIEW_TYPE_THREAD == type) {
                    highlight_title = itemData.getSearchString();
                    highlight_subTitle = "";
                    viewHolder.date.setVisibility(View.GONE);
                } else if (VIEW_TYPE_MESSAGE == type) {
                    highlight_title = "";
                    highlight_subTitle = itemData.getSearchString();
                    viewHolder.date.setText(itemData.getDateValue());
                    viewHolder.date.setVisibility(View.VISIBLE);
                }
                viewHolder.title.setText(itemData.getTitleValue(), highlight_title);
                viewHolder.subTitle.setText(itemData.getSubTitleValue(), highlight_subTitle);
                setSimiconVisibility(viewHolder.simicon, itemData.getSubId());

                view.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        final Intent onClickIntent = new Intent(mContext, ComposeMessageActivity.class);
                        onClickIntent.putExtra(ComposeMessageActivity.THREAD_ID, itemData.getThreadId());
                        onClickIntent.putExtra(HIGH_LIGHT_PLACE, type);
                        onClickIntent.putExtra(HIGH_LIGHT_TEXT, itemData.getSearchString());
                        onClickIntent.putExtra(ComposeMessageActivity.SELECT_ID, itemData.getRowId());
                        mContext.startActivity(onClickIntent);
                    }
                });
                break;
            default:
                break;
        }

        return view;
    }

    private void setSimiconVisibility(ImageView simicon, int subId) {
        if (DEBUG) Log.d(TAG, "setSimiconVisibility(), subId = " + subId);
        if (subId >= 0) {
            int slotId = SubscriptionManager.getSlotId(subId);
            boolean isShowSimIcon = MmsApp.isCreateConversaitonIdBySim
                    && MessageUtils.isMsimIccCardActive() && slotId >= 0;
            if (isShowSimIcon) {
                simicon.setVisibility(View.VISIBLE);
                Drawable mSimIndicatorIcon = MessageUtils.getMultiSimIcon(mContext, slotId);
                simicon.setImageDrawable(mSimIndicatorIcon);
                return;
            }
        }
        simicon.setVisibility(View.GONE);
    }
}//end of MstSearchAdapter