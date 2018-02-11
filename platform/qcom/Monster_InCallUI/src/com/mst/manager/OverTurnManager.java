package com.mst.manager;

import com.android.incallui.Call;
import com.android.incallui.CallList;
import com.android.incallui.InCallApp;
import com.android.incallui.InCallPresenter;
import com.android.incallui.InCallPresenter.InCallState;
import com.mst.utils.SettingUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Message;
import android.telecom.TelecomManager;
import android.util.Log;

public class OverTurnManager extends MstManagerBase {
	private static final String LOG_TAG = "OverTurnManager";

	private boolean mIsOverTurn = false;
	private boolean mGravitySensorFlg = false;
	private SensorManager mSensorMgr;
	private Sensor mGravitySensor;
	private TelecomManager mTelecomManager;

	private static final float CRITICAL_DOWN_ANGLE = -5.0f;
	private static final float CRITICAL_UP_ANGLE = 5.0f;
	private static final int Z_ORATIATION = 2;
	private int mReverseDownFlg = -1;

	public OverTurnManager(Context context) {
		mContext = context;
		mSensorMgr = (SensorManager) mContext
				.getSystemService(Context.SENSOR_SERVICE);
		mGravitySensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mTelecomManager = (TelecomManager) mContext
				.getSystemService(Context.TELECOM_SERVICE);
	}
	
	   public void tearDown() {
	        super.tearDown();
	        if (true == mGravitySensorFlg) {
                mSensorMgr.unregisterListener(mSensorEventListener);
                mIsOverTurn = false;
                mGravitySensorFlg = false;
            }
	    }

	private static OverTurnManager mOverTurnManager;

	public static synchronized OverTurnManager getInstance() {
		if (mOverTurnManager == null) {
			mOverTurnManager = new OverTurnManager(InCallApp.getInstance());
		}
		return mOverTurnManager;
	}

	private final SensorEventListener mSensorEventListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			// if (InCallPresenter.getInstance().isShowingInCallUi()
			// && isSwitchOn()) {
			// Log.d(LOG_TAG,
			// "onSensorChanged()...  event.values[SensorManager.DATA_Z] = "
			// + event.values[SensorManager.DATA_Z]);

			if (event.values[SensorManager.DATA_Z] >= CRITICAL_UP_ANGLE) {
				// screen up first
				mReverseDownFlg = 0;
			} else if (event.values[SensorManager.DATA_Z] <= CRITICAL_DOWN_ANGLE
					&& mReverseDownFlg == 0) {
				// screen down next
				mReverseDownFlg = 1;
			}

			if (mReverseDownFlg == 1) {
				// screen reverse from up to down
				if (!mIsOverTurn) {
					mIsOverTurn = true;
					muteIncomingCall(true);
				}

			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}
	};

	private void muteIncomingCall(boolean mute) {
		Log.d(LOG_TAG, "muteIncomingCall");
		mTelecomManager.silenceRinger();
	}


   @Override
    public void onIncomingCall(InCallState oldState, InCallState newState,
            Call call) {
        // TODO Auto-generated method stub
          handleOverTurn();
    }

	
	@Override
	public void onStateChange(InCallState oldState, InCallState newState,
			CallList callList) {
		// TODO Auto-generated method stub
		handleOverTurn();
	}

	private void handleOverTurn() {
		InCallState state = InCallPresenter.getInstance().getInCallState();
		Log.d(LOG_TAG, "handleOverTurn state = " + state);
		if (state == InCallState.INCOMING && isSwitchOn()) {
			if (false == mGravitySensorFlg) {
				mSensorMgr.registerListener(mSensorEventListener,
						mGravitySensor, 12000);
				mGravitySensorFlg = true;
			}
		} else {
			if (true == mGravitySensorFlg) {
				mSensorMgr.unregisterListener(mSensorEventListener);
				mIsOverTurn = false;
				mGravitySensorFlg = false;
			}
		}
	}

	private boolean isSwitchOn() {	
	  	return SettingUtils.getSetting(InCallApp.getInstance(), "overturn");
	}
}
