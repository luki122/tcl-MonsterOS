package com.mst.privacy;

import java.io.File;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;

import com.android.providers.contacts.R;
import com.android.providers.contacts.ContactsProvidersApplication;
import com.monster.privacymanage.service.IPrivacyManageService;

public class PrivacyUtils {
	public static final boolean mIsPrivacySupport = true;
    private static final String TAG = "ContactsProvider_PrivacyUtils";
	public static boolean mIsPrivacyMode = false;
    public static boolean mIsServiceConnected = false;
    private static final String SERVICE_ACTION = "com.privacymanage.service.IPrivacyManageService";
    private static final Intent intent = new Intent(SERVICE_ACTION);
    
    private static IPrivacyManageService mPrivacyManSer;
    public static long mCurrentAccountId = 0;
    public static String mCurrentAccountHomePath = null;
    public static int mPrivacyContactsNum = 0;    
    
    private static void logs(String str) {
        Log.i(TAG, str);
    }
    
    private static ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            logs("onServiceConnected");
            mIsPrivacyMode = true;
            mIsServiceConnected = true;
            mPrivacyManSer = IPrivacyManageService.Stub.asInterface(service);
            initCurrentAccountId();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            logs("onServiceDisconnected");
            mIsServiceConnected = false;
            mPrivacyManSer = null;
            mCurrentAccountId = 0;
            mCurrentAccountHomePath = null;
        }
    };
	
	public static void bindService(Context context) {
		if (!mIsServiceConnected && ContactsProvidersApplication.sIsMstPrivacySupport) {
		    try {
		        boolean c = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE|Context.BIND_IMPORTANT);
	            mIsPrivacyMode = true;
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		}
	}
	
	public static void unbindService(Context context) {
        if (mIsServiceConnected) {
            try {
                context.unbindService(serviceConnection);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private static void initCurrentAccountId() {
        try {
            if (mPrivacyManSer != null) {
                mCurrentAccountId = 
                        mPrivacyManSer.getCurrentAccount(
                                "com.android.contacts", 
                                "com.android.contacts.activity.AuroraPrivacyContactListActivity")
                                .getAccountId();
                mCurrentAccountHomePath = 
                        mPrivacyManSer.getCurrentAccount(
                                "com.android.contacts", 
                                "com.android.contacts.activity.AuroraPrivacyContactListActivity")
                                .getHomePath();
                logs("ContactsProvider initCurrentAccountId mCurrentAccountId = " + mCurrentAccountId + "  mCurrentAccountHomePath = " + mCurrentAccountHomePath);
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException se) {
            se.printStackTrace();
        }
        
    }
    
    public static long getCurrentAccountId() {
        return mCurrentAccountId;
    }
	
    public static void setPrivacyNum(Context context, final String calssName, final int number, final long accountId) {
        try {
            if (mPrivacyManSer == null) {
                if (!mIsServiceConnected) {
                    context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE|Context.BIND_IMPORTANT);
                }
            }
            
            new Thread(new Runnable() {
                
                @Override
                public void run() {
                    int i = 0;
                    while(true) {
                        if (mIsServiceConnected) {
                            break;
                        }
                        
                        try {
                            Thread.sleep(10);
                            i++;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        
                        if (i > 15) {
                            break;
                        }
                    }
                    
                    if (mPrivacyManSer != null && mIsServiceConnected) {
                        try {
                            logs("setPrivacyNum calssName = " + calssName +  "  number = " + number + "  accountId = " + accountId);
                            mPrivacyManSer.setPrivacyNum("com.android.contacts", calssName, number, accountId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * ContactsProvidersApplication.getInstance().getApplicationContext() maybe is null
     * please use resetPrivacyNumOfAllAccount(final Context context, final String pkgName, final String className) instead
     */
    @Deprecated
    public static void resetPrivacyNumOfAllAccount(final String pkgName, final String className) {
    	resetPrivacyNumOfAllAccount(ContactsProvidersApplication.getInstance().getApplicationContext(), pkgName, className);
    }
    
    public static void resetPrivacyNumOfAllAccount(final Context context, final String pkgName, final String className) {
        try {
            if (mPrivacyManSer == null) {
                if (!mIsServiceConnected) {
                	if(ContactsProvidersApplication.getInstance() != null) {
                		ContactsProvidersApplication.getInstance().bindService(
                                    intent,
                                    serviceConnection,
                                    Context.BIND_AUTO_CREATE
                                            | Context.BIND_IMPORTANT);
                	}
                    
                    new Thread(new Runnable() {
                        
                        @Override
                        public void run() {
                            int i = 0;
                            while(true) {
                                if (mIsServiceConnected) {
                                    break;
                                }
                                
                                try {
                                    Thread.sleep(10);
                                    i++;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                
                                if (i > 15) {
                                    break;
                                }
                            }
                            
                            logs("mIsServiceConnected = " + mIsServiceConnected);
                            if (mPrivacyManSer != null && mIsServiceConnected) {
                                try {
                                    mPrivacyManSer.resetPrivacyNumOfAllAccount(pkgName, className);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
                }
            } else {
                mPrivacyManSer.resetPrivacyNumOfAllAccount(pkgName, className);
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException se) {
            se.printStackTrace();
        }
    }
    
    //----------------------------------------------------
    public static void sendBroadToMms(String number, int type, boolean isMan) {
        if (number == null) {
            return;
        }
        
        Intent intent = new Intent("com.aurora.privacy.contact.UPDATE");
        long currentAccountId = PrivacyUtils.mCurrentAccountId;
        logs("type = " + type + "  currentAccountId = " + currentAccountId);
        long oldValue = 0;
        long newValue = 0;
        if (type > 0) {
            oldValue = 0;
            newValue = currentAccountId;
            if (isMan) {
                oldValue = -1;
            }
        } else if (type == 0) {
            oldValue = currentAccountId;
            newValue = 0;
        } else if (type == -1) {
            oldValue = currentAccountId;
            newValue = -1;
        }
        
        ContentValues values = new ContentValues();
        values.put("old_account_id", oldValue);
        values.put("new_account_id", newValue);
        values.put("privacy_number", number);
        values.put("feature_name", "privacy");
        logs("old = " + oldValue + "  new = " + newValue + "  number = " + number);
        ContactsProvidersApplication.getInstance().getApplicationContext().getContentResolver().update(Uri.parse("content://mms-sms/aurora_special_feature"), values, null, null);
        values.clear();
        values = null;
    }
    
    public static void updateCallRecordings(String number, int type) {
        logs("number = " + number + "  type = " + type);
        if (number == null) {
            return;
        }
        
        String privacyCallRecodingEncodePath = PrivacyUtils.mCurrentAccountHomePath
                + Base64.encodeToString(("audio").getBytes(), Base64.URL_SAFE);
        privacyCallRecodingEncodePath = replaceBlank(privacyCallRecodingEncodePath);
        File audio = new File(privacyCallRecodingEncodePath);
        logs("audio = " + audio);
        if (audio == null || !audio.exists()) {
            return;
        }
        
        if (type == 0) { // move to normal area
            auroraMoveToNormal(privacyCallRecodingEncodePath, audio, number);
            
        } else if (type > 0) { // move to privacy area
            auroraMoveToPrivacy(privacyCallRecodingEncodePath, number);
            
        } else if (type == -1) { // delete
            auroraDeleteRecods(privacyCallRecodingEncodePath, audio, number);
        }
    }
    
    private static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            dest = str.replace("\n", "");
        }
        return dest;
    }
    
    private static void auroraMoveToPrivacy(String privacyCallRecodingEncodePath, String number) {
        String path = "sdcard";
        if (path == null) {
            return;
        }
        
        String historyPath = path + "/" + ContactsProvidersApplication.getInstance().getApplicationContext().getString(R.string.mst_call_record_history_path);
        int found = 0;
        try {
            File file = new File(historyPath);
            if (file.isDirectory()) {
                File[] filesArr = file.listFiles();
                if (filesArr != null) {
                    int fileLen = filesArr.length;
                    if (fileLen > 0) {
                        for (int i = 0; i < fileLen; i++) {
                            String name = filesArr[i].getName();
                            if (name != null && name.length() > 20) {
                                int durEnd = (name.substring(15, name.length())).indexOf("_");
                                durEnd += 15;
                                String num = name.substring(durEnd + 1,  name.indexOf(".amr"));
                                logs("num = " + num);
                                // found
                                if (num != null && num.equals(number)) {
                                    boolean success = filesArr[i].renameTo(new File(privacyCallRecodingEncodePath, name));
                                    logs("move to privacy success = " + success);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void auroraMoveToNormal(String audioPath, File audioFile, String number) {
        File[] records = audioFile.listFiles();
        logs("auroraMoveToNormal  audioPath = " + audioPath + "   records = " + records);
        if (records != null) {
            String recordName = null;
            boolean check = false;
            for (int i = 0; i < records.length; i++) {
                check = false;
                recordName = records[i].getName();
                logs("records[" + i + "] = " + recordName);
                if (recordName.contains(".amr")) { // no coding
                    check = true;
                } else {
                    try {
                        synchronized(ContactsProvidersApplication.getInstance().getApplicationContext()) {
                            boolean change = auroraChangeFile(audioPath + recordName);
                            if (change) {
                                check = true;
                                recordName = new String(Base64.decode(records[i].getName(), Base64.URL_SAFE), "UTF-8");
                                logs("decode recordName = " + recordName);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                if (recordName != null && check && recordName.contains(".amr")) {
                    if (recordName.length() > 20) {
                        int durEnd = (recordName.substring(15, recordName.length())).indexOf("_");
                        durEnd += 15;
                        String num = recordName.substring(durEnd + 1,  recordName.indexOf(".amr"));
                        // found, move to normal area
                        if (num != null && num.equals(number)) {
                            String path = "sdcard";
                            if (path == null) {
                                return;
                            }
                            
                            String historyPath = path + "/" + ContactsProvidersApplication.getInstance().getApplicationContext().getString(R.string.mst_call_record_history_path);
                            File old = new File(audioPath, recordName);
                            boolean success = old.renameTo(new File(historyPath, recordName));
                            logs("move to normal success = " + success);
                        }
                    }
                }
            }
        }
    }
    
    private static void auroraDeleteRecods(String audioPath, File audioFile, String number) {
        File[] records = audioFile.listFiles();
        logs("auroraDeleteRecods  audioPath = " + audioPath + "   records = " + records);
        if (records != null) {
            String recordName = null;
            boolean check = false;
            for (int i = 0; i < records.length; i++) {
                check = false;
                recordName = records[i].getName();
                logs("records[" + i + "] = " + recordName);
                if (recordName.contains(".amr")) { // no coding
                    check = true;
                } else {
                    try {
                        boolean change = auroraChangeFile(audioPath + recordName);
                        if (change) {
                            check = true;
                            recordName = new String(Base64.decode(records[i].getName(), Base64.URL_SAFE), "UTF-8");
                            logs("decode recordName = " + recordName);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                if (recordName != null && check && recordName.contains(".amr")) {
                    if (recordName.length() > 20) {
                        int durEnd = (recordName.substring(15, recordName.length())).indexOf("_");
                        durEnd += 15;
                        String num = recordName.substring(durEnd + 1,  recordName.indexOf(".amr"));
                        // found, delete
                        if (num != null && num.equals(number)) {
                            File old = new File(audioPath, recordName);
                            boolean success = old.delete();
                            logs("delete call record success = " + success);
                        }
                    }
                }
            }
        }
    }
    
    // 解密
    private static boolean auroraChangeFile(String file) throws Exception {
        int len = 8;
        java.io.RandomAccessFile raf = new java.io.RandomAccessFile(file, "rw");
        java.nio.channels.FileChannel channel = raf.getChannel();
        java.nio.MappedByteBuffer buffer = channel.map(
                java.nio.channels.FileChannel.MapMode.READ_WRITE, 0, len);
        
        for (int i = 0; i < len; i++) {
            byte src = buffer.get(i);
            buffer.put(i, (byte) (src ^ 2));
        }
        buffer.force();
        buffer.clear();
        channel.close();
        raf.close();
        return true;
    }
}