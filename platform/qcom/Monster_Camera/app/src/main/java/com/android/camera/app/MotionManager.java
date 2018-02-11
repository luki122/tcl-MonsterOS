/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera.app;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.android.camera.debug.Log;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;

import java.util.LinkedList;

public class MotionManager implements SensorEventListener {

    private static final Log.Tag TAG = new Log.Tag("MotionManager");

    private boolean mEnabled;
    private boolean mChecking;

    private SensorManager mSensorManager;
    private Sensor mRVSensor;

    // The device's orientation based on the rotation matrix. All three angles are in radians and
    // positive counter-clockwise direction.
    private float[] mRadians = new float[3];
    // The rotation around the -Z axis.
    private double mAzimuth;
    // The rotation around the -X axis.
    private double mPitch;
    // The rotation around the -Y axis.
    private double mRoll;

    private final float NS2MS = 1.0f / 1000000.0f;
    private final float INTERVAL = 100f;
    private float mTimestamp;
    private final double THRESHOLD = 30d;
    private int mExceedNum;


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event == null || event.sensor.getType() != Sensor.TYPE_ROTATION_VECTOR) {
            return;
        }

        // Check very 100ms.
        if ((event.timestamp - mTimestamp) * NS2MS > INTERVAL) {
            // The rotation matrix converted from the rotation vector, the length should be 9 or 16.
            float[] mTempMatrix = new float[9];
            float[] mTempRadians = new float[3];

            SensorManager.getRotationMatrixFromVector(mTempMatrix, event.values);
            SensorManager.getOrientation(mTempMatrix, mTempRadians);

            if (isChecking()) {
                if (mRadians == null) {
                    recordOrientation(mTempRadians);
                }

                if (checkOrientation(mTempRadians)) {
                    mExceedNum++;
                } else {
                    mExceedNum = 0;
                }

                if (mExceedNum > 2 && mListeners.size() > 0) {
                    for (MotionListener listener : mListeners) {
                        listener.onMoving();
                    }
                    mExceedNum = 0;
                }
            }

            mTimestamp = event.timestamp;
        }
    }

    private void recordOrientation(float[] current) {
        mRadians = current;
        mAzimuth = Math.toDegrees(mRadians[0]);
        mPitch = Math.toDegrees(mRadians[1]);
        mRoll = Math.toDegrees(mRadians[2]);
    }

    private boolean checkOrientation(float[] current) {
        if (mRadians == null || current == null || current.length != mRadians.length) {
            return false;
        }

        double azimuth = Math.toDegrees(current[0]);
        double pitch = Math.toDegrees(current[1]);
        double roll = Math.toDegrees(current[2]);

        return (Math.abs(mAzimuth - azimuth) > THRESHOLD ||
                Math.abs(mPitch - pitch) > THRESHOLD ||
                Math.abs(mRoll - roll) > THRESHOLD);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public static interface MotionListener {
        public void onMoving();
    }

    private final LinkedList<MotionListener> mListeners =
        new LinkedList<MotionListener>();

    public MotionManager(Context context) {
        mEnabled = CustomUtil.getInstance().getBoolean(
                CustomFields.DEF_CAMERA_SUPPORT_CANCEL_FOCUS_AFTER_MOVING, false);
        mSensorManager = (SensorManager) (context.getSystemService(Context.SENSOR_SERVICE));
        mRVSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    public void addListener(MotionListener listener) {
        if (listener == null) {
            Log.e(TAG, "MotionListener can't be null, ignore.");
            return;
        }

        if (mListeners.contains(listener)) {
            Log.e(TAG, "MotionListener has been added.");
            return;
        }

        mListeners.add(listener);
    }

    public void removeListener(MotionListener listener) {
        if (listener == null) {
            Log.e(TAG, "MotionListener can't be null, ignore.");
            return;
        }

        if (!mListeners.contains(listener)) {
            Log.e(TAG, "MotionListener is not added.");
            return;
        }

        mListeners.remove(listener);
    }

    public void reset() {
        mExceedNum = 0;
        mRadians = null;
        mAzimuth = 0d;
        mPitch = 0d;
        mRoll = 0d;
    }

    public void start() {
        if (!isEnabled()) {
            return;
        }

        if (mRVSensor == null) {
            return;
        }

        Log.d(TAG, "register rv sensor listener.");
        mSensorManager.registerListener(this, mRVSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stop() {
        if (!isEnabled()) {
            return;
        }

        if (mRVSensor == null) {
            return;
        }

        Log.d(TAG, "unregister rv sensor listener.");
        mSensorManager.unregisterListener(this, mRVSensor);
        stopChecking();
    }

    public void startChecking() {
        reset();
        mChecking = true;
    }

    public void stopChecking() {
        mChecking = false;
        reset();
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public boolean isChecking() {
        return mChecking;
    }
}
