/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckedTextView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SpinnerPopupDialog;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import cn.tcl.meetingassistant.bean.Meeting;
import cn.tcl.meetingassistant.bean.MeetingDecisionData;
import cn.tcl.meetingassistant.bean.MeetingInfo;
import cn.tcl.meetingassistant.bean.MeetingStaticInfo;
import cn.tcl.meetingassistant.db.MeetingDecisionDBUtil;
import cn.tcl.meetingassistant.db.MeetingInfoDBUtil;
import cn.tcl.meetingassistant.db.OnDoneInsertAndUpdateListener;
import cn.tcl.meetingassistant.log.MeetingLog;
import cn.tcl.meetingassistant.utils.FileUtils;
import cn.tcl.meetingassistant.utils.TimeFormatUtil;
import cn.tcl.meetingassistant.view.AbsMeetingActivity;
import cn.tcl.meetingassistant.view.DateAndTimePickerDialogFragment;
import cn.tcl.meetingassistant.view.FixedEditText;
import cn.tcl.meetingassistant.view.MeetingToast;
import cn.tcl.meetingassistant.view.SelectAttachPopupWindow;
import mst.app.dialog.AlertDialog;
import mst.app.dialog.DateTimeDialog;
import mst.app.dialog.PopupDialog;
import mst.app.dialog.TimePickerDialog;
import mst.preference.MultiSelectListPreference;
import mst.view.menu.BottomWidePopupMenu;
import mst.widget.ActionMode;
import mst.widget.ActionModeListener;
import mst.widget.DatePicker;
import mst.widget.DateTimePicker;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-3.
 * The edit meeting decision page
 */
public class EditDecisionActivity extends AbsMeetingActivity implements View.OnClickListener {

    private String TAG = EditDecisionActivity.class.getSimpleName();

    private FixedEditText mDecisionEdit;
    private EditText mPersonEdit;
    private TextView mDeadLine;
    private ImageView mAddPersonBtn;
    private TextView mSaveBtn;
    private LinearLayout mEditTable;
    private DateAndTimePickerDialogFragment mDialog;

    private MeetingDecisionData mMeetingDecisionData;
    private long mDecisionId = -1;
    public static final String DECISION_ID = "decision_id";
    private int mClickBtnId;
    // the time chosen with dialog
    private long mCurrentChosenTime = 0;
    private final long ONE_DAY = 1000L * 60 * 60 * 24;

    private DateTimeDialog mDateTimeDialog;

    private List<String> mOwnerList;

    private SpinnerPopupDialog mPopMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MeetingLog.i(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        initView();
        initMeetingId();
        initActionMode();
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.decision_edit_saveBtn:
                mClickBtnId = R.id.decision_edit_saveBtn;
                addADecision();
                break;
            case R.id.decision_edit_deadline:
                if(null != mDateTimeDialog && mDateTimeDialog.isShowing()){
                    MeetingLog.i(TAG,"dialog showed");
                    return;
                }
                MeetingLog.i(TAG,"setCurrentTime");
                if(mCurrentChosenTime == 0){
                    mCurrentChosenTime = System.currentTimeMillis() + ONE_DAY;
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(mCurrentChosenTime);
                int mYear = calendar.get(Calendar.YEAR);
                int mMonth = calendar.get(Calendar.MONTH);
                int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                int mMinute = calendar.get(Calendar.MINUTE);

                MeetingLog.i(TAG,"setCurrentTime year:" + mYear + "month:" + mMonth +"day:" + mDay + "hour:" + mHour + "minute:" + mMinute);

                mDateTimeDialog.updateDate(mYear, mMonth, mDay, mHour, mMinute);
                mDateTimeDialog.getDatePicker().updateDate(mYear, mMonth, mDay, mHour, mMinute);
                mDateTimeDialog.show();
                break;
            case R.id.decision_edit_person_btn:
                if(TextUtils.isEmpty(MeetingStaticInfo.getCurrentMeeting().getMeetingInfo().getPersons())){
                    return;
                }

                final String[] personList = getPersonList(MeetingStaticInfo.getCurrentMeeting().getMeetingInfo().getPersons());
                String[] ownerArray = getPersonList(mPersonEdit.getText().toString());
                boolean[] cho = new boolean[personList.length];

                mOwnerList = new ArrayList<>();
                List<String> ownerListTemp = new ArrayList<>();
                if(!TextUtils.isEmpty(mPersonEdit.getText().toString())){
                    Collections.addAll(mOwnerList,ownerArray);
                    Collections.addAll(ownerListTemp,ownerArray);
                }

                for(int i = 0;i< cho.length ;i++){
                    if(ownerListTemp.contains(personList[i])){
                        ownerListTemp.remove(ownerListTemp.indexOf(personList[i]));
                        cho[i] = true;
                    }else {
                        cho[i] = false;
                    }
                }

                if(mPopMenu == null){
                    mPopMenu = new SpinnerPopupDialog(this);
                }

                mPopMenu.setNegativeButton(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                mPopMenu.setMultipleChoiceItems(personList, cho,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if(isChecked){
                                    mOwnerList.add(personList[which]);
                                }else {
                                    mOwnerList.remove(personList[which]);
                                }
                            }
                        });

                mPopMenu.setPositiveButton(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for(String s:mOwnerList){
                            stringBuilder.append(s).append(";");
                        }
                        mPersonEdit.setText(stringBuilder.toString());
                        mPersonEdit.setSelection(mPersonEdit.getText().length());
                    }
                });

                mPopMenu.setTitle(R.string.choose_owner);

                mPopMenu.setCanceledOnTouchOutside(true);
                mPopMenu.show();
                break;
            default:
                break;
        }
    }

    private void initView() {
        setMstContentView(R.layout.activity_edit_decision_page_layout);
        mDecisionEdit = (FixedEditText) findViewById(R.id.decision_edit_text);
        mPersonEdit = (EditText) findViewById(R.id.decision_edit_person);
        mDeadLine = (TextView) findViewById(R.id.decision_edit_deadline);
        mAddPersonBtn = (ImageView) findViewById(R.id.decision_edit_person_btn);
        mSaveBtn = (TextView) findViewById(R.id.decision_edit_saveBtn);
        mEditTable = (LinearLayout) findViewById(R.id.decision_edit_person_deadline);
        mAddPersonBtn.setOnClickListener(this);
        mDeadLine.setOnClickListener(this);
        mSaveBtn.setOnClickListener(this);

        mDateTimeDialog = new DateTimeDialog(EditDecisionActivity.this);
        mDateTimeDialog.getDatePicker().setOnDateChangedListener(new DateTimePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DateTimePicker dateTimePicker, int i, int i1, int i2, int i3, int i4) {
                mDateTimeDialog.setTitle(TimeFormatUtil.getDateString(i,i1 + 1,i2,i3,i4));
                MeetingLog.i(TAG,"onDateChanged " + TimeFormatUtil.getDateString(i,i1 + 1,i2,i3,i4));
            }
        });

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

        mDateTimeDialog.setOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == DialogInterface.BUTTON_POSITIVE){
                    mDateTimeDialog.getDatePicker().getY();
                    DateTimePicker datePicker = mDateTimeDialog.getDatePicker();
                    int year = datePicker.getYear();
                    int month = datePicker.getMonth();
                    int day = datePicker.getDayOfMonth();
                    int hour= datePicker.getHour();
                    int minute = datePicker.getMinute();

                    Date date = new Date(year - 1900,month,day,hour,minute);
                    mDeadLine.setText(TimeFormatUtil.getDateTimeTimeString(date.getTime()));
                    setCurrentChosenTime(date.getTime());
                    MeetingLog.i(TAG,"choose time is " + TimeFormatUtil.getDateTimeTimeString(date.getTime()));
                }
                mDateTimeDialog.dismiss();
            }
        });
        mDateTimeDialog.setCanceledOnTouchOutside(true);

        getToolbar().setNavigationIcon(null);
        getToolbar().setTitle("");
        getToolbar().setVisibility(View.INVISIBLE);
        cleanViews();
        ViewTreeObserver viewTreeObserver = findViewById(android.R.id.content).getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
               showActionMode(true);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            //showActionMode(true);
        }
    }

    private void initActionMode(){
        setupActionModeWithDecor(getToolbar());
        setActionModeListener(new ActionModeListener() {
            @Override
            public void onActionItemClicked(ActionMode.Item item) {
                switch (item.getItemId()) {
                    case ActionMode.NAGATIVE_BUTTON:
                        onBackPressed();
                        break;
                    case ActionMode.POSITIVE_BUTTON:
                        mClickBtnId = R.id.toolbar_btn_confirm;
                        addADecision();
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

    private void cleanViews() {
        mDecisionEdit.setText("");
        mPersonEdit.setText("");
        mDeadLine.setText("");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showActionMode(false);
    }

    private void initData() {
        List<MeetingDecisionData> decisionDatas = MeetingStaticInfo.
                getCurrentMeeting().getMeetingDecisions();
        if(mDecisionId != -1){
            for(MeetingDecisionData meetingDecisionData : decisionDatas){
                if(meetingDecisionData.getId() == mDecisionId){
                    if(meetingDecisionData.getDeadline() == 0){
                        setCurrentChosenTime(System.currentTimeMillis() + ONE_DAY);
                    }
                    setCurrentChosenTime(meetingDecisionData.getDeadline());
                    mMeetingDecisionData = meetingDecisionData;
                    mDecisionEdit.setText(meetingDecisionData.getDecisionInfo());
                    mPersonEdit.setText(meetingDecisionData.getPersons());
                    if(meetingDecisionData.getDeadline() > 0){
                        mDeadLine.setText(TimeFormatUtil.getDateTimeTimeString(meetingDecisionData.getDeadline()));
                    }
                }
            }
        }else {
            setCurrentChosenTime(System.currentTimeMillis() + ONE_DAY);
        }

        MeetingInfo meetingInfo = MeetingStaticInfo.getCurrentMeeting().getMeetingInfo();
        if(TextUtils.isEmpty(meetingInfo.getPersons())){
            mAddPersonBtn.setVisibility(View.GONE);
        }
    }

    private void initMeetingId() {
        Intent intent = getIntent();
        mDecisionId = intent.getLongExtra(DECISION_ID, -1);
        if (mDecisionId != -1) {
            mSaveBtn.setVisibility(View.GONE);
        }
        initData();
    }

    /**
     * add a new decision to data base
     */
    private void addADecision() {
        String s = mDecisionEdit.getTextString();
        if(TextUtils.isEmpty(s.trim())){
            MeetingToast.makeText(this,R.string.enter_decision_content,Toast.LENGTH_SHORT).show();
            return;
        }

        // add a decision
        if (mDecisionId == -1) {
            mMeetingDecisionData = new MeetingDecisionData();
            mMeetingDecisionData.setMeetingId(MeetingStaticInfo.getCurrentMeeting().getId());
            mMeetingDecisionData.setDecisionInfo(mDecisionEdit.getTextString());
            mMeetingDecisionData.setPersons(mPersonEdit.getText().toString());
            if(TextUtils.isEmpty(mDeadLine.getText())){
                mMeetingDecisionData.setDeadline(0L);
            }else {
                mMeetingDecisionData.setDeadline(mCurrentChosenTime);
            }
            OnDoneInsertAndUpdateListener onDoneListener = new OnDoneInsertAndUpdateListener() {

                @Override
                public void onDone(long id) {
                    MeetingLog.i(TAG,"insert a decision id is " + id);
                    if (id > 0) {
                        cleanViews();
                        // update memory data
                        mMeetingDecisionData.setId(id);
                        MeetingStaticInfo.getCurrentMeeting().addDecision(mMeetingDecisionData);
                        MeetingStaticInfo.updateCurrentTime(EditDecisionActivity.this);
                    } else {
                        MeetingStaticInfo.getCurrentMeeting().getMeetingDecisions().
                                remove(mMeetingDecisionData);
                    }
                    if(mClickBtnId == R.id.toolbar_btn_confirm){
                        Toast.makeText(EditDecisionActivity.this, R.string.saved_decision, Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }else {
                        Toast.makeText(EditDecisionActivity.this, R.string.saved_last_one_decision, Toast.LENGTH_SHORT).show();
                    }
                }
            };
            MeetingDecisionDBUtil.insert(this, mMeetingDecisionData, onDoneListener);
        }
        // update a decision
        else{
            mMeetingDecisionData.setMeetingId(MeetingStaticInfo.getCurrentMeeting().getId());
            mMeetingDecisionData.setDecisionInfo(mDecisionEdit.getTextString());
            mMeetingDecisionData.setPersons(mPersonEdit.getText().toString());
            mMeetingDecisionData.setDeadline(mCurrentChosenTime);
            OnDoneInsertAndUpdateListener onDoneInsertAndUpdateListener = new OnDoneInsertAndUpdateListener() {
                @Override
                public void onDone(long id) {
                    MeetingLog.i(TAG,"update a decision num is " + id);
                    if (id > 0) {
                        // update memory data
                        Toast.makeText(EditDecisionActivity.this, R.string.saved_decision, Toast.LENGTH_SHORT).show();
                        MeetingStaticInfo.updateCurrentTime(EditDecisionActivity.this);
                    } else {
                        Toast.makeText(EditDecisionActivity.this, "fail", Toast.LENGTH_SHORT).show();
                    }
                }
            };
            MeetingDecisionDBUtil.update(this, mMeetingDecisionData, onDoneInsertAndUpdateListener);
            onBackPressed();
        }
    }

    private String[] getPersonList(String list){
        List<String> personList = new ArrayList<>();
        int lastPosition = 0;
        for(int i = 0;i < list.length();i++){
            if(list.charAt(i) == ';' || list.charAt(i) == '；'){
                if(lastPosition < i){
                    personList.add(list.substring(lastPosition,i));
                    lastPosition = i + 1;
                }else if(lastPosition == i){
                    lastPosition++;
                }
            }
        }

        if(list.length() >=1 && list.charAt(list.length() -1) !=  ';' && list.charAt(list.length() -1) !=  '；' &&
                lastPosition <= list.length()){
            personList.add(list.substring(lastPosition,list.length()));
        }
        String[] result = new String[personList.size()];
        int i = 0;
        for(String item : personList){
            result[i++] = item;
        }
        return result;
    }

    private void setCurrentChosenTime(long time){
        MeetingLog.i(TAG,"setCurrentChosenTime --> time is " + time);
        this.mCurrentChosenTime = time;
    }

}
