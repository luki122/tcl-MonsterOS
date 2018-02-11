/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.camera.testcases;


import com.tct.camera.utils.CommonUtil;

public class LastTest extends TestCase {

    public LastTest() {
        mCurrentTestCaseIndex = CommonUtil.LAST_TEST;
    }
    public void testFinishAll() {
        setTestFinished(true);
    }
}
