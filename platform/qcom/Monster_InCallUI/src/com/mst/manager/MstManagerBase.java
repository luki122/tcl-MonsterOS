/*
 * Copyright (c) 2013 The Linux Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of The Linux Foundation, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mst.manager;

import com.android.incallui.Call;
import com.android.incallui.CallList;
import com.android.incallui.InCallPresenter;
import com.android.incallui.InCallPresenter.InCallState;
import com.android.incallui.InCallPresenter.InCallStateListener;
import com.android.incallui.InCallPresenter.IncomingCallListener;

import android.telecom.DisconnectCause;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.os.SystemVibrator;
import android.util.Log;
import android.provider.Settings;

public class MstManagerBase implements InCallStateListener, IncomingCallListener, CallList.Listener{
	private static final String LOG_TAG = "MstManagerBase";

	protected Context mContext;
	protected CallList mCallList = null;

	protected InCallState mPreviousCallState = InCallState.NO_CALLS;
	protected Handler mHandler = new Handler();

	public void setUp(Context context) {
		mContext = context;
		mCallList = CallList.getInstance();
		mCallList.addListener(this);
		InCallPresenter.getInstance().addListener(this);
		InCallPresenter.getInstance().addIncomingCallListener(this);
	}

	public void tearDown() {
		mContext = null;
		if (mCallList != null) {
			mCallList.removeListener(this);
			mCallList = null;
		}
		InCallPresenter.getInstance().removeListener(this);
		InCallPresenter.getInstance().removeIncomingCallListener(this);
	}

	@Override
	public void onStateChange(InCallState oldState, InCallState newState,
			CallList callList) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void onIncomingCall(InCallState oldState, InCallState newState,
            Call call) {
        // TODO Auto-generated method stub
        
    }

	@Override
	public void onIncomingCall(Call call) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpgradeToVideo(Call call) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCallListChange(CallList callList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisconnect(Call call) {
		// TODO Auto-generated method stub
		
	}

}
