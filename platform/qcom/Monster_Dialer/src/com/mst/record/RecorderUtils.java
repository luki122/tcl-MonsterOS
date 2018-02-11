package com.mst.record;

import java.io.File;

import android.os.Environment;
//import com.mediatek.storage.StorageManagerEx;

public class RecorderUtils {
	private final static String TAG = "RecorderUtils";

   

    public static String getExternalStorageDefaultPath() {
//        return StorageManagerEx.getDefaultPath();
    	return Environment.getExternalStorageDirectory().getPath();
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
    
   
}
