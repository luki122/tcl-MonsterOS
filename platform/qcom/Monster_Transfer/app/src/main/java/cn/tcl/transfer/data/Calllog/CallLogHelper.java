package cn.tcl.transfer.data.Calllog;


import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.tcl.transfer.systemApp.DialerSysApp;

public class CallLogHelper {
    Context mContext;
    ContentResolver mContenResolver;
    Map<String, String> mPartDataNames;
    public static final String TAG = "CallLogHelper";
    private static final Uri CALLLOG_DATA_URI = Uri.parse("content://call_log/calls");

    public static String[] CALLLOG_PROJECTION = new String[]{
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE,
            CallLog.Calls.NEW,
            CallLog.Calls.PHONE_ACCOUNT_ID,
    };

    public CallLogHelper(Context context) {
        mContext = context;
        mContenResolver = mContext.getContentResolver();
        mPartDataNames = new HashMap<>();
    }

    public void backupCallLog(String dest) {
        if (TextUtils.isEmpty(dest)) {
            return;
        }
        Cursor cursor = null;
        ArrayList<CallLogModel> callLogList = new ArrayList<>();
        DialerSysApp.localCount = 0;
        try {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            cursor = mContenResolver.query(CALLLOG_DATA_URI, CALLLOG_PROJECTION, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                Log.i(TAG, "backupCallLog: localCount = " + cursor.getCount());

                if (cursor.moveToFirst()) {
                    do {
                        Log.i(TAG, "backupCallLog: number = " + cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)));
                        CallLogModel model = new CallLogModel();
                        setModelFromCursor(model, cursor);
                        callLogList.add(model);
                    } while (cursor.moveToNext());

                    ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(dest));
                    os.writeObject(callLogList);
                    os.close();
                    DialerSysApp.localCount = callLogList.size();
                }
            } else {
                Log.e(TAG, "backupCallLog: cursor is null or localCount < 1");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void setModelFromCursor(CallLogModel model, Cursor cursor) {
        model.mCallType = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
        model.mDate = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
        model.mNumber = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
        model.mDuration = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION));
        model.mNew = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.NEW));
        model.mAccountid = cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID));
    }

    public int getCallLogCount(String source) {

        if (TextUtils.isEmpty(source)) {
            return 0;
        }
        File file = new File(source);
        if (!file.exists() || file.length() == 0) {
            return 0;
        }

        try {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(source));
            ArrayList<CallLogModel> callLogList = (ArrayList<CallLogModel>) is.readObject();
            Log.i(TAG, "restoreCallLog: callLogList.localCount = " + callLogList.size());
            return callLogList.size();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


    public void restoreCallLog(String source) {
        DialerSysApp.inertCount = 0;
        if (TextUtils.isEmpty(source)) {
            return;
        }
        File file = new File(source);
        if (!file.exists() || file.length() == 0) {
            return;
        }

        try {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(source));
            ArrayList<CallLogModel> callLogList = (ArrayList<CallLogModel>) is.readObject();
            Log.i(TAG, "restoreCallLog: callLogList.localCount = " + callLogList.size());
            for (CallLogModel model : callLogList) {
                Log.i(TAG, "restoreCallLog: number = " + model.mNumber);
                saveCallLogToDB(model);
                DialerSysApp.inertCount++;
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    private boolean saveCallLogToDB(CallLogModel model) {
        if (model == null) {
            Log.e(TAG, "Attempted to save null model.");
            return false;
        }

        ContentValues values = getContentValuesFromCallLogModel(model);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        Uri newUri = mContenResolver.insert(CALLLOG_DATA_URI, values);
        Log.i(TAG, "saveCallLogToDB: newUri = " + newUri);

        return true;
    }

    private ContentValues getContentValuesFromCallLogModel(CallLogModel model) {
        ContentValues values = new ContentValues();

        values.put(CallLog.Calls.NUMBER, model.mNumber);
        values.put(CallLog.Calls.DATE, model.mDate);
        values.put(CallLog.Calls.TYPE, model.mCallType);
        values.put(CallLog.Calls.DURATION, model.mDuration);
        values.put(CallLog.Calls.NEW, model.mNew);
        values.put(CallLog.Calls.PHONE_ACCOUNT_ID, model.mAccountid);

        return values;
    }
}
