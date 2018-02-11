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

import mst.app.dialog.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.android.dialer.R;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import mst.widget.MstListView;
import mst.view.menu.BottomWidePopupMenu;

/**
 * Provides only common interface and functions. Should be derived to implement the actual UI.
 */
public abstract class AnswerFragmentMst extends BaseFragment<AnswerPresenterMst, AnswerPresenterMst.AnswerUi>
        implements AnswerPresenterMst.AnswerUi {

    public static final int TARGET_SET_FOR_AUDIO_WITHOUT_SMS = 0;
    public static final int TARGET_SET_FOR_AUDIO_WITH_SMS = 1;
    public static final int TARGET_SET_FOR_VIDEO_WITHOUT_SMS = 2;
    public static final int TARGET_SET_FOR_VIDEO_WITH_SMS = 3;
    public static final int TARGET_SET_FOR_VIDEO_ACCEPT_REJECT_REQUEST = 4;

    public static final int TARGET_SET_FOR_QTI_VIDEO_WITHOUT_SMS = 1000;
    public static final int TARGET_SET_FOR_QTI_VIDEO_WITH_SMS = 1001;
    public static final int TARGET_SET_FOR_QTI_VIDEO_ACCEPT_REJECT_REQUEST = 1003;
    public static final int TARGET_SET_FOR_QTI_BIDIRECTIONAL_VIDEO_ACCEPT_REJECT_REQUEST = 1004;
    public static final int TARGET_SET_FOR_QTI_VIDEO_TRANSMIT_ACCEPT_REJECT_REQUEST = 1005;
    public static final int TARGET_SET_FOR_QTI_VIDEO_RECEIVE_ACCEPT_REJECT_REQUEST = 1006;
    public static final int TARGET_SET_FOR_QTI_AUDIO_WITHOUT_SMS = 1007;
    public static final int TARGET_SET_FOR_QTI_AUDIO_WITH_SMS = 1008;
    public static final int TARGET_SET_FOR_QTI_VIDEO_TRANSMIT_ACCEPT_REJECT_WITHOUT_SMS = 1009;
    public static final int TARGET_SET_FOR_QTI_VIDEO_TRANSMIT_ACCEPT_REJECT_WITH_SMS = 1010;
    public static final int TARGET_SET_FOR_QTI_VIDEO_RECEIVE_ACCEPT_REJECT_WITHOUT_SMS = 1011;
    public static final int TARGET_SET_FOR_QTI_VIDEO_RECEIVE_ACCEPT_REJECT_WITH_SMS = 1012;

    /**
     * This fragment implement no UI at all. Derived class should do it.
     */
    @Override
    public abstract View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState);

    /**
     * The popup showing the list of canned responses.
     *
     * This is an AlertDialog containing a ListView showing the possible choices.  This may be null
     * if the InCallScreen hasn't ever called showRespondViaSmsPopup() yet, or if the popup was
     * visible once but then got dismissed.
     */
    private PopupWindow mCannedResponsePopup = null;

    /**
     * The popup showing a text field for users to type in their custom message.
     */
    private AlertDialog mCustomMessagePopup = null;

    private ArrayAdapter<String> mSmsResponsesAdapter;

    private final List<String> mSmsResponses = new ArrayList<>();

    @Override
    public AnswerPresenterMst createPresenter() {
        return InCallPresenter.getInstance().getAnswerPresenter();
    }

    @Override
    public AnswerPresenterMst.AnswerUi getUi() {
        return this;
    }

    @Override
    public void showMessageDialog() {
//        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//
//        mSmsResponsesAdapter = new ArrayAdapter<>(builder.getContext(),
//                android.R.layout.simple_list_item_1, android.R.id.text1, mSmsResponses);
//
//        final ListView lv = new ListView(getActivity());
//        lv.setAdapter(mSmsResponsesAdapter);
//        lv.setOnItemClickListener(new RespondViaSmsItemClickListener());
//
//        builder.setCancelable(true).setView(lv).setOnCancelListener(
//                new DialogInterface.OnCancelListener() {
//                    @Override
//                    public void onCancel(DialogInterface dialogInterface) {
//                        onMessageDialogCancel();
//                        dismissCannedResponsePopup();
//                        getPresenter().onDismissDialog();
//                    }
//                });
//        mCannedResponsePopup = builder.create();
//        mCannedResponsePopup.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
//        mCannedResponsePopup.show();
    	showMessageDialogMst();
    }

    private boolean isCannedResponsePopupShowing() {
        if (mCannedResponsePopup != null) {
            return mCannedResponsePopup.isShowing();
        }
        return false;
    }

    private boolean isCustomMessagePopupShowing() {
        if (mCustomMessagePopup != null) {
            return mCustomMessagePopup.isShowing();
        }
        return false;
    }

    /**
     * Dismiss the canned response list popup.
     *
     * This is safe to call even if the popup is already dismissed, and even if you never called
     * showRespondViaSmsPopup() in the first place.
     */
    protected void dismissCannedResponsePopup() {
        if (mCannedResponsePopup != null) {
            mCannedResponsePopup.dismiss();  // safe even if already dismissed
            mCannedResponsePopup = null;
        }
    }

    /**
     * Dismiss the custom compose message popup.
     */
    private void dismissCustomMessagePopup() {
        if (mCustomMessagePopup != null) {
            mCustomMessagePopup.dismiss();
            mCustomMessagePopup = null;
        }
    }

    public void dismissPendingDialogs() {
        if (isCannedResponsePopupShowing()) {
            dismissCannedResponsePopup();
        }

        if (isCustomMessagePopupShowing()) {
            dismissCustomMessagePopup();
        }
    }

    public boolean hasPendingDialogs() {
        return !(mCannedResponsePopup == null && mCustomMessagePopup == null);
    }

    /**
     * Shows the custom message entry dialog.
     */
    public void showCustomMessageDialog() {
        // Create an alert dialog containing an EditText
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText et = new EditText(builder.getContext());
        builder.setCancelable(true).setView(et)
                .setPositiveButton(R.string.custom_message_send,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // The order is arranged in a way that the popup will be destroyed
                                // when the InCallActivity is about to finish.
                                final String textMessage = et.getText().toString().trim();
                                dismissCustomMessagePopup();
//                                getPresenter().rejectCallWithMessage(textMessage);
                                getPresenter().sendMessage(textMessage);
                                getPresenter().onDismissDialog();
                            }
                        })
                .setNegativeButton(R.string.custom_message_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dismissCustomMessagePopup();
                                getPresenter().onDismissDialog();
                            }
                        })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        dismissCustomMessagePopup();
                        getPresenter().onDismissDialog();
                    }
                })
                .setTitle(R.string.respond_via_sms_custom_message);
        mCustomMessagePopup = builder.create();
        
        mCustomMessagePopup.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface arg0) {
                // TODO Auto-generated method stub
//                mHandler.removeMessages(TIME_OUT);
            }
        });     

        // Enable/disable the send button based on whether there is a message in the EditText
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
//                mHandler.removeMessages(TIME_OUT);
//                mHandler.sendEmptyMessageDelayed(TIME_OUT, 10*1000);
                final Button sendButton = mCustomMessagePopup.getButton(
                        DialogInterface.BUTTON_POSITIVE);
                sendButton.setEnabled(s != null && s.toString().trim().length() != 0);
            }
        });

        // Keyboard up, show the dialog
        mCustomMessagePopup.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        mCustomMessagePopup.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        mCustomMessagePopup.show();
//        mHandler.removeMessages(TIME_OUT);
//        mHandler.sendEmptyMessageDelayed(TIME_OUT, 10*1000);

        // Send button starts out disabled
        final Button sendButton = mCustomMessagePopup.getButton(DialogInterface.BUTTON_POSITIVE);
        sendButton.setEnabled(false);
    }

    @Override
    public void configureMessageDialog(List<String> textResponses) {
        Log.d(this, "configureMessageDialog " + textResponses);
    	if(textResponses != null) {
            Log.d(this, "textResponses size = " + textResponses.size());
    	}
        mSmsResponses.clear();
        mSmsResponses.addAll(textResponses);
        mSmsResponses.add(getResources().getString(
                R.string.respond_via_sms_custom_message));
        if (mSmsResponsesAdapter != null) {
            mSmsResponsesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    public void onAnswer(int videoState, Context context) {
        Log.d(this, "onAnswer videoState=" + videoState + " context=" + context);
        startAnswerAnim();
        getPresenter().onAnswer(videoState, context);
    }

    public void onDecline(Context context) {
        getPresenter().onDecline(context);
    }

    public void onDeclineUpgradeRequest(Context context) {
        InCallPresenter.getInstance().declineUpgradeRequest(context);
    }

    public void onText() {
        getPresenter().onText();
    }

    public void onDeflect(Context context) {
        getPresenter().onDeflect(context);
    }

    /**
     * OnItemClickListener for the "Respond via SMS" popup.
     */
    public class RespondViaSmsItemClickListener implements AdapterView.OnItemClickListener {

        /**
         * Handles the user selecting an item from the popup.
         */
        @Override
        public void onItemClick(AdapterView<?> parent,  // The ListView
                View view,  // The TextView that was clicked
                int position, long id) {
            Log.d(this, "RespondViaSmsItemClickListener.onItemClick(" + position + ")...");
            final String message = (String) parent.getItemAtPosition(position);
            Log.v(this, "- message: '" + message + "'");
            dismissCannedResponsePopup();

            // The "Custom" choice is a special case.
            // (For now, it's guaranteed to be the last item.)
            if (position == (parent.getCount() - 2)) {
                // Show the custom message dialog
                showCustomMessageDialog();
            } else if(position == (parent.getCount() - 1)) {
                dismissCannedResponsePopup();
                getPresenter().onDismissDialog();
            } else {
//                getPresenter().rejectCallWithMessage(message);
                getPresenter().sendMessage(message);
                dismissCannedResponsePopup();
                getPresenter().onDismissDialog();
            }
        }
    }

    public void onShowAnswerUi(boolean shown) {
        // Do Nothing
    }

    public void showTargets(int targetSet) {
        // Do Nothing
    }

    public void showTargets(int targetSet, int videoState) {
        // Do Nothing
    }

    protected void onMessageDialogCancel() {
        // Do nothing.
    }
    

    
    public void updateUI() {
    	Call call = CallList.getInstance().getIncomingCall();
//    	if(call == null) {
//    		dismissPendingDialogs();
//    	}
        setCallWaiting();
    }
    

    
    public void showMessageDialogMst() {

		mSmsResponsesAdapter = new ArrayAdapter<>(getActivity(),
				R.layout.list_item_1_line, R.id.text1, mSmsResponses);

		final ListView lv = new ListView(getActivity());
		lv.setFooterDividersEnabled(false);
		lv.setDividerHeight(0);
		lv.setAdapter(mSmsResponsesAdapter);

		View group = LayoutInflater.from(getActivity()).inflate(
				R.layout.list_item_cancel, null);
		final TextView cancel = (TextView) group.findViewById(R.id.text1);
		cancel.setText(R.string.custom_message_cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
	               dismissCannedResponsePopup();
	               getPresenter().onDismissDialog();
			}
		});
		lv.addFooterView(group);
		lv.setOnItemClickListener(new RespondViaSmsItemClickListener());

		mCannedResponsePopup = new PopupWindow(lv, LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT, true);

		mCannedResponsePopup.setTouchable(true);
		mCannedResponsePopup
				.setAnimationStyle(R.style.mypopwindow_anim_style);

		// 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
		// 我觉得这里是API的一个bug
		mCannedResponsePopup.setBackgroundDrawable(new BitmapDrawable());

		mCannedResponsePopup
				.setOnDismissListener(new PopupWindow.OnDismissListener() {
					public void onDismiss() {
					    onMessageDialogCancel();
//						InCallApp.getInCallActivity().showUp();
					    mHandler.removeMessages(TIME_OUT);
					}
				});		

		// 设置好参数之后再show
		mCannedResponsePopup.showAtLocation(this.getView(), Gravity.BOTTOM, 0, 0);
		mHandler.sendEmptyMessageDelayed(TIME_OUT, 10*1000);
    }
    
    private void startAnswerAnim() {
    	InCallApp.getInCallActivity().getCallCardFragment().startAnswerAnim();
    }
    
    public void stopAnim() {
    }
    
    protected void setCallWaiting() {
        
    }
    
    
    private static final int TIME_OUT = 2;
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIME_OUT: {
                    dismissPendingDialogs();
                    getPresenter().onDismissDialog();
                    break;
                }
            }
        }
    };
    
    void restoreState() {
        
    }
}
