/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.data.sms;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony.Sms;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.tcl.transfer.systemApp.MmsSysApp;

public class SmsHelper {

    public static final String TAG = "SmsHelper";

    public static String[] SMS_PROJECTION = new String[] {
            Sms.ADDRESS,
            Sms.PERSON,
            Sms.DATE,
            Sms.DATE_SENT,
            Sms.PROTOCOL,
            Sms.READ,
            Sms.STATUS,
            Sms.TYPE,
            Sms.REPLY_PATH_PRESENT,
            Sms.SUBJECT,
            Sms.BODY,
            Sms.SERVICE_CENTER,
            Sms.LOCKED,
            Sms.ERROR_CODE,
            Sms.SEEN
    };

    public static String SMS_WHERE = Sms.ADDRESS + "=?";

    public static Uri MMS_CONTENT_URI = Uri.parse("content://mms");
    public static Uri MMS_PART_CONTENT_URI = Uri.parse("content://mms/part");
    public static Uri MMS_ADDR_CONTENT_URI = Uri.parse("content://mms/addr");

    Context mContext;
    ContentResolver mContenResolver;
    Map<String, String> mPartDataNames;

    public SmsHelper(Context context) {
        mContext = context;
        mContenResolver = mContext.getContentResolver();
        mPartDataNames = new HashMap<>();
    }

    public void backupSms(String dest) {
        if (TextUtils.isEmpty(dest)) {
            return;
        }
        Cursor cursor = null;
        ArrayList<SmsModel> smsList = new ArrayList<>();
        MmsSysApp.smsCount = 0;
        try {
            cursor = mContenResolver.query(Sms.CONTENT_URI, SMS_PROJECTION, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                Log.i(TAG, "backupSms: localCount = " + cursor.getCount());

                if (cursor.moveToFirst()) {
                    do {
                        Log.i(TAG, "backupSms: address = " + cursor.getString(cursor.getColumnIndex(Sms.ADDRESS)));
                        SmsModel model = new SmsModel();
                        setModelFromCursor(model, cursor);
                        smsList.add(model);
                    } while (cursor.moveToNext());

                    ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(dest));
                    os.writeObject(smsList);
                    os.close();
                    MmsSysApp.smsCount = smsList.size();
                }
            } else {
                Log.e(TAG, "backupSms: cursor is null or localCount < 1");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void backupMms(String dest) {
        if (TextUtils.isEmpty(dest)) {
            return;
        }

        Cursor cursor = null;
        ArrayList<MmsModel> mmsList = new ArrayList<>();
        MmsSysApp.mmsCount = 0;
        try {
            cursor = mContenResolver.query(MMS_CONTENT_URI, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                Log.i(TAG, "backupMms: mms_count = " + cursor.getCount());
                if (cursor.moveToFirst()) {
                    do {
                        MmsModel model = new MmsModel();
                        setMmsModelFromCursor(model, cursor);
                        mmsList.add(model);
                    } while (cursor.moveToNext());

                    ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(dest));
                    os.writeObject(mmsList);
                    os.close();
                    MmsSysApp.mmsCount = mmsList.size();
                }
            } else {
                Log.e(TAG, "backupMms: cursor is null or localCount < 1");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void setModelFromCursor(SmsModel model, Cursor cursor) {
        model.mAddress = cursor.getString(cursor.getColumnIndex(Sms.ADDRESS));
        model.mDate = cursor.getLong(cursor.getColumnIndex(Sms.DATE));
        model.mDateSent = cursor.getLong(cursor.getColumnIndex(Sms.DATE_SENT));
        model.mProtocol = cursor.getInt(cursor.getColumnIndex(Sms.PROTOCOL));
        model.mRead = cursor.getInt(cursor.getColumnIndex(Sms.READ));
        model.mStatus = cursor.getInt(cursor.getColumnIndex(Sms.STATUS));
        model.mType = cursor.getInt(cursor.getColumnIndex(Sms.TYPE));
        model.mReply = cursor.getInt(cursor.getColumnIndex(Sms.REPLY_PATH_PRESENT));
        model.mSubject = cursor.getString(cursor.getColumnIndex(Sms.SUBJECT));
        model.mBody = cursor.getString(cursor.getColumnIndex(Sms.BODY));
        model.mServiceCenter = cursor.getString(cursor.getColumnIndex(Sms.SERVICE_CENTER));
        model.mLocked = cursor.getInt(cursor.getColumnIndex(Sms.LOCKED));
        model.mErrorCode = cursor.getInt(cursor.getColumnIndex(Sms.ERROR_CODE));
        model.mSeen = cursor.getInt(cursor.getColumnIndex(Sms.SEEN));
    }

    private void setMmsModelFromCursor(MmsModel model, Cursor cursor) {
        // set pdu data
        model._id = cursor.getInt(cursor.getColumnIndex("_id"));
        model.thread_id = cursor.getInt(cursor.getColumnIndex("thread_id"));
        model.date = cursor.getInt(cursor.getColumnIndex("date"));
        model.date_sent = cursor.getInt(cursor.getColumnIndex("date_sent"));
        model.msg_box = cursor.getInt(cursor.getColumnIndex("msg_box"));
        model.read = cursor.getInt(cursor.getColumnIndex("read"));
        model.m_id = cursor.getString(cursor.getColumnIndex("m_id"));
        model.sub = cursor.getString(cursor.getColumnIndex("sub"));
        model.sub_cs = cursor.getInt(cursor.getColumnIndex("sub_cs"));
        model.ct_t = cursor.getString(cursor.getColumnIndex("ct_t"));
        model.ct_l = cursor.getString(cursor.getColumnIndex("ct_l"));
        model.exp = cursor.getInt(cursor.getColumnIndex("exp"));
        model.m_cls = cursor.getString(cursor.getColumnIndex("m_cls"));
        model.m_type = cursor.getInt(cursor.getColumnIndex("m_type"));
        model.v = cursor.getInt(cursor.getColumnIndex("v"));
        model.m_size = cursor.getInt(cursor.getColumnIndex("m_size"));
        model.pri = cursor.getInt(cursor.getColumnIndex("pri"));
        model.rr = cursor.getInt(cursor.getColumnIndex("rr"));
        model.rpt_a = cursor.getInt(cursor.getColumnIndex("rpt_a"));
        model.resp_st = cursor.getInt(cursor.getColumnIndex("resp_st"));
        model.st = cursor.getInt(cursor.getColumnIndex("st"));
        //model.st_ext = cursor.getInt(cursor.getColumnIndex("st_ext"));
        model.tr_id = cursor.getString(cursor.getColumnIndex("tr_id"));
        model.retr_st = cursor.getInt(cursor.getColumnIndex("retr_st"));
        model.retr_txt = cursor.getString(cursor.getColumnIndex("retr_txt"));
        model.retr_txt_cs = cursor.getInt(cursor.getColumnIndex("retr_txt_cs"));
        model.read_status = cursor.getInt(cursor.getColumnIndex("read_status"));
        model.ct_cls = cursor.getInt(cursor.getColumnIndex("ct_cls"));
        model.resp_txt = cursor.getString(cursor.getColumnIndex("resp_txt"));
        model.d_tm = cursor.getInt(cursor.getColumnIndex("d_tm"));
        model.d_rpt = cursor.getInt(cursor.getColumnIndex("d_rpt"));
        model.locked = cursor.getInt(cursor.getColumnIndex("locked"));
        model.sub_id = cursor.getInt(cursor.getColumnIndex("sub_id"));
        //model.service_center = cursor.getString(cursor.getColumnIndex("service_center"));
        model.seen = cursor.getInt(cursor.getColumnIndex("seen"));
        model.text_only = cursor.getInt(cursor.getColumnIndex("text_only"));

        setMmsPart(model);
        setMmsAddr(model);
    }

    private void setMmsPart(MmsModel model) {
        // set part data
        Cursor mmsPartCurosr = null;
        Uri mmsPartUri = Uri.parse("content://mms/" + model._id + "/part");
        Log.i(TAG, "pduId = " + model._id);
        try {
            mmsPartCurosr = mContenResolver.query(mmsPartUri, null, null, null, null);
            if (mmsPartCurosr != null && mmsPartCurosr.getCount() > 0) {
                if (mmsPartCurosr.moveToFirst()) {
                    do {
                        MmsModel.MmsPart mmsPart = new MmsModel.MmsPart();
                        mmsPart.seq = mmsPartCurosr.getInt(mmsPartCurosr.getColumnIndex("seq"));
                        mmsPart.ct = mmsPartCurosr.getString(mmsPartCurosr.getColumnIndex("ct"));
                        mmsPart.name = mmsPartCurosr.getString(mmsPartCurosr.getColumnIndex("name"));
                        mmsPart.chset = mmsPartCurosr.getInt(mmsPartCurosr.getColumnIndex("chset"));
                        mmsPart.cd = mmsPartCurosr.getString(mmsPartCurosr.getColumnIndex("cd"));
                        mmsPart.fn = mmsPartCurosr.getString(mmsPartCurosr.getColumnIndex("fn"));
                        mmsPart.cid = mmsPartCurosr.getString(mmsPartCurosr.getColumnIndex("cid"));
                        mmsPart.cl = mmsPartCurosr.getString(mmsPartCurosr.getColumnIndex("cl"));
                        mmsPart.ctt_s = mmsPartCurosr.getInt(mmsPartCurosr.getColumnIndex("ctt_s"));
                        mmsPart.ctt_t = mmsPartCurosr.getString(mmsPartCurosr.getColumnIndex("ctt_t"));
                        mmsPart._data = mmsPartCurosr.getString(mmsPartCurosr.getColumnIndex("_data"));
                        mmsPart.text = mmsPartCurosr.getString(mmsPartCurosr.getColumnIndex("text"));

                        model.mmsPartsList.add(mmsPart);
                        Log.i(TAG, "mmsPart is:" + mmsPart.toString());
                    } while (mmsPartCurosr.moveToNext());
                }
            } else {
                Log.e(TAG, "setModelFromCursor: mmsPartCurosr is null or localCount < 1");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mmsPartCurosr != null) {
                mmsPartCurosr.close();
            }
        }
    }

    private void setMmsAddr(MmsModel model) {
        // set addr data
        Cursor mmsAddrCurosr = null;
        Uri mmsAddrUri = Uri.parse("content://mms/" + model._id + "/addr");
        try {
            mmsAddrCurosr = mContenResolver.query(mmsAddrUri, null, null, null, null);
            if (mmsAddrCurosr != null && mmsAddrCurosr.getCount() > 0) {
                if (mmsAddrCurosr.moveToFirst()) {
                    do {
                        MmsModel.MmsAddr mmsAddr = new MmsModel.MmsAddr();
                        mmsAddr.msg_id = mmsAddrCurosr.getInt(mmsAddrCurosr.getColumnIndex("msg_id"));
                        mmsAddr.address = mmsAddrCurosr.getString(mmsAddrCurosr.getColumnIndex("address"));
                        mmsAddr.type = mmsAddrCurosr.getInt(mmsAddrCurosr.getColumnIndex("type"));
                        mmsAddr.charset = mmsAddrCurosr.getInt(mmsAddrCurosr.getColumnIndex("charset"));

                        model.mmsAddrsList.add(mmsAddr);
                        Log.i(TAG, "mmsAddr is:" + mmsAddr.toString());
                    } while (mmsAddrCurosr.moveToNext());
                }
            } else {
                Log.e(TAG, "setModelFromCursor: mmsAddrCurosr is null or localCount < 1");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mmsAddrCurosr != null) {
                mmsAddrCurosr.close();
            }
        }
    }

    public int getSmsCount(String source) {
        if (TextUtils.isEmpty(source)) {
            return 0;
        }
        File file = new File(source);
        if (!file.exists() || file.length() == 0) {
            return 0;
        }

        try {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(source));
            ArrayList<SmsModel> smsList = (ArrayList<SmsModel>) is.readObject();
            Log.i(TAG, "restoreSms: smsList.localCount = " + smsList.size());
            return smsList.size();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void restoreSms(String source) {
        MmsSysApp.smsInertCount = 0;
        if (TextUtils.isEmpty(source)) {
            return;
        }
        File file = new File(source);
        if (!file.exists() || file.length() == 0) {
            return;
        }

        try {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(source));
            ArrayList<SmsModel> smsList = (ArrayList<SmsModel>) is.readObject();
            Log.i(TAG, "restoreSms: smsList.localCount = " + smsList.size());
            for(SmsModel model: smsList) {
                Log.i(TAG, "restoreSms: address = " + model.mAddress);
                Log.i(TAG, "restoreSms: body = " + model.mBody);
                saveSmsToDB(model);
                MmsSysApp.smsInertCount++;
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    private boolean saveSmsToDB(SmsModel model) {
        if (model == null) {
            Log.e(TAG, "Attempted to save null model.");
            return false;
        }

        /*if (mContext.checkSelfPermission(android.Manifest.permission.WRITE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "has no WRITE_SMS permission");
            return false;
        }*/
        ContentValues values = getContentValuesFromSmsModel(model);
        Uri newUri = mContenResolver.insert(Sms.CONTENT_URI, values);
        Log.i(TAG, "saveSmsToDB: newUri = " + newUri);

        return true;
    }

    public int getMmsCount(String source) {
        if (TextUtils.isEmpty(source)) {
            return 0;
        }
        File file = new File(source);
        if (!file.exists() || file.length() == 0) {
            return 0;
        }

        try {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(source));
            ArrayList<SmsModel> smsList = (ArrayList<SmsModel>) is.readObject();
            Log.i(TAG, "restoreSms: smsList.localCount = " + smsList.size());
            return smsList.size();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Map<String, String> restoreMms(String source) {
        MmsSysApp.mmsInertCount = 0;
        if (TextUtils.isEmpty(source)) {
            return null;
        }
        File file = new File(source);
        if (!file.exists() || file.length() == 0) {
            return null;
        }

        try {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(source));
            ArrayList<MmsModel> mmsList = (ArrayList<MmsModel>) is.readObject();
            Log.i(TAG, "restoreMms: mmsList.localCount = " + mmsList.size());
            for(MmsModel model: mmsList) {
                saveMmsToDB(model);
                MmsSysApp.mmsInertCount++;
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        renamePartDataName();
        return mPartDataNames;
    }

    private void saveMmsToDB(MmsModel model) {

        if (model == null) {
            Log.e(TAG, "saveMmsToDB: MmsModel is null");
            return;
        }

        String address = null;
        for (MmsModel.MmsAddr mmsAddr : model.mmsAddrsList) {
            if (hasDigit(mmsAddr.address)) {
                address = mmsAddr.address;
            }
        }

        long smsId;
        int threadId = 0;
        ContentValues values = new ContentValues();
        values.put(Sms.ADDRESS, address);
        Uri newSmsUri = mContenResolver.insert(Sms.CONTENT_URI, values);
        if (newSmsUri != null) {
            Log.i(TAG, "saveMmsToDB: newSmsUri = " + newSmsUri);
            Cursor cursor = mContenResolver.query(newSmsUri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    threadId = cursor.getInt(cursor.getColumnIndex(Sms.THREAD_ID));
                    smsId = cursor.getLong(cursor.getColumnIndex(Sms._ID));
                    Log.i(TAG, "saveMmsToDB: threadId = " + threadId + ", smsId = " + smsId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            if (threadId > 0) {
                model.thread_id = threadId;
                insertMmsData(model);
            }

            // delete the temporary sms from DB
            mContenResolver.delete(newSmsUri, null, null);
        }
    }

    private void insertMmsData(MmsModel model) {
        ContentValues values = new ContentValues();

        if (model.thread_id > 0) {
            values.put("thread_id", model.thread_id);
        }
        if (model.date > 0) {
            values.put("date", model.date);
        }
        if (model.date_sent > 0) {
            values.put("date_sent", model.date_sent);
        }
        values.put("msg_box", model.msg_box);
        values.put("read", model.read);
        if (model.m_id != null) {
            values.put("m_id", model.m_id);
        }
        if (model.sub != null) {
            values.put("sub", model.sub);
        }
        if (model.sub_cs > 0) {
            values.put("sub_cs", model.sub_cs);
        }
        if (model.ct_t != null) {
            values.put("ct_t", model.ct_t);
        }
        if (model.ct_l != null) {
            values.put("ct_l", model.ct_l);
        }
        if (model.exp > 0) {
            values.put("exp", model.exp);
        }
        if (model.m_cls != null) {
            values.put("m_cls", model.m_cls);
        }
        if (model.m_type > 0) {
            values.put("m_type", model.m_type);
        }
        if (model.v > 0) {
            values.put("v", model.v);
        }
        if (model.m_size > 0) {
            values.put("m_size", model.m_size);
        }
        if (model.pri > 0) {
            values.put("pri", model.pri);
        }
        if (model.rr > 0) {
            values.put("rr", model.rr);
        }
        if (model.rpt_a > 0) {
            values.put("rpt_a", model.rpt_a);
        }
        if (model.resp_st > 0) {
            values.put("resp_st", model.resp_st);
        }
        if (model.st > 0) {
            values.put("st", model.st);
        }
//        if (model.st_ext > 0) {
//            values.put("st_ext", model.st_ext);
//        }
        if (model.tr_id != null) {
            values.put("tr_id", model.tr_id);
        }
        if (model.retr_st > 0) {
            values.put("retr_st", model.retr_st);
        }
        if (model.retr_txt != null) {
            values.put("retr_txt", model.retr_txt);
        }
        if (model.retr_txt_cs > 0) {
            values.put("retr_txt_cs", model.retr_txt_cs);
        }
        if (model.read_status > 0) {
            values.put("read_status", model.read_status);
        }
        if (model.ct_cls > 0) {
            values.put("ct_cls", model.ct_cls);
        }
        if (model.resp_txt != null) {
            values.put("resp_txt", model.resp_txt);
        }
        if (model.d_tm > 0) {
            values.put("d_tm", model.d_tm);
        }
        if (model.d_rpt > 0) {
            values.put("d_rpt", model.d_rpt);
        }
        values.put("locked", model.locked);
        if (model.sub_id > 0) {
            values.put("sub_id", model.sub_id);
        }
//        if (model.service_center != null) {
//            values.put("service_center", model.service_center);
//        }
        values.put("seen", model.seen);
        values.put("text_only", model.text_only);

        Uri newPduUri = mContenResolver.insert(MMS_CONTENT_URI, values);
        if (newPduUri != null) {
            Log.i(TAG, "insertPduData: pduUri = " + newPduUri);
            model._id = Long.parseLong(newPduUri.getPathSegments().get(0));

            insertPartData(model);
            insertAddrData(model);
        }

    }

    private void insertPartData(MmsModel model) {
        Log.i(TAG, "insertPartData: mmsPartsList.size = " + model.mmsPartsList.size());
        for (MmsModel.MmsPart mmsPart : model.mmsPartsList) {
            ContentValues values = new ContentValues();

            if (mmsPart.mid > 0) {
                values.put("mid", mmsPart.mid);
            }
            values.put("seq", mmsPart.seq);
            if (mmsPart.ct != null) {
                values.put("ct", mmsPart.ct);
            }
            if (mmsPart.name != null) {
                values.put("name", mmsPart.name);
            }
            if (mmsPart.chset > 0) {
                values.put("chset", mmsPart.chset);
            }
            if (mmsPart.cd != null) {
                values.put("cd", mmsPart.cd);
            }
            if (mmsPart.fn != null) {
                values.put("fn", mmsPart.fn);
            }
            if (mmsPart.cid != null) {
                values.put("cid", mmsPart.cid);
            }
            if (mmsPart.cl != null) {
                values.put("cl", mmsPart.cl);
            }
            if (mmsPart.ctt_s > 0) {
                values.put("ctt_s", mmsPart.ctt_s);
            }
            if (mmsPart.ctt_t != null) {
                values.put("ctt_t", mmsPart.ctt_t);
            }
            if (mmsPart.text != null) {
                values.put("text", mmsPart.text);
            }

            Uri partUri = Uri.parse("content://mms/" + model._id + "/part");
            Uri newPartUri = mContenResolver.insert(partUri, values);

            if (newPartUri != null) {
                Log.i(TAG, "insertPartData: newPartUri = " + newPartUri);
                //values.clear();
                //values.put("_data", mmsPart._data);
                //mContenResolver.update(newPartUri, values, null, null);
                Cursor partCursor = null;
                try {
                    partCursor = mContenResolver.query(newPartUri, null, null, null, null);
                    if (partCursor != null && partCursor.moveToFirst()) {
                        String _data = partCursor.getString(partCursor.getColumnIndex("_data"));
                        if (mmsPart._data != null && _data != null) {
                            Log.i(TAG, "insertPartData: mmsPart._data = " + mmsPart._data);
                            Log.i(TAG, "insertPartData: _data = " + _data);
                            mPartDataNames.put(mmsPart._data, _data);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (partCursor != null) {
                        partCursor.close();
                    }
                }
            }
        }
    }

    private void insertAddrData(MmsModel model) {
        Log.i(TAG, "insertAddrData: mmsAddrsList.size = " + model.mmsAddrsList.size());
        for (MmsModel.MmsAddr mmsAddr : model.mmsAddrsList) {
            ContentValues values = new ContentValues();

            if (model._id > 0) {
                values.put("msg_id", model._id);
            }
            if (mmsAddr.address != null) {
                values.put("address", mmsAddr.address);
            }
            if (mmsAddr.type > 0) {
                values.put("type", mmsAddr.type);
            }
            if (mmsAddr.charset > 0) {
                values.put("charset", mmsAddr.charset);
            }

            Uri addrUri = Uri.parse("content://mms/" + model._id + "/addr");
            Uri newAddrUri = mContenResolver.insert(addrUri, values);
            if (newAddrUri != null) {
                Log.i(TAG, "insertAddrData: newAddrUri = " + newAddrUri);
            }
        }
    }

    private ContentValues getContentValuesFromSmsModel(SmsModel model) {
        ContentValues values = new ContentValues();

        values.put(Sms.ADDRESS, model.mAddress);
        values.put(Sms.DATE, model.mDate);
        values.put(Sms.DATE_SENT, model.mDateSent);
        values.put(Sms.PROTOCOL, model.mProtocol);
        values.put(Sms.READ, model.mRead);
        values.put(Sms.STATUS, model.mStatus);
        values.put(Sms.TYPE, model.mType);
        values.put(Sms.REPLY_PATH_PRESENT, model.mReply);
        values.put(Sms.SUBJECT, model.mSubject);
        values.put(Sms.BODY, model.mBody);
        values.put(Sms.SERVICE_CENTER, model.mServiceCenter);
        values.put(Sms.LOCKED, model.mLocked);
        values.put(Sms.ERROR_CODE, model.mErrorCode);
        values.put(Sms.SEEN, model.mSeen);

        return values;
    }

    private boolean hasDigit(String content) {
        boolean flag = false;
        Pattern p = Pattern.compile(".*\\d+.*");
        Matcher m = p.matcher(content);
        if (m.matches()) {
            flag = true;
        }
        return flag;
    }

    public void queryPart() {
        Cursor cursor = null;
        String[] projection = new String[]{"_data"};
        String selection = "_data is not null";
        try {
            cursor = mContenResolver.query(MMS_PART_CONTENT_URI, projection, selection, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String _data = cursor.getString(0);
                    Log.i(TAG, "queryPart: _data = " + _data);
                } while (cursor.moveToNext());
            } else {
                Log.e(TAG, "queryPart: cursor is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void renamePartDataName() {
        if (mPartDataNames != null && mPartDataNames.size() > 0) {
            for (Map.Entry<String, String> entry : mPartDataNames.entrySet()) {
                String oldDataPath = entry.getKey();
                String updatedDataPath = entry.getValue();

                try {
                    if (oldDataPath != null && updatedDataPath != null) {
                        Log.i(TAG, "renamePartDataName: oldDataPath = " + oldDataPath);
                        Log.i(TAG, "renamePartDataName: updatedDataPath = " + updatedDataPath);
                        File oldFile = new File(oldDataPath);
                        File updatedFile = new File(updatedDataPath);

                        if (oldFile.exists() && updatedFile.exists()) {
                            updatedFile.delete();
                            oldFile.renameTo(new File(updatedDataPath));
                        }
                        if (oldFile.exists() && !updatedFile.exists()) {
                            oldFile.renameTo(new File(updatedDataPath));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.i(TAG, "renamePartDataName: map is null or size < 1");
        }
    }
}
