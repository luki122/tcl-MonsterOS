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

package com.android.mms.ui;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Directory;
import android.text.TextUtils;
import android.text.util.Rfc822Token;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.android.ex.chips.RecipientEntry;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.Inflater;

import com.android.mms.R;

/**
 * Adapter for showing a recipient list.
 */
public class MstRecipientAdapter /*extends BaseAdapter implements Filterable, AccountSpecifier*/ {/*
    private static final String TAG = "BaseRecipientAdapter";

    private static final boolean DEBUG = false;

    private static final int DEFAULT_PREFERRED_MAX_RESULT_COUNT = 10;
    static final int ALLOWANCE_FOR_DUPLICATES = 5;

    // This is ContactsContract.PRIMARY_ACCOUNT_NAME. Available from ICS as hidden
    static final String PRIMARY_ACCOUNT_NAME = "name_for_primary_account";
    // This is ContactsContract.PRIMARY_ACCOUNT_TYPE. Available from ICS as hidden
    static final String PRIMARY_ACCOUNT_TYPE = "type_for_primary_account";

    private static final int MESSAGE_SEARCH_PENDING_DELAY = 1000;
    private static final int MESSAGE_SEARCH_PENDING = 1;

    public final static class DirectorySearchParams {
        public long directoryId;
        public String directoryType;
        public String displayName;
        public String accountName;
        public String accountType;
        public CharSequence constraint;
        public DirectoryFilter filter;
    }

    protected static class DirectoryListQuery {

        public static final Uri URI =
                Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "directories");
        public static final String[] PROJECTION = {
            Directory._ID,              // 0
            Directory.ACCOUNT_NAME,     // 1
            Directory.ACCOUNT_TYPE,     // 2
            Directory.DISPLAY_NAME,     // 3
            Directory.PACKAGE_NAME,     // 4
            Directory.TYPE_RESOURCE_ID, // 5
        };

        public static final int ID = 0;
        public static final int ACCOUNT_NAME = 1;
        public static final int ACCOUNT_TYPE = 2;
        public static final int DISPLAY_NAME = 3;
        public static final int PACKAGE_NAME = 4;
        public static final int TYPE_RESOURCE_ID = 5;
    }

    protected static class TemporaryEntry {
        public final String displayName;
        public final String destination;
        public final int destinationType;
        public final String destinationLabel;
        public final long contactId;
        public final Long directoryId;
        public final long dataId;
        public final String thumbnailUriString;
        public final int displayNameSource;
        public final String lookupKey;

        public TemporaryEntry(
                String displayName,
                String destination,
                int destinationType,
                String destinationLabel,
                long contactId,
                Long directoryId,
                long dataId,
                String thumbnailUriString,
                int displayNameSource,
                String lookupKey) {
            this.displayName = displayName;
            this.destination = destination;
            this.destinationType = destinationType;
            this.destinationLabel = destinationLabel;
            this.contactId = contactId;
            this.directoryId = directoryId;
            this.dataId = dataId;
            this.thumbnailUriString = thumbnailUriString;
            this.displayNameSource = displayNameSource;
            this.lookupKey = lookupKey;
        }

        public TemporaryEntry(Cursor cursor, Long directoryId) {
            this.displayName = cursor.getString(Queries.Query.NAME);
            this.destination = cursor.getString(Queries.Query.DESTINATION);
            this.destinationType = cursor.getInt(Queries.Query.DESTINATION_TYPE);
            this.destinationLabel = cursor.getString(Queries.Query.DESTINATION_LABEL);
            this.contactId = cursor.getLong(Queries.Query.CONTACT_ID);
            this.directoryId = directoryId;
            this.dataId = cursor.getLong(Queries.Query.DATA_ID);
            this.thumbnailUriString = cursor.getString(Queries.Query.PHOTO_THUMBNAIL_URI);
            this.displayNameSource = cursor.getInt(Queries.Query.DISPLAY_NAME_SOURCE);
            this.lookupKey = cursor.getString(Queries.Query.LOOKUP_KEY);
        }
    }

    private static class DefaultFilterResult {
        public final List<RecipientEntry> entries;
        public final LinkedHashMap<Long, List<RecipientEntry>> entryMap;
        public final List<RecipientEntry> nonAggregatedEntries;
        public final Set<String> existingDestinations;
        public final List<DirectorySearchParams> paramsList;

        public DefaultFilterResult(List<RecipientEntry> entries,
                LinkedHashMap<Long, List<RecipientEntry>> entryMap,
                List<RecipientEntry> nonAggregatedEntries,
                Set<String> existingDestinations,
                List<DirectorySearchParams> paramsList) {
            this.entries = entries;
            this.entryMap = entryMap;
            this.nonAggregatedEntries = nonAggregatedEntries;
            this.existingDestinations = existingDestinations;
            this.paramsList = paramsList;
        }
    }

    private final class DefaultFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (DEBUG) {
                Log.d(TAG, "start filtering. constraint: " + constraint + ", thread:"
                        + Thread.currentThread());
            }

            final FilterResults results = new FilterResults();
            Cursor defaultDirectoryCursor = null;
            Cursor directoryCursor = null;

            if (TextUtils.isEmpty(constraint)) {
                clearTempEntries();
                // Return empty results.
                return results;
            }

            try {
                defaultDirectoryCursor = doQuery(constraint, mPreferredMaxResultCount,
                        null );

                if (defaultDirectoryCursor == null) {
                    if (DEBUG) {
                        Log.w(TAG, "null cursor returned for default Email filter query.");
                    }
                } else {
                    // These variables will become mEntries, mEntryMap, mNonAggregatedEntries, and
                    // mExistingDestinations. Here we shouldn't use those member variables directly
                    // since this method is run outside the UI thread.
                    final LinkedHashMap<Long, List<RecipientEntry>> entryMap =
                            new LinkedHashMap<Long, List<RecipientEntry>>();
                    final List<RecipientEntry> nonAggregatedEntries =
                            new ArrayList<RecipientEntry>();
                    final Set<String> existingDestinations = new HashSet<String>();

                    while (defaultDirectoryCursor.moveToNext()) {
                        // Note: At this point each entry doesn't contain any photo
                        // (thus getPhotoBytes() returns null).
                        putOneEntry(new TemporaryEntry(defaultDirectoryCursor,
                                null  directoryId ),
                                true, entryMap, nonAggregatedEntries, existingDestinations);
                    }

                    // We'll copy this result to mEntry in publicResults() (run in the UX thread).
                    final List<RecipientEntry> entries = constructEntryList(
                            entryMap, nonAggregatedEntries);

                    final List<DirectorySearchParams> paramsList =
                            searchOtherDirectories(existingDestinations);

                    results.values = new DefaultFilterResult(
                            entries, entryMap, nonAggregatedEntries,
                            existingDestinations, paramsList);
                    results.count = entries.size();
                }
            } finally {
                if (defaultDirectoryCursor != null) {
                    defaultDirectoryCursor.close();
                }
                if (directoryCursor != null) {
                    directoryCursor.close();
                }
            }
            return results;
        }

        @Override
        protected void publishResults(final CharSequence constraint, FilterResults results) {
            mCurrentConstraint = constraint;
            clearTempEntries();

            if (results.values != null) {
                DefaultFilterResult defaultFilterResult = (DefaultFilterResult) results.values;
                mEntryMap = defaultFilterResult.entryMap;
                mNonAggregatedEntries = defaultFilterResult.nonAggregatedEntries;
                mExistingDestinations = defaultFilterResult.existingDestinations;

                cacheCurrentEntriesIfNeeded(defaultFilterResult.entries.size(),
                        defaultFilterResult.paramsList == null ? 0 :
                                defaultFilterResult.paramsList.size());

                updateEntries(defaultFilterResult.entries);
            } else {
                updateEntries(Collections.<RecipientEntry>emptyList());
            }
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            final RecipientEntry entry = (RecipientEntry)resultValue;
            final String displayName = entry.getDisplayName();
            final String emailAddress = entry.getDestination();
            if (TextUtils.isEmpty(displayName) || TextUtils.equals(displayName, emailAddress)) {
                 return emailAddress;
            } else {
                return new Rfc822Token(displayName, emailAddress, null).toString();
            }
        }
    }

    protected List<DirectorySearchParams> searchOtherDirectories(Set<String> existingDestinations) {
        // After having local results, check the size of results. If the results are
        // not enough, we search remote directories, which will take longer time.
        final int limit = mPreferredMaxResultCount - existingDestinations.size();
        if (limit > 0) {
            if (DEBUG) {
                Log.d(TAG, "More entries should be needed (current: "
                        + existingDestinations.size()
                        + ", remaining limit: " + limit + ") ");
            }
            Cursor directoryCursor = null;
            try {
                directoryCursor = mContentResolver.query(
                        DirectoryListQuery.URI, DirectoryListQuery.PROJECTION,
                        null, null, null);
                return setupOtherDirectories(mContext, directoryCursor, mAccount);
            } finally {
                if (directoryCursor != null) {
                    directoryCursor.close();
                }
            }
        } else {
            // We don't need to search other directories.
            return null;
        }
    }

    private final Context mContext;
    private final ContentResolver mContentResolver;
    private Account mAccount;
    protected final int mPreferredMaxResultCount;
    private DropdownChipLayouter mDropdownChipLayouter;

    private LinkedHashMap<Long, List<RecipientEntry>> mEntryMap;
    private List<RecipientEntry> mNonAggregatedEntries;
    private Set<String> mExistingDestinations;
    private List<RecipientEntry> mEntries;
    private List<RecipientEntry> mTempEntries;

    private int mRemainingDirectoryCount;

    protected CharSequence mCurrentConstraint;


    private final class DelayedMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (mRemainingDirectoryCount > 0) {
                updateEntries(constructEntryList());
            }
        }

        public void sendDelayedLoadMessage() {
            sendMessageDelayed(obtainMessage(MESSAGE_SEARCH_PENDING, 0, 0, null),
                    MESSAGE_SEARCH_PENDING_DELAY);
        }

        public void removeDelayedLoadMessage() {
            removeMessages(MESSAGE_SEARCH_PENDING);
        }
    }

    private final DelayedMessageHandler mDelayedMessageHandler = new DelayedMessageHandler();

    private EntriesUpdatedObserver mEntriesUpdatedObserver;

    public MstRecipientAdapter(Context context) {
        this(context, DEFAULT_PREFERRED_MAX_RESULT_COUNT);
    }

    public MstRecipientAdapter(Context context, int preferredMaxResultCount) {
        mContext = context;
        mContentResolver = context.getContentResolver();
        mPreferredMaxResultCount = preferredMaxResultCount;
    }

    public Context getContext() {
        return mContext;
    }

    public boolean forceShowAddress() {
        return false;
    }

    public void getMatchingRecipients(ArrayList<String> inAddresses,
            RecipientAlternatesAdapter.RecipientMatchCallback callback) {
        RecipientAlternatesAdapter.getMatchingRecipients(
                getContext(), this, inAddresses, getAccount(), callback);
    }

    @Override
    public void setAccount(Account account) {
        mAccount = account;
    }

    @Override
    public Filter getFilter() {
        return new DefaultFilter();
    }

    public Map<String, RecipientEntry> getMatchingRecipients(Set<String> addresses) {
        return null;
    }

    public static List<DirectorySearchParams> setupOtherDirectories(Context context,
            Cursor directoryCursor, Account account) {
        final PackageManager packageManager = context.getPackageManager();
        final List<DirectorySearchParams> paramsList = new ArrayList<DirectorySearchParams>();
        DirectorySearchParams preferredDirectory = null;
        while (directoryCursor.moveToNext()) {
            final long id = directoryCursor.getLong(DirectoryListQuery.ID);

            // Skip the local invisible directory, because the default directory already includes
            // all local results.
            if (id == Directory.LOCAL_INVISIBLE) {
                continue;
            }

            final DirectorySearchParams params = new DirectorySearchParams();
            final String packageName = directoryCursor.getString(DirectoryListQuery.PACKAGE_NAME);
            final int resourceId = directoryCursor.getInt(DirectoryListQuery.TYPE_RESOURCE_ID);
            params.directoryId = id;
            params.displayName = directoryCursor.getString(DirectoryListQuery.DISPLAY_NAME);
            params.accountName = directoryCursor.getString(DirectoryListQuery.ACCOUNT_NAME);
            params.accountType = directoryCursor.getString(DirectoryListQuery.ACCOUNT_TYPE);
            if (packageName != null && resourceId != 0) {
                try {
                    final Resources resources =
                            packageManager.getResourcesForApplication(packageName);
                    params.directoryType = resources.getString(resourceId);
                    if (params.directoryType == null) {
                        Log.e(TAG, "Cannot resolve directory name: "
                                + resourceId + "@" + packageName);
                    }
                } catch (NameNotFoundException e) {
                    Log.e(TAG, "Cannot resolve directory name: "
                            + resourceId + "@" + packageName, e);
                }
            }

            // If an account has been provided and we found a directory that
            // corresponds to that account, place that directory second, directly
            // underneath the local contacts.
            if (account != null && account.name.equals(params.accountName) &&
                    account.type.equals(params.accountType)) {
                preferredDirectory = params;
            } else {
                paramsList.add(params);
            }
        }

        if (preferredDirectory != null) {
            paramsList.add(1, preferredDirectory);
        }

        return paramsList;
    }

    protected void startSearchOtherDirectories(
            CharSequence constraint, List<DirectorySearchParams> paramsList, int limit) {
        final int count = paramsList.size();
        // Note: skipping the default partition (index 0), which has already been loaded
        for (int i = 1; i < count; i++) {
            final DirectorySearchParams params = paramsList.get(i);
            params.constraint = constraint;
            if (params.filter == null) {
                params.filter = new DirectoryFilter(params);
            }
            params.filter.setLimit(limit);
            params.filter.filter(constraint);
        }

        // Directory search started. We may show "waiting" message if directory results are slow
        // enough.
        mRemainingDirectoryCount = count - 1;
        mDelayedMessageHandler.sendDelayedLoadMessage();
    }

    protected void putOneEntry(TemporaryEntry entry, boolean isAggregatedEntry) {
        putOneEntry(entry, isAggregatedEntry,
                mEntryMap, mNonAggregatedEntries, mExistingDestinations);
    }

    private static void putOneEntry(TemporaryEntry entry, boolean isAggregatedEntry,
            LinkedHashMap<Long, List<RecipientEntry>> entryMap,
            List<RecipientEntry> nonAggregatedEntries,
            Set<String> existingDestinations) {
        if (existingDestinations.contains(entry.destination)) {
            return;
        }

        existingDestinations.add(entry.destination);

        if (!isAggregatedEntry) {
            nonAggregatedEntries.add(RecipientEntry.constructTopLevelEntry(
                    entry.displayName,
                    entry.displayNameSource,
                    entry.destination, entry.destinationType, entry.destinationLabel,
                    entry.contactId, entry.directoryId, entry.dataId, entry.thumbnailUriString,
                    true, entry.lookupKey));
        } else if (entryMap.containsKey(entry.contactId)) {
            // We already have a section for the person.
            final List<RecipientEntry> entryList = entryMap.get(entry.contactId);
            entryList.add(RecipientEntry.constructSecondLevelEntry(
                    entry.displayName,
                    entry.displayNameSource,
                    entry.destination, entry.destinationType, entry.destinationLabel,
                    entry.contactId, entry.directoryId, entry.dataId, entry.thumbnailUriString,
                    true, entry.lookupKey));
        } else {
            final List<RecipientEntry> entryList = new ArrayList<RecipientEntry>();
            entryList.add(RecipientEntry.constructTopLevelEntry(
                    entry.displayName,
                    entry.displayNameSource,
                    entry.destination, entry.destinationType, entry.destinationLabel,
                    entry.contactId, entry.directoryId, entry.dataId, entry.thumbnailUriString,
                    true, entry.lookupKey));
            entryMap.put(entry.contactId, entryList);
        }
    }

    protected List<RecipientEntry> constructEntryList() {
        return constructEntryList(mEntryMap, mNonAggregatedEntries);
    }

    private List<RecipientEntry> constructEntryList(
            LinkedHashMap<Long, List<RecipientEntry>> entryMap,
            List<RecipientEntry> nonAggregatedEntries) {
        final List<RecipientEntry> entries = new ArrayList<RecipientEntry>();
        int validEntryCount = 0;
        for (Map.Entry<Long, List<RecipientEntry>> mapEntry : entryMap.entrySet()) {
            final List<RecipientEntry> entryList = mapEntry.getValue();
            final int size = entryList.size();
            for (int i = 0; i < size; i++) {
                RecipientEntry entry = entryList.get(i);
                entries.add(entry);
                validEntryCount++;
            }
            if (validEntryCount > mPreferredMaxResultCount) {
                break;
            }
        }
        if (validEntryCount <= mPreferredMaxResultCount) {
            for (RecipientEntry entry : nonAggregatedEntries) {
                if (validEntryCount > mPreferredMaxResultCount) {
                    break;
                }
                entries.add(entry);
                validEntryCount++;
            }
        }

        return entries;
    }


    public interface EntriesUpdatedObserver {
        public void onChanged(List<RecipientEntry> entries);
    }

    public void registerUpdateObserver(EntriesUpdatedObserver observer) {
        mEntriesUpdatedObserver = observer;
    }

    protected void updateEntries(List<RecipientEntry> newEntries) {
        mEntries = newEntries;
        mEntriesUpdatedObserver.onChanged(newEntries);
        notifyDataSetChanged();
    }

    protected void cacheCurrentEntriesIfNeeded(int newEntryCount, int paramListCount) {
        if (newEntryCount == 0 && paramListCount > 1) {
            cacheCurrentEntries();
        }
    }

    protected void cacheCurrentEntries() {
        mTempEntries = mEntries;
    }

    protected void clearTempEntries() {
        mTempEntries = null;
    }

    protected List<RecipientEntry> getEntries() {
        return mTempEntries != null ? mTempEntries : mEntries;
    }

    public static final Uri MST_CONTENT_FILTER_URI = Uri.withAppendedPath(
        Phone.CONTENT_URI, "mstfilter");
    private String[] mst_project = new String[] {
        Contacts.DISPLAY_NAME,                          // 0
        Phone.NUMBER,                                   // 1
        Phone.TYPE,                                     // 2
        Phone.LABEL,                                    // 3
        Phone.CONTACT_ID,                               // 4
        Phone._ID,                                      // 5
        Contacts.PHOTO_THUMBNAIL_URI,                   // 6
        Contacts.DISPLAY_NAME_SOURCE,                   // 7
        Contacts.LOOKUP_KEY,                            // 8
        ContactsContract.CommonDataKinds.Email.MIMETYPE // 9
    };

    private Cursor doQuery(CharSequence constraint, int limit, Long directoryId) {
        
        final Uri.Builder builder = MST_CONTENT_FILTER_URI.buildUpon()
                .appendPath(constraint.toString())
                .appendQueryParameter(ContactsContract.LIMIT_PARAM_KEY,
                        String.valueOf(limit + ALLOWANCE_FOR_DUPLICATES));
        if (directoryId != null) {
            builder.appendQueryParameter(ContactsContract.DIRECTORY_PARAM_KEY,
                    String.valueOf(directoryId));
        }
        if (mAccount != null) {
            builder.appendQueryParameter(PRIMARY_ACCOUNT_NAME, mAccount.name);
            builder.appendQueryParameter(PRIMARY_ACCOUNT_TYPE, mAccount.type);
        }
        final long start = System.currentTimeMillis();
        final Cursor cursor = mContentResolver.query(
                builder.build(), mst_project, null, null, null);
        final long end = System.currentTimeMillis();
        if (DEBUG) {
            Log.d(TAG, "Time for autocomplete (query: " + constraint
                    + ", directoryId: " + directoryId + ", num_of_results: "
                    + (cursor != null ? cursor.getCount() : "null") + "): "
                    + (end - start) + " ms");
        }
        return cursor;
    }

    @Override
    public int getCount() {
        final List<RecipientEntry> entries = getEntries();
        return entries != null ? entries.size() : 0;
    }

    @Override
    public RecipientEntry getItem(int position) {
        return getEntries().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return RecipientEntry.ENTRY_TYPE_SIZE;
    }

    @Override
    public int getItemViewType(int position) {
        return getEntries().get(position).getEntryType();
    }

    @Override
    public boolean isEnabled(int position) {
        return getEntries().get(position).isSelectable();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final RecipientEntry entry = getEntries().get(position);
        final String constraint = mCurrentConstraint == null ? null :
                mCurrentConstraint.toString();
        ViewHolder holder = null;
        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.recipients_editor_popupwindow, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        holder = (ViewHolder)convertView.getTag();
        bindTextToView(entry.getDisplayName(), holder.displayNameView);
        bindTextToView(entry.getDestination(), holder.destinationView);
        return convertView;
    }

    protected void bindTextToView(CharSequence text, TextView view) {
        if (view == null) {
            return;
        }

        if (text != null) {
            view.setText(text);
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    protected class ViewHolder {
        public final TextView displayNameView;
        public final TextView destinationView;

        public ViewHolder(View view) {
            displayNameView = (TextView) view.findViewById(R.id.title);
            destinationView = (TextView) view.findViewById(R.id.text1);
        }
    }
*/}
