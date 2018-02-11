package com.mediatek.telecom.recording;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.util.Base64;
import android.util.Log;


import com.android.incallui.Call;
import com.android.incallui.CallList;
import com.android.incallui.ContactInfoCache;
import com.android.incallui.InCallApp;
import com.android.incallui.ContactInfoCache.ContactCacheEntry;
import com.mst.privacy.PrivacyDataUtils;
//import com.mediatek.storage.StorageManagerEx;

import java.io.File;
import java.text.SimpleDateFormat;

public class RecorderUtils {
	private final static String TAG = "RecorderUtils";

    public static boolean diskSpaceAvailable(long sizeAvailable) {
        return (getDiskAvailableSize() - sizeAvailable) > 0;
    }

    public static boolean diskSpaceAvailable(String defaultPath, long sizeAvailable) {
        if (null == defaultPath) {
            return diskSpaceAvailable(sizeAvailable);
        } else {
            File sdCardDirectory = new File(defaultPath);
            StatFs statfs;
            try {
                if (sdCardDirectory.exists() && sdCardDirectory.isDirectory()) {
                    statfs = new StatFs(sdCardDirectory.getPath());
                } else {
                //    log("-----diskSpaceAvailable: sdCardDirectory is null----");
                    return false;
                }
            } catch (IllegalArgumentException e) {
             //   log("-----diskSpaceAvailable: IllegalArgumentException----");
                return false;
            }
            long blockSize = statfs.getBlockSize();
            long availBlocks = statfs.getAvailableBlocks();
            long totalSize = blockSize * availBlocks;
            return (totalSize - sizeAvailable) > 0;
        }
    }

    public static boolean isExternalStorageMounted(Context context) {
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        if (null == storageManager) {
           // log("-----story manager is null----");
            return false;
        }
        String storageState = storageManager.getVolumeState(getExternalStorageDefaultPath());
        return storageState.equals(Environment.MEDIA_MOUNTED) ? true : false;
    }

    public static String getExternalStorageDefaultPath() {
//        return StorageManagerEx.getDefaultPath();
    	return Environment.getExternalStorageDirectory().getPath();
    }

    public static long getDiskAvailableSize() {
        File sdCardDirectory = new File(getExternalStorageDefaultPath());
        StatFs statfs;
        try {
            if (sdCardDirectory.exists() && sdCardDirectory.isDirectory()) {
                statfs = new StatFs(sdCardDirectory.getPath());
            } else {
             //   log("-----diskSpaceAvailable: sdCardDirectory is null----");
                return -1;
            }
        } catch (IllegalArgumentException e) {
         //   log("-----diskSpaceAvailable: IllegalArgumentException----");
            return -1;
        }
        long blockSize = statfs.getBlockSize();
        long availBlocks = statfs.getAvailableBlocks();
        long totalSize = blockSize * availBlocks;
        return totalSize;
    }
    
    //add by lgy
    public static String getRecordPath() {
       String path = getExternalStorageDefaultPath()  + "/PhoneRecord/";
      File fileDir = new File(path);
      if (!fileDir.exists()) {
          fileDir.mkdirs();
      }
      return path;
  }
    
    public static String getRecordFileName() {
        String fileName = "";
        String dirPath = getRecordPath();
        if (null != dirPath) {
            SimpleDateFormat   sDateFormat   =   new   SimpleDateFormat("yyyyMMddHHmm");    
            String   date   =   sDateFormat.format(new   java.util.Date());
            fileName = date + "_" + getCurrentNumber();   	        	        	        
        }
		Log.i(TAG, "getRecordFileName fileName =" + fileName);
        return fileName;
    }
    
    public static String changeRecordFileName(String name, String prefix) {
		Log.i(TAG, "changeRecordFileName name =" + name);
    	String fileName = name.substring(name.indexOf("_"));
    	fileName = prefix + fileName;
        return fileName;
    }
    
    private static String getCurrentNumber() {
		String name = "";
		String number = "";
		Call call = CallList.getInstance().getActiveCall();
		if(call != null) {
	        final ContactInfoCache cache = ContactInfoCache.getInstance(InCallApp.getInstance());
	        ContactCacheEntry cce= cache.getInfo(call.getId());
			name = cce.namePrimary;
			number = cce.number;
		}
		Log.i(TAG, "getCurrentNumber = " + number);
		return number;
    }
   
    public static long getPrivateId() {
        Log.i(TAG, "getPrivateId");
        Call call = CallList.getInstance().getActiveCall();
        if(call != null) {
            final ContactInfoCache cache = ContactInfoCache.getInstance(InCallApp.getInstance());
            ContactCacheEntry cce = cache.findInfoInCache(call);
            if(cce != null) {
                Log.i(TAG, " getPrivateId = " + cce.mPrivateId);                
                return cce.mPrivateId;
            }
        }
        return 0;
    }
    
    public static String getPrivateRecordPath(long PrivateId) {       
        String privacyCallRecodingEncodePath = PrivacyDataUtils.getPrivateHomePath(PrivateId) + Base64.encodeToString(("audio").getBytes(), Base64.URL_SAFE);
        privacyCallRecodingEncodePath = replaceBlank(privacyCallRecodingEncodePath); //必须去掉特殊字符
        return privacyCallRecodingEncodePath;
    }
    
    
    private static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            dest = str.replace("\n", "");
        }
        return dest;
    }
}
