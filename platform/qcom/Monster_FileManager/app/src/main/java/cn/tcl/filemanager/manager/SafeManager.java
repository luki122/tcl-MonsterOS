/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.manager;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import cn.tcl.filemanager.R;
import cn.tcl.filemanager.activity.FileSafeBrowserActivity;
import cn.tcl.filemanager.service.FileManagerService;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.utils.SafeInfo;
import cn.tcl.filemanager.utils.SafeUtils;
import cn.tcl.filemanager.utils.SharedPreferenceUtils;

/**
 * Created by user on 16-3-3.
 */
public class SafeManager {

    public static final String OPEN_ACCESS_SAFEBOX = "filemanager.open.access.safebox"; // MODIFIED by wenjing.ni, 2016-04-20,BUG-1967152
    public static final String PRIVATE_KEY = "%lphago3";
    public static final int DESTORY_RECOVER_MODE = 0;
    public static final int DESTORY_DELETE_MODE = 1;
    public static final int DESTORY_SHIFT_OUT_MODE = 2;
    public static final int NORMAL_DELETE_MODE = 0;
    public static final int SAFE_DELETE_MODE = 1;
    public static final int DELETE_ALBUM_MODE = 3;
    public static final int SAFE_DESTORY_DELETE_MODE = 2;
    private static final String TAG = SafeManager.class.getSimpleName(); // MODIFIED by haifeng.tang, 2016-04-23,BUG-1989942

    public static int DEL_PRIVATE = 100;
    public static int ADD_PRIVATE = 200;

    public static final int FILE_NORMAL = 0;

    public static final int FILE_MOVE_IN = 1;

//    public static final int FILE_MOVE_OUT =2;

    public static int mCurrentmode = 0;

    public static boolean notQuitSafe = false;
    public static final int ENETER_SAFE_SETTINGS_REQUEST_CODE = 6;
    public static final int REMOVE_SAFE_PATH_SELECT_REQUEST_CODE = 5;
    public static boolean mInFingerprintLockout;


    public static SafeInfo getSafeInfo(Context context, String dbPath, int storageID) {
        SafeInfo info = new SafeInfo();
        info.setSafe_path(dbPath);
        Log.d("PATH", "this is enter FilePath " + dbPath);
        String[] columns = new String[]{PrivateHelper.USER_FIELD_CT, PrivateHelper.USER_FIELD_AL}; // MODIFIED by haifeng.tang, 2016-04-26,BUG-1989911
        Cursor cursor = null;
        SQLiteDatabase db = null;
        try {
            /* MODIFIED-BEGIN by haifeng.tang, 2016-04-23,BUG-1989942*/
            PrivateHelper mPrivateHelper = new PrivateHelper(context, dbPath + File.separator);
            db = mPrivateHelper.getWritableDatabase();
            cursor = db.query(PrivateHelper.USER_TABLE_NAME, columns, null, null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                info.setSafe_ct(cursor.getLong(0));
                /* MODIFIED-BEGIN by haifeng.tang, 2016-04-26,BUG-1989911*/
                String name = cursor.getString(1);
                if (TextUtils.isEmpty(name)){
                    name=Build.MODEL; // MODIFIED by wenjing.ni, 2016-05-03,BUG-802835
                }
                info.setSafe_name(name);
                /* MODIFIED-END by haifeng.tang,BUG-1989911*/
                Log.d("PATH", "this is enter FilePath " + dbPath + "cursor.get" + cursor.getString(1));
                /* MODIFIED-END by haifeng.tang,BUG-1989942*/
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        if (storageID == SafeUtils.PHONE_STORAGE_SAFE) {
            info.setSafe_info(context.getString(R.string.phone_storage_cn));
        } else if (storageID == SafeUtils.SD_STORAGE_SAFE) {
            info.setSafe_info(context.getString(R.string.sd_card));
        } else if (storageID == SafeUtils.EXTERNAL_STORAGE_SAFE) {
            info.setSafe_info(context.getString(R.string.usbotg_m)); // MODIFIED by songlin.qi, 2016-06-08,BUG-2278011
        }

        return info;
    }


    public static String getRawKey(long createTime) {

        String baseKey = String.valueOf(createTime);
        String key = baseKey;
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(key.getBytes());
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(64, sr);
            SecretKey secretKey = keyGenerator.generateKey();
            String realKey = new String(Base64.encode(secretKey.getEncoded(), Base64.DEFAULT));
            return realKey;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "safeSAFE";

    }


    /**
     * read publicKey from current database
     *
     * @param context
     * @return private key;
     */

    public static String readKey(Context context) {

        String currentSafeRoot = SharedPreferenceUtils.getCurrentSafeRoot(context);
        String currentSafeName = SharedPreferenceUtils.getCurrentSafeName(context);
        Cursor cursor = null;
        String key = null;
        /*MODIFIED-BEGIN by wenjing.ni, 2016-04-13,BUG-1940959*/
        SQLiteDatabase db = null; // MODIFIED by haifeng.tang, 2016-04-23,BUG-1989942
        try {
            PrivateHelper privateHelper = new PrivateHelper(context, currentSafeRoot + File.separator + currentSafeName + File.separator);
            db = privateHelper.getWritableDatabase();
            /*MODIFIED-END by wenjing.ni,BUG-1940959*/
            cursor = db.query(PrivateHelper.USER_TABLE_NAME, new String[]{PrivateHelper.USER_FIELD_WT1}, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                key = cursor.getString(0);
                return key;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }


        }

        return key;


    }

    /**
     * @param context
     * @return make up publicKey {@link #readKey(Context)}with privateKey{@link SafeManager#PRIVATE_KEY}
     */


    public static byte[] getKey(Context context) {

        List<Byte> keyList = new ArrayList<>();

        byte[] publicKey = Base64.decode(readKey(context).getBytes(), Base64.DEFAULT);
        byte[] privateKey = PRIVATE_KEY.getBytes();

        for (int i = 0; i < publicKey.length; i++) {
            keyList.add(publicKey[i]);
        }

        for (int i = 0; i < privateKey.length; i++) {
            keyList.add(privateKey[i]);
        }

        byte[] keyArray = new byte[keyList.size()];

        for (int i = 0; i < privateKey.length; i++) {
            keyArray[i] = keyList.get(i);
        }
        return keyArray;


    }


    /* MODIFIED-BEGIN by haifeng.tang, 2016-04-23,BUG-1989942*/
    public static void insertSafe(Context context, String name, String dbPath, long createTime, String password, String answser, int qd,MountManager mMountPointManager,boolean relateFingerPrint,boolean isFirstSafeBox) { // MODIFIED by wenjing.ni, 2016-05-04,BUG-802835
        Log.d("RELATE","this is relate value"+relateFingerPrint);
        SQLiteDatabase db = null;
        try {

            SafeInfo info = new SafeInfo();
            PrivateHelper mPrivateHelper = new PrivateHelper(context, dbPath);
            db = mPrivateHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(PrivateHelper.USER_FIELD_WT, 0);
            values.put(PrivateHelper.USER_FIELD_WT1, getRawKey(createTime));
            values.put(PrivateHelper.USER_FIELD_WT2, password);
            values.put(PrivateHelper.USER_FIELD_WT3, answser);
            values.put(PrivateHelper.USER_FIELD_QD, qd);
            values.put(PrivateHelper.USER_FIELD_ST, ""); // MODIFIED by haifeng.tang, 2016-04-23,BUG-1989942
            values.put(PrivateHelper.USER_FIELD_CT, createTime);
            values.put(PrivateHelper.USER_FIELD_CP, Build.MODEL); // MODIFIED by wenjing.ni, 2016-05-03,BUG-802835
            values.put(PrivateHelper.USER_FIELD_CD, "cd");
            values.put(PrivateHelper.USER_FIELD_OP, 0);
            values.put(PrivateHelper.USER_FIELD_AL, name);
            values.put(PrivateHelper.USER_FIELD_UT, createTime);
            db.insert(PrivateHelper.USER_TABLE_NAME, null, values);
            SharedPreferenceUtils.setCurrentSafeName(context, new File(dbPath).getName());
            SharedPreferenceUtils.setCurrentSafeRoot(context, new File(dbPath).getParent());
            if(!relateFingerPrint) {
                Intent intent = new Intent(context, FileSafeBrowserActivity.class);
                intent.putExtra("currentsafepath", new File(dbPath).getName());
                intent.putExtra("currentsaferootpath", new File(dbPath).getParent());
                intent.putExtra("FirstSafebox", isFirstSafeBox); // MODIFIED by wenjing.ni, 2016-05-13,BUG-2003636
                context.startActivity(intent);
            }
        } catch (SQLiteCantOpenDatabaseException e) {
            e.printStackTrace(); // MODIFIED by haifeng.tang, 2016-04-23,BUG-1989942
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }


    }


    /* MODIFIED-BEGIN by haifeng.tang, 2016-04-23,BUG-1989942*/
    public static String getSafeBoxName(Context context, String dbPath) {
        /*MODIFIED-BEGIN by wenjing.ni, 2016-04-13,BUG-1940959*/
        SQLiteDatabase db = null;
        String SafeBoxName = "";
        String question = null;
        Cursor cursor = null;
        try {
            PrivateHelper mPrivateHelper = new PrivateHelper(context, dbPath);
            Log.d("QUES", "this is sql path" + dbPath);
            db = mPrivateHelper.getWritableDatabase();
            /*MODIFIED-END by wenjing.ni,BUG-1940959*/
            cursor = db.query(PrivateHelper.USER_TABLE_NAME, new String[]{PrivateHelper.USER_FIELD_AL}, null, null, null, null, null); // MODIFIED by haifeng.tang, 2016-04-26,BUG-1989911
            if (cursor != null && cursor.moveToFirst()) {
                SafeBoxName = cursor.getString(0);
            }
            return SafeBoxName;

        } catch (Exception e) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return SafeBoxName; // MODIFIED by wenjing.ni, 2016-05-04,BUG-802835

    }


    public static String queryQuestion(Context context, String dbPath) {
        /*MODIFIED-BEGIN by wenjing.ni, 2016-04-13,BUG-1940959*/
        SQLiteDatabase db = null;
        int questionIndex = 0;
        String question = null;
        Cursor cursor = null;
        /* MODIFIED-END by haifeng.tang,BUG-1989942*/
        try {
            PrivateHelper mPrivateHelper = new PrivateHelper(context, dbPath);
            Log.d("QUES", "this is sql path" + dbPath);
            db = mPrivateHelper.getWritableDatabase();
            /*MODIFIED-END by wenjing.ni,BUG-1940959*/
            cursor = db.query(PrivateHelper.USER_TABLE_NAME, new String[]{PrivateHelper.USER_FIELD_QD}, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                questionIndex = cursor.getInt(0);
            }
            question = context.getResources().getStringArray(R.array.safe_question)[questionIndex];

        /* MODIFIED-BEGIN by haifeng.tang, 2016-04-23,BUG-1989942*/
        } catch (Exception e) {

        } finally {
        /* MODIFIED-END by haifeng.tang,BUG-1989942*/
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        /* MODIFIED-BEGIN by haifeng.tang, 2016-04-23,BUG-1989942*/
        return question;

    }

    public static boolean destorySafe(Context context, String dbPath, String mEditPassword) {
        String password = null;
        Cursor cursor = null;
        /*MODIFIED-BEGIN by wenjing.ni, 2016-04-13,BUG-1940959*/
        SQLiteDatabase db = null;
        /* MODIFIED-END by haifeng.tang,BUG-1989942*/
        try {
            PrivateHelper mPrivateHelper = new PrivateHelper(context, dbPath);
            Log.d("QUES", "this is sql path" + dbPath);
            db = mPrivateHelper.getWritableDatabase();
            /*MODIFIED-END by wenjing.ni,BUG-1940959*/
            cursor = db.query(PrivateHelper.USER_TABLE_NAME, new String[]{PrivateHelper.USER_FIELD_WT2}, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                password = cursor.getString(0);
            }
            /* MODIFIED-BEGIN by haifeng.tang, 2016-04-23,BUG-1989942*/
            if (mEditPassword.equals(password)) {
                File safeFile = new File(dbPath);
                safeFile.delete();
                return true;
            }
        } catch (Exception e) {

        } finally {
        /* MODIFIED-END by haifeng.tang,BUG-1989942*/
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return false;
    }

    /* MODIFIED-BEGIN by haifeng.tang, 2016-04-23,BUG-1989942*/
    public static boolean isAuthorizationAnswer(Context context, String dbPath, String mEditAnswer) {
        String answer = null;
        Cursor cursor = null;
        /*MODIFIED-BEGIN by wenjing.ni, 2016-04-13,BUG-1940959*/
        SQLiteDatabase db = null;
        /* MODIFIED-END by haifeng.tang,BUG-1989942*/
        try {
            PrivateHelper mPrivateHelper = new PrivateHelper(context, dbPath);
            Log.d("QUES", "this is sql path" + dbPath);
            db = mPrivateHelper.getWritableDatabase();
            /*MODIFIED-END by wenjing.ni,BUG-1940959*/
            cursor = db.query(PrivateHelper.USER_TABLE_NAME, new String[]{PrivateHelper.USER_FIELD_WT3}, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                answer = cursor.getString(0);
            }
            /* MODIFIED-BEGIN by haifeng.tang, 2016-04-23,BUG-1989942*/
            if (answer.equals(mEditAnswer)) {
                return true;
            }

        } catch (Exception e) {

        } finally {
        /* MODIFIED-END by haifeng.tang,BUG-1989942*/
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return false;
    }

    /* MODIFIED-BEGIN by haifeng.tang, 2016-04-23,BUG-1989942*/
    public static boolean isAuthorizationOriginalPassword(Context context, String dbPath, String mEditOriginal, String mEditFirst) {
        /*MODIFIED-BEGIN by wenjing.ni, 2016-04-13,BUG-1940959*/
        String currentSafeRoot = SharedPreferenceUtils.getCurrentSafeRoot(context);
        String currentSafeName = SharedPreferenceUtils.getCurrentSafeName(context);
        String original = null;
        Cursor cursor = null;
        String mSafePath = null;
        SQLiteDatabase db = null;
        try {
            if (currentSafeRoot == null || currentSafeRoot.equals("")) {
                mSafePath = dbPath;
            } else {
                mSafePath = currentSafeRoot + File.separator + currentSafeName + File.separator;
            }
            PrivateHelper mPrivateHelper = new PrivateHelper(context, mSafePath);
            Log.d("QUES", "this is sql path" + dbPath + "mEditOriginal is" + mEditOriginal);
            db = mPrivateHelper.getWritableDatabase();
            /*MODIFIED-END by wenjing.ni,BUG-1940959*/
            cursor = db.query(PrivateHelper.USER_TABLE_NAME, new String[]{PrivateHelper.USER_FIELD_WT2}, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                original = cursor.getString(0);
            }
            if (original.equals(mEditOriginal)) {

                if (original.equals(mEditOriginal)) {
                    ContentValues values = new ContentValues();
                    values.put(PrivateHelper.USER_FIELD_WT2, mEditFirst);
                    db.update(PrivateHelper.USER_TABLE_NAME, values, null, null);
                    return true;
                }
            }

        } catch (Exception e) {

        } finally {
        /* MODIFIED-END by haifeng.tang,BUG-1989942*/
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return false;
    }


    /* MODIFIED-BEGIN by haifeng.tang, 2016-04-23,BUG-1989942*/
    public static boolean isAuthorizationOriginalQuestion(Context context, String dbPath, String mEditOriginal, int questionIndex, String mEditAnswer) {
        /*MODIFIED-BEGIN by wenjing.ni, 2016-04-13,BUG-1940959*/
        String currentSafeRoot = SharedPreferenceUtils.getCurrentSafeRoot(context);
        String currentSafeName = SharedPreferenceUtils.getCurrentSafeName(context);
        String original = null;
        Cursor cursor = null;
        SQLiteDatabase db = null;
        String mSafePath = null;
        try {
            if (currentSafeRoot == null || currentSafeRoot.equals("")) {
                mSafePath = dbPath;
            } else {
                mSafePath = currentSafeRoot + File.separator + currentSafeName + File.separator;
            }
            PrivateHelper mPrivateHelper = new PrivateHelper(context, mSafePath);
            Log.d("QUES", "this is sql path" + dbPath + "mEditOriginal is" + mEditOriginal);
            db = mPrivateHelper.getWritableDatabase();
            /*MODIFIED-END by wenjing.ni,BUG-1940959*/
            cursor = db.query(PrivateHelper.USER_TABLE_NAME, new String[]{PrivateHelper.USER_FIELD_WT2}, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                original = cursor.getString(0);
            }
            if (original.equals(mEditOriginal)) {

                if (original.equals(mEditOriginal)) {
                    ContentValues values = new ContentValues();
                    values.put(PrivateHelper.USER_FIELD_WT3, mEditAnswer);
                    values.put(PrivateHelper.USER_FIELD_QD, questionIndex);
                    db.update(PrivateHelper.USER_TABLE_NAME, values, null, null);
                    return true;
                }
            }

        } catch (Exception e) {

        } finally {
        /* MODIFIED-END by haifeng.tang,BUG-1989942*/
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return false;
    }



    /* MODIFIED-BEGIN by haifeng.tang, 2016-04-23,BUG-1989942*/
    public static boolean updateSafeBoxName(Context context, String dbPath, String name) {
        String currentSafeRoot = SharedPreferenceUtils.getCurrentSafeRoot(context);
        String currentSafeName = SharedPreferenceUtils.getCurrentSafeName(context);
        SQLiteDatabase db = null;
        String mSafePath = null;
        try {
            if (currentSafeRoot == null || currentSafeRoot.equals("")) {
                mSafePath = dbPath;
            } else {
                mSafePath = currentSafeRoot + File.separator + currentSafeName + File.separator;
            }
            PrivateHelper mPrivateHelper = new PrivateHelper(context, mSafePath);
            Log.d("QUES", "this is sql path" + dbPath + "mEditOriginal is" + name);
            db = mPrivateHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(PrivateHelper.USER_FIELD_AL, name); // MODIFIED by wenjing.ni, 2016-05-04,BUG-802835
            db.update(PrivateHelper.USER_TABLE_NAME, values, null, null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return false;
    }

    /*MODIFIED-BEGIN by wenjing.ni, 2016-04-14,BUG-1924019*/
    public static boolean UpdatePassword(Context context, String dbPath, String mEditOriginal) {
    /* MODIFIED-END by haifeng.tang,BUG-1989942*/
        String currentSafeRoot = SharedPreferenceUtils.getCurrentSafeRoot(context);
        String currentSafeName = SharedPreferenceUtils.getCurrentSafeName(context);
        SQLiteDatabase db = null;
        String mSafePath = null;
        try {
            if (currentSafeRoot == null || currentSafeRoot.equals("")) {
                mSafePath = dbPath;
            } else {
                mSafePath = currentSafeRoot + File.separator + currentSafeName + File.separator;
            }
            PrivateHelper mPrivateHelper = new PrivateHelper(context, mSafePath);
            /* MODIFIED-BEGIN by haifeng.tang, 2016-04-23,BUG-1989942*/
            Log.d("QUES", "this is sql path" + dbPath + "mEditOriginal is" + mEditOriginal);
            db = mPrivateHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(PrivateHelper.USER_FIELD_WT2, mEditOriginal);
            db.update(PrivateHelper.USER_TABLE_NAME, values, null, null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
            /* MODIFIED-END by haifeng.tang,BUG-1989942*/
                db.close();
            }
        }
        return false;
    }
    /*MODIFIED-END by wenjing.ni,BUG-1924019*/

    /* MODIFIED-BEGIN by haifeng.tang, 2016-04-23,BUG-1989942*/
    public static int deleteSafeFileRecord(Context context, String path) {
        String dbPath = SafeUtils.getCurrentSafePath(context);

        LogUtils.d("DELE", "shareutils is" + SharedPreferenceUtils.getCurrentSafeName(context));
        LogUtils.d("DELE", "this is sqlite path" + path + "db is path" + dbPath);
        /*MODIFIED-BEGIN by wenjing.ni, 2016-04-13,BUG-1940959*/
        SQLiteDatabase db = null;
        try {
            PrivateHelper mPrivateHelper = new PrivateHelper(context, dbPath);
            db = mPrivateHelper.getWritableDatabase();
            /*MODIFIED-END by wenjing.ni,BUG-1940959*/
            db.delete(PrivateHelper.FILE_TABLE_NAME, PrivateHelper.FILE_FIELD_TP + "=?", new String[]{path});
            File tempFile = new File(path + "_temp");
            if (tempFile.exists()) {
                tempFile.delete();
            }
            LogUtils.d("DELE", "this is sqlite path--11--" + path);
        } catch (Exception e) {

        } finally {
        /* MODIFIED-END by haifeng.tang,BUG-1989942*/
            if (db != null) {
                db.close();
            }
        }
        return FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
    }

    public static List<String> queryShiftOutSourcePath(Context context, List<FileInfo> srcList) {
        List<String> sourcePath = new ArrayList<String>();
        String dbPath = SafeUtils.getCurrentSafePath(context);
        Log.d("SHIFT", "this is enter--ShiftOutSafeFileTask--11111111--" + dbPath);
        /*MODIFIED-BEGIN by wenjing.ni, 2016-04-13,BUG-1940959*/
        SQLiteDatabase db = null;
        try {
            PrivateHelper mPrivateHelper = new PrivateHelper(context, dbPath);
            db = mPrivateHelper.getWritableDatabase();
            for (int i = 0; i < srcList.size(); i++) {
                Cursor cursor = null;
                try {
                    Log.d("SHIFT", "this is enter--ShiftOutSafeFileTask--333333--" + srcList.get(i).getFileAbsolutePath());
                    cursor = db.query(PrivateHelper.FILE_TABLE_NAME, new String[]{PrivateHelper.FILE_FIELD_SP}, PrivateHelper.FILE_FIELD_TP + " = ? ",
                            new String[]{srcList.get(i).getFileAbsolutePath()}, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {

                        sourcePath.add(cursor.getString(0));
                        Log.d("SHIFT", "this is enter--ShiftOutSafeFileTask--2222222--" + cursor.getString(0));
                    }

                } catch (Exception e) {

                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }

            }
        } catch (Exception e) { // MODIFIED by haifeng.tang, 2016-04-23,BUG-1989942

        } finally {
            if (db != null) {
                db.close();
            }
            /*MODIFIED-END by wenjing.ni,BUG-1940959*/
        }

        if (sourcePath != null) {
            return sourcePath;
        }

        return new ArrayList<String>() {
        };
    }


    /* MODIFIED-BEGIN by haifeng.tang, 2016-04-23,BUG-1989942*/
    public static boolean needDecrypt(String mimeType) {
        boolean noNeedDecrpyt = mimeType.startsWith("application/zip")
                || mimeType.startsWith("application/x-rar-compressed")
                || mimeType.startsWith("application/x-tar")
                || mimeType.startsWith("application/x-7z-compressed")
                || mimeType.startsWith("application/vnd.android.package-archive");
        return !noNeedDecrpyt;
    }


    public static List<FileInfo> querySafeAllFileInfo(Context context) {
        List<FileInfo> mSafeAllInfo = new ArrayList<FileInfo>();
        String dbPath = SafeUtils.getCurrentSafePath(context);

        Cursor cursor = null;
        /*MODIFIED-BEGIN by wenjing.ni, 2016-04-13,BUG-1940959*/
        SQLiteDatabase db = null;
        /* MODIFIED-END by haifeng.tang,BUG-1989942*/
        try {
            PrivateHelper mPrivateHelper = new PrivateHelper(context, dbPath);
            db = mPrivateHelper.getWritableDatabase();
            String[] project = new String[]{PrivateHelper.FILE_FIELD_TP};
            /*MODIFIED-END by wenjing.ni,BUG-1940959*/
            cursor = db.query(PrivateHelper.FILE_TABLE_NAME, project, null, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    mSafeAllInfo.add(new FileInfo(context, cursor.getString(0)));
                }
            }
        } catch (Exception e) {

        } finally {
            if (db != null) {
                db.close();
            }
            if (cursor != null) {
                cursor.close();
            }
        }

        if (mSafeAllInfo == null) {
            return new ArrayList<FileInfo>();

        }

        return mSafeAllInfo;
    }


    public static boolean isRelateFingerPrint(Context context, String dbPath) {
        /*MODIFIED-BEGIN by wenjing.ni, 2016-04-13,BUG-1940959*/
        SQLiteDatabase db = null;
        String relateStr = null;
        Cursor cursor = null;
        try {
            PrivateHelper mPrivateHelper = new PrivateHelper(context, dbPath +File.separator);
            db = mPrivateHelper.getWritableDatabase();
            cursor = db.query(PrivateHelper.USER_TABLE_NAME, new String[]{PrivateHelper.USER_FIELD_ST}, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                relateStr = cursor.getString(0);
            }
        } catch (Exception e) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        if(relateStr != null && !relateStr.equals("")){

            return true;
        }
        return false;

    }
}
