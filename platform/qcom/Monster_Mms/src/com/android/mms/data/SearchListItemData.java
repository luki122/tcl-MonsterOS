package com.android.mms.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import mst.provider.Telephony.Mms;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.mms.LogTag;
import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.ui.MessageUtils;


public class SearchListItemData {

    private static final String TAG = LogTag.TAG + "/SearchListItemData";
    private static final boolean DEBUG = true;

    //public static final int DATA_TYPE_TOTAL_COUNT = 3;
    public static final int DATA_TYPE_UNKNOWN = -1;
    public static final int DATA_TYPE_HEADER = 0;
    public static final int DATA_TYPE_THREAD = 1;
    public static final int DATA_TYPE_MESSAGE = 2;

    private long mRowId;
    private long mThreadId;
    private String mTitleValue;
    private String mSubTitleValue;
    private String mDateValue;
    private int mSubId;
    private int mDataType;
    private String mSearchString;
    public static final int CACHE_SIZE = 1000;
    public static HashMap<String, List<SearchListItemData>> mSearchDatasCache =
            new HashMap<String, List<SearchListItemData>>(CACHE_SIZE);

    public SearchListItemData(long rowId, long threadId, String titleValue, String subTitleValue,
                              String dateValue, int subId, int dataType, String searchString) {
        mRowId = rowId;
        mThreadId = threadId;
        mTitleValue = titleValue;
        mSubTitleValue = subTitleValue;
        mDateValue = dateValue;
        mSubId = subId;
        mDataType = dataType;
        mSearchString = searchString;
    }

    public String getSearchString() {
        return mSearchString;
    }

    public void setSearchString(String searchString) {
        this.mSearchString = searchString;
    }

    public int getDataType() {
        return mDataType;
    }

    public void setDataType(int dataType) {
        this.mDataType = mDataType;
    }

    public String getTitleValue() {
        return mTitleValue;
    }

    public void setTitleValue(String titleValue) {
        this.mTitleValue = mTitleValue;
    }

    public String getSubTitleValue() {
        return mSubTitleValue;
    }

    public void setSubTitleValue(String subTitleValue) {
        this.mSubTitleValue = mSubTitleValue;
    }

    public String getDateValue() {
        return mDateValue;
    }

    public void setDateValue(String dateValue) {
        this.mDateValue = mDateValue;
    }

    public int getSubId() {
        return mSubId;
    }

    public void setSubId(int subId) {
        this.mSubId = subId;
    }

    public long getRowId() {
        return mRowId;
    }

    public void setRowId(long rowId) {
        this.mRowId = rowId;
    }

    public long getThreadId() {
        return mThreadId;
    }

    public void setThreadId(long threadId) {
        this.mThreadId = threadId;
    }

    @Override
    public java.lang.String toString() {
        return "SearchListItemData{" +
                "mSearchString=" + mSearchString +
                ", mDataType=" + mDataType +
                ", mTitleValue=" + mTitleValue +
                ", mSubTitleValue=" + mSubTitleValue +
                ", mDateValue=" + mDateValue +
                ", mSubId=" + mSubId +
                "}\n\n";
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;

        SearchListItemData that = (SearchListItemData) object;

        if (mRowId != that.mRowId) return false;
        if (mThreadId != that.mThreadId) return false;
        if (mSubId != that.mSubId) return false;
        if (mDataType != that.mDataType) return false;
        if (!mTitleValue.equals(that.mTitleValue)) return false;
        if (!mSubTitleValue.equals(that.mSubTitleValue)) return false;
        if (!mDateValue.equals(that.mDateValue)) return false;
        if (!mSearchString.equals(that.mSearchString)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (mRowId ^ (mRowId >>> 32));
        result = 31 * result + (int) (mThreadId ^ (mThreadId >>> 32));
        result = 31 * result + mTitleValue.hashCode();
        result = 31 * result + mSubTitleValue.hashCode();
        result = 31 * result + mDateValue.hashCode();
        result = 31 * result + mSubId;
        result = 31 * result + mDataType;
        result = 31 * result + mSearchString.hashCode();
        return result;
    }
}
