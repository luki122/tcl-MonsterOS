package com.monster.paymentsecurity.scan;

//import android.content.Context;
//import android.util.Log;
//
//import com.monster.paymentsecurity.BuildConfig;
//import com.monster.paymentsecurity.MyApplication;
//import com.monster.paymentsecurity.scan.utils.DefaultConfig;
//import com.monster.paymentsecurity.scan.wifi.WifiStateTask;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.robolectric.RobolectricGradleTestRunner;
//import org.robolectric.RobolectricTestRunner;
//import org.robolectric.RuntimeEnvironment;
//import org.robolectric.annotation.Config;

/**
 * Created by logic on 16-11-23.
 */
//
//@RunWith(RobolectricGradleTestRunner.class)
//@Config( constants = BuildConfig.class, sdk = DefaultConfig.EMULATE_SDK)
//public class ScanningEngineTest{
//
//    private ScanningEngine engine;
//    private Context mContext;
//
//    @Before
//    public void setUp(){
//        mContext = RuntimeEnvironment.application;
//        engine = ScanningEngine.getInstance(mContext);
//        engine.init();
//    }
//
//    @Test
//    public void testScanSingleTask(){
//        BaseScanTask task = new WifiStateTask(mContext);
//        engine.startScanning(new ScanningEngine.ScanningResultObserver() {
//            @Override
//            public void notifyScanningResult(Result result, float progress) {
//
//            }
//
//            @Override
//            public void notifyScanningState(@ScanningEngine.ScanningState int state) {
//
//            }
//        }, task);
//    }
//
//    @After
//    public void shutDown(){
//        engine.stopScanning();
//    }
//
//}
