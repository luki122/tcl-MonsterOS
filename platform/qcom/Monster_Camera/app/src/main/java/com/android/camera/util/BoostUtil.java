package com.android.camera.util;

import android.content.Context;

import com.android.camera.debug.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Created by sichao.hu on 1/28/16.
 */
public class BoostUtil {


    private static final Log.Tag TAG=new Log.Tag("BoostUtil");

    private static class BoostUtilHolder{
        private static final BoostUtil mBoostUtilHolder=new BoostUtil();
    }

    public static BoostUtil getInstance(){
        return BoostUtilHolder.mBoostUtilHolder;
    }

    Class Performance;
    Object mPerf;
    Method perfLockAcquire;
    Method perfLockRelease;

    public void acquireCpuLock() {
        try {
            Performance = Class.forName("android.util.BoostFramework");
            Class[] argClasses = new Class[] {int.class, int[].class};
            perfLockAcquire = Performance.getDeclaredMethod("perfLockAcquire", argClasses);
            perfLockRelease = Performance.getDeclaredMethod("perfLockRelease");
            if (mPerf == null) {
                Constructor c = Performance.getConstructor();
                mPerf = c.newInstance();
            }
            if (mPerf != null) {
                // duration for 4 seconds.
                // 0x20F, 0x1F0F are the values to bump the clocks to max
                // for all the CPU cores.
                perfLockAcquire.invoke(mPerf, 4 * 1000, new int []{0x20F, 0x1F0F});
            }
        } catch (Exception e) {
            Log.v(TAG, e.toString());
        }

    }

    public void releaseCpuLock() {
        try {
            if (mPerf != null) {
                perfLockRelease.invoke(mPerf);
                mPerf = null;
            }
        } catch (Exception e) {
            Log.v(TAG, e.toString());
        }

    }
}
