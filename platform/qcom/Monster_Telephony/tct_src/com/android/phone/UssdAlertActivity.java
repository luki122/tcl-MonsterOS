/******************************************************************************/
/*                                                               Date:08/2013 */
/*                                PRESENTATION                                */
/*                                                                            */
/*       Copyright 2013 TCL Communication Technology Holdings Limited.        */
/*                                                                            */
/* This material is company confidential, cannot be reproduced in any form    */
/* without the written permission of TCL Communication Technology Holdings    */
/* Limited.                                                                   */
/*                                                                            */
/* -------------------------------------------------------------------------- */
/*  Author :  Tang.Ding                                                       */
/*  Email  :  Tang.Ding@tcl.com                                               */
/*  Role   :                                                                  */
/*  Reference documents :                                                     */
/* -------------------------------------------------------------------------- */
/*  Comments :                                                                */
/*  File     :packages/apps/Phone/tct_src/com/android/phone/UssdAlertActivity */
/*  Labels   :                                                                */
/* -------------------------------------------------------------------------- */
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/*    date   |        author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 08/28/2013|      Tang.Ding       |      FR-467815       |Orange's request  */
/* ----------|----------------------|----------------------|----------------- */
/* 10/18/2013|     Xijun.Zhang      |      PR-536340       |[USSD]Display the */
/*           |                      |                      | notification da- */
/*           |                      |                      |ilog should keep  */
/*           |                      |                      |at least 5min     */
/* ----------|----------------------|----------------------|----------------- */
/* 10/24/2013|     Xijun.Zhang      |      PR-536338       |Set numberic mode */
/*           |                      |                      | as default input */
/*           |                      |                      | method for USSD. */
/* ----------|----------------------|----------------------|----------------- */
/* 11/20/2013|     haibin.yu          |      FR-555969       |[TMO][ID 57294] */
/*           |                      |                      | Failed USSD reques*/
/* ----------|----------------------|----------------------|----------------- */
/* 12/02/2013|     xian.jiang       |      CR-560220       |[HOMO]For silent  */
/*           |                      |                      |or vibration modes*/
/*           |                      |                      |USSD reply should */
/*           |                      |                      |be silent        */
/* ----------|----------------------|----------------------|----------------- */
/* 02/22/2014|     Dandan.Fang      |       PR601618       |[SS][GCF]DUT able */
/*           |                      |                      | to receive resp- */
/*           |                      |                      |onse message but  */
/*           |                      |                      |hear vibrate sou- */
/*           |                      |                      |nd in silent mode */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

package com.android.phone;

import java.io.IOException;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;//[BUGFIX]-Add by TCTNB.xian.jiang,12/02/2013,560220,
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.Vibrator;//[BUGFIX]-Add by TCTNB.xian.jiang,12/02/2013,560220,
//[BUGFIX]-Add-BEGIN by TCTNB.Xijun.Zhang,10/24/2013,PR-536338,
//Set numberic mode as default input method for USSD.
import android.text.InputType;
//[BUGFIX]-Add-END by TCTNB.Xijun.Zhang,10/24/2013,PR-536338
import android.text.TextWatcher;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import android.widget.EditText;
import android.widget.TextView;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.phone.PhoneApp;
import com.android.phone.PhoneUtils;
import com.android.phone.R;

public class UssdAlertActivity extends AlertActivity implements
        DialogInterface.OnClickListener {

    private static final String LOG_TAG = "UssdAlertActivity";
    public static final int USSD_DIALOG_REQUEST = 1;
    public static final int USSD_DIALOG_NOTIFICATION = 2;
    public static final String USSD_MESSAGE_EXTRA = "ussd_message";
    public static final String USSD_TYPE_EXTRA = "ussd_type";
    public static final String USSD_SLOT_ID = "slot_id";
    public static final String USSD_PHONE_ID = "phone_id"; //add by  Fuqiang.song for PR-1070103,2015-09-06

    private TextView mMsg;
    private EditText mInputText;
    private String mText;
    private int mType = USSD_DIALOG_REQUEST;
    private int mSlotId;
    private Phone mphone;
    private static final String TAG = "UssdAlertActivity";
    private MediaPlayer mMediaPlayer;

    //for the orange feature :the display time of the USSD shall be between 2 and 5 min
    private boolean isSupportOrangeUssdFeatrue = true;
    private static final int CMD_DISMISS = 1001;
    private static final long CMD_DISMISS_Millis = 180000;//default:3min
    //[BUGFIX]-Add-BEGIN by TCTNB.Xijun.Zhang,10/18/2013,PR-536340,
    //[USSD]Display the notification dailog should keep at least 5min.
    private static final long CMD_DISMISS_NOTIFICATION_Millis = 300000;
    //[BUGFIX]-Add-END by TCTNB.Xijun.Zhang,10/18/2013,PR-536340
    //[FEATURE]-Add-BEGIN by TSNJ.(haibin.yu),11/19/2013,PR-555969,
    private boolean isSupportTmoblieUssdFeature = true;
    private static final long TMOBILE_DISMISS_NOTIFICATION_Millis = 5000;
    //[FEATURE]-Add-END by TSNJ.(haibin.yu),

    private Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Log.d(LOG_TAG, "handleMessage...delta time is "+(System.currentTimeMillis()-beginTime));
            switch(msg.what){
                case CMD_DISMISS:
                    if(mType == USSD_DIALOG_REQUEST){
                        PhoneUtils.cancelUssdDialog();
                    }
                    dismiss();
                    break;

                default:
                    break;
            }
        }
    };
    private long beginTime;//for testing the delay time ,can be removed in the official version

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the "dialog"
        final AlertController.AlertParams p = mAlertParams;

//        mphone = PhoneGlobals.getPhone();
        Intent intent = getIntent();
        mText = intent.getStringExtra(USSD_MESSAGE_EXTRA);
        mType = intent.getIntExtra(USSD_TYPE_EXTRA, USSD_DIALOG_REQUEST);
        mSlotId = intent.getIntExtra(USSD_SLOT_ID, 0);
        //[BUGFIX]-ADD-BEGIN by TCTNB.Fuqiang.Song,09/06/2015,1070103,
        //[USSD] when enter second USSD selection, pop up error message
        mphone = PhoneFactory.getPhone(intent.getIntExtra(USSD_PHONE_ID, 0));
        Log.v(LOG_TAG,"phone id:"+mphone.getPhoneId());
        //[BUGFIX]-ADD-END by TCTNB.Fuqiang.Song
        //p.mIconId = android.R.drawable.ic_dialog_alert;
        //p.mTitle = getString(R.string.bt_enable_title);
        //p.mTitle = "USSD";
        p.mView = createView();
        if (mType == USSD_DIALOG_REQUEST) {
            p.mPositiveButtonText = getString(R.string.send_button);
            p.mNegativeButtonText = getString(R.string.cancel);
        } else {
            p.mPositiveButtonText = getString(R.string.ok);
        }

        p.mPositiveButtonListener = this;
        p.mNegativeButtonListener = this;

        //[BUGFIX]-Mod-BEGIN by TCTNB.xian.jiang,12/02/2013,CR-560220,
        //For silent and vibration modes USSD replies should be silent
        //playUSSDTone(PhoneGlobals.getInstance().getApplicationContext());
        playUSSDToneOrSilent();
        //[BUGFIX]-Mod-END by TCTNB.xian.jiang

        PhoneUtils.mUssdActivity = this;
        setupAlert();


        isSupportOrangeUssdFeatrue =!(this.getResources().getBoolean(R.bool.feature_phone_ussdPermanentDisplay_on));
        if(isSupportOrangeUssdFeatrue){
            //[BUGFIX]-Mod-BEGIN by TCTNB.Xijun.Zhang,10/18/2013,PR-536340,
            //[USSD]Display the notification dailog should keep at least 5min.
            //mhandler.sendEmptyMessageDelayed(CMD_DISMISS, CMD_DISMISS_Millis);
            if (mType == USSD_DIALOG_REQUEST) {
                Log.v(LOG_TAG,"USSD dialog type is USSD_DIALOG_REQUEST,will dismiss after 3min");
                mhandler.sendEmptyMessageDelayed(CMD_DISMISS, CMD_DISMISS_Millis);
            }
            if (mType == USSD_DIALOG_NOTIFICATION) {
                Log.v(LOG_TAG,"USSD dialog type is USSD_DIALOG_NOTIFICATION,will dismiss after 5min");
                mhandler.sendEmptyMessageDelayed(CMD_DISMISS, CMD_DISMISS_NOTIFICATION_Millis);
            }
            //[BUGFIX]-Mod-END by TCTNB.Xijun.Zhang,10/18/2013,PR-536340
            beginTime = System.currentTimeMillis();
        }
        //[FEATURE]-Add-BEGIN by TSNJ.(haibin.yu),11/19/2013,PR-555969,
        isSupportTmoblieUssdFeature =(this.getResources().getBoolean(R.bool.feature_phone_ussdFailedDisplay_on));
        if(isSupportTmoblieUssdFeature){
            if (mType == USSD_DIALOG_NOTIFICATION) {
                mhandler.sendEmptyMessageDelayed(CMD_DISMISS, TMOBILE_DISMISS_NOTIFICATION_Millis);
            }
        }
        //[FEATURE]-Add-END by TSNJ.(haibin.yu),
    }

    protected void onResume() {
        super.onResume();
        if (mType == USSD_DIALOG_REQUEST) {
            String text = mInputText.getText().toString();
            if (text != null && text.length() > 0) {
                mAlert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
            } else {
                mAlert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
            }
            mInputText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start,
                    int count, int after) {

                if(isSupportOrangeUssdFeatrue){
                    beginTime = 0L;
                    mhandler.removeMessages(CMD_DISMISS);
                }
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                int count = s == null ? 0 : s.length();
                if (count > 0) {
                    mAlert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    mAlert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                }

                if(isSupportOrangeUssdFeatrue){
                    mhandler.sendEmptyMessageDelayed(CMD_DISMISS, CMD_DISMISS_Millis);
                    beginTime = System.currentTimeMillis();
                }
            }
          });
        }
    }


	/*added by liling 20141223 for pr878488 begin*/
	public boolean onTouchEvent(MotionEvent event) {
		Log.i("UssdAlertActivity","onTouchEvent");
		return false;
	 }
	/*added by liling 20141223 for pr878488 end*/

	

    private View createView() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_ussd_response, null);
        mMsg = (TextView)dialogView.findViewById(R.id.msg);
        mInputText = (EditText) dialogView.findViewById(R.id.input_field);
        //[BUGFIX]-Add-BEGIN by TCTNB.Xijun.Zhang,10/24/2013,PR-536338,
        //Set numberic mode as default input method for USSD.
        //[BUGFIX]-Add-BEGIN by TCTNB.xianzhuan.hu,11/19/2013,555209,
        //can not input "*" and "#" for ptcrb
        
        //PR-915106-wei-gao-001 begin
	/*added by liling 20141229 for pr883909 begin*/
//        Boolean mboolean = (this.getResources().getBoolean(R.bool.def_telephony_ussd_inputtype));
//	    android.util.Log.i("UssdAlertActivity","mboolean="+mboolean);
//        if(mboolean==false){
//         	mInputText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_PHONE);
//        }
	/*added by liling 20141229 for pr883909 end*/
        //PR-915106-wei-gao-001 end
        
        //[BUGFIX]-Add-END by TCTNB.xianzhuan.hu
        //[BUGFIX]-Add-END by TCTNB.Xijun.Zhang,10/24/2013,PR-536338
        if (mMsg != null) {
            mMsg.setText(mText);
        }

        if (mType == USSD_DIALOG_NOTIFICATION) {
            mInputText.setVisibility(View.GONE);
        }

        return dialogView;
    }

    public void onClick(DialogInterface dialog, int which) {
        if(isSupportOrangeUssdFeatrue)
            mhandler.removeMessages(CMD_DISMISS);

        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                if (mType == USSD_DIALOG_REQUEST) {
                    sendUssd();
                }
                finish();
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                PhoneUtils.cancelUssdDialog();
                finish();
                break;
            default:
                break;
        }
    }

    private void sendUssd() {
        Log.w(LOG_TAG, "sendUssd USSR string :" + mInputText.getText().toString());
        mphone.sendUssdResponse(mInputText.getText().toString());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.v(TAG, "onNewIntent");
        //force to finish ourself and then start new one

        //[BUGFIX]-Modify-BEGIN by TCTNB.Bo.Yu, 15/03/2014, PR-615489, [HOMO][TMO Poland 55314 ]Terminal behavior when more than one push message was sent to terminal
        if(!PhoneGlobals.getInstance().getApplicationContext().getResources().getBoolean(R.bool.feature_phone_previousUssdDialogShow_on)){
            finish();
         }
        //[BUGFIX]-Modify-END by TCTNB.Bo.Yu, 15/03/2014, PR-615489

        //[BUGFIX]-Mod-BEGIN by TCTNB.xian.jiang,12/02/2013,560220,
        //For silent and vibration modes USSD replies should be silent
        //playUSSDTone(PhoneGlobals.getInstance().getApplicationContext());
        playUSSDToneOrSilent();
        //[BUGFIX]-Mod-END by TCTNB.xian.jiang

        startActivity(intent);
    }

    //[BUGFIX]-Add-BEGIN by TCTNB.xian.jiang,12/02/2013,560220,
    //For silent and vibration modes USSD replies should be silent
    public void playUSSDToneOrSilent () {
        if (getResources().getBoolean(R.bool.feature_phone_ussdPlaySilent_on)) {
            AudioManager audioManager =(AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
            //[BUGFIX]-Add-BEGIN by TCTNB.Dandan.Fang,02/22/2014,PR601618,
            //[SS][GCF]DUT able to receive response message but hear vibrate sound in silent mode
            //boolean silentMode = ((audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT)||(audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE));
            boolean silentMode = audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT ;
            boolean vibratorMode = audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE;
            //if (silentMode) {
            if (vibratorMode) {
                //[BUGFIX]-Add-END by TCTNB.Dandan.Fang
                Vibrator vibrator;
                vibrator = (Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);
                long[] pattern = {200,200,200,200};
                vibrator.vibrate(pattern, -1);
                }
            //[BUGFIX]-Add-BEGIN by TCTNB.Dandan.Fang,02/22/2014,PR601618,
            //[SS][GCF]DUT able to receive response message but hear vibrate sound in silent mode
            else if (silentMode){
            }
            //[BUGFIX]-Add-END by TCTNB.Dandan.Fang
            else{
                playUSSDTone(PhoneGlobals.getInstance().getApplicationContext());
                }
            }
        else {
           playUSSDTone(PhoneGlobals.getInstance().getApplicationContext());
        }
    }
    //[BUGFIX]-Add-END by TCTNB.xian.jiang

    public void playUSSDTone(final Context context) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    //[FEATURE]-Mod-BEGIN by TSCD.xiaotian.wang,FR-796469,12/16/2014
                    mMediaPlayer = new MediaPlayer();
                    mMediaPlayer.reset();
                    //[FEATURE]-Mod-END by TSCD.xiaotian.wang,FR-796469,12/16/2014
                    mMediaPlayer.setDataSource(context,
                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                    mMediaPlayer.prepare();

                    //[BUGFIX]-Add-BEGIN by TCTNB.Bo.Yu, 15/03/2014, PR-615489, [HOMO][TMO Poland 55314 ]Terminal behavior when more than one push message was sent to terminal
                    mMediaPlayer.start();
                    setMediaListener(mMediaPlayer);
                    //[BUGFIX]-Add-END by TCTNB.Bo.Yu, 15/03/2014, PR-615489
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //[BUGFIX]-Del-BEGIN by TCTNB.Bo.Yu, 15/03/2014, PR-615489, [HOMO][TMO Poland 55314 ]Terminal behavior when more than one push message was sent to terminal
                //mMediaPlayer.start();
                //setMediaListener(mMediaPlayer);
                //[BUGFIX]-Del-END by TCTNB.Bo.Yu, 15/03/2014, PR-615489
            }
        }).start();
    }

    public void setMediaListener(MediaPlayer mediaPlayer) {
       mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
           public void onCompletion(MediaPlayer mp) {
               mMediaPlayer.release();
               mMediaPlayer = null;
           }
       });
    }

    /**
     * @return the mInputText widget
     */
    public EditText getmInputTextOnlyForTest() {
        return mInputText;
    }

    /**
     * @return the mAlert widget
     */
    public AlertController getmAlertOnlyForTest() {
        return super.mAlert;
    }

    /**
     * @return the mType widget
     */
    public int getmTypeOnlyForTest() {
        return mType;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if(isSupportOrangeUssdFeatrue){
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                beginTime = 0L;
                mhandler.removeMessages(CMD_DISMISS);
            }
            else if(event.getAction() == MotionEvent.ACTION_UP){
                mhandler.sendEmptyMessageDelayed(CMD_DISMISS, CMD_DISMISS_Millis);
                beginTime = System.currentTimeMillis();
            }
        }
        return super.dispatchTouchEvent(event);
    }
}
