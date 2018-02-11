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

import android.app.Activity;
import android.app.ActionBar;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SqliteWrapper;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.AdapterView.OnItemClickListener;
import android.graphics.drawable.AnimationDrawable;

import com.android.mms.MmsApp;
import com.android.mms.LogTag;
import com.android.mms.MmsConfig;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;

import java.util.Collection;
import java.util.HashSet;

import mst.app.MstActivity;
import mst.provider.Telephony.Threads;
import mst.view.menu.bottomnavigation.BottomNavigationView;
import mst.view.menu.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;
import mst.widget.ActionMode;
import mst.widget.ActionModeListener;
import mst.widget.ActionMode.Item;
import mst.widget.toolbar.Toolbar;
import mst.widget.MstListView;
import mst.provider.Telephony.ThreadsColumns;

/**
 * This activity provides a list view of existing conversations except black list.
 */
public class AddBlackListActivity extends MstActivity
        implements OnItemClickListener {
    private static final String TAG = "Mms/AddBlackListActivity";
    private static final boolean DEBUG = true;

    private static final int THREAD_LIST_QUERY_TOKEN = 1701;
    private static final int DELETE_OBSOLETE_THREADS_TOKEN = 1803;

    public static final int COLUMN_ID = 0;

    private ThreadListQueryHandler mQueryHandler;
    private AddBlackListAdapter mListAdapter;
    //private boolean mDoOnceAfterFirstQuery;
    //private TextView mUnreadConvCount;
    //private MenuItem mSearchItem;//lichao delete

    // keys for extras and icicles
    private final static String LAST_LIST_POS = "last_list_pos";
    private final static String LAST_LIST_OFFSET = "last_list_offset";

    static private final String CHECKED_MESSAGE_LIMITS = "checked_message_limits";
    private final static int DELAY_TIME = 500;

    //same as mEditMode
    //private boolean mMultiChoiceMode = false;
    //private boolean mEditMode = false;//mViewMode == ViewMode.EDIT_MODE
    
    private MstListView mListView;

    private ImageView mEmptyImageView;
    private TextView mEmptyTextview;
    private TextView mEmptyHeaderText;
    private ImageView mEmptyHeaderImage;
    private AnimationDrawable mEmptyLoadAnim;



    private View mHeaderView;
    private TextView mHeaderText;
    private ImageView mHeaderImage;
    private AnimationDrawable mLoadAnim;

    private Toolbar myToolbar;

    boolean isKeyboardShowing = false;

    public enum ViewMode  { NORMAL_MODE, ADD_BLACK_MODE }
    private ViewMode mViewMode;

    public enum QueryStatus  { BEFORE_QUERY, IS_QUERY, AFTER_QUERY }
    private QueryStatus mQueryStatus;

    private BottomNavigationView mBottomNavigationView;
    private ActionMode mActionMode;
    private HashSet<Long> mSelectedThreadIds = new HashSet<Long>();
    private HashSet<String> mSelectedRecipientIds = new HashSet<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "==onCreate()==");
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            Log.i(TAG, "[onCreate]startPermissionActivity,return.");
            return;
        }
        setMstContentView(R.layout.add_black_list_screen);
        mQueryHandler = new ThreadListQueryHandler(getContentResolver());
        mViewMode = ViewMode.NORMAL_MODE;
        mQueryStatus = QueryStatus.BEFORE_QUERY;
        initListView();
        initListAdapter();
        initToolBarAndActionbar();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (DEBUG) Log.d(TAG, "==onPause()==");
        // Don't listen for changes while we're paused.
        mListAdapter.setOnContentChangedListener(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DEBUG) Log.d(TAG, "==onResume()==");
        mListAdapter.setOnContentChangedListener(mContentChangedListener);
        //if (!mDoOnceAfterFirstQuery) {
        //    startAsyncQuery();
        //}
        //updateEmptyView();
    }

    private final AddBlackListAdapter.OnContentChangedListener mContentChangedListener =
            new AddBlackListAdapter.OnContentChangedListener() {
                @Override
                public void onContentChanged(AddBlackListAdapter adapter) {
                    if (DEBUG) Log.d(TAG, "onContentChanged(), >>>startAsyncQuery()");
                    startAsyncQuery();
                }
            };

    @Override
    protected void onNewIntent(Intent intent) {
        if (DEBUG) Log.d(TAG, "==onNewIntent()==, >>>startAsyncQuery");
        // Handle intents that occur after the activity has already been created.
        startAsyncQuery();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (DEBUG) Log.d(TAG, "==onStart()==");
        //mDoOnceAfterFirstQuery = true;
        startAsyncQuery();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (DEBUG) Log.d(TAG, "==onStop()==");
        //stopAsyncQuery();
        updateHeaderView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DEBUG) Log.d(TAG, "==onDestroy()==");
        if (mListAdapter != null) {
            mListAdapter.changeCursor(null);
        }
        Contact.clearListener();
    }

    private void startAsyncQuery() {
        if (DEBUG) Log.d(TAG, "==startAsyncQuery()==");
        try {
            mQueryStatus = QueryStatus.IS_QUERY;
            updateEmptyView();//show is querying while showing EmptyView
            updateHeaderView();//show is querying while showing ListView
            mQueryHandler.cancelOperation(THREAD_LIST_QUERY_TOKEN);
            String searchMode = String.valueOf(MessageUtils.SEARCH_MODE_BLACK);
            String matchWhole = String.valueOf(MessageUtils.MATCH_BY_ADDRESS);
            String blackList = MessageUtils.getSeparatedBlackList(this);
            Uri queryUri = MessageUtils.SEARCH_THREAD_URI.buildUpon()
                    .appendQueryParameter("blacklist", blackList).build().buildUpon()
                    .appendQueryParameter("search_mode", searchMode).build().buildUpon()
                    .appendQueryParameter("match_whole", matchWhole).build();
            mQueryHandler.startQuery(THREAD_LIST_QUERY_TOKEN, null, queryUri, null, null, null, null);
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(this, e);
        }
    }

    private void stopAsyncQuery() {
        if (mQueryHandler != null) {
            mQueryHandler.cancelOperation(THREAD_LIST_QUERY_TOKEN);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    private final class ThreadListQueryHandler extends AsyncQueryHandler {
        public ThreadListQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (DEBUG) Log.d(TAG, "==onQueryComplete()==");
            switch (token) {
            case THREAD_LIST_QUERY_TOKEN:
                mListAdapter.changeCursor(cursor);
                mQueryStatus = QueryStatus.AFTER_QUERY;
                updateEmptyView();
                updateHeaderView();//stop show is querying
                if(null != mListAdapter && 0 != mListAdapter.getCount()){
                    showEditMode();
                }
                break;
            default:
                Log.e(TAG, "onQueryComplete called with unknown token " + token);
            }
        }
    }

    public void checkAll() {
        //lichao modify: getListView()
        int count = mListView.getCount();

        for (int i = 0; i < count; i++) {
            mListView.setItemChecked(i, true);
        }
        mListAdapter.notifyDataSetChanged();
    }

    public void unCheckAll() {
        //lichao modify: getListView()
        int count = mListView.getCount();

        for (int i = 0; i < count; i++) {
            mListView.setItemChecked(i, false);
        }
        mListAdapter.notifyDataSetChanged();
    }

    private void log(String format, Object... args) {
        String s = String.format(format, args);
        Log.d(TAG, "[" + Thread.currentThread().getId() + "] " + s);
    }

    private ActionModeListener mActionModeListener = new ActionModeListener() {
        @Override
        public void onActionItemClicked(Item item) {
            switch (item.getItemId()) {
                case ActionMode.POSITIVE_BUTTON:
                    int checkedCount = mSelectedThreadIds.size();
                    int all = mListAdapter.getCount();
                    if (DEBUG) Log.d(TAG, " POSITIVE_BUTTON, checkedCount = " + checkedCount);
                    if (DEBUG) Log.d(TAG, " POSITIVE_BUTTON, all = " + all);
                    selectAll(checkedCount < all);
                    break;
                case ActionMode.NAGATIVE_BUTTON:
                    if (DEBUG) Log.i(TAG, "case ActionMode.NAGATIVE_BUTTON");
                    onBackPressed();
                    break;
                default:
            }
        }
        @Override
        public void onActionModeDismiss(ActionMode arg0) {
        }
        @Override
        public void onActionModeShow(ActionMode arg0) {
        }
    };

    private void selectAll(boolean all) {
        mSelectedThreadIds.clear();
        mSelectedRecipientIds.clear();
        if (all) {
            Cursor cursor = mListAdapter.getCursor();
            cursor.moveToFirst();
            do {
                long threadId = -1L;
                int column_id = cursor.getColumnIndex(Threads._ID);//"_id"
                if(column_id >= 0){
                    threadId = cursor.getLong(column_id);
                }
                String recipient_ids = "";
                int column_re = cursor.getColumnIndex(ThreadsColumns.RECIPIENT_IDS);//"recipient_ids"
                if(column_re >= 0){
                    recipient_ids = cursor.getString(column_re);
                }
                if(threadId > 0 && !TextUtils.isEmpty(recipient_ids)){
                    mSelectedThreadIds.add(threadId);
                    mSelectedRecipientIds.add(recipient_ids);
                }
            } while (cursor.moveToNext());
        }
        mListAdapter.setCheckList(mSelectedThreadIds);
        //updateActionMode after mSelectedThreadIds size changed
        updateActionMode();
        mListAdapter.notifyDataSetChanged();
    }

    private void showEditMode() {
        if (DEBUG) Log.d(TAG, "[showEditMode]");
        mViewMode = ViewMode.ADD_BLACK_MODE;
        showActionMode(true);
        updateBottomNavigationVisibility();
        mSelectedThreadIds.clear();
        mSelectedRecipientIds.clear();
        mListAdapter.setCheckList(mSelectedThreadIds);
        //updateBottomMenuItems();
        //updateActionMode for updateBottomMenuItems and updateActionModeTitle
        updateActionMode();
        mListAdapter.setCheckBoxEnable(true);
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (DEBUG) Log.d(TAG, "[onItemClick], position = " + position);
        if (mListAdapter == null) {
            return;
        }
        Cursor cursor = (Cursor) mListView.getItemAtPosition(position);
        long threadId = -1L;
        int column_id = cursor.getColumnIndex(Threads._ID);//"_id"
        if(column_id >= 0){
            threadId = cursor.getLong(column_id);
        }
        if (DEBUG) Log.d(TAG, "onItemClick, threadId = " + threadId);

        int column_re = cursor.getColumnIndex(ThreadsColumns.RECIPIENT_IDS);//"recipient_ids"
        String recipient_ids = "";
        if(column_re >= 0){
            recipient_ids = cursor.getString(column_re);
        }
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.black_item_check_box);
        if (null == checkBox) {
            Log.e(TAG, "onItemClick, checkBox is null, return");
            return;
        }

        boolean check_now = !checkBox.isChecked();
        checkBox.setChecked(check_now);
        if (threadId <= 0) {
            return;
        }
        if (TextUtils.isEmpty(recipient_ids)) {
            return;
        }
        if (true == check_now) {
            mSelectedThreadIds.add(threadId);
            mSelectedRecipientIds.add(recipient_ids);
        } else {
            mSelectedThreadIds.remove(threadId);
            mSelectedRecipientIds.remove(recipient_ids);
        }
        mListAdapter.setCheckList(mSelectedThreadIds);
        //updateActionMode after mSelectedThreadIds size changed
        updateActionMode();
    }

    private void updateActionMode() {
        if (mListAdapter == null) {
            finish();
            return;
        }
        if (mActionMode == null) {
            mActionMode = getActionMode();
        }
        String mSelectAllStr = getResources().getString(R.string.selected_all);
        String mUnSelectAllStr = getResources().getString(R.string.deselected_all);

        int checkedCount = mSelectedThreadIds.size();
        if (DEBUG) Log.d(TAG, "updateActionMode, checkedCount = " + checkedCount);

        int all = mListAdapter.getCount();
        if (DEBUG) Log.d(TAG, "updateActionMode, all = " + all);

        if (checkedCount >= all) {
            mActionMode.setPositiveText(mUnSelectAllStr);
        } else {
            mActionMode.setPositiveText(mSelectAllStr);
        }
        updateBottomMenuItems();
        //same as: getActionMode().setTitle(getString(R.string.selected_total_num, checkedCount));
        updateActionModeTitle(getString(R.string.selected_total_num, checkedCount));
    }

    private void updateBottomMenuItems() {
        int checkedCount = mSelectedThreadIds.size();
        //as mst:menu="@menu/add_black_multi_select_menu" in add_black_list_screen.xml
        mBottomNavigationView.setItemEnable(R.id.menu_add_black, (checkedCount > 0));
        //mBottomNavigationView.showItem(R.id.menu_add_black, isAddBlackMode());

    }

    private void initBottomNavigationView() {
        if (mBottomNavigationView == null) {
            mBottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation_view);
            mBottomNavigationView.setNavigationItemSelectedListener(mNavigationItemListener);
        }
    }

    private void updateBottomNavigationVisibility() {
        initBottomNavigationView();
        if (isAddBlackMode()) {
            mBottomNavigationView.setVisibility(View.VISIBLE);
        } else {
            mBottomNavigationView.setVisibility(View.GONE);
        }
    }

    private OnNavigationItemSelectedListener mNavigationItemListener = new OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_add_black:
                    if (null == mSelectedThreadIds || mSelectedThreadIds.size() <= 0) {
                        return false;
                    }
                    addSelectedContactsToBlacklist(mSelectedRecipientIds);
                    final Intent retIntent = new Intent();
                    AddBlackListActivity.this.setResult(Activity.RESULT_OK, retIntent);
                    AddBlackListActivity.this.finish();
                    return true;
                default:
                    return false;
            }
        }
    };

    private boolean isAddBlackMode() {
        return (mViewMode == ViewMode.ADD_BLACK_MODE);
    }

    private void initToolBarAndActionbar() {
        myToolbar = (Toolbar) findViewById(com.mst.R.id.toolbar);
        myToolbar.setTitle(R.string.app_label);
        setupActionModeWithDecor(myToolbar);
        setActionModeListener(mActionModeListener);
    }

    @Override
    public void onNavigationClicked(View view) {
        if (DEBUG) Log.i(TAG, "[onNavigationClicked]");
        //handle click back botton on Toolbar
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (DEBUG) Log.i(TAG, "[onBackPressed]");
        if (isAddBlackMode()) {
            final Intent retIntent = new Intent();
            AddBlackListActivity.this.setResult(Activity.RESULT_CANCELED, retIntent);
            AddBlackListActivity.this.finish();
            return;
        }
        super.onBackPressed();
    }

    private void initListView() {
        mListView = (MstListView) findViewById(R.id.conversation_list);
        //mListView.setItemsCanFocus(true);
        mListView.setOnItemClickListener(this);
        mListView.setFastScrollEnabled(false);
        mListView.setFastScrollAlwaysVisible(false);

        // Tell the list view which view to display when the list is empty
        //mListView.setEmptyView(findViewById(R.id.empty));
        initEmptyView();

        final LayoutInflater inflater = (LayoutInflater) getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mHeaderView = inflater.inflate(R.layout.conversation_list_header, null, false);
        //View v,Object data,boolean isSelectable
        mListView.addHeaderView(mHeaderView, null, false);
    }

    private void initListAdapter() {
        mListAdapter = new AddBlackListAdapter(this, null);
        mListAdapter.setOnContentChangedListener(mContentChangedListener);
        mListView.setAdapter(mListAdapter);//lichao modify
    }

    private void initEmptyView() {
        mListView.setEmptyView(findViewById(R.id.emptyview_layout_id));
        View emptyView = mListView.getEmptyView();
        mEmptyImageView = (ImageView) emptyView.findViewById(R.id.emptyview_img);
        mEmptyTextview = (TextView) emptyView.findViewById(R.id.emptyview_tv);
        mEmptyHeaderText = (TextView) emptyView.findViewById(R.id.list_header_tv);
        mEmptyHeaderImage = (ImageView) emptyView.findViewById(R.id.list_header_img);
        mEmptyHeaderImage.setBackgroundResource(R.anim.loading_anim);
        mEmptyLoadAnim = (AnimationDrawable) mEmptyHeaderImage.getBackground();
    }

    private void updateEmptyView(){
        if (mViewMode == ViewMode.NORMAL_MODE && null != mListAdapter && mListAdapter.getCount() == 0){
            if(mQueryStatus == QueryStatus.IS_QUERY){
                mEmptyTextview.setVisibility(View.GONE);
                mEmptyImageView.setVisibility(View.GONE);
                mEmptyHeaderText.setText(getString(R.string.loading_conversations));
                mEmptyHeaderText.setVisibility(View.VISIBLE);
                mEmptyHeaderImage.setVisibility(View.VISIBLE);
                mEmptyLoadAnim.start();
            }else {
                mEmptyLoadAnim.stop();
                mEmptyHeaderImage.setVisibility(View.GONE);
                mEmptyHeaderText.setVisibility(View.GONE);
                mEmptyTextview.setText(getString(R.string.no_conversations));
                mEmptyTextview.setVisibility(View.VISIBLE);
                mEmptyImageView.setVisibility(View.VISIBLE);
            }
            return;
        }
    }

    private void updateHeaderView() {
        if (mHeaderText == null) {
            ViewStub stub = (ViewStub) mHeaderView.findViewById(R.id.viewstub_conv_list_header);
            if (stub != null) {
                stub.inflate();
            }
            mHeaderText = (TextView) mHeaderView.findViewById(R.id.list_header_tv);
        }
        if (mHeaderImage == null) {
            mHeaderImage = (ImageView) mHeaderView.findViewById(R.id.list_header_img);
            mHeaderImage.setBackgroundResource(R.anim.loading_anim);
        }
        if (mLoadAnim == null && mHeaderImage != null) {
            mLoadAnim = (AnimationDrawable) mHeaderImage.getBackground();
        }
        if (mQueryStatus == QueryStatus.IS_QUERY) {
            mHeaderView.setVisibility(View.VISIBLE);
            mHeaderText.setVisibility(View.VISIBLE);
            mHeaderText.setText(getString(R.string.searching));
            mHeaderImage.setVisibility(View.VISIBLE);
            mLoadAnim.start();
        } else {
            mLoadAnim.stop();
            mHeaderImage.setVisibility(View.GONE);
            mHeaderText.setVisibility(View.GONE);
            mHeaderView.setVisibility(View.GONE);
        }
    }

    private void addSelectedContactsToBlacklist(Collection<String> recipientIdsSet) {
        if (null == recipientIdsSet || 0 == recipientIdsSet.size()) {
            return;
        }
        ContactList recipients;
        HashSet<Contact> contactSet = new HashSet<Contact>();
        for (String recipientIds : recipientIdsSet) {
            recipients = ContactList.getByIds(recipientIds, false);
            for (Contact contact : recipients) {
                if(null != contact){
                    contactSet.add(contact);
                }
            }
        }
        for (Contact contact : contactSet) {
            if(null != contact){
                contact.addToBlacklist(this);
            }
        }
    }
}
