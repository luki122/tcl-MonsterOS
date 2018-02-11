package com.mst.record;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import android.text.TextUtils;
import android.util.Log;

import com.android.dialer.PhoneCallDetails;

public class RecordParseUtil{
    public final static String TAG = "RecordParseUtil";
    
    public static boolean foundAndSetPhoneRecords(PhoneCallDetails[] phoneCallDetails) {
        if (null == phoneCallDetails) {
            return false;
        }

        String historyPath = RecorderUtils.getRecordPath();
        ArrayList<PhoneCallRecord> records = new ArrayList<PhoneCallRecord>();
        int found = 0;

        parseRecording(records, historyPath, false);

//      if (DialerApplication.sIsPrivacySupport && PrivacyUtils.mCurrentAccountId > 0) {
//          historyPath = PrivacyUtils.mCurrentAccountHomePath
//                  + Base64.encodeToString(("audio").getBytes(), Base64.URL_SAFE);
//          historyPath = ContactsUtils.replaceBlank(historyPath);
//          parseRecording(records, historyPath, true);
//      }

//        for (PhoneCallDetails detail : phoneCallDetails) {
//            for (int i = 0; i < records.size() && found < records.size(); ++i) {
//                if (null != records.get(i) && detail.betweenCall(records.get(i).getEndTime())) {
//                    detail.addPhoneRecords(records.get(i));
//                    ++found;
//                }
//            }
//        }
        

        for (PhoneCallDetails detail : phoneCallDetails) {           
            Iterator<PhoneCallRecord> it = records.iterator();
            while (it.hasNext()) {
                PhoneCallRecord r = it.next();
                if (null != r && detail.betweenCall(r.getEndTime())) {
                    detail.addPhoneRecords(r);
                    it.remove();
                    ++found;
                }
            }
        }

        return found > 0;
    }
    
    private static void parseRecording(ArrayList<PhoneCallRecord> records, String path, boolean isPrivacyPath) {
        Log.d(TAG, "parseRecording start path = " + path );
        try {
            synchronized (RecordParseUtil.class) {
                File file = new File(path);
                if (file.isDirectory()) {
                    String[] filesArr = file.list();
                    File[] files = file.listFiles();
                    if (filesArr != null && filesArr.length > 0) {
                        for (int i = 0; i < filesArr.length; i++) {
                            PhoneCallRecord record = new PhoneCallRecord();
                            String name = filesArr[i];
                            Log.d(TAG, "name = " + name);
                            String postfix = getPostfix(name);
                            // name = handlePrivacy(isPrivacyPath, files[i]);
                            fillRecord(name, record, postfix, path);
                            if (record.getDruation() > 0) {
                                records.add(record);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static String getPostfix(String name) {
        String postfix = ".3gpp";
        if (!TextUtils.isEmpty(name) && name.endsWith(".amr")) {
            postfix = ".amr";
        }
        return postfix;
    }
    
    private static String handlePrivacy(boolean isPrivacyPath, File f) {
//      if (isPrivacyPath && !name.contains(postfix)) {
//      boolean change = ContactsUtils.auroraChangeFile(files[i].getPath());
//      Log.i(TAG, "files[i].getPath():" + files[i].getPath() + "  change:" + change);
//      if (!change) {
//          continue;
//      } else {
//          name = new String(Base64.decode(name, Base64.URL_SAFE), "UTF-8");
//          try {
//              boolean rename = files[i].renameTo(new File(path, name));
//              Log.i(TAG, "rename:" + rename + "  path:" + path + "  name:" + name);
//          } catch (Exception ex) {
//              ex.printStackTrace();
//          }
//      }
//  }
        return "";
    }
    
    private static void fillRecord(String name, PhoneCallRecord record,
            String postfix, String path) {
        if (name != null) {
            if (name.length() > 20) {
               String startTime = name.substring(0, 13);
                if (!TextUtils.isEmpty(startTime)) {
                    long endTime = 0;
                    long durationTime = 0;
                    try {
                        int durEnd = (name.substring(15, name.length())).indexOf("_");
                        durEnd += 15;
                        String  duration = name.substring(14, durEnd);
                        if (!TextUtils.isEmpty(duration)) {
                            durationTime = Long.valueOf(duration);
                            endTime = Long.valueOf(startTime) + durationTime;
                            record.setPath(path + "/" + name);
                            record.setEndTime(endTime);
                            record.setDruation(durationTime);
                            record.setMimeType("audio/amr");          
                            Log.d(TAG, "name = " + name + "  startTime = " + startTime + " duration = " + duration);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}