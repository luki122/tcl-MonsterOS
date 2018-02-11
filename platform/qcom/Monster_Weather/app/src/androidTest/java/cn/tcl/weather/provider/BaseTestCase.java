/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.provider;

import android.os.Handler;
import android.test.ActivityInstrumentationTestCase2;

import cn.tcl.weather.MainActivity;
import cn.tcl.weather.TestUtils;
import cn.tcl.weather.utils.ThreadHandler;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 */
public class BaseTestCase extends ActivityInstrumentationTestCase2<MainActivity> {

    protected ProviderDataService mDefaultService;
    private Handler mHandler;
    protected TestUtils.Lock lock = new TestUtils.Lock();
    private ThreadHandler mThreadHandler;

    public BaseTestCase() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        lock.lockWait();
        try {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mHandler = new Handler();
                    lock.lockNotify();
                }
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        mThreadHandler = new ThreadHandler("TclUpdateService", android.os.Process.THREAD_PRIORITY_FOREGROUND);
        mThreadHandler.init();
        mDefaultService = new ProviderDataService(getActivity(), mHandler, mThreadHandler);
        mDefaultService.init();
    }

    @Override
    protected void tearDown() throws Exception {
        mThreadHandler.recycle();
        mDefaultService.recycle();
        super.tearDown();
    }

}
