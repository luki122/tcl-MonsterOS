/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.internet;

import android.location.Location;
import android.test.ActivityInstrumentationTestCase2;


import cn.tcl.weather.MainActivity;
import cn.tcl.weather.TestUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-4.
 * $desc
 */
public class GpsLocationClientTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private ILocationClient mLocationClient;
    private TestUtils.Lock mLock = new TestUtils.Lock();
    private Location mLocation;
    private boolean isLocated;

    public GpsLocationClientTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLocationClient = new GpsLocationClient(getActivity());
        mLocationClient.init();
    }


    public void test_gpsLocation() {
        isLocated = false;
        mLocationClient.regiestOnLocationClientListener(new ILocationClient.OnLocationClientListener() {
            @Override
            public void onLocated(Location location) {
                isLocated = true;
                mLocation = location;
                mLock.lockNotify();
            }

            @Override
            public void onLocating(int state) {
                if (state == ILocationClient.STATE_FAILED_NETWORK_USELESS || state == ILocationClient.STATE_FAILED_LOCATE_FAILED || state == ILocationClient.STATE_FAILED_NO_PERMISSION) {
                    isLocated = false;
                    mLock.lockNotify();
                }
            }
        });
        mLocationClient.startLocate();
        mLock.lockWait(20000);//wait 20s
        assertTrue("located failed", isLocated);
        assertNotNull("location isn't null, but is null", mLocation);
        assertNotNull("location longitude isn't null, but is null", mLocation.getLongitude());
    }


    @Override
    protected void tearDown() throws Exception {
        mLocationClient.recycle();
        super.tearDown();
    }
}
