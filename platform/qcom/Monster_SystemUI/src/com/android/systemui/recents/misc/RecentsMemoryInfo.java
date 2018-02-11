package com.android.systemui.recents.misc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Debug;

public class RecentsMemoryInfo {

    // 获得可用的内存
    public static float getmem_unused(Context mContext, ActivityManager am) {
        float mem_unused;

        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);

        mem_unused = (float)mi.availMem / (float)1024;
        return mem_unused;
    }

    public static float getmem_total() {	
        float mTotal;
        String path = "/proc/meminfo";
        String content = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path), 8);
            String line;
            if ((line = br.readLine()) != null) {
                content = line;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // beginIndex
        int begin = content.indexOf(':');
        // endIndex
        int end = content.indexOf('k');

        content = content.substring(begin + 1, end).trim();
        mTotal = Float.parseFloat(content);
        return mTotal;
    }
    
    public static void getRunningAppProcessInfo(Context context) {    
    	ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);       
    	List<RunningAppProcessInfo> runningAppProcessesList = am.getRunningAppProcesses();       
    	for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcessesList) {     
    		int pid = runningAppProcessInfo.pid;         
    		int uid = runningAppProcessInfo.uid;        
    		String processName = runningAppProcessInfo.processName;          
    		int[] pids = new int[] {pid};       
    		Debug.MemoryInfo[] memoryInfo = am.getProcessMemoryInfo(pids);          
    		int memorySize = memoryInfo[0].dalvikPrivateDirty;            
    		System.out.println("processName="+processName+",pid="+pid+",uid="+uid+",memorySize="+memorySize+"kb");      
    	} 
    }
}
