package com.android.phone;

import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Phone;

import mst.app.dialog.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.text.BidiFormatter;
import android.text.SpannableString;
import android.text.TextDirectionHeuristics;
import android.telephony.ServiceState;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import org.codeaurora.ims.QtiImsException;
import org.codeaurora.ims.QtiImsExtListenerBaseImpl;
import org.codeaurora.ims.QtiImsExtManager;
import org.codeaurora.ims.utils.QtiImsExtUtils;

//[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 09/09/2016, SOLUTION-2472520 And TASk-2781039
//No Delay option for Call Forward unanswered
import android.widget.ArrayAdapter;
import android.widget.Spinner;
//[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
import static com.android.phone.TimeConsumingPreferenceActivity.RESPONSE_ERROR;
import static com.android.phone.TimeConsumingPreferenceActivity.EXCEPTION_ERROR;

/* MODIFIED-BEGIN by sunyandong, 2016-08-09,BUG-2699359*/
//[HOMO][EE - Orange][Call Forwarding] Call Forward should revert back to  original VM number
import android.telephony.TelephonyManager;
import android.widget.EditText;
/* MODIFIED-END by sunyandong,BUG-2699359*/

public class CallForwardEditPreference extends EditPhoneNumberPreference {
    private static final String LOG_TAG = "CallForwardEditPreference";
    private static final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 2);

    private static final String SRC_TAGS[]       = {"{0}"};
    private CharSequence mSummaryOnTemplate;
    /**
     * Remembers which button was clicked by a user. If no button is clicked yet, this should have
     * {@link DialogInterface#BUTTON_NEGATIVE}, meaning "cancel".
     *
     * TODO: consider removing this variable and having getButtonClicked() in
     * EditPhoneNumberPreference instead.
     */
    private int mButtonClicked;
    private int mServiceClass;
    private MyHandler mHandler = new MyHandler();
    int reason;
    private Phone mPhone;
    CallForwardInfo callForwardInfo;
    private TimeConsumingPreferenceListener mTcpListener;
    boolean isTimerEnabled;

    boolean mAllowSetCallFwding = false;
    /*Variables which holds CFUT response data*/
    private int mStartHour;
    private int mStartMinute;
    private int mEndHour;
    private int mEndMinute;
    private int mStatus;
    private String mNumber;
    //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 09/09/2016, SOLUTION-2472520 And TASk-2781039
    //No Delay option for Call Forward unanswered
    private boolean mShowDelayOption;
    private String[] mDelayTimeValue;
    private Spinner mDelayTimeSpinner;
    //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)

    public CallForwardEditPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mSummaryOnTemplate = this.getSummaryOn();

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CallForwardEditPreference, 0, R.style.EditPhoneNumberPreference);
        reason = a.getInt(R.styleable.CallForwardEditPreference_reason,
                CommandsInterface.CF_REASON_UNCONDITIONAL);
        a.recycle();
        //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 09/09/2016, SOLUTION-2472520 And TASk-2781039
        //No Delay option for Call Forward unanswered
        mShowDelayOption = context.getResources().getBoolean(R.bool.def_support_callforward_delay);
        //mShowDelayOption = false;
        if (mShowDelayOption) {
            if (reason == CommandsInterface.CF_REASON_NO_REPLY) {
                setDialogLayoutResource(R.layout.pref_dialog_editphonenumber_no_reply);
                mDelayTimeValue = context.getResources().getStringArray(R.array.delay_time_values);
            }
        }
        //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
        if (DBG) Log.d(LOG_TAG, "mServiceClass=" + mServiceClass + ", reason=" + reason);
    }

    public CallForwardEditPreference(Context context) {
        this(context, null);
    }

    void init(TimeConsumingPreferenceListener listener, boolean skipReading, Phone phone,
            int serviceClass) {
        mServiceClass = serviceClass;
        mPhone = phone;
        mTcpListener = listener;
        isTimerEnabled = isTimerEnabled();
        Log.d(LOG_TAG, "isTimerEnabled="+isTimerEnabled);
        if (!skipReading) {
            if (reason == CommandsInterface.CF_REASON_UNCONDITIONAL && isTimerEnabled) {
                setTimeSettingVisibility(true);
                try {
                    QtiImsExtManager.getInstance().getCallForwardUncondTimer(reason,
                            mServiceClass,
                            imsInterfaceListener);
                } catch (QtiImsException e){
                    Log.d(LOG_TAG, "getCallForwardUncondTimer failed. Exception = " + e);
                }
            } else {
                mPhone.getCallForwardingOption(reason, mServiceClass,
                        mHandler.obtainMessage(MyHandler.MESSAGE_GET_CF,
                        // unused in this case
                        CommandsInterface.CF_ACTION_DISABLE,
                        MyHandler.MESSAGE_GET_CF, null));
            }
            if (mTcpListener != null) {
                mTcpListener.onStarted(this, true);
            }
        }
    }

    private boolean isTimerEnabled() {
        //Timer is enabled only when UT services are enabled
        return (SystemProperties.getBoolean("persist.radio.ims.cmcc", false)
                || getContext().getResources().getBoolean(R.bool.config_enable_cfu_time))
                && mPhone.isUtEnabled();
    }

    @Override
    protected void onBindDialogView(View view) {
        // default the button clicked to be the cancel button.
        mButtonClicked = DialogInterface.BUTTON_NEGATIVE;
        super.onBindDialogView(view);
        //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 09/09/2016, SOLUTION-2472520 And TASk-2781039
        //No Delay option for Call Forward unanswered
        if (mShowDelayOption) {
            if (reason == CommandsInterface.CF_REASON_NO_REPLY) {
                mDelayTimeSpinner = (Spinner) view
                    .findViewById(R.id.no_reply_delay_spinner);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter
                    .createFromResource(getContext(),
                            R.array.delay_time_choices,
                            android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mDelayTimeSpinner.setAdapter(adapter);
                if (callForwardInfo != null) {
                    for (int i = 0; i < mDelayTimeValue.length; i++) {
                        if (Integer.valueOf(mDelayTimeValue[i]) == callForwardInfo.timeSeconds) {
                            mDelayTimeSpinner.setSelection(i);
                            break;
                        }
                    }
                }
            }
        }
        //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)

        /* MODIFIED-BEGIN by sunyandong, 2016-08-09,BUG-2699359*/
        //[HOMO][EE - Orange][Call Forwarding] Call Forward should revert back to  original VM number
        boolean showVoicemail = getContext().getResources().getBoolean(R.bool.def_show_voicemail_in_callforward_editbox);
        if ( showVoicemail ) {
            String voiceMailNum = "";
            voiceMailNum = TelephonyManager.getDefault().getVoiceMailNumber(mPhone.getSubId());
            if ( !TextUtils.isEmpty(voiceMailNum) ) {
                EditText editText = getEditText();
                editText.setText(voiceMailNum);
            }
        }
        /* MODIFIED-END by sunyandong,BUG-2699359*/
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        mButtonClicked = which;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (DBG) Log.d(LOG_TAG, "mButtonClicked=" + mButtonClicked
                + ", positiveResult=" + positiveResult);
        // Ignore this event if the user clicked the cancel button, or if the dialog is dismissed
        // without any button being pressed (back button press or click event outside the dialog).
        if (this.mButtonClicked != DialogInterface.BUTTON_NEGATIVE) {
            int action = (isToggled() || (mButtonClicked == DialogInterface.BUTTON_POSITIVE)) ?
                    CommandsInterface.CF_ACTION_REGISTRATION :
                    CommandsInterface.CF_ACTION_DISABLE;
            int time = (reason != CommandsInterface.CF_REASON_NO_REPLY) ? 0 : 20;
            final String number = getPhoneNumber();
            final int editStartHour = isAllDayChecked()? 0 : getStartTimeHour();
            final int editStartMinute = isAllDayChecked()? 0 : getStartTimeMinute();
            final int editEndHour = isAllDayChecked()? 0 : getEndTimeHour();
            final int editEndMinute = isAllDayChecked()? 0 : getEndTimeMinute();
            //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 09/09/2016, SOLUTION-2472520 And TASk-2781039
            //No Delay option for Call Forward unanswered
            if (mShowDelayOption) {
                if (reason == CommandsInterface.CF_REASON_NO_REPLY
                        && mDelayTimeValue != null && mDelayTimeSpinner != null) {
                    time = Integer.valueOf(mDelayTimeValue[mDelayTimeSpinner
                            .getSelectedItemPosition()]);
                }
            }
            //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
            if (DBG) Log.d(LOG_TAG, "callForwardInfo=" + callForwardInfo);

            boolean isCFSettingChanged = true;
            if (action == CommandsInterface.CF_ACTION_REGISTRATION
                    && callForwardInfo != null
                    && callForwardInfo.status == 1
                    && number.equals(callForwardInfo.number)
                    && time == callForwardInfo.timeSeconds) {//[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 09/09/2016, SOLUTION-2472520 And TASk-2781039
                if (reason == CommandsInterface.CF_REASON_UNCONDITIONAL){
                    // need to check if the time period for CFUT is changed
                    if (isAllDayChecked()){
                        isCFSettingChanged = isTimerValid();
                    } else {
                        isCFSettingChanged = mStartHour != editStartHour
                                || mStartMinute != editStartMinute
                                || mEndHour != editEndHour
                                || mEndMinute != editEndMinute;
                    }
                } else {
                    // no change, do nothing
                    if (DBG) Log.d(LOG_TAG, "no change, do nothing");
                    isCFSettingChanged = false;
                }
            }
            if (DBG) Log.d(LOG_TAG, "isCFSettingChanged = " + isCFSettingChanged);
            if (isCFSettingChanged) {
                // set to network
                if (DBG) Log.d(LOG_TAG, "reason=" + reason + ", action=" + action
                        + ", number=" + number);

                // Display no forwarding number while we're waiting for
                // confirmation
                setSummaryOn("");

                // the interface of Phone.setCallForwardingOption has error:
                // should be action, reason...
                if (reason == CommandsInterface.CF_REASON_UNCONDITIONAL
                        && !isAllDayChecked() && isTimerEnabled
                        && (action != CommandsInterface.CF_ACTION_DISABLE)) {

                    if (true) Log.d(LOG_TAG, "setCallForwardingUncondTimerOption,"
                                                +"starthour = " + editStartHour
                                                + "startminute = " + editStartMinute
                                                + "endhour = " + editEndHour
                                                + "endminute = " + editEndMinute);
                    try {
                        QtiImsExtManager.getInstance().setCallForwardUncondTimer(editStartHour,
                                editStartMinute,
                                editEndHour,
                                editEndMinute,
                                action,
                                reason,
                                mServiceClass,
                                trimPrefix(number),
                                imsInterfaceListener);
                    } catch (QtiImsException e) {
                        Log.d(LOG_TAG, "setCallForwardUncondTimer exception!" +e);
                    }
                } else {
                    mPhone.setCallForwardingOption(action,
                        reason,
                        trimPrefix(number),
                        mServiceClass,
                        time,
                        mHandler.obtainMessage(MyHandler.MESSAGE_SET_CF,
                                action,
                                MyHandler.MESSAGE_SET_CF));
                }
                if (mTcpListener != null) {
                    mTcpListener.onStarted(this, false);
                }
            }
        }
    }

    void handleCallForwardTimerResult() {
        if (DBG) Log.d(LOG_TAG, "handleCallForwardTimerResult: ");
        setToggled(mStatus == 1);
        setPhoneNumber(mNumber);
        /*Setting Timer*/
        if (reason == CommandsInterface.CF_REASON_UNCONDITIONAL) {
            setAllDayCheckBox(!(mStatus == 1 && isTimerValid()));
            //set timer info even all be zero
            setPhoneNumberWithTimePeriod(mNumber, mStartHour, mStartMinute, mEndHour, mEndMinute);
        }
    }

    void handleCallForwardResult(CallForwardInfo cf) {
        callForwardInfo = cf;
        if (DBG) Log.d(LOG_TAG, "handleGetCFResponse done, callForwardInfo=" + callForwardInfo);
        if (reason == CommandsInterface.CF_REASON_UNCONDITIONAL) {
            mStartHour = 0;
            mStartMinute = 0;
            mEndHour = 0;
            mEndMinute = 0;
        }
        setToggled(callForwardInfo.status == 1);
        setPhoneNumber(callForwardInfo.number);
    }

    private void updateSummaryText() {
        if (DBG) Log.d(LOG_TAG, "updateSummaryText, complete fetching for reason " + reason);
        if (isToggled()) {
            String number = getRawPhoneNumber();
            if (reason == CommandsInterface.CF_REASON_UNCONDITIONAL
                    && isTimerEnabled && isTimerValid()){
                number = getRawPhoneNumberWithTime();
            }
            if (number != null && number.length() > 0) {
                // Wrap the number to preserve presentation in RTL languages.
                String wrappedNumber = BidiFormatter.getInstance().unicodeWrap(
                        number, TextDirectionHeuristics.LTR);
                String values[] = { wrappedNumber };
                String summaryOn = String.valueOf(
                        TextUtils.replace(mSummaryOnTemplate, SRC_TAGS, values));
                int start = summaryOn.indexOf(wrappedNumber);

                SpannableString spannableSummaryOn = new SpannableString(summaryOn);
                PhoneNumberUtils.addTtsSpan(spannableSummaryOn,
                        start, start + wrappedNumber.length());
                setSummaryOn(spannableSummaryOn);
            } else {
                setSummaryOn(getContext().getString(R.string.sum_cfu_enabled_no_number));
            }
        }

    }

    private QtiImsExtListenerBaseImpl imsInterfaceListener =
            new QtiImsExtListenerBaseImpl() {

        @Override
        public void onSetCallForwardUncondTimer(int status) {
            if (DBG) Log.d(LOG_TAG, "onSetCallForwardTimer status= "+status);
            try {
                mAllowSetCallFwding = true;
                QtiImsExtManager.getInstance().getCallForwardUncondTimer(reason,
                        mServiceClass,
                        imsInterfaceListener);
            } catch (QtiImsException e) {
                if (DBG) Log.d(LOG_TAG, "setCallForwardUncondTimer exception! ");
            }
        }

        @Override
        public void onGetCallForwardUncondTimer(int startHour, int endHour, int startMinute,
                int endMinute, int reason, int status, String number, int service) {
            Log.d(LOG_TAG,"onGetCallForwardUncondTimer startHour= " + startHour + " endHour = "
                    + endHour + "endMinute = " + endMinute + "status = " + status
                    + "number = " + number + "service= " +service);
            mStartHour = startHour;
            mStartMinute = startMinute;
            mEndHour = endHour;
            mEndMinute = endMinute;
            mStatus = status;
            mNumber = number;

            handleGetCFTimerResponse();
        }

        @Override
        public void onUTReqFailed(int errCode, String errString) {
            if (DBG) Log.d(LOG_TAG, "onUTReqFailed errCode= "+errCode + "errString ="+ errString);
            mTcpListener.onFinished(CallForwardEditPreference.this, true);
            mTcpListener.onError(CallForwardEditPreference.this, RESPONSE_ERROR);
        }
    };

    private void handleGetCFTimerResponse() {
        if (DBG) Log.d(LOG_TAG, "handleGetCFTimerResponse: done");
        if (mAllowSetCallFwding) {
            mTcpListener.onFinished(CallForwardEditPreference.this, false);
            mAllowSetCallFwding = false;
        } else {
            mTcpListener.onFinished(CallForwardEditPreference.this, true);
        }
        handleCallForwardTimerResult();
        updateSummaryText();
    }

    // Message protocol:
    // what: get vs. set
    // arg1: action -- register vs. disable
    // arg2: get vs. set for the preceding request
    private class MyHandler extends Handler {
        static final int MESSAGE_GET_CF = 0;
        static final int MESSAGE_SET_CF = 1;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_GET_CF:
                    handleGetCFResponse(msg);
                    break;
                case MESSAGE_SET_CF:
                    handleSetCFResponse(msg);
                    break;
            }
        }

        private void handleGetCFResponse(Message msg) {
            if (DBG) Log.d(LOG_TAG, "handleGetCFResponse: done");

            mTcpListener.onFinished(CallForwardEditPreference.this, msg.arg2 != MESSAGE_SET_CF);

            AsyncResult ar = (AsyncResult) msg.obj;

            callForwardInfo = null;
            if (ar.exception != null) {
                if (DBG) Log.d(LOG_TAG, "handleGetCFResponse: ar.exception=" + ar.exception);
                if (ar.exception instanceof CommandException) {
                    mTcpListener.onException(CallForwardEditPreference.this,
                            (CommandException) ar.exception);
                } else {
                    // Most likely an ImsException and we can't handle it the same way as
                    // a CommandException. The best we can do is to handle the exception
                    // the same way as mTcpListener.onException() does when it is not of type
                    // FDN_CHECK_FAILURE.
                    mTcpListener.onError(CallForwardEditPreference.this, EXCEPTION_ERROR);
                }
            } else {
                if (ar.userObj instanceof Throwable) {
                    mTcpListener.onError(CallForwardEditPreference.this, RESPONSE_ERROR);
                }
                CallForwardInfo cfInfoArray[] = (CallForwardInfo[]) ar.result;
                if (cfInfoArray.length == 0) {
                    if (DBG) Log.d(LOG_TAG, "handleGetCFResponse: cfInfoArray.length==0");
                    setEnabled(false);
                    mTcpListener.onError(CallForwardEditPreference.this, RESPONSE_ERROR);
                } else {
                    for (int i = 0, length = cfInfoArray.length; i < length; i++) {
                        if (DBG) Log.d(LOG_TAG, "handleGetCFResponse, cfInfoArray[" + i + "]="
                                + cfInfoArray[i]);
                        if ((mServiceClass & cfInfoArray[i].serviceClass) != 0) {
                            // corresponding class
                            CallForwardInfo info = cfInfoArray[i];
                            handleCallForwardResult(info);

                            // Show an alert if we got a success response but
                            // with unexpected values.
                            // Currently only handle the fail-to-disable case
                            // since we haven't observed fail-to-enable.
                            if (msg.arg2 == MESSAGE_SET_CF &&
                                    msg.arg1 == CommandsInterface.CF_ACTION_DISABLE &&
                                    info.status == 1) {
//[SOLUTION]-Add-BEGIN by TCTNB.(JiangLong Pan), 08/12/2016, SOLUTION-2475020
//Error message when Call forward when busy is off
                                boolean isShowErrorMsg = getContext().getResources().getBoolean(R.bool.def_phone_show_cf_error_message);
                                if (isShowErrorMsg){
                                    CharSequence s;
                                    switch (reason) {
                                        case CommandsInterface.CF_REASON_BUSY:
                                            s = getContext().getText(R.string.disable_cfb_forbidden);
                                            break;
                                        case CommandsInterface.CF_REASON_NO_REPLY:
                                            s = getContext().getText(R.string.disable_cfnry_forbidden);
                                            break;
                                        default: // not reachable
                                            s = getContext().getText(R.string.disable_cfnrc_forbidden);
                                    }
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setNeutralButton(R.string.close_dialog, null);
                                    builder.setTitle(getContext().getText(R.string.error_updating_title));
                                    builder.setMessage(s);
                                    builder.setCancelable(true);
                                    builder.create().show();
                                } else {
                                    CharSequence s = null;
                                    switch (reason) {
                                        case CommandsInterface.CF_REASON_BUSY:
                                            s = getContext().getText(R.string.disable_cfb_forbidden);
                                            break;
                                        case CommandsInterface.CF_REASON_NO_REPLY:
                                            s = getContext().getText(R.string.disable_cfnry_forbidden);
                                            break;
                                        case CommandsInterface.CF_REASON_NOT_REACHABLE:
                                            s = getContext().getText(R.string.disable_cfnrc_forbidden);
                                            break;
                                        default:
                                            break;

                                    }
                                    if (s != null) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setNeutralButton(R.string.close_dialog, null);
                                        builder.setTitle(getContext().getText(R.string.error_updating_title));
                                        builder.setMessage(s);
                                        builder.setCancelable(true);
                                        builder.create().show();
                                    }
                                }
//[SOLUTION]-Add-END by TCTNB.(JiangLong Pan)
                            }
                        }
                    }
                }
            }

            // Now whether or not we got a new number, reset our enabled
            // summary text since it may have been replaced by an empty
            // placeholder.
            updateSummaryText();
        }

        private void handleSetCFResponse(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;

            if (ar.exception != null) {
                if (DBG) Log.d(LOG_TAG, "handleSetCFResponse: ar.exception=" + ar.exception);
                // setEnabled(false);
            }
            if (DBG) Log.d(LOG_TAG, "handleSetCFResponse: re get");

//[SOLUTION]-Add-BEGIN by TCTNB.(JiangLong Pan), 08/12/2016, SOLUTION-2475020
//Error message when Call forward when busy is off
            boolean isShowErrorMsg = getContext().getResources().getBoolean(R.bool.def_phone_show_cf_error_message);
            mPhone.getCallForwardingOption(reason, mServiceClass,
                    obtainMessage(MESSAGE_GET_CF, (isShowErrorMsg ? msg.arg1 : reason), MESSAGE_SET_CF, ar.exception));
//[SOLUTION]-Add-END by TCTNB.(JiangLong Pan)
        }
    }

    //used to check if timer infor is valid
    private boolean isTimerValid() {
        return mStartHour != 0 || mStartMinute != 0 || mEndHour != 0 || mEndMinute != 0;
    }
    
    //add by lgy
    private String trimPrefix(String number) {
        String result = number;
        if(number.startsWith("+86")){
            result =  number.substring(3);
            if(!isMobileNO(result)) {
                result = "0" + result;
            }
        }
        return result;
    }
    
    //add by lgy for 3460234
    /** 
     * 验证手机格式 
     */  
    private static boolean isMobileNO(String number) {  
        /* 
        移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188 
        联通：130、131、132、152、155、156、185、186 
        电信：133、153、180、189、（1349卫通） 
        总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9 
        */  
        String telRegex = "[1][358]\\d{9}";//"[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。  
        if (TextUtils.isEmpty(number)) return false;  
        else return number.matches(telRegex);  
       }  
}
