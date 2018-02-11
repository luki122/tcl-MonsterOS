package com.mst.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import com.android.incallui.Call;
import com.android.incallui.CallList;
import com.android.incallui.InCallApp;
import com.android.incallui.InCallPresenter;
import com.android.incallui.InCallPresenter.InCallState;
import com.mst.utils.SettingUtils;


public class AntiTouchManager extends MstManagerBase {
	private static final String LOG_TAG = "AntiTouchManager";

	private static AntiTouchManager sAntiTouchManager;
	
	private SensorManager mSensorMgr;
	private boolean mProximitySensorFlg = false;
	private Sensor mProximitySensor;
	private float mProximityThreshold = 5.0f;
	public static volatile boolean mIsTouchEnable = true;
    public static volatile boolean mIsProximityOn = false;

	public AntiTouchManager() {
		mSensorMgr = (SensorManager) InCallApp.getInstance().getSystemService(Context.SENSOR_SERVICE); 

	}
	
	public static synchronized AntiTouchManager getInstance() {
		if (sAntiTouchManager == null) {
			sAntiTouchManager = new AntiTouchManager();
		}
		return sAntiTouchManager;
	}
	
	public void tearDown() {
        super.tearDown();

        if (mProximitySensorFlg) {
            log("updateState: mProSensorEventListener unregisterListener");
            mSensorMgr.unregisterListener(mProSensorEventListener);
            mProximitySensorFlg = false;
            mIsTouchEnable = true;
        }
    
	}

	private void handleProSensor() {
		if (canWork()) {
			if (!mProximitySensorFlg) {
				if (mProximitySensor == null) {
					mProximitySensor = mSensorMgr
							.getDefaultSensor(Sensor.TYPE_PROXIMITY);
					if (mProximitySensor != null) {
						mProximityThreshold = Math.min(
								mProximitySensor.getMaximumRange(), 5.0f);
					}
				}
				if (mProximitySensor != null) {
					boolean result = mSensorMgr.registerListener(
							mProSensorEventListener, mProximitySensor,
							SensorManager.SENSOR_DELAY_NORMAL);
					log("updateState: mProSensorEventListener registerListener result ="
							+ result);
					mProximitySensorFlg = true;
				}
			}
		} else {
			if (mProximitySensorFlg) {
				log("updateState: mProSensorEventListener unregisterListener");
				mSensorMgr.unregisterListener(mProSensorEventListener);
				mProximitySensorFlg = false;
				mIsTouchEnable = true;
			}
		}
	}

	private boolean canWork() {
		boolean touchSwitch = isAntiTouchSwitchOn();
		log("onSensorChanged: touchSwitch =" + touchSwitch);
		InCallState state = InCallPresenter.getInstance().getInCallState();
		return state == InCallState.INCOMING && touchSwitch;		
	}
	
	public void reset() {
		if (mProximitySensorFlg) {
			log("updateState: mProSensorEventListener unregisterListener");
			mSensorMgr.unregisterListener(mProSensorEventListener);
			mProximitySensorFlg = false;
			mIsTouchEnable = true;
		}
	}
	
    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
    
    public boolean isAntiTouchSwitchOn(){    	
    	return SettingUtils.getSetting(InCallApp.getInstance(), "touch");
    }
    
	private final SensorEventListener mProSensorEventListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			final float distance = event.values[0];
			boolean positive = distance >= 0.0f
					&& distance < mProximityThreshold;
			mIsProximityOn = positive;
			Log.i(LOG_TAG, " proximity onSensorChanged positive = " + positive
					+ " distance = " + distance);		

			if (InCallPresenter.getInstance().getInCallState() == InCallState.INCOMING) {
				mIsTouchEnable = !positive;
			} else {
				mIsTouchEnable = true;
			}		

		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}
	};
    
	@Override
	public void onStateChange(InCallState oldState, InCallState newState,
			CallList callList) {
		handleProSensor();

	}
	
    @Override
    public void onIncomingCall(InCallState oldState, InCallState newState,
            Call call) {
        // TODO Auto-generated method stub
        handleProSensor();
    }
    
}