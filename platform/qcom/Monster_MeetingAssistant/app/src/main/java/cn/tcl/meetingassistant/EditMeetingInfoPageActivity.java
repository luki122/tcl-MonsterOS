/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

import cn.tcl.meetingassistant.bean.Meeting;
import cn.tcl.meetingassistant.bean.MeetingInfo;
import cn.tcl.meetingassistant.bean.MeetingStaticInfo;
import cn.tcl.meetingassistant.db.MeetingInfoDBUtil;
import cn.tcl.meetingassistant.db.OnDoneInsertAndUpdateListener;
import cn.tcl.meetingassistant.log.MeetingLog;
import cn.tcl.meetingassistant.utils.CurrentTimeUtil;
import cn.tcl.meetingassistant.utils.TimeFormatUtil;
import cn.tcl.meetingassistant.view.AbsMeetingActivity;
import cn.tcl.meetingassistant.view.DateAndTimePickerDialogFragment;
import cn.tcl.meetingassistant.view.FixedEditText;
import cn.tcl.meetingassistant.view.MeetingToast;
import cn.tcl.meetingassistant.view.SpannableTextWatcher;
import mst.app.dialog.DateTimeDialog;
import mst.widget.ActionMode;
import mst.widget.ActionModeListener;
import mst.widget.DateTimePicker;
import mst.widget.toolbar.Toolbar;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-3.
 * The editing meeting info page
 */
public class EditMeetingInfoPageActivity extends AbsMeetingActivity implements View.OnClickListener {
    //edit view for meeting title
    private EditText mTitleEdit;
    private FixedEditText mObjectivesEdit;
    private EditText mPersonEdit;
    private EditText mAddressEdit;
    private Toolbar mToolbar;

    //time views
    private LinearLayout mStartTime;
    private TextView mStartDateText;
    private TextView mStartTimeText;
    private LinearLayout mEndTime;
    private TextView mEndDateText;
    private TextView mEndTimeText;

    private DateTimeDialog mStartTimeDateTimeDialog;
    private DateTimeDialog mEndTimeDialog;


    private String TAG = EditMeetingInfoPageActivity.class.getSimpleName();

    //time picker dialog
    private DateAndTimePickerDialogFragment mDialog;
    private Long mChosenStartTime;
    private Long mChosenEndTime;
    private final String FLAG_CHOOSE_START_TIME = "start_time";
    private final String FLAG_CHOOSE_END_TIME = "end_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MeetingLog.i(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        initView();
        initMeetingInfo();
    }

    @Override
    protected void onResume() {
        MeetingLog.i(TAG,"onResume");
        super.onResume();
    }

    private void initView() {
        setMstContentView(R.layout.activity_edit_meeting_info_page_layout);
        mTitleEdit = (EditText) findViewById(R.id.key_note);
        mObjectivesEdit = (FixedEditText) findViewById(R.id.objectives);
        mPersonEdit = (EditText) findViewById(R.id.person);
        mAddressEdit = (EditText) findViewById(R.id.address);
        mStartTime = (LinearLayout) findViewById(R.id.start_time);
        mEndTime = (LinearLayout) findViewById(R.id.end_time);
        mStartDateText = (TextView) findViewById(R.id.edit_meeting_start_date);
        mStartTimeText = (TextView) findViewById(R.id.edit_meeting_start_time);
        mEndDateText = (TextView) findViewById(R.id.edit_meeting_end_date);
        mEndTimeText = (TextView) findViewById(R.id.edit_meeting_end_time);
        mStartTime.setOnClickListener(this);
        mEndTime.setOnClickListener(this);

        // init date info
        mStartDateText.setText(CurrentTimeUtil.getDateAfterNDay(0));
        mEndDateText.setText(CurrentTimeUtil.getDateAfterNDay(0));
        mAddressEdit.setText(MeetingStaticInfo.getLastMeetingLocation());
        // init time info
        mStartTimeText.setText(CurrentTimeUtil.getTimeAfterNHour(0));
        mEndTimeText.setText(CurrentTimeUtil.getTimeAfterNHour(2));

        mObjectivesEdit.addTextChangedListener(new SpannableTextWatcher(this));

        mPersonEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if(charSequence.length()>=1 && before < charSequence.length() && start < charSequence.length()){
                    if(charSequence.charAt(start) == '\n'){
                        MeetingLog.i(TAG,"onTextChanged get char \\n " );
                        String newString = charSequence.toString().replace('\n',';');
                        mPersonEdit.setText(newString);
                        mPersonEdit.setSelection(start+1);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        initDialog();

        initActionMode();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            showActionMode(true);
        }
    }

    private void initActionMode(){
        mToolbar = getToolbar();
        mToolbar.setVisibility(View.INVISIBLE);
        setupActionModeWithDecor(mToolbar);
        setActionModeListener(new ActionModeListener() {
            @Override
            public void onActionItemClicked(ActionMode.Item item) {
                switch (item.getItemId()) {
                    case ActionMode.NAGATIVE_BUTTON:
                        onBackPressed();
                        break;
                    case ActionMode.POSITIVE_BUTTON:
                        String title = mTitleEdit.getText().toString();
                        title = title.trim();
                        if(TextUtils.isEmpty(title)){
                            MeetingToast.makeText(EditMeetingInfoPageActivity.this,R.string.meeting_title_not_be_empty,MeetingToast.LENGTH_SHORT).show();
                            return;
                        }
                        String topics = mObjectivesEdit.getTextString();
                        String person = mPersonEdit.getText().toString();
                        String address = mAddressEdit.getText().toString();

                        final MeetingInfo meetingInfo = new MeetingInfo();
                        meetingInfo.setId(MeetingStaticInfo.getCurrentMeeting().getId());
                        meetingInfo.setTitle(title);
                        meetingInfo.setTopics(topics);
                        meetingInfo.setPersons(person);
                        meetingInfo.setStartTime(mChosenStartTime);
                        meetingInfo.setEndTime(mChosenEndTime);
                        meetingInfo.setAddress(address);
                        meetingInfo.setUpdateTime(System.currentTimeMillis());

                        OnDoneInsertAndUpdateListener onDoneInsertAndUpdateListener = new OnDoneInsertAndUpdateListener() {
                            @Override
                            public void onDone(long id) {
                                MeetingLog.i(TAG,"update meeting info successful");
                                // update memory data
                                MeetingStaticInfo.getCurrentMeeting().setMeetingInfo(meetingInfo);
                                finish();
                            }
                        };
                        MeetingInfoDBUtil.update(meetingInfo, EditMeetingInfoPageActivity.this, onDoneInsertAndUpdateListener);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onActionModeShow(ActionMode actionMode) {

            }

            @Override
            public void onActionModeDismiss(ActionMode actionMode) {

            }
        });
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.start_time:
                    if((null != mStartTimeDateTimeDialog && mStartTimeDateTimeDialog.isShowing()) || (null != mEndTimeDialog && mEndTimeDialog.isShowing()) ){
                    MeetingLog.i(TAG,"dialog showed");
                    return;
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(mChosenStartTime);
                MeetingLog.i(TAG,"setCurrentTime " + TimeFormatUtil.getDateTimeTimeString(mChosenStartTime));//1.2
                int sYear = calendar.get(Calendar.YEAR);
                int sMonth = calendar.get(Calendar.MONTH);
                int sDay = calendar.get(Calendar.DAY_OF_MONTH);
                int sHour = calendar.get(Calendar.HOUR_OF_DAY);
                int sMinute = calendar.get(Calendar.MINUTE);
                MeetingLog.i(TAG,"setCurrentTime year:" + sYear + "-" + sMonth +"-" + sDay + " " + sHour + ":" + sMinute);
                mStartTimeDateTimeDialog.updateDate(sYear, sMonth, sDay, sHour, sMinute);
                mStartTimeDateTimeDialog.getDatePicker().updateDate(sYear, sMonth, sDay, sHour, sMinute);
                mStartTimeDateTimeDialog.show();

                break;
            case R.id.end_time:
                if((null != mStartTimeDateTimeDialog && mStartTimeDateTimeDialog.isShowing()) || (null != mEndTimeDialog && mEndTimeDialog.isShowing()) ){
                    MeetingLog.i(TAG,"dialog showed");
                    return;
                }

                Calendar eCalendar = Calendar.getInstance();
                eCalendar.setTimeInMillis(mChosenEndTime);
                int eYear = eCalendar.get(Calendar.YEAR);
                int eMonth = eCalendar.get(Calendar.MONTH);
                int eDay = eCalendar.get(Calendar.DAY_OF_MONTH);
                int eHour = eCalendar.get(Calendar.HOUR_OF_DAY);
                int eMinute = eCalendar.get(Calendar.MINUTE);
                MeetingLog.i(TAG,"setCurrentTime year:" + eYear + "-" + eMonth +"-" + eDay + " " + eHour + ":" + eMinute);
                mEndTimeDialog.updateDate(eYear, eMonth, eDay, eHour, eMinute);
                mEndTimeDialog.getDatePicker().updateDate(eYear, eMonth, eDay, eHour, eMinute);
                mEndTimeDialog.show();
                break;
            default:
                break;
        }
    }

    private void initMeetingInfo() {
        MeetingInfo meetingInfo = MeetingStaticInfo.getCurrentMeeting().getMeetingInfo();

        mTitleEdit.setText(meetingInfo.getTitle());
        mObjectivesEdit.setText(meetingInfo.getTopics());
        mPersonEdit.setText(meetingInfo.getPersons());;
        mChosenStartTime = meetingInfo.getStartTime();
        mChosenEndTime = meetingInfo.getEndTime();

        //set start TimeView
        if(mChosenStartTime !=null && mChosenStartTime > 0){
            // do nothing
        }else {
            mChosenStartTime = System.currentTimeMillis();
        }
        mStartDateText.setText(TimeFormatUtil.getDateString(mChosenStartTime));
        mStartTimeText.setText((TimeFormatUtil.getTimeString(mChosenStartTime)));

        //set end TimeView
        if(mChosenEndTime !=null && mChosenEndTime > 0){
            //do nothing
        }else {
            mChosenEndTime = System.currentTimeMillis() + 2 * 1000 *60 *60;
        }
        mEndDateText.setText(TimeFormatUtil.getDateString(mChosenEndTime));
        mEndTimeText.setText(TimeFormatUtil.getTimeString(mChosenEndTime));

        String lastAddress = MeetingStaticInfo.getLastMeetingLocation();
        mObjectivesEdit.addTextChangedListener(new SpannableTextWatcher(this));
        if (lastAddress != null && meetingInfo.isEmpty()) {
            mAddressEdit.setText(lastAddress);
        }else{
            mAddressEdit.setText(meetingInfo.getAddress());
        }
    }

    @Override
    public void onBackPressed() {
        MeetingLog.i(TAG,"onBackPressed");
        super.onBackPressed();
        showActionMode(false);
    }

    void initDialog(){
        mStartTimeDateTimeDialog = new DateTimeDialog(EditMeetingInfoPageActivity.this);
        mEndTimeDialog = new DateTimeDialog(EditMeetingInfoPageActivity.this);
        mStartTimeDateTimeDialog.getDatePicker().setOnDateChangedListener(new DateTimePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DateTimePicker dateTimePicker, int i, int i1, int i2, int i3, int i4) {
                mStartTimeDateTimeDialog.setTitle(TimeFormatUtil.getDateString(i,i1 + 1,i2,i3,i4));
                MeetingLog.i(TAG,"onDateChanged " + TimeFormatUtil.getDateString(i,i1 + 1,i2,i3,i4));//0.2
            }
        });
        mEndTimeDialog.getDatePicker().setOnDateChangedListener(new DateTimePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DateTimePicker dateTimePicker, int i, int i1, int i2, int i3, int i4) {
                mEndTimeDialog.setTitle(TimeFormatUtil.getDateString(i,i1 + 1,i2,i3,i4));
                MeetingLog.i(TAG,"onDateChanged " + TimeFormatUtil.getDateString(i,i1 + 1,i2,i3,i4));//0.2
            }
        });
        mStartTimeDateTimeDialog.setOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == DialogInterface.BUTTON_POSITIVE){
                    mStartTimeDateTimeDialog.getDatePicker().getY();
                    DateTimePicker datePicker = mStartTimeDateTimeDialog.getDatePicker();
                    int year = datePicker.getYear();
                    int month = datePicker.getMonth();
                    int day = datePicker.getDayOfMonth();
                    int hour= datePicker.getHour();
                    int minute = datePicker.getMinute();

                    Date date = new Date(year-1900,month,day,hour,minute);
                    mChosenStartTime = date.getTime();
                    mStartDateText.setText(TimeFormatUtil.getDateString(mChosenStartTime));
                    mStartTimeText.setText(TimeFormatUtil.getTimeString(mChosenStartTime));

                    MeetingLog.i(TAG,"choose time is " + TimeFormatUtil.getDateTimeTimeString(date.getTime()));
                }
                mStartTimeDateTimeDialog.dismiss();
            }
        });

        mEndTimeDialog.setOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == DialogInterface.BUTTON_POSITIVE){
                    mEndTimeDialog.getDatePicker().getY();
                    DateTimePicker datePicker = mEndTimeDialog.getDatePicker();
                    int year = datePicker.getYear();
                    int month = datePicker.getMonth();
                    int day = datePicker.getDayOfMonth();
                    int hour= datePicker.getHour();
                    int minute = datePicker.getMinute();

                    Date date = new Date(year-1900,month,day,hour,minute);

                    mChosenEndTime = date.getTime();
                    mEndDateText.setText(TimeFormatUtil.getDateString(mChosenEndTime));
                    mEndTimeText.setText(TimeFormatUtil.getTimeString(mChosenEndTime));

                    MeetingLog.i(TAG,"choose time is " + TimeFormatUtil.getDateTimeTimeString(date.getTime()));
                }
                mEndTimeDialog.dismiss();
            }
        });
    }

}
