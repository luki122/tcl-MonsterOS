/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.camera.instantcapture;

import android.os.ConditionVariable;

public class CameraLock extends ConditionVariable {
    public static final int CAMERA_BLOCK_TIMEOUT = 1000;

    private CameraLock(boolean state) {
        super(state);
    }

    private static class SingletonHolder {
        private static final CameraLock INSTANCE = new CameraLock(true);
    }

    public static CameraLock getInstance() {
        return SingletonHolder.INSTANCE;
    }
}