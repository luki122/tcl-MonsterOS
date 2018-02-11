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
//import android.app.AlertDialog;
import android.app.ListActivity;
//import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SqliteWrapper;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.util.DisplayMetrics;
import mst.provider.Telephony;
import mst.provider.Telephony.Mms;
import mst.provider.Telephony.Threads;
import android.util.Log;
//import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
//import android.widget.ListView;
//import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.mms.LogTag;
import com.android.mms.MmsConfig;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.data.Conversation.ConversationQueryHandler;
import com.android.mms.data.RecipientIdCache;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.transaction.SmsRejectedReceiver;
import com.android.mms.ui.PopupList;
import com.android.mms.ui.SelectionMenu;
import com.android.mms.util.DraftCache;
import com.android.mms.util.Recycler;
//import com.android.mms.widget.MmsWidgetProvider;
import com.google.android.mms.pdu.PduHeaders;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.HashSet;

//lichao add begin
import android.database.sqlite.SQLiteFullException;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.SearchRecentSuggestions;
import android.text.TextUtils;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CursorAdapter;
import android.widget.MstSearchView;
import android.widget.MstSearchView.OnQueryTextListener;
import android.widget.MstSearchView.OnCloseListener;
import android.widget.MstSearchView.OnSuggestionListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.android.mms.MmsApp;
import com.android.mms.ui.SearchActivity.TextViewSnippet;


import java.util.HashMap;
import java.util.Map;

import mst.app.dialog.AlertDialog;
import mst.app.dialog.ProgressDialog;
import mst.app.MstActivity;
import mst.view.menu.bottomnavigation.BottomNavigationView;
import mst.view.menu.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;
import mst.widget.ActionMode;
import mst.widget.ActionModeListener;
import mst.widget.ActionMode.Item;
import mst.widget.toolbar.Toolbar;
import mst.widget.toolbar.Toolbar.OnMenuItemClickListener;
import mst.widget.FloatingActionButton.OnFloatActionButtonClickListener;
import mst.widget.FloatingActionButton;
import mst.widget.MstListView;
import android.database.MergeCursor;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import android.view.View.OnFocusChangeListener;
import mst.widget.SliderView;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.graphics.Rect;
import android.provider.BaseColumns;
import mst.provider.Telephony.CanonicalAddressesColumns;
import android.telephony.PhoneNumberUtils;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.android.mms.ui.SearchTask;
import android.graphics.drawable.AnimationDrawable;
//lichao add end
//XiaoYuan SDK start???
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
//XiaoYuan SDK end
//tangyisen
import android.telephony.SubscriptionManager;

/**
 * This activity provides a list view of existing conversations.
 */
//lichao modify ListActivity to MstActivity
//add "OnItemClickListener, OnScrollListener, OnItemLongClickListener"
//lichao delete OnScrollListener, OnFocusChangeListener in 2016-09-23
public class ConversationList extends MstActivity implements DraftCache.OnDraftChangedListener,
        OnItemClickListener, OnItemLongClickListener,
        OnMenuItemClickListener, SliderView.OnSliderButtonLickListener {
    private static final String TAG = "Mms/ConversationList";
    private static final boolean DEBUG = false;
    private static final boolean DEBUGCLEANUP = false;

    public static final int THREAD_LIST_QUERY_TOKEN       = 1701;
    //public static final int UNREAD_THREADS_QUERY_TOKEN    = 1702;
    public static final int DELETE_CONVERSATION_TOKEN      = 1801;
    public static final int HAVE_LOCKED_MESSAGES_TOKEN     = 1802;
    public static final int DELETE_OBSOLETE_THREADS_TOKEN = 1803;
    //begin tangyisen
    public static final int HAVE_LOCKED_MESSAGES_TOKEN_MARKED     = 1804;
    //end tangyisen

    // IDs of the context menu items for the list of conversations.
    public static final int MENU_DELETE               = 0;
    public static final int MENU_VIEW                 = 1;
    public static final int MENU_VIEW_CONTACT         = 2;
    public static final int MENU_ADD_TO_CONTACTS      = 3;

    public static final int COLUMN_ID = 0;

    private ThreadListQueryHandler mQueryHandler;
    private ConversationListAdapter mListAdapter;
    private SharedPreferences mPrefs;
    private Handler mHandler;
    private boolean mDoOnceAfterFirstQuery;
    //private TextView mUnreadConvCount;
    //private MenuItem mSearchItem;//lichao delete
    private MstSearchView mSearchView;//lichao modify
    //private View mSmsPromoBannerView;
    private int mSavedFirstVisiblePosition = AdapterView.INVALID_POSITION;
    private int mSavedFirstItemOffset;
    private ProgressDialog mProgressDialog;

    // keys for extras and icicles
    private final static String LAST_LIST_POS = "last_list_pos";
    private final static String LAST_LIST_OFFSET = "last_list_offset";

    static private final String CHECKED_MESSAGE_LIMITS = "checked_message_limits";
    private final static int DELAY_TIME = 500;

    // Whether or not we are currently enabled for SMS. This field is updated in onResume to make
    // sure we notice if the user has changed the default SMS app.
    private boolean mIsSmsEnabled;
    private Toast mComposeDisabledToast;
    private static long mLastDeletedThread = -1;
    //private int mDeleteThreadCount = -1;
    //same as mEditMode
    //private boolean mMultiChoiceMode = false;
    //private boolean mEditMode = false;//mViewMode == ViewMode.EDIT_MODE
    
	//lichao add begin
    private MstListView mListView;
    private FloatingActionButton mFloatButton;

    private boolean mIsShowDefaultSmsAppDialog = false;

    private ImageView mEmptyImageView;
    private TextView mEmptyTextview;
    private TextView mEmptyHeaderText;
    private ImageView mEmptyHeaderImage;
    private AnimationDrawable mEmptyLoadAnim;

    private SearchTask mSearchTask;

    private View mFooterView;
    private TextView mFooterText;

    private View mHeaderView;
    private TextView mHeaderText;
    private ImageView mHeaderImage;
    private AnimationDrawable mLoadAnim;

    private Toolbar myToolbar;
    private Menu mToolbarMenu;
    private View mBackIcon;

    boolean isKeyboardShowing = false;

    private String mSearchString = "";
    private SearchListAdapter mSearchAdapter;
    //public static final int CACHE_SIZE = 100;
    public static HashMap<String, Cursor> mSearchCursorCache =
            new HashMap<String, Cursor>();

    public enum ViewMode  { NORMAL_MODE, EDIT_MODE, EDIT_BLACK_MODE, SEARCH_MODE }
    private ViewMode mViewMode;

    public enum SearchStatus  { EXITED, EMPTY_INPUT, IS_SEARCHING, SHOWING_RESULT }
    private SearchStatus mSearchStatus;

    public enum QueryStatus  { BEFORE_QUERY, IS_QUERY, AFTER_QUERY }
    private QueryStatus mQueryStatus;

    private BottomNavigationView mBottomNavigationView;
    private ActionMode mActionMode;
    //private final Collection<Long> mSelectedThreadIds = new HashSet<Long>();
    private HashSet<Long> mSelectedThreadIds;
	//lichao add end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "==onCreate()==");
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            Log.i(TAG, "[onCreate]startPermissionActivity,return.");
            return;
        }

        // Cache recipients information in a background thread in advance.
        RecipientIdCache.init(getApplication());

        //lichao modify
        setMstContentView(R.layout.conversation_list_screen);

        if (MessageUtils.isMailboxMode()) {
            //Intent modeIntent = new Intent(this, MailBoxMessageList.class);
            //startActivityIfNeeded(modeIntent, -1);
            finish();
            return;
        }

        mQueryHandler = new ThreadListQueryHandler(getContentResolver());

        setTitle(R.string.app_label);

        mHandler = new Handler();

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean checkedMessageLimits = mPrefs.getBoolean(CHECKED_MESSAGE_LIMITS, false);
        if (DEBUG) Log.v(TAG, "checkedMessageLimits: " + checkedMessageLimits);
        if (!checkedMessageLimits) {
            runOneTimeStorageLimitCheckForLegacyMessages();
        }

        if (savedInstanceState != null) {
            mSavedFirstVisiblePosition = savedInstanceState.getInt(LAST_LIST_POS,
                    AdapterView.INVALID_POSITION);
            mSavedFirstItemOffset = savedInstanceState.getInt(LAST_LIST_OFFSET, 0);
        } else {
            mSavedFirstVisiblePosition = AdapterView.INVALID_POSITION;
            mSavedFirstItemOffset = 0;
        }

        initViews();

        if(!MmsConfig.isSmsEnabled(this)){
		    //if startActivity(MmsConfig.getRequestDefaultSmsAppActivity()) here will cause the screen flashing
            mIsShowDefaultSmsAppDialog = true;
        }

        mViewMode = ViewMode.NORMAL_MODE;
        mSearchStatus = SearchStatus.EXITED;
        mQueryStatus = QueryStatus.BEFORE_QUERY;
		//lichao add end
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(LAST_LIST_POS, mSavedFirstVisiblePosition);
        outState.putInt(LAST_LIST_OFFSET, mSavedFirstItemOffset);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (DEBUG) Log.d(TAG, "==onPause()==");
        // Don't listen for changes while we're paused.
        mListAdapter.setOnContentChangedListener(null);

        // Remember where the list is scrolled to so we can restore the scroll position
        // when we come back to this activity and *after* we complete querying for the
        // conversations.
		//lichao modify
        //ListView listView = getListView();
        mSavedFirstVisiblePosition = mListView.getFirstVisiblePosition();
        View firstChild = mListView.getChildAt(0);
        mSavedFirstItemOffset = (firstChild == null) ? 0 : firstChild.getTop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DEBUG) Log.d(TAG, "==onResume()==");
        boolean isSmsEnabled = MmsConfig.isSmsEnabled(this);
        if (isSmsEnabled != mIsSmsEnabled) {
            mIsSmsEnabled = isSmsEnabled;
            invalidateOptionsMenu();
        }

        mListAdapter.setOnContentChangedListener(mContentChangedListener);
        if (!mDoOnceAfterFirstQuery) {
            startAsyncQuery();
        }

        //lichao add in 2016-09-24
        updateEmptyView();
        updateFooterView();
    }

    private final ConversationListAdapter.OnContentChangedListener mContentChangedListener =
        new ConversationListAdapter.OnContentChangedListener() {
        @Override
        public void onContentChanged(ConversationListAdapter adapter) {
            if (DEBUG) Log.d(TAG, "onContentChanged(), >>>startAsyncQuery()");
            startAsyncQuery();
        }
    };

    private final SearchListAdapter.OnContentChangedListener mSearchContentChangedListener =
            new SearchListAdapter.OnContentChangedListener() {
                @Override
                public void onContentChanged(SearchListAdapter adapter) {
                    if (DEBUG) Log.d(TAG, "onContentChanged(), >>>startSearch()");
                    startSearch(mSearchString);
                }
            };

    /**
     * Checks to see if the number of MMS and SMS messages are under the limits for the
     * recycler. If so, it will automatically turn on the recycler setting. If not, it
     * will prompt the user with a message and point them to the setting to manually
     * turn on the recycler.
     */
    public synchronized void runOneTimeStorageLimitCheckForLegacyMessages() {
        if (Recycler.isAutoDeleteEnabled(this)) {
            if (DEBUG) Log.v(TAG, "recycler is already turned on");
            // The recycler is already turned on. We don't need to check anything or warn
            // the user, just remember that we've made the check.
            markCheckedMessageLimit();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Recycler.checkForThreadsOverLimit(ConversationList.this)) {
                    if (DEBUG) Log.v(TAG, "checkForThreadsOverLimit TRUE");
                    // Dang, one or more of the threads are over the limit. Show an activity
                    // that'll encourage the user to manually turn on the setting. Delay showing
                    // this activity until a couple of seconds after the conversation list appears.
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(ConversationList.this,
                                    WarnOfStorageLimitsActivity.class);
                            startActivity(intent);
                        }
                    }, 2000);
                } else {
                    if (DEBUG) Log.v(TAG, "checkForThreadsOverLimit silently turning on recycler");
                    // No threads were over the limit. Turn on the recycler by default.
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SharedPreferences.Editor editor = mPrefs.edit();
                            editor.putBoolean(MessagingPreferenceActivity.AUTO_DELETE, true);
                            editor.apply();
                        }
                    });
                }
                // Remember that we don't have to do the check anymore when starting MMS.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        markCheckedMessageLimit();
                    }
                });
            }
        }, "ConversationList.runOneTimeStorageLimitCheckForLegacyMessages").start();
    }

    /**
     * Mark in preferences that we've checked the user's message limits. Once checked, we'll
     * never check them again, unless the user wipe-data or resets the device.
     */
    private void markCheckedMessageLimit() {
        if (DEBUG) Log.v(TAG, "markCheckedMessageLimit");
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(CHECKED_MESSAGE_LIMITS, true);
        editor.apply();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (DEBUG) Log.d(TAG, "onNewIntent(), >>>startAsyncQuery");
        // Handle intents that occur after the activity has already been created.
        startAsyncQuery();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (DEBUG) Log.d(TAG, "==onStart()==");
        MessagingNotification.cancelNotification(getApplicationContext(),
                SmsRejectedReceiver.SMS_REJECTED_NOTIFICATION_ID);

        DraftCache.getInstance().addOnDraftChangedListener(this);

        mDoOnceAfterFirstQuery = true;

        startAsyncQuery();

        // We used to refresh the DraftCache here, but
        // refreshing the DraftCache each time we go to the ConversationList seems overly
        // aggressive. We already update the DraftCache when leaving CMA in onStop() and
        // onNewIntent(), and when we delete threads or delete all in CMA or this activity.
        // I hope we don't have to do such a heavy operation each time we enter here.

        // we invalidate the contact cache here because we want to get updated presence
        // and any contact changes. We don't invalidate the cache by observing presence and contact
        // changes (since that's too untargeted), so as a tradeoff we do it here.
        // If we're in the middle of the app initialization where we're loading the conversation
        // threads, don't invalidate the cache because we're in the process of building it.
        // TODO: think of a better way to invalidate cache more surgically or based on actual
        // TODO: changes we care about
        if (!Conversation.loadingThreads()) {
            Contact.invalidateCache();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (DEBUG) Log.d(TAG, "==onStop()==");
        stopAsyncQuery();

        DraftCache.getInstance().removeOnDraftChangedListener(this);

        unbindListeners(null);

        clearSearchTask();

        mSearchStatus = SearchStatus.EXITED;
        updateHeaderView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DEBUG) Log.d(TAG, "==onDestroy()==");
        if (mListAdapter != null) {
            mListAdapter.changeCursor(null);
        }

        MessageUtils.removeDialogs();
        Contact.clearListener();
    }

    private void unbindListeners(final Collection<Long> threadIds) {
	    //lichao modify getListView() to mListView
        for (int i = 0; i < mListView.getChildCount(); i++) {
            View view = mListView.getChildAt(i);
            if (view instanceof ConversationListItem) {
                ConversationListItem item = (ConversationListItem)view;
                if (threadIds == null) {
                    item.unbindConversation(false);
                } else if (threadIds.contains(item.getConversation().getThreadId())) {
                    item.unbindConversation(false);
                }
            }
        }
    }

    @Override
    public void onDraftChanged(final long threadId, final boolean hasDraft) {
        // Run notifyDataSetChanged() on the main thread.
        mQueryHandler.post(new Runnable() {
            @Override
            public void run() {
                if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                    log("onDraftChanged: threadId=" + threadId + ", hasDraft=" + hasDraft);
                }
                mListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void startAsyncQuery() {
        try {
            mQueryStatus = QueryStatus.IS_QUERY;
            updateEmptyView();
            Conversation.startQueryForAll(mQueryHandler, THREAD_LIST_QUERY_TOKEN);
            //Conversation.startQuery(mQueryHandler, UNREAD_THREADS_QUERY_TOKEN, Threads.READ + "=0");
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(this, e);
        }
    }

    private void stopAsyncQuery() {
        if (mQueryHandler != null) {
            mQueryHandler.cancelOperation(THREAD_LIST_QUERY_TOKEN);
            //mQueryHandler.cancelOperation(UNREAD_THREADS_QUERY_TOKEN);
        }
    }

    MstSearchView.OnQueryTextListener mQueryTextListener = new MstSearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if(DEBUG) Log.d(TAG, "\n\n onQueryTextChange, newText = " + newText);
            //if(DEBUG) Log.d(TAG, " onQueryTextChange, oldText = " + mSearchString);
            if (newText.equals(mSearchString)) {
                return true;
            }
            //newText maybe is empty
        	mSearchString = newText;
            clearSearchTask();
            if(TextUtils.isEmpty(mSearchString)) {
                //mListView.setAdapter(mListAdapter);
                //mListAdapter.notifyDataSetChanged();
                mSearchStatus = SearchStatus.EMPTY_INPUT;
                updateHeaderView();//hide HeaderView
                showEmptySearchListView();//Empty list
                updateEmptyView();//show EmptyView
                updateFooterView();//hide FooterView
                return true;
            }
            mSearchStatus = SearchStatus.IS_SEARCHING;
            updateEmptyView();//show is searching while showing EmptyView
            updateHeaderView();//show is searching while showing ListView
            startSearch(mSearchString);
            return true;
        }
    };

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    }
	*/

    @Override
    public boolean onSearchRequested() {
        if (DEBUG) Log.d(TAG, "[onSearchRequested]");
        if (mViewMode == ViewMode.EDIT_MODE) {
            // block search entirely (by simply returning false).
            return false;
        }

        if (getResources().getBoolean(R.bool.config_classify_search)) {
            //start classify search
            Intent searchintent = new Intent(this, SearchActivityExtend.class);
            startActivityIfNeeded(searchintent, -1);
            return true;
        }

        enterSearchMode();
        return true;
    }

    /*
    //lichao delete with onCreateOptionsMenu(Menu menu) and onPrepareOptionsMenu(Menu menu)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    }
    */

    /*
	//lichao delete
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    }
	*/

    private void createNewMessage() {
        startActivity(ComposeMessageActivity.createIntent(this, 0));
    }

    private void openThread(long threadId) {
        startActivity(ComposeMessageActivity.createIntent(this, threadId));
    }

    public static Intent createAddContactIntent(String address) {
        // address must be a single recipient
        Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        intent.setType(Contacts.CONTENT_ITEM_TYPE);
        if (Mms.isEmailAddress(address)) {
            intent.putExtra(ContactsContract.Intents.Insert.EMAIL, address);
        } else {
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, address);
            intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        return intent;
    }

    //lichao add in 2016-09-02 begin
    public static Intent createNewContactIntent(String address) {
        // address must be a single recipient
        Intent intent = new Intent(Intent.ACTION_INSERT/*action*/, ContactsContract.Contacts.CONTENT_URI/*uri*/);
        if (Mms.isEmailAddress(address)) {
            intent.putExtra(ContactsContract.Intents.Insert.EMAIL, address);
        } else {
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, address);
            intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        return intent;
    }
    //lichao add in 2016-09-02 end

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // We override this method to avoid restarting the entire
        // activity when the keyboard is opened (declared in
        // AndroidManifest.xml).  Because the only translatable text
        // in this activity is "New Message", which has the full width
        // of phone to work with, localization shouldn't be a problem:
        // no abbreviated alternate words should be needed even in
        // 'wide' languages like German or Russian.

        super.onConfigurationChanged(newConfig);
        if (DEBUG) Log.v(TAG, "onConfigurationChanged: " + newConfig);
        if (DEBUG) Log.v(TAG, "onConfigurationChanged: newConfig.keyboardHidden = " + newConfig.keyboardHidden);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SEARCH && mViewMode == ViewMode.NORMAL_MODE) {
            if (getResources().getBoolean(R.bool.config_classify_search)) {
                Intent searchintent = new Intent(this, SearchActivityExtend.class);
                startActivityIfNeeded(searchintent, -1);
            }
            else {
                enterSearchMode();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Start the process of putting up a dialog to confirm deleting a thread,
     * but first start a background query to see if any of the threads or thread
     * contain locked messages so we'll know how detailed of a UI to display.
     * @param threadId id of the thread to delete or -1 for all threads
     * @param handler query handler to do the background locked query
     */
    public static void confirmDeleteThread(long threadId, AsyncQueryHandler handler) {
        ArrayList<Long> threadIds = null;
        if (threadId != -1) {
            threadIds = new ArrayList<Long>();
            threadIds.add(threadId);
        }
        confirmDeleteThreads(threadIds, handler);
    }

    /**
     * Start the process of putting up a dialog to confirm deleting threads,
     * but first start a background query to see if any of the threads
     * contain locked messages so we'll know how detailed of a UI to display.
     * @param threadIds list of threadIds to delete or null for all threads
     * @param handler query handler to do the background locked query
     */
    public static void confirmDeleteThreads(Collection<Long> threadIds, AsyncQueryHandler handler) {
        if(DEBUG) Log.d(TAG, "confirmDeleteThreads(), threadIds.size() = "+threadIds.size());
        Conversation.startQueryHaveLockedMessages(handler, threadIds,
                HAVE_LOCKED_MESSAGES_TOKEN);
    }

    /**
     * Build and show the proper delete thread dialog. The UI is slightly different
     * depending on whether there are locked messages in the thread(s) and whether we're
     * deleting single/multiple threads or all threads.
     * @param listener gets called when the delete button is pressed
     * @param threadIds the thread IDs to be deleted (pass null for all threads)
     * @param hasLockedMessages whether the thread(s) contain locked messages
     * @param context used to load the various UI elements
     */
    public static void confirmDeleteThreadDialog(final DeleteThreadListener listener,
            Collection<Long> threadIds,
            boolean hasLockedMessages,
            Context context) {
        //lichao modify in 2016-10-27 begin
        //View contents = View.inflate(context, R.layout.delete_thread_dialog_view, null);
        //TextView msg = (TextView)contents.findViewById(R.id.message);

        String messageStr = context.getResources().getString(R.string.confirm_delete_message);
        if (threadIds == null) {
            //msg.setText(R.string.confirm_delete_all_conversations);
            messageStr = context.getResources().getString(R.string.confirm_delete_all_conversations);
        } else {
            // Show the number of threads getting deleted in the confirmation dialog.
            int cnt = threadIds.size();
            //messageStr = context.getResources().getQuantityString(
            //        R.plurals.confirm_delete_conversation, cnt, cnt);
            if(cnt == 1) {
                messageStr = context.getResources().getString(R.string.confirm_delete_conversation_one);
            } else {
                messageStr = context.getResources().getString(R.string.confirm_delete_conversation_other, cnt);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder/*.setTitle(R.string.confirm_dialog_title)*/
            .setIconAttribute(android.R.attr.alertDialogIcon)
            .setCancelable(true)
            .setPositiveButton(R.string.delete, listener)
            .setNegativeButton(R.string.no, null)
            //.setView(contents)
            .setMessage(messageStr)
            .show();
        //lichao modify in 2016-10-27 end
    }

    private final OnKeyListener mThreadListKeyListener = new OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DEL: {
					    //lichao modify: getListView()
                        long id = mListView.getSelectedItemId();
                        if (id > 0) {
                            confirmDeleteThread(id, mQueryHandler);
                        }
                        return true;
                    }
                }
            }
            return false;
        }
    };

    public static class DeleteThreadListener implements OnClickListener {
        private final Collection<Long> mThreadIds;
        private final ConversationQueryHandler mHandler;
        private final Context mContext;
        private boolean mDeleteLockedMessages;
        private final Runnable mCallBack_DeletingRunnable;

        public DeleteThreadListener(Collection<Long> threadIds, ConversationQueryHandler handler,
                Runnable callBackDeletingRunnable, Context context) {
            //Log.d(TAG, "new DeleteThreadListener: threadIds = "+threadIds);
            mThreadIds = threadIds;
            mHandler = handler;
            mContext = context;
            mCallBack_DeletingRunnable = callBackDeletingRunnable;
        }

        public void setDeleteLockedMessage(boolean deleteLockedMessages) {
            mDeleteLockedMessages = deleteLockedMessages;
        }

        @Override
        public void onClick(DialogInterface dialog, final int whichButton) {
            MessageUtils.handleReadReport(mContext, mThreadIds,
                    PduHeaders.READ_STATUS__DELETED_WITHOUT_BEING_READ, new Runnable() {
                @Override
                public void run() {
                    int token = DELETE_CONVERSATION_TOKEN;
                    if (mCallBack_DeletingRunnable != null) {
                        mCallBack_DeletingRunnable.run();
                    }
                    if (mContext instanceof ConversationList) {
                        ((ConversationList)mContext).unbindListeners(mThreadIds);
                    }
                    if (mThreadIds == null) {
                        //Log.d(TAG, "class DeleteThreadListener, onClick>>>startDeleteAll");
                        Conversation.startDeleteAll(mHandler, token, mDeleteLockedMessages);
                        DraftCache.getInstance().refresh();
                    } else {
                        int size = mThreadIds.size();
                        if (size > 0 && mCallBack_DeletingRunnable != null) {
                            // Save the last thread id.
                            // And cancel deleting dialog after this thread been deleted.
                            mLastDeletedThread = (mThreadIds.toArray(new Long[size]))[size - 1];
                        }
                        //Log.d(TAG, "class DeleteThreadListener, onClick>>>startDelete, mThreadIds = "+mThreadIds);
                        Conversation.startDelete(mHandler, token, mDeleteLockedMessages,
                                mThreadIds);
                    }
                }
            });
            dialog.dismiss();
        }
    }

    private final Runnable mDeleteObsoleteThreadsRunnable = new Runnable() {
        @Override
        public void run() {
            if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                LogTag.debug("mDeleteObsoleteThreadsRunnable getSavingDraft(): " +
                        DraftCache.getInstance().getSavingDraft());
            }
            if (DraftCache.getInstance().getSavingDraft()) {
                // We're still saving a draft. Try again in a second. We don't want to delete
                // any threads out from under the draft.
                if (DEBUGCLEANUP) {
                    LogTag.debug("mDeleteObsoleteThreadsRunnable saving draft, trying again");
                }
                mHandler.postDelayed(mDeleteObsoleteThreadsRunnable, 1000);
            } else {
                if (DEBUGCLEANUP) {
                    LogTag.debug("mDeleteObsoleteThreadsRunnable calling " +
                            "asyncDeleteObsoleteThreads");
                }
                Conversation.asyncDeleteObsoleteThreads(mQueryHandler,
                        DELETE_OBSOLETE_THREADS_TOKEN);
            }
        }
    };

    private final class ThreadListQueryHandler extends ConversationQueryHandler {
        public ThreadListQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (DEBUG) Log.d(TAG, "==onQueryComplete()==");
            switch (token) {
            case THREAD_LIST_QUERY_TOKEN:
                mListAdapter.changeCursor(cursor);
                //lichao add
                mQueryStatus = QueryStatus.AFTER_QUERY;
                updateEmptyView();
                updateFooterView();

                if (mDoOnceAfterFirstQuery) {
                    mDoOnceAfterFirstQuery = false;
                    // Delay doing a couple of DB operations until we've initially queried the DB
                    // for the list of conversations to display. We don't want to slow down showing
                    // the initial UI.

                    // 1. Delete any obsolete threads. Obsolete threads are threads that aren't
                    // referenced by at least one message in the pdu or sms tables.
                    mHandler.post(mDeleteObsoleteThreadsRunnable);

                    // 2. Mark all the conversations as seen.
                    Conversation.markAllConversationsAsSeen(getApplicationContext());
                }
                if (mSavedFirstVisiblePosition != AdapterView.INVALID_POSITION) {
                    // Restore the list to its previous position.
					//lichao modify: getListView()
                    mListView.setSelectionFromTop(mSavedFirstVisiblePosition,
                            mSavedFirstItemOffset);
                    mSavedFirstVisiblePosition = AdapterView.INVALID_POSITION;
                }

                //lichao add
                if(mIsShowDefaultSmsAppDialog){
                    startActivity(MmsConfig.getRequestDefaultSmsAppActivity());
                    mIsShowDefaultSmsAppDialog = false;
                }
                break;

            /*
            case UNREAD_THREADS_QUERY_TOKEN:
                int count = 0;
                if (cursor != null) {
                    count = cursor.getCount();
                    cursor.close();
                }
				//lichao add null judge
                if(mUnreadConvCount != null) {
                	mUnreadConvCount.setText(count > 0 ? Integer.toString(count) : null);
                }
                break;
                */

            case HAVE_LOCKED_MESSAGES_TOKEN:
                if (ConversationList.this.isFinishing()) {
                    Log.w(TAG, "ConversationList is finished, do nothing ");
                    if (cursor != null) {
                        cursor.close();
                    }
                    return ;
                }
                @SuppressWarnings("unchecked")
                Collection<Long> threadIds = (Collection<Long>)cookie;
                if(DEBUG) Log.d(TAG, "case HAVE_LOCKED_MESSAGES_TOKEN: threadIds = "+threadIds);
                //Log.d(TAG, "case HAVE_LOCKED_MESSAGES_TOKEN: cursor.getCount() = "+cursor.getCount());

                //mDeleteThreadCount = threadIds.size();

                confirmDeleteThreadDialog(
                        new DeleteThreadListener(threadIds, mQueryHandler, mDeletingRunnable, ConversationList.this),
                        threadIds,
                        cursor != null && cursor.getCount() > 0,
                        ConversationList.this);
                if (cursor != null) {
                    cursor.close();
                }
                break;
            default:
                Log.e(TAG, "onQueryComplete called with unknown token " + token);
            }
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            super.onDeleteComplete(token, cookie, result);
            if (DEBUG) Log.d(TAG, "==onDeleteComplete()==");
            switch (token) {
            case DELETE_CONVERSATION_TOKEN:
                long threadId = cookie != null ? (Long)cookie : -1;     // default to all threads
                if (threadId < 0 || threadId == mLastDeletedThread) {
                    mHandler.removeCallbacks(mShowProgressDialogRunnable);
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                    mLastDeletedThread = -1;
                    //lichao add in 2016-11-15 for interactive design requirements
                    Toast.makeText(ConversationList.this, ConversationList.this.getResources()
                            .getString(R.string.conversations_delete_completed), Toast.LENGTH_SHORT).show();
                }
                if (threadId == -1) {
                    // Rebuild the contacts cache now that all threads and their associated unique
                    // recipients have been deleted.
                    Contact.init(getApplication());
                } else {
                    // Remove any recipients referenced by this single thread from the
                    // contacts cache. It's possible for two or more threads to reference
                    // the same contact. That's ok if we remove it. We'll recreate that contact
                    // when we init all Conversations below.
                    Conversation conv = Conversation.get(ConversationList.this, threadId, false);
                    if (conv != null) {
                        ContactList recipients = conv.getRecipients();
                        for (Contact contact : recipients) {
                            contact.removeFromCache();
                        }
                    }
                }
                // Make sure the conversation cache reflects the threads in the DB.
                Conversation.init(ConversationList.this);

                // Update the notification for new messages since they
                // may be deleted.
                MessagingNotification.nonBlockingUpdateNewMessageIndicator(ConversationList.this,
                        MessagingNotification.THREAD_NONE, false);
                // Update the notification for failed messages since they
                // may be deleted.
                MessagingNotification.nonBlockingUpdateSendFailedNotification(ConversationList.this);

                // Make sure the list reflects the delete
                startAsyncQuery();

                //MmsWidgetProvider.notifyDatasetChanged(getApplicationContext());
                break;

            case DELETE_OBSOLETE_THREADS_TOKEN:
                if (DEBUGCLEANUP) {
                    LogTag.debug("onDeleteComplete finished DELETE_OBSOLETE_THREADS_TOKEN");
                }

                if (result > 0) {
                    startAsyncQuery();
                }
                break;
            }
        }
    }

    private ProgressDialog createProgressDialog() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        //dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setInformationVisibility(false);
        //dialog.setProgress(mDeleteThreadCount);
        dialog.setMessage(getText(R.string.deleting_threads));
        return dialog;
    }

    private Runnable mDeletingRunnable = new Runnable() {
        @Override
        public void run() {
            //lichao add in 2016-12-02 begin
            if (mViewMode == ViewMode.EDIT_MODE) {
                mHandler.post(mQuitDeleteModeRunnable);
            }
            //lichao add in 2016-12-02 end
            mHandler.postDelayed(mShowProgressDialogRunnable, DELAY_TIME);
        }
    };

    private Runnable mShowProgressDialogRunnable = new Runnable() {
        @Override
        public void run() {
            if (mProgressDialog != null) {
                mProgressDialog.show();
            }
        }
    };

    //lichao add in 2016-12-02 begin
    private Runnable mQuitDeleteModeRunnable = new Runnable() {
        @Override
        public void run() {
            //safeQuitDeleteMode();
            changeToNormalMode();
        }
    };
    //lichao add in 2016-12-02 end

//    private ProgressDialog createSearchProgressDialog() {
//        ProgressDialog dialog = new ProgressDialog(this);
//        dialog.setIndeterminate(true);
//        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.setCancelable(false);
//        //dialog.setProgress(mDeleteThreadCount);
//        dialog.setMessage(getText(R.string.searching));
//        return dialog;
//    }

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
	
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
    //add by lgy or lichao begin
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onSliderButtonClick(int id, View view, ViewGroup parent) {

        if(DEBUG) Log.d(TAG, "onSliderButtonClick1,id:" + id + ", view:" + view + ", parent:" + parent +
                ", parent.getTag():" + ((SliderView) parent).getTag(R.id.slider_tag));

        //Cursor cursor  = (Cursor)mListView.getItemAtPosition(position);
        //Conversation conv = Conversation.from(ConversationList.this, cursor);
        //long threadId = conv.getThreadId();

        long threadId = Long.parseLong(((SliderView) parent).getTag(R.id.slider_tag).toString());
        if (DEBUG) Log.d(TAG, "onSliderButtonClick, threadId = " + threadId);

        switch (id) {
            case ConversationListAdapter.SLIDER_BTN_POSITION_DELETE:
                //Toast.makeText(this, "view: 1, threadId = "+threadId, Toast.LENGTH_SHORT).show();
                confirmDeleteThread(threadId, mQueryHandler);
                break;
            default:
                break;
        }
        if (((SliderView) parent).isOpened()) {
            ((SliderView) parent).close(false);
        }
    }

    private void initViews() {
        //mSmsPromoBannerView = findViewById(R.id.banner_sms_promo);
        
        initListView();

        initListAdapter();

        mProgressDialog = createProgressDialog();
    	
    	//initBottomMenu();//change to initBottomNavigationView()

		initToolBarAndActionbar();
		
		initFloatActionButton();

        initSearchView();

        //lichao add in 2016-10-20
        setListenerToRootView();
    }
	//lichao add end

	private ActionModeListener mActionModeListener = new ActionModeListener() {
		@Override
		public void onActionItemClicked(Item item) {
			// TODO Auto-generated method stub
			switch (item.getItemId()) {
			case ActionMode.POSITIVE_BUTTON:
				int checkedCount = mSelectedThreadIds.size();
				int all = mListAdapter.getCount();

                if(DEBUG) Log.d(TAG, "onActionItemClicked, POSITIVE_BUTTON, checkedCount = "+checkedCount);
                if(DEBUG) Log.d(TAG, "onActionItemClicked, POSITIVE_BUTTON, all = "+all);

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
		if(all) {
	        Cursor cursor  = mListAdapter.getCursor();
	        cursor.moveToFirst();
            mSelectedThreadIds.clear();
	        do {
		        Conversation conv = Conversation.from(ConversationList.this, cursor);

                long threadId = conv.getThreadId();
                //if(DEBUG) Log.d(TAG, "selectAll, mSelectedThreadIds.add : "+threadId);
                //if(DEBUG) Log.d(TAG, "selectAll, conv.setIsChecked(true): "+threadId);

                mSelectedThreadIds.add(conv.getThreadId());
				conv.setIsChecked(true);
	        } while (cursor.moveToNext());
		} else {
            mSelectedThreadIds.clear();
	        mListAdapter.uncheckAll();
		}
		
		updateActionMode();
		mListAdapter.notifyDataSetChanged();
	}

    private void safeQuitDeleteMode() {
        try {
            Thread.sleep(300);
            changeToNormalMode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	private void changeToNormalMode() {
        if(DEBUG) Log.d(TAG, "[changeToNormalMode]");
        mViewMode = ViewMode.NORMAL_MODE;
        showActionMode(false);
        updateBottomNavigationVisibility();
        mSelectedThreadIds.clear();
        mSelectedThreadIds = null;
        mListAdapter.uncheckAll();
        updateFooterView();
        mListAdapter.setCheckBoxEnable(false);
        mListAdapter.notifyDataSetChanged();
        updateToolbarMenuVisible();//show menu_settings
        updateFloatActionButtonVisibility();//show mFloatButton
	}

    //lichao add in 2016-09-01 begin
    private void showEditMode(){
        if(DEBUG) Log.d(TAG, "[showEditMode]");
        mViewMode = ViewMode.EDIT_MODE;
        updateToolbarMenuVisible();//hide menu_settings first
        updateFloatActionButtonVisibility();//hide mFloatButton second
        updateFooterView();//hide mFooterView third
        if (mListAdapter != null && mListAdapter.getCurrentSliderView() != null) {
            mListAdapter.getCurrentSliderView().close(false);
            mListAdapter.setCurrentSliderView(null);
        }
        showActionMode(true);
        initBottomNavigationView();
        updateBottomNavigationVisibility();
        if(null == mSelectedThreadIds){
            mSelectedThreadIds = new HashSet<Long>();
        }
        mSelectedThreadIds.clear();
        //updateBottomMenuItems();
        //updateActionMode for updateBottomMenuItems and updateActionModeTitle
        updateActionMode();
        mListAdapter.setCheckBoxEnable(true);
        mListAdapter.notifyDataSetChanged();
    }
    //lichao add in 2016-09-01 end

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
        if(DEBUG) Log.d(TAG, "[onItemClick], position = "+position);

        if (mViewMode == ViewMode.SEARCH_MODE) {
            return;
        }

        if (mListAdapter == null) {
            return;
        }

        if(mListAdapter.getCurrentSliderView()!=null){
            mListAdapter.getCurrentSliderView().close(false);
            mListAdapter.setCurrentSliderView(null);
            return;
        }

        Cursor cursor  = (Cursor)mListView.getItemAtPosition(position);
        Conversation conv = Conversation.from(ConversationList.this, cursor);
        long threadId = conv.getThreadId();

        if(DEBUG) Log.d(TAG, "onItemClick, threadId = "+threadId);
		
		if (!isInDeleteMode()) {
            openThread(threadId);	        
			return;
		}

		final CheckBox checkBox = (CheckBox) view
				.findViewById(R.id.list_item_check_box);
		if (null != checkBox) {
			boolean checked_before = checkBox.isChecked();
            boolean check_now = !checked_before;
			checkBox.setChecked(check_now);
            if(DEBUG) Log.d(TAG, "onItemClick, >>>conv.setIsChecked: "+check_now);
            conv.setIsChecked(check_now);

			if(true == check_now) {
                if(DEBUG) Log.d(TAG, "onItemClick, mSelectedThreadIds.add: "+threadId);
                mSelectedThreadIds.add(threadId);
			} else {
                if(DEBUG) Log.d(TAG, "onItemClick, mSelectedThreadIds.remove: "+threadId);
                mSelectedThreadIds.remove(threadId);
			}
            //updateActionMode after mSelectedThreadIds size changed
            updateActionMode();
		}else{
            Log.d(TAG, "onItemClick, checkBox is null");
        }
	}
	
	private void updateActionMode() {
		if (mListAdapter == null) {
			finish();
			return;
		}
        if(mActionMode == null){
            mActionMode = getActionMode();
        }

		String mSelectAllStr = getResources().getString(R.string.selected_all);
		String mUnSelectAllStr = getResources().getString(R.string.deselected_all);
		
		int checkedCount = mSelectedThreadIds.size();
        if(DEBUG) Log.d(TAG, "updateActionMode, checkedCount = "+checkedCount);

		int all = mListAdapter.getCount();
        if(DEBUG) Log.d(TAG, "updateActionMode, all = "+all);

		if (checkedCount >= all) {
			mActionMode.setPositiveText(mUnSelectAllStr);
		} else {
			mActionMode.setPositiveText(mSelectAllStr);
		}

	    //mBottomNavigationView.setEnabled(checkedCount > 0);
        updateBottomMenuItems();

        //same as: getActionMode().setTitle(getString(R.string.selected_total_num, checkedCount));
		updateActionModeTitle(getString(R.string.selected_total_num, checkedCount));
	}

    //lichao merge from ComposeMessageActivity begin
    private void updateBottomMenuItems() {
        int checkedCount = mSelectedThreadIds.size();

        //Menu menu = mBottomNavigationView.getMenu();

        //see conversation_multi_select_menu.xml
        //as mst:menu="@menu/conversation_multi_select_menu" in conversation_list_screen.xml
        //MenuItem delete_thread = menu.findItem(R.id.menu_delete_thread);
        //delete_thread.setEnabled(checkedCount > 0);
        mBottomNavigationView.setItemEnable(R.id.menu_delete_thread, (checkedCount > 0));
        mBottomNavigationView.showItem(R.id.menu_delete_thread, true);

        //tangyisen
        //MenuItem mark_thread = menu.findItem(R.id.menu_mark_thread);
        //mark_thread.setEnabled(hasSelectedUnreadMessages);
        mBottomNavigationView.setItemEnable(R.id.menu_mark_thread, hasSelectedUnreadMessages());
        mBottomNavigationView.showItem(R.id.menu_mark_thread, true);
    }
    //lichao merge from ComposeMessageActivity end

	private boolean isInDeleteMode() {
	    //tangyisen modify
		//return getActionMode().isShowing();
	    return (mViewMode == ViewMode.EDIT_MODE);
	}

    private void initBottomNavigationView() {
        if (mBottomNavigationView == null) {
            mBottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation_view);
            mBottomNavigationView.setNavigationItemSelectedListener(mNavigationItemListener);
        }
    }
	
	private void updateBottomNavigationVisibility() {
        if (mBottomNavigationView != null) {
            if (mViewMode == ViewMode.EDIT_MODE || mViewMode == ViewMode.EDIT_BLACK_MODE) {
                mBottomNavigationView.setVisibility(View.VISIBLE);
            } else {
                mBottomNavigationView.setVisibility(View.GONE);
            }

        }
    }

    private OnNavigationItemSelectedListener mNavigationItemListener = new OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                //see conversation_multi_select_menu.xml
                case R.id.menu_delete_thread:
                    if (mSelectedThreadIds.size() <= 0){
                        return false;
                    }
                    confirmDeleteThreads(mSelectedThreadIds, mQueryHandler);
                    //lichao remove in 2016-12-02
                    //safeQuitDeleteMode();
                    return true;
                //tangyisen
                case R.id.menu_mark_thread:
                    boolean hasSelectedUnread = hasSelectedUnreadMessages();
                    if(DEBUG) Log.d(TAG, "NavigationItemSelectedListener, hasSelectedUnread = "+hasSelectedUnread);
                    if (hasSelectedUnread) {
                        //confirmMarkThreads(mSelectedThreadIds, mQueryHandler);
                        //confirmMarkThreadDialog(mSelectedThreadIds);
                        markThreadAsRead(mSelectedThreadIds);
                        safeQuitDeleteMode();
                        return true;
                    }else{
                        Toast.makeText(ConversationList.this, R.string.no_unread_messages_selected, Toast.LENGTH_SHORT).show();
                    }
                    return false;
                default:
                    return false;
            }
        }
    };

    private void markThreadAsRead(Collection<Long> threadIds) {
        for (long tmpthreadId : threadIds) {
            Conversation conv = Conversation.get(ConversationList.this, tmpthreadId, false);
            if(conv.hasUnreadMessages()){
                conv.markAsRead();
            }
        }
    }

    private void initToolBarAndActionbar() {
        //same as: myToolbar = getToolbar();
		myToolbar = (Toolbar) findViewById(com.mst.R.id.toolbar);
		myToolbar.setTitle(R.string.app_label);
		myToolbar.inflateMenu(R.menu.conversation_list_menu_mst);
		//inflateToolbarMenu(R.menu.conversation_list_menu_mst);
		//lichao add in 2016-10-31
        mToolbarMenu = myToolbar.getMenu();
		myToolbar.setOnMenuItemClickListener(this);
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

        if(mListAdapter!=null && mListAdapter.getCurrentSliderView()!=null){
            mListAdapter.getCurrentSliderView().close(false);
            mListAdapter.setCurrentSliderView(null);
            return;
        }

        //same as isInDeleteMode()
        if (mViewMode == ViewMode.EDIT_MODE) {
            changeToNormalMode();
            return;
        }
        //if(mIsSearchMode && mSearchView != null && !mSearchView.isFocused()){
        if(mViewMode == ViewMode.SEARCH_MODE){
            if(!isKeyboardShowing || TextUtils.isEmpty(mSearchView.getQuery())){
                exitSearchMode();
                return;
            }
        }
        super.onBackPressed();
    }

    //lichao add in 2016-10-31 begin
    public void enterSearchMode() {
        if (DEBUG) Log.i(TAG, "[enterSearchMode]");
        if(mViewMode != ViewMode.SEARCH_MODE){
            mViewMode = ViewMode.SEARCH_MODE;
            if(null != mListAdapter){
                //for Lock sliderView
                mListAdapter.setSearchMode(true);
            }
            mSearchStatus = SearchStatus.EMPTY_INPUT;
            //updateHeaderView();//hide HeaderView
            showEmptySearchListView();//set SearchAdapter
            updateEmptyView();//show EmptyView
            updateFooterView();//hide FooterView
            updateSearchIconVisibility();
            updateFloatActionButtonVisibility();
            updateBackIconVisibility();
            updateToolbarMenuVisible();
        }
    }
    //lichao add in 2016-10-31 end

    public void exitSearchMode() {
        if (DEBUG) Log.i(TAG, "[exitSearchMode]");
        if(mViewMode == ViewMode.SEARCH_MODE){
            //mSearchView.getQuery() is same as mSearchString
            if (!TextUtils.isEmpty(mSearchView.getQuery())) {
                mSearchView.setQuery(null, true);//CharSequence query, boolean submit
                mSearchString = "";
            }
            mViewMode = ViewMode.NORMAL_MODE;
            mSearchStatus = SearchStatus.EXITED;
            updateSearchIconVisibility();
            updateBackIconVisibility();
            updateToolbarMenuVisible();
            mListAdapter.setSearchMode(false);
            mListView.setAdapter(mListAdapter);
            mListAdapter.notifyDataSetChanged();
            updateEmptyView();
            updateFooterView();
            updateFloatActionButtonVisibility();
            clearSearchCache();
            clearSearchTask();
        }
    }

    private void showSoftKeyboard(View view, boolean show) {
        if(null == view){
            return;
        }
        if(show && isKeyboardShowing){
            return;
        }
        if(!show && !isKeyboardShowing){
            return;
        }
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null && show){
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            return;
        }
        if(imm != null && imm.isActive() && !show){
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            return;
        }
    }

    private void hideSoftKeyboardForCurrentFocusView() {
        if(!isKeyboardShowing){
            return;
        }
        View focusView = this.getCurrentFocus();
        if(focusView != null){
            final InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && imm.isActive()) {
                imm.hideSoftInputFromWindow(focusView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    private View mSearchContainer;

    private void initSearchView() {
        /*
        if (!getResources().getBoolean(R.bool.config_classify_search)) {
            mSearchView = (MstSearchView) findViewById(R.id.searchview);
            mSearchView.setOnQueryTextListener(mQueryTextListener);
            mSearchView.setQueryHint(getString(R.string.search_hint));
            //only show the search icon
            mSearchView.setIconifiedByDefault(true);
            //show the whole search input view
            mSearchView.onActionViewExpanded();
        }
        */
        final LayoutInflater inflater = (LayoutInflater) getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        //lichao add for back icon in 2016-10-31 begin
        View backIconView=inflater.inflate(R.layout.mst_back_icon_view, null);
        backIconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "backIconView onclick");
                //clearFocusForSearchView();
                mSearchView.clearFocus();
                exitSearchMode();
            }
        });
        mBackIcon=backIconView.findViewById(R.id.mst_back_icon_img);
        mBackIcon.setVisibility(View.GONE);
        myToolbar.addView(backIconView,
                new mst.widget.toolbar.Toolbar.LayoutParams(Gravity.CENTER_VERTICAL | Gravity.START));
        //lichao add for back icon in 2016-10-31 end
        mSearchContainer = inflater.inflate(R.layout.mst_search_bar_expanded, myToolbar,
				/* attachToRoot = */ false);
        mSearchContainer.setVisibility(View.VISIBLE);
        //remove the back arrow, as "showBackIcon(false)" is no use
        //myToolbar.removeViewAt(0);
        //myToolbar.addView(mSearchContainer);
        myToolbar.addView(mSearchContainer,
                new mst.widget.toolbar.Toolbar.LayoutParams(Gravity.CENTER_VERTICAL | Gravity.START));
        mSearchView=(MstSearchView)mSearchContainer.findViewById(R.id.search_view);
        //mSearchView.needHintIcon(false);
        //mSearchView.setVisibility(View.GONE);
        mSearchView.setQueryHint(getString(R.string.search_hint));
        mSearchView.setQueryHintTextColor(getResources().getColor(R.color.mst_searchview_hint_text_color));
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnQueryTextListener(mQueryTextListener);
        mSearchView.setOnQueryTextFocusChangeListener(mFocusChangeListener);
        //clear Focus For SearchView when onCreate()
        clearFocusForSearchView();
    }

    //lichao add for show FloatActionButton when hide keyboard begin
    private void setListenerToRootView() {
        final View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //int actionBarHeight = getActionBar().getHeight();
                int actionBarHeight = 144;
                if(null != myToolbar){
                    actionBarHeight = myToolbar.getHeight();
                }
                final int headerHeight = actionBarHeight + getStatusBarHeight();
                int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();

                if (heightDiff > headerHeight) {
                    //if(DEBUG) Log.d(TAG, "setListenerToRootView(), keyboard is up");
                    isKeyboardShowing = true;
                } else{
                    //if(DEBUG) Log.d(TAG, "setListenerToRootView(), keyboard is hidden");
                    //must judge if isKeyboardShowing in the before
                    if(isKeyboardShowing){
                        //after clearFocusForCurrentFocusView, go to mFocusChangeListener
                        //if mSearchString isEmpty, will exitSearchMode, and updateFloatActionButtonVisibility
                        //if mSearchString is not Empty, will keep in SearchMode
                        clearFocusForCurrentFocusView();
                    }
                    isKeyboardShowing = false;
                }
            }
        });
    }

    private int getStatusBarHeight(){
        Rect frame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        return frame.top;
    }
    //lichao add for show FloatActionButton when hide keyboard end

    OnFocusChangeListener mFocusChangeListener = new OnFocusChangeListener(){
        public void onFocusChange(View v, boolean hasFocus){
            if (DEBUG) Log.i(TAG, "[onFocusChange]");
            if(hasFocus){
                showSoftKeyboard(mSearchView, true);
            }else{
                showSoftKeyboard(mSearchView, false);
            }
            //mSearchString same as mSearchView.getQuery().toString();
            if(hasFocus){
                enterSearchMode();
            } else if(TextUtils.isEmpty(mSearchView.getQuery())){
                // has not Focus && mSearchView.getQuery() isEmpty
                exitSearchMode();
            }
        }
    };
	
    private void clearFocusForSearchView(){
        //it works while add codes in manifest: android:windowSoftInputMode="stateAlwaysHidden"
        if (mSearchView != null) {
            mSearchView.clearFocus();
            //clear Focus on mSearchView completely by myToolbar.requestFocus()
            if(null != myToolbar){
                myToolbar.setFocusableInTouchMode(true);
                myToolbar.setFocusable(true);
                myToolbar.requestFocus();
            }
        }
    }

    private void clearFocusForCurrentFocusView() {
        View focusView = this.getCurrentFocus();
        if(focusView != null){
            focusView.clearFocus();
        }
    }

    /*
    @Override
    public void updateOptionMenu(){
    	Menu menu = getOptionMenu();
        MenuItem item = menu.findItem(R.id.action_delete_all);
        if (item != null) {
            item.setVisible((mListAdapter.getCount() > 0) && mIsSmsEnabled);
        }
    }
    */
    
    @Override
    public boolean onMenuItemClick(MenuItem item) {

        Log.d(TAG, "onMenuItemClick--->"+item.getTitle());

        switch(item.getItemId()) {
            /*
            case R.id.search:
                if (getResources().getBoolean(R.bool.config_classify_search)) {
                    Intent searchintent = new Intent(this, SearchActivityExtend.class);
                    startActivityIfNeeded(searchintent, -1);
                    break;
                }
                return true;

            case R.id.action_delete_all:
                // The invalid threadId of -1 means all threads here.
                confirmDeleteThread(-1L, mQueryHandler);
                return true;
            */
            case R.id.action_settings:
                Intent intent = new Intent(this, MessagingPreferenceActivity.class);
                startActivityIfNeeded(intent, -1);
                return true;
            /*
            case R.id.action_cell_broadcasts:
                try {
                    startActivity(MessageUtils.getCellBroadcastIntent());
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, "ActivityNotFoundException for CellBroadcastListActivity");
                }
                return true;
            */
            default:
                return false;
        }
    }

    private void initListView() {
        mListView = (MstListView) findViewById(R.id.conversation_list);
        //mListView.setItemsCanFocus(true);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        mListView.setFastScrollEnabled(false);
        mListView.setFastScrollAlwaysVisible(false);
        mListView.setOnKeyListener(mThreadListKeyListener);

        //XiaoYuan SDK start
        mListView.setOnScrollListener(mScrollListener);
        //XiaoYuan SDK end

        // Tell the list view which view to display when the list is empty
        //mListView.setEmptyView(findViewById(R.id.empty));
        initEmptyView();

        final LayoutInflater inflater = (LayoutInflater) getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mFooterView = inflater.inflate(R.layout.conversation_list_footer, null, false);
        //View v,Object data,boolean isSelectable
        mListView.addFooterView(mFooterView, null, false);

        mHeaderView = inflater.inflate(R.layout.conversation_list_header, null, false);
        //View v,Object data,boolean isSelectable
        mListView.addHeaderView(mHeaderView, null, false);
    }

    private void initListAdapter() {
        mListAdapter = new ConversationListAdapter(this, null);
        mListAdapter.setOnContentChangedListener(mContentChangedListener);
        mListAdapter.setOnSliderButtonLickListener(this);//lichao add

        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        int screenWidthDip = MessageUtils.px2dip(this, screenWidth);
        mListAdapter.setScreenWidthDip(screenWidthDip);

        mListView.setAdapter(mListAdapter);//lichao modify
        mListView.setRecyclerListener(mListAdapter);//lichao modify
    }

    private void initFloatActionButton() {
        if (mFloatButton == null) {
            mFloatButton = (FloatingActionButton) findViewById(R.id.floating_action_button);
        }
        mFloatButton.setOnFloatingActionButtonClickListener(new OnFloatActionButtonClickListener() {
            public void onClick(View view) {
                if (mIsSmsEnabled) {
                    createNewMessage();
                } else {
                    // Display a toast letting the user know they can not compose.
                    if (mComposeDisabledToast == null) {
                        mComposeDisabledToast = Toast.makeText(ConversationList.this,
                                R.string.compose_disabled_toast, Toast.LENGTH_SHORT);
                    }
                    mComposeDisabledToast.show();
                    startActivity(MmsConfig.getRequestDefaultSmsAppActivity());
                }
            }
        });
	}

    private void updateFloatActionButtonVisibility(){
        if(mViewMode == ViewMode.NORMAL_MODE){
            //modify for do not show the afterimage
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mFloatButton.setVisibility(View.VISIBLE);
                }
            }, 150);
        } else {
            mFloatButton.setVisibility(View.GONE);
        }
    }

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if(DEBUG) Log.d(TAG, "[onItemLongClick], position = "+position);
        if(mViewMode != ViewMode.NORMAL_MODE){
            return true;
        }
        showEditMode();
        //return false here so will goto onItemClick() after
        return false;
	}

	private void startSearch(final String mSearchString) {
        mSearchTask = new SearchTask(ConversationList.this);
        mSearchTask.setSearchString(mSearchString);
        mSearchTask.setMySearchListener(mySearchListener);
        //mSearchTask.execute();
        mSearchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
	}

    private void clearSearchTask(){
        if(mSearchTask != null){
            mSearchTask.setSearchString(null);
            mSearchTask.cancel();
            mSearchTask = null;
        }
    }

    //clearSearchCache only when exitSearchMode
    private void clearSearchCache(){
        if(DEBUG) Log.d(TAG, "[clearSearchCache]");
        if(null == mSearchCursorCache){
            return;
        }
        for (Map.Entry<String, Cursor> entry : mSearchCursorCache.entrySet()) {
            Cursor c = entry.getValue();
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        mSearchCursorCache.clear();
    }

    //lichao add in 2016-09-24
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
        if (mViewMode == ViewMode.SEARCH_MODE && null != mSearchAdapter && mSearchAdapter.getCount() == 0) {
            if(mSearchStatus == SearchStatus.IS_SEARCHING){
                mEmptyTextview.setVisibility(View.GONE);
                mEmptyImageView.setVisibility(View.GONE);
                mEmptyHeaderText.setText(getString(R.string.searching));
                mEmptyHeaderText.setVisibility(View.VISIBLE);
                mEmptyHeaderImage.setVisibility(View.VISIBLE);
                mEmptyLoadAnim.start();
            }else{
                mEmptyLoadAnim.stop();
                mEmptyHeaderImage.setVisibility(View.GONE);
                mEmptyHeaderText.setVisibility(View.GONE);
                mEmptyTextview.setText(getString(R.string.none_matched_message));
                mEmptyTextview.setVisibility(View.VISIBLE);
                mEmptyImageView.setVisibility(View.VISIBLE);
            }
            return;
        }
    }

    private void updateFooterView(){
        if (mFooterText == null) {
            ViewStub stub = (ViewStub) mFooterView.findViewById(R.id.viewstub_conv_list_footer);
            if (stub != null) {
                stub.inflate();
            }
            mFooterText = (TextView) mFooterView.findViewById(R.id.list_footer_count_textview);
        }
        if(mViewMode == ViewMode.EDIT_MODE || mViewMode == ViewMode.EDIT_BLACK_MODE){
            mFooterView.setVisibility(View.GONE);
            mFooterText.setVisibility(View.GONE);
        }
        else if (mViewMode == ViewMode.SEARCH_MODE) {
            int msgCount = 0;
            if(null != mSearchAdapter){
                msgCount = mSearchAdapter.getCount();
                //msgCount = mSearchAdapter.getMmsDatasCount();
            }
            //Log.d(TAG, "updateFooterView, msgCount = "+msgCount);
            //int visibleItemCount = mListView.getLastVisiblePosition()-mListView.getFirstVisiblePosition()+1;
            //Log.d(TAG, "updateFooterView, visibleItemCount = "+visibleItemCount);
            if(msgCount <= 6){
                mFooterView.setVisibility(View.GONE);
                mFooterText.setVisibility(View.GONE);
            }else{
                mFooterView.setVisibility(View.VISIBLE);
                mFooterText.setVisibility(View.VISIBLE);
                mFooterText.setText(ConversationList.this
                        .getString(R.string.mst_searched_messages_count, msgCount));
            }
        }
        else{
            int listCount = 0;
            if(null != mListAdapter){
                listCount = mListAdapter.getCount();
            }
            //Log.d(TAG, "updateFooterView, listCount = "+listCount);
            if(listCount <= 6){
                mFooterView.setVisibility(View.GONE);
                mFooterText.setVisibility(View.GONE);
            }else{
                mFooterView.setVisibility(View.VISIBLE);
                mFooterText.setVisibility(View.VISIBLE);
                mFooterText.setText(ConversationList.this
                        .getString(R.string.mst_total_conversation_count, listCount));
            }
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
        if (mSearchStatus == SearchStatus.IS_SEARCHING) {
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

    //lichao add in 2016-10-31 begin
    private void updateBackIconVisibility(){
        if(null != mBackIcon){
            mBackIcon.setVisibility((mViewMode == ViewMode.SEARCH_MODE) ? View.VISIBLE : View.GONE);
        }
    }

    private void updateSearchIconVisibility(){
        if(null != mSearchView){
            mSearchView.needHintIcon(mViewMode != ViewMode.SEARCH_MODE);
        }
    }

    private void updateToolbarMenuVisible(){
        MenuItem menu_settings = mToolbarMenu.findItem(R.id.action_settings);
        if(null != menu_settings){
            menu_settings.setVisible(mViewMode != ViewMode.SEARCH_MODE);
        }
    }
    //lichao add in 2016-10-31 end

    //lichao add in 2016-11-02 begin
    private boolean hasSelectedUnreadMessages(){
        if(null == mSelectedThreadIds){
            return false;
        }
        if(mSelectedThreadIds.size() <= 0){
            return false;
        }
        Conversation conv = null;
        for(long tmpthreadId : mSelectedThreadIds) {
            conv = Conversation.get(ConversationList.this, tmpthreadId, false);
            if(conv.hasUnreadMessages()){
                return true;
            }
        }
        return false;
    }
    //lichao add in 2016-11-02 end

    //lichao add in 2016-11-22 begin
    private SearchTask.MySearchListener mySearchListener = new SearchTask.MySearchListener() {
        @Override
        public void onSearchCompleted(boolean isCursorChanged) {
            Log.d(TAG, "mySearchListener");
            mSearchStatus = SearchStatus.SHOWING_RESULT;
            updateHeaderView();
            showSearchResultListView(isCursorChanged);
            updateEmptyView();
            updateFooterView();
        }
    };

    private void showSearchResultListView(boolean isCursorChanged) {
        if (null == mSearchTask) {
            return;
        }
        if (null == mSearchAdapter) {
            mSearchAdapter = new SearchListAdapter(ConversationList.this, null);
            mSearchAdapter.setOnContentChangedListener(mSearchContentChangedListener);
        }
        if (isCursorChanged) {
            mSearchAdapter.changeCursor(mSearchTask.getSearchedCursor());
            mSearchAdapter.setSearchString(mSearchString);
        }
        mListView.setAdapter(mSearchAdapter);
        mSearchAdapter.notifyDataSetChanged();
    }

    private void showEmptySearchListView() {
        if (null == mSearchAdapter) {
            mSearchAdapter = new SearchListAdapter(ConversationList.this, null);
        } else {
            mSearchAdapter.changeCursor(null);
        }
        mSearchAdapter.setSearchString(mSearchString);
        mListView.setAdapter(mSearchAdapter);
        mSearchAdapter.notifyDataSetChanged();
    }
    //lichao add in 2016-11-22 end
	
    //XiaoYuan SDK start
	OnScrollListener mScrollListener = new OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

            switch (scrollState) {
                case OnScrollListener.SCROLL_STATE_FLING:
                case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    mListAdapter.SCROLL_STATE_FLING = true;
                    if(mViewMode == ViewMode.SEARCH_MODE){
                        //hideSoftKeyboard >>>setListenerToRootView() >>>clearFocusForSearchView()
                        hideSoftKeyboardForCurrentFocusView ();
                    }
                    break;
                default:
                    mListAdapter.SCROLL_STATE_FLING = false;
                    mListAdapter.notifyDataSetChanged();
                    break;
            }

        }

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			if(firstVisibleItem ==0){
				if(mListAdapter !=null){
					mListAdapter.SCROLL_STATE_FLING=false;

				}
			}
			else if(firstVisibleItem+visibleItemCount == totalItemCount){
				if(mListAdapter !=null){
					mListAdapter.SCROLL_STATE_FLING=false;
				}
			}

		}
	};
	//XiaoYuan SDK end
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
    //add by lgy or lichao end
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	
}
