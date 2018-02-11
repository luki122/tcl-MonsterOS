/*
 * Copyright (C) 2013 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.incallui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telecom.VideoProfile;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class GlowPadAnswerFragment extends AnswerFragmentMst {

    private GlowPadWrapper mGlowpad;

    public GlowPadAnswerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
//        mGlowpad = (GlowPadWrapper) inflater.inflate(R.layout.answer_fragment,
//                container, false);
        final View parent = inflater.inflate(R.layout.answer_fragment_mst, container, false);      
        mGlowpad = (GlowPadWrapper) parent.findViewById(R.id.glow_pad_view);

        Log.d(this, "Creating view for answer fragment ", this);
        Log.d(this, "Created from activity", getActivity());
        mGlowpad.setAnswerFragment(this);
        
        mCallWaitingContainer = (ViewGroup) parent.findViewById(R.id.call_waiting_line);
        mHold = (Button)mCallWaitingContainer.findViewById(R.id.answer_callwaiting_hold);
        mHold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	 getPresenter().onAnswer(VideoProfile.STATE_AUDIO_ONLY, getContext());
            }
        });
        mAnswer = (Button)mCallWaitingContainer.findViewById(R.id.answer_callwaiting_hangup);
        mAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	getPresenter().onDeclineForeground(getContext());
            }
        });
        mReject = (Button)mCallWaitingContainer.findViewById(R.id.answer_callwaiting_reject);
        mReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPresenter().onDecline(getContext());
            }
        });

//        return mGlowpad;
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        getActivity().registerReceiver(mScreenReceiver, filter);
        return parent;
    }

    @Override
    public void onResume() {
        super.onResume();
        mGlowpad.requestFocus();
    }

    @Override
    public void onDestroyView() {
        Log.d(this, "onDestroyView");
        if (mGlowpad != null) {
            mGlowpad.stopPing();
            mGlowpad = null;
        }
        getActivity().unregisterReceiver(mScreenReceiver);
        super.onDestroyView();
    }

    @Override
    public void onShowAnswerUi(boolean shown) {
        Log.d(this, "Show answer UI: " + shown);
        setCallWaiting();
        if (shown) {
            mGlowpad.startPing();
        } else {
            mGlowpad.stopPing();
        }
    }

    /**
     * Sets targets on the glowpad according to target set identified by the parameter.
     *
     * @param targetSet Integer identifying the set of targets to use.
     */
    public void showTargets(int targetSet) {
        showTargets(targetSet, VideoProfile.STATE_BIDIRECTIONAL);
    }

    /**
     * Sets targets on the glowpad according to target set identified by the parameter.
     *
     * @param targetSet Integer identifying the set of targets to use.
     */
    @Override
    public void showTargets(int targetSet, int videoState) {
//        final int targetResourceId;
//        final int targetDescriptionsResourceId;
//        final int directionDescriptionsResourceId;
//        final int handleDrawableResourceId;
//        mGlowpad.setVideoState(videoState);
//
//        switch (targetSet) {
//            case TARGET_SET_FOR_AUDIO_WITH_SMS:
//                targetResourceId = R.array.incoming_call_widget_audio_with_sms_targets;
//                targetDescriptionsResourceId =
//                        R.array.incoming_call_widget_audio_with_sms_target_descriptions;
//                directionDescriptionsResourceId =
//                        R.array.incoming_call_widget_audio_with_sms_direction_descriptions;
//                handleDrawableResourceId = R.drawable.ic_incall_audio_handle;
//                break;
//            case TARGET_SET_FOR_VIDEO_WITHOUT_SMS:
//                targetResourceId = R.array.incoming_call_widget_video_without_sms_targets;
//                targetDescriptionsResourceId =
//                        R.array.incoming_call_widget_video_without_sms_target_descriptions;
//                directionDescriptionsResourceId =
//                        R.array.incoming_call_widget_video_without_sms_direction_descriptions;
//                handleDrawableResourceId = R.drawable.ic_incall_video_handle;
//                break;
//            case TARGET_SET_FOR_VIDEO_WITH_SMS:
//                targetResourceId = R.array.incoming_call_widget_video_with_sms_targets;
//                targetDescriptionsResourceId =
//                        R.array.incoming_call_widget_video_with_sms_target_descriptions;
//                directionDescriptionsResourceId =
//                        R.array.incoming_call_widget_video_with_sms_direction_descriptions;
//                handleDrawableResourceId = R.drawable.ic_incall_video_handle;
//                break;
//            case TARGET_SET_FOR_VIDEO_ACCEPT_REJECT_REQUEST:
//                targetResourceId =
//                        R.array.incoming_call_widget_video_request_targets;
//                targetDescriptionsResourceId =
//                        R.array.incoming_call_widget_video_request_target_descriptions;
//                directionDescriptionsResourceId = R.array
//                        .incoming_call_widget_video_request_target_direction_descriptions;
//                handleDrawableResourceId = R.drawable.ic_incall_video_handle;
//                break;
//            case TARGET_SET_FOR_QTI_VIDEO_WITHOUT_SMS:
//                targetResourceId = R.array.qti_incoming_call_widget_video_without_sms_targets;
//                targetDescriptionsResourceId =
//                        R.array.qti_incoming_call_widget_video_without_sms_target_descriptions;
//                directionDescriptionsResourceId =
//                        R.array.qti_incoming_call_widget_video_without_sms_direction_descriptions;
//                handleDrawableResourceId = R.drawable.ic_incall_video_handle;
//                break;
//            case TARGET_SET_FOR_QTI_VIDEO_WITH_SMS:
//                targetResourceId = R.array.qti_incoming_call_widget_video_with_sms_targets;
//                targetDescriptionsResourceId =
//                        R.array.qti_incoming_call_widget_video_with_sms_target_descriptions;
//                directionDescriptionsResourceId =
//                        R.array.qti_incoming_call_widget_video_with_sms_direction_descriptions;
//                handleDrawableResourceId = R.drawable.ic_incall_video_handle;
//                break;
//            case TARGET_SET_FOR_QTI_VIDEO_TRANSMIT_ACCEPT_REJECT_WITHOUT_SMS:
//                targetResourceId =
//                    R.array.qti_incoming_call_widget_tx_video_without_sms_targets;
//                targetDescriptionsResourceId =
//                    R.array.qti_incoming_call_widget_tx_video_without_sms_target_descriptions;
//                directionDescriptionsResourceId =
//                    R.array.qti_incoming_call_widget_tx_video_without_sms_direction_descriptions;
//                handleDrawableResourceId = R.drawable.ic_incall_video_handle;
//                break;
//            case TARGET_SET_FOR_QTI_VIDEO_TRANSMIT_ACCEPT_REJECT_WITH_SMS:
//                targetResourceId =
//                    R.array.qti_incoming_call_widget_tx_video_with_sms_targets;
//                targetDescriptionsResourceId =
//                    R.array.qti_incoming_call_widget_tx_video_with_sms_target_descriptions;
//                directionDescriptionsResourceId =
//                    R.array.qti_incoming_call_widget_tx_video_with_sms_direction_descriptions;
//                handleDrawableResourceId = R.drawable.ic_incall_video_handle;
//                break;
//            case TARGET_SET_FOR_QTI_VIDEO_RECEIVE_ACCEPT_REJECT_WITHOUT_SMS:
//                targetResourceId =
//                    R.array.qti_incoming_call_widget_rx_video_without_sms_targets;
//                targetDescriptionsResourceId =
//                    R.array.qti_incoming_call_widget_rx_video_without_sms_target_descriptions;
//                directionDescriptionsResourceId =
//                    R.array.qti_incoming_call_widget_rx_video_without_sms_direction_descriptions;
//                handleDrawableResourceId = R.drawable.ic_incall_video_handle;
//                break;
//            case TARGET_SET_FOR_QTI_VIDEO_RECEIVE_ACCEPT_REJECT_WITH_SMS:
//                targetResourceId =
//                    R.array.qti_incoming_call_widget_rx_video_with_sms_targets;
//                targetDescriptionsResourceId =
//                    R.array.qti_incoming_call_widget_rx_video_with_sms_target_descriptions;
//                directionDescriptionsResourceId =
//                    R.array.qti_incoming_call_widget_rx_video_with_sms_direction_descriptions;
//                handleDrawableResourceId = R.drawable.ic_incall_video_handle;
//                break;
//            case TARGET_SET_FOR_QTI_VIDEO_ACCEPT_REJECT_REQUEST:
//                targetResourceId = R.array.qti_incoming_call_widget_video_request_targets;
//                targetDescriptionsResourceId =
//                        R.array.qti_incoming_call_widget_video_request_target_descriptions;
//                directionDescriptionsResourceId = R.array.
//                        qti_incoming_call_widget_video_request_target_direction_descriptions;
//                handleDrawableResourceId = R.drawable.ic_incall_video_handle;
//                break;
//            case TARGET_SET_FOR_QTI_BIDIRECTIONAL_VIDEO_ACCEPT_REJECT_REQUEST:
//                targetResourceId = R.array.
//                        qti_incoming_call_widget_bidirectional_video_accept_reject_request_targets;
//                targetDescriptionsResourceId =
//                        R.array.qti_incoming_call_widget_video_request_target_descriptions;
//                directionDescriptionsResourceId = R.array.
//                        qti_incoming_call_widget_video_request_target_direction_descriptions;
//                handleDrawableResourceId = R.drawable.ic_incall_video_handle;
//                break;
//            case TARGET_SET_FOR_QTI_VIDEO_TRANSMIT_ACCEPT_REJECT_REQUEST:
//                targetResourceId = R.array.
//                        qti_incoming_call_widget_video_transmit_accept_reject_request_targets;
//                targetDescriptionsResourceId = R.array.
//                        qti_incoming_call_widget_video_transmit_request_target_descriptions;
//                directionDescriptionsResourceId = R.array
//                        .qti_incoming_call_widget_video_request_target_direction_descriptions;
//                handleDrawableResourceId = R.drawable.ic_incall_video_handle;
//                break;
//            case TARGET_SET_FOR_QTI_VIDEO_RECEIVE_ACCEPT_REJECT_REQUEST:
//                targetResourceId = R.array.
//                        qti_incoming_call_widget_video_receive_accept_reject_request_targets;
//                targetDescriptionsResourceId =
//                        R.array.qti_incoming_call_widget_video_receive_request_target_descriptions;
//                directionDescriptionsResourceId = R.array
//                        .qti_incoming_call_widget_video_request_target_direction_descriptions;
//                handleDrawableResourceId = R.drawable.ic_incall_video_handle;
//                break;
//
//            case TARGET_SET_FOR_QTI_AUDIO_WITH_SMS:
//                targetResourceId = R.array.qti_incoming_call_widget_audio_with_sms_targets;
//                targetDescriptionsResourceId =
//                        R.array.qti_incoming_call_widget_audio_with_sms_target_descriptions;
//                directionDescriptionsResourceId = R.array
//                        .qti_incoming_call_widget_audio_with_sms_direction_descriptions;
//                handleDrawableResourceId = R.drawable.ic_incall_audio_handle;
//                break;
//            case TARGET_SET_FOR_QTI_AUDIO_WITHOUT_SMS:
//                targetResourceId = R.array.qti_incoming_call_widget_audio_without_sms_targets;
//                targetDescriptionsResourceId =
//                        R.array.qti_incoming_call_widget_audio_without_sms_target_descriptions;
//                directionDescriptionsResourceId = R.array
//                        .qti_incoming_call_widget_audio_without_sms_direction_descriptions;
//                handleDrawableResourceId = R.drawable.ic_incall_audio_handle;
//                break;
//
//            case TARGET_SET_FOR_AUDIO_WITHOUT_SMS:
//            default:
//                targetResourceId = R.array.incoming_call_widget_audio_without_sms_targets;
//                targetDescriptionsResourceId =
//                        R.array.incoming_call_widget_audio_without_sms_target_descriptions;
//                directionDescriptionsResourceId =
//                        R.array.incoming_call_widget_audio_without_sms_direction_descriptions;
//                handleDrawableResourceId = R.drawable.ic_incall_audio_handle;
//                break;
//        }
//
//        if (targetResourceId != mGlowpad.getTargetResourceId()) {
//            mGlowpad.setTargetResources(targetResourceId);
//            mGlowpad.setTargetDescriptionsResourceId(targetDescriptionsResourceId);
//            mGlowpad.setDirectionDescriptionsResourceId(directionDescriptionsResourceId);
//            mGlowpad.setHandleDrawable(handleDrawableResourceId);
//            mGlowpad.reset(false);
//        }
                
        final int targetResourceId;
        
        switch (targetSet) {
        case TARGET_SET_FOR_AUDIO_WITHOUT_SMS:
            targetResourceId = R.array.mst_incoming_call_widget_audio_without_sms_targets;
            break;
        case TARGET_SET_FOR_AUDIO_WITH_SMS:
        default:
            targetResourceId = R.array.mst_incoming_call_widget_audio_with_sms_targets;
            break;
        }
        
        if (targetResourceId != mGlowpad.getTargetResourceId()) {
            mGlowpad.setTargetResources(targetResourceId);
            mGlowpad.reset(false);
        }        

    }

    @Override
    protected void onMessageDialogCancel() {
        if (mGlowpad != null) {
            mGlowpad.startPing();
        }
    }
    
    public void stopAnim() {
      mGlowpad.stopAnim();
    }
    
    private ViewGroup mCallWaitingContainer;
    private Button mHold, mAnswer, mReject;
    
    
    protected void setCallWaiting() {
 	   Call activeCall = CallList.getInstance().getActiveOrBackgroundCall();
        if(activeCall != null) {
	           	mCallWaitingContainer.setVisibility(View.VISIBLE);
	           	mGlowpad.setVisibility(View.GONE);
	           	mAnswer.setVisibility(getPresenter().isShowRejctAndAnswer() ? View.VISIBLE : View.GONE);
        } else {
	           	mGlowpad.setVisibility(View.VISIBLE);
	           	mGlowpad.setAlpha(1.0f);
	           	mCallWaitingContainer.setVisibility(View.GONE);
        }
 }
    

    private ScreenBroadcastReceiver mScreenReceiver = new ScreenBroadcastReceiver();

    private class ScreenBroadcastReceiver extends BroadcastReceiver {

        private String action = null;

        @Override
        public void onReceive(Context context, Intent intent) {

            action = intent.getAction();

            if (Intent.ACTION_SCREEN_ON.equals(action)) {

            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                if(hasPendingDialogs()) {
                    dismissPendingDialogs();
                    getPresenter().onDismissDialog();
                }
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {

            }

        }
    }
    
    
    void restoreState() {
        mGlowpad.reset(false);
    }
}
