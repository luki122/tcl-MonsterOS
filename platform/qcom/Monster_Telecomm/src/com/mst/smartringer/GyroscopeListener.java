/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.mst.smartringer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * This class is used to listen to the accelerometer to monitor the
 * orientation of the phone. The client of this class is notified when
 * the orientation changes between horizontal and vertical.
 */
public final class GyroscopeListener {
    private static final String TAG = "GyroscopeListener";
    private static final boolean DEBUG = true;
    private static final boolean VDEBUG = false;

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private Listener mListener;
    private boolean mSensorFlg = false;

    private static final int LOWER_THE_RINGER = 1;
    
    public interface Listener {
        public void onAction();
    }

    public GyroscopeListener(Context context, Listener listener) {
        mListener = listener;
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);        
        mProximitySensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		if (mProximitySensor != null) {
			mProximityThreshold = Math.min(
					mProximitySensor.getMaximumRange(), 5.0f);
		}
    }

    public void enable(boolean enable) {
        if (DEBUG) Log.d(TAG, "enable(" + enable + ")");
        synchronized (this) {
            if (enable) {
            	  if (!mSensorFlg) {
	                mSensorManager.registerListener(mSensorListener, mSensor,
	                        SensorManager.SENSOR_DELAY_GAME);
	                mSensorManager.registerListener(
							mProSensorEventListener, mProximitySensor,
							SensorManager.SENSOR_DELAY_NORMAL);
	                mSensorFlg= true;
            	  }
            } else {
            	 if (mSensorFlg) {
	                mSensorManager.unregisterListener(mSensorListener);
	                mSensorManager.unregisterListener(mProSensorEventListener);
	                mHandler.removeMessages(LOWER_THE_RINGER);
	                mSensorFlg = false;
            	 }
            }
        }
    }
    
	private Sensor mProximitySensor;
	private float mProximityThreshold = 5.0f;
	private volatile boolean mIsProximityOn = false;
	private final SensorEventListener mProSensorEventListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			final float distance = event.values[0];
			boolean positive = distance >= 0.0f
					&& distance < mProximityThreshold;
			mIsProximityOn = positive;
			Log.i(TAG, " proximity onSensorChanged positive = " + positive);			

		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}
	};

    private SensorEventListener mSensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            onSensorEvent(event.values[0], event.values[1], event.values[2]);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    
    private static final double mThreshold = 2.0; 
    private void onSensorEvent(double x, double y, double z) {
        if (VDEBUG) Log.d(TAG, "onSensorEvent(" + x + ", " + y + ", " + z + ")");

        if (x == 0.0 || y == 0.0 || z == 0.0) return;

        if (x >= mThreshold || x <= -mThreshold || y >= mThreshold || y <= -mThreshold || z >= mThreshold || z <= -mThreshold) {
        	if(!mIsProximityOn) {
              mHandler.sendEmptyMessage(LOWER_THE_RINGER);
        	}
        } 
    }

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case LOWER_THE_RINGER:
                synchronized (this) {
                	mListener.onAction();
                    enable(false);
                }
                break;
            }
        }
    };
}
