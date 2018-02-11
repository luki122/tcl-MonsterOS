/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.utils;


import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.xdja.cssp.was.open.auth.sdk.OpenAuthUtil;
import com.xdja.safekeyservice.jarv2.EntityManager;
import com.xdja.safekeyservice.jarv2.SecurityGroupManager;
import com.xdja.safekeyservice.jarv2.SecuritySDKManager;
import com.xdja.sks.IEncDecListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.tcl.filemanager.FileManagerApplication;
import cn.tcl.filemanager.R;
import cn.tcl.filemanager.fragment.FileBrowserFragment;
import cn.tcl.filemanager.manager.CategoryManager;
import cn.tcl.filemanager.manager.MountManager;
import cn.tcl.filemanager.manager.SafeManager;

import mst.app.dialog.AlertDialog;

/* MODIFIED-BEGIN by songlin.qi, 2016-06-05,BUG-2204966*/
/* MODIFIED-END by songlin.qi,BUG-2204966*/

/**
 * Created by user on 16-2-26.
 */
public class SafeUtils {

    public static final String TAG = "SafeUtils";
    public static final int PHONE_STORAGE_SAFE = 0;
    public static final int SD_STORAGE_SAFE = 1;
    public static final int EXTERNAL_STORAGE_SAFE = 2;
    public static final String SAFE_ROOT_DIR = ".File_SafeBox";
    public static final String DECRYPT_TEMP_ROOT_DIR = ".Decrypt_Temp";

    public static final int ENCRYPT_SUCCESS_DELET_FILE_MSG = 1001;
    public static final int DECRYPT_SUCCESS_MSG = 1002;

    public static final int OPEN_ENCRYPT_MSG = 0X1003;

    private static  List<FileInfo> mSafeFiles = new ArrayList<>(); // MODIFIED by haifeng.tang, 2016-04-23,BUG-1956936

    private static String mStrSGroupsId;

    private static String mDeviceID;

    public static final int ENCRYPT_OR_DECRYPT_TIME_LIMIT = 30;
    public static final int ENCRYPT_OR_DECRYPT_TIME_MINUES = 60;

    public static final int FILE_TRANSFER_SPEED = 2* 1024 * 1024;

    public static final int OPEN_ENCRYPT_FILE = 0x01;
    public static final int BATCH_FILE_DECRYPTION = 0x02;
    public static final int BATCH_FILE_ENCRYPTION = 0x03;
    public static final int UPDATE_ENCRYPT_PROCESS = 0x04;
    public static final int UPDATE_DECRYPT_PROCESS = 0x05;

    public static final int SDK_SECURITY_EXCEPTION = 0x50001;




    public static void addSafeFiles(List<FileInfo> safeFiles) {
        mSafeFiles.clear(); // MODIFIED by wenjing.ni, 2016-05-13,BUG-2003636
        mSafeFiles.addAll(safeFiles);
    }

    public static List<FileInfo> getSafeFiles() {
        return mSafeFiles;
    }


    /**
     * 4.2.4.3 encrypt file
     *
     * @param context
     * @param sourceFilePath
     * @param encryptFilePath
     * @param encDecListener
     */
    public static boolean encryptFile(Context context, String sourceFilePath, String encryptFilePath, IEncDecListener encDecListener) {
        try {
            LogUtils.i(TAG, "sourceFilePath:" + sourceFilePath + ",encryptFilePath:" + encryptFilePath);
//            SecuritySDKManager.getInstance().encryptFile(FileManagerApplication.getImei(context), FileManagerApplication.getImei(context), sourceFilePath, encryptFilePath, encDecListener);
            return copyFile(sourceFilePath, encryptFilePath, encDecListener);
        } catch (SecurityException e) {
            LogUtils.e(TAG, e.toString());
            try {
                if (null != encDecListener) {
                    encDecListener.onOperComplete(SDK_SECURITY_EXCEPTION);
                }
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
        }
        return false;
    }

    private static boolean copyFile(String srcPath, String destPath, IEncDecListener encDecListener) {
        try {
            InputStream in = new FileInputStream(srcPath);
            OutputStream out = new FileOutputStream(destPath);
            if (in == null || out == null) {
                return false;
            }
            byte[] buffer = new byte[1024];
            int iRead = 0;
            while ((iRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, iRead);
                if (null != encDecListener) {
                    encDecListener.onOperProgress(iRead, 0);
                }
            }
            in.close();
            out.close();
            return true;
            //encDecListener.onOperComplete(0);//modify by liaoah
        } catch (Exception e) {
            e.printStackTrace();
            /*
            try {

                if (null != encDecListener) {
                    encDecListener.onOperComplete(SDK_SECURITY_EXCEPTION);
                }
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }*/
        }
        return false;
    }

    public static void clearSafeFiles() {
        mSafeFiles.clear();
    }

    public static String getMD5String(String s) {
        char hexDigits[] = {'0', '1', '2', '3', '4',
                '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            byte[] btInput = s.getBytes();
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * get encrypt entity
     * @return entity
     */
    public static String getDeviceID(){
        if (null == mDeviceID){
            JSONObject result = EntityManager.getInstance().getDeviceID();
            try {
                int ret_code = result.getInt("ret_code");
                if(ret_code == 0){
                    JSONObject device_id = result.getJSONObject("result");
                    mDeviceID = device_id.getString("device_id");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return mDeviceID;
    }

    /**
     * 4.2.4.4 decrypt file
     *
     * @param context
     * @param encryptFilePath
     * @param decryptFilePath
     * @param encDecListener
     */
    public static void decryptFile(Context context, String encryptFilePath, String decryptFilePath, IEncDecListener encDecListener) {
        LogUtils.i(TAG, "encryptFilePath:" + encryptFilePath + ",decryptFilePath:" + decryptFilePath);
        try {
//            SecuritySDKManager.getInstance().decryptFile(FileManagerApplication.getImei(context), encryptFilePath, decryptFilePath, encDecListener);
            copyFile(encryptFilePath, decryptFilePath, encDecListener);
        }catch (Exception e){
            try {
                LogUtils.e(TAG, e.toString());
                encDecListener.onOperComplete(SDK_SECURITY_EXCEPTION);
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static String filterEmoji(String source) {
        if(source != null)
        {
            Pattern emoji = Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]", Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE) ;
            Matcher emojiMatcher = emoji.matcher(source);
            if ( emojiMatcher.find())
            {
                source = emojiMatcher.replaceAll("*");
                return source ;
            }
            return source;
        }
        return source;
    }

    /**
     * opcode signature
     * @param sourceData
     * @return
     */
    public  static String getSignatureOfOpCode(String sourceData){
        OpenAuthUtil openAuthUtil = new OpenAuthUtil();

        /** real key */
        String secretKey = "9fd9b7f3dd64440e61a1f3ecf65f127b";
        String result = null;
        try
        {
            result = openAuthUtil.getBase64SignatureBySecretKey( secretKey, sourceData );
            LogUtils.i(TAG, "result:" + result);
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        return result;

    }

    public static String getEncryptRootPath(Context context) {
        String path = context.getFilesDir().getAbsolutePath() + File.separator + SAFE_ROOT_DIR;
//        String path = MountManager.getInstance().getPhonePath() + File.separator + SAFE_ROOT_DIR;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        return path;
    }

    public static String getDecryptRootPath(Context context) {
//        String path = context.getFilesDir().getAbsolutePath() + File.separator + DECRYPT_TEMP_ROOT_DIR;
        String path = MountManager.getInstance().getPhonePath() + File.separator + DECRYPT_TEMP_ROOT_DIR;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        return path;
    }

    public static String createDirInRootPathByPath(String filePath, Context context) {
        String dir = encryptFileDir(filePath);
        String path = context.getFilesDir().getAbsolutePath() + File.separator + SAFE_ROOT_DIR + dir;

        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }

    public static String createDirForDecryptByPath(Context context, String filePath) {
        String dir = filePath.replace(SafeUtils.getEncryptRootPath(context), "");
        String path = ((FileManagerApplication)context.getApplicationContext()).mCurrentPath + dir;

        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }

    public static String encryptFileDir(String dir) {
        if (null != CategoryManager.getPhoneRootPath()) {
            dir = dir.replace(CategoryManager.getPhoneRootPath(), "");
        }
        if (null != CategoryManager.getSDRootPath()) {
            dir = dir.replace(CategoryManager.getSDRootPath(), "");
        }
        dir = dir.replace(CategoryManager.OTG_ROOT_PATH, "");
        return dir;
    }

    /**
     * Calculate the encryption and decryption size by fileinfos
     */
    public static long calculateSize(List<FileInfo> fileInfos) {
        if (null == fileInfos){
            return 0;
        }
        long size = 0;
        for (FileInfo fileInfo : fileInfos) {
            if (!fileInfo.getFileName().startsWith(".")) {
                if (fileInfo.isDirectory()) {
                    if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES) {
                        size += calculateSize(fileInfo.getSubFileInfo());
                    } else {
                        size += calculateSize(fileInfo.getFile().listFiles());
                    }
                } else {
                    size += fileInfo.getFileSize();
                }
            }
        }
        return size;
    }

    /**
     * Calculate the encryption and decryption size by files
     */
    public static long calculateSize(File[] files) {
        if (null == files) {
            return 0;
        }
        long size = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                size += calculateSize(file.listFiles());
            } else {
                size += file.length();
            }

        }
        return size;
    }

    public static int calculateFileCount(List<FileInfo> fileInfos) {
        if (null == fileInfos) {
            return 0;
        }
        int count = 0;
        for (FileInfo fileInfo : fileInfos) {
            if (!fileInfo.getFileName().startsWith(".")) {
                if (fileInfo.isDirectory()) {
                    count += calculateFileCount(fileInfo.getFile().listFiles());
                } else {
                    count++;
                }
            }
        }
        return count;
    }

    public static int calculateFileCount(File[] files) {
        if (null == files) {
            return 0;
        }
        int count = 0;
        for (File file : files) {
            if (!file.getName().startsWith(".")) {
                if (file.isDirectory()) {
                    count += calculateFileCount(file.listFiles());
                } else {
                    count++;
                }
            }
        }
        return count;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo[] info = cm.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * get sGroup id by current entity
     * @param context
     * @return sgroupid
     */
    public static String getSGroupsId(Context context) {
        if (null == mStrSGroupsId) {
            List<String> list = new ArrayList<>();
            list.add(FileManagerApplication.getImei(context));
            JSONObject result = SecurityGroupManager.getInstance().getSGroups(list);
            int resultCode = JSONObjectUtils.getResultCode(result);
            if (resultCode == 0){
                JSONObject resultObject = JSONObjectUtils.getResultObject(result);
                if (resultObject != null) {
                    // TODO Detail jsonobject
                } else {
                    LogUtils.d(TAG, "JSONObject is null");
                }
            } else {
                LogUtils.d(TAG, "get groups id is  false，message ： " + JSONObjectUtils.getResultError(result));
            }
        }
        return mStrSGroupsId;
    }

    public static void getSafeRootPath(Context context, int safeType, MountManager mMountPointManager, String password, String answser, int qIndex,String name,boolean isRelateFingerPrint,boolean isFirstSafebox) { // MODIFIED by haifeng.tang, 2016-04-23,BUG-1989942 // MODIFIED by wenjing.ni, 2016-05-13,BUG-2003636
        long createTime = System.currentTimeMillis();
        String isRelateStr = null;
        StringBuilder rootPath = new StringBuilder();
        /* MODIFIED-BEGIN by songlin.qi, 2016-06-05,BUG-2204966*/
        String storageDirectory = null;
        switch (safeType) {
            case PHONE_STORAGE_SAFE:
                storageDirectory = mMountPointManager.getPhonePath();
                break;

            case SD_STORAGE_SAFE:
                storageDirectory = mMountPointManager.getSDCardPath();
                break;

            case EXTERNAL_STORAGE_SAFE:
                storageDirectory = mMountPointManager.getUsbOtgPath();
                break;
        }

        if (TextUtils.isEmpty(storageDirectory) || "null".equals(storageDirectory)) {
            Log.d("SafeUtils", "getSafeRootPath(): get a null value for rootPath from mMountPointManager!");
            // if rootPath is null try to get from ExternalStorageDirectory
            File path = Environment.getExternalStorageDirectory();
            if (path != null && !TextUtils.isEmpty(path.getAbsolutePath())) {
                rootPath.append(path.getAbsolutePath());
            }
        } else {
            rootPath.append(storageDirectory);
        }
        /* MODIFIED-END by songlin.qi,BUG-2204966*/

        rootPath.append(File.separator + ".File_SafeBox");
        File rootFile = new File(rootPath.toString());
        if (!rootFile.exists()) {
            rootFile.mkdirs();
        }
        File noMedia = new File(rootFile.getAbsolutePath() + File.separator + ".nomedia");
        if (!noMedia.exists()) {
            try {
                noMedia.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String md5FileName = getMD5String(String.valueOf(createTime));
        File safeRoot = new File(rootFile.getAbsolutePath() + File.separator + md5FileName);
        if (safeRoot.exists()) {
            safeRoot.mkdirs();
        }
        File safeFile = new File(rootFile.getAbsolutePath() + File.separator + md5FileName + File.separator + "file");
        safeFile.mkdirs();
        File safeFilenoMedia = new File(safeFile.getAbsolutePath() + File.separator + ".nomedia");
        if (!safeFilenoMedia.exists()) {
            try {
                safeFilenoMedia.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File safeTemp = new File(rootFile.getAbsolutePath() + File.separator + md5FileName + File.separator + "temp");
        safeTemp.mkdirs();
        File safeTempnoMedia = new File(safeTemp.getAbsolutePath() + File.separator + ".nomedia");
        if (!safeTempnoMedia.exists()) {
            try {
                safeTempnoMedia.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File safeNomedia = new File(rootFile.getAbsolutePath() + File.separator + md5FileName + File.separator + ".nomedia");
        Log.d("SAFE", "this is saferoot is" + safeRoot.getAbsolutePath() + File.separator);
//        PrivateHelper mPrivateHelper = new PrivateHelper(context,safeRoot.getAbsolutePath()+File.separator);
//        mPrivateHelper.getWritableDatabase();
        try {
            safeNomedia.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if(isRelateFingerPrint){
//             isRelateStr = "relate";
//        }
        SafeManager.insertSafe(context, name,safeRoot.getAbsolutePath() + File.separator, createTime, password, answser, qIndex,mMountPointManager,isRelateFingerPrint,isFirstSafebox); // MODIFIED by haifeng.tang, 2016-04-23,BUG-1989942 // MODIFIED by wenjing.ni, 2016-05-04,BUG-802835


    }

    public static List<SafeInfo> getStorageItem(MountManager mMountPointManager, Context mContext) {

        List<SafeInfo> storageItem = new ArrayList<SafeInfo>();

        if (!CommonUtils.isPhoneStorageZero()) {
            SafeInfo info = new SafeInfo();
            info.setStorage_name(mContext.getResources().getString(R.string.phone_storage));
            storageItem.add(info);
        }
        if (mMountPointManager.isSDCardMounted()) {
            SafeInfo info = new SafeInfo();
            info.setStorage_name(mContext.getResources().getString(R.string.sd_card));
            storageItem.add(info);
        }

        if (mMountPointManager.isOtgMounted()) {
            SafeInfo info = new SafeInfo();
            info.setStorage_name(mContext.getResources().getString(R.string.usbotg_m));
            storageItem.add(info);
        }
        return storageItem;
    }

    public static List<SafeInfo> getStorageExternalItem(MountManager mMountPointManager, Context mContext) {

        List<SafeInfo> storageItem = new ArrayList<SafeInfo>();

        if (mMountPointManager.isSDCardMounted()) {
            SafeInfo info = new SafeInfo();
            info.setStorage_name(mContext.getResources().getString(R.string.sd_card));
            storageItem.add(info);
        }

        if (mMountPointManager.isOtgMounted()) {
            SafeInfo info = new SafeInfo();
            info.setStorage_name(mContext.getResources().getString(R.string.usbotg_m));
            storageItem.add(info);
        }
        return storageItem;
    }

    public static List<SafeInfo> getSafeItem(MountManager mMountPointManager, Context mContext) {
        List<SafeInfo> storageItem = new ArrayList<SafeInfo>();
        String phoneSafePath = null;
        if (!CommonUtils.isPhoneStorageZero()) {
            phoneSafePath = mMountPointManager.getPhonePath() + File.separator + SAFE_ROOT_DIR;
            File phoneFile = new File(phoneSafePath);
            if (phoneFile.exists()) {
                File[] tempList = phoneFile.listFiles();
                /* MODIFIED-BEGIN by wenjing.ni, 2016-05-04,BUG-802835*/
                if (tempList != null) {
                    for (int i = 0; i < tempList.length; i++) {
                        if (tempList[i].getName().length() == 32) {
                            if (new File(tempList[i].getAbsolutePath() + File.separator + "msb.db").exists()) {
                                storageItem.add(SafeManager.getSafeInfo(mContext, tempList[i].getAbsolutePath(), SafeUtils.PHONE_STORAGE_SAFE));
                            }
                            /* MODIFIED-END by wenjing.ni,BUG-802835*/
                        }
                    }
                }
            }
        }
        if (mMountPointManager.isSDCardMounted()) {
            phoneSafePath = mMountPointManager.getSDCardPath() + File.separator + SAFE_ROOT_DIR;
            File sdFile = new File(phoneSafePath);
            if (sdFile.exists()) {
                File[] tempList = sdFile.listFiles();
                /* MODIFIED-BEGIN by wenjing.ni, 2016-05-04,BUG-802835*/
                if (tempList != null) {
                    for (int i = 0; i < tempList.length; i++) {
                        if (tempList[i].getName().length() == 32) {
                            if (new File(tempList[i].getAbsolutePath() + File.separator + "msb.db").exists()) {
                                storageItem.add(SafeManager.getSafeInfo(mContext, tempList[i].getAbsolutePath(), SafeUtils.SD_STORAGE_SAFE));
                            }
                            /* MODIFIED-END by wenjing.ni,BUG-802835*/
                        }
                    }
                }
            }
        }

        if (mMountPointManager.isOtgMounted()) {
            phoneSafePath = mMountPointManager.getUsbOtgPath() + File.separator + SAFE_ROOT_DIR;
            File externalFile = new File(phoneSafePath);
            if (externalFile.exists()) {
                File[] tempList = externalFile.listFiles();
                /* MODIFIED-BEGIN by wenjing.ni, 2016-05-04,BUG-802835*/
                if (tempList != null) {
                    for (int i = 0; i < tempList.length; i++) {
                        if (tempList[i].getName().length() == 32) {
                            if (new File(tempList[i].getAbsolutePath() + File.separator + "msb.db").exists()) {
                                storageItem.add(SafeManager.getSafeInfo(mContext, tempList[i].getAbsolutePath(), SafeUtils.EXTERNAL_STORAGE_SAFE));
                            }
                            /* MODIFIED-END by wenjing.ni,BUG-802835*/
                        }
                    }
                }
            }
        }

        return storageItem;
    }


    public static String getCurrentSafePath(Context context) {
        return SharedPreferenceUtils.getCurrentSafeRoot(context) + File.separator +
                SharedPreferenceUtils.getCurrentSafeName(context) + File.separator;
    }

    public static List<FileInfo> getSafeTempFile(Context context) {
        List<FileInfo> tempList = new ArrayList<FileInfo>();
        File tempFile = new File(getCurrentSafePath(context) + "temp");
        File[] tempArray = tempFile.listFiles();
        if (tempArray == null) {
            return new ArrayList<FileInfo>();
        }
        for (int i = 0; i < tempArray.length; i++) {
            tempList.add(new FileInfo(context, tempArray[i].getAbsoluteFile()));
        }

        if (tempList == null) {
            return new ArrayList<FileInfo>();
        }
        return tempList;
    }

    public static String getSafeRootName(Context context,MountManager mMountPointManager, String rootPath) {
        String storageLocation = null;
        File rootFile = new File(rootPath);
        if (mMountPointManager.getPhonePath() != null && mMountPointManager.getPhonePath().equals(rootFile.getParent())) {
            storageLocation = context.getString(R.string.phone_storage_cn);
        } else if (mMountPointManager.getSDCardFile() != null && mMountPointManager.getSDCardPath().equals(rootFile.getParent())) {
            storageLocation = context.getString(R.string.sd_card);
        } else if (mMountPointManager.getUsbOtgPath() != null && mMountPointManager.getUsbOtgPath().equals(rootFile.getParent())) {
            storageLocation = context.getString(R.string.usbotg_m); // MODIFIED by songlin.qi, 2016-06-08,BUG-2278011
        }
        if (storageLocation != null) {
            storageLocation = context.getString(R.string.phone_storage_cn);
        }
        return storageLocation;

    }

    public static String getSafeRestoreDefaultPath(MountManager mountManager) {
        String rootPath = mountManager.getPhonePath();
        String restorePath = rootPath + File.separator + "File_Restore";
        File restoreFile = new File(restorePath);
        if (!restoreFile.exists()) {
            restoreFile.mkdirs();
        }

        return restorePath + File.separator;
    }

    public static boolean deleteSafeRootFolder(String mRootPath) {
        File mSafeRootPath = new File(mRootPath);
        File[] mFileList = mSafeRootPath.listFiles();
        if (mFileList == null) {
            return false;
        }
        for (int i = 0; i < mFileList.length; i++) {
            if (mFileList[i].isDirectory()) {
                File[] mChildList = mFileList[i].listFiles();

                if (mChildList.length > 0) {
                    for (int j = 0; j < mChildList.length; j++) {
                        if (mChildList[j].exists()) {
                            mChildList[j].delete();
                        }
                    }
                }
                mFileList[i].delete();
            } else {
                mFileList[i].delete();
            }
        }
        mSafeRootPath.delete();
        return true;
    }


    /* MODIFIED-BEGIN by wenjing.ni, 2016-05-10,BUG-1967152*/
    public static void openSafeBoxRelate(Context context){
        try {
            FingerprintManager mFingerprintManager = (FingerprintManager) context.getSystemService(
                    Context.FINGERPRINT_SERVICE);
            Class clazz = mFingerprintManager.getClass();
            Method method = clazz.getMethod("tctEnableSafeboxFp",new Class[]{boolean.class});
            Object result = method.invoke(mFingerprintManager, true);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    /* MODIFIED-END by wenjing.ni,BUG-1967152*/

    /* MODIFIED-BEGIN by wenjing.ni, 2016-05-14,BUG-2104869*/
    public static int getFingerAuthenticationResult(Context context,int resultID){
        int result = -1;
        try {
            FingerprintManager mFingerprintManager = (FingerprintManager) context.getSystemService(
                    Context.FINGERPRINT_SERVICE);
            Class clazz = mFingerprintManager.getClass();
            Method method = clazz.getMethod("tctGetTagForFingerprintId",new Class[]{int.class});
            result = (int)method.invoke(mFingerprintManager, resultID);
        } catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }
    /* MODIFIED-END by wenjing.ni,BUG-2104869*/


    public static boolean isUserFingerPrint(Context context) {
        List<Fingerprint> result = new ArrayList<Fingerprint>();
        try {
            FingerprintManager mFingerprintManager = (FingerprintManager) context.getSystemService(
                    Context.FINGERPRINT_SERVICE);
            Class clazz = mFingerprintManager.getClass();
            Method method = clazz.getMethod("tctGetEnrolledFingerprints", new Class[]{int.class});
            result = (List<Fingerprint>) method.invoke(mFingerprintManager, 0);
        } catch (Exception e) {
            LogUtils.e(TAG, e.toString());
        }
        if (result != null && result.size() > 0) {
            return true;
        }
        return false;
    }

    public static void fieldDialog(AlertDialog dialog){
        try {
            Field field = dialog.getClass().getDeclaredField("mAlert");
            field.setAccessible(true);
            Object obj = field.get(dialog);
            field = obj.getClass().getDeclaredField("mHandler");
            field.setAccessible(true);
            field.set(obj, new ButtonHandler(dialog));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getSafeTitle(MountManager mMountPointManager,Context mContext) {
        String rootSafePath = SharedPreferenceUtils.getCurrentSafeRoot(mContext);
        String mSafeBoxName = SafeManager.getSafeBoxName(mContext, SafeUtils.getCurrentSafePath(mContext));
        /* MODIFIED-END by wenjing.ni,BUG-802835*/
        String rootPath = null;
        String actionTitle = null;
        if (rootSafePath != null) {
            rootPath = new File(rootSafePath).getParentFile().getAbsolutePath();
        }
        if (rootPath != null) {
            if (rootPath.equals(mMountPointManager.getPhonePath())) {
                /* MODIFIED-BEGIN by wenjing.ni, 2016-05-04,BUG-802835*/
                actionTitle = mSafeBoxName + "(" + mContext.getResources().getString(R.string.phone_storage_cn) + ")";
            } else if (rootPath.equals(mMountPointManager.getSDCardPath())) {
                actionTitle = mSafeBoxName + "(" + mContext.getResources().getString(R.string.sd_card) + ")";
            } else if (rootPath.equals(mMountPointManager.getUsbOtgPath())) {
                actionTitle = mSafeBoxName + "(" + mContext.getResources().getString(R.string.usbotg_m) + ")"; // MODIFIED by songlin.qi, 2016-06-08,BUG-2278011
            }
        } else {
            actionTitle = mSafeBoxName + "(" + mContext.getResources().getString(R.string.phone_storage) + ")";
            /* MODIFIED-END by wenjing.ni,BUG-802835*/
        }

        Log.e("SafeBox", "getSafeTitle()=" + actionTitle);
        return actionTitle;

    }

    public static boolean isQuitSafe(Context context) {
        SafeManager.notQuitSafe = false;
        if (!SafeUtils.isActivityRunning(context, "FileSafeBrowserActivity") && context
                .getClass().getName().equals("FileSafeBrowserActivity")) {
            return true;
        }
        return false;
    }

    public static boolean isActivityRunning(Context mContext, String ActivityName) {
        ActivityManager mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        KeyguardManager mKeyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        boolean flag = mKeyguardManager.inKeyguardRestrictedInputMode();
        List<ActivityManager.RunningTaskInfo> info = mActivityManager.getRunningTasks(1);
        if (info != null && info.size() > 0) {
            ComponentName component = info.get(0).topActivity;
            if (ActivityName.equals(component.getClassName()) && !flag) {
                return true;
            }
        }
        return false;
    }

}
