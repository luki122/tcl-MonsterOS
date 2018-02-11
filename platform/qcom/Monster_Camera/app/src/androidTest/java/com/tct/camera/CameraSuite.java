/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.camera;

import com.tct.camera.testcases.AutoBackTest;
import com.tct.camera.testcases.AutoFrontTest;
import com.tct.camera.testcases.LastTest;
import com.tct.camera.testcases.ManualTest;
import com.tct.camera.testcases.MicroVideoBackTest;
import com.tct.camera.testcases.MicroVideoFrontTest;
import com.tct.camera.testcases.PanoramaTest;
import com.tct.camera.testcases.SettingsTest;
import com.tct.camera.testcases.SlomotionTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Created by wenhua.tu on 7/29/16.
 */
public class CameraSuite extends TestSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(AutoBackTest.class);
        suite.addTestSuite(AutoFrontTest.class);
        suite.addTestSuite(PanoramaTest.class);
        suite.addTestSuite(ManualTest.class);
        suite.addTestSuite(SlomotionTest.class);
        suite.addTestSuite(MicroVideoBackTest.class);
        suite.addTestSuite(MicroVideoFrontTest.class);
//        suite.addTestSuite(SettingsTest.class);
        suite.addTestSuite(LastTest.class);
        return suite;
    }
}
