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
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.util.Rfc822Token;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.support.annotation.Nullable;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.android.mms.R;

/**
 * Adapter for showing a recipient list.
 */
public class MstRecipientAdapter extends BaseAdapter implements Filterable {
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

    /*private static class DefaultFilterResult {
        public String number;
        public String displayName;
        public String matchString;

        public DefaultFilterResult(String number, String displayName, String matchString) {
            super();
            this.number = number;
            this.displayName = displayName;
            this.matchString = matchString;
        }
    }*/
    public static final int NUMBER = 0;         // String
    public static final int NAME = 1;                // String
    public static final int CONTACT_ID = 2;          // long
    public static final int DATA_ID = 3;             // long
    public static final int LOOKUP_KEY = 4;          // String

    ArrayList<MatchEntry> mMatchEntry = new ArrayList<>();

    //tmp not add email
    public class MatchEntry {
        private String number;
        private String displayName;
        private String matchString;
        private long contactId;
        private long dataId;
        private String lookupKey;

        public MatchEntry(String number, String displayName, String matchString, long contactId, long dataId,
            String lookupKey) {
            super();
            this.number = number;
            this.displayName = displayName;
            this.matchString = matchString;
            this.contactId = contactId;
            this.dataId = dataId;
            this.lookupKey = lookupKey;
        }

        public MatchEntry(Cursor cursor) {
            this.number = cursor.getString(NUMBER);
            this.displayName = cursor.getString(NAME);
            this.matchString = null;
            this.contactId = cursor.getLong(CONTACT_ID);
            this.dataId = cursor.getLong(DATA_ID);
            this.lookupKey = cursor.getString(LOOKUP_KEY);
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public long getDataId() {
            return dataId;
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
            Cursor cursor = null;

            if (TextUtils.isEmpty(constraint)) {
                return results;
            }

            try {
                cursor = doQuery(constraint, mPreferredMaxResultCount);

                if (cursor == null) {
                    if (DEBUG) {
                        Log.w(TAG, "null cursor returned for default Email filter query.");
                    }
                } else {
                    if(cursor.moveToFirst()) {
                        do {
                            MatchEntry entry = new MatchEntry(cursor);
                            mMatchEntry.add(entry);
                        } while(cursor.moveToNext());
                    }
                    /*results.values = mMatchEntry;
                    results.count = mMatchEntry.size();*/
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return results;
        }

        @Override
        protected void publishResults(final CharSequence constraint, FilterResults results) {
            mCurrentConstraint = constraint;
            updateEntries();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            final MatchEntry entry = (MatchEntry)resultValue;
            final String displayName = entry.getDisplayName();
            /*final String emailAddress = entry.getDestination();
            if (TextUtils.isEmpty(displayName) || TextUtils.equals(displayName, emailAddress)) {
                 return emailAddress;
            } else {
                return new Rfc822Token(displayName, emailAddress, null).toString();
            }*/
            return displayName;
        }
    }

   
    private final Context mContext;
    private final ContentResolver mContentResolver;
    private Account mAccount;
    protected final int mPreferredMaxResultCount;

    private int mRemainingDirectoryCount;

    protected CharSequence mCurrentConstraint;

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

    public void setAccount(Account account) {
        mAccount = account;
    }

    @Override
    public Filter getFilter() {
        return new DefaultFilter();
    }

    protected void updateEntries() {
        notifyDataSetChanged();
    }

    protected List<MatchEntry> getEntries() {
        return mMatchEntry;
    }

    public static final Uri MST_CONTENT_FILTER_URI = Uri.withAppendedPath(
        Phone.CONTENT_URI, "mstfilter");
    private String[] mst_project = new String[] {
        Phone.NUMBER,                                   // 0
        Contacts.DISPLAY_NAME,                          // 1
        //Phone.TYPE,                                     // 2
        //Phone.LABEL,                                    // 3
        Phone.CONTACT_ID,                               // 4
        Phone._ID,                                      // 5
        Contacts.LOOKUP_KEY,                            // 6
        //ContactsContract.CommonDataKinds.Email.MIMETYPE // 7
    };

    private Cursor doQuery(CharSequence constraint, int limit) {
        mMatchEntry.clear();
        final Uri.Builder builder = MST_CONTENT_FILTER_URI.buildUpon()
                .appendPath(constraint.toString())
                .appendQueryParameter(ContactsContract.LIMIT_PARAM_KEY,
                        String.valueOf(limit + ALLOWANCE_FOR_DUPLICATES));
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
                    + ", num_of_results: "
                    + (cursor != null ? cursor.getCount() : "null") + "): "
                    + (end - start) + " ms");
        }
        return cursor;
    }

    @Override
    public int getCount() {
        final List<MatchEntry> entries = getEntries();
        return entries != null ? entries.size() : 0;
    }

    @Override
    public MatchEntry getItem(int position) {
        return getEntries().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final MatchEntry entry = getEntries().get(position);
        CharSequence[] styledResults =
            getStyledResults(mCurrentConstraint.toString(), entry.getDisplayName(), entry.getNumber());
        CharSequence displayName = styledResults[0];
        CharSequence number = styledResults[1];
        final String constraint = mCurrentConstraint == null ? null :
                mCurrentConstraint.toString();
        ViewHolder holder = null;
        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.recipients_editor_popupwindow, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        holder = (ViewHolder)convertView.getTag();
        bindTextToView(displayName, holder.displayNameView);
        bindTextToView(number, holder.destinationView);
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

    protected CharSequence[] getStyledResults(@Nullable String constraint, String... results) {
        if (isAllWhitespace(constraint)) {
            return results;
        }

        CharSequence[] styledResults = new CharSequence[results.length];
        boolean foundMatch = false;
        for (int i = 0; i < results.length; i++) {
            String result = results[i];
            if (result == null) {
                continue;
            }

            if (!foundMatch) {
                int index = result.toLowerCase().indexOf(constraint.toLowerCase());
                if (index != -1) {
                    SpannableStringBuilder styled = SpannableStringBuilder.valueOf(result);
                    ForegroundColorSpan highlightSpan =
                            new ForegroundColorSpan(mContext.getResources().getColor(
                                    R.color.recipients_edit_hightlight));
                    styled.setSpan(highlightSpan,
                            index, index + constraint.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    styledResults[i] = styled;
                    foundMatch = true;
                    continue;
                }
            }
            styledResults[i] = result;
        }
        return styledResults;
    }

    private static boolean isAllWhitespace(@Nullable String string) {
        if (TextUtils.isEmpty(string)) {
            return true;
        }

        for (int i = 0; i < string.length(); ++i) {
            if (!Character.isWhitespace(string.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    protected class ViewHolder {
        public final TextView displayNameView;
        public final TextView destinationView;

        public ViewHolder(View view) {
            displayNameView = (TextView) view.findViewById(R.id.recipients_dropdown_title);
            destinationView = (TextView) view.findViewById(R.id.recipients_dropdown_subtitle);
        }
    }
}
